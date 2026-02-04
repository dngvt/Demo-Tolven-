

package org.tolven.surescripts;


import java.io.Serializable;

import org.tolven.surescripts.MessageType;

/**
 * @author root
 *
 */
public class MessageHolder implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8687168107118483046L;
	private MessageType message;

	public MessageType getMessage() {
		return message;
	}

	public void setMessage(MessageType message) {
		this.message = message;
	}

}
