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
package org.tolven.gatekeeper.client.bean;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.commons.codec.binary.Base64;
import org.tolven.gatekeeper.client.api.GatekeeperClientLocal;
import org.tolven.gatekeeper.client.api.UserKeyLocal;
import org.tolven.session.TolvenSessionWrapperFactory;

@Stateless
@Local(UserKeyLocal.class)
public class UserKeyBean implements UserKeyLocal {

    @EJB
    private GatekeeperClientLocal gatekeeperClientBean;

    public UserKeyBean() {
    }

    @Override
    public PublicKey getUserPublicKey() {
        return TolvenSessionWrapperFactory.getInstance().getUserPublicKey();
    }

    @Override
    public PublicKey getUserPublicKey(String uid, String realm, String requestorId, char[] requestorPassword) {
        X509Certificate x509Certificate = getUserX509Certificate(uid, realm, requestorId, requestorPassword);
        if (x509Certificate == null) {
            return null;
        } else {
            return x509Certificate.getPublicKey();
        }
    }

    @Override
    public X509Certificate getUserX509Certificate() {
        return TolvenSessionWrapperFactory.getInstance().getUserX509Certificate();
    }

    @Override
    public X509Certificate getUserX509Certificate(String uid, String realm, String requestorId, char[] requestorPassword) {
        return gatekeeperClientBean.getUserX509Certificate(uid, realm, requestorId, requestorPassword);
    }

    @Override
    public String getUserX509CertificateString() {
        return TolvenSessionWrapperFactory.getInstance().getUserX509CertificateString();
    }

    @Override
    public String getUserX509CertificateString(String uid, String realm, String requestorId, char[] requestorPassword) {
        X509Certificate x509Certificate = getUserX509Certificate(uid, realm, requestorId, requestorPassword);
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

}
