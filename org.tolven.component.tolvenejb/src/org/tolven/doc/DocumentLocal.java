/*
 *  Copyright (C) 2006 Tolven Inc 
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * Contact: info@tolvenhealth.com
 */
package org.tolven.doc;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.tolven.core.entity.AccountUser;
import org.tolven.doc.entity.CCRException;
import org.tolven.doc.entity.DocAttachment;
import org.tolven.doc.entity.DocBase;
import org.tolven.doc.entity.DocCCR;
import org.tolven.doc.entity.DocImage;
import org.tolven.doc.entity.DocXML;


/**
 * This is the business interface for Document enterprise bean. Store, encrypt, and fetch whole 
 * documents. Also store transformed documents. For instance, each sharing of a doc is a new document.
 */
public interface DocumentLocal {
	
    /**
	 * Persist a new document and return it's ID
	 * @param doc
	 * @param userId of the author
	 * @param accountId of the account that owns the document
	 * @return the id assigned to the new document.
	 */
	public long createDocument( DocBase doc, long userId, long accountId );
	
    /**
     * Create a new document of the given media type. The document is persisted (though not committed).
     * Therefore, it's ID has been established. 
     * @return The document object
     */
    public DocBase createNewDocument( String mediaType, String namespace, AccountUser accountUser );

    /**
     * Complete the document submission process by rendering the document immutable. A merge will be done
     * in case the finalization occurs in a different transaction from the creation.
     * @param docId
     */
    public void finalizeDocument(long docId);

    /**
     * Complete the document submission process by rendering the document immutable. A merge will be done
     * in case the finalization occurs in a different transaction from the creation.
     * @param doc
     */
    public void finalizeDocument( DocBase doc);

	/**
	 * Persist a new document and return it's ID. This is a final submission. the document is now
	 * immutable. Account and author should be provided in the document. Also, mediaType should
	 * be present.
	 * @param doc
	 * @param userId of the author
	 * @return the id assigned to the new document.
	 */
	public long createFinalDocument( DocBase doc );

		/**
	 * Save the document without finalizing. A merge will be done
	 * in case the save occurs in a different transaction from the creation.
	 * Note: Withholding a "Save" does not imply a rollback. For example, a document
	 * that has been created or fetched and then modified within the same local VM and same transaction
	 * will "automatically" be saved. So this method simply ensures that documents in other states will also be saved.
	 * @param doc
	 */
	public void saveDocument( DocBase doc);
	
	/**
	 * The document is immediately persisted (with no XML in it). This gives us the ID we'll need
	 * to create actual CCR object graph. 
	 * @param userId
	 * @param accountId
	 * @return
	 * @throws CCRException 
	 */
	public DocCCR createCCRDocument( long userId, long accountId ) throws CCRException;

    /**
     * Use createNewDocument() with mediaType text/xml and null namspace
     */
    @Deprecated
	public DocXML createXMLDocument( String xmlNS, long userId, long accountId );

	/**
	 * Not a very practical method but we'll use it for testing.
	 * @return
	 */
	public List<DocBase>findAllDocuments();
    
	/**
	 * Get a document by its internal ID
	 * @param docId the id of the document
	 * @return the document object
	 */
	public DocBase findDocument( long docId );
	
	/**
	 * Get a document by its internal ID for the current account in context
	 * @param docId
	 * @return
	 */
	public DocXML findXMLDocument(long docId);

	/**
	 * Return XML documents for the specified account
	 */
	public List<DocXML> findXMLDocuments(long accountId, int pageSize, int offset, String sortAttribute, String sortDir );

	/**
     * Return XML documents for the specified user
     */
    public List<DocXML> findAllXMLDocuments(long userId, int pageSize, int offset, String sortAttribute, String sortDir );

	/**
	 * Get a document by its internal ID
	 * @param docId the id of the document
	 * @param accountId This is not part of the primay key but is required to verify account ownership.
	 * A request for a docId outside of the current account is rejected. 
	 * @return the document object
	 */
	public DocImage findImage( long docId, long accountId );

   	/**
   	 * Count of all documents owned by this account.
   	 */
   	public long countDocuments( long accountId );

   	/**
   	 * Count of all documents owned by this user (ignoring account) - for demo use only.
   	 */
   	public long countXMLDocuments( long userId );

   	/**
   	 * Count of all photo-documents owned by this account.
   	 */
   	public long countImages( long account );
   	
	/**
	 * Return selected documents
	 */
	public List<DocBase> findDocuments(long author, int pageSize, int offset, String sortAttribute, String sortDir );
	
	/**
	 * Return images for the specified account
	 */
	public List<DocImage> findImages(long accountId, int pageSize, int offset, String sortAttribute, String sortDir );

	/**
	 * Persist a new image and return it's ID
	 * @param doc
	 * @param accountId of the author
	 * @param content the actual image to store
	 * @return the id assigned to the new document.
	 */
	public long createImage( DocImage doc, long userId, long accountId, byte[] content  );

	/**
	 * Create a new document attachment
	 * @param parentDocument The account of the parent document and attachment must be the same.
	 * @param attachedDocument The document to attach
	 * @param description
	 * @param tolvenAuthor
	 * @param now
	 * @return A new attachment object which has already been persisted 
	 */
	public DocAttachment createAttachment( DocBase parentDocument, DocBase attachedDocument, 
			String description, AccountUser tolvenAuthor, Date now );
	
	/**
	 * Find the attachments to the specified document 
	 * @param parentDocument The document for which attachments are sought
	 * @return The list of attachments. The list may be empty
	 */
	public List<DocAttachment> findAttachments( DocBase parentDocument );

	/**
	 * Delete an attachment. the attachment must is part of an editable parent document.
	 * @param attId
	 * @param accountUser
	 */
    public void deleteAttachment( long attId, AccountUser accountUser );
    
    /**
     * Copy all attachments to the from document to be attachments to the to document.
     * The attachment is copied but the attached document is not. Therefore, an attached document to the 
     * from document the same exact document as attached to the to document. The reason that this works is
     * because all attachments are by definition immutable. Thus, if one were to edit an attached document, it
     * would actually be a different document.
     * @param fromDoc
     * @param toDoc
     */
    public void copyAttachments( DocBase fromDoc, DocBase toDoc );
    
    /**
     * Return XML content, optionally transformed.
     * @throws IOException 
     */	
    public void returnXML(long docId, Map<String,String[]> params,String localAddress,Writer writer) throws IOException;
}
