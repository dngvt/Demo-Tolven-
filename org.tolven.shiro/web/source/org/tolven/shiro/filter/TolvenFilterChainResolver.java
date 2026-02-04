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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.apache.shiro.web.util.WebUtils;
import org.tolven.shiro.api.authz.TolvenAuthorizationLocal;
import org.tolven.shiro.entity.authz.TolvenAuthorization;

public class TolvenFilterChainResolver implements FilterChainResolver {

    private static Logger logger = Logger.getLogger(TolvenFilterChainResolver.class);

    private Map<String, Filter> allChainFilters;
    
    @EJB
    private TolvenAuthorizationLocal authBean;
    
    private FilterConfig filterConfig;

    public TolvenFilterChainResolver() {
    }

    public TolvenFilterChainResolver(FilterConfig filterConfig) {
        setFilterConfig(filterConfig);
    }

    private Map<String, Filter> getAllChainFilters(ServletContext servletContext) {
        if(allChainFilters == null) {
            allChainFilters = TolvenShiroFilter.getChainFilters(servletContext);
        }
        return allChainFilters;
    }

    private TolvenAuthorizationLocal getAuthBean() {
        if (authBean == null) {
            String jndiName = null;
            try {
                InitialContext ctx = new InitialContext();
                jndiName = "java:app/shiro-ejb/TolvenAuthorizationBean!org.tolven.shiro.api.authz.TolvenAuthorizationLocal";
                if (logger.isDebugEnabled()) {
                    logger.debug("JNDI lookup: " + jndiName);
                }
                authBean = (TolvenAuthorizationLocal) ctx.lookup(jndiName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Found: " + jndiName);
                }
            } catch (NamingException e) {
                throw new RuntimeException("Failed to lookup " + jndiName, e);
            }
        }
        return authBean;
    }

    /*
     * This method is required to ensure filter chains are not cached, but created on the fly from the database
     * (non-Javadoc)
     * @see org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver#getChain(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public FilterChain getChain(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain originalChain) {
        String requestURI = getPathWithinApplication(servletRequest);
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String urlMethod = request.getMethod();
        if (logger.isDebugEnabled()) {
            logger.debug("requestURI=" + urlMethod + " " + requestURI);
        }
        TolvenAuthorization authz = getAuthBean().getAuthorization(urlMethod, request.getContextPath(), requestURI);
        if (authz == null) {
            throw new RuntimeException("authorization url cannot be found for request: " + urlMethod + " " + requestURI);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Matched TolvenAuthorization=" + authz);
        }
        String authorizationURI = authz.getUrl();
        if (!StringUtils.hasText(authorizationURI)) {
            throw new RuntimeException("authorization url cannot be null or empty for request: " + urlMethod + " " + requestURI);
        }
        String filterString = authz.getFilters();
        if (filterString == null || filterString.trim().length() == 0) {
            return originalChain;
        } else {
            DefaultFilterChainManager temporaryManager = new DefaultFilterChainManager(getFilterConfig());
            boolean init = getFilterConfig() != null; //only call filter.init if there is a FilterConfig available
            if (logger.isDebugEnabled()) {
                logger.debug("Adding filters to temporary manager");
            }
            Map<String, Filter> filters = null;
            try {
                filters = getFilters(filterString, servletRequest.getServletContext());
            } catch (Exception ex) {
                throw new RuntimeException("Failed to get chain filters for: " + request.getContextPath(), ex);
            }
            for (Entry<String, Filter> entry : filters.entrySet()) {
                temporaryManager.addFilter(entry.getKey(), entry.getValue(), init);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Added filters to temporary manager");
            }
            try {
                temporaryManager.createChain(authorizationURI, filterString);
            } catch (Exception ex) {
                throw new RuntimeException("Could not create chain: " + authorizationURI + " for filters: " + filterString + " in context: " + getFilterConfig().getServletContext().getContextPath(), ex);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Created filter chain: " + authorizationURI + " with " + filterString);
            }
            NamedFilterList chain = temporaryManager.getChain(authorizationURI);
            return chain.proxy(originalChain);
        }
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }
    
    private Map<String, Filter> getFilters(String filterString, ServletContext servletContext) {
        List<String> filterStrings = Arrays.asList(filterString.split(","));
        List<String> filterNames = new ArrayList<String>();
        for (String string : filterStrings) {
            int index = string.indexOf("[");
            String filterName = null;
            if (index == -1) {
                filterName = string;
            } else {
                filterName = string.substring(0, index);
            }
            filterNames.add(filterName);
        }
        Map<String, Filter> selectedChainFilters = new HashMap<String, Filter>();
        for (String filterName : filterNames) {
            Filter filter = getAllChainFilters(servletContext).get(filterName);
            if(filter == null) {
                throw new RuntimeException("Found no filter named: " + filterName);
            }
            selectedChainFilters.put(filterName, getAllChainFilters(servletContext).get(filterName));
        }
        return selectedChainFilters;
    }

    protected String getPathWithinApplication(ServletRequest request) {
        return WebUtils.getPathWithinApplication(WebUtils.toHttp(request));
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

}
