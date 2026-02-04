package org.tolven.trim.ex;

import org.tolven.trim.PQ;

public class PQEx extends PQ {

	@Override
	public boolean equals(Object obj) {
    	if (obj==null) return false;
    	if (!(obj instanceof PQ)) return false;
    	PQ pq = (PQ)obj;
    	if (!pq.getValue().equals(getValue())) return false; 
    	if (!pq.getUnit().equals(getUnit())) return false; 
    	return true;
	}

	@Override
	public int hashCode() {
		if (getValue() !=null) return getValue().hashCode();
		return 0;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer( 100 );
		if (this.getOriginalText()==null) {
			sb.append(getValue());
		} else {
			sb.append(this.getOriginalText());
		}
		sb.append(' ');
		sb.append(getUnit());
		return sb.toString();
	}
	@Override
	public String getOriginalText() {
	  if (super.getOriginalText()==null) {
		  if (getValue()==null) {
			  return "";
		  }
	   return Double.toString(getValue());
	  } else {
	   return super.getOriginalText();
	  }
	}
	
	@Override
	public void setOriginalText(String originalText) {
		super.setOriginalText(originalText);
		try {
			setValue(Double.valueOf(originalText));
		} catch (NumberFormatException e) {
			setValue(null);
			//swallow
		}
		
	}
	
	public boolean isOriginalTextValid() {
		try {
			Double.valueOf(originalText);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public String getUnit() {
		String theUnit = super.getUnit();
		if (theUnit != null && theUnit.length() == 0) {
			return null;
		} else {
			return theUnit;
		}
	}
}
