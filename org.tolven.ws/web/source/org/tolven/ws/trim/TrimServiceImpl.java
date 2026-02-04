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
package org.tolven.ws.trim;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.RequestWrapper;

import org.tolven.app.TrimLocal;
import org.tolven.core.AccountDAOLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.TolvenUser;
import org.tolven.doc.DocumentLocal;
import org.tolven.doc.bean.TolvenMessage;
import org.tolven.doc.bean.TolvenMessageWithAttachments;
import org.tolven.logging.TolvenLogger;
import org.tolven.msg.TolvenMessageSchedulerLocal;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.trim.CE;
import org.tolven.trim.Trim;
import org.tolven.trim.ex.TrimEx;
import org.tolven.trim.ex.TrimFactory;

/**
* 
* @author Joseph Isaac
*/
@WebServlet(urlPatterns = { "/trim" })
@HandlerChain(file = "../ws-handler-chain.xml")
@WebService(name = "trim", serviceName = "TrimService", targetNamespace = "http://tolven.org/trim")
public class TrimServiceImpl {

    private static JAXBContext jc;
    private static final String TRIM_NS = "urn:tolven-org:trim:4.0";

    private static final String TRIM_Package = "org.tolven.trim";

    static JAXBContext setupJAXBContext() {
        if (jc == null) {
            try {
                jc = JAXBContext.newInstance(TRIM_Package);
            } catch (Exception ex) {
                throw new RuntimeException("Could not create JAXBContext", ex);
            }
        }
        return jc;
    }

    @EJB
    private AccountDAOLocal accountBean;

    @EJB
    private DocumentLocal documentBean;

    @EJB
    private TrimLocal trimBean;

    @EJB 
    private TolvenMessageSchedulerLocal tmSchedulerBean;
    
    public TrimServiceImpl() {
    }

    /**
     * Marshal a Trim object graph to XML and add it to the message. 
     * @param trim
     * @param tm
     */

    public void addTrimAsPayload(Trim trim, TolvenMessage tm) {
        JAXBContext jc = setupJAXBContext();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(trim, output);
        } catch (Exception ex) {
            throw new RuntimeException("Could not marshal trim", ex);
        }
        tm.setPayload(output.toByteArray());
    }

    /**
     * Create a TolvenMessage payload wrapper. Notice that the accountId and user id must be supplied in the wrapper.
     * Tolven does not accept anonymous messages.
     * @param ns The namespace that defines the payload 
     * @return
     */

    public TolvenMessageWithAttachments createTolvenMessage(String ns, long accountId, long userId) {
        TolvenMessageWithAttachments tm = new TolvenMessageWithAttachments();
        tm.setAccountId(accountId);
        tm.setAuthorId(userId);
        tm.setXmlNS(ns);
        return tm;
    }

    /**
     * Given a message, display the XML playload.
     * @param tm
     */
    public void displayInstantiatedTrim(TolvenMessageWithAttachments tm) {
        String x = new String(tm.getPayload());
        TolvenLogger.info("************************** Instantiated Trim ***************************", TrimServiceImpl.class);
        TolvenLogger.info(x, TrimServiceImpl.class);
        TolvenLogger.info("************************************************************************", TrimServiceImpl.class);
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

    /**
     * Put a message onto the processing queue
     * @param tm
     * @throws Exception
     */
    public void submitMessage(TolvenMessageWithAttachments tm) throws Exception {
        tmSchedulerBean.queueTolvenMessage(tm);
    }

    @WebMethod(action = "submitTrim")
    @RequestWrapper(localName = "submitTrim", className = "org.tolven.ws.trim.jaxws.SubmitTrimRequest")
    public void submitTrim(@WebParam(name = "webServiceTrim") WebServiceTrim webServiceTrim) {

        Map<String, Object> sourceMap = new HashMap<String, Object>();
        for (WebServiceField field : webServiceTrim.getFields()) {
            if (field.getValue() instanceof XMLGregorianCalendar) {
                sourceMap.put(field.getName(), ((XMLGregorianCalendar) field.getValue()).toGregorianCalendar().getTime());
            } else {
                sourceMap.put(field.getName(), field.getValue());
            }
        }
        TrimFactory factory = new TrimFactory();
        CE gender = factory.createCE();
        gender.setDisplayName("Male");
        gender.setCode("C0024554");
        // Male
        //      gender.setDisplayName("Female");
        //      gender.setCode("C0015780"); // Female
        gender.setCodeSystem("2.16.840.1.113883.6.56");
        gender.setCodeSystemVersion("2007AA");
        sourceMap.put("gender", gender);
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        try {
            AccountUser accountUser = accountBean.findAccountUser(principal, webServiceTrim.getAccountId());
            TolvenUser tolvenUser = accountUser.getUser();
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("account", accountUser.getAccount());
            variables.put("user", tolvenUser);
            variables.put("now", new Date());
            variables.put("source", sourceMap);
            TrimEx trim = trimBean.evaluateAndParseTrim(webServiceTrim.getName(), variables);
            TolvenMessageWithAttachments tm = createTolvenMessage(TRIM_NS, accountUser.getAccount().getId(), tolvenUser.getId());
            addTrimAsPayload(trim, tm);
            displayInstantiatedTrim(tm);
            submitMessage(tm);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        TolvenLogger.info("Processing WebServiceTrim: " + webServiceTrim.getName(), TrimServiceImpl.class);

    }

}
