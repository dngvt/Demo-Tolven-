package org.tolven.app;

import java.io.Serializable;

import org.tolven.doc.bean.TolvenMessage;
/**
 
 */
/** A message used to start data migration for a menustructure.
 * @author skandula
 *
 */
@SuppressWarnings("serial")
public class RollbackMigrationMessage extends TolvenMessage implements Serializable {
	private long changeId;

	public long getChangeId() {
		return changeId;
	}

	public void setChangeId(long changeId) {
		this.changeId = changeId;
	}

	public RollbackMigrationMessage(long changeId,long accountId) {
		this.changeId = changeId;
		this.setAccountId(accountId);
	}
	
	
}
