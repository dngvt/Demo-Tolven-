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
package org.tolven.plugin.boot;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.xml.DOMConfigurator;
import org.java.plugin.boot.Boot;
import org.tolven.plugin.repository.ConfigPluginsWrapper;
import org.tolven.plugin.repository.RepositoryMetadata;
import org.tolven.plugin.repository.RepositoryUpgrade;

/**
 * The main entry point class for the Tolven Plugin Framework
 * 
 * @author Joseph Isaac
 *
 */
public class TPFBoot {

    public static final String BOOT_PROPERTIES_CONFIG = "jpf.boot.config";
    public static final String CMD_LINE_CONF_OPTION = "conf";
    public static final String CMD_LINE_GENMETADATA_OPTION = "genMetadata";
    public static final String CMD_LINE_GETPLUGINS_OPTION = "getPlugins";
    public static final String CMD_LINE_PLUGIN_OPTION = "plugin";
    public static final String CMD_LINE_PLUGINSXML_OPTION = "pluginsxml";
    public static final String CMD_LINE_PROPS_FILE_OPTION = "propsFile";
    public static final String CMD_LINE_VERSION_OPTION = "version";
    public static final String DEFAULT_LOG_FILE = System.getProperty("user.dir") + "/tolven.log";
    public static final String ENV_CONF = "TOLVEN_CONFIG_DIR";
    public static final String JPF_BOOT_PLUGINS_REPOSITORIES = "org.java.plugin.boot.pluginsRepositories";
    public static final String JPF_BOOT_SHADOW_FOLDER_REPOSITORIES = "org.java.plugin.standard.ShadingPathResolver.shadowFolder";
    public static final String LOG_FILE_PROPERTY = "tolven.log.file";
    public static final Logger logger = Logger.getLogger(TPFBoot.class);
    public static ConfigPluginsWrapper pluginsWrapper;
    public static final String TPF_VERSION_PROPERTY = "TPF_VERSION";

    public static void main(String[] args) throws Exception {
        try {
            new TPFBoot().run(args);
        } catch (Exception ex) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(baos));
            logger.error(new String(baos.toByteArray(), "UTF-8"));
            throw ex;
        }
    }

    private void addBootRepositoryRuntime(ConfigPluginsWrapper pluginsWrapper, File configDir) {
        File bootPropertiesFile = new File(configDir, "boot.properties");
        if (!bootPropertiesFile.exists()) {
            throw new RuntimeException("Could not locate boot file: " + bootPropertiesFile.getPath());
        }
        Properties bootProperties = null;
        try {
            bootProperties = load(bootPropertiesFile);
        } catch (Exception ex) {
            throw new RuntimeException("Could not load boot properties file:" + bootPropertiesFile.getPath());
        }
        bootProperties.put(JPF_BOOT_PLUGINS_REPOSITORIES, pluginsWrapper.getRepositoryRuntimePluginsDir().getPath().replace("\\", "/"));
        bootProperties.put(JPF_BOOT_SHADOW_FOLDER_REPOSITORIES, pluginsWrapper.getRepositoryRuntimeUnpackedDir().getPath().replace("\\", "/"));
        File generatedBootPropertiesFile = null;
        try {
            generatedBootPropertiesFile = File.createTempFile("tpf_", "_" + bootPropertiesFile.getName());
            generatedBootPropertiesFile.deleteOnExit();
            store(bootProperties, generatedBootPropertiesFile);
            logger.info("Generated boot.properties: " + generatedBootPropertiesFile.getPath());
            System.setProperty(BOOT_PROPERTIES_CONFIG, generatedBootPropertiesFile.getPath());
        } catch (Exception ex) {
            throw new RuntimeException("Could not store generate boot properties file to:" + generatedBootPropertiesFile.getPath(), ex);
        }
    }

    private String getCommandLineConfigDir(CommandLine commandLine) {
        String configDir = commandLine.getOptionValue(CMD_LINE_CONF_OPTION);
        if (configDir == null) {
            configDir = System.getenv(ENV_CONF);
            if (configDir == null) {
                System.out.print("Please enter config directory: ");
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                try {
                    configDir = input.readLine();
                } catch (IOException ex) {
                    throw new RuntimeException("Could not read configuration directory from System.in", ex);
                }
            }
        }
        return configDir;
    }

    private File getLog4JConfiguration() {
        return new File(System.getProperty("user.dir"), "tolven-log4j.xml");
    }

    private File getLogFile() {
        File parentLogDir = new File(System.getProperty("user.dir")).getParentFile();
        return new File(parentLogDir, "log/tolven.log");
    }

    private void initialize(String log4jConfiguration, File logFile) {
        try {
            File configFile = new File(log4jConfiguration);
            initialize(configFile.toURI().toURL().toExternalForm(), logFile.getPath());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not convert logFile: '" + logFile.getPath() + "' to a URI");
        }
    }

    /**
     * <p>Initialize log4j logging using the Tolven appender specification.</p>
     * <p>Note: This method should <i>not</i> be called within an environment such as JBoss that has a separate
     * log4j configuration.</p>
     * @param log4jConfiguration The name of the file containing the log4j configuration. If null, the
     * a file named tolven-log4j.xml on the classpath will be used.
     * @param logFilename The name of the log file. This file will be created if it does not already exist. If null, 
     * a default file named <code>${user.dir}/tolven.log</code> will be used.
     */
    private void initialize(String log4jConfiguration, String logFilename) {
        try {
            File logFile;
            if (logFilename != null) {
                logFile = new File(logFilename);
            } else {
                logFile = new File(DEFAULT_LOG_FILE);
            }
            System.setProperty(LOG_FILE_PROPERTY, logFile.getAbsolutePath());
            logFile.getParentFile().mkdirs();
            logFile.createNewFile();
            BasicConfigurator.configure();
            Logger.getRootLogger().info("Start log4j - Configuration: " + log4jConfiguration + ", logFileName: " + logFile.getAbsolutePath());
            BasicConfigurator.resetConfiguration();
            URL configURL;
            try {
                configURL = new URL(log4jConfiguration);
            } catch (Exception e) {
                configURL = Loader.getResource(log4jConfiguration);
            }
            DOMConfigurator.configure(configURL);
        } catch (Exception e) {
            throw new RuntimeException("Exception while initializing Tolven log4j. log4jConfiguration: " + log4jConfiguration + " logFilename: " + logFilename, e);
        }
    }

    private Properties load(File propertiesFile) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(propertiesFile);
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private void loadLog4JConfiguration() {
        File log4JConfiguration = getLog4JConfiguration();
        if (!log4JConfiguration.getPath().equals(log4JConfiguration.getAbsolutePath())) {
            log4JConfiguration = new File(System.getProperty("user.dir"), log4JConfiguration.getName());
        }
        File logFile = getLogFile();
        if (!logFile.getPath().equals(logFile.getAbsolutePath())) {
            logFile = new File(System.getProperty("user.dir"), logFile.getName());
        }
        initialize(log4JConfiguration.getPath(), logFile);
    }

    private void run(String[] args) throws Exception {
        loadLog4JConfiguration();
        URL manifestURL = ClassLoader.getSystemResource("tolven-plugin.xml");
        String TPF_VERSION = RepositoryMetadata.getVersion(manifestURL);
        System.setProperty(TPF_VERSION_PROPERTY, TPF_VERSION);
        Options cmdLineOptions = new Options();
        OptionGroup optionGroup = new OptionGroup();
        optionGroup.setRequired(true);
        Option upgradeOption = new Option(CMD_LINE_GETPLUGINS_OPTION, CMD_LINE_GETPLUGINS_OPTION, false, "getPlugins from repositories");
        optionGroup.addOption(upgradeOption);
        Option pluginOption = new Option(CMD_LINE_PLUGIN_OPTION, CMD_LINE_PLUGIN_OPTION, true, "execute one or more plugins");
        optionGroup.addOption(pluginOption);
        Option genMetadataOption = new Option(CMD_LINE_GENMETADATA_OPTION, CMD_LINE_GENMETADATA_OPTION, true, "generate a repository metadata file");
        optionGroup.addOption(genMetadataOption);
        Option versionOption = new Option(CMD_LINE_VERSION_OPTION, CMD_LINE_VERSION_OPTION, false, "TPF Version");
        optionGroup.addOption(versionOption);
        Option propsFileOption = new Option(CMD_LINE_PROPS_FILE_OPTION, CMD_LINE_PROPS_FILE_OPTION, true, "file to output plugins.xml properties");
        optionGroup.addOption(propsFileOption);
        cmdLineOptions.addOptionGroup(optionGroup);
        Option confOption = new Option(CMD_LINE_CONF_OPTION, CMD_LINE_CONF_OPTION, true, "configuration directory");
        cmdLineOptions.addOption(confOption);
        Option pluginsxmlOption = new Option(CMD_LINE_PLUGINSXML_OPTION, CMD_LINE_PLUGINSXML_OPTION, true, "plugins.xml file");
        cmdLineOptions.addOption(pluginsxmlOption);
        try {
            GnuParser parser = new GnuParser();
            CommandLine commandLine = parser.parse(cmdLineOptions, args, true);
            boolean propertiesRequest = commandLine.hasOption(CMD_LINE_PROPS_FILE_OPTION);
            boolean genMetadata = commandLine.hasOption(CMD_LINE_GENMETADATA_OPTION);
            boolean versionRequest = commandLine.hasOption(CMD_LINE_VERSION_OPTION);
            if (propertiesRequest) {
                String propsFilename = commandLine.getOptionValue(CMD_LINE_PROPS_FILE_OPTION);
                String pluginsxml = commandLine.getOptionValue(CMD_LINE_PLUGINSXML_OPTION);
                pluginsWrapper = new ConfigPluginsWrapper();
                File pluginsxmlFile = new File(pluginsxml);
                pluginsWrapper.loadPluginsXML(pluginsxmlFile);
                Properties props = pluginsWrapper.getProperties();
                File file = new File(propsFilename);
                if (!file.getPath().equals(file.getAbsolutePath())) {
                    String userDir = System.getProperty("user.dir");
                    file = new File(userDir, propsFilename);
                }
                storeProperties(props, file);
            } else if (genMetadata) {
                StringBuffer buff = new StringBuffer();
                for (String arg : args) {
                    if (!("-" + CMD_LINE_GENMETADATA_OPTION).equals(arg)) {
                        buff.append(arg + ",");
                    }
                }
                RepositoryMetadata.main(buff.toString().split(","));
            } else if (versionRequest) {
                logger.info("\nTPF Version: " + TPF_VERSION);
            } else {
                String configDirname = getCommandLineConfigDir(commandLine);
                File configDir = new File(configDirname);
                pluginsWrapper = new ConfigPluginsWrapper();
                pluginsWrapper.loadConfigDir(configDir);
                logger.info("TPF Version: " + TPF_VERSION);
                logger.info("javax.net.ssl.keyStore: " + System.getProperty("javax.net.ssl.keyStore") + " javax.net.ssl.trustStore: " + System.getProperty("javax.net.ssl.trustStore"));
                logger.info("Loaded configDir " + configDir.getPath());
                boolean upgrade = commandLine.hasOption(CMD_LINE_GETPLUGINS_OPTION);
                String plugins = commandLine.getOptionValue(CMD_LINE_PLUGIN_OPTION);
                if (upgrade) {
                    //RepositoryUpgrade.main(commandLine.getArgs()); //CLI seems to only recognize the first letter of an option?
                    StringBuffer buff = new StringBuffer();
                    for (String arg : args) {
                        if (!("-" + CMD_LINE_GETPLUGINS_OPTION).equals(arg)) {
                            buff.append(arg + ",");
                        }
                    }
                    RepositoryUpgrade.main(buff.toString().split(","));
                } else if (plugins != null) {
                    addBootRepositoryRuntime(pluginsWrapper, configDir);
                    Boot.main(args);
                }
            }
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(TPFBoot.class.getName(), cmdLineOptions);
            throw new RuntimeException("Could not parse command line for: " + TPFBoot.class.getName(), ex);
        }
    }

    private Properties store(Properties properties, File propertiesFile) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(propertiesFile);
            properties.store(out, null);
            return properties;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private Properties storeProperties(Properties properties, File file) {
        try {
            List<String> lines = new ArrayList<String>();
            List<String> names = new ArrayList<String>();
            names.addAll(properties.stringPropertyNames());
            for (String name : names) {
                lines.add(name + "=" + properties.getProperty(name));
            }
            Collections.sort(lines);
            lines.add(0, "#TPF Autogenerated File - DO NOT EDIT");
            lines.add(1, "#" + new Date());
            FileUtils.writeLines(file, lines);
            return properties;
        } catch (Exception ex) {
            throw new RuntimeException("Could not store properties to " + file.getPath(), ex);
        }
    }

}
