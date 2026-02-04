package org.tolven.app;


public class HCQMMeasure {

	private String dateFrom;
	private String mrn;
	private String result;
	private String dateTo;
	private String value;
	private String measureNumber;
	private String measureName;
	
	/*	pqriColumnHeadings = new ArrayList<String>();
	    		pqriColumnHeadings.add("From (date01)");
	    		pqriColumnHeadings.add("Measure (string01)");
	    		pqriColumnHeadings.add("MRN (string05)");
	    		pqriColumnHeadings.add("Result (string02)");
	    		pqriColumnHeadings.add("To (date02)");
	    		pqriColumnHeadings.add("Value (pqValue01)");*/
	
	

	public HCQMMeasure(String measureName, String result) {
		this.result = result;
		this.measureName = measureName;
		this.lookupMeasureNumber(measureName);
	}
	
	public void lookupMeasureNumber(String measure) {
		if(measure.equals("VTE1")) {
			this.measureNumber = "NQF 0371";
		} else if (measure.equals("VTE2")) {
			this.measureNumber = "NQF 0372";
		} else if (measure.equals("VTE3")) {
			this.measureNumber = "NQF 0373";
		} else if (measure.equals("VTE4")) {
			this.measureNumber = "NQF 0374";
		} else if (measure.equals("VTE5")) {
			this.measureNumber = "NQF 0375";
		} else if (measure.equals("VTE6")) {
			this.measureNumber = "NQF 0376";
		} else if (measure.equals("VTE7")) {
			this.measureNumber = "NQF 0377";
		} else if (measure.equals("VTE8")) {
			this.measureNumber = "NQF 0378";
		} else if (measure.equals("STK2")) {
			this.measureNumber = "NQF 0435";
		} else if (measure.equals("STK3")) {
			this.measureNumber = "NQF 0436";
		} else if (measure.equals("STK4")) {
			this.measureNumber = "NQF 0437";
		} else if (measure.equals("STK5")) {
			this.measureNumber = "NQF 0438";
		} else if (measure.equals("STK6")) {
			this.measureNumber = "NQF 0439";
		} else if (measure.equals("STK8")) {
			this.measureNumber = "NQF 0440";
		} else if (measure.equals("STK10")) {
			this.measureNumber = "NQF 0441";
		} else if (measure.startsWith("ED1")) {
			this.measureNumber = "NQF 0495".concat("_" + measure.substring(measure.indexOf(".")+1));
		} else if (measure.startsWith("ED2")) {
			this.measureNumber = "NQF 0497".concat("_" + measure.substring(measure.indexOf(".")+1));
		} else {
			this.measureNumber = measure;
		}
	}
	
	public String getMeasureName() {
		return measureName;
	}

	public void setMeasureName(String measureName) {
		this.measureName = measureName;
	}

	public void setMeasureNumber(String measureNumber) {
		this.measureNumber = measureNumber;
	}
	
	public String getMeasureNumber() {
		return measureNumber;
	}

	public String getDateFrom() {
		return dateFrom;
	}
	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getMrn() {
		return mrn;
	}
	public void setMrn(String mrn) {
		this.mrn = mrn;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getDateTo() {
		return dateTo;
	}
	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
