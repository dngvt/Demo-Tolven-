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
 * @version $Id: S3Bean.java 6283 2012-04-15 08:17:00Z joe.isaac $
 */
package org.tolven.document.store.s3.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.io.IOUtils;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.document.store.s3.S3Local;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

@Stateless
public class S3Bean implements S3Local {

    private static final String ACCESSKEY_PROP = "org.tolven.document.store.s3.accesskey";
    private static final String SECRETKEY_PROP = "org.tolven.document.store.s3.secretkey";

    @EJB
    private TolvenPropertiesLocal propertyBean;

    private transient AmazonS3Client s3Client;

    @Override
    public void copy(String sourceKey, String sourceBucketName, String destinationKey, String destinationBucketName) {
        getS3Client().copyObject(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
    }

    @Override
    public void delete(String key, String bucketName) {
        getS3Client().deleteObject(bucketName, key);
    }

    @Override
    public boolean exists(String bucketKey, String bucketName) {
        ObjectListing objectListing = getS3Client().listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(bucketKey));
        return objectListing.getObjectSummaries().size() > 0;
    }

    @Override
    public byte[] get(String bucketKey, String bucketName) {
        S3Object s3Object = getS3Client().getObject(new GetObjectRequest(bucketName, bucketKey));
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(s3Object.getObjectContent(), baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private AmazonS3Client getS3Client() {
        if (s3Client == null) {
            String accessKeyString = propertyBean.getProperty(ACCESSKEY_PROP);
            if(accessKeyString == null) {
                throw new RuntimeException("Could not find property: " + ACCESSKEY_PROP);
            }
            char[] accessKey = accessKeyString.toCharArray();
            String secretKeyString = propertyBean.getProperty(SECRETKEY_PROP);
            if(secretKeyString == null) {
                throw new RuntimeException("Could not find property: " + SECRETKEY_PROP);
            }
            char[] secretKey = secretKeyString.toCharArray();
            AWSCredentials credentials = new BasicAWSCredentials(new String(accessKey), new String(secretKey));
            setS3Client(new AmazonS3Client(credentials));
        }
        return s3Client;
    }

    @Override
    public void put(byte[] bytes, String bucketKey, String bucketName) {
        byte[] b = null;
        if(bytes == null) {
            b = new byte[0];
        } else {
            b = bytes;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        PutObjectRequest req = new PutObjectRequest(bucketName, bucketKey, bais, null);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(b.length);
        req.setMetadata(metadata);
        getS3Client().putObject(req);
    }

    private void setS3Client(AmazonS3Client s3Client) {
        this.s3Client = s3Client;
    }

}
