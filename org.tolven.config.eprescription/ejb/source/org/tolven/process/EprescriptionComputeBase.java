package org.tolven.process;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.tolven.app.EPrescriptionLocal;
import org.tolven.app.FDBInterface;

public abstract class EprescriptionComputeBase extends ComputeBase {
	@EJB FDBInterface fdbInterface;
	@EJB EPrescriptionLocal ePrescriptionBean;
	
	public EPrescriptionLocal getePrescriptionBean() {
		return ePrescriptionBean;
	}
	public void setePrescriptionBean(EPrescriptionLocal ePrescriptionBean) {
		this.ePrescriptionBean = ePrescriptionBean;
	}
	public EprescriptionComputeBase() {
		try {
			if (this.fdbInterface==null) {
				InitialContext ctx = new InitialContext();
			    this.fdbInterface = (FDBInterface) ctx.lookup("java:global/tolven/tolvenEJB/FDBBean!org.tolven.app.FDBInterface");
			}
			
		} catch (NamingException e) {
			throw new RuntimeException( "Unable to access FDBBean in InsertFdbMedicationAct", e);
		}
		try {
			if (this.ePrescriptionBean==null) {
				InitialContext ctx = new InitialContext();
			    this.ePrescriptionBean = (EPrescriptionLocal) ctx.lookup("java:global/tolven/tolvenEJB/EPrescriptionBean!org.tolven.app.EPrescriptionLocal");
			}
		} catch (NamingException e) {
			throw new RuntimeException( "Unable to access FDBBean in InsertFdbMedicationAct", e);
		}
		
	}
	public FDBInterface getFdbInterface() {
		return fdbInterface;
	}
	public void setFdbInterface(FDBInterface fdbInterface) {
		this.fdbInterface = fdbInterface;
	}
}
