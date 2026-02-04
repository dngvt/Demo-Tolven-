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
 * @version $Id: DefaultS3ContentProvider.java 6283 2012-04-15 08:17:00Z joe.isaac $
 */
package org.tolven.document.store.s3;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.tolven.doc.ContentProvider;
import org.tolven.doc.StoredDocument;

public class DefaultS3ContentProvider implements ContentProvider {

    @Override
    public byte[] getContent(StoredDocument storedDocument) {
        String storageMetadata = storedDocument.getStorageMetadata();
        if (storageMetadata == null) {
            return null;
        }
        int index = storageMetadata.indexOf("/");
        String bucketName = storageMetadata.substring(0, index);
        String bucketKey = storageMetadata.substring(index + 1);
        byte[] bytes = null;
        if (getS3Bean().exists(bucketKey, bucketName)) {
            bytes = getS3Bean().get(bucketKey, bucketName);
        }
        return bytes;
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

}
