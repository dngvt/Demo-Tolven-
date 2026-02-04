package org.tolven.trim.ex;

import org.tolven.trim.NonPersonLivingSubject;

public class NonPersonLivingSubjectEx extends NonPersonLivingSubject {
	public void blend( NonPersonLivingSubject nonPersonLivingSubjectInclude ) {
		if (nonPersonLivingSubjectInclude!=null) {
			if (getGenderStatusCode()==null) setGenderStatusCode(nonPersonLivingSubjectInclude.getGenderStatusCode());
			if (getStrainText()==null) setStrainText(nonPersonLivingSubjectInclude.getStrainText());
		}
	}

}
