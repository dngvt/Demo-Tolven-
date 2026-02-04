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
package org.tolven.gatekeeper.bean;

import java.security.KeyStore;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.directory.Attributes;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.tolven.exception.TolvenSecurityException;
import org.tolven.gatekeeper.LdapLocal;
import org.tolven.ldap.LdapManager;
import org.tolven.naming.LdapRealmContext;
import org.tolven.naming.TolvenContext;
import org.tolven.naming.TolvenPerson;
import org.tolven.security.cert.KeyHelper;
import org.tolven.session.TolvenSessionWrapperFactory;

/**
 * Interface to communicate with LDAP based on a realm lookup of the Directory service
 * 
 * @author Joseph Isaac
 *
 */
@Stateless()
@Local(LdapLocal.class)
public class LdapBean implements LdapLocal {

    private static TolvenContext tolvenContext;
    private Logger logger = Logger.getLogger(LdapBean.class);

    /**
     * Add role to a user
     * 
     * @param role
     * @param uid
     * @param realm
     * @param requestorPassword
     */
    @Override
    public void addRole(String role, String uid, String realm, char[] requestorPassword) {
        LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        String tolvenPersonDN = ldapRealmContext.getDN(uid);
        LdapManager ldapManager = null;
        try {
            ldapManager = ldapRealmContext.getLdapManager(principal, requestorPassword);
            ldapManager.addRole(role, tolvenPersonDN);
        } catch (TolvenSecurityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to add role: " + role + " to user" + uid + " for principal: " + principal, ex);
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
    }

    /**
     * Change userPassword
     * 
     * @param uid
     * @param oldPassword
     * @param realm
     * @param newPassword
     */
    @Override
    public void changeUserPassword(String uid, char[] oldPassword, String realm, char[] newPassword) {
        LdapManager ldapManager = null;
        try {
            LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
            ldapManager = ldapRealmContext.getLdapManager(uid, oldPassword);
            ldapManager.changePassword(newPassword);
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
    }

    /**
     * Create a role in ldap
     * 
     * @param role
     * @param realm
     * @param requestorPassword
     */
    @Override
    public void createRole(String role, char[] requestorPassword) {
        String realm = (String) TolvenSessionWrapperFactory.getInstance().getRealm();
        LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        LdapManager ldapManager = null;
        try {
            ldapManager = ldapRealmContext.getLdapManager(principal, requestorPassword);
            ldapManager.createRole(role);
        } catch (TolvenSecurityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to find role: " + role + " for principal: " + principal, ex);
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
    }

    /**
     * Create a TolvenPerson, supplying the uid, realm, userPassword and userPKCS12 explicitly, although
     * tolvenPerson may contain those, as well as other attributes
     * 
     * @param tolvenPerson
     * @param uid
     * @param uidPassword
     * @param realm
     * @param base64UserPKCS12
     * @param admin
     * @param adminPassword
     * @return
     */
    @Override
    public char[] createTolvenPerson(TolvenPerson tolvenPerson, String uid, char[] uidPassword, String realm, String base64UserPKCS12, String admin, char[] adminPassword) {
        LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
        String tolvenPersonDN = ldapRealmContext.getDN(tolvenPerson.getUid());
        String tolvenPersonRole = ldapRealmContext.getTolvenPersonRole();
        LdapManager ldapManager = null;
        try {
            if (base64UserPKCS12 != null) {
                updateUserCredentials(tolvenPerson, uidPassword, base64UserPKCS12);
            }
            ldapManager = ldapRealmContext.getLdapManager(admin, adminPassword);
            //char[] generatedPassword = ldapManager.createUser(tolvenPersonDN, uidPassword, tolvenPerson.dirAttributes(false));
            char[] generatedPassword = ldapManager.createUser(tolvenPersonDN, null, uidPassword, tolvenPersonRole, tolvenPerson.dirAttributes(false));
            logger.info(admin + " added " + tolvenPersonDN + " to LDAP realm: " + realm);
            return generatedPassword;
        } catch (TolvenSecurityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create TolvenPerson: " + tolvenPersonDN + " in realm " + realm + " for admin " + admin, ex);
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
    }

    /**
     * Create a TolvenPerson, supplying the uid and realm explicitly, although tolvenPerson may contain those, as well as other attributes
     * The userPassword and credentials will be generated automatically
     * @param tolvenPerson
     * @param uid
     * @param realm
     * @param admin
     * @param adminPassword
     * @return
     */
    public char[] createTolvenPerson(TolvenPerson tolvenPerson, String uid, String realm, String admin, char[] adminPassword) {
        return createTolvenPerson(tolvenPerson, uid, null, realm, null, admin, adminPassword);
    }

    /**
     * Find a TolvenPerson
     * 
     * @param uid
     * @param realm
     * @param admin
     * @param adminPassword
     * @return
     */
    @Override
    public TolvenPerson findTolvenPerson(String uid, String realm, String admin, char[] adminPassword) {
        LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
        String tolvenPersonDN = ldapRealmContext.getDN(uid);
        LdapManager ldapManager = null;
        try {
            ldapManager = ldapRealmContext.getLdapManager(admin, adminPassword);
            Attributes attrs = ldapManager.findUser(tolvenPersonDN);
            if (attrs == null) {
                return null;
            } else {
                return new TolvenPerson(tolvenPersonDN, attrs);
            }
        } catch (TolvenSecurityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to find TolvenPerson: " + tolvenPersonDN + " in realm " + realm + " for admin: " + admin, ex);
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
    }

    private LdapRealmContext getLdapRealmContext(String realm) {
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

    /**
     * Get a user's X509Certificate
     * 
     * @param uid
     * @param realm
     * @param requestorId
        LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
        String tolvenPersonDN = ldapRealmContext.getDN(uid);
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        LdapManager ldapManager = null;
        try {
            ldapManager = ldapRealmContext.getLdapManager(principal, requestorPassword);
            Attributes attrs = ldapManager.findUser(tolvenPersonDN);
            TolvenPerson tolvenPerson = null;
            if (attrs != null) {
                tolvenPerson = tolvenPersonAttributes.getTolvenPerson(tolvenPersonDN, attrs);
                tolvenPerson.setRealms(ldapManager.getRealms());
            }
            return tolvenPerson;
        } catch (TolvenSecurityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to find TolvenPerson: " + uid + " in realm " + realm + " for principal: " + principal, ex);
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
     * @param requestorPassword
     * @return
     */
    @Override
    public byte[] getUserX509Certificate(String uid, String realm, String requestorId, char[] requestorPassword) {
        LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
        String tolvenPersonDN = ldapRealmContext.getDN(uid);
        LdapManager ldapManager = null;
        try {
            ldapManager = ldapRealmContext.getLdapManager(requestorId, requestorPassword);
            Attributes attrs = ldapManager.getAttributes(tolvenPersonDN, new String[] { "userCertificate" });
            if (attrs == null) {
                return null;
            } else {
                byte[] userCertificateBytes = null;
                if(attrs.get("userCertificate;binary") != null) {
                    userCertificateBytes = (byte[]) attrs.get("userCertificate;binary").get();
                } else if(attrs.get("userCertificate") != null) {
                    userCertificateBytes = (byte[]) attrs.get("userCertificate").get();
                }
                return userCertificateBytes;
            }
        } catch (TolvenSecurityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to find X509Certificate for TolvenPerson: " + tolvenPersonDN + " in realm " + realm + " for requestor: " + requestorId, ex);
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
    }

    /**
     * Remove role to a user
     * 
     * @param role
     * @param uid
     * @param realm
     * @param requestorPassword
     */
    @Override
    public void removeRole(String role, String uid, String realm, char[] requestorPassword) {
        LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        String tolvenPersonDN = ldapRealmContext.getDN(uid);
        LdapManager ldapManager = null;
        try {
            ldapManager = ldapRealmContext.getLdapManager(principal, requestorPassword);
            ldapManager.removeRole(role, tolvenPersonDN);
        } catch (TolvenSecurityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to remove role: " + role + " from user" + uid + " for principal: " + principal, ex);
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
    }

    /**
     * Reset userPassword
     * 
     * @param uid
     * @param realm
     * @param admin
     * @param adminPassword
     * @return
     */
    @Override
    public char[] resetUserPassword(String uid, String realm, String admin, char[] adminPassword) {
        LdapManager ldapManager = null;
        try {
            LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
            ldapManager = ldapRealmContext.getLdapManager(admin, adminPassword);
            String userDN = ldapRealmContext.getDN(uid);
            return ldapManager.resetPassword(userDN);
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
    }

    private void updateUserCredentials(TolvenPerson tolvenPerson, char[] userPassword, String base64UserPKCS12) {
        if (userPassword == null) {
            throw new RuntimeException("A base64UserPKCS12 has been supplied without the accompanying user password");
        }
        byte[] userPKCS12Bytes = null;
        try {
            userPKCS12Bytes = Base64.decodeBase64(base64UserPKCS12.getBytes("UTF-8"));
        } catch (Exception ex) {
            throw new RuntimeException("Could not convert base64UserPKCS12 to bytes", ex);
        }
        KeyStore userPKCS12KeyStore = KeyHelper.getKeyStore(userPKCS12Bytes, userPassword);
        byte[] certBytes = KeyHelper.getX509CertificateByteArray(userPKCS12KeyStore);
        tolvenPerson.setAttributeValue("userPKCS12", userPKCS12Bytes);
        tolvenPerson.setAttributeValue("userCertificate", certBytes);
    }

    /**
     * Verify password.
     * @param uid
     * @param password
     * @param realm
     * @return
     */
    @Override
    public boolean verifyPassword(String uid, char[] password, String realm) {
        LdapManager ldapManager = null;
        try {
            LdapRealmContext ldapRealmContext = getLdapRealmContext(realm);
            ldapManager = ldapRealmContext.getLdapManager(uid, password);
            ldapManager.checkPassword();
            return true;
        } catch (TolvenSecurityException ex) {
            logger.info(ex.getMessage());
            return false;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to verify password for uid: " + uid + " in realm " + realm + " by: " + uid, ex);
        } finally {
            if (ldapManager != null) {
                ldapManager.disconnect();
            }
        }
    }

}
