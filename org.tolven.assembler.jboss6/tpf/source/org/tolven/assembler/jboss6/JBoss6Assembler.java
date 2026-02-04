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
 * @version $Id: JBoss6Assembler.java 1881 2011-07-28 03:56:25Z joe.isaac $
 */
package org.tolven.assembler.jboss6;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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
import org.tolven.naming.JndiManager;
import org.tolven.naming.JndiManagerFactory;
import org.tolven.plugin.TolvenCommandPlugin;
import org.tolven.tools.ant.TolvenJar;

/**
 * This plugin assembles all of the tolven specific configuration files for the JBoss6 appserver
 * 
 * @author Joseph Isaac
 *
 */
public class JBoss6Assembler extends TolvenCommandPlugin {

    public static final String ATTR_STAGE_CONFIG_DIR = "configDir";
    public static final String ATTR_STAGE_DEPLOY_DIR = "deployDir";
    public static final String ATTR_STAGE_LIB = "libDir";
    public static final String CMD_JNDI = "jndi";
    public static final String CMD_LINE_DESTDIR_OPTION = "destDir";
    public static final String EXNPT_CONFIG = "config";
    public static final String EXNPT_DB_PLUGIN = "databasePlugin";
    public static final String EXNPT_DEPLOY = "deploy";
    public static final String EXNPT_ID = "extension-point";
    public static final String EXNPT_LIB_CLASSES = "classes";
    public static final String EXNPT_LIBJAR = "libJar";
    public static final String EXNPT_LIBPROD_ADPTR = "libProduct-adaptor";
    public static final String SRC_PLUGIN_ID = "source-plugin-id";
    public static final String TABS = "\t\t\t\t\t";

    private String appServerDirname;

    private Logger logger = Logger.getLogger(JBoss6Assembler.class);

    protected void assembleConfigFiles() throws IOException {
        ExtensionPoint exnPt = getDescriptor().getExtensionPoint(EXNPT_CONFIG);
        ExtensionPoint appServerExnPt = getParentExtensionPoint(exnPt);
        String relativeConfigExtDirPath = getDescriptor().getAttribute(ATTR_STAGE_CONFIG_DIR).getValue();
        File destDir = new File(getPluginTmpDir(), getAppServerDirname() + "/" + relativeConfigExtDirPath);
        for (Extension exn : appServerExnPt.getConnectedExtensions()) {
            PluginDescriptor pd = exn.getDeclaringPluginDescriptor();
            for (Parameter param : exn.getParameters("file")) {
                File src = getFilePath(pd, param.valueAsString());
                FileUtils.copyFileToDirectory(src, destDir);
            }
            for (Parameter param : exn.getParameters("dir")) {
                File srcDir = getFilePath(pd, param.valueAsString());
                FileUtils.copyDirectory(srcDir, destDir);
            }
        }
    }

    protected void assembleDeployFiles() throws IOException {
        ExtensionPoint exnPt = getDescriptor().getExtensionPoint(EXNPT_DEPLOY);
        ExtensionPoint appServerExnPt = getParentExtensionPoint(exnPt);
        String relativeConfigExtDirPath = getDescriptor().getAttribute(ATTR_STAGE_DEPLOY_DIR).getValue();
        File destDir = new File(getPluginTmpDir(), getAppServerDirname() + "/" + relativeConfigExtDirPath);
        for (Extension exn : appServerExnPt.getConnectedExtensions()) {
            PluginDescriptor pd = exn.getDeclaringPluginDescriptor();
            for (Parameter param : exn.getParameters("file")) {
                File src = getFilePath(pd, param.valueAsString());
                FileUtils.copyFileToDirectory(src, destDir);
            }
            for (Parameter param : exn.getParameters("dir")) {
                File srcDir = getFilePath(pd, param.valueAsString());
                FileUtils.copyDirectoryToDirectory(srcDir, destDir);
            }
        }
    }

    private void assembleJNDI(Properties srcProperties) {
        JndiManager jndiManager = JndiManagerFactory.getInstance();
        Properties jndiProperties = jndiManager.getJndiProperties(srcProperties);
        File stageAppServerDir = new File(getStageDir(), getAppServerDirname());
        String relativeConfigExtDirPath = getDescriptor().getAttribute(ATTR_STAGE_DEPLOY_DIR).getValue();
        File destDir = new File(stageAppServerDir, "/" + relativeConfigExtDirPath);
        File dest = new File(destDir, "tolven-jndi-service.xml");
        StringBuffer buff = new StringBuffer();
        try {
            String prefix = FileUtils.readFileToString(getFilePath("tolven-jndi-service-prefix.txt"));
            String appserverHome = srcProperties.getProperty("appserver.home");
            prefix = prefix.replace("${appserver.home}", appserverHome);
            buff.append(prefix);
            List<String> keys = new ArrayList<String>(jndiProperties.stringPropertyNames());
            Collections.sort(keys);
            for (String key : keys) {
                String value = xmlEscape(jndiProperties.getProperty(key));
                if (value == null) {
                    throw new RuntimeException("null value for JNDI property: " + key);
                }
                buff.append("\n" + TABS + "<tolven:property>");
                buff.append("\n" + TABS + "\t<tolven:key>" + key + "</tolven:key>");
                buff.append("\n" + TABS + "\t<tolven:value>" + value + "</tolven:value>");
                buff.append("\n" + TABS + "</tolven:property>");
            }
            buff.append("\n");
            buff.append(FileUtils.readFileToString(getFilePath("tolven-jndi-service-suffix.txt")));
            String substitutedString = substitute(buff.toString(), srcProperties);
            if (substitutedString.indexOf("${") != -1) {
                int index = substitutedString.indexOf("${");
                String s = substitutedString.substring(index);
                throw new RuntimeException("Missing JNDI substitution: ${" + s.substring(0, s.indexOf("}")) + "}");
            }
            FileUtils.writeStringToFile(dest, substitutedString);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create " + dest.getPath(), ex);
        }
    }

    protected void assembleLibClasses() {
        String relativeLibDirPath = getDescriptor().getAttribute(ATTR_STAGE_LIB).getValue();
        File tmpStageLibDir = new File(getPluginTmpDir(), getAppServerDirname() + "/" + relativeLibDirPath);
        File destJar = new File(tmpStageLibDir, getDescriptor().getId() + ".jar");
        destJar.delete();
        destJar.getParentFile().mkdirs();
        ExtensionPoint classesExnPt = getDescriptor().getExtensionPoint(EXNPT_LIB_CLASSES);
        for (Extension classesExn : classesExnPt.getConnectedExtensions()) {
            PluginDescriptor pd = classesExn.getDeclaringPluginDescriptor();
            String srcDirname = classesExn.getParameter("dir").valueAsString();
            String eval_srcDirname = (String) evaluate(srcDirname, pd);
            if (eval_srcDirname == null) {
                throw new RuntimeException("plugin property: dir '" + srcDirname + "'evaluated to: null for: " + pd);
            }
            File srcDir = getFilePath(pd, srcDirname);
            TolvenJar.jarDir(srcDir, destJar, true);
        }
    }

    /**
     * Add libraries located by extension-point libProduct-adaptor to the appserver lib directory
     * 
     * @param pd
     * @throws IOException
     */
    protected void assembleLibProductAdaptors() throws IOException {
        ExtensionPoint exnPt = getDescriptor().getExtensionPoint(EXNPT_LIBPROD_ADPTR);
        ExtensionPoint appServerExnPt = getParentExtensionPoint(exnPt);
        String relativeLibExtDirPath = getDescriptor().getAttribute(ATTR_STAGE_LIB).getValue();
        File destDir = new File(getPluginTmpDir(), getAppServerDirname() + "/" + relativeLibExtDirPath);
        for (Extension exn : appServerExnPt.getConnectedExtensions()) {
            for (File src : getAdaptorFiles(exn)) {
                FileUtils.copyFileToDirectory(src, destDir);
            }
        }
    }

    protected void copyToStageDir() throws IOException {
        File tmpAppServerStageDir = new File(getPluginTmpDir(), getAppServerDirname());
        File stageAppServerDir = new File(getStageDir(), getAppServerDirname());
        logger.debug("Copy " + tmpAppServerStageDir.getPath() + " to " + stageAppServerDir.getPath());
        stageAppServerDir.mkdirs();
        FileUtils.copyDirectory(tmpAppServerStageDir, stageAppServerDir);
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
        if (commandLine.hasOption(CMD_JNDI)) {
            Properties srcProperties = getPluginsWrapper().getProperties();
            assembleJNDI(srcProperties);
        } else {
            File[] tmpFiles = getPluginTmpDir().listFiles();
            if (tmpFiles != null && tmpFiles.length > 0) {
                return;
            }
            executeRequiredPlugins(args);
            assembleLibProductAdaptors();
            assembleLibClasses();
            assembleConfigFiles();
            assembleDeployFiles();
            upgradeJAXB();
            copyToStageDir();
        }
    }
    
    protected void executeRequiredPlugins(String[] args) throws Exception {
        ExtensionPoint dbPluginExnPt = getDescriptor().getExtensionPoint(EXNPT_DB_PLUGIN);
        Extension dbPluginExn = getSingleConnectedExtension(dbPluginExnPt);
        String dbPD = dbPluginExn.getDeclaringPluginDescriptor().getId();
        execute(dbPD, args);
    }

    private List<File> getAdaptorFiles(Extension exn) {
        String pluginId = exn.getParameter(SRC_PLUGIN_ID).valueAsString();
        if (pluginId == null || pluginId.trim().length() == 0) {
            throw new RuntimeException("No parameter value for " + SRC_PLUGIN_ID + " found in " + exn.getUniqueId());
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

    protected String getAppServerDirname() {
        if (appServerDirname == null) {
            String appserverPathname = getPluginsWrapper().getProperties().getProperty("appserver.home");
            File appserverDir = new File(appserverPathname);
            appServerDirname = appserverDir.getName();
        }
        return appServerDirname;
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
        Option installBuildDirOption = new Option(CMD_JNDI, CMD_JNDI, false, "\"" + CMD_JNDI + " deploy jndi properties\"");
        cmdLineOptions.addOption(installBuildDirOption);
        return cmdLineOptions;
    }

    private String substitute(String string, Properties properties) {
        String substitutedString = string;
        for (String key : properties.stringPropertyNames()) {
            substitutedString = substitutedString.replace("${" + key + "}", properties.getProperty(key));
        }
        return substitutedString;
    }

    private void upgradeJAXB() throws Exception {
        File mainAppserverLibDir = new File(getPluginTmpDir(), getAppServerDirname() + "/lib");
        File srcJaxbJar = getFilePath("jaxb-impl.jar");
        FileUtils.copyFileToDirectory(srcJaxbJar, mainAppserverLibDir);
    }

    private String xmlEscape(String string) {
        return string.replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(";", "&apos").replaceAll("&", "&amp;");
    }

}
