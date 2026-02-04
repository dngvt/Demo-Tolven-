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
 * @version $Id: LdapManagerFactory.java 5891 2012-02-14 06:33:24Z joe.isaac $
 */
package org.tolven.ldap;

import java.util.Hashtable;
import java.util.Properties;

public class LdapManagerFactory {

    public static final String BASE_ROLES_NAME = "baseRolesName";
    public static final String REALM = "realm";
    public static final String ROLE_DN_PREFIX = "roleDNPrefix";
    
    /**
     * @param envProps contains environment properties (sent to LDAP)
     * 
     * @param envProps
     * @param ldapProps
     * @return
     */
    public static LdapManager getInstance(Hashtable<String, String> envProps) {
        return getInstance(envProps, null);
    }

    /**
     * @param envProps contains environment properties (sent to LDAP) while props contains properties used to search LDAP
     * 
     * @param envProps
     * @param ldapProps
     * @return
     */
    public static LdapManager getInstance(Hashtable<String, String> envProps, Properties ldapProps) {
        DefaultLdapManager ldapManager = new DefaultLdapManager();
        ldapManager.setEnvironment(envProps);
        ldapManager.setProperties(ldapProps);
        return ldapManager;
    }

    /**
     * @param envProps contains environment properties (sent to LDAP)
     * 
     * @param envProps
     * @param ldapProps
     * @return
     */
    public static LdapManager getInstance(Properties envProps) {
        return getInstance(envProps, null);
    }

    /**
     * @param envProps contains environment properties (sent to LDAP) while props contains properties used to search LDAP
     * 
     * @param envProps
     * @param ldapProps
     * @return
     */
    public static LdapManager getInstance(Properties envProps, Properties ldapProps) {
        Hashtable<String, String> env = new Hashtable<String, String>();
        for (String key : envProps.stringPropertyNames()) {
            env.put(key, envProps.getProperty(key));
        }
        return getInstance(env, ldapProps);
    }

}
