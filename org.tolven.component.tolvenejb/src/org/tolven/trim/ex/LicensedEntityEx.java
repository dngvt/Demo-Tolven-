package org.tolven.trim.ex;

import org.tolven.trim.LicensedEntity;

public class LicensedEntityEx extends LicensedEntity {
	public void blend( LicensedEntity licensedEntityInclude ) {
		if (licensedEntityInclude!=null) {
			if (getRecertificationTime()==null) setRecertificationTime(licensedEntityInclude.getRecertificationTime());
		}
	}

}
