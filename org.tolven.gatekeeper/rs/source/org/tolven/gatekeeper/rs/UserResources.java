/*
 * Copyright (C) 2009 Tolven Inc

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
 * @author Joseph Isaac
 */
package org.tolven.gatekeeper.rs;

import java.net.URLEncoder;
import java.util.Arrays;

import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.tolven.exception.TolvenAuthenticationException;
import org.tolven.exception.TolvenSecurityException;
import org.tolven.gatekeeper.LdapLocal;
import org.tolven.naming.TolvenPerson;
import org.tolven.session.TolvenSessionWrapperFactory;

import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * A class to support RESTful API for managing users
 * 
 * @author Joseph Isaac
 *
 */
@Path("user")
@ManagedBean
public class UserResources {

    private static Logger logger = Logger.getLogger(UserResources.class);

    @EJB
    private LdapLocal ldapLocal;
    @Context
    private HttpServletRequest request;

    public UserResources() {
    }

    @Path("addRole")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addRole(
            @FormParam("role") String role,
            @FormParam("uid") String uid,
            @FormParam("realm") String realm,
            @FormParam("requestorPassword") String requestorPassword) {
        try {
            if (role == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("role parameter missing").build();
            }
            if (uid == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("uid parameter missing").build();
            }
            if (realm == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("realm parameter missing").build();
            }
            if (requestorPassword == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("requestorPassword parameter missing").build();
            }
            char[] password = null;
            password = requestorPassword.toCharArray();
            getLdapLocal().addRole(role, uid, realm, password);
            return Response.ok().build();
        } catch (Exception ex) {
            TolvenSecurityException gex = TolvenSecurityException.getRootGatekeeperException(ex);
            if (gex == null) {
                String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
                throw new RuntimeException("User: " + principal + " failed to create role: " + role, ex);
            } else {
                ex.printStackTrace();
                return Response.status(Status.UNAUTHORIZED).entity(gex.getFormattedMessage()).build();
            }
        }
    }

    @Path("createRole")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createRole(
            @FormParam("role") String role,
            @FormParam("requestorPassword") String requestorPassword) {
        try {
            if (role == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("role parameter missing").build();
            }
            if (requestorPassword == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("requestorPassword parameter missing").build();
            }
            char[] password = null;
            password = requestorPassword.toCharArray();
            getLdapLocal().createRole(role, password);
            return Response.ok().build();
        } catch (Exception ex) {
            TolvenSecurityException gex = TolvenSecurityException.getRootGatekeeperException(ex);
            if (gex == null) {
                String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
                throw new RuntimeException("User: " + principal + " failed to create role: " + role, ex);
            } else {
                ex.printStackTrace();
                return Response.status(Status.UNAUTHORIZED).entity(gex.getFormattedMessage()).build();
            }
        }
    }

    @Path("{userId}/user/{uid}")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createTolvenPerson(
            @PathParam("uid") String uid,
            @FormParam("uidPassword") String uidPassword,
            @FormParam("realm") String realm,
            @FormParam("givenName") String givenName,
            @FormParam("surname") String surname,
            @FormParam("organizationUnit") String organizationUnit,
            @FormParam("organization") String organization,
            @FormParam("stateOrProvince") String stateOrProvince,
            @FormParam("emails") String emails,
            @FormParam("userPKCS12") String base64UserPKCS12,
            @PathParam("userId") String userId,
            @FormParam("userIdPassword") String userIdPassword) {
        try {
            if (uid == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("uid parameter missing").build();
            }
            if (givenName == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("givenName parameter missing").build();
            }
            if (surname == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("surname parameter missing").build();
            }
            if (realm == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("realm parameter missing").build();
            }
            if (uidPassword == null) {
                logger.info("uidPassword is null so a password will be generated for user: " + uid);
            }
            if (userId == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("userId parameter missing").build();
            }
            if (userIdPassword == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("userIdPassword parameter missing").build();
            }
            if (organizationUnit == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("organizationUnit parameter missing").build();
            }
            if (organization == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("organization parameter missing").build();
            }
            if (stateOrProvince == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("stateOrProvince parameter missing").build();
            }
            if (emails == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("emails parameter missing").build();
            }
            TolvenPerson tolvenPerson = new TolvenPerson();
            tolvenPerson.setUid(uid);
            char[] userPassword = null;
            if (uidPassword != null) {
                userPassword = uidPassword.toCharArray();
            }
            tolvenPerson.setGivenName(givenName);
            tolvenPerson.setSn(surname);
            tolvenPerson.setCn(givenName + " " + surname);
            tolvenPerson.setOrganizationUnitName(organizationUnit);
            tolvenPerson.setOrganizationName(organization);
            tolvenPerson.setStateOrProvince(stateOrProvince);
            tolvenPerson.setMail(Arrays.asList(emails.split(",")));
            TolvenPerson existingTolvenPerson = getLdapLocal().findTolvenPerson(tolvenPerson.getUid(), realm, userId, userIdPassword.toCharArray());
            if (existingTolvenPerson != null) {
                return Response.status(Status.BAD_REQUEST).entity(existingTolvenPerson.getDn() + " already exists").build();
            }
            char[] generatedPassword = getLdapLocal().createTolvenPerson(tolvenPerson, uid, userPassword, realm, base64UserPKCS12, userId, userIdPassword.toCharArray());
            if(generatedPassword == null) {
                return Response.ok().build();
            } else {
                return Response.ok(new String(generatedPassword)).build();
            }
        } catch (Exception ex) {
            TolvenSecurityException gex = TolvenSecurityException.getRootGatekeeperException(ex);
            if (gex == null) {
                throw new RuntimeException("User: " + userId + " failed to create TolvenPerson: " + uid + " in realm: " + realm, ex);
            } else {
                ex.printStackTrace();
                return Response.status(Status.UNAUTHORIZED).entity(gex.getFormattedMessage()).build();
            }
        }
    }

    @Path("{userId}/user/{uid}/exists")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response existsTolvenPerson(
            @PathParam("uid") String uid,
            @FormParam("realm") String realm,
            @PathParam("userId") String userId,
            @FormParam("userIdPassword") String userIdPassword) {
        try {
            if (realm == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("realm parameter missing").build();
            }
            if (!request.getUserPrincipal().getName().equals(userId)) {
                return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("userId " + userId + " does not match resource path").build();
            }
            if (userIdPassword == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("userIdPassword parameter missing").build();
            }
            TolvenPerson tolvenPerson = getLdapLocal().findTolvenPerson(uid, realm, userId, userIdPassword.toCharArray());
            Boolean exists = tolvenPerson != null;
            return Response.ok(Boolean.toString(exists)).build();
        } catch (Exception ex) {
            TolvenSecurityException gex = TolvenSecurityException.getRootGatekeeperException(ex);
            if (gex == null) {
                throw new RuntimeException("User: " + userId + " failed to determine if TolvenPerson: " + uid + " exists in realm: " + realm, ex);
            } else {
                ex.printStackTrace();
                Status status = null;
                if(gex instanceof TolvenAuthenticationException) {
                    status = Status.UNAUTHORIZED;
                } else {
                    status = Status.FORBIDDEN;
                }
                return Response.status(status).entity(gex.getFormattedMessage()).build();
            }
        }
    }

    @Path("{userId}/user/{uid}")
    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public Response findTolvenPerson(
            @PathParam("uid") String uid,
            @QueryParam("realm") String realm,
            @PathParam("userId") String userId,
            @PathParam("userIdPassword") String userIdPassword,
            @QueryParam("attributes") String attributes) {
        try {
            if (realm == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("realm parameter missing").build();
            }
            if (attributes == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("attributes parameter missing").build();
            }
            if (!request.getUserPrincipal().getName().equals(userId)) {
                return Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity("userId " + userId + " does not match resource path").build();
            }
            if (userIdPassword == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("userIdPassword parameter missing").build();
            }
            TolvenPerson tolvenPerson = getLdapLocal().findTolvenPerson(uid, realm, userId, userIdPassword.toCharArray());
            if (tolvenPerson == null) {
                return Response.status(Status.NOT_FOUND).entity(uid + " does not exist").build();
            }
            MultivaluedMap<String, String> map = new MultivaluedMapImpl();
            for (String name : attributes.split(",")) {
                Object obj = tolvenPerson.getAttributeValue(name);
                if (obj != null) {
                    if (obj instanceof String) {
                        map.putSingle(name, (String) obj);
                    } else {
                        String base64Encoded = new String(Base64.encodeBase64((byte[]) obj), "UTF-8");
                        String urlEncoded = URLEncoder.encode(base64Encoded, "UTF-8");
                        map.putSingle(name, urlEncoded);
                    }
                }
            }
            return Response.ok(map).build();
        } catch (Exception ex) {
            TolvenSecurityException gex = TolvenSecurityException.getRootGatekeeperException(ex);
            if (gex == null) {
                throw new RuntimeException("User: " + userId + " failed to find TolvenPerson: " + uid + " in realm: " + realm, ex);
            } else {
                ex.printStackTrace();
                return Response.status(Status.UNAUTHORIZED).entity(gex.getFormattedMessage()).build();
            }
        }
    }

    protected LdapLocal getLdapLocal() {
        if (ldapLocal == null) {
            String jndiName = "java:app/gatekeeper-common-ejb/LdapBean!org.tolven.gatekeeper.LdapLocal";
            try {
                InitialContext ctx = new InitialContext();
                ldapLocal = (LdapLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return ldapLocal;
    }

    @Path("{userId}/user/{uid}/X509Certificate")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getUserX509Certificate (
            @PathParam("uid") String uid,
            @FormParam("realm") String realm,
            @PathParam("userId") String userId,
            @FormParam("userIdPassword") String userIdPassword) {
        try {
            if (uid == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("uid parameter missing").build();
            }
            if (realm == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("realm parameter missing").build();
            }
            if (userId == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("userId parameter missing").build();
            }
            if (userIdPassword == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("userIdPassword parameter missing").build();
            }
            byte[] x509CertificateBytes = getLdapLocal().getUserX509Certificate(uid, realm, userId, userIdPassword.toCharArray());
            if (x509CertificateBytes == null) {
                return Response.status(Status.NOT_FOUND).entity("X509Certificate for: " + uid + " does not exist").build();
            }
            String base64Encoded = new String(Base64.encodeBase64((byte[]) x509CertificateBytes), "UTF-8");
            String urlEncoded = URLEncoder.encode(base64Encoded, "UTF-8");
            return Response.ok(urlEncoded).build();
        } catch (Exception ex) {
            TolvenSecurityException gex = TolvenSecurityException.getRootGatekeeperException(ex);
            if (gex == null) {
                throw new RuntimeException("User: " + userId + " failed to create TolvenPerson: " + uid + " in realm: " + realm, ex);
            } else {
                ex.printStackTrace();
                return Response.status(Status.UNAUTHORIZED).entity(gex.getFormattedMessage()).build();
            }
        }
    }

    @Path("removeRole")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeRole(
            @FormParam("role") String role,
            @FormParam("uid") String uid,
            @FormParam("realm") String realm,
            @FormParam("requestorPassword") String requestorPassword) {
        try {
            if (role == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("role parameter missing").build();
            }
            if (uid == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("uid parameter missing").build();
            }
            if (realm == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("realm parameter missing").build();
            }
            if (requestorPassword == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("requestorPassword parameter missing").build();
            }
            char[] password = null;
            password = requestorPassword.toCharArray();
            getLdapLocal().removeRole(role, uid, realm, password);
            return Response.ok().build();
        } catch (Exception ex) {
            TolvenSecurityException gex = TolvenSecurityException.getRootGatekeeperException(ex);
            if (gex == null) {
                String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
                throw new RuntimeException("User: " + principal + " failed to create role: " + role, ex);
            } else {
                ex.printStackTrace();
                return Response.status(Status.UNAUTHORIZED).entity(gex.getFormattedMessage()).build();
            }
        }
    }

    @Path("{userId}/user/{uid}/verifyPassword")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response verifyPassword(
            @PathParam("uid") String uid,
            @FormParam("uidPassword") String uidPassword,
            @FormParam("realm") String realm,
            @PathParam("userId") String userId) {
        try {
            if (uid == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("uid parameter missing").build();
            }
            if (realm == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("realm parameter missing").build();
            }
            if (uidPassword == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("uidPassword parameter missing").build();
            }
            if (userId == null) {
                return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("userId parameter missing").build();
            }
            Boolean verified = getLdapLocal().verifyPassword(uid, uidPassword.toCharArray(), realm);
            return Response.ok(Boolean.toString(verified)).build();
        } catch (Exception ex) {
            TolvenSecurityException gex = TolvenSecurityException.getRootGatekeeperException(ex);
            if (gex == null) {
                throw new RuntimeException("User: " + userId + " failed to verify password for user: " + uid + " in realm: " + realm, ex);
            } else {
                ex.printStackTrace();
                Status status = null;
                if(gex instanceof TolvenAuthenticationException) {
                    status = Status.UNAUTHORIZED;
                } else {
                    status = Status.FORBIDDEN;
                }
                return Response.status(status).entity(gex.getFormattedMessage()).build();
            }
        }
    }

}
