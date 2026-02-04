package org.tolven.process;

import org.apache.commons.lang.StringUtils;
import org.tolven.logging.TolvenLogger;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.Compute.Property;

public class InsertLabResultAct extends InsertAct {

	private String listPath;
	private String arType;

	@Override
	public void compute() throws Exception {
		TolvenLogger.info( "Compute enabled=" + isEnabled(), EditLabOrderAct.class);
		super.checkProperties();		
		if (isEnabled() && StringUtils.isNotBlank(getTemplate())) {		
			if (getAction().equals("add")){
	            ActRelationship newRelationship = parseTemplate();
	            newRelationship.setEnabled(true);
	            //newRelationship.setEnabled(isEnableAct());
	            this.getAct().getRelationships().add(newRelationship);		
	            TolvenLogger.info( "added ...", EditLabOrderAct.class);
			}	
			
		    // Reset the Sequence
	        int index = 1;
	        for (ActRelationship lRel : this.getAct().getRelationships()){
	        	if(!lRel.getName().equals(getArName())) // set the sequence number only for the Inserted acts
	        		continue;
	        	lRel.setSequenceNumber(new Integer(index));
	        	index++;
	        }
	        
	        // Disable the Compute since its job is done.
	    	for (Property property : getComputeElement().getProperties()) {
				if (property.getName().equals("enabled")) {
					property.setValue(Boolean.FALSE);
					break;
				}
			}
		}
		// TolvenLogger.info( "Compute Execute over", EditFdbMedicationAct.class);
	}
			
	public String getListPath() {
		return listPath;
	}
	public void setListPath(String listPath) {
		this.listPath = listPath;
	}
	public String getArType() {
		return arType;
	}
	public void setArType(String arType) {
		this.arType = arType;
	}
	
}
