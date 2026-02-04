package org.tolven.app.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.tolven.core.entity.Account;
/**
 * Represent resources accessed by an application. The primary key is qualified by account, 
 * typically a template account.
 * @author John Churin
 *
 */
@Entity
@Table
public class MSResource implements Serializable{
	/**
	 * Version number for this entity object
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="APP_SEQ_GEN")
    private long id;
	
	@ManyToOne()
	private Account account;

	@Column
	private String name;

    @Lob
    @Column(insertable = false, updatable = false)
    private byte[] value;

    @Column
    private byte[] valueBody;

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public byte[] getValue() {
        if(getValueBody() != null) {
            return getValueBody();
        }
        return value;
    }

    public void setValue(byte[] value) {
        setValueBody(value);
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

    public byte[] getValueBody() {
        return valueBody;
    }

    public void setValueBody(byte[] valueBody) {
        this.valueBody = valueBody;
    }
    
}
