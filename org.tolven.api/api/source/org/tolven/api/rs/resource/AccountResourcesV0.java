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
 */

package org.tolven.api.rs.resource;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64;
import org.tolven.api.security.GeneralSecurityFilter;
import org.tolven.app.MenuLocal;
import org.tolven.core.AccountDAOLocal;
import org.tolven.core.ActivationLocal;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountType;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.TolvenUser;
import org.tolven.security.LoginLocal;
import org.tolven.security.TolvenPerson;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.util.ExceptionFormatter;

@Path("account")
public class AccountResourcesV0 {

    @EJB
    private ActivationLocal activationBean;

    @EJB
    private AccountDAOLocal accountBean;

    @EJB
    private MenuLocal menuBean;

    @EJB
    private TolvenPropertiesLocal propertyBean;
    
    @EJB
    private LoginLocal loginBean;

    @Context
    private HttpServletRequest request;

    /**
     * Get general information about the current account for the current user
     * @return
     */
    @Path("info")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getInfo() {
        HttpSession session = request.getSession();
        StringBuffer sb = new StringBuffer();
        sb.append("Session Attributes:\n");
        for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
            String attr = e.nextElement();
            sb.append("  ");
            sb.append(attr);
            sb.append("=");
            sb.append(session.getAttribute(attr));
            sb.append("\n");
        }
        Long accountUserId = (Long) session.getAttribute("accountUserId");
        if (accountUserId!=null) {
            sb.append("\nUser Attributes:\n");
            AccountUser accountUser = activationBean.findAccountUser(accountUserId);
            TolvenPerson tp = getTolvenPerson();
            sb.append("  UID=");sb.append(tp.getUid());sb.append("\n");
            sb.append("  GivenName=");sb.append(tp.getGivenName());sb.append("\n");
            sb.append("  SN=");sb.append(tp.getSn());sb.append("\n");
            try {
                sb.append("  email=");sb.append(tp.getPrimaryMail());sb.append("\n");
            } catch (Exception e) {
                sb.append("Exception: " + e.getMessage());
            }
            sb.append("\nAccount Attributes:\n");
            Account account = accountUser.getAccount();
            sb.append("  accountId=");sb.append(account.getId());sb.append("\n");
            sb.append("  Title=");sb.append(account.getTitle());sb.append("\n");
            {
                Account accountTemplate = account.getAccountTemplate();
                if (accountTemplate!=null) {
                    sb.append("  templateAccountId=");sb.append(accountTemplate.getId());sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
    
    @SuppressWarnings("unchecked")
	private TolvenPerson getTolvenPerson() {
        TolvenPerson tp = new TolvenPerson();
        Set<String> cnSet = (Set<String>) request.getAttribute("cn");
        if (cnSet != null && !cnSet.isEmpty()) {
            tp.setCn(cnSet.iterator().next());
        }
        Set<String> dnSet = (Set<String>) request.getAttribute("dn");
        if (dnSet != null && !dnSet.isEmpty()) {
            tp.setDn(dnSet.iterator().next());
        }
        Set<String> givenNameSet = (Set<String>) request.getAttribute("givenName");
        if (givenNameSet != null && !givenNameSet.isEmpty()) {
            tp.setGivenName(givenNameSet.iterator().next());
        }
        Set<String> oSet = (Set<String>) request.getAttribute("o");
        if (oSet != null && !oSet.isEmpty()) {
            tp.setOrganizationName(oSet.iterator().next());
        }
        Set<String> ouSet = (Set<String>) request.getAttribute("ou");
        if (ouSet != null && !ouSet.isEmpty()) {
            tp.setOrganizationUnitName(ouSet.iterator().next());
        }
        Set<String> snSet = (Set<String>) request.getAttribute("sn");
        if (snSet != null && !snSet.isEmpty()) {
            tp.setSn(snSet.iterator().next());
        }
        Set<String> stSet = (Set<String>) request.getAttribute("st");
        if (stSet != null && !stSet.isEmpty()) {
            tp.setStateOrProvince(stSet.iterator().next());
        }
        Set<String> uidSet = (Set<String>) request.getAttribute("uid");
        if (uidSet != null && !uidSet.isEmpty()) {
            tp.setUid(uidSet.iterator().next());
        }
        Set<String> mailSet = (Set<String>) request.getAttribute("dn");
        if (mailSet != null) {
            tp.setMail(new ArrayList<String>(mailSet));
        }
        return tp;
    }

    /**
     * Create a new Tolven account
     * Parameters include attributes of account object.
     * Parameters can also include account properties.
     * The initialUser must be specified. This user will have the account adminitrator permission
     * and will be the user capable of "inviting" other users to the account.
     * The response status code will be "created" if the account was created.
     * The location url will contain the account id.
     */
    @Path("create")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Deprecated
	public Response createAccount(
			@FormParam("accountType") String accountTypeName,
			@FormParam("initialUser") String initialUserString,
			@FormParam("title") String title,
			@FormParam("timezone") String timezone,
			@FormParam("locale") String localeName,
			@DefaultValue("html") @FormParam("emailFormat") String emailFormat,
			@DefaultValue("true" ) @FormParam("enableBackButton") String enableBackButton,
			@DefaultValue("false" )@FormParam("disableAutoRefresh") String disableAutoRefresh,
			@DefaultValue("false" )@FormParam("manualMetadataUpdate") String manualMetadataUpdate,
			@FormParam("property") String property,
			@Context SecurityContext sc,
			MultivaluedMap<String, String> formParams) {
        AccountType accountType = accountBean.findAccountTypebyKnownType(accountTypeName);
        if (accountType == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Account Type not provided").build();
        }
        if (!accountType.isCreatable()) {
            return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("Account Type is not creatable").build();
        }
        if (initialUserString == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Missing Initial User").build();
        }
        TolvenUser initialUser = activationBean.findUser(initialUserString);
        if (initialUser == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Invalid Initial User").build();
        }
        Account account = null;
        try {
            account = accountBean.createAccount2(title, timezone, accountType);
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ExceptionFormatter.toSimpleString(e, "\\n")).build();
        }
        account.setAccountType(accountType);
        account.setEmailFormat(emailFormat);
        if (localeName != null) {
            Locale[] availableLocales = Locale.getAvailableLocales();
            Locale foundLocale = null;
            for (Locale locale : availableLocales) {
                if (localeName.equalsIgnoreCase(locale.getDisplayName())) {
                    foundLocale = locale;
                    break;
                }
            }
            account.setLocale(foundLocale.getDisplayName());
        }
        account.setEnableBackButton(enableBackButton.equalsIgnoreCase("true"));
        account.setDisableAutoRefresh(disableAutoRefresh.equalsIgnoreCase("true"));
        accountBean.setManualMetadataUpdate(manualMetadataUpdate.equalsIgnoreCase("true"), account);
        // Add the initial user to the account
        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
        PublicKey userPublicKey = sessionWrapper.getUserPublicKey();
        accountBean.addAccountUser(account, initialUser, new Date(), true, userPublicKey);
        menuBean.updateMenuStructure(account);
        URI uri = null;
        try {
            uri = new URI(URLEncoder.encode(Long.toString(account.getId()), "UTF-8"));
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ExceptionFormatter.toSimpleString(e, "\\n")).build();
        }
        return Response.created(uri).type(MediaType.TEXT_PLAIN).entity(String.valueOf(account.getId())).build();
    }

    @Path("default")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response setDefaultAccount(@FormParam("defaulted") String defaulted) {
        AccountUser accountUser = (AccountUser) request.getAttribute(GeneralSecurityFilter.ACCOUNTUSER);
        if("true".equals(defaulted)) {
            activationBean.setDefaultAccountUser(accountUser);
        } else {
             accountUser.setDefaultAccount(false);
        }
        return Response.ok().type(MediaType.TEXT_PLAIN).build();
    }

    /**
     * Create or update Account Properties
     * @return
     */
    @Path("properties/set")
    @POST
    @Produces
    public Response putAccountProperties(MultivaluedMap<String, String> mvMap) {
        try {
            if (mvMap == null) {
                return Response.status(400).type(MediaType.TEXT_PLAIN).entity("No properties supplied").build();
            }
            AccountUser accountUser = (AccountUser) request.getAttribute(GeneralSecurityFilter.ACCOUNTUSER);
            if (accountUser == null) {
                throw new RuntimeException("No accountUser found");
            }
            Properties properties = new Properties();
            for (String name : mvMap.keySet()) {
                properties.setProperty(name, mvMap.getFirst(name));
            }
            accountBean.putAccountProperties(accountUser.getAccount().getId(), properties);
            return Response.ok().build();
        } catch (Exception ex) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(ExceptionFormatter.toSimpleString(ex, "\\n")).build();
        }
    }
    
    @Path("details")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getUserDetailsXML(
            @Context SecurityContext sc,
            @QueryParam("fields") String fields) throws Exception{
        Account account = TolvenRequest.getInstance().getAccount();
        //title,accountType,timeZone,locale,emailFormat,disableAutoRefresh,manualMetadataUpdate,accountPublicKey
        //return prepareAccountDetails(principal.getName(), fields);
        if (account == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN_TYPE).entity("Tolven Account not found").build();
        }        
        return prepareAccountDetails(account, fields);
    }
    
    protected Response prepareAccountDetails( Account account, String fields ) throws Exception {
    	
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
            xmlStreamWriter.writeStartElement("account");
            xmlStreamWriter.writeAttribute("id", String.valueOf(account.getId()));
            if(fields != null) {
                for (String bareField : fields.split(",")) {
                    String field = bareField.trim();
                    if ("title".equals(field)) {
                        if (account.getTitle()!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(account.getTitle());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("accountType".equals(field)){
                        if (account.getAccountType()!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(account.getAccountType().toString());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("timezone".equals(field)){
                        if (account.getTimeZone()!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(account.getTimeZone());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("locale".equals(field)){
                        if (account.getLocale()!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(account.getLocale());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("emailFormat".equals(field)){
                        if (account.getEmailFormat()!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(account.getEmailFormat());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("disableAutoRefresh".equals(field)){
                        if (account.getDisableAutoRefresh()!=null) {
                            xmlStreamWriter.writeStartElement(field);
                            xmlStreamWriter.writeCharacters(account.getDisableAutoRefresh().toString());
                            xmlStreamWriter.writeEndElement();
                        }
                    } else if ("manualMetadataUpdate".equals(field)) {
                    	 if (account.getManualMetadataUpdate()!=null) {
                             xmlStreamWriter.writeStartElement(field);
                             xmlStreamWriter.writeCharacters(account.getManualMetadataUpdate().toString());
                             xmlStreamWriter.writeEndElement();
                         }                       
                    } else if ("accountPublicKey".equals(field)) {
                    	 if (account.getAccountPublicKey()!=null) {
                             xmlStreamWriter.writeStartElement(field);
                             xmlStreamWriter.writeCharacters(account.getAccountPublicKey().toString());
                             xmlStreamWriter.writeEndElement();
                         }                        
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
    

}
