/*
 * Copyright (C) 2009 Tolven Inc

 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;  
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 *
 * Contact: info@tolvenhealth.com 
 *
 * @author Joseph Isaac
 */
package org.tolven.msg.bean;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.interceptor.Interceptors;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.tolven.app.ConfigChangeLocal;
import org.tolven.app.MigrateMenuDataMessage;
import org.tolven.app.RollbackMigrationMessage;
import org.tolven.gatekeeper.client.interceptor.QueueAuthenticationInterceptor;
import org.tolven.gatekeeper.client.interceptor.QueueSessionInterceptor;
import org.tolven.msg.QueueTolvenRequestInterceptor;
import org.tolven.util.ExceptionFormatter;

@MessageDriven
@Interceptors({
        QueueSessionInterceptor.class,
        QueueAuthenticationInterceptor.class,
        QueueTolvenRequestInterceptor.class })
public class MigrationMDB implements MessageListener {

    private static Logger logger = Logger.getLogger(MigrationMDB.class);

    @EJB
    private ConfigChangeLocal changeLocal;

    @Resource
    private MessageDrivenContext ctx;

    @Override
    public void onMessage(Message msg) {
        String msgId = null;
        try {
            msgId = msg.getJMSMessageID();
            if (((ObjectMessage) msg).getObject() instanceof MigrateMenuDataMessage) {
                MigrateMenuDataMessage tmmsg = (MigrateMenuDataMessage) ((ObjectMessage) msg).getObject();
                // Keep queuing work until there is no more to do
                if (changeLocal.migrateMenuDataByMenuStructure(tmmsg.getMenustructureId())) {
                    //the userPassword can be null now. It is only needed for the first time calling migration
                    changeLocal.startDataMigration(tmmsg, null);
                }
            }
            if (((ObjectMessage) msg).getObject() instanceof RollbackMigrationMessage) {
                RollbackMigrationMessage tmmsg = (RollbackMigrationMessage) ((ObjectMessage) msg).getObject();
                // Keep queuing work until there is no more to do
                if (changeLocal.rollBackMigrationChanges(tmmsg.getChangeId())) {
                    //the userPassword can be null now. It is only needed for the first time calling migration
                    changeLocal.startRollBackMigration(tmmsg, null);
                }
            } else {
                throw new RuntimeException("Message is not ObjectMessage - rejected " + msg.getJMSMessageID());
            }

        } catch (Exception e) {
            ctx.setRollbackOnly();
            String errorString = ExceptionFormatter.toSimpleString(e, "\n");
            logger.error("Message " + msgId + " failed:\n" + errorString + "\nMessage will be rolled back to the Queue and Read/Consumed again");
            throw new RuntimeException(e);
        }
    }

}
