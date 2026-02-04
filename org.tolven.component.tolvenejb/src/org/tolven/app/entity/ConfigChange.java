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

/** Entity that records a configuration change in menustructure
 * @author skandula
 *
 */
@Entity
@Table
public class ConfigChange {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "APP_SEQ_GEN")
	private long id;
	@ManyToOne(fetch = FetchType.LAZY)
	private AccountMenuStructure menuStructure;
	@Column
	private String heading;
	@Column
	private String newHeading; // used to move the data among the extended fields
	
	@Column
	private String oldLocation;
	@Column
	private String newLocation;
	@Column
	private boolean skipMigration;
	
	public boolean isSkipMigration() {
		return skipMigration;
	}
	public void setSkipMigration(boolean skipMigration) {
		this.skipMigration = skipMigration;
	}
	//created date is needed to process the migration in order
	@Column
	private Date createdDate;
	
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public AccountMenuStructure getMenuStructure() {
		return menuStructure;
	}
	public void setMenuStructure(AccountMenuStructure menuStructure) {
		this.menuStructure = menuStructure;
	}
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}
	
	public String getOldLocation() {
		return oldLocation;
	}
	public void setOldLocation(String oldLocation) {
		this.oldLocation = oldLocation;
	}
	public String getNewLocation() {
		return newLocation;
	}
	public void setNewLocation(String newLocation) {
		this.newLocation = newLocation;
	}
	public String toString() {
		return new StringBuilder().append("Id").append(this.getId()).append("  MenuStructure:").append(getMenuStructure()).append(" Column :")
				.append(getHeading()).append("internal mapping changed from ").append(getOldLocation()).append(" to ").append(getNewLocation()).toString();
		/*else
			return String.format("MenuStructure:%s's column Column:%s internal changed from %s to %s" , 
					getMenuStructure().getPath(),getHeading(),getOldLocation(),getNewLocation());*/
	}
	public String getNewHeading() {
		if(this.newHeading == null)
			return heading;
		return newHeading;
	}
	public void setNewHeading(String newHeading) {
		this.newHeading = newHeading;
	}
}
