package org.tolven.process;



import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


public class InstantiateMedicationOrder extends InsertAct {
	private Logger log = Logger.getLogger(getClass());
	public void compute( ) throws Exception {
		log.info( "Compute enabled=" + isEnabled());
		super.checkProperties();
		
		if (isEnabled() && StringUtils.isNotBlank(getTemplate())) {
			
		}
	}
}
