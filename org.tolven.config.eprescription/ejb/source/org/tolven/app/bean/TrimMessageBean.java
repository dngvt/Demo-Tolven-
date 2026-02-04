package org.tolven.app.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.tolven.app.CreatorLocal;
import org.tolven.app.EPrescriptionLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.TrimLocal;
import org.tolven.app.TrimMessageLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuDataWord;
import org.tolven.app.entity.MenuStructure;
import org.tolven.app.entity.TrimHeader;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.DocProtectionLocal;
import org.tolven.doc.DocumentLocal;
import org.tolven.doc.bean.TolvenMessageAttachment;
import org.tolven.doc.bean.TolvenMessageWithAttachments;
import org.tolven.doc.entity.DocAttachment;
import org.tolven.doc.entity.DocBase;
import org.tolven.doc.entity.DocXML;
import org.tolven.logging.TolvenLogger;
import org.tolven.msg.TolvenMessageSchedulerLocal;
import org.tolven.security.key.DocumentSecretKey;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.Application;
import org.tolven.trim.BindPhase;
import org.tolven.trim.ENXPSlot;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.Trim;
import org.tolven.trim.TrimMarshaller;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.TRIMException;
import org.tolven.trim.ex.TrimEx;
import org.tolven.trim.ex.TrimFactory;


@Stateless()
@Local(TrimMessageLocal.class)

public class TrimMessageBean implements TrimMessageLocal {

	private static final String TRIM_NS = "urn:tolven-org:trim:4.0";
	
	@PersistenceContext private EntityManager em;	
	@EJB private TrimLocal trimBean;
	@EJB private MenuLocal menuBean;
	@EJB private EPrescriptionLocal epBean;
	@EJB private DocumentLocal documentBean;
	@EJB private CreatorLocal creatorBean;
	@EJB DocProtectionLocal docProtectionBean;
	@EJB TolvenPropertiesLocal propertyBean;
	
	@EJB
    protected TolvenMessageSchedulerLocal tmSchedulerBean;
	
	private Date now;
	private TrimFactory factory;	
	protected AccountUser accountUser;
	private String physicianName;
	//@EJB InvitationLocal mailBean;
	@Resource EJBContext ejbContext;
    private static TrimMarshaller trimMarshaller;
	Logger logger = Logger.getLogger(this.getClass());

	
	public TrimMessageBean( ) {
	}
	
	public String getPhysicianName() {
		return physicianName;
	}

	public void setPhysicianName(String physicianName) {
		this.physicianName = physicianName;
	}
	
	public void setFactory(TrimFactory factory) {
		this.factory = new TrimFactory();
	}
	public AccountUser getAccountUser() {
        return accountUser;
    }
	public void setAccountUser(AccountUser accountUser) {
		this.accountUser = accountUser;
	}
	public Date getNow() {
		return now;
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
	public MenuData createTRIMPlaceholder( AccountUser accountUser, String trimPath, String context, Date now, String alias, ArrayList<ActRelationship> RelationshipList ,PrivateKey privateKey) throws JAXBException, TRIMException {
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
			trim = trimBean.parseTrim(trimHeader.getTrim(), accountUser, context, now, alias );
		} catch (RuntimeException e) {
			throw new RuntimeException( "Error parsing TRIM '" + trimHeader.getName() + "'", e );
		}
		// Setup variables we'll need for populate evaluations
		
		MenuPath contextPath = new MenuPath(context);
		Map<String, Object> variables = new HashMap<String, Object>(10);
		variables.putAll(contextPath.getNodeValues());
		{
//			String assignedPath = accountUser.getProperty().get("assignedAccountUser");
//			if (assignedPath!=null) {
//				MenuData assigned = menuBean.findMenuDataItem(accountUser.getAccount().getId(), assignedPath);
//				variables.put("assignedAccountUser", assigned);
//			}
		}
		variables.put("trim", trim);

		// Create an event to hold this trim document
		DocXML docXML = documentBean.createXMLDocument( TRIM_NS, accountUser.getUser().getId(), accountUser.getAccount().getId() );
		docXML.setSignatureRequired( isSignatureRequired(trim, accountUser.getAccount().getAccountType().getKnownType() ) );
		TolvenLogger.info( "Document (placeholder) created, id: " + docXML.getId() , TrimMessageBean.class);
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
		String firstName = "";
		String middleName = "";
		String lastName = "";
		// insert message data to trim
		ListIterator<ActRelationship> iter = RelationshipList.listIterator();
		while(iter.hasNext()){
			ActRelationship relationship = iter.next();
			String relationName = relationship.getName();
			if(relationName.equals("response")){
				((ActEx) trim.getAct()).getRelationship().get("response")
				.setAct(relationship.getAct());
			}
			if(relationName.equals("header")){
				((ActEx) trim.getAct()).getRelationship().get("header")
				.setAct(relationship.getAct());
			}
			else if(relationName.equals("pharmacy")){
				((ActEx) trim.getAct()).getRelationship().get("pharmacy")
				.setAct(relationship.getAct());
			}
			else if(relationName.equals("prescriber")){
				String physician = "";
				((ActEx) trim.getAct()).getRelationship().get("prescriber")
				.setAct(relationship.getAct());
				for (int i = 0; i < relationship.getAct().getObservation().getValues().size(); i++) {
					String label = relationship.getAct().getObservation().getValues().get(i).getLabel().getValue().toString();
					String value = relationship.getAct().getObservation().getValues().get(i).getST().getValue().toString();
					if (label.equals("Prescriber FirstName") && !value.isEmpty()) {
						firstName = relationship.getAct().getObservation().getValues().get(i).getST().getValue().toString();
						physician = firstName;						
					}
					if (label.equals("Prescriber MiddleName") && !value.isEmpty()) {
						middleName = relationship.getAct().getObservation().getValues().get(i).getST().getValue().toString();
						physician = physician+ " " + middleName ;
					}
					if (label.equals("Prescriber LastName") && !value.isEmpty()) {
						lastName = relationship.getAct().getObservation().getValues().get(i).getST().getValue().toString();
						physician = physician+ " " + lastName ;
					}
					setPhysicianName(physician.toLowerCase());
					}
				}
			else if(relationName.equals("patient")){
				((ActEx) trim.getAct()).getRelationship().get("patient")
				.setAct(relationship.getAct());
			}
			else if(relationName.equals("MedicationPrescribed")){
				((ActEx) trim.getAct()).getRelationship().get("MedicationPrescribed")
				.setAct(relationship.getAct());
			}
			else if(relationName.equals("MedicationRequested")){
				((ActEx) trim.getAct()).getRelationship().get("MedicationRequested")
				.setAct(relationship.getAct());
			}
			else if(relationName.equals("MedicationRequested1")){
				((ActEx) trim.getAct()).getRelationship().get("MedicationRequested1")
				.setAct(relationship.getAct());
			}
			else if(relationName.equals("MedicationRequested2")){
				((ActEx) trim.getAct()).getRelationship().get("MedicationRequested2")
				.setAct(relationship.getAct());
			}
			else if(relationName.equals("toSureScripts")){
				((ActEx) trim.getAct()).getRelationship().get("toSureScripts")
				.setAct(relationship.getAct());
			}
			else if(relationName.equals("medicationDetails")){
				((ActEx) trim.getAct()).getRelationship().get("medicationDetails")
				.setAct(relationship.getAct());
			}else if(relationName.equals("supervisor")){
				((ActEx) trim.getAct()).getRelationship().get("supervisor")
				.setAct(relationship.getAct());
			}else if(relationName.equals("originalPrescription")){
				((ActEx) trim.getAct()).getRelationship().get("originalPrescription")
				.setAct(relationship.getAct());
			}
			else if(relationName.equals("currentMedication")){
				((ActEx) trim.getAct()).getRelationship().get("currentMedication")
				.setAct(relationship.getAct());
			}
		}
		
		if (trim.getName().equals("obs/evn/refillRequest")) {
			List<ENXPSlot> nameENXPSlot = ((ActEx) trim.getAct()).getParticipations().get(0).getRole().getPlayer().getName().getENS().get(0).getParts();
			nameENXPSlot.get(0).getST().setValue(firstName);//FN
			if(!lastName.equals(firstName))
				nameENXPSlot.get(2).getST().setValue(lastName);//LN
			if (null != middleName && !middleName.isEmpty()) { 
				if(!middleName.equals(firstName) && !middleName.equals(lastName))
				nameENXPSlot.get(1).getST().setValue(middleName);//MN
			}
			
			
		}
		// Marshal the finished TRIM into XML and store in the document.
		creatorBean.marshalToDocument( trim, docXML );
		
		// Make sure this item shows up on the activity list
		addToList(mdEvent, trim, now, variables, "echr:assigned:activity:all");
		addToList(mdEvent, trim, now, variables, "echr:activity:all");
		Date currentDate = new Date();
		mdEvent.setDate01(currentDate);
		TolvenLogger.info("Current time: "+ currentDate, TrimMessageBean.class);
		startTimer(mdEvent, trim);
		return mdEvent;
	}
	
	private void addToList(MenuData mdEvent, TrimEx trim, Date now, Map<String, Object> variables, String wip) {
		if (wip!=null) {
			// Add item to the to do list as well.
			// The Wip item points to the Event which in turn points to the placeholder. 
			MenuStructure msToDo = menuBean.findMenuStructure(mdEvent.getAccount().getId(), wip);			
			MenuData mdToDo = new MenuData( );
			mdToDo.setMenuStructure(msToDo.getAccountMenuStructure());
			mdToDo.setReference(mdEvent);
			mdToDo.setDate01( now );
			if (mdEvent.getParent01()!=null) {
				mdToDo.setString01(mdEvent.getParent01().getString02() + " " + mdEvent.getParent01().getString01());
			} else {
				mdToDo.setString01("-New- ");
				
			}
	//		mdToDo.setParent01(md.getParent01());
//			mdToDo.setString02("Unfinished: " + mdEvent.getString01());
			
			mdToDo.setString03((String)TolvenSessionWrapperFactory.getInstance().getPrincipal());
			mdToDo.setDocumentId(mdEvent.getDocumentId());
			if (wip.equals("echr:activity:all") && getPhysicianName() != null) {
				addPhrase(getPhysicianName(), "For", "en_US", mdToDo);
			}
			menuBean.populateMenuData(variables, mdToDo);
			menuBean.persistMenuData(mdToDo);
		}
	}
	
	/**
	 * Adds the phrase to menu data word so that it can be filtered in the all activity list.
	 * @param phrase
	 * @param field
	 * @param language
	 * @param md
	 */
	private void addPhrase(String phrase, String field, String language, MenuData md) {
		ArrayList<String> data = new ArrayList<String>();
		if( phrase==null) return;
		// Split by any non-word characters
		String words[] = phrase.split("\\W");
		int position = 0;
		for ( String rawWord : words ) {
			String word = rawWord.trim().toLowerCase();
			
			if (word.length()>0 && !StopList.ignore( word, language )) {
				position++;
				
				MenuDataWord newWord = new MenuDataWord( );
				newWord.setField(field);
				newWord.setLanguage(language);
				newWord.setPosition(position);
				newWord.setWord(word);
				newWord.setMenuData(md);
				newWord.setMenuStructure(md.getMenuStructure());
				
				if (!data.contains(word)) {		//!md.getWords().contains(newWord)
					data.add(word);
					md.getWords().add(newWord); 
				}
				
			}
		}
	}
	
	public boolean isSignatureRequired( TrimEx trim, String knownType ) {
		Application application = getApplication( trim, knownType );
		if (application != null) {
			return application.isSignatureRequired();
		}
		return false;
	}
	public Application getApplication( Trim trim, String knownType ) {
		// Find the application section
		for ( Application app : trim.getApplications()) {
			if (knownType.equals(app.getName())) {
				return app;
			}
		}
		return null;
	}
	
	/**
	 * An existing placeholder is transitioned (updated). The placeholder document is immutable. So we create
	 * a new document.
	 * We won't actually change the placeholder until the change is submitted.
	 * @author Syed
	 * added on 10/28/2009
	 */
	public MenuData createTRIMEvent( MenuData mdPlaceholder, AccountUser accountUser, String transitionName, Date now, Long refillQuantity,PrivateKey privateKey ) throws JAXBException, TRIMException {
		if (mdPlaceholder==null) {
			throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "]Missing placeholder for transition");
		}
		String knownType = accountUser.getAccount().getAccountType().getKnownType();
		DocXML docXMLFrom = (DocXML) documentBean.findDocument(mdPlaceholder.getDocumentId());
		// Note: We're reading this in from an immutable document 
		// but will be creating a new document with the modified trim, not
		// modifying the original document.
		TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(docXMLFrom, accountUser,privateKey);
		// We reverse the normal, that is, use the trim to populate the event
		DocXML docXMLNew = documentBean.createXMLDocument( TRIM_NS, accountUser.getUser().getId(), accountUser.getAccount().getId() );
		docXMLNew.setSignatureRequired(isSignatureRequired( trim, knownType ));
		// Carry forward attachments
		documentBean.copyAttachments(docXMLFrom, docXMLNew );
        logger.info( "Document (event) created, id: " + docXMLNew.getId() );
		
		// Adjust status based on selected transition
		StatusInfo sInfo = creatorBean.calculateTransition( trim, transitionName);
		
		// If we have not stored a tolvenId for placeholder yet, do it now.
		// This can happen when the placeholder started from an external message
		if (trim.getTolvenId(accountUser.getAccount().getId())!=null) {
			trim.addTolvenId(creatorBean.createTolvenId( mdPlaceholder, null, now, sInfo.getStatus(), accountUser.getUser().getLdapUID() ));
		}
		// Setup up context variables for substitution
		Map<String, Object> variables = new HashMap<String, Object>(10);
		MenuPath contextPath = new MenuPath (mdPlaceholder.getPath());
		variables.putAll(contextPath.getNodeValues());
		{
			String assignedPath = accountUser.getProperty().get("assignedAccountUser");
			if (assignedPath!=null) {
				MenuData assigned = menuBean.findMenuDataItem(accountUser.getAccount().getId(), assignedPath);
				variables.put("assignedAccountUser", assigned);
			}
		}
		variables.put("trim", trim);
		// New menu data event
		String instancePath = creatorBean.getInstancePath(trim, knownType);
		// Create an event to hold this transaction
		MenuData mdEvent = creatorBean.createEvent( accountUser.getAccount(), instancePath, trim, now, variables);
		mdEvent.setDocumentId(docXMLNew.getId());
		mdEvent.setActStatus(sInfo.getStatus());
		if(sInfo.getStatusReason() != null)
		mdEvent.setActStatusReason(sInfo.getStatusReason());
		
		// Reference the event in the Trim (add to event history)
		trim.addTolvenEventId(creatorBean.createTolvenId( mdEvent, null, now, sInfo.getStatus(), accountUser.getUser().getLdapUID() ));
		/*setting refillQuantity*/
		((ActEx)trim.getAct()).getRelationship().get("medicationDetails").getAct()
		.getObservation().getValues().get(4).getINT().setValue(refillQuantity);
		
		// OK, now marshal the finished TRIM into XML and store in the document.
		marshalToDocument( trim, docXMLNew );
		try {
			creatorBean.submit(mdEvent, accountUser,privateKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// add this item to activity list
		creatorBean.addToWIP(mdEvent, trim, now, variables);
		return mdEvent;
	}
	
	/**
	* Submit the specified document for processing (remote-friendly). The document in this case
	* is already in the Tolven document store. We decrypt it and include in the message (where it 
	* is encrypted with MDBUser private key) so that
	* the message processor will be able to see the contents.
	* If attachments are involved, then we'll also include them in the message.
	*/
	public void submit( long documentId, AccountUser activeAccountUser, Date queueOnDate,PrivateKey privateKey) throws Exception {
		if (0==documentId) throw new IllegalArgumentException( "Submitted DocumentId must not be 0");
		logger.info( "Submit documentId=" + documentId );
		DocBase doc = documentBean.findDocument(documentId);
		TolvenMessageWithAttachments tm = new TolvenMessageWithAttachments();
		tm.setAccountId(doc.getAccount().getId());
		tm.setFromAccountId(doc.getAccount().getId());
		tm.setAuthorId(doc.getAuthor().getId());
		tm.setDocumentId(doc.getId() );
		tm.setMediaType( doc.getMediaType());
		if (doc instanceof DocXML ) {
			tm.setXmlNS( ((DocXML)doc).getXmlNS());
		}
		tm.setPayload(docProtectionBean.getDecryptedContent(doc, activeAccountUser,privateKey));
		// Now add attachments, if any.
		List<DocAttachment> attachments = documentBean.findAttachments(doc);
		for (DocAttachment attachment : attachments) {
			TolvenMessageAttachment tma = new TolvenMessageAttachment(); 
			DocBase attachedDoc = attachment.getAttachedDocument();
			tma.setMediaType(attachedDoc.getMediaType());
			tma.setDocumentId(attachedDoc.getId());
			if (attachedDoc instanceof DocXML) {
				tma.setXmlNS(((DocXML)attachedDoc).getXmlNS());
			}
			tma.setPayload(docProtectionBean.getDecryptedContent(attachedDoc, activeAccountUser,privateKey));
			tm.getAttachments().add(tma);
		}
		tmSchedulerBean.queueTolvenMessage(tm, queueOnDate);
	}
	
	/**
	 * Submit the document associated with this event
	 * @throws Exception 
	 */
	public void submit(MenuData mdEvent, AccountUser activeAccountUser,PrivateKey privateKey) throws Exception {
	    submit(mdEvent, activeAccountUser, null,privateKey);
    }

    /**
     * Submit the document associated with this event to be queued for processing on queueOnDate
     * @throws Exception 
     */
    public void submit(MenuData mdEvent, AccountUser activeAccountUser, Date queueOnDate,PrivateKey privateKey) throws Exception {
        if (0 == mdEvent.getDocumentId()) {
            throw new IllegalArgumentException("Submitted event, " + mdEvent.getId() + " must have a document id");
        }
        submit(mdEvent.getDocumentId(), activeAccountUser, queueOnDate,privateKey);
    }
    

	/**
	 * Marshal a Trim object graph to XML and store it in the document provided. The document will then be persisted.
	 */
	public void marshalToDocument( Trim trim, DocXML docXML ) throws JAXBException, TRIMException {
		ByteArrayOutputStream trimXML = new ByteArrayOutputStream() ;
		getTrimMarshaller().marshalTRIM(trim, trimXML);
        String kbeKeyAlgorithm = propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
        int kbeKeyLength = Integer.parseInt(propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
        docXML.setAsEncryptedContent(trimXML.toByteArray(), kbeKeyAlgorithm, kbeKeyLength);
		// Document remains in new status, but is saved (persist/merge)
        documentBean.saveDocument(docXML);
	}
	
	/**
	 * This function starts a timer to check whether a refill request to a prescriber
	 * has been processed
	 * @author Nevin
	 * @param Timer
	 * @throws JAXBException
	 * @throws IOException
	 * @throws NamingException
	 * added on 08/16/2010
	 */
	public TimerHandle startTimer(MenuData md, TrimEx refillRequestTrim) {
		String prescriberEmail = null;
		String to = null;
		String from = null;
		String messageId = null;
		ActRelationship prescriberRelation = ((ActEx) refillRequestTrim.getAct()).getRelationship().get("prescriber");
		List<ObservationValueSlot> listObsSlot = prescriberRelation.getAct().getObservation().getValues();
		for (Iterator<ObservationValueSlot> iterator = listObsSlot.iterator(); iterator.hasNext();) {
			ObservationValueSlot observationValueSlot = (ObservationValueSlot) iterator
					.next();
			if (observationValueSlot.getST()!=null && observationValueSlot.getLabel().getValue().equalsIgnoreCase("Email")) {
				prescriberEmail = observationValueSlot.getST().toString();
			}
		}
		List<ObservationValueSlot> headerOVS = ((ActEx) refillRequestTrim.getAct()).getRelationship().get("header").getAct().getObservation().getValues();
		from = headerOVS.get(0).getST().getValue().trim();
		to = headerOVS.get(1).getST().getValue().trim();
		messageId = headerOVS.get(2).getST().getValue().trim();
		md.setString08(prescriberEmail);
		md.setString04(to);
		md.setString05(from);
		md.setString06(messageId);
		if (headerOVS.get(6).getST().getValue().trim().length() > 0 ) {
			md.setString03(headerOVS.get(6).getST().getValue().trim());
		}
		
		Timer timer = null;
		timer = ejbContext.getTimerService().createTimer(43200000, md); //Actual Scenario.
//		timer = ejbContext.getTimerService().createTimer(60000, md); // 1 Minute test Scenario.
//		timer = ejbContext.getTimerService().createTimer(300000, md); // 5 Minute test Scenario.
		
	    TolvenLogger.info("Timer for 12 hours started. -Menudata Id : "+ md.getId(), TrimMessageBean.class);
	    return timer.getHandle();
    }
	/**
	 * This function is called when startTimer() method gets timed out
	 * @author Nevin
	 * @param Timer
	 * @throws JAXBException
	 * @throws IOException
	 * @throws NamingException
	 * added on 08/16/2010
	 */
    /*@Timeout
    public void timeout( Timer timer) throws JAXBException, IOException, NamingException{
    	LoginContext loginContext = getLoginContext();
        try {
            loginContext.login();
        } catch (LoginException ex) {
            throw new RuntimeException("Could not login into tolvenRuleQueue", ex);
        }
        
        //Check whether the refillRequest is not processed for 48 hrs.
    	MenuData mdTimer = (MenuData)timer.getInfo();
    	Date mdDate = mdTimer.getDate01();
		logger.info("MDat Date : "+ mdDate);
		Calendar trimCal = Calendar.getInstance();
		trimCal.setTime(mdDate);
		trimCal.add(Calendar.HOUR_OF_DAY, 48);	//Actual scenario.
//		trimCal.add(Calendar.HOUR_OF_DAY, 2);	//for convenience 2 Hours.
//		trimCal.add(Calendar.MILLISECOND, 1200000); // 20 min Test.
//		trimCal.add(Calendar.MILLISECOND, 240000); //4 min Test.
		Date trimPassedHourDate = trimCal.getTime();
		logger.info("48hr Date : "+ trimPassedHourDate);
		
		Calendar newCal = Calendar.getInstance();
		Date currentDate = newCal.getTime();
		logger.info("Curr date : " +currentDate);
		
		if (trimPassedHourDate.after(currentDate)) {
			String prescriberEmail = mdTimer.getString08();
	    	String qs = null;
			Query query = null;
			Boolean deleted = null;
			qs = String.format(Locale.US, "SELECT md.deleted FROM MenuData md WHERE md.id = :p");
			query = em.createQuery( qs );
			query.setParameter( "p", mdTimer.getId() );
			deleted =  ((Boolean)query.getResultList().get(0));
			boolean submitted = epBean.checkForSubmittedData(mdTimer);
			if ((deleted!=null && deleted == true) || submitted) {
				TolvenLogger.info( "Refill request was processed - "+mdTimer.getId(), this.getClass());
			}else {
				try{
					sendReminderEmail(prescriberEmail);
				}catch(Exception e){
					e.printStackTrace();
					TolvenLogger.error("Failed to send e-mail to : "+ prescriberEmail, this.getClass());
				}
				sleep(43200000, mdTimer); //Timer for 12 hour (Actual scenario).
//				sleep(1800000, mdTimer);	//Test scenario 30 Minute.
//				sleep(300000, mdTimer);	//Test scenario 5 Minute.
//				sleep(60000, mdTimer);	//Test scenario 1 Minute.
			}
		}else {
			if (mdTimer.getStatus()==Status.NEW) {
				menuBean.removeReferencingMenuData( mdTimer.getAccount().getId(), mdTimer.getDocumentId(), true);
			}
			TolvenLogger.info("Removed the RefillRequest and response send automatically. ", this.getClass());
			SureScriptsCommunicator sureComm = new SureScriptsCommunicator();
			sureComm.createRefillRequestErrorMessage(mdTimer);
			
		}
		timer.cancel();
		TolvenLogger.info("Timer Cancelled Successfully", this.getClass());
		try {
            loginContext.logout();
        } catch (LoginException ex) {
            throw new RuntimeException("Could not logout of tolvenRuleQueue", ex);
        }
    }*/
    
    private void sleep(long interval,MenuData mdTimer) {
        ejbContext.getTimerService().createTimer(interval, mdTimer);
    }
    
    /*private LoginContext getLoginContext() {
        try {
            
             * tolvenRuleQueue is the LoginModule, which defines alias and password, that can be used
             * to authenticate and obtain required mdbuser keys in LDAP.
             
            final Subject aliasSubject = TolvenAlias.getAliasSubject("tolvenRuleQueue");
            final String mdbuserPrincipalName = TolvenAlias.getTolvenUserName(aliasSubject);
            if (mdbuserPrincipalName == null) {
                throw new RuntimeException("Could not find mdbuser principal name for security domain tolvenRuleQueue");
            }
            final char[] mdbUserPassword = TolvenAlias.getTolvenAliasPassword(aliasSubject);
            if (mdbUserPassword == null) {
                throw new RuntimeException("Could not find mdbuser password for'" + mdbuserPrincipalName + "' in security domain tolvenRuleQueue");
            }
            LoginContext loginContext = new LoginContext("tolvenLDAP", new CallbackHandler() {
                public void handle(Callback[] callbacks) {
                    int len = callbacks.length;
                    Callback cb;
                    for (int i = 0; i < len; i++) {
                        cb = callbacks[i];
                        if (cb instanceof NameCallback) {
                            NameCallback ncb = (NameCallback) cb;
                            ncb.setName(mdbuserPrincipalName);
                        } else if (cb instanceof PasswordCallback) {
                            PasswordCallback pcb = (PasswordCallback) cb;
                            pcb.setPassword(mdbUserPassword);
                        }
                    }
                }
            });
            return loginContext;
        } catch (Exception ex) {
            throw new RuntimeException("Could not get a LoginContext for tolvenRuleQueue", ex);
        }
    }
    */
    /**
	 * This function sends a reminder mail to the prescriber if a refill request is not processed in time 
	 * @author Nevin
	 * @param String
	 * added on 08/16/2010
	 */
    /*
    private void sendReminderEmail(String prescriberEmail) { 
    	try {
			ExpressionEvaluator ee = new ExpressionEvaluator();
			//ee.addVariable("account", this.getAccountUser().getUser().getId());
			ee.addVariable("to", prescriberEmail);
			ee.addVariable("subject", "Sub: You have an unprocessed refill request");
			ee.addVariable("body", "Please process the Refill request send to you");
			ee.addVariable("now", getNow());
			mailBean.sendMessage(ee);
			TolvenLogger.info("Reminder E-mail send Successfully to : "+ prescriberEmail, this.getClass());
		} catch (Exception e) {
			TolvenLogger.info(e.getMessage(), this.getClass());
			e.printStackTrace();
		}
    }
    */

    private TrimMarshaller getTrimMarshaller() {
        if(trimMarshaller == null) {
            trimMarshaller = new TrimMarshaller();
        }
        return trimMarshaller;
    }
    
}
