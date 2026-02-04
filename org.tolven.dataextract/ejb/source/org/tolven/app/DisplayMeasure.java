package org.tolven.app;

public class DisplayMeasure {
	private String accountId;
	private String measureName;
	private String measureNumber;
	private String numerator;
	private String denominator;
	private String percentage;
	private Integer exclusions;
	private Integer performanceNotMet;
	private Boolean emergencyDepartmentMeasure;
	private Boolean excludeFromPQRI;
	
	
	public DisplayMeasure(String accountId,String measure,String numerator,String denominator,String percentage) {
		this.accountId = accountId;
		this.measureName = measure;
		this.numerator = numerator;
		this.denominator = denominator;
		this.percentage = percentage;
		this.exclusions = 0;
		this.performanceNotMet = 0;
		this.emergencyDepartmentMeasure = false;
	}
	
	public DisplayMeasure(String measure) {
		this.measureName = measure;		
	}
	
	public DisplayMeasure(String measure, String measureNumber) {
		this.measureName = measure;		
		this.measureNumber = measureNumber;
	}
	
	public String getMeasureName() {
		return measureName;
	}

	public void setMeasureName(String measureName) {
		this.measureName = measureName;
	}

	public Boolean getExcludeFromPQRI() {
		return excludeFromPQRI;
	}

	public void setExcludeFromPQRI(Boolean excludeFromPQRI) {
		this.excludeFromPQRI = excludeFromPQRI;
	}

	public void setMeasureNumber(String measureNumber) {
		this.measureNumber = measureNumber;
	}

	public String getMeasureNumber() {
		return measureNumber;
	}

	public Boolean getEmergencyDepartmentMeasure() {
		return emergencyDepartmentMeasure;
	}


	public void setEmergencyDepartmentMeasure(Boolean emergencyDepartmentMeasure) {
		this.emergencyDepartmentMeasure = emergencyDepartmentMeasure;
	}

	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getNumerator() {
		return numerator;
	}
	public void setNumerator(String numerator) {
		this.numerator = numerator;
	}
	public String getDenominator() {
		return denominator;
	}
	public void setDenominator(String denominator) {
		this.denominator = denominator;
	}
	public String getPercentage() {
		return percentage;
	}
	public void setPercentage(String percentage) {
		this.percentage = percentage;
	}

	public Integer getExclusions() {
		return exclusions;
	}
	
	public String getExclusionsStr() {
		return String.valueOf(exclusions);
	}

	public void setExclusions(Integer exclusions) {
		this.exclusions = exclusions;
	}

	public Integer getPerformanceNotMet() {
		return performanceNotMet;
	}
	
	public String getPerformanceNotMetStr() {
		return String.valueOf(performanceNotMet);
	}


	public void setPerformanceNotMet(Integer performanceNotMet) {
		this.performanceNotMet = performanceNotMet;
	}


}
	