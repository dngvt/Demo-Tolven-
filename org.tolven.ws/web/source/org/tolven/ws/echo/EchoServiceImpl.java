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
package org.tolven.ws.echo;

import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.tolven.session.TolvenSessionWrapperFactory;

/**
 * This class is a Java Service Endpoint which provides an EchoService implementation for testing
 * 
 * @author Joseph Isaac
 *
 */
@WebServlet(urlPatterns = { "/echo" })
@HandlerChain(file = "../ws-handler-chain.xml")
@WebService(name = "echo", serviceName = "EchoService", targetNamespace = "http://tolven.org/echo")
public class EchoServiceImpl {

    public EchoServiceImpl() {
    }

    /**
     * 
     * @param string
     * @return
     *     returns java.lang.String
     */
    @WebMethod(action = "echo")
    @RequestWrapper(localName = "echo", className = "org.tolven.ws.echo.jaxws.EchoRequest")
    @ResponseWrapper(localName = "echoResponse", className = "org.tolven.ws.echo.jaxws.EchoResponse")
    public String echo(@WebParam(name = "string") String string) {
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        return principal + ": " + string;
    }

    /**
     * The actual logout is handled by the JAXWS handler which performed the login, since it may need
     * to know the principal before they are logged out
     */
    @WebMethod(action = "logout")
    public void logout() {
        /*
         * The actual logout is handled by the JAXWS handler which performed the login since it may need
         * to know the principal before they are logged out
         */
    }

}
