package org.tolven.app.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.tolven.core.entity.Account;

@Entity
@Table
public class PlaceholderTransition {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "APP_SEQ_GEN")
	private long id;

	@Column
	private String code;
	
	@Column
	private String codeSystem;
	
	@Column
	private String codeSystemName;
	
	@Column
	private String path;

	@ManyToOne(fetch = FetchType.LAZY)
	private TrimHeader trimHeader;
	
	@Column
	private String transitionName;
	
	@Column
	private String label;

	@Column
	private String fromStatus;

	@Column
	private String toStatus;

	
	

	/**
	 * Compare two PlaceholderTransformation objects for equality. MenuStructure
	 * is not included in the test because placeholder (MenuData) is more
	 * selective and implies MenuStructure.
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof PlaceholderTransition))
			return false;
		return true;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public TrimHeader getTrimHeader() {
		return trimHeader;
	}

	public void setTrimHeader(TrimHeader trimHeader) {
		this.trimHeader = trimHeader;
	}

	public String getTransitionName() {
		return transitionName;
	}

	public void setTransitionName(String transitionName) {
		this.transitionName = transitionName;
	}

	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getFromStatus() {
		return fromStatus;
	}

	public void setFromStatus(String fromStatus) {
		this.fromStatus = fromStatus;
	}

	public String getToStatus() {
		return toStatus;
	}

	public void setToStatus(String toStatus) {
		this.toStatus = toStatus;
	}

	/**
	 * Return a hash code for this object. Note: The hashCode is based on the
	 * the extension only which is adequate for a hash.
	 */
	public int hashCode() {
		if (getId() == 0)
			throw new IllegalStateException(
					"id not yet established in MenuData object");
		return new Long(getId()).hashCode();
	}


	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCodeSystem() {
		return codeSystem;
	}

	public void setCodeSystem(String codeSystem) {
		this.codeSystem = codeSystem;
	}

	public String getCodeSystemName() {
		return codeSystemName;
	}

	public void setCodeSystemName(String codeSystemName) {
		this.codeSystemName = codeSystemName;
	}
	
	

}
