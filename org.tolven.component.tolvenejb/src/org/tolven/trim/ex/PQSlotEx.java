package org.tolven.trim.ex;

import org.tolven.trim.DataType;
import org.tolven.trim.INT;
import org.tolven.trim.NullFlavor;
import org.tolven.trim.PQ;
import org.tolven.trim.PQSlot;
import org.tolven.trim.REAL;
import org.tolven.trim.URGINT;
import org.tolven.trim.URGPQ;
import org.tolven.trim.URGREAL;

public class PQSlotEx extends PQSlot {
	
	public DataType getValue( ) {
		if (getPQ()!=null) {
			return getPQ();
		} else if (getURGINT() != null) {
			return getURGINT();
		} else if (getURGREAL() != null) {
			return getURGREAL();
		} else if (getURGPQ() != null) {
			return getURGPQ();
		} else if (getINT() != null) {
			return getINT();
		} else if (getREAL() != null) {
			return getREAL();
		} else if (getNull() != null) {
			return getNull();
		} else {
			return null;
		}
	}

	public void setValue( DataType value ) {
		setPQ( null );
		setURGINT(null);
		setURGPQ(null);
		setURGREAL(null);
		setINT(null);
		setREAL(null);
		setNull(null);
		
		if (value instanceof PQ ) {
			setPQ( (PQ) value );
		} else if ( value instanceof URGINT) {
			setURGINT ( (URGINT)value); 
		} else if (value instanceof URGREAL) {
			setURGREAL ((URGREAL) value);
		} else if (value instanceof URGPQ) {
			setURGPQ ((URGPQ) value);
		} else if (value instanceof INT) {
			setINT ((INT) value);
		} else if (value instanceof REAL) {
			setREAL ((REAL) value);
		} else if ( value instanceof NullFlavor) {
			setNull( (NullFlavor)value);
		} 
	}
}
