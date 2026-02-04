package org.tolven.trim.ex;

import java.util.List;
import java.util.Map;

import org.tolven.trim.Compute;
import org.tolven.trim.Role;
import org.tolven.trim.RoleParticipation;
import org.tolven.trim.RoleStatus;

@SuppressWarnings("serial")
public class RoleEx extends Role {
	private transient RoleParticipationMap rpMap = null;

	public Boolean getEnabled() {
		return enabled;
	}

	public Map<String, RoleParticipation> getParticipation() {
        if (rpMap == null) {
        	rpMap = new RoleParticipationMap( getParticipations());
        }
        return rpMap;
	}
	
	
	
	/**
	 * Set the statusCode using a string to create the enum 
	 * @param code
	 */
	public void setStatusCodeValue(String code) {
		if (code==null || code.equalsIgnoreCase("")) {
			this.setStatusCode(null);
		} else {
			this.setStatusCode(RoleStatus.fromValue(code));
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

    public void blend(Role roleInclude) {
    	TrimFactory.blend(this, roleInclude);
		RoleEx roleIncludeEx = (RoleEx) roleInclude;
		// Blend all computes
		for (Compute compute : roleInclude.getComputes()) {
			this.getComputes().add(compute);
		}
		if (this.getClassCode()==null) this.setClassCode(roleInclude.getClassCode());
		// Need to check raw property, not default getter in this case due to default processing in the default method
		if (this.getEnabled()==null && roleIncludeEx.getEnabled()!=null) this.setEnabled(roleInclude.isEnabled());
		if (this.getAccess()==null) this.setAccess(roleInclude.getAccess());
		if (this.getAddr()==null) this.setAddr(roleInclude.getAddr());
		if (this.getCertificateText()==null) this.setCertificateText(roleInclude.getCertificateText());
		if (this.getCode()==null) this.setCode(roleInclude.getCode());
		if (this.getConfidentiality()==null) this.setConfidentiality(roleInclude.getConfidentiality());
		if (this.getEffectiveTime()==null) this.setEffectiveTime(roleInclude.getEffectiveTime());
		if (this.getEmployee()==null) this.setEmployee(roleInclude.getEmployee());
		if (this.getId()==null) this.setId(roleInclude.getId());
		if (this.getLicensedEntity()==null) this.setLicensedEntity(roleInclude.getLicensedEntity());
		if (this.getName()==null) this.setName(roleInclude.getName());
		if (this.getNegationInd()==null) this.setNegationInd(roleInclude.getNegationInd());
		if (this.getPositionNumber()==null) this.setPositionNumber(roleInclude.getPositionNumber());
		if (this.getQuantity()==null) this.setQuantity(roleInclude.getQuantity());
		if (this.getStatusCode()==null) this.setStatusCode(roleInclude.getStatusCode());
		if (this.getTelecom()==null) this.setTelecom(roleInclude.getTelecom());
		if (getStatusCode()==null) setStatusCode(roleInclude.getStatusCode());
		// Blend or replace the player
		if (roleInclude.getPlayer()!=null) {
			if (this.getPlayer()==null) {
				this.setPlayer(roleInclude.getPlayer());
			} else {
				((EntityEx)this.getPlayer()).blend(roleInclude.getPlayer());
			}
		}
		// Blend or replace the scoper
		if (roleInclude.getScoper()!=null) {
			if (this.getScoper()==null) {
				this.setScoper(roleInclude.getScoper());
			} else {
				((EntityEx)this.getScoper()).blend(roleInclude.getScoper());
			}
		}
		for (RoleParticipation p : roleInclude.getParticipations()) {
			RoleParticipation plocal = this.getParticipation().get(p.getName());
			if (plocal !=null) {
				((RoleParticipationEx)plocal).blend(p);
			} else {
				getParticipations().add(p);
			}
		}
		if (getAccess()!=null) {
			((AccessEx)getAccess()).blend(roleInclude.getAccess());
		} else {
			setAccess( roleInclude.getAccess());
		}
		if (getEmployee()!=null) {
			((EmployeeEx)getEmployee()).blend(roleInclude.getEmployee());
		} else {
			setEmployee( roleInclude.getEmployee());
		}
		if (getLicensedEntity()!=null) {
			((LicensedEntityEx)getLicensedEntity()).blend(roleInclude.getLicensedEntity());
		} else {
			setLicensedEntity( roleInclude.getLicensedEntity());
		}
		if (getPatient()!=null) {
			((PatientEx)getPatient()).blend(roleInclude.getPatient());
		} else {
			setPatient( roleInclude.getPatient());
		}
		if (getQualifiedEntity()!=null) {
			((QualifiedEntityEx)getQualifiedEntity()).blend(roleInclude.getQualifiedEntity());
		} else {
			setQualifiedEntity( roleInclude.getQualifiedEntity());
		}

    }
}
