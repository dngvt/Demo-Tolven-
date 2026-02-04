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
 * @version $Id: DocumentResources.java 8704 2014-02-19 09:56:43Z srini.kandula@tolvenhealth.com $
 */

package org.tolven.api.rs.resource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.MenuData;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.entity.DocBase;
import org.tolven.doc.entity.DocXML;
import org.tolven.security.key.DocumentSecretKey;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.trim.Act;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.ActRelationshipDirection;
import org.tolven.trim.ActRelationshipType;
import org.tolven.trim.Choice;
import org.tolven.trim.TrimMarshaller;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.TrimEx;
import org.tolven.trim.ex.TrimFactory;
import org.tolven.util.ExceptionFormatter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@Path("document")
@ManagedBean
public class DocumentResources extends TolvenResources {


    private static Logger logger = Logger.getLogger(DocumentResources.class);
    private static TrimMarshaller trimMarshaller;
   	/**
     * Create a document
     * @return response
     */
    @Path("create")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createDocument(
            @FormParam("mediaType") String mediaType,
            @FormParam("namespace") String namespace,
            @FormParam("payload") String payload) throws Exception {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        DocBase doc = getDocumentBean().createNewDocument(mediaType, namespace, accountUser);
        if(logger.isDebugEnabled())
        	logger.debug("Document created, id: " + doc.getId() + " Account: " + doc.getAccount().getId());
        String kbeKeyAlgorithm = getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
        int kbeKeyLength = Integer.parseInt(getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
        byte[] bytes = null;
        if(payload != null) {
            bytes = payload.getBytes("UTF-8");
        }
        doc.setAsEncryptedContent(bytes, kbeKeyAlgorithm, kbeKeyLength);
        doc.setFinalSubmitTime(TolvenRequest.getInstance().getNow());
        Response response = Response.ok().entity(String.valueOf(doc.getId())).build();
        return response;
    }

    /** 
     * Return placeholder, list based on document ID.
     * Type (placeholder or list) is optional.
     */
    @Path("referencedBy")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getReferencedBy(
            @QueryParam("documentId") String documentId,
            @QueryParam("role") @DefaultValue("") String roleFilter) throws Exception {
        Account account = TolvenRequest.getInstance().getAccount();
        if (account == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Account not found").build();
        }
        if (documentId == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("documentId is required").build();
        }
        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlStreamWriter = null;
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            xmlStreamWriter = factory.createXMLStreamWriter(sw);
            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
            xmlStreamWriter.writeStartElement("results");
            xmlStreamWriter.writeAttribute("document", documentId.toString());
            xmlStreamWriter.writeAttribute("account", Long.toString(account.getId()));
            xmlStreamWriter.writeAttribute("database", getPropertyBean().getProperty("tolven.repository.oid"));
            GregorianCalendar now = new GregorianCalendar();
            now.setTime(TolvenRequest.getInstance().getNow());
            DatatypeFactory xmlFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar ts = xmlFactory.newXMLGregorianCalendar(now);
            xmlStreamWriter.writeAttribute("timestamp", ts.toXMLFormat());
            List<MenuData> mdList = getDataExtractBean().findMenuDataByDocumentId(account, Long.parseLong(documentId));
            Iterator<MenuData> it = mdList.iterator();
            while (it.hasNext()) {
                MenuData md = (MenuData) it.next();
                String path = md.getPath();
                MenuPath mp = new MenuPath(path);
                AccountMenuStructure ms = getDataExtractBean().findAccountMenuStructure(account, mp);
                String role = ms.getRole();
                roleFilter = roleFilter.toLowerCase();
                if (roleFilter.equals("") || roleFilter.equals(role)) {
                    xmlStreamWriter.writeStartElement("row");
                    xmlStreamWriter.writeStartElement("path");
                    xmlStreamWriter.writeCharacters(path);
                    xmlStreamWriter.writeEndElement();
                    xmlStreamWriter.writeStartElement("role");
                    xmlStreamWriter.writeCharacters(role);
                    xmlStreamWriter.writeEndElement();
                    xmlStreamWriter.writeEndElement();
                }
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
    
    @Path("body")
    @GET
    public Response getDocumentBody(@QueryParam("id") String id) throws Exception {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        DocBase doc = getDocumentBean().findDocument(Long.parseLong(id));
        if (doc.getAccount().getId() != accountUser.getAccount().getId()) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Document not found in this account").build();
        }
        String keyAlgorithm = getPropertyBean().getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
        String body = getDocProtectionBean().getDecryptedContentString(doc, accountUser, sessionWrapper.getUserPrivateKey(keyAlgorithm));
        Response response = Response.ok().type(doc.getMediaType()).entity(body).build();
        return response;
    }

    @Path("header")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getDocumentHeader(@QueryParam("id") String id) throws Exception {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        DocBase doc = getDocumentBean().findDocument(Long.parseLong(id));
        if (doc.getAccount().getId() != accountUser.getAccount().getId()) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Document not found in this account").build();
        }
        Response response = Response.ok().entity(doc).build();
        return response;
    }

    @Path("signature")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDocumentSignature(@QueryParam("id") String id) throws Exception {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        DocBase doc = getDocumentBean().findDocument(Long.parseLong(id));
        if (doc.getAccount().getId() != accountUser.getAccount().getId()) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Document not found in this account").build();
        }
        String keyAlgorithm = getPropertyBean().getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
        String signature = getDocProtectionBean().getDocumentSignaturesString(doc, accountUser, sessionWrapper.getUserPrivateKey(keyAlgorithm));
        if (signature == null || signature.length() == 0) {
            return Response.noContent().build();
        }
        Response response = Response.ok().entity(signature).build();
        return response;
    }


    /**
     * Create a document
     * @return response
     */
    @Path("process/{id}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response processDocument(@PathParam("id") String id) throws Exception {
        getProcessLocal().processDocument(Long.parseLong(id), new Date());
        return Response.ok().build();
    }
    
    /**
    * Process a document (synchronously)
    * @return response
    */
    @Path("process")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response processDocument(@DefaultValue("urn:astm-org:CCR") @FormParam("xmlns") String xmlns, @DefaultValue("text/xml") @FormParam("mediaType") String mediaType, @FormParam("payload") String payload) throws Exception {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        long documentId = getProcessLocal().processMessage(payload.getBytes(), mediaType, xmlns, accountUser.getAccount().getId(), accountUser.getUser().getId(), new Date());
        URI uri = null;
        try {
            uri = new URI(URLEncoder.encode(Long.toString(documentId), "UTF-8"));
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ExceptionFormatter.toSimpleString(e, "\\n")).build();
        }
        Response response = Response.created(uri).entity(String.valueOf(documentId)).build();
        return response;
    }
    
    /**
     * Create a document
     * @return response
     */
    @Path("processXML/{id}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response processXMLDocument(@PathParam("id") String id) throws Exception {
        getProcessLocal().processXMLDocument(Long.parseLong(id), new Date());
        return Response.ok().build();
    }

  	/**
     * Submit a document for processing
     * @return response
     */
    @Path("submit")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response submitDocument(@DefaultValue("urn:astm-org:CCR") @FormParam("xmlns") String xmlns, @FormParam("payload") String payload) throws Exception {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        getTolvenMessageSchedulerBean().queueWSMessage(payload.getBytes(), xmlns, accountUser.getAccount().getId(), accountUser.getUser().getId());
        Response response = Response.ok().entity("Document submitted").build();
        return response;
    }

    /**
     * Submit a document for processing
     * @return response
     */
    @Path("submitJSON")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response submitJSON(@FormParam("payload") String payload){
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        JsonObject data = null;
        try{
        	data = new Gson().fromJson(payload, JsonObject.class);
        }catch (JsonSyntaxException e) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Bad JSON format").build();
		}
        if (data.get("patientPath") == null) {
        	throw new RuntimeException("patientPath not found");
        }
        String patientPath = data.get("patientPath").getAsString(); 
        JSONTrimProcessor processor = new JSONTrimProcessor();
        TrimEx trim;
		try {
			trim = processor.createPlaceholderAct(payload);
		} catch (Exception e1) {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(e1.getMessage()).build();
		}
	       
		try {
			getTrimCreatorBean().submitTrim(trim, patientPath, accountUser, new Date(),getUserPrivateKey());
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("error submitting trim :"+trim.getName()+" for patient:"+patientPath).build();
		}
       
        Response response = Response.ok().entity("Document submitted").build();
        return response;
    }
    /**
     * Submit a document for processing
     * @return response
     */
    @Path("submitHybridJSON")
    @POST
    @Consumes("application/octet-stream")
    public Response submitHybridJSON(InputStream stream){
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
       
        JsonObject data = null;
        try{
        	data = new Gson().fromJson(IOUtils.toString(stream), JsonObject.class);
        }catch (Exception e) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Bad JSON format").build();
		} 
        if (data.get("patientPath") == null) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("patientPath not found").build();
        }
        if (data.get("trimName") == null) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("trimName not found").build();
        }
        String trimName = data.get("trimName").getAsString();        
        String patientPath = data.get("patientPath").getAsString(); 
        TrimEx trim;
		try {
			trim = getTrimCreatorBean().createTrim(accountUser,trimName, patientPath, new Date());
		} catch (Exception e1) {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(e1.getMessage()).build();
		}
		MenuPath context = new MenuPath(patientPath);
		getCreatorBean().computeScan( trim, accountUser, context, new Date(), null);
		TrimExpressionEvaluator ee = new TrimExpressionEvaluator();
		TrimFactory trimFactory = new TrimFactory();
	    ee.addVariable("trim", trim);
	    List<Choice> choices = (List<Choice>) ee.evaluate("#{trim.act.relationship['content'].choices}");
	    JSONTrimProcessor processor = new JSONTrimProcessor();
	    for(Choice choice:choices){
	    	ee.addVariable("choice", choice);
	    	//for each choice process the incoming data if exists
	    	if(!data.has(choice.getName())){
	    		ee.setValue("#{trim.act.relationship[choice.name].act.enableAct}", false);
	    		logger.info("No data found for "+choice.getName());
	    	}else{
	    		JsonObject choiceObject = data.get(choice.getName()).getAsJsonObject();
	    		
	    		// if the type is allergy or problem or medication or immunization 
	    		if(choiceObject.has("type")){
	    			if( (choiceObject.get("type").getAsString().equals("problem") || 
	    				choiceObject.get("type").getAsString().equals("immunization")|| 
	    				choiceObject.get("type").getAsString().equals("medication")|| 
		    			choiceObject.get("type").getAsString().equals("allergy")) ||
		    			choiceObject.get("type").getAsString().equals("recommendedMedication")){
	    				JsonArray typeData = choiceObject.get("data").getAsJsonArray();
						for(int j =0;j<typeData.size();j++){
							//instantiate a new Trim, populate the data and add the act to the container trim
							TrimEx typeTrim = null;
							try {
								typeTrim = processor.createPlaceholderAct(typeData.get(j).getAsJsonObject().get("data").getAsJsonObject());
							} catch (Exception e) {
								return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("error processing data :"+typeData.get(j)+" for patient:"+patientPath).build();
							}
							ActRelationship ar = trimFactory.createActRelationship();
							ar.setTypeCode(ActRelationshipType.valueOf("COMP"));
							ar.setDirection(ActRelationshipDirection.valueOf("OUT"));
							ar.setName(choiceObject.get("type").getAsString());
							ar.setAct(typeTrim.getAct());
							ActEx act = (ActEx) ee.evaluate("#{trim.act.relationship[choice.name].act}");
							act.getRelationships().add(ar);
						}
					}else{
						populateAssessmentForm(choiceObject, ee, accountUser, processor);
					}					
	    		}else{
	    			populateAssessmentForm(choiceObject, ee, accountUser, processor);
				}
			}
	    }
	    try {
			getTrimCreatorBean().submitTrim(trim, patientPath, accountUser, new Date(),getUserPrivateKey());
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("error submitting trim :"+trim.getName()+" for patient:"+patientPath).build();
		}
        Response response = Response.ok().entity("Document submitted").build();
        return response;
    }
    private void populateAssessmentForm(JsonObject choiceObject,TrimExpressionEvaluator ee,AccountUser accountUser, JSONTrimProcessor processor ){
    	JsonObject choiceData = choiceObject.getAsJsonObject("data");
		if(choiceData.get("placeholder") != null){
			// pick the trim from container act
			Act act = (Act) ee.evaluate("#{trim.act.relationship[choice.name].act}");
			ee.addVariable("act", act);
			String placeholder = choiceData.get("placeholder").getAsString();
			AccountMenuStructure ams = getMenuBean().findAccountMenuStructure(accountUser.getAccount().getId(), placeholder);
			if(logger.isDebugEnabled())
				logger.debug("uploading "+choiceData);
			processor.populateAct(ams, ee, choiceData);
		}
    }
    
    
    /**
     * Instantiate an Assessment for processing
     * @return response
     */
    @Path("instantiateAssessment")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response instantiateAssessment(@FormParam("payload") String payload){
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        JsonObject data = null;
        try{
        	data = new Gson().fromJson(payload, JsonObject.class);
        }catch (JsonSyntaxException e) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Bad JSON format").build();
		}
        if (data.get("patientPath") == null) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("patientPath not found").build();
        }
        if (data.get("trimName") == null) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("trimName not found").build();
        }
        String trimName = data.get("trimName").getAsString();        
        String patientPath = data.get("patientPath").getAsString(); 
        MenuData assessmentMD;
		try {
			assessmentMD = getTrimCreatorBean().createTRIMPlaceholder(accountUser,trimName, patientPath, new Date(),null,null);
		} catch (Exception e1) {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(e1.getMessage()).build();
		}
		if(assessmentMD == null){
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Failed to create assessment").build();
		}
		Response response = Response.ok().entity(assessmentMD.getPath()).build();
	    return response;
	}
    /**
     * Saves an Assessment with data sent via JSON. This method unmarshalls  the trim document 
     * @return response
     */
    @Path("saveAssessmentForm")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response saveAssessmentForm(@FormParam("payload") String payload){
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        JsonObject data = null;
        try{
        	data = new Gson().fromJson(payload, JsonObject.class);
        }catch (JsonSyntaxException e) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Bad JSON format").build();
		}
        if (data.get("assessmentId") == null) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("assessmentId not found").build();
        }
        if (data.get("formName") == null) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("formName not found").build();
        }
        if (data.get(data.get("formName").getAsString()) == null) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("formName not found").build();
        }
        MenuData assessmentMD = getMenuBean().findMenuDataItem(accountUser.getAccount().getId(), data.get("assessmentId").getAsString());
		if (assessmentMD == null) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Assessment not found with id "+data.get("assessmentId").getAsString()).build();
        }
                
        DocXML doc = (DocXML) getDocumentBean().findDocument(assessmentMD.getDocumentId());
		TrimEx assessmentTrim = null;
		try {
			assessmentTrim = (TrimEx) getTrimMarshaller().unmarshal(doc, accountUser,getUserPrivateKey());
		} catch (JAXBException e) {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Error unmarshalling Assessment trim "+data.get("assessmentId").getAsString()+"  "+e.getMessage()).build();
		}
        JSONTrimProcessor processor = new JSONTrimProcessor();
		TrimExpressionEvaluator ee = new TrimExpressionEvaluator();
	    ee.addVariable("trim", assessmentTrim);
	    ee.addVariable("formname", data.get("formName").getAsString());
        ActEx formAct = (ActEx) ee.evaluate("#{trim.act.relationship[formname].act}");
        ee.addVariable("act", formAct);
        JsonObject formData = data.get(data.get("formName").getAsString()).getAsJsonObject();
        processor.populateAct(null, ee, formData);
        ByteArrayOutputStream trimXML = new ByteArrayOutputStream() ;
		try {
			getTrimMarshaller().marshalTRIM(assessmentTrim, trimXML);
		} catch (JAXBException e) {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Error marshalling Assessment trim "+data.get("assessmentId").getAsString()+"  "+e.getMessage()).build();
		}
        String kbeKeyAlgorithm = getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
        int kbeKeyLength = Integer.parseInt(getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
		doc.setAsEncryptedContent(trimXML.toByteArray(), kbeKeyAlgorithm, kbeKeyLength);
		Response response = Response.ok().entity(assessmentMD.getPath()).build();
	    return response;
	}
    
    /**
     * Instantiate an Assessment for processing
     * @return response
     */
    @Path("submitAssessment")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response submitAssessment(@FormParam("payload") String payload){
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        JsonObject data = null;
        try{
        	data = new Gson().fromJson(payload, JsonObject.class);
        }catch (JsonSyntaxException e) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Bad JSON format").build();
		}
        if (data.get("assessmentId") == null) {
        	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("assessmentId not found").build();
        }
        MenuData assessmentMD = getMenuBean().findMenuDataItem(accountUser.getAccount().getId(), data.get("assessmentId").getAsString());
        try {
			getCreatorBean().submitNow(assessmentMD, accountUser, new Date(), getUserPrivateKey());
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Failed to Assessment trim "+data.get("assessmentId").getAsString()+"  "+e.getMessage()).build();
		}
		
		Response response = Response.ok().entity(assessmentMD.getPath()).build();
	    return response;
	}
    
  	/**
     * Create a document
     * @return response
     */
    @Path("update")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateDocument(
            @FormParam("id") String id,
            @FormParam("payload") String payload) throws Exception {
        AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
        if (accountUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("AccountUser not found").build();
        }
        DocBase docBase = getDocumentBean().findDocument(Long.parseLong(id));
        if (docBase.getAccount().getId() != accountUser.getAccount().getId()) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Document not found in this account").build();
        }
        String kbeKeyAlgorithm = getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
        int kbeKeyLength = Integer.parseInt(getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
        byte[] bytes = null;
        if(payload != null) {
            bytes = payload.getBytes("UTF-8");
        }
        docBase.setAsEncryptedContent(bytes, kbeKeyAlgorithm, kbeKeyLength);
        docBase.setFinalSubmitTime(TolvenRequest.getInstance().getNow());
        Response response = Response.ok().build();
        return response;
    }

    private TrimMarshaller getTrimMarshaller() {
        if(trimMarshaller == null) {
            trimMarshaller = new TrimMarshaller();
        }
        return trimMarshaller;
    }
   
}
