package org.tolven.trim.ex;

import org.tolven.trim.Organization;

public class OrganizationEx extends Organization {
	public void blend( Organization organizationInclude ) {
		if (organizationInclude!=null) {
			if (this.getAddr()==null) setAddr(organizationInclude.getAddr());
			if (this.getStandardIndustryClassCode()==null) setStandardIndustryClassCode(organizationInclude.getStandardIndustryClassCode());
		}
	}

}
