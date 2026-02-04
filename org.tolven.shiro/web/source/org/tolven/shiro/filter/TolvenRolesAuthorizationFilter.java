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
 * @version $Id: TolvenRolesAuthorizationFilter.java 5935 2012-02-21 03:28:44Z joe.isaac $
 */
package org.tolven.shiro.filter;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;

public class TolvenRolesAuthorizationFilter extends RolesAuthorizationFilter {

    private Logger logger = Logger.getLogger(TolvenRolesAuthorizationFilter.class);

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        boolean result = super.onAccessDenied(request, response);
        HttpServletRequest req = (HttpServletRequest) request;
        Subject subject = getSubject(request, response);
        String host = "";
        Session session = subject.getSession(false);
        if (session != null) {
            host = session.getHost();
        }
        logger.info("User: " + subject.getPrincipal() + " host: " + host + " ACCESS_DENIED: " + req.getRequestURI());
        return result;
    }

}
