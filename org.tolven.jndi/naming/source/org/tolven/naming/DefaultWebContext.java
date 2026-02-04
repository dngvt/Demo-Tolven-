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

public class DefaultWebContext extends DefaultTolvenJndiContext implements WebContext {

    private Properties mapping;

    public DefaultWebContext() {
    }

    @Override
    public String getContextPath() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId()+ DefaultJndiManager.CONTEXTPATH_SUFFIX);
    }

    @Override
    public String getHtmlLoginPath() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId()+ DefaultJndiManager.LOGINPATH_SUFFIX);
    }

    @Override
    public String getHtmlLoginUrl() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId()+ DefaultJndiManager.LOGINURL_SUFFIX);
    }

    public Properties getMapping() {
        if (mapping == null) {
            mapping = new Properties();
        }
        return mapping;
    }

    @Override
    public String getRsInterface() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId()+ DefaultJndiManager.INTERFACE_SUFFIX);
    }

    @Override
    public String getRsLoginPath() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId()+ DefaultJndiManager.LOGINPATH_SUFFIX);
    }

    @Override
    public String getRsLoginUrl() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId()+ DefaultJndiManager.LOGINURL_SUFFIX);
    }

    @Override
    public String getSslPort() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId()+ DefaultJndiManager.SSLPORT_SUFFIX);
    }

    @Override
    public String getWsLoginPath() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId()+ DefaultJndiManager.LOGINPATH_SUFFIX);
    }

    @Override
    public String getWsLoginUrl() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId()+ DefaultJndiManager.LOGINURL_SUFFIX);
    }

    public void setMapping(Properties mapping) {
        this.mapping = mapping;
    }

}
