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
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

/**
 * This class encapsulates an x509EncodedKeySpec for a Public Key
 * 
 * @author Joseph Isaac
 * 
 */
@MappedSuperclass
public abstract class TolvenPublicKey implements Serializable {

    protected static final String INITIALIZED = "TolvenPublicKey already initialized";
    private static Map<String, KeyFactory> keyFactories;

    protected static final String NOT_INITIALIZED = "TolvenPublicKey not initialized";
    
    @Column
    private String algorithm;

    private transient PublicKey publicKey;

    @Basic
    @Column
    private byte[] publicKeySpec;

    @Lob
    @Basic
    @Column(insertable = false, updatable = false)
    private byte[] x509EncodedKeySpec;

    protected TolvenPublicKey() {
    }

    public String getAlgorithm() {
        return algorithm;
    }

    protected KeyFactory getKeyFactory(String keyAlgorithm) {
        if(keyFactories == null) {
            keyFactories = new HashMap<String, KeyFactory>();
        }
        KeyFactory keyFactory = keyFactories.get(keyAlgorithm);
        if (keyFactory == null) {
            try {
                keyFactory = KeyFactory.getInstance(keyAlgorithm);
                keyFactories.put(keyAlgorithm, keyFactory);
            } catch (Exception ex) {
                throw new RuntimeException("Could not get instance of KeyFactory", ex);
            }
        }
        return keyFactory;
    }

    /**
     * Decode and return the encapsulated PublicKey
     * @return
     * @throws GeneralSecurityException
     */
    public PublicKey getPublicKey() {
        if (publicKey == null) {
            if (getX509EncodedKeySpec() == null || getAlgorithm() == null)
                throw new IllegalStateException(NOT_INITIALIZED);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(getX509EncodedKeySpec());
            try {
                setPublicKey(getKeyFactory(getAlgorithm()).generatePublic(keySpec));
            } catch (Exception ex) {
                throw new RuntimeException("Could not PublicKey with algorithm: " + getAlgorithm(), ex);
            }
        }
        return publicKey;
    }

    public byte[] getPublicKeySpec() {
        return publicKeySpec;
    }

    public byte[] getX509EncodedKeySpec() {
        if(getPublicKeySpec() != null) {
            return getPublicKeySpec();
        }
        return x509EncodedKeySpec;
    }

    /**
     * Initialize TolvenPublicKey with aPublicKey
     * @param aPublicKey
     */
    public void init(PublicKey aPublicKey) {
        if (getX509EncodedKeySpec() != null || getAlgorithm() != null)
            throw new IllegalStateException(INITIALIZED);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(aPublicKey.getEncoded());
        setX509EncodedKeySpec(keySpec.getEncoded());
        setAlgorithm(aPublicKey.getAlgorithm());
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setPublicKeySpec(byte[] publicKeySpec) {
        this.publicKeySpec = publicKeySpec;
    }

    public void setX509EncodedKeySpec(byte[] x509EncodedKeySpec) {
        setPublicKeySpec(x509EncodedKeySpec);
    }

}
