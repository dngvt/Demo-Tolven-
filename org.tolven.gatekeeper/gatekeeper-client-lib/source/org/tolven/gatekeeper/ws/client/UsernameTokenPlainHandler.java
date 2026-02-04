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
package org.tolven.gatekeeper.ws.client;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.tolven.exception.TolvenAuthenticationException;
import org.tolven.gatekeeper.client.api.GatekeeperClientLocal;
import org.tolven.naming.TolvenContext;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.w3c.dom.Node;

public class UsernameTokenPlainHandler implements SOAPHandler<SOAPMessageContext> {

    public static final String LOGOUT = "logout";
    public static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String WSSE11_NS = "http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd";

    @EJB
    private GatekeeperClientLocal gatekeeperClientBean;

    private SimpleCookie getCookieTemplate() {
        TolvenContext tolvenContext = null;
        String jndiName = "tolvenContext";
        try {
            InitialContext ictx = new InitialContext();
            tolvenContext = (TolvenContext) ictx.lookup(jndiName);
        } catch (Exception ex) {
            throw new RuntimeException("Could not look up " + jndiName, ex);
        }
        SimpleCookie cookie = new SimpleCookie(tolvenContext.getSsoCookieName());
        cookie.setDomain(tolvenContext.getSsoCookieDomain());
        cookie.setPath(tolvenContext.getSsoCookiePath());
        cookie.setSecure(Boolean.parseBoolean(tolvenContext.getSsoCookieSecure()));
        cookie.setMaxAge(Integer.parseInt(tolvenContext.getSsoCookieMaxAge()));
        return cookie;
    }

    @Override
    public void close(MessageContext mc) {
        QName operation = (QName) mc.get(MessageContext.WSDL_OPERATION);
        if (operation != null && LOGOUT.equals(operation.getLocalPart())) {
            gatekeeperClientBean.logout();
        }
    }

    private Node findLocalName(String localName, Node node) {
        Node n = node.getFirstChild();
        do {
            if (localName.equals(n.getLocalName())) {
                return n;
            }
            n = n.getNextSibling();
        } while (n != null);
        return null;
    }

    private Node findSecurity(SOAPHeader soapHeader) {
        Node node = soapHeader.getFirstChild();
        do {
            if ("Security".equals(node.getLocalName()) && WSSE_NS.equals(node.getNamespaceURI()) || WSSE11_NS.equals(node.getNamespaceURI())) {
                return node;
            }
            node = node.getNextSibling();
        } while (node != null);
        return null;
    }

    @Override
    public Set<QName> getHeaders() {
        HashSet<QName> headers = new HashSet<QName>();
        headers.add(new QName(WSSE_NS, "Security"));
        headers.add(new QName(WSSE11_NS, "Security"));
        return headers;
    }

    @Override
    public boolean handleFault(SOAPMessageContext smc) {
        return false;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        HttpServletRequest request = (HttpServletRequest) smc.get(MessageContext.SERVLET_REQUEST);
        HttpServletResponse response = (HttpServletResponse) smc.get(MessageContext.SERVLET_RESPONSE);
        Cookie templateCookie = getCookieTemplate();
        String extendedSessionId = templateCookie.readValue(request, response);
        Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outboundProperty.booleanValue()) {
            try {
                if (extendedSessionId == null) {
                    extendedSessionId = TolvenSessionWrapperFactory.getInstance().getExtendedSessionId();
                    templateCookie.setValue(extendedSessionId);
                    templateCookie.saveTo(request, response);
                }
                //smc.getMessage().writeTo(System.out);
                //System.out.println();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            try {
                if (extendedSessionId == null) {
                    String realm = null;
                    String host = request.getRemoteAddr();
                    SOAPEnvelope envelope = smc.getMessage().getSOAPPart().getEnvelope();
                    SOAPHeader soapHeader = envelope.getHeader();
                    if (soapHeader == null) {
                        throw new TolvenAuthenticationException("SOAPEnvelope contains no SOAPHeader", null, null, host);
                    }
                    Node securityHeader = findSecurity(soapHeader);
                    if (securityHeader == null) {
                        throw new TolvenAuthenticationException("SOAPHeader contains no Security header", null, null, host);
                    }
                    Node usernameTokenNode = findLocalName("UsernameToken", securityHeader);
                    if (usernameTokenNode == null) {
                        throw new TolvenAuthenticationException("Security header contains no UsernameToken", null, null, host);
                    }
                    Node usernameNode = findLocalName("Username", usernameTokenNode);
                    String username = usernameNode == null ? null : usernameNode.getTextContent();
                    Node passwordNode = findLocalName("Password", usernameTokenNode);
                    String password = passwordNode == null ? null : passwordNode.getTextContent();
                    if (username == null || password == null) {
                        throw new TolvenAuthenticationException("Authentication failed", null, null, host);
                    }
                    //TODO The realm needs to be obtained from the WS client
                    realm = "tolven";
                    extendedSessionId = gatekeeperClientBean.login(username, password.toCharArray(), realm, true);
                }
                TolvenSessionWrapperFactory.getInstance().touch();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            //smc.getMessage().writeTo(System.out);
            //System.out.println();

        }
        return true;
    }

}
