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
package org.tolven.gatekeeper.realm.ldap;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.tolven.exception.TolvenAuthenticationException;
import org.tolven.ldap.LdapManager;
import org.tolven.ldap.PasswordExpiring;
import org.tolven.naming.LdapRealmContext;
import org.tolven.naming.TolvenContext;
import org.tolven.shiro.authc.UsernamePasswordRealmToken;

public class TolvenLdapRealm extends AuthorizingRealm {

    public static final String REALM = "org.tolven.session.attribute.realm";
    public static final String TOLVEN_CREDENTIAL_FORMAT_PKCS12 = "pkcs12";
    public static final String USER_ROLES = "org.tolven.session.attribute.roles";

    private Logger logger = Logger.getLogger(TolvenLdapRealm.class);
    private static TolvenContext tolvenContext;

    public TolvenLdapRealm() {
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordRealmToken uprToken = (UsernamePasswordRealmToken) token;
        String uid = (String) uprToken.getPrincipal();
        char[] password = uprToken.getPassword();
        String realm = uprToken.getRealm();
        LdapManager ldapManager = null;
        try {
            LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
            ldapManager = ldapRealmContext.getLdapManager(uid, password);
            ldapManager.checkPassword();
            Session session = SecurityUtils.getSubject().getSession();
            boolean passwordExpired = ldapManager.isPasswordExpired();
            if (passwordExpired) {
                session.setAttribute(TolvenAuthenticationException.PASSWORD_EXPIRED, passwordExpired);
            } else {
                session.removeAttribute(TolvenAuthenticationException.PASSWORD_EXPIRED);
            }
            String formattedExpiration = null;
            PasswordExpiring passwordExpiring = ldapManager.getPasswordExpiring();
            if (passwordExpiring != null) {
                formattedExpiration = passwordExpiring.getFormattedExpiration();
                session.setAttribute(TolvenAuthenticationException.PASSWORD_EXPIRING, formattedExpiration);
            } else {
                session.removeAttribute(TolvenAuthenticationException.PASSWORD_EXPIRING);
            }
            String sessionAttributes = ldapRealmContext.getSessionAttributes();
            if (sessionAttributes == null) {
                sessionAttributes = "";
            }
            String[] sessionAttributeIds = sessionAttributes.split(",");
            if (!passwordExpired) {
                Set<String> roles = ldapManager.getRoles();
                String tolvenPersonRole = ldapRealmContext.getTolvenPersonRole();
                if(!roles.contains(tolvenPersonRole)) {
                    String msg = "Not authorized to use application user: " + uid + " realm: " + realm;
                    throw new AuthenticationException(msg);
                }
                session.setAttribute(REALM, realm);
                session.setAttribute(USER_ROLES, roles);
                Attributes attributes = ldapManager.getAttributes(sessionAttributeIds);
                try {
                    if (attributes.get("userPKCS12") == null && attributes.get("cn") != null && attributes.get("ou") != null && attributes.get("o") != null && attributes.get("st") != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("No userPKCS12 found for: " + uid + " so will autogenerate new credentials");
                        }
                        ldapManager.generateNewUserPKCS12IfNone();
                        attributes = ldapManager.getAttributes(sessionAttributeIds);
                    }
                    injectSessionAttributes(attributes, session, uid, password);
                } catch (NamingException e) {
                    String msg = "LDAP naming error while attempting to authenticate user.";
                    throw new AuthenticationException(msg, e);
                }
            }
            return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Session session = SecurityUtils.getSubject().getSession();
        Set<String> roles = (Set<String>) session.getAttribute(USER_ROLES);
        if (roles == null) {
            roles = new HashSet<String>();
        }
        return new SimpleAuthorizationInfo(roles);
    }

    protected LdapRealmContext getLdapRealmContext(String realm) {
        try {
            return (LdapRealmContext) getTolvenContext().getRealmContext(realm);
        } catch (Exception ex) {
            throw new RuntimeException("Could not get LdapRealmContext", ex);
        }
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

    protected void injectSessionAttributes(Attributes attrs, Session session, String principal, char[] password) throws NamingException {
        NamingEnumeration<String> namingEnumIDs = attrs.getIDs();
        while (namingEnumIDs.hasMoreElements()) {
            String attrID = null;
            try {
                attrID = namingEnumIDs.next();
            } catch (NamingException ex) {
                throw new RuntimeException("Could not obtain next enumeration", ex);
            }
            // Deal with RFC4523
            if ("userCertificate".equals(attrID)) {
                Object obj = null;
                try {
                    obj = attrs.get(attrID).get();
                } catch (NamingException ex) {
                    throw new RuntimeException("Could not get attribute userCertificate", ex);
                }
                session.setAttribute("userCertificate;binary", obj);
            } else {
                session.setAttribute(attrID, attrs.get(attrID).get());
            }
        }
        injectUserCredentials(session, principal, password);
    }

    protected void injectUserCredentials(Session session, Object principal, char[] password) throws AuthenticationException {
        KeyStore keyStore = null;
        byte[] userPKCS12 = (byte[]) session.getAttribute("userPKCS12");
        if (userPKCS12 == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No userPKCS12 found for: " + principal);
            }
            //No userPKCS12 to process
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("userPKCS12 found for: " + principal);
        }
        try {
            keyStore = KeyStore.getInstance(TOLVEN_CREDENTIAL_FORMAT_PKCS12);
            ByteArrayInputStream bais = null;
            try {
                bais = new ByteArrayInputStream(userPKCS12);
                keyStore.load(bais, password);
            } finally {
                if (bais != null)
                    bais.close();
            }
        } catch (Exception ex) {
            throw new AuthenticationException("Could not obtain user's private keystore: " + principal, ex);
        }
        String alias = null;
        PrivateKey userPrivateKey = null;
        try {
            Enumeration<String> aliases = keyStore.aliases();
            if (!aliases.hasMoreElements()) {
                throw new RuntimeException(getClass() + ": userPKCS12 contains no aliases for principal " + principal);
            }
            alias = aliases.nextElement();
            userPrivateKey = (PrivateKey) keyStore.getKey(alias, password);
        } catch (Exception ex) {
            throw new AuthenticationException("Could not obtain user's private key: " + principal, ex);
        }
        if (userPrivateKey == null) {
            throw new RuntimeException(getClass() + ": userPKCS12 contains no key with alias " + alias + " for " + principal);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Obtained user private key for: " + principal);
        }
        PKCS8EncodedKeySpec userPKCS8EncodedKey = new PKCS8EncodedKeySpec(userPrivateKey.getEncoded());
        session.setAttribute("userPKCS8EncodedKey", userPKCS8EncodedKey.getEncoded());
        if (logger.isDebugEnabled()) {
            logger.debug("Stored user private key in session under userPKCS8EncodedKey for: " + principal);
        }
        Certificate[] certificateChain = null;
        try {
            certificateChain = keyStore.getCertificateChain(alias);
        } catch (KeyStoreException ex) {
            throw new RuntimeException("Could not obtain user's keystore certificate chain: " + principal, ex);
        }
        if (certificateChain == null || certificateChain.length == 0) {
            throw new RuntimeException("LDAP's userPKCS12 contains no certificate with alias " + alias + " for " + principal);
        }
        X509Certificate certificate = (X509Certificate) certificateChain[0];
        if (logger.isDebugEnabled()) {
            logger.debug("Obtained user certificate for: " + principal);
        }
        try {
            session.setAttribute("userX509Certificate", certificate.getEncoded());
            if (logger.isDebugEnabled()) {
                logger.debug("Stored user certificate in session under userX509Certificate for: " + principal);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not get encoded X509 certificate: " + principal, ex);
        }

    }

    @Override
    public void onLogout(PrincipalCollection principals) {
        logger.info("User: " + principals.getPrimaryPrincipal() + " realm: " + principals.getRealmNames() + " LOGGED_OUT");
        super.onLogout(principals);
    }

}
