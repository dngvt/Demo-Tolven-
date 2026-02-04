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
 * @version $Id: ApplicationResources.java 8704 2014-02-19 09:56:43Z srini.kandula@tolvenhealth.com $
 */  

package org.tolven.api.rs.resource;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.tolven.api.security.GeneralSecurityFilter;
import org.tolven.app.DataQueryResults;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.MDQueryResults;
import org.tolven.app.entity.MSColumn;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;
import org.tolven.util.ExceptionFormatter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Path("application")
public class ApplicationResources extends TolvenResources{
	
	private Logger logger = Logger.getLogger(this.getClass());
    
    @Context
    private HttpServletRequest request;
    
		
	/**
	 * Perform a general application query. 
	 * @param path Specify the path to query, includes placeholder ids where appropriate.
	 * @param fields A list of fields to include in the query or null to include all fields
	 * @param order A list of fields to sort on. Maybe include " asc" or " desc" to specify direction
	 * @param totalCount If true, (default is false), also return the total count of items in the list
	 * @param offset Starting row to return, defaults to zero (first row)
	 * @param limit With a (default) limit of 0, no query is performed, only metadata is returned. A limit of -1 returns all rows.
	 * @return
	 */
    @Path("list")
    @GET
    @Produces(MediaType.APPLICATION_XML)
	public Response getApplicationList (
			@QueryParam("path") String path, 
			@QueryParam("fields") String fields,
			@QueryParam("order") String order,
			@DefaultValue("false" ) @QueryParam("totalCount") String totalCount,
			@DefaultValue("false" ) @QueryParam("filterCount") String filterCount,
			@DefaultValue("0" ) @QueryParam("offset") String offset,
			@DefaultValue("0" ) @QueryParam("limit") String limit
			) {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        //			MenuPath menuPath = new MenuPath( path );
        //			long accountId = accountUser.getAccount().getId();
        Date now = (Date) request.getAttribute("tolvenNow");
        DataQueryResults dataQueryResults = getDataExtractBean().setupQuery(path, accountUser);
        dataQueryResults.setNow(now);
        if (order != null) {
            dataQueryResults.setOrder(order.split(","));
        }
        dataQueryResults.setOffset(Integer.parseInt(offset));
        dataQueryResults.setLimit(Integer.parseInt(limit));
        if (fields != null && fields.length() > 0) {
            dataQueryResults.disableAllFields();
            // Setup fields
            String fieldArray[] = fields.split(",");
            for (String external : fieldArray) {
                dataQueryResults.findExternalField(external).setEnabled(true);
            }
        } else {
            dataQueryResults.enableAllFields();
        }
        dataQueryResults.setReturnTotalCount("true".equals(totalCount));
        dataQueryResults.setReturnFilterCount("true".equals(filterCount));
        Response response = Response.ok().entity(dataQueryResults).build();
        return response;
    }
    /**
	 * Perform a general application query and return the contents in JSON format. 
	 * @param path Specify the path to query, includes placeholder ids where appropriate.
	 * @param fields A list of fields to include in the query or null to include all fields
	 * @param order A list of fields to sort on. Maybe include " asc" or " desc" to specify direction
	 * @param totalCount If true, (default is false), also return the total count of items in the list
	 * @param offset Starting row to return, defaults to zero (first row)
	 * @param limit With a (default) limit of 0, no query is performed, only metadata is returned. A limit of -1 returns all rows.
	 * @return
	 */
    @Path("listJSON")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
	public Response getApplicationListJSON (
			@QueryParam("path") String path, 
			@QueryParam("fields") String fields,
			@QueryParam("order") String order,
			@DefaultValue("false" ) @QueryParam("totalCount") String totalCount,
			@DefaultValue("false" ) @QueryParam("filterCount") String filterCount,
			@DefaultValue("0" ) @QueryParam("offset") String offset,
			@DefaultValue("0" ) @QueryParam("limit") String limit
			) {
    	if(logger.isDebugEnabled())
    		logger.debug("Start:getApplicationListJSON  for "+path +" offset:"+offset);
        Long accountUserId = (Long) request.getAttribute(GeneralSecurityFilter.ACCOUNTUSER_ID);
        if (accountUserId == null) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Account required").build();
        }
        AccountUser accountUser = getActivationBean().findAccountUser(accountUserId);
        
        if (accountUser == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        //			MenuPath menuPath = new MenuPath( path );
        //			long accountId = accountUser.getAccount().getId();
        MenuPath menuPath = new MenuPath(path);
        MenuStructure ms =  getMenuBean().findMenuStructure(accountUser, menuPath.getPath() );
        MenuQueryControl ctrl = new MenuQueryControl();
		ctrl.setAccountUser(accountUser);		
		ctrl.setMenuStructure( ms );
		ctrl.setNow( new Date() );		
		ctrl.setOriginalTargetPath(menuPath );
		ctrl.setRequestedPath( menuPath );
		ctrl.setSortOrder( order );
		if(offset != null)
			ctrl.setOffset(Integer.parseInt(offset));
		if(limit != null)
			ctrl.setLimit(Integer.parseInt(limit));
		MDQueryResults mdQueryResults = getMenuBean().findMenuDataByColumns( ctrl );
		List<Map<String, Object>>  results = mdQueryResults.getResults();
		JsonArray result = new JsonArray();
		for(Map<String, Object>  rowMap : results ){
			JsonObject object = new JsonObject();
			for(String column:rowMap.keySet()){
				Object value = rowMap.get(column);
				if(value != null)
				object.addProperty(column, value.toString());
			}
			result.add(object);
		}
		if(logger.isDebugEnabled())
    		logger.debug("End: getApplicationListJSON  for "+path +" offset:"+offset);
        
        Response response = Response.ok().entity(result.toString()).build();
        return response;
    }
	
    /**
	 * Find the size of the list contents. 
	 * @param path Specify the path to query, includes placeholder ids where appropriate.
	 * @return
	 */
    @Path("listSize")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
	public Response getListSize(@QueryParam("path") String path) {
        Long accountUserId = (Long) request.getAttribute(GeneralSecurityFilter.ACCOUNTUSER_ID);
        if (accountUserId == null) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Account required").build();
        }
        AccountUser accountUser = getActivationBean().findAccountUser(accountUserId);
        
        if (accountUser == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        //			MenuPath menuPath = new MenuPath( path );
        //			long accountId = accountUser.getAccount().getId();
        Date now = (Date) request.getAttribute("tolvenNow");
        MenuPath menuPath = new MenuPath(path);
        MenuStructure ms =  getMenuBean().findMenuStructure(accountUser, menuPath.getPath() );
        MenuQueryControl ctrl = new MenuQueryControl();
		ctrl.setAccountUser(accountUser);		
		ctrl.setMenuStructure( ms );
		ctrl.setNow( now );		
		ctrl.setOriginalTargetPath(menuPath );
		ctrl.setRequestedPath( menuPath );
		long count = getMenuBean().countMenuData(ctrl);
		Response response = Response.ok().entity(String.valueOf(count)).build();
        return response;
    }
    
    @Path("placeholderMetada")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
	public Response getPlaceholderConfiguration(@QueryParam("path") String path) {
        Long accountUserId = (Long) request.getAttribute(GeneralSecurityFilter.ACCOUNTUSER_ID);
        if (accountUserId == null) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Account required").build();
        }
        AccountUser accountUser = getActivationBean().findAccountUser(accountUserId);
        
        if (accountUser == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        MenuPath menuPath = new MenuPath(path);
        AccountMenuStructure ms =  getMenuBean().findAccountMenuStructure(accountUser.getAccount().getId(), menuPath.getPath() );
        Response response = Response.ok().entity(ms).build();
        return response;
    }
    
	
    
	/**
	 * Perform a general application query. 
	 * @param path Specify the path to query, includes placeholder ids where appropriate.
	 * @param fields A list of fields to include in the query or null to include all fields
	 * @param order A list of fields to sort on. Maybe include " asc" or " desc" to specify direction
	 * @param offset Starting row to return, defaults to zero (first row)
	 * @param limit With a (default) limit of 0, no query is performed, only metadata is returned. A limit of -1 returns all rows.
	 * @return
	 */
    @Path("item")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getApplicationItem (
            @QueryParam("path") String path, 
            @QueryParam("fields") String fields
            ) throws Exception  {
        Long accountUserId = (Long) request.getAttribute(GeneralSecurityFilter.ACCOUNTUSER_ID);
        if (accountUserId == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Account required").build();
        }
        AccountUser accountUser = getActivationBean().findAccountUser(accountUserId);
        
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        Date now = (Date) request.getAttribute("tolvenNow");
        DataQueryResults dataQueryResults = getDataExtractBean().setupQuery(path, accountUser);
        dataQueryResults.setNow(now);
        if (fields != null && fields.length() > 0) {
            dataQueryResults.disableAllFields();
            // Setup fields
            String fieldArray[] = fields.split(",");
            for (String external : fieldArray) {
                dataQueryResults.findExternalField(external).setEnabled(true);
            }
        } else {
            dataQueryResults.enableAllFields();
        }
        dataQueryResults.setItemQuery(true);
        dataQueryResults.setLimit(1);
        Response response = Response.ok().entity(dataQueryResults).build();
        return response;
    }
    
    @Path("menudata")
    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public Response findMenuData(@QueryParam("path") String path) {
    	if(path == null || path.length() == 0)
    		return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("path is missing").build();
    	AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
    	MenuData md = getMenuBean().findMenuDataItem(accountUser.getAccount().getId(), path);
    	if(md == null){
    		return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("No Menudata found").build();
    	}
    	JsonObject json = getMenuDataAsJson(md);
    	return Response.ok().type(MediaType.APPLICATION_XML_TYPE).entity(json.toString()).build();
    }
    /**
     * Method to construct JSON representation of a menudata item
     * @param md
     * @return
     */
    private JsonObject getMenuDataAsJson(MenuData md) {
    	JsonObject json = new JsonObject();
    	AccountMenuStructure ams = md.getMenuStructure();
    	for(MSColumn column:ams.getColumns()){
    		Object value = md.getField(column.getHeading());
    		if(column.getDisplayFunction() != null && column.getDisplayFunction().startsWith("parent")){
    			MenuData parent =(MenuData) value;
    			json.add(column.getHeading(),getMenuDataAsJson(parent));
    		}else if(md.getField(column.getHeading()) != null){    			
    			json.addProperty(column.getHeading(), md.getField(column.getHeading()).toString());
    		}
    	}
		return json;
	}
	/**
	 * Perform a general application query. 
	 * @param path Specify the path to query, includes placeholder ids where appropriate.
	 * @param fields A list of fields to include in the query or null to include all fields
	 * @param order A list of fields to sort on. Maybe include " asc" or " desc" to specify direction
	 * @param offset Starting row to return, defaults to zero (first row)
	 * @param limit With a (default) limit of 0, no query is performed, only metadata is returned. A limit of -1 returns all rows.
	 * @return
	 */
    @Path("xslt")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getXSLT (
            @QueryParam("path") String path,
            @QueryParam("xslt") String xsltFile,
            @QueryParam("context") String context
            ) throws Exception  {
        Long accountUserId = (Long) request.getAttribute(GeneralSecurityFilter.ACCOUNTUSER_ID);
        if (accountUserId == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Account required").build();
        }
        AccountUser accountUser = getActivationBean().findAccountUser(accountUserId);

        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        return generateXSLTResponse(accountUser.getProperty().get(xsltFile), accountUser, context);
    }
    
    public Response generateXSLTResponse(String pvalue, AccountUser accountUser, String context) {

        try {
	        Source source = new StreamSource( new StringReader(pvalue) );
			Writer writer = new StringWriter();
			Result outputTarget = new StreamResult( writer);
			logger.info(outputTarget.toString());
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			getAppResolver().setAccountUser(accountUser);
		    transformerFactory.setURIResolver(getAppResolver());
		    //transformerFactory.setAttribute("indent-number", 2);
		    Transformer transformer = transformerFactory.newTransformer(source);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			DOMSource domsrc = new DOMSource(db.newDocument());
			if(context != null) {
				transformer.setParameter("context", context);
			}
			transformer.transform(domsrc, outputTarget);
			//transformer.transform(new DOMSource(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), outputTarget);
	        Response response = Response.ok().type(MediaType.APPLICATION_XML).entity(writer.toString()).build(); 
	        return response;
        } catch (Exception ex) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ExceptionFormatter.toSimpleString(ex, "\\n")).build();
        }
	}

	/**
	 * Get column metadata for a specific metadata item. 
	 * @param path Specify the path. Placeholder ids, of present, will be ignored.
	 * @return
	 */
    @Path("describe")
    @GET
    @Produces(MediaType.APPLICATION_XML)
	public Response getDescribeColumns (@QueryParam("path") String path) {
        Long accountUserId = (Long) request.getAttribute(GeneralSecurityFilter.ACCOUNTUSER_ID);
        if (accountUserId == null) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Account required").build();
        }
        AccountUser accountUser = getActivationBean().findAccountUser(accountUserId);

        if (accountUser == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        MenuPath menuPath = new MenuPath(path);
        //			long accountId = accountUser.getAccount().getId();
        //			Date now = (Date) request.getAttribute("tolvenNow");
        AccountMenuStructure ams;
        try {
            ams = getDataExtractBean().findAccountMenuStructure(accountUser.getAccount(), menuPath);
        } catch (Exception e1) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Metadata not found: " + menuPath).build();
        }
        Response response = Response.ok().entity(ams).build();
        return response;
    }
		
    /**
     * Get all metadata  
     * @param path Specify the path. Placeholder ids, of present, will be ignored.
     * @return
     */
    @Path("metadata")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getMetadata() {
        Long accountUserId = (Long) request.getAttribute(GeneralSecurityFilter.ACCOUNTUSER_ID);
        if (accountUserId == null) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Account required").build();
        }
        AccountUser accountUser = getActivationBean().findAccountUser(accountUserId);

        if (accountUser == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        List<AccountMenuStructure> amss;
        amss = getMenuBean().findFullAccountMenuStructure(accountUser.getAccount().getId());
        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlStreamWriter = null;
        try {
            GregorianCalendar now = new GregorianCalendar();
            now.setTime((Date) request.getAttribute("tolvenNow"));
            DatatypeFactory xmlFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar ts = xmlFactory.newXMLGregorianCalendar(now);
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            xmlStreamWriter = factory.createXMLStreamWriter(sw);
            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
            xmlStreamWriter.writeStartElement("user");
            xmlStreamWriter.writeAttribute("accountId", Long.toString(accountUser.getAccount().getId()));
            xmlStreamWriter.writeAttribute("timestamp", ts.toXMLFormat());
            for (AccountMenuStructure ams : amss) {
                String role = ams.getRole();
                xmlStreamWriter.writeStartElement(role);
                xmlStreamWriter.writeAttribute("name", ams.getNode());
                xmlStreamWriter.writeAttribute("path", ams.getPath());
                if (ams.getText() != null) {
                    xmlStreamWriter.writeStartElement("text");
                    xmlStreamWriter.writeCharacters(ams.getText());
                    xmlStreamWriter.writeEndElement();
                }
                if (ams.getAllowRoles() != null) {
                    xmlStreamWriter.writeStartElement("rolesAllowed");
                    xmlStreamWriter.writeCharacters(ams.getAllowRoles());
                    xmlStreamWriter.writeEndElement();
                }
                xmlStreamWriter.writeEndElement();
            }
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeEndDocument();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN_TYPE).entity(ExceptionFormatter.toSimpleString(e, "\n")).build();
        } finally {
            if (xmlStreamWriter != null) {
                try {
                    xmlStreamWriter.close();
                } catch (XMLStreamException e) {
                }
            }
        }
        Response response = Response.ok().entity(sw.toString()).build();
        return response;
    }
    
    
    
   
    
}
