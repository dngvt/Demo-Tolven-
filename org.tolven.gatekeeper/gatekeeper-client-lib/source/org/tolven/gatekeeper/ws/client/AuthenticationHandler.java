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
package org.tolven.gatekeeper.ws.client;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tolven.exception.TolvenAuthenticationException;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;

/**
 * This class is the last handler to check authentication, since previous handlers may not be interested in this particular message
 * 
 * @author Joseph Isaac
 *
 */
public class AuthenticationHandler implements SOAPHandler<SOAPMessageContext> {

    public static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String WSSE11_NS = "http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd";

    @Override
    public void close(MessageContext smc) {
    }

    @Override
    public Set<QName> getHeaders() {
        HashSet<QName> headers = new HashSet<QName>();
        headers.add(new QName(WSSE_NS, "Security"));
        headers.add(new QName(WSSE11_NS, "Security"));
        return headers;
    }

    @Override
    public boolean handleFault(SOAPMessageContext smc) {
        return false;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outboundProperty.booleanValue()) {
            try {
                //smc.getMessage().writeTo(System.out);
                //System.out.println();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            Subject subject = SecurityUtils.getSubject();
            TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
            if (!subject.isAuthenticated()) {
                throw new TolvenAuthenticationException("Authentication failed", (String) subject.getPrincipal(), sessionWrapper.getRealm(), sessionWrapper.getHost());
            }
            //smc.getMessage().writeTo(System.out);
            //System.out.println();

        }
        return true;
    }

}
