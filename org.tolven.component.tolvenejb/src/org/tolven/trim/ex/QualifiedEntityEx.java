package org.tolven.trim.ex;

import org.tolven.trim.QualifiedEntity;

public class QualifiedEntityEx extends QualifiedEntity {
	public void blend( QualifiedEntity qualifiedEntityInclude ) {
		if (qualifiedEntityInclude!=null) {
			if (this.getEquivalenceInd()==null) setEquivalenceInd(qualifiedEntityInclude.getEquivalenceInd());
		}
	}

}
