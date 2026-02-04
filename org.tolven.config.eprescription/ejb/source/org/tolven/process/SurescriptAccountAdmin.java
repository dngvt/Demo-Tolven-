package org.tolven.process;

import javax.resource.spi.IllegalStateException;

import org.tolven.trim.Compute.Property;
import org.tolven.trim.ST;
import org.tolven.trim.ex.ActEx;

/**
 * Sets the current logged in Administrator name to SureScript Account
 * Registration Drilldown.
 * 
 * @author dersh
 */

public class SurescriptAccountAdmin extends InsertAct {
	private ActEx act;
	private String adminName = null;

	public void compute() throws Exception {
		if (isEnabled()) {
			act = (ActEx) this.getAct();
			
			adminName = getUser();
			act.getRelationship().get("registration").getAct().getObservation()
					.getValues().get(11).setST(getST(adminName));
			act.getComputes().get(1).getProperties().get(0).setValue(true);
			disableCompute();
			throw new IllegalStateException("Fix getUser() method in SurescriptAccountAdmin.java");
		}
	}

	/**
	 * Returns the current logged in user's name.
	 * 
	 * @return string
	 */
	public String getUser() {
		
		return null;
		//This need to refactored 
		/*String accountName = null;
		Subject subject = null;
		try {
			subject = (Subject) PolicyContext
					.getContext("javax.security.auth.Subject.container");
			if (subject == null)
				throw new IllegalStateException(
						"No Subject found in PolicyContext");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Principal principal = null;
		Object obj = null;
		for (java.util.Iterator iter = subject.getPrincipals().iterator(); iter
				.hasNext();) {
			obj = iter.next();
			if (obj instanceof Principal && !(obj instanceof Group)) {
				principal = (Principal) obj;
				break;
			}
		}
		if (principal == null)
			throw new IllegalStateException("No Principal found in Subject");
		try {
			ldapBean = (LDAPLocal) getCtx().lookup("tolven/LDAPBean/local");
			TolvenPerson tp = ldapBean.createTolvenPerson(principal.getName());
			accountName = tp.getGivenName() + " " + tp.getSn();

		} catch (NamingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.checkProperties();
		return accountName;*/
	}

	/**
	 * This function is used to convert string to ST.
	 * 
	 * @author Valsaraj added on 07/16/09
	 * @param str
	 * @return st
	 */
	public ST getST(String str) {
		ST st = new ST();
		st.setValue(str);
		return st;
	}

	/**
	 * This function is used to disable compute.
	 * 
	 * @author Suja added on 7/14/09
	 * @param void
	 * @return void
	 */
	private void disableCompute() {
		for (Property property : getComputeElement().getProperties()) {
			if ("enabled".equals(property.getName())) {
				property.setValue(Boolean.FALSE);
			}
		}
	}
}
