package org.tolven.surescripts.entity;

import java.io.Serializable;
import java.sql.Timestamp;

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
@Table(name="message_details" , schema="surescripts")
public class MessageDetails implements Serializable {

	/**
	 * Auto Generated Serial Version Id
	 */
	private static final long serialVersionUID = -9023179240088900511L;
	/**
	 * Default Constructor
	 */
	public MessageDetails(){
		super();
	}
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="SURESCRIPTS_SEQ_GEN")
	@Column(name="id")
	private Long id;
	@Column(name="message_id")
	private String messageId;
	@Column(name="relates_to_messageId")
	private String relatesToMessageId;
	@Column(name="status")
	private String status;
	@Column(name="message_type")
	private String messageType;
	@Column(name="message" ,length=10000)
	private String message;
	@Column(name="recieved_time")
	private Timestamp recievedTime;
	@Column(name="direction")
	private String direction;
	@Column(name="message_info_id")
	private Long msgInfoId;
	@Column(name="presc_order_num")
	private Long prescOrderNum;
	@Column(name="account_id")
	private Long accountId;
	
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
	 * @return the messageId
	 */
	public String getMessageId() {
		return messageId;
	}
	/**
	 * @param messageId the messageId to set
	 */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	/**
	 * @return the relatesToMessageId
	 */
	public String getRelatesToMessageId() {
		return relatesToMessageId;
	}
	/**
	 * @param relatesToMessageId the relatesToMessageId to set
	 */
	public void setRelatesToMessageId(String relatesToMessageId) {
		this.relatesToMessageId = relatesToMessageId;
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
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the recievedTime
	 */
	public Timestamp getRecievedTime() {
		return recievedTime;
	}
	/**
	 * @param recievedTime the recievedTime to set
	 */
	public void setRecievedTime(Timestamp recievedTime) {
		this.recievedTime = recievedTime;
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
	/**
	 * @return the msgInfoId
	 */
	public Long getMsgInfoId() {
		return msgInfoId;
	}
	/**
	 * @param msgInfoId the msgInfoId to set
	 */
	public void setMsgInfoId(Long msgInfoId) {
		this.msgInfoId = msgInfoId;
	}
	/**
	 * @return the prescOrderNum
	 */
	public Long getPrescOrderNum() {
		return prescOrderNum;
	}
	/**
	 * @param prescOrderNum the prescOrderNum to set
	 */
	public void setPrescOrderNum(Long prescOrderNum) {
		this.prescOrderNum = prescOrderNum;
	}
	/**
	 * @return the accountId
	 */
	public Long getAccountId() {
		return accountId;
	}
	/**
	 * @param accountId the accountId to set
	 */
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	
}
