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
package org.tolven.plugin.repository;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.tolven.plugin.repository.bean.PluginDetail;
import org.tolven.plugin.repository.bean.PluginPropertyDetail;
import org.tolven.plugin.repository.bean.Plugins;
import org.tolven.plugin.repository.el.ExpressionEvaluator;

/**
 * This class acts as a wrapper for the JAXB Plugins class.
 * 
 * @author Joseph Isaac
 *
 */
public class ConfigPluginsWrapper {

    public static final String BUILD_DIR = "build";
    public static final String CONFIG_DIR = "tolvenConfigDir";
    public static final String LIBRARY_PLUGS_DIR = "plugins";
    public static final String OVERWRITE_SNAPSHOT = "overwriteSnapshot";
    public static final String REPOSITORY_DEVLIB = "devLib";
    public static final String REPOSITORY_LIBRARY = "repositoryLibrary";
    public static final String REPOSITORY_RUNTIME = "repositoryRuntime";
    public static final String REPOSITORY_RUNTIME_UNPACKED = "temp/.jpf-shadow";
    public static final String REPOSITORY_SNAPSHOT_METADATA = "snapshotMetadata";
    public static final String REPOSITORY_STAGE = "repositoryStage";
    public static final String REPOSITORY_TMP = "repositoryTmp";
    public static final String REPOSITORY_TRUNK_METADATA = "trunkMetadata";
    public static final String USE_SNAPSHOT = "useSnapshot";

    private ExpressionEvaluator ee;
    private Logger logger = Logger.getLogger(ConfigPluginsWrapper.class);
    private Map<String, Map<String, String>> pluginPropertyMap = new HashMap<String, Map<String, String>>();

    private Plugins plugins;

    private File pluginsFile;

    public ConfigPluginsWrapper() {
    }

    public String evaluate(String aString) {
        String value = aString;
        int count = 0;
        do {
            value = (String) ee.evaluate(value);
            //TODO cheap loop detection
            if (value == null || count++ > 10) {
                break;
            }
        } while (value.contains("#{"));
        return value;
    }

    public String evaluate(String aString, String pluginId) {
        ee.pushContext();
        ee.addVariable("pluginProperty", pluginPropertyMap.get(pluginId));
        String value = aString;
        int count = 0;
        do {
            value = (String) ee.evaluate(value);
            //TODO cheap loop detection
            if (value == null || count++ > 10) {
                break;
            }
        } while (value.contains("#{"));
        ee.popContext();
        return value;
    }

    public File getBuildDir() {
        return new File(getConfigDir(), BUILD_DIR);
    }

    public File getConfigDir() {
        PluginPropertyDetail pluginProperty = getPluginProperty(CONFIG_DIR);
        if (pluginProperty == null) {
            throw new RuntimeException(CONFIG_DIR + " property is not defined in plugins.xml");
        }
        String value = pluginProperty.getValue();
        String evaluated = (String) evaluate(value);
        if (evaluated == null) {
            throw new RuntimeException("Evaluated to null: property:'" + CONFIG_DIR + "' value: '" + value + "'");
        }
        return new File(evaluated);
    }

    public PluginPropertyDetail getPluginProperty(String name) {
        for (PluginPropertyDetail pluginProperty : getPlugins().getProperty()) {
            if (pluginProperty.getName().equals(name)) {
                return pluginProperty;
            }
        }
        return null;
    }

    public Plugins getPlugins() {
        return plugins;
    }

    public File getPluginsFile() {
        return pluginsFile;
    }

    public Properties getProperties() {
        Properties props = new Properties();
        return getProperties(props);
    }

    public Properties getProperties(Properties props) {
        for (PluginPropertyDetail gProperty : getPlugins().getProperty()) {
            if (gProperty.getValue() == null) {
                updateGlobalMap(props, gProperty.getName(), gProperty.getProperty());
            } else {
                String expr = "#{" + gProperty.getName() + "}";
                String value = evaluate(expr);
                if (value == null) {
                    throw new RuntimeException("Evaluates to null: property: " + gProperty.getName() + " value: " + expr);
                }
                props.setProperty(gProperty.getName(), value);
            }
        }
        return props;
    }

    private String getProperty(PluginPropertyDetail pluginProperty, String name) {
        return getProperty(pluginProperty, name, null);
    }

    private String getProperty(PluginPropertyDetail pluginProperty, String name, String defaultValue) {
        return getProperty(name, defaultValue, pluginProperty.getProperty());
    }

    private String getProperty(String name, String defaultValue, List<PluginPropertyDetail> pluginProperties) {
        for (PluginPropertyDetail pp : pluginProperties) {
            if (pp.getName().equals(name)) {
                return pp.getValue();
            }
        }
        return defaultValue;
    }

    public File getRepositoryDevLibDir() {
        return new File(getConfigDir(), REPOSITORY_DEVLIB);
    }

    public PluginPropertyDetail getRepositoryLibrary() {
        PluginPropertyDetail pluginProperty = getPluginProperty(REPOSITORY_LIBRARY);
        if (pluginProperty == null) {
            throw new RuntimeException("Plugin property " + REPOSITORY_LIBRARY + " is not defined");
        } else {
            return pluginProperty;
        }
    }

    public File getRepositoryRuntimeDir() {
        return new File(getConfigDir(), REPOSITORY_RUNTIME);
    }

    public File getRepositoryRuntimePluginsDir() {
        return new File(getRepositoryRuntimeDir(), LIBRARY_PLUGS_DIR);
    }

    public File getRepositoryRuntimeUnpackedDir() {
        return new File(getBuildDir(), REPOSITORY_RUNTIME_UNPACKED);
    }

    public File getRepositoryStageDir() {
        return new File(getBuildDir(), REPOSITORY_STAGE);
    }

    public File getRepositoryTmpDir() {
        return new File(getBuildDir(), REPOSITORY_TMP);
    }

    public void loadConfigDir(File configDir) {
        if (!configDir.exists()) {
            throw new RuntimeException("Could not find the configuration directory: " + configDir.getPath());
        }
        setPluginsFile(new File(configDir, RepositoryMetadata.METADATA_XML));
        loadPlugins(getPluginsFile());
        //TODO Commented until TPF can resolve installation properties in the plugins.xml
        /*
        if (!configDir.equals(getConfigDir())) {
            throw new RuntimeException(CONFIG_DIR + " property " + getConfigDir().getPath() + " does not match installation tpfenv " + configDir.getPath());
        }
        */
    }

    /**
     * Unmarshall a Plugins from an InputStream
     * @param xsdStream
     * @return
     */
    public Plugins loadPlugins(File pluginsFile) {
        setPlugins(RepositoryMetadata.getPlugins(pluginsFile));
        validate();
        ee = new ExpressionEvaluator();
        for (PluginPropertyDetail gProperty : getPlugins().getProperty()) {
            if (gProperty.getValue() == null) {
                Map<String, Object> map = new HashMap<String, Object>();
                ee.addVariable(gProperty.getName(), map);
                updateGlobalMap(map, gProperty.getProperty());
            } else {
                ee.addVariable(gProperty.getName(), gProperty.getValue());
            }
        }
        for (PluginDetail plugin : getPlugins().getPlugin()) {
            Map<String, String> pMap = new HashMap<String, String>();
            for (PluginPropertyDetail pProperty : plugin.getProperty()) {
                updatePropertyMap(pMap, pProperty);
            }
            pluginPropertyMap.put(plugin.getId(), pMap);
        }
        return getPlugins();
    }

    public void loadPluginsXML(File pluginsxml) {
        if (!pluginsxml.exists()) {
            throw new RuntimeException("Could not find the pluginsxml: " + pluginsxml.getPath());
        }
        setPluginsFile(pluginsxml);
        loadPlugins(getPluginsFile());
    }

    public void setPlugins(Plugins plugins) {
        this.plugins = plugins;
    }

    public void setPluginsFile(File pluginsFile) {
        this.pluginsFile = pluginsFile;
    }

    public void storeMetadata(File pluginsXMLFile) {
        logger.info("Write Runtime plugins metadata to " + pluginsXMLFile);
        try {
            FileUtils.writeStringToFile(pluginsXMLFile, RepositoryMetadata.getPluginsXML(getPlugins()));
        } catch (IOException ex) {
            throw new RuntimeException("Could not write runtime plugins to: " + pluginsXMLFile, ex);
        }
    }

    private void updateGlobalMap(Map<String, Object> map, List<PluginPropertyDetail> childProperties) {
        if (childProperties.isEmpty()) {
            return;
        }
        for (PluginPropertyDetail pluginProperty : childProperties) {
            if (pluginProperty.getValue() == null) {
                Map<String, Object> m = new HashMap<String, Object>();
                map.put(pluginProperty.getName(), m);
                updateGlobalMap(m, pluginProperty.getProperty());
            } else {
                map.put(pluginProperty.getName(), pluginProperty.getValue());
            }
        }
    }

    private void updateGlobalMap(Properties props, String prefix, List<PluginPropertyDetail> childProperties) {
        if (childProperties.isEmpty()) {
            return;
        }
        for (PluginPropertyDetail pluginProperty : childProperties) {
            if (pluginProperty.getValue() == null) {
                updateGlobalMap(props, prefix + "." + pluginProperty.getName(), pluginProperty.getProperty());
            } else {
                String expr = "#{" + prefix + "['" + pluginProperty.getName() + "']}";
                String value = evaluate(expr);
                if (value == null) {
                    throw new RuntimeException("Evaluates to null: property: " + prefix + "." + pluginProperty.getName() + " value: " + expr);
                }
                props.setProperty(prefix + "." + pluginProperty.getName(), value);
            }
        }
    }

    /**
     * Update properties map for property and any child properties
     * 
     * @param pMap
     * @param property
     */
    private void updatePropertyMap(Map<String, String> pMap, PluginPropertyDetail property) {
        pMap.put(property.getName(), property.getValue());
        Collection<PluginPropertyDetail> childProperties = property.getProperty();
        if (childProperties.isEmpty()) {
            return;
        }
        for (PluginPropertyDetail pluginProperty : childProperties) {
            updatePropertyMap(pMap, pluginProperty);
        }
    }

    private void validate() {
        //Check for duplicates
        Set<String> pluginIds = new HashSet<String>();
        for (PluginDetail plugin : getPlugins().getPlugin()) {
            if (pluginIds.contains(plugin.getId())) {
                throw new RuntimeException(plugin.getId() + " has a duplicate entry in " + getPluginsFile().getPath());
            } else {
                pluginIds.add(plugin.getId());
            }
        }
    }

}
