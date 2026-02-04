/**
 * 
 */
package org.tolven.surescripts;

import java.io.Serializable;

import org.tolven.core.entity.AccountUser;

/**
 * @author mohammed
 *
 */
public class SurescriptsVO implements Serializable{

	/**
	 * Auto generated Serial Version ID
	 */
	private static final long serialVersionUID = 8148113333491738564L;
	/**
	 * The variable to hold the accountID
	 */
	private AccountUser accountUser;
	/**
	 * @return the accountUser
	 */
	public AccountUser getAccountUser() {
		return accountUser;
	}
	/**
	 * @param accountUser the accountUser to set
	 */
	public void setAccountUser(AccountUser accountUser) {
		this.accountUser = accountUser;
	}
	/**
	 * The variable to hold the message
	 */
	private String message;
	private Long accountId;
	
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
