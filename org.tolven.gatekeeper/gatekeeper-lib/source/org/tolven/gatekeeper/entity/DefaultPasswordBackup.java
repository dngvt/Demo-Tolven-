/*
 *  Copyright (C) 2006 Tolven Inc 
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * Contact: info@tolvenhealth.com
 */
package org.tolven.gatekeeper.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 
 * @author Joseph Isaac
 * 
 * This entity contains encrypted passwords, which can be used when a user has forgotten their main password.
 * Access is based on security questions/answers specific to the user, and the purpose for the password.
 *
 */
@Entity
@IdClass(DefaultPasswordBackupPK.class)
public class DefaultPasswordBackup implements PasswordBackup {

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date creation;

    @Lob
    @Column(insertable = false, updatable = false)
    private byte[] encryptedPassword;

    @Basic
    @Column
    private byte[] encryptedPasswordChars;

    @Column
    private int iterationCount;

    @Id
    @Column
    private String passwordPurpose;

    @Id
    @Column
    private String realm;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(insertable = false, updatable = false)
    private byte[] salt;

    @Basic(fetch = FetchType.LAZY)
    @Column
    private byte[] saltChars;

    @Id
    @Column
    private String securityQuestion;

    @Id
    @Column
    private String userId;

    @Override
    public Date getCreation() {
        return creation;
    }

    @Override
    public byte[] getEncryptedPassword() {
        if(getEncryptedPasswordChars() != null) {
            return getEncryptedPasswordChars();
        }
        return encryptedPassword;
    }

    @Override
    public int getIterationCount() {
        return iterationCount;
    }

    @Override
    public String getPasswordPurpose() {
        return passwordPurpose;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public byte[] getSalt() {
        if(getSaltChars() != null) {
            return getSaltChars();
        }
        return salt;
    }

    @Override
    public String getSecurityQuestion() {
        return securityQuestion;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setCreation(Date creation) {
        this.creation = creation;
    }

    @Override
    public void setEncryptedPassword(byte[] encryptedPassword) {
        setEncryptedPasswordChars(encryptedPassword);
    }

    @Override
    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    @Override
    public void setPasswordPurpose(String passwordPurpose) {
        this.passwordPurpose = passwordPurpose;
    }

    @Override
    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    public void setSalt(byte[] salt) {
        setSaltChars(salt);
    }

    @Override
    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public byte[] getEncryptedPasswordChars() {
        return encryptedPasswordChars;
    }

    public void setEncryptedPasswordChars(byte[] encryptedPasswordChars) {
        this.encryptedPasswordChars = encryptedPasswordChars;
    }

    public byte[] getSaltChars() {
        return saltChars;
    }

    public void setSaltChars(byte[] saltChars) {
        this.saltChars = saltChars;
    }
    
}
