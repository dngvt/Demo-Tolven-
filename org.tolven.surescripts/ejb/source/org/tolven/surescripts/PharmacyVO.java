package org.tolven.surescripts;

import java.io.Serializable;
import java.util.Date;

public class PharmacyVO implements Serializable{

	private static final long serialVersionUID = 392692423526811027L;
	
	private String ncpdpid;
	private String storeNumber;
	private Long referenceNumberAlt1;
	private String referenceNumberAlt1Qualifier;
	private String storeName;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String state;
	private Long zip;
	private String phonePrimary;
	private String fax;
	private String email;
	private String phoneAlt1;
	private String phoneAlt1Qualifier;
	private String phoneAlt2;
	private String phoneAlt2Qualifier;
	private String phoneAlt3;
	private String phoneAlt3Qualifier;
	private String phoneAlt4;
	private String phoneAlt4Qualifier;
	private String phoneAlt5;
	private String phoneAlt5Qualifier;
    private Date activeStartTime;
    private Date activeEndTime;
	private Long serviceLevel;
	private String partnerAccount;
    private Date lastModifiedDate;
	private String twentyFourHourFlag;
	private String crossStreet;
	private String recordChange;
	private Long oldServiceLevel;
	private String textServiceLevel;
	private String textServiceLevelChange;
	private String version;
	private Long npi;
    private Boolean newRx;
    private Boolean refReq;
    private Boolean rxChg;
    private Boolean rxFill;
    private Boolean canRx;
    private Boolean medicatioHistory;
    private Boolean eligibility;
	private String status;
	
	/**
	 * @return the ncpdpid
	 */
	public String getNcpdpid() {
		return ncpdpid;
	}
	/**
	 * @param ncpdpid the ncpdpid to set
	 */
	public void setNcpdpid(String ncpdpid) {
		this.ncpdpid = ncpdpid;
	}
	/**
	 * @return the storeNumber
	 */
	public String getStoreNumber() {
		return storeNumber;
	}
	/**
	 * @param storeNumber the storeNumber to set
	 */
	public void setStoreNumber(String storeNumber) {
		this.storeNumber = storeNumber;
	}
	/**
	 * @return the referenceNumberAlt1
	 */
	public Long getReferenceNumberAlt1() {
		return referenceNumberAlt1;
	}
	/**
	 * @param referenceNumberAlt1 the referenceNumberAlt1 to set
	 */
	public void setReferenceNumberAlt1(Long referenceNumberAlt1) {
		this.referenceNumberAlt1 = referenceNumberAlt1;
	}
	/**
	 * @return the referenceNumberAlt1Qualifier
	 */
	public String getReferenceNumberAlt1Qualifier() {
		return referenceNumberAlt1Qualifier;
	}
	/**
	 * @param referenceNumberAlt1Qualifier the referenceNumberAlt1Qualifier to set
	 */
	public void setReferenceNumberAlt1Qualifier(String referenceNumberAlt1Qualifier) {
		this.referenceNumberAlt1Qualifier = referenceNumberAlt1Qualifier;
	}
	/**
	 * @return the storeName
	 */
	public String getStoreName() {
		return storeName;
	}
	/**
	 * @param storeName the storeName to set
	 */
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	/**
	 * @return the addressLine1
	 */
	public String getAddressLine1() {
		return addressLine1;
	}
	/**
	 * @param addressLine1 the addressLine1 to set
	 */
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}
	/**
	 * @return the addressLine2
	 */
	public String getAddressLine2() {
		return addressLine2;
	}
	/**
	 * @param addressLine2 the addressLine2 to set
	 */
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}
	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}
	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}
	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
	/**
	 * @return the zip
	 */
	public Long getZip() {
		return zip;
	}
	/**
	 * @param zip the zip to set
	 */
	public void setZip(Long zip) {
		this.zip = zip;
	}
	/**
	 * @return the phonePrimary
	 */
	public String getPhonePrimary() {
		return phonePrimary;
	}
	/**
	 * @param phonePrimary the phonePrimary to set
	 */
	public void setPhonePrimary(String phonePrimary) {
		this.phonePrimary = phonePrimary;
	}
	/**
	 * @return the fax
	 */
	public String getFax() {
		return fax;
	}
	/**
	 * @param fax the fax to set
	 */
	public void setFax(String fax) {
		this.fax = fax;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the phoneAlt1
	 */
	public String getPhoneAlt1() {
		return phoneAlt1;
	}
	/**
	 * @param phoneAlt1 the phoneAlt1 to set
	 */
	public void setPhoneAlt1(String phoneAlt1) {
		this.phoneAlt1 = phoneAlt1;
	}
	/**
	 * @return the phoneAlt1Qualifier
	 */
	public String getPhoneAlt1Qualifier() {
		return phoneAlt1Qualifier;
	}
	/**
	 * @param phoneAlt1Qualifier the phoneAlt1Qualifier to set
	 */
	public void setPhoneAlt1Qualifier(String phoneAlt1Qualifier) {
		this.phoneAlt1Qualifier = phoneAlt1Qualifier;
	}
	/**
	 * @return the phoneAlt2
	 */
	public String getPhoneAlt2() {
		return phoneAlt2;
	}
	/**
	 * @param phoneAlt2 the phoneAlt2 to set
	 */
	public void setPhoneAlt2(String phoneAlt2) {
		this.phoneAlt2 = phoneAlt2;
	}
	/**
	 * @return the phoneAlt2Qualifier
	 */
	public String getPhoneAlt2Qualifier() {
		return phoneAlt2Qualifier;
	}
	/**
	 * @param phoneAlt2Qualifier the phoneAlt2Qualifier to set
	 */
	public void setPhoneAlt2Qualifier(String phoneAlt2Qualifier) {
		this.phoneAlt2Qualifier = phoneAlt2Qualifier;
	}
	/**
	 * @return the phoneAlt3
	 */
	public String getPhoneAlt3() {
		return phoneAlt3;
	}
	/**
	 * @param phoneAlt3 the phoneAlt3 to set
	 */
	public void setPhoneAlt3(String phoneAlt3) {
		this.phoneAlt3 = phoneAlt3;
	}
	/**
	 * @return the phoneAlt3Qualifier
	 */
	public String getPhoneAlt3Qualifier() {
		return phoneAlt3Qualifier;
	}
	/**
	 * @param phoneAlt3Qualifier the phoneAlt3Qualifier to set
	 */
	public void setPhoneAlt3Qualifier(String phoneAlt3Qualifier) {
		this.phoneAlt3Qualifier = phoneAlt3Qualifier;
	}
	/**
	 * @return the phoneAlt4
	 */
	public String getPhoneAlt4() {
		return phoneAlt4;
	}
	/**
	 * @param phoneAlt4 the phoneAlt4 to set
	 */
	public void setPhoneAlt4(String phoneAlt4) {
		this.phoneAlt4 = phoneAlt4;
	}
	/**
	 * @return the phoneAlt4Qualifier
	 */
	public String getPhoneAlt4Qualifier() {
		return phoneAlt4Qualifier;
	}
	/**
	 * @param phoneAlt4Qualifier the phoneAlt4Qualifier to set
	 */
	public void setPhoneAlt4Qualifier(String phoneAlt4Qualifier) {
		this.phoneAlt4Qualifier = phoneAlt4Qualifier;
	}
	/**
	 * @return the phoneAlt5
	 */
	public String getPhoneAlt5() {
		return phoneAlt5;
	}
	/**
	 * @param phoneAlt5 the phoneAlt5 to set
	 */
	public void setPhoneAlt5(String phoneAlt5) {
		this.phoneAlt5 = phoneAlt5;
	}
	/**
	 * @return the phoneAlt5Qualifier
	 */
	public String getPhoneAlt5Qualifier() {
		return phoneAlt5Qualifier;
	}
	/**
	 * @param phoneAlt5Qualifier the phoneAlt5Qualifier to set
	 */
	public void setPhoneAlt5Qualifier(String phoneAlt5Qualifier) {
		this.phoneAlt5Qualifier = phoneAlt5Qualifier;
	}
	/**
	 * @return the activeStartTime
	 */
	public Date getActiveStartTime() {
		return activeStartTime;
	}
	/**
	 * @param activeStartTime the activeStartTime to set
	 */
	public void setActiveStartTime(Date activeStartTime) {
		this.activeStartTime = activeStartTime;
	}
	/**
	 * @return the activeEndTime
	 */
	public Date getActiveEndTime() {
		return activeEndTime;
	}
	/**
	 * @param activeEndTime the activeEndTime to set
	 */
	public void setActiveEndTime(Date activeEndTime) {
		this.activeEndTime = activeEndTime;
	}
	/**
	 * @return the serviceLevel
	 */
	public Long getServiceLevel() {
		return serviceLevel;
	}
	/**
	 * @param serviceLevel the serviceLevel to set
	 */
	public void setServiceLevel(Long serviceLevel) {
		this.serviceLevel = serviceLevel;
	}
	/**
	 * @return the partnerAccount
	 */
	public String getPartnerAccount() {
		return partnerAccount;
	}
	/**
	 * @param partnerAccount the partnerAccount to set
	 */
	public void setPartnerAccount(String partnerAccount) {
		this.partnerAccount = partnerAccount;
	}
	/**
	 * @return the lastModifiedDate
	 */
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	/**
	 * @param lastModifiedDate the lastModifiedDate to set
	 */
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	/**
	 * @return the twentyFourHourFlag
	 */
	public String getTwentyFourHourFlag() {
		return twentyFourHourFlag;
	}
	/**
	 * @param twentyFourHourFlag the twentyFourHourFlag to set
	 */
	public void setTwentyFourHourFlag(String twentyFourHourFlag) {
		this.twentyFourHourFlag = twentyFourHourFlag;
	}
	/**
	 * @return the crossStreet
	 */
	public String getCrossStreet() {
		return crossStreet;
	}
	/**
	 * @param crossStreet the crossStreet to set
	 */
	public void setCrossStreet(String crossStreet) {
		this.crossStreet = crossStreet;
	}
	/**
	 * @return the recordChange
	 */
	public String getRecordChange() {
		return recordChange;
	}
	/**
	 * @param recordChange the recordChange to set
	 */
	public void setRecordChange(String recordChange) {
		this.recordChange = recordChange;
	}
	/**
	 * @return the oldServiceLevel
	 */
	public Long getOldServiceLevel() {
		return oldServiceLevel;
	}
	/**
	 * @param oldServiceLevel the oldServiceLevel to set
	 */
	public void setOldServiceLevel(Long oldServiceLevel) {
		this.oldServiceLevel = oldServiceLevel;
	}
	/**
	 * @return the textServiceLevel
	 */
	public String getTextServiceLevel() {
		return textServiceLevel;
	}
	/**
	 * @param textServiceLevel the textServiceLevel to set
	 */
	public void setTextServiceLevel(String textServiceLevel) {
		this.textServiceLevel = textServiceLevel;
	}
	/**
	 * @return the textServiceLevelChange
	 */
	public String getTextServiceLevelChange() {
		return textServiceLevelChange;
	}
	/**
	 * @param textServiceLevelChange the textServiceLevelChange to set
	 */
	public void setTextServiceLevelChange(String textServiceLevelChange) {
		this.textServiceLevelChange = textServiceLevelChange;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the npi
	 */
	public Long getNpi() {
		return npi;
	}
	/**
	 * @param npi the npi to set
	 */
	public void setNpi(Long npi) {
		this.npi = npi;
	}
	/**
	 * @return the newRx
	 */
	public Boolean getNewRx() {
		return newRx;
	}
	/**
	 * @param newRx the newRx to set
	 */
	public void setNewRx(Boolean newRx) {
		this.newRx = newRx;
	}
	/**
	 * @return the refReq
	 */
	public Boolean getRefReq() {
		return refReq;
	}
	/**
	 * @param refReq the refReq to set
	 */
	public void setRefReq(Boolean refReq) {
		this.refReq = refReq;
	}
	/**
	 * @return the rxChg
	 */
	public Boolean getRxChg() {
		return rxChg;
	}
	/**
	 * @param rxChg the rxChg to set
	 */
	public void setRxChg(Boolean rxChg) {
		this.rxChg = rxChg;
	}
	/**
	 * @return the rxFill
	 */
	public Boolean getRxFill() {
		return rxFill;
	}
	/**
	 * @param rxFill the rxFill to set
	 */
	public void setRxFill(Boolean rxFill) {
		this.rxFill = rxFill;
	}
	/**
	 * @return the canRx
	 */
	public Boolean getCanRx() {
		return canRx;
	}
	/**
	 * @param canRx the canRx to set
	 */
	public void setCanRx(Boolean canRx) {
		this.canRx = canRx;
	}
	/**
	 * @return the medicatioHistory
	 */
	public Boolean getMedicatioHistory() {
		return medicatioHistory;
	}
	/**
	 * @param medicatioHistory the medicatioHistory to set
	 */
	public void setMedicatioHistory(Boolean medicatioHistory) {
		this.medicatioHistory = medicatioHistory;
	}
	/**
	 * @return the eligibility
	 */
	public Boolean getEligibility() {
		return eligibility;
	}
	/**
	 * @param eligibility the eligibility to set
	 */
	public void setEligibility(Boolean eligibility) {
		this.eligibility = eligibility;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
