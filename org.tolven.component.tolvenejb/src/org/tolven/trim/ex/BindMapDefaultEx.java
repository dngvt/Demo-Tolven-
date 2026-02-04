package org.tolven.trim.ex;

import org.tolven.trim.AD;
import org.tolven.trim.ActStatus;
import org.tolven.trim.BindMapDefault;
import org.tolven.trim.CD;
import org.tolven.trim.CE;
import org.tolven.trim.CS;
import org.tolven.trim.CV;
import org.tolven.trim.ED;
import org.tolven.trim.EN;
import org.tolven.trim.EntityStatus;
import org.tolven.trim.II;
import org.tolven.trim.IVLPQ;
import org.tolven.trim.PQ;
import org.tolven.trim.RTO;
import org.tolven.trim.RoleStatus;
import org.tolven.trim.ST;
import org.tolven.trim.TS;
import org.tolven.trim.URGTS;

public class BindMapDefaultEx extends BindMapDefault {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Object getValue() {
		if (this.ad != null) {
			return this.ad;
		} else if (this.cd != null) {
			return this.cd;
		} else if (this.ce != null) {
			return this.ce;
		} else if (this.cs != null) {
			return this.cs;
		} else if (this.cv != null) {
			return this.cv;
		} else if (this.ed != null) {
			return this.ed;
		} else if (this.en != null) {
			return this.en;
		} else if (this.ii != null) {
			return this.ii;
		} else if (this.ivlpq != null) {
			return this.ivlpq;
		} else if (this.pq != null) {
			return this.pq;
		} else if (this.rto != null) {
			return this.rto;
		} else if (this.st != null) {
			return this.st;
		} else if (this.ts != null) {
			return this.ts;
		} else if (this.urgts != null) {
			return this.urgts;
		} else if (this.actStatus != null) {
			return this.actStatus;
		} else if (this.entityStatus != null) {
			return this.entityStatus;
		} else if (this.roleStatus != null) {
			return this.roleStatus;
		} else {
			return null;
		}
	}
	
	public void setValue(Object value) {
		setAD(null);
		setCD(null);
		setCE(null);
		setCS(null);
		setCV(null);
		setED(null);
		setEN(null);
		setII(null);
		setIVLPQ(null);
		setPQ(null);
		setRTO(null);
		setST(null);
		setTS(null);
		setURGTS(null);
		setActStatus(null);
		setEntityStatus(null);
		setRoleStatus(null);
		
		if (value == null) {
			return;
		} else if (value instanceof AD) {
			this.ad = (AD) value;
		} else if (value instanceof CD) {
			this.cd = (CD) value;
		} else if (value instanceof CE) {
			this.ce = (CE) value;
		} else if (value instanceof CS) {
			this.cs = (CS) value;
		} else if (value instanceof CV) {
			this.cv = (CV) value;
		} else if (value instanceof ED) {
			this.ed = (ED) value;
		} else if (value instanceof EN) {
			this.en = (EN) value;
		} else if (value instanceof II) {
			this.ii = (II) value;
		} else if (value instanceof IVLPQ) {
			this.ivlpq = (IVLPQ) value;
		} else if (value instanceof PQ) {
			this.pq = (PQ) value;
		} else if (value instanceof RTO) {
			this.rto = (RTO) value;
		} else if (value instanceof ST) {
			this.st = (ST) value;
		} else if (value instanceof TS) {
			this.ts = (TS) value;
		} else if (value instanceof URGTS) {
			this.urgts = (URGTS) value;
		} else if (value instanceof ActStatus) {
			this.actStatus = (ActStatus) value;
		} else if (value instanceof EntityStatus) {
			this.entityStatus = (EntityStatus) value;
		} else  if (value instanceof RoleStatus) {
			this.roleStatus = (RoleStatus) value;
		} else {
			throw new RuntimeException("In BindMapEx: Cannot set value of unknown DataType: " + value.getClass().getName());
		}
	}
}
