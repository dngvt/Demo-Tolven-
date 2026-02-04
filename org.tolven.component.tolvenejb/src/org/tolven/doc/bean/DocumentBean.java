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
package org.tolven.doc.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.security.PrivateKey;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.Status;
import org.tolven.core.entity.TolvenUser;
import org.tolven.doc.DocContentSecurity;
import org.tolven.doc.DocProtectionLocal;
import org.tolven.doc.DocumentLocal;
import org.tolven.doc.entity.CCRException;
import org.tolven.doc.entity.DocAttachment;
import org.tolven.doc.entity.DocBase;
import org.tolven.doc.entity.DocBaseInitializer;
import org.tolven.doc.entity.DocCCR;
import org.tolven.doc.entity.DocImage;
import org.tolven.doc.entity.DocXML;
import org.tolven.doctype.DocTypeFactory;
import org.tolven.logging.TolvenLogger;
import org.tolven.security.key.DocumentSecretKey;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.xml.Transformer;

/**
 * This is the bean class for the DocumentBean enterprise bean.
 * Most document work can be done indirectly through other beans but we provide these methods
 * for explicit document manipulation.
 * In particular, the details of most documents are stored in XML form. This session bean provides methods to marshall and unmarshall
 * that XML into and out of java object trees.
 * Created Apr 19, 2006 11:41:52 AM
 * @author John Churin
 */
@Stateless
@Local(DocumentLocal.class)
public class DocumentBean implements DocumentLocal {
    private static final String CCRns = "urn:astm-org:CCR";
    private static final String CCD_XSLT = "org.tolven.xslt.ccd";

    @PersistenceContext
    private EntityManager em;

    @EJB private DocProtectionLocal docProtectionBean;
    @EJB TolvenPropertiesLocal propertyBean;
    
    @EJB
    private DocBaseInitializer docBaseInitializer;

    
    private DocTypeFactory docTypeFactory;
    
    public DocumentBean() {
        super();
    }

    public byte[] getDecryptedContent(DocContentSecurity doc, AccountUser activeAccountUser, PrivateKey userPrivateKey) {
        return docProtectionBean.getDecryptedContent(doc, activeAccountUser, userPrivateKey);
    }

    /**
     * Get an instance of the DocumentType factory
     * @return
     */
    public DocTypeFactory getDocTypeFactory() {
        if (docTypeFactory == null) {
            docTypeFactory = new DocTypeFactory();
        }
        return docTypeFactory;
    }

    //	public DocumentType getDocumentType (DocBase doc) {
    //		DocumentType documentType = findDocumentType(doc);
    //		documentType.prepareEvaluator( ee );
    //	}

    /**
     * Get a document by its internal ID
     * @param docId the id of the document
     * @return the document object
     */
    public DocBase findDocument(long docId) {
        DocBase doc = em.find(DocBase.class, docId);
        getDocTypeFactory().associateDocumentType(doc);
        return doc;
    }

    /**
     * The document is immediately persisted (with no XML in it). This gives us the ID we'll need
     * to create actual CCR object graph. 
     * @param userId
     * @param accountId
     * @return
     * @throws CCRException 
     * @throws IOException 
     * @throws CCRException 
     */
    public DocCCR createCCRDocument(long userId, long accountId) throws CCRException {
        DocBase doc = getDocTypeFactory().createNewDocument("text/xml", CCRns);
        createDocument(doc, userId, accountId);
        return (DocCCR) doc;
    }

    /**
     * Use createNewDocument() with mediaType text/xml and null namspace
     */
    @Deprecated
    public DocXML createXMLDocument(String xmlNS, long userId, long accountId) {
        try {
			if (CCRns.equals(xmlNS)) return createCCRDocument( userId, accountId);
            DocBase doc = getDocTypeFactory().createNewDocument("text/xml", xmlNS);
            createDocument(doc, userId, accountId);
            return (DocXML) doc;
        } catch (CCRException e) {
            throw new RuntimeException("Excption creating XML Document", e);
        }
    }

    /**
     * Create a new document of the given media type. The document is persisted (though not committed).
     * Therefore, it's ID has been established. 
     * @return The document object
     */
    public DocBase createNewDocument(String mediaType, String namespace, AccountUser accountUser) {
        DocBase doc = getDocTypeFactory().createNewDocument(mediaType, namespace);
        doc.setAccount(accountUser.getAccount());
        doc.setAuthor(accountUser.getUser());
        doc.setStatus(Status.NEW.value());
        em.persist(doc);
        return doc;
    }

    /**
     * Persist a new document and return it's ID. This is a final submission. the document is now
     * immutable. Account and author should be provided in the document. Also, mediaType should
     * be present.
     * @param doc
     * @return the id assigned to the new document.
     */
    public long createFinalDocument(DocBase doc) {
        doc.setStatus(Status.ACTIVE.value());
        em.persist(doc);
        return doc.getId();
    }

    /**
     * Persist a new document and return it's ID. This is not a final submission.
     * At this point the document is persisted but not actionable. It can still be edited.
     * When the document is ready for submission, the finalizeDocument method should be called.
     * TODO: Prior to finalization, the document can also be digitally signed.
     * @param doc
     * @param userId of the author
     * @return the id assigned to the new document.
     */
    public long createDocument(DocBase doc, long userId, long accountId) {
        doc.setStatus(Status.NEW.value());
        doc.setAuthor(em.find(TolvenUser.class, userId));
        doc.setAccount(em.find(Account.class, accountId));
        em.persist(doc);
        return doc.getId();
    }

    /**
     * Complete the document submission process by rendering the document immutable. A merge will be done
     * in case the finalization occurs in a different transaction as the creation.
     * @param doc
     */
    @Override
    public void finalizeDocument(long docId) {
        DocBase doc = findDocument(docId);
        if(doc == null) {
            throw new RuntimeException("Could not find document: " + docId);
        }
        finalizeDocument(doc);
    }

    /**
     * Complete the document submission process by rendering the document immutable. A merge will be done
     * in case the finalization occurs in a different transaction as the creation.
     * @param doc
     */
    @Override
    public void finalizeDocument(DocBase doc) {
        saveDocument(doc);
        doc.makeImmutable();
    }

    /**
     * Save the document without finalizing. A merge will be done
     * in case the save occurs in a different transaction from the creation.
     * Note: Withholding a "Save" does not imply a rollback. For example, a document
     * that has been created or fetched and then modified within the same local VM and same transaction
     * will "automatically" be saved. So this method simply ensures that documents in other states will also be saved.
     * @param doc
     */
    public void saveDocument(DocBase doc) {
        em.merge(doc);
    }

    /**
     * Not a very practical method but we'll use it for testing.
     * @return
     */
    public List<DocBase> findAllDocuments() {
        Query query = em.createQuery("SELECT d FROM DocBase d");
        query.setMaxResults(100);
        List<DocBase> items = query.getResultList();
        return items;
    }

    /**
     * Return documents for the specified author
     */
    public List<DocBase> findDocuments(long author, int pageSize, int offset, String sortAttribute, String sortDir) {
        Query query = em.createQuery("SELECT d FROM DocBase d where author.id = :author ORDER BY d." + sortAttribute + " " + sortDir);
        query.setMaxResults(pageSize);
        query.setFirstResult(offset);
        query.setParameter("author", author);
        List<DocBase> items = query.getResultList();
        return items;
    }

    /**
     * Return all XML documents owned by any account the specified user is a member of (for demo use only). 
     * This is otherwise a security violation but is used to demonstrate that even if documents are returned
     * that the user doesn't currently have access to, they will remain encrypted.
     */
    public List<DocXML> findAllXMLDocuments(long userId, int pageSize, int offset, String sortAttribute, String sortDir) {
        Query query = em.createQuery("SELECT d FROM DocXML d, AccountUser au WHERE au.user.id = :user AND d.account.id = au.account.id ORDER BY d." + sortAttribute + " " + sortDir);
        query.setMaxResults(pageSize);
        query.setFirstResult(offset);
        query.setParameter("user", userId);
        List<DocXML> items = query.getResultList();
        return items;
    }

    /**
     * Count of all XML documents owned by any account the specified user is a member of (for demo use only). This is otherwise a security violation.
     */
    public long countXMLDocuments(long userId) {
        Query query = em.createQuery("SELECT COUNT(d) FROM DocXML d, AccountUser au WHERE au.user.id = :userId AND d.account.id = au.account.id");
        query.setParameter("userId", userId);
        Long rslt = (Long) query.getSingleResult();
        return rslt.longValue();
    }

    /**
     * @see DocumentLocal
     */
    public DocImage findImage(long docId, long accountId) {
        DocImage doc = em.find(DocImage.class, docId);
        if (doc.getAccount().getId()!=accountId) throw new IllegalArgumentException( "Document is not owned by the " +
                "current user's account.");
        return doc;
    }

    /**
     * Count of all documents owned by this account.
     */
    public long countDocuments(long accountId) {
        Query query = em.createQuery("SELECT COUNT(d) FROM DocBase d where account.id=:account");
        query.setParameter("account", accountId);
        Long rslt = (Long) query.getSingleResult();
        return rslt.longValue();
    }

    /**
     * Count of all photo-documents owned by this account.
     */
    public long countImages(long accountId) {
        Query query = em.createQuery("SELECT COUNT(d) FROM DocImage d where account.id=:account");
        query.setParameter("account", accountId);
        Long rslt = (Long) query.getSingleResult();
        return rslt.longValue();
    }

    /**
     * Return images for the specified account
     */
    public List<DocImage> findImages(long accountId, int pageSize, int offset, String sortAttribute, String sortDir) {
        Query query = em.createQuery("SELECT d FROM DocImage d where account.id = :account ORDER BY d." + sortAttribute + " " + sortDir);
        query.setMaxResults(pageSize);
        query.setFirstResult(offset);
        query.setParameter("account", accountId);
        List<DocImage> items = query.getResultList();
        return items;
    }

    /**
     * Get a document by its internal ID for the current account in context
     * @param docId
     * @return
     */
    @Override
    public DocXML findXMLDocument(long docId) {
        Account account = TolvenRequest.getInstance().getAccount();
        if(account == null) {
            throw new RuntimeException("Account not find in TolvenRequest");
        }
        Query query = em.createQuery("SELECT d FROM DocXML d WHERE d.id = :docId AND d.account.id = :account");
        query.setParameter("docId", docId);
        query.setParameter("account", account.getId());
        List<DocXML> items = query.getResultList();
        if(items.size() == 0) {
            return null;
        } else {
            return items.get(0);
        }
    }

    /**
     * Return XML documents for the specified account
     */
    public List<DocXML> findXMLDocuments(long accountId, int pageSize, int offset, String sortAttribute, String sortDir) {
        Query query = em.createQuery("SELECT d FROM DocCCR d where account.id = :account ORDER BY d." + sortAttribute + " " + sortDir);
        query.setMaxResults(pageSize);
        query.setFirstResult(offset);
        query.setParameter("account", accountId);
        List<DocXML> items = query.getResultList();
        //        TolvenLogger.info( "Query XML Docs for account " + accountId + " count is " + items.size(), DocumentBean.class);
        return items;
    }

    /**
     * Persist a new image and return it's ID
     * @param doc
     * @param accountId of the author
     */
    public long createImage(DocImage doc, long userId, long accountId, byte[] content) {
        long id = createDocument(doc, userId, accountId);
        String kbeKeyAlgorithm = propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
        int kbeKeyLength = Integer.parseInt(propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
        doc.setAsEncryptedContent(content, kbeKeyAlgorithm, kbeKeyLength);
        return id;
    }

    @PostConstruct
    public void init() {
        docBaseInitializer.initialize();
    }

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
            String description, AccountUser tolvenAuthor, Date now ) {
        DocAttachment da = new DocAttachment();
        da.setAccount(parentDocument.getAccount());
        da.setParentDocument(parentDocument);
        da.setAttachedDocument(attachedDocument);
        da.setDescription(description);
        // Denormalize media type (from document)
        da.setMediaType(attachedDocument.getMediaType());
        da.setTolvenAuthor(tolvenAuthor);
        da.setUploadTime(now);
        em.persist(da);
        return da;
    }

    /**
     * Copy all attachments to the from document to be attachments to the to document.
     * The attachment is copied but the attached document is not. Therefore, an attached document to the 
     * from document the same exact document as attached to the to document. The reason that this works is
     * because all attachments are by definition immutable. Thus, if one were to edit an attached document, it
     * would actually be a different document.
     * @param fromDoc
     * @param toDoc
     */
    public void copyAttachments(DocBase fromDoc, DocBase toDoc) {
        Set<DocBase> copyAttachments = new HashSet<DocBase>();
        List<DocAttachment> fromAttachments = findAttachments(fromDoc);
        // Add the list of attachments of the from document to the list we need to copy
        for (DocAttachment att : fromAttachments) {
            copyAttachments.add(att.getAttachedDocument());
        }
        // But subtract any attachments already in the to list (if any)
        for (DocAttachment att : findAttachments(toDoc)) {
            copyAttachments.remove(att.getAttachedDocument());
        }
        // Now the copy list contains the list of attachments we need to add to the toDoc.
        // Run back through the attachment list and create the new attachments
        for (DocAttachment att : fromAttachments) {
            if (copyAttachments.contains(att.getAttachedDocument())) {
    			DocAttachment newAtt = createAttachment( toDoc,att.getAttachedDocument(), 
    					att.getDescription(), att.getTolvenAuthor(), att.getUploadTime() );
                newAtt.setMediaType(att.getMediaType());
            }
        }
    }

    /**
     * Find the attachments to the specified document 
     * @param parentDocument The document for which attachments are sought
     * @return The list of attachments. The list may be empty
     */
    public List<DocAttachment> findAttachments(DocBase parentDocument) {
    	Query query = em.createQuery("SELECT da FROM DocAttachment da WHERE da.parentDocument = :parent");
        query.setParameter("parent", parentDocument);
        List<DocAttachment> attachments = query.getResultList();
        for (DocAttachment attachment : attachments) {
            getDocTypeFactory().associateDocumentType(attachment.getAttachedDocument());
        }
        return attachments;
    }

    /**
     * Find the attachments to the specified document 
     * @param parentDocumentId The id of the document for which attachments are sought
     * @return The list of attachments. The list may be empty
     */
    public List<DocAttachment> findAttachments(long parentDocumentId) {
        DocBase parentDoc = em.find(DocBase.class, parentDocumentId);
        List<DocAttachment> attachments = findAttachments(parentDoc);
        // Touch each attachment just to make sure lazy attributes are fetched
        for (DocAttachment attachment : attachments) {
            getDocTypeFactory().associateDocumentType(attachment.getAttachedDocument());
            // Don't remove this...
            attachment.getAttachedDocument().getContent();
        }
        return attachments;
    }

    /**
     * Delete an attachment. the attachment must is part of an editable parent document.
     * @param attId
     * @param accountUser
     */
    public void deleteAttachment(long attId, AccountUser accountUser) {
        DocAttachment attachment = em.find(DocAttachment.class, attId);
        if (attachment.getAccount().getId() != accountUser.getAccount().getId()) {
            throw new IllegalArgumentException("Attachment cannot be deleted by this user");
        }
        DocBase parentDoc = attachment.getParentDocument();
        if (!parentDoc.isEditable()) {
            throw new IllegalArgumentException("Attachment cannot be deleted from an immutable parent");
        }
        em.remove(attachment);
        // Document is gone too (this is forever)
        em.remove(attachment.getAttachedDocument());
    }
    
    /**
     * Return XML content, optionally transformed.
     * @throws IOException 
     */	
    public void returnXML(long docId, Map<String,String[]> params,String localAddress,Writer writer) throws IOException {
    	AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
    	DocBase doc = findDocument(docId);
    	try {
    		String transform[] = params.get("escape");
    		boolean useXslt = true;
    		if (transform != null) {
    			if ("false".equals(transform[0])) { 
    				useXslt = false;
    		    }       
    		}
    		InputStream xsltStream = null;
    		if (useXslt) {
    			xsltStream = openXSLT( accountUser, doc, localAddress);
    		}
    		if (xsltStream==null) {
    			returnXMLData( doc, writer);
    			return;
    		}
    		Transformer transformer = new Transformer( xsltStream );
    		  
    		String keyAlgorithm = propertyBean.getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
            TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
            Reader reader = new StringReader(docProtectionBean.getDecryptedContentString(doc, accountUser, sessionWrapper.getUserPrivateKey(keyAlgorithm)));
    		transformer.transform(reader, writer);
    	} catch (Exception e) {
    		throw new RuntimeException("Unable to render XML document " + doc.getId(), e);
    	}
    }
    /**
     * Open the appropriate XSLT for a document. The XSLT can be specific to an Account, account type, or
     * or system-wide. The schema-url controls which XSLT to fetch as does the locale of the AcountUser and and brand.
     * If the url-schema of the document is cda-medication, then the property name is <code>tolven.xslt.cda-medication</code>
     * 
     * @param doc
     * @param brand
     * @param servletContext
     * @return
     */	
    private InputStream openXSLT(AccountUser accountUser, DocBase doc, String brand) {
    	String key = "tolven.xslt/" + doc.getSchemaURI();
    	TolvenLogger.info( "openXSLT key: " + key.toString(), DocumentBean.class);
    	String value = accountUser.getBrandedProperty(brand).get(key);
    	if (value==null) return null;
    	InputStream xsltStream = new StringInputStream( value );
    	return xsltStream;
    }

    private void returnXMLData(DocBase doc,Writer writer) throws IOException {
		TolvenLogger.debug( "Returning XML Data", DocumentBean.class );
		AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
		
		String keyAlgorithm = propertyBean.getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
	    TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
		String content = docProtectionBean.getDecryptedContentString(doc, accountUser, sessionWrapper.getUserPrivateKey(keyAlgorithm));
		writer.write(content);
		writer.flush();
	}
    
 }
class StringInputStream extends InputStream {
	char[] string;
	int index = 0;
	public StringInputStream( String string ) {
		this.string = string.toCharArray();
	}
	
	@Override
	public int read() throws IOException {
		if (index < string.length) {
			return string[index++];
		}
		return -1;
	}
	
}