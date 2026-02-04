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
package org.tolven.shiro.mgt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Nameable;
import org.apache.shiro.util.ThreadContext;
import org.tolven.shiro.authc.UsernamePasswordRealmToken;
import org.tolven.shiro.session.mgt.TolvenSessionManager;

public class TolvenSecurityManager extends DefaultSecurityManager {

    private static final Logger logger = Logger.getLogger(TolvenSecurityManager.class);
    private static final Map<String, TolvenSecurityManager> securityManagers = new HashMap<String, TolvenSecurityManager>();

    public static TolvenSecurityManager getSecurityManager() {
        return getSecurityManager(null);
    }

    public static TolvenSecurityManager getSecurityManager(String securityContextId) {
        TolvenSecurityManager securityManager = securityManagers.get(securityContextId);
        if (securityManager == null) {
            securityManager = internalGetSecurityManager(securityContextId);
        }
        return securityManager;
    }

    private static synchronized TolvenSecurityManager internalGetSecurityManager(String securityContextId) {
        TolvenSecurityManager securityManager = securityManagers.get(securityContextId);
        if (securityManager == null) {
            securityManager = new TolvenSecurityManager();
            securityManager.setSecurityContextId(securityContextId);
            initializeRealms(securityManager, securityContextId);
            securityManagers.put(securityContextId, securityManager);
        }
        return securityManager;
    }

    public static void initializeRealms(DefaultSecurityManager securityManager, String securityContextId) {
        if (securityContextId == null) {
            //TODO properties need to be externalized as configuration properties (see v3.0)
            securityContextId = "queueSecurityContext";
            //
            if (securityContextId == null) {
                throw new RuntimeException("Security contextId cannot be null");
            }
        }
        //TODO properties need to be externalized as configuration properties (see v3.0)
        //Also tolven needs to be switched to queue_REALM
        List<String> realmIds = new ArrayList<String>();
        String queue_REALM = "tolven";
        realmIds.add(queue_REALM);
        //
        StringBuffer buff = new StringBuffer();
        List<Realm> realms = new ArrayList<Realm>();
        //TODO properties need to be externalized as configuration properties (see v3.0)
        Map<String, Properties> securityContextsProps = new HashMap<String, Properties>();
        Properties rProps = new Properties();
        rProps.setProperty("id", queue_REALM);
        rProps.setProperty("authClass", "org.tolven.gatekeeper.realm.ldap.TolvenLdapRealm");
        securityContextsProps.put(queue_REALM, rProps);
        //
        for (String realmId : realmIds) {
            Properties realmsProps = securityContextsProps.get(realmId);
            String realmName = realmsProps.getProperty("id");
            String classname = realmsProps.getProperty("authClass");
            try {
                Class<?> clazz = Class.forName(classname);
                Realm realm = (Realm) clazz.newInstance();
                ((Nameable) realm).setName(realmName);
                realms.add(realm);
                buff.append(realmName + ",");
            } catch (Exception ex) {
                throw new RuntimeException("Could not instantiate: " + classname, ex);
            }
        }
        if (realms.isEmpty()) {
            throw new RuntimeException(securityManager + " configured with no realms for security context: " + securityContextId);
        }
        securityManager.setRealms(realms);
        logger.info("Configured " + securityManager.getClass().getSimpleName() + " with security context: " + securityContextId + " containing realms: " + buff.toString());
    }

    private String securityContextId;

    private TolvenSecurityManager() {
        setSessionManager(new TolvenSessionManager());
        setRememberMeManager(null);
    }

    public String getSecurityContextId() {
        return securityContextId;
    }

    public void login(UsernamePasswordRealmToken token) {
        Subject subject = new Subject.Builder(this).buildSubject();
        ThreadContext.bind(subject);
        subject.login(token);
    }

    @Override
    protected void onFailedLogin(AuthenticationToken token, AuthenticationException ae, Subject subject) {
        String realm = ((UsernamePasswordRealmToken) token).getRealm();
        logger.info("User: " + token.getPrincipal() + " realm: " + realm + " FAILED_LOGIN");
        super.onFailedLogin(token, ae, subject);
    }

    @Override
    protected void onSuccessfulLogin(AuthenticationToken token, AuthenticationInfo info, Subject subject) {
        String realm = ((UsernamePasswordRealmToken) token).getRealm();
        logger.info("User: " + token.getPrincipal() + " realm: " + realm + " SUCCESSFUL_LOGIN");
        super.onSuccessfulLogin(token, info, subject);
    }

    public void setSecurityContextId(String securityContextId) {
        this.securityContextId = securityContextId;
    }

}
