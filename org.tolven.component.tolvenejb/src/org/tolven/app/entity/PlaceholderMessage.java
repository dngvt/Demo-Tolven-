package org.tolven.app.entity;

import java.io.Serializable;

import org.tolven.doc.bean.TolvenMessage;

public class PlaceholderMessage extends TolvenMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long placeholderId;
	
	public PlaceholderMessage(MenuData md) {
		setAccountId(md.getAccount().getId());
		placeholderId = md.getId();
	}

	public long getPlaceholderId() {
		return placeholderId;
	}

	public void setPlaceholderId(long placeholderId) {
		this.placeholderId = placeholderId;
	}

}
