/**
 * 
 */
package org.tolven.app.bean;

import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.tolven.app.CreatorLocal;
import org.tolven.app.EPrescriptionLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuStructure;
import org.tolven.app.entity.PlaceholderID;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.Status;
import org.tolven.doc.DocumentLocal;
import org.tolven.doc.entity.DocXML;
import org.tolven.logging.TolvenLogger;
import org.tolven.security.key.DocumentSecretKey;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.ST;
import org.tolven.trim.Trim;
import org.tolven.trim.TrimMarshaller;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.TRIMException;
import org.tolven.trim.ex.TrimEx;

/**
 * @author mohammed
 *
 */
@Stateless()
@Local(EPrescriptionLocal.class)
public class EPrescriptionBean implements EPrescriptionLocal {

	private static final String TRIM_NS = "urn:tolven-org:trim:4.0"; 
	public static final String DIRECTION_INCOMING = "incoming";
	
	private Connection tolvenCon;
	@PersistenceContext private EntityManager em;
	@EJB private CreatorLocal creatorBean;
	@EJB private DocumentLocal docBean;
	@EJB private MenuLocal menuBean;
	@EJB private TolvenPropertiesLocal propertyBean;	
    private static TrimMarshaller trimMarshaller;
	Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * @return the docBean
	 */
	public DocumentLocal getDocBean() {
		return docBean;
	}
	/**
	 * @param docBean the docBean to set
	 */
	public void setDocBean(DocumentLocal docBean) {
		this.docBean = docBean;
	}
	/**
	 * @return the creatorBean
	 */
	public CreatorLocal getCreatorBean() {
		return creatorBean;
	}
	/**
	 * @param creatorBean the creatorBean to set
	 */
	public void setCreatorBean(CreatorLocal creatorBean) {
		this.creatorBean = creatorBean;
	}

	/**
	 * @return the menuBean
	 */
	public MenuLocal getMenuBean() {
		return menuBean;
	}
	/**
	 * @param menuBean the menuBean to set
	 */
	public void setMenuBean(MenuLocal menuBean) {
		this.menuBean = menuBean;
	}
	/**
	 * @return the tolvenCon
	 */
	public Connection getTolvenCon() {
		return tolvenCon;
	}
	/**
	 * @param tolvenCon the tolvenCon to set
	 */
	public void setTolvenCon(Connection tolvenCon) {
		this.tolvenCon = tolvenCon;
	}

	/**
	 * @author mohammed
	 * Function to retrieve the menu data
	 * @param firstName
	 * @param middleName
	 * @param lastName
	 * @return patientId
	 */
	public Long getPatientIdFromMenuData(String firstName , String middleName , String lastName){
		String qs = null;
		Query query = null;
		if((!firstName.equals("null") && firstName.trim().length() > 0)
				&& (!middleName.equals("null") && middleName.trim().length() > 0)
				&& (!lastName.equals("null") && lastName.trim().length() > 0)){
						qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.string02 = :f AND md.string03 = :m AND md.string01 = :l");
						query = em.createQuery( qs );
						query.setParameter( "f", firstName );
						query.setParameter( "m", middleName );
						query.setParameter( "l", lastName );
		}else if((firstName.equals("null") || firstName.trim().length() == 0)
				&& (!middleName.equals("null") && middleName.trim().length() > 0)
				&& (!lastName.equals("null") && lastName.trim().length() > 0)){
			qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.string03 = :m AND md.string01 = :l");
			query = em.createQuery( qs );
			query.setParameter( "m", middleName );
			query.setParameter( "l", lastName );
		}else if((!firstName.equals("null") && firstName.trim().length() > 0)
				&& (middleName.equals("null") || middleName.trim().length() == 0)
				&& (!lastName.equals("null") && lastName.trim().length() > 0)){
			qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.string02 = :f AND md.string01 = :l");
			query = em.createQuery( qs );
			query.setParameter( "f", firstName );
			query.setParameter( "l", lastName );
		}else if((firstName.equals("null") || firstName.trim().length() == 0)
				&& (middleName.equals("null") || middleName.trim().length() == 0)
				&& (!lastName.equals("null") && lastName.trim().length() > 0)){
			qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.string01 = :l");
			query = em.createQuery( qs );
			query.setParameter( "l", lastName );
		}
		List<MenuData> results = query.getResultList();
	return results.get(0).getId();
	}
	
	/**
	 * @author mohammed
	 * @param prescriberOrderNumber
	 * @return
	 */
	public MenuData getMenuDataFromPrescriberOrderNumber(String prescriberOrderNumber){
		try {
			MenuData menuData = new MenuData();
			if(null != prescriberOrderNumber && !prescriberOrderNumber.equals("")){
				String qs = null;
				Query query = null;
				qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.id = :p");
				query = em.createQuery( qs );
				query.setParameter( "p", Long.parseLong(prescriberOrderNumber));
				if(query.getResultList() != null && query.getResultList().size() > 0)
					menuData =  ((MenuData)query.getResultList().get(0));
			}	
			return menuData;
			}
		catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Method to check whether the refillRequest was submitted or not.
	 * @param mdRefill
	 * @return
	 */
	public boolean checkForSubmittedData(MenuData mdRefill){
		long documentId = mdRefill.getDocumentId();
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.documentId = :docId and md.status = 'ACTIVE'");
		query = em.createQuery( qs );
		query.setParameter( "docId", documentId);
		if(query.getResultList() != null && query.getResultList().size() > 0){
			return true;
		}
		return false;	
	}
	
	/**
	 * @author mohammed 
	 * @param prescriberOrderNumber
	 * @param refill
	 * @return
	 */
	public MenuData changeNumberOfRefills(String prescriberOrderNumber, Long refill){
		String qs = null;
		Query query = null;
		MenuData menuData = null;
		qs = String.format(Locale.US, "SELECT md FROM MenuData md WHERE md.id = :p");
		query = em.createQuery( qs );
		query.setParameter( "p", Long.parseLong(prescriberOrderNumber ));
		menuData =  ((MenuData)query.getResultList().get(0));
		MenuData mdNew = new MenuData();
		mdNew = menuData;	
		/* Deleting all the references of the menu data in the same table with other rows */
		String qs1 = null;
		Query query1 = null;
		qs1 = String.format(Locale.US, "SELECT md FROM MenuData md WHERE md.referenceId = :p");
		query1 = em.createQuery( qs1 );
		query1.setParameter( "p", Long.parseLong(prescriberOrderNumber ));
		for(int i=0; i< query1.getResultList().size(); i++){
			MenuData md = ((MenuData)query1.getResultList().get(i));
			em.remove(md);
		}
		/* Deleting the reference of the menu data with the placeholder_id table */
		String qs2 = null;
		Query query2 = null;
		qs2 = String.format(Locale.US, "SELECT pd FROM PlaceholderID pd WHERE pd.placeholder.id = :p");
		query2 = em.createQuery( qs2 );
		query2.setParameter( "p", Long.parseLong(prescriberOrderNumber ));
		if(query2.getResultList().size() > 0)
			em.remove(((PlaceholderID)query2.getResultList().get(0)));
		
		/* removing the already existing menu data main entry */
		em.remove(menuData);
		
		if(mdNew.getLong03() < refill){
			return mdNew;
		}else
			mdNew.setLong03(refill);
		return mdNew;	
	}
	
	/**
	 * @author mohammed
	 * This function retrieve latest trim object corresponding to a menudata id.
	 * @param Long id
	 * @param AccountUser accountUser
	 * @return TrimEx
	 * @throws JAXBException
	 */
	public TrimEx findTrimData(Long id, AccountUser accountUser,PrivateKey privateKey) throws JAXBException{
		MenuData md = getMenuBean().findMenuDataItem(id);
		if (md!=null){
			DocXML docXMLFrom = (DocXML) docBean.findDocument(md.getDocumentId());
			TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(docXMLFrom, accountUser,privateKey);
			return trim;
		}
		else
			return new TrimEx();
	}
	
	/**
	 * @author mohammed
	 * This function is used to retrieve the AccountID containing the prescriber with a particular SPI
	 * @param spi
	 * @return accountId
	 */
	public String retrieveAccountIdFromSPI (String spi){
		String accountId = null;
		String qs = null;
		Query query = null;
		qs = null;
		qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.pqUnits03 = :spi");
		query = em.createQuery(qs);
		query.setParameter( "spi", spi);
		if(query.getResultList().size() > 0){
			accountId = String.valueOf(((MenuData)query.getResultList().get(0)).getId());
			return accountId;
		}else{
			return "failure";
		}
	}
	
	/**
	 * The function is used to retrieve the MenuData based on the relatesToMessageId
	 *  and set the status(Success/Error) accordingly.
	 * @param relatesToMessageId
	 * @param Status
	 * @return MenuData
	 */
	public MenuData retrieveMenuData(String relatesToMessageId, String surescriptStatus){
		MenuData mdBase = new MenuData();
		String qs = null;
		String qu = null;
		Query query = null;
		Query query1 = null;
		try {
			if (!surescriptStatus.equals("AddPrescriberResponse") && !surescriptStatus.equals("RxFill")) {			
			
				qu = String.format(Locale.US, "SELECT md.string06  FROM MenuData md WHERE md.status = 'ACTIVE' and md.string07 = :relatesToMessageId");
				Query qry = em.createQuery(qu);
				qry.setParameter("relatesToMessageId", relatesToMessageId);
				String medicationStatus = (String) qry.getResultList().get(0);
				if (medicationStatus.equals("Discontinued")&& !surescriptStatus.equals("CancelRXResponse Received")) {
					surescriptStatus = "Awaiting CancelRxResponse";
				}
			}else if(surescriptStatus.equals("AddPrescriberResponse")){
				surescriptStatus = "Active";
			}else if (surescriptStatus.equals("RxFill")) {
				surescriptStatus = "Success";
				qs = String.format(Locale.US,
								"SELECT md  FROM MenuData md WHERE md.status = 'ACTIVE' and md.id = "+relatesToMessageId);
				query = em.createQuery(qs);
				MenuData resultData = ((MenuData)query.getResultList().get(0));
				resultData.setString08(surescriptStatus);
				resultData.setPqStringVal02("RxFill");
				em.persist(resultData);
				return resultData;
			}
			query1 = em.createQuery("SELECT md  FROM MenuData md WHERE md.status = 'ACTIVE' and md.string07 = :relatesToMessageId");
			query1.setParameter("relatesToMessageId", relatesToMessageId );
			MenuData resultMd = (MenuData) query1.getResultList().get(0);
			resultMd.setString08(surescriptStatus);
			em.persist(resultMd);
			return resultMd;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(query.getResultList().size() > 0){
			mdBase = (MenuData)query.getResultList().get(0);
			return mdBase;
		}else{
			return null;
		}
	}	
	
	/**
	 * @author mohammed
	 * @param prescriberOrderNumber
	 * @return menuData
	 */
	public MenuData incorporatePrescriberDetailsInNewRX(String prescriberOrderNumber , String spi){
		String qs = null;
		Query query = null;
		String qs1 = null;
		Query query1 = null;
		MenuData menuData = null;
		MenuData menuData1 = null;
		qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.id = :p");
		query = em.createQuery( qs );
		query.setParameter( "p", Long.parseLong(prescriberOrderNumber));
		qs1 = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.pqUnits03 = :spi");
		query1 = em.createQuery( qs1 );
		query1.setParameter( "spi", spi);
		menuData =  ((MenuData)query.getResultList().get(0));
		if (query1.getResultList().size() > 0) {
			menuData1 =  ((MenuData)query1.getResultList().get(0));
			menuData.setParent04(menuData1);
			em.persist(menuData);
		}
		return menuData;	
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
		docBean.saveDocument(docXML);
	}
	
    /**
	 * An existing placeholder is transitioned (updated). The placeholder document is immutable. So we create
	 * a new document.
	 * We won't actually change the placeholder until the change is submitted.
	 */
	public MenuData genTRIMEvent( MenuData mdPlaceholder, AccountUser accountUser, String transitionName, Date now, String messageType,PrivateKey privateKey ) throws JAXBException, TRIMException {
		if (mdPlaceholder==null) {
			throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "]Missing placeholder for transition");
		}
		CreatorBean cb = new CreatorBean();
		String knownType = accountUser.getAccount().getAccountType().getKnownType();
		DocXML docXMLFrom = (DocXML) docBean.findDocument(mdPlaceholder.getDocumentId());
		// Note: We're reading this in from an immutable document 
		// but will be creating a new document with the modified trim, not
		// modifying the original document.
		TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(docXMLFrom, accountUser,privateKey);
		// We reverse the normal, that is, use the trim to populate the event
		DocXML docXMLNew = docBean.createXMLDocument( TRIM_NS, accountUser.getUser().getId(), accountUser.getAccount().getId() );
		docXMLNew.setSignatureRequired(cb.isSignatureRequired( trim, knownType ));
		// Carry forward attachments
		docBean.copyAttachments(docXMLFrom, docXMLNew );
        logger.info( "Document (event) created, id: " + docXMLNew.getId() );
		
		// Adjust status based on selected transition
//		String status = cb.calculateTransition( trim, transitionName);
        String status = "active";
		
		// If we have not stored a tolvenId for placeholder yet, do it now.
		// This can happen when the placeholder started from an external message
		if (trim.getTolvenId(accountUser.getAccount().getId())!=null) {
			trim.addTolvenId(cb.createTolvenId( mdPlaceholder, null, now, status, accountUser.getUser().getLdapUID() ));
		}
		// Setup up context variables for substitution
		Map<String, Object> variables = new HashMap<String, Object>(10);
		MenuPath contextPath = new MenuPath (mdPlaceholder.getPath());
		variables.putAll(contextPath.getNodeValues());
//		{
//			String assignedPath = accountUser.getProperty().get("assignedAccountUser");
//			if (assignedPath!=null) {
//				MenuData assigned = menuBean.findMenuDataItem(accountUser.getAccount().getId(), assignedPath);
//				variables.put("assignedAccountUser", assigned);
//			}
//		}
		variables.put("trim", trim);
		// New menu data event
		String instancePath = cb.getInstancePath(trim, knownType);
		// Create an event to hold this transaction
		MenuData mdEvent = assignEvent( accountUser.getAccount(), instancePath, trim, now, variables);
		mdEvent.setDocumentId(docXMLNew.getId());
		mdEvent.setActStatus(status);
		
		String trimName = ((TrimEx) trim).getName();
		if (trimName.equals("reg/evn/assigned/md")) {
			List<ObservationValueSlot> prescriberOVS = ((ActEx) trim.getAct()).getRelationship().
			get("prescriber").getAct().getObservation().getValues();
			
			if (messageType.equals("Error")) {
				prescriberOVS.get(9).setST(getST("Error"));
			} else if (!messageType.equals("Error") && null!= messageType) {
				if (((ActEx) trim.getAct()).getRelationship()
						.get("updateStatus") != null && ((ActEx) trim.getAct()).getRelationship()
						.get("updateStatus").getAct().getObservation()
						.getValues().get(0).getST().getValue().length() > 0) {
							((ActEx) trim.getAct()).getRelationship()
							.get("updateStatus").getAct().getObservation()
							.getValues().get(0).setST(getST(""));
				} else {
					prescriberOVS.get(6).setST(getST(messageType));
				}
				DateFormat df = new SimpleDateFormat("yyyyMMdd");
				try {
					String endDate = prescriberOVS.get(5).getTS().getValue();
					endDate = endDate.trim().subSequence(0, 8).toString();
					Date activeEndDate = df.parse(endDate);
					
					Date currentDate = new Date();
					if (prescriberOVS.get(3).getST().getValue().equals("0") &&
							currentDate.after(activeEndDate)) {
						prescriberOVS.get(9).setST(getST("Disabled"));
					} else {
						prescriberOVS.get(9).setST(getST("Active"));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IndexOutOfBoundsException e) {
					TolvenLogger.error("Invalid ActiveEndDate field.", EPrescriptionBean.class);
					e.printStackTrace();
				}
			}
		} else if (trimName.equals("obs/evn/prescriberlocation")) {
			if (messageType.equals("Error")) {
				((ActEx) trim.getAct()).getRelationships().get(0).getAct().getObservation().getValues().get(10).setST(getST("Error"));
			}else if (!messageType.equals("Error") && null!= messageType) {
				((ActEx) trim.getAct()).getRelationships().get(0).getAct().getObservation().getValues().get(10).setST(getST("Active"));
				((ActEx) trim.getAct()).getRelationships().get(0).getAct().getObservation().getValues().get(9).setST(getST(messageType));
			}
		} else if (trimName.equals("obs/evn/patientPrescription")) {
			if (messageType.equals("Status")) {
				((ActEx) trim.getAct()).getRelationships().get(0).getAct().getObservation().getValues().get(40).setST(getST("Verified"));
			}else if (messageType.equals("Error")) {
				((ActEx) trim.getAct()).getRelationships().get(0).getAct().getObservation().getValues().get(40).setST(getST("Error"));
			}
		} else if (trimName.equals("obs/evn/refillRequest")) {
			if (messageType.equals("Status")) {
				((ActEx) trim.getAct()).getRelationships().get(0).getAct().getObservation().getValues().get(6).setST(getST("Verified"));
			}else if (messageType.equals("Error")) {
				((ActEx) trim.getAct()).getRelationships().get(0).getAct().getObservation().getValues().get(6).setST(getST("Error"));
			}
		} else if (trimName.equals("obs/evn/currentMedicationDetailsForRefillRequest")) {
			if (messageType.equals("Status")) {
				((ActEx) trim.getAct()).getRelationships().get(0).getAct().getObservation().getValues().get(11).setST(getST("Verified"));
			}else if (messageType.equals("Error")) {
				((ActEx) trim.getAct()).getRelationships().get(0).getAct().getObservation().getValues().get(11).setST(getST("Error"));
			}
		}
		// Reference the event in the Trim (add to event history)
		trim.addTolvenEventId(cb.createTolvenId( mdEvent, null, now, status, accountUser.getUser().getLdapUID() ));
		// OK, now marshal the finished TRIM into XML and store in the document.
		marshalToDocument( trim, docXMLNew );
		return mdEvent;
	}
	
	/**
	 * This function is used to convert string to ST.
	 * @param str
	 * @return st
	 */
	public ST getST(String str) {
		ST st = new ST();
		st.setValue(str);
		return st;
	}
	
	/**
	 * An Event is created for each update of a placeholder, including the first instance.
	 *  The Event MenuData entry represents the trim message/document.
	 * @param account
	 * @param The placeholder
	 * @param now
	 * @return The new event
	 */
	public MenuData assignEvent( Account account, String instancePath, Trim trim, Date now, Map<String, Object> variables ) {
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
	
	/**
	 * Method to get the MenuData based on relates to message id.
	 * @param relatesToMessageId
	 * @return
	 */
	public MenuData getMdFromMessageId(String relatesToMessageId) {
		Query query = em.createQuery("SELECT md  FROM MenuData md WHERE md.status = 'ACTIVE' and md.string07 = :relatesToMessageId");
		query.setParameter("relatesToMessageId", relatesToMessageId );
		MenuData resultMd = new MenuData();
		if(query.getResultList().size() > 0)
			resultMd = (MenuData) query.getResultList().get(0);
		return resultMd;
	}

	/**
	 * Method to alter the Table entry in admin SurescriptsResponse
	 * @param relatesToMessageId
	 * @return
	 */
	public String retrieveMdFromRefillResponseData(String relatesToMessageId, String result){
		String finalResult="failure";
		try {
			Query query = em.createQuery("SELECT md  FROM MenuData md WHERE md.string01= 'Refill Response' and md.string07 = :relatesToMessageId");
			query.setParameter("relatesToMessageId", relatesToMessageId );
			MenuData resultMd = null;
			if(query.getResultList().size() > 0){
				resultMd = (MenuData) query.getResultList().get(0);
				if(result.equals("Status")){
					resultMd.setString08("Verified");
				}else if(result.equals("Error")){
					resultMd.setString08("Error");
				}
				finalResult="success";
				em.persist(resultMd);
			}
		} catch (Exception e) {
			TolvenLogger.error("retrieveMdFromRefillResponseData "+ relatesToMessageId, EPrescriptionBean.class);
			e.printStackTrace();
		}
		return finalResult;
	}
	
	
	
	/**
	 * Method to retrieve all the spi numbers corresponding to a prescriber
	 */
	public ArrayList<String> retrieveAllSPIs(long prescId){
		ArrayList<String> spiNumbers = new ArrayList<String>();
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.id = :prescId");
		query = em.createQuery(qs);
		query.setParameter( "prescId", prescId);
		if(null != query.getResultList() && query.getResultList().size() > 0){
			spiNumbers.add(((MenuData)query.getResultList().get(0)).getPqUnits03());
			String qsnew = null;
			Query qry = null;
			qsnew = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.parentPath01 = :parent AND md.status = 'ACTIVE' ");
			//qsnew = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.parentPath01 = :parent ");
			qry = em.createQuery(qsnew);
			qry.setParameter( "parent", ((MenuData)query.getResultList().get(0)).getPath());
			for(MenuData md : (ArrayList<MenuData>)qry.getResultList()){
				spiNumbers.add(md.getString05());
			}
		}
		return spiNumbers;
	}
	
	/**
	 * Method to retrieve all the prescriberLocation Details corresponding to a prescriber
	 */
	public ArrayList<MenuData> retrieveAllPrescriberLocations(long id){
		ArrayList<MenuData> prescriberLocations = new ArrayList<MenuData>();
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.id = :prescId");
		query = em.createQuery(qs);
		query.setParameter( "prescId", id);
		if(null != query.getResultList() && query.getResultList().size() > 0){
			String qsnew = null;
			Query qry = null;
			qsnew = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.parentPath01 = :parent AND md.status = 'ACTIVE' ");
			qry = em.createQuery(qsnew);
			qry.setParameter( "parent", ((MenuData)query.getResultList().get(0)).getPath());
			
			for(MenuData md : (ArrayList<MenuData>)qry.getResultList()){
				prescriberLocations.add(md);
			}
		}
		return prescriberLocations;
	}
	
	/**
	 * Method to retrieve all the Administration Details corresponding to a prescriber order number
	 * Use CCHITBean.findMenuChildren() instead 
	 */
	/*public ArrayList<MenuData> retrieveAllAdministrationDetails(long id){
		ArrayList<MenuData> administrationDetails = new ArrayList<MenuData>();
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.id = :prescId");
		query = em.createQuery(qs);
		query.setParameter( "prescId", id);
		if(null != query.getResultList() && query.getResultList().size() > 0){
			String qsnew = null;
			Query qry = null;
			qsnew = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.parentPath01 = :parent AND md.status = 'ACTIVE' ");
			qry = em.createQuery(qsnew);
			qry.setParameter( "parent", ((MenuData)query.getResultList().get(0)).getPath());
			
			for(MenuData md : (ArrayList<MenuData>)qry.getResultList()){
				administrationDetails.add(md);
			}
		}
		return administrationDetails;
	}
	*/
	/**
	 * Method to retrieve the Prescriber Name by passing the SPI number
	 * @param spi
	 * @return
	 */
	public String retrievePrescriberNameFromId(String spi){
		String prescriberName="";
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.pqUnits03 = :spi");
		query = em.createQuery(qs);
		query.setParameter( "spi", spi);
		if(null != query.getResultList() && query.getResultList().size() > 0){
			MenuData md = (MenuData)query.getResultList().get(0);
			if(null!= md.getString02() && !md.getString02().equals("")){
				prescriberName = prescriberName + md.getString02() + ", ";
			}
			if(null!= md.getString03() && !md.getString03().equals("")){
				prescriberName = prescriberName + md.getString03() + ", ";			
			}
			if(null!= md.getString01() && !md.getString01().equals("")){
				prescriberName = prescriberName + md.getString01();
			}
		}
		return prescriberName;
	}
	
	/**
	 * Method to get MenuData provided a field and value.
	 * @param field eg: string01, pqUnits03
	 * @param value only string
	 * @return
	 */
	public MenuData getMdFromFieldAndValue(String field, String value) {
		Query query = em.createQuery("SELECT md  FROM MenuData md WHERE md.status = 'ACTIVE' and md."+field+" = :value");
		query.setParameter("value", value );
		MenuData resultMd = new MenuData();
		if(query.getResultList().size() > 0)
			resultMd = (MenuData) query.getResultList().get(0);
		return resultMd;
	}

    private TrimMarshaller getTrimMarshaller() {
        if(trimMarshaller == null) {
            trimMarshaller = new TrimMarshaller();
        }
        return trimMarshaller;
    }
    
}
