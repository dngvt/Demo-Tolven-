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
package org.tolven.gatekeeper.client.bean;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Base64;
import org.tolven.exception.TolvenAuthenticationException;
import org.tolven.exception.TolvenAuthorizationException;
import org.tolven.gatekeeper.client.api.RSGatekeeperClientLocal;
import org.tolven.naming.TolvenContext;
import org.tolven.naming.WebContext;
import org.tolven.session.TolvenSessionWrapperFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

@Stateless
@Local(RSGatekeeperClientLocal.class)
public class RSGatekeeperClientBean implements RSGatekeeperClientLocal {

    private static CertificateFactory certificateFactory;
    private static Client client;
    static {
        ClientConfig config = new DefaultClientConfig();
        client = Client.create(config);
        client.setFollowRedirects(true);
    }

    private static TolvenContext tolvenContext;

    @Override
    public boolean existsTolvenPerson(String uid, String realm, String userId, char[] userIdPassword) {
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        WebResource webResource = getWebResource("user/" + principal + "/user/" + uid + "/exists");
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("uid", uid);
        formData.add("realm", realm);
        formData.add("userId", userId);
        formData.add("userIdPassword", new String(userIdPassword));
        ClientResponse response = webResource.post(ClientResponse.class, formData);
        String responseString = response.getEntity(String.class);
        if (response.getStatus() == 401) {
            throw new TolvenAuthenticationException(responseString);
        } else if (response.getStatus() == 403) {
            throw new TolvenAuthorizationException(responseString);
        } else if (response.getStatus() != 200) {
            throw new RuntimeException("Error: " + response.getStatus() + " " + uid + " " + webResource.getURI() + " " + responseString);
        }
        return Boolean.parseBoolean(responseString);
    }

    private CertificateFactory getCertificateFactory() {
        if (certificateFactory == null) {
            try {
                certificateFactory = CertificateFactory.getInstance("X509");
            } catch (CertificateException ex) {
                throw new RuntimeException("Could not get instance of CertificateFactory", ex);
            }
        }
        return certificateFactory;
    }

    private Client getClient() {
        return client;
    }

    private String getCookie(ClientResponse response) {
        String ssoCookieName = getTolvenContext().getSsoCookieName();
        for (Cookie cookie : response.getCookies()) {
            if (ssoCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new RuntimeException("Cookie " + ssoCookieName + " not found");
    }

    @Override
    public List<String> getRealmIds() {
        return getTolvenContext().getRealmIds();
    }

    private WebContext getRsWebContext() {
        return (WebContext) getTolvenContext().getRsGatekeeperWebContext();
    }

    private Cookie getSessionCookie() {
        TolvenContext context = getTolvenContext();
        Cookie cookie = new Cookie(context.getSsoCookieName(), TolvenSessionWrapperFactory.getInstance().getExtendedSessionId(), context.getSsoCookiePath(), context.getSsoCookieDomain());
        return cookie;
    }

    private TolvenContext getTolvenContext() {
        if (tolvenContext == null) {
            String jndiName = "tolvenContext";
            try {
                InitialContext ictx = new InitialContext();
                tolvenContext = (TolvenContext) ictx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not look up " + jndiName, ex);
            }
        }
        return tolvenContext;
    }

    @Override
    public X509Certificate getUserX509Certificate(String uid, String realm, String requestorId, char[] requestorPassword) {
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        WebResource webResource = getWebResource("user/" + requestorId + "/user/" + uid + "/X509Certificate");
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("uid", uid);
        formData.add("realm", realm);
        formData.add("userId", requestorId);
        formData.add("userIdPassword", new String(requestorPassword));
        ClientResponse response = webResource.post(ClientResponse.class, formData);
        if (response.getStatus() != 200) {
            throw new RuntimeException("Error: " + response.getStatus() + " " + principal + " " + webResource.getURI() + " " + response.getEntity(String.class));
        }
        String encodedUserCertificate = response.getEntity(String.class);
        byte[] userX509CertificateBytes = null;
        if (encodedUserCertificate != null) {
            try {
                String urlDecodedUserCertificate = URLDecoder.decode(encodedUserCertificate, "UTF-8");
                userX509CertificateBytes = Base64.decodeBase64(urlDecodedUserCertificate.getBytes("UTF-8"));
            } catch (Exception ex) {
                throw new RuntimeException("Could not convert userCertificate to bytes using UTF-8", ex);
            }
        }
        if (userX509CertificateBytes == null) {
            return null;
        } else {
            try {
                return (X509Certificate) getCertificateFactory().generateCertificate(new ByteArrayInputStream(userX509CertificateBytes));
            } catch (Exception ex) {
                throw new RuntimeException("Could not get X509Certificate from SSO userCertificate", ex);
            }
        }
    }

    /**
     * Callers of this method are required to have previously authenticated with the gatekeeper
     */
    private WebResource getWebResource(String path) {
        /*
         * No new session, so add the existing SSOCookie to the WebResource request
         */
        return getWebResource(path, false);
    }

    private WebResource getWebResource(String path, boolean newSession) {
        WebContext webContext = getRsWebContext();
        WebResource webResource = getClient().resource(webContext.getRsInterface()).path(path);
        if (newSession) {
            /*
             * Current SSOCookie is not added to the WebResource
             */
        } else {
            /*
             * Add the current SSOCookie to the WebResource.
             */
            webResource.addFilter(new ClientFilter() {
                @Override
                public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
                    Cookie sessionCookie = getSessionCookie();
                    clientRequest.getHeaders().putSingle("Cookie", sessionCookie.getName() + "=" + sessionCookie.getValue());
                    return getNext().handle(clientRequest);
                }
            });
        }
        return webResource;
    }

    /**
     * User RESTful to log into the gatekeeper and return the extended session Id
     * @param username
     * @param password
     * @param realm
     * @return
     */
    @Override
    public String login(String username, char[] password, String realm) {
        return login(username, password, realm, false);
    }

    /**
     * User RESTful to log into the gatekeeper and return the extended session Id
     * When newSession is false, the existing session will be used, otherwise a new one
     * will be created and returned by the gatekeeper.
     */
    @Override
    public String login(String username, char[] password, String realm, boolean newSession) {
        WebContext webContext = getRsWebContext();
        /*
         * Do not attempt to add SSOCookie, because this is a request for a new login
         */
        WebResource webResource = getWebResource(webContext.getRsLoginPath(), newSession);
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("username", username);
        formData.add("password", new String(password));
        formData.add("realm", realm);
        ClientResponse response = webResource.post(ClientResponse.class, formData);
        if (response.getStatus() == 401) {
            throw new TolvenAuthenticationException(response.getEntity(String.class));
        } else if (response.getStatus() == 403) {
            throw new TolvenAuthorizationException(response.getEntity(String.class));
        } else if (response.getStatus() != 200) {
            throw new RuntimeException("Error: " + response.getStatus() + " " + username + " " + webResource.getURI() + " " + response.getEntity(String.class));
        }
        return getCookie(response);
    }

    /**
     * Logout the current Subject
     */
    @Override
    public void logout() {
        TolvenSessionWrapperFactory.getInstance().logout();
    }

    @Override
    public boolean verifyUserPassword(String uid, char[] password, String realm) {
        String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        WebResource webResource = getWebResource("user/" + principal + "/user/" + uid + "/verifyPassword");
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("userId", principal);
        formData.add("uid", uid);
        formData.add("uidPassword", new String(password));
        formData.add("realm", realm);
        ClientResponse response = webResource.post(ClientResponse.class, formData);
        String responseString = response.getEntity(String.class);
        if (response.getStatus() == 401) {
            throw new TolvenAuthenticationException(responseString);
        } else if (response.getStatus() == 403) {
            throw new TolvenAuthorizationException(responseString);
        } else if (response.getStatus() != 200) {
            throw new RuntimeException("Error: " + response.getStatus() + " " + uid + " " + webResource.getURI() + " " + responseString);
        }
        return Boolean.parseBoolean(responseString);
    }

}
