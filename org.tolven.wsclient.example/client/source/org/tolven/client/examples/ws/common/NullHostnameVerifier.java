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
 * @version $Id: NullHostnameVerifier.java 6686 2012-06-22 22:31:29Z joe.isaac $
 */
package org.tolven.client.examples.ws.common;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * This class is for development, and should not be used in production environments
 * 
 * @author Joseph Isaac
 *
 */
public class NullHostnameVerifier implements HostnameVerifier {

    public NullHostnameVerifier() {
        System.out.println("WARN: using " + getClass().getName());
    }
    
    @Override
    public boolean verify(String arg0, SSLSession arg1) {
        return true;
    }

}
