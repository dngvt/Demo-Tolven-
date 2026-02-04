package org.tolven.ccd.resource;
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
 * @author <your name>
 * @version $Id: ApplicationResources.java 8456 2013-11-11 04:07:07Z srini.kandula@tolvenhealth.com $
 */  


import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.tolven.ccd.CCDLocal;

@Path("CCD")
public class CCDResources {
	
	@EJB
	private CCDLocal ccdLocal;
   
	public void setCcdLocal(CCDLocal ccdLocal) {
		this.ccdLocal = ccdLocal;
	}

	@Path("xml")
    @GET
    @Produces(MediaType.APPLICATION_XML)
	public Response getXML (@QueryParam("path") String path) throws ParserConfigurationException, TransformerException {
		Response response = Response.ok().entity(getCcdLocal().getCCDXml(path)).build();
        return response;
	}
	
	private CCDLocal  getCcdLocal() {
		if (ccdLocal == null) {
			String jndiName = "java:app/tolvenEJB/CCDBean!org.tolven.ccd.CCDLocal";
			try {
				InitialContext ctx = new InitialContext();
				ccdLocal = (CCDLocal) ctx.lookup(jndiName);
			}catch (Exception ex) {
			throw new RuntimeException("Could not lookup " + jndiName);
			}
		}
		return ccdLocal;
	}
}
