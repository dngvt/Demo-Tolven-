/**
 * 
 */
package org.tolven.surescripts.entity;

import java.io.Serializable;

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
@Table(name="surescripts_user_credentials" , schema="surescripts")
public class SurescriptsUserCredentials implements Serializable {

	/**
	 * Auto Generated Serial Version ID
	 */
	private static final long serialVersionUID = -1302841586482246458L;
	/**
	 * Default Constructor
	 */
	public SurescriptsUserCredentials(){
		super();
	}
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="SURESCRIPTS_SEQ_GEN")
	@Column(name="id")
	private Long id;
	@Column(name="username")
	private String username;
	@Column(name="password")
	private String password;
	@Column(name="original_password")
	private String originalPassword;
	@Column(name="message_type")
	private String messageType;
	@Column(name="direction")
	private String direction;
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the originalPassword
	 */
	public String getOriginalPassword() {
		return originalPassword;
	}
	/**
	 * @param originalPassword the originalPassword to set
	 */
	public void setOriginalPassword(String originalPassword) {
		this.originalPassword = originalPassword;
	}
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
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}
	/**
	 * @param messageType the messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	/**
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}
	/**
	 * @param direction the direction to set
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}
}