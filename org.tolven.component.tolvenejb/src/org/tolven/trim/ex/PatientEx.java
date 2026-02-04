package org.tolven.trim.ex;

import org.tolven.trim.Patient;

public class PatientEx extends Patient {
	public void blend( Patient patientInclude ) {
		if (patientInclude!=null) {
			if (getVeryImportantPersonCode()==null) setVeryImportantPersonCode(patientInclude.getVeryImportantPersonCode());
		}
	}

}
