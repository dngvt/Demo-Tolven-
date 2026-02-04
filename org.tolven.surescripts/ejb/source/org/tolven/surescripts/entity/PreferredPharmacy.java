package org.tolven.surescripts.entity;

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
import java.util.Date;

/**
 * This file contains servlet function for RHF.
 *
 * @package org.tolven.surescripts.entity
 * @author Suja Sundaresan <suja.sundaresan@cyrusxp.com>
 * @copyright Tolven Inc
 * @since File available since Release 0.0.15
 * added on 05/Dec/2010
 */
public class PreferredPharmacy {
	
	private String id;
	private String storeName;
	private String addressLine1;
	private String city;
	private String state;
	private Long zip;
    private Date activeEndTime;
	private Long serviceLevel;
    private Boolean newRx;
    private Boolean refReq;
    private Boolean rxChg;
    private Boolean rxFill;
    private Boolean canRx;
    private Boolean medicatioHistory;
    private Boolean eligibility;
    private Boolean inactive;
    private Boolean disabled;
	private long menuDataId;
	private String parentPath;
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the menuDataId
	 */
	public long getMenuDataId() {
		return menuDataId;
	}
	/**
	 * @param menuDataId the menuDataId to set
	 */
	public void setMenuDataId(long menuDataId) {
		this.menuDataId = menuDataId;
	}
	/**
	 * @return the parentPath
	 */
	public String getParentPath() {
		return parentPath;
	}
	/**
	 * @param parentPath the parentPath to set
	 */
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}
	/**
	 * @return the inactive
	 */
	public Boolean getInactive() {
		return inactive;
	}
	/**
	 * @param inactive the inactive to set
	 */
	public void setInactive(Boolean inactive) {
		this.inactive = inactive;
	}
	/**
	 * @return the disabled
	 */
	public Boolean getDisabled() {
		return disabled;
	}
	/**
	 * @param disabled the disabled to set
	 */
	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}
	
}
