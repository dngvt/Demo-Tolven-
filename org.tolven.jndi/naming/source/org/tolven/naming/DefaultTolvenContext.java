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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DefaultTolvenContext implements TolvenContext, Serializable {

    private Properties mapping;

    public DefaultTolvenContext() {
    }

    @Override
    public String getContextId() {
        return JndiManager.TOLVENCONTEXT_ID_REF;
    }

    @Override
    public Object getHtmlGatekeeperWebContext() {
        return getWebContext(DefaultJndiManager.GATEKEEPER_WEB_HTML_ID);
    }

    public Properties getMapping() {
        if (mapping == null) {
            mapping = new Properties();
        }
        return mapping;
    }

    @Override
    public Object getQueueContext(String ctxId) {
        if(ctxId == null) {
            throw new RuntimeException("QueueContext id is null");
        }
        DefaultQueueContext ctx = new DefaultQueueContext();
        ctx.setContextId(ctxId);
        ctx.setMapping(getMapping());
        return ctx;
    }

    @Override
    public List<String> getQueueIds() {
        return Arrays.asList(getMapping().getProperty(JndiManager.QUEUE_IDS_REF).split(","));
    }

    @Override
    public Object getRealmContext(String ctxId) {
        if(ctxId == null) {
            throw new RuntimeException("RealmContext id is null");
        }
        DefaultLdapRealmContext ctx = new DefaultLdapRealmContext();
        ctx.setContextId(ctxId);
        ctx.setMapping(getMapping());
        return ctx;
    }

    @Override
    public List<String> getRealmIds() {
        String realmIds = getMapping().getProperty(JndiManager.REALM_IDS_REF);
        if (realmIds == null) {
            return new ArrayList<String>();
        } else {
            return Arrays.asList(getMapping().getProperty(JndiManager.REALM_IDS_REF).split(","));
        }
    }

    @Override
    public Object getRsGatekeeperWebContext() {
        return getWebContext(DefaultJndiManager.GATEKEEPER_WEB_RS_ID);
    }

    @Override
    public String getSsoCookieDomain() {
        return getMapping().getProperty(getContextId() + DefaultJndiManager.SSOCOOKIE_DOMAIN_SUFFIX);
    }

    @Override
    public String getSsoCookieMaxAge() {
        return getMapping().getProperty(getContextId() + DefaultJndiManager.SSOCOOKIE_MAXAGE_SUFFIX);
    }

    @Override
    public String getSsoCookieName() {
        return getMapping().getProperty(getContextId() + DefaultJndiManager.SSOCOOKIE_NAME_SUFFIX);
    }

    @Override
    public String getSsoCookiePath() {
        return getMapping().getProperty(getContextId() + DefaultJndiManager.SSOCOOKIE_PATH_SUFFIX);
    }

    @Override
    public String getSsoCookieSecure() {
        return getMapping().getProperty(getContextId() + DefaultJndiManager.SSOCOOKIE_SECURE_SUFFIX);
    }

    @Override
    public Object getWebContext(String ctxId) {
        if(ctxId == null) {
            throw new RuntimeException("WebContext id is null");
        }
        DefaultWebContext ctx = new DefaultWebContext();
        ctx.setContextId(ctxId);
        ctx.setMapping(getMapping());
        return ctx;
    }

    @Override
    public List<String> getWebIds() {
        return Arrays.asList(getMapping().getProperty(JndiManager.WEB_IDS_REF).split(","));
    }

    public void setMapping(Properties mapping) {
        this.mapping = mapping;
    }

}
