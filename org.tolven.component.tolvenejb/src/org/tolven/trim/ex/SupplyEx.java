package org.tolven.trim.ex;

import org.tolven.trim.Supply;

public class SupplyEx extends Supply {
	public void blend( Supply supplyInclude ) {
		if (supplyInclude!=null) {
			if (this.getExpectedUseTime()==null) setExpectedUseTime(supplyInclude.getExpectedUseTime());
			if (this.getQuantity()==null) setQuantity(supplyInclude.getQuantity());
		}
	}

}
