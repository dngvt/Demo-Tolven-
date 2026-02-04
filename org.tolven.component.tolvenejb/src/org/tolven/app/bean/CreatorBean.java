package org.tolven.app.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.tolven.app.CreatorLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.TrimLocal;
import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuStructure;
import org.tolven.app.entity.PlaceholderTransition;
import org.tolven.app.entity.TrimHeader;
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
import org.tolven.doctype.DocumentType;
import org.tolven.msg.AccountProcessingProtectionLocal;
import org.tolven.msg.TolvenMessageSchedulerLocal;
import org.tolven.security.key.DocumentSecretKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.trim.ActStatus;
import org.tolven.trim.Application;
import org.tolven.trim.BindPhase;
import org.tolven.trim.Transition;
import org.tolven.trim.Transitions;
import org.tolven.trim.Trim;
import org.tolven.trim.TrimMarshaller;
import org.tolven.trim.ex.TRIMException;
import org.tolven.trim.ex.TolvenIdEx;
import org.tolven.trim.ex.TrimEx;
import org.tolven.trim.scan.ComputeScanner;
import org.tolven.trim.scan.CreateScanner;

/**
 * The Creator class is a factory for creating instances of placeholders and events.
 * @author John Churin
 *
 */
@Stateless
@Local(CreatorLocal.class)
public class CreatorBean implements CreatorLocal {
	@PersistenceContext
	private EntityManager em;
	@EJB MenuLocal menuBean;
	@EJB TrimLocal trimBean;
	@EJB DocumentLocal documentBean;
    @EJB DocProtectionLocal docProtectionBean;
    @EJB AccountProcessingProtectionLocal accountProcessingProctectionLocal;
    @EJB TolvenPropertiesLocal propertyBean;
    @EJB TolvenMessageSchedulerLocal tmSchedulerBean;
	
    @Resource EJBContext ejbContext;
    
	Logger logger = Logger.getLogger(this.getClass());

	static final String TRIM_NS = "urn:tolven-org:trim:4.0";
    private static TrimMarshaller trimMarshaller;
	
	public CreatorBean( ) {
	}
	
	/**
	 * An Event is created for each update of a placeholder, including the first instance.
	 *  The Event MenuData entry represents the trim message/document.
	 * @param account
	 * @param The placeholder
	 * @param now
	 * @return The new event
	 */
	public MenuData createEvent( Account account, String instancePath, Trim trim, Date now, Map<String, Object> variables ) {
		MenuData mdEvent;
		try {
			mdEvent = new MenuData();
			MenuStructure ms = menuBean.findMenuStructure(account.getId(), instancePath);
			mdEvent.setMenuStructure(ms.getAccountMenuStructure());
			mdEvent.setAccount(account);
			menuBean.populateMenuData( variables, mdEvent);
			mdEvent.setStatus( Status.NEW );
			menuBean.persistMenuData(mdEvent);
		} catch (RuntimeException e) {
			throw new RuntimeException( "Error creating event for instance path: " + instancePath, e);
		}
		return mdEvent;
	}
	
	public void addToWIP( MenuData mdEvent, TrimEx trim, Date now, Map<String, Object> variables ) {
		// See if we're asked to add this to WIP, do so.
		String wip = getWIPPath( trim, mdEvent.getAccount().getAccountType().getKnownType() );
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
			menuBean.populateMenuData(variables, mdToDo);
			menuBean.persistMenuData(mdToDo);
		}

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
	
	public String getInstancePath( Trim trim, String knownType ) {
		Application application = getApplication( trim, knownType );
		if (application!=null) {
			return application.getInstance();
		}
		return null;
	}

	public String getWIPPath( TrimEx trim, String knownType) {
		Application application = getApplication( trim, knownType );
		if (application!=null) {
			return application.getWip();
		}
		return null;
	}
	
	public boolean isSignatureRequired( TrimEx trim, String knownType ) {
		Application application = getApplication( trim, knownType );
		if (application != null) {
			return application.isSignatureRequired();
		}
		return false;
	}

	public TolvenIdEx createTolvenId( MenuData md, String context, Date now, String status, String principal ) {
		TolvenIdEx tolvenId = new TolvenIdEx();
		tolvenId.setAccountId(Long.toString(md.getAccount().getId()));
		tolvenId.setContextPath(context);
		tolvenId.setApplication(md.getAccount().getAccountType().getKnownType());
		tolvenId.setPath(md.getPath());
		tolvenId.setId(Long.toString(md.getId()));
		tolvenId.setDate( now );
		tolvenId.setStatus( status );
		tolvenId.setPrincipal( principal );
		return tolvenId;
	}

	public TolvenIdEx createTolvenId( TolvenIdEx original ) {
		TolvenIdEx tolvenId = new TolvenIdEx();
		tolvenId.setAccountId(original.getAccountId());
		tolvenId.setApplication(original.getApplication());
		tolvenId.setContextPath(original.getContextPath());
		tolvenId.setId(original.getId());
		tolvenId.setPath(original.getPath() );
		tolvenId.setDate(original.getDate() );
		tolvenId.setStatus( original.getStatus() );
		tolvenId.setPrincipal( original.getPrincipal() );
		return tolvenId;
	}
	
	public Transition findTransition(Transitions transitions, String transitionName) {
		for (Transition transition : transitions.getTransitions()) {
			if (transition.getName().equals(transitionName)) {
				return transition;
			}
		}
		return null;
	}
	
	public Transition findInitialTransition(Transitions transitions ) {
		for (Transition transition : transitions.getTransitions()) {
			if (transition.getFrom()==null) {
				return transition;
			}
		}
		return null;
	}
	
	/**
	 * The state (status) of this "document" is transitioned based on the supplied transition, if any.
	 * This method has a new purpose and a backward-compatibility responsibility.
	 * If the path is specified and it points to an act, entity, or role that has no transition of it's own,
	 * the transitions table are copied to that node - as if it had been there all along.
	 * In any case, the trim-level transitions now controls the document as a whole (not an individual act/entity/role).
	 * @param trim
	 * @param transitionName If null, set initial state. If non-null, transition to the named transition.
	 * @return Status resulting from the transition
	 */
	public StatusInfo calculateTransition( Trim trim, String transitionName ) {
		Transitions transitions = trim.getTransitions();
		// If no transitions, then nothing to do
		if (transitions==null) return null;
//		if (transitions.getPath()==null) {
//			throw new RuntimeException( "Missing path in transitions for Trim " + trim.getName());
//		}
		// Use expresssionEvaluator to get/set state as specified in path
		TrimExpressionEvaluator evaluator = new TrimExpressionEvaluator();
		evaluator.addVariable("trim", trim);
//		Object state = evaluator.evaluate(transitions.getPath());
		Transition transition = null;
		if (transitionName==null) {
			// look for the initial transition
			transition = findInitialTransition( transitions );
		} else {
			// look for the named transition
			transition = findTransition( transitions, transitionName);
		}
		
		StatusInfo statusInfo = null;
		
		//String toStatusAndReason = null;
		if (transition!=null) {
			trim.setTransition( transition.getName() );
			// Set the value in the trim
			//toStatusAndReason = transition.getTo();
			statusInfo = new StatusInfo(transition.getTo());
		}

		TransitionPath transitionPath = new TransitionPath(transitions.getPath());
		if (transitionPath != null && statusInfo != null) {
			//set status if not null
			if (transitionPath.getStatusCodePath() != null) {
				evaluator.setValue(transitionPath.getStatusCodePath(), statusInfo.getStatus());
			}
			//set status reason if not null
			if (transitionPath.getStatusReasonCodePath() != null) {
				evaluator.setValue(transitionPath.getStatusReasonCodePath(), statusInfo.getStatusReason());
			}
			
		}
		
		/*
		if (transitions.getPath() != null) {
			String[] codePaths = transitions.getPath().split(" ");
			for (String p : codePaths) {
				String path = "#{" + p + "}";
				evaluator.setValue(path, newTransitionValue[count]);
			
		}
		
		if(toStatusAndReason != null) {
			String[] newTransitionValue = toStatusAndReason.split(" ");
			if (newTransitionValue.length > 0) {
				statusInfo.setStatus(newTransitionValue[0]);
			}
			if (transitions.getPath() != null) {
				String[] codePaths = transitions.getPath().split(" ");
				int count = 0;				
				for (String p : codePaths) {
					String path = "#{" + p + "}";
					evaluator.setValue(path, newTransitionValue[count]);
					if (count > 0) {
						statusInfo.setStatusReason(newTransitionValue[count]);
					}
					count++;
				}
				return statusInfo;
			}
		}*/
		return statusInfo;
	/*
		if(toStatusReason != null && transitions.getPath() != null) {
			String[] codePaths = transitions.getPath().split(" ");
			String[] result = new String[codePaths.length];
			int count = 0;
			for ( String p : codePaths ){			
				String path = "#{" + p + "}";
				if(newTransitionValue.length > count) {
					evaluator.setValue(path, newTransitionValue[count]);
					result[count] = newTransitionValue[count];
				}
				count++;			
			}
			return result;
		}
		String[] result1 = new String[1];
		result1[0] = toStatusReason;
		return result1;*/
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
	
	public void computeScan( Trim trim, AccountUser accountUser, MenuPath contextPath, Date now, DocumentType documentType ) {
		// Scan for binding requirements in TRIM - request-phase
		computeScanWithResults(trim, accountUser, contextPath, now, documentType);
		
	}

	public List<String> computeScanWithResults( Trim trim, AccountUser accountUser, MenuPath contextPath, Date now, DocumentType documentType) {
		
		// Scan for binding requirements in TRIM - request-phase
		List<MenuPath> contextList = new ArrayList<MenuPath>();
		contextList.add(contextPath);

		ComputeScanner scanner = new ComputeScanner();
		scanner.setTrim(trim);
//		scanner.setMenuDataSource(md);
		scanner.setPhase(BindPhase.CREATE);
		scanner.setAccountUser(accountUser);
		scanner.setKnownType(accountUser.getAccount().getAccountType().getKnownType());
		scanner.setMenuBean(menuBean);
		scanner.setTrimBean(trimBean);
		scanner.setDocumentBean(documentBean);
		scanner.setDocProtectionBean(docProtectionBean);
		scanner.setPropertyBean(propertyBean);
		scanner.setMenuContext(contextList);
		scanner.setNow(now);
		scanner.setDocumentType( documentType );
		scanner.scan( );
		
		return scanner.getValidationMessages();
	}

	/**
	 * Perform create scan on this trim looking for unsatisfied placeholder bind requests.
	 * @param account
	 * @param trim
	 * @param mdTrim - The "trimList" menuData item where this trim came from, if any. (This is largely obsolete and normally should be null.) 
	 * @param contextList - A list of context elements which can be used to establish certain placeholders. For example, echr:patient-123:diagnosis is
	 * enough to determine that a new problem trim is to be associated with patient-123.
	 * @param now - Transaction Time
	 * @param bindPhase - Which phase 
	 * @return A list of all placeholders with applicable binding instructions
	 */
	public List<MenuData> placeholderBindScan( Account account, TrimEx trim, MenuData mdTrim, MenuPath context, Date now, BindPhase bindPhase, DocBase doc, TrimEx originalTrim ) {
		String knownType = account.getAccountType().getKnownType();
		// Scan for placeholder binding requests in TRIM
		CreateScanner scanner = new CreateScanner();
		scanner.setTrim(trim);
		scanner.setDocumentId(doc.getId());
		scanner.setPhase(bindPhase);
		scanner.setMenuDataSource(mdTrim);
		scanner.setAccount(account);
		scanner.setKnownType(knownType);
		scanner.setMenuBean(menuBean);
		scanner.setTrimBean(trimBean);
		scanner.setOriginalTrim(originalTrim);
		scanner.addMenuContext(context);
		scanner.setNow(now);
		scanner.scan( );
		return scanner.getPlaceholders();
	}
	
	/**
	 * Perform create scan on this trim looking for unsatisfied placeholder bind requests.
	 * @param accountUser
	 * @param trim
	 * @param mdTrim - The "trimList" menuData item where this trim came from, if any. (This is largely obsolete and normally should be null.) 
	 * @param contextList - A list of context elements which can be used to establish certain placeholders. For example, echr:patient-123:diagnosis is
	 * enough to determine that a new problem trim is to be associated with patient-123.
	 * @param now - Transaction Time
	 * @param bindPhase - Which phase 
	 * @return A list of all placeholders with applicable binding instructions
	 */
	public List<MenuData> placeholderBindScan( AccountUser accountUser, TrimEx trim, MenuData mdTrim, MenuPath context, Date now, BindPhase bindPhase, DocBase doc ) {
		String knownType = accountUser.getAccount().getAccountType().getKnownType();
		// Scan for placeholder binding requests in TRIM
		CreateScanner scanner = new CreateScanner();
		scanner.setTrim(trim);
		scanner.setDocumentId(doc.getId());
		scanner.setPhase(bindPhase);
		scanner.setMenuDataSource(mdTrim);
		scanner.setAccount(accountUser.getAccount());
		scanner.setKnownType(knownType);
		scanner.setMenuBean(menuBean);
		scanner.setTrimBean(trimBean);
		scanner.addMenuContext(context);
		{
			String assignedPath = accountUser.getProperty().get("assignedAccountUser");
			if (assignedPath!=null) {
				MenuData assigned = menuBean.findMenuDataItem(accountUser.getAccount().getId(), assignedPath);
				scanner.addContextVariables("assignedAccountUser", assigned.getId() );
			}
		}
		scanner.setNow(now);
		scanner.scan( );
		return scanner.getPlaceholders();
	}
	
	/**
	 * Create an event entry
	 * that represents this transaction in the database. If the trim does not indicate
	 * that an event is needed (specified as instance path in application element), then we return null.
	 * @param account
	 * @param trim
	 * @param mdTrim - The "trimList" menuData item where this trim came from, if any. (This is largely obsolete and normally should be null.) 
	 * @param contextList - A list of context elements which can be used to establish certain placeholders. For example, echr:patient-123:diagnosis is
	 * enough to determine that a new problem trim is to be associated with patient-123.
	 * @param now - Transaction Time
	 * @return New event or null
	 */
	public MenuData establishEvent( Account account, TrimEx trim, Date now, Map<String, Object> variables ) {
		String knownType = account.getAccountType().getKnownType();
		
		// Setup this transition (initial state)
		
		try {
			StatusInfo sInfo = new StatusInfo(trim.getStatus());
		/*	if (status == null) {			
				String[] transitionStatus = calculateTransition(trim, null);
				
				if (transitionStatus != null) {
					StringBuffer statusReasonSB = new StringBuffer();
					for (String p : transitionStatus) {
						if (statusReasonSB.length() > 0) {
							statusReasonSB.append(" ");
						}
						statusReasonSB.append(p);			
					}
					statusReasonStr = statusReasonSB.toString();
				} 
			} else {*/
				//statusReasonStr = status;
			//}
			
	//JMC		mdPlaceholder.setActStatus(actStatus);
	
			// InstancePath tells us where to instantiate this new event
			String instancePath = getInstancePath(trim, knownType);
			if (instancePath==null) return null;
	//		if (instancePath==null) throw new IllegalStateException( "No instancePath found for accountType " + knownType + " in trim " + trim.getName());
			// Create an event to hold this transaction
			MenuData mdEvent = createEvent( account, instancePath, trim, now, variables );
			mdEvent.setActStatus(sInfo.getStatus()); //status
			// Reference the event in the Trim (add to event history)
			trim.addTolvenEventId(createTolvenId( mdEvent, null, now, sInfo.getStatusAndReasonStr(), (String)TolvenSessionWrapperFactory.getInstance().getPrincipal() )); //status
			
			return mdEvent;
		} catch (Exception e) {
			throw new RuntimeException("Error caught in CreatorBean:establishEvent", e);
		}
	}
		
/*	
	public MenuData createTRIMPlaceholder( AccountUser accountUser, String trimPath, String context, Date now, String alias) throws JAXBException, TRIMException {
		return createTRIMPlaceholder(accountUser, trimPath, context, now, alias,null);
	}
*/
	/**
	 * Instantiate a new document and new event pointing to it. The event also points to the eventual placeholder.
	 * @param accountUser
	 * @param trimPath The trimName The key to the MenuStructure item containing the template XML. 
	 * @param context
	 * @param now
	 * @param alias - when binding trim, use this alias for the context object.
	 * @param transition - transition to be set for the placeholders
	 * @return MenuData - containing the event, not the placeholder
	 * @throws JAXBException
	 * @throws TRIMException
	 */
	public MenuData createTRIMPlaceholder( AccountUser accountUser, String trimPath, String context, Date now, String alias, String transition ) throws JAXBException, TRIMException {
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
           //trim = trimBean.parseTrim(trimHeader.getTrim(), accountUser, context, now, alias );
	       //CCHIT merge	
            String trimStr=new String (trimHeader.getTrim());
            trimStr = trimStr.replace("&", "&amp;");
            byte[] btrim=trimStr.getBytes();
            trim = trimBean.parseTrim(btrim, accountUser, context, now, alias );
	    } catch (RuntimeException e) {
			throw new RuntimeException( "Error parsing TRIM '" + trimHeader.getName() + "'", e );
		}
		// Setup variables we'll need for populate evaluations
		
		MenuPath contextPath = new MenuPath(context);
		Map<String, Object> variables = new HashMap<String, Object>(10);
		variables.putAll(contextPath.getNodeValues());
		{
			String assignedPath = accountUser.getProperty().get("assignedAccountUser");
			if (assignedPath!=null) {
				MenuData assigned = menuBean.findMenuDataItem(accountUser.getAccount().getId(), assignedPath);
				variables.put("assignedAccountUser", assigned);
			}
		}
		variables.put("trim", trim);
		
		// Create an event to hold this trim document
		DocXML docXML =(DocXML) documentBean.createNewDocument("text/xml", TRIM_NS, accountUser);
		docXML.setSignatureRequired( isSignatureRequired(trim, accountUser.getAccount().getAccountType().getKnownType() ) );
		logger.info( "Document (placeholder) created, id: " + docXML.getId());
		// Call computes for the first time now
		computeScan( trim, accountUser, contextPath, now, docXML.getDocumentType());
		// Bind to placeholders
		List<MenuData> palceholders = placeholderBindScan( accountUser.getAccount(), trim, mdTrim, contextPath, now, BindPhase.CREATE, docXML, null);
		
		//changes for placeholder transition
		StatusInfo status = null;
		if(transition != null && !transition.equals("")) {
			status = calculateTransition(trim, transition);
			if(status == null){
				throw new RuntimeException("Invalid transition found for trim:"+trimPath+" transtion:"+transition);
			}
		}
		if(status != null){
			for(MenuData md:palceholders){
				//Set the actStatus of the placeholder passed in the context. Not all the placeholders found in the document
				if(context.equals(md.getPath())){					
					if(logger.isDebugEnabled()){
						logger.debug("setting the status of "+md.getPath()+" to "+status.getStatus());
					}
					md.setActStatus(status.getStatus());
				}
			}
		}
		// Create an event
		MenuData mdEvent = establishEvent( accountUser.getAccount(), trim, now, variables);
		if (mdEvent==null) {
			throw new RuntimeException( "Unable to create instance of event for " + trim.getName());
		}
		mdEvent.setDocumentId(docXML.getId());
		// Marshal the finished TRIM into XML and store in the document.
		marshalToDocument( trim, docXML );
		
		// Make sure this item shows up on the activity list
		addToWIP(mdEvent, trim, now, variables );
		
		return mdEvent;
		
	}
	
	/**
	 * Instantiate a new document and new event pointing to it. The event also points to the eventual placeholder.
	 * @param accountUser
	 * @param trimPath The trimName The key to the MenuStructure item containing the template XML. 
	 * @param context
	 * @param now
	 * @param alias - when binding trim, use this alias for the context object.
	 * @return MenuData - containing the event, not the placeholder
	 * @throws JAXBException
	 * @throws TRIMException
	 */
	public MenuData createTRIMPlaceholder( AccountUser accountUser, String trimPath, String context, Date now, String alias ) throws JAXBException, TRIMException {
		return createTRIMPlaceholder(accountUser, trimPath, context, now, alias,null);
	}
	
	/**
	 * An existing placeholder is transitioned (updated). The placeholder document is immutable. So we create
	 * a new document.
	 * We won't actually change the placeholder until the change is submitted.
	 */
	public MenuData createTRIMEvent( MenuData mdPlaceholder, AccountUser accountUser, String transitionName, Date now, PrivateKey userPrivateKey ) throws JAXBException, TRIMException {
		if (mdPlaceholder==null) {
			throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "]Missing placeholder for transition");
		}
		String knownType = accountUser.getAccount().getAccountType().getKnownType();
		DocXML docXMLFrom = (DocXML) documentBean.findDocument(mdPlaceholder.getDocumentId());
		// Note: We're reading this in from an immutable document 
		// but will be creating a new document with the modified trim, not
		// modifying the original document.
		TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(docXMLFrom, accountUser, userPrivateKey);
		// We reverse the normal, that is, use the trim to populate the event
		DocXML docXMLNew = documentBean.createXMLDocument( TRIM_NS, accountUser.getUser().getId(), accountUser.getAccount().getId() );
		docXMLNew.setSignatureRequired(isSignatureRequired( trim, knownType ));
		// Carry forward attachments
		documentBean.copyAttachments(docXMLFrom, docXMLNew );
        logger.info( "Document (event) created, id: " + docXMLNew.getId() );
		
		// Adjust status based on selected transition
		//String status = calculateTransition( trim, transitionName);
        // Now we track status and reason for selected transition
        
        StatusInfo statusInfo = calculateTransition( trim, transitionName);
	
		
		// If we have not stored a tolvenId for placeholder yet, do it now.
		// This can happen when the placeholder started from an external message
		if (trim.getTolvenId(accountUser.getAccount().getId())!=null) {
			trim.addTolvenId(createTolvenId( mdPlaceholder, null, now, statusInfo.getStatusAndReasonStr(), accountUser.getUser().getLdapUID() ));
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
		String instancePath = getInstancePath(trim, knownType);
		// Create an event to hold this transaction
		MenuData mdEvent = createEvent( accountUser.getAccount(), instancePath, trim, now, variables);
		mdEvent.setDocumentId(docXMLNew.getId());
		mdEvent.setActStatus(statusInfo.getStatus());
		if (statusInfo.getStatusReason() != null) {
			mdEvent.setActStatusReason(statusInfo.getStatusReason());
		}
		
		// Reference the event in the Trim (add to event history)
		trim.addTolvenEventId(createTolvenId( mdEvent, null, now, statusInfo.getStatusAndReasonStr(), accountUser.getUser().getLdapUID() ));

		// OK, now marshal the finished TRIM into XML and store in the document.
		marshalToDocument( trim, docXMLNew );
		
		// add this item to activity list
		addToWIP(mdEvent, trim, now, variables);

		return mdEvent;
	}
	
	private TrimEx saveOriginalDocument(MenuData mdPlaceholder) {
		long originalDocId = mdPlaceholder.getDocumentId();
		DocXML doc = (DocXML) documentBean.findDocument(originalDocId);
		
		String keyAlgorithm = propertyBean.getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
		TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
		PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);
		
		byte[] bytes = docProtectionBean.getDecryptedContent(doc, TolvenRequest.getInstance().getAccountUser(), userPrivateKey);
		
			try {
			TrimEx sourceTrim = (TrimEx) getTrimMarshaller().unmarshal(doc.getXmlNS(), new ByteArrayInputStream(bytes));
			return sourceTrim;
		} catch (JAXBException e) {
			throw new RuntimeException("Unable to unmarshal sourceTrim in CreateScanner:populateBindMaps" + e);
		}
	}
	
	public MenuData createPHEvent( MenuData mdPlaceholder, String transitionName, PrivateKey userPrivateKey, String context ) throws JAXBException, TRIMException {
		if (mdPlaceholder==null) {
			throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "]Missing placeholder for transition");
		}
		AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
		
		TrimEx originalTrim = saveOriginalDocument(mdPlaceholder);
		
		//Get the transition from the name
		Query query = em.createQuery("SELECT pt FROM PlaceholderTransition pt WHERE pt.path = :path AND pt.code = :code AND pt.transitionName = :transitionName");
		String ptPath = findPlaceholderPath(mdPlaceholder);
		query.setParameter("path", ptPath);
		String ptCode = mdPlaceholder.getCode01Code();
		query.setParameter("code", ptCode); //TODO: This may change as well 
		query.setParameter("transitionName", transitionName);
		List<PlaceholderTransition> items = query.getResultList();
		
		PlaceholderTransition pt = null;
		if(items.size() > 1) {
			throw new RuntimeException("More than one Placeholder Transition was found for path, code, and transitionName: " + ptPath + ", " + ptCode + ", " + transitionName);
		} else if (items.size() > 0) {
			pt = items.get(0);			
		} else {
			throw new RuntimeException("No Placeholder Transition was found for path, code, and transitionName: " + ptPath + ", " + ptCode + ", " + transitionName);
		}
		
		//Instantiate new trim from placeholder transition trim header linkage - with context
		TrimHeader th = pt.getTrimHeader();
		TrimEx trim = null;
		try {
             String trimStr=new String (th.getTrim());
            trimStr = trimStr.replace("&", "&amp;");
            byte[] btrim=trimStr.getBytes();
            trim = trimBean.parseTrim(btrim, accountUser, context, TolvenRequest.getInstance().getNow(), null);
	    } catch (RuntimeException e) {
			throw new RuntimeException( "Error parsing TRIM '" + th.getName() + "'", e );
		}
		
		String knownType = accountUser.getAccount().getAccountType().getKnownType();
		////DocXML docXMLFrom = (DocXML) documentBean.findDocument(mdPlaceholder.getDocumentId());
		// Note: We're reading this in from an immutable document 
		// but will be creating a new document with the modified trim, not
		// modifying the original document.
		////TrimEx trim = (TrimEx) xmlProtectedBean.unmarshal(docXMLFrom, accountUser, userPrivateKey);
		// We reverse the normal, that is, use the trim to populate the event
		
		//Based on the 'new' trim
		DocXML docXMLNew = documentBean.createXMLDocument( TRIM_NS, accountUser.getUser().getId(), accountUser.getAccount().getId() );
		docXMLNew.setSignatureRequired(isSignatureRequired( trim, knownType ));
		//No longer Carry forward attachments
		////documentBean.copyAttachments(docXMLFrom, docXMLNew );
        logger.info( "Document (event) created, id: " + docXMLNew.getId() );
		
		// Adjust status based on selected transition
		//String status = calculateTransition( trim, transitionName);
        // Now we track status and reason for selected transition
        StatusInfo statusInfo = calculateTransition( trim, transitionName);
		/*StringBuffer statusReasonSB = new StringBuffer();
		for (String p : statusReason) {
			if(p != null) {
				statusReasonSB.append(p);
			}
		}*/
		// If we have not stored a tolvenId for placeholder yet, do it now.
		// This can happen when the placeholder started from an external message
		Date now = TolvenRequest.getInstance().getNow();
		if (trim.getTolvenId(accountUser.getAccount().getId())!=null) {
			trim.addTolvenId(createTolvenId( mdPlaceholder, null, now, statusInfo.getStatusAndReasonStr(), accountUser.getUser().getLdapUID() ));
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
		String instancePath = getInstancePath(trim, knownType);
		
		// Bind to placeholders
		placeholderBindScan( accountUser.getAccount(), trim, mdPlaceholder, contextPath, now, BindPhase.CREATE, docXMLNew, originalTrim);
				
		
		// Create an event to hold this transaction
		MenuData mdEvent = createEvent( accountUser.getAccount(), instancePath, trim, now, variables);
		mdEvent.setDocumentId(docXMLNew.getId());
		mdEvent.setActStatus(statusInfo.getStatus());
		if (statusInfo.getStatusReason() !=  null) {
			mdEvent.setActStatusReason(statusInfo.getStatusReason());
		}
		
		// Reference the event in the Trim (add to event history)
		trim.addTolvenEventId(createTolvenId( mdEvent, null, now, statusInfo.getStatusAndReasonStr(), accountUser.getUser().getLdapUID() ));

		// OK, now marshal the finished TRIM into XML and store in the document.
		marshalToDocument( trim, docXMLNew );
		
		// add this item to activity list
		addToWIP(mdEvent, trim, now, variables);

		return mdEvent;
	}
	
	public String findPlaceholderPath(MenuData mdPlaceholder) {
		AccountMenuStructure ms = mdPlaceholder.getMenuStructure().getAccountMenuStructure();		
		return ms.getPath();
	}
	

	public List<PlaceholderTransition> findTransitions(MenuData mdPlaceholder) {
		//TODO:Restrict transitions based on authorization
		String code = mdPlaceholder.getCode01Code();
		String path = findPlaceholderPath(mdPlaceholder);
		StatusInfo sInfo = new StatusInfo(mdPlaceholder.getActStatus(), mdPlaceholder.getActStatusReason());
		/*String fromStatus = mdPlaceholder.getActStatus();	
		String actStatusReason = mdPlaceholder.getActStatusReason();		
		if(actStatusReason != null && !actStatusReason.equalsIgnoreCase("")) {
			fromStatus.concat(" ").concat(actStatusReason);
		}*/
		
		Query query = em.createQuery("SELECT pt FROM PlaceholderTransition pt WHERE pt.path = :path AND pt.code = :code AND pt.fromStatus = :fromStatus");
		query.setParameter("path", path);
		query.setParameter("code", code);
		query.setParameter("fromStatus", sInfo.getStatusAndReasonStr());
		List<PlaceholderTransition> items = query.getResultList();		
		return items;
		
	}
	
	public List<TrimHeader> findTRIM(MenuData mdPlaceholder) {
		String path = mdPlaceholder.getMenuStructure().getPath();
		String code = mdPlaceholder.getCode01Code();
		Query query = em.createQuery("SELECT th FROM TrimHeader th WHERE th.path = :path and td.code = :code");
		query.setParameter("path", path);
		query.setParameter("code", code);
		List<TrimHeader> items = query.getResultList();
		/*Find the list of trims that are suitable for editing this placeholder*/
		
		//ex: Move patient (update location or transfer) or discharge patient, two trims with two sets of transitions which may or may not match
		//return <List>Transitions
		
		return items;
	}
	
/*	public void updateTRIMPlaceholder(String placeholderPath) {
		MenuData md = menuBean.findMenuDataItem(TolvenRequest.getInstance().getAccount().getId(), placeholderPath);
		try {
			updateTRIMPlaceholder(md);
		} catch (Exception e) {
			throw new RuntimeException("Exception updatingTRIMPlaceholder in CreatorBean", e);
		}
	}*/
	
	/**
	 * Instantiate a new document and new event pointing to it. The event also points to the eventual placeholder.
	 * @param accountUser
	 * @param trimPath The trimName The key to the MenuStructure item containing the template XML. 
	 * @param context - this placeholder in focus
	 * @param now
	 * @param alias - when binding trim, use this alias for the context object.
	 * @return MenuData - containing the event, not the placeholder
	 * @throws JAXBException
	 * @throws TRIMException
	 */
	/*public void updateTRIMPlaceholder( MenuData mdPlaceholder) throws JAXBException, TRIMException {
		//1. Figure out which TRIM to instantiate
		//a. what's the name of trim that we're going to use to update this placeholder?
		
		//b. look it up
		
		
		
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
                    //trim = trimBean.parseTrim(trimHeader.getTrim(), accountUser, context, now, alias );
	       //CCHIT merge	
            String trimStr=new String (trimHeader.getTrim());
            trimStr = trimStr.replace("&", "&amp;");
            byte[] btrim=trimStr.getBytes();
            trim = trimBean.parseTrim(btrim, accountUser, context, now, alias );
	    } catch (RuntimeException e) {
			throw new RuntimeException( "Error parsing TRIM '" + trimHeader.getName() + "'", e );
		}
		// Setup variables we'll need for populate evaluations
		
		MenuPath contextPath = new MenuPath(context);
		Map<String, Object> variables = new HashMap<String, Object>(10);
		variables.putAll(contextPath.getNodeValues());
		{
			String assignedPath = accountUser.getProperty().get("assignedAccountUser");
			if (assignedPath!=null) {
				MenuData assigned = menuBean.findMenuDataItem(accountUser.getAccount().getId(), assignedPath);
				variables.put("assignedAccountUser", assigned);
			}
		}
		variables.put("trim", trim);
		if(!actStatus.equals("")) {
			trim.getAct().setStatusCode(ActStatus.fromValue(actStatus));
			actStatus = "";
		}

		// Create an event to hold this trim document
		DocXML docXML = documentBean.createXMLDocument( TRIM_NS, accountUser.getUser().getId(), accountUser.getAccount().getId() );
		docXML.setSignatureRequired( isSignatureRequired(trim, accountUser.getAccount().getAccountType().getKnownType() ) );
		logger.info( "Document (placeholder) created, id: " + docXML.getId());
		// Call computes for the first time now
		computeScan( trim, accountUser, contextPath, now, docXML.getDocumentType());
		// Bind to placeholders
		placeholderBindScan( accountUser.getAccount(), trim, mdTrim, contextPath, now, BindPhase.CREATE, docXML);
		// Create an event
		MenuData mdEvent = establishEvent( accountUser.getAccount(), trim, now, variables);
		if (mdEvent==null) {
			throw new RuntimeException( "Unable to create instance of event for " + trim.getName());
		}
		mdEvent.setDocumentId(docXML.getId());
		// Marshal the finished TRIM into XML and store in the document.
		marshalToDocument( trim, docXML );
		
		// Make sure this item shows up on the activity list
		addToWIP(mdEvent, trim, now, variables );
		
		return mdEvent;
	}*/

	/**
	 * Submit the document associated with this event
	 * @throws Exception 
	 */
	public void submit(MenuData mdEvent, AccountUser activeAccountUser, PrivateKey userPrivateKey) throws Exception {
	    submit(mdEvent, activeAccountUser, null, userPrivateKey);
    }

    /**
	 * Submit the document associated with this event
	 * @throws Exception 
	 */
	public void submitNow(MenuData mdEvent, AccountUser activeAccountUser, Date now, PrivateKey userPrivateKey) throws Exception {
        if (0 == mdEvent.getDocumentId()) {
            throw new IllegalArgumentException("Submitted event, " + mdEvent.getId() + " must have a document id");
        }
	    submit(mdEvent.getDocumentId(), activeAccountUser, null, now, userPrivateKey);
    }
	
    /**
     * Submit the document associated with this event to be queued for processing on queueOnDate
     * @throws Exception 
     */
    public void submit(MenuData mdEvent, AccountUser activeAccountUser, Date queueOnDate, PrivateKey userPrivateKey) throws Exception {
        if (0 == mdEvent.getDocumentId()) {
            throw new IllegalArgumentException("Submitted event, " + mdEvent.getId() + " must have a document id");
        }
        submit(mdEvent.getDocumentId(), activeAccountUser, queueOnDate, userPrivateKey);
    }

    /**
     * Submit the specified document for processing (remote-friendly). The document in this case
     * is already in the Tolven document store. We decrypt it and include in the message (where it 
     * is encrypted with MDBUser private key) so that
     * the message processor will be able to see the contents.
     * If attachments are involved, then we'll also include them in the message.
     */
     public void submit(long documentId, AccountUser activeAccountUser) throws Exception {
        submit(documentId, activeAccountUser);
    }
     
 	public void submit( long documentId, AccountUser activeAccountUser, Date queueOnDate, PrivateKey userPrivateKey) throws Exception {
 		submit( documentId, activeAccountUser, queueOnDate, new Date(), userPrivateKey);
 	}
   /**
	* Submit the specified document for processing (remote-friendly). The document in this case
	* is already in the Tolven document store. We decrypt it and include in the message (where it 
	* is encrypted with MDBUser private key) so that
	* the message processor will be able to see the contents.
	* If attachments are involved, then we'll also include them in the message.
	*/
	public void submit( long documentId, AccountUser activeAccountUser, Date queueOnDate, Date now, PrivateKey userPrivateKey) throws Exception {
		if (0==documentId) throw new IllegalArgumentException( "Submitted DocumentId must not be 0");
		logger.info( "Submit documentId=" + documentId );
		DocBase doc = documentBean.findDocument(documentId);
    	if (doc.getStatus().equals(Status.ACTIVE.value())) {
    		throw new RuntimeException( "Document cannot be submitted, it is immutable");
    	}
    	doc.setFinalSubmitTime(now);
//    	documentBean.saveDocument(doc);
		TolvenMessageWithAttachments tm = new TolvenMessageWithAttachments();
		tm.setAccountId(doc.getAccount().getId());
		tm.setFromAccountId(doc.getAccount().getId());
		tm.setAuthorId(doc.getAuthor().getId());
		tm.setDocumentId(doc.getId() );
		tm.setMediaType( doc.getMediaType());
		if (doc instanceof DocXML ) {
			tm.setXmlNS( ((DocXML)doc).getXmlNS());
		}
		tm.setPayload(docProtectionBean.getDecryptedContent(doc, activeAccountUser, userPrivateKey));
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
			tma.setPayload(docProtectionBean.getDecryptedContent(attachedDoc, activeAccountUser, userPrivateKey));
			tm.getAttachments().add(tma);
		}
		tmSchedulerBean.queueTolvenMessage(tm, queueOnDate);
	}
/*
	public void submit( long accountId, long userId, ContinuityOfCareRecord ccr ) throws Exception{
		// Send the resulting document to tolven for persistence and rule processing
		TolvenMessage tm = new TolvenMessage();
		tm.setAccountId(accountId);
		tm.setFromAccountId(tm.getAccountId());
		tm.setAuthorId(userId);
		tm.setXmlName("ContinuityOfCareRecord");
		tm.setXmlNS( "urn:astm-org:CCR");
		ByteArrayOutputStream output = new ByteArrayOutputStream( );
		xmlBean.marshalCCR( (ContinuityOfCareRecordEx) ccr, output );
        accountProcessingProctectionLocal.setAsEncryptedContent(output.toByteArray(), tm);
        tmSchedulerBean.queue(tm);
	}
	*/
	/**
	 * Verify and cross reference a CCR document exchange. For each id we are about to send, store
	 * a cross-reference entry in our "send" account exchange record, no duplicates.
	 * 
	 */
	public void verifyCCR( ) {
		
	}
	
	/**
	 * Send a copy of an existing document to another account
	 * @throws Exception 
	 */
	public void sendCopyTo( AccountUser activeAccountUser, long documentId, long otherAccountId, PrivateKey userPrivateKey ) throws Exception {
		DocXML docXML = (DocXML) documentBean.findDocument(documentId);
		byte[] payload = docProtectionBean.getDecryptedContent(docXML, activeAccountUser, userPrivateKey);
		// Send the document to the other account for persistence and rule processing
		TolvenMessage tm = new TolvenMessage();
		tm.setFromAccountId(activeAccountUser.getAccount().getId());
		tm.setAccountId(otherAccountId);
		tm.setAuthorId(activeAccountUser.getUser().getId());
        accountProcessingProctectionLocal.setAsEncryptedContent(payload, tm);
//		tm.setPayload( payload );
		tm.setXmlName( docXML.getXmlName());
		tm.setXmlNS( docXML.getXmlNS());
//		ByteArrayOutputStream output = new ByteArrayOutputStream( );
//		xmlBean.marshalCCR( ccr, output );
//		tm.setPayload(output.toByteArray());
        tmSchedulerBean.queue(tm);
	}
	
	
	/**
	 * Given an option name in the specified trim, create an instance of that
	 * option in the current trim. This typically involves opening the trim referenced by
	 * an include element and including that into the trim.
	 * @param trim
	 * @param path
	 * @return the newly added node in the trim
	 */
	public Object insertChoice(Trim trim, String path, String choice) {
		
		return null;
	}

    private TrimMarshaller getTrimMarshaller() {
        if(trimMarshaller == null) {
            trimMarshaller = new TrimMarshaller();
        }
        return trimMarshaller;
    }
    
}
