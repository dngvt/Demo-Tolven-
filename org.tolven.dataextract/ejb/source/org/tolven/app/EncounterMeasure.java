package org.tolven.app;

import java.util.List;

public class EncounterMeasure {
	private String pqriMeasureNumber;
	private String measureName;
	private int totalX;
	private int totalD;
	private int totalN;
	private List<HCQMMeasure> hcqmMeasures;
	
	public EncounterMeasure(int countX, int countD, int countN, String pqrimeasurenumber) {
		super();
		this.totalX = countX;
		this.totalD = countD;
		this.totalN = countN;
		this.pqriMeasureNumber = pqrimeasurenumber;
	}
	
	public EncounterMeasure(String pqrimeasurenumber, List<HCQMMeasure> hcqmMeasures) {
		super();
		this.pqriMeasureNumber = pqrimeasurenumber;
		this.hcqmMeasures = hcqmMeasures;
	}
	
	public EncounterMeasure(String measureName, String pqrimeasurenumber, List<HCQMMeasure> hcqmMeasures) {
		super();
		this.pqriMeasureNumber = pqrimeasurenumber;
		this.hcqmMeasures = hcqmMeasures;
		this.measureName = measureName;
	}
	
	public String getMeasureName() {
		return measureName;
	}

	public void setMeasureName(String measureName) {
		this.measureName = measureName;
	}

	public String getPqrimeasurenumber() {
		return pqriMeasureNumber;
	}

	public void setPqrimeasurenumber(String pqrimeasurenumber) {
		this.pqriMeasureNumber = pqrimeasurenumber;
	}

	public int getTotalX() {
		return totalX;
	}

	public void setTotalX(int totalX) {
		this.totalX = totalX;
	}

	public int getTotalD() {
		return totalD;
	}

	public void setTotalD(int totalD) {
		this.totalD = totalD;
	}

	public int getTotalN() {
		return totalN;
	}

	public void setTotalN(int totalN) {
		this.totalN = totalN;
	}

	public List<HCQMMeasure> getHcqmMeasures() {
		return hcqmMeasures;
	}

	public void setHcqmMeasures(List<HCQMMeasure> hcqmMeasures) {
		this.hcqmMeasures = hcqmMeasures;
	}
	
	

}
