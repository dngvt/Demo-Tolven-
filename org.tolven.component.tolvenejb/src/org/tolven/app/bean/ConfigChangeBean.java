/*
 * Copyright (C) 2012 Tolven Inc

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

package org.tolven.app.bean;

import java.io.InputStream;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.tolven.app.ConfigChangeLocal;
import org.tolven.app.MigrateMenuDataMessage;
import org.tolven.app.MigrationAdapterLocal;
import org.tolven.app.RollbackMigrationMessage;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.ConfigChange;
import org.tolven.app.entity.DataMigrationLog;
import org.tolven.app.entity.MSColumn;
import org.tolven.app.entity.MenuData;
import org.tolven.core.AccountDAOLocal;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.RuleQueueLocal;
import org.tolven.naming.QueueContext;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;

/** Session bean that handles data migration
 * @author skandula
 *
 */
@Local(ConfigChangeLocal.class)
@Stateless

public class ConfigChangeBean implements ConfigChangeLocal {
	public static int MIGRATE_LIMIT = 100;
	public final static String ADAPTER_PROPERTY = "migrationAdapters";
	private final String QUEUE_ID = "queue/rule";
	@PersistenceContext
	private EntityManager em;

	@Resource(name = QUEUE_ID)
	private Queue ruleQueue;
	
	@EJB
	private AccountDAOLocal daoLocal;

	@Resource(name = "jms/JmsXA")
	private ConnectionFactory connectionFactory;
	
	@EJB 
	private TolvenPropertiesLocal propertyBean;
	
	@EJB
	private RuleQueueLocal ruleQueueLocal;
	private Map<String,MigrationAdapterLocal> migrationAdapters;

	Logger logger = Logger.getLogger(this.getClass());

	public Map<String, MigrationAdapterLocal> getMigrationAdapters() {
		return migrationAdapters;
	}

	public void setMigrationAdapters(Map<String, MigrationAdapterLocal> migrationAdapters) {
		this.migrationAdapters = migrationAdapters;
	}

	public EntityManager getEm() {
		return em;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}
	/*
	 * (non-Javadoc)
	 * @see org.tolven.app.ConfigChangeLocal#startDataMigration(org.tolven.app.MigrateMenuDataMessage, char[])
	 */
	@Override
	public void startDataMigration(MigrateMenuDataMessage msg,char[] userPassword){
		Connection connection = null;
        try {
        	hasAccountUserForQueueProcessing(msg.getAccountId(),userPassword);
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(ruleQueue);
            ObjectMessage message = session.createObjectMessage(msg);
            messageProducer.send(message);	         
        } catch (JMSException ex) {
            throw new RuntimeException("Failed to send payload to queue: queue/migrateMenuData", ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                throw new RuntimeException("Failed to close connection to queue : + queue/migrateMenuData", e);
            }
        }
	}
	
	
	
	/**
	 * Method to find configuration change for a MenuData
	 */
	@SuppressWarnings("unchecked")
	private List<ConfigChange> findMigrationChanges(MenuData md,boolean skippedForMigration) {
		if(md.getLatestConfig() == null)
			md.setLatestConfig(new Long(0));
		//find the changes in ascending order of the creation date
		Query query = em.createQuery("SELECT change FROM ConfigChange change WHERE change.menuStructure = :ms" +
				" and change.id > :latest and change.skipMigration = :skipMigration ORDER BY change.createdDate asc");
		query.setParameter("ms", md.getMenuStructure());
		query.setParameter("latest", md.getLatestConfig());
		query.setParameter("skipMigration", skippedForMigration);
		List<ConfigChange> items = query.getResultList();
		return items;
	}
	
	/**
	 * Method to find configuration change for a MenuStructure
	 */
	@SuppressWarnings("unchecked")
	public List<ConfigChange> findMigrationChanges(AccountMenuStructure ams,boolean skippedForMigration) {
		//find the changes in ascending order of the creation date
		
		Query query = null;
		//include condition for latestConfig only if menustructure has a value for latestConfig
		if(ams.getLatestConfig() != null){
			/*if(skippedForMigration){ 
				// when migration is skipped the old columns should always be included in the query in 
				//MenuBean.findMenuDataByColumns(). this method help finding the old column names
				query = em.createQuery("SELECT change FROM ConfigChange change WHERE change.menuStructure = :ms " +
						"and change.id > :latest or change.skipMigration = :skipMigration ORDER BY change.createdDate asc");
				query.setParameter("latest", ams.getLatestConfig());
			}else{*/
				query = em.createQuery("SELECT change FROM ConfigChange change WHERE change.menuStructure = :ms " +
					"and change.id > :latest and change.skipMigration = :skipMigration ORDER BY change.createdDate asc");
				query.setParameter("latest", ams.getLatestConfig());
			//}
		}		
		else{
			query = em.createQuery("SELECT change FROM ConfigChange change WHERE change.menuStructure = :ms " +
					"and change.skipMigration = :skipMigration ORDER BY change.createdDate asc");
		}
		query.setParameter("ms", ams);
		query.setParameter("skipMigration", skippedForMigration);
		List<ConfigChange> items = query.getResultList();
		return items;
	}
	/**
	 * 
	 */
	@Override
	public boolean migrateMenuDataByMenuStructure(long msId){
		AccountMenuStructure ams = findMenuStructure(msId);
		String limit = propertyBean.getProperty("tolven.datamigration.batch.limit");
		if( limit != null && NumberUtils.isNumber(limit)){
			MIGRATE_LIMIT = Integer.parseInt(limit);
		}
		try{
			//lookup the migration adapter if configured
			initializeMigrationAdapters(ams.getPath());
			List<MenuData> items = findMenuDataForMigrate(ams,MIGRATE_LIMIT);
			if(items != null && items.size() >0){
				//lock the account for data migration
				ams.getAccount().setDataMigrationInProgress(true);
			}
			logger.info("Migrating menu data with batch size: "+items.size());
			for(MenuData md:items)
				migrateMenuData(md,ams.getPath());
			if (items.size() >= MIGRATE_LIMIT) {
				return true;
			}
		}catch (Exception e) {
			 throw new RuntimeException( "Exception migrating (" + ams + ") ", e);
		}
		//update menustructure to indicate migration is completed
		logger.info("Data migration done for: "+ams);
		Query query = getEm().createQuery("update AccountMenuStructure ams set ams.latestConfig =(select max(cc.id) from ConfigChange cc where cc.menuStructure=:ams )");
		query.setParameter("ams", ams);
		query.executeUpdate();
		ams.getAccount().setDataMigrationInProgress(false);
		return false;
	}
	
	
	/** Initialize the migration adapters configured
	 * @param msPath
	 */
	private void initializeMigrationAdapters(String msPath) {
		try {			
            if (getMigrationAdapters()==null) {
                Properties properties = new Properties();
                String propertyFileName = this.getClass().getSimpleName()+".properties"; 
                InputStream is = this.getClass().getResourceAsStream(propertyFileName);
                String adapters = null;
                if (is!=null) {
                    properties.load(is);
                    adapters = properties.getProperty(ADAPTER_PROPERTY);
                    is.close();
                }
                this.migrationAdapters = new HashMap<String,MigrationAdapterLocal>();
                if (adapters!=null && adapters.length()>0) {                	
                    String names[] = adapters.split(",");
                    for (String name : names) {
                        String params[] = name.split("=");
                        if(params.length == 2){
                        	InitialContext ctx = new InitialContext();
                        	migrationAdapters.put(params[0], (MigrationAdapterLocal)ctx.lookup(params[1]));
                        }
                        
                    }
                } 
            }
        } catch (Exception e) {
            throw new RuntimeException( "Error initializing MenuData Migration Adapters", e);
        }
		
	}

	/** Method to migrate config changes for MenuData item. It first records all the changes 
	 * in a temporary map and then runs the tranformations at once.
	 * 
	 * This method will use MigrationAdapters if found any
	 */
	private boolean migrateMenuData(MenuData md,String msPath){
		if(logger.isDebugEnabled())
			logger.debug(" Adapters found for MenuStructure:"+msPath+" "+getMigrationAdapters().get(msPath));
		
		List<ConfigChange> changes = findMigrationChanges(md,false);
		
		Map<String,Object> trackChanges = new HashMap<String, Object>();
		//start recording changes per menustructure
		for(ConfigChange change:changes){
			recordMenuDataChanges(md, change,trackChanges);
		}	
		if(logger.isDebugEnabled())
			logger.debug("recorded changes for "+md+"  "+trackChanges);
		if(getMigrationAdapters().get(msPath) != null){
			if(logger.isDebugEnabled())
				logger.debug(" Adapters found for MenuStructure:"+msPath+" "+getMigrationAdapters().get(msPath));
				//Use the migration adapther if found
			getMigrationAdapters().get(msPath).execute(md,trackChanges);
		}else{
			if(logger.isDebugEnabled())
				logger.debug(" No adapters found for MenuStructure:"+msPath);
			transformMenuData(md,trackChanges);
		}
		return true;
	}
	
	
	
	/** Method to record the tranformation for each ConfigChange it found for the MenuData
	 * @param md
	 * @param change
	 * @param trackChanges
	 * @return
	 */
	public boolean recordMenuDataChanges(MenuData md,ConfigChange change,Map<String,Object> trackChanges){
		try{
			if(md.getField(change.getOldLocation()) == null && md.getField(change.getNewLocation()) == null){
				//no data to migrate; move on to next item
				return true;
			}
			if(logger.isDebugEnabled())
				logger.debug("Record: Change-"+change+" for MenuData:"+md);
			DataMigrationLog log = new DataMigrationLog(md.getId(),change,new Date());
			if(change.getOldLocation().equalsIgnoreCase(MSColumn.EXTENDED_FIELD)){ //move from extended field
				log.setHeading(change.getNewHeading());
				if(md.getExtendedField(change.getNewHeading()) != null)
					log.setOldValue(md.getExtendedField(change.getNewHeading()).toString());
				if(md.getExtendedField(change.getHeading()) != null)
					log.setNewValue(md.getExtendedField(change.getHeading()).toString());
				//logger.info("tracking extendedfield : MenuData-"+md+"   "+change.getNewHeading()+"="+md.getExtendedField(change.getHeading()));
				trackChanges.put(change.getNewHeading(), md.getExtendedField(change.getHeading()));
			} else{				
				log.setHeading(change.getHeading());
				if(md.getField(change.getHeading()) != null)
					log.setOldValue(md.getField(change.getHeading()).toString());
				if(md.getInternalField(change.getOldLocation()) != null)
					log.setNewValue(md.getInternalField(change.getOldLocation()).toString());
				//logger.info("tracking internalfield: MenuData-"+md+"   "+change.getHeading()+"="+md.getInternalField(change.getOldLocation()));
				trackChanges.put(change.getHeading(), md.getInternalField(change.getOldLocation()));
				
			}
			//if there is no data to migrate do not create the log
			if(log.getOldValue() != null || log.getNewValue() != null)
				em.persist(log);
			//TODO skandula
			//clean up the extended fields when possible
			if(!change.getOldLocation().equalsIgnoreCase(MSColumn.EXTENDED_FIELD) 
					&& !change.getOldLocation().equalsIgnoreCase(MSColumn.COMPUTED_FIELD))
				md.setField(change.getOldLocation(), null);

		}catch(Exception e){
			logger.error("Error migrating MenuData:"+md+" with ConfigChange:"+change, e);
			throw new RuntimeException("Error migrating MenuData:"+md+" with ConfigChange:"+change, e);
		}
		return true;
	}
	
	/** This method moves the data in to new targetted fields of MenuData.
	 * @param md
	 * @param changes
	 * @return
	 */
	//method need to be public for unit tests
	public void transformMenuData(MenuData md,Map<String,Object> changes){
		try{
			for(String field:changes.keySet()){
				if(changes.get(field) != null)	{			
					md.setField(field, changes.get(field));	
					logger.info("In Transform:"+md+" setting  field  "+field+" = "+changes.get(field));
				}
			}
			md.setLatestConfig(md.getMenuStructure().getAccountMenuStructure().getLatestConfig());
		}catch (Exception e) {
			logger.error("Error in transforming "+md,e);
		}		
	}
	
	/** module to look up the menudata that need to be migrated
	 * @param MenuStructure 
	 * @param limit
	 * @return List<MenuData>
	 */
	private List<MenuData> findMenuDataForMigrate(AccountMenuStructure ams,int limit) {
		if(ams.getLatestConfig() == null)
			return new ArrayList<MenuData>();
    	Query query = em.createQuery("SELECT md FROM MenuData md WHERE md.account = :account " 
    			+"AND (md.deleted is null OR md.deleted = false) "                
    			+"AND md.menuStructure = :ams "
    			+"AND (md.latestConfig  < :configChange or md.latestConfig is NULL )") ;
    	 query.setParameter("account", ams.getAccount());
     	 query.setParameter("ams", ams);
     	 query.setMaxResults(limit);
     	 query.setParameter("configChange", ams.getAccountMenuStructure().getLatestConfig());
     	 return query.getResultList();
    }
	
	
    private AccountMenuStructure findMenuStructure( long msId) {
       return em.find(AccountMenuStructure.class, msId);
    }
    /*
     * Method to save new configChange
     */
    @Override
    public ConfigChange saveConfigChange(ConfigChange change){
    	em.persist(change);
    	return change;
    }

	@Override
	public boolean rollBackMigrationChanges(long changeId) {
		List<DataMigrationLog> changes = findMigrationChangeLog(changeId);
		for(DataMigrationLog change:changes){
			
			MenuData md = em.find(MenuData.class, change.getMenuData());
			logger.info("MenuData :"+change.getMenuData()+" field "+change.getHeading()+"  will be changed to  "+ change.getOldValue());
			
			if(change.getChange().getOldLocation().equalsIgnoreCase(MSColumn.EXTENDED_FIELD)){
				md.setExtendedField(change.getHeading(), change.getOldValue());
				logger.info("Setting extended field:"+change.getHeading()+" to " +change.getOldValue()+" for MenuData:"+md);				
			}
			else{
				logger.info("Setting field:"+change.getHeading()+" to " +change.getOldValue()+" for MenuData:"+md);				
				md.setField(change.getHeading(), change.getOldValue());
			}
			change.setRolledback(true);
		}
		if(changes.size() >= MIGRATE_LIMIT){
			return true;
		}
		 return false;
	}
	//need to be public for unit tests
	public List<DataMigrationLog> findMigrationChangeLog(long changeId){
		Query query = em.createQuery("SELECT log FROM DataMigrationLog log WHERE log.change = :change" 
    			+" AND (log.rolledback is null OR log.rolledback = false)  and log.oldValue is not null order by log.id desc") ;
		query.setParameter("change", em.find(ConfigChange.class, changeId));
		query.setMaxResults(MIGRATE_LIMIT);		
		return (List<DataMigrationLog>) query.getResultList();
	}
	

	@Override
	public void startRollBackMigration(RollbackMigrationMessage tmmsg,char[] userPassword) {
		Connection connection = null;
        try {
        	hasAccountUserForQueueProcessing(tmmsg.getAccountId(),userPassword);
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(ruleQueue);
            ObjectMessage message = session.createObjectMessage(tmmsg);
            messageProducer.send(message);	         
        } catch (JMSException ex) {
            throw new RuntimeException("Failed to send payload to queue: queue/migrateMenuData", ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                throw new RuntimeException("Failed to close connection to queue : + queue/migrateMenuData", e);
            }
        }
	}
	
	
	/** Method to find menustrctures that does not migrated to latest configuration
	 * @param account
	 * @return
	 */
	@Override
	public List<AccountMenuStructure> findMenuStructureToMigrate(Account account){
		/*select * from app.menu_structure ms,app.config_change cc where 
		cc.menustructure_id = ms.id 
		and cc.id > ms.latestconfig and ms.account_id = 8003*/
		Query query = getEm().createQuery("select distinct(am) FROM AccountMenuStructure am, ConfigChange cc WHERE cc.menuStructure = am " +
				"and cc.id > am.latestConfig and cc.skipMigration = :skipMigration and am.account = :account");
		query.setParameter("account", account );
		query.setParameter("skipMigration", false);
		return (List<AccountMenuStructure>)query.getResultList();
	}
	/** Method to find if there exists any menustructure that does not migrated to latest configuration.
	 * If no menu structure found it sets the flag 'DataMigrationInProgress' to false on the account
	 * @param account
	 * @return
	 */
	@Override
	public boolean hasMenuStructureToMigrate(Account account){
		/*select * from app.menu_structure ms,app.config_change cc where 
		cc.menustructure_id = ms.id 
		and cc.id > ms.latestconfig and ms.account_id = 8003*/
		Query query = getEm().createQuery("select count(am) FROM AccountMenuStructure am, ConfigChange cc WHERE cc.menuStructure = am " +
				"and cc.id > am.latestConfig and cc.skipMigration =:skipMigration and am.account = :account");
		query.setParameter("account", account );
		query.setParameter("skipMigration", false);
		int count = Integer.parseInt(query.getSingleResult().toString());
		if(count > 0)
			return true;
		else{
			return false;
		}
	}
	
	/**
	 * Method to check if the account user exists for processing messages on Queue. An account user with account private
	 * key should exists to be able decrypt any encrypted data.If not found the user will invited to the account
	 */
	private void hasAccountUserForQueueProcessing(long accountId,char[] userPassword){
		QueueContext qc = ruleQueueLocal.getQueueContext();
		AccountUser accountUser = daoLocal.findAccountUser( qc.getUser(), accountId );
	    if (accountUser==null) {
	    	TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
	        String principal = (String) sessionWrapper.getPrincipal();
	    	AccountUser activeAccountUser = daoLocal.findAccountUser(principal, accountId);
	    	if(userPassword == null) {
	    	    throw new RuntimeException("A user password is required to invite other users to an account");
	    	}
	    	daoLocal.inviteAccountUser(activeAccountUser.getAccount(), activeAccountUser, qc.getUser(),qc.getRealm(), getUserPrivateKey(), userPassword, new Date(), false, false);
	    }
	}
	
	private PrivateKey getUserPrivateKey() {
		String keyAlgorithm = propertyBean.getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
        return sessionWrapper.getUserPrivateKey(keyAlgorithm);
    }
}

