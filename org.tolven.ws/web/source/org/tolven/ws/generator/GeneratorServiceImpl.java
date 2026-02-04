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
package org.tolven.ws.generator;

import javax.ejb.EJB;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.tolven.gen.GeneratorLocal;

/**
* Generate patients for a CHR account.
* @author Joseph Isaac
*/
@WebServlet(urlPatterns = { "/generator" })
@HandlerChain(file = "../ws-handler-chain.xml")
@WebService(name = "generator", serviceName = "GeneratorService", targetNamespace = "http://tolven.org/generator")
public class GeneratorServiceImpl {

    @EJB
    private GeneratorLocal generatorBean;

    public GeneratorServiceImpl() {
    }

    @WebMethod(action = "generateCCRXML")
    @RequestWrapper(localName = "generateCCRXML", className = "org.tolven.ws.generator.jaxws.GenerateCCRXMLRequest")
    @ResponseWrapper(localName = "generateCCRXMLResponse", className = "org.tolven.ws.generator.jaxws.GenerateCCRXMLResponse")
    public String generateCCRXML(@WebParam(name = "startYear") int startYear) {
        byte[] bytes = generatorBean.generateCCRXML(startYear);
        return new String(bytes);
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
