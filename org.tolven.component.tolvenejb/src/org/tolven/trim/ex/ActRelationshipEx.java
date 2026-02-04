package org.tolven.trim.ex;

import org.tolven.trim.Act;
import org.tolven.trim.ActRelationship;

@SuppressWarnings("serial")
public class ActRelationshipEx extends ActRelationship {

	public void setEnableRelationship(Boolean aEnableRelationship){
		this.enabled = aEnableRelationship;
	}
	
	public Boolean getEnableRelationship() {
		return this.enabled;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}
	public void blend( ActRelationship arInclude ) {
		ActRelationshipEx arIncludeEx = (ActRelationshipEx) arInclude;
		((ActEx)getAct()).blend(arInclude.getAct());
	}
}
