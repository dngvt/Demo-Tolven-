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
package org.tolven.security.key;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

/**
 * This class encapsulates a SecretKey which has been encrypted using a
 * PublicKey during initialization. To obtain the unencrypted SecretKey, the
 * PrivateKey companion of the encrypting PublicKey must be supplied.
 * 
 * @author Joseph Isaac
 * 
 */
@MappedSuperclass
public abstract class TolvenEncryptedSecretKey implements Serializable {

    @Column
    private String algorithm;

    @Lob
    @Basic
    @Column(insertable = false, updatable = false)
    private byte[] encryptedKey;

    @Basic
    @Column
    private byte[] encryptedSecretKey;

    protected TolvenEncryptedSecretKey() {
    }

    /**
     * Return the algorithm of the encrypted SecretKey
     * 
     * @return
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Return the encrypted SecretKey
     * 
     * @return
     */
    public byte[] getEncryptedKey() {
        if(getEncryptedSecretKey() != null) {
            return getEncryptedSecretKey();
        }
        return encryptedKey;
    }

    public byte[] getEncryptedSecretKey() {
        return encryptedSecretKey;
    }
    
    /**
     * Decrypt the SecretKey using a PrivateKey and return it
     * 
     * @param aDecryptionKey
     * @return
     * @throws GeneralSecurityException
     */
    public SecretKey getSecretKey(PrivateKey aDecryptionPrivateKey) throws GeneralSecurityException {
        if (getEncryptedKey() == null || getAlgorithm() == null) {
            throw new IllegalStateException(getClass() + " not initialized");
        }
        Cipher cipher = Cipher.getInstance(aDecryptionPrivateKey.getAlgorithm());
        cipher.init(Cipher.UNWRAP_MODE, aDecryptionPrivateKey);
        try {
            return (SecretKey) cipher.unwrap(getEncryptedKey(), getAlgorithm(), Cipher.SECRET_KEY);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to decrypt " + getClass().getName() + " with a PrivateKey", ex);
        }
    }

    /**
     * Encrypt a SecretKey using a PublicKey. Subclasses are responsible for ensuring that this key is not initialized more than once i.e. is immutable
     * 
     * @param aPublicKey
     * @throws GeneralSecurityException
     */
    public abstract SecretKey init(PublicKey aPublicKey, String kbeKeyAlgorithm, int kbeKeyLength) throws GeneralSecurityException;

    protected void setAlgorithm(String aString) {
        algorithm = aString;
    }

    protected void setEncryptedKey(byte[] array) {
        setEncryptedSecretKey(array);
    }
    
    protected void setEncryptedSecretKey(byte[] encryptedSecretKey) {
        this.encryptedSecretKey = encryptedSecretKey;
    }

}
