/*
 *  Copyright (C) 2006 Tolven Inc 
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * Contact: info@tolvenhealth.com
 */
package org.tolven.web;

import java.util.List;
import java.util.Map;

import javax.ejb.EJB;

import org.tolven.app.AutomatedMeasureCalcInterface;
import org.tolven.app.CalculatedQualityMeasuresInterface;
import org.tolven.app.DisplayMeasure;
import org.tolven.app.HCQMMeasuresInterface;

/**
 * Faces backing bean to create SQLs for Automated measure calculation
 * 
 * @author John Churin
 * 
 */
public class MUAction extends MenuAction {

	@EJB
	private AutomatedMeasureCalcInterface calcInterface;
	private List<DisplayMeasure> measures;

	@EJB
	private CalculatedQualityMeasuresInterface qualityCalcInterface;
	private List<DisplayMeasure> qualityMeasures;
	private String cqmpqri;
	
	@EJB
	private HCQMMeasuresInterface hcqmInterface;
	private List<DisplayMeasure> hcqmMeasures;
	private String hcqmpqri;
	
	private boolean failedSql;
	private Map<String, String> sqlMap = null;


	public String getCqmpqri() {
		cqmpqri = qualityCalcInterface.getPQRI(getQualityMeasures());
		return cqmpqri;
	}

	public void setCqmpqri(String cqmpqri) {
		this.cqmpqri = cqmpqri;
	}


	public String getHcqmpqri() {
		hcqmpqri = hcqmInterface.getPQRI(getHcqmMeasures());
		return hcqmpqri;
	}

	public void setHcqmpqri(String hcqmpqri) {
		this.hcqmpqri = hcqmpqri;
	}

	public List<DisplayMeasure> getHcqmMeasures() {
		if (this.hcqmMeasures == null) {
			hcqmMeasures = (List<DisplayMeasure>) hcqmInterface.getMeasures();
		}
		return hcqmMeasures;
	}

	public void setHcqmMeasures(List<DisplayMeasure> hcqmMeasures) {
		this.hcqmMeasures = hcqmMeasures;
	}

	public boolean isFailedSql() {
		return failedSql;
	}

	public void setFailedSql(boolean failedSql) {
		this.failedSql = failedSql;
	}


	public Map<String, String> getSqlMap() {
		return sqlMap;
	}

	public void setSqlMap(Map<String, String> sqlMap) {
		this.sqlMap = sqlMap;
	}

	public void setMeasures(List<DisplayMeasure> measures) {
		this.measures = measures;
	}

	public Map<String, String> getPlaceholderSQL() {
		if (this.sqlMap == null) {
			sqlMap = calcInterface.buildSql();
		}
		return sqlMap;
	}

	public List<DisplayMeasure> getMeasures() {
		if (this.measures == null) {
			measures = (List<DisplayMeasure>) calcInterface.getMeasures();
		}
		return measures;
	}

	public void setQualityMeasures(List<DisplayMeasure> qualityMeasures) {
		this.qualityMeasures = qualityMeasures;
	}

	public List<DisplayMeasure> getQualityMeasures() {
		if (this.qualityMeasures == null) {
			qualityMeasures = (List<DisplayMeasure>) qualityCalcInterface.getMeasures();
		}
		return qualityMeasures;
	}
}