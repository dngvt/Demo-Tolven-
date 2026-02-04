/*
 * Copyright (C) 2009 Tolven Inc

 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;  
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 *
 * Contact: info@tolvenhealth.com 
 *
 * @author Joseph Isaac
 */
package org.tolven.shiro.session;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.tolven.session.ExtendedSessionIdFactory;
import org.tolven.session.TolvenSessionWrapper;

/**
 * A class which wraps a TolvenSession
 * 
 * @author Joseph Isaac
 *
 */
public class ShiroSessionWrapper implements TolvenSessionWrapper {

    private static CertificateFactory certificateFactory;
    private static final ThreadLocal<TolvenSessionWrapper> instance = new ThreadLocal<TolvenSessionWrapper>();
    private static Map<String, KeyFactory> keyFactories;
    private static final Logger logger = Logger.getLogger(ShiroSessionWrapper.class);
    public static final String REALM = "org.tolven.session.attribute.realm";

    public static TolvenSessionWrapper getInstance() {
        if (instance.get() == null) {
            instance.set(new ShiroSessionWrapper());
        }
        return instance.get();
    }

    private byte[] secretKey;

    public ShiroSessionWrapper() {
    }

    @Override
    public void bind(String extendedSessionId) {
        if (extendedSessionId == null) {
            throw new RuntimeException("Extended session Id cannot be null");
        } else if (!extendedSessionId.contains("_")) {
            throw new RuntimeException("Extended session Id must be of the form underscore (persistentSessionId_secretSessionId)");
        }
        byte[] sKey = null;
        try {
            String urlDecodedSecretKey = URLDecoder.decode(extendedSessionId.substring(1 + extendedSessionId.indexOf("_")), "UTF-8");
            sKey = Base64.decodeBase64(urlDecodedSecretKey.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Could not base64 decode the extended session Id", ex);
        }
        setSecretKey(sKey);
        if (logger.isDebugEnabled()) {
            logger.debug("TOLVENSESSION_THREADCONTEXT_BIND");
        }
    }

    @Override
    public void clear() {
        instance.set(null);
        if (logger.isDebugEnabled()) {
            logger.debug("TOLVENSESSION_THREADCONTEXT_CLEAR");
        }
    }

    @Override
    public Object getAttribute(Object name) {
        return getSession().getAttribute(name);
    }

    @Override
    public Collection<Object> getAttributeKeys() {
        return getSession().getAttributeKeys();
    }
    
    private CertificateFactory getCertificateFactory() {
        if (certificateFactory == null) {
            try {
                certificateFactory = CertificateFactory.getInstance("X509");
            } catch (CertificateException ex) {
                throw new RuntimeException("Could not get instance of CertificateFactory", ex);
            }
        }
        return certificateFactory;
    }

    @Override
    public String getExtendedSessionId() {
        if (getSecretKey() == null) {
            throw new RuntimeException("Instance not initialized with secret key");
        }
        String urlEncodedSecretKey = null;
        try {
            String encodedSecretKey = new String(Base64.encodeBase64(getSecretKey()), "UTF-8");
            urlEncodedSecretKey = URLEncoder.encode(encodedSecretKey, "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException("Could not encode secret key", ex);
        }
        return getId() + "_" + urlEncodedSecretKey;
    }

    @Override
    public String getHost() {
        return getSession().getHost();
    }

    @Override
    public Serializable getId() {
        return getSession().getId();
    }

    private KeyFactory getKeyFactory(String keyAlgorithm) {
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

    @Override
    public Date getLastAccessTime() {
        return getSession().getLastAccessTime();
    }

    @Override
    public Object getPrincipal() {
        return getSubject().getPrincipal();
    }

    @Override
    public String getRealm() {
        return (String) getAttribute(REALM);
    }

    @Override
    public byte[] getSecretKey() {
        return secretKey;
    }

    private Session getSession() {
        return getSubject().getSession();
    }

    @Override
    public String getSessionId(String extendedSessionId) {
        return ExtendedSessionIdFactory.getInstance().getPersistentSessionId(extendedSessionId);
    }

    @Override
    public Date getStartTimestamp() {
        return getSession().getStartTimestamp();
    }

    private Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    @Override
    public long getTimeout() {
        return getSession().getTimeout();
    }

    public List<String> getTolvenPersonList(String name) {
        Set<String> set = (Set<String>) getAttribute(name);
        if (set == null || set.isEmpty()) {
            return new ArrayList<String>();
        } else {
            return (List<String>) new ArrayList<String>(set);
        }
    }

    public String getTolvenPersonString(String name) {
        Set<String> set = (Set<String>) getAttribute(name);
        if (set == null || set.isEmpty()) {
            return null;
        } else {
            return (String) set.iterator().next();
        }
    }

    @Override
    public PrivateKey getUserPrivateKey(String keyAlgorithm) {
        byte[] userPKCS8EncodedKeyBytes = (byte[]) getAttribute("userPKCS8EncodedKey");
        if (userPKCS8EncodedKeyBytes == null) {
            return null;
        } else {
            try {
                PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(userPKCS8EncodedKeyBytes);
                return getKeyFactory(keyAlgorithm).generatePrivate(pkcs8EncodedKeySpec);
            } catch (Exception ex) {
                throw new RuntimeException("Could not get PrivateKey from pkcs8EncodedKey", ex);
            }
        }
    }

    @Override
    public PublicKey getUserPublicKey() {
        X509Certificate x509Certificate = getUserX509Certificate();
        if (x509Certificate == null) {
            return null;
        } else {
            return x509Certificate.getPublicKey();
        }
    }

    @Override
    public X509Certificate getUserX509Certificate() {
        byte[] userX509CertificateBytes = (byte[]) getAttribute("userX509Certificate");
        if (userX509CertificateBytes == null) {
            return null;
        } else {
            try {
                return (X509Certificate) getCertificateFactory().generateCertificate(new ByteArrayInputStream(userX509CertificateBytes));
            } catch (Exception ex) {
                throw new RuntimeException("Could not get X509Certificate from SSO userCertificate", ex);
            }
        }
    }

    @Override
    public String getUserX509CertificateString() {
        X509Certificate x509Certificate = getUserX509Certificate();
        if (x509Certificate == null) {
            return null;
        }
        try {
            StringBuffer buff = new StringBuffer();
            buff.append("-----BEGIN CERTIFICATE-----");
            buff.append("\n");
            String pemFormat = new String(Base64.encodeBase64Chunked(x509Certificate.getEncoded()));
            buff.append(pemFormat);
            buff.append("\n");
            buff.append("-----END CERTIFICATE-----");
            buff.append("\n");
            return buff.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Could not convert X509Certificate into a String", ex);
        }
    }

    @Override
    public boolean hasAllRoles(Collection<String> roleIdentifiers) {
        return getSubject().hasAllRoles(roleIdentifiers);
    }

    @Override
    public boolean hasRole(String roleIdentifier) {
        return getSubject().hasRole(roleIdentifier);
    }

    @Override
    public boolean[] hasRoles(List<String> roleIdentifiers) {
        return getSubject().hasRoles(roleIdentifiers);
    }

    @Override
    public boolean isAuthenticated() {
        return getSubject().isAuthenticated();
    }

    @Override
    public void logout() {
        getSubject().logout();
    }

    @Override
    public Object removeAttribute(Object name) {
        return getSession().removeAttribute(name);
    }

    @Override
    public void setAttribute(Object name, Object value) {
        getSession().setAttribute(name, value);
    }

    private void setSecretKey(byte[] secretKey) {
        if (this.secretKey != null) {
            throw new RuntimeException("SecretKey already initialized");
        }
        this.secretKey = secretKey;
    }

    @Override
    public void setTimeout(long timeout) {
        getSession().setTimeout(timeout);
    }

    @Override
    public void stop() {
        getSession().stop();
    }

    @Override
    public void touch() {
        getSession().touch();
    }

}
