package org.tolven.trim.ex;

import org.tolven.trim.Access;

public class AccessEx extends Access {
	public void blend( Access accessInclude ) {
		if (accessInclude!=null) {
			if (getApproachSiteCode()==null) setApproachSiteCode(accessInclude.getApproachSiteCode());
			if (getGaugeQuantity()==null) setGaugeQuantity(accessInclude.getGaugeQuantity());
			if (getTargetSiteCode()==null) setTargetSiteCode(accessInclude.getTargetSiteCode());
		}
	}

}
