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
 * @version $Id: DocumentResources.java 8567 2013-12-17 15:01:08Z srini.kandula@tolvenhealth.com $
 */

package org.tolven.api.rs.resource;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import javax.annotation.ManagedBean;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.tolven.app.DataQueryResults;
import org.tolven.app.entity.MSColumn;
import org.tolven.app.entity.MenuData;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;

@Path("dataextract")
@ManagedBean
public class DataExtractResources extends TolvenResources {
	
    private static Logger logger = Logger.getLogger(DataExtractResources.class);

    /** 
     * Return placeholder, list based on document ID.
     * Type (placeholder or list) is optional.
     */
    @Path("data")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getData( @QueryParam("path") String path,@QueryParam("filter") @DefaultValue("") String filter) throws Exception {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        DataQueryResults dataQueryResults = getDataExtractBean().setupQuery(path, accountUser); //.substring(appSchema.length()
        dataQueryResults.setNow(new Date());
        dataQueryResults.enableAllFields();
        dataQueryResults.setReturnTotalCount(true);
        dataQueryResults.setReturnFilterCount(false);
        dataQueryResults.setLimit(10);
        boolean isItemQuery = true;  //works for Patient data and info
        if(path.split(":").length > 3) {
        	isItemQuery = false;  //works for lists like Allergies and problems
        }
        dataQueryResults.setItemQuery(isItemQuery);
        StringWriter buffer = new StringWriter();
		getDataExtractBean().streamResultsXML(buffer, dataQueryResults);
		Response response = Response.ok().entity(buffer.toString()).build();
        return response;
    }
    /** 
     * Return placeholder, list based on document ID.
     * Type (placeholder or list) is optional.
     */
    @Path("dataForSQL")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getDataForSQL( @QueryParam("query") String queryString) throws Exception {
        StringWriter buffer = new StringWriter();
		getDataExtractBean().streamResultsSQL(buffer, queryString);
		Response response = Response.ok().entity(buffer.toString()).build();
        return response;
    }

    @Path("transform")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response transform(@QueryParam("element") String element,@QueryParam("xslt") String xslt){
    	StringWriter writer = new StringWriter();
    	try {
    	AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
		Source source = new StreamSource( new StringReader(xslt) );
		Result outputTarget = new StreamResult( writer);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		getAppResolver().setAccountUser(accountUser);
		transformerFactory.setURIResolver(getAppResolver());
		Transformer transformer = transformerFactory.newTransformer(source);		
		MenuData elementMD = getMenuBean().findMenuDataItem(accountUser.getAccount().getId(), element);
		MenuData patientMd = elementMD.getParent01();
		transformer.setParameter("context", patientMd.getPath());
		for(MSColumn column:patientMd.getMenuStructure().getColumns()){
			Object obj = patientMd.getField(column.getHeading());
			if(obj != null)
				transformer.setParameter(column.getHeading(), obj.toString());
		}
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		DOMSource domsrc = new DOMSource(db.newDocument());
		transformer.transform(domsrc, outputTarget);
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	Response response = Response.ok().entity(writer.toString()).build();
        return response;
    }
}
