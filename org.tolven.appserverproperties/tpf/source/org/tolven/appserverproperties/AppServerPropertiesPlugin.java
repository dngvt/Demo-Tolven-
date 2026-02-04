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
package org.tolven.appserverproperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.ExtensionPoint.ParameterDefinition;
import org.java.plugin.registry.PluginDescriptor;
import org.tolven.appserverproperties.api.AppServerProperties;
import org.tolven.plugin.TolvenCommandPlugin;

/**
 * This plugin updates the appserver with server properties
 * 
 * @author Joseph Isaac
 *
 */
public class AppServerPropertiesPlugin extends TolvenCommandPlugin {

    public static final String CMD_LINE_DISPLAY_OPTION = "display";
    public static final String CMD_LINE_FIND_OPTION = "find";
    public static final String CMD_LINE_LOAD_OPTION = "load";
    public static final String CMD_LINE_REMOVE_OPTION = "remove";
    public static final String CMD_LINE_SET_OPTION = "set";
    public static final String EXTENSIONPOINT_CUSTOM_SERVER_PROPERTIES = "customServerProperties";
    public static final String EXTENSIONPOINT_DEFAULT_SERVER_PROPERTIES = "defaultServerProperties";
    public static final String EXTNPT_TOLVENPROPERTIES = "tolvenProperties";

    private Logger logger = Logger.getLogger(AppServerPropertiesPlugin.class);

    @Override
    protected void doStart() throws Exception {
        logger.info("*** start ***");
    }

    @Override
    protected void doStop() throws Exception {
        logger.info("*** stop ***");
    }

    @Override
    public void execute(String[] args) {
        logger.info("*** execute ***");
        CommandLine commandLine = getCommandLine(args);
        String adminId = getTolvenConfigWrapper().getAdminId();
        char[] adminPassword = getTolvenConfigWrapper().getAdminPassword();
        AppServerProperties loadServerProperties = new AppServerProperties(adminId, adminPassword);
        if (commandLine.hasOption(CMD_LINE_DISPLAY_OPTION)) {
            logger.info("Displaying server properties...");
            loadServerProperties.displayProperties();
        } else if (commandLine.hasOption(CMD_LINE_LOAD_OPTION)) {
            logger.info("Importing server properties...");
            List<File> propertiesFiles = getPropertiesFiles();
            loadServerProperties.uploadPropertiesFiles(propertiesFiles);
            Properties properties = getContributedProperties();
            if(!properties.isEmpty()) {
                loadServerProperties.setProperties(properties);
            }
        } else if (commandLine.hasOption(CMD_LINE_FIND_OPTION)) {
            String property = commandLine.getOptionValue(CMD_LINE_FIND_OPTION);
            logger.info("Find server property: " + property);
            String value = loadServerProperties.findProperty(property);
            System.out.println(value);
        } else if (commandLine.hasOption(CMD_LINE_SET_OPTION)) {
            Properties optionProperties = commandLine.getOptionProperties(CMD_LINE_SET_OPTION);
            String propertyName = (String) optionProperties.keys().nextElement();
            String propertyValue = optionProperties.getProperty(propertyName);
            logger.info("Setting server property: " + propertyName);
            loadServerProperties.setProperty(propertyName, propertyValue);
        } else if (commandLine.hasOption(CMD_LINE_REMOVE_OPTION)) {
            String property = commandLine.getOptionValue(CMD_LINE_REMOVE_OPTION);
            logger.info("Removing server property: " + property);
            loadServerProperties.removeProperty(property);
        } else {
            throw new RuntimeException("Unrecognized command line option:" + commandLine);
        }
        logger.info("server properties completed");
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
        OptionGroup optionGroup = new OptionGroup();
        Option displayOption = new Option(CMD_LINE_DISPLAY_OPTION, CMD_LINE_DISPLAY_OPTION, false, "\"display server properties\"");
        optionGroup.addOption(displayOption);
        Option importOption = new Option(CMD_LINE_LOAD_OPTION, CMD_LINE_LOAD_OPTION, false, "\"load server properties\"");
        optionGroup.addOption(importOption);
        Option changeOption = new Option(CMD_LINE_SET_OPTION, CMD_LINE_SET_OPTION, true, "\"set a server property e.g. -set someKey someValue\"");
        changeOption.setArgs(2);
        optionGroup.addOption(changeOption);
        Option removeOption = new Option(CMD_LINE_REMOVE_OPTION, CMD_LINE_REMOVE_OPTION, true, "\"remove a server property e.g. -remove someKey\"");
        optionGroup.addOption(removeOption);
        optionGroup.setRequired(true);
        cmdLineOptions.addOptionGroup(optionGroup);
        return cmdLineOptions;
    }

    private Properties getContributedProperties() {
        Properties properties = new Properties();
        List<PluginDescriptor> pluginDescriptors = new ArrayList<PluginDescriptor>();
        pluginDescriptors.addAll(getManager().getRegistry().getPluginDescriptors());
        pluginDescriptors.remove(getDescriptor());
        pluginDescriptors.add(0, getDescriptor());
        for (final PluginDescriptor pluginDescriptor : pluginDescriptors) {
            ExtensionPoint tolvenPropertiesExnPt = pluginDescriptor.getExtensionPoint(EXTNPT_TOLVENPROPERTIES);
            if (tolvenPropertiesExnPt != null) {
                for (ParameterDefinition paramDef : tolvenPropertiesExnPt.getParameterDefinitions()) {
                    String name = paramDef.getId();
                    String rawValue = paramDef.getDefaultValue();
                    String value = evaluate(rawValue, pluginDescriptor);
                    if (value != null) {
                        properties.setProperty(name, value);
                    }
                }
            }
        }
        return properties;
    }

    private File getCustomServerPropertiesFile(Extension extension) {
        PluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
        String propertiesFilename = extension.getParameter("appserver.propertiesFile").valueAsString();
        return getServerPropertiesFile(propertiesFilename, pluginDescriptor);
    }

    private File getDefaultPropertiesFile() {
        ExtensionPoint extensionPoint = getDescriptor().getExtensionPoint(EXTENSIONPOINT_DEFAULT_SERVER_PROPERTIES);
        String propertiesFilename = extensionPoint.getParameterDefinition("appserver.default.propertiesFile").getDefaultValue();
        return getServerPropertiesFile(propertiesFilename, getDescriptor());
    }

    private List<File> getPropertiesFiles() {
        List<File> propertiesFiles = new ArrayList<File>();
        File defaultPropertiesFile = getDefaultPropertiesFile();
        if (defaultPropertiesFile != null) {
            propertiesFiles.add(defaultPropertiesFile);
        }
        ExtensionPoint extensionPoint = getDescriptor().getExtensionPoint(EXTENSIONPOINT_CUSTOM_SERVER_PROPERTIES);
        for (Extension extension : extensionPoint.getConnectedExtensions()) {
            File propertiesFile = getCustomServerPropertiesFile(extension);
            propertiesFiles.add(propertiesFile);
        }
        return propertiesFiles;
    }

    private File getServerPropertiesFile(String propertiesFilename, PluginDescriptor pluginDescriptor) {
        String eval_propertiesFilename = evaluate(propertiesFilename, pluginDescriptor);
        if (eval_propertiesFilename == null) {
            throw new RuntimeException(pluginDescriptor.getUniqueId() + " propertiesFile evaluated to null using: " + propertiesFilename);
        }
        File eval_propertiesFile = new File(eval_propertiesFilename);
        File propertiesFile = null;
        if (isAbsoluteFilePath(eval_propertiesFile)) {
            propertiesFile = eval_propertiesFile;
        } else {
            propertiesFile = getFilePath(pluginDescriptor, eval_propertiesFile.getName());
        }
        if (!propertiesFile.exists()) {
            throw new RuntimeException("Missing server properties file: " + (isAbsoluteFilePath(eval_propertiesFile) ? propertiesFile.getPath() : getPluginZip(pluginDescriptor) + "!/" + eval_propertiesFilename));
        }
        return propertiesFile;
    }
}
