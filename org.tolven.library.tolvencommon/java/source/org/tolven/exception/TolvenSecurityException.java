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

public abstract class TolvenSecurityException extends RuntimeException {

    public static final String HOST_HEADER = "Host:";
    public static final String MESSAGE_HEADER = "Message:";
    public static final String REALM_HEADER = "Realm:";
    public static final String SEPARATOR = " ";
    public static final String USER_HEADER = "User:";

    public static TolvenSecurityException getRootGatekeeperException(Exception ex) {
        Throwable root = null;
        Throwable t = ex;
        do {
            if (t instanceof TolvenSecurityException) {
                root = t;
            }
            t = t.getCause();
        } while (t != null);
        return (TolvenSecurityException) root;
    }

    public static TolvenSecurityException getTopGatekeeperException(Exception ex) {
        Throwable t = ex;
        do {
            if (t instanceof TolvenSecurityException) {
                return (TolvenSecurityException) t;
            }
            t = t.getCause();
        } while (t != null);
        return null;
    }

    public TolvenSecurityException() {
    }

    public TolvenSecurityException(String message) {
        super(message);
    }

    public TolvenSecurityException(String message, String userId, String realm) {
        super(message);
        setUserId(userId);
        setRealm(realm);
    }

    public TolvenSecurityException(String message, String userId, String realm, String host) {
        super(message);
        setUserId(userId);
        setRealm(realm);
        setHost(host);
    }

    public TolvenSecurityException(String message, String userId, String realm, String host, Throwable cause) {
        super(message, cause);
        setUserId(userId);
        setRealm(realm);
        setHost(host);
    }

    public TolvenSecurityException(String message, String userId, String realm, Throwable cause) {
        super(message, cause);
        setUserId(userId);
        setRealm(realm);
    }

    public TolvenSecurityException(String userId, String realm, Throwable cause) {
        super(cause);
        setUserId(userId);
        setRealm(realm);
    }

    public TolvenSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public TolvenSecurityException(Throwable cause) {
        super(cause);
    }

    public abstract String getExceptionHeader();

    public String getFormattedMessage() {
        return getExceptionHeader() + SEPARATOR + USER_HEADER + SEPARATOR + getUserId() + SEPARATOR + REALM_HEADER + SEPARATOR + getRealm() + SEPARATOR + HOST_HEADER + SEPARATOR + getHost() + SEPARATOR + MESSAGE_HEADER + SEPARATOR + getMessage();
    }

    public abstract String getHost();

    public abstract String getRealm();

    public abstract String getUserId();

    public abstract void setHost(String host);

    public abstract void setRealm(String realm);

    public abstract void setUserId(String userId);

}
