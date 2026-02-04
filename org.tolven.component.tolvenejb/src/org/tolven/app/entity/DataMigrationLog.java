package org.tolven.app.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** Entity that records migration changes
 * @author skandula
 *
 */
@Entity
@Table
public class DataMigrationLog {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "APP_SEQ_GEN")
	private long id;
	
	@Column
	private long menuData;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private ConfigChange change;
	

	@Column
	private String heading;
	@Column
	private String oldValue; 
	
	@Column
	private String newValue;
	@Column
	private Boolean rolledback;


	@Column
	private Date createdDate; 
	
	public DataMigrationLog(long md,ConfigChange change,Date date){
		this.menuData = md;
		this.change = change;
		this.createdDate = date;
	}

	public DataMigrationLog(){
		
	}

	public ConfigChange getChange() {
		return change;
	}

	public void setChange(ConfigChange change) {
		this.change = change;
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	
	public String getHeading() {
		return heading;
	}

	public void setHeading(String heading) {
		this.heading = heading;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public long getMenuData() {
		return menuData;
	}

	public void setMenuData(long menuData) {
		this.menuData = menuData;
	}
	
	
	public Boolean getRolledback() {
		return rolledback;
	}

	public void setRolledback(Boolean rolledback) {
		this.rolledback = rolledback;
	}	
}
