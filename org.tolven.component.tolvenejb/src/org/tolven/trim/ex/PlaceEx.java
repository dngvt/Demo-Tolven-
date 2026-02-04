package org.tolven.trim.ex;

import org.tolven.trim.Place;

public class PlaceEx extends Place {
	public void blend( Place placeInclude ) {
		if (placeInclude!=null) {
			if (getAddr()==null) setAddr(placeInclude.getAddr());
			if (getDirectionsText()==null) setDirectionsText(placeInclude.getDirectionsText());
			if (getGpsText()==null) setGpsText(placeInclude.getGpsText());
			if (getMobileInd()==null) setMobileInd(placeInclude.getMobileInd());
			if (getPositionText()==null) setPositionText(placeInclude.getPositionText());
		}
	}

}
