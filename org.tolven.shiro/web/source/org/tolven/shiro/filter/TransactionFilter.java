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
import java.io.Writer;

import javax.naming.InitialContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.tolven.naming.TolvenContext;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.shiro.exception.ShiroSessionNotFoundException;

public class TransactionFilter implements Filter {

    private static Logger logger = Logger.getLogger(TransactionFilter.class);
    
    private static TolvenContext tolvenContext;

    public TransactionFilter() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        double beginNanoTime = System.nanoTime();
        UserTransaction ut = null;
        try {
            long start = 0;
            if (logger.isDebugEnabled()) {
                start = System.currentTimeMillis();
            }
            InitialContext ctx = new InitialContext();
            while (true) {
                ut = (UserTransaction) ctx.lookup("UserTransaction");
                if (ut.getStatus() == Status.STATUS_NO_TRANSACTION) {
                    ut.begin();
                    break;
                } else if (ut.getStatus() == Status.STATUS_ACTIVE) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("***** Transaction already active *******");
                    }
                    break;
                } else {
                    logger.error("***** Transaction not in a good state [" + ut.getStatus() + "] trying again *******");
                    ut.setRollbackOnly();
                    ut.rollback();
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("TOLVEN_PERF: " + (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();
            }
            try {
                chain.doFilter(servletRequest, servletResponse);
            } catch (Exception ex) {
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof ShiroSessionNotFoundException) {
                    logger.warn(cause.getMessage());
                    if (ut != null && (ut.getStatus() == Status.STATUS_ACTIVE)) {
                        //roll back any changes that might have occurred downstream of the filter and begin transaction again
                        logger.warn("rolling back transaction and beginning transaction again with SSO cookie removed");
                        ut.rollback();
                        ut.begin();
                    }
                    HttpServletRequest origRequest = (HttpServletRequest) servletRequest;
                    final String cookieTemplateName = getCookieTemplate().getName();
                    HttpServletRequest req = new HttpServletRequestWrapper(origRequest) {
                        public Cookie[] getCookies() {
                            Cookie[] origCookies = super.getCookies();
                            int origNumCookies = origCookies.length;
                            Cookie[] cookies = new Cookie[origNumCookies - 1];
                            int index = 0;
                            for (int i = 0; i < origNumCookies; i++) {
                                Cookie origCookie = origCookies[i];
                                if (!cookieTemplateName.equals(origCookie.getName())) {
                                    cookies[index] = origCookie;
                                    index++;
                                }
                            }
                            return cookies;
                        }
                    };
                    HttpServletResponse resp = (HttpServletResponse) servletResponse;
                    chain.doFilter(req, resp);
                } else {
                    throw ex;
                }
            }
            if (logger.isDebugEnabled()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("TOLVEN_PERF: downstream: " + (System.currentTimeMillis() - start));
                }
            }
            if (ut != null) {
                if (ut.getStatus() == Status.STATUS_ACTIVE) {
                    if (logger.isDebugEnabled()) {
                        start = System.currentTimeMillis();
                    }
                    String readOnlyTransaction = (String) servletRequest.getAttribute("org.tolven.request.attribute.readonlytransaction");
                    if ("true".equals(readOnlyTransaction)) {
                        ut.rollback();
                    } else {
                        ut.commit();
                    }
                } else
                    ut.rollback();
                // We need to put the web page commit *after* the database commit
                // otherwise it is possible that the web page can query for uncommitted work in the next interaction.
                // The situation actually occurs in underpowered configurations.
                Writer writer = (Writer) servletRequest.getAttribute("activeWriter");
                if (writer != null)
                    writer.close();
                ut = null;
            }
        } catch (Exception e) {
            try {
                if (ut != null && (ut.getStatus() == Status.STATUS_ACTIVE)) {
                    ut.rollback();
                    ut = null;
                }
            } catch (Exception e2) {
                logger.error("*************** !!!!!TRANSACTION ROLLBACK EXCEPTION!!!! *******************");
                throw new ServletException(e2);
            }
            Throwable cause = e.getCause();
            String message = "";
            if (cause != null) {
                message = cause.getMessage();
                logger.error(message);
            }
            throw new ServletException("Exception caught in Transaction: " + message, e);
        } finally {
            try {
                try {
                    if (ut != null) {// && (ut.getStatus()==Status.STATUS_ACTIVE)
                        ut.rollback();
                        ut = null;
                    }
                    HttpSession session = ((HttpServletRequest) servletRequest).getSession(false);
                    if (session != null) {
                        Double elapsed = (Double) session.getAttribute("elapsedMilli");
                        if (elapsed == null)
                            elapsed = 0.0;
                        elapsed += (System.nanoTime() - beginNanoTime) / 1000000;
                        session.setAttribute("elapsedMilli", elapsed);
                    }
                } catch (Exception e) {
                    logger.error("*************** !!!!!TRANSACTION ROLLBACK EXCEPTION!!!! *******************");
                    throw new ServletException(e);
                }
            } finally {
                TolvenSessionWrapperFactory.getInstance().clear();
            }
        }
    }

    private SimpleCookie getCookieTemplate() {
        TolvenContext tolvenContext = getTolvenContext();
        SimpleCookie cookie = new SimpleCookie(tolvenContext.getSsoCookieName());
        cookie.setDomain(tolvenContext.getSsoCookieDomain());
        cookie.setPath(tolvenContext.getSsoCookiePath());
        cookie.setSecure(Boolean.parseBoolean(tolvenContext.getSsoCookieSecure()));
        cookie.setMaxAge(Integer.parseInt(tolvenContext.getSsoCookieMaxAge()));
        return cookie;
    }

    private TolvenContext getTolvenContext() {
        if(tolvenContext == null) {
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
    public void init(FilterConfig chain) throws ServletException {
    }

}
