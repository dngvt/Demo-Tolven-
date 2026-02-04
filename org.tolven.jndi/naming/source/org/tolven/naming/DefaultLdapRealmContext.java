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
package org.tolven.naming;

import java.util.Properties;

import javax.naming.Context;

import org.tolven.ldap.LdapManager;
import org.tolven.ldap.LdapManagerFactory;

public class DefaultLdapRealmContext extends DefaultTolvenJndiContext implements LdapRealmContext {

    private Properties mapping;

    public DefaultLdapRealmContext() {
    }

    @Override
    public String getBaseDN() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.BASE_DN_SUFFIX);
    }

    @Override
    public String getBasePeopleName() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.BASE_PEOPLE_NAME_SUFFIX);
    }

    @Override
    public String getBaseRolesName() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.BASE_ROLES_NAME_SUFFIX);
    }

    @Override
    public String getDN(String principal) {
        return getPrincipalName(principal) + "," + getBasePeopleName();
    }

    @Override
    public Properties getLdapEnv() {
        Properties ldapEnv = new Properties();
        String prefix = DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.ENVIRONMENT_SUFFIX;
        for (String propertyName : getMapping().stringPropertyNames()) {
            if (propertyName.startsWith(prefix)) {
                String key = propertyName.substring(prefix.length() + 1);
                String value = getMapping().getProperty(propertyName);
                ldapEnv.setProperty(key, value);
            }
        }
        return ldapEnv;
    }

    @Override
    public LdapManager getLdapManager(String uid, char[] password) {
        String userDN = getDN(uid);
        Properties envProps = getLdapEnv();
        envProps.put(Context.SECURITY_PRINCIPAL, userDN);
        envProps.put(Context.SECURITY_CREDENTIALS, new String(password));
        Properties ldapProps = new Properties();
        ldapProps.setProperty("realm", DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId());
        ldapProps.setProperty(LdapManagerFactory.BASE_ROLES_NAME, getBaseRolesName());
        ldapProps.setProperty(LdapManagerFactory.ROLE_DN_PREFIX, getRoleDNPrefix());
        return LdapManagerFactory.getInstance(envProps, ldapProps);
    }

    public Properties getMapping() {
        if (mapping == null) {
            mapping = new Properties();
        }
        return mapping;
    }

    @Override
    public String getPrincipalDNPrefix() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.PRINCIPAL_DN_PREFIX_SUFFIX);
    }

    @Override
    public String getPrincipalName(String principal) {
        return getPrincipalDNPrefix() + "=" + principal;
    }

    @Override
    public String getRoleDNPrefix() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.ROLE_DN_PREFIX_SUFFIX);
    }

    @Override
    public String getSessionAttributes() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.SESSION_ATTRIBUTES_SUFFIX);
    }

    @Override
    public String getTolvenPersonRole() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.TOLVENPERSON_ROLE);
    }

    @Override
    public String getUserDnTemplate() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.USER_DN_TEMPLATE_SUFFIX);
    }

    @Override
    public String getUserSubstitutionToken() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.USER_SUBSTITUTION_TOKEN_SUFFIX);
    }

    public void setMapping(Properties mapping) {
        this.mapping = mapping;
    }

}
