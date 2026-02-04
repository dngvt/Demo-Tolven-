package org.tolven.trim.ex;

import org.tolven.trim.Procedure;

public class ProcedureEx extends Procedure {
	public void blend( Procedure procedureInclude ) {
		if (procedureInclude!=null) {
			if (this.getApproachSiteCode()==null) setApproachSiteCode(procedureInclude.getApproachSiteCode());
			if (this.getMethodCode()==null) setMethodCode(procedureInclude.getMethodCode());
			if (this.getTargetSiteCode()==null) setTargetSiteCode(procedureInclude.getTargetSiteCode());
		}
	}

}
