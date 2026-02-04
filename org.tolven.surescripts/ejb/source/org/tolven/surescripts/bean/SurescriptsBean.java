/*
 *  Copyright (C) 2011 Tolven Inc
 *
 * This library is free software; you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either version 2.1 of the License, or (at your option) 
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 * details.
 *
 * Contact: info@tolvenhealth.com
 */
package org.tolven.surescripts.bean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.security.auth.login.LoginContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.tolven.app.CreatorLocal;
import org.tolven.app.EPrescriptionLocal;
import org.tolven.app.FDBInterface;
import org.tolven.app.MenuLocal;
import org.tolven.app.TrimLocal;
import org.tolven.app.TrimMessageLocal;
import org.tolven.app.entity.DrugQualifier;
import org.tolven.app.entity.MenuData;
import org.tolven.core.AccountDAOLocal;
import org.tolven.core.ActivationLocal;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.entity.AccountType;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.Status;
import org.tolven.core.entity.TolvenUser;
import org.tolven.doc.DocumentLocal;
import org.tolven.logging.TolvenLogger;
import org.tolven.surescripts.BodyType;
import org.tolven.surescripts.DirectoryDownload;
import org.tolven.surescripts.ErrorStatusMessageCreator;
import org.tolven.surescripts.GenderType;
import org.tolven.surescripts.HeaderType;
import org.tolven.surescripts.MandatoryPharmacyType;
import org.tolven.surescripts.MedicationType;
import org.tolven.surescripts.MedicationType.Diagnosis;
import org.tolven.surescripts.MessageType;
import org.tolven.surescripts.PasswordType;
import org.tolven.surescripts.PatientType;
import org.tolven.surescripts.PharmacyLocal;
import org.tolven.surescripts.PhoneType;
import org.tolven.surescripts.PrescriberType;
import org.tolven.surescripts.SecurityType;
import org.tolven.surescripts.SupervisorType;
import org.tolven.surescripts.SureScriptsCommunicator;
import org.tolven.surescripts.SurescriptsLocal;
import org.tolven.surescripts.SurescriptsVO;
import org.tolven.surescripts.TaxonomyType;
import org.tolven.surescripts.UsernameTokenType;
import org.tolven.surescripts.entity.ErrorCodes;
import org.tolven.surescripts.entity.MessageDetails;
import org.tolven.surescripts.entity.MessageInfos;
import org.tolven.surescripts.entity.ValidMessageTypes;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.CE;
import org.tolven.trim.LabelFacet;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.ST;
import org.tolven.trim.ValueSet;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.TRIMException;
import org.tolven.trim.ex.TrimEx;
import org.tolven.trim.ex.TrimFactory;
import org.tolven.us.states.DemographicsLocal;
import org.tolven.us.states.entity.StateNames;
import org.tolven.xml.NamespacePrefixMapperImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>The bean class to perform operation on SureScripts table.</p>
 * @author mohammed
 */

@Stateless()
@Local(SurescriptsLocal.class)
public class SurescriptsBean implements SurescriptsLocal {

	private static final String JAAS_LOGIN_CONFIG_PROPERTY = "java.security.auth.login.config";
    public static final String TOLVEN_LOGINCONTEXT = "tolvenLDAP";
    public static final String DIRECTION_INCOMING = "incoming";
    public static final String REFILL_REQUEST_TRIM_NAME = "obs/evn/refillRequest";
    
	@PersistenceContext private EntityManager em;
	@EJB private EPrescriptionLocal epBean;
	@EJB private DemographicsLocal demographicsBean;
	@EJB private CreatorLocal creatorBean;
	@EJB private DocumentLocal docBean;
	@EJB private MenuLocal menuBean;
	@EJB private TolvenPropertiesLocal propertyBean;
	@EJB private TrimMessageLocal trimMsgBean;
	@EJB private TrimLocal trimBean;
	@EJB private AccountDAOLocal accountBean;
	@EJB private TolvenPropertiesLocal tproperties;
	@EJB private FDBInterface fdbBean;
    @EJB private ActivationLocal activationBean;
    @EJB private PharmacyLocal pharmacyBean;
	private AccountUser accountUser;
	private LoginContext lc;
	private Connection tolvenCon;
	private TrimEx templateTrim;
	private String source = null;
	private String templateId = "";
	private ActRelationship responseRelation;
	private ActRelationship relationHeader;
	private ActRelationship relationPharmacy;
	private ActRelationship relationPrescriber;
	private ActRelationship relationPatient;
	private ActRelationship relationMedicationPrescribed;
	private ActRelationship relationMedicationRequested;
	private ActRelationship relationSupervisor;
	private ObservationValueSlot valueSlot;
	private MenuData mdPrior;
	private int messageCount;
	private AccountType accountType = null;
	private TrimFactory trimFactory = new TrimFactory();
	private Logger logger = Logger.getLogger(this.getClass());
	private ErrorStatusMessageCreator creator = new ErrorStatusMessageCreator();
	private Date now = new Date();
	protected String uid;
	private Calendar cal = Calendar.getInstance();
	
	/**
	 * @return the accountUser
	 */
	public AccountUser getAccountUser() {
		return accountUser;
	}

	/**
	 * @param accountUser the accountUser to set
	 */
	public void setAccountUser(AccountUser accountUser) {
		this.accountUser = accountUser;
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
	 * @return the messageCount
	 */
	public int getMessageCount() {
		return messageCount;
	}

	/**
	 * @param messageCount the messageCount to set
	 */
	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

	/**
	 * Checks whether the master user is associated with account being used.
	 * @param accountId
	 * @return true if Master user is added to the account.
	 */
	public boolean checkMasterAccountAssociation(Long accountId){
		
		if (tproperties.getProperty("eprescription.surescripts.masteruser") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.masteruser- .", SureScriptsCommunicator.class);
		} else {
			String uid = tproperties.getProperty("eprescription.surescripts.masteruser");
			// Make sure that the master user is already an active member of the account
			AccountUser accountUser = accountBean.findAccountUser(uid, accountId);
			if (accountUser != null) {
				return true;
				/* // TODO Automatically add master account.
				TolvenUser user = getActivationBean().findUser(uid);
				try {
		            UserPrivateKey userPrivateKey = KeyUtility.getUserPrivateKey();
		            accountBean.inviteAccountUser(accountBean.findAccount(accountId), getActivationBean().findAccountUser(getSessionAccountUserId()), user, userPrivateKey, getNow(), false);
		            TolvenLogger.info("Master user added to this account successfully. "+accountId, SurescriptsBean.class);	
		            return true;
		        } catch (Exception ex) {
		            throw new RuntimeException("When invited to an Account, a user must have UserPublicKey to protect the AccountPrivateKey");
		        }*/
			}
		}
		return false;
	}
	
	/**
	 * Method to changed the status of the AddPresciber Message in the message_details table in the database.
	 * @param messageId
	 * @param status
	 */
	private void changeStatusOfPrescriber(String messageId, String status) {
		try {
			String qs = null;
			Query query = null;
			MessageDetails msgDetails = null;
			qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :p AND md.status = 'new' AND md.direction = 'outgoing'");
			query = em.createQuery( qs );
			query.setParameter( "p", messageId);
			
			if (query.getResultList() != null && query.getResultList().size() > 0) {
				msgDetails = ((MessageDetails) query.getResultList().get(0));
				if (status.equals("approved")) {
					msgDetails.setStatus("success");
				} else if (status.equals("denied")) {
					msgDetails.setStatus("failure");
				}
				em.persist(msgDetails);
				TolvenLogger.info("Status of AddPrescriber message changed to "+ 
						(status.trim().toLowerCase().indexOf("approved") != -1 ? "success": "failure"), SurescriptsBean.class);
			}
		} catch (Exception e) {
			TolvenLogger.error("Exception occured while changing status of perscriber.", SurescriptsBean.class); 
			e.printStackTrace();
		}
	}

	/**
	 * @author mohammed
	 * This function is used to persist the messages in the database
	 * @param messageId
	 * @param messageType
	 * @param message
	 * @return
	 */
	public ArrayList<String> persistSurescriptsMessages(String messageId, String messageType, String message,
			String direction, String messageInfoId, String prescriberOrderNum, String accountId, String relatesToMessageID){
		ArrayList<String> result = new ArrayList<String>();
		try {
			String qs = null;
			Query query = null;
			MessageDetails msgDetails = new MessageDetails();
			qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :p");
			query = em.createQuery( qs );
			query.setParameter( "p", messageId);
			if(query.getResultList().size() > 0){
				if(!messageType.equals("Status") && !messageType.equals("Error")){
					result.add(0,"0");
					return result;
				}
			}	
			msgDetails.setMessageId(messageId);
			msgDetails.setMessageType(messageType);
			msgDetails.setMessage(message);
			msgDetails.setDirection(direction);
			if (relatesToMessageID != null) {
				msgDetails.setRelatesToMessageId(relatesToMessageID);
			}
			try {
				msgDetails.setMsgInfoId(Long.parseLong(messageInfoId));
			}
			catch (Exception e) {
				msgDetails.setMsgInfoId(new Long(0));
			}
			try {
				msgDetails.setPrescOrderNum(Long.parseLong(prescriberOrderNum));
			}
			catch (Exception e) {
				msgDetails.setPrescOrderNum(new Long(0));
			}
			try {
				msgDetails.setAccountId(Long.parseLong(accountId));
			}
			catch (Exception e) {
				msgDetails.setAccountId(new Long(0));
			}
			
			if(messageType.equals("GetPrescriberResponse")) {
				msgDetails.setStatus("processed");
			} else {
				msgDetails.setStatus("new");
			}
			msgDetails.setRecievedTime(new Timestamp(new Date().getTime()));
			
			em.persist(msgDetails);
			em.flush();
			result.add(0,"1");
			TolvenLogger.info("persistSurescriptsMessages "+ messageType, SurescriptsBean.class);
		} catch (Exception e) {
			TolvenLogger.error("Exception while persistSurescriptsMessages "+ messageType, SurescriptsBean.class);
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * @author mohammed
	 * The function is used to retrieve all the unprocessed messages from SureScripts.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<SurescriptsVO> retrieveUnprocessedMessages(){
		//TODO used with scheduler, remove later.
		ArrayList<SurescriptsVO> result = new ArrayList<SurescriptsVO>();
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.status = 'new' AND md.direction = 'incoming'");
		query = em.createQuery( qs );
		if(null != query.getResultList()){
			ArrayList<MessageDetails> unprocessedUnits = (ArrayList<MessageDetails>)query.getResultList();
			for(MessageDetails msgDtl : unprocessedUnits){
				SurescriptsVO surVO = new SurescriptsVO();
				surVO.setMessage(msgDtl.getMessage());
				surVO.setAccountUser(retrieveAccountIdFromMessage(msgDtl.getMessageType() , msgDtl.getMessage()));
				result.add(surVO);
			}
		}	
		return result;
	}

	/**
	 * @author mohammed
	 * Method to connect to the postgres Database
	 * @return tolvenCon
	 */
	private Connection connectToPostgres() {
		try {
			Class.forName("org.postgresql.Driver");
			this.tolvenCon = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres" ,"postgres","postgres");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.tolvenCon;
	}
	
	/**
	 * Method to disconnect from the postgres Database
	 */
	private void disconnectFromPostgres(){
		try {
			this.tolvenCon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The function is used to process all the status and error messages received from the surescripts
	 * @param condition
	 * @param messageId
	 */
	public void processStatusErrorMessages(String condition, String messageId){
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :p AND md.status = 'new' AND md.direction = 'outgoing'");
		query = em.createQuery( qs );
		query.setParameter( "p", messageId);
		if(null != query.getResultList() && query.getResultList().size() > 0){
			MessageDetails msgDtls = (MessageDetails)query.getResultList().get(0);
			if(condition.equals("Status"))
				msgDtls.setStatus("success");
			else if(condition.equals("Error"))	
				msgDtls.setStatus("failure");
			em.persist(msgDtls);
		}	
	}
	
	
	/**
	 * Method to authenticate the username and password from the messages
	 * @param username
	 * @param password
	 * @return boolean
	 */
	public boolean authenticateUserCredentials (String username , String password){
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT uc FROM SurescriptsUserCredentials uc WHERE uc.username = :user AND uc.password = :pass ");
		query = em.createQuery( qs );
		query.setParameter( "user", username);
		query.setParameter( "pass", password);
		if(null != query.getResultList()){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Method to process the Received Message from the surescripts
	 * @param messageString
	 * @param needCredentialsCheck
	 * @param accountId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String convertToMessage(String message, boolean needCredentialsCheck, String accountId,PrivateKey privateKey) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document messageDoc = null;
		DocumentBuilder builder = null;
		String messageType = "";
		String messageId = "";
		String fileName = "";
		if(message != null && !message.equals("") && message.trim().length() > 0){
			try {
				builder = factory.newDocumentBuilder();
				messageDoc = builder.parse(new InputSource(new StringReader(message)));
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
				return getCreator().generateErrorMessage(messageDoc ,"601","SCRIPT Validation Error: See free text for description","000");
			} catch (SAXException e1) {
				e1.printStackTrace();
				return getCreator().generateErrorMessage(messageDoc ,"601","SCRIPT Validation Error: See free text for description","000");
			} catch (IOException e1) {
				e1.printStackTrace();
				return getCreator().generateErrorMessage(messageDoc ,"601","SCRIPT Validation Error: See free text for description","000");
			}
			/*  The incoming String message is converted to a Document which can later by parsed and scrutinized */
			messageDoc.getDocumentElement().normalize();
			/* Only if the xml message contains the main tag as Message will further proceedings be done  */
			if(messageDoc.getDocumentElement().getNodeName().equals("Message")){
				 /*	 Processing the Document and Finding Out which prescriber it corresponds to. */
				MessageType recievedMessage = new MessageType();
				JAXBElement< MessageType> messageJAXB = null;
				ParseXML parse = new ParseXML();
				
				try {
					messageJAXB = (JAXBElement<MessageType>) parse.getUnmarshaller().unmarshal(messageDoc);
					recievedMessage = messageJAXB.getValue();
					TolvenLogger.info("XML Unmarshalled to MessageType Object. RelatesToMessageId: "+ recievedMessage.getHeader().getRelatesToMessageID(), SurescriptsBean.class);
				} catch (JAXBException e1) {
					TolvenLogger.info("XML Unmarshalling FAILED. RelatesToMessageId: "+ recievedMessage.getHeader().getRelatesToMessageID(), SurescriptsBean.class);
					e1.printStackTrace();
				}
				
				/* User Credentials Checking*/
                if(needCredentialsCheck){	
					if(recievedMessage.getBody().getError() != null || recievedMessage.getBody().getStatus() != null){
						
					}else{	
						if(recievedMessage.getHeader().getSecurity() != null && recievedMessage.getHeader().getSecurity().getUsernameToken() != null){
							boolean verified = authenticateUserCredentials(recievedMessage.getHeader().getSecurity().getUsernameToken().getUsername()
									, recievedMessage.getHeader().getSecurity().getUsernameToken().getPassword().getValue());
							if(!verified){
								TolvenLogger.info("Authentication Failed. Error Response will be sent.", SurescriptsBean.class);
								return getCreator().generateErrorMessage(messageDoc ,"900","Invalid password for sender.","003");
							}
						}else{
							TolvenLogger.info("Security/UsernameToken missing in incoming message. Error Response will be sent.", SurescriptsBean.class);	
							return getCreator().generateErrorMessage(messageDoc ,"900","No password on file for sender.","005");
						}
					}
                }	
                
				 /* The Type of the message is decided based upon the tag inside the Body */
				String SPI = null;
				String ncpdpid = "0";
				NodeList nodeLst1 = messageDoc.getElementsByTagName("Body");
				Node fstNode1 = nodeLst1.item(0);
				
				if (fstNode1.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode1;
			        fstElmnt.getChildNodes().item(0);
			        if(fstElmnt.getElementsByTagName("RefillRequest").getLength() > 0) {
			        	for(int i=0; i< recievedMessage.getBody().getRefillRequest().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++) {
			        		if(recievedMessage.getBody().getRefillRequest().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")) {
			        			SPI = recievedMessage.getBody().getRefillRequest().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue();
				        	}
			        	}	  
			        	messageId = recievedMessage.getHeader().getMessageID();
			        	messageType = "RefillRequest";
			        	fileName = messageType+"_"+messageId+".xml";
			        } else if(fstElmnt.getElementsByTagName("RxFill").getLength() > 0){
			        	  for(int i=0; i< recievedMessage.getBody().getRxFill().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++){
				        	  if(recievedMessage.getBody().getRxFill().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")){
				        		  SPI = recievedMessage.getBody().getRxFill().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue();
				        	  }
			        	  }	  
			        	  for(int i=0; i< recievedMessage.getBody().getRxFill().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().size(); i++){
				        	  if(recievedMessage.getBody().getRxFill().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("NCPDP")){
				        		  ncpdpid = recievedMessage.getBody().getRxFill().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().get(i).getValue();
				        	  }
			        	  }	  
			        	  messageId = recievedMessage.getHeader().getMessageID();
			        	  messageType = "RxFill";
			        	  fileName = messageType+"_"+messageId+".xml";
			        } else if(fstElmnt.getElementsByTagName("AddPrescriberResponse").getLength() > 0){
			        	  for(int i=0; i< recievedMessage.getBody().getAddPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++){
				        	  if(recievedMessage.getBody().getAddPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")){
				        		  SPI = recievedMessage.getBody().getAddPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue();
				        	  }
			        	  }	
			        	  messageId = recievedMessage.getHeader().getMessageID();
			        	  messageType = "AddPrescriberResponse";
			        	  fileName = messageType+"_"+messageId+".xml";
			        } else if(fstElmnt.getElementsByTagName("AddPrescriberLocationResponse").getLength() > 0){
			        	  for(int i=0; i< recievedMessage.getBody().getAddPrescriberLocationResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++){
				        	  if(recievedMessage.getBody().getAddPrescriberLocationResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")){
				        		  SPI = recievedMessage.getBody().getAddPrescriberLocationResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue();
				        	  }
			        	  }	
			        	  messageId = recievedMessage.getHeader().getMessageID();
			        	  messageType = "AddPrescriberLocationResponse";
			        	  fileName = messageType+"_"+messageId+".xml";
			        } else if(fstElmnt.getElementsByTagName("RxChangeRequest").getLength() > 0){
			        	  for(int i=0; i< recievedMessage.getBody().getRxChangeRequest().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++){
				        	  if(recievedMessage.getBody().getRxChangeRequest().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")){
				        		  SPI = recievedMessage.getBody().getRxChangeRequest().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue();
				        	  }
			        	  }	 
			        	  for(int i=0; i< recievedMessage.getBody().getRxChangeRequest().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().size(); i++){
				        	  if(recievedMessage.getBody().getRxChangeRequest().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("NCPDP")){
				        		  ncpdpid = recievedMessage.getBody().getRxChangeRequest().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().get(i).getValue();
				        	  }
			        	  }	 
			        	  messageId = recievedMessage.getHeader().getMessageID();
			        	   messageType = "RxChangeRequest";
			        	  fileName = messageType+"_"+messageId+".xml";
			        } else if(fstElmnt.getElementsByTagName("CancelRxResponse").getLength() > 0){
			        	   messageType = "CancelRxResponse";
			        	   messageId = recievedMessage.getHeader().getMessageID();
			        	  fileName = messageType+"_"+messageId+".xml";
			        } else if(fstElmnt.getElementsByTagName("Error").getLength() > 0){
			        	  messageType = "Error";
			        	  fileName = messageType+"_"+cal.get(Calendar.YEAR)+cal.get(Calendar.MONTH)+cal.get(Calendar.DATE)+".xml";
			        } else if(fstElmnt.getElementsByTagName("Status").getLength() > 0){
			        	  messageType = "Status";
			        	  fileName = messageType+"_"+cal.get(Calendar.YEAR)+cal.get(Calendar.MONTH)+cal.get(Calendar.DATE)+".xml";
			        } else if(fstElmnt.getElementsByTagName("DirectoryDownloadResponse").getLength() > 0){
			        	  messageId = recievedMessage.getHeader().getMessageID();
			        	  messageType = "DirectoryDownloadResponse";
			        	  fileName = messageType+"_"+messageId+".xml";
			        } else if(fstElmnt.getElementsByTagName("GetPrescriberResponse").getLength() > 0){
			        	  messageId = recievedMessage.getHeader().getMessageID();
			        	   messageType = "GetPrescriberResponse";
			        	  fileName = messageType+"_"+messageId+".xml";
			        } else if(fstElmnt.getElementsByTagName("Verify").getLength() > 0){
			        	  messageType = "Verify";
			        	  fileName = messageType+"_"+cal.get(Calendar.YEAR)+cal.get(Calendar.MONTH)+cal.get(Calendar.DATE)+".xml";
			        }
			        TolvenLogger.info("Request/Response messageType from SureScripts identified as: "+messageType, SurescriptsBean.class);
				}	// end of if loop for NODE_ELEMENT.	
			       
				/* Saving the message to file system. */
				int savedStatus = saveMessageToFileSystem(message, fileName, messageType, SPI);
				if (savedStatus == 1) {
					TolvenLogger.error("Error encountered while saving message: "+messageType, SurescriptsBean.class);
					return getCreator().generateErrorMessage(messageDoc ,"601","SCRIPT Validation Error: See free text for description","000");
				}
								
				if(messageType.equals("AddPrescriberResponse")){
					if(recievedMessage.getBody().getAddPrescriberResponse().getResponse().getApproved() != null){
						changeStatusOfPrescriber(recievedMessage.getHeader().getRelatesToMessageID() , "approved");
					} else{
						changeStatusOfPrescriber(recievedMessage.getHeader().getRelatesToMessageID() , "denied");
					}
				} else if(messageType.equals("AddPrescriberLocationResponse")){
					if(recievedMessage.getBody().getAddPrescriberLocationResponse().getResponse().getApproved() != null){
						changeStatusOfPrescriber(recievedMessage.getHeader().getRelatesToMessageID() , "approved");
					} else{
						changeStatusOfPrescriber(recievedMessage.getHeader().getRelatesToMessageID() , "approved");
					}
					if(!SPI.endsWith("1")){
						updatePrescriberSPI(recievedMessage.getHeader().getRelatesToMessageID(), SPI);
					}	
				} else if(messageType.equals("GetPrescriberResponse")){
					changeStatusOfGetPrescriber(recievedMessage.getHeader().getRelatesToMessageID());
				}
				
				message = message.replaceAll("'","''");
				String messageInfoId = "0";
				String menuDataId = "";
				if (messageType.equals("RxFill")){
					menuDataId = matchingAlgorithm(recievedMessage);	
					for(int i=0; i<10; i++) {
						System.out.println("Matching.....");
					}
				} else if(messageType.equals("RxChangeRequest")){
					menuDataId = matchingAlgorithm(recievedMessage);
					for(int i=0; i<10; i++) {
						System.out.println("Matching.....");
					}
				}
				
				ArrayList<String> results = null;
				String prescriberOrderNum = "0";
				if(messageType.equals("NewRx") && null != recievedMessage.getBody().getNewRx().getPrescriberOrderNumber()){
					prescriberOrderNum = recievedMessage.getBody().getNewRx().getPrescriberOrderNumber();
				}
				if(messageType.equals("RxFill") && null != recievedMessage.getBody().getRxFill().getPrescriberOrderNumber()){
					prescriberOrderNum = recievedMessage.getBody().getRxFill().getPrescriberOrderNumber(); 
				}
				if(messageType.equals("RxChangeRequest") && null != recievedMessage.getBody().getRxChangeRequest().getPrescriberOrderNumber() ){
					prescriberOrderNum = recievedMessage.getBody().getRxChangeRequest().getPrescriberOrderNumber(); 
				}
				if(messageType.equals("RefillRequest") && null != recievedMessage.getBody().getRefillRequest().getPrescriberOrderNumber()){
					prescriberOrderNum = recievedMessage.getBody().getRefillRequest().getPrescriberOrderNumber();
				}
				
				if(messageType.equals("Error") || messageType.equals("Status")){
					messageId = recievedMessage.getHeader().getRelatesToMessageID();
					results = persistSurescriptsMessages(recievedMessage.getHeader().getRelatesToMessageID(), messageType, message , DIRECTION_INCOMING , messageInfoId, prescriberOrderNum, accountId, recievedMessage.getHeader().getRelatesToMessageID());
					processStatusErrorMessages(messageType, recievedMessage.getHeader().getRelatesToMessageID());
				} else if(messageType.equals("RxFill") || messageType.equals("RxChangeRequest")){
					results = persistSurescriptsMessages(recievedMessage.getHeader().getMessageID(), messageType, message, DIRECTION_INCOMING,
							messageInfoId, prescriberOrderNum, accountId, null);
					changeStatusOfNewRxMessage(menuDataId , messageType);
				} else if (messageType.equals("RefillRequest")) {
					MenuData mdAccount = getMdFromsSpi(SPI);
					results = persistSurescriptsMessages(recievedMessage.getHeader().getMessageID(), messageType, message , DIRECTION_INCOMING , messageInfoId , prescriberOrderNum, String.valueOf(mdAccount.getAccount().getId()), null);
				} else{
					results = persistSurescriptsMessages(recievedMessage.getHeader().getMessageID(), messageType, message , DIRECTION_INCOMING , messageInfoId , prescriberOrderNum, accountId, null);
				}
				if(results.get(0).equals("0")){
					return getCreator().generateErrorMessage(messageDoc ,"900","Message is a duplicate","220");
				}
				
				if(messageType.equals("RefillRequest")){
					if(recievedMessage.getBody().getRefillRequest().getMedicationPrescribed().getRefills() != null && 
							null != recievedMessage.getBody().getRefillRequest().getMedicationPrescribed().getRefills().getQuantity()){
						if(recievedMessage.getBody().getRefillRequest().getMedicationPrescribed().getRefills().getQuantity().length() > 3
								|| recievedMessage.getBody().getRefillRequest().getMedicationPrescribed().getRefills().getQuantity().contains(".")){
							TolvenLogger.info("Invalid Refill Quantity. Error message will be sent to SureScripts.", SurescriptsBean.class);
							return getCreator().generateErrorMessage(messageDoc ,"900","DRU refill quantity is invalid.","144");
						}
					}
				}
				
				/* Method to do the scheduler's job */
				if (!messageType.equals("GetPrescriberResponse") && !messageType.equals("DirectoryDownloadResponse")) {
					makeChangesInApplication(recievedMessage , messageType , message , messageId,privateKey);
				}
				
				if (messageType.equals("Error") && !recievedMessage.getHeader().getMessageID().equals("0")) {
					return getCreator().generateStatusMessage(messageDoc);
				} else if(!messageType.equals("Status") && !messageType.equals("Error")) {
					return getCreator().generateStatusMessage(messageDoc);
				} else {
					return "Status / Error received.";
				}
					
			} else{
				return getCreator().generateErrorMessage(messageDoc ,"601","SCRIPT Validation Error: See free text for description","000");
			}
		} else{
			return getCreator().generateErrorMessage(messageDoc ,"601","SCRIPT Validation Error: See free text for description","000");
		}
	}
	
	/**
	 * Saves the message to file system.
	 * @param messageContent
	 * @param fileName
	 * @param messageType
	 * @param SPI
	 * @return
	 */
	private int saveMessageToFileSystem(String messageContent, String fileName, String messageType, String SPI) {
		
		final String messagePath = "eprescription.surescripts.messages.directory";
		File myDir = new File(tproperties.getProperty(messagePath)+"/inbox/");
		myDir.mkdirs();
		File statusDir = new File(tproperties.getProperty(messagePath)+"/outbox/status/");
		statusDir.mkdirs();
		File errorDir = new File(tproperties.getProperty(messagePath)+"/outbox/error/");
		errorDir.mkdirs();
		File statusInbDir = new File(tproperties.getProperty(messagePath)+"/inbox/status/");
		statusInbDir.mkdirs();
		File verifyInbDir = new File(tproperties.getProperty(messagePath)+"/inbox/verify/");
		verifyInbDir.mkdirs();
		File errorInbDir = new File(tproperties.getProperty(messagePath)+"/inbox/error/");
		errorInbDir.mkdirs();
		File spiGenerel = new File(tproperties.getProperty(messagePath)+"/inbox/SPI_general/");
		spiGenerel.mkdirs();
		if( myDir.exists() && myDir.isDirectory()){
			File xmlMessage = null;
			if(null != SPI){
				File folderSPIPath = new File(tproperties.getProperty(messagePath)+"/inbox/SPI_"+SPI+"/");
				folderSPIPath.mkdirs();
				if(folderSPIPath.exists() && folderSPIPath.isDirectory()){
					xmlMessage = new File(tproperties.getProperty(messagePath)+"/inbox/SPI_"+SPI+"/"+fileName);
				}	
			}else{
				if(messageType.equals("Status")){
					xmlMessage = new File(tproperties.getProperty(messagePath)+"/inbox/status/"+fileName);
				}else if(messageType.equals("Error")){
					xmlMessage = new File(tproperties.getProperty(messagePath)+"/inbox/error/"+fileName);
				}else if(messageType.equals("Verify")){
					xmlMessage = new File(tproperties.getProperty(messagePath)+"/inbox/verify/"+fileName);
				}else{
					xmlMessage = new File(tproperties.getProperty(messagePath)+"/inbox/SPI_general/"+fileName);
				}
			}
			Writer output;
			try {
				output = new BufferedWriter(new FileWriter(xmlMessage));
				output.write(messageContent);
				output.close();
				TolvenLogger.info("Message successfully saved in file system. MessgeType- "+messageType, SurescriptsBean.class);
			} catch (IOException e) {
				return 1;
			}
		}
		return 0;
	}

	private void makeChangesInApplication(MessageType recievedMessage , String messageType,
			String message,String messageId,PrivateKey privateKey) {
		templateId = "docclin/evn/responseTemplate";
	 	TolvenUser tUser = null;//login(tproperties.getProperty("eprescription.surescripts.masteruser"),tproperties.getProperty("eprescription.surescripts.masterpassword"));
	 	AccountUser accUser = retrieveAccountIdFromMessage(messageType , message);
	 	if(accUser == null || accUser.getAccount() == null){
	 		if(messageType.equals("Status") || messageType.equals("Error"))
	 			changeStatusOfProcessedMessage(recievedMessage.getHeader().getRelatesToMessageID(), messageType, "processed");
	 		else
	 			changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), messageType, "processed");
	 		return;
	 	}
	 	List<AccountUser> accountUsersList = getActivationBean().findUserAccounts(tUser);
		for(AccountUser acUser: accountUsersList) {
			if (accUser.getAccount().getId() != 0) {
				if (accUser.getAccount().getId() == acUser.getAccount()
						.getId()) {
					accUser = acUser;
					setAccountUser(acUser);
					accountType = getAccountBean().findAccountTypebyKnownType("echr");
					accUser.getAccount().setAccountType(accountType);
					break;
				}
			}
		}
		 
		try{
			if(messageType.equals("Error")){
				if (isRefillResponse(recievedMessage.getHeader().getRelatesToMessageID()) ) {
					MessageType msgtyp = getRefillRequestMessage(recievedMessage.getHeader().getRelatesToMessageID());
					
					// Find out the md of presently pending if any and remove it before posting error message.
					removeRefillRequestFromActivityList(msgtyp.getHeader().getMessageID());
					
					if (msgtyp != null) {
						postRefillRequestInActivityList(msgtyp, "Error",privateKey);
					}
					
				} else { // NewRx flow.
					if(message.contains("SPI not found in SureScripts directory")) {
						TolvenLogger.error("Create new event: Get Prescriber Returned Error.", SurescriptsBean.class);
					} else{
						mdPrior = getEpBean().getMdFromMessageId(recievedMessage.getHeader().getRelatesToMessageID());
						if(null != mdPrior.getAccount()){
							MenuData mdNew = null;
							mdNew = getEpBean().genTRIMEvent(mdPrior, accUser, "reviseActive", now, "Error",privateKey);
							TolvenLogger.info("Create new event: " + mdPrior.getPath(), SurescriptsBean.class);
							// Placing success message on Prescriber/Patient Activity list.
							getTrimMsgBean().submit(mdNew, accUser,privateKey);
						}
						getEpBean().retrieveMdFromRefillResponseData(messageId , "Error");
					}	
					if (recievedMessage.getHeader().getMessageID().equals("0")) {
						changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), messageType, "processed");
					}
				}
				
				
			} else if(messageType.equals("Status")){
				if (isRefillResponse(recievedMessage.getHeader().getRelatesToMessageID())) {
					if (recievedMessage.getBody().getStatus().getCode().equals("010")) {
						mdPrior = getEpBean().getMdFromMessageId(recievedMessage.getHeader().getRelatesToMessageID());
						if (recievedMessage.getBody().getStatus().getCode().equals("010")) {
							if(null != mdPrior.getAccount()){
							MenuData mdNew = null;
							mdNew = getEpBean().genTRIMEvent(mdPrior, accUser, "reviseActive", now, "Status",privateKey);
							TolvenLogger.info("Create new event: " + mdPrior.getPath(), SurescriptsBean.class);
							// Placing success message on Prescriber/Patient Activity list.
							getTrimMsgBean().submit(mdNew, accUser,privateKey);
							}	
							getEpBean().retrieveMdFromRefillResponseData(messageId , "Status");
							changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), messageType, "processed");
						}
					} else {
						// Asynchronous communication.
						MessageType msgtyp = getRefillRequestMessage(recievedMessage.getHeader().getRelatesToMessageID());
						if (msgtyp != null) {
							postRefillRequestInActivityList(msgtyp, "Pending",privateKey);
							TolvenLogger.info("REFREQ with Pending status posted on prescriber activity list", SurescriptsBean.class);
						}
					}
					
				} else { // NewRx flow.
					mdPrior = getEpBean().getMdFromMessageId(recievedMessage.getHeader().getRelatesToMessageID());
					if (recievedMessage.getBody().getStatus().getCode().equals("010")) {
						if(null != mdPrior.getAccount()){
						MenuData mdNew = null;
						mdNew = getEpBean().genTRIMEvent(mdPrior, accUser, "reviseActive", now, "Status",privateKey);
						TolvenLogger.info("Create new event: " + mdPrior.getPath(), SurescriptsBean.class);
						// Placing success message on Prescriber/Patient Activity list.
						getTrimMsgBean().submit(mdNew, accUser,privateKey);
						}	
						getEpBean().retrieveMdFromRefillResponseData(messageId , "Status");
						changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), messageType, "processed");
					} else if (getMessageTypeFromErrorStatus(recievedMessage.getHeader().getRelatesToMessageID()) != null &&
							getMessageTypeFromErrorStatus(recievedMessage.getHeader().getRelatesToMessageID()).equals(ValidMessageTypes.UpdatePrescriberLocation.toString())) {
						if(null != mdPrior.getAccount()){
							MenuData mdNew = null;
							mdNew = getEpBean().genTRIMEvent(mdPrior, accUser, "reviseActive", now, "Status",privateKey);
							TolvenLogger.info("Create new event: " + mdPrior.getPath(), SurescriptsBean.class);
							// Placing success message on Prescriber/Patient Activity list.
							getTrimMsgBean().submit(mdNew, accUser,privateKey);
						}	
						getEpBean().retrieveMdFromRefillResponseData(messageId , "Status");
						changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), messageType, "processed");
					}
				}
				
			}  else if (messageType.equals("Verify")) {
				
				if (isRefillResponse(recievedMessage.getHeader().getRelatesToMessageID()) ) {
					MessageType msgtyp = getRefillRequestMessage(recievedMessage.getHeader().getRelatesToMessageID());
					// Find out the md of presently pending if any and remove it before posting error message.
					removeRefillRequestFromActivityList(msgtyp.getHeader().getMessageID());
					
				} 
				mdPrior = getEpBean().getMdFromMessageId(recievedMessage.getHeader().getRelatesToMessageID());
				if(null != mdPrior.getAccount()){
					MenuData mdNew = null;
					mdNew = getEpBean().genTRIMEvent(mdPrior, accUser, "reviseActive", now, "Status",privateKey);
					TolvenLogger.info("Create new event: " + mdPrior.getPath(), SurescriptsBean.class);
					getTrimMsgBean().submit(mdNew, accUser,privateKey);
					getEpBean().retrieveMdFromRefillResponseData(messageId , "Status");
					changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), messageType, "processed");
//					if(null != mdPrior.getAccount()){
//						MenuData mdNew = null;
//						mdNew = getEpBean().genTRIMEvent(mdPrior, accUser, "reviseActive", now, "Status");
//						TolvenLogger.info("Verify-Create new event: " + mdPrior.getPath(), SurescriptsBean.class);
//						getTrimMsgBean().submit(mdNew, accUser);
				}	
			} else if(messageType.equals("CancelRxResponse")){
				mdPrior = getEpBean().retrieveMenuData(recievedMessage.getHeader().getRelatesToMessageID(), "CancelRXResponse Received");
				if(null != mdPrior.getAccount()){
					TolvenLogger.info("Create new event: " + mdPrior.getPath(), SurescriptsBean.class);
					// Placing success message on Prescriber/Patient Activity list.
					getTrimMsgBean().submit(mdPrior, accUser,privateKey);
				}
				changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), messageType, "processed");
			} else if (messageType.equals("AddPrescriberResponse")) {
				String SPI = null;
				for(int i=0; i< recievedMessage.getBody().getAddPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++){
		        	  if(recievedMessage.getBody().getAddPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")){
		        		  SPI = recievedMessage.getBody().getAddPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue();
		        	  }
	        	 }	  
				// Capturing the MenuData based on the messageId
				mdPrior = getEpBean().getMdFromMessageId(recievedMessage.getHeader().getRelatesToMessageID());
				if(null != mdPrior.getAccount()){
					MenuData mdNew = null;
					mdNew = getEpBean().genTRIMEvent(mdPrior, accUser, "reviseActive", now, SPI,privateKey);
					TolvenLogger.info("Create new event: " + mdPrior.getPath(), SurescriptsBean.class);
					// Placing success message on Prescriber list.
					getTrimMsgBean().submit(mdNew, accUser,privateKey);
				}
				changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), messageType, "processed");
			} else if (messageType.equals("AddPrescriberLocationResponse")){
				if (null != recievedMessage.getBody().getAddPrescriberLocationResponse().getResponse().getApproved()) {
					String SPI = null;
					for(int i=0; i< recievedMessage.getBody().getAddPrescriberLocationResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++){
			        	  if(recievedMessage.getBody().getAddPrescriberLocationResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")){
			        		  SPI = recievedMessage.getBody().getAddPrescriberLocationResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue();
			        	  }
		        	 }	  
					// Capturing the MenuData based on the messageId
					mdPrior = getEpBean().getMdFromMessageId(recievedMessage.getHeader().getRelatesToMessageID());
					if(null != mdPrior.getAccount()){
					MenuData mdNew = null;
					mdNew = getEpBean().genTRIMEvent(mdPrior, accUser, "reviseActive", now, SPI,privateKey);
					TolvenLogger.info("Create new event: " + mdPrior.getPath(), SurescriptsBean.class);
					// Placing success message on Prescriber Location list.
					getTrimMsgBean().submit(mdNew, accUser,privateKey);
					}
				}
				changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), messageType, "processed");
			} else if(messageType.equals("RefillRequest")) {
				postRefillRequestInActivityList(recievedMessage, "New",privateKey);
			} else if (messageType.equals("RxFill")) {
				responseRelation.getAct().getObservation().getValues()
						.get(0).getST().setValue("RxFill");
				changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), messageType, "processed");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes REFREQ with messageId from activity list. 
	 * @param messageId
	 */
	@SuppressWarnings("unchecked")
	public void removeRefillRequestFromActivityList(String messageId) {
		try {
			String qs = null;
			Query query = null;
			List<MenuData> refillRequestMd = null;
			qs = String.format(Locale.US, "SELECT md FROM MenuData md WHERE md.string06 = :mid and md.string03 = 'Pending' and md.status = 'NEW'" +
					"and (md.deleted is null)");
			query = em.createQuery( qs );
			query.setParameter("mid", messageId);
			if(null != query.getResultList() && query.getResultList().size() > 0){
				refillRequestMd = (List<MenuData>)query.getResultList();
				em.flush();
				for (MenuData md: refillRequestMd) {
					if (md != null && md.getStatus() == Status.NEW) {
						try {
							menuBean.removeReferencingMenuData( md.getAccount().getId(), md.getDocumentId(), true);
							TolvenLogger.info("Removed the RefillRequest automatically. ", this.getClass());
						} catch (Exception e) {
							TolvenLogger.error("Exception occured during removing REFREQ. ", this.getClass());
							continue;
						}
					}
					em.flush();
				}
			}
			
		} catch (Exception e) {
			TolvenLogger.error("Exception occured during findMenuDataOfPendingRefillRequest. ", this.getClass());
			e.printStackTrace();
		}
		
	}

	/**
	 * Method to get the MenuData based on SPI id of prescriber.
	 * @param SPI
	 * @return
	 */
	public MenuData getMdFromsSpi(String spi) {
		Query query = em.createQuery("SELECT md  FROM MenuData md WHERE md.status = 'ACTIVE' and md.pqUnits03 = :spi");
		query.setParameter("spi", spi );
		MenuData resultMd = new MenuData();
		if(query.getResultList().size() > 0)
			resultMd = (MenuData) query.getResultList().get(0);
		return resultMd;
	}

	private void changeStatusOfGetPrescriber(String relatesToMessageID) {
		String qs = null;
		Query query = null;
		MessageDetails msgDetails = null;
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :p AND md.status = 'new' AND md.direction = 'outgoing'");
		query = em.createQuery( qs );
		query.setParameter( "p", relatesToMessageID);
		msgDetails =  ((MessageDetails)query.getResultList().get(0));
		msgDetails.setStatus("success");
		em.merge(msgDetails);
		
	}

	/**
	 * Method to change the status of the NewRx Message in the message_details table
	 * @param menuDataId
	 */
	private void changeStatusOfNewRxMessage(String menuDataId , String messageType) {
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT msgInfo FROM MessageInfos msgInfo WHERE msgInfo.prescOrderNum = :pod ");
		query = em.createQuery( qs );
		query.setParameter( "pod", menuDataId);
		if(null != query.getResultList()){
			MessageInfos msgInfo = (MessageInfos)query.getResultList().get(0);
			Query newQuery = null;
			String qsNew = null;
			qsNew = String.format(Locale.US, "SELECT msgdtl FROM MessageDetails WHERE msgdtls.msgInfoId = :id ");
			newQuery = em.createQuery( qsNew );
			newQuery.setParameter( "msgId", msgInfo.getId());
			if(null != newQuery.getResultList()){
				MessageDetails msgDtls = (MessageDetails)newQuery.getResultList().get(0);
					if(messageType.equals("RxFill"))
						msgDtls.setStatus("active");
					else
						msgDtls.setStatus("changed");
					
					em.persist(msgDtls);
			}
		
		}
	}

	/**
	 * Method to persist message related information in the message_info table
	 * @param SPI
	 * @param ncpdpid
	 * @param lastName
	 * @param firstName
	 * @param dateOfBirth
	 * @param gender
	 * @param drugDescription
	 * @param value
	 * @return
	 */
	public String persistSurescriptsInfo(String SPI, String ncpdpid,
			String lastName, String firstName, String dateOfBirth,
			GenderType gender, String drugDescription, String drugQuantity, String prescriberOrderNumber) {
		MessageInfos messageInfo = new MessageInfos();
		if(!StringUtils.isBlank(SPI))
			messageInfo.setSpi(Long.parseLong(SPI));
		if(!StringUtils.isBlank(ncpdpid))
			messageInfo.setNcpdpid(Long.parseLong(ncpdpid));
		messageInfo.setPatientLastName(lastName);
		messageInfo.setPatientFirstName(firstName);
		messageInfo.setPatientDOB(new Timestamp(Long.parseLong(dateOfBirth.trim())));
		if(gender != null)
			messageInfo.setPatientGender(gender.name());
		messageInfo.setMedicineDescription(drugDescription);
		messageInfo.setDrugQuantity(drugQuantity);
		messageInfo.setPrescOrderNum(Long.parseLong(prescriberOrderNumber));
		em.persist(messageInfo);
		
	return messageInfo.getId().toString();
	}

	/**
	 * Matching Algorithm method to find out the previous parent message
	 * @param recievedMessage
	 * @return
	 */
	public String matchingAlgorithm(MessageType recievedMessage) {
		String menuDataId = "";
		String SPI = null;
		String ncpdpid = null;
		String patLastName = null;
		String patFirstName = null;
		String patDob = null;
		String patGender = null;
		String medDesc = null;
		String medQnty = null;
		String prescriberOrderNumber = null;
		if(recievedMessage.getBody().getRxChangeRequest() != null){
				if(null != recievedMessage.getBody().getRxChangeRequest().getPrescriberOrderNumber()
						&& !recievedMessage.getBody().getRxChangeRequest().getPrescriberOrderNumber().equals("")){
					prescriberOrderNumber = recievedMessage.getBody().getRxChangeRequest().getPrescriberOrderNumber();
					return prescriberOrderNumber;
				}
			    for(int i=0; i< recievedMessage.getBody().getRxChangeRequest().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++){
		      	  if(recievedMessage.getBody().getRxChangeRequest().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")){
		      		  SPI = recievedMessage.getBody().getRxChangeRequest().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue();
		      	  }
			    }	
				for(int i=0; i< recievedMessage.getBody().getRxChangeRequest().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().size(); i++){
		      	  if(recievedMessage.getBody().getRxChangeRequest().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("NCPDPID")){
		      		  ncpdpid = recievedMessage.getBody().getRxChangeRequest().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().get(i).getValue();
		      	  }
			    }
				if(null != recievedMessage.getBody().getRxChangeRequest().getPatient()){
					if(null != recievedMessage.getBody().getRxChangeRequest().getPatient().getName()){
						if(null != recievedMessage.getBody().getRxChangeRequest().getPatient().getName().getLastName()
								&& !recievedMessage.getBody().getRxChangeRequest().getPatient().getName().getLastName().equals("")){
							patLastName = recievedMessage.getBody().getRxChangeRequest().getPatient().getName().getLastName();
						}
						if(null != recievedMessage.getBody().getRxChangeRequest().getPatient().getName().getFirstName()
								&& !recievedMessage.getBody().getRxChangeRequest().getPatient().getName().getFirstName().equals("")){
							patFirstName = recievedMessage.getBody().getRxChangeRequest().getPatient().getName().getFirstName();
						}	
					}
					if(null != recievedMessage.getBody().getRxChangeRequest().getPatient().getDateOfBirth()
							&& !recievedMessage.getBody().getRxChangeRequest().getPatient().getDateOfBirth().equals("")){
						patDob = recievedMessage.getBody().getRxChangeRequest().getPatient().getDateOfBirth();
					}
					if(null != recievedMessage.getBody().getRxChangeRequest().getPatient().getGender()){
						if(null != recievedMessage.getBody().getRxChangeRequest().getPatient().getGender().name()
								&& !recievedMessage.getBody().getRxChangeRequest().getPatient().getGender().name().equals("")){
							patGender = recievedMessage.getBody().getRxChangeRequest().getPatient().getGender().name();
						}	
					}	
				}
				if(null != recievedMessage.getBody().getRxChangeRequest().getMedicationPrescribed()){
					if(null != recievedMessage.getBody().getRxChangeRequest().getMedicationPrescribed().getDrugDescription()
							&& !recievedMessage.getBody().getRxChangeRequest().getMedicationPrescribed().getDrugDescription().equals("")){
						medDesc = recievedMessage.getBody().getRxChangeRequest().getMedicationPrescribed().getDrugDescription();
					}
					if(null != recievedMessage.getBody().getRxChangeRequest().getMedicationPrescribed().getQuantity()){
						if(null != recievedMessage.getBody().getRxChangeRequest().getMedicationPrescribed().getQuantity().getValue()
								&& !recievedMessage.getBody().getRxChangeRequest().getMedicationPrescribed().getQuantity().getValue().equals("")){
							medQnty = recievedMessage.getBody().getRxChangeRequest().getMedicationPrescribed().getQuantity().getValue();
						}	
					}
				}
				
		}else if(recievedMessage.getBody().getRxFill() != null){
				if(null != recievedMessage.getBody().getRxFill().getPrescriberOrderNumber()
						&& !recievedMessage.getBody().getRxFill().getPrescriberOrderNumber().equals("")){
					prescriberOrderNumber = recievedMessage.getBody().getRxFill().getPrescriberOrderNumber();
					return prescriberOrderNumber;
				}
			 for(int i=0; i< recievedMessage.getBody().getRxFill().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++){
		      	  if(recievedMessage.getBody().getRxFill().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")){
		      		  SPI = recievedMessage.getBody().getRxFill().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue();
		      	  }
			    }	
				for(int i=0; i< recievedMessage.getBody().getRxFill().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().size(); i++){
		      	  if(recievedMessage.getBody().getRxFill().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("NCPDPID")){
		      		  ncpdpid = recievedMessage.getBody().getRxFill().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().get(i).getValue();
		      	  }
			    }
				if(null != recievedMessage.getBody().getRxFill().getPatient()){
					if(null != recievedMessage.getBody().getRxFill().getPatient().getName()){
						if(null != recievedMessage.getBody().getRxFill().getPatient().getName().getLastName()
								&& !recievedMessage.getBody().getRxFill().getPatient().getName().getLastName().equals("")){
							patLastName = recievedMessage.getBody().getRxFill().getPatient().getName().getLastName();
						}
						if(null != recievedMessage.getBody().getRxFill().getPatient().getName().getFirstName()
								&& !recievedMessage.getBody().getRxFill().getPatient().getName().getFirstName().equals("")){
							patFirstName = recievedMessage.getBody().getRxFill().getPatient().getName().getFirstName();
						}	
					}
					if(null != recievedMessage.getBody().getRxFill().getPatient().getDateOfBirth()
							&& !recievedMessage.getBody().getRxFill().getPatient().getDateOfBirth().equals("")){
						patDob = recievedMessage.getBody().getRxFill().getPatient().getDateOfBirth();
					}
					if(null != recievedMessage.getBody().getRxFill().getPatient().getGender()){
						if(null != recievedMessage.getBody().getRxFill().getPatient().getGender().name()
								&& !recievedMessage.getBody().getRxFill().getPatient().getGender().name().equals("")){
							patGender = recievedMessage.getBody().getRxFill().getPatient().getGender().name();
						}	
					}	
				}
				if(null != recievedMessage.getBody().getRxFill().getMedicationPrescribed()){
					if(null != recievedMessage.getBody().getRxFill().getMedicationPrescribed().getDrugDescription()
							&& !recievedMessage.getBody().getRxFill().getMedicationPrescribed().getDrugDescription().equals("")){
						medDesc = recievedMessage.getBody().getRxFill().getMedicationPrescribed().getDrugDescription();
					}
					if(null != recievedMessage.getBody().getRxFill().getMedicationPrescribed().getQuantity()){
						if(null != recievedMessage.getBody().getRxFill().getMedicationPrescribed().getQuantity().getValue()
								&& !recievedMessage.getBody().getRxFill().getMedicationPrescribed().getQuantity().getValue().equals("")){
							medQnty = recievedMessage.getBody().getRxFill().getMedicationPrescribed().getQuantity().getValue();
						}	
					}
				}
		}
		
		Statement st;
		ResultSet rs = null;
		this.tolvenCon = connectToPostgres();
		try {
			st = tolvenCon.createStatement();
			StringBuffer queryBuffer = new StringBuffer("SELECT presc_ordr_num FROM surescripts.message_info WHERE ");
			
			if(null != SPI && !SPI.equals(""))
				queryBuffer.append("spi = "+SPI+" AND ");
			
			if(null != ncpdpid && !ncpdpid.equals(""))
				queryBuffer.append("ncpdpid = "+ncpdpid+" AND ");
			
			if(null != patLastName && !patLastName.equals(""))
				queryBuffer.append("pat_last_name = '"+patLastName+"' AND ");
			
			if(null != patFirstName && !patFirstName.equals(""))
				queryBuffer.append("pat_first_name = '"+patFirstName+"' AND ");
			
			if(null != medDesc && !medDesc.equals(""))
				queryBuffer.append("med_desc = '"+medDesc+"' AND ");
			
			if(null != medQnty && !medQnty.equals(""))
				queryBuffer.append("drug_qnty = "+medQnty+" AND ");
			
			if(null != patGender && !patGender.equals(""))
				queryBuffer.append("pat_gender = '"+patGender+"' AND ");
			
			if(null != patDob && !patDob.equals("")){
				String patDobFormatted = "";
				patDobFormatted = patDob.substring(0,4)+"-"+patDob.substring(4,6)+"-"+patDob.substring(6);
				queryBuffer.append("pat_dob = '"+patDobFormatted+"'");
			}
				
			String query = queryBuffer.toString();
			if(query.endsWith("AND ")){
				query = query.substring(0,query.length() - 4);
			}
			
			rs = st.executeQuery(query);
				while(rs.next()){
					menuDataId = String.valueOf(rs.getBigDecimal("presc_ordr_num"));
				}
			
				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnectFromPostgres();	
		return menuDataId;
	}


	private class ParseXML {
	    private JAXBContext jc;
	    
	    protected String getParsePackageName(){
	    	return "org.tolven.surescripts";
	    }
	    
	    protected JAXBContext getJAXBContext() throws JAXBException {
	        if (jc==null) {
	            jc = JAXBContext.newInstance(getParsePackageName(), getClass().getClassLoader());
	        }
	        return jc;
	    }
	    
	    protected Unmarshaller getUnmarshaller() throws JAXBException {
	        return getJAXBContext().createUnmarshaller();
	    }

	    protected Marshaller getMarshaller() throws JAXBException {
	        Marshaller m = getJAXBContext().createMarshaller();
	        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
	        m.setProperty(  "com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
	        return m;
	    }
	}
	
	
	/**
	 * Method to generate the latest date in the NIST format
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getPresentDate() {
		Date d = new Date();
		//2008-02-20T16:10:34.0Z
		StringBuffer date = new StringBuffer();
		int year = d.getYear() + 1900;
		int month = d.getMonth() + 1;
		int day = d.getDate();
		int hour = d.getHours();
		int minutes = d.getMinutes();
		int seconds = d.getSeconds();
		String monthString = String.valueOf(month);
		String dayString = String.valueOf(day);
		String hourString = String.valueOf(hour);
		String minuteString = String.valueOf(minutes);
		String secondString = String.valueOf(seconds);
		if(month <= 9){
			monthString = "0"+String.valueOf(month);
		}
		if(day <= 9){
			dayString = "0"+String.valueOf(day);
		}
		if(hour <= 9){
			hourString = "0"+String.valueOf(hour);
		}
		if(minutes <= 9){
			minuteString = "0"+String.valueOf(minutes);
		}
		if(seconds <= 9){
			secondString = "0"+String.valueOf(seconds);
		}
		date.append(year).append('-').append(monthString)
			.append('-').append(dayString).append('T')
			.append(hourString).append(':').append(minuteString)
			.append(':').append(secondString).append(".0Z");
		return date.toString();
	}
	
	public ErrorStatusMessageCreator getCreator() {
		return creator;
	}

	public void setCreator(ErrorStatusMessageCreator creator) {
		this.creator = creator;
	}
		
	/*
	 * Method to generate the getDirectory Download message
	 * @return
	 */
	public String generateDirectoryDownloadMessage(PrivateKey privateKey){
		MessageType msg = new MessageType();
		/*Coding for header performed here*/
		HeaderType header = new HeaderType();
		header.setTo("mailto:SSSDIR.dp@surescripts.com");
		header.setFrom("mailto:TOLVEN.dp@surescripts.com");
		String messageId = generateMessageId(getPresentDate());
		header.setMessageID(messageId);
		header.setSentTime(getPresentDate());
		SecurityType security = new SecurityType();
		UsernameTokenType username = new UsernameTokenType();
		username.setUsername(tproperties.getProperty("eprescription.surescripts.adminconsole.username"));
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeDirectoryCredentials(tproperties.getProperty("eprescription.surescripts.adminconsole.password")));
		username.setPassword(password);
		username.setCreated(getPresentDate());
		security.setUsernameToken(username);
		header.setSecurity(security);
		
		msg.setHeader(header);
		BodyType body = new BodyType();
		DirectoryDownload directoryDownload = new DirectoryDownload();
		directoryDownload.setAccountID("725");
		directoryDownload.setVersionID("4");
		TaxonomyType taxonomy = new TaxonomyType();
		taxonomy.setTaxonomyCode("183500000X");
		directoryDownload.setTaxonomy(taxonomy);
		body.setDirectoryDownload(directoryDownload);
		msg.setBody(body);
		msg.setVersion("1.0");
		/* Marshalling Process on the way */
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		SureScriptsCommunicator comm = new SureScriptsCommunicator();
		String locationOfMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/");
		File messageOutbox =  new File(locationOfMessage);
		if (!messageOutbox.exists())
			messageOutbox.mkdirs();
		locationOfMessage = locationOfMessage + "DirectoryDownload_"+msg.getHeader().getMessageID()+".xml";
		String result = comm.createMessageFile(msg, locationOfMessage, "0",privateKey);
		
		String downloadFileName = "";
		if(null != result && !result.equals("") && result.contains("<URL>"))
			downloadFileName = result.substring(result.indexOf("<URL>") + 5 , result.indexOf("</URL>"));
		
		return downloadFileName;
		
	}
	/**
	 * Generates message id.
	 * 
	 * @param plain
	 * @return
	 */
	public static String generateMessageId(String plain) {
		byte[] cipher = new byte[35];
		String messageId = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(plain.getBytes());
			cipher = md5.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < cipher.length; i++) {
				String hex = Integer.toHexString(0xff & cipher[i]);
				if (hex.length() == 1)
					sb.append('0');
				sb.append(hex);
			}
			StringBuffer pass = new StringBuffer();
			pass.append(sb.substring(0, 6));
			pass.append("H");
			pass.append(sb.substring(6, 11));
			pass.append("H");
			pass.append(sb.substring(11, 21));
			pass.append("H");
			pass.append(sb.substring(21));
			messageId = new String(pass);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return messageId;
	}
	
	/**
	 * Method to retrieve the name of the directorydownload zip file
	 * @param messageId
	 * @return String
	 */
	public String retrieveDirectoryDownloadFileName(String messageId){
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :mid ");
		query = em.createQuery( qs );
		query.setParameter( "mid", messageId);
		String fileName = "";
		if(null != query.getResultList()){
			fileName = ((MessageDetails)query.getResultList().get(0)).getMessage();
		}
		return fileName;
	}
	
	/**
	 * Method used to fetch the message based on the Relates to messageId.
	 * @param messageId
	 * @return Message
	 */
	public String getMessageData(String messageId){
		String qs = null;
		Query query = null;
		String message = "";
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :mid AND md.messageType = 'Error'");
		query = em.createQuery( qs );
		query.setParameter("mid", messageId);
		if(null != query.getResultList()){
			message = ((MessageDetails)query.getResultList().get(0)).getMessage();
		}
		return message;
	}
	
	/**
	 * Method used to get the Error desc for REFRES message.
	 * @param messageId
	 * @return Message
	 */
	public String getRefillErrorDesc(String messageId) {
		String message = "";
		try {
			String qs = null;
			Query query = null;
			qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageType='Error' and " +
					"md.messageId = (SELECT messageId FROM MessageDetails msgDet WHERE msgDet.relatesToMessageId=:mid)");
			query = em.createQuery( qs );
			query.setParameter("mid", messageId);
			if(null != query.getResultList() && query.getResultList().size() > 0){
				message = ((MessageDetails)query.getResultList().get(0)).getMessage();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message;
	}
	
	
	/**
	 * Checks whether the Status/Error's relatesTomessageId corresponds to a REFRES.
	 * @param messageId
	 * @return true if it is a REFRES.
	 */
	public boolean isRefillResponse(String messageId) {
		String qs = null;
		Query query = null;
		String message = "";
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :mid AND md.messageType = 'RefillResponse'");
		query = em.createQuery( qs );
		query.setParameter("mid", messageId);
		if(null != query.getResultList() && query.getResultList().size() > 0){
			message = ((MessageDetails)query.getResultList().get(0)).getMessageType();
			TolvenLogger.info("MessageType corresponding to the messageId identified as: "+ message, SurescriptsBean.class);
			return true;
		}
		return false;
	}
	
	/**
	 * With the relatesToMessageId of Status/Error gets the corresponding REFRES and the 
	 * associated REFREQ
	 * @param relatesToMessageId
	 * @return MessaegeType REFREQ
	 * @author unni.s@cyrusxp.com
	 */
	public MessageType getRefillRequestMessage(String relatesToMessageId) {
		String qs = null;
		String qs1 = null;
		Query query = null;
		Query query1 = null;
		String message = "";
		String refillRequestMsgId = null;
		MessageType messageType = null;
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :mid AND md.messageType = 'RefillResponse'");
		query = em.createQuery( qs );
		query.setParameter("mid", relatesToMessageId);
		if(null != query.getResultList() && query.getResultList().size() > 0){
			message = ((MessageDetails)query.getResultList().get(0)).getMessage();
			if(message.contains("<RelatesToMessageID>")) {
				refillRequestMsgId = message.split("<RelatesToMessageID>")[1].split("</RelatesToMessageID>")[0].trim();
				TolvenLogger.info("REFREQ messageId: "+ refillRequestMsgId, SurescriptsBean.class);
			}
		}
		em.flush();
		
		if (refillRequestMsgId != null) {
			qs1 = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :mid AND md.messageType = 'RefillRequest'");
			query1 = em.createQuery( qs1 );
			query1.setParameter("mid", refillRequestMsgId);
			if(null != query1.getResultList() && query1.getResultList().size() > 0){
				message = ((MessageDetails)query1.getResultList().get(0)).getMessage();
				messageType = convertToMessageType(message);
			}
		}
		
		return messageType;
	}
	
	/**
	 * Converts the message from database to MessageType object
	 * @param message
	 * @return MessageType
	 */
	@SuppressWarnings("unchecked")
	private MessageType convertToMessageType(String message) {
		DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document messageDoc = null;
		DocumentBuilder builder = null;
		MessageType recievedMessage = null;
		if(message != null && !message.equals("") && message.trim().length() > 0){
			try {
				builder = factory.newDocumentBuilder();
				messageDoc = builder.parse(new InputSource(new StringReader(message)));
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
				return recievedMessage;
			} catch (SAXException e1) {
				e1.printStackTrace();
				return recievedMessage;
			} catch (IOException e1) {
				e1.printStackTrace();
				return recievedMessage;
			}
			messageDoc.getDocumentElement().normalize();
			if(messageDoc.getDocumentElement().getNodeName().equals("Message")){
				recievedMessage = new MessageType();
				JAXBElement< MessageType> messageJAXB = null;
				ParseXML parse = new ParseXML();
				try {
					Unmarshaller um = parse.getJAXBContext().createUnmarshaller();
					messageJAXB = (JAXBElement<MessageType>) um.unmarshal(messageDoc);
					recievedMessage = messageJAXB.getValue();
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}
		}
		TolvenLogger.info("Converting to MessageType Object.", SurescriptsBean.class);
		return recievedMessage;
	}
	
	/**
	 * Method to return to all messageTypes corresponding to a prescription based on prescriberOrderNum.
	 * @param prescriberOrderNum
	 * @return ArrayList<MessageType>
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<MessageType> retrievePatientPrescriptionReport(String prescriberOrderNum) {
		ArrayList<MessageType> msgType = new ArrayList<MessageType>();
		String qs = null;
		Query query = null;
		String message = "";
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.prescOrderNum = :pod ");
		query = em.createQuery( qs );
		query.setParameter( "pod", Long.parseLong(prescriberOrderNum));
		if(null != query.getResultList()){
			ArrayList<MessageDetails> messageDetailsList = (ArrayList<MessageDetails>)query.getResultList();
			int i=0;
			for(MessageDetails msgdtl : messageDetailsList){
				message = msgdtl.getMessage();
				MessageType msg = convertToMessageType(message);
				msgType.add(i, msg);
				i++;
			}
		}
		return msgType;
	}
	
	/**
     * Method to retrieve the
     * @param relatesToMessageId
     * @return
     */
    @SuppressWarnings("unchecked")
	public String getMessageTypeFromErrorStatus(String relatesToMessageId){
    	String qs = null;
		Query query = null;
		String messageType = null;
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :mid");
		query = em.createQuery( qs );
		query.setParameter( "mid", relatesToMessageId);
		if(null != query.getResultList()){
			ArrayList<MessageDetails> messageDetailsList = (ArrayList<MessageDetails>)query.getResultList();
			for(MessageDetails msgdtl : messageDetailsList){
				if(!msgdtl.getMessageType().equals("Error") && !msgdtl.getMessageType().equals("Status"))
					messageType = msgdtl.getMessageType();
			}
		}
       
        return messageType;
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
	 * @author mohammed
	 * Method to retrieve the account Information from the retrieved message
	 * @param array
	 * @return
	 */
	private AccountUser retrieveAccountIdFromMessage(String messageType , String message) {
		AccountUser accUser = new AccountUser();
		String qs = null;
		Query query = null;
		String spi = null;
		String relatedToMessageId = null;
		if(messageType.equals("RxFill")){
		 spi = message.substring(message.indexOf("<SPI>") + 5, message.indexOf("</SPI>")).trim();
		}else if(messageType.equals("RefillRequest")){
			spi = message.substring(message.indexOf("<SPI>") + 5, message.indexOf("</SPI>")).trim();	 
		}else if(messageType.equals("RxChangeRequest")){
			spi = message.substring(message.indexOf("<SPI>") + 5, message.indexOf("</SPI>")).trim();  
		}else if(messageType.equals("CancelRxResponse")){
			relatedToMessageId = message.substring(message.indexOf("<RelatesToMessageID>") + 20, message.indexOf("</RelatesToMessageID>")).trim();
		}else if(messageType.equals("Status")){
			relatedToMessageId = message.substring(message.indexOf("<RelatesToMessageID>") + 20, message.indexOf("</RelatesToMessageID>")).trim();
		}else if(messageType.equals("Error")){
			relatedToMessageId = message.substring(message.indexOf("<RelatesToMessageID>") + 20, message.indexOf("</RelatesToMessageID>")).trim();
		}else if(messageType.equals("AddPrescriberResponse")){
			relatedToMessageId = message.substring(message.indexOf("<RelatesToMessageID>") + 20, message.indexOf("</RelatesToMessageID>")).trim();  
		}else if(messageType.equals("AddPrescriberLocationResponse")){
			relatedToMessageId = message.substring(message.indexOf("<RelatesToMessageID>") + 20, message.indexOf("</RelatesToMessageID>")).trim();  
		}else if(messageType.equals("GetPrescriberResponse")){
			spi = message.substring(message.indexOf("<SPI>") + 5, message.indexOf("</SPI>")).trim();   
		}else if(messageType.equals("Verify")){
			relatedToMessageId = message.substring(message.indexOf("<RelatesToMessageID>") + 20, message.indexOf("</RelatesToMessageID>")).trim();  
		}
		
		if(null != relatedToMessageId){
			qs = String.format(Locale.US, "SELECT md FROM MenuData md WHERE md.string07 = :rmid AND md.string01 = 'Refill Request'");
			query = em.createQuery(qs);
			query.setParameter( "rmid", relatedToMessageId);
			if(query.getResultList().size() > 1){
				for(AccountUser acc : ((MenuData)query.getResultList().get(1)).getAccount().getAccountUsers()){
					accUser = acc;
					break;
				}
			}else{
				qs = String.format(Locale.US, "SELECT md FROM MenuData md WHERE md.string07 = :rmid");
				query = em.createQuery(qs);
				query.setParameter( "rmid", relatedToMessageId);
				if(query.getResultList().size() > 0){
					for(AccountUser acc : ((MenuData)query.getResultList().get(0)).getAccount().getAccountUsers()){
						accUser = acc;
						break;
					}
				}
			}	
		}else if(null != spi){
			qs = String.format(Locale.US, "SELECT md FROM MenuData md WHERE md.pqUnits03 = :spi");
			query = em.createQuery(qs);
			query.setParameter( "spi", spi);
			if(query.getResultList().size() > 0){
				for(AccountUser acc : ((MenuData)query.getResultList().get(0)).getAccount().getAccountUsers()){
					accUser = acc;
					break;
				}
			}
		}else{
			return accUser;
		}
		
		return accUser;
	}
	
	/**
	 * Method to retrieve Qual Description When QualCode is in hand
	 * @return
	 */
	public String retrieveDrugQualDescription(String code){
		String qualDesc = code;
    	String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT dr FROM DrugQualifier dr WHERE dr.qualcode = :code");
		query = em.createQuery( qs );
		query.setParameter( "code", code);
		if(query.getResultList() != null && query.getResultList().size() > 0){
			qualDesc = ((DrugQualifier)query.getResultList().get(0)).getQualDesc();
		}
    	return qualDesc;
	}
	
	/**
	 * Method to retrieve Qual Code when Qual Description in hand
	 * @return
	 */
	public String retrieveDrugQualCode(String desc){
		String qualCode = desc;
    	String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT dr FROM DrugQualifier dr WHERE dr.qualDesc = :desc");
		query = em.createQuery( qs );
		query.setParameter( "desc", desc);
		if(query.getResultList() != null && query.getResultList().size() > 0){
			qualCode = ((DrugQualifier)query.getResultList().get(0)).getQualcode();
		}
    	return qualCode;
	}
	
	/**
	 * Method to change the status of the processsed message
	 */
	@SuppressWarnings("unchecked")
	public void changeStatusOfProcessedMessage(String messageId , String messageType , String finalStatus){
		try {
			String qs = null;
			Query query = null;
			qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.status = 'new' AND md.direction = 'incoming' AND md.messageId = :msgId AND md.messageType = :msgType");
			query = em.createQuery( qs );
			query.setParameter( "msgId", messageId);
			query.setParameter( "msgType", messageType);
			if(null != query.getResultList()){
				ArrayList<MessageDetails> unprocessedUnits = (ArrayList<MessageDetails>)query.getResultList();
				for(MessageDetails msgDtl : unprocessedUnits){
					msgDtl.setStatus(finalStatus);
					em.merge(msgDtl);
					TolvenLogger.info("Changed status of "+messageType+" to "+ finalStatus, SurescriptsBean.class);
				}
			}
		} catch (Exception e) { 
			TolvenLogger.error("Exception occurred while changing status of "+messageType+" to "+ finalStatus, SurescriptsBean.class);
			e.printStackTrace();
		}	
	}
	
	/**
	 * @author mohammed
	 * Updates the prescriber SPI number in MenuData for AddPrescriberLocation.
	 * @return
	 */
	public boolean updatePrescriberSPI(String messageId , String spi){
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT md FROM MenuData md WHERE md.status = 'ACTIVE' AND md.string07 = :messageId");
		query = em.createQuery( qs );
		query.setParameter( "messageId", messageId);
		if(null != query.getResultList() && query.getResultList().size() > 0){
			MenuData md = (MenuData)query.getResultList().get(0);
			md.setPqUnits03(spi);
			em.merge(md);
			return true;
		}	
		return false;
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
	 * @return the propertyBean
	 */
	public TolvenPropertiesLocal getPropertyBean() {
		return propertyBean;
	}

	/**
	 * @param propertyBean the propertyBean to set
	 */
	public void setPropertyBean(TolvenPropertiesLocal propertyBean) {
		this.propertyBean = propertyBean;
	}

	/**
	 * @return the epBean
	 */
	public EPrescriptionLocal getEpBean() {
		return epBean;
	}

	/**
	 * @param epBean the epBean to set
	 */
	public void setEpBean(EPrescriptionLocal epBean) {
		this.epBean = epBean;
	}

	/**
	 * @return the trimMsgBean
	 */
	public TrimMessageLocal getTrimMsgBean() {
		return trimMsgBean;
	}

	/**
	 * @param trimMsgBean the trimMsgBean to set
	 */
	public void setTrimMsgBean(TrimMessageLocal trimMsgBean) {
		this.trimMsgBean = trimMsgBean;
	}
	
	/*
	 * This function is used to insert an observation value slot containing
	 * label and ST values
	 */
	/**
	 * @param labelValue
	 * @param stValue
	 * @return
	 */
	private ObservationValueSlot insertObservationValueSlot(String labelValue,
			String stValue) {

		TrimFactory trimFactory = new TrimFactory();
		ObservationValueSlot valueSlot = trimFactory
				.createObservationValueSlot();

		/* Setting label for each ST value slot as the tag name from message */
		LabelFacet label = trimFactory.createLabelFacet();
		label.setValue(labelValue);
		valueSlot.setLabel(label);

		/* Setting ST values */
		valueSlot.setST(trimFactory.createNewST(stValue));
		return valueSlot;
	}

	/**
	 * @return the accountBean
	 */
	public AccountDAOLocal getAccountBean() {
		return accountBean;
	}

	/**
	 * @param accountBean the accountBean to set
	 */
	public void setAccountBean(AccountDAOLocal accountBean) {
		this.accountBean = accountBean;
	}

	/**
	 * @return the activationBean
	 */
	public ActivationLocal getActivationBean() {
		return activationBean;
	}

	/**
	 * @param activationBean the activationBean to set
	 */
	public void setActivationBean(ActivationLocal activationBean) {
		this.activationBean = activationBean;
	}
	
	/* *//**
     * Login a user.
     * @param uid
     * @param password
     * @return The TolvenUser object for the logged in user.
     * @throws GeneralSecurityException 
     * @throws Exception
     *//*
    public TolvenUser login(String uid, String password) {
        // Login now
        TolvenLogger.info("Attempting to log in", SurescriptsBean.class);
        System.setProperty(JAAS_LOGIN_CONFIG_PROPERTY, "tolven.auth");
        UsernamePasswordHandler handler = new UsernamePasswordHandler(uid, password.toCharArray());
        try {
            lc = new LoginContext("tolvenLDAP", handler);
            lc.login();
            TolvenUser user = getActivationBean().loginUser(uid, new Date());
            TolvenLogger.info("User logged in as: "+ lc.getSubject().getPrincipals(), SurescriptsBean.class);
            this.uid = uid;
            return user;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to login: " + uid, ex);
        }
    }
    
    protected static class UsernamePasswordHandler implements CallbackHandler {
        String username;
        char[] password;

        public UsernamePasswordHandler(String username, char[] password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            int len = callbacks.length;
            Callback cb;
            for (int i = 0; i < len; i++) {
                cb = callbacks[i];
                if (cb instanceof NameCallback) {
                    NameCallback ncb = (NameCallback) cb;
                    ncb.setName(username);
                } else if (cb instanceof PasswordCallback) {
                    PasswordCallback pcb = (PasswordCallback) cb;
                    pcb.setPassword(password);
                } else {
                    throw new UnsupportedCallbackException(cb, "Unknown callback request");
                }
            }
        }
    }*/


	/**
	 * @return the tproperties
	 */
	public TolvenPropertiesLocal getTproperties() {
		return tproperties;
	}

	/**
	 * @param tproperties the tproperties to set
	 */
	public void setTproperties(TolvenPropertiesLocal tproperties) {
		this.tproperties = tproperties;
	}
	
	/**
	 * Method to retrieve MenuData from last name and first name
	 * @param lastName
	 * @param firstName
	 * @return
	 */
	public MenuData getMdFromPatientName(String lastName, String firstName){
		String qs = null;
		Query query = null;
		MenuData md = null;
		qs = String.format(Locale.US, "SELECT md  FROM MenuData md WHERE md.string02 = :f AND md.string01 = :l");
		query = em.createQuery( qs );
		query.setParameter( "f", firstName );
		query.setParameter( "l", lastName );
		if(null != query.getResultList() && query.getResultList().size() > 0){
			for(int i=0; i<query.getResultList().size(); i++){
				if(((MenuData)query.getResultList().get(i)).getPath().contains("echr:patient-")){
					md = (MenuData)query.getResultList().get(i);
					break;
				}	
			}
		}
		return md;
	}
	
	/**
	 * This method is to create a TS object
	 */
	private ObservationValueSlot insertObservationValueSlotTS(String labelValue,
			String stValue) {

		TrimFactory trimFactory = new TrimFactory();
		ObservationValueSlot valueSlot = trimFactory
				.createObservationValueSlot();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		try {
			/* Setting label for each ST value slot as the tag name from message */
			LabelFacet label = trimFactory.createLabelFacet();
			label.setValue(labelValue);
			valueSlot.setLabel(label);
			/* Setting ST values */
			valueSlot.setTS(trimFactory.createNewTS(df.parse(stValue)));
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return valueSlot;
	}
	
	/**
	 * This method is called upon for generating the Nightly Download Message
	 * @return
	 */
	public String generateNightlyDirectoryDownloadMessage(PrivateKey privateKey){

		MessageType msg = new MessageType();
		/*Coding for header performed here*/
		HeaderType header = new HeaderType();
		header.setTo("mailto:SSSDIR.dp@surescripts.com");
		header.setFrom("mailto:TOLVEN.dp@surescripts.com");
		String messageId = generateMessageId(getPresentDate());
		header.setMessageID(messageId);
		header.setSentTime(getPresentDate());
		SecurityType security = new SecurityType();
		UsernameTokenType username = new UsernameTokenType();
		username.setUsername(tproperties.getProperty("eprescription.surescripts.adminconsole.username"));
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeDirectoryCredentials(tproperties.getProperty("eprescription.surescripts.adminconsole.password")));
		username.setPassword(password);
		username.setCreated(getPresentDate());
		security.setUsernameToken(username);
		header.setSecurity(security);
		
		msg.setHeader(header);
		BodyType body = new BodyType();
		DirectoryDownload directoryDownload = new DirectoryDownload();
		directoryDownload.setAccountID("725");
		directoryDownload.setVersionID("4");
		TaxonomyType taxonomy = new TaxonomyType();
		taxonomy.setTaxonomyCode("183500000X");
		directoryDownload.setTaxonomy(taxonomy);
		Date d = new Date();
		int year = d.getYear()+1900;
		int day = d.getDate()-1;
		int month = d.getMonth()+1;
		String date = "";
		String monthString = "";
		if(day <=9){
			date = "0"+String.valueOf(day);
		}else{
			date = String.valueOf(day);
		}
		if(month <=9){
			monthString = "0"+String.valueOf(month);
		}else{
			monthString = String.valueOf(month);
		}
		String now = String.valueOf(year)+monthString+date;
		directoryDownload.setDirectoryDate(now);
		body.setDirectoryDownload(directoryDownload);
		msg.setBody(body);
		msg.setVersion("1.0");
		/* Marshalling Process on the way */
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		SureScriptsCommunicator comm = new SureScriptsCommunicator();
		String locationOfMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/");
		File messageOutbox =  new File(locationOfMessage);
		if (!messageOutbox.exists())
			messageOutbox.mkdirs();
		locationOfMessage = locationOfMessage + "DirectoryDownloadNightly_"+msg.getHeader().getMessageID()+".xml";
		String result = comm.createMessageFile(msg, locationOfMessage, "0",privateKey);
		
		String downloadFileName = "";
		if(null != result && !result.equals("") && result.contains("<URL>"))
			downloadFileName = result.substring(result.indexOf("<URL>") + 5 , result.indexOf("</URL>"));
		
		return downloadFileName;
	}
	/**
	 * Method to encode the password for Directory / Administrative Messages
	 * @param password
	 * @return
	 */
	private String encodeDirectoryCredentials(String password){
		String result = "";
		try {
			result = result + new String (new Base64().encode(MessageDigest.getInstance("SHA1").digest(password.trim().toUpperCase().getBytes("UTF-16LE"))));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @return the fdbBean
	 */
	public FDBInterface getFdbBean() {
		return fdbBean;
	}

	/**
	 * @param fdbBean the fdbBean to set
	 */
	public void setFdbBean(FDBInterface fdbBean) {
		this.fdbBean = fdbBean;
	}
	
	/**
	 * This method is to retreive all the Surescripts Messages
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<MessageDetails> retreiveAllSurescriptsMessages(String rowId, long accountId){
		String qs = null;
		Query query = null;		
		ArrayList<MessageDetails> messageDetailsList = new ArrayList<MessageDetails>();
		qs = String.format(Locale.US, "FROM MessageDetails WHERE accountId = :accId ORDER BY recievedTime DESC ");
		query = em.createQuery( qs );
		query.setParameter( "accId", accountId);
		query.setFirstResult(Integer.parseInt(rowId)*10);
		query.setMaxResults(10);
		if(null != query.getResultList() && query.getResultList().size() > 0){
			for(MessageDetails msg : (ArrayList<MessageDetails>)query.getResultList()){
					if(msg.getMessage().startsWith("<?")){
						String message = msg.getMessage().substring(msg.getMessage().indexOf("?>")+2);
						msg.setMessage(message);
						messageDetailsList.add(msg);
					}else{
						messageDetailsList.add(msg);
					}
			}
		}
        return messageDetailsList;
	}

	@Override
	public void generateErrorMessageForScheduleII(MessageType message,PrivateKey privateKey) {
		SureScriptsCommunicator comm = new SureScriptsCommunicator();
		MessageType errorMessage = getCreator().createErrorMessageForScheduleIIDrug(message);
		comm.createMessageFile(errorMessage, tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/error/"+ "Error_"+cal.get(Calendar.YEAR)+cal.get(Calendar.MONTH)+cal.get(Calendar.DATE)+".xml", "0",privateKey);
	}
	
	/**
	 * Method to retrieve the xml based on the id.
	 * @param id
	 * @return
	 */
	public String retrieveXml(String id) {
		String qs = null;
		Query query = null;
		String message = "";
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.id = :id");
		query = em.createQuery( qs );
		query.setParameter( "id", Long.parseLong(id));
		if(null != query.getResultList()){
			message = ((MessageDetails)query.getResultList().get(0)).getMessage();
			logger.info("Message retrieved for id : "+ id);
		}
		return message;
	}
	
	/**
	 * Method to retrieve filtered SureScripts Messages based on inputs.
	 * @param messageType
	 * @param date
	 * @param messageID
	 * @param rowID
	 * @param accountID
	 * @return ArrayList<MessageDetails>
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<MessageDetails> retrieveFilteredSurescriptsMessages(String messageType, String date,
			String messageID, String rowID, long accountID){
		Date inputDate = null;
		String queryString = null;
		Query query = null;
		StringBuilder queryBuilder = new StringBuilder();
		ArrayList<MessageDetails> messageDetailsList = new ArrayList<MessageDetails>();
		
		if(date != null && !date.isEmpty()){
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			try {
				inputDate = dateFormat.parse(date);
			} catch (ParseException e1) {
				TolvenLogger.error("Error parsing date for SureScripts Message Center.", SurescriptsBean.class);
				e1.printStackTrace();
			}
		} 
		queryBuilder.append("FROM MessageDetails WHERE accountId = :accId ");
		if (messageType != null && !messageType.isEmpty()) {
			queryBuilder.append("AND messageType = :msg ");
		}
		if (date != null && !date.isEmpty()) {
			queryBuilder.append("AND recievedTime >=:date AND recievedTime <:date1 ");
		}
		if (messageID != null && !messageID.isEmpty()) {
			queryBuilder.append("AND messageId = :msgId ");
		}
		queryBuilder.append("ORDER BY recievedTime DESC");
		
		queryString = String.format(Locale.US, queryBuilder.toString().trim());
		query = em.createQuery(queryString);
		query.setParameter("accId", accountID);
		if (messageType != null && !messageType.isEmpty()) {query.setParameter("msg", messageType);}
		if (date != null && !date.isEmpty()) {
			query.setParameter("date", inputDate);
			query.setParameter("date1", new Date(inputDate.getTime() + (24 * 60 * 60 * 1000)));
		}
		if (messageID != null && !messageID.isEmpty()) {query.setParameter("msgId", messageID);}
		
		if (null != query.getResultList() && query.getResultList().size() > 0) {
			setMessageCount(query.getResultList().size());
			query.setFirstResult(Integer.parseInt(rowID)*10);
			query.setMaxResults(10);
			for(MessageDetails msgDtl : (ArrayList<MessageDetails>)query.getResultList()){
				if(msgDtl.getMessage().startsWith("<?")){
					String message = msgDtl.getMessage().substring(msgDtl.getMessage().indexOf("?>")+2);
					msgDtl.setMessage(message);
					messageDetailsList.add(msgDtl);
				}else{
					messageDetailsList.add(msgDtl);
				}
			}
		}
		TolvenLogger.info("[SureScripts Message Center] Search complete, Count: "+getMessageCount(), SurescriptsBean.class);
        return messageDetailsList;
	}
	
	
	/**
	 * Posts the REFREQ into the application when a status(000) or error message is received.
	 * @param recievedMessage
	 * @param status
	 */
	public void postRefillRequestInActivityList(MessageType recievedMessage, String status,PrivateKey privateKey) {

		MedicationType medicationType = recievedMessage.getBody().getRefillRequest().getMedicationPrescribed();
		String schedule = getFdbBean().checkForDrugValidity(medicationType.getDrugDescription()); //Controlled Drug Check
		if(!schedule.equals("2")){
		ArrayList<ActRelationship> relationshipList = new ArrayList<ActRelationship>();
		templateId = "obs/evn/refillRequest";
		try {
			templateTrim = (TrimEx) trimBean.findTrim(templateId);
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}

		// Add header relation to relationshipList.
		relationHeader = ((ActEx) templateTrim.getAct()).getRelationship().get("header");
		
		relationHeader.getAct().getObservation().getValues().get(0).getST().
			setValue(recievedMessage.getHeader().getFrom());
		relationHeader.getAct().getObservation().getValues().get(1).getST().
			setValue(recievedMessage.getHeader().getTo());
		
		relationHeader.getAct().getObservation().getValues().get(2).getST().
			setValue(recievedMessage.getHeader().getMessageID());
		/* Setting RXRefNo */
		relationHeader.getAct().getObservation().getValues().get(3).getST().
			setValue(recievedMessage.getBody().getRefillRequest().getRxReferenceNumber());
		
		//Setting sent time.
		relationHeader.getAct().getObservation().getValues().get(7).setTS(trimFactory.createNewTS(new Date()));
		
		// Setting status of the RefillRequest
		relationHeader.getAct().getObservation().getValues().get(6).getST().setValue(status);	
		
		
		/* Null Checking For Prescriber order no as it is not mandatory.*/
		if (null != recievedMessage.getBody().getRefillRequest().getPrescriberOrderNumber()) {
			relationHeader.getAct().getObservation().getValues().get(4).getST().
				setValue(recievedMessage.getBody().getRefillRequest().getPrescriberOrderNumber());
			relationHeader.getAct().getObservation().getValues().get(10).getST().
			setValue(recievedMessage.getBody().getRefillRequest().getPrescriberOrderNumber()); // Original Prescriber Order Number
		}
		relationHeader.getAct().getObservation().getValues().get(5).getST().
			setValue(recievedMessage.getHeader().getRelatesToMessageID());
//		relationshipList.add(0, relationHeader);

		/* Add pharmacy relation to relationshipList.*/
		relationPharmacy = ((ActEx) templateTrim.getAct()).getRelationship().get("pharmacy");
		MandatoryPharmacyType pharmacy = recievedMessage.getBody().getRefillRequest().getPharmacy();
		//Pharmacy ID
		if (pharmacy.getIdentification() != null) {
			List<JAXBElement<String>> pharmIdList = pharmacy.getIdentification()
					.getNCPDPIDOrFileIDOrStateLicenseNumber();
			if (!pharmIdList.isEmpty()) {
				for (int i = 0; i < pharmIdList.size(); i++) {
					valueSlot = insertObservationValueSlot(pharmIdList.get(i).getName()
									.getLocalPart(), pharmIdList.get(i).getValue());
					relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
				}
			}
		}
		//Setting StoreName
		if (pharmacy.getStoreName() != null) {
			valueSlot = insertObservationValueSlot("StoreName", pharmacy.getStoreName());
			relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
		}
		//Setting Address
		if (pharmacy.getAddress().getAddressLine1() != null) {
			valueSlot = insertObservationValueSlot("AddressLine1", pharmacy.getAddress().getAddressLine1());
			relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
		}
		if (pharmacy.getAddress().getAddressLine2() != null) {
			valueSlot = insertObservationValueSlot("AddressLine2", pharmacy.getAddress().getAddressLine2());
			relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
		}
		if (pharmacy.getAddress().getCity() != null) {
			valueSlot = insertObservationValueSlot("City", pharmacy.getAddress().getCity());
			relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
		}
		if (pharmacy.getAddress().getState() != null) {
			valueSlot = insertObservationValueSlot("State", pharmacy.getAddress().getState());
			relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
		}
		if (pharmacy.getAddress().getZipCode() != null) {
			valueSlot = insertObservationValueSlot("ZipCode", pharmacy.getAddress().getZipCode());
			relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
		}
		//Setting Email
		if (pharmacy.getEmail() != null) {
			valueSlot = insertObservationValueSlot("Email", pharmacy.getEmail());
			relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
		}
		//Setting Pharmacist
		if (pharmacy.getPharmacist() != null) {
			if (pharmacy.getPharmacist().getFirstName() != null) {
				valueSlot = insertObservationValueSlot("Pharmacist FirstName", pharmacy.getPharmacist().getFirstName());
				relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
			}
			if (pharmacy.getPharmacist().getLastName() != null) {
				valueSlot = insertObservationValueSlot("Pharmacist LastName", pharmacy.getPharmacist().getLastName());
				relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
			}
			if (pharmacy.getPharmacist().getMiddleName() != null) {
				valueSlot = insertObservationValueSlot("Pharmacist MiddleName", pharmacy.getPharmacist().getMiddleName());
				relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
			}
			if (pharmacy.getPharmacist().getPrefix() != null) {
				valueSlot = insertObservationValueSlot("Pharmacist Prefix", pharmacy.getPharmacist().getPrefix());
				relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
			}
			if (pharmacy.getPharmacist().getSuffix() != null) {
				valueSlot = insertObservationValueSlot("Pharmacist Suffix", pharmacy.getPharmacist().getSuffix());
				relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
			}
		}
		//Setting PharmacistAgent
		if (pharmacy.getPharmacistAgent() != null) {
			if (pharmacy.getPharmacistAgent().getFirstName() != null) {
				valueSlot = insertObservationValueSlot("PharmacistAgent FirstName", pharmacy.getPharmacistAgent().getFirstName());
				relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
			}
			if (pharmacy.getPharmacistAgent().getLastName() != null) {
				valueSlot = insertObservationValueSlot("PharmacistAgent LastName", pharmacy.getPharmacistAgent().getLastName());
				relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
			}
			if (pharmacy.getPharmacistAgent().getMiddleName() != null) {
				valueSlot = insertObservationValueSlot("PharmacistAgent MiddleName", pharmacy.getPharmacistAgent().getMiddleName());
				relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
			}
			if (pharmacy.getPharmacistAgent().getPrefix() != null) {
				valueSlot = insertObservationValueSlot("PharmacistAgent Prefix", pharmacy.getPharmacistAgent().getPrefix());
				relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
			}
			if (pharmacy.getPharmacistAgent().getSuffix() != null) {
				valueSlot = insertObservationValueSlot("PharmacistAgent Suffix", pharmacy.getPharmacistAgent().getSuffix());
				relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
			}
		}
		//Setting Pharmacy PhoneNumber
		if (pharmacy.getPhoneNumbers() != null) {
			if (pharmacy.getPhoneNumbers().getPhone() != null) {
				ArrayList<PhoneType> phoneList = (ArrayList<PhoneType>) pharmacy.getPhoneNumbers().getPhone();
				if (!phoneList.isEmpty()) {
					for (int i = 0; i < phoneList.size(); i++) {
						valueSlot = insertObservationValueSlot(phoneList.get(i).getQualifier(),
								phoneList.get(i).getNumber());
						relationPharmacy.getAct().getObservation().getValues().add(valueSlot);
					}
				}
			}
		}
//		relationshipList.add(1, relationPharmacy);

		/*Add prescriber relation to relationshipList.*/
		relationPrescriber = ((ActEx) templateTrim.getAct()).getRelationship().get("prescriber");
		PrescriberType prescriber = recievedMessage.getBody().getRefillRequest().getPrescriber();
		String prescriberSPI = null;
		//Setting Prescriber Identification
		if (prescriber.getIdentification() != null) {
			List<JAXBElement<String>> prescriberIdList = prescriber.getIdentification().getSPIOrFileIDOrStateLicenseNumber();
			if (!prescriberIdList.isEmpty()) {
				for (int i = 0; i < prescriberIdList.size(); i++) {
					if (prescriberIdList.get(i).getName().getLocalPart().equals("SPI")) {
						valueSlot = insertObservationValueSlot(prescriberIdList.get(i).getName().getLocalPart()
								,prescriberIdList.get(i).getValue());
						relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
						prescriberSPI = prescriberIdList.get(i).getValue();
					} else if(!prescriberIdList.get(i).getValue().isEmpty()) {
						valueSlot = insertObservationValueSlot(prescriberIdList.get(i).getName().getLocalPart()
								,prescriberIdList.get(i).getValue());
						relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
					}
				}
			}
		}
		//Setting clinic name
		if (prescriber.getClinicName() != null) {
			valueSlot = insertObservationValueSlot("ClinicName", prescriber.getClinicName());
			relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
		}
		//Setting name
		if (prescriber.getName() != null) {
			if (prescriber.getName().getFirstName() != null) {
				valueSlot = insertObservationValueSlot("Prescriber FirstName", prescriber.getName().getFirstName());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getName().getLastName() != null) {
				valueSlot = insertObservationValueSlot("Prescriber LastName", prescriber.getName().getLastName());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getName().getMiddleName() != null) {
				valueSlot = insertObservationValueSlot("Prescriber MiddleName", prescriber.getName().getMiddleName());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getName().getPrefix() != null) {
				valueSlot = insertObservationValueSlot("Prescriber Prefix", prescriber.getName().getPrefix());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getName().getSuffix() != null) {
				valueSlot = insertObservationValueSlot(
						"Prescriber Suffix", prescriber.getName().getSuffix());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
		}
		//Setting speciality
		if (prescriber.getSpecialty() != null) {
			if (prescriber.getSpecialty().getQualifier() != null) {
				valueSlot = insertObservationValueSlot("Speciality", prescriber.getSpecialty().getQualifier());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getSpecialty().getSpecialtyCode() != null) {
				valueSlot = insertObservationValueSlot("SpecialityCode", prescriber.getSpecialty().getSpecialtyCode());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
		}
		//Setting address
		if (prescriber.getAddress() != null) {
			if (prescriber.getAddress().getAddressLine1() != null) {
				valueSlot = insertObservationValueSlot("AddressLine1", prescriber.getAddress().getAddressLine1());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getAddress().getAddressLine2() != null) {
				valueSlot = insertObservationValueSlot("AddressLine2", prescriber.getAddress().getAddressLine2());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getAddress().getCity() != null) {
				valueSlot = insertObservationValueSlot("City", prescriber.getAddress().getCity());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getAddress().getState() != null) {
				valueSlot = insertObservationValueSlot("State", prescriber.getAddress().getState());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getAddress().getZipCode() != null) {
				valueSlot = insertObservationValueSlot("ZipCode", prescriber.getAddress().getZipCode());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
		}
		//Setting email
		if (prescriber.getEmail() != null) {
			valueSlot = insertObservationValueSlot("Email",prescriber.getEmail());
			relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
		}
		//Setting prescriber phone nos
		if (prescriber.getPhoneNumbers() != null) {
			if (prescriber.getPhoneNumbers().getPhone() != null) {
				ArrayList<PhoneType> prescriberPhoneList = (ArrayList<PhoneType>) prescriber.getPhoneNumbers().getPhone();
				if (!prescriberPhoneList.isEmpty()) {
					for (int i = 0; i < prescriberPhoneList.size(); i++) {
						valueSlot = insertObservationValueSlot(prescriberPhoneList.get(i)
								.getQualifier(), prescriberPhoneList.get(i).getNumber());
						relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
					}
				}
			}
		}
		//Setting the menuPath of the prescriber(vacation check purpose)
		MenuData prescriberMd = getMdFromsSpi(prescriberSPI);
		relationHeader.getAct().getObservation().getValues().get(8).getST().
		setValue(prescriberMd.getPath().toString());
		
		if (prescriberMd.getPqStringVal03() != null) {
			valueSlot = insertObservationValueSlot("DEA Number", prescriberMd.getPqStringVal03());
			relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
		}
		
		if (prescriber.getPrescriberAgent() != null) {
			//Setting name
			if (prescriber.getPrescriberAgent().getFirstName() != null) {
				valueSlot = insertObservationValueSlot("PrescriberAgent FirstName", prescriber
								.getPrescriberAgent().getFirstName());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getPrescriberAgent().getLastName() != null) {
				valueSlot = insertObservationValueSlot("PrescriberAgent LastName", prescriber
								.getPrescriberAgent().getLastName());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getPrescriberAgent().getMiddleName() != null) {
				valueSlot = insertObservationValueSlot("PrescriberAgent MiddleName",
						prescriber.getPrescriberAgent().getMiddleName());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getPrescriberAgent().getPrefix() != null) {
				valueSlot = insertObservationValueSlot("PrescriberAgent Prefix", prescriber
								.getPrescriberAgent().getPrefix());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
			if (prescriber.getPrescriberAgent().getSuffix() != null) {
				valueSlot = insertObservationValueSlot("PrescriberAgent Suffix", prescriber
								.getPrescriberAgent().getSuffix());
				relationPrescriber.getAct().getObservation().getValues().add(valueSlot);
			}
		}
		relationshipList.add(0, relationHeader);
		relationshipList.add(1, relationPharmacy);
		relationshipList.add(2, relationPrescriber);

		//Adding supervisor to relationshipList
		relationSupervisor = ((ActEx) templateTrim.getAct()).getRelationship().get("supervisor");
		if (recievedMessage.getBody().getRefillRequest().getSupervisor() != null) {
			SupervisorType supervisor = recievedMessage.getBody().getRefillRequest().getSupervisor();
			if (supervisor.getIdentification() != null) {
				List<JAXBElement<String>> supervisorIdList = prescriber
						.getIdentification().getSPIOrFileIDOrStateLicenseNumber();
				if (!supervisorIdList.isEmpty()) {
					for (int i = 0; i < supervisorIdList.size(); i++) {
						valueSlot = insertObservationValueSlot(supervisorIdList.get(i)
								.getName().getLocalPart(), supervisorIdList.get(i).getValue());
						relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
					}
				}
			}
			if (supervisor.getClinicName() != null) {
				valueSlot = insertObservationValueSlot("ClinicName", supervisor
								.getClinicName());
				relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
			}
			if (supervisor.getName() != null) {
				//Setting name
				if (supervisor.getName().getFirstName() != null) {
					valueSlot = insertObservationValueSlot("Supervisor FirstName", supervisor
								.getName().getFirstName());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getName().getLastName() != null) {
					valueSlot = insertObservationValueSlot("Supervisor LastName", prescriber
								.getName().getLastName());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getName().getMiddleName() != null) {
					valueSlot = insertObservationValueSlot("Supervisor MiddleName", supervisor
								.getName().getMiddleName());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getName().getPrefix() != null) {
					valueSlot = insertObservationValueSlot("Supervisor Prefix", supervisor
								.getName().getPrefix());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getName().getSuffix() != null) {
					valueSlot = insertObservationValueSlot("Supervisor Suffix", supervisor
								.getName().getSuffix());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
			}
			if (supervisor.getSpecialty() != null) {
				if (supervisor.getSpecialty().getQualifier() != null) {
					valueSlot = insertObservationValueSlot("Speciality", supervisor
							.getSpecialty().getQualifier());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getSpecialty()
						.getSpecialtyCode() != null) {
					valueSlot = insertObservationValueSlot("SpecialityCode", supervisor
								.getSpecialty().getSpecialtyCode());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
			}
			if (supervisor.getPrescriberAgent() != null) {
				if (supervisor.getPrescriberAgent()
						.getFirstName() != null) {
					valueSlot = insertObservationValueSlot("PrescriberAgent FirstName",
							supervisor.getPrescriberAgent().getFirstName());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getPrescriberAgent()
						.getLastName() != null) {
					valueSlot = insertObservationValueSlot("PrescriberAgent LastName",
							supervisor.getPrescriberAgent().getLastName());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getPrescriberAgent()
						.getMiddleName() != null) {
					valueSlot = insertObservationValueSlot("PrescriberAgent MiddleName",
							supervisor.getPrescriberAgent().getMiddleName());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getPrescriberAgent().getPrefix() != null) {
					valueSlot = insertObservationValueSlot("PrescriberAgent Prefix",
							supervisor.getPrescriberAgent().getPrefix());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getPrescriberAgent().getSuffix() != null) {
					valueSlot = insertObservationValueSlot("PrescriberAgent Suffix",
							supervisor.getPrescriberAgent().getSuffix());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
			}
			if (supervisor.getAddress() != null) {
				if (supervisor.getAddress().getAddressLine1() != null) {
					valueSlot = insertObservationValueSlot("AddressLine1", 
							supervisor.getAddress().getAddressLine1());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getAddress().getAddressLine2() != null) {
					valueSlot = insertObservationValueSlot("AddressLine2", 
							supervisor.getAddress().getAddressLine2());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getAddress().getCity() != null) {
					valueSlot = insertObservationValueSlot("City", 
							supervisor.getAddress().getCity());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getAddress().getState() != null) {
					valueSlot = insertObservationValueSlot("State", 
							supervisor.getAddress().getState());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
				if (supervisor.getAddress().getZipCode() != null) {
					valueSlot = insertObservationValueSlot("ZipCode", supervisor.getAddress().getZipCode());
					relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
				}
			}
			if (supervisor.getEmail() != null) {
				valueSlot = insertObservationValueSlot("Email",supervisor.getEmail());
				relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
			}
			if (supervisor.getPhoneNumbers() != null) {
				if (supervisor.getPhoneNumbers().getPhone() != null) {
					ArrayList<PhoneType> supervisorPhoneList = (ArrayList<PhoneType>) supervisor.getPhoneNumbers().getPhone();
					if (!supervisorPhoneList.isEmpty()) {
						for (int i = 0; i < supervisorPhoneList.size(); i++) {
							valueSlot = insertObservationValueSlot(supervisorPhoneList.get(i)
									.getQualifier(),supervisorPhoneList.get(i).getNumber());
							relationSupervisor.getAct().getObservation().getValues().add(valueSlot);
						}
					}
				}
			}
		}
		relationshipList.add(3, relationSupervisor);

		// Add patient relation to relationshipList.
		relationPatient = ((ActEx) templateTrim.getAct()).getRelationship().get("patient");
		PatientType patient = recievedMessage.getBody().getRefillRequest().getPatient();
		
		relationPatient.getAct().getObservation().getValues()
				.get(0).getST().setValue(patient.getName().getLastName());
		relationPatient.getAct().getObservation().getValues()
				.get(1).getST().setValue(patient.getName().getFirstName());
		relationPatient.getAct().getObservation().getValues()
				.get(2).getST().setValue(patient.getName().getMiddleName());
		ObservationValueSlot obs = insertObservationValueSlotTS("DOB",patient.getDateOfBirth());
		relationPatient.getAct().getObservation().getValues()
				.get(3).setTS(obs.getTS());
		
		relationPatient.getAct().getObservation().getValues()
				.get(4).getST().setValue(patient.getGender().toString());
		
		relationPatient.getAct().getObservation().getValues()
		.get(6).getST().setValue(patient.getName().getPrefix()); //Prefix
		relationPatient.getAct().getObservation().getValues()
		.get(7).getST().setValue(patient.getName().getSuffix()); // Suffix
		
		if (patient.getAddress() != null) {
			if (patient.getAddress().getAddressLine1() != null) {
				relationPatient.getAct().getObservation().getValues()
				.get(8).getST().setValue(patient.getAddress().getAddressLine1()); // Address Line1
			}
			if (patient.getAddress().getAddressLine2() != null) {
				relationPatient.getAct().getObservation().getValues()
				.get(9).getST().setValue(patient.getAddress().getAddressLine2()); // Address Line2
			}
			if (patient.getAddress().getCity() != null) {
				relationPatient.getAct().getObservation().getValues()
				.get(10).getST().setValue(patient.getAddress().getCity()); //City 
			}
			if (patient.getAddress().getState() != null) {
				relationPatient.getAct().getObservation().getValues()
				.get(11).getST().setValue(patient.getAddress().getState()); //State
			}
			if (patient.getAddress().getZipCode() != null) {
				relationPatient.getAct().getObservation().getValues()
				.get(12).getST().setValue(patient.getAddress().getZipCode()); //Zip
			}
		}
		
		//Setting email
		if (patient.getEmail() != null) {
			valueSlot = insertObservationValueSlot("Email", patient.getEmail());
			relationPatient.getAct().getObservation().getValues().add(valueSlot);
		}
		
		if (patient.getIdentification() != null) {
			List<JAXBElement<String>> patientIdList = patient.getIdentification().getFileIDOrMedicareNumberOrMedicaidNumber();
			if (!patientIdList.isEmpty()) {
				for (int i = 0; i < patientIdList.size(); i++) {
					valueSlot = insertObservationValueSlot(patientIdList.get(i)
							.getName().getLocalPart(), patientIdList.get(i).getValue());
					relationPatient.getAct().getObservation().getValues().add(valueSlot);
				}
			}
		}
		
		//Setting patient phone nos
		if (patient.getPhoneNumbers() != null) {
			if (patient.getPhoneNumbers().getPhone() != null) {
				ArrayList<PhoneType> patientPhoneList = (ArrayList<PhoneType>) patient.getPhoneNumbers().getPhone();
				if (!patientPhoneList.isEmpty()) {
					for (int i = 0; i < patientPhoneList.size(); i++) {
						valueSlot = insertObservationValueSlot(patientPhoneList.get(i)
								.getQualifier(), patientPhoneList.get(i).getNumber());
						relationPatient.getAct().getObservation().getValues().add(valueSlot);
					}
				}
			}
		}
		
		relationshipList.add(4, relationPatient);

		//Add MedicationPrescribed relation to relationshipList.
		relationMedicationPrescribed = ((ActEx) templateTrim.getAct()).getRelationship().get("MedicationPrescribed");
		MedicationType medicationPrescribed = recievedMessage.getBody().getRefillRequest().getMedicationPrescribed();
		
		relationMedicationRequested = ((ActEx) templateTrim.getAct()).getRelationship().get("MedicationRequested");
		if(medicationPrescribed.getDrugDescription() != null){
			valueSlot = insertObservationValueSlot("DrugDescription", medicationPrescribed.getDrugDescription());
			relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
		}
		if(medicationPrescribed.getDrugCoded() != null){
			if(medicationPrescribed.getDrugCoded().getProductCode() != null &&
					!medicationPrescribed.getDrugCoded().getProductCode().isEmpty()){
				valueSlot = insertObservationValueSlot("ProductCode", medicationPrescribed.getDrugCoded().getProductCode());
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
			if(medicationPrescribed.getDrugCoded().getProductCodeQualifier()!=null){
				valueSlot = insertObservationValueSlot("ProductCodeQualifier", medicationPrescribed.getDrugCoded().getProductCodeQualifier());
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
			if(medicationPrescribed.getDrugCoded().getDosageForm()!=null){
				valueSlot = insertObservationValueSlot("DosageForm", pharmacyBean.getDosageForm(medicationPrescribed.getDrugCoded().getDosageForm(), true));
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
			if(medicationPrescribed.getDrugCoded().getStrength() != null &&
					!medicationPrescribed.getDrugCoded().getStrength().isEmpty()){
				valueSlot = insertObservationValueSlot("Strength", medicationPrescribed.getDrugCoded().getStrength());
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
			if(medicationPrescribed.getDrugCoded().getStrengthUnits()!=null){
				valueSlot = insertObservationValueSlot("StrengthUnits", retrieveDrugQualDescription(medicationPrescribed.getDrugCoded().getStrengthUnits()));
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
			if(medicationPrescribed.getDrugCoded().getDrugDBCode()!=null){
				valueSlot = insertObservationValueSlot("DrugDBCode", medicationPrescribed.getDrugCoded().getDrugDBCode());
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
			if(medicationPrescribed.getDrugCoded().getDrugDBCodeQualifier()!=null){
				valueSlot = insertObservationValueSlot("DrugDBCodeQualifier", medicationPrescribed.getDrugCoded().getDrugDBCodeQualifier());
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
		}
		if(medicationPrescribed.getQuantity() != null){
			if(medicationPrescribed.getQuantity().getQualifier()!= null){
				valueSlot = insertObservationValueSlot("Quantity(qualifier)", retrieveDrugQualDescription(medicationPrescribed.getQuantity().getQualifier()));
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
			if(medicationPrescribed.getQuantity().getValue()!= null){
				valueSlot = insertObservationValueSlot("Quantity(value)", medicationPrescribed.getQuantity().getValue());
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
		}
		if(medicationPrescribed.getDaysSupply() != null){
			valueSlot = insertObservationValueSlot("DaysSupply", medicationPrescribed.getDaysSupply());
			relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
		}
		if(medicationPrescribed.getDirections() != null){
			valueSlot = insertObservationValueSlot("Directions", medicationPrescribed.getDirections());
			relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
		}
		if(medicationPrescribed.getNote() != null){
			valueSlot = insertObservationValueSlot("Note", medicationPrescribed.getNote());
			relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
		}
		if(medicationPrescribed.getRefills() != null){
			if(medicationPrescribed.getRefills().getQualifier()!= null){
				valueSlot = insertObservationValueSlot("Refills(qualifier)", medicationPrescribed.getRefills().getQualifier());
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
				
				ValueSet statusSet = templateTrim.getValueSet().get("refillQualifier");
				List<Object> values  = statusSet.getBindsAndADSAndCDS();
				for(Object value : values){
					CE ce = (CE) value;
					if(ce.getDisplayName().equals(medicationPrescribed.getRefills().getQualifier())) {
						relationMedicationRequested.getAct().getObservation().getValues().get(2).setCE(ce);
					}
				}
			}
			if(medicationPrescribed.getRefills().getQuantity()!= null){
				valueSlot = insertObservationValueSlot("Refills(value)", medicationPrescribed.getRefills().getQuantity());
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
				relationMedicationRequested.getAct().getObservation().getValues().get(0).getST().setValue(medicationPrescribed.getRefills().getQuantity());
			}
		}
		if(medicationPrescribed.getSubstitutions() != null){
			if(medicationPrescribed.getSubstitutions().equals("7") || medicationPrescribed.getSubstitutions().equals("1"))
				valueSlot = insertObservationValueSlot("Substitutions", "No");
			else 
				valueSlot = insertObservationValueSlot("Substitutions", "Yes");
			
			relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
		}
		if(medicationPrescribed.getWrittenDate() != null){
			valueSlot = insertObservationValueSlotTS("WrittenDate", medicationPrescribed.getWrittenDate());
			relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
		}
		if(medicationPrescribed.getLastFillDate() != null){
			DateFormat df = new SimpleDateFormat("yyyyMMdd");
			try {
				Date dt = df.parse(medicationPrescribed.getLastFillDate());
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				valueSlot = insertObservationValueSlot("LastFillDate", sdf.format(dt));
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		if(medicationPrescribed.getDiagnosis()!= null){
			List<Diagnosis> diagList = medicationPrescribed.getDiagnosis();
			if(!diagList.isEmpty()){
				for(int i=0;i<diagList.size();i++){
					if(diagList.get(i).getClinicalInformationQualifier()!=null){
						valueSlot = insertObservationValueSlot("ClinicalInformationQualifier#"+(i+1),diagList.get(i).getClinicalInformationQualifier());
						relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
					}
					if(diagList.get(i).getPrimary()!=null){
						if(diagList.get(i).getPrimary().getQualifier()!=null){
							valueSlot = insertObservationValueSlot("Primary(qualifier)#"+(i+1),diagList.get(i).getPrimary().getQualifier());
							relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
						}
						if(diagList.get(i).getPrimary().getValue()!=null){
							valueSlot = insertObservationValueSlot("Primary(value)#"+(i+1),diagList.get(i).getPrimary().getValue());
							relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
						}
					}	
					if(diagList.get(i).getSecondary()!=null){
						if(diagList.get(i).getSecondary().getQualifier()!=null){
							valueSlot = insertObservationValueSlot("Secondary(qualifier)#"+(i+1),diagList.get(i).getSecondary().getQualifier());
							relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
						}
						if(diagList.get(i).getSecondary().getValue()!=null){
							valueSlot = insertObservationValueSlot("Secondary(value)#"+(i+1),diagList.get(i).getSecondary().getValue());
							relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
						}
					}	
				}
			}
		}
		if(medicationPrescribed.getPriorAuthorization() != null){
			if(medicationPrescribed.getPriorAuthorization().getQualifier() != null){
				valueSlot = insertObservationValueSlot("PriorAuthorization(qualifier)",medicationPrescribed.getPriorAuthorization().getQualifier());
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
			if(medicationPrescribed.getPriorAuthorization().getValue() != null){
				valueSlot = insertObservationValueSlot("PriorAuthorization(value)",medicationPrescribed.getPriorAuthorization().getValue());
				relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
			}
		}
		relationMedicationRequested.getAct().getObservation().getValues().get(3).getST().setValue(schedule); // Setting Schedule Drug Value
		if(schedule.equals("3") || schedule.equals("4") || schedule.equals("5")){
			valueSlot = insertObservationValueSlot("IsScheduleDrug", "Yes");
		} else {
			valueSlot = insertObservationValueSlot("IsScheduleDrug", "No");
		}
		relationMedicationPrescribed.getAct().getObservation().getValues().add(valueSlot);
		
		relationshipList.add(5, relationMedicationPrescribed);
		relationshipList.add(6, relationMedicationRequested);
		try {
			getTrimMsgBean().createTRIMPlaceholder(getAccountUser(), templateId, prescriberMd.getPath()+":activity", now, source, relationshipList,privateKey);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (TRIMException e) {
			e.printStackTrace();
		}
//		changeStatusOfProcessedMessage(recievedMessage.getHeader().getMessageID(), "RefillRequest", "processed");
		} else{
			generateErrorMessageForScheduleII(recievedMessage,privateKey);
			TolvenLogger.info("Schedule-II drug received, Error message will be sent back to SureScripts.", SurescriptsBean.class);
		}
	}
	
	/**
	 * Checks whether the communication with SureScripts was successful. 
	 * @param messageId
	 * @return true only if the communication was successful.
	 */
	public boolean isSuccessfulCommunication(String messageId) {
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT md FROM MessageDetails md WHERE md.messageId = :messageId");
		query = em.createQuery( qs );
		query.setParameter("messageId", messageId);
		if(query.getResultList() != null && query.getResultList().size() > 0) {
			int count = query.getResultList().size();
			switch (count) {
			case 1:
				TolvenLogger.info("Message sent resopnse not yet received.", SurescriptsBean.class);
				return true;
			case 2:
				TolvenLogger.info("Duplicate messageId, message wont be sent to SureScripsts.", SurescriptsBean.class);
				return true;
			default:
				TolvenLogger.error("Message with same messageId found in excess in database", SurescriptsBean.class);
				break;
			}
		}	
		return false;
	}
	/**
	 * Method to load state names into StateNames table in Surescripts schema.
	 * @param stateCode
	 * @param name
	 */
	public boolean createStateNames(String stateCode, String name) {
		if (!checkForStateName(stateCode)) {
			StateNames stateName = new StateNames();
			stateName.setStateCode(stateCode);
			stateName.setStateName(name);
			persistStateName(stateName);
			return true;
		}
		return false;
	}
	
	/**
	 * Method to load drug qualifiers into DrugQualifier table in Surescripts schema.
	 * @param stateCode
	 * @param name
	 */
	public boolean createDrugQualifiers(String qualifierCode, String description) {
		if (!checkForDrugQualifer(qualifierCode)) {
			DrugQualifier drugQualifier = new DrugQualifier();
			drugQualifier.setQualcode(qualifierCode);
			drugQualifier.setQualDesc(description);
			persistDrugQualifiers(drugQualifier);
			return true;
		}
		return false;
	}
	
	/**
	 * Method to load error codes into ErrorCodes table in Surescripts schema.
	 * @param stateCode
	 * @param name
	 */
	public boolean createErrorCodes(String errorCode, String description) {
		if (!checkForErrorCodes(errorCode)) {
			ErrorCodes errorcodes = new ErrorCodes();
			errorcodes.setError_code(errorCode);
			errorcodes.setError_desc(description);
			persistErrorCodes(errorcodes);
			return true;
		}
		return false;
	}
	
	public void persistStateName( StateNames stateName ) {
		em.persist(stateName);
	}
	
	public void persistDrugQualifiers( DrugQualifier drugQualifier ) {
		em.persist(drugQualifier);
	}
	
	public void persistErrorCodes( ErrorCodes errorCodes ) {
		em.persist(errorCodes);
	}
	/**
	 * Method to check whether state name exists.
	 * @param mdRefill
	 * @return
	 */
	private boolean checkForStateName(String stateCode){
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT sn FROM StateNames sn WHERE sn.stateCode = :code");
		query = em.createQuery( qs );
		query.setParameter( "code", stateCode);
		if(query.getResultList() != null && query.getResultList().size() > 0){
			return true;
		}
		return false;	
	}
	
	/**
	 * Method to check whether drug qualifier exists.
	 * @param mdRefill
	 * @return
	 */
	private boolean checkForDrugQualifer(String qualifierCode){
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT dq FROM DrugQualifier dq WHERE dq.qualcode = :code");
		query = em.createQuery( qs );
		query.setParameter( "code", qualifierCode);
		if(query.getResultList() != null && query.getResultList().size() > 0){
			return true;
		}
		return false;	
	}
	
	/**
	 * Method to check whether error codes exists.
	 * @param mdRefill
	 * @return
	 */
	private boolean checkForErrorCodes(String errorCode){
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT ec FROM ErrorCodes ec WHERE ec.error_code = :code");
		query = em.createQuery( qs );
		query.setParameter( "code", errorCode);
		if(query.getResultList() != null && query.getResultList().size() > 0){
			return true;
		}
		return false;	
	}
	
}
