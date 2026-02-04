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

public class DefaultQueueContext extends DefaultTolvenJndiContext implements QueueContext {

    private Properties mapping;

    public DefaultQueueContext() {
    }

    public Properties getMapping() {
        if (mapping == null) {
            mapping = new Properties();
        }
        return mapping;
    }

    @Override
    public String getPassword() {
        return getMapping().getProperty("password." + getContextId());
    }

    @Override
    public String getRealm() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.REALM_SUFFIX);
    }

    @Override
    public String getUser() {
        return getMapping().getProperty(DefaultJndiManager.TOLVENCONTEXT_PREFIX + getContextId() + DefaultJndiManager.USER_SUFFIX);
    }

    public void setMapping(Properties mapping) {
        this.mapping = mapping;
    }

}
