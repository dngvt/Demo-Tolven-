package org.tolven.process;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.tolven.app.MenuLocal;
import org.tolven.surescripts.PharmacyLocal;

public abstract class SurescriptsComputeBase extends ComputeBase{
	private MenuLocal menuBean;
	@EJB private PharmacyLocal pharmBean;
	
	public SurescriptsComputeBase(){
		try {
			if (this.pharmBean==null) {
				InitialContext ctx = new InitialContext();
			    this.pharmBean = (PharmacyLocal) ctx.lookup("java:global/tolven/tolvenEJB/PharmacyBean!org.tolven.surescripts.PharmacyLocal");
			}
		} catch (NamingException e) {
			throw new RuntimeException( "error looking up ejbs", e);
		}
	}
	
	public MenuLocal getMenuBean() {
		return menuBean;
	}
	public void setMenuBean(MenuLocal menuBean) {
		this.menuBean = menuBean;
	}
	public PharmacyLocal getPharmBean() {
		return pharmBean;
	}
	public void setPharmBean(PharmacyLocal pharmBean) {
		this.pharmBean = pharmBean;
	}
	

}
