package org.tolven.trim.ex;

import org.tolven.trim.Container;

public class ContainerEx extends Container {
	public void blend( Container containerInclude ) {
		if (containerInclude!=null) {
			if (getBarrierDeltaQuantity()==null) setBarrierDeltaQuantity(containerInclude.getBarrierDeltaQuantity());
			if (getBottomDeltaQuantity()==null) setBottomDeltaQuantity(containerInclude.getBottomDeltaQuantity());
			if (getCapacityQuantity()==null) setCapacityQuantity(containerInclude.getCapacityQuantity());
			if (getCapTypeCode()==null) setCapTypeCode(containerInclude.getCapTypeCode());
			if (getDiameterQuantity()==null) setDiameterQuantity(containerInclude.getDiameterQuantity());
			if (getHeightQuantity()==null) setHeightQuantity(containerInclude.getHeightQuantity());
			if (getSeparatorTypeCode()==null) setSeparatorTypeCode(containerInclude.getSeparatorTypeCode());
		}
	}

}
