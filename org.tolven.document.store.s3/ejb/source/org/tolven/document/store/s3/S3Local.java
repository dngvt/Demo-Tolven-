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
 * @version $Id: S3Local.java 6283 2012-04-15 08:17:00Z joe.isaac $
 */  
package org.tolven.document.store.s3;


public interface S3Local {

    public void copy(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey);

    public void delete(String key, String bucketName);
    
    public boolean exists(String bucketKey, String bucketName);

    public byte[] get(String bucketKey, String bucketName);

    public void put(byte[] bytes, String bucketKey, String bucketName);
    
}
