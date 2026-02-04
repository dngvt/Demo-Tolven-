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
package org.tolven.shiro.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.Nameable;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.tolven.naming.TolvenContext;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.shiro.web.mgt.TolvenWebSecurityManager;

public class TolvenShiroFilter extends AbstractShiroFilter {

    private static String[] commonInShiroFilters = {
            "anon,org.apache.shiro.web.filter.authc.AnonymousFilter",
            "authc,org.apache.shiro.web.filter.authc.FormAuthenticationFilter",
            "authcBasic,org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter",
            "perms,org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter",
            "port,org.apache.shiro.web.filter.authz.PortFilter",
            "rest,org.apache.shiro.web.filter.authz.HttpMethodPermissionFilter",
            "roles,org.apache.shiro.web.filter.authz.RolesAuthorizationFilter",
            "ssl,org.apache.shiro.web.filter.authz.SslFilter",
            "user,org.apache.shiro.web.filter.authc.UserFilter",

            "rspreauthc,org.tolven.shiro.filter.RSPreAuthenticationFilter",
            "preauthc,org.tolven.shiro.filter.PreAuthFormAuthenticationFilter",
            "tssl,org.tolven.shiro.filter.TolvenSslFilter",
            "troles,org.tolven.shiro.filter.TolvenRolesAuthorizationFilter" };

    private static Logger logger = Logger.getLogger(TolvenShiroFilter.class);

    public static final String SHIRO_CHAIN_FILTERS = "org.tolven.shiro.chainfilters";

    public static final String SHIRO_REALMS = "org.tolven.shiro.realms";

    public static synchronized void addChainFilter(String name, String filterClass, ServletContext servletContext) {
        try {
            Map<String, Filter> chainFilters = (Map<String, Filter>) servletContext.getAttribute(SHIRO_CHAIN_FILTERS);
            if (chainFilters == null) {
                chainFilters = new HashMap<String, Filter>();
                servletContext.setAttribute(SHIRO_CHAIN_FILTERS, chainFilters);
            }
            if (chainFilters.get(name) != null && !filterClass.equals(chainFilters.get(name).getClass().getName())) {
                throw new RuntimeException("Filter chains cannot have the same name: " + name + " for " + chainFilters.get(name) + " and " + filterClass);
            }
            Class<?> clazz = Class.forName(filterClass);
            chainFilters.put(name, (Filter) clazz.newInstance());
        } catch (Exception ex) {
            throw new RuntimeException("Could not create chain filter: " + name + "=" + filterClass, ex);
        }
    }

    public static synchronized void addRealm(String name, String realmClass, ServletContext servletContext) {
        try {
            Map<String, Realm> realms = (Map<String, Realm>) servletContext.getAttribute(SHIRO_REALMS);
            if (realms == null) {
                realms = new HashMap<String, Realm>();
                servletContext.setAttribute(SHIRO_REALMS, realms);
            }
            if (realms.get(name) != null && !realmClass.equals(realms.get(name).getClass().getName())) {
                throw new RuntimeException("Realms cannot have the same name: " + name + " for " + realms.get(name) + " and " + realmClass);
            }
            Class<?> clazz = Class.forName(realmClass);
            Realm realm = (Realm) clazz.newInstance();
            ((Nameable) realm).setName(name);
            realms.put(name, realm);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create realm: " + name + "=" + realmClass, ex);
        }
    }

    public static Map<String, Filter> getChainFilters(ServletContext servletContext) {
        return (Map<String, Filter>) servletContext.getAttribute(SHIRO_CHAIN_FILTERS);
    }

    public static Map<String, Realm> getRealms(ServletContext servletContext) {
        return (Map<String, Realm>) servletContext.getAttribute(SHIRO_REALMS);
    }

    private static TolvenContext tolvenContext;

    public TolvenShiroFilter() {
    }

    @Override
    protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse, final FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        if (logger.isDebugEnabled()) {
            logger.debug("Contact: local=" + servletRequest.getLocalAddr() + " remote=" + servletRequest.getRemoteAddr());
        }
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        TolvenContext tolvenContext = getTolvenContext();
        SimpleCookie cookie = new SimpleCookie(tolvenContext.getSsoCookieName());
        cookie.setDomain(tolvenContext.getSsoCookieDomain());
        cookie.setPath(tolvenContext.getSsoCookiePath());
        cookie.setSecure(Boolean.parseBoolean(tolvenContext.getSsoCookieSecure()));
        cookie.setMaxAge(Integer.parseInt(tolvenContext.getSsoCookieMaxAge()));
        String extendedSessionId = cookie.readValue(httpRequest, httpResponse);
        if (extendedSessionId != null) {
            TolvenSessionWrapperFactory.getInstance().bind(extendedSessionId);
        }
        super.doFilterInternal(servletRequest, servletResponse, chain);
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

    @Override
    public void init() throws Exception {
        ServletContext servletContext = getFilterConfig().getServletContext();
        initSecurityManager(servletContext);
        initChainResolver();
        initChainFilters(servletContext);
    }

    protected void initChainFilters(ServletContext servletContext) {
        for (String builtInFilter : commonInShiroFilters) {
            String[] nameAndClass = builtInFilter.split(",");
            String name = nameAndClass[0];
            String clazz = nameAndClass[1];
            addChainFilter(name, clazz, servletContext);
            logger.debug("Added filter: " + clazz + "=" + clazz);
        }
    }

    protected void initChainResolver() {
        TolvenFilterChainResolverFactory filterChainResolverFactory = new TolvenFilterChainResolverFactory();
        filterChainResolverFactory.setFilterConfig(getFilterConfig());
        FilterChainResolver resolver = filterChainResolverFactory.getInstance();
        setFilterChainResolver(resolver);
    }

    protected void initSecurityManager(ServletContext servletContext) {
        TolvenWebSecurityManager securityManager = new TolvenWebSecurityManager();
        Map<String, Realm> realmMap = getRealms(servletContext);
        if (realmMap == null) {
            logger.debug("No realms configured for: " + servletContext.getContextPath());
        } else {
            securityManager.setRealms(realmMap.values());
        }
        setSecurityManager(securityManager);
    }

}
