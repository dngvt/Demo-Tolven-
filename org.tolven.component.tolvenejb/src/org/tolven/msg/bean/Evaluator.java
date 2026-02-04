/*
 *  Copyright (C) 2007 Tolven Inc 
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * Contact: info@tolvenhealth.com
 */
package org.tolven.msg.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.tolven.app.ConfigChangeLocal;
import org.tolven.app.JMSMessageProcessorLocal;
import org.tolven.app.MessageProcessorLocal;
import org.tolven.app.MigrateMenuDataMessage;
import org.tolven.app.RollbackMigrationMessage;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.doc.bean.TolvenMessage;
import org.tolven.gatekeeper.client.interceptor.QueueAuthenticationInterceptor;
import org.tolven.gatekeeper.client.interceptor.QueueSessionInterceptor;
import org.tolven.msg.QueueTolvenRequestInterceptor;
import org.tolven.msg.TolvenMessageSchedulerLocal;
import org.tolven.util.ExceptionFormatter;
/**
 * Evaluate new documents that have arrived. We do this after a document is safely stored and in the
 * active state. In most cases, we simple create/update indexes be we can do other things like create new documents.
 * However, in most cases, we must limit what a document affects to the account in which the document lives.
 * @author John Churin
 */

@MessageDriven
@Interceptors({
        QueueSessionInterceptor.class,
        QueueAuthenticationInterceptor.class,
        QueueTolvenRequestInterceptor.class })
public class Evaluator implements MessageListener {
    
	protected @EJB TolvenPropertiesLocal propertyBean;
    protected @EJB TolvenMessageSchedulerLocal tmSchedulerBean;

	protected @Resource MessageDrivenContext ctx;
	private @EJB ConfigChangeLocal changeLocal;
    private String messageProcessors[];
    
    public final static String PROCESSOR_NAME = "processorJNDI";
	private Logger logger = Logger.getLogger(getClass());

    /**
     * Find the list of message evaluators we are using.
     */
    public void initializeMessageProcessors() {
    	try {
			if (messageProcessors==null) {
				Properties properties = new Properties();
				String propertyFileName = getClass().getSimpleName()+".properties"; 
				properties.load(getClass().getResourceAsStream(propertyFileName));
				String processorNames = properties.getProperty(PROCESSOR_NAME);
				List<String> arr = new ArrayList<String>();
				for(String processorName : processorNames.split(",")) {
				    arr.add(processorName);
				}
				messageProcessors = new String[arr.size()];
				arr.toArray(messageProcessors);
			}
		} catch (Exception e) {
			throw new RuntimeException( "Error initializing message processors", e);
		}
    }
    
    /**
     * Given a message, ask each message process requesting to process that message (there is usually just one) to process the message.
     * @param message
     */
    public void dispatchToMessageProcessors( ObjectMessage message ) throws Exception {
    	initializeMessageProcessors();
        if (messageProcessors.length==0) throw new RuntimeException( "No message processor found for " + message.getClass().getName());
    	Object payload = message.getObject();
    	TolvenMessage tm = tmSchedulerBean.unwrapTolvenMessage(message);
    	if (tm != null) {
            payload = tm;
        }
		Date now = new Date();
        InitialContext ctx = new InitialContext();
    	for (String messageProcessor : messageProcessors ) {
    	    Object obj = ctx.lookup(messageProcessor);
    	    if(obj instanceof JMSMessageProcessorLocal) {
    	        JMSMessageProcessorLocal mpBean = (JMSMessageProcessorLocal) obj;
                mpBean.process( message, now );
            } else {
                MessageProcessorLocal mpBean = (MessageProcessorLocal) obj;
                long start = 0;
                if (logger.isDebugEnabled()) {
                    start = System.currentTimeMillis();
                    logger.debug("[Start_Processor] " + messageProcessor + " thread: " + Thread.currentThread().getId());
                }
                try {
                    mpBean.process(payload, now);
                } finally {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[End_Processor] " + messageProcessor + "[" + (System.currentTimeMillis() - start) + " ms]" + " thread: " + Thread.currentThread().getId());
                    }
                }
    	}
    	}
    	if(tm != null) {
            tmSchedulerBean.delete(tm);
    	}
    }

    /**
     * Process one message from the Rule queue
     */
    public void onMessage(Message msg)  {
        long start = 0;
        String jmsMessageId = null;
        String messageLogId = null;
        if (logger.isDebugEnabled()) {
            start = System.currentTimeMillis();
            try {
                jmsMessageId = msg.getJMSMessageID();
            } catch (JMSException ex) {
                throw new RuntimeException("Could not get JMSMessageID", ex);
            }
            messageLogId = "MsgID: " + jmsMessageId;
            logger.debug("[Start] " + messageLogId);
        }
		try {
	    	if (((ObjectMessage)msg).getObject() instanceof MigrateMenuDataMessage) {
				MigrateMenuDataMessage tmmsg = (MigrateMenuDataMessage) ((ObjectMessage)msg).getObject();
				// Keep queuing work until there is no more to do
				if (changeLocal.migrateMenuDataByMenuStructure(tmmsg.getMenustructureId())) {
					//the userPassword can be null now. It is only needed for the first time calling migration
					changeLocal.startDataMigration(tmmsg,null);
				}
			}if (((ObjectMessage)msg).getObject() instanceof RollbackMigrationMessage) {
				RollbackMigrationMessage tmmsg = (RollbackMigrationMessage) ((ObjectMessage)msg).getObject();
				// Keep queuing work until there is no more to do
				if (changeLocal.rollBackMigrationChanges(tmmsg.getChangeId())) {
					//the userPassword can be null now. It is only needed for the first time calling migration
					changeLocal.startRollBackMigration(tmmsg,null);
				}
			}else if (msg instanceof ObjectMessage ) {
				dispatchToMessageProcessors( (ObjectMessage)msg );
			} else {
		    	throw new RuntimeException( "Message is not typed - rejected " + msg.getJMSMessageID());
			}
			
		} catch (Exception e) {
			ctx.setRollbackOnly();
			String errorString = ExceptionFormatter.toSimpleString(e, "\n");
			logger.error( "Message " + jmsMessageId + " failed:\n" + errorString + "\nMessage will be rolled back to the Queue and Read/Consumed again");
			throw new RuntimeException(e);
		} finally {
            if (logger.isDebugEnabled()) {
                logger.info("[End] " + messageLogId + "[" + (System.currentTimeMillis() - start) + " ms]");
            }
		}
	}
}
