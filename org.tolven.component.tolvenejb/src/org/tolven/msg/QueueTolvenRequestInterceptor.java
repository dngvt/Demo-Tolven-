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
package org.tolven.msg;

import java.security.PrivateKey;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.tolven.core.AccountDAOLocal;
import org.tolven.core.ActivationLocal;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.TolvenUser;
import org.tolven.doc.bean.TolvenMessage;
import org.tolven.exception.TolvenAuthenticationException;
import org.tolven.exception.TolvenAuthorizationException;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;

/**
 * This class is responsible for setting up a TolvenRequest for MDBs
 * 
 * @author Joseph Isaac
 *
 */
@Interceptor
public class QueueTolvenRequestInterceptor {

    //TODO Constants need to be moved to a more common location (these appear in web tier also)
    public static final String ACCOUNT_ID = "accountId";
    public static final String ACCOUNTUSER_ID = "accountUserId";
    public static final String USER_CONTEXT = "userContext";

    @EJB
    private AccountDAOLocal accountBean;

    @EJB
    private ActivationLocal activationBean;

    private Logger logger = Logger.getLogger(QueueTolvenRequestInterceptor.class);

    @EJB
    private TolvenPropertiesLocal propertyBean;

    @EJB
    private TolvenMessageSchedulerLocal tmSchedulerBean;

    @AroundInvoke
    public Object initializeRequest(final InvocationContext invCtx) {
        ObjectMessage message = (ObjectMessage) invCtx.getParameters()[0];
        String jmsMessageId = null;
        try {
            jmsMessageId = message.getJMSMessageID();
        } catch (JMSException ex) {
            throw new RuntimeException("Could not get JMSMessageID", ex);
        }
        String messageLogId = "MsgID: " + jmsMessageId + " thread: " + Thread.currentThread().getId();
        Long tmId = null;
        try {
            logger.debug("[Start] " + messageLogId);
            TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
            String principal = (String) sessionWrapper.getPrincipal();
            String realm = (String) sessionWrapper.getRealm();
            if (principal == null) {
                throw new TolvenAuthenticationException("Principal cannot be null", principal, sessionWrapper.getRealm());
            }
            TolvenRequest tolvenRequest = TolvenRequest.getInstance();
            TolvenMessage tm = tmSchedulerBean.unwrapTolvenMessage(message);
            if (tm == null) {
                logger.debug("Processing non-TolvenMessage: " + jmsMessageId);
                TolvenUser user = activationBean.findUser(principal);
                /*
                 * Initialize tolvenRequest with TolvenUser
                 */
                tolvenRequest.initializeTolvenUser(user);
            } else {
                logger.debug("Processing TolvenMessage: " + tm.getId());
                Long accountId = tm.getAccountId();
                if (accountId == 0) {
                    throw new RuntimeException("JMSMessageID: " + jmsMessageId + " TolvenMessage: " + tm.getId() + " has no account Id");
                }
                tmId = tm.getId();
                logger.debug("TolvenMessage: " + tmId + " is for account: " + accountId);
                Account account = accountBean.findAccount(accountId);
                if (account == null) {
                    throw new RuntimeException("Could not find account: " + accountId + " for TolvenMessage: " + tmId);
                }
                String keyAlgorithm = propertyBean.getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
                PrivateKey privateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);
                if (privateKey == null) {
                    throw new TolvenAuthorizationException("User requires a UserPrivateKey to log into account: " + account.getId(), principal, sessionWrapper.getRealm());
                }
                AccountUser accountUser = accountBean.findAccountUser(principal,account.getId());
                if(accountUser == null)
        			throw new RuntimeException("No account user found for UserName: "+principal+" Account: "+account);
                TolvenUser user = accountUser.getUser();
                if (!user.getLdapUID().equals(principal)) {
                    throw new TolvenAuthorizationException("System user " + user.getLdapUID() + " accessing account:" + account.getId() + " does not match current principal", principal, sessionWrapper.getRealm());
                }
                /*
                 * Initialize tolvenRequest with TolvenUser
                 */
                tolvenRequest.initializeTolvenUser(user);
                // Save ACCOUNTUSER in session for subsequent request so the security filters can intercept appropriately
                sessionWrapper.setAttribute(ACCOUNTUSER_ID, String.valueOf(accountUser.getId()));
                sessionWrapper.setAttribute(ACCOUNT_ID, String.valueOf(account.getId()));
                // Record the time when the user logged into this particular account
                //accountUser.setLastLoginTime(tolvenRequest.getNow());
                sessionWrapper.setAttribute(USER_CONTEXT, "account");
                /*
                 * Initialize tolvenRequest with AccountUser
                 */
                tolvenRequest.initializeAccountUser(accountUser);
                //
                logger.debug(principal + " in realm: " + realm + " ENTERED_ACCOUNT: " + account.getId());
            }
            return invCtx.proceed();
        } catch (Exception ex) {
            if (tmId == null) {
                logger.error("[Failed End] " + messageLogId);
            } else {
                logger.error("[Failed End] " + messageLogId + " TolvenMessage: " + tmId);
            }
            throw new RuntimeException(ex);
        } finally {
            if (tmId == null) {
                logger.debug("[End] " + messageLogId);
            } else {
                logger.debug("[End] " + messageLogId + " TolvenMessage: " + tmId);
            }
            TolvenRequest.getInstance().clear();
        }
    }

}
