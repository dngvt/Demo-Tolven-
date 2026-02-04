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
 * @version $Id: DefaultJndiManager.java 1779 2011-07-22 04:31:50Z joe.isaac $
 */
package org.tolven.naming;

import java.util.Properties;

public class DefaultJndiManager implements JndiManager {

    public static final String TOLVENCONTEXT_PREFIX = TOLVENCONTEXT_ID_REF + ".";
    public static final String GATEKEEPER_WEB_HTML_ID = "gatekeeperhtml";
    public static final String GATEKEEPER_WEB_RS_ID = "gatekeeperrs";

    public static final String SSOCOOKIE_NAME_SUFFIX = ".ssocookie.name";
    public static final String SSOCOOKIE_DOMAIN_SUFFIX = ".ssocookie.domain";
    public static final String SSOCOOKIE_SECURE_SUFFIX = ".ssocookie.secure";
    public static final String SSOCOOKIE_PATH_SUFFIX = ".ssocookie.path";
    public static final String SSOCOOKIE_MAXAGE_SUFFIX = ".ssocookie.maxAge";

    public static final String CONTEXTPATH_SUFFIX = ".contextPath";
    public static final String SSLPORT_SUFFIX = ".sslPort";
    public static final String LOGINPATH_SUFFIX = ".loginPath";
    public static final String LOGINURL_SUFFIX = ".loginUrl";
    public static final String INTERFACE_SUFFIX = ".interface";

    public static final String ENVIRONMENT_SUFFIX = ".realm.ldap.env";
    public static final String BASE_DN_SUFFIX = ".realm.ldap.baseDN";
    public static final String BASE_PEOPLE_NAME_SUFFIX = ".realm.ldap.basePeopleName";
    public static final String BASE_ROLES_NAME_SUFFIX = ".realm.ldap.baseRolesName";
    public static final String TOLVENPERSON_ROLE = ".realm.ldap.tolvenPersonRole";
    public static final String USER_SUBSTITUTION_TOKEN_SUFFIX = ".realm.ldap.userSubstitutionToken";
    public static final String USER_DN_TEMPLATE_SUFFIX = ".realm.ldap.userDnTemplate";
    public static final String PRINCIPAL_DN_PREFIX_SUFFIX = ".realm.ldap.principalDNPrefix";
    public static final String ROLE_DN_PREFIX_SUFFIX = ".realm.ldap.roleDNPrefix";
    public static final String SESSION_ATTRIBUTES_SUFFIX = ".realm.ldap.sessionAttributes";

    public static final String USER_SUFFIX = ".user";
    public static final String REALM_SUFFIX = ".realm";

    public static final String[] TOLVEN_SUFFIXES = new String[] {
            SSOCOOKIE_NAME_SUFFIX,
            SSOCOOKIE_DOMAIN_SUFFIX,
            SSOCOOKIE_SECURE_SUFFIX,
            SSOCOOKIE_PATH_SUFFIX,
            SSOCOOKIE_MAXAGE_SUFFIX };

    public static final String[] WEB_SUFFIXES = new String[] {
            CONTEXTPATH_SUFFIX,
            SSLPORT_SUFFIX,
            LOGINPATH_SUFFIX,
            LOGINURL_SUFFIX,
            INTERFACE_SUFFIX };

    public static final String[] REALM_SUFFIXES = new String[] {
            ENVIRONMENT_SUFFIX,
            BASE_DN_SUFFIX,
            BASE_PEOPLE_NAME_SUFFIX,
            BASE_ROLES_NAME_SUFFIX,
            TOLVENPERSON_ROLE,
            USER_SUBSTITUTION_TOKEN_SUFFIX,
            USER_DN_TEMPLATE_SUFFIX,
            PRINCIPAL_DN_PREFIX_SUFFIX,
            ROLE_DN_PREFIX_SUFFIX,
            SESSION_ATTRIBUTES_SUFFIX };

    public static final String[] QUEUE_SUFFIXES = new String[] {
            USER_SUFFIX,
            REALM_SUFFIX };

    public DefaultJndiManager() {
    }

    @Override
    public Properties getJndiProperties(Properties srcProperties) {
        Properties jndiProperties = new Properties();
        updateTolvenId(jndiProperties, srcProperties);
        updateWebIds(jndiProperties, srcProperties);
        updateRealmIds(jndiProperties, srcProperties);
        updateQueueIds(jndiProperties, srcProperties);
        return jndiProperties;
    }

    private void updateTolvenId(Properties jndiProperties, Properties srcProperties) {
        addProperties("", TOLVEN_SUFFIXES, jndiProperties, srcProperties);
    }

    private void updateWebIds(Properties jndiProperties, Properties srcProperties) {
        String ids = srcProperties.getProperty(WEB_IDS_REF);
        if (ids == null) {
            throw new RuntimeException("Could not find property " + WEB_IDS_REF + " in source for jndi properties");
        }
        jndiProperties.setProperty(WEB_IDS_REF, ids);
        String[] webIds = ids.split(",");
        for (String webId : webIds) {
            addProperties(webId, WEB_SUFFIXES, jndiProperties, srcProperties);
        }
    }

    private void updateRealmIds(Properties jndiProperties, Properties srcProperties) {
        String ids = srcProperties.getProperty(REALM_IDS_REF);
        if (ids == null) {
            throw new RuntimeException("Could not find property " + REALM_IDS_REF + " in source for jndi properties");
        }
        jndiProperties.setProperty(REALM_IDS_REF, ids);
        String[] realmIds = ids.split(",");
        if (realmIds.length == 0) {
            throw new RuntimeException("At least one value must exist for jndi property realmIds");
        }
        for (String realmId : realmIds) {
            addProperties(realmId, REALM_SUFFIXES, jndiProperties, srcProperties);
        }
    }

    private void updateQueueIds(Properties jndiProperties, Properties srcProperties) {
        String ids = srcProperties.getProperty(QUEUE_IDS_REF);
        if (ids == null) {
            throw new RuntimeException("Could not find property " + QUEUE_IDS_REF + " in source for jndi properties");
        }
        jndiProperties.setProperty(QUEUE_IDS_REF, ids);
        String[] queueIds = ids.split(",");
        if (queueIds.length == 0) {
            throw new RuntimeException("At least one value must exist for jndi property queueIds");
        }
        for (String queueId : queueIds) {
            addProperties(queueId, QUEUE_SUFFIXES, jndiProperties, srcProperties);
            addProperties("password." + queueId, jndiProperties, srcProperties);
        }
    }

    private void addProperties(String prefix, String[] suffixes, Properties jndiProperties, Properties srcProperties) {
        for (String suffix : suffixes) {
            String nameKey = null;
            if (prefix.length() == 0) {
                nameKey = TOLVENCONTEXT_ID_REF + suffix;
            } else {
                nameKey = TOLVENCONTEXT_ID_REF + "." + prefix + suffix;
            }
            addProperties(nameKey, jndiProperties, srcProperties);
        }
    }

    private void addProperties(String nameKey, Properties jndiProperties, Properties srcProperties) {
        for (String srcPropertyName : srcProperties.stringPropertyNames()) {
            if (srcPropertyName.startsWith(nameKey)) {
                String value = srcProperties.getProperty(srcPropertyName);
                if (value != null) {
                    if (value.contains("${")) {
                        throw new RuntimeException(TOLVENCONTEXT_PREFIX + nameKey + " contains undefined properties: " + value);
                    }
                    jndiProperties.setProperty(srcPropertyName, value);
                }
            }
        }
    }

}
