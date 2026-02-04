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
package org.tolven.ws.document;

import java.util.Date;

import javax.ejb.EJB;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.tolven.core.AccountDAOLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.DocumentLocal;
import org.tolven.msg.ProcessLocal;
import org.tolven.msg.TolvenMessageSchedulerLocal;
import org.tolven.session.TolvenSessionWrapperFactory;

/**
* Provide document services.
* 
* @author Joseph Isaac
*/
@WebServlet(urlPatterns = { "/document" })
@HandlerChain(file = "../ws-handler-chain.xml")
@WebService(name = "document", serviceName = "DocumentService", targetNamespace = "http://tolven.org/document")
public class DocumentServiceImpl {
    
    @EJB
    private AccountDAOLocal accountBean;

    @EJB
    private DocumentLocal documentBean;

    @EJB
    private ProcessLocal processLocal;

    @EJB 
    private TolvenMessageSchedulerLocal tmSchedulerBean;
    
    public DocumentServiceImpl() {
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

    @WebMethod(action = "processDocument")
    @RequestWrapper(localName = "processDocument", className = "org.tolven.ws.document.jaxws.ProcessDocumentRequest")
    @ResponseWrapper(localName = "processDocumentResponse", className = "org.tolven.ws.document.jaxws.ProcessDocumentResponse")
    public long processDocument(
            @WebParam(name = "payload") byte[] payload,
            @WebParam(name = "ns") String ns,
            @WebParam(name = "accountId") long accountId) {
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        AccountUser accountUser = accountBean.findAccountUser(principal, accountId);
        if(accountUser == null) {
            throw new RuntimeException(principal + " not authorized for Account: " + accountId);
        }
        return processLocal.processMessage(payload, "text/xml", ns, accountId, accountUser.getUser().getId(), new Date());
    }
    
    @WebMethod(action = "queueMessage")
    @RequestWrapper(localName = "queueMessage", className = "org.tolven.ws.document.jaxws.QueueMessageRequest")
    @ResponseWrapper(localName = "queueMessageResponse", className = "org.tolven.ws.document.jaxws.QueueMessageResponse")
    public String queueMessage(
            @WebParam(name = "payload") byte[] payload,
            @WebParam(name = "ns") String ns,
            @WebParam(name = "accountId") long accountId) {
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        AccountUser accountUser = accountBean.findAccountUser(principal, accountId);
        if(accountUser == null) {
            throw new RuntimeException(principal + " not authorized for Account: " + accountId);
        }
        return tmSchedulerBean.queueWSMessage(payload, ns, accountId, accountUser.getUser().getId());
    }

    @WebMethod(action = "test")
    @RequestWrapper(localName = "test", className = "org.tolven.ws.document.jaxws.TestRequest")
    @ResponseWrapper(localName = "testResponse", className = "org.tolven.ws.document.jaxws.TestResponse")
    public String test() {
        return tmSchedulerBean.testWS();
    }
    
}
