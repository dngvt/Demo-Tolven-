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
 * @version $Id: DefaultFSContentProvider.java 6283 2012-04-15 08:17:00Z joe.isaac $
 */
package org.tolven.document.store.fs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.tolven.doc.ContentProvider;
import org.tolven.doc.StoredDocument;

public class DefaultFSContentProvider implements ContentProvider {

    @Override
    public byte[] getContent(StoredDocument storedDocument) {
        if (storedDocument.getStorageMetadata() == null) {
            return null;
        } else {
            try {
                String contentFilename = storedDocument.getStorageMetadata();
                File contentFile = new File(contentFilename);
                byte[] bytes = null;
                if (contentFile.exists()) {
                    bytes = FileUtils.readFileToByteArray(contentFile);
                }
                return bytes;
            } catch (IOException ex) {
                throw new RuntimeException("Could not read content for document: " + storedDocument.getId(), ex);
            }
        }
    }

}
