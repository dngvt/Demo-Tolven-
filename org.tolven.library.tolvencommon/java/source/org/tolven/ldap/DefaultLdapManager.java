/*
 *  Copyright (C) 2011 Tolven Inc 
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
package org.tolven.ldap;

import static org.tolven.ldap.LdapManagerFactory.BASE_ROLES_NAME;
import static org.tolven.ldap.LdapManagerFactory.ROLE_DN_PREFIX;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NamingSecurityException;
import javax.naming.NoPermissionException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;
import org.tolven.config.api.TolvenConstants;
import org.tolven.exception.TolvenAuthenticationException;
import org.tolven.exception.TolvenAuthorizationException;
import org.tolven.security.cert.KeyHelper;

/**
 * 
 * @author John Churin
 *
 */
public class DefaultLdapManager implements LdapManager {

    public static final String PASSWORD_EXPIRED_OID = "2.16.840.1.113730.3.4.4";
    public static final String PASSWORD_EXPIRING_OID = "2.16.840.1.113730.3.4.5";
    public static final String PASSWORD_POLICY_OID = "1.3.6.1.4.1.42.2.27.8.5.1";
    public static final String TOLVEN_CREDENTIAL_FORMAT_PKCS12 = "pkcs12";

    private KeyHelper certificateHelper;
    private Hashtable<String, String> environment;
    private LdapContext ldapContext;
    private Logger logger = Logger.getLogger(DefaultLdapManager.class);
    private Properties properties;
    private Control[] responseControls;

    /**
     * Add role to userDN
     * 
     * @param role
     * @param userDN
     */
    @Override
    public void addRole(String role, String userDN) {
        try {
            if (!(isRealm(role) || "tolven".equals(role)) && !(isRole(role))) {
                throw new RuntimeException("Unrecognized role format (should end with _REALM or _ROLE): " + role);
            }
            for (String r : role.split(",")) {
                Set<String> roles = getRoles(userDN);
                if (!roles.contains(r)) {
                    ModificationItem[] mods = new ModificationItem[1];
                    Attribute mod = new BasicAttribute("uniqueMember", userDN);
                    mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, mod);
                    String baseRolesName = getProperties().getProperty(TolvenConstants.BASE_ROLES_NAME);
                    String roleDNPrefix = getProperties().getProperty(TolvenConstants.ROLE_DN_PREFIX);
                    if (roleDNPrefix == null) {
                        throw new RuntimeException("Property not found: " + TolvenConstants.ROLE_DN_PREFIX);
                    }
                    if (baseRolesName == null) {
                        throw new RuntimeException("Property not found: " + TolvenConstants.BASE_ROLES_NAME);
                    }
                    String groupDN = roleDNPrefix + "=" + r + "," + baseRolesName;
                    getLdapContext().modifyAttributes(groupDN, mods);
                    logger.debug(getUserDN() + " added role: " + r + " to: " + userDN);
                }
            }
        } catch (TolvenAuthorizationException ex) {
            throw ex;
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Adding role: " + role + " to: " + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Adding role: " + role + " to: " + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException("Adding role: " + role + " to: " + userDN, ex);
        }
    }

    /**
     * Connect to admin account
     * @throws NamingException
     */
    public void adminConnect() throws NamingException {
        connect(getUserDN(), getUserPassword());
    }

    public void adminDisplayConfigAttributes(String cn) {
        try {
            adminConnect();
            displayAttributes(cn);
        } catch (NamingException e) {
            logger.debug("AttributeDisplay failed for cn " + cn + " " + e.getMessage());
        } finally {
            disconnect();
        }

    }

    public void adminSetup() {
        try {
            connect("cn=Directory Manager", "password".toCharArray());
            Attributes domainAttrs = new BasicAttributes();
            domainAttrs.put(new BasicAttribute("objectClass", "organizationalunit"));
            //			domainAttrs.put(new BasicAttribute("ou", "testpeople"));
            getLdapContext().createSubcontext("ou=testpeople,dc=tolven,dc=com", domainAttrs);
            //			Attributes attrs = new BasicAttributes();
            //			attrs.put(new BasicAttribute("objectClass", "organizationalunit"));
            //			domainCtx.createSubcontext("ou=people", attrs);

        } catch (NamingException e) {
            throw new RuntimeException("Setup error ", e);
        } finally {
            disconnect();
        }
    }

    /**
     * Change password of user given by userDN
     * 
     * @param newPassword
     */
    @Override
    public void changePassword(char[] newPassword) {
        changePassword(getUserDN(), getUserPassword(), newPassword);
    }

    /**
     * User given by getUserDN() changes password or the parameter userDN
     * 
     * @param userDN
     * @param oldPassword
     * @param newPassword
     */
    @Override
    public void changePassword(String userDN, char[] oldPassword, char[] newPassword) {
        try {
            // Prepare to get reason for change failure, if needed
            Control[] controls = new Control[] { new BasicControl(PASSWORD_POLICY_OID) };
            getLdapContext().setRequestControls(controls);
            // Setup extended operation
            PasswordChangeRequest req = new PasswordChangeRequest(userDN, newPassword);
            getLdapContext().extendedOperation(req);
            Attributes attrs = getAttributes(userDN, new String[] { "userPKCS12" });
            Attribute attr = attrs.get("userPKCS12");
            if (attr != null) {
                byte[] newBytes = null;
                try {
                    byte[] oldBytes = (byte[]) attr.get();
                    KeyStore keyStore = KeyHelper.getKeyStore(oldBytes, oldPassword);
                    KeyHelper.changeKeyStorePassword(keyStore, oldPassword, newPassword);
                    newBytes = KeyHelper.toByteArray(keyStore, newPassword);
                } catch (Exception ex) {
                    throw new TolvenAuthorizationException("change password for: " + userDN, getUserDN(), getRealm(), ex);
                }
                attrs.put("userPKCS12", newBytes);
                getLdapContext().modifyAttributes(userDN, DirContext.REPLACE_ATTRIBUTE, attrs);
            }
            setUserPassword(newPassword);
            logger.debug(getUserDN() + " changed password for " + userDN + " in realm: " + getRealm());
        } catch (TolvenAuthorizationException ex) {
            throw ex;
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Change password for:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Change password for:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException("Password change failed: " + userDN, ex);
        }
    }

    /**
     * Check password of user given by getUserDN()
     * 
     */
    @Override
    public void checkPassword() {
        checkPassword(getUserDN(), getUserPassword());
    }

    /**
     * User given by getUserDN() checks password of the user given by parameter userDN
     * 
     * @param userDN
     * @param password
     */
    @Override
    public void checkPassword(String userDN, char[] password) {
        try {
            logger.debug(getUserDN() + " checking password for: " + userDN + " at " + getProviderURL());
            //getLdapContext() authenticates the user.
            LdapContext ctx = getLdapContext();
            //The controls are saved and apply to the authenticated user
            setResponseControls(ctx.getResponseControls());
            logger.debug(getUserDN() + " obtained successful password check for " + userDN + " in realm: " + getRealm());
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Check password for:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Change password for:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException("Exception in password check for user: " + userDN + " ", ex);
        }
    }

    /**
     * Connect to LDAP object using the supplied URL and SSL properties 
     * <ul>
     * <li>-Djavax.net.ssl.keyStore=keystore.jks</li>
     * <li>-Djavax.net.ssl.trustStore=cacerts.jks</li>
     * <li>-Djavax.net.ssl.keyStorePassword=tolven</li>
     * </ul>
     * @param userObject Specifies the DN of the object to connect to
     * @param password The (user's) password in plan text
     * @throws NamingException
     */
    public void connect(String userObject, char[] password) throws NamingException {
        logger.debug("Connecting to " + userObject + " at " + getProviderURL());
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, getProviderURL());
        // env.put(Context.SECURITY_PROTOCOL, "ssl");

        // env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userObject);
        env.put(Context.SECURITY_CREDENTIALS, new String(password));

        // Create the initial context
        //		Control[] controls = new Control[]{new PasswordPolicyRequestControl(),new PasswordExpirationWarningControl()};
        //		ctx.setRequestControls(controls);
        setLdapContext(new InitialLdapContext(env, null));
        //		ctx = new InitialLdapContext(env, controls);
        setResponseControls(getLdapContext().getResponseControls());
        logger.debug("Connected to " + userObject + " at " + getProviderURL());
    }

    /**
     * Create a role
     * @param role
     */
    @Override
    public void createRole(String role) {
        try {
            logger.debug(getUserDN() + " creating role: " + role + " at " + getProviderURL());
            if (!(isRealm(role) || "tolven".equals(role)) && !(isRole(role))) {
                throw new RuntimeException("Unrecognized role format (should end with _REALM or _ROLE): " + role);
            }
            String baseRolesName = getProperties().getProperty(TolvenConstants.BASE_ROLES_NAME);
            String roleDNPrefix = getProperties().getProperty(TolvenConstants.ROLE_DN_PREFIX);
            if (roleDNPrefix == null) {
                throw new RuntimeException("Property not found: " + TolvenConstants.ROLE_DN_PREFIX);
            }
            if (baseRolesName == null) {
                throw new RuntimeException("Property not found: " + TolvenConstants.BASE_ROLES_NAME);
            }
            String groupDN = roleDNPrefix + "=" + role + "," + baseRolesName;
            Attributes groupOfUniqueNamesAttrs = new BasicAttributes();
            groupOfUniqueNamesAttrs.put(new BasicAttribute("objectClass", "groupOfUniqueNames"));
            getLdapContext().createSubcontext(groupDN, groupOfUniqueNamesAttrs);
            logger.debug(getUserDN() + " successfully created role: " + role);
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Create role:" + role, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Create role:" + role, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException(getUserDN() + " got exception in create user: " + role + " ", ex);
        }
    }

    /**
     * User given by getUserDN() creates user given by parameter userDN and userAttributes.
     * A generated password is returned if the userPassword is null, otherwise null is returned
     * 
     * @param userDN
     * @param userRealms
     * @param userPassword
     * @param userRoles
     * @param userAttributes
     * @return
     */
    @Override
    public char[] createUser(String userDN, String userRealms, char[] userPassword, String userRoles, Attributes userAttributes) {
        try {
            logger.debug(getUserDN() + " creating user: " + userDN + " in realm: " + getRealm() + " at " + getProviderURL());
            Attributes attrs = new BasicAttributes(false);
            NamingEnumeration<String> namingEnum = userAttributes.getIDs();
            String attrID = null;
            while (namingEnum.hasMoreElements()) {
                attrID = namingEnum.next();
                Attribute attr = userAttributes.get(attrID);
                if (!"userPassword".equalsIgnoreCase(attrID)) {
                    /*
                     * Do not send the SSHA of the password, since the password is handled by the PasswordChangeRequest
                     */
                    attrs.put(attr);
                }
            }
            getLdapContext().createSubcontext(userDN, userAttributes);
            PasswordChangeRequest req = new PasswordChangeRequest(userDN, userPassword);
            getLdapContext().extendedOperation(req);
            char[] generatedPassword = null;
            if (userPassword == null) {
                generatedPassword = req.getNewPassword();
                userPassword = generatedPassword;
                logger.debug(getUserDN() + " successfully generated password for user: " + userDN + " in realm: " + getRealm());
            }
            if (userAttributes.get("userPKCS12") == null) {
                Attributes certAttrs = generateNewUserPKCS12Attributes(userDN, userPassword);
                getLdapContext().modifyAttributes(userDN, DirContext.REPLACE_ATTRIBUTE, certAttrs);
                logger.info(getUserDN() + " generated new userPKCS12 for " + userDN + " in realm: " + getRealm());
            }
            if (userRealms == null) {
                addRole("tolven_REALM", userDN);
            } else {
                for (String realm : userRealms.split(",")) {
                    //TODO Reference to tolven is for backward compatibility
                    if (!(isRealm(realm) || "tolven".equals(realm))) {
                        throw new RuntimeException("Unrecognized realm format (should end with _REALM): " + realm);
                    }
                    addRole(realm, userDN);
                }
            }
            if (userRoles != null) {
                for (String role : userRoles.split(",")) {
                    if (!(isRole(role))) {
                        throw new RuntimeException("Unrecognized role format (should end with _ROLE): " + role);
                    }
                    addRole(role, userDN);
                }
            }
            logger.info(getUserDN() + " successfully created user: " + userDN + " in realm: " + getRealm());
            //Only return generated passwords
            return generatedPassword;
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Create user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Create user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException(getUserDN() + " got exception in create user: " + userDN + " ", ex);
        }
    }

    /**
     * Disconnect from LDAP
     */
    @Override
    public void disconnect() {
        logger.debug(getUserDN() + " disconnect from " + getProviderURL());
        try {
            if (getLdapContext() != null) {
                getLdapContext().close();
            }
        } catch (Exception e) {
            logger.debug("Ignoring Ldap context close exception " + e.getMessage());
        } finally {
            setLdapContext(null);
        }
    }

    /**
     * Display all attributes from the specified LDAP DN. The connection must already be open.
     * @param userObject
     * @throws NamingException
     */
    public void displayAttributes(String userObject) throws NamingException {
        Attributes attrs = getLdapContext().getAttributes(userObject);
        //		Attributes attrs = ctx.getAttributes(userObject, attrIDs);
        setResponseControls(getLdapContext().getResponseControls());
        NamingEnumeration<? extends Attribute> ae = attrs.getAll();
        logger.debug(" Attributes ");
        while (ae.hasMore()) {
            Attribute attr = (Attribute) ae.next();
            if (attr.get(0) instanceof byte[]) {
                logger.debug(attr.getID() + "=" + new String((byte[]) attr.get(0)) + ", ");
            } else {
                logger.debug(attr.getID() + "=" + attr.get(0) + ", ");
            }
        }
    }

    /**
     * If the connection is still floating around when we destroy this object, then disconnect now.
     */
    @Override
    protected void finalize() {
        if (getLdapContext() != null) {
            disconnect();
        }
    }

    /**
     * Find user userDN
     * 
     * @param userDN
     * @return
     */
    @Override
    public Attributes findUser(String userDN) {
        try {
            NamingEnumeration<SearchResult> namingEnum = null;
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctls.setCountLimit(1);
            ctls.setTimeLimit(1000);
            int index = userDN.indexOf(",");
            if (index == -1) {
                throw new RuntimeException("Unrecognized format for userDN: " + userDN);
            }
            String name = userDN.substring(0, index);
            String baseName = userDN.substring(index + 1);
            namingEnum = getLdapContext().search(baseName, name, ctls);
            while (namingEnum.hasMore()) {
                SearchResult result = namingEnum.next();
                return result.getAttributes();
            }
            return null;
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Find user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Find user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException(getUserDN() + " got exception in find user: " + userDN + " ", ex);
        }
    }

    private Attributes generateNewUserPKCS12Attributes(String username, char[] generatedPassword) {
        Attributes currentAttrs = getAttributes(username, new String[] {
                "cn",
                "ou",
                "o",
                "st" });
        Attribute cnAttr = currentAttrs.get("cn");
        if (cnAttr == null) {
            throw new RuntimeException("To generate userPKCS12 user: " + username + " must have a defined: cn in LDAP");
        }
        Attribute ouAttr = currentAttrs.get("ou");
        if (ouAttr == null) {
            throw new RuntimeException("To generate userPKCS12 user: " + username + " must have a defined: ou in LDAP");
        }
        Attribute oAttr = currentAttrs.get("o");
        if (oAttr == null) {
            throw new RuntimeException("To generate userPKCS12 user: " + username + " must have a defined: o in LDAP");
        }
        Attribute stAttr = currentAttrs.get("st");
        if (stAttr == null) {
            throw new RuntimeException("To generate userPKCS12 user: " + username + " must have a defined: st in LDAP");
        }
        String cn = null;
        String ou = null;
        String o = null;
        String st = null;
        try {
            cn = (String) cnAttr.get();
            ou = (String) ouAttr.get();
            o = (String) oAttr.get();
            st = (String) stAttr.get();
        } catch (NamingException ex) {
            throw new RuntimeException("Could not get current cert creation attributes for: " + username, ex);
        }
        Attributes attrs = new BasicAttributes(true);
        KeyStore userPKCS12KeyStore = getCertificateHelper().createPKCS12KeyStore(username, cn, ou, o, st, generatedPassword);
        byte[] userPKCS12Bytes = KeyHelper.toByteArray(userPKCS12KeyStore, generatedPassword);
        Attribute keystoreAttr = new BasicAttribute("userPKCS12");
        keystoreAttr.add(userPKCS12Bytes);
        attrs.put(keystoreAttr);
        Attribute certAttr = new BasicAttribute("userCertificate;binary");
        byte[] certBytes = KeyHelper.getX509CertificateByteArray(userPKCS12KeyStore);
        certAttr.add(certBytes);
        attrs.put(certAttr);
        return attrs;
    }

    /**
     * Generate new credentials for user who is logged in, but only if they had none
     */
    @Override
    public boolean generateNewUserPKCS12IfNone() {
        String userDN = getUserDN();
        try {
            Attributes userAttributes = getAttributes(userDN, new String[] { "userPKCS12" });
            if (userAttributes.get("userPKCS12") == null) {
                Attributes certAttrs = generateNewUserPKCS12Attributes(userDN, getUserPassword());
                getLdapContext().modifyAttributes(userDN, DirContext.REPLACE_ATTRIBUTE, certAttrs);
                logger.info(getUserDN() + " autogenerated new userPKCS12 for " + userDN + " in realm: " + getRealm());
                return true;
            } else {
                logger.warn(getUserDN() + " no userPKCS12 generated for " + userDN + " in realm: " + getRealm());
                return false;
            }
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Create user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Create user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException(getUserDN() + " got exception in create user: " + userDN + " ", ex);
        }
    }

    /**
     * User given by getUserDN() gets attributes of user given by parameter userDN
     * 
     * @param userDN
     * @param attrIds
     * @return
     */
    @Override
    public Attributes getAttributes(String userDN, String[] attrIds) {
        try {
            return getLdapContext().getAttributes(userDN, attrIds);
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Get attributes for user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Get attributes for user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException("Could not get ldap attributes for user: " + userDN, ex);
        }
    }

    /**
     * Get atributes of user given by getUserDN()
     * 
     * @param attrIds
     * @return
     */
    @Override
    public Attributes getAttributes(String[] attrIds) {
        return getAttributes(getUserDN(), attrIds);
    }

    public KeyHelper getCertificateHelper() {
        if (certificateHelper == null) {
            certificateHelper = new KeyHelper();
        }
        return certificateHelper;
    }

    public Hashtable<String, String> getEnvironment() {
        return environment;
    }

    public LdapContext getLdapContext() {
        if (ldapContext == null) {
            try {
                /*
                 * The creation of the InitialLdapContext triggers an LDAP authentication
                 */
                ldapContext = new InitialLdapContext(getEnvironment(), null);
            } catch (AuthenticationException ex) {
                throw new TolvenAuthenticationException("Authentication failed", getUserDN(), getRealm(), ex);
            } catch (NoPermissionException ex) {
                throw new TolvenAuthorizationException("Authorization failed", getUserDN(), getRealm(), ex);
            } catch (Exception ex) {
                throw new RuntimeException("Could not get InitialLdapContext for: " + getUserDN(), ex);
            }
        }
        return ldapContext;
    }

    /**
     * Determine whether the password of user given by getUserDN() is expiring.
     * null means it is not, otherwise the remaining time can be determined from the returned PasswordExpiring
     * 
     * @return
     */
    @Override
    public PasswordExpiring getPasswordExpiring() {
        if (getResponseControls() == null) {
            return null;
        }
        for (int x = 0; x < getResponseControls().length; x++) {
            if (PASSWORD_EXPIRING_OID.equals(getResponseControls()[x].getID())) {
                return new PasswordExpiring(getResponseControls()[x]);
            }
        }
        return null;
    }

    /**
     * Return the control that specifies that the password is about to expire.
     * @return 
     */
    public PasswordPolicy getPasswordPolicy() {
        if (getResponseControls() == null)
            return null;
        for (int x = 0; x < getResponseControls().length; x++) {
            if (PASSWORD_POLICY_OID.equals(getResponseControls()[x].getID())) {
                return new PasswordPolicy(getResponseControls()[x]);
            }
        }
        return null;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public String getProviderURL() {
        return (String) getEnvironment().get(Context.PROVIDER_URL);
    }

    /**
     * Return the realm for this LDAP
     * 
     * @return
     */
    @Override
    public String getRealm() {
        return getProperties().getProperty("realm");
    }

    /**
     * Get realms of user given by getUserDN()
     * 
     * @return
     */
    @Override
    public String getRealms() {
        List<String> realms = new ArrayList<String>();
        for (String role : getRoles()) {
            if (isRealm(role)) {
                realms.add(role);
            }
        }
        Collections.sort(realms);
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = realms.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public Control[] getResponseControls() {
        return responseControls;
    }

    /**
     * Get roles of user given by getUserDN()
     * 
     * @return
     */
    @Override
    public Set<String> getRoles() {
        return getRoles(getUserDN());
    }

    /**
     * User given by getUserDN() gets roles of user given by parameter userDN
     * 
     * @param userDN
     * @return
     */
    @Override
    public Set<String> getRoles(String userDN) {
        try {
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String roleDNPrefix = getProperties().getProperty(ROLE_DN_PREFIX);
            if (roleDNPrefix == null) {
                throw new RuntimeException("Property not found: " + ROLE_DN_PREFIX);
            }
            ctls.setReturningAttributes(new String[] { roleDNPrefix });
            ctls.setTimeLimit(10000);
            Object[] filterArgs = { userDN };
            String baseRolesName = getProperties().getProperty(BASE_ROLES_NAME);
            if (baseRolesName == null) {
                throw new RuntimeException("Property not found: " + BASE_ROLES_NAME);
            }
            NamingEnumeration<SearchResult> namingEnum = getLdapContext().search(baseRolesName, "(uniqueMember={0})", filterArgs, ctls);
            String roleName = null;
            Set<String> roles = new HashSet<String>();
            StringBuffer buff = new StringBuffer();
            buff.append("[");
            while (namingEnum.hasMore()) {
                SearchResult rslt = namingEnum.next();
                Attributes attrs = rslt.getAttributes();
                Attribute rolesAttr = attrs.get(roleDNPrefix);
                for (int i = 0; i < rolesAttr.size(); i++) {
                    roleName = (String) rolesAttr.get(i);
                    roles.add(roleName);
                    buff.append(roleName);
                    if (i < rolesAttr.size() - 1) {
                        buff.append(",");
                    }
                }
            }
            buff.append("]");
            if (logger.isDebugEnabled()) {
                logger.debug(userDN + "has roles: " + buff.toString());
            }
            return roles;
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Get roles for user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Get roles for user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException("Could not get ldap roles for user: " + userDN, ex);
        }
    }

    private String getUser(String userDN) {
        int index = userDN.indexOf(",");
        String user = null;
        if (index == -1) {
            user = userDN;
        } else {
            user = userDN.substring(0, index);
        }
        return user;
    }

    /**
     * Return the userDN which is connected to LDAP by this instance
     * 
     * @return
     */
    @Override
    public String getUserDN() {
        return getEnvironment().get(Context.SECURITY_PRINCIPAL);
    }

    public char[] getUserPassword() {
        if (getEnvironment().get(Context.SECURITY_CREDENTIALS) == null) {
            return null;
        } else {
            return getEnvironment().get(Context.SECURITY_CREDENTIALS).toCharArray();
        }
    }

    /**
     * Determine whether the password of user given by getUserDN() is expired.
     * 
     * @return
     */
    @Override
    public boolean isPasswordExpired() {
        if (getResponseControls() != null) {
            for (int x = 0; x < getResponseControls().length; x++) {
                if (PASSWORD_EXPIRED_OID.equals(getResponseControls()[x].getID())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRealm(String role) {
        return role.toLowerCase().endsWith("_REALM".toLowerCase());
    }

    private boolean isRole(String role) {
        return role.toLowerCase().endsWith("_ROLE".toLowerCase());
    }

    /**
     * Update attributes for userDN
     * 
     * @param userDN
     * @param attrs
     */
    @Override
    public void modifyAttributes(String userDN, Attributes attrs) {
        try {
            getLdapContext().modifyAttributes(userDN, DirContext.REPLACE_ATTRIBUTE, attrs);
            logger.debug(getUserDN() + " updated attributes for: " + userDN + " in realm: " + getRealm());
        } catch (TolvenAuthorizationException ex) {
            throw ex;
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Modify attributes for:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Modify attributes for:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException("Modified attributes failed: " + userDN, ex);
        }
    }

    public void printControls() {
        if (getResponseControls() != null) {
            for (int x = 0; x < getResponseControls().length; x++) {
                logger.debug("responseControl: " + getResponseControls()[x].getID() + " " + new String(getResponseControls()[x].getEncodedValue()));
            }
        }
    }

    /**
     * Remove role from userDN
     * 
     * @param role
     * @param userDN
     */
    @Override
    public void removeRole(String role, String userDN) {
        try {
            Set<String> roles = getRoles(userDN);
            for (String r : role.split(",")) {
                if (roles.remove(r)) {
                    ModificationItem[] mods = new ModificationItem[1];
                    Attribute mod = new BasicAttribute("uniqueMember", userDN);
                    mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod);
                    String baseRolesName = getProperties().getProperty(TolvenConstants.BASE_ROLES_NAME);
                    String roleDNPrefix = getProperties().getProperty(TolvenConstants.ROLE_DN_PREFIX);
                    if (roleDNPrefix == null) {
                        throw new RuntimeException("Property not found: " + TolvenConstants.ROLE_DN_PREFIX);
                    }
                    if (baseRolesName == null) {
                        throw new RuntimeException("Property not found: " + TolvenConstants.BASE_ROLES_NAME);
                    }
                    String groupDN = roleDNPrefix + "=" + r + "," + baseRolesName;
                    getLdapContext().modifyAttributes(groupDN, mods);
                    logger.debug(getUserDN() + " removed role: " + r + " from: " + userDN);
                }
            }
        } catch (TolvenAuthorizationException ex) {
            throw ex;
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Removing role: " + role + " from: " + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Removing role: " + role + " from: " + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException ex) {
            throw new RuntimeException("Removing role: " + role + " from: " + userDN, ex);
        }
    }

    /**
     * User given by getUserDN() resets the password of user given by parameter userDN
     * 
     * @param userDN
     * @return
     * @throws NamingSecurityException
     */
    @Override
    public char[] resetPassword(String userDN) {
        char[] generatedPassword = null;
        try {
            adminConnect();
            // Setup extended operation
            PasswordChangeRequest req = new PasswordChangeRequest(userDN, null);
            getLdapContext().extendedOperation(req);
            generatedPassword = req.getNewPassword();
            Attributes certAttrs = generateNewUserPKCS12Attributes(userDN, generatedPassword);
            getLdapContext().modifyAttributes(userDN, DirContext.REPLACE_ATTRIBUTE, certAttrs);
            logger.info(getUserDN() + " generated userPKCS12 during resetPassword for " + userDN + " in realm: " + getRealm());
        } catch (AuthenticationException ex) {
            throw new TolvenAuthenticationException("Reset password for user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NoPermissionException ex) {
            throw new TolvenAuthorizationException("Reset password for user:" + userDN, getUserDN(), getRealm(), ex);
        } catch (NamingException e) {
            throw new RuntimeException("Password reset failed: " + userDN, e);
        }
        return generatedPassword;
    }

    public void setCertificateHelper(KeyHelper certificateHelper) {
        this.certificateHelper = certificateHelper;
    }

    public void setEnvironment(Hashtable<String, String> environment) {
        this.environment = environment;
    }

    public void setLdapContext(LdapContext ldapContext) {
        this.ldapContext = ldapContext;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setResponseControls(Control[] responseControls) {
        this.responseControls = responseControls;
    }

    public void setUserDN(String username) {
        getEnvironment().put(Context.SECURITY_PRINCIPAL, username);
    }

    public void setUserPassword(char[] userPassword) {
        getEnvironment().put(Context.SECURITY_CREDENTIALS, new String(userPassword));
    }

}
