package org.tolven.trim.ex;

import org.tolven.trim.ManufacturedMaterial;

public class ManufacturedMaterialEx extends ManufacturedMaterial {
	public void blend( ManufacturedMaterial manufacturedMaterialInclude ) {
		if (manufacturedMaterialInclude!=null) {
			if (getExpirationTime()==null) setExpirationTime(manufacturedMaterialInclude.getExpirationTime());
			if (getLotNumberText()==null) setLotNumberText(manufacturedMaterialInclude.getLotNumberText());
			if (getStabilityTime()==null) setStabilityTime(manufacturedMaterialInclude.getStabilityTime());
		}
	}

}
