package org.tolven.trim.ex;

import org.tolven.trim.BL;

public class BLEx extends BL {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Boolean getValue() {
		return isValue();
	}
	
	@Override
	public boolean equals(Object obj) {
    	if (obj==null) return false;
    	if (!(obj instanceof BL)) return false;
    	BL bl = (BL)obj;
    	if (!bl.isValue().equals(isValue())) return false;  
    	return true;
	}

	@Override
	public int hashCode() {
		if (isValue() !=null) return isValue().hashCode();
		return 0;
	}

	@Override
	public String toString() {
		return isValue().toString();
	}

}
