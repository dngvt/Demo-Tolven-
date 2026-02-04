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

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.tolven.gatekeeper.client.api.GatekeeperClientLocal;
import org.tolven.gatekeeper.client.api.LocalGatekeeperClientLocal;
import org.tolven.gatekeeper.client.api.RSGatekeeperClientLocal;
import org.tolven.session.ExtendedSessionIdFactory;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.shiro.mgt.TolvenSecurityManager;

@Stateless
@Local(GatekeeperClientLocal.class)
public class GatekeeperClientBean implements GatekeeperClientLocal {
    
    private static final TolvenSecurityManager securityManager = TolvenSecurityManager.getSecurityManager();

    @EJB
    private LocalGatekeeperClientLocal localGatekeeperClientBean;
    
    private boolean localMode = true;
    
    @EJB
    private RSGatekeeperClientLocal rsGatekeeperClientBean;

    @Override
    public boolean existsTolvenPerson(String uid, String realm, String userId, char[] userIdPassword) {
        if(isLocalMode()) {
            return localGatekeeperClientBean.existsTolvenPerson(uid, realm, userId, userIdPassword);
        } else {
            return rsGatekeeperClientBean.existsTolvenPerson(uid, realm, userId, userIdPassword);
        }
    }

    @Override
    public List<String> getRealmIds() {
        if(isLocalMode()) {
            return localGatekeeperClientBean.getRealmIds();
        } else {
            return rsGatekeeperClientBean.getRealmIds();
        }
    }

    @Override
    public X509Certificate getUserX509Certificate(String uid, String realm, String requestorId, char[] requestorPassword) {
        if(isLocalMode()) {
            return localGatekeeperClientBean.getUserX509Certificate(uid, realm, requestorId, requestorPassword);
        } else {
            return rsGatekeeperClientBean.getUserX509Certificate(uid, realm, requestorId, requestorPassword);
        }
    }

    private boolean isLocalMode() {
        return localMode;
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
        String extendedSessionId = null;
        if(isLocalMode()) {
            extendedSessionId = localGatekeeperClientBean.login(username, password, realm, newSession);
        } else {
            extendedSessionId = rsGatekeeperClientBean.login(username, password, realm, newSession);
            TolvenSessionWrapperFactory.getInstance().bind(extendedSessionId);
            String persistentSessionId = ExtendedSessionIdFactory.getInstance().getPersistentSessionId(extendedSessionId);
            Subject subject = new Subject.Builder(securityManager).sessionId(persistentSessionId).buildSubject();
            ThreadContext.bind(subject);
        }
        return extendedSessionId;
    }

    /**
     * Logout the current Subject
     */
    @Override
    public void logout() {
        if(isLocalMode()) {
            localGatekeeperClientBean.logout();
        } else {
            rsGatekeeperClientBean.logout();
        }
    }

    @Override
    public boolean verifyUserPassword(String uid, char[] password, String realm) {
        if(isLocalMode()) {
            return localGatekeeperClientBean.verifyUserPassword(uid, password, realm);
        } else {
            return rsGatekeeperClientBean.verifyUserPassword(uid, password, realm);
        }
    }

}
