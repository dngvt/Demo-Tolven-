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
 * @version $Id: DefaultS3MutableStore.java 6941 2012-08-25 23:12:11Z joe.isaac $
 */
package org.tolven.document.store.s3;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.doc.MutableStore;
import org.tolven.doc.StoredDocument;

public class DefaultS3MutableStore implements MutableStore {

    public static final String DOC_MUTABLE_STORE_BUCKETNAME = "org.tolven.document.store.s3.mutable-bucket";
    public static final String S3 = "S3";

    @Override
    public void cleanupMutable(String storeFormat, String storageMetadata) {
        if (storageMetadata != null) {
            int index = storageMetadata.indexOf("/");
            String bucketName = storageMetadata.substring(0, index);
            String bucketKey = storageMetadata.substring(index + 1);
            getS3Bean().delete(bucketKey, bucketName);
        }
    }

    private TolvenPropertiesLocal getPropertyBean() {
        String jndiName = null;
        try {
            InitialContext ctx = new InitialContext();
            jndiName = "java:app/tolvenEJB/TolvenProperties!org.tolven.core.TolvenPropertiesLocal";
            return (TolvenPropertiesLocal) ctx.lookup(jndiName);
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup " + jndiName, e);
        }
    }

    private S3Local getS3Bean() {
        String jndiName = null;
        try {
            InitialContext ctx = new InitialContext();
            jndiName = "java:app/doc-store-s3-ejb/S3Bean!org.tolven.document.store.s3.S3Local";
            return (S3Local) ctx.lookup(jndiName);
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup " + jndiName, e);
        }
    }

    @Override
    public String storeMutable(byte[] content, StoredDocument storedDocument) {
        if (content != null) {
            String mutableBucketName = getPropertyBean().getProperty(DOC_MUTABLE_STORE_BUCKETNAME);
            if (mutableBucketName == null) {
                throw new RuntimeException("Could not find property: " + DOC_MUTABLE_STORE_BUCKETNAME + " to store document:" + storedDocument.getId());
            }
            String bucketKey = String.valueOf(storedDocument.getId());
            getS3Bean().put(content, bucketKey, mutableBucketName);
                storedDocument.setStorageMetadata(mutableBucketName + "/" + bucketKey);
        }
        return S3;
    }

}
