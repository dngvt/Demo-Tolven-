package org.tolven.trim.ex;

import org.tolven.trim.ActParticipation;
import org.tolven.trim.RoleParticipation;

@SuppressWarnings("serial")
public class RoleParticipationEx extends RoleParticipation {

	public Boolean getEnabled() {
		return enabled;
	}
	public void blend( RoleParticipation pInclude ) {
		RoleParticipationEx pIncludeEx = (RoleParticipationEx) pInclude;
		if (this.getTypeCode()==null) this.setTypeCode(pIncludeEx.getTypeCode());
		if (this.getContextControlCode()==null) this.setContextControlCode(pIncludeEx.getContextControlCode());
		if (this.getAwarenessCode()==null) this.setAwarenessCode(pIncludeEx.getAwarenessCode());
		if (this.getEnabled()==null && pIncludeEx.getEnabled()!=null) this.setEnabled(pIncludeEx.isEnabled());
		if (this.getFunctionCode()==null) this.setFunctionCode(pIncludeEx.getFunctionCode());
		if (this.getModeCode()==null) this.setModeCode(pIncludeEx.getModeCode());
		if (this.getNegationInd()==null) this.setNegationInd(pIncludeEx.getNegationInd());
		if (this.getNoteText()==null) this.setNoteText(pIncludeEx.getNoteText());
		if (this.getPerformInd()==null) this.setPerformInd(pIncludeEx.getPerformInd());
		if (this.getSequenceNumber()==null) this.setSequenceNumber(pIncludeEx.getSequenceNumber());
		if (this.getSignatureText()==null) this.setSignatureText(pIncludeEx.getSignatureText());
		if (this.getSubsetCode()==null) this.setSubsetCode(pIncludeEx.getSubsetCode());
		if (this.getSubstitutionConditionCode()==null) this.setSubstitutionConditionCode(pIncludeEx.getSubstitutionConditionCode());
		if (this.getTime()==null) this.setTime(pIncludeEx.getTime());
		if (this.getTypeId()==null) this.setTypeId(pIncludeEx.getTypeId());
		// InfrastructureRoot
		if (this.getDrilldown()==null) this.setDrilldown(pIncludeEx.getDrilldown());
		if (this.getPage()==null) this.setPage(pIncludeEx.getPage());
		if (this.getAct()==null) {
			setAct(pInclude.getAct());
		} else {
			((ActEx)getAct()).blend(pInclude.getAct());
		}
	}

}
