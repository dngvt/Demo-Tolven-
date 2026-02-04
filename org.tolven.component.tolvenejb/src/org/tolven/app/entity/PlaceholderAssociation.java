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
public class PlaceholderAssociation implements Serializable {
	@Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="APP_SEQ_GEN")
    private long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private MenuData placeholder;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private MenuData associatedPlaceholder;
	
	@Column
	private String associationName;
	
	public MenuData getPlaceholder() {
		return placeholder;
	}
	public void setPlaceholder(MenuData placeholder) {
		this.placeholder = placeholder;
	}
	public MenuData getAssociatedPlaceholder() {
		return associatedPlaceholder;
	}
	public void setAssociatedPlaceholder(MenuData associatedPlaceholder) {
		this.associatedPlaceholder = associatedPlaceholder;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}	
	public String getAssociationName() {
		return associationName;
	}
	public void setAssociationName(String associationName) {
		this.associationName = associationName;
	}	
}
