/*
 *  Copyright (C) 2006 Tolven Inc 
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * Contact: info@tolvenhealth.com
 */
package org.tolven.api.security;

import java.io.IOException;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.tolven.core.AccountDAOLocal;
import org.tolven.core.ActivationLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.TolvenUser;
import org.tolven.exception.TolvenAuthenticationException;
import org.tolven.exception.TolvenAuthorizationException;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.web.security.GeneralSecurityFilter;

/**
 * Filter checks that the user is in the vestibule and can proceed, or redirects out of the Vestibule when necessary
 * 
 * @author Joseph Isaac
 */
public class SelectAccountVestibuleFilter extends AuthorizationFilter {

    @EJB
    private ActivationLocal activationBean;

    @EJB
    private AccountDAOLocal accountBean;

    private Logger logger = Logger.getLogger(SelectAccountVestibuleFilter.class);

    public void destroy() {
    }

    private ActivationLocal getActivationBean() {
        if (activationBean == null) {
            String jndiName = null;
            try {
                InitialContext ctx = new InitialContext();
                jndiName = "java:app/tolvenEJB/ActivationBean!org.tolven.core.ActivationLocal";
                activationBean = (ActivationLocal) ctx.lookup(jndiName);
            } catch (NamingException ex) {
                throw new RuntimeException("Failed to lookup " + jndiName, ex);
            }
        }
        return activationBean;
    }

    protected AccountDAOLocal getAccountBean() {
        if (accountBean == null) {
            String jndiName = null;
            try {
                InitialContext ctx = new InitialContext();
                jndiName = "java:app/tolvenEJB/AccountDAOBean!org.tolven.core.AccountDAOLocal";
                accountBean = (AccountDAOLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName, ex);
            }
        }
        return accountBean;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object chain) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
        String principal = (String) sessionWrapper.getPrincipal();
        String realm = (String) sessionWrapper.getRealm();
        if (principal == null) {
            throw new TolvenAuthenticationException("Principal cannot be null in the Vestibule", principal, sessionWrapper.getRealm());
        }
        String userContext = (String) sessionWrapper.getAttribute(GeneralSecurityFilter.USER_CONTEXT);
        if (userContext == null) {
            throw new RuntimeException("userContext not set (normally set during login)");
        } else if ("account".equals(userContext)) {
            //An account must be exited before a vestibule URL can be processed
            //TODO This should be configurable, based on userContext
            throw new TolvenAuthorizationException("An account must be exited before a vestibule URL can be processed", principal, sessionWrapper.getRealm());
        } else if ("vestibule".equals(userContext)) {
            String accountUserIdString = (String) sessionWrapper.getAttribute(GeneralSecurityFilter.ACCOUNT_ID);
            if (accountUserIdString == null) {
                TolvenUser user = getActivationBean().findUser(principal);
                if (user == null) {
                    throw new TolvenAuthorizationException("Could not find TolvenUser", principal, sessionWrapper.getRealm());
                }
                AccountUser defaultAccountUser = getActivationBean().findDefaultAccountUser(user);
                boolean hasSelectedAccount = false;
                if (defaultAccountUser != null) {
                    String switchAccount = request.getParameter(GeneralSecurityFilter.SWITCH_ACCOUNT);
                    if ("GET".equals(request.getMethod()) && !"true".equals(switchAccount)) {
                        sessionWrapper.setAttribute(GeneralSecurityFilter.PROPOSED_ACCOUNTUSER_ID, Long.toString(defaultAccountUser.getId()));
                        hasSelectedAccount = true;
                        logger.info("User: " + principal + " realm: " + realm + " AUTO_SELECTING_DEFAULT_ACCOUNT: " + defaultAccountUser.getAccount().getId());
                    }
                }
                if (!hasSelectedAccount) {
                    String accountIdString = request.getParameter(GeneralSecurityFilter.ACCOUNT_ID);
                    if (accountIdString == null) {
                        throw new TolvenAuthorizationException("Account parameter: " + GeneralSecurityFilter.ACCOUNT_ID + " not found in request by: " + principal, principal, sessionWrapper.getRealm());
                    }
                    AccountUser accountUser = getAccountBean().findAccountUser(principal, Long.parseLong(accountIdString));
                    if (accountUser == null) {
                        throw new TolvenAuthorizationException("Account " + accountIdString + " not found for: " + principal, principal, sessionWrapper.getRealm());
                    }
                    sessionWrapper.setAttribute(GeneralSecurityFilter.PROPOSED_ACCOUNTUSER_ID, String.valueOf(accountUser.getId()));
                }
            } else {
                sessionWrapper.setAttribute(GeneralSecurityFilter.PROPOSED_ACCOUNTUSER_ID, accountUserIdString);
            }
            //The ExitVestibuleFilter will check the proposed account, if one was made
        } else {
            throw new RuntimeException("Unrecognized userContext: " + userContext);
        }
        return true;
    }

    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        String redirect = (String) request.getAttribute(GeneralSecurityFilter.TOLVEN_REDIRECT);
        if (StringUtils.hasText(redirect)) {
            WebUtils.issueRedirect(request, response, redirect);
        } else {
            WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return false;
    }

}