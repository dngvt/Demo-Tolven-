package org.tolven.trim.ex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.tolven.trim.Act;
import org.tolven.trim.ActMood;
import org.tolven.trim.ActParticipation;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.ActStatus;
import org.tolven.trim.BindTo;
import org.tolven.trim.CD;
import org.tolven.trim.CE;
import org.tolven.trim.Compute;
import org.tolven.trim.Party;

@SuppressWarnings("serial")
public class ActEx extends Act implements Serializable {
	private transient ActRelationshipMap arMap = null;
	private transient ActRelationshipsMap arsMap = null;
	private transient ActParticipationMap apMap = null;
	
	public void setMoodCodeValue(String value) {
		super.setMoodCode(ActMood.fromValue(value));		
	}
	public String getMoodCodeValue() {
		return super.getMoodCode().value();
	}
	

	/**
	 * Set the statusCode using a string to create the enum 
	 * @param code
	 */
	public void setStatusCodeValue(String code) {
		if (code==null) {
			setStatusCode(null);
		} else {
			setStatusCode(ActStatus.fromValue(code));
		}
	}
	/**
	 * Get the plain enabled flag - which will be null if the flag is not set
	 * @return
	 */
	public Boolean getEnabled() {
		return enabled;
	}
	
	/**
	 * Get the string value of the statusCode 
	 * @return A string containing the statusCode value
	 */
	public String getStatusCodeValue( ) {
		if (getStatusCode()==null) return null;
		return getStatusCode().value();
	}
	
	public String getCodeValue() {
		if (code!=null) {
			if (code.getCD()!=null) {
				CD cd = code.getCD();
				return cd.getCode();
			}
			if (code.getCE()!=null) {
				CE ce = code.getCE();
				return ce.getCode();
			}
		}
		return "";	// easier for rules
	}

	public Map<String, ActRelationship> getRelationship() {
        if (arMap == null) {
        	arMap = new ActRelationshipMap(getRelationships());
        }
        return arMap;
	}
	
	/** Method to return the relationships with a given name as a list.
	 * @param key
	 * @return
	 */
	public Map<String, List<ActRelationship>> getRelationshipsList(){		
		 if (arsMap == null) {
	        	arsMap = new ActRelationshipsMap(getRelationships());
	        }
	        return arsMap;
	}
	
	public Map<String, ActParticipation> getParticipation() {
        if (apMap == null) {
        	apMap = new ActParticipationMap( getParticipations());
        }
        return apMap;
	}

	public ObservationEx getObservationEx() {
		return (ObservationEx) super.getObservation();
	}

    public void setAccountShares(List<String> accountShares) {
		accountShares = accountShares;
	}
    
    public void setEnableAct(Boolean value) {
		enabled = value;
	}

	public Boolean getEnableAct() {
		return enabled;
	}
	
    @Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
	/**
	 * Add the included act into this act
	 * @param actInclude
	 */
	public void blend( Act actInclude ) {
		if (actInclude!=null) {
			TrimFactory.blend(this, actInclude);
			ActEx actIncludeEx = (ActEx) actInclude;
			// Add the included act into this act.
			//setActivityTime(actInclude.getActivityTime());
			//setAvailabilityTime(actInclude.getAvailabilityTime());
			if (getBinds().size()==0) {
				for (BindTo bindTo : actInclude.getBinds()) {
					if (bindTo.getPlaceholder()!=null) {
						getBinds().add(bindTo);
					}
				}
			}
			// Blend all computes
			for (Compute compute : actInclude.getComputes()) {
				getComputes().add(compute);
			}
			if (getClassCode()==null) setClassCode(actInclude.getClassCode());
			if (getCode()==null) setCode(actInclude.getCode());
			if (getConfidentialityCode()==null) setConfidentialityCode(actInclude.getConfidentialityCode());
			if (getDerivationExpr()==null) setDerivationExpr(actInclude.getDerivationExpr());
			if (getEffectiveTime()==null) setEffectiveTime(actInclude.getEffectiveTime());
			if (getActivityTime()==null) setActivityTime(actInclude.getActivityTime());
			if (getAvailabilityTime()==null) setAvailabilityTime(actInclude.getAvailabilityTime());
			if (getId()==null) setId(actInclude.getId());
			if (getIndependentInd()==null) setIndependentInd(actInclude.getIndependentInd());
			if (getInternalId()==null) setInternalId(actInclude.getInternalId());
			if (getInterruptibleInd()==null) setInterruptibleInd(actInclude.getInterruptibleInd());
			if (getLanguageCode()==null) setLanguageCode(actInclude.getLanguageCode());
			if (getLevelCode()==null) setLevelCode(actInclude.getLevelCode());
			if (getMoodCode()==null) setMoodCode(actInclude.getMoodCode());
			if (getNegationInd()==null) setNegationInd(actInclude.getNegationInd());
			if (getPatientEncounter()==null) setPatientEncounter(actInclude.getPatientEncounter());
			if (getPriorityCode()==null) setPriorityCode(actInclude.getPriorityCode());
			if (getProcedure()==null) setProcedure(actInclude.getProcedure());
			if (getReasonCode()==null) setReasonCode(actInclude.getReasonCode());
			if (getRepeatNumber()==null) setRepeatNumber(actInclude.getRepeatNumber());
			if (getStatusCode()==null) setStatusCode(actInclude.getStatusCode());
			if (getText()==null) setText(actInclude.getText());
			if (getTitle()==null) setTitle(actInclude.getTitle());
			if (getUncertaintyCode()==null) setUncertaintyCode(actInclude.getUncertaintyCode());
			if (getSendTos()==null) sendTos = actInclude.getSendTos();
			// Need to check raw property, not default getter in this case due to default processing in the default method
			if (getEnabled()==null && actIncludeEx.getEnabled()!=null) setEnabled(actInclude.isEnabled());
			// Act participations could need blending as well
			// Iterate though the participations to be included and either blend or, if not found, append to the end of the list.
			for (ActParticipation p : actInclude.getParticipations()) {
				ActParticipation plocal = getParticipation().get(p.getName());
				if (plocal !=null) {
					((ActParticipationEx)plocal).blend(p);
				} else {
					getParticipations().add(p);
				}
			}
			// Act Relationships could need blending as well
			// Iterate though the relationships to be included and either blend or, if not found, append to the end of the list.
			for (ActRelationship ar : actInclude.getRelationships()) {
				ActRelationship arlocal = getRelationship().get(ar.getName());
				if (arlocal !=null) {
					((ActRelationshipEx)arlocal).blend(ar);
				} else {
					getRelationships().add(ar);
				}
			}
			// Blend Act extension classes
			if (getPatientEncounter()==null) {
				setPatientEncounter(actInclude.getPatientEncounter());
			} else {
				((PatientEncounterEx)getPatientEncounter()).blend(actInclude.getPatientEncounter());
			}
			if (getObservation()==null) {
				setObservation(actInclude.getObservation());
			} else {
				((ObservationEx)getObservation()).blend(actInclude.getObservation());
			}
			if (getProcedure()==null) {
				setProcedure(actInclude.getProcedure());
			} else {
				((ProcedureEx)getProcedure()).blend(actInclude.getProcedure());
			}
			if (getSubstanceAdministration()==null) {
				setSubstanceAdministration(actInclude.getSubstanceAdministration());
			} else {
				((SubstanceAdministrationEx)getSubstanceAdministration()).blend(actInclude.getSubstanceAdministration());
			}
			if (getSupply()==null) {
				setSupply(actInclude.getSupply());
			} else {
				((SupplyEx)getSupply()).blend(actInclude.getSupply());
			}
		}
	}
	
	public List<Party> getSendTos(){
		List<Party> list = new ArrayList<Party>();
		for(Party p:super.getSendTos()){
			String args[] = {p.getAccountPath(),p.getAccountId(),String.valueOf(p.getProviderId())};
			list.add(TrimFactory.createParty(args));
		}
		return list;
	}
	
	/** This has to check if the accountShare is already there on the Act since there is no
	 * setter method for sendTos on Act.java
	 * @param sendTos
	 */
	public void setSendTos(List<String> sendTos){
		List<PartyEx> selectedParties = new ArrayList<PartyEx>();
		for(String sendTo:sendTos){
			String args[] = TrimFactory.decode(sendTo);
			PartyEx party = TrimFactory.createParty(args);
			selectedParties.add(party);						
		}
		super.getSendTos().clear();		
		super.getSendTos().addAll(selectedParties);
	}
}
