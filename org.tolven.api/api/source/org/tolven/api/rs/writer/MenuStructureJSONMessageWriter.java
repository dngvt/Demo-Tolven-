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
 * @version $Id: MenuStructureMessageWriter.java 6180 2012-03-29 10:25:16Z joe.isaac $
 */  

package org.tolven.api.rs.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.MSColumn;
import org.tolven.core.TolvenPropertiesLocal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
/**
 * Send Metadata for a specific MenuStructure to client
 * @author John Churin
 *
 */
@Provider
@Produces("application/json")
public class MenuStructureJSONMessageWriter implements MessageBodyWriter<AccountMenuStructure> {

    @EJB
    private TolvenPropertiesLocal propertiesBean;

    
    //private TrimExpressionEvaluator ee = new TrimExpressionEvaluator();

    @Override
    public long getSize(AccountMenuStructure ams, Class<?> genericType, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> genericType, Type type, Annotation[] annotations, MediaType mediaType) {
        return (genericType == AccountMenuStructure.class);
    }

	@Override
	public void writeTo(AccountMenuStructure ams, Class<?> genericType, Type type, 
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException, WebApplicationException {
        try {
            JsonObject ms = new JsonObject();
            //ms.("metadata");
            ms.addProperty("path", ams.getPath());
            ms.addProperty("account", String.valueOf(ams.getAccount().getId()));
            ms.addProperty("database", getPropertyBean().getProperty("tolven.repository.oid"));
            JsonArray columns = new JsonArray();
            for (MSColumn col : ams.getColumns()) {
            	JsonObject column = new JsonObject();
            	column.addProperty("name", col.getHeading());
                if (col.getDatatype() != null) {
                	column.addProperty("datatype", col.getDatatype());
                }
                column.addProperty("text", col.getText());
                columns.add(column);
            }
            ms.add("columns", columns);
            out.write(new Gson().toJson(ms).getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Exception writing metadata", e);
        } 
    }
	
	protected TolvenPropertiesLocal getPropertyBean() {
  		if (propertiesBean == null) {
            String jndiName = "java:app/tolvenEJB/TolvenProperties!org.tolven.core.TolvenPropertiesLocal";
            try {
                InitialContext ctx = new InitialContext();
                propertiesBean = (TolvenPropertiesLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
  		return propertiesBean;
  	}
}
