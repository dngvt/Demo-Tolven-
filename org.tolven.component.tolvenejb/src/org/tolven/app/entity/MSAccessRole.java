package org.tolven.app.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table
public class MSAccessRole implements Serializable{
	@Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="APP_SEQ_GEN")
    private long id;
   
	@ManyToOne()
    private AccountMenuStructure menuStructure;
  
	@Column
    private String role;
	
	@Column
	private Boolean allowed = true;
	
	public Boolean getAllowed() {
		return allowed;
	}
	public void setAllowed(Boolean allowed) {
		this.allowed = allowed;
	}
	public AccountMenuStructure getMenuStructure() {
		return menuStructure;
	}
	public void setMenuStructure(AccountMenuStructure menuStructure) {
		this.menuStructure = menuStructure;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}	
	/**
     * Privide a debug string representation for this object.
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	StringBuffer sb = new StringBuffer(200);
    	if(getMenuStructure() != null){
            sb.append( " MenuStructure: "); sb.append(getMenuStructure().getId());
            sb.append( " path: "); sb.append(getMenuStructure().getPath());
            sb.append( " role: "); sb.append(getRole());
            sb.append( " allowed: "); sb.append(getAllowed());
    	}
        return sb.toString();
    }
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof MSAccessRole){
    		MSAccessRole compareObj = (MSAccessRole)obj;
    		if(compareObj.getMenuStructure().getId() != this.getMenuStructure().getId())
    			return false;
    		if(!compareObj.getRole().equalsIgnoreCase(this.getRole())){
    			return false;
    		}
    		return true;
    	}
    	return false;
    }
    @Override
    public int hashCode() {
    	return getRole().hashCode();
    }
    
}
