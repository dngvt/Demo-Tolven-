/**
 * 
 */
package org.tolven.surescripts.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author mohammed
 *
 */
@Entity
@Table(name="message_info" , schema="surescripts")
public class MessageInfos implements Serializable{
	
	/**
	 * Auto Generated Serial Version ID
	 */
	private static final long serialVersionUID = -3029028094964869149L;
	/**
	 * Default Constructor
	 */
	public MessageInfos(){
		super();
	}
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="SURESCRIPTS_SEQ_GEN")
	@Column(name="id")
	private Long id;
	@Column(name="spi")
	private Long spi;
	@Column(name="ncpdpid")
	private Long ncpdpid;
	@Column(name="pat_last_name")
	private String patientLastName;
	@Column(name="pat_first_name")
	private String patientFirstName;
	@Column(name="med_desc")
	private String medicineDescription;
	@Column(name="drug_qnty")
	private String drugQuantity;
	@Column(name="pat_dob")
	private Timestamp patientDOB;
	@Column(name="pat_gender")
	private String patientGender;
	@Column(name="presc_ordr_num")
	private Long prescOrderNum;
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the spi
	 */
	public Long getSpi() {
		return spi;
	}
	/**
	 * @param spi the spi to set
	 */
	public void setSpi(Long spi) {
		this.spi = spi;
	}
	/**
	 * @return the ncpdpid
	 */
	public Long getNcpdpid() {
		return ncpdpid;
	}
	/**
	 * @param ncpdpid the ncpdpid to set
	 */
	public void setNcpdpid(Long ncpdpid) {
		this.ncpdpid = ncpdpid;
	}
	/**
	 * @return the patientLastName
	 */
	public String getPatientLastName() {
		return patientLastName;
	}
	/**
	 * @param patientLastName the patientLastName to set
	 */
	public void setPatientLastName(String patientLastName) {
		this.patientLastName = patientLastName;
	}
	/**
	 * @return the patientFirstName
	 */
	public String getPatientFirstName() {
		return patientFirstName;
	}
	/**
	 * @param patientFirstName the patientFirstName to set
	 */
	public void setPatientFirstName(String patientFirstName) {
		this.patientFirstName = patientFirstName;
	}
	/**
	 * @return the medicineDescription
	 */
	public String getMedicineDescription() {
		return medicineDescription;
	}
	/**
	 * @param medicineDescription the medicineDescription to set
	 */
	public void setMedicineDescription(String medicineDescription) {
		this.medicineDescription = medicineDescription;
	}
	/**
	 * @return the patientDOB
	 */
	public Timestamp getPatientDOB() {
		return patientDOB;
	}
	/**
	 * @param patientDOB the patientDOB to set
	 */
	public void setPatientDOB(Timestamp patientDOB) {
		this.patientDOB = patientDOB;
	}
	/**
	 * @return the patientGender
	 */
	public String getPatientGender() {
		return patientGender;
	}
	/**
	 * @param patientGender the patientGender to set
	 */
	public void setPatientGender(String patientGender) {
		this.patientGender = patientGender;
	}
	/**
	 * @return the prescOrderNum
	 */
	public Long getPrescOrderNum() {
		return prescOrderNum;
	}
	/**
	 * @param prescOrderNum the prescOrderNum to set
	 */
	public void setPrescOrderNum(Long prescOrderNum) {
		this.prescOrderNum = prescOrderNum;
	}
	/**
	 * @return the drugQuantity
	 */
	public String getDrugQuantity() {
		return drugQuantity;
	}
	/**
	 * @param drugQuantity the drugQuantity to set
	 */
	public void setDrugQuantity(String drugQuantity) {
		this.drugQuantity = drugQuantity;
	}

	
}
