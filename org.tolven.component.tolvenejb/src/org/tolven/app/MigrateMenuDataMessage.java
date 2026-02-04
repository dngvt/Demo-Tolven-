package org.tolven.app;

import java.io.Serializable;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.doc.bean.TolvenMessage;
/**
 
 */
/** A message used to start data migration for a menustructure.
 * @author skandula
 *
 */
@SuppressWarnings("serial")
public class MigrateMenuDataMessage extends TolvenMessage implements Serializable {
	private long menustructureId;

	public MigrateMenuDataMessage(AccountMenuStructure ams) {
		this.menustructureId = ams.getId();
		this.setAccountId(ams.getAccount().getId());
	}
	public long getMenustructureId() {
		return menustructureId;
	}

	public void setMenustructureId(long menustructureId) {
		this.menustructureId = menustructureId;
	}
	
}
