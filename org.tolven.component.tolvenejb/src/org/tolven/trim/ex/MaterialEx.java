package org.tolven.trim.ex;

import org.tolven.trim.Material;

public class MaterialEx extends Material {
	public void blend( Material materialInclude ) {
		if (materialInclude!=null) {
			if (getFormCode()==null) setFormCode(materialInclude.getFormCode());
		}
	}

}
