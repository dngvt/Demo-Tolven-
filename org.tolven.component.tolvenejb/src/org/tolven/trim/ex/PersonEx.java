package org.tolven.trim.ex;

import org.tolven.trim.Person;

public class PersonEx extends Person {
	public void blend( Person personInclude ) {
		if (personInclude!=null) {
			if (getAddr()==null) setAddr(personInclude.getAddr());
			if (getDisabilityCode()==null) setDisabilityCode(personInclude.getDisabilityCode());
			if (getEducationLevelCode()==null) setEducationLevelCode(personInclude.getEducationLevelCode());
			if (getEthnicGroupCode()==null) setEthnicGroupCode(personInclude.getEthnicGroupCode());
			if (getLivingArrangementCode()==null) setLivingArrangementCode(personInclude.getLivingArrangementCode());
			if (getMaritalStatusCode()==null) setMaritalStatusCode(personInclude.getMaritalStatusCode());
			if (getRaceCode()==null) setRaceCode(personInclude.getRaceCode());
			if (getReligiousAffiliationCode()==null) setReligiousAffiliationCode(personInclude.getReligiousAffiliationCode());
		}
	}

}
