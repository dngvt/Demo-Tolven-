/*
 * Copyright (C) 2010 Tolven Inc

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
 * @author John Churin
 * @version $Id: LoadUSStatesResources.java 7406 2012-12-13 02:48:24Z joe.isaac $
 */

package org.tolven.restful;

import java.io.ByteArrayInputStream;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.tolven.us.states.DemographicsLocal;
import org.w3c.dom.Document;

@Path("loaderustates")
public class LoadUSStatesResources {

    @EJB
    private DemographicsLocal demographicsBean;

    //XPath variable
	XPath xpath = XPathFactory.newInstance().newXPath();

    public LoadUSStatesResources() {
    }

    protected DemographicsLocal getDemographicsBean() {
        if (demographicsBean == null) {
            String jndiName = "java:app/tolvenEJB/DemographicsBean!org.tolven.us.states.DemographicsLocal";
            try {
                InitialContext ctx = new InitialContext();
                demographicsBean = (DemographicsLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return demographicsBean;
    }

    /**
     * Queue a process to activate new trim headers.
     * @return true if there's more work to be done
     */
    @Path("loadStateNames")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response loadStateNames(String xml) {

    	try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
			
			
			String stateCode=(String)path("/StateNames/StateCode/text()").evaluate(doc, XPathConstants.STRING );
			String stateName=(String)path("/StateNames/StateName/text()").evaluate(doc, XPathConstants.STRING );
			
			//System.out.println("State Code: "+stateCode+"  State Description: "+stateDescription  );
			
			getDemographicsBean().createStateNames(stateCode,stateName);
			
    	} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
        return Response.ok().build();
    }

    /**
     * Method to compile xpath expression 
     * @param expression - String representing the expression
     * @return 
     */
    private XPathExpression path( String expression ) {
        try {
            return xpath.compile(expression);
        } catch (XPathExpressionException e) {
            throw new RuntimeException( "Invalue XPath Expression", e );
        }
    }

}
