package org.tolven.app;

import java.util.List;

import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.ConfigChange;
import org.tolven.core.entity.Account;


public interface ConfigChangeLocal {
	
	/** Start data migration for a menu structure. This method returns a boolean 
	 * indicating if the the data migration for the menu structure is completed.
	 * @param msId
	 * @return
	 */
	public boolean migrateMenuDataByMenuStructure(long msId);
	
	/** Post a message to JMS queue to start data migration
	 * @param migrateMenuDataMessage
	 */
	public void startDataMigration(MigrateMenuDataMessage migrateMenuDataMessage,char[] userPassword);
	/**
	 * Post a message to rollback migration changes for a ChangeId
	 * @param tmmsg
	 * @param userPassword
	 */
	public void startRollBackMigration(RollbackMigrationMessage tmmsg,char[] userPassword);
	
	/** Module to find config changes for a menu structure
	 * @param ams
	 * @return
	 */
	public List<ConfigChange> findMigrationChanges(AccountMenuStructure ams,boolean skippedForMigration);
	
	/**
	 * Record a configuration change in metadata
	 * @param change
	 * @return
	 */
	public ConfigChange saveConfigChange(ConfigChange change);

	boolean rollBackMigrationChanges(long changeId);
	
	/** Method to find menustrctures that does not migrated to latest configuration
	 * @param account
	 * @return List<AccountMenuStructure>
	 */
	public List<AccountMenuStructure> findMenuStructureToMigrate(Account account);
	
	/** Method to find if there exists any menustructure that does not migrated to latest configuration
	 * @param account
	 * @return booelan
	 */
	public boolean hasMenuStructureToMigrate(Account account);
}
