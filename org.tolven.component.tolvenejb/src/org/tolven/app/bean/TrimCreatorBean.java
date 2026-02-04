package org.tolven.app.bean;

import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.tolven.app.CreatorLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.TrimCreatorLocal;
import org.tolven.app.TrimCreatorRemote;
import org.tolven.app.TrimLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.TrimHeader;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.DocumentLocal;
import org.tolven.doc.entity.DocXML;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.BindPhase;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.TRIMException;
import org.tolven.trim.ex.TrimEx;

/**
 * This class handles create new trim and submit the wizard

 */
@Stateless()
@Local(TrimCreatorLocal.class)
@Remote(TrimCreatorRemote.class)

public class TrimCreatorBean implements TrimCreatorLocal {
	@EJB
	private TrimLocal trimBean;
	@EJB
	private MenuLocal menuBean;
	@EJB
	private DocumentLocal documentBean;
	
	@EJB
	private CreatorLocal creatorBean;
	
	Logger logger = Logger.getLogger(this.getClass());
	static final String TRIM_NS = "urn:tolven-org:trim:4.0"; 
	
	public TrimCreatorBean( ) {

	}
	public TrimEx createTrim( AccountUser accountUser, String trimPath, String context, Date now ) throws JAXBException, TRIMException {
		MenuData mdTrim = null;
		TrimHeader trimHeader = trimBean.findOptionalTrimHeader(trimPath);
		if (trimHeader==null) {
			// Get the TRIM template as XML
			// If the account doesn't know about this, then we'll allow access to the accountTemplate for this account type.
			mdTrim = menuBean.findDefaultedMenuDataItem(accountUser.getAccount(), trimPath);
			if (mdTrim==null) throw new IllegalArgumentException( "No TRIM item found for " + trimPath);
			trimHeader = mdTrim.getTrimHeader();
		}
		if (trimHeader==null) throw new IllegalArgumentException( "No TRIM found for " + trimPath);
		TrimEx trim = null;
		try {
			trim = trimBean.parseTrim(trimHeader.getTrim(), accountUser, context, now, null );
		} catch (RuntimeException e) {
			throw new RuntimeException( "Error parsing TRIM '" + trimHeader.getName() + "'", e );
		}
		return trim;
	}

	
	/**
	 * Instantiate a new document and new event pointing to it. The event also points to the eventual placeholder.
	 * @param accountUser
	 * @param trimPath The trimName The key to the MenuStructure item containing the template XML. 
	 * @param context
	 * @param now
	 * @param alias - when binding trim, use this alias for the context object.
	 * @param responseRelation 
	 * @return MenuData - containing the event, not the placeholder
	 * @throws JAXBException
	 * @throws TRIMException
	 */
	public MenuData createTRIMPlaceholder( AccountUser accountUser, String trimPath, String context, Date now, String relationName, ActRelationship actRelationship ) throws JAXBException, TRIMException {
		MenuData mdTrim = null;
		TrimHeader trimHeader = trimBean.findOptionalTrimHeader(trimPath);
		if (trimHeader==null) {
			// Get the TRIM template as XML
			// If the account doesn't know about this, then we'll allow access to the accountTemplate for this account type.
			mdTrim = menuBean.findDefaultedMenuDataItem(accountUser.getAccount(), trimPath);
			if (mdTrim==null) throw new IllegalArgumentException( "No TRIM item found for " + trimPath);
			trimHeader = mdTrim.getTrimHeader();
		}
		if (trimHeader==null) throw new IllegalArgumentException( "No TRIM found for " + trimPath);
		TrimEx trim = null;
		try {
			trim = trimBean.parseTrim(trimHeader.getTrim(), accountUser, context, now, null );
		} catch (RuntimeException e) {
			throw new RuntimeException( "Error parsing TRIM '" + trimHeader.getName() + "'", e );
		}
		// Setup variables we'll need for populate evaluations
		
		MenuPath contextPath = new MenuPath(context);
		Map<String, Object> variables = new HashMap<String, Object>(10);
		variables.putAll(contextPath.getNodeValues());
		{
			String assignedPath=null;
			try{
				assignedPath = accountUser.getProperty().get("assignedAccountUser");
			}catch (Exception e) {
			}
			if (assignedPath!=null) {
				MenuData assigned = menuBean.findMenuDataItem(accountUser.getAccount().getId(), assignedPath);
				variables.put("assignedAccountUser", assigned);
			}
		}
		variables.put("trim", trim);

		// Create an event to hold this trim document
		DocXML docXML = documentBean.createXMLDocument( TRIM_NS, accountUser.getUser().getId(), accountUser.getAccount().getId() );
		docXML.setSignatureRequired( creatorBean.isSignatureRequired(trim, accountUser.getAccount().getAccountType().getKnownType() ) );
		logger.info( "Document (placeholder) created, id: " + docXML.getId());
		// Call computes for the first time now
		creatorBean.computeScan( trim, accountUser, contextPath, now, docXML.getDocumentType());
		// Bind to placeholders
		creatorBean.placeholderBindScan( accountUser, trim, mdTrim, contextPath, now, BindPhase.CREATE, docXML);
		// Create an event
		MenuData mdEvent = creatorBean.establishEvent( accountUser.getAccount(), trim, now, variables);
		if (mdEvent==null) {
			throw new RuntimeException( "Unable to create instance of event for " + trim.getName());
		}
		mdEvent.setDocumentId(docXML.getId());
		// insert message data to trim
		if (actRelationship!=null)
			((ActEx)trim.getAct()).getRelationship().get(relationName).setAct(actRelationship.getAct());
		// Marshal the finished TRIM into XML and store in the document.
		creatorBean.marshalToDocument( trim, docXML );
		
		// Make sure this item shows up on the activity list
		creatorBean.addToWIP(mdEvent, trim, now, variables );
		
		return mdEvent;
	}
	
	/**
	 * Submit the document associated with this event
	 * @throws Exception 
	 */
	public void submitTrim(MenuData mdEvent, AccountUser activeAccountUser) throws Exception {
		creatorBean.submit(mdEvent, activeAccountUser, null);
    }
	/**
	 * Submit the document associated with this event
	 * @throws Exception 
	 */
	public void submitTrim(TrimEx trim,String context, AccountUser accountUser,Date now,PrivateKey privateKey) throws Exception {
		MenuData mdTrim = null;		
		TrimHeader trimHeader = trimBean.findOptionalTrimHeader(trim.getName());
		
		if (trimHeader==null) {
			// Get the TRIM template as XML
			// If the account doesn't know about this, then we'll allow access to the accountTemplate for this account type.
			mdTrim = menuBean.findDefaultedMenuDataItem(accountUser.getAccount(), trim.getName());
			if (mdTrim==null) throw new IllegalArgumentException( "No TRIM item found for " + trim.getName());
			trimHeader = mdTrim.getTrimHeader();
		
		}
		
		MenuPath contextPath = new MenuPath(context);
		Map<String, Object> variables = new HashMap<String, Object>(10);
		variables.putAll(contextPath.getNodeValues());
		{
			String assignedPath=null;
			try{
				assignedPath = accountUser.getProperty().get("assignedAccountUser");
			}catch (Exception e) {
			}
			if (assignedPath!=null) {
				MenuData assigned = menuBean.findMenuDataItem(accountUser.getAccount().getId(), assignedPath);
				variables.put("assignedAccountUser", assigned);
			}
		}
		variables.put("trim", trim);

		// Create an event to hold this trim document
		DocXML docXML = documentBean.createXMLDocument( TRIM_NS, accountUser.getUser().getId(), accountUser.getAccount().getId() );
		docXML.setSignatureRequired( creatorBean.isSignatureRequired(trim, accountUser.getAccount().getAccountType().getKnownType() ) );
		logger.info( "Document (placeholder) created, id: " + docXML.getId());
		// Call computes for the first time now
		creatorBean.computeScan( trim, accountUser, contextPath, now, docXML.getDocumentType());
		// Bind to placeholders
		creatorBean.placeholderBindScan( accountUser, trim, mdTrim, contextPath, now, BindPhase.CREATE, docXML);
		// Create an event
		MenuData mdEvent = creatorBean.establishEvent( accountUser.getAccount(), trim, now, variables);
		
		if (mdEvent==null) {
			throw new RuntimeException( "Unable to create instance of event for " + trim.getName());
		}
		
		mdEvent.setDocumentId(docXML.getId());
		// insert message data to trim
		creatorBean.marshalToDocument( trim, docXML );
		
		// Make sure this item shows up on the activity list
		creatorBean.addToWIP(mdEvent, trim, now, variables );
		
		creatorBean.submit(mdEvent, accountUser, privateKey);
		
		
    }

}