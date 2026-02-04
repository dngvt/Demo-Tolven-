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
package org.tolven.ws.common;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJB;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.tolven.core.ActivationLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.TolvenUser;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;

/**
 * This class is responsible for setting up a TolvenRequest for webservices
 * 
 * @author Joseph Isaac
 *
 */
public class TolvenRequestHandler implements SOAPHandler<SOAPMessageContext> {

    public static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String WSSE11_NS = "http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd";

    @EJB
    private ActivationLocal activationBean;

    @Override
    public void close(MessageContext smc) {
        TolvenRequest.getInstance().clear();
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
            TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
            String principal = (String) sessionWrapper.getPrincipal();
            TolvenUser user = activationBean.findUser(principal);
            TolvenRequest tolvenRequest = TolvenRequest.getInstance();
            tolvenRequest.initializeTolvenUser(user);
            //smc.getMessage().writeTo(System.out);
            //System.out.println();

        }
        return true;
    }

}
