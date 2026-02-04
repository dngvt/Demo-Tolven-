package org.tolven.trim.ex;

import org.tolven.trim.Device;
import org.tolven.trim.LivingSubject;

public class LivingSubjectEx extends LivingSubject {
	public void blend( LivingSubject livingSubjectInclude ) {
		if (livingSubjectInclude!=null) {
			if (getAdministrativeGenderCode()==null) setAdministrativeGenderCode(livingSubjectInclude.getAdministrativeGenderCode());
			if (getBirthTime()==null) setBirthTime(livingSubjectInclude.getBirthTime());
			if (getDeceasedInd()==null) setDeceasedInd(livingSubjectInclude.getDeceasedInd());
			if (getDeceasedTime()==null) setDeceasedTime(livingSubjectInclude.getDeceasedTime());
			if (getMultipleBirthInd()==null) setMultipleBirthInd(livingSubjectInclude.getMultipleBirthInd());
			if (getMultipleBirthOrderNumber()==null) setMultipleBirthOrderNumber(livingSubjectInclude.getMultipleBirthOrderNumber());
			if (getOrganDonorInd()==null) setOrganDonorInd(livingSubjectInclude.getOrganDonorInd());
		}
	}

}
