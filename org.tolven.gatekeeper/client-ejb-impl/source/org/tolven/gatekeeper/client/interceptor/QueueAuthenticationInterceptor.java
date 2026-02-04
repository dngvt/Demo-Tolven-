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
package org.tolven.gatekeeper.client.interceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tolven.exception.TolvenAuthenticationException;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;

@Interceptor
public class QueueAuthenticationInterceptor {

    @AroundInvoke
    public Object authenticate(final InvocationContext invCtx) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
            throw new TolvenAuthenticationException("Authentication failed", (String) subject.getPrincipal(), sessionWrapper.getRealm(), sessionWrapper.getHost());
        }
        return invCtx.proceed();
    }

}
