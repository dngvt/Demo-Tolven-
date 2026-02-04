/*
 * Copyright (C) 2013 Tolven Inc

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
 * @author Srini Kandula
 * @version $Id: TrimResources.java 8582 2013-12-23 15:47:05Z srini.kandula@tolvenhealth.com $
 */

package org.tolven.api.rs.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.tolven.app.MDVersionDTO;

@Path("menu")
public class MenuResources extends TolvenResources{
	 private static Logger logger = Logger.getLogger(MenuResources.class);
   
    @Path("versionCheck")
    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public Response versionCheck(@QueryParam("listPaths") String listPaths) {
    	if(logger.isDebugEnabled())
    		logger.debug("Start: versionCheck for "+listPaths);
    	if(listPaths == null || listPaths.length() == 0)
    		return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("listPaths is missing").build();
    	String paths[] = listPaths.split(",");
    	Map<String,String> versions = new HashMap<String,String>();
    	for(String path:paths){
    		String entry[] = path.split("=");
    		if(entry.length != 2)
    			continue;
    		versions.put(entry[0], entry[1]);
    	}
    	StringBuilder response = new StringBuilder();
    	List<MDVersionDTO> mdvs = getMenuBean().findGlobalMenuDataVersions(new ArrayList<String>(versions.keySet()));
    	for (MDVersionDTO mdv : mdvs ) {
    		long version = Long.parseLong(versions.get(mdv.getElement()));
    		if (mdv.getVersion() > version) {
    			if (response.length() > 0)
    				response.append( ",");
    			response.append(mdv.getElement());
    			response.append("=");
    			response.append(mdv.getVersion());
    		}
    		versions.remove(mdv.getElement());
    	}
    	for(String key:versions.keySet()){
    		if (response.length() > 0)
				response.append( ",");
			response.append(key);
			response.append("=");
			response.append(0);
		}
    	if(logger.isDebugEnabled())
    		logger.debug("End: versionCheck for "+listPaths);
        return Response.ok().type(MediaType.APPLICATION_XML_TYPE).entity(response.toString()).build();
    }
  
}
