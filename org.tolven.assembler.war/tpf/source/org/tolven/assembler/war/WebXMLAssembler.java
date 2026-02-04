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
 * @version $Id: WebXMLAssembler.java 6942 2012-08-25 23:15:05Z joe.isaac $
 */
package org.tolven.assembler.war;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.tolven.plugin.TolvenCommandPlugin;

/**
 * This plugin assemblers a web.xml for a war file.
 * 
 * @author Joseph Isaac
 *
 */
public class WebXMLAssembler {

    public static final String ATTRIBUTE_TEMPLATE_WEBXML = "template-webxml";
    public static final String CMD_CONTEXT_ID_OPTION = "contextId";
    public static final String CMD_LINE_DESTDIR_OPTION = "destDir";
    public static final String CMD_LINE_WAR_PLUGIN_OPTION = "warPlugin";
    public static final String EXNPT_ABSTRACT_WAR = "abstractWAR";
    public static final String EXNPT_CONTEXT_ID = "context-id";
    public static final String EXNPT_CTX_PARAM_ADPTR = "context-param-adaptor";
    public static final String EXNPT_FILTER_ADPTR = "filter-adaptor";
    public static final String EXNPT_FILTER_MAPPING_CONTRIBUTION_ADPTR = "filter-mapping-contribution-adaptor";
    public static final String EXNPT_SERVLET_ADPTR = "servlet-adaptor";
    public static final String EXNPT_SERVLET_MAPPING_CONTRIBUTION_ADPTR = "servlet-mapping-contribution-adaptor";

    private Logger logger = Logger.getLogger(WebXMLAssembler.class);
    private TolvenCommandPlugin tolvenCommandPlugin;

    public WebXMLAssembler(TolvenCommandPlugin tolvenCommandPlugin) {
        setTolvenCommandPlugin(tolvenCommandPlugin);
    }

    protected void addContextParameterCallTemplate(String paramName, String paramValue, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:context-param[tp:param-name = '" + paramName + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "context-param");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:with-param");
        xmlStreamWriter.writeAttribute("name", "param-name");
        xmlStreamWriter.writeCharacters(paramName);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:with-param");
        xmlStreamWriter.writeAttribute("name", "param-value");
        xmlStreamWriter.writeCharacters(paramValue);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addContextParameterCallTemplates(PluginDescriptor pd, String contextId, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addContextParameters");
        xmlStreamWriter.writeCharacters("\n");
        List<Extension> contextParams = new ArrayList<Extension>();
        contextParams.addAll(getTolvenCommandPlugin().getExtensions(pd, "context-param"));
        ExtensionPoint adaptorExnPt = pd.getExtensionPoint(EXNPT_CTX_PARAM_ADPTR);
        if (adaptorExnPt != null) {
            for (Extension exn : adaptorExnPt.getConnectedExtensions()) {
                Parameter parentContextIdParam = exn.getParameter("parent-context-id");
                if (parentContextIdParam == null || contextId.equals(parentContextIdParam.valueAsString())) {
                    contextParams.add(exn);
                }
            }
        }
        Comparator<Object> comparator = new Comparator<Object>() {
            public int compare(Object obj1, Object obj2) {
                Extension e1 = (Extension) obj1;
                Extension e2 = (Extension) obj2;
                return e1.getParameter("param-name").valueAsString().compareTo(e2.getParameter("param-name").valueAsString());
            };
        };
        Collections.sort(contextParams, comparator);
        for (Extension exn : contextParams) {
            String paramValue = getTolvenCommandPlugin().evaluate(exn.getParameter("param-value").valueAsString(), pd);
            if(paramValue == null) {
                throw new RuntimeException("context-param value is null: " + exn.getUniqueId());
            }
            addContextParameterCallTemplate(exn.getParameter("param-name").valueAsString(), paramValue, xmlStreamWriter);
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        List<Extension> relevantExns = new ArrayList<Extension>();
        for (Extension exn : getTolvenCommandPlugin().getExtensions(pd, "taglib")) {
            if (pd.getId().equals(exn.getParameter("target-plugin-id").valueAsString())) {
                relevantExns.add(exn);
            }
        }
        boolean semicolonSeparator = false;
        if (relevantExns.isEmpty()) {
            addInitTagLibContextParameterCallTemplate(null, xmlStreamWriter);
        } else {
            for (int i = 0; i < relevantExns.size(); i++) {
                Extension exn = relevantExns.get(i);
                String tagLib = "/META-INF/tags/" + exn.getParameter("tag-filename").valueAsString();
                String templateName = tagLib.replace("/", "");
                if (i == 0) {
                    addInitTagLibContextParameterCallTemplate(templateName, xmlStreamWriter);
                }
                if (i > 0) {
                    semicolonSeparator = true;
                }
                if (i == relevantExns.size() - 1) {
                    addTagLibContextParameterCallTemplate(tagLib, semicolonSeparator, templateName, "context-param", xmlStreamWriter);
                } else {
                    String tagLib2 = "/META-INF/tags/" + relevantExns.get(i + 1).getParameter("tag-filename").valueAsString();
                    String templateName2 = tagLib2.replace("/", "");
                    addTagLibContextParameterCallTemplate(tagLib, semicolonSeparator, templateName, templateName2, xmlStreamWriter);
                }
            }
        }
    }

    protected void addContextParameterTemplate(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "context-param");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:param");
        xmlStreamWriter.writeAttribute("name", "param-name");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:param");
        xmlStreamWriter.writeAttribute("name", "param-value");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("context-param");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("param-name");
        xmlStreamWriter.writeStartElement("xsl:value-of");
        xmlStreamWriter.writeAttribute("select", "$param-name");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("param-value");
        xmlStreamWriter.writeStartElement("xsl:value-of");
        xmlStreamWriter.writeAttribute("select", "$param-value");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addEJBLocalRef(String ejbRefName, String ejbRefType, String localHome, String local, String ejbLink, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:ejb-local-ref[tp:ejb-ref-name = '" + ejbRefName + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("ejb-local-ref");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("ejb-ref-name");
        xmlStreamWriter.writeCharacters(ejbRefName);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        if (ejbRefType != null) {
            xmlStreamWriter.writeStartElement("ejb-ref-type");
            xmlStreamWriter.writeCharacters(ejbRefType);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        if (localHome != null) {
            xmlStreamWriter.writeStartElement("local-home");
            xmlStreamWriter.writeCharacters(localHome);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        if (local != null) {
            xmlStreamWriter.writeStartElement("local");
            xmlStreamWriter.writeCharacters(local);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        if (ejbLink != null) {
            xmlStreamWriter.writeStartElement("ejb-link");
            xmlStreamWriter.writeCharacters(ejbLink);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addEJBLocalRefTemplates(PluginDescriptor pd, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addEJBLocalRefs");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addEnvEntry(String envEntryName, String envEntryType, String envEntryValue, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:env-entry[tp:env-entry-name = '" + envEntryName + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("env-entry");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("env-entry-name");
        xmlStreamWriter.writeCharacters(envEntryName);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("env-entry-type");
        xmlStreamWriter.writeCharacters(envEntryType);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("env-entry-value");
        xmlStreamWriter.writeCharacters(envEntryValue);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addEnvEntryTemplates(PluginDescriptor pd, String contextId, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addEnvEntrys");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addErrorPage(String errorCode, String exceptionType, String location, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:error-page[tp:error-code = '" + errorCode + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("error-page");
        xmlStreamWriter.writeCharacters("\n");
        if (errorCode.length() > 0) {
            xmlStreamWriter.writeStartElement("error-code");
            xmlStreamWriter.writeCharacters(errorCode);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        if (exceptionType.length() > 0) {
            xmlStreamWriter.writeStartElement("exception-type");
            xmlStreamWriter.writeCharacters(exceptionType);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        if (location.length() > 0) {
            xmlStreamWriter.writeStartElement("location");
            xmlStreamWriter.writeCharacters(location);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addErrorPageTemplates(PluginDescriptor pd, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addErrorPages");
        xmlStreamWriter.writeCharacters("\n");
        Map<String, Extension> errorPageMap = new HashMap<String, Extension>();
        for (Extension defaultExn : getErrorPageExtensions(pd, true)) {
            String errorCode = defaultExn.getParameter("error-code").valueAsString();
            errorPageMap.put(errorCode, defaultExn);
        }
        for (Extension nonDefaultExn : getErrorPageExtensions(pd, false)) {
            String errorCode = nonDefaultExn.getParameter("error-code").valueAsString();
            Extension errorPageExtension = errorPageMap.get(errorCode);
            if (errorPageExtension != null) {
                if (nonDefaultExn.getParameter("override") == null) {
                    throw new RuntimeException(nonDefaultExn.getUniqueId() + " requires an override parameter to override " + errorPageExtension.getUniqueId());
                }
                logger.debug(nonDefaultExn.getUniqueId() + " overrides " + errorPageExtension.getUniqueId());
            }
            errorPageMap.put(errorCode, nonDefaultExn);
        }
        for (Extension exn : errorPageMap.values()) {
            Parameter errorCode = exn.getParameter("error-code");
            String errorCodeString = "";
            if (errorCode != null) {
                errorCodeString = errorCode.valueAsString();
            }
            String exceptionTypeString = "";
            Parameter exceptionType = exn.getParameter("exception-type");
            if (exceptionType != null) {
                exceptionTypeString = exceptionType.valueAsString();
            }
            Parameter location = exn.getParameter("location");
            String locationString = "";
            if (location != null) {
                locationString = location.valueAsString();
            }
            addErrorPage(errorCodeString, exceptionTypeString, locationString, xmlStreamWriter);
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addFilter(Extension filterExtension, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        String filterName = filterExtension.getParameter("filter-name").valueAsString();
        String filterClass = filterExtension.getParameter("filter-class").valueAsString();
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:filter[tp:filter-name = '" + filterName + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("filter");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("filter-name");
        xmlStreamWriter.writeCharacters(filterName);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("filter-class");
        xmlStreamWriter.writeCharacters(filterClass);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        for (Parameter param : filterExtension.getParameters("init-param")) {
            xmlStreamWriter.writeStartElement("init-param");
            String paramName = param.getSubParameter("param-name").valueAsString();
            xmlStreamWriter.writeStartElement("param-name");
            xmlStreamWriter.writeCharacters(paramName);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
            String paramValue = param.getSubParameter("param-value").valueAsString();
            xmlStreamWriter.writeStartElement("param-value");
            xmlStreamWriter.writeCharacters(paramValue);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addFilterMapping(String filterName, String urlPattern, String[] dispatchers, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:filter-mapping[tp:filter-name = '" + filterName + "' and tp:url-pattern = '" + urlPattern + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("filter-mapping");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("filter-name");
        xmlStreamWriter.writeCharacters(filterName);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("url-pattern");
        xmlStreamWriter.writeCharacters(urlPattern);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        if (dispatchers != null) {
            for (String dispatcher : dispatchers) {
                xmlStreamWriter.writeStartElement("dispatcher");
                xmlStreamWriter.writeCharacters(dispatcher);
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeCharacters("\n");
            }
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addFilterTemplates(PluginDescriptor pd, String contextId, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addFilters");
        xmlStreamWriter.writeCharacters("\n");
        List<Extension> filterExns = getFilterExtensions(pd, contextId);
        Map<String, Map<String, Object>> filterMappings = getFilterMappings(pd, contextId, filterExns);
        for (Extension exn : filterExns) {
            String filterName = exn.getParameter("filter-name").valueAsString();
            addFilter(exn, xmlStreamWriter);
            Set<String> urlPatterns = new HashSet<String>();
            String[] dispatchers = null;
            Map<String, Object> map = filterMappings.get(filterName);
            List<Extension> mappingExns = (List<Extension>) map.get("mappings");
            for (Extension mappingExn : mappingExns) {
                for (Parameter param : mappingExn.getParameters("url-pattern")) {
                    urlPatterns.add(param.valueAsString());
                }
                Parameter dispatcherParam = mappingExn.getParameter("dispatchers");
                if (dispatcherParam != null && dispatcherParam.valueAsString().length() > 0) {
                    dispatchers = dispatcherParam.valueAsString().split(",");
                }
            }
            List<String> sortedURLPatterns = new ArrayList<String>(urlPatterns);
            Collections.sort(sortedURLPatterns);
            for (String urlPattern : sortedURLPatterns) {
                addFilterMapping(filterName, urlPattern, dispatchers, xmlStreamWriter);
            }
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addInitTagLibContextParameterCallTemplate(String initialTagTemplate, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "initTagContextParameter");
        xmlStreamWriter.writeCharacters("\n");
        if (initialTagTemplate != null) {
            xmlStreamWriter.writeStartElement("xsl:call-template");
            xmlStreamWriter.writeAttribute("name", initialTagTemplate);
            xmlStreamWriter.writeCharacters("\n");
            xmlStreamWriter.writeStartElement("xsl:with-param");
            xmlStreamWriter.writeAttribute("name", "param-name");
            xmlStreamWriter.writeCharacters("javax.faces.FACELETS_LIBRARIES");
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
            xmlStreamWriter.writeStartElement("xsl:with-param");
            xmlStreamWriter.writeAttribute("name", "param-value");
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addListener(String listenerClass, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:listener[tp:listener-class = '" + listenerClass + "']) = 0");
        xmlStreamWriter.writeStartElement("listener");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("listener-class");
        xmlStreamWriter.writeCharacters(listenerClass);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addListenerTemplates(PluginDescriptor pd, String contextId, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addListeners");
        xmlStreamWriter.writeCharacters("\n");
        List<Extension> listeners = new ArrayList<Extension>();
        listeners.addAll(getTolvenCommandPlugin().getExtensions(pd, "listener"));
        Comparator<Object> comparator = new Comparator<Object>() {
            public int compare(Object obj1, Object obj2) {
                Extension e1 = (Extension) obj1;
                Extension e2 = (Extension) obj2;
                return e1.getParameter("listener-class").valueAsString().compareTo(e2.getParameter("listener-class").valueAsString());
            };
        };
        Collections.sort(listeners, comparator);
        for (Extension exn : listeners) {
            addListener(exn.getParameter("listener-class").valueAsString(), xmlStreamWriter);
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addLoginConfig(String authMethod, String formLoginPage, String formErrorPage, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:login-config[tp:form-login-config/tp:form-login-page = '" + formLoginPage + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("login-config");
        xmlStreamWriter.writeCharacters("\n");
        if (authMethod.length() > 0) {
            xmlStreamWriter.writeStartElement("auth-method");
            xmlStreamWriter.writeCharacters(authMethod);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        xmlStreamWriter.writeStartElement("form-login-config");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("form-login-page");
        xmlStreamWriter.writeCharacters(formLoginPage);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("form-error-page");
        xmlStreamWriter.writeCharacters(formErrorPage);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addLoginConfigTemplates(PluginDescriptor pd, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addLoginConfigs");
        xmlStreamWriter.writeCharacters("\n");
        for (Extension exn : getTolvenCommandPlugin().getExtensions(pd, "login-config")) {
            Parameter authMethod = exn.getParameter("auth-method");
            String authMethodString = "";
            if (authMethod != null) {
                authMethodString = authMethod.valueAsString();
            }
            addLoginConfig(authMethodString, exn.getParameter("form-login-page").valueAsString(), exn.getParameter("form-error-page").valueAsString(), xmlStreamWriter);
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addMainTemplate(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("match", "/ | * | @* | text() | comment()");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootContextParameterSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "initTagContextParameter");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:context-param");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addContextParameters");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootEJBLocalRefSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:ejb-local-ref");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:variable");
        xmlStreamWriter.writeAttribute("name", "ejb-ref-name");
        xmlStreamWriter.writeStartElement("xsl:value-of");
        xmlStreamWriter.writeAttribute("select", "tp:ejb-ref-name");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addEJBLocalRefs");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootEnvEntrySelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:env-entry");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addEnvEntrys");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootErrorPageSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:error-page");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addErrorPages");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootFilterSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:filter");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:variable");
        xmlStreamWriter.writeAttribute("name", "filter-name");
        xmlStreamWriter.writeStartElement("xsl:value-of");
        xmlStreamWriter.writeAttribute("select", "tp:filter-name");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", "../tp:filter-mapping[tp:filter-name = $filter-name]");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addFilters");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootListenerSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:listener");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addListeners");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootLoginConfigSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:login-config");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addLoginConfigs");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootSecurityRoleSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:security-role");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addSecurityRoles");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootServletSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:servlet");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:variable");
        xmlStreamWriter.writeAttribute("name", "servlet-name");
        xmlStreamWriter.writeStartElement("xsl:value-of");
        xmlStreamWriter.writeAttribute("select", "tp:servlet-name");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", "../tp:servlet-mapping[tp:servlet-name = $servlet-name]");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addServlets");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootSessionConfigSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:session-config");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addSessionConfigs");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootTemplate(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("match", "tp:web-app");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("web-app");
        xmlStreamWriter.writeAttribute("version", "{@version}");
        xmlStreamWriter.writeAttribute("xmlns", "http://java.sun.com/xml/ns/javaee");
        xmlStreamWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        xmlStreamWriter.writeAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd");
        xmlStreamWriter.writeCharacters("\n");
        addRootContextParameterSelects(xmlStreamWriter);
        addRootListenerSelects(xmlStreamWriter);
        addRootFilterSelects(xmlStreamWriter);
        addRootServletSelects(xmlStreamWriter);
        addRootEJBLocalRefSelects(xmlStreamWriter);
        addRootSessionConfigSelects(xmlStreamWriter);
        addRootWelcomeFileListSelects(xmlStreamWriter);
        addRootWebSecurityConstraintSelects(xmlStreamWriter);
        addRootLoginConfigSelects(xmlStreamWriter);
        addRootSecurityRoleSelects(xmlStreamWriter);
        addRootEnvEntrySelects(xmlStreamWriter);
        addRootErrorPageSelects(xmlStreamWriter);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootWebSecurityConstraintSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:security-constraint");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addWebSecurityConstraints");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addRootWelcomeFileListSelects(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:for-each");
        xmlStreamWriter.writeAttribute("select", "tp:welcome-file-list");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:copy-of");
        xmlStreamWriter.writeAttribute("select", ".");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:apply-templates");
        xmlStreamWriter.writeAttribute("select", "* | @* | text() | comment()");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", "addWelcomeFileLists");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addSecurityFilterMapping(Extension securityFilterExn, Map<String, Map<String, Object>> filterMappings, PluginDescriptor pd, String contextId, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        Set<String> urlPatterns = new HashSet<String>();
        String filterName = securityFilterExn.getParameter("filter-name").valueAsString();
        Map<String, Object> map = filterMappings.get(filterName);
        List<Extension> mappingExns = (List<Extension>) map.get("mappings");
        for (Extension mappingExn : mappingExns) {
            for (Parameter param : mappingExn.getParameters("url-pattern")) {
                urlPatterns.add(param.valueAsString());
            }
        }
        Parameter urlPatternExcludesParam = securityFilterExn.getParameter("url-pattern-excludes");
        if (urlPatternExcludesParam != null) {
            List<String> excludes = Arrays.asList(urlPatternExcludesParam.valueAsString().split(","));
            urlPatterns.removeAll(excludes);
        }
        List<String> sortedURLPatterns = new ArrayList<String>(urlPatterns);
        Collections.sort(sortedURLPatterns);
        for (String urlPattern : sortedURLPatterns) {
            addFilterMapping(filterName, urlPattern, null, xmlStreamWriter);
        }
    }

    protected void addSecurityRole(String roleName, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:security-role[tp:role-name = '" + roleName + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("security-role");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("role-name");
        xmlStreamWriter.writeCharacters(roleName);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addSecurityRoleTemplates(PluginDescriptor pd, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addSecurityRoles");
        xmlStreamWriter.writeCharacters("\n");
        for (Extension exn : getTolvenCommandPlugin().getExtensions(pd, "security-role")) {
            addSecurityRole(exn.getParameter("role-name").valueAsString(), xmlStreamWriter);
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addServlet(Extension servletExn, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        String servletName = servletExn.getParameter("servlet-name").valueAsString();
        String servletClass = servletExn.getParameter("servlet-class").valueAsString();
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:servlet[tp:servlet-name = '" + servletName + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("servlet");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("servlet-name");
        xmlStreamWriter.writeCharacters(servletName);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("servlet-class");
        xmlStreamWriter.writeCharacters(servletClass);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        for (Parameter param : servletExn.getParameters("init-param")) {
            xmlStreamWriter.writeStartElement("init-param");
            String paramName = param.getSubParameter("param-name").valueAsString();
            xmlStreamWriter.writeStartElement("param-name");
            xmlStreamWriter.writeCharacters(paramName);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
            String paramValue = param.getSubParameter("param-value").valueAsString();
            xmlStreamWriter.writeStartElement("param-value");
            xmlStreamWriter.writeCharacters(paramValue);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        if (servletExn.getParameter("load-on-startup") != null) {
            Number loadOnStartUp = servletExn.getParameter("load-on-startup").valueAsNumber();
            xmlStreamWriter.writeStartElement("load-on-startup");
            xmlStreamWriter.writeCharacters(loadOnStartUp.toString());
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addServletMapping(String servletName, String urlPattern, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:servlet-mapping[tp:servlet-name = '" + servletName + "' and tp:url-pattern = '" + urlPattern + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("servlet-mapping");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("servlet-name");
        xmlStreamWriter.writeCharacters(servletName);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("url-pattern");
        xmlStreamWriter.writeCharacters(urlPattern);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addServletTemplates(PluginDescriptor pd, String contextId, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addServlets");
        xmlStreamWriter.writeCharacters("\n");
        List<Extension> servletExtensions = getServletExtensions(pd, contextId);
        Map<String, Map<String, Object>> servletMappings = getServletMappings(pd, contextId, servletExtensions);
        for (Extension exn : servletExtensions) {
            String servletName = exn.getParameter("servlet-name").valueAsString();
            addServlet(exn, xmlStreamWriter);
            Set<String> urlPatterns = new HashSet<String>();
            Map<String, Object> map = servletMappings.get(servletName);
            if (map != null) {
                List<Extension> mappingExns = (List<Extension>) map.get("mappings");
                for (Extension mappingExn : mappingExns) {
                    for (Parameter param : mappingExn.getParameters("url-pattern")) {
                        urlPatterns.add(param.valueAsString());
                    }
                }
            }
            for (String urlPattern : urlPatterns) {
                addServletMapping(servletName, urlPattern, xmlStreamWriter);
            }
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addSessionConfig(Number sessionTimeout, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:session-config[tp:session-timeout = '" + sessionTimeout + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("session-config");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("session-timeout");
        xmlStreamWriter.writeCharacters(sessionTimeout.toString());
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addSessionConfigTemplates(PluginDescriptor pd, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addSessionConfigs");
        xmlStreamWriter.writeCharacters("\n");
        for (Extension exn : getTolvenCommandPlugin().getExtensions(pd, "session-config")) {
            addSessionConfig(exn.getParameter("session-timeout").valueAsNumber(), xmlStreamWriter);
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addTagLibContextParameterCallTemplate(String tagLib, boolean semicolonSeparator, String templateName1, String templateName2, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", templateName1);
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:param");
        xmlStreamWriter.writeAttribute("name", "param-name");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:param");
        xmlStreamWriter.writeAttribute("name", "param-value");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:choose");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:when");
        xmlStreamWriter.writeAttribute("test", "contains($param-value, '" + tagLib + "')");
        xmlStreamWriter.writeCharacters("\n");
        addTagLibContextParameterWhenCallTemplate(templateName2, xmlStreamWriter);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:otherwise");
        xmlStreamWriter.writeCharacters("\n");
        addTagLibContextParameterOtherwiseCallTemplate(templateName2, tagLib, semicolonSeparator, xmlStreamWriter);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addTagLibContextParameterOtherwiseCallTemplate(String templateName, String tabLib, boolean semicolonSeparator, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", templateName);
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:with-param");
        xmlStreamWriter.writeAttribute("name", "param-name");
        xmlStreamWriter.writeStartElement("xsl:value-of");
        xmlStreamWriter.writeAttribute("select", "$param-name");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:with-param");
        xmlStreamWriter.writeAttribute("name", "param-value");
        xmlStreamWriter.writeStartElement("xsl:value-of");
        if (semicolonSeparator) {
            xmlStreamWriter.writeAttribute("select", "concat($param-value,'" + ";" + tabLib + "')");
        } else {
            xmlStreamWriter.writeAttribute("select", "concat($param-value,'" + tabLib + "')");
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addTagLibContextParameterWhenCallTemplate(String templateName, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:call-template");
        xmlStreamWriter.writeAttribute("name", templateName);
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:with-param");
        xmlStreamWriter.writeAttribute("name", "param-name");
        xmlStreamWriter.writeStartElement("xsl:value-of");
        xmlStreamWriter.writeAttribute("select", "$param-name");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("xsl:with-param");
        xmlStreamWriter.writeAttribute("name", "param-value");
        xmlStreamWriter.writeStartElement("xsl:value-of");
        xmlStreamWriter.writeAttribute("select", "$param-value");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addTransactionFilterMapping(Extension transactionFilterExn, Map<String, Map<String, Object>> filterMappings, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        Set<String> urlPatterns = new HashSet<String>();
        String filterName = transactionFilterExn.getParameter("filter-name").valueAsString();
        Map<String, Object> map = filterMappings.get(filterName);
        List<Extension> mappingExs = (List<Extension>) map.get("mappings");
        for (Extension mappingExn : mappingExs) {
            for (Parameter parameter : mappingExn.getParameters("url-pattern")) {
                urlPatterns.add(parameter.valueAsString());
            }
        }
        List<String> sortedURLPatterns = new ArrayList<String>(urlPatterns);
        Collections.sort(sortedURLPatterns);
        for (String urlPattern : sortedURLPatterns) {
            addFilterMapping(filterName, urlPattern, null, xmlStreamWriter);
        }
    }

    protected void addWebResourceCollection(String webResourceName, Set<String> urlPatterns, Set<String> httpMethods, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:security-constraint[tp:web-resource-collection/tp:web-resource-name = '" + webResourceName + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("web-resource-collection");
        xmlStreamWriter.writeStartElement("web-resource-name");
        xmlStreamWriter.writeCharacters(webResourceName);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        List<String> sortedURLPatterns = new ArrayList<String>(urlPatterns);
        Collections.sort(sortedURLPatterns);
        for (String urlPattern : sortedURLPatterns) {
            xmlStreamWriter.writeStartElement("url-pattern");
            xmlStreamWriter.writeCharacters(urlPattern);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        List<String> sortedHttpMethods = new ArrayList<String>(httpMethods);
        Collections.sort(sortedHttpMethods);
        for (String httpMethod : sortedHttpMethods) {
            xmlStreamWriter.writeStartElement("http-method");
            xmlStreamWriter.writeCharacters(httpMethod);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addWebSecurityConstraint(PluginDescriptor pd, String contextId, Extension securityConstraintExn, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("security-constraint");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addWebSecurityConstraintTemplates(PluginDescriptor pd, String contextId, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addWebSecurityConstraints");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addWelcomeFileList(String welcomeFile, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:if");
        xmlStreamWriter.writeAttribute("test", "count(tp:welcome-file-list[tp:welcome-file = '" + welcomeFile + "']) = 0");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("welcome-file-list");
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeStartElement("welcome-file");
        xmlStreamWriter.writeCharacters(welcomeFile);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    protected void addWelcomeFileListTemplates(PluginDescriptor pd, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("xsl:template");
        xmlStreamWriter.writeAttribute("name", "addWelcomeFileLists");
        xmlStreamWriter.writeCharacters("\n");
        for (Extension exn : getTolvenCommandPlugin().getExtensions(pd, "welcome-file-list")) {
            addWelcomeFileList(exn.getParameter("welcome-file").valueAsString(), xmlStreamWriter);
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    public void execute(String[] args) throws Exception {
        logger.debug("*** execute ***");
        CommandLine commandLine = getCommandLine(args);
        String warPluginId = commandLine.getOptionValue(CMD_LINE_WAR_PLUGIN_OPTION);
        String contextId = commandLine.getOptionValue(CMD_CONTEXT_ID_OPTION);
        String destDirname = commandLine.getOptionValue(CMD_LINE_DESTDIR_OPTION);
        PluginDescriptor pd = getTolvenCommandPlugin().getManager().getRegistry().getPluginDescriptor(warPluginId);
        File webXMLFile = new File(destDirname, "web.xml");
        logger.debug(webXMLFile.getPath() + " does not exist, so processing is required.");
        if (!webXMLFile.exists()) {
            logger.debug(webXMLFile.getPath() + " does not exist");
            String templateFilename = getTolvenCommandPlugin().getDescriptor().getAttribute(ATTRIBUTE_TEMPLATE_WEBXML).getValue();
            File templateFile = getTolvenCommandPlugin().getFilePath(templateFilename);
            if (!templateFile.exists()) {
                throw new RuntimeException("Could not locate: '" + templateFile.getPath());
            }
            logger.debug("Copy " + templateFile + " to " + webXMLFile.getPath());
            FileUtils.copyFile(templateFile, webXMLFile);
        }
        StringBuffer originalXML = new StringBuffer();
        logger.debug("Read " + webXMLFile.getPath());
        originalXML.append(FileUtils.readFileToString(webXMLFile));
        String xslt = getXSLT(pd, contextId);
        File xsltFile = new File(getTolvenCommandPlugin().getPluginTmpDir(), "webxml-xslt.xml");
        logger.debug("Write web.xml xslt to " + xsltFile.getPath());
        FileUtils.writeStringToFile(new File(getTolvenCommandPlugin().getPluginTmpDir(), "webxml-xslt.xml"), xslt);
        String translatedXMLString = getTolvenCommandPlugin().getTranslatedXML(originalXML.toString(), xslt);
        webXMLFile.getParentFile().mkdirs();
        logger.debug("Write translated web.xml file to " + webXMLFile.getPath());
        FileUtils.writeStringToFile(webXMLFile, translatedXMLString);
    }

    protected PluginDescriptor getAbstractWARPluginDescriptor() {
        ExtensionPoint exntPt = getTolvenCommandPlugin().getDescriptor().getExtensionPoint(EXNPT_ABSTRACT_WAR);
        String parentPluginId = exntPt.getParentPluginId();
        PluginDescriptor pd = getTolvenCommandPlugin().getManager().getRegistry().getPluginDescriptor(parentPluginId);
        return pd;
    }

    private CommandLine getCommandLine(String[] args) {
        GnuParser parser = new GnuParser();
        try {
            return parser.parse(getCommandOptions(), args, true);
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(getClass().getName(), getCommandOptions());
            throw new RuntimeException("Could not parse command line for: " + getClass().getName(), ex);
        }
    }

    private Options getCommandOptions() {
        Options cmdLineOptions = new Options();
        Option warPluginOption = new Option(CMD_LINE_WAR_PLUGIN_OPTION, CMD_LINE_WAR_PLUGIN_OPTION, true, "war plugin");
        warPluginOption.setRequired(true);
        cmdLineOptions.addOption(warPluginOption);
        Option contextIdOption = new Option(CMD_CONTEXT_ID_OPTION, CMD_CONTEXT_ID_OPTION, true, CMD_CONTEXT_ID_OPTION);
        cmdLineOptions.addOption(contextIdOption);
        Option destDirPluginOption = new Option(CMD_LINE_DESTDIR_OPTION, CMD_LINE_DESTDIR_OPTION, true, "destination directory");
        destDirPluginOption.setRequired(true);
        cmdLineOptions.addOption(destDirPluginOption);
        return cmdLineOptions;
    }

    protected List<Extension> getDuplicateErrorPages(Set<Extension> errorExtensions) {
        List<Extension> duplicates = new ArrayList<Extension>();
        Map<String, List<Extension>> exnMap = new HashMap<String, List<Extension>>();
        for (Extension exn : errorExtensions) {
            Parameter errorCodeParam = exn.getParameter("error-code");
            if (errorCodeParam == null || errorCodeParam.valueAsString() == null || errorCodeParam.valueAsString().trim().isEmpty()) {
                throw new RuntimeException("A value for error-code on " + exn.getUniqueId() + " must be supplied");
            }
            Parameter locationParam = exn.getParameter("location");
            if (locationParam == null || locationParam.valueAsString() == null || locationParam.valueAsString().trim().isEmpty()) {
                throw new RuntimeException("A value for location on " + exn.getUniqueId() + " must be supplied");
            }
            String errorCode = errorCodeParam.valueAsString();
            List<Extension> exns = exnMap.get(errorCode);
            if (exns == null) {
                exns = new ArrayList<Extension>();
                exnMap.put(errorCode, exns);
            }
            exns.add(exn);
        }
        for (String key : exnMap.keySet()) {
            if (exnMap.get(key).size() > 1) {
                duplicates.addAll(exnMap.get(key));
            }
        }
        return duplicates;
    }

    protected Set<Extension> getErrorPageExtensions(PluginDescriptor pd, boolean defaults) {
        Set<Extension> exns = new HashSet<Extension>();
        PluginDescriptor abstractPD = getAbstractWARPluginDescriptor();
        ExtensionPoint errorPageExnPt = abstractPD.getExtensionPoint("error-page");
        if (errorPageExnPt == null) {
            throw new RuntimeException(abstractPD.getUniqueId() + " does not have an error-page extension point");
        }
        for (Extension exn : errorPageExnPt.getConnectedExtensions()) {
            PluginDescriptor errorPagePD = exn.getDeclaringPluginDescriptor();
            String targetPluginId = exn.getParameter("target-plugin-id").valueAsString();
            if (targetPluginId == null) {
                throw new RuntimeException("A value for target-plugin-id on " + exn.getUniqueId() + " must be supplied");
            }
            if (defaults) {
                if (errorPagePD.getId().equals(pd.getId()) && targetPluginId.equals(pd.getId())) {
                    exns.add(exn);
                }
            } else {
                if (!errorPagePD.getId().equals(pd.getId()) && targetPluginId.equals(pd.getId())) {
                    exns.add(exn);
                }
            }
        }
        List<Extension> duplicates = getDuplicateErrorPages(exns);
        if (!duplicates.isEmpty()) {
            StringBuffer buff = new StringBuffer();
            for (Extension exn : duplicates) {
                buff.append(exn.getUniqueId() + " ");
            }
            throw new RuntimeException("ErrorPages with duplicate codes detected: " + buff.toString());
        }
        return exns;
    }

    protected List<Extension> getFilterExtensions(PluginDescriptor pd, String contextId) {
        List<Extension> filterExns = new ArrayList<Extension>();
        ExtensionPoint adaptorExnPt = pd.getExtensionPoint(EXNPT_FILTER_ADPTR);
        if (adaptorExnPt != null) {
            for (Extension exn : adaptorExnPt.getConnectedExtensions()) {
                Parameter parentContextIdParam = exn.getParameter("parent-context-id");
                if (parentContextIdParam == null || contextId.equals(parentContextIdParam.valueAsString())) {
                    filterExns.add(exn);
                }
            }
        }
        PluginDescriptor abstractPD = getAbstractWARPluginDescriptor();
        ExtensionPoint exnPt = abstractPD.getExtensionPoint("filter");
        if (exnPt != null) {
            for (Extension exn : exnPt.getConnectedExtensions()) {
                Parameter targetPluginId = exn.getParameter("target-plugin-id");
                if (targetPluginId == null || targetPluginId.valueAsString().trim().length() == 0) {
                    throw new RuntimeException(exn.getUniqueId() + " does not have a target-plugin-id parameter value");
                }
                if (pd.getId().equals(targetPluginId.valueAsString())) {
                    filterExns.add(exn);
                }
            }
        }
        for (Extension exn : filterExns) {
            Parameter filterNameParam = exn.getParameter("filter-name");
            if (filterNameParam == null || filterNameParam.valueAsString().trim().length() == 0) {
                throw new RuntimeException(exn.getUniqueId() + " does not have a filter-name parameter value");
            }
            Parameter filterClassParam = exn.getParameter("filter-class");
            if (filterClassParam == null || filterClassParam.valueAsString().trim().length() == 0) {
                throw new RuntimeException(exn.getUniqueId() + " does not have a filter-class parameter value");
            }
        }

        Comparator<Extension> comparator = new Comparator<Extension>() {
            public int compare(Extension filterExn1, Extension filterExn2) {
                Number num1 = filterExn1.getParameter("filter-sequence").valueAsNumber();
                Number num2 = filterExn2.getParameter("filter-sequence").valueAsNumber();
                if (num1.doubleValue() < num2.doubleValue()) {
                    return -1;
                } else if (num1.doubleValue() > num2.doubleValue()) {
                    return 1;
                } else {
                    return 0;
                }
            };
        };
        Collections.sort(filterExns, comparator);
        return filterExns;
    }

    protected Map<String, Map<String, Object>> getFilterMappings(PluginDescriptor pd, String contextId, List<Extension> filterExns) {
        Map<String, Map<String, Object>> filterMappings = new HashMap<String, Map<String, Object>>();
        List<String> filterNames = new ArrayList<String>();
        for (Extension exn : filterExns) {
            Map<String, Object> map = new HashMap<String, Object>();
            String filterName = exn.getParameter("filter-name").valueAsString();
            filterNames.add(filterName);
            map.put("extension", exn);
            map.put("mappings", new ArrayList<Extension>());
            filterMappings.put(filterName, map);
        }
        ExtensionPoint adaptorExnPt = pd.getExtensionPoint(EXNPT_FILTER_MAPPING_CONTRIBUTION_ADPTR);
        if (adaptorExnPt != null) {
            for (Extension exn : adaptorExnPt.getConnectedExtensions()) {
                Parameter parentContextIdParam = exn.getParameter("parent-context-id");
                if (parentContextIdParam == null || contextId.equals(parentContextIdParam.valueAsString())) {
                    Parameter filterNameParam = exn.getParameter("filter-name");
                    if (filterNameParam == null || filterNameParam.valueAsString().trim().length() == 0) {
                        throw new RuntimeException(exn.getUniqueId() + " does not have a filter-name parameter value");
                    }
                    Parameter optionalParam = exn.getParameter("optional");
                    if (!filterNames.contains(filterNameParam.valueAsString())) {
                        if (optionalParam != null && optionalParam.valueAsBoolean()) {
                            continue;
                        } else {
                            throw new RuntimeException(exn.getUniqueId() + " is not optional yet has a filter-name which does not exist");
                        }
                    }
                    if (filterNameParam == null || filterNameParam.valueAsString().trim().length() == 0) {
                        throw new RuntimeException(exn.getUniqueId() + " does not have a filter-name parameter value");
                    }
                    String filterName = filterNameParam.valueAsString();
                    Map<String, Object> map = filterMappings.get(filterName);
                    List<Extension> exns = (List<Extension>) map.get("mappings");
                    exns.add(exn);
                }
            }
        }
        PluginDescriptor abstractPD = getAbstractWARPluginDescriptor();
        ExtensionPoint filterMappingExnPt = abstractPD.getExtensionPoint("filter-mapping-contribution");
        if (filterMappingExnPt != null) {
            for (Extension exn : filterMappingExnPt.getConnectedExtensions()) {
                Parameter targetPluginId = exn.getParameter("target-plugin-id");
                if (targetPluginId == null || targetPluginId.valueAsString().trim().length() == 0) {
                    throw new RuntimeException(exn.getUniqueId() + " does not have a target-plugin-id parameter value");
                }
                if (pd.getId().equals(targetPluginId.valueAsString())) {
                    Parameter filterNameParam = exn.getParameter("filter-name");
                    if (filterNameParam == null || filterNameParam.valueAsString().trim().length() == 0) {
                        throw new RuntimeException(exn.getUniqueId() + " does not have a filter-name parameter value");
                    }
                    Parameter optionalParam = exn.getParameter("optional");
                    if (!filterNames.contains(filterNameParam.valueAsString())) {
                        if (optionalParam != null && optionalParam.valueAsBoolean()) {
                            continue;
                        } else {
                            throw new RuntimeException(exn.getUniqueId() + " is not optional yet has a filter-name which does not exist");
                        }
                    }
                    if (filterNameParam == null || filterNameParam.valueAsString().trim().length() == 0) {
                        throw new RuntimeException(exn.getUniqueId() + " does not have a filter-name parameter value");
                    }
                    String filterName = filterNameParam.valueAsString();
                    Map<String, Object> map = filterMappings.get(filterName);
                    List<Extension> exns = (List<Extension>) map.get("mappings");
                    exns.add(exn);
                }
            }
        }
        return filterMappings;
    }

    protected List<Extension> getServletExtensions(PluginDescriptor pd, String contextId) {
        List<Extension> servletExns = new ArrayList<Extension>();
        ExtensionPoint adaptorExnPt = pd.getExtensionPoint(EXNPT_SERVLET_ADPTR);
        if (adaptorExnPt != null) {
            for (Extension exn : adaptorExnPt.getConnectedExtensions()) {
                Parameter parentContextIdParam = exn.getParameter("parent-context-id");
                if (parentContextIdParam == null || contextId.equals(parentContextIdParam.valueAsString())) {
                    servletExns.add(exn);
                }
            }
        }
        PluginDescriptor abstractPD = getAbstractWARPluginDescriptor();
        ExtensionPoint exnPt = abstractPD.getExtensionPoint("servlet");
        if (exnPt != null) {
            for (Extension exn : exnPt.getConnectedExtensions()) {
                Parameter targetPluginId = exn.getParameter("target-plugin-id");
                if (targetPluginId == null || targetPluginId.valueAsString().trim().length() == 0) {
                    throw new RuntimeException(exn.getUniqueId() + " does not have a target-plugin-id parameter value");
                }
                if (pd.getId().equals(targetPluginId.valueAsString())) {
                    servletExns.add(exn);
                }
            }
        }
        for (Extension exn : servletExns) {
            Parameter servletNameParam = exn.getParameter("servlet-name");
            if (servletNameParam == null || servletNameParam.valueAsString().trim().length() == 0) {
                throw new RuntimeException(exn.getUniqueId() + " does not have a servlet-name parameter value");
            }
            Parameter servletClassParam = exn.getParameter("servlet-class");
            if (servletClassParam == null || servletClassParam.valueAsString().trim().length() == 0) {
                throw new RuntimeException(exn.getUniqueId() + " does not have a servlet-class parameter value");
            }
        }
        return servletExns;
    }

    protected Map<String, Map<String, Object>> getServletMappings(PluginDescriptor pd, String contextId, List<Extension> servletExns) {
        Map<String, Extension> servletMap = new HashMap<String, Extension>();
        for (Extension exn : servletExns) {
            servletMap.put(exn.getParameter("servlet-name").valueAsString(), exn);
        }
        List<String> servletNames = new ArrayList<String>(servletMap.keySet());
        Map<String, Map<String, Object>> servletMappings = new HashMap<String, Map<String, Object>>();
        ExtensionPoint adaptorExnPt = pd.getExtensionPoint(EXNPT_SERVLET_MAPPING_CONTRIBUTION_ADPTR);
        if (adaptorExnPt != null) {
            for (Extension exn : adaptorExnPt.getConnectedExtensions()) {
                Parameter parentContextIdParam = exn.getParameter("parent-context-id");
                if (parentContextIdParam == null || contextId.equals(parentContextIdParam.valueAsString())) {
                    Parameter servletNameParam = exn.getParameter("servlet-name");
                    if (servletNameParam == null || servletNameParam.valueAsString().trim().length() == 0) {
                        throw new RuntimeException(exn.getUniqueId() + " does not have a servlet-name parameter value");
                    }
                    Parameter optionalParam = exn.getParameter("optional");
                    if (!servletNames.contains(servletNameParam.valueAsString())) {
                        if (optionalParam != null && optionalParam.valueAsBoolean()) {
                            continue;
                        } else {
                            throw new RuntimeException(exn.getUniqueId() + " is not optional yet has a servlet-name which does not exist");
                        }
                    }
                    if (servletNameParam == null || servletNameParam.valueAsString().trim().length() == 0) {
                        throw new RuntimeException(exn.getUniqueId() + " does not have a servlet-name parameter value");
                    }
                    String servletName = servletNameParam.valueAsString();
                    Map<String, Object> map = servletMappings.get(servletName);
                    if (map == null) {
                        map = new HashMap<String, Object>();
                        map.put("extension", servletMap.get(servletName));
                        map.put("mappings", new ArrayList<Extension>());
                        servletMappings.put(servletName, map);
                    }
                    List<Extension> exns = (List<Extension>) map.get("mappings");
                    exns.add(exn);
                }
            }
        }
        PluginDescriptor abstractPD = getAbstractWARPluginDescriptor();
        ExtensionPoint servletMappingExnPt = abstractPD.getExtensionPoint("servlet-mapping-contribution");
        if (servletMappingExnPt != null) {
            for (Extension exn : servletMappingExnPt.getConnectedExtensions()) {
                Parameter targetPluginId = exn.getParameter("target-plugin-id");
                if (targetPluginId == null || targetPluginId.valueAsString().trim().length() == 0) {
                    throw new RuntimeException(exn.getUniqueId() + " does not have a target-plugin-id parameter value");
                }
                if (pd.getId().equals(targetPluginId.valueAsString())) {
                    Parameter servletNameParam = exn.getParameter("servlet-name");
                    if (servletNameParam == null || servletNameParam.valueAsString().trim().length() == 0) {
                        throw new RuntimeException(exn.getUniqueId() + " does not have a servlet-name parameter value");
                    }
                    Parameter optionalParam = exn.getParameter("optional");
                    if (!servletNames.contains(servletNameParam.valueAsString())) {
                        if (optionalParam != null && optionalParam.valueAsBoolean()) {
                            continue;
                        } else {
                            throw new RuntimeException(exn.getUniqueId() + " is not optional yet has a servlet-name which does not exist");
                        }
                    }
                    if (servletNameParam == null || servletNameParam.valueAsString().trim().length() == 0) {
                        throw new RuntimeException(exn.getUniqueId() + " does not have a servlet-name parameter value");
                    }
                    String servletName = servletNameParam.valueAsString();
                    Map<String, Object> map = servletMappings.get(servletName);
                    if (map == null) {
                        map = new HashMap<String, Object>();
                        map.put("extension", servletMap.get(servletName));
                        map.put("mappings", new ArrayList<Extension>());
                        servletMappings.put(servletName, map);
                    }
                    List<Extension> exns = (List<Extension>) map.get("mappings");
                    exns.add(exn);
                }
            }
        }
        return servletMappings;
    }

    private TolvenCommandPlugin getTolvenCommandPlugin() {
        return tolvenCommandPlugin;
    }

    protected String getXSLT(PluginDescriptor pd, String contextId) throws XMLStreamException {
        StringWriter xslt = new StringWriter();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlStreamWriter = null;
        try {
            xmlStreamWriter = factory.createXMLStreamWriter(xslt);
            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
            xmlStreamWriter.writeCharacters("\n");
            xmlStreamWriter.writeStartElement("xsl:stylesheet");
            xmlStreamWriter.writeAttribute("version", "2.0");
            xmlStreamWriter.writeNamespace("xsl", "http://www.w3.org/1999/XSL/Transform");
            xmlStreamWriter.writeNamespace("tp", "http://java.sun.com/xml/ns/javaee");
            xmlStreamWriter.writeAttribute("exclude-result-prefixes", "tp");
            xmlStreamWriter.writeCharacters("\n");
            xmlStreamWriter.writeStartElement("xsl:output");
            xmlStreamWriter.writeAttribute("method", "xml");
            xmlStreamWriter.writeAttribute("indent", "yes");
            xmlStreamWriter.writeAttribute("encoding", "UTF-8");
            xmlStreamWriter.writeAttribute("omit-xml-declaration", "no");
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
            addMainTemplate(xmlStreamWriter);
            addRootTemplate(xmlStreamWriter);
            addContextParameterTemplate(xmlStreamWriter);
            addContextParameterCallTemplates(pd, contextId, xmlStreamWriter);
            addFilterTemplates(pd, contextId, xmlStreamWriter);
            addListenerTemplates(pd, contextId, xmlStreamWriter);
            addServletTemplates(pd, contextId, xmlStreamWriter);
            addEJBLocalRefTemplates(pd, xmlStreamWriter);
            addSessionConfigTemplates(pd, xmlStreamWriter);
            addWelcomeFileListTemplates(pd, xmlStreamWriter);
            addWebSecurityConstraintTemplates(pd, contextId, xmlStreamWriter);
            addLoginConfigTemplates(pd, xmlStreamWriter);
            addSecurityRoleTemplates(pd, xmlStreamWriter);
            addEnvEntryTemplates(pd, contextId, xmlStreamWriter);
            addErrorPageTemplates(pd, xmlStreamWriter);
            xmlStreamWriter.writeEndDocument();
        } finally {
            if (xmlStreamWriter != null) {
                xmlStreamWriter.close();
            }
        }
        return xslt.toString();
    }

    private void setTolvenCommandPlugin(TolvenCommandPlugin tolvenCommandPlugin) {
        this.tolvenCommandPlugin = tolvenCommandPlugin;
    }

}
