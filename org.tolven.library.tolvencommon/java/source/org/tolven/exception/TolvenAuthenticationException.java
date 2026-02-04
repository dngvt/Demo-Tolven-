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
package org.tolven.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class TolvenAuthenticationException extends TolvenSecurityException {

    public static final String PASSWORD_EXPIRED = "passwordExpired";
    public static final String PASSWORD_EXPIRING = "passwordExpiring";

    private String host;
    private String realm;
    private String userId;

    public TolvenAuthenticationException() {
    }

    public TolvenAuthenticationException(String message) {
        super(message);
    }

    public TolvenAuthenticationException(String message, String userId, String realm) {
        super(message, userId, realm);
    }

    public TolvenAuthenticationException(String message, String userId, String realm, String host) {
        super(message, userId, realm, host);
    }

    public TolvenAuthenticationException(String message, String userId, String realm, String host, Throwable cause) {
        super(message, userId, realm, host, cause);
    }

    public TolvenAuthenticationException(String message, String userId, String realm, Throwable cause) {
        super(message, userId, realm, cause);
    }

    public TolvenAuthenticationException(String userId, String realm, Throwable cause) {
        super(userId, realm, cause);
    }

    public TolvenAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TolvenAuthenticationException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getExceptionHeader() {
        return "Authentication Exception:";
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
