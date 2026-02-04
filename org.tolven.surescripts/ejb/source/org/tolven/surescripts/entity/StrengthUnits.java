package org.tolven.surescripts.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="strength_units" , schema="surescripts")
public class StrengthUnits implements Serializable{

	private static final long serialVersionUID = -9027924570428154963L;
	
	@Id
	private long id;
	
	@Column
	private String fdbUnit;
	
	@Column
	private String unit;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getFdbUnit() {
		return fdbUnit;
	}
	public void setFdbUnit(String fdbUnit) {
		this.fdbUnit = fdbUnit;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
}
