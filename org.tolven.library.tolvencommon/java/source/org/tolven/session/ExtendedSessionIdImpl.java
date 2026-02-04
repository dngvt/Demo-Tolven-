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
package org.tolven.session;

import java.net.URLEncoder;
import java.security.GeneralSecurityException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class ExtendedSessionIdImpl implements ExtendedSessionId {

    private static final Logger logger = Logger.getLogger(ExtendedSessionIdImpl.class);

    @Override
    public String createExtendedSessionId(String persistentSessionId) {
        byte[] secretKey = generateSessionSecretKey().getEncoded();
        if (logger.isDebugEnabled()) {
            logger.debug("Generated session secret key");
        }
        String urlEncodedSecretKey = null;
        try {
            String encodedSecretKey = new String(Base64.encodeBase64(secretKey), "UTF-8");
            urlEncodedSecretKey = URLEncoder.encode(encodedSecretKey, "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException("Could not encode secret key", ex);
        }
        return persistentSessionId + "_" + urlEncodedSecretKey;
    }

    //ported from v3.0, but should be in CertificateHelper
    private static SecretKey generateSessionSecretKey() {
        //TODO need to be externalized config properties
        //Properties props = PkiContextFactory.getPkiContext().getPkiProperties();
        String algorithm = "AES";
        String keyLength = "128";
        int kbeKeyLength = Integer.parseInt(keyLength);
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(algorithm);
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException("Could not key algorithm" + algorithm, ex);
        }
        keyGenerator.init(kbeKeyLength);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey;
    }

    @Override
    public String getExtendedSessionId(String persistentSessionId, String secretSessionId) {
        return persistentSessionId + "_" + secretSessionId;
    }

    @Override
    public String getPersistentSessionId(String extendedSessionId) {
        if (extendedSessionId == null || extendedSessionId.trim().length() == 0) {
            return null;
        }
        int index = extendedSessionId.indexOf("_");
        if (index == -1) {
            throw new RuntimeException("Unrecognized format: extended session Id must contain an _");
        }
        return extendedSessionId.substring(0, index);
    }

}