package org.tolven.trim.ex;

import org.tolven.trim.PatientEncounter;

public class PatientEncounterEx extends PatientEncounter {
	public void blend( PatientEncounter patientEncounterInclude ) {
		if (patientEncounterInclude!=null) {
			if (getAdmissionReferralSourceCode()==null) setAdmissionReferralSourceCode(patientEncounterInclude.getAdmissionReferralSourceCode());
			if (getDischargeDispositionCode()==null) setDischargeDispositionCode(patientEncounterInclude.getDischargeDispositionCode());
			if (getLengthOfStayQuantity()==null) setLengthOfStayQuantity(patientEncounterInclude.getLengthOfStayQuantity());
			if (getPreAdmitTestInd()==null) setPreAdmitTestInd(patientEncounterInclude.getPreAdmitTestInd());
			if (getSpecialArrangementCode()==null) setSpecialArrangementCode(patientEncounterInclude.getSpecialArrangementCode());
			if (getSpecialCourtesiesCode()==null) setSpecialCourtesiesCode(patientEncounterInclude.getSpecialCourtesiesCode());
		}
	}

}
