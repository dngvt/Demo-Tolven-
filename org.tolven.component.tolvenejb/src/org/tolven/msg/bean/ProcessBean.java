package org.tolven.msg.bean;

import java.security.PrivateKey;
import java.util.Date;
import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.tolven.app.MessageProcessorLocal;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.DocProtectionLocal;
import org.tolven.doc.DocumentLocal;
import org.tolven.doc.bean.TolvenMessage;
import org.tolven.doc.entity.DocBase;
import org.tolven.doc.entity.DocXML;
import org.tolven.msg.ProcessLocal;
import org.tolven.msg.TolvenMessageSchedulerLocal;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;

@Stateless()
@Local(ProcessLocal.class)
public class ProcessBean implements ProcessLocal {
    
    private static String messageProcessorNames[];
    private MessageProcessorLocal[] messageProcessors;

    //@PersistenceContext
    //private EntityManager em;
    
    @EJB
    private DocumentLocal documentBean;

    @EJB
    private DocProtectionLocal docProtectionBean;

    @EJB
    private TolvenPropertiesLocal propertyBean;

    @EJB
    protected TolvenMessageSchedulerLocal tmSchedulerBean;
    
    public final static String PROCESSOR_NAME = "processorJNDI";
	public final static String PROPERTY_FILE_NAME = "Evaluator.properties"; 
    
	private static Logger logger = Logger.getLogger(ProcessBean.class);

	/**
     * Find the list of message evaluators we are using.
     */
    static void initializeMessageProcessors() {
        try {
            if (messageProcessorNames == null) {
                Properties properties = new Properties();
                properties.load(ProcessBean.class.getResourceAsStream(PROPERTY_FILE_NAME));
                String processorNames = properties.getProperty(PROCESSOR_NAME);
                logger.debug("Message processors: " + processorNames);
                messageProcessorNames = processorNames.split(",");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error initializing message processors", ex);
        }
    }

    /**
     * Process document with docId associated with accountUser
     * 
     * @param id
     * @param now
     */
    @Override
    public void processDocument(long id, Date now) {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if(accountUser == null) {
            throw new RuntimeException("AccountUser not find in TolvenRequest");
        }
        DocBase doc = documentBean.findDocument(id);
        long accountId = accountUser.getAccount().getId();
        if (doc == null || doc.getAccount().getId() != accountId) {
            throw new RuntimeException("Document: " + id + " not found in account: " + accountId);
        }
        byte[] payload = getDecryptedContent(doc, accountUser);
        processTolvenMessage(payload, doc.getMediaType(), null, accountId, accountUser.getUser().getId(), now);
        documentBean.finalizeDocument(id);
    }

    /**
     * Process document with docId associated with the current account in context
     * 
     * @param id
     * @param now
     */
    @Override
    public void processXMLDocument(long id, Date now) {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if(accountUser == null) {
            throw new RuntimeException("AccountUser not find in TolvenRequest");
        }
        DocXML doc = (DocXML) documentBean.findXMLDocument(id);
        long accountId = accountUser.getAccount().getId();
        if (doc == null || doc.getAccount().getId() != accountId) {
            throw new RuntimeException("Document: " + id + " not found in account: " + accountId);
        }
        byte[] payload = getDecryptedContent(doc, accountUser);
        processTolvenMessage(payload, doc.getMediaType(), doc.getXmlNS(), accountId, accountUser.getUser().getId(), now);
        documentBean.finalizeDocument(id);
    }
    
    private byte[] getDecryptedContent(DocBase doc, AccountUser accountUser) {
        String keyAlgorithm = propertyBean.getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
        PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);
        String decryptedContent = docProtectionBean.getDecryptedContentString(doc, accountUser, userPrivateKey);
        byte[] payload = null;
        if(decryptedContent != null) {
            try {
                payload = decryptedContent.getBytes("UTF-8");
            } catch (Exception e) {
                throw new RuntimeException("Could not convert decrypted contents to bytes");
            }
        }
        return payload;
    }

    @Override
    public long processMessage(byte[] payload, String mediaType, String xmlns, long accountId, long userId, Date now) {
        return processTolvenMessage(payload, mediaType, xmlns, accountId, userId, now);
    }
    
    private long processTolvenMessage(byte[] payload, String mediaType, String xmlns, long accountId, long userId, Date now) {
        /*
        org.hibernate.Session hibernateSession = (org.hibernate.Session) em.getDelegate();
        org.hibernate.SessionFactory hibernateFactory = hibernateSession.getSessionFactory();
        Statistics stats = hibernateFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();
        */
        TolvenMessage tm = new TolvenMessage();
        tm.setAccountId(accountId);
        tm.setAuthorId(userId);
        tm.setXmlNS(xmlns);
        tm.setPayload(payload);
        tm.setMediaType(mediaType);
        tm.setDecrypted(true);
        dispatchToMessageProcessors(tm, now);
        //printHibernateStatistics(stats);
        //stats.setStatisticsEnabled(false);
        return tm.getDocumentId();
    }
/*
    private void printHibernateStatistics(Statistics stats) {
        try {
            StringBuffer qBuff = new StringBuffer();
            File qFile = new File("/tmp/query.csv");
            if (!qFile.exists()) {
                qFile.createNewFile();
                qBuff.append("query");
                qBuff.append("|");
                qBuff.append("ExecutionMinTime");
                qBuff.append("|");
                qBuff.append("ExecutionMaxTime");
                qBuff.append("|");
                qBuff.append("ExecutionAvgTime");
                qBuff.append("|");
                qBuff.append("ExecutionCount");
                qBuff.append("|");
                qBuff.append("ExecutionRowCount");
                qBuff.append("\n");
            }
            List<String> qLines = FileUtils.readLines(qFile);
            printHibernateQueryStatistics(stats, qBuff);
            qLines.add(qBuff.toString());
            FileUtils.writeLines(qFile, qLines);
            StringBuffer eBuff = new StringBuffer();
            File eFile = new File("/tmp/entity.csv");
            if (!eFile.exists()) {
                eFile.createNewFile();
                eBuff.append("entity");
                eBuff.append("|");
                eBuff.append("FetchCount");
                eBuff.append("|");
                eBuff.append("InsertCount");
                eBuff.append("|");
                eBuff.append("UpdateCount");
                eBuff.append("|");
                eBuff.append("DeleteCount");
                eBuff.append("|");
                eBuff.append("LoadCount");
                eBuff.append("\n");
            }
            List<String> eLines = FileUtils.readLines(eFile);
            printHibernateEntityStatistics(stats, eBuff);
            eLines.add(eBuff.toString());
            FileUtils.writeLines(eFile, eLines);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printHibernateQueryStatistics(Statistics stats, StringBuffer buff) {
        try {
            String[] queries = stats.getQueries();
            for (int i = 0; i < queries.length; i++) {
                String query = queries[i];
                org.hibernate.stat.QueryStatistics qStats = stats.getQueryStatistics(query);
                buff.append(query);
                buff.append("|");
                buff.append(qStats.getExecutionMinTime());
                buff.append("|");
                buff.append(qStats.getExecutionMaxTime());
                buff.append("|");
                buff.append(qStats.getExecutionAvgTime());
                buff.append("|");
                buff.append(qStats.getExecutionCount());
                buff.append("|");
                buff.append(qStats.getExecutionRowCount());
                if (i < queries.length - 1) {
                    buff.append("\n");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printHibernateEntityStatistics(Statistics stats, StringBuffer buff) {
        try {
            String[] entities = stats.getEntityNames();
            for (int i = 0; i < entities.length; i++) {
                String entity = entities[i];
                org.hibernate.stat.EntityStatistics eStats = stats.getEntityStatistics(entity);
                if (eStats.getFetchCount() + eStats.getInsertCount() + eStats.getUpdateCount() + eStats.getDeleteCount() + eStats.getLoadCount() > 0) {
                    buff.append(entity);
                    buff.append("|");
                    buff.append(eStats.getFetchCount());
                    buff.append("|");
                    buff.append(eStats.getInsertCount());
                    buff.append("|");
                    buff.append(eStats.getUpdateCount());
                    buff.append("|");
                    buff.append(eStats.getDeleteCount());
                    buff.append("|");
                    buff.append(eStats.getLoadCount());
                    if (i < entities.length - 1) {
                        buff.append("\n");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
*/
    public void dispatchToMessageProcessors(TolvenMessage tm, Date now) {
    	initializeMessageProcessors();
    	for (MessageProcessorLocal messageProcessor : getMessageProcessors() ) {
			logger.debug("dispatching to: " + messageProcessor);
    		messageProcessor.process( tm, now );
    	}
        tmSchedulerBean.delete(tm);
    }

    private MessageProcessorLocal[] getMessageProcessors() {
        if(messageProcessors == null) {
            try {
                messageProcessors = new MessageProcessorLocal[messageProcessorNames.length];
                InitialContext ctx = new InitialContext();
                for (int i = 0; i < messageProcessorNames.length; i++) {
                    String messageProcessor = messageProcessorNames[i];
                    Object obj = ctx.lookup(messageProcessor);
                    messageProcessors[i] = (MessageProcessorLocal) obj;
                }
            } catch (Exception ex) {
                throw new RuntimeException("Error initializing message processors", ex);
            }
        }
        return messageProcessors;
    }
}
