package org.tolven.trim.ex;

import org.tolven.trim.BL;
import org.tolven.trim.BLSlot;
import org.tolven.trim.DataType;
import org.tolven.trim.NullFlavor;

public class BLSlotEx extends BLSlot {
	
	@Override
	public String toString() {
		if (this.getBL()!=null) return getBL().toString();
		if (this.getNull()!=null) return getNull().toString();
		return null;
	}

	@Override
	public String getDatatype() {
		return "BL";
	}
	public DataType getValue( ) {
		if (getBL()!=null) {
			return getBL();
		}
		if (getNull()!=null) {
			return getNull();
		}
		return null;
	}

	public void setValue( DataType value ) {
		setBL( null );
		setNull( null );
		if (value instanceof BL ) {
			setBL( (BL) value );
		} else if ( value instanceof NullFlavor) {
			setNull( (NullFlavor)value);
		}
	}

}
