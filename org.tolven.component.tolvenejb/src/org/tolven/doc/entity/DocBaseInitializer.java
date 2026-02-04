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
 * @version $Id: DocBaseInitializer.java 6295 2012-04-16 08:45:26Z joe.isaac $
 */
package org.tolven.doc.entity;

import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.Singleton;

import org.tolven.core.TolvenPropertiesLocal;

@Singleton
public class DocBaseInitializer {

    private boolean initialized = false;

    @EJB
    private TolvenPropertiesLocal propertyBean;

    public void initialize() {
        if (!initialized) {
            Properties properties = new Properties();
            String contentProviderKeyValuePairs = propertyBean.getProperty(DocBase.CONTENT_PROVIDERS);
            if (contentProviderKeyValuePairs != null) {
                properties.setProperty(DocBase.CONTENT_PROVIDERS, contentProviderKeyValuePairs);
            }
            String mutableStoresValue = propertyBean.getProperty(DocBase.MUTABLE_STORES);
            if (mutableStoresValue != null) {
                properties.setProperty(DocBase.MUTABLE_STORES, mutableStoresValue);
            }
            String immutableStoresValue = propertyBean.getProperty(DocBase.IMMUTABLE_STORES);
            if (immutableStoresValue != null) {
                properties.setProperty(DocBase.IMMUTABLE_STORES, immutableStoresValue);
            }
            DocBase.configure(properties);
            initialized = true;
        }
    }

}
