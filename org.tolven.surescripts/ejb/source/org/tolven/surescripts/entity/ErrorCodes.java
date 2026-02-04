/**
 * 
 */
package org.tolven.surescripts.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author mohammed
 *
 */
@Entity
@Table(name="error_codes" , schema="surescripts")
public class ErrorCodes implements Serializable {

	/**
	 * Auto generated Serial Version ID
	 */
	private static final long serialVersionUID = -6140110117807419525L;
	/**
	 * Default Constructor
	 */
	public ErrorCodes(){
		super();
	}
	/**
	 * Parameterised Constructor
	 * @param errorCode
	 * @param errorDesc
	 */
	public ErrorCodes(String errorCode , String errorDesc){
		this.error_code = errorCode;
		this.error_desc = errorDesc;
	}
	/**
	 * Variable to map to column error_code
	 */
	@Id
	@Column
	private String error_code;
	/**
	 * Variable to map to column error_desc
	 */
	@Column
	private String error_desc;
	/**
	 * @return the error_code
	 */
	public String getError_code() {
		return error_code;
	}
	/**
	 * @param errorCode the error_code to set
	 */
	public void setError_code(String errorCode) {
		error_code = errorCode;
	}
	/**
	 * @return the error_desc
	 */
	public String getError_desc() {
		return error_desc;
	}
	/**
	 * @param errorDesc the error_desc to set
	 */
	public void setError_desc(String errorDesc) {
		error_desc = errorDesc;
	}
	

}
