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
package org.tolven.doc.entity;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.Status;
import org.tolven.core.entity.TolvenUser;
import org.tolven.doc.ContentProvider;
import org.tolven.doc.DocContentSecurity;
import org.tolven.doc.ImmutableStore;
import org.tolven.doc.MutableStore;
import org.tolven.doc.StoredDocument;
import org.tolven.doctype.DocumentType;
import org.tolven.security.key.DocumentSecretKey;

/**
 * A Document managed by Tolven.
 * @author John Churin
 */
@Entity
@Table
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("DOC")
public class DocBase implements DocContentSecurity, StoredDocument, Serializable {

    public static final String CONTENT_PROVIDERS = "org.tolven.document.store.contentprovider";
    public static final String IMMUTABLE_STORES = "org.tolven.document.store.immutablestores";
    public static final String MUTABLE_STORES = "org.tolven.document.store.mutablestores";
    /**
     * Version number used for serialization
     */
    private static final long serialVersionUID = 3L;
    
    private static final Logger logger = Logger.getLogger(DocBase.class);

    private transient DocumentType documentType;

    private transient static Map<String, ContentProvider> contentProviders;
    private transient static Set<ImmutableStore> immutableStores;
    private transient static Set<MutableStore> mutableStores;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DOC_SEQ_GEN")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    TolvenUser author;

    @Column
    private String mediaType;

    @Column
    private String schemaURI;

    @Column
    private String status;

    @Version
    @Column
    private Integer version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date finalSubmitTime;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(insertable = false, updatable = false)
    private byte[] content;

    @Basic(fetch=FetchType.LAZY)
    @Column
    private byte[] body;

    @Embedded
    private DocumentSecretKey documentSecretKey;

    @Column
    private Boolean isSignatureRequired;

    @Column
    private String storageFormat;

    @Column
    private String storageMetadata;

    /**
     * Creates a new instance of DocBase
     */
    public DocBase() {
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DocBase)) {
            return false;
        }
        if (this.getId() == ((DocBase) obj).getId()) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "DocBase: " + getId();
    }

    public int hashCode() {
        if (getId() == 0) {
            throw new IllegalStateException("id not yet established in DocBase object");
        }
        return new Long(getId()).hashCode();
    }

    /**
     * The unique and primary key of the document.
     */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * The author of a document is a user (or principal of some kind). This is different from the healthRecord or other subjects to which a document applies.
     * While the Subject contains an additional layer of privacy, the author of the document is relatively easy to determine. While this may appear to be
     * a way for an insider to find, say, personal information when the author is the patient, in fact it does not because the document itself is usually encrypted.
     * So, as long as both author and subject are not juxtaposed, then it would remain difficult for an insider to make a connection between, say,
     * a patient (subject) and an obstetrician (author).
     */
    public TolvenUser getAuthor() {
        return author;
    }

    public void setAuthor(TolvenUser author) {
        this.author = author;
    }

    /**
     * Return the raw contents of the document.
     */
    public byte[] getContent() {
        return getStoredContent();
    }

    /**
     * Return the raw contents of the document.
     */
    public byte[] getStoredContent() {
        if(contentProviders == null) {
            throw new RuntimeException("contentProviders not intialized");
        }
        if(getStorageFormat() == null) {
            if(getBody() != null) {
                return getBody();
            }
            return content;
        } else {
            ContentProvider contentProvider = contentProviders.get(getStorageFormat());
            if (contentProvider == null) {
                throw new RuntimeException("Could not find content provider for: " + getStorageFormat());
            } else {
                return contentProvider.getContent(this);
            }
        }
    }

    /**
     * Set the raw encrypted contents for this document. Normally not called directly.
     * @param content
     */
    public void setContent(byte[] content) {
        setStoredContent(content);
    }

    /**
     * Set the raw encrypted contents for this document. Normally not called directly.
     * IF mutableStores is passed in as null, the content will be set to the document without consulting any mutableStores present
     *
     * @param content
     */
    public void setStoredContent(byte[] content) {
        if (Status.ACTIVE.value().equals(getStatus())) {
            logger.warn("setStoredContent cannot be executed for an immutable document: " + getId());
            return;
        }
        if(mutableStores == null) {
            throw new RuntimeException("mutableStores not intialized");
        }
        if (getId() == 0) {
            throw new RuntimeException("Document content cannot be set when the Id is: " + getId());
        }
        if (mutableStores.isEmpty()) {
            setBody(content);
            setStorageMetadata(null);
            setStorageFormat(null);
        } else {
            boolean storageFormatSet = false;
            for (MutableStore mutableStore : mutableStores) {
                String sFormat = mutableStore.storeMutable(content, this);
                if (sFormat != null) {
                    if (storageFormatSet) {
                        throw new RuntimeException("Storage format already set to:" + getStorageFormat() + " and cannot be reset to: " + sFormat + " for document " + getId());
                    } else {
                        setStorageFormat(sFormat);
                        storageFormatSet = true;
                    }
                }
            }
            if (!mutableStores.isEmpty() && !storageFormatSet) {
                throw new RuntimeException("Storage format was not set for document " + getId() + " by any of " + mutableStores);
            }
            this.content = null;
        }
    }

    //TODO This method should be protected using package protection for use by the DocProtectionBean
    public DocumentSecretKey getDocumentSecretKey() {
        return documentSecretKey;
    }

    /**
     * Get the AccountPublicKey for this document. This key may have been provided
     * explicitly via setAccountPublicKey or, if not, will be acquired from the Account object
     * pointed to by the document.
     * @return PublicKey Object which can be used to encrypt the underlying document.
     */
    public PublicKey getPublicKey() {
        try {
            if (getAccount() != null) {
                return getAccount().getPublicKey();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Unable to acquire account private key", e);
        }
    }

    /**
     * Set the content of this document to an encrypted byte array.
     */
    public void setAsEncryptedContent(byte[] unencryptedContent, String kbeKeyAlgorithm, int kbeKeyLength) {
        try {
            // Force the doctype to use specified key algorithm and key length
            getDocumentType().setKeyAlgorithm(kbeKeyAlgorithm);
            getDocumentType().setKeyLength(kbeKeyLength);
            getDocumentType().setEncryptedContent(unencryptedContent, getPublicKey());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Set the content of this document from a base 64 encoded string.
     */
    public void setAsEncryptedContentB64(String content, String kbeKeyAlgorithm, int kbeKeyLength) {
        setAsEncryptedContent(Base64.decodeBase64(content.getBytes()), kbeKeyAlgorithm, kbeKeyLength);
    }

    /**
     * Set the content of this document to a string which is encrypted.
     */
    public void setAsEncryptedContentString(String content, String kbeKeyAlgorithm, int kbeKeyLength) {
        setAsEncryptedContent(content.getBytes(), kbeKeyAlgorithm, kbeKeyLength);
    }

    /**
     * Return the Media type for this document.
     */
    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Either "new" or "active".
     * @return
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * If the status is new, then the document is editable
     * @return
     */
    public boolean isEditable() {
        return ("new".equals(getStatus()));
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Boolean getIsSignatureRequired() {
        // Autoboxing is used here but we need to check for null
        // in case of legacy documents.
        if (isSignatureRequired != null) {
            return isSignatureRequired;
        }
        return false;
    }

    public boolean isSignatureRequired() {
        return getIsSignatureRequired();
    }

    public void setIsSignatureRequired(Boolean isSignatureRequired) {
        this.isSignatureRequired = isSignatureRequired;
    }

    public void setSignatureRequired(boolean isSignatureRequired) {
        setIsSignatureRequired(isSignatureRequired);
    }

    /**
     * A transient containing a documentType type instance that performs
     * document-specific behavior on the document.
     * @return
     */
    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public void setDocumentSecretKey(DocumentSecretKey documentSecretKey) {
        this.documentSecretKey = documentSecretKey;
    }

    public String getSchemaURI() {
        return schemaURI;
    }

    public void setSchemaURI(String schemaURI) {
        this.schemaURI = schemaURI;
    }

    /**
     * A Version number incremented by the persistence manager. Therefore, 
     * no set method is provided publicly.
     * @return The version number of the entity
     */
    public int getVersion() {
        return version;
    }

    public Date getFinalSubmitTime() {
        return finalSubmitTime;
    }

    public void setFinalSubmitTime(Date finalSubmitTime) {
        this.finalSubmitTime = finalSubmitTime;
    }

    public String getStorageFormat() {
        return storageFormat;
    }

    public void setStorageFormat(String storageFormat) {
        this.storageFormat = storageFormat;
    }

    public String getStorageMetadata() {
        return storageMetadata;
    }

    public void setStorageMetadata(String storageMetadata) {
        this.storageMetadata = storageMetadata;
    }

    public void makeImmutable() {
	     if (Status.ACTIVE.value().equals(getStatus())) {
  /*          throw new RuntimeException("Document is immutable: " + getId()); FDS  */
                  logger.warn("Trying to make immutable document immutable " + getId());
				  return;
        }
        if(mutableStores == null || immutableStores == null) {
            throw new RuntimeException("immutableStores not intialized");
        }

        if (getId() == 0) {
            throw new RuntimeException("Document content cannot be made immutable when the Id is: " + getId());
        }
        boolean storageFormatSet = false;
        byte[] currentContent = getContent();
        String mutableStorageFormat = getStorageFormat();
        String mutableStorageMetadata = getStorageMetadata();
        for (ImmutableStore immutableStore : immutableStores) {
            String sFormat = immutableStore.storeImmutable(currentContent, this);
            if (sFormat != null) {
                if (storageFormatSet) {
                    throw new RuntimeException("Storage format already set to:" + getStorageFormat() + " and cannot be reset to: " + sFormat + " for document " + getId());
                } else {
                    setBody(null);
                    setStorageFormat(sFormat);
                    storageFormatSet = true;
                }
            }
        }
        if (!immutableStores.isEmpty() && !storageFormatSet) {
            throw new RuntimeException("Storage format was not set for document " + getId() + " by any of " + immutableStores);
        }
        if(immutableStores.isEmpty()) {
            setBody(currentContent);
            setStorageMetadata(null);
            setStorageFormat(null);
        }
        for (MutableStore mutableStore : mutableStores) {
            mutableStore.cleanupMutable(mutableStorageFormat, mutableStorageMetadata);
        }
        setStatus(Status.ACTIVE.value());
    }
    
    /**
     * This method is called by the DocBasePropertyInitializer singleton during its startup
     */
    public static synchronized void configure(Properties configProperties) {
        contentProviders = new HashMap<String, ContentProvider>();
        mutableStores = new HashSet<MutableStore>();
        immutableStores = new HashSet<ImmutableStore>();
        try {
            String contentProviderKeyValuePairs = configProperties.getProperty(CONTENT_PROVIDERS);
            if (contentProviderKeyValuePairs != null) {
                for (String contentProviderKeyValuePair : contentProviderKeyValuePairs.split(",")) {
                    int index = contentProviderKeyValuePair.indexOf("=");
                    if(index == -1) {
                        throw new RuntimeException("Incorrect content provider format (e.g. code1=classname1, code2=classname2 but found: " + contentProviderKeyValuePair);
                    }
                    String contentProviderKey = contentProviderKeyValuePair.substring(0, index);
                    String contentProviderValue = contentProviderKeyValuePair.substring(1 + index);
                    Class<?> clazz = Class.forName(contentProviderValue);
                    ContentProvider contentProvider = (ContentProvider) clazz.newInstance();
                    contentProviders.put(contentProviderKey, contentProvider);
                    logger.info("Initialized for " + DocBase.class + " " + ContentProvider.class.getSimpleName() + "=" + contentProvider);
                }
            }
            String mutableStoresValue = configProperties.getProperty(MUTABLE_STORES);
            if (mutableStoresValue != null) {
                for (String mutableStoreClass : mutableStoresValue.split(",")) {
                    Class<?> clazz = Class.forName(mutableStoreClass);
                    MutableStore mutableStore = (MutableStore) clazz.newInstance();
                    mutableStores.add(mutableStore);
                    logger.info("Initialized for " + DocBase.class + " " + MutableStore.class.getSimpleName() + "=" + mutableStore);
                }
            }
            String immutableStoresValue = configProperties.getProperty(IMMUTABLE_STORES);
            if (immutableStoresValue != null) {
                for (String immutableStoreClass : immutableStoresValue.split(",")) {
                    Class<?> clazz = Class.forName(immutableStoreClass);
                    ImmutableStore immutableStore = (ImmutableStore) clazz.newInstance();
                    immutableStores.add(immutableStore);
                    logger.info("Initialized for " + DocBase.class + " " + ImmutableStore.class.getSimpleName() + "=" + immutableStore);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not initialize content managers", ex);
        }
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

}
