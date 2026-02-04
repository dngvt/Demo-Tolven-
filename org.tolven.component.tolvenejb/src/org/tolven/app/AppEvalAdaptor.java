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
 * @author <your name>
 * @version $Id: AppEvalAdaptor.java,v 1.27.2.3 2010/11/06 12:34:56 jchurin Exp $
 */   

package org.tolven.app;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.drools.core.StatefulSession;
import org.tolven.app.bean.Mode;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuDataVersionMessage;
import org.tolven.app.entity.MenuStructure;
import org.tolven.app.entity.Touch;
import org.tolven.core.AccountDAOLocal;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.Status;
import org.tolven.doc.DocProtectionLocal;
import org.tolven.doc.DocumentLocal;
import org.tolven.doc.bean.TolvenMessage;
import org.tolven.doc.bean.TolvenMessageAttachment;
import org.tolven.doc.bean.TolvenMessageWithAttachments;
import org.tolven.doc.entity.DocAttachment;
import org.tolven.doc.entity.DocBase;
import org.tolven.doc.entity.DocXML;
import org.tolven.el.ExpressionEvaluator;
import org.tolven.msg.AccountProcessingProtectionLocal;
import org.tolven.msg.TolvenMessageSchedulerLocal;
import org.tolven.msg.TouchQueueLocal;
import org.tolven.provider.ProviderLocal;
import org.tolven.rules.RuleBaseLocal;
import org.tolven.rules.RulesLocal;
import org.tolven.rules.TolvenAgendaEventListener;
import org.tolven.security.key.DocumentSecretKey;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.trim.CE;
import org.tolven.trim.ex.TrimFactory;
/**
 * Provide general callback functions from rules.
 * @author John Churin
 *
 */
public abstract class AppEvalAdaptor implements MessageProcessorLocal {
    protected @Resource EJBContext ejbContext;
    protected @Resource SessionContext sessionContext;   
    protected @EJB AccountDAOLocal accountBean;
    protected @EJB AccountProcessingProtectionLocal accountProcessingProctectionBean;
    protected @EJB CreatorLocal creatorBean;
    protected @EJB DocumentLocal documentBean;
    protected @EJB DocProtectionLocal docProtectionBean;
    protected @EJB MenuLocal menuBean;
    protected @EJB RuleBaseLocal ruleBaseBean;
    protected @EJB RulesLocal rulesBean;
    protected @EJB ShareLocal shareBean;
    protected @EJB TolvenPropertiesLocal propertyBean;
    protected @EJB ProviderLocal providerBean;
    protected @EJB TouchLocal touchPlaceholderBean;
    protected @EJB TolvenMessageSchedulerLocal tmSchedulerBean;

    @EJB
    private TouchQueueLocal touchQueueBean;

    @EJB
    private TouchLocal touchBean;
    
    private static Logger logger = Logger.getLogger(AppEvalAdaptor.class);
    
    // Callback variables see cleanup() method
    protected TolvenMessage tm;
    private DocBase docBase;
    private Account account;
    private Account sourceAccount;
    private Date now;
    private StatefulSession workingMemory;
    private Map<String, MenuDataVersionMessage> mdvs;
    
    protected static final TrimFactory trimFactory = new TrimFactory( );

        
    protected CE sexToGender( String sex ) {
        CE gender = trimFactory.createCE();
        if ("female".equalsIgnoreCase(sex)) {
            gender.setDisplayName("Female");
            gender.setCode("C0015780"); // Female
        } else {
            gender.setDisplayName("Male");
            gender.setCode("C0024554"); // Male
        }
        gender.setCodeSystem("2.16.840.1.113883.6.56");
        gender.setCodeSystemVersion("2007AA");
        return gender;
    }
    
    public Object lookupResource( String resourceName) {
        Object rslt =  sessionContext.lookup(resourceName);
        return rslt;
    }
    
    /**
     * Return true if the specified list meets the condition, the placeholder
     * specifies the parent, typically the patient, of the list.
     * 
     * @param mdPlaceholder
     * @param condition
     *            - in ejbQL
     * @return true if the condition matches
     */
    
    public boolean onList(MenuData mdPlaceholder, MenuStructure msList, String condition, String... params) {
        return menuBean.onList(mdPlaceholder, msList, condition, params);
    }
    
    public void assertPlaceholder( MenuData mdPlaceholder) {
     assertPlaceholder(mdPlaceholder, false);
    }

    /**
     * Assert a placeholder into working memory
     * @param mdPlaceholder
     * @param override - Ignore check that limits assert to placeholders affected by this document
     */
    public void assertPlaceholder( MenuData mdPlaceholder, boolean override) {
     // make sure that we have not been asserted already.
     if (workingMemory.getFactHandle(mdPlaceholder) != null) {
        // we have already asserted this. so, ignore
            if (logger.isDebugEnabled()) {
                logger.debug("Ignoring the Assert Placeholder as it is already processed: " + mdPlaceholder);
            }
        return;         
     }
     if (!override && getDocument() != null && mdPlaceholder.getDocumentId()!=getDocument().getId()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignoring Assert Placeholder, not changed by this document: " + mdPlaceholder);
            }
            logger.info( "mdPlaceholder.getDcumentID: " + mdPlaceholder.getDocumentId());
            logger.info( "getDocument().getId(): " + getDocument().getId());
            return;
     }
     logger.info( "Assert Placeholder: " + mdPlaceholder);

     // remember the placeholder in the variables list, in case we need it later for populating
//[JMC, 3/1/2010] Is this needed???          ee.put(mdPlaceholder.getMenuStructure().getNode(), mdPlaceholder);
     // If it's a changed placeholder
//       if (mdPlaceholder.getDocumentId()==getDocument().getId()) {
         mdPlaceholder.setStatus(Status.ACTIVE);
         workingMemory.insert(mdPlaceholder);
//       }
        }

    protected void assertPlaceholders( List<MenuData> placeholders) {
        for (MenuData mdPlaceholder : placeholders) {
            assertPlaceholder(mdPlaceholder);
        }
    }
    
    /**
     * INitialize the working memory and set the passed in variables.
     * @param tm TolvenMessage used to get the associated account
     * @param now Time
     */
    public void initializeWorkingMemory(TolvenMessage tm, Date now) {
        if (null != now) {
            this.now = now;
        }
        if (null == this.tm) {
            this.tm = tm;
        }
        if (null == account) {
            account = accountBean.findAccount(tm.getAccountId());
        }
        workingMemory = ruleBaseBean.newStatefulSession(account);
        if (TolvenAgendaEventListener.logger.isDebugEnabled()) {
            TolvenAgendaEventListener listener = new TolvenAgendaEventListener();
            workingMemory.addEventListener(listener);
        }
        workingMemory.setGlobal("app", this);
        workingMemory.setGlobal("now", getNow());
        //new WMLogger(workingMemory);
    }
    
    /**
     * Initialize the message so that all member data variables are configured properly.
     * @param tm TolvenMessage that will be processed
     * @param now date
     */
    public void initializeMessage(TolvenMessage tm, Date now) {
        // setup callback variables
        this.tm = tm;
        this.now = now;
        
        mdvs = new HashMap<String, MenuDataVersionMessage>();
        account = accountBean.findAccount(tm.getAccountId());

        try {
            accountProcessingProctectionBean.decryptTolvenMessage(tm);
        } catch (Exception ex) {
            throw new RuntimeException("Could not decrypt TolvenMessage: " + tm.getId(), ex);
        }
        
//      System.out.println(new String(getDocumentContent( tm.getDocumentId() )));
        
        initializeWorkingMemory(tm, null);
    }
    /**
     * Simplified version of associateDocument to allow HL7 msg processing
     * Better approach might be to alter the original associateDocument so it
     * doesn't require the document to be an XML doc.
     * @param tm
     * @return
     * @throws LoginException
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void associateHL7Document( TolvenMessage tm, Date now ) throws Exception {
        initializeMessage(tm, now);
        
        docBase = documentBean.createNewDocument( "text/HL7", tm.getXmlNS(), getAccountUser() );
        logger.info( "Document created, id: " + docBase.getId() + " Account: " + docBase.getAccount().getId());
        String kbeKeyAlgorithm = propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
        int kbeKeyLength = Integer.parseInt(propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
        docBase.setAsEncryptedContent(tm.getPayload(), kbeKeyAlgorithm, kbeKeyLength);
        docBase.setFinalSubmitTime(now);
        
        // Finally, make sure that the document id is set in the message header
        if (tm.getDocumentId()==0) {
            tm.setDocumentId(docBase.getId());
        }
    }
    /**
     * Associate the document and attachments in the message with documents in the database either by
     * finding document(s) or creating document(s).
     * @param tm
     * @return
     * @throws LoginException
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void associateDocument( TolvenMessage tm, Date now ) throws Exception {
        initializeMessage(tm, now);
        
        // If not present in document DB, add it now.
        if (0==tm.getDocumentId() && tm.getPayload()!=null) {
            AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
            docBase = documentBean.createNewDocument(tm.getMediaType(), tm.getXmlNS(), accountUser );
            logger.info( "Document created, id: " + docBase.getId() + " Account: " + docBase.getAccount().getId());
            String kbeKeyAlgorithm = propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
            int kbeKeyLength = Integer.parseInt(propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
            docBase.setAsEncryptedContent(tm.getPayload(), kbeKeyAlgorithm, kbeKeyLength);
            docBase.setFinalSubmitTime(now);
        } else  if (0!=tm.getDocumentId()){
            // Document already exists, so get it.
            docBase = documentBean.findDocument(tm.getDocumentId() );
            logger.info( "Document, id: " + docBase.getId() + " already exists" );
            }
        // Check for attachments
        if (tm instanceof TolvenMessageWithAttachments) {
            logger.info( "Attachments... ");
            for (TolvenMessageAttachment attachment : ((TolvenMessageWithAttachments)tm).getAttachments()) {
                if (0==attachment.getDocumentId()) {
                    DocBase doc = new DocBase();
                    doc.setAccount(account);
                    doc.setMediaType( attachment.getMediaType() );
                    doc.setStatus(Status.NEW.value());
                    String kbeKeyAlgorithm = propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
                    int kbeKeyLength = Integer.parseInt(propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
                    doc.setAsEncryptedContent(attachment.getPayload(), kbeKeyAlgorithm, kbeKeyLength);
                    documentBean.createFinalDocument(doc );
                    documentBean.createAttachment( docBase, doc, attachment.getDescription(), null, now);
                    doc.setFinalSubmitTime(now);
                    logger.info( "Attachment: " + doc.getId() + " Length=" + attachment.getPayload().length);
                }
            }
        }
        // Special case if inbound (from another account)
        // If the message is inbound, we will be modifying the message so save the original as an attachment
        if (tm.getFromAccountId() !=0 && tm.getAccountId()!=tm.getFromAccountId()) {
            DocBase doc = documentBean.createXMLDocument( tm.getXmlNS(), tm.getAuthorId(), getAccount().getId() );
            doc.setMediaType( getDocument().getMediaType() );
            doc.setStatus(Status.NEW.value());
            String kbeKeyAlgorithm = propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
            int kbeKeyLength = Integer.parseInt(propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
            doc.setAsEncryptedContent(tm.getPayload(), kbeKeyAlgorithm, kbeKeyLength);
            documentBean.createAttachment( doc, getDocument(), "Original message", null, now);
            logger.info( "Saving original inbound message as attachment: " + doc.getId() + " Length=" + tm.getPayload().length);
            
            //Add attachments to original document, if any
            if (tm instanceof TolvenMessageWithAttachments) {
                TolvenMessageWithAttachments tma = (TolvenMessageWithAttachments) tm;
                for (TolvenMessageAttachment attachment : tma.getAttachments() ) {
                    DocBase docAttach = documentBean.createXMLDocument( tm.getXmlNS(), tm.getAuthorId(), getAccount().getId() );
                    docAttach.setAccount(account);
                    docAttach.setMediaType( attachment.getMediaType() );
                    ((DocXML)docAttach).setXmlNS(attachment.getXmlNS());
                    ((DocXML)docAttach).setSchemaURI(attachment.getXmlNS());
                    docAttach.setStatus(Status.NEW.value());
                    kbeKeyAlgorithm = propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
                    kbeKeyLength = Integer.parseInt(propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
                    docAttach.setAsEncryptedContent(attachment.getPayload(), kbeKeyAlgorithm, kbeKeyLength);
                    documentBean.createFinalDocument(docAttach );
                    documentBean.createAttachment( doc, docAttach, attachment.getDescription(), null, now);
                    logger.info( "Attachment DOC XX: " + docAttach.getId() + " Length=" + attachment.getPayload().length);
                }
            }
            
            
            scanInboundDocument(doc);
            // We actually mess with the copy, not the original, now an attachment.
            docBase = doc;
        }
        // Finally, make sure that the document id is set in the message header
        if (tm.getDocumentId()==0) {
            tm.setDocumentId(docBase.getId());
        }
    }
    protected abstract DocBase scanInboundDocument(DocBase doc) throws Exception;
    
    public void addMyProvider( Object providerId ) {
        providerBean.addMyProvider(getAccount(), (Long)providerId);
    }

    protected void runRules( ) throws Exception {
        if (workingMemory==null) throw new IllegalArgumentException("Missing working memory argument");
        
        // We don't assert the message, 
        // instead we assert a surrogate that describes the general mode of the message.
        Mode mode = new Mode( getAccount(), tm.getFromAccountId(), tm.getAccountId() );
        workingMemory.insert(mode);
        if ( !(mode.getDirection()==Mode.INBOUND)  && getDocument() != null) {
            // Now we need to remove any list items that reference this event. 
            // These will be WIP entries for the new Event.
            int count = menuBean.removeReferencingMenuData( getAccount().getId(), getDocument().getId(), false );
            if (logger.isDebugEnabled()) {
                logger.debug("Removed " + count + " non-placeholder menuData references to document " + getDocument().getId());
            }
        }
        
        // Assert the document itself.
        workingMemory.insert(getDocument());
        
        // Load working memory with the placeholders
        loadWorkingMemory( workingMemory );
    
        // Run the rule
        workingMemory.fireAllRules();
        workingMemory.dispose();
        
        menuBean.queueDeferredMDVs(mdvs);
    }
    /**
     * Persist a MenuData which includes keeping track of versions
     */
    public void persistMenuData( MenuData md ) {
        menuBean.persistMenuDataDeferred(md, mdvs);
    }
    
    protected abstract void loadWorkingMemory( StatefulSession workingMemory ) throws Exception;

    /**
     * Create a reference using an overridden parent context
     * @param mdPlaceholder
     * @param ms
     * @param mdParent
     * @return
     */
    public MenuData createReferenceMD( MenuData mdPlaceholder, MenuStructure ms, MenuData mdParent, boolean ignoreDuplicates ) {
        Map<String, Object> sourceMap = new HashMap<String, Object>();
        if (mdParent!=null) {
            sourceMap.put("_parent", mdParent);
            sourceMap.put(mdParent.getMenuStructure().getNode(), mdParent);
        }
        return createReferenceMD( mdPlaceholder, ms, sourceMap, ignoreDuplicates);
    }   

    /**
     * Create a new reference to the specified placeholder on the specified list (ms)
     * @param mdPlaceholder The menu Item to be referenced
     * @param ms MenuStructure of the item (usually a list) that will reference this placeholder 
     * @param act If not null, make an "act" variable available in the expression language.
     * @param ignoreDuplicates If there's already an item in the specified path referencing this placeholder, don't add it again.
     * @return The new MenuData item, usually a list item referencing the placeholder
     */
    public MenuData createReferenceMD( MenuData mdPlaceholder, MenuStructure ms, Map<String, Object> sourceMap, boolean ignoreDuplicates ) {
        try {
            ExpressionEvaluator ee = getExpressionEvaluator();
            MenuData md = null;
            // If the menuStructure says this should be unique by a certain key, then avoid duplicates.
                        if (md==null) {
                //If we're being asked to add a new one, check for duplicates.
                                if (ignoreDuplicates) {
                                        List<MenuData> references = menuBean.findReferencingMDs(mdPlaceholder, ms);
                                        if (references.size() > 0) {
                                                md = references.get(0);
                                                return md;
                                        }
                                }
                                md = new MenuData();
            }
            md.setMenuStructure(ms.getAccountMenuStructure());
            md.setAccount(mdPlaceholder.getAccount());
            md.setReference(mdPlaceholder);
            md.setDocumentId(mdPlaceholder.getDocumentId());
            md.setSourceAccountId(mdPlaceholder.getSourceAccountId());
            //set the latestConfig of MenuStructure on menudata
            md.setLatestConfig(ms.getAccountMenuStructure().getLatestConfig());
            
            if (sourceMap!=null) {
                MenuData mdParent = (MenuData) sourceMap.get("_parent"); 
                if (mdParent!=null) {
                    md.setParent01(mdParent);
                }
            }
            ee.pushContext();
            // Populate from the act
            ee.addVariables(sourceMap);
            ee.addVariable("_placeholder", mdPlaceholder);
            menuBean.populateMenuData(ee, md);
            ee.popContext();
            // Now see if it's already there and if not, create the new one.
            persistMenuData(md);
            if (logger.isDebugEnabled()) {
                logger.debug("Reference: " + md.getPath() + " to " + md.getReference().getPath());
            }
            return md;
        } catch (Exception e) {
            throw new RuntimeException( "Error creating reference from " + mdPlaceholder.getPath() + " to " + ms.getPath() + " with context " + sourceMap, e);
        }
    }
    
    public MenuData createReferenceMD( MenuData mdPlaceholder, MenuStructure ms ) {
        return createReferenceMD( mdPlaceholder, ms, new HashMap<String, Object>(), true );
    }
    
    /**
     * Create a reference to the specified menuData item
     * @param mdReferenced The menu Item to be referenced
     * @param path The path of the new item 
     * @return
     */
    @Deprecated
    public MenuData createReferenceMD( MenuData mdReferenced, String path ) {
        if(logger.isDebugEnabled()) {
            logger.debug("createReferenceMD(MenuData, String) is DEPRECATED due to performance. Use: createReferenceMD(MenuData, MenuStructure)");
        }
        MenuStructure ms = menuBean.findMenuStructure(getAccount().getId(), path);
        return createReferenceMD(mdReferenced, ms);
    }
    /**
     * Accept a rule fact, which must be a PlaceholderFact, extract MenuData from it and create a reference to
     * the placeholder from the named list (path).
     * @param fact
     * @param path
     * @return
     */
    //Commented out for making it obsolete for drools 5 upgrade
    /*public MenuData createReferenceMD( Fact fact, String path ) {
        if (!(fact instanceof PlaceholderFact)) {
            throw new RuntimeException( "Fact " + fact + " must be a PlaceholderFact to be referenced by " + path);
        }
        PlaceholderFact mdReferencedFact = (PlaceholderFact)fact;
        MenuStructure ms = menuBean.findMenuStructure(getAccount().getId(), path);
        return createReferenceMD(mdReferencedFact.getPlaceholder(), ms);
    }
    */
    /**
     * Create a reference using an overridden parent context
     * @param mdPlaceholder
     * @param ms
     * @param mdParent
     * @return
     */
    public MenuData createReferenceMD(MenuData mdPlaceholder, MenuStructure ms, MenuData mdParent) {
        return createReferenceMD(mdPlaceholder, ms, mdParent, true);
    }
    
    /**
     * Callback to create and populate a placeholder
     * @param trim
     * @param ms
     * @return
     */
    public MenuData createPlaceholder(MenuStructure ms) {
        return createPlaceholder(ms , null);
    }

    /**
     * Create placeholder with MenuStructure ms, and set one of its fields to menuData
     * 
     * @param ms
     * @param field
     * @param menuData
     * @return
     */
    public MenuData createPlaceholder(MenuStructure ms, String field, MenuData menuData) {
        Map<String, Object> menuDataMap = new HashMap<String, Object>();
        menuDataMap.put(field, menuData);
        return createPlaceholder(ms, menuDataMap);
    }

    /**
     * Create placeholder with MenuStructure ms, and set its fields to menuData objects in menuDataMap
     * 
     * @param ms
     * @param menuDataMap
     * @return
     */
    public MenuData createPlaceholder(MenuStructure ms, Map<String, Object> menuDataMap) {
        ExpressionEvaluator ee = getExpressionEvaluator();
        MenuData mdPlaceholder = new MenuData();
        mdPlaceholder.setMenuStructure(ms.getAccountMenuStructure());
        mdPlaceholder.setAccount(ms.getAccount());
        ee.pushContext();
        if (menuDataMap != null) {
            for (String key : menuDataMap.keySet()) {
                ee.addVariable(key, menuDataMap.get(key));
            }
        }
        menuBean.populateMenuData(ee, mdPlaceholder);
        ee.popContext();
        mdPlaceholder.setStatus(Status.NEW);
        if (getDocument() != null) {
            mdPlaceholder.setDocumentId(getDocument().getId());
        }
        persistMenuData(mdPlaceholder);
        return mdPlaceholder;
    }

    /**
     * Return a list of zero or more attachments. 
     * @return List of DocAttachment objects which reference the attached documents. 
     */
    public List<DocAttachment> getAttachments() {
        return documentBean.findAttachments(getDocument());
    }
    
    /**
     * Given a placeholder and a path to a list, find the item(s) in the list pointing to the placeholder.
     * @param ms
     * @param path
     * @return
     */
    public List<MenuData> findReferencingMDs( MenuData mdPlaceholder, String path ) {
        MenuStructure msList = menuBean.findMenuStructure(mdPlaceholder.getAccount().getId(), path);
        return menuBean.findReferencingMDs( mdPlaceholder, msList );
    }   
   
    /**
     * Insert any references to a specific placeholder in a specified list into working memory.
     * Items on the list do not need to be new.
     * @param mdPlaceholder
     * @param msList
     */
    public void insertReferencingItems(MenuData mdPlaceholder, MenuStructure msList ) {
        List<MenuData> items = menuBean.findReferencingMDs( mdPlaceholder, msList );
        for (MenuData item : items) {
            workingMemory.insert(item);
        }
        }

    /**
     * Insert a MenuData into working memory.
     * @param id
     */
    public void insertMenuDataItem(long id) {
        MenuData menuData = menuBean.findMenuDataItem(id);
        workingMemory.insert(menuData);
    }
    
    public void queueAttachments( String ns ) {
        if (!(tm instanceof TolvenMessageWithAttachments)) return;
        long destinationAccountId = getAccount().getId();
        try {
            TolvenMessageWithAttachments tma = (TolvenMessageWithAttachments) tm;
            for (TolvenMessageAttachment attachment : tma.getAttachments() ) {
                TolvenMessageWithAttachments tm = new TolvenMessageWithAttachments();
                tm.setAccountId(destinationAccountId);
                tm.setFromAccountId(getSourceAccount().getId());
                tm.setDocumentId(attachment.getDocumentId());
                tm.setAuthorId(tma.getAuthorId());
                tm.setXmlNS( ns );
                tm.setMediaType(attachment.getMediaType());
                tm.setPayload(attachment.getPayload());
                tmSchedulerBean.queueTolvenMessage(tm);
            }
        } catch (Exception e) {
            throw new RuntimeException( "Error queueing attachment to account " + destinationAccountId, e );
        }
    }
    
    public void createPlaceholdersForAttachments( String ns,String trimName,String menuPath,String status ) {
        if (!(tm instanceof TolvenMessageWithAttachments)) return;
        long destinationAccountId = getAccount().getId();
        try {
            TolvenMessageWithAttachments tma = (TolvenMessageWithAttachments) tm;
            for (TolvenMessageAttachment attachment : tma.getAttachments() ) {
                MenuData md = creatorBean.createTRIMPlaceholder(getAccountUser(), trimName, menuPath, getNow(), null);
                DocBase parentDoc = documentBean.findDocument(md.getDocumentId());
                DocBase attachmentDoc = documentBean.findDocument(attachment.getDocumentId());
                documentBean.createAttachment( parentDoc, attachmentDoc, attachment.getDescription(), getAccountUser(), getNow());              
            }
        } catch (Exception e) {
            throw new RuntimeException( "Error queueing attachment to account " + destinationAccountId, e );
        }
    }
    
    
    /**
     * Return the date of n years ago
     * @param years
     * @return
     */
    public Date yearsAgo( int years ) {
        GregorianCalendar cal = new GregorianCalendar( );
        cal.setTime(getNow());
        cal.add(GregorianCalendar.YEAR, -years);
        return cal.getTime();
    }
    
    /**
     * Return the date of n years from now
     * @param years
     * @return
     */
    public Date yearsFromNow( int years ) {
        GregorianCalendar cal = new GregorianCalendar( );
        cal.setTime(getNow());
        cal.add(GregorianCalendar.YEAR, years);
        return cal.getTime();
    }
    
    /**
     * Return an appropriate expression evaluator. Subclasses must implement this method 
     * @return
     */
    protected abstract ExpressionEvaluator getExpressionEvaluator();
    
    public void finalize( DocBase doc) {
        documentBean.finalizeDocument(doc);
        logger.info( "Finalized Document, id: " + doc.getId());
    }

    public DocBase getDocument() {
        return docBase;
    }

    public Account getSourceAccount() {
        if (sourceAccount==null) {
            sourceAccount = accountBean.findAccount(tm.getFromAccountId());
        }
        return sourceAccount;
    }

    public void initSourceAccount() {
        sourceAccount = null;
    }
        
    public void info( String message ) {
        logger.info( "AccountId: " + getAccount().getId() + " " + message);
    }

    public void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug("AccountId: " + getAccount().getId() + " " + message);
        }
    }

    public Account getAccount() {
        return account;
    }

    public Date getNow() {
        return now;
    }

    public StatefulSession getWorkingMemory() {
        return workingMemory;
    }
    /**
     * Get the account User for this message session. We first look for a conventional account user
     * that is, a real user with real access to this account. If not found, then look for or create
     * a "system" user and/or a "system" associate
     accountUser. The user is usually mdbuser. 
     * @return AccountUser
     */
    public AccountUser getAccountUser() {
        return TolvenRequest.getInstance().getAccountUser();
        /*String principal = (String) TolvenSessionWrapperFactory.getInstance().getPrincipal();
        AccountUser au = accountBean.findAccountUser(principal,getAccount().getId());
        if(au == null)
            throw new RuntimeException("No account user found for UserName: "+principal+" Account: "+getAccount());
        return au;*/
    }
    
    /**
     * Get the decrypted content of a document. This requires that the account user be 
     * @param documentId The document to be decrypted
     * @return The decrypted byte array 
     */
    public byte[] getDocumentContent(long documentId, PrivateKey userPrivateKey) {
        String principal = (String)TolvenSessionWrapperFactory.getInstance().getPrincipal();
        long accountId = getAccount().getId();
        AccountUser accountUser = accountBean.findAccountUser(principal, accountId);
        if (accountUser==null) {
            throw new RuntimeException( "Document decryption not possible; Account " + accountId + " has not added " + principal + " as a user of the account");
        }
        DocBase d = documentBean.findDocument(documentId);
        byte[] bytes = docProtectionBean.getDecryptedContent(d, accountUser, userPrivateKey);
        return bytes;
    }
    
    /**
     * This can be called as the last expression (finally) by process() since all instVars are
     * related to the actual process method call and not used by subsequent calls
     */
    protected void cleanup() {
        tm = null;
        docBase = null;
        account = null;
        sourceAccount = null;
        now = null;
        workingMemory = null;
        mdvs = null;
    }

    /**
     * Ensure that the "update placeholder" will (later) be reprocessed.
     */
    public void touchIf(MenuData updatePlaceholder, MenuData focalPlaceholder) {
        List<Touch> touches = touchBean.findTouches(updatePlaceholder);
        if (!touches.isEmpty()) {
            return;
        }
        Touch touch = new Touch();
        touch.setAccount(updatePlaceholder.getAccount());
        touch.setFocalPlaceholder(focalPlaceholder);
        touch.setUpdatePlaceholder(updatePlaceholder);
        touchPlaceholderBean.persistTouch(touch);
        TouchPlaceholderMessage m = new TouchPlaceholderMessage(updatePlaceholder.getId(), updatePlaceholder.getAccount().getId());
        touchQueueBean.send(m);
    }
    
}
