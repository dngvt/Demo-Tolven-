package org.tolven.trim.ex;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.trim.Application;
import org.tolven.trim.CS;
import org.tolven.trim.CSSlot;
import org.tolven.trim.CopyTo;
import org.tolven.trim.Field;
import org.tolven.trim.TolvenId;
import org.tolven.trim.Transition;
import org.tolven.trim.Transitions;
import org.tolven.trim.Trim;
import org.tolven.trim.ValueSet;

public class TrimEx extends Trim {
	private transient FieldMap fieldMap;
	private transient ValueSetMap valueSetMap;
	
	private static final long serialVersionUID = 1L;
	
	private static final TrimFactory trimFactory = new TrimFactory();

	
	/**
	 * Set the reasonCode using a string to create the enum 
	 * @param code
	 */
	public void setReasonCodeValue(String code) {
		
		if (code==null) {
			setReasonCode(null);
		} else {
			CSSlot csslot = trimFactory.createCSSlot();
			CS cs = trimFactory.createCS();
			cs.setCode(code);
			csslot.setCS(cs);
			setReasonCode(csslot);
		}
	}
	
	/**
	 * Get the string value of the reasonCode 
	 * @return A string containing the reasonCode value
	 */
	public String getReasonCodeValue( ) {
		if (getReasonCode()==null) return null;
		if (getReasonCode().getCS() == null) return null;
		return getReasonCode().getCS().getCode();
	}


	public String getStatus() {
		TransitionsEx transitions = (TransitionsEx)getTransitions();
		if (transitions==null) return null;
		TrimExpressionEvaluator evaluator = new TrimExpressionEvaluator();
		evaluator.addVariable("trim", this);
		String statusReason = transitions.getInitialState();
		//if the location of the status is not specified
		if (transitions.getPath() == null) {
			//if there is no status, get initial transition
			//statusReason = transitions.getInitialState();
			return statusReason;
		}
		String[] codePaths = transitions.getPath().split(" ");
		String[] result = new String[codePaths.length];		
		int count = 0;
		for ( String p : codePaths ){			
			String path = "#{" + p + "}";
			String pathVal = (String)evaluator.evaluate(path);
			if(pathVal == null || pathVal.length() == 0)
				continue;
			result[count] = pathVal;
			if (count == 0) {
				statusReason = pathVal;
			} else if (pathVal != null && pathVal != ""){ 
				statusReason.concat(" " + pathVal);
			}
			count++;			
		}
		
/*		String path = "#{" + transitions.getPath() + "}";
		String status = (String)evaluator.evaluate(path);
		return status;*/
		return statusReason;
	}
	/**
	 * Set the status where it belongs in the trim based on the path in Transitions
	 * @param status
	 */
	public void setStatus(String status) {
		TransitionsEx transitions = (TransitionsEx)getTransitions();
		if (transitions==null) throw new RuntimeException( "No transitions element specified in Trim");
		TrimExpressionEvaluator evaluator = new TrimExpressionEvaluator();
		evaluator.addVariable("trim", this);
		
		String[] codePaths = transitions.getPath().split(" ");
		String[] result = new String[codePaths.length];
		String statusReason = "";
		int count = 0;
		for ( String p : codePaths ){
			
			String path = "#{" + p + "}";
			evaluator.setValue(path, status);
			result[count] = status;
			if (count == 0) {
				statusReason = status;
			} else { 
				statusReason.concat(" " + status);
			}
			count++;
			
		}
		
		/*String path = "#{" + transitions.getPath() + "}";
		evaluator.setValue(path, status);*/
	}
	

	public String getTransitionStatus() {
		TransitionsEx transitions = (TransitionsEx)getTransitions();
		if (transitions==null) return null;
		if (getTransition()==null) return null;
		Transition trans = transitions.getTransition().get(getTransition());
		return trans.getTo();
	}
	
	@Override
	public String getDescription() {
		String desc = super.getDescription();
		if (desc==null && getAct()!=null && getAct().getTitle()!=null && getAct().getTitle().getST().getValue()!=null) {
			desc = getAct().getTitle().getST().getValue();
		}
		return desc;
	}

	public void blend( Trim trimInclude ) {
		for (Application app : trimInclude.getApplications()) {
			this.getApplications().add(app);
		}
		if (this.getConfirm()==null) this.setConfirm( trimInclude.getConfirm());
		for (CopyTo copyTo : trimInclude.getCopyTos()) {
			this.getCopyTos().add(copyTo);
		}
		if (this.getMessage()==null) this.setMessage(trimInclude.getMessage());
		else if (trimInclude.getMessage()!=null) {
			if (this.getMessage().getReceiver()==null) this.getMessage().setReceiver(trimInclude.getMessage().getReceiver());
			if (this.getMessage().getSender()==null) this.getMessage().setSender(trimInclude.getMessage().getSender());
		}
		if (this.getDescription()==null) this.setDescription( trimInclude.getDescription());
		if (this.getDrilldown()==null) this.setDrilldown( trimInclude.getDrilldown());
		if (this.getElement()==null) this.setElement( trimInclude.getElement());
		if (this.getName()==null) this.setName( trimInclude.getName());
		if (this.getOrigin()==null) this.setOrigin( trimInclude.getOrigin());
		if (this.getPage()==null) this.setPage( trimInclude.getPage());
		if (this.getReference()==null) this.setReference( trimInclude.getReference());
		if (this.getTransition()==null) this.setTransition(trimInclude.getTransition());
		if (this.getTransitions()==null) this.setTransitions(trimInclude.getTransitions());
		if (trimInclude.getAct()!=null) {
			if (getAct()!=null) {
				((ActEx)getAct()).blend(trimInclude.getAct());
			} else {
				setAct( trimInclude.getAct());
			}
		}
		// Copy menus
		for (String menuPath : trimInclude.getMenus()) {
			this.getMenus().add(menuPath);
		}
		// ValueSets are blended at the individual item level.
		// For example, if there is a valueSet="X" in both the base and extension,
		// then the entries in each set are combined. The base entries are added to the end of the list
		// in the extension list.
		for (ValueSet vsi : trimInclude.getValueSets()) {
			// Lookup the entry in the extension trim
			ValueSetEx vsx = (ValueSetEx) getValueSet().get(vsi.getName());
			if(vsx==null) {
				// Only found in base so just copy the whole thing
				getValueSet().put(vsi.getName(), vsi);
			} else {
				// Need to append each entry, but avoid duplicates
				for (Object ientry: ((ValueSetEx)vsi).getValues()) {
					boolean found = false;
					for (Object xentry : vsx.getValues()) {
						if (xentry.equals(ientry)) {
							found = true;
							break;
						}
					}
					if (!found) {
						vsx.getValues().add(ientry);
					}
				}
			}
		}
		for (Field field : trimInclude.getFields()) {
			// Add field if not already there
			if (!this.getField().containsKey(field.getName())) {
				this.getField().put(field.getName(), field);
			}
		}
	}
	
	public TrimNameMap getIsName( ) {
		return new TrimNameMap( this );
	}
	
	/**
	 * Add a new tolvenId (or replace an existing one) for an account
	 * @param tolvenId
	 */
	public void addTolvenId( TolvenIdEx tolvenId ) {
		TolvenIdEx existingTolvenId = getTolvenId( tolvenId.getAccountIdAsLong());
		if (existingTolvenId!=null) {
			getTolvenIds().remove(existingTolvenId);
		}
		// Add the new entry
		getTolvenIds().add(tolvenId);
	}
	/**
	 * Return the starting status for this event. This is the statusCode
	 * of the event when the event was created. The event statusCode to use is determined by
	 * the path specified in the transitions node.
	 * @return The StatusCodeValue
	 */
	public String getTolvenEventStatus( ) {
		if (getTolvenEventIds().size()==0) return null;
		// Prepend the new entry
		TolvenId event = getTolvenEventIds().get(0);
		return event.getStatus();
	}
	
	/**
	 * Return a list of transitions appropriate to the current event
	 * @return A list of transitions.
	 */
	public List<Transition> getEventTransitions( ) {
		List<Transition> results = new ArrayList<Transition>(10);
		String statusCodeValue = getTolvenEventStatus();		
		Transitions transitions = this.getTransitions();
		if (transitions!=null ) {
			for (Transition t : transitions.getTransitions()) {
				if (statusCodeValue!=null && statusCodeValue.equals(t.getFrom())) {
					results.add(t);
				} else if (statusCodeValue==null && t.getFrom()==null) {
					results.add(t);
				}
			}
		}
		return results;
	}
	
	/**
	 * Return a list of transitions appropriate to the current event
	 * @return A list of transitions.
	 */
	public Map<String,List<Transition>> getValidTransitions( ) {
		Map<String,List<Transition>> retTransitions = new HashMap<String,List<Transition>>();
		Transitions transitions = this.getTransitions();
		if (transitions!=null ) {
			for (Transition t : transitions.getTransitions()) {
				List<Transition> trans = (List<Transition>) retTransitions.get(t.getFrom());
				if(trans == null){
					trans = new ArrayList<Transition>(10);
					retTransitions.put(t.getFrom(), trans);
				}
				trans.add(t);
			}
		}
		return retTransitions;
	}
	/**
	 * Add a new tolvenId (or replace an existing one) for an account.
	 * Last-in is at the top of the list.
	 * @param tolvenId
	 */
	public void addTolvenEventId( TolvenIdEx tolvenId ) {
		// Prepend the new entry
		getTolvenEventIds().add(0, tolvenId);
	}
	/**
	 * Find the tolvenId associated with the specified account. In most cases, there will be at most one
	 * tolvenID associated with each account.
	 * @param accountId
	 * @return
	 */
	public TolvenIdEx getTolvenId( long accountId) {
		for (TolvenId tolvenId : getTolvenIds()) {
			TolvenIdEx tolvenIdEx = (TolvenIdEx) tolvenId;
			if ((tolvenIdEx.getIdAsLong() != null && tolvenIdEx.getAccountIdAsLong()==accountId)) return tolvenIdEx;
		}
		return null;
	}

	/**
	 * Find the tolvenEventId associated with the specified account. There can be more than one tolvenId in a trim, one for each
	 * time the trim is modified. Thus, this creates a history of actions against this trim. The most recent tolvenId is
	 * always first in the list and the original event will be last,
	 * @param accountId
	 * @return
	 */
	public TolvenIdEx getTolvenEventId( long accountId) {
		for (TolvenId tolvenId : getTolvenEventIds()) {
			TolvenIdEx tolvenIdEx = (TolvenIdEx) tolvenId;
			if ((tolvenIdEx.getIdAsLong() != null && tolvenIdEx.getAccountIdAsLong()!=null && tolvenIdEx.getAccountIdAsLong()==accountId)) return tolvenIdEx;
		}
		return null;
	}

	public Map<String, Object> getField( ) {
		if (fieldMap==null) {
			fieldMap = new FieldMap( this );
		}
		return fieldMap;
	}

	public Map<String, ValueSet> getValueSet( ) {
		if (valueSetMap==null) {
			valueSetMap = new ValueSetMap( this );
		}
		return valueSetMap;
	}

	public Object getFieldValue( String fieldName ) {
		for (Field field : getFields()) {
			if (field.getName().equals(fieldName)) {
				return field.getValue( );
			}
		}
		return null;
	}

}
