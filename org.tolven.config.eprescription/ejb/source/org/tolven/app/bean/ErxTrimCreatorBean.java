package org.tolven.app.bean;

import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.tolven.app.CreatorLocal;
import org.tolven.app.ErxTrimCreatorLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.TrimLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.TrimHeader;
import org.tolven.core.ActivationLocal;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.DocProtectionLocal;
import org.tolven.doc.DocumentLocal;
import org.tolven.doc.entity.DocXML;
import org.tolven.provider.ProviderLocal;
import org.tolven.trim.BindPhase;
import org.tolven.trim.ex.TRIMException;
import org.tolven.trim.ex.TrimEx;

/**
 * This class handles create new trim and submit the wizard
 * 
 * @author Suja Sundaresan <suja.sundaresan@cyrusxp.com>
 * @author Vineetha George
 * @since File available since Release 0.0.1
 */
/*
=============================================================================================================================================
No:  	|  Created/Updated Date |    Created/Updated By |     Method name/Comments            
==============================================================================================================================================
1    	|     12/12/2009       |     Suja Sundaresan 	|      Initial Version 
==============================================================================================================================================
2    	|     03/16/2011        |     Vineetha George 	|      Changed TrimCreatorBean to ErxTrimCreatorBean 
==============================================================================================================================================
*/
@Stateless()
@Local(ErxTrimCreatorLocal.class)
public class ErxTrimCreatorBean implements ErxTrimCreatorLocal {
	@EJB ActivationLocal activationBean;
	@EJB MenuLocal menuBean;
	@EJB TrimLocal trimBean;
	@EJB DocumentLocal documentBean;
    @EJB DocProtectionLocal docProtectionBean;
    @EJB ProviderLocal providerBean;
    @EJB TolvenPropertiesLocal propertyBean;
	@EJB private CreatorLocal creatorBean;
	private static final String TRIM_NS = "urn:tolven-org:trim:4.0";
	Logger logger = Logger.getLogger(this.getClass());
	
	public TrimEx createTrim(AccountUser accountUser, String trimPath,
			String context, Date now) throws JAXBException, TRIMException {
		MenuData mdTrim = null;
		TrimHeader trimHeader = trimBean.findOptionalTrimHeader(trimPath);
		if (trimHeader == null) {
			// Get the TRIM template as XML
			// If the account doesn't know about this, then we'll allow access
			// to the accountTemplate for this account type.
			mdTrim = menuBean.findDefaultedMenuDataItem(accountUser
					.getAccount(), trimPath);
			if (mdTrim == null)
				throw new IllegalArgumentException("No TRIM item found for "
						+ trimPath);
			trimHeader = mdTrim.getTrimHeader();
		}
		if (trimHeader == null)
			throw new IllegalArgumentException("No TRIM found for " + trimPath);
		TrimEx trim = null;
		try {
			trim = trimBean.parseTrim(trimHeader.getTrim(), accountUser,
					context, now, null);
		} catch (RuntimeException e) {
			throw new RuntimeException("Error parsing TRIM '"
					+ trimHeader.getName() + "'", e);
		}
		return trim;
	}

	/**
	 * Submit the document associated with this event
	 * 
	 * @throws Exception
	 */
	public void submitTrim(TrimEx trim, String context,
			AccountUser accountUser, Date now,PrivateKey privateKey	) throws Exception {
		MenuData mdTrim = null;
		TrimHeader trimHeader = trimBean.findOptionalTrimHeader(trim.getName());

		if (trimHeader == null) {
			// Get the TRIM template as XML
			// If the account doesn't know about this, then we'll allow access
			// to the accountTemplate for this account type.
			mdTrim = menuBean.findDefaultedMenuDataItem(accountUser
					.getAccount(), trim.getName());
			if (mdTrim == null)
				throw new IllegalArgumentException("No TRIM item found for "
						+ trim.getName());
			trimHeader = mdTrim.getTrimHeader();

		}

		MenuPath contextPath = new MenuPath(context);
		Map<String, Object> variables = new HashMap<String, Object>(10);
		variables.putAll(contextPath.getNodeValues());
		{
			String assignedPath = null;
			try {
				assignedPath = accountUser.getProperty().get(
						"assignedAccountUser");
			} catch (Exception e) {
			}
			if (assignedPath != null) {
				MenuData assigned = menuBean.findMenuDataItem(accountUser
						.getAccount().getId(), assignedPath);
				variables.put("assignedAccountUser", assigned);
			}
		}
		variables.put("trim", trim);

		// Create an event to hold this trim document
		DocXML docXML = documentBean.createXMLDocument(TRIM_NS, accountUser
				.getUser().getId(), accountUser.getAccount().getId());
		docXML.setSignatureRequired(creatorBean.isSignatureRequired(trim, accountUser
				.getAccount().getAccountType().getKnownType()));
		logger.info("Document (placeholder) created, id: " + docXML.getId());
		// Call computes for the first time now
		creatorBean.computeScan(trim, accountUser, contextPath, now, docXML	.getDocumentType());
		// Bind to placeholders
		creatorBean.placeholderBindScan(accountUser, trim, mdTrim,contextPath, now, BindPhase.CREATE, docXML);
		// Create an event
		MenuData mdEvent = creatorBean.establishEvent(accountUser.getAccount(), trim, now,variables);

		if (mdEvent == null) {
			throw new RuntimeException(
					"Unable to create instance of event for " + trim.getName());
		}

		mdEvent.setDocumentId(docXML.getId());
		// insert message data to trim
		creatorBean.marshalToDocument(trim, docXML);

		// Make sure this item shows up on the activity list
		creatorBean.addToWIP(mdEvent, trim, now, variables);

		creatorBean.submit(mdEvent, accountUser, null);
	}

	public String addTrimToActivityList(TrimEx trim, String context,
			AccountUser accountUser, Date now,PrivateKey privateKey) throws Exception {
		MenuData mdTrim = null;
		TrimHeader trimHeader = trimBean.findOptionalTrimHeader(trim.getName());

		if (trimHeader == null) {
			// Get the TRIM template as XML
			// If the account doesn't know about this, then we'll allow access
			// to the accountTemplate for this account type.
			mdTrim = menuBean.findDefaultedMenuDataItem(accountUser
					.getAccount(), trim.getName());
			if (mdTrim == null)
				throw new IllegalArgumentException("No TRIM item found for "
						+ trim.getName());
			trimHeader = mdTrim.getTrimHeader();

		}

		MenuPath contextPath = new MenuPath(context);
		Map<String, Object> variables = new HashMap<String, Object>(10);
		variables.putAll(contextPath.getNodeValues());
		{
			String assignedPath = null;
			try {
				assignedPath = accountUser.getProperty().get(
						"assignedAccountUser");
			} catch (Exception e) {
			}
			if (assignedPath != null) {
				MenuData assigned = menuBean.findMenuDataItem(accountUser
						.getAccount().getId(), assignedPath);
				variables.put("assignedAccountUser", assigned);
			}
		}
		variables.put("trim", trim);

		// Create an event to hold this trim document
		DocXML docXML = documentBean.createXMLDocument(TRIM_NS, accountUser
				.getUser().getId(), accountUser.getAccount().getId());
		docXML.setSignatureRequired(creatorBean.isSignatureRequired(trim, accountUser
				.getAccount().getAccountType().getKnownType()));
		logger.info("Document (placeholder) created, id: " + docXML.getId());
		// Call computes for the first time now
		creatorBean.computeScan(trim, accountUser, contextPath, now, docXML.getDocumentType());
		// Bind to placeholders
		creatorBean.placeholderBindScan(accountUser, trim, mdTrim,
				contextPath, now, BindPhase.CREATE, docXML);
		// Create an event
		MenuData mdEvent = creatorBean.establishEvent(accountUser.getAccount(), trim, now,
				variables);

		if (mdEvent == null) {
			throw new RuntimeException(
					"Unable to create instance of event for " + trim.getName());
		}

		mdEvent.setDocumentId(docXML.getId());
		// insert message data to trim
		creatorBean.marshalToDocument(trim, docXML);

		// Make sure this item shows up on the activity list
		creatorBean.addToWIP(mdEvent, trim, now, variables);

		return mdEvent.getPath();
	}
}
