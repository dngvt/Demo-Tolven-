package org.tolven.app.bean;

import org.tolven.app.entity.MenuData;

public class PatientFact {
	private MenuData patient;
    private String name;

    private volatile int hashCode = 0;
	
    public PatientFact(MenuData patient, String name) {
        super();
        this.patient = patient;
        this.name = name;
    }

    @Override
	public boolean equals(Object obj) {
	    if(this == obj) {
            return true;
        }
        if (!(obj instanceof PatientFact)) {
            return false; 
        }
        PatientFact pFact = (PatientFact)obj;
        boolean status = patient.equals(pFact.getPatient()) && name.equals(pFact.getName());
	    
        return status;
	}
	@Override
	public int hashCode() {
		final int multiplier = 23;
        if (hashCode == 0) {
            int code = 133;
            code = multiplier * code + patient.hashCode();
            code = multiplier * code + name.hashCode();
            hashCode = code;
        }
		return hashCode;
	}

    public MenuData getPatient() {
        return patient;
    }

    public void setPatient(MenuData patient) {
        this.patient = patient;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
