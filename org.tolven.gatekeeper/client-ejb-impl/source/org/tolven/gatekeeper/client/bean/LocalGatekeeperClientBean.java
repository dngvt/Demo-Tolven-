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

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.tolven.gatekeeper.LdapLocal;
import org.tolven.gatekeeper.client.api.LocalGatekeeperClientLocal;
import org.tolven.naming.TolvenContext;
import org.tolven.naming.TolvenPerson;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.shiro.authc.UsernamePasswordRealmToken;
import org.tolven.shiro.mgt.TolvenSecurityManager;

@Stateless
@Local(LocalGatekeeperClientLocal.class)
public class LocalGatekeeperClientBean implements LocalGatekeeperClientLocal {

    private static CertificateFactory certificateFactory;
    private static final TolvenSecurityManager securityManager = TolvenSecurityManager.getSecurityManager();
    private static TolvenContext tolvenContext;
    
    @EJB
    private LdapLocal ldapBean;

    @Override
    public boolean existsTolvenPerson(String uid, String realm, String userId, char[] userIdPassword) {
        TolvenPerson tolvenPerson = ldapBean.findTolvenPerson(uid, realm, userId, userIdPassword);
        return tolvenPerson != null;
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
    public List<String> getRealmIds() {
        return getTolvenContext().getRealmIds();
    }

    private TolvenContext getTolvenContext() {
        if (tolvenContext == null) {
            String jndiName = "tolvenContext";
            try {
                InitialContext ictx = new InitialContext();
                tolvenContext = (TolvenContext) ictx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not look up " + jndiName, ex);
            }
        }
        return tolvenContext;
    }

    @Override
    public X509Certificate getUserX509Certificate(String uid, String realm, String requestorId, char[] requestorPassword) {
        byte[] x509CertificateBytes = ldapBean.getUserX509Certificate(uid, realm, requestorId, requestorPassword);
        if (x509CertificateBytes == null) {
            return null;
        } else {
            try {
                return (X509Certificate) getCertificateFactory().generateCertificate(new ByteArrayInputStream(x509CertificateBytes));
            } catch (Exception ex) {
                throw new RuntimeException("Could not get X509Certificate from SSO userCertificate", ex);
            }
        }
    }

    /**
     * User RESTful to log into the gatekeeper and return the extended session Id
     * @param username
     * @param password
     * @param realm
     * @return
     */
    @Override
    public String login(String username, char[] password, String realm) {
        return login(username, password, realm, false);
    }

    /**
     * User to log into the gatekeeper and return the extended session Id
     * When newSession is false, the existing session will be used, otherwise a new one
     * will be created and returned by the gatekeeper.
     */
    @Override
    public String login(String username, char[] password, String realm, boolean newSession) {
        UsernamePasswordRealmToken token = new UsernamePasswordRealmToken(username, password, realm);
        securityManager.login(token);
        return TolvenSessionWrapperFactory.getInstance().getExtendedSessionId();
    }

    /**
     * Logout the current Subject
     */
    @Override
    public void logout() {
        TolvenSessionWrapperFactory.getInstance().logout();
    }

    @Override
    public boolean verifyUserPassword(String uid, char[] password, String realm) {
        return  ldapBean.verifyPassword(uid, password, realm);
    }

}
