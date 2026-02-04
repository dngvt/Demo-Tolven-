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
 * @version $Id$
 */
package org.tolven.assembler.war;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.java.plugin.registry.ExtensionPoint.ParameterDefinition;
import org.java.plugin.registry.PluginDescriptor;
import org.tolven.plugin.TolvenCommandPlugin;
import org.tolven.plugin.repository.ConfigPluginsWrapper;
import org.tolven.plugin.repository.bean.PluginDetail;
import org.tolven.plugin.repository.bean.PluginPropertyDetail;
import org.tolven.plugin.repository.bean.Plugins;
import org.tolven.tools.ant.TolvenJar;
import org.tolven.tools.ant.TolvenReplaceRegExp;

/**
 * This plugin assembles a war file.
 * 
 * @author Joseph Isaac
 *
 */
public class WARAssembler extends TolvenCommandPlugin {

    public static final String CMD_CONTEXT_ID_OPTION = "contextId";
    public static final String CMD_DESTDIR_OPTION = "destDir";
    public static final String CMD_WAR_PLUGIN_OPTION = "warPlugin";
    public static final String CMD_WEB_URI_OPTION = "webURI";

    public static final String EXNPT_ABSTRACT_WAR = "abstractWAR";
    public static final String EXNPT_CLASSES = "classes";
    public static final String EXNPT_FILE_INCLUDE = "fileInclude";
    public static final String EXNPT_ID = "extension-point";
    public static final String EXNPT_METAINF = "META-INF";
    public static final String EXNPT_SRC_PLUGIN_ID = "source-plugin-id";
    public static final String EXNPT_WAR_PROPERTY = "property";
    public static final String EXNPT_WAR_PROPERTY_SEQUENCE = "propertySequence";
    public static final String EXNPT_WARMODULE_DECL = "warModule-declaration";
    @Deprecated
    public static final String EXNPT_WEBDIRECTORY = "webDirectory";
    public static final String EXNPT_WEBDIRECTORY_ADPTR = "webDirectory-adaptor";
    public static final String EXNPT_WEBINF_FILE_ADPTR = "WEB-INF-FILE-adaptor";
    public static final String EXNPT_WEBINF_LIB_ADPTR = "WEB-INF-LIB-adaptor";
    public static final String EXNPT_WEBINFCLASSES_ADPTR = "WEB-INF-CLASSES-adaptor";
    @Deprecated
    public static final String EXNPT_WEBINFLIB = "WEB-INF-LIB";
    public static final String EXNPT_WEBROOTFILES_ADPTR = "WEB-ROOT-FILES-adaptor";
    public static final String EXTENSIONPOINT_SCRIPT_SORT_ORDER = "scriptsOrder";
    
    public static final String CORE_PLUGIN_CONTRIBUTORS ="coreContributors";
    public static final String TAGLIB_EXTENSIONPOINT = "taglib";

    private Logger logger = Logger.getLogger(WARAssembler.class);

    protected void addTagConverter(PluginDescriptor pd, XMLStreamWriter writer) throws XMLStreamException {
        ExtensionPoint converterExnPt = pd.getExtensionPoint("tagConverter");
        if (converterExnPt != null) {
            for (Extension extension : converterExnPt.getConnectedExtensions()) {
                writer.writeStartElement("tag");
                writer.writeCharacters("\n");
                writer.writeStartElement("tag-name");
                writer.writeCharacters(extension.getParameter("tag-name").valueAsString());
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeStartElement("converter");
                writer.writeCharacters("\n");
                writer.writeStartElement("converter-id");
                writer.writeCharacters(extension.getParameter("converter-id").valueAsString());
                writer.writeEndElement();
                writer.writeCharacters("\n");
                Parameter handlerClass = extension.getParameter("handler-class");
                if (handlerClass != null) {
                    writer.writeStartElement("handler-class");
                    writer.writeCharacters(handlerClass.valueAsString());
                    writer.writeEndElement();
                    writer.writeCharacters("\n");
                }
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeEndElement();
                writer.writeCharacters("\n");
            }
        }
    }

    protected void addTagInfo(Extension taglibExn, PluginDescriptor warPD, File myWARPluginDir) throws IOException, XMLStreamException {
        File metaInfTagDir = new File(myWARPluginDir.getPath() + "/META-INF/tags");
        metaInfTagDir.mkdirs();
        StringWriter tagLibWriter = new StringWriter();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null;
        try {
            writer = factory.createXMLStreamWriter(tagLibWriter);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            writer.writeDTD("<!DOCTYPE facelet-taglib PUBLIC \"-//Sun Microsystems, Inc.//DTD Facelet Taglib 1.0//EN\" \"http://java.sun.com/dtd/facelet-taglib_1_0.dtd\">");
            writer.writeCharacters("\n");
            writer.writeStartElement("facelet-taglib");
            writer.writeCharacters("\n");
            String namespace = taglibExn.getParameter("namespace").valueAsString();
            writer.writeStartElement("namespace");
            writer.writeCharacters(namespace);
            writer.writeEndElement();
            writer.writeCharacters("\n");
            PluginDescriptor taglibPD = taglibExn.getDeclaringPluginDescriptor();
            addTagSource(taglibPD, metaInfTagDir, warPD, writer);
            addTagValidator(taglibPD, writer);
            addTagConverter(taglibPD, writer);
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.writeEndDocument();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        String tagFilename = taglibExn.getParameter("tag-filename").valueAsString();
        File metaInfTagFile = new File(metaInfTagDir, tagFilename);
        logger.debug("Write tagLib string to " + metaInfTagFile.getPath());
        FileUtils.writeStringToFile(metaInfTagFile, tagLibWriter.toString());
    }

    protected void addTagSource(File dir, XMLStreamWriter writer) throws XMLStreamException {
        for (File file : dir.listFiles()) {
            String src = dir.getName() + "/" + file.getName();
            writer.writeStartElement("tag");
            writer.writeCharacters("\n");
            writer.writeStartElement("tag-name");
            int index = file.getName().lastIndexOf(".");
            if (index == -1) {
                writer.writeCharacters(file.getName());
            } else {
                writer.writeCharacters(file.getName().substring(0, index));
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.writeStartElement("source");
            writer.writeCharacters(src);
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.writeEndElement();
            writer.writeCharacters("\n");
        }
    }

    protected void addTagSource(PluginDescriptor taglibPD, File metaInfTagDir, PluginDescriptor warPD, XMLStreamWriter xmlStreamWriter) throws IOException, XMLStreamException {
        ExtensionPoint tagSourceDirectoryExtensionPoint = taglibPD.getExtensionPoint("tagSourceDirectory");
        if (tagSourceDirectoryExtensionPoint != null) {
            List<Extension> defaultExtensions = new ArrayList<Extension>();
            List<Extension> additionalExtensions = new ArrayList<Extension>();
            for (Extension exn : tagSourceDirectoryExtensionPoint.getConnectedExtensions()) {
                PluginDescriptor exnDeclaringPluginDescriptor = exn.getDeclaringPluginDescriptor();
                if(exnDeclaringPluginDescriptor.getId().equals(warPD.getId())) {
                    defaultExtensions.add(exn);
                } else {
                    additionalExtensions.add(exn);
                }
            }
            Set<File> dirs = new HashSet<File>();
            for (Extension exn : defaultExtensions) {
                String dirname = exn.getParameter("source-directory").valueAsString();
                File dir = getFilePath(exn.getDeclaringPluginDescriptor(), dirname);
                logger.debug("Copy default " + dir.getPath() + " to " + metaInfTagDir.getPath());
                FileUtils.copyDirectoryToDirectory(dir, metaInfTagDir);
                dirs.add(dir);
            }
            for (Extension exn : additionalExtensions) {
                String dirname = exn.getParameter("source-directory").valueAsString();
                File dir = getFilePath(exn.getDeclaringPluginDescriptor(), dirname);
                logger.debug("Copy additional " + dir.getPath() + " to " + metaInfTagDir.getPath());
                FileUtils.copyDirectoryToDirectory(dir, metaInfTagDir);
                dirs.add(dir);
            }
            for (File dir : dirs) {
                addTagSource(dir, xmlStreamWriter);
            }
        }
    }

    protected void addTagValidator(PluginDescriptor pd, XMLStreamWriter writer) throws XMLStreamException {
        ExtensionPoint validatorExnPt = pd.getExtensionPoint("tagValidator");
        if (validatorExnPt != null) {
            for (Extension exn : validatorExnPt.getConnectedExtensions()) {
                writer.writeStartElement("tag");
                writer.writeCharacters("\n");
                writer.writeStartElement("tag-name");
                writer.writeCharacters(exn.getParameter("tag-name").valueAsString());
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeStartElement("validator");
                writer.writeCharacters("\n");
                writer.writeStartElement("validator-id");
                writer.writeCharacters(exn.getParameter("validator-id").valueAsString());
                writer.writeEndElement();
                writer.writeCharacters("\n");
                Parameter handlerClass = exn.getParameter("handler-class");
                if (handlerClass != null) {
                    writer.writeStartElement("handler-class");
                    writer.writeCharacters(handlerClass.valueAsString());
                    writer.writeEndElement();
                    writer.writeCharacters("\n");
                }
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeEndElement();
                writer.writeCharacters("\n");
            }
        }
    }

    @Deprecated
    protected void assembleClasses(PluginDescriptor pd, File myWARPluginDir) throws IOException {
        //The following loop is where class contributions are not directly attached to pd.getId()
        for (Extension exn : getExtensions(pd, EXNPT_CLASSES)) {
            assembleWARClasses(exn, myWARPluginDir);
        }
        for (ExtensionPoint classesExPt : pd.getExtensionPoints()) {
            if (EXNPT_CLASSES.equals(classesExPt.getParentExtensionPointId()) && classesExPt.getParentPluginId().equals(getDescriptor().getId())) {
                for (Extension exn : classesExPt.getConnectedExtensions()) {
                    assembleWARClasses(exn, myWARPluginDir);
                }
            }
        }
    }

    protected void assembleMetaInf(PluginDescriptor pd, File myWARPluginDir) throws IOException {
        for (ExtensionPoint exnPt : pd.getExtensionPoints()) {
            if (EXNPT_METAINF.equals(exnPt.getParentExtensionPointId()) && exnPt.getParentPluginId().equals(getDescriptor().getId())) {
                for (Extension metaInfExn : exnPt.getConnectedExtensions()) {
                    String dirname = metaInfExn.getParameter("dir").valueAsString();
                    File srcDir = getFilePath(metaInfExn.getDeclaringPluginDescriptor(), dirname);
                    File destDir = new File(myWARPluginDir + "/META-INF");
                    logger.debug("Copy " + srcDir.getPath() + " to " + destDir.getPath());
                    FileUtils.copyDirectory(srcDir, destDir);
                }
            }
        }
    }

    protected void assembleProperties(PluginDescriptor pd, File myWARPluginDir) {
        Map<String, Map<String, Set<String>>> propertyClassMapping = new HashMap<String, Map<String, Set<String>>>();
        Map<String, List<String>> propertySequenceMapping = propertySequenceMapping(pd);
        Set<Extension> classesExns = new HashSet<Extension>();
        classesExns.addAll(getChildExtensions(EXNPT_WAR_PROPERTY, pd));
        PluginDescriptor abstractWARPD = getAbstractWARPluginDescriptor();
        ExtensionPoint propertyExnPt = abstractWARPD.getExtensionPoint(EXNPT_WAR_PROPERTY);
        for (Extension exn : propertyExnPt.getConnectedExtensions()) {
            Parameter pluginIdParam = exn.getParameter("target-plugin-id");
            if (pluginIdParam == null || pluginIdParam.valueAsString().trim().length() == 0) {
                throw new RuntimeException(exn.getUniqueId() + " does not have a target-plugin-id parameter value");
            }
            if (pd.getId().equals(pluginIdParam.valueAsString())) {
                classesExns.add(exn);
            }
        }
        for (Extension classesExn : classesExns) {
            String classname = classesExn.getParameter("class").valueAsString();
            Map<String, Set<String>> propertySetMap = propertyClassMapping.get(classname);
            if (propertySetMap == null) {
                propertySetMap = new HashMap<String, Set<String>>();
                String propertiesFilename = classname.replace(".", "/") + ".properties";
                File propertiesFile = new File(propertiesFilename);
                File previousPropertiesFile = new File(myWARPluginDir, propertiesFile.getPath());
                if (previousPropertiesFile.exists()) {
                    Properties props = loadProperties(previousPropertiesFile);
                    for (Object key : props.keySet()) {
                        Set<String> valueSet = new HashSet<String>();
                        String[] values = ((String) props.get(key)).split(",");
                        for (String v : values) {
                            valueSet.add(v);
                        }
                        propertySetMap.put((String) key, valueSet);
                    }
                }
                propertyClassMapping.put(classname, propertySetMap);
            }
            String name = classesExn.getParameter("name").valueAsString();
            Set<String> valueSet = propertySetMap.get(name);
            if (valueSet == null) {
                valueSet = new HashSet<String>();
                propertySetMap.put(name, valueSet);
            }
            valueSet.add(classesExn.getParameter("value").valueAsString());
        }
        for (String classname : propertyClassMapping.keySet()) {
            Properties props = new Properties();
            Map<String, Set<String>> propertySetMap = propertyClassMapping.get(classname);
            for (String name : propertySetMap.keySet()) {
                final List<String> sequence = propertySequenceMapping.get(classname + "." + name);
                List<String> orderedValues = new ArrayList<String>();
                List<String> unorderedValues = new ArrayList<String>();
                for (String value : propertySetMap.get(name)) {
                    if (sequence != null && sequence.contains(value)) {
                        orderedValues.add(value);
                    } else {
                        unorderedValues.add(value);
                    }
                }
                if (sequence != null) {
                    Comparator<String> comparator = new Comparator<String>() {
                        public int compare(String string1, String string2) {
                            return new Integer(sequence.indexOf(string1)).compareTo(new Integer(sequence.indexOf(string2)));
                        };
                    };
                    Collections.sort(orderedValues, comparator);
                }
                List<String> allValues = new ArrayList<String>();
                allValues.addAll(orderedValues);
                allValues.addAll(unorderedValues);
                Iterator<String> it = allValues.iterator();
                StringBuffer buff = new StringBuffer();
                while (it.hasNext()) {
                    buff.append(it.next());
                    if (it.hasNext()) {
                        buff.append(",");
                    }
                }
                String value = buff.toString();
                props.put(name, value);
            }
            String propertiesFilename = classname.replace(".", "/") + ".properties";
            File propertiesFile = new File(myWARPluginDir, "WEB-INF/classes/" + propertiesFilename);
            logger.debug("Write " + propertiesFile.getPath());
            propertiesFile.getParentFile().mkdirs();
            storeProperties(props, propertiesFile);
        }
    }

    /**
     * An inclusion tags for java scripts
     * 
     * @param pd
     * @throws IOException
     */
    protected void assembleScriptIncludes(PluginDescriptor pd, File myWARPluginDir) throws IOException {
        Map<File, Map<String, Object>> fileIncludesMap = new HashMap<File, Map<String, Object>>();
        for (ExtensionPoint fileIncludeExnPt : pd.getExtensionPoints()) {
            if (EXNPT_FILE_INCLUDE.equals(fileIncludeExnPt.getParentExtensionPointId()) && fileIncludeExnPt.getParentPluginId().equals(getDescriptor().getId())) {
                String matchExp = fileIncludeExnPt.getParameterDefinition("matchExp").getDefaultValue();
                if (matchExp == null) {
                    throw new RuntimeException(fileIncludeExnPt.getUniqueId() + " must supply a default value for matchExp");
                }
                String targetFilename = fileIncludeExnPt.getParameterDefinition("targetFile").getDefaultValue();
                if (targetFilename == null) {
                    throw new RuntimeException(fileIncludeExnPt.getUniqueId() + " must supply a default value for targetFile");
                }
                File targetFile = new File(myWARPluginDir, targetFilename);
                if (!targetFile.exists()) {
                    throw new RuntimeException(targetFilename + " is not available for the war file");
                }
                //sort the java script file includes
                PluginPropertyDetail propertyDetail = getPluginProperty(pd.getId(), EXTENSIONPOINT_SCRIPT_SORT_ORDER);
                Collection<Extension> extensions = fileIncludeExnPt.getConnectedExtensions();
                if (propertyDetail != null && propertyDetail.getValue() != null && propertyDetail.getValue().trim().length() > 0)
                    extensions = sortScriptIncludes(extensions, propertyDetail);
                for (Extension exn : extensions) {
                    String includeFilename = exn.getParameter("includeFile").valueAsString();
                    if (includeFilename == null) {
                        throw new RuntimeException(exn.getUniqueId() + " must supply a value for includeFile");
                    }
                    File includeFile = getFilePath(exn.getDeclaringPluginDescriptor(), includeFilename);
                    if (!includeFile.exists()) {
                        throw new RuntimeException("Could not locate: " + includeFile.getPath());
                    }
                    Map<String, Object> info = fileIncludesMap.get(targetFile);
                    if (info == null) {
                        info = new HashMap<String, Object>();
                        info.put("matchExp", matchExp);
                        List<File> includeFiles = new ArrayList<File>();
                        info.put("includeFiles", includeFiles);
                        fileIncludesMap.put(targetFile, info);
                    }
                    List<File> includeFiles = (List<File>) info.get("includeFiles");
                    includeFiles.add(includeFile);
                }
            }
        }
        for (File targetFile : fileIncludesMap.keySet()) {
            StringBuffer buff = new StringBuffer();
            Map<String, Object> info = fileIncludesMap.get(targetFile);
            List<File> includeFiles = (List<File>) info.get("includeFiles");
            //Collections.sort(includeFiles);
            for (File includeFile : includeFiles) {
                buff.append(FileUtils.readFileToString(includeFile) + "\n");
            }
            if (buff.toString().length() > 0) {
                TolvenReplaceRegExp.replaceregexp(targetFile, (String) info.get("matchExp"), buff.toString());
            }
        }
    }

    protected void assembleTagLibs(PluginDescriptor pd, File myWARPluginDir) throws IOException, XMLStreamException {
        PluginDescriptor tagLibPD = getTagLibPluginDescriptor();
        ExtensionPoint tagLibExnPt = tagLibPD.getExtensionPoint("taglib");
        if (tagLibExnPt == null) {
            throw new RuntimeException(tagLibPD.getUniqueId() + " must have a taglib extension point");
        }
        for (Extension taglibExn : tagLibExnPt.getConnectedExtensions()) {
            if (pd.getId().equals(taglibExn.getParameter("target-plugin-id").valueAsString())) {
                addTagInfo(taglibExn, pd, myWARPluginDir);
            }
        }
    }

    private void assembleWARClasses(Extension exn, File pluginWARDir) throws IOException {
        String srcDirname = exn.getParameter("dir").valueAsString();
        File srcDir = getFilePath(exn.getDeclaringPluginDescriptor(), srcDirname);
        assembleWARClasses(srcDir, pluginWARDir);
    }

    private void assembleWARClasses(File srcDir, File pluginWARDir) throws IOException {
        File destDir = new File(pluginWARDir + "/WEB-INF/classes");
        logger.debug("Copy " + srcDir.getPath() + " to " + destDir.getPath());
        destDir.mkdirs();
        FileUtils.copyDirectory(srcDir, destDir);
    }

    @Deprecated
    protected void assembleWebDirectory(Extension exn, File destDir) throws IOException {
        Parameter srcDirnameParam = exn.getParameter("sourceDirectory");
        if (srcDirnameParam == null || srcDirnameParam.valueAsString().trim().length() == 0) {
            throw new RuntimeException(exn.getUniqueId() + " does not have a sourceDirectory parameter value");
        }
        String srcDirname = srcDirnameParam.valueAsString();
        File srcDir = getFilePath(exn.getDeclaringPluginDescriptor(), srcDirname);
        logger.debug("Copy " + srcDir.getPath() + " to " + destDir.getPath());
        FileUtils.copyDirectory(srcDir, destDir);
    }

    @Deprecated
    protected void assembleWebDirectory(PluginDescriptor pd, File myWARPluginDir) throws IOException {
        PluginDescriptor abstractWarPD = getAbstractWARPluginDescriptor();
        ExtensionPoint abstractExnPt = abstractWarPD.getExtensionPoint("webDirectory");
        if (abstractExnPt == null) {
            throw new RuntimeException(abstractWarPD.getUniqueId() + " does not have a webDirectory extension point");
        }
        /*
         * Extensions directly connected to the webDirectory extension-point
         * These contribute a new webDirectory to the war file
         */
        for (Extension exn : abstractExnPt.getConnectedExtensions()) {
            Parameter pluginIdParam = exn.getParameter("target-plugin-id");
            if (pluginIdParam == null || pluginIdParam.valueAsString() == null || pluginIdParam.valueAsString().trim().length() == 0) {
                throw new RuntimeException(exn.getUniqueId() + " does not have a default parameter target-plugin-id value");
            }
            if (pluginIdParam.valueAsString().equals(pd.getId())) {
                Parameter functionParam = exn.getParameter("function");
                if (functionParam == null || functionParam.valueAsString() == null || functionParam.valueAsString().trim().length() == 0) {
                    throw new RuntimeException(exn.getUniqueId() + " does not have a parameter function value");
                }
                File destDir = new File(myWARPluginDir, functionParam.valueAsString());
                assembleWebDirectory(exn, destDir);
            }
        }
        /*
         * Extensions directly connected to the descendent of the webDirectory extension-point
         * These are webDirectories offered directly by the war file
         */
        for (ExtensionPoint descendentExPt : abstractExnPt.getDescendants()) {
            ParameterDefinition pluginIdParamDef = descendentExPt.getParameterDefinition("target-plugin-id");
            if (pluginIdParamDef == null || pluginIdParamDef.getDefaultValue() == null || pluginIdParamDef.getDefaultValue().trim().length() == 0) {
                throw new RuntimeException(descendentExPt.getUniqueId() + " does not have a default parameter-def target-plugin-id value");
            }
            if (pluginIdParamDef.getDefaultValue().equals(pd.getId())) {
                ParameterDefinition functionParamDef = descendentExPt.getParameterDefinition("function");
                if (functionParamDef == null || functionParamDef.getDefaultValue() == null || functionParamDef.getDefaultValue().trim().length() == 0) {
                    throw new RuntimeException(descendentExPt.getUniqueId() + " does not have a default parameter-def function value");
                }
                String defaultWebDirname = functionParamDef.getDefaultValue();
                File destDir = new File(myWARPluginDir, defaultWebDirname);
                for (Extension exn : descendentExPt.getConnectedExtensions()) {
                    assembleWebDirectory(exn, destDir);
                }
            }
        }
    }

    protected void assembleWebDirectoryAdaptor(Extension exn, File destDir) throws IOException {
        for (File srcDir : getAdaptorFiles(exn)) {
            logger.debug("Copy " + srcDir.getPath() + " to " + destDir.getPath());
            FileUtils.copyDirectory(srcDir, destDir);
        }
    }

    protected void assembleWebDirectoryAdaptors(PluginDescriptor pd, String contextId, File myWARPluginDir) throws IOException {
    	ExtensionPoint exnPt = pd.getExtensionPoint(EXNPT_WEBDIRECTORY_ADPTR);
    	if (exnPt != null) {
	    	List<String> corePlugins = new ArrayList<String>();
	    	//try assembling the core plugins first
	    	PluginPropertyDetail propertyDetail = getPluginProperty(pd.getId(), CORE_PLUGIN_CONTRIBUTORS);
	    	if (propertyDetail != null && propertyDetail.getValue().trim().length() != 0){
	    		logger.debug("found coreContributors"+propertyDetail.getValue());
	    		corePlugins = Arrays.asList(propertyDetail.getValue().split(","));
				for (Extension exn : exnPt.getConnectedExtensions()) {
					if(corePlugins.contains(exn.getDeclaringPluginDescriptor().getId())){
						assembleWebDirectoryAdaptor(exn,contextId,myWARPluginDir);  
					}
				}
	    	}	
	    	//exclude the plugins already assembled
            for (Extension exn : exnPt.getConnectedExtensions()) {
            	if(!corePlugins.contains(exn.getDeclaringPluginDescriptor().getId())){
            		assembleWebDirectoryAdaptor(exn,contextId,myWARPluginDir);
            	}else{
            		logger.debug("skipping "+exn.getDeclaringPluginDescriptor().getId());
            	}
            }
        }
    }
    private void assembleWebDirectoryAdaptor(Extension exn,String contextId,File myWARPluginDir) throws IOException{
    	Parameter parentContextIdParam = exn.getParameter("parent-context-id");
        if (parentContextIdParam == null || contextId.equals(parentContextIdParam.valueAsString())) {
            String targetDirectory = exn.getParameter("targetWebDirectory").valueAsString();
            if (targetDirectory == null || targetDirectory.trim().length() == 0) {
                throw new RuntimeException(exn.getUniqueId() + " does not have a value for a targetDirectory paramater");
            }
            File destDir = new File(myWARPluginDir, targetDirectory);
            assembleWebDirectoryAdaptor(exn, destDir);
        }
    }


    protected void assembleWebInfClassesAdaptors(PluginDescriptor pd, String contextId, File myWARPluginDir) throws IOException {
        ExtensionPoint exnPt = pd.getExtensionPoint(EXNPT_WEBINFCLASSES_ADPTR);
        if (exnPt != null) {
            for (Extension exn : exnPt.getConnectedExtensions()) {
                Parameter parentContextIdParam = exn.getParameter("parent-context-id");
                if (parentContextIdParam == null || contextId.equals(parentContextIdParam.valueAsString())) {
                    for (File srcDir : getAdaptorFiles(exn)) {
                        assembleWARClasses(srcDir, myWARPluginDir);
                    }
                }
            }
        }
    }

    protected void assembleWebInfFileAdaptors(PluginDescriptor pd, String contextId, File myWARPluginDir) throws IOException {
        ExtensionPoint exnPt = pd.getExtensionPoint(EXNPT_WEBINF_FILE_ADPTR);
        if (exnPt != null) {
            File destDir = new File(myWARPluginDir + "/WEB-INF");
            destDir.getParentFile().mkdirs();
            for (Extension exn : exnPt.getConnectedExtensions()) {
                Parameter parentContextIdParam = exn.getParameter("parent-context-id");
                if (parentContextIdParam == null || contextId.equals(parentContextIdParam.valueAsString())) {
                    for (File src : getAdaptorFiles(exn)) {
                        logger.debug("Copy " + src.getPath() + " to " + destDir.getPath());
                        FileUtils.copyFileToDirectory(src, destDir);
                    }
                }
            }
        }
    }

    protected void assembleWebInfLibAdaptors(PluginDescriptor pd, String contextId, File myWARPluginDir) throws IOException {
        ExtensionPoint exnPt = pd.getExtensionPoint(EXNPT_WEBINF_LIB_ADPTR);
        if (exnPt != null) {
            File destDir = new File(myWARPluginDir + "/WEB-INF/lib");
            destDir.getParentFile().mkdirs();
            for (Extension exn : exnPt.getConnectedExtensions()) {
                Parameter parentContextIdParam = exn.getParameter("parent-context-id");
                if (parentContextIdParam == null || contextId.equals(parentContextIdParam.valueAsString())) {
                    for (File src : getAdaptorFiles(exn)) {
                        logger.debug("Copy " + src.getPath() + " to " + destDir.getPath());
                        FileUtils.copyFileToDirectory(src, destDir);
                    }
                }
            }
        }
    }

    @Deprecated
    protected void assembleWebInfLibs(PluginDescriptor pd, File myWARPluginDir) throws IOException {
        File destDir = new File(myWARPluginDir + "/WEB-INF/lib");
        destDir.getParentFile().mkdirs();
        for (ExtensionPoint extnPt : pd.getExtensionPoints()) {
            if (EXNPT_WEBINFLIB.equals(extnPt.getParentExtensionPointId()) && extnPt.getParentPluginId().equals(getDescriptor().getId())) {
                for (Extension exn : extnPt.getConnectedExtensions()) {
                    String srcName = exn.getParameter("jar").valueAsString();
                    File src = getFilePath(exn.getDeclaringPluginDescriptor(), srcName);
                    logger.debug("Copy " + src.getPath() + " to " + destDir.getPath());
                    FileUtils.copyFileToDirectory(src, destDir);
                }
            }
        }
    }

    protected void assembleWebRootFilesAdaptors(PluginDescriptor pd, String contextId, File myWARPluginDir) throws IOException {
        ExtensionPoint exnPt = pd.getExtensionPoint(EXNPT_WEBROOTFILES_ADPTR);
        if (exnPt != null) {
            //For the root directory, only copy files. Otherwise a webdirectory can be used
            FileFilter ff = new FileFilter() {
                public boolean accept(File file) {
                    return file.isFile();
                }
            };
            for (Extension exn : exnPt.getConnectedExtensions()) {
                Parameter parentContextIdParam = exn.getParameter("parent-context-id");
                if (parentContextIdParam == null || contextId.equals(parentContextIdParam.valueAsString())) {
                    for (File srcDir : getAdaptorFiles(exn)) {
                        File destDir = new File(myWARPluginDir + "/");
                        logger.debug("Copy " + srcDir.getPath() + " to " + destDir.getPath());
                        destDir.mkdirs();
                        FileUtils.copyDirectory(srcDir, destDir, ff);
                    }
                }
            }
        }
    }

    @Override
    protected void doStart() throws Exception {
        logger.debug("*** start ***");
        logger.debug("deleting: " + getPluginTmpDir());
        FileUtils.deleteDirectory(getPluginTmpDir());
        getPluginTmpDir().mkdirs();
    }

    @Override
    protected void doStop() throws Exception {
        logger.debug("*** stop ***");
    }

    @Override
    public void execute(String[] args) throws Exception {
        logger.debug("*** execute ***");
        CommandLine commandLine = getCommandLine(args);
        String warPluginId = commandLine.getOptionValue(CMD_WAR_PLUGIN_OPTION);
        String webURI = commandLine.getOptionValue(CMD_WEB_URI_OPTION);
        String contextId = commandLine.getOptionValue(CMD_CONTEXT_ID_OPTION);
        String destDirname = commandLine.getOptionValue(CMD_DESTDIR_OPTION);
        if (destDirname == null) {
            destDirname = new File(getStageDir(), warPluginId).getPath();
        }
        PluginDescriptor warPD = getManager().getRegistry().getPluginDescriptor(warPluginId);
        List<ExtensionPoint> declExnPts = new ArrayList<ExtensionPoint>();
        for (ExtensionPoint exnPt : warPD.getExtensionPoints()) {
            if (EXNPT_WARMODULE_DECL.equals(exnPt.getParentExtensionPointId())) {
                declExnPts.add(exnPt);
            }
        }
        if (declExnPts.isEmpty()) {
            throw new RuntimeException(warPD.getUniqueId() + " does not define a warModule-declaration extension-point");
        }
        if (contextId == null) {
            if (declExnPts.size() > 1) {
                throw new RuntimeException(warPD.getId() + " has more than one warModule-declaration. A context-id must be provided");
            }
            if (declExnPts.get(0).getParameterDefinition("context-id") == null) {
                throw new RuntimeException(declExnPts.get(0).getUniqueId() + " must define a context-id parameter");
            }
            contextId = declExnPts.get(0).getParameterDefinition("context-id").getDefaultValue();
        }
        File myPluginTmpDir = getPluginTmpDir();
        File myWARPluginDir = new File(myPluginTmpDir, warPD.getId() + "-" + contextId);
        File srcWARFile = getFilePath(warPD, webURI);
        if (srcWARFile.exists()) {
            logger.debug("Unjar " + srcWARFile.getPath() + " to " + myWARPluginDir.getPath());
            FileUtils.deleteDirectory(myWARPluginDir);
            myWARPluginDir.mkdirs();
            TolvenJar.unjar(srcWARFile, myWARPluginDir);
        } else {
            assembleWebInfFileAdaptors(warPD, contextId, myWARPluginDir);
        }
        executeRequiredPlugins(warPluginId, contextId, myWARPluginDir);
        assembleWebDirectoryAdaptors(warPD, contextId, myWARPluginDir);
        assembleWebDirectory(warPD, myWARPluginDir);
        assembleWebRootFilesAdaptors(warPD, contextId, myWARPluginDir);      
        assembleScriptIncludes(warPD, myWARPluginDir);
        assembleTagLibs(warPD, myWARPluginDir);
        assembleMetaInf(warPD, myWARPluginDir);
        assembleWebInfLibs(warPD, myWARPluginDir);
        assembleWebInfLibAdaptors(warPD, contextId, myWARPluginDir);
        assembleClasses(warPD, myWARPluginDir);
        assembleWebInfClassesAdaptors(warPD, contextId, myWARPluginDir);
        assembleProperties(warPD, myWARPluginDir);
        File destinationWARFile = new File(destDirname, webURI);
        destinationWARFile.getParentFile().mkdirs();
        logger.debug("Jar " + myWARPluginDir.getPath() + " to " + destinationWARFile.getPath());
        TolvenJar.jar(myWARPluginDir, destinationWARFile);
    }

    protected void executeRequiredPlugins(String warPluginId, String contextId, File myWARPluginDir) throws Exception {
        File destDir = new File(myWARPluginDir, "WEB-INF");
        destDir.mkdirs();
        String argString = "-warPlugin " + warPluginId + " -contextId " + contextId + " -destDir " + destDir;
        new WebXMLAssembler(this).execute(argString.split(" "));
        new FacesConfigAssembler(this).execute(argString.split(" "));
    }

    protected PluginDescriptor getAbstractWARPluginDescriptor() {
        ExtensionPoint exnPt = getDescriptor().getExtensionPoint(EXNPT_ABSTRACT_WAR);
        String parentPluginId = exnPt.getParentPluginId();
        PluginDescriptor pd = getManager().getRegistry().getPluginDescriptor(parentPluginId);
        return pd;
    }

    private List<File> getAdaptorFiles(Extension exn) {
        String pluginId = exn.getParameter(EXNPT_SRC_PLUGIN_ID).valueAsString();
        if (pluginId == null || pluginId.trim().length() == 0) {
            throw new RuntimeException("No parameter value for " + EXNPT_SRC_PLUGIN_ID + " found in " + exn.getUniqueId());
        }
        String exnPtId = exn.getParameter(EXNPT_ID).valueAsString();
        if (exnPtId == null || exnPtId.trim().length() == 0) {
            throw new RuntimeException("No parameter value for " + EXNPT_ID + " found in " + exn.getUniqueId());
        }
        ExtensionPoint exnPt = getManager().getRegistry().getExtensionPoint(pluginId + "@" + exnPtId);
        List<File> files = new ArrayList<File>();
        for (ParameterDefinition paramDef : exnPt.getParameterDefinitions()) {
            String filename = paramDef.getDefaultValue();
            if (filename == null || filename.trim().length() == 0) {
                throw new RuntimeException("No default-value for parameter-def found in " + exnPt.getUniqueId());
            }
            File src = getFilePath(exnPt.getDeclaringPluginDescriptor(), filename);
            files.add(src);
        }
        return files;
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
        Option warPluginOption = new Option(CMD_WAR_PLUGIN_OPTION, CMD_WAR_PLUGIN_OPTION, true, "war plugin");
        warPluginOption.setRequired(true);
        cmdLineOptions.addOption(warPluginOption);
        Option webURIPluginOption = new Option(CMD_WEB_URI_OPTION, CMD_WEB_URI_OPTION, true, "web-uri");
        webURIPluginOption.setRequired(true);
        cmdLineOptions.addOption(webURIPluginOption);
        Option contextIdOption = new Option(CMD_CONTEXT_ID_OPTION, CMD_CONTEXT_ID_OPTION, true, CMD_CONTEXT_ID_OPTION);
        cmdLineOptions.addOption(contextIdOption);
        Option destDirPluginOption = new Option(CMD_DESTDIR_OPTION, CMD_DESTDIR_OPTION, true, "destination directory");
        cmdLineOptions.addOption(destDirPluginOption);
        return cmdLineOptions;
    }

    private Map<String, Extension> getNamedExtensions(Collection<Extension> extensions) {
        Map<String, Extension> namedExtensions = new HashMap<String, Extension>(extensions.size());
        for (Extension extension : extensions) {
            namedExtensions.put(extension.getUniqueId(), extension);
        }
        return namedExtensions;
    }

    /** Method to find a property in a plugin in tolven-config/plugins.xml. 
     * This method can find the property only if it exists at one level deep. 
     * @param pluginName
     * @param propertyName
     * @return
     */
    private PluginPropertyDetail getPluginProperty(String pluginName, String propertyName) {
        ConfigPluginsWrapper pluginsWrapper = getPluginsWrapper();
        Plugins plugins = pluginsWrapper.getPlugins();
        for (PluginDetail pluginDetail : plugins.getPlugin()) {
            if (pluginDetail.getId().equalsIgnoreCase(pluginName)) {
                for (PluginPropertyDetail propertyDetail : pluginDetail.getProperty()) {
                    if (propertyDetail.getName().equalsIgnoreCase(propertyName))
                        return propertyDetail;
                }
            }
        }
        return null;
    }

    protected PluginDescriptor getTagLibPluginDescriptor() {
        ExtensionPoint exnPt = getDescriptor().getExtensionPoint(TAGLIB_EXTENSIONPOINT);
        String parentPluginId = exnPt.getParentPluginId();
        PluginDescriptor pd = getManager().getRegistry().getPluginDescriptor(parentPluginId);
        return pd;
    }

    private Map<String, List<String>> propertySequenceMapping(PluginDescriptor pd) {
        Map<String, List<String>> propertySequenceMapping = new HashMap<String, List<String>>();
        Set<Extension> classesExns = new HashSet<Extension>();
        classesExns.addAll(getChildExtensions(EXNPT_WAR_PROPERTY_SEQUENCE, pd));
        PluginDescriptor abstractWARPD = getAbstractWARPluginDescriptor();
        ExtensionPoint propertyExnPt = abstractWARPD.getExtensionPoint(EXNPT_WAR_PROPERTY_SEQUENCE);
        classesExns.addAll(propertyExnPt.getConnectedExtensions());
        for (Extension classesExn : classesExns) {
            Parameter pluginIdParam = classesExn.getParameter("target-plugin-id");
            if (pluginIdParam == null || pluginIdParam.valueAsString().trim().length() == 0) {
                throw new RuntimeException(classesExn.getUniqueId() + " does not have a target-plugin-id parameter value");
            }
            if (pd.getId().equals(pluginIdParam.valueAsString())) {
                String classname = classesExn.getParameter("class").valueAsString();
                String name = classesExn.getParameter("name").valueAsString();
                String key = classname + "." + name;
                if (propertySequenceMapping.get(key) == null) {
                    String sequence = null;
                    if (classesExn.getParameter("sequence") != null) {
                        sequence = classesExn.getParameter("sequence").valueAsString();
                    }
                    String evaluatedSequence = null;
                    if (sequence != null) {
                        evaluatedSequence = evaluate(sequence, classesExn.getDeclaringPluginDescriptor());
                    }
                    if (evaluatedSequence == null && classesExn.getParameter("defaultSequence") != null) {
                        String defaultSequence = classesExn.getParameter("defaultSequence").valueAsString();
                        evaluatedSequence = evaluate(defaultSequence, classesExn.getDeclaringPluginDescriptor());
                    }
                    if (evaluatedSequence != null) {
                        List<String> list = new ArrayList<String>();
                        for (String string : evaluatedSequence.split(",")) {
                            list.add(string);
                        }
                        propertySequenceMapping.put(key, list);
                    }
                } else {
                    throw new RuntimeException(classesExn.getUniqueId() + " is redefining a propertySequence for: " + classname + " when one exists for: " + key);
                }
            }
        }
        return propertySequenceMapping;
    }

    /** Sort the javascript file includes based on the order defined in tolven-config/plugins.xml
     * an example sorting order configuration would be
     * <plugin id="org.tolven.component.tolvenweb">
    	  <root />
    	 	<property name="scriptsOrder" value="org.tolven.component.tolvenweb@prototype,org.tolven.component.tolvenweb@datastructures"/>
    	 </plugin> 
     
     * @param extensions
     * @param propertyDetail
     * @return
     */
    public List<Extension> sortScriptIncludes(Collection<Extension> extensions, PluginPropertyDetail propertyDetail) {
        List<Extension> sortedExtensions = new ArrayList<Extension>(extensions.size());
        Map<String, Extension> namedExtensions = getNamedExtensions(extensions);
        String[] scriptsOrder = propertyDetail.getValue().split(",");
        if (scriptsOrder != null) {
            for (String detail : scriptsOrder) {
                if (namedExtensions.containsKey(detail)) {
                    sortedExtensions.add(namedExtensions.get(detail));
                    namedExtensions.remove(detail);
                }
            }
        }
        //add unordered extensions at the bottom
        sortedExtensions.addAll(namedExtensions.values());
        return sortedExtensions;
    }
}
