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
 * @version $Id: UserResources.java 6180 2012-03-29 10:25:16Z joe.isaac $
 */  

package org.tolven.api.rs.resource;

import java.io.StringWriter;
import java.security.Principal;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Set;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.tolven.core.ActivationLocal;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.TolvenUser;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.util.ExceptionFormatter;

@Path("user")
public class UserResources {
    
    @EJB
    private ActivationLocal activationBean;

    @EJB
    private TolvenPropertiesLocal propertyBean;
    
    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;
    
    protected Response prepareUserDetails( String username, String fields ) throws Exception {
        TolvenUser tolvenUser = TolvenRequest.getInstance().getTolvenUser();
        if (tolvenUser == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN_TYPE).entity("TolvenUser not found").build();
        }
        //TolvenPerson tp = ldapBean.createTolvenPerson(tolvenUser.getLdapUID());
        StringWriter sw = new StringWriter();  
        XMLStreamWriter xmlStreamWriter = null;
        try {
            GregorianCalendar now = new GregorianCalendar();
            now.setTime(TolvenRequest.getInstance().getNow());
            DatatypeFactory xmlFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar ts = xmlFactory.newXMLGregorianCalendar(now);
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            xmlStreamWriter = factory.createXMLStreamWriter(sw);
            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
            xmlStreamWriter.writeStartElement("user");
            xmlStreamWriter.writeAttribute("username", username);
            xmlStreamWriter.writeAttribute("id", Long.toString(tolvenUser.getId()));
            xmlStreamWriter.writeAttribute("timestamp", ts.toXMLFormat());
            if(fields != null) {
                for (String bareField : fields.split(",")) {
                    String field = bareField.trim();
                    if ("lastLogin".equals(field)) {
                        if (tolvenUser.getLastLogin()!=null) {
                            GregorianCalendar lastLogin = new GregorianCalendar();
                            lastLogin.setTime(tolvenUser.getLastLogin());
                            XMLGregorianCalendar lastLoginXML = xmlFactory.newXMLGregorianCalendar(lastLogin);
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(lastLoginXML.toXMLFormat());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("creation".equals(field)){
                        if (tolvenUser.getCreation()!=null) {
                            GregorianCalendar creation = new GregorianCalendar();
                            creation.setTime(tolvenUser.getCreation());
                            XMLGregorianCalendar creationXML = xmlFactory.newXMLGregorianCalendar(creation);
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(creationXML.toXMLFormat());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("emailFormat".equals(field)){
                        if (tolvenUser.getEmailFormat()!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(tolvenUser.getEmailFormat().toString());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("locale".equals(field)){
                        if (tolvenUser.getLocale()!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(tolvenUser.getLocale());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("primaryMail".equals(field)){
                        /*
                        if (tp.getPrimaryMail()!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(tp.getPrimaryMail());
                            xmlStreamWriter.writeEndElement();
                        }
                        */
                    } else if ("timezone".equals(field)){
                        if (tolvenUser.getTimeZone()!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(tolvenUser.getTimeZone());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("appEncryptionActive".equals(field)) {
                        xmlStreamWriter.writeStartElement(field);
                        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
                        String appEncryptionActiveValue = (String) sessionWrapper.getAttribute("appEncryptionActive");
                        boolean appEncryptionActive = Boolean.TRUE.toString().equals(appEncryptionActiveValue);
                        xmlStreamWriter.writeCharacters(String.valueOf(appEncryptionActive));
                        xmlStreamWriter.writeEndElement();
                    } else if ("hasUserPKCS12".equals(field)) {
                        xmlStreamWriter.writeStartElement(field);
                        @SuppressWarnings("rawtypes")
                        Set userPKCS12Set = (Set) request.getAttribute("userPKCS12");
                        boolean hasUserPKCS12 = userPKCS12Set != null && !userPKCS12Set.isEmpty();
                        xmlStreamWriter.writeCharacters(String.valueOf(hasUserPKCS12));
                        xmlStreamWriter.writeEndElement();
                    } else if ("userPKCS12Active".equals(field)) {
                        xmlStreamWriter.writeStartElement(field);
                        String keyAlgorithm = getPropertyBean().getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
                        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
                        boolean encryptionActive = sessionWrapper.getUserPrivateKey(keyAlgorithm) != null;
                        xmlStreamWriter.writeCharacters(String.valueOf(encryptionActive));
                        xmlStreamWriter.writeEndElement();
                    } else {
                        /*
                        Object value = tp.getAttributeValue(field);
                        if (value!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(value.toString());
                            xmlStreamWriter.writeEndElement();
                        }
                        */
                    }
                    
                }
            }
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeEndDocument();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN_TYPE).entity("Exception writing metadata\n" + ExceptionFormatter.toSimpleString(e,"\n")).build();
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
    
    /**
     * Get details for a user. If the username is not the logged in user, then
     * the user must have tolvenAdmin permission.
     * @return
     */
    @Path("details/{username}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getUserDetailsXML(
            @Context SecurityContext sc, 
            @PathParam("username") String username,
            @QueryParam("fields") String fields) throws Exception{
        return prepareUserDetails(username, fields);
    }
    
    /**
     * Get a list of accounts that the current user is allowed to select
     * @return
     */
    @Path("details")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getUserDetailsXML(
            @Context SecurityContext sc,
            @QueryParam("fields") String fields) throws Exception{
        Principal principal = request.getUserPrincipal();
        return prepareUserDetails(principal.getName(), fields);
    }

    /**
     * Get a count of the number of users known to the server. 
     * A user must be logged in and must have the tolvenAdmin permission.
     * 
     * @param pattern
     * @return
     */
    @Path("count")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getUserCount(@Context SecurityContext sc) {
        long count = getActivationBean().countUsers();
        return Response.ok(Long.toString(count)).build();
    }
    
    /**
     * Get general information about the current user
     * @return
     */
    @Path("info")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getUserInfo() {
        HttpSession session = request.getSession();
        StringBuffer sb = new StringBuffer();
        sb.append("Session Attributes ");
        sb.append("(Principal=");
        sb.append(request.getUserPrincipal());
        sb.append(")");
        sb.append("\n");
        for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
            String attr = e.nextElement();
            sb.append("  ");
            sb.append(attr);
            sb.append("=");
            sb.append(session.getAttribute(attr));
            sb.append("\n");
        }
        return sb.toString();
    }
    
/*
    @Path("accountUsers")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response accountUsers(@QueryParam("accountUserId") String accountUserId) {
        List<AccountUser> accountUsers = new ArrayList<AccountUser>();
        TolvenUser user = getActivationBean().findUser(request.getUserPrincipal().getName());
        if (accountUserId == null || accountUserId.length() == 0) {
            accountUsers.addAll(getActivationBean().findUserAccounts(user));
        } else {
            AccountUser accountUser = getActivationBean().findAccountUser(Long.parseLong(accountUserId));
            if (accountUser.getUser().getId() != user.getId()) {
                return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("AccountUser: " + accountUser.getUser().getId() + " does not belong to TolvenUser: " + user.getId()).build();
            }
            accountUsers.add(accountUser);
        }
        XFacadeAccountUsers uas = XFacadeAccountUserFactory.createXFacadeAccountUsers(accountUsers, user, (Date) request.getAttribute("tolvenNow"));
        return Response.ok(uas).build();
    }

    @Path("accountUsers/{tolvenUserId}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response accountUsers(@QueryParam("accountUserId") String accountUserId, @PathParam("accountUserId") String tolvenUserId) {
        List<AccountUser> accountUsers = new ArrayList<AccountUser>();
        TolvenUser user = getActivationBean().findTolvenUser(Long.parseLong(tolvenUserId));
        if (accountUserId == null || accountUserId.length() == 0) {
            accountUsers.addAll(getActivationBean().findUserAccounts(user));
        } else {
            AccountUser accountUser = getActivationBean().findAccountUser(Long.parseLong(accountUserId));
            if (accountUser.getUser().getId() != user.getId()) {
                return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("AccountUser: " + accountUser.getUser().getId() + " does not belong to TolvenUser: " + user.getId()).build();
            }
            accountUsers.add(accountUser);
        }
        XFacadeAccountUsers uas = XFacadeAccountUserFactory.createXFacadeAccountUsers(accountUsers, user, (Date) request.getAttribute("tolvenNow"));
        return Response.ok(uas).build();
    }
*/
    private ActivationLocal getActivationBean() {
        if (activationBean == null) {
            String jndiName = "java:app/tolvenEJB/ActivationBean!org.tolven.core.ActivationLocal";
            try {
                InitialContext ctx = new InitialContext();
                activationBean = (ActivationLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return activationBean;
    }
    
    private TolvenPropertiesLocal getPropertyBean() {
  		if (propertyBean == null) {
            String jndiName = "java:app/tolvenEJB/TolvenProperties!org.tolven.core.TolvenPropertiesLocal";
            try {
                InitialContext ctx = new InitialContext();
                propertyBean = (TolvenPropertiesLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
  		return propertyBean;
  	}


}
