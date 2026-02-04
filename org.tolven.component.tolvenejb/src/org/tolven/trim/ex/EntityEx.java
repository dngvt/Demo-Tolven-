package org.tolven.trim.ex;

import java.util.List;

import org.tolven.trim.Compute;
import org.tolven.trim.ENSlot;
import org.tolven.trim.Entity;
import org.tolven.trim.EntityStatus;
import org.tolven.trim.LanguageCommunication;
import org.tolven.trim.RealmCode;
import org.tolven.trim.Role;

@SuppressWarnings("serial")
public class EntityEx extends Entity {

	private static final long serialVersionUID = 1L;

	public Boolean getEnabled() {
		return enabled;
	}
	
	
	/**
	 * Make an attempt to return the name of the entity
	 * @return A concatenated formatted name or null if none.
	 */
	public String getEntityName() {
		ENSlot slot = getName();
		// If no name slot, no name
		if (slot==null) return null;
		// If no ENs in the slot, then no name.
		if (slot.getENS().size()==0) return null;
		// The first EN will be fine
		ENEx en = (ENEx) slot.getENS().get(0);
		return en.getFormatted();
	}
	
	/**
	 * Set the statusCode using a string to create the enum 
	 * @param code
	 */
	public void setStatusCodeValue(String code) {
		if (code==null) {
			this.setStatusCode(null);
		} else {
			this.setStatusCode(EntityStatus.fromValue(code));
		}
	}
	
	/**
	 * Get the string value of the statusCode 
	 * @return A string containing the statusCode value
	 */
	public String getStatusCodeValue( ) {
		if (this.getStatusCode()==null) return null;
		return this.getStatusCode().value();
	}
	
    public void setAccountShares(List<String> accountShares) {
		this.accountShares = accountShares;
	}
    
    public void blend( Entity entityInclude ) {
    	if (entityInclude!=null) {
	    	TrimFactory.blend(this, entityInclude);
			if (this.getClassCode()==null) this.setClassCode(entityInclude.getClassCode());
			if (this.getCode()==null) this.setCode(entityInclude.getCode());
			// Blend all computes
			for (Compute compute : entityInclude.getComputes()) {
				this.getComputes().add(compute);
			}
			if (this.getDesc()==null) this.setDesc(entityInclude.getDesc());
			if (this.getDeterminerCode()==null) setDeterminerCode(entityInclude.getDeterminerCode());
			if (this.getEnabled()==null) setEnabled(entityInclude.isEnabled());
			if (this.getExistenceTime()==null) setExistenceTime(entityInclude.getExistenceTime());
			if (this.getHandlingCode()==null) setHandlingCode(entityInclude.getHandlingCode());
			// Blend languages
			for (LanguageCommunication lang : entityInclude.getLanguageCommunications()) {
				getLanguageCommunications().add(lang);
			}
			if (this.getId()==null) setId(entityInclude.getId());
			if (this.getName()==null) setName(entityInclude.getName());
			if (this.getQuantity()==null) setQuantity(entityInclude.getQuantity());
			for (RealmCode rc : entityInclude.getRealmCodes()) {
				getRealmCodes().add(rc);
			}
			if (this.getRiskCode()==null) setRiskCode(entityInclude.getRiskCode());
			if (this.getStatusCode()==null) setStatusCode(entityInclude.getStatusCode());
			if (this.getTelecom()==null) setTelecom(entityInclude.getTelecom());
			// We can't blend players and scopers because there is no named relationship so
			// no way to tell which role is which. So, instead, we just add them together.
			for (Role role : entityInclude.getPlayedRoles()) {
				getPlayedRoles().add(role);
			}
			for (Role role : entityInclude.getScopedRoles()) {
				getScopedRoles().add(role);
			}
			// Each specialization is also blended
			if (getContainer()!=null) {
				((ContainerEx)getContainer()).blend( entityInclude.getContainer());
			} else {
				setContainer( entityInclude.getContainer());
			}
			if (getDevice()!=null) {
				((DeviceEx)getDevice()).blend( entityInclude.getDevice());
			} else {
				setDevice( entityInclude.getDevice());
			}
			if (getLivingSubject()!=null) {
				((LivingSubjectEx)getLivingSubject()).blend( entityInclude.getLivingSubject());
			} else {
				setLivingSubject( entityInclude.getLivingSubject());
			}
			if (getManufacturedMaterial()!=null) {
				((ManufacturedMaterialEx)getManufacturedMaterial()).blend( entityInclude.getManufacturedMaterial());
			} else {
				setManufacturedMaterial( entityInclude.getManufacturedMaterial());
			}
			if (getMaterial()!=null) {
				((MaterialEx)getMaterial()).blend( entityInclude.getMaterial());
			} else {
				setMaterial( entityInclude.getMaterial());
			}
			if (getNonPersonLivingSubject()!=null) {
				((NonPersonLivingSubjectEx)getNonPersonLivingSubject()).blend( entityInclude.getNonPersonLivingSubject());
			} else {
				setNonPersonLivingSubject( entityInclude.getNonPersonLivingSubject());
			}
			if (getOrganization()!=null) {
				((OrganizationEx)getOrganization()).blend( entityInclude.getOrganization());
			} else {
				setOrganization( entityInclude.getOrganization());
			}
			if (getPerson()!=null) {
				((PersonEx)getPerson()).blend( entityInclude.getPerson());
			} else {
				setPerson( entityInclude.getPerson());
			}
			if (getPlace()!=null) {
				((PlaceEx)getPlace()).blend( entityInclude.getPlace());
			} else {
				setPlace( entityInclude.getPlace());
			}
    	}
    }

}
