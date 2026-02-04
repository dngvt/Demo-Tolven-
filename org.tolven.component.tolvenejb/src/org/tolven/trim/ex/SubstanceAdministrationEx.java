package org.tolven.trim.ex;

import org.tolven.trim.SubstanceAdministration;

public class SubstanceAdministrationEx extends SubstanceAdministration {
	public void blend( SubstanceAdministration substanceAdministrationInclude ) {
		if (substanceAdministrationInclude!=null) {
			if (getAdministrationUnitCode()==null) setAdministrationUnitCode(substanceAdministrationInclude.getAdministrationUnitCode());
			if (getApproachSiteCode()==null) setApproachSiteCode(substanceAdministrationInclude.getApproachSiteCode());
			if (getDoseCheckQuantity()==null) setDoseCheckQuantity(substanceAdministrationInclude.getDoseCheckQuantity());
			if (getDoseQuantity()==null) setDoseQuantity(substanceAdministrationInclude.getDoseQuantity());
			if (getMaxDoseQuantity()==null) setMaxDoseQuantity(substanceAdministrationInclude.getMaxDoseQuantity());
			if (getMethodCode()==null) setMethodCode(substanceAdministrationInclude.getMethodCode());
			if (getRateQuantity()==null) setRateQuantity(substanceAdministrationInclude.getRateQuantity());
			if (getRouteCode()==null) setRouteCode(substanceAdministrationInclude.getRouteCode());
		}
	}
}
