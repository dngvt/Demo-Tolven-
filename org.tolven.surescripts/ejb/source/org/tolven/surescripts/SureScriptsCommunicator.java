/**
 * 
 */
package org.tolven.surescripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.tolven.app.CreatorLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.TrimLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.core.AccountDAOLocal;
import org.tolven.us.states.DemographicsLocal;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.el.ExpressionEvaluator;
import org.tolven.logging.TolvenLogger;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.surescripts.entity.ValidMessageTypes;
import org.tolven.trim.ADXPSlot;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.CE;
import org.tolven.trim.ENXPSlot;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.TelecommunicationAddressUse;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.HL7DateFormatUtility;
import org.tolven.trim.ex.TrimEx;
import org.tolven.xml.NamespacePrefixMapperImpl;
import org.tolven.xml.ParseXML;

/**
 * @author mohammed
 * 
 */
public class SureScriptsCommunicator extends ParseXML{

	public static final String DIRECTION_OUTGOING = "outgoing";
	public static final String SOURCE_ADDRESS = "mailto:TOLVEN.dp@surescripts.com";
	public static final String DESTINATION_ADDRESS = "mailto:SSSDIR.dp@surescripts.com";
	
	@EJB private SurescriptsLocal sureBean;
	@EJB private DemographicsLocal demographicsBean;
	@EJB private TolvenPropertiesLocal tproperties;
	@EJB private CreatorLocal creatorBean;
	@EJB private TrimLocal trimBean;
	@EJB private AccountDAOLocal accountBean;
	@EJB private MenuLocal menuBean;
    @EJB private PharmacyLocal pharmacyBean;
	
	private static JAXBContext jc;
	/**
	 * Construct a Tolven application XML parser
	 * @throws Exception
	 */
	static{
		try{
			jc= JAXBContext.newInstance(getParsePackageName());
		}catch (Exception e) {
			throw new RuntimeException("Exception creating JAXBContext",e);
		}
	}
	protected static String getParsePackageName() {
		return "org.tolven.surescripts";
	}
	
	public SureScriptsCommunicator() {
		super(jc);
		try {
			InitialContext ctx = new InitialContext();
			if (tproperties==null) {
				tproperties = (TolvenPropertiesLocal) ctx.lookup("java:global/tolven/tolvenEJB/TolvenProperties!org.tolven.core.TolvenPropertiesLocal");
			}
			if (sureBean==null) {
				sureBean = (SurescriptsLocal) ctx.lookup("java:global/tolven/tolvenEJB/SurescriptsBean!org.tolven.surescripts.SurescriptsLocal");
			}
			if (demographicsBean==null) {
				demographicsBean = (DemographicsLocal) ctx.lookup("java:global/tolven/tolvenEJB/DemographicsBean!org.tolven.us.states.DemographicsLocal");
			}
			if (creatorBean==null) {
				creatorBean = (CreatorLocal) ctx.lookup("java:global/tolven/tolvenEJB/CreatorBean!org.tolven.app.CreatorLocal");
			}
			if (trimBean==null) {
				trimBean = (TrimLocal) ctx.lookup("java:global/tolven/tolvenEJB/TrimBean!org.tolven.app.TrimLocal");
			}
			if (accountBean==null) {
				accountBean = (AccountDAOLocal) ctx.lookup("java:global/tolven/tolvenEJB/AccountDAOBean!org.tolven.core.AccountDAOLocal");
			}	
			if (menuBean==null) {
				menuBean = (MenuLocal) ctx.lookup("java:global/tolven/tolvenEJB/MenuBean!org.tolven.app.MenuLocal");
			}	
			
		} catch (NamingException e) {
			throw new RuntimeException( "Unable to access Properties Bean in ResourceResolver", e);
		}
	}
	
	/**
	 * Checks whether the master user is associated with account being used.
	 * @param accountId
	 * @return true if Master user is added to the account.
	 */
	private boolean validateMasterAccountAssociation(Long accountId){
		
		if (tproperties.getProperty("eprescription.surescripts.masteruser") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.masteruser- .", SureScriptsCommunicator.class);
		} else {
			String uid = tproperties.getProperty("eprescription.surescripts.masteruser");
			if(uid == null)
				return false;
			// Make sure that the master user is already an active member of the account
			AccountUser accountUser = accountBean.findAccountUser(uid, accountId);
			if (accountUser != null) {
		           return true;
			} 
		}
		return false;
	}
	
	

	private void createMessageId(TrimEx trim) {
		//String accountType = getAccountUser().getAccount().getAccountType().getKnownType();
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		evaluator.addVariable("trim", trim);
		StringBuffer plain = new StringBuffer();
		if(trim.getName().equals("obs/evn/patientPrescription")){	
			
			String newRxStatus = evaluateToString(evaluator,"#{trim.act.relationship['status'].act.text.ST.value}");
			if (StringUtils.isBlank(newRxStatus) || (!newRxStatus.equals("Verified") && !newRxStatus.equals("Success"))) {
				//plain.append(act.getRelationship().get("toSureScripts").getAct().getObservation().getValues().get(0).getST().getValue());
				//get patient last name
				plain.append(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.name.EN['L'].part['FAM'].ST.value}"));
				//plain.append(act.getRelationship().get("toSureScripts").getAct().getObservation().getValues().get(1).getST().getValue());
				//plain.append(act.getRelationship().get("toSureScripts").getAct().getObservation().getValues().get(9).getST().getValue());
				//plain.append(act.getRelationship().get("medicationDetails").getAct().getObservation().getValues().get(11).getTS().getValue());
				//get medication date
				plain.append(evaluateToString(evaluator,"#{trim.act.effectiveTime.TS.value}"));
				//effectiveHighTime = act.getRelationship().get("medicationDetails").getAct().getEffectiveTime().getIVLTS().getHigh().getTS().getValue();
				//effectiveLowTime = act.getRelationship().get("medicationDetails").getAct().getEffectiveTime().getIVLTS().getLow().getTS().getValue();
				String effectiveHighTime = evaluateToString(evaluator,"#{trim.act.participation['consumableProduct'].role.effectiveTime.IVLTS.high.TS.value}");
				String effectiveLowTime	=  evaluateToString(evaluator,"#{trim.act.participation['consumableProduct'].role.effectiveTime.IVLTS.low.TS.value}");
				if (!effectiveLowTime.equals("") && !effectiveHighTime.equals("")) {
					plain.append(effectiveLowTime);
					plain.append(effectiveHighTime);
					plain.append(System.currentTimeMillis());
					evaluator.setValue("#{trim.act.relationship['messageId'].act.text.ST.value}",generateMessageId(new String(plain)));						
				}
			}
		}
	}

	
	/**
	 * Error message to be sent when refillRequest times out after 48 hrs.
	 * @param refillMessage
	 * @return
	 */
	public void createRefillRequestErrorMessage(MenuData mdTimer,PrivateKey privateKey) {
		
		String locationOfMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/error/");
		File messageOutbox = new File(locationOfMessage);
		if (!messageOutbox.exists())
			messageOutbox.mkdirs();
		
		MessageType errorMessage = new MessageType();
		
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
//		userToken.setUsername(USERNAME);
		userToken.setUsername("To3%lS1#");
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
//		password.setValue(PASSWORD);
		password.setValue("Basic VG8zJWxTMSM6cnRnQCpkdzI=");
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		
		HeaderType errorHeader = new HeaderType();
		errorHeader.setTo(mdTimer.getString05());
		errorHeader.setFrom(mdTimer.getString04());
		errorHeader.setMessageID("0");
		errorHeader.setRelatesToMessageID(mdTimer.getString06());
		errorHeader.setSentTime(getCurrentUTC());
		errorHeader.setSecurity(security);
		errorMessage.setHeader(errorHeader);
		
		BodyType errorBody = new BodyType();
		Error error = new Error();
		error.setCode("601");
		error.setDescription("REFREQ timed out on the Prescriber processing queue."); 
		error.setDescriptionCode("210");
//		error.setDescription("Request timed out before response could be received.");
//		error.setDescriptionCode("008");
		errorBody.setError(error);
		errorMessage.setBody(errorBody);
		
		/* Marshalling Process on the way */
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		locationOfMessage = locationOfMessage + "Error_"+mdTimer.getString06()+ ".xml";
		createMessageFile(errorMessage, locationOfMessage, String.valueOf(mdTimer.getAccount().getId()),privateKey);
	}
	
	/**
	 * Method to post NEWRX, REFREQ/RES, ERROR and STATUS messages to surescripts.
	 * @param file
	 */
	private void postMessage(StringWriter sw, String accountId, String messageType,PrivateKey privateKey) {
		TolvenLogger.info("Skipping sending message to surescripts", SureScriptsCommunicator.class);
	/*	String res_line;
		try {
			URL url = new URL("https://erxtest.tolven.org/Tolven/eRxOutbox");
			StringReader fr = new StringReader(sw.getBuffer().toString());
			char[] buffer = new char[1024 * 10];
			int b_read = 0;
			if ((b_read = fr.read(buffer)) != -1) {
				URLConnection urlc = url.openConnection();
				urlc.setConnectTimeout(30000);
				urlc.setReadTimeout(30000);
				urlc.setRequestProperty("RequestType", "NONDIR");
				urlc.setRequestProperty("MessageType", messageType);
				urlc.setRequestProperty("SourceURL", "http://98.111.131.124/Tolven/ClientInbox");
				urlc.setRequestProperty("Content-Type", "text/xml");
				urlc.setDoOutput(true);
				urlc.setDoInput(true);
				TolvenLogger.info("Message being sent to SURESCRIPTS via PORKY....Awaiting response.", SureScriptsCommunicator.class);
				PrintWriter pw = new PrintWriter(urlc.getOutputStream());
				pw.write(buffer, 0, b_read);
				pw.close();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
				StringBuffer result = new StringBuffer();
				while ((res_line = in.readLine()) != null) {
					result.append(res_line);
				}
				in.close();
				
				if (result.length() > 0) {
					TolvenLogger.info("SURESCRIPTS Response Recieved via PORKY Successfully", SureScriptsCommunicator.class);
					sureBean.convertToMessage(result.toString(), false, accountId,privateKey);
				} else {
					TolvenLogger.error("Failed to receive SURESCRIPTS Response via PORKY.", SureScriptsCommunicator.class);
				}
				TolvenLogger.error("Failed to receive SURESCRIPTS Response via PORKY.", SureScriptsCommunicator.class);
			}
		} catch (MalformedURLException me) {
			me.printStackTrace();
		} catch (UnknownServiceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			TolvenLogger.error("Exception occurred while posting Non Directory message.", SureScriptsCommunicator.class);
			e.printStackTrace();
		}*/
	}
	
	/**
	 * Method to post Directory messages to surescripts.
	 * @param file
	 */
	private String postDirectoryMessage(StringWriter sw, String accountId, String messageType,PrivateKey privateKey) {
		String resultString = "";
/*		try {
			URL url = new URL("https://erxtest.tolven.org/Tolven/eRxOutbox");
			StringReader fr = new StringReader(sw.getBuffer().toString());
			char[] buffer = new char[1024*10];
			int b_read = 0;
			if ((b_read = fr.read(buffer)) != -1) {
				URLConnection urlc = url.openConnection();
				urlc.setConnectTimeout(30000);
				urlc.setReadTimeout(30000);
				urlc.setRequestProperty("RequestType", "DIR");
				urlc.setRequestProperty("MessageType", messageType);
				urlc.setRequestProperty("SourceURL", "http://98.111.131.124/Tolven/ClientInbox");
				urlc.setRequestProperty("Content-Type", "text/xml");
				urlc.setDoOutput(true);
				urlc.setDoInput(true);
				TolvenLogger.info("Message being sent to SURESCRIPTS via PORKY....Awaiting response.", SureScriptsCommunicator.class);
				PrintWriter pw = new PrintWriter(urlc.getOutputStream());
				pw.write(buffer, 0, b_read);
				pw.close();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
				String res_line;
				StringBuffer result = new StringBuffer();
				while ((res_line = in.readLine()) != null){
					    result.append(res_line);
					    System.out.println(res_line);
				}
				in.close();
				
				if(result.length() > 0){
					TolvenLogger.info("SURESCRIPTS Response Recieved via PORKY Successfully", SureScriptsCommunicator.class);
					resultString = result.toString();	// in the case of DirectoryDownloadRespone.
					sureBean.convertToMessage(result.toString() , false, accountId,privateKey);
				}
			}
		} catch (MalformedURLException me) {
			me.printStackTrace();
		} catch (UnknownServiceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			TolvenLogger.error("Exception occurred while posting Non Directory message.", SureScriptsCommunicator.class);
			e.printStackTrace();
		}
*/	
		TolvenLogger.info("Skipping sending message to surescripts", SureScriptsCommunicator.class);
		return resultString;
	}

	/**
	 * Method to process the Refill Request and create Refill Response.
	 */
	public void processRefillRequest(TrimEx trim,PrivateKey privateKey){
		
		List<ObservationValueSlot> responseValues = ((ActEx) trim.getAct()).getRelationship().
		get("response").getAct().getObservation().getValues();

		// Check with messageId that a successful communication has already been occurred.
		if (sureBean.isSuccessfulCommunication(responseValues.get(4).getST().getValue().trim())) {
			TolvenLogger.info("Duplicate messageId. "+ responseValues.get(4).getST().getValue().trim(), SureScriptsCommunicator.class);
			return;
		}
		
		String locationOfRefillResponse = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/");
		File messageOutbox = new File(locationOfRefillResponse);
		if (!messageOutbox.exists())
			messageOutbox.mkdirs();
		
		/* Header coding done here */
		HeaderType header = new HeaderType();
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername("To3%lS1#");
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeInformativeCredentials("To3%lS1#","rtg@*dw2"));
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		
		List<ObservationValueSlot> headerValues = ((ActEx) trim.getAct()).getRelationship().
							get("header").getAct().getObservation().getValues();
		for (int i = 0; i < headerValues.size(); i++) {
			if (headerValues.get(i).getST() != null) {
				if (headerValues.get(i).getST().getValue().trim().length() == 0)
					headerValues.get(i).setST(null);
			} else if (headerValues.get(i).getTS() != null && null != headerValues.get(i).getTS().getValue()) {
				if (headerValues.get(i).getTS().getValue().trim().length() == 0)
					headerValues.get(i).setTS(null);
			} else if (headerValues.get(i).getCE() != null) {
				if (headerValues.get(i).getCE().getDisplayName().trim()
						.length() == 0)
					headerValues.get(i).setCE(null);
			} else if (headerValues.get(i).getINT() != null) {
				if (headerValues.get(i).getINT().toString().trim()
						.length() == 0)
					headerValues.get(i).setINT(null);
			}
		}
		
		if (null != headerValues.get(2).getST()) {
			header.setRelatesToMessageID(headerValues.get(2).getST().toString().trim());
		}
		if (null != headerValues.get(1).getST()) {
			header.setFrom(headerValues.get(1).getST().toString().trim());
		}
		if (null != headerValues.get(0).getST()) {
			header.setTo(headerValues.get(0).getST().toString().trim());
		}
		header.setMessageID(responseValues.get(4).getST().getValue());
		
		header.setSentTime(getCurrentUTC());
		//header.setSecurity(security);
		AppVersionType appVersion = new AppVersionType();
		appVersion.setApplicationVersion(tproperties.getProperty("eprescription.surescripts.appVersion"));
		appVersion.setAppName(tproperties.getProperty("eprescription.surescripts.applicationName"));
		appVersion.setVendorName(tproperties.getProperty("eprescription.surescripts.vendorName"));
		header.setAppVersion(appVersion);
		header.setSMSVersion(tproperties.getProperty("eprescription.surescripts.SMSVersion"));
		
		MessageType message = new MessageType();
		message.setHeader(header);
		message.setVersion(tproperties.getProperty("eprescription.surescripts.appVersion"));
		message.setRelease(tproperties.getProperty("eprescription.surescripts.appRelease"));
		BodyType body = new BodyType();

		RefillResponse refillResponse = new RefillResponse();
		if(null != headerValues.get(3).getST())
			refillResponse.setRxReferenceNumber(headerValues.get(3).getST()
				.toString().trim()); // Reference Number entered
//		if(null != headerValues.get(4).getST())
//			refillResponse.setPrescriberOrderNumber(headerValues.get(4).getST().toString().trim()); // PON entered
		
		if(null != headerValues.get(10).getST() && headerValues.get(10).getST().getValue().length() > 0) {
			refillResponse.setPrescriberOrderNumber(headerValues.get(10).getST().getValue().trim()); // PON entered
		}

		/*Declarations for Pharmacy*/
		PharmacyType pharmacy = new PharmacyType();
		AddressType pharmacyAddress = new AddressType();
		PharmacyIDType pharmacyId = new PharmacyIDType();
		MandatoryNameType pharmacist = new MandatoryNameType();
		MandatoryNameType pharmacistAgent = new MandatoryNameType();
		PhoneNumbersType pharmacyPhoneNumType = new PhoneNumbersType();
		
		ActRelationship pharmacyRelation = ((ActEx)trim.getAct()).getRelationship().get("pharmacy");
		if(pharmacyRelation.getAct().getObservation().getValues() != null) {
			List<ObservationValueSlot>  pharmacyValues = pharmacyRelation.getAct().getObservation().getValues();
				for( int i=0;i<pharmacyValues.size();i++) {
					if(pharmacyValues.get(i).getLabel() != null && pharmacyValues.get(i).getLabel().getValue().length() > 0){
						/* Setting pharmacyId */
						if(pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().size() < 3) {
							if(pharmacyValues.get(i).getLabel().getValue().equals("StateLicenseNumber")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "StateLicenseNumber"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("SocialSecurity")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "SocialSecurity"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("DEANumber")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "DEANumber"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("MedicaidNumber")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "MedicaidNumber"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("FileID")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "FileID"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("NPI")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "NPI"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("NAICCode")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "StateLicenseNumber"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("PayerID")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "PayerID"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("MedicareNumber")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "MedicareNumber"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("MutuallyDefined")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "MutuallyDefined"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("NCPDPID")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "NCPDPID"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("PriorAuthorization")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "PriorAuthorization"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("BINLocationNumber")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "StateLicenseNumber"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("PromotionNumber")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "PromotionNumber"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("SecondaryCoverage")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "SecondaryCoverage"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("PPONumber")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "PPONumber"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							else if(pharmacyValues.get(i).getLabel().getValue().equals("HIN")){
								JAXBElement<String> pharmIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "HIN"),
										String.class, pharmacyValues.get(i).getST().getValue().trim());
								pharmacyId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(pharmIds);
							}
							pharmacy.setIdentification(pharmacyId);
						}//if(pharmId<3)
						
						/* Setting StoreName */
						if(pharmacyValues.get(i).getLabel().getValue().equals("StoreName")){
							pharmacy.setStoreName(pharmacyValues.get(i).getST().getValue().trim());
						}
						
						/* Setting Address */
						if (pharmacyValues.get(i).getLabel().getValue().equals("AddressLine1")) {
							pharmacyAddress.setAddressLine1(pharmacyValues.get(i).getST().getValue().trim());
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("AddressLine2")) {
							pharmacyAddress.setAddressLine2(pharmacyValues.get(i).getST().getValue().trim());
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("City")) {
							pharmacyAddress.setCity(pharmacyValues.get(i).getST().getValue().trim());
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("State")) {
							pharmacyAddress.setState(pharmacyValues.get(i).getST().getValue().trim());
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("ZipCode")) {
							pharmacyAddress.setZipCode(pharmacyValues.get(i).getST().getValue().trim());
						}
						
						/* Setting Email */
						if (pharmacyValues.get(i).getLabel().getValue().equals("Email")) {
							pharmacy.setEmail(pharmacyValues.get(i).getST().getValue().trim());
						}

						/* Setting Pharmacist */
						boolean isPharmacist = false;
						if (pharmacyValues.get(i).getLabel().getValue().equals("Pharmacist FirstName")) {
								pharmacist.setFirstName(pharmacyValues.get(i).getST().getValue().trim());
								isPharmacist = true;
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("Pharmacist LastName")) {
								pharmacist.setLastName(pharmacyValues.get(i).getST().getValue().trim());
								isPharmacist = true;
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("Pharmacist MiddleName")) {
								pharmacist.setMiddleName(pharmacyValues.get(i).getST().getValue().trim());
								isPharmacist = true;
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("Pharmacist Prefix")) {
								pharmacist.setPrefix(pharmacyValues.get(i).getST().getValue().trim());
								isPharmacist = true;
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("Pharmacist Suffix")) {
								pharmacist.setSuffix(pharmacyValues.get(i).getST().getValue().trim());
								isPharmacist = true;
						}
						if(isPharmacist){
							pharmacy.setPharmacist(pharmacist);
						}

						/* Setting PharmacistAgent */
						boolean isPharmacistAgent = false;
						if (pharmacyValues.get(i).getLabel().getValue().equals("PharmacistAgent FirstName")) {
								pharmacistAgent.setFirstName(pharmacyValues.get(i).getST().getValue().trim());
								isPharmacistAgent = true;
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("PharmacistAgent LastName")) {
								pharmacistAgent.setLastName(pharmacyValues.get(i).getST().getValue().trim());
								isPharmacistAgent = true;
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("PharmacistAgent MiddleName")) {
								pharmacistAgent.setMiddleName(pharmacyValues.get(i).getST().getValue().trim());
								isPharmacistAgent = true;
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("PharmacistAgent Prefix")) {
								pharmacistAgent.setPrefix(pharmacyValues.get(i).getST().getValue().trim());
								isPharmacistAgent = true;
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("PharmacistAgent Suffix")) {
								pharmacistAgent.setSuffix(pharmacyValues.get(i).getST().getValue().trim());
								isPharmacistAgent = true;
						}
						if(isPharmacistAgent){
							pharmacy.setPharmacistAgent(pharmacistAgent);
						}
						
						// Setting Pharmacy PhoneNumber
						PhoneType pharmPhone = new PhoneType();
						if (pharmacyValues.get(i).getLabel().getValue().equals("BN")) {
							pharmPhone.setQualifier(pharmacyValues.get(i).getLabel().getValue().trim());
							pharmPhone.setNumber(pharmacyValues.get(i).getST().getValue().trim());
							pharmacyPhoneNumType.getPhone().add(pharmPhone);
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("CP")) {
							pharmPhone.setQualifier(pharmacyValues.get(i).getLabel().getValue().trim());
							pharmPhone.setNumber(pharmacyValues.get(i).getST().getValue().trim());
							pharmacyPhoneNumType.getPhone().add(pharmPhone);
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("FX")) {
							pharmPhone.setQualifier("FX");
							pharmPhone.setNumber(pharmacyValues.get(i).getST().getValue().trim());
							pharmacyPhoneNumType.getPhone().add(pharmPhone);
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("HP")) {
							pharmPhone.setQualifier(pharmacyValues.get(i).getLabel().getValue().trim());
							pharmPhone.setNumber(pharmacyValues.get(i).getST().getValue().trim());
							pharmacyPhoneNumType.getPhone().add(pharmPhone);
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("NP")) {
							pharmPhone.setQualifier(pharmacyValues.get(i).getLabel().getValue().trim());
							pharmPhone.setNumber(pharmacyValues.get(i).getST().getValue().trim());
							pharmacyPhoneNumType.getPhone().add(pharmPhone);
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("TE")) {
							pharmPhone.setQualifier("TE");
							pharmPhone.setNumber(pharmacyValues.get(i).getST().getValue().trim());
							pharmacyPhoneNumType.getPhone().add(pharmPhone);
						}
						if (pharmacyValues.get(i).getLabel().getValue().equals("WP")) {
							pharmPhone.setQualifier(pharmacyValues.get(i).getLabel().getValue().trim());
							pharmPhone.setNumber(pharmacyValues.get(i).getST().getValue().trim());
							pharmacyPhoneNumType.getPhone().add(pharmPhone);	
						}
							
					}//if label is null
				}//for
				pharmacy.setAddress(pharmacyAddress);
				if (pharmacyPhoneNumType.getPhone() != null && pharmacyPhoneNumType.getPhone().size() > 0) {
					pharmacy.setPhoneNumbers(pharmacyPhoneNumType);
				}
		} //if pharmacyobservtationslot null
		refillResponse.setPharmacy(pharmacy); // Pharmacy Added to the response message
		
		/* Declarations for prescriber*/
		PrescriberType prescriber = new PrescriberType();
		AddressType addressPresc = new AddressType();
		MandatoryNameType prescName = new MandatoryNameType();
		PrescriberIDType prescId = new PrescriberIDType();
		SpecialtyType specialty = new SpecialtyType();
		PhoneNumbersType prescriberPhoneNumType = new PhoneNumbersType();
		MandatoryNameType prescriberAgent = new MandatoryNameType();
		ActRelationship prescriberRelation = ((ActEx)trim.getAct()).getRelationship().get("prescriber");
		
		if(prescriberRelation.getAct().getObservation().getValues()!=null){
			List<ObservationValueSlot>  prescriberValues = prescriberRelation.getAct().getObservation().getValues();
				for( int i=0; i < prescriberValues.size(); i++){
					if(prescriberValues.get(i).getLabel() != null && prescriberValues.get(i).getLabel().getValue().length() > 0){
						/* Setting prescriberId */
						if(prescId.getSPIOrFileIDOrStateLicenseNumber().size()<3){
							if(prescriberValues.get(i).getLabel().getValue().equals("SPI")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "SPI"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("SocialSecurity")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "SocialSecurity"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("PPONumber")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "PPONumber"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("UPIN")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "UPIN"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("DentistLicenseNumber")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "DentistLicenseNumber"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("MedicaidNumber")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "MedicaidNumber"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("FileID")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "FileID"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("DEANumber")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "DEANumber"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("MedicareNumber")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "MedicareNumber"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("StateLicenseNumber")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "StateLicenseNumber"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("PriorAuthorization")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "PriorAuthorization"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("NPI")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "NPI"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							if(prescriberValues.get(i).getLabel().getValue().equals("MutuallyDefined")){
								JAXBElement<String> prescIds = new JAXBElement<String>(
										new javax.xml.namespace.QName(
												"http://www.surescripts.com/messaging", "MutuallyDefined"),
										String.class, prescriberValues.get(i).getST().getValue().trim());
								prescId.getSPIOrFileIDOrStateLicenseNumber().add(prescIds);
							}
							prescriber.setIdentification(prescId);
						}//if(presId<3)
						
						/* Setting ClinicName */
						if(prescriberValues.get(i).getLabel().getValue().equals("ClinicName")){
							if(prescriberValues.get(i).getST().getValue().length() <= 35)
							prescriber.setClinicName(prescriberValues.get(i).getST().getValue().trim());
							else
								prescriber.setClinicName(prescriberValues.get(i).getST().getValue().substring(0,35).trim());
						}
						/*Setting name*/
						if(prescriberValues.get(i).getLabel().getValue().equals("Prescriber FirstName")){
							if(null != prescriberValues.get(i).getST().getValue() && prescriberValues.get(i).getST().getValue().trim().length() > 0)
								prescName.setFirstName(prescriberValues.get(i).getST().getValue().trim());
							else
								prescName.setFirstName(null);
							
							prescriber.setName(prescName);
						}
						if(prescriberValues.get(i).getLabel().getValue().equals("Prescriber LastName")){
							if(null != prescriberValues.get(i).getST().getValue() && prescriberValues.get(i).getST().getValue().trim().length() > 0)
								prescName.setLastName(prescriberValues.get(i).getST().getValue().trim());
							else
								prescName.setLastName(null);
							
							prescriber.setName(prescName);
						}
						if(prescriberValues.get(i).getLabel().getValue().equals("Prescriber MiddleName")){
							if(null != prescriberValues.get(i).getST().getValue() && prescriberValues.get(i).getST().getValue().trim().length() > 0)
								prescName.setMiddleName(prescriberValues.get(i).getST().getValue().trim());
							else
								prescName.setMiddleName(null);
							
							prescriber.setName(prescName);
						}
						if(prescriberValues.get(i).getLabel().getValue().equals("Prescriber Prefix")){
							prescName.setPrefix(prescriberValues.get(i).getST().getValue().trim());
							prescriber.setName(prescName);
						}
						if(prescriberValues.get(i).getLabel().getValue().equals("Prescriber Suffix")){
							prescName.setSuffix(prescriberValues.get(i).getST().getValue().trim());
							prescriber.setName(prescName);
						}
						
						/*Setting Speciality*/
						if(prescriberValues.get(i).getLabel().getValue().equals("Speciality")){
							specialty.setQualifier(prescriberValues.get(i).getST().getValue().trim());
							prescriber.setSpecialty(specialty);
						}
						if(prescriberValues.get(i).getLabel().getValue().equals("SpecialityCode")){
							specialty.setSpecialtyCode(prescriberValues.get(i).getST().getValue().trim());
							prescriber.setSpecialty(specialty);
						}
						
						/* Setting Address */
						if (prescriberValues.get(i).getLabel().getValue().equals("AddressLine1")) {
							addressPresc.setAddressLine1(prescriberValues.get(i).getST().getValue().trim());
							prescriber.setAddress(addressPresc);
						}

						if (prescriberValues.get(i).getLabel().getValue().equals("AddressLine2")) {
							addressPresc.setAddressLine2(prescriberValues.get(i).getST().getValue().trim());
							prescriber.setAddress(addressPresc);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("City")) {
							addressPresc.setCity(prescriberValues.get(i).getST().getValue().trim());
							prescriber.setAddress(addressPresc);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("State")) {
							addressPresc.setState(prescriberValues.get(i).getST().getValue().trim());
							prescriber.setAddress(addressPresc);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("ZipCode")) {
							addressPresc.setZipCode(prescriberValues.get(i).getST().getValue().trim());
							prescriber.setAddress(addressPresc);
						}
						
						/* Setting Email */
						if (prescriberValues.get(i).getLabel().getValue().equals("Email")) {
							prescriber.setEmail(prescriberValues.get(i).getST().getValue().trim());
						}

						// Setting Prescriber PhoneNumber
						PhoneType prescPhone = new PhoneType();
						if (prescriberValues.get(i).getLabel().getValue().equals("BN")) {
							prescPhone.setQualifier(prescriberValues.get(i).getLabel().getValue().trim());
							prescPhone.setNumber(prescriberValues.get(i).getST().getValue().trim());
							prescriberPhoneNumType.getPhone().add(prescPhone);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("CP")) {
							prescPhone.setQualifier(prescriberValues.get(i).getLabel().getValue().trim());
							prescPhone.setNumber(prescriberValues.get(i).getST().getValue().trim());
							prescriberPhoneNumType.getPhone().add(prescPhone);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("FX")) {
							prescPhone.setQualifier("FX");
							prescPhone.setNumber(prescriberValues.get(i).getST().getValue().trim());
							prescriberPhoneNumType.getPhone().add(prescPhone);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("HP")) {
							prescPhone.setQualifier(prescriberValues.get(i).getLabel().getValue().trim());
							prescPhone.setNumber(prescriberValues.get(i).getST().getValue().trim());
							prescriberPhoneNumType.getPhone().add(prescPhone);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("NP")) {
							prescPhone.setQualifier(prescriberValues.get(i).getLabel().getValue().trim());
							prescPhone.setNumber(prescriberValues.get(i).getST().getValue().trim());
							prescriberPhoneNumType.getPhone().add(prescPhone);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("TE")) {
							prescPhone.setQualifier("TE");
							prescPhone.setNumber(prescriberValues.get(i).getST().getValue().trim());
							prescriberPhoneNumType.getPhone().add(prescPhone);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("WP")) {
							prescPhone.setQualifier(prescriberValues.get(i).getLabel().getValue().trim());
							prescPhone.setNumber(prescriberValues.get(i).getST().getValue().trim());
							prescriberPhoneNumType.getPhone().add(prescPhone);
						}
							
						// Setting PrescriberAgent
						if (prescriberValues.get(i).getLabel().getValue().equals("PrescriberAgent FirstName")) {
							prescriberAgent.setFirstName(prescriberValues.get(i).getST().getValue().trim());
								prescriber.setPrescriberAgent(prescriberAgent);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("PrescriberAgent LastName")) {
							prescriberAgent.setLastName(prescriberValues.get(i).getST().getValue().trim());
								prescriber.setPrescriberAgent(prescriberAgent);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("PrescriberAgent MiddleName")) {
							prescriberAgent.setMiddleName(prescriberValues.get(i).getST().getValue().trim());
								prescriber.setPrescriberAgent(prescriberAgent);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("PrescriberAgent Prefix")) {
							prescriberAgent.setPrefix(prescriberValues.get(i).getST().getValue().trim());
								prescriber.setPrescriberAgent(prescriberAgent);
						}
						if (prescriberValues.get(i).getLabel().getValue().equals("PrescriberAgent Suffix")) {
							prescriberAgent.setSuffix(prescriberValues.get(i).getST().getValue().trim());
								prescriber.setPrescriberAgent(prescriberAgent);
						}
					}//if label is null
				}//for
				if (prescriberPhoneNumType.getPhone() != null && prescriberPhoneNumType.getPhone().size() > 0) {
					prescriber.setPhoneNumbers(prescriberPhoneNumType);
				}
		} // if prescriber relationship is null
		refillResponse.setPrescriber(prescriber); // Prescriber Added to the response message
		
		PatientType patient = new PatientType();
		MandatoryPatientNameType patName = new MandatoryPatientNameType();
		List<ObservationValueSlot> patinetValues = ((ActEx) trim.getAct()).getRelationship().
							get("patient").getAct().getObservation().getValues();
		for (int i = 0; i < patinetValues.size(); i++) {
			if (patinetValues.get(i).getST() != null) {
				if (patinetValues.get(i).getST().getValue().trim().length() == 0)
					patinetValues.get(i).setST(null);
			} else if (patinetValues.get(i).getTS() != null) {
				if (patinetValues.get(i).getTS().getValue().trim().length() == 0)
					patinetValues.get(i).setTS(null);
			} else if (patinetValues.get(i).getCE() != null) {
				if (patinetValues.get(i).getCE().getDisplayName().trim()
						.length() == 0)
					headerValues.get(i).setCE(null);
			} else if (patinetValues.get(i).getINT() != null) {
				if (patinetValues.get(i).getINT().toString().trim().length() == 0)
					patinetValues.get(i).setINT(null);
			}
		}
		if(null != patinetValues.get(1).getST()) {
			patName.setFirstName(patinetValues.get(1).getST().getValue().trim());
		}
		if(null != patinetValues.get(2).getST()) {
			patName.setMiddleName(patinetValues.get(2).getST().getValue().trim());
		}
		if(null != patinetValues.get(0).getST()) {
			patName.setLastName(patinetValues.get(0).getST().getValue().trim());
		}
		if(null != patinetValues.get(6).getST()) {
			patName.setPrefix(patinetValues.get(6).getST().getValue().trim());
		}
		if(null != patinetValues.get(7).getST()) {
			patName.setSuffix(patinetValues.get(7).getST().getValue().trim());
		}
		patient.setName(patName);
		
		if(null != patinetValues.get(3).getTS())
			patient.setDateOfBirth(patinetValues.get(3).getTS().getValue().substring(0,8));
		if(null != patinetValues.get(4).getST())
			patient.setGender( GenderType.fromValue(patinetValues.get(4).getST().getValue()
				.substring(0, 1).toUpperCase()));
		
		AddressType patAddress = new AddressType();
		if(null != patinetValues.get(8).getST() && patinetValues.get(8).getST().getValue().trim().length() > 0) {
			patAddress.setAddressLine1(patinetValues.get(8).getST().getValue().trim());
		}
		if(null != patinetValues.get(9).getST() && patinetValues.get(9).getST().getValue().trim().length() > 0) {
			patAddress.setAddressLine2(patinetValues.get(9).getST().getValue().trim());
		}
		if(null != patinetValues.get(10).getST() && patinetValues.get(10).getST().getValue().trim().length() > 0) {
			patAddress.setCity(patinetValues.get(10).getST().getValue().trim());
		}
		if(null != patinetValues.get(11).getST() && patinetValues.get(11).getST().getValue().trim().length() > 0) {
			patAddress.setState(patinetValues.get(11).getST().getValue().trim());
		}
		if(null != patinetValues.get(12).getST() && patinetValues.get(12).getST().getValue().trim().length() > 0) {
			patAddress.setZipCode(patinetValues.get(12).getST().getValue().trim());
		}
		patient.setAddress(patAddress);
		
		PatientIDType patientIdType= new PatientIDType();
		PhoneNumbersType patientPhoneNumType = new PhoneNumbersType();
		// Up to index 12 it is fixed in trim. So we will start with 13 and check for ph nums and ID's.
		for (int i = 13; i < patinetValues.size(); i++) {
			if(null != patinetValues.get(i).getLabel() && patinetValues.get(i).getLabel().getValue().length() > 0) {
				
				// Setting Email.
				if (patinetValues.get(i).getLabel().getValue().equals("Email")) {
					patient.setEmail(patinetValues.get(i).getST().getValue().trim());
				}
				// Setting patient Identification.
				if(patientIdType.getFileIDOrMedicareNumberOrMedicaidNumber().size() < 2){
					if(patinetValues.get(i).getLabel().getValue().equals("FileID")){
						JAXBElement<String> patientIds = new JAXBElement<String>(
								new javax.xml.namespace.QName(
										"http://www.surescripts.com/messaging", "FileID"),
								String.class, patinetValues.get(i).getST().getValue().trim());
						patientIdType.getFileIDOrMedicareNumberOrMedicaidNumber().add(patientIds);
					} else if(patinetValues.get(i).getLabel().getValue().equals("MedicareNumber")){
						JAXBElement<String> patientIds = new JAXBElement<String>(
								new javax.xml.namespace.QName(
										"http://www.surescripts.com/messaging", "MedicareNumber"),
								String.class, patinetValues.get(i).getST().getValue().trim());
						patientIdType.getFileIDOrMedicareNumberOrMedicaidNumber().add(patientIds);
					} else if(patinetValues.get(i).getLabel().getValue().equals("MedicaidNumber")){
						JAXBElement<String> patientIds = new JAXBElement<String>(
								new javax.xml.namespace.QName(
										"http://www.surescripts.com/messaging", "MedicaidNumber"),
								String.class, patinetValues.get(i).getST().getValue().trim());
						patientIdType.getFileIDOrMedicareNumberOrMedicaidNumber().add(patientIds);
					} else if(patinetValues.get(i).getLabel().getValue().equals("PPONumber")){
						JAXBElement<String> patientIds = new JAXBElement<String>(
								new javax.xml.namespace.QName(
										"http://www.surescripts.com/messaging", "PPONumber"),
								String.class, patinetValues.get(i).getST().getValue().trim());
						patientIdType.getFileIDOrMedicareNumberOrMedicaidNumber().add(patientIds);
					} else if(patinetValues.get(i).getLabel().getValue().equals("SocialSecurity")){
						JAXBElement<String> patientIds = new JAXBElement<String>(
								new javax.xml.namespace.QName(
										"http://www.surescripts.com/messaging", "SocialSecurity"),
								String.class, patinetValues.get(i).getST().getValue().trim());
						patientIdType.getFileIDOrMedicareNumberOrMedicaidNumber().add(patientIds);
					} else if(patinetValues.get(i).getLabel().getValue().equals("PayerID")){
						JAXBElement<String> patientIds = new JAXBElement<String>(
								new javax.xml.namespace.QName(
										"http://www.surescripts.com/messaging", "PayerID"),
								String.class, patinetValues.get(i).getST().getValue().trim());
						patientIdType.getFileIDOrMedicareNumberOrMedicaidNumber().add(patientIds);
					} else if(patinetValues.get(i).getLabel().getValue().equals("PriorAuthorization")){
						JAXBElement<String> patientIds = new JAXBElement<String>(
								new javax.xml.namespace.QName(
										"http://www.surescripts.com/messaging", "PriorAuthorization"),
								String.class, patinetValues.get(i).getST().getValue().trim());
						patientIdType.getFileIDOrMedicareNumberOrMedicaidNumber().add(patientIds);
					} else if(patinetValues.get(i).getLabel().getValue().equals("BIN")){
						JAXBElement<String> patientIds = new JAXBElement<String>(
								new javax.xml.namespace.QName(
										"http://www.surescripts.com/messaging", "BIN"),
								String.class, patinetValues.get(i).getST().getValue().trim());
						patientIdType.getFileIDOrMedicareNumberOrMedicaidNumber().add(patientIds);
					} else if(patinetValues.get(i).getLabel().getValue().equals("MutuallyDefined")){
						JAXBElement<String> patientIds = new JAXBElement<String>(
								new javax.xml.namespace.QName(
										"http://www.surescripts.com/messaging", "MutuallyDefined"),
								String.class, patinetValues.get(i).getST().getValue().trim());
						patientIdType.getFileIDOrMedicareNumberOrMedicaidNumber().add(patientIds);
					}
					patient.setIdentification(patientIdType);
				} // if(patientIdType < 2)
					
				PhoneType patientPhone = new PhoneType();
				if (patinetValues.get(i).getLabel().getValue().equals("BN")) {
					patientPhone.setQualifier(patinetValues.get(i).getLabel().getValue().trim());
					patientPhone.setNumber(patinetValues.get(i).getST().getValue().trim());
					patientPhoneNumType.getPhone().add(patientPhone);
				} else if (patinetValues.get(i).getLabel().getValue().equals("CP")) {
					patientPhone.setQualifier(patinetValues.get(i).getLabel().getValue().trim());
					patientPhone.setNumber(patinetValues.get(i).getST().getValue().trim());
					patientPhoneNumType.getPhone().add(patientPhone);
				} else if (patinetValues.get(i).getLabel().getValue().equals("FX")) {
					patientPhone.setQualifier(patinetValues.get(i).getLabel().getValue().trim());
					patientPhone.setNumber(patinetValues.get(i).getST().getValue().trim());
					patientPhoneNumType.getPhone().add(patientPhone);
				} else if (patinetValues.get(i).getLabel().getValue().equals("HP")) {
					patientPhone.setQualifier(patinetValues.get(i).getLabel().getValue().trim());
					patientPhone.setNumber(patinetValues.get(i).getST().getValue().trim());
					patientPhoneNumType.getPhone().add(patientPhone);
				} else if (patinetValues.get(i).getLabel().getValue().equals("NP")) {
					patientPhone.setQualifier(patinetValues.get(i).getLabel().getValue().trim());
					patientPhone.setNumber(patinetValues.get(i).getST().getValue().trim());
					patientPhoneNumType.getPhone().add(patientPhone);
				} else if (patinetValues.get(i).getLabel().getValue().equals("TE")) {
					patientPhone.setQualifier(patinetValues.get(i).getLabel().getValue().trim());
					patientPhone.setNumber(patinetValues.get(i).getST().getValue().trim());
					patientPhoneNumType.getPhone().add(patientPhone);
				} else if (patinetValues.get(i).getLabel().getValue().equals("WP")) {
					patientPhone.setQualifier(patinetValues.get(i).getLabel().getValue().trim());
					patientPhone.setNumber(patinetValues.get(i).getST().getValue().trim());
					patientPhoneNumType.getPhone().add(patientPhone);
				}
			}
		}
		if (patientPhoneNumType.getPhone() != null && patientPhoneNumType.getPhone().size() > 0) {
			patient.setPhoneNumbers(patientPhoneNumType);
		}
		refillResponse.setPatient(patient);
		
		// Setting Supervisor tag.
		ActRelationship supervisorRelation = ((ActEx)trim.getAct()).getRelationship().get("supervisor");
		if(supervisorRelation.getAct().getObservation().getValues() != null && 
				supervisorRelation.getAct().getObservation().getValues().size() > 0) {
			
			SupervisorType supervisor = new SupervisorType();
			SupervisorIDType supervisorId = new SupervisorIDType();
			PhoneNumbersType supervisorPhoneNumType = new PhoneNumbersType();
			MandatoryNameType supervisorName = new MandatoryNameType();
			SpecialtyType supervisorSpecialty = new SpecialtyType();
			MandatoryNameType supervisorPrescriberAgent = new MandatoryNameType();
			AddressType addressSupervisor= new AddressType();
			
			List<ObservationValueSlot>  supervisorValues = supervisorRelation.getAct().getObservation().getValues();			
			for(int i = 0; i < supervisorValues.size(); i++) {
				if(supervisorValues.get(i).getLabel() != null && supervisorValues.get(i).getLabel().getValue().length() > 0){
					// Setting prescriberId 
					if(supervisorId.getSPIOrFileIDOrStateLicenseNumber().size() < 3) {
						if(supervisorValues.get(i).getLabel().getValue().equals("SPI")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "SPI"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("FileID")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "FileID"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("StateLicenseNumber")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "StateLicenseNumber"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("MedicareNumber")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "MedicareNumber"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}							
						if(supervisorValues.get(i).getLabel().getValue().equals("MedicaidNumber")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "MedicaidNumber"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("DentistLicenseNumber")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "DentistLicenseNumber"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("UPIN")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "UPIN"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("PPONumber")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "PPONumber"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("DEANumber")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "DEANumber"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("SocialSecurity")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "SocialSecurity"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("NPI")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "NPI"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("PriorAuthorization")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "PriorAuthorization"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						if(supervisorValues.get(i).getLabel().getValue().equals("MutuallyDefined")){
							JAXBElement<String> supervisorIds = new JAXBElement<String>(
									new javax.xml.namespace.QName(
											"http://www.surescripts.com/messaging", "MutuallyDefined"),
									String.class, supervisorValues.get(i).getST().getValue().trim());
							supervisorId.getSPIOrFileIDOrStateLicenseNumber().add(supervisorIds);
						}
						supervisor.setIdentification(supervisorId);
					}//if(supervisorId<3)
						
					// Setting ClinicName
					if(supervisorValues.get(i).getLabel().getValue().equals("ClinicName")) {
						supervisor.setClinicName(supervisorValues.get(i).getST().getValue().trim());
					}
					
					// Setting name
					if (supervisorValues.get(i).getLabel().getValue().equals("Supervisor FirstName")) {
						supervisorName.setFirstName(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setName(supervisorName);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("Supervisor LastName")) {
						supervisorName.setLastName(supervisorValues.get(i).getST().getValue().trim());
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("Supervisor MiddleName")) {
						supervisorName.setMiddleName(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setName(supervisorName);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("Supervisor Prefix")) {
						supervisorName.setPrefix(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setName(supervisorName);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("Supervisor Suffix")) {
						supervisorName.setSuffix(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setName(supervisorName);
					}
					
					// Setting Speciality
					if(supervisorValues.get(i).getLabel().getValue().equals("Speciality")){
						supervisorSpecialty.setQualifier(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setSpecialty(supervisorSpecialty);
					}
					if(supervisorValues.get(i).getLabel().getValue().equals("SpecialityCode")){
						supervisorSpecialty.setSpecialtyCode(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setSpecialty(supervisorSpecialty);
					}
					
					// Setting PrescriberAgent
					if (supervisorValues.get(i).getLabel().getValue().equals("PrescriberAgent FirstName")) {
						supervisorPrescriberAgent.setFirstName(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setPrescriberAgent(supervisorPrescriberAgent);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("PrescriberAgent LastName")) {
						supervisorPrescriberAgent.setLastName(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setPrescriberAgent(supervisorPrescriberAgent);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("PrescriberAgent MiddleName")) {
						supervisorPrescriberAgent.setMiddleName(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setPrescriberAgent(supervisorPrescriberAgent);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("PrescriberAgent Prefix")) {
						supervisorPrescriberAgent.setPrefix(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setPrescriberAgent(supervisorPrescriberAgent);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("PrescriberAgent Suffix")) {
						supervisorPrescriberAgent.setSuffix(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setPrescriberAgent(supervisorPrescriberAgent);
					}
					
					// Setting Address
					if (supervisorValues.get(i).getLabel().getValue().equals("AddressLine1")) {
						addressSupervisor.setAddressLine1(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setAddress(addressSupervisor);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("AddressLine2")) {
						addressSupervisor.setAddressLine2(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setAddress(addressSupervisor);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("City")) {
						addressSupervisor.setCity(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setAddress(addressSupervisor);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("State")) {
						addressSupervisor.setState(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setAddress(addressSupervisor);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("ZipCode")) {
						addressSupervisor.setZipCode(supervisorValues.get(i).getST().getValue().trim());
						supervisor.setAddress(addressSupervisor);
					}
					
					// Setting Email
					if (supervisorValues.get(i).getLabel().getValue().equals("Email")) {
						supervisor.setEmail(supervisorValues.get(i).getST().getValue().trim());
					}
					
					// Setting Supervisor PhoneNumber
					PhoneType supervisorPhone = new PhoneType();
					if (supervisorValues.get(i).getLabel().getValue().equals("BN")) {
						supervisorPhone.setQualifier(supervisorValues.get(i).getLabel().getValue().trim());
						supervisorPhone.setNumber(supervisorValues.get(i).getST().getValue().trim());
						supervisorPhoneNumType.getPhone().add(supervisorPhone);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("CP")) {
						supervisorPhone.setQualifier(supervisorValues.get(i).getLabel().getValue().trim());
						supervisorPhone.setNumber(supervisorValues.get(i).getST().getValue().trim());
						supervisorPhoneNumType.getPhone().add(supervisorPhone);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("FX")) {
							supervisorPhone.setQualifier(supervisorValues.get(i).getLabel().getValue().trim());
							supervisorPhone.setNumber(supervisorValues.get(i).getST().getValue().trim());
							supervisorPhoneNumType.getPhone().add(supervisorPhone);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("HP")) {
							supervisorPhone.setQualifier(supervisorValues.get(i).getLabel().getValue().trim());
							supervisorPhone.setNumber(supervisorValues.get(i).getST().getValue().trim());
							supervisorPhoneNumType.getPhone().add(supervisorPhone);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("NP")) {
							supervisorPhone.setQualifier(supervisorValues.get(i).getLabel().getValue().trim());
							supervisorPhone.setNumber(supervisorValues.get(i).getST().getValue().trim());
							supervisorPhoneNumType.getPhone().add(supervisorPhone);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("TE")) {
							supervisorPhone.setQualifier(supervisorValues.get(i).getLabel().getValue().trim());
							supervisorPhone.setNumber(supervisorValues.get(i).getST().getValue().trim());
							supervisorPhoneNumType.getPhone().add(supervisorPhone);
					}
					if (supervisorValues.get(i).getLabel().getValue().equals("WP")) {
							supervisorPhone.setQualifier(supervisorValues.get(i).getLabel().getValue().trim());
							supervisorPhone.setNumber(supervisorValues.get(i).getST().getValue().trim());
							supervisorPhoneNumType.getPhone().add(supervisorPhone);
					}
				} // if label is null
			} // for loop
			if (supervisorPhoneNumType.getPhone() != null && supervisorPhoneNumType.getPhone().size() > 0) {
				supervisor.setPhoneNumbers(supervisorPhoneNumType);
			}
			refillResponse.setSupervisor(supervisor);
		} // null checking for supervisor relation
			
		
		ResponseMedicationType medicationPrescribed = new ResponseMedicationType();
		DrugCodedType drugCode = new DrugCodedType();
		QuantityType qty =new QuantityType();
		RefillsType refill = new RefillsType();
		
		/* Refill Quantity requested by the pharmacy */
		String priorRefill = "" ;
		String priorRefillQualifier = "";
		
		List<ResponseMedicationType.Diagnosis> diagnosisList = new ArrayList<ResponseMedicationType.Diagnosis>();
		HashMap<String, String> diagValues1 = new HashMap<String, String>();
		HashMap<String, String> diagValues2 = new HashMap<String, String>();
		PriorAuthorizationType priorAuthorization = new PriorAuthorizationType();
		
		ActRelationship medicationPrescribedRelation = ((ActEx)trim.getAct()).getRelationship().get("MedicationPrescribed");
		if(medicationPrescribedRelation.getAct().getObservation().getValues()!=null){
			List<ObservationValueSlot>  medicationPrescribedValues = medicationPrescribedRelation.getAct().getObservation().getValues();
				for( int i=0;i<medicationPrescribedValues.size();i++){
					if(!medicationPrescribedValues.get(i).getLabel().getValue().equals(null)){
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("DrugDescription")){
							medicationPrescribed.setDrugDescription(medicationPrescribedValues.get(i).getST().getValue().trim());
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("ProductCode")){
							drugCode.setProductCode(medicationPrescribedValues.get(i).getST().getValue().trim());
							medicationPrescribed.setDrugCoded(drugCode);
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("ProductCodeQualifier")){
							drugCode.setProductCodeQualifier(medicationPrescribedValues.get(i).getST().getValue().trim());
							medicationPrescribed.setDrugCoded(drugCode);
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("DosageForm")){
							drugCode.setDosageForm(pharmacyBean.getDosageForm(medicationPrescribedValues.get(i).getST().getValue().trim(), false));
							medicationPrescribed.setDrugCoded(drugCode);
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("Strength")){
							drugCode.setStrength(medicationPrescribedValues.get(i).getST().getValue().trim());
							medicationPrescribed.setDrugCoded(drugCode);
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("StrengthUnits")){
							drugCode.setStrengthUnits(sureBean.retrieveDrugQualCode(medicationPrescribedValues.get(i).getST().getValue().trim()));
							medicationPrescribed.setDrugCoded(drugCode);
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("DrugDBCode")){
							drugCode.setDrugDBCode(medicationPrescribedValues.get(i).getST().getValue().trim());
							medicationPrescribed.setDrugCoded(drugCode);
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("DrugDBCodeQualifier")){
							drugCode.setDrugDBCodeQualifier(medicationPrescribedValues.get(i).getST().getValue().trim());
							medicationPrescribed.setDrugCoded(drugCode);
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("Quantity(qualifier)")){
							qty.setQualifier(sureBean.retrieveDrugQualCode(medicationPrescribedValues.get(i).getST().getValue().trim()));
							medicationPrescribed.setQuantity(qty);
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("Quantity(value)")){
							qty.setValue(medicationPrescribedValues.get(i).getST().getValue().trim());
							medicationPrescribed.setQuantity(qty);
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("DaysSupply")){
							medicationPrescribed.setDaysSupply(medicationPrescribedValues.get(i).getST().getValue().trim());
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("Directions")){
							medicationPrescribed.setDirections(medicationPrescribedValues.get(i).getST().getValue().trim());
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("Note")){
							medicationPrescribed.setNote(medicationPrescribedValues.get(i).getST().getValue().trim());
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("Refills(qualifier)")){
							refill.setQualifier(medicationPrescribedValues.get(i).getST().getValue().trim());
							priorRefillQualifier = medicationPrescribedValues.get(i).getST().getValue().trim();
						}
						else {
							refill.setQualifier("R");
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("Refills(value)")){
							refill.setQuantity(medicationPrescribedValues.get(i).getST().getValue().trim());
							priorRefill = medicationPrescribedValues.get(i).getST().getValue().trim();
						}
						else {
							refill.setQuantity("0");
						}
						medicationPrescribed.setRefills(refill);
						
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("Substitutions")){
							if (medicationPrescribedValues.get(i).getST().getValue().equals("No")) {
								medicationPrescribed.setSubstitutions("1");
							}else{
								medicationPrescribed.setSubstitutions("0");
							}
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("LastFillDate")){
							String trimdate= medicationPrescribedValues.get(i).getST().getValue().trim();
							DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
							try {
								Date date = dateFormat.parse(trimdate);
								SimpleDateFormat simpleDf = new SimpleDateFormat("yyyyMMdd");
								medicationPrescribed.setLastFillDate(simpleDf.format(date));
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
						}
						/*Setting Diagnosis*/
						
						if(medicationPrescribedValues.get(i).getLabel().getValue().endsWith("1")){
							if(medicationPrescribedValues.get(i).getLabel().getValue().contains("ClinicalInformationQualifier#")){
								diagValues1.put("ClinicalInformationQualifier", medicationPrescribedValues.get(i).getST().getValue());
							}
							if(medicationPrescribedValues.get(i).getLabel().getValue().contains("Primary(qualifier)#")){
								diagValues1.put("Primary_qualifier", medicationPrescribedValues.get(i).getST().getValue());
							}
							if(medicationPrescribedValues.get(i).getLabel().getValue().contains("Primary(value)#")){
								diagValues1.put("Primary_value", medicationPrescribedValues.get(i).getST().getValue());
							}
							if(medicationPrescribedValues.get(i).getLabel().getValue().contains("Secondary(qualifier)#")){
								diagValues1.put("Secondary_qualifier", medicationPrescribedValues.get(i).getST().getValue());
							}
							if(medicationPrescribedValues.get(i).getLabel().getValue().contains("Secondary(value)#")){
								diagValues1.put("Secondary_value", medicationPrescribedValues.get(i).getST().getValue());
							}
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().endsWith("2")){
							if(medicationPrescribedValues.get(i).getLabel().getValue().contains("ClinicalInformationQualifier#")){
								diagValues2.put("ClinicalInformationQualifier", medicationPrescribedValues.get(i).getST().getValue());
							}
							if(medicationPrescribedValues.get(i).getLabel().getValue().contains("Primary(qualifier)#")){
								diagValues2.put("Primary_qualifier", medicationPrescribedValues.get(i).getST().getValue());
							}
							if(medicationPrescribedValues.get(i).getLabel().getValue().contains("Primary(value)#")){
								diagValues2.put("Primary_value", medicationPrescribedValues.get(i).getST().getValue());
							}
							if(medicationPrescribedValues.get(i).getLabel().getValue().contains("Secondary(qualifier)#")){
								diagValues2.put("Secondary_qualifier", medicationPrescribedValues.get(i).getST().getValue());
							}
							if(medicationPrescribedValues.get(i).getLabel().getValue().contains("Secondary(value)#")){
								diagValues2.put("Secondary_value", medicationPrescribedValues.get(i).getST().getValue());
							}
							
						}
						
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("PriorAuthorization(qualifier)")){
							priorAuthorization.setQualifier(medicationPrescribedValues.get(i).getST().getValue().trim());
							medicationPrescribed.setPriorAuthorization(priorAuthorization);
						}
						if(medicationPrescribedValues.get(i).getLabel().getValue().equals("PriorAuthorization(value)")){
							priorAuthorization.setValue(medicationPrescribedValues.get(i).getST().getValue().trim());
							medicationPrescribed.setPriorAuthorization(priorAuthorization);
						}
					}//if label is null
				}//for
		}//if medicationPrescribedobservtationslot null
		
		
		ResponseMedicationType.Diagnosis diagnosis1 = new ResponseMedicationType.Diagnosis();
		ResponseMedicationType.Diagnosis diagnosis2 = new ResponseMedicationType.Diagnosis();
		/*Setting diagnosis*/
		if(diagValues1.get("Primary_qualifier")!=null){
			DiagnosisType primary = new DiagnosisType();
			primary.setQualifier(diagValues1.get("Primary_qualifier"));
			primary.setValue(diagValues1.get("Primary_value"));
			diagnosis1.setPrimary(primary);
			diagnosis1.setClinicalInformationQualifier(diagValues1.get("ClinicalInformationQualifier"));
		}
		if(diagValues1.get("Secondary_qualifier")!=null){
			DiagnosisType secondary = new  DiagnosisType();
			secondary.setQualifier(diagValues1.get("Secondary_qualifier"));
			secondary.setValue(diagValues1.get("Secondary_value"));
			diagnosis1.setSecondary(secondary);
			diagnosis1.setClinicalInformationQualifier(diagValues1.get("ClinicalInformationQualifier"));
		}
		if(diagValues2.get("Primary_qualifier")!=null){
			DiagnosisType primary = new DiagnosisType();
			primary.setQualifier(diagValues2.get("Primary_qualifier"));
			primary.setValue(diagValues2.get("Primary_value"));
			diagnosis2.setPrimary(primary);
			diagnosis2.setClinicalInformationQualifier(diagValues2.get("ClinicalInformationQualifier"));
		}
		if(diagValues2.get("Secondary_qualifier")!=null){
			DiagnosisType secondary = new  DiagnosisType();
			secondary.setQualifier(diagValues2.get("Secondary_qualifier"));
			secondary.setValue(diagValues2.get("Secondary_value"));
			diagnosis2.setSecondary(secondary);
			diagnosis2.setClinicalInformationQualifier(diagValues2.get("ClinicalInformationQualifier"));
		}
		if(diagValues1.get("Primary_qualifier")!=null || diagValues1.get("Secondary_qualifier")!=null)
			diagnosisList.add(diagnosis1);
		if(diagValues2.get("Secondary_qualifier")!=null || diagValues2.get("Primary_qualifier")!=null)
			diagnosisList.add(diagnosis2);
		//Setting Diagnosis to RefillResponse
		medicationPrescribed.diagnosis = diagnosisList;
		
		ResponseType response = new ResponseType();
		for (int i = 0; i < responseValues.size(); i++) {
			if (responseValues.get(i).getST() != null) {
				if (responseValues.get(i).getST().getValue().trim().length() == 0)
					responseValues.get(i).setST(null);
			} else if (responseValues.get(i).getTS() != null) {
				if (responseValues.get(i).getTS().getValue().trim().length() == 0)
					responseValues.get(i).setTS(null);
			} else if (responseValues.get(i).getCE() != null) {
				if (responseValues.get(i).getCE().getDisplayName().trim()
						.length() == 0)
					responseValues.get(i).setCE(null);
			} else if (responseValues.get(i).getINT() != null) {
				if (responseValues.get(i).getINT().toString().trim().length() == 0)
					responseValues.get(i).setINT(null);
			}
		}
		List<ObservationValueSlot> medicaitonReqValues = ((ActEx) trim.getAct()).getRelationship().
						get("MedicationRequested").getAct().getObservation().getValues();
		for (int i = 0; i < medicaitonReqValues.size(); i++) {
			if (medicaitonReqValues.get(i).getST() != null) {
				if (medicaitonReqValues.get(i).getST().getValue().trim().length() == 0)
					medicaitonReqValues.get(i).setST(null);
			} else if (medicaitonReqValues.get(i).getTS() != null) {
				if (medicaitonReqValues.get(i).getTS().getValue().trim().length() == 0)
					medicaitonReqValues.get(i).setTS(null);
			} else if (medicaitonReqValues.get(i).getCE() != null) {
				if (medicaitonReqValues.get(i).getCE().getDisplayName().trim()
						.length() == 0)
					medicaitonReqValues.get(i).setCE(null);
			} else if (medicaitonReqValues.get(i).getINT() != null) {
				if (medicaitonReqValues.get(i).getINT().toString().trim().length() == 0)
					medicaitonReqValues.get(i).setINT(null);
			}
		}
		String ceValue = null;
		String newRefill = null;
		if (null != responseValues.get(0).getCE())
			ceValue = responseValues.get(0).getCE().toString();

	if(null != ceValue && ceValue.equals("Approved")){
		
		boolean isCS = false;
		
		List<ObservationValueSlot> medPrescribed = ((ActEx) trim.getAct()).getRelationship().get("MedicationPrescribed").
			getAct().getObservation().getValues();
		for(ObservationValueSlot medPrsc: medPrescribed) {
			if (medPrsc.getLabel().getValue().trim().equals("IsScheduleDrug")) {
				if (medPrsc.getST().getValue().trim().equals("Yes")) {
					isCS = true;
				}
			}
		}
		
		if (isCS) {
			TolvenLogger.info("Approve selected for Controlled Substance.", SureScriptsCommunicator.class);
			DeniedNewPrescriptionToFollow deniedNewPrescription = new DeniedNewPrescriptionToFollow();
			deniedNewPrescription.setNote("Schedule III to V:The approved controlled substance Rx will be faxed.");
			response.setDeniedNewPrescriptionToFollow(deniedNewPrescription);
			refill.setQuantity("0");
			refill.setQualifier("R");
		} else {
				/*Obtaining newRefill*/
				if(null != medicaitonReqValues.get(0).getST())
					newRefill = medicaitonReqValues.get(0).getST().getValue();
						if((null != priorRefillQualifier && priorRefillQualifier.equals("PRN")) ||
								(priorRefillQualifier.equals(priorRefill))){
							Approved approve = new Approved();
							if(null != responseValues.get(1).getST())
								approve.setNote(responseValues.get(1).getST().getValue().trim());
							else
								approve.setNote("No Notes were entered.");
							
								if(null != medicaitonReqValues.get(2).getCE() && 
										null != medicaitonReqValues.get(2).getCE().getDisplayName()
										&& medicaitonReqValues.get(2).getCE().getDisplayName().equals("R")){  		//PRN - Non Zero
									response.setApproved(approve);
									refill.setQualifier("R");
									refill.setQuantity(newRefill);
									medicationPrescribed.setRefills(refill);
								}else{ 																				// PRN - PRN
									response.setApproved(approve);      
									refill.setQualifier("PRN");
									refill.setQuantity(null);
								}
						}else if(null != priorRefillQualifier && priorRefillQualifier.equals("R")){
								if(priorRefill.equals("") || priorRefill.equals("0")){
									
									Approved approve = new Approved();
									if(null != responseValues.get(1).getST())
										approve.setNote(responseValues.get(1).getST().getValue().trim());
									else
										approve.setNote("No Notes were entered.");
										response.setApproved(approve);
									
										if(null != medicaitonReqValues.get(2).getCE() && 
												null != medicaitonReqValues.get(2).getCE().getDisplayName()
												&& medicaitonReqValues.get(2).getCE().getDisplayName().equals("R")){ // Zero - Non Zero & "" - Non Zero
											refill.setQualifier("R");
											refill.setQuantity(newRefill);
										}else{  																	 // Zero - PRN & "" - PRN
											refill.setQualifier("PRN");
											refill.setQuantity(null);
										}
								}else{
										if(!priorRefill.equals(newRefill)){												
											ApprovedWithChanges appWithChanges = new ApprovedWithChanges();
											
											if(null != responseValues.get(1).getST())
												appWithChanges.setNote(responseValues.get(1).getST().getValue().trim());
											else
												appWithChanges.setNote("No Notes were entered.");
											response.setApprovedWithChanges(appWithChanges);
											
												if(medicaitonReqValues.get(2).getCE().getDisplayName().equals("R")){
													refill.setQualifier("R");       								// Non Zero - Other Non Zero
													refill.setQuantity(newRefill);
												}else{
													refill.setQualifier("PRN"); 
													refill.setQuantity(null);            								// Non Zero - PRN
												}
										}else {
											if(medicaitonReqValues.get(2).getCE().getDisplayName().equals("R")){
												Approved approve = new Approved();
												if(null != responseValues.get(1).getST())
													approve.setNote(responseValues.get(1).getST().getValue().trim());
												else
													approve.setNote("No Notes were entered.");
												
													response.setApproved(approve);
													refill.setQualifier("R");       								         // Non Zero - Non Zero
													refill.setQuantity(newRefill);
											}else{
												ApprovedWithChanges appWithChanges = new ApprovedWithChanges();
												if(null != responseValues.get(1).getST())
													appWithChanges.setNote(responseValues.get(1).getST().getValue().trim());
												else
													appWithChanges.setNote("No Notes were entered.");
												
													response.setApprovedWithChanges(appWithChanges);
													refill.setQualifier("PRN");       								         // Non Zero - PRN
													refill.setQuantity(null);
											}
										}
								}
							
						}
			}		
				
		} else if (ceValue.equals("Denied New Prescription")) {
			/* Denied, New Prescription To Follow */
			DeniedNewPrescriptionToFollow deniedNewPrescription = new DeniedNewPrescriptionToFollow();
			if(((ActEx) trim.getAct()).getRelationship().
					get("MedicationRequested").getAct().getObservation().getValues().get(3).getST().getValue().equals("3")
					|| ((ActEx) trim.getAct()).getRelationship().
					get("MedicationRequested").getAct().getObservation().getValues().get(3).getST().getValue().equals("4")
					|| ((ActEx) trim.getAct()).getRelationship().
					get("MedicationRequested").getAct().getObservation().getValues().get(3).getST().getValue().equals("5")){ // Check for Controlled Substance
					
				/* Denied, New Prescription To Follow */
				deniedNewPrescription.setNote(responseValues.get(1).getST().getValue().trim());
			} else {
				if(null != responseValues.get(1).getST()) {
					deniedNewPrescription.setNote(responseValues.get(1).getST().getValue().trim());
				} else {
					deniedNewPrescription.setNote("No Notes were entered.");
				}
			}
			response.setDeniedNewPrescriptionToFollow(deniedNewPrescription);
			refill.setQuantity("0");
			refill.setQualifier("R");
		} else if (ceValue.equals("Denied")) {
			/* Denied Case */
			CE denialReasonCode = null;
			Denied denied = new Denied();
			if(null != responseValues.get(2).getCE())
				denialReasonCode = responseValues.get(2).getCE();
			if (denialReasonCode.getDisplayName().equals("Others")) {
				denied.setDenialReason(((ActEx)trim.getAct()).getRelationship().get("response")
						.getAct().getObservation().getValues().get(3).getST()
						.getValue().trim());
			}else if(!denialReasonCode.getDisplayName().equals("")){
				denied.setDenialReasonCode(denialReasonCode.toString().split(" ")[0].trim());
			}
			response.setDenied(denied);
			refill.setQuantity("0");
			refill.setQualifier("R");
		}
		// Setting the full response to the message
		refillResponse.setResponse(response);
		
		/*Setting written date to current date*/
		medicationPrescribed.setWrittenDate(getWrittenDate());
		
		refillResponse.setMedicationPrescribed(medicationPrescribed);
		body.setRefillResponse(refillResponse);
		message.setBody(body);
		locationOfRefillResponse = locationOfRefillResponse
				+ "RefillResponse_"+message.getHeader().getMessageID()+".xml";
		TolvenLogger.info("RefillResponse Message Generated. ", SureScriptsCommunicator.class);
		SureScriptsCommunicator objRefillResponse = new SureScriptsCommunicator();
		objRefillResponse.createMessageFile(message, locationOfRefillResponse, trim.getTolvenEventIds().get(0).getAccountId().trim(),privateKey);
	}



	
	/**
	 * Rule triggered method
	 * @param trim
	 */
	public void addPrescriber(TrimEx trim,PrivateKey privateKey){
		
		if(validateMasterAccountAssociation(
				Long.parseLong(trim.getTolvenEventIds().get(0).getAccountId().trim()))) {
			
			if (((ActEx) trim.getAct()).getRelationship()
					.get("updateStatus") != null && ((ActEx) trim.getAct()).getRelationship()
					.get("updateStatus").getAct().getObservation()
					.getValues().get(0).getST().getValue().length() > 0) {
				updatePrescriberLocation(trim,privateKey);
			} else if(((ActEx) trim.getAct()).getRelationship()
					.get("prescriber").getAct().getObservation()
					.getValues().get(6).getST().getValue() == null
					 || ((ActEx) trim.getAct()).getRelationship()
					.get("prescriber").getAct().getObservation()
					.getValues().get(6).getST().getValue().equals("")){
				createAddPrescriberMessage(trim,privateKey);
			} else{
				createAddPrescriberLocation(trim,privateKey);
			}
		} else {
			TolvenLogger.error("Master User Account Not Associated with this Account: " +
					trim.getTolvenEventIds().get(0).getAccountId().trim(), SureScriptsCommunicator.class);
		}
	}
	
	/**
	 * @author unni.s@cyrusxp.com
	 * Method used to create UpdatePrescriberLocation message.
	 * @param physicianTrim
	 */
	private void updatePrescriberLocation(TrimEx physicianTrim,PrivateKey privateKey) {
		String messageId = null;
		String dEANumber = null;
		String SPI = null;
		
		if (tproperties.getProperty("eprescription.surescripts.messages.directory") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.messages.directory- .", SureScriptsCommunicator.class);
			return;
		}
		if (tproperties.getProperty("eprescription.surescripts.adminconsole.username") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.adminconsole.username- .", SureScriptsCommunicator.class);
			return;
		}
		if (tproperties.getProperty("eprescription.surescripts.adminconsole.password") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.adminconsole.password- .", SureScriptsCommunicator.class);
			return;
		}
		
		String updatePrescriberLocationMessagePath = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/");
		File messageOutbox = new File(updatePrescriberLocationMessagePath);
		if (!messageOutbox.exists()) {
			messageOutbox.mkdirs();
		}
		
		List<ObservationValueSlot> prescriberOVS = ((ActEx) physicianTrim.getAct()).getRelationship().get("prescriber").
		getAct().getObservation().getValues();
			for(int i=0; i< prescriberOVS.size(); i++){
				if(prescriberOVS.get(i).getST() != null){
					if(prescriberOVS.get(i).getST().getValue().trim().length() == 0)
						prescriberOVS.get(i).setST(null);
				}else if(prescriberOVS.get(i).getTS() != null){
					if(prescriberOVS.get(i).getTS().getValue().trim().length() == 0)
						prescriberOVS.get(i).setTS(null);
				}else if(prescriberOVS.get(i).getCE() != null){
					if(prescriberOVS.get(i).getCE().getDisplayName().trim().length() == 0)
						prescriberOVS.get(i).setCE(null);
				}else if(prescriberOVS.get(i).getINT() != null){
					if(String.valueOf(prescriberOVS.get(i).getINT().getValue()).trim().length() < 0)
						prescriberOVS.get(i).setINT(null);
				}
			}
		
		String source = SOURCE_ADDRESS;
		String destination = DESTINATION_ADDRESS;
		if (null != prescriberOVS.get(10).getST()) {
			messageId = prescriberOVS.get(10).getST().getValue().trim();
		}
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername(tproperties.getProperty("eprescription.surescripts.adminconsole.username"));
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeDirectoryCredentials(tproperties.getProperty("eprescription.surescripts.adminconsole.password")));
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		
		/* Header Body */
		HeaderType mainHeader = new HeaderType();
		mainHeader.setFrom(source);
		mainHeader.setTo(destination);
		mainHeader.setMessageID(messageId);
		mainHeader.setSecurity(security);
		mainHeader.setSentTime(getCurrentUTC());
		
		DirectoryPrescriberType prescriber = new DirectoryPrescriberType();
		DirectoryInformationType directoryinformation = new DirectoryInformationType();
		if (null != prescriberOVS.get(0).getINT())
			directoryinformation.setAccountID(String.valueOf(prescriberOVS.get(0).getINT().getValue()));
		if (null != prescriberOVS.get(1).getINT())
			directoryinformation.setPortalID(String.valueOf(prescriberOVS.get(1).getINT().getValue()));
		if (null != prescriberOVS.get(3).getST())
			directoryinformation.setServiceLevel(prescriberOVS.get(3).getST().getValue());
		if (null != prescriberOVS.get(4).getTS())
			directoryinformation.setActiveStartTime(getDateFromString(prescriberOVS.get(4).getTS().getValue()));
		if (null != prescriberOVS.get(5).getTS())
			directoryinformation.setActiveEndTime(getDateFromString(prescriberOVS.get(5).getTS().getValue()));
		prescriber.setDirectoryInformation(directoryinformation);
		
		PrescriberIDType identification = new PrescriberIDType();
		if (null != prescriberOVS.get(7).getST())
			dEANumber = prescriberOVS.get(7).getST().getValue().trim();
		if (null != prescriberOVS.get(6).getST()){
			SPI = prescriberOVS.get(6).getST().getValue().trim();
		}
		JAXBElement<String> deaNum =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","DEANumber"), String.class,  dEANumber);
		JAXBElement<String> spiNum =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","SPI"), String.class,  SPI);
		identification.getSPIOrFileIDOrStateLicenseNumber().add(0,spiNum);
		identification.getSPIOrFileIDOrStateLicenseNumber().add(1,deaNum);
		prescriber.setIdentification(identification);
		
		DirectoryNameType name = new DirectoryNameType();
		
		List<ENXPSlot> nameENXPSlot = ((ActEx) physicianTrim.getAct()).getParticipations().get(0).getRole().getPlayer().getName().getENS().get(0).getParts();
		if (null != nameENXPSlot.get(0).getST() && nameENXPSlot.get(0).getST().getValue().trim().length()>0)
			name.setFirstName(nameENXPSlot.get(0).getST().getValue().trim());
		if (null != nameENXPSlot.get(2).getST() && nameENXPSlot.get(2).getST().getValue().trim().length()>0)
			name.setLastName(nameENXPSlot.get(2).getST().getValue().trim());
		if (null != nameENXPSlot.get(1).getST() && nameENXPSlot.get(1).getST().getValue().trim().length()>0)
			name.setMiddleName(nameENXPSlot.get(1).getST().getValue().trim());
		prescriber.setName(name);
		
		MandatoryAddressType address = new MandatoryAddressType();
		if(null != ((ActEx) physicianTrim.getAct()).getParticipations().get(0).getRole().getPlayer().getPerson().getAddr().getADS()
				&& null != ((ActEx) physicianTrim.getAct()).getParticipations().get(0).getRole().getPlayer().getPerson().getAddr().getADS().get(0)
				&& null != ((ActEx) physicianTrim.getAct()).getParticipations().get(0).getRole().getPlayer().getPerson().getAddr().getADS().get(0).getParts()){
			List<ADXPSlot> locationAD = ((ActEx) physicianTrim.getAct()).getParticipations().get(0).getRole().getPlayer().getPerson().getAddr().getADS().get(0).getParts();
			if (null != locationAD.get(0) && locationAD.get(0).getST().getValue().trim().length() > 0) { 
				address.setAddressLine1(locationAD.get(0).getST().getValue().trim());
			}
			if (null != locationAD.get(1) && locationAD.get(1).getST().getValue().trim().length() > 0) {
				address.setAddressLine2(locationAD.get(1).getST().getValue().trim());
			}
			if (null != locationAD.get(2) && locationAD.get(2).getST().getValue().trim().length() > 0) {
				address.setCity(locationAD.get(2).getST().getValue().trim());
			}
			if (null != locationAD.get(3) && locationAD.get(3).getST().getValue().trim().length() > 0) {	
				address.setState(locationAD.get(3).getST().getValue().trim());	
			}
			if (null != locationAD.get(4) && locationAD.get(4).getST().getValue().trim().length() > 0) {
				address.setZipCode(locationAD.get(4).getST().getValue().trim());
			}
			prescriber.setAddress(address);
		}	
		
		if (null != prescriberOVS.get(11).getST()) {
			prescriber.setEmail(prescriberOVS.get(11).getST().getValue().trim());
		}
		
		PhoneNumbersType phonenumbers = new PhoneNumbersType();
		PhoneType phoneTE = new PhoneType();
		if (null != prescriberOVS.get(12).getST() && null != prescriberOVS.get(13).getST()){
			phoneTE.setNumber(prescriberOVS.get(12).getST().getValue()+"x"+prescriberOVS.get(13).getST().getValue());
			phoneTE.setQualifier("TE");
			phonenumbers.getPhone().add(phoneTE);
		}else if (null != prescriberOVS.get(12).getST() && prescriberOVS.get(13).getST() == null){
			phoneTE.setNumber(prescriberOVS.get(12).getST().getValue().trim());
			phoneTE.setQualifier("TE");
			phonenumbers.getPhone().add(phoneTE);
		}
		PhoneType phoneFX = new PhoneType();
		if (null != prescriberOVS.get(8).getST()) {
			phoneFX.setNumber(prescriberOVS.get(8).getST().getValue());
			phoneFX.setQualifier("FX");
			phonenumbers.getPhone().add(phoneFX);
		}
		prescriber.setPhoneNumbers(phonenumbers);
		
		/* Main Message Body */
		UpdatePrescriberLocation updatePrescriberlocation = new UpdatePrescriberLocation();
		updatePrescriberlocation.setPrescriber(prescriber);
		
		BodyType body = new BodyType();
		body.setUpdatePrescriberLocation(updatePrescriberlocation);
		
		MessageType message = new MessageType();
		message.setBody(body);
		message.setHeader(mainHeader);
		message.setVersion(tproperties.getProperty("eprescription.surescripts.appVersion"));
		message.setRelease(tproperties.getProperty("eprescription.surescripts.appRelease"));
		
		updatePrescriberLocationMessagePath = updatePrescriberLocationMessagePath + "UpdatePrescriberLocation_"+physicianTrim.getTolvenEventIds().get(0).getId()+ ".xml";
		TolvenLogger.info("UpdatePrescriberLocation Message Generated with MessageId: "+ messageId, SureScriptsCommunicator.class);
		SureScriptsCommunicator objSureScriptComm = new SureScriptsCommunicator();
		objSureScriptComm.createMessageFile(message, updatePrescriberLocationMessagePath, physicianTrim.getTolvenEventIds().get(0).getAccountId().trim(),privateKey);
	}
	
	/**
	 * @author rais
	 * Method to create a AddPrescriberLocation when a Get Prescriber is submitted.
	 * @param trim
	 */
	private void createAddPrescriberLocation(TrimEx trim,PrivateKey privateKey) {
		String messageId = null;
		String dEANumber = null;
		String SPI = null;
		
		if (tproperties.getProperty("eprescription.surescripts.messages.directory") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.messages.directory- .", SureScriptsCommunicator.class);
			return;
		}
		if (tproperties.getProperty("eprescription.surescripts.adminconsole.username") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.adminconsole.username- .", SureScriptsCommunicator.class);
			return;
		}
		if (tproperties.getProperty("eprescription.surescripts.adminconsole.password") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.adminconsole.password- .", SureScriptsCommunicator.class);
			return;
		}
		
		String locationOfNewPrescriberMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/");
		File messageOutbox = new File(locationOfNewPrescriberMessage);
		if (!messageOutbox.exists())
			messageOutbox.mkdirs();
		
		List<ObservationValueSlot> prescriberOVS = ((ActEx) trim.getAct()).getRelationship().get("prescriber").
		getAct().getObservation().getValues();
			for(int i=0; i< prescriberOVS.size(); i++){
				if(prescriberOVS.get(i).getST() != null){
					if(prescriberOVS.get(i).getST().getValue().trim().length() == 0)
						prescriberOVS.get(i).setST(null);
				}else if(prescriberOVS.get(i).getTS() != null){
					if(prescriberOVS.get(i).getTS().getValue().trim().length() == 0)
						prescriberOVS.get(i).setTS(null);
				}else if(prescriberOVS.get(i).getCE() != null){
					if(prescriberOVS.get(i).getCE().getDisplayName().trim().length() == 0)
						prescriberOVS.get(i).setCE(null);
				}else if(prescriberOVS.get(i).getINT() != null){
					if(String.valueOf(prescriberOVS.get(i).getINT().getValue()).trim().length() < 0)
						prescriberOVS.get(i).setINT(null);
				}
			}

		
		/* Header Body */
		HeaderType mainHeader = new HeaderType();
//		AppVersionType appVersion = new AppVersionType();
		String source = SOURCE_ADDRESS;
		String destination = DESTINATION_ADDRESS;		
		if (null != prescriberOVS.get(10).getST()) {
			messageId = prescriberOVS.get(10).getST().getValue().trim();
		}		
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername(tproperties.getProperty("eprescription.surescripts.adminconsole.username"));
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeDirectoryCredentials(tproperties.getProperty("eprescription.surescripts.adminconsole.password")));
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		
//		mainHeader.setAppVersion(appVersion);
		mainHeader.setFrom(source);
		mainHeader.setTo(destination);
		mainHeader.setMessageID(messageId);
		mainHeader.setSecurity(security);
		mainHeader.setSentTime(getCurrentUTC());
		/* Main Message Body */
		MessageType message = new MessageType();
		BodyType body = new BodyType();
		AddPrescriberLocation addprescriberlocation = new AddPrescriberLocation();
		DirectoryPrescriberType prescriber = new DirectoryPrescriberType();		
		DirectoryInformationType directoryinformation = new DirectoryInformationType();
		if (null != prescriberOVS.get(0).getINT())
			directoryinformation.setAccountID(String.valueOf(prescriberOVS.get(0).getINT().getValue()));
		if (null != prescriberOVS.get(1).getINT())
			directoryinformation.setPortalID(String.valueOf(prescriberOVS.get(1).getINT().getValue()));
		if (null != prescriberOVS.get(3).getST())
			directoryinformation.setServiceLevel(prescriberOVS.get(3).getST().getValue());
		if (null != prescriberOVS.get(4).getTS())
			directoryinformation.setActiveStartTime(getDateFromString(prescriberOVS.get(4).getTS().getValue()));
		if (null != prescriberOVS.get(5).getTS())
			directoryinformation.setActiveEndTime(getDateFromString(prescriberOVS.get(5).getTS().getValue()));
		prescriber.setDirectoryInformation(directoryinformation);
		
		PrescriberIDType identification = new PrescriberIDType();
		if (null != prescriberOVS.get(7).getST())
			dEANumber = prescriberOVS.get(7).getST().getValue().trim();
//		No SPI for addPrescriberLocation message. At this point only SPI root is available.
		if (null != prescriberOVS.get(6).getST()){
			SPI = prescriberOVS.get(6).getST().getValue().trim();
			SPI = SPI.substring(0, 10);
		}
		JAXBElement<String> deaNum =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","DEANumber"), String.class,  dEANumber);
		JAXBElement<String> spiNum =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","SPI"), String.class,  SPI);
		identification.getSPIOrFileIDOrStateLicenseNumber().add(0,spiNum);
		identification.getSPIOrFileIDOrStateLicenseNumber().add(1,deaNum);
		prescriber.setIdentification(identification);
		
		DirectoryNameType name = new DirectoryNameType();
		
		List<ENXPSlot> nameENXPSlot = ((ActEx) trim.getAct()).getParticipations().get(0).getRole().getPlayer().getName().getENS().get(0).getParts();
		if (null != nameENXPSlot.get(0).getST() && nameENXPSlot.get(0).getST().getValue().trim().length()>0)
			name.setFirstName(nameENXPSlot.get(0).getST().getValue().trim());
		if (null != nameENXPSlot.get(2).getST() && nameENXPSlot.get(2).getST().getValue().trim().length()>0)
			name.setLastName(nameENXPSlot.get(2).getST().getValue().trim());
		if (null != nameENXPSlot.get(1).getST() && nameENXPSlot.get(1).getST().getValue().trim().length()>0)
			name.setMiddleName(nameENXPSlot.get(1).getST().getValue().trim());
		prescriber.setName(name);
		
		MandatoryAddressType address = new MandatoryAddressType();
		if(null != ((ActEx) trim.getAct()).getParticipations().get(0).getRole().getPlayer().getPerson().getAddr().getADS()
				&& null != ((ActEx) trim.getAct()).getParticipations().get(0).getRole().getPlayer().getPerson().getAddr().getADS().get(0)
				&& null != ((ActEx) trim.getAct()).getParticipations().get(0).getRole().getPlayer().getPerson().getAddr().getADS().get(0).getParts()){
			List<ADXPSlot> locationAD = ((ActEx) trim.getAct()).getParticipations().get(0).getRole().getPlayer().getPerson().getAddr().getADS().get(0).getParts();
			if (null != locationAD.get(0) && null != locationAD.get(0).getST()) 
				address.setAddressLine1(locationAD.get(0).getST().getValue().trim());	
			if (null != locationAD.get(1) && null != locationAD.get(1).getST()) 
				address.setAddressLine2(locationAD.get(1).getST().getValue().trim());
			if (null != locationAD.get(2) && null != locationAD.get(2).getST())
				address.setCity(locationAD.get(2).getST().getValue().trim());
			if (null != locationAD.get(3) && null != locationAD.get(3).getST())	
				address.setState(locationAD.get(3).getST().getValue().trim());		
			if (null != locationAD.get(4) && null != locationAD.get(4).getST())
				address.setZipCode(locationAD.get(4).getST().getValue().trim());
			prescriber.setAddress(address);
		}	
		
		if (null != prescriberOVS.get(11).getST())  //Setting email address in AddPrescriberLocation
			prescriber.setEmail(prescriberOVS.get(11).getST().getValue().trim());
		
		PhoneNumbersType phonenumbers = new PhoneNumbersType();
		PhoneType phoneTE = new PhoneType();
		if (null != prescriberOVS.get(12).getST() && null != prescriberOVS.get(13).getST()){
			phoneTE.setNumber(prescriberOVS.get(12).getST().getValue()+"x"+prescriberOVS.get(13).getST().getValue());
			phoneTE.setQualifier("TE");
			phonenumbers.getPhone().add(phoneTE);
		}else if (null != prescriberOVS.get(12).getST() && prescriberOVS.get(13).getST() == null){
			phoneTE.setNumber(prescriberOVS.get(12).getST().getValue().trim());
			phoneTE.setQualifier("TE");
			phonenumbers.getPhone().add(phoneTE);
		}
		PhoneType phoneFX = new PhoneType();
		if (null != prescriberOVS.get(8).getST()) {
			phoneFX.setNumber(prescriberOVS.get(8).getST().getValue());
			phoneFX.setQualifier("FX");
			phonenumbers.getPhone().add(phoneFX);
		}
		prescriber.setPhoneNumbers(phonenumbers);
		
		addprescriberlocation.setPrescriber(prescriber);
		body.setAddPrescriberLocation(addprescriberlocation);
		message.setBody(body);
		message.setHeader(mainHeader);
		message.setVersion(tproperties.getProperty("eprescription.surescripts.appVersion"));
		message.setRelease(tproperties.getProperty("eprescription.surescripts.appRelease"));
		
		locationOfNewPrescriberMessage = locationOfNewPrescriberMessage + "AddPrescriberLocation_"+trim.getTolvenEventIds().get(0).getId()+ ".xml";
		TolvenLogger.info("AddPrescriberLocation Message Generated with MessageId: "+ messageId, SureScriptsCommunicator.class);
		SureScriptsCommunicator objSureScriptComm = new SureScriptsCommunicator();
		objSureScriptComm.createMessageFile(message, locationOfNewPrescriberMessage, trim.getTolvenEventIds().get(0).getAccountId().trim(),privateKey);
	}
	
	/**
	 * Method to Create a addPrescriber Message to be sent to the SureScripts
	 * @param trim
	 */
	private void createAddPrescriberMessage(TrimEx trim,PrivateKey privateKey) {
		
		if (tproperties.getProperty("eprescription.surescripts.messages.directory") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.messages.directory- .", SureScriptsCommunicator.class);
			return;
		}
		if (tproperties.getProperty("eprescription.surescripts.adminconsole.username") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.adminconsole.username- .", SureScriptsCommunicator.class);
			return;
		}
		if (tproperties.getProperty("eprescription.surescripts.adminconsole.password") == null) {
			TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.adminconsole.password- .", SureScriptsCommunicator.class);
			return;
		}
		String locationOfNewPrescriberMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/");
		File messageOutbox = new File(locationOfNewPrescriberMessage);
		if (!messageOutbox.exists())
			messageOutbox.mkdirs();
		
		String messageId = null;
		String dEANumber = null;
		
		List<ObservationValueSlot> prescriberOVS = ((ActEx) trim.getAct()).getRelationship().get("prescriber").
													getAct().getObservation().getValues();
		for(int i=0; i< prescriberOVS.size(); i++){
			if(prescriberOVS.get(i).getST() != null){
				if(prescriberOVS.get(i).getST().getValue().trim().length() == 0)
					prescriberOVS.get(i).setST(null);
			}else if(prescriberOVS.get(i).getTS() != null){
				if(prescriberOVS.get(i).getTS().getValue().trim().length() == 0)
					prescriberOVS.get(i).setTS(null);
			}else if(prescriberOVS.get(i).getCE() != null){
				if(prescriberOVS.get(i).getCE().getDisplayName().trim().length() == 0)
					prescriberOVS.get(i).setCE(null);
			}else if(prescriberOVS.get(i).getINT() != null){
				if(String.valueOf(prescriberOVS.get(i).getINT().getValue()).trim().length() < 0)
					prescriberOVS.get(i).setINT(null);
			}
		}
		
		/* Header Body */
		HeaderType mainHeader = new HeaderType();
//		AppVersionType appVersion = new AppVersionType();
		String source = SOURCE_ADDRESS;
		String destination = DESTINATION_ADDRESS;
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername(tproperties.getProperty("eprescription.surescripts.adminconsole.username"));
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeDirectoryCredentials(tproperties.getProperty("eprescription.surescripts.adminconsole.password")));
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		if (null != prescriberOVS.get(10).getST()) {
			messageId = prescriberOVS.get(10).getST().getValue().trim();
		}
		
//		mainHeader.setAppVersion(appVersion);
		mainHeader.setFrom(source);
		mainHeader.setTo(destination);
		mainHeader.setMessageID(messageId);
		mainHeader.setSecurity(security);
		mainHeader.setSentTime(getCurrentUTC());
		
		/* Main Message Body */
		MessageType message = new MessageType();
		BodyType body = new BodyType();
		
		AddPrescriber addprescriber = new AddPrescriber();
		DirectoryPrescriberType prescriber = new DirectoryPrescriberType();
		DirectoryInformationType directoryinformation = new DirectoryInformationType();
		if (null != prescriberOVS.get(0).getINT())
			directoryinformation.setAccountID(String.valueOf(prescriberOVS.get(0).getINT().getValue()));
		if (null != prescriberOVS.get(1).getINT())
			directoryinformation.setPortalID(String.valueOf(prescriberOVS.get(1).getINT().getValue()));
		if (null != prescriberOVS.get(3).getST())
			directoryinformation.setServiceLevel(prescriberOVS.get(3).getST().getValue());
		if (null != prescriberOVS.get(4).getTS())
			directoryinformation.setActiveStartTime(getDateFromString(prescriberOVS.get(4).getTS().getValue()));
		if (null != prescriberOVS.get(5).getTS())
			directoryinformation.setActiveEndTime(getDateFromString(prescriberOVS.get(5).getTS().getValue()));
		prescriber.setDirectoryInformation(directoryinformation);
		
		PrescriberIDType identification = new PrescriberIDType();
		if (null != prescriberOVS.get(7).getST())
			dEANumber = prescriberOVS.get(7).getST().getValue().trim();
		JAXBElement<String> deaNum =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","DEANumber"), String.class,  dEANumber);
		identification.getSPIOrFileIDOrStateLicenseNumber().add(0,deaNum);
		if(null != prescriberOVS.get(14).getST()){
			String npiNumber = prescriberOVS.get(14).getST().getValue().trim(); 
			JAXBElement<String> npiNum =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","NPI"), String.class,  npiNumber);
			identification.getSPIOrFileIDOrStateLicenseNumber().add(1,npiNum);
		}
		prescriber.setIdentification(identification);
		
		DirectoryNameType name = new DirectoryNameType();
		List<ENXPSlot> presNameSlot = ((ActEx) trim.getAct()).getParticipations().get(0).getRole().
								getPlayer().getName().getENS().get(0).getParts();
		if(null != presNameSlot.get(0).getST() && presNameSlot.get(0).getST().getValue().trim().length()>0)
			name.setFirstName(presNameSlot.get(0).getST().getValue().trim());
		else
			name.setFirstName(null);
		
		if(null != presNameSlot.get(1).getST() && presNameSlot.get(1).getST().getValue().trim().length()>0)
			name.setMiddleName(presNameSlot.get(1).getST().getValue().trim());
		else
			name.setMiddleName(null);
		
		if(null != presNameSlot.get(2).getST() && presNameSlot.get(2).getST().getValue().trim().length()>0)
			name.setLastName(presNameSlot.get(2).getST().getValue().trim());
		else
			name.setLastName(null);
		
		prescriber.setName(name);
		
		MandatoryAddressType address = new MandatoryAddressType();
		List<ADXPSlot> presAddSlot = ((ActEx) trim.getAct()).getParticipations().get(0).getRole().
							getPlayer().getPerson().getAddr().getADS().get(0).getParts();
		if(null != presAddSlot.get(0).getST() && presAddSlot.get(0).getST().getValue().trim().length()>0)
			address.setAddressLine1(presAddSlot.get(0).getST().getValue().trim());
		if(null != presAddSlot.get(1).getST() && presAddSlot.get(1).getST().getValue().trim().length()>0)
			address.setAddressLine2(presAddSlot.get(1).getST().getValue().trim());
		if(null != presAddSlot.get(2).getST() && presAddSlot.get(2).getST().getValue().trim().length()>0)
			address.setCity(presAddSlot.get(2).getST().getValue().trim());
		if(null != presAddSlot.get(3).getST() && presAddSlot.get(3).getST().getValue().trim().length()>0)
			address.setState(presAddSlot.get(3).getST().getValue().trim());
		if(null != presAddSlot.get(4).getST() && presAddSlot.get(4).getST().getValue().trim().length()>0)
			address.setZipCode(presAddSlot.get(4).getST().getValue().trim());
		prescriber.setAddress(address);
		
		if (null != prescriberOVS.get(11).getST())
			prescriber.setEmail(prescriberOVS.get(11).getST().getValue().trim());
		
		PhoneNumbersType phonenumbers = new PhoneNumbersType();
		PhoneType phoneTE = new PhoneType();
		if (null != prescriberOVS.get(12).getST() && null != prescriberOVS.get(13).getST()){
			phoneTE.setNumber(prescriberOVS.get(12).getST().getValue()+"x"+prescriberOVS.get(13).getST().getValue());
			phoneTE.setQualifier("TE");
			phonenumbers.getPhone().add(phoneTE);
		}else if (null != prescriberOVS.get(12).getST() && prescriberOVS.get(13).getST() == null){
			phoneTE.setNumber(prescriberOVS.get(12).getST().getValue().trim());
			phoneTE.setQualifier("TE");
			phonenumbers.getPhone().add(phoneTE);
		}
		PhoneType phoneFX = new PhoneType();
		if (null != prescriberOVS.get(8).getST()) {
			phoneFX.setNumber(prescriberOVS.get(8).getST().getValue());
			phoneFX.setQualifier("FX");
			phonenumbers.getPhone().add(phoneFX);
		}
		prescriber.setPhoneNumbers(phonenumbers);
		
		addprescriber.setPrescriber(prescriber);
		body.setAddPrescriber(addprescriber);
		message.setBody(body);
		message.setHeader(mainHeader);
		message.setVersion(tproperties.getProperty("eprescription.surescripts.appVersion"));
		message.setRelease(tproperties.getProperty("eprescription.surescripts.appRelease"));
		
		locationOfNewPrescriberMessage = locationOfNewPrescriberMessage + "NewPrescriber_"+messageId+ ".xml";
		TolvenLogger.info("AddPrescriber Message Generated with MessageId: "+ messageId, SureScriptsCommunicator.class);
		SureScriptsCommunicator objSureScriptComm = new SureScriptsCommunicator();
		objSureScriptComm.createMessageFile(message, locationOfNewPrescriberMessage, trim.getTolvenEventIds().get(0).getAccountId().trim(),privateKey);
	}
	
	/**
	 * Method to Create a file and write the message onto this file
	 * @param message
	 * @param location
	 */

	public String createMessageFile(MessageType message , String location, String accountId,PrivateKey privateKey){
		String result = "";
		try {
			Marshaller marshaller = getMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
			StringWriter sw = new StringWriter();
			marshaller.marshal(message, sw);
			saveOutgoingMessageInFileSystem(sw,message);
			processMessage(sw,message, accountId);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			
			if(null != message.getBody().getAddPrescriber() || null != message.getBody().getUpdatePrescriberLocation() ||  
					null!= message.getBody().getAddPrescriberLocation()){
				postDirectoryMessage(sw, accountId, findMessageType(message),privateKey);
			}else if(null != message.getBody().getDirectoryDownload()){
				result = postDirectoryMessage(sw, accountId, findMessageType(message),privateKey);
			}else {
				postMessage(sw, accountId, findMessageType(message),privateKey);
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private String postToPorky(MessageType message, String messageType, String accountID,PrivateKey privateKey) {
		
		String resopnseMessage = null;
		String requestType;
		if (messageType == null) {
			requestType = "DOWNLOAD";
		} else if (messageType.equals("NewRx") || messageType.equals("RefillResponse")) {
	    	requestType = "NONDIR";
		} else {
			requestType = "DIR";
		}  
		TolvenLogger.info("RequestType: "+requestType+ " MessageType: "+messageType, SureScriptsCommunicator.class);
		try {
			StringWriter messageWriter = new StringWriter();
			Marshaller m = getMarshaller();
			m.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		    m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
		    m.marshal(message, messageWriter);
			
		    URL url = new URL("https://erxtest.tolven.org/Tolven/eRxOutbox");
			StringReader messageReader = new StringReader(messageWriter.getBuffer().toString());
			char[] buffer = new char[1024*10];
			int b_read = 0;
			if ((b_read = messageReader.read(buffer)) != -1) {
				URLConnection urlc = url.openConnection();
				urlc.setConnectTimeout(30000);
				urlc.setReadTimeout(30000);
				urlc.setRequestProperty("RequestType", requestType);
				urlc.setRequestProperty("MessageType", messageType);
				urlc.setRequestProperty("SourceURL", "http://98.111.131.124/Tolven/ClientInbox");
				urlc.setRequestProperty("Content-Type", "text/xml");
				urlc.setDoOutput(true);
				urlc.setDoInput(true);
				TolvenLogger.info("Message being sent to PORKY SERVER.......Awaiting Response", SureScriptsCommunicator.class);
				PrintWriter pw = new PrintWriter(urlc.getOutputStream());
				pw.write(buffer, 0, b_read);
				pw.close();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
				StringBuffer resultBuf = new StringBuffer();
				String res_line;
				while ((res_line = in.readLine()) != null){
						resultBuf.append(res_line);
				}
				in.close();
				if(resultBuf.length() > 0) {
					resopnseMessage = resultBuf.toString().trim();
					TolvenLogger.info("Response from PORKY recieved successfully", SureScriptsCommunicator.class);
					sureBean.convertToMessage(resopnseMessage, false, accountID,privateKey);
				} else {
					TolvenLogger.error("Failed to receive response from PORKY.", SureScriptsCommunicator.class);
				}
			}
		} catch (MalformedURLException me) {
			me.printStackTrace();
		} catch (UnknownServiceException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception ee) {
			TolvenLogger.error("Exception occurred while posting "+requestType+" message."+ ee.getMessage(), SureScriptsCommunicator.class);
			ee.printStackTrace();
		}
		return resopnseMessage;
	}
	
	
	public final String findMessageType(MessageType messageType){
		String incomingMessageType = null;
		if (messageType.getBody().getGetPrescriber() != null) {
			incomingMessageType = ValidMessageTypes.GetPrescriber.value();
		} else if (messageType.getBody().getAddPrescriber() != null) {
			incomingMessageType = ValidMessageTypes.AddPrescriber.value();
		} else if (messageType.getBody().getAddPrescriberLocation() != null) {
			incomingMessageType = ValidMessageTypes.AddPrescriberLocation.value();
		} else if (messageType.getBody().getUpdatePrescriberLocation() != null) {
			incomingMessageType = ValidMessageTypes.UpdatePrescriberLocation.value();
		} else if (messageType.getBody().getDirectoryDownload() != null) {
			incomingMessageType = ValidMessageTypes.DirectoryDownload.value();
		} else if (messageType.getBody().getNewRx() != null) {
			incomingMessageType = ValidMessageTypes.NewRx.value();
		} else if (messageType.getBody().getRefillResponse() != null) {
			incomingMessageType = ValidMessageTypes.RefillResponse.value();
		} else if (messageType.getBody().getRefillRequest() != null) {
			incomingMessageType = ValidMessageTypes.RefillRequest.value();
		} else if (messageType.getBody().getStatus() != null) {
			incomingMessageType = ValidMessageTypes.Status.value();
		} else if (messageType.getBody().getError() != null) {
			incomingMessageType = ValidMessageTypes.Error.value();
		} else if (messageType.getBody().getVerify() != null) {
			incomingMessageType = ValidMessageTypes.Verify.value();
		}
		TolvenLogger.info("MessageType Identified as:- "+incomingMessageType, SureScriptsCommunicator.class);
		return incomingMessageType;
	}
	
	private void saveOutgoingMessageInFileSystem(StringWriter buffer,MessageType messageType) {
		String messageID = messageType.getHeader().getMessageID();
		File file = null;
		try {
			StringBuffer locationOfMessage = new StringBuffer(tproperties.getProperty("eprescription.surescripts.messages.directory"));
			File messageOutbox = new File(locationOfMessage.toString());
			if (!messageOutbox.exists()) {
				messageOutbox.mkdirs();
			}
			locationOfMessage.append(findMessageType(messageType)+"_").append(messageID).append(".xml");
			file = new File(locationOfMessage.toString());
			FileWriter fw = new FileWriter(file);
			fw.write(buffer.toString());
			fw.close();
			TolvenLogger.info("Outgoing message saved in File System " +messageID+" at localtion:"+file.getAbsolutePath(), SureScriptsCommunicator.class);
		} 
		 catch (Exception e) {
			throw new RuntimeException("Exception in saveOutgoingMessageInFileSystem ",e);
		}
	}
	
	/**
	 * Method to persist the message into database.
	 * @param message
	 * @param accountId
	 */
	private void processMessage(StringWriter sw,MessageType message, String accountId) {
		String messageId = message.getHeader().getMessageID();
		String messageType = null;
		String messageString = null;
		String SPI = "";
		String ncpdpid = "";
		String patLastName = "";
		String patFirstName = "";
		String patDob = "";
		String prescriberOrderNumber = "";
		GenderType patGender = null;
		String medDesc = "";
		String medQnty = "";
		if(message.getBody().getAddPharmacy() != null)
			messageType = "AddPharmacy";
		if(message.getBody().getAddPrescriber() != null)
			messageType = "AddPrescriber";
		if(message.getBody().getAddPrescriberLocation() != null)
			messageType = "AddPrescriberLocation";
		if(message.getBody().getUpdatePrescriberLocation() != null)
			messageType = "UpdatePrescriberLocation";
		if(message.getBody().getRefillResponse() != null)
			messageType = "RefillResponse";
		if(message.getBody().getStatus() != null)
			messageType = "Status";
		if(message.getBody().getError() != null)
			messageType = "Error";
		if(message.getBody().getCancelRx() != null)
			messageType = "CancelRx";
		if(message.getBody().getRxChangeResponse() != null)
			messageType = "RxChangeResponse";
		if(message.getBody().getNewRx() != null){
			messageType = "NewRx";
			if(null != message.getBody().getNewRx().getPrescriber().getIdentification() && null != message.getBody().getNewRx().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber()){
				for(int i=0; i< message.getBody().getNewRx().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++){
		        	  if(message.getBody().getNewRx().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")){
		        		  SPI = message.getBody().getNewRx().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue();
		        	  }
	      	    }	
				for(int i=0; i< message.getBody().getNewRx().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().size(); i++){
		        	  if(message.getBody().getNewRx().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("NCPDPID")){
		        		  ncpdpid = message.getBody().getNewRx().getPharmacy().getIdentification().getNCPDPIDOrFileIDOrStateLicenseNumber().get(i).getValue();
		        	  }
	      	    }	
			}
			patLastName = message.getBody().getNewRx().getPatient().getName().getLastName();
			patFirstName = message.getBody().getNewRx().getPatient().getName().getFirstName();
			patDob = message.getBody().getNewRx().getPatient().getDateOfBirth();
			patGender = message.getBody().getNewRx().getPatient().getGender();
			medDesc = message.getBody().getNewRx().getMedicationPrescribed().getDrugDescription();
			medQnty = message.getBody().getNewRx().getMedicationPrescribed().getQuantity().getValue();
			prescriberOrderNumber = message.getBody().getNewRx().getPrescriberOrderNumber();
			
		}	
		if(message.getBody().getDirectoryDownload() != null) {
			messageType = "DirectoryDownload";
		}
		
		try {
			messageString = sw.toString().replaceAll("'","''");
			String prescriberOrderNum = "0";
			List<String> persist = new ArrayList<String>();
			if(messageType.equals("NewRx")){
				if(null != message.getBody().getNewRx().getPrescriberOrderNumber()){
					prescriberOrderNum = message.getBody().getNewRx().getPrescriberOrderNumber();
				}
				String msgInfoId = sureBean.persistSurescriptsInfo(SPI, ncpdpid, patLastName, patFirstName, patDob, patGender, medDesc, medQnty, prescriberOrderNum);
				//String msgInfoId = sureBean.persistSurescriptsInfo("1234", ncpdpid, patLastName, patFirstName, patDob, patGender, medDesc, medQnty, prescriberOrderNumber);
				persist = sureBean.persistSurescriptsMessages(messageId, messageType, messageString, DIRECTION_OUTGOING, msgInfoId , prescriberOrderNum, accountId, null);
			} else if (messageType.equals("RefillResponse")) {
				persist = sureBean.persistSurescriptsMessages(messageId, messageType, messageString, DIRECTION_OUTGOING, "0" , prescriberOrderNum, accountId, message.getHeader().getRelatesToMessageID());
			}else{
				persist = sureBean.persistSurescriptsMessages(messageId, messageType, messageString, DIRECTION_OUTGOING, "0" , prescriberOrderNum, accountId, null);
			}
			if (persist.size() > 0) {
				TolvenLogger.info(messageType+ " Persisted into the Database with MessageId: "+ messageId, SureScriptsCommunicator.class);
			} else {
				TolvenLogger.info(messageType+ " Persistance FAILED. MessageId: "+ messageId, SureScriptsCommunicator.class);
			} 
		} catch (Exception e) {
			throw new RuntimeException("Exception in processing mesage",e);
		}
	}

	/**
	 * Method to obtained the proccessed date as per the surescripts standards for DOB and WRITTEN date
	 * @param dateValue
	 * @param dobOrWritten
	 * @return
	 */
	private String getProcessedDateFromString(String dateValue , String dobOrWritten) {
		String processedDate="";
		if(dobOrWritten.equals("dob")){
			//1987-03-18 00:00:00.0	
			processedDate = processedDate + dateValue.substring(0,4) + dateValue.substring(5,7) + dateValue.substring(8,10);
		}else if(dobOrWritten.equals("written")){
			//20100405112700+0530
			processedDate = processedDate + dateValue.substring(0,8);
		}
		return processedDate;
	}

	/**
	 *  Method to convert the date format into NIST format
	 * @param dateString eg:201004101609
	 * @return eg: 2010-04-10T16:09:04.5Z
	 */
	private String getDateFromString(String dateString){
		StringBuffer date = new StringBuffer();
		if(dateString.length() == 12){
			date.append(dateString.substring(0, 4)).append('-').append(dateString.substring(4, 6))
			.append('-').append(dateString.substring(6, 8)).append('T')
			.append(dateString.substring(8, 10)).append(':').append(dateString.substring(10))
			.append(':').append("00.0Z");
		}else if(dateString.length() == 14){
			date.append(dateString.substring(0, 4)).append('-').append(dateString.substring(4, 6))
			.append('-').append(dateString.substring(6, 8)).append('T')
			.append(dateString.substring(8, 10)).append(':').append(dateString.substring(10, 12))
			.append(':').append(dateString.substring(12)).append(".0Z");
		}else if(dateString.length() == 19){
			dateString = dateString.substring(0 , dateString.length() - 5);
			date.append(dateString.substring(0, 4)).append('-').append(dateString.substring(4, 6))
			.append('-').append(dateString.substring(6, 8)).append('T')
			.append(dateString.substring(8, 10)).append(':').append(dateString.substring(10, 12))
			.append(':').append(dateString.substring(12)).append(".0Z");
		}
		return date.toString();
	}
	
	/**
	 * Method to encode the username and password for informative messages
	 * @param username
	 * @param password
	 * @return
	 */
	private String encodeInformativeCredentials(String username , String password){
		String joint = username.trim()+":"+password.trim();
		String result = "Basic ";
		try {
			result = result + new String(new Base64().encode(joint.getBytes("UTF-8")));
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Method to encode the password for Directory / Administrative Messages
	 * @param password
	 * @return
	 */
	public final String encodeDirectoryCredentials(String password){
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
     * Method to return the current time in UTC Format
     * @return UTC(Coordinated Universal Time) eg: 09-04-2010T21:37:27.2Z
     */
    public final String getCurrentUTC(){
        Date now = Calendar.getInstance().getTime();       
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(now);       
        formatter = new SimpleDateFormat("HH:mm:ss.F");
        String time = formatter.format(now);       
        return date+"T"+time+"Z";
    }
    
    /**
	 * Method to convert the date into UTC format.
	 * @param inputDate eg: 2010-05-14 12:31:35.8
	 * @return UTC 		eg: 2010-05-14T12:31:35.2Z
	 */
	private String convertToUTC(String inputDate) {
		String outputDate = null;
		SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.F");
		try {
			Date date = sdfSource.parse(inputDate);
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String dfDate = formatter.format(date);
			formatter =  new SimpleDateFormat("HH:mm:ss.F");
			String dfTime = formatter.format(date);
			outputDate = dfDate+"T"+dfTime+"Z";
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		return outputDate;
	}
	
	/**
	 * Method to return the current date in yyyyMMdd format. 
	 * @return
	 */
	private String getWrittenDate(){
		return HL7DateFormatUtility.formatHL7TSFormatL8Date(new Date());		
	}
	
	

	/**
	 * Generates message id.
	 * @param plain
	 * @return unique MessageID
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

	//Re-factored methods
	/**
	 * Rule triggered Method
	 * @param trim
	 * @throws NamingException 
	 */
	public void communicateWithSureScripts(TrimEx trim) throws Exception {
		//if(validateMasterAccountAssociation(Long.parseLong(trim.getTolvenEventIds().get(0).getAccountId().trim()))) {
		String keyAlgorithm = tproperties.getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
	    TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
	    PrivateKey privateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);
	    
	    createMessageId(trim);
		if(trim.getName().equals("obs/evn/patientPrescription")){
			ExpressionEvaluator evaluator = new ExpressionEvaluator();
			evaluator.addVariable("trim", trim);
			// If dispenseFromOffice is No
			if(evaluateToString(evaluator,"#{trim.act.relationship['dispenseFromOffice'].act.observation.value.CE.displayName}").toString().equalsIgnoreCase("no")){
				// Proceeds when not a Controlled Substance and prescriber ID is found.
				if(evaluateToString(evaluator,"#{trim.act.relationship['isControlled'].enableRelationship}").toString().equalsIgnoreCase("false") && 
						 evaluateToString(evaluator,"#{trim.act.relationship['prescriber'].act.id.IIS[0].extension}").toString().length() > 0){
					 	 createNewRXMessage(trim,privateKey);
				}else{
					if(!evaluateToString(evaluator,"#{trim.act.relationship['isControlled'].enableRelationship}").toString().equalsIgnoreCase("false"))
						TolvenLogger.error("Skipping eRx Message creation since the medication is controlled", SureScriptsCommunicator.class);
					if(evaluateToString(evaluator,"#{trim.act.relationship['prescriber'].act.id.IIS[0].extension}").toString().length() == 0)
						TolvenLogger.error("Skipping eRx Message creation since prescriber is not selected for the account", SureScriptsCommunicator.class);
				}
				
			}
		}
		else if(trim.getName().equals("obs/evn/refillRequest")){
			if(trim.getAct().getRelationships().get(0).getAct().getObservation().getValues().get(6).getST().getValue().equals("New")){
				if(!((ActEx)trim.getAct()).getRelationship().get("MedicationRequested").getAct().getObservation().getValues().get(3).getST().getValue().equals("2")){
					processRefillRequest(trim,privateKey);
				}
			}	
		}	
		/*} else {
			TolvenLogger.error("Master User Account Not Associated with this Account: " +
					trim.getTolvenEventIds().get(0).getAccountId().trim(), SureScriptsCommunicator.class);
		}*/
	}
	/*
	 * Evaluate an expression to string type
	 */
	public String evaluateToString(ExpressionEvaluator evaluator,String expression){
		Object obj = evaluator.evaluate(expression);
		if(obj != null)
			return obj.toString();
		return "";
	}
	
	/**
	 * Method to generate a NewRX message to be sent to the SureScripts.
	 * @param trim
	 * @throws NamingException 
	 */
	private void createNewRXMessage(TrimEx trim,PrivateKey privateKey) throws Exception {
		try {
			if (tproperties.getProperty("eprescription.surescripts.messages.directory") == null) {
				TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.messages.directory- .", SureScriptsCommunicator.class);
				return;
			}
			if (tproperties.getProperty("eprescription.surescripts.appVersion") == null) {
				TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.appVersion- .", SureScriptsCommunicator.class);
				return;
			}
			if (tproperties.getProperty("eprescription.surescripts.applicationName") == null) {
				TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.applicationName- .", SureScriptsCommunicator.class);
				return;
			}
			if (tproperties.getProperty("eprescription.surescripts.vendorName") == null) {
				TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.vendorName- .", SureScriptsCommunicator.class);
				return;
			}
			if (tproperties.getProperty("eprescription.surescripts.SMSVersion") == null) {
				TolvenLogger.error("APP_SERVER PROPERTY MISSING : -eprescription.surescripts.SMSVersion- .", SureScriptsCommunicator.class);
				return;
			}
			
			String locationOfMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/");
			File messageOutbox = new File(locationOfMessage);
			if (!messageOutbox.exists())
				messageOutbox.mkdirs();
			ExpressionEvaluator evaluator = new ExpressionEvaluator();
			evaluator.addVariable("trim", trim);
			/* Header Body */
			HeaderType mainHeader = new HeaderType();
			AppVersionType appVersion = new AppVersionType();
			appVersion.setApplicationVersion(tproperties.getProperty("eprescription.surescripts.appVersion"));
			appVersion.setAppName(tproperties.getProperty("eprescription.surescripts.applicationName"));
			appVersion.setVendorName(tproperties.getProperty("eprescription.surescripts.vendorName"));
			
			/*List<ObservationValueSlot> surescriptsValues = ((ActEx) trim.getAct()).getRelationship().
								get("toSureScripts").getAct().getObservation().getValues();
			for(int i=0; i< surescriptsValues.size(); i++){
				if(surescriptsValues.get(i).getST() != null){
					if(surescriptsValues.get(i).getST().getValue().trim().length() == 0)
						surescriptsValues.get(i).setST(null);
				}else if(surescriptsValues.get(i).getTS() != null){
					if(surescriptsValues.get(i).getTS().getValue().trim().length() == 0)
						surescriptsValues.get(i).setTS(null);
				}else if(surescriptsValues.get(i).getCE() != null){
					if(surescriptsValues.get(i).getCE().getDisplayName().trim().length() == 0)
					surescriptsValues.get(i).setCE(null);
				}else if(surescriptsValues.get(i).getINT() != null){
					if(surescriptsValues.get(i).getINT().toString().trim().length() == 0)
						surescriptsValues.get(i).setINT(null);
				}
			}
			
			List<ObservationValueSlot> medicationValues = ((ActEx) trim.getAct()).getRelationship()
								.get("medicationDetails").getAct().getObservation().getValues();
			for(int i=0; i< medicationValues.size(); i++){
				if(medicationValues.get(i).getST() != null){
					if(medicationValues.get(i).getST().getValue().trim().length() == 0)
						medicationValues.get(i).setST(null);
				}else if(medicationValues.get(i).getTS() != null){
					if(medicationValues.get(i).getTS().getValue().trim().length() == 0)
						medicationValues.get(i).setTS(null);
				}else if(medicationValues.get(i).getCE() != null){
					if(medicationValues.get(i).getCE().getDisplayName().trim().length() == 0)
					medicationValues.get(i).setCE(null);
				}else if(medicationValues.get(i).getINT() != null){
					if(medicationValues.get(i).getINT().toString().trim().length() == 0)
						medicationValues.get(i).setINT(null);
				}
			}*/
			String	messageId = evaluateToString(evaluator,"#{trim.act.relationship['messageId'].act.text.ST.value}");
			String time = evaluateToString(evaluator,"#{trim.act.effectiveTime.TS.value}");
		
			/* New RX related information */
			NewRx newRx = new NewRx();
			NewRxMedicationType medType = new NewRxMedicationType();
			String diagnosisPath = evaluateToString(evaluator,"#{trim.act.relationship['diagnosis'].act.id.IIS[0].extension}"); 
			if(!StringUtils.isBlank(diagnosisPath)){
				MenuData diagnosisMd = menuBean.findMenuDataItem(Long.parseLong(diagnosisPath.substring(diagnosisPath.lastIndexOf("-")+1)));
				if(null != diagnosisMd){
				org.tolven.surescripts.NewRxMedicationType.Diagnosis diagnosis = new org.tolven.surescripts.NewRxMedicationType.Diagnosis();
				DiagnosisType primaryDiagnosis = new DiagnosisType();
				diagnosis.setClinicalInformationQualifier("PrescriberSupplied");
				primaryDiagnosis.setQualifier(diagnosisMd.getField("codeSystemName").toString());
				primaryDiagnosis.setValue(diagnosisMd.getField("title").toString());
				diagnosis.setPrimary(primaryDiagnosis);
				medType.getDiagnosis().add(diagnosis);
			}	
			}
			medType.setDaysSupply(evaluateToString(evaluator,"#{trim.act.relationship['daysSupply'].act.observation.value.ST.value}")); 
			
			/*if(null != surescriptsValues.get(15).getST()){
				String sigDirections = surescriptsValues.get(15).getST().getValue().trim();
				// TODO unknown why the sig is truncated using '-' character. Commented as bug is reported.
//				sigDirections = sigDirections.substring(sigDirections.indexOf('-')+1).trim();
				medType.setDirections(sigDirections);
			}*/
			medType.setDirections(evaluateToString(evaluator,"#{trim.act.relationship['sigCodes'].act.observation.value.ED.stringValue}"));
/*			if(null != surescriptsValues.get(21).getST())
				medType.setDrugDescription(surescriptsValues.get(21).getST().getValue().trim());
*/	
			medType.setDrugDescription(evaluateToString(evaluator,"#{trim.act.participation['consumableProduct'].role.player.code.CE.displayName}"));
			//if(null != surescriptsValues.get(16).getST())
			//	medType.setNote(surescriptsValues.get(16).getST().getValue().trim());
			medType.setNote(evaluateToString(evaluator,"#{trim.act.relationship['comment'].act.observation.value.ED.stringValue}"));
			
			QuantityType quantity = new QuantityType();
			//if(null != surescriptsValues.get(39).getST())
			//	quantity.setQualifier(surescriptsValues.get(39).getST().getValue().trim());
			quantity.setQualifier(evaluateToString(evaluator,"#{trim.act.participation['consumableProduct'].role.player.quantity.PQ.unit}"));
			//if(null != surescriptsValues.get(13).getST())
			//	quantity.setValue(surescriptsValues.get(13).getST().getValue().trim());
			quantity.setValue(evaluateToString(evaluator,"#{trim.act.participation['consumableProduct'].role.player.quantity.PQ.value}"));
			medType.setQuantity(quantity);
			
			//if(null!= medicationValues.get(11).getTS().getValue()){
			//	medType.setWrittenDate(getWrittenDate());
			//}
			medType.setWrittenDate(getWrittenDate());
			/*if(null != surescriptsValues.get(53).getST()){ //Only if there exists a NDC Code will the NDC information be sent in NewRx
				DrugCodedType drugCodeType = new DrugCodedType();
				if(null != surescriptsValues.get(53).getST())
				drugCodeType.setProductCode(surescriptsValues.get(53)
						.getST().getValue().trim());
				
				if(null != surescriptsValues.get(54).getST())
				drugCodeType.setProductCodeQualifier(surescriptsValues.get(54)
						.getST().getValue().trim());
				
				//if(null != surescriptsValues.get(55).getST())   //TODO find a correct field for dosageform
				//drugCodeType.setDosageForm(surescriptsValues.get(55)
				//		.getST().getValue().trim());
				
				if(null != surescriptsValues.get(56).getST())
				drugCodeType.setStrength(surescriptsValues.get(56)
						.getST().getValue().trim());
				
				if(null != surescriptsValues.get(63).getST())
				drugCodeType.setStrengthUnits(surescriptsValues.get(63)
						.getST().getValue().trim());
				
				if (((ActEx) trim.getAct()).getRelationship().get("medID") != null ) {
					for (ObservationValueSlot ovsMedID :((ActEx) trim.getAct()).getRelationship().get("medID")
							.getAct().getObservation().getValues()) {
						if (ovsMedID.getLabel().getValue().equals("MedID") &&
								!ovsMedID.getST().getValue().isEmpty()) {
							drugCodeType.setDrugDBCode(ovsMedID.getST().getValue());
							drugCodeType.setDrugDBCodeQualifier("FG");
						}
					}
				}
				
				medType.setDrugCoded(drugCodeType);
			}*/
			
			DrugCodedType drugCodeType = new DrugCodedType();
			drugCodeType.setStrength(evaluateToString(evaluator,"#{trim.act.participation['consumableProduct'].role.player.quantity.PQ.originalText}"));
			//Do we need NDC strength unit here??
			drugCodeType.setStrengthUnits(evaluateToString(evaluator,"#{trim.act.participation['consumableProduct'].role.player.quantity.PQ.unit}"));
			drugCodeType.setProductCode(evaluateToString(evaluator,"#{trim.act.relationship['ndcDetails'].act.observation.value.CE.code}"));
			drugCodeType.setProductCodeQualifier(evaluateToString(evaluator,"#{trim.act.relationship['ndcDetails'].act.observation.value.CE.codeSystem}"));
			drugCodeType.setDrugDBCode(evaluateToString(evaluator,"#{trim.act.participation['consumableProduct'].role.player.code.CE.code}"));
			drugCodeType.setDrugDBCodeQualifier(evaluateToString(evaluator,"#{trim.act.relationship['ndcDetails'].act.observation.value.CE.codeSystemName}"));
			medType.setDrugCoded(drugCodeType);
			RefillsType refills = new RefillsType();
			CE refillStatus= (CE)evaluator.evaluate("#{trim.act.relationship['refill'].act.observation.value.CE}");
			String refill = null;
			
			if (refillStatus.toString().equals("PRN"))
				refills.setQualifier("PRN");
			else if (refillStatus.toString().equals("R")) {
				refills.setQualifier("R");
				refills.setQuantity(evaluateToString(evaluator,"#{trim.act.relationship['refill'].act.text.ST.value}")); 
			}
			medType.setRefills(refills);
			
			String substitution = evaluateToString(evaluator,"#{trim.act.relationship['subAllowed'].act.observation.value.CE.displayName}");
			if(substitution.equals("No"))
				medType.setSubstitutions("1");
			else if(substitution.equals("Yes"))
				medType.setSubstitutions("0");
			
			PatientType patient = new PatientType();
			MandatoryPatientNameType manName = new MandatoryPatientNameType();
			/*List<ENXPSlot> patientName = ((ActEx) trim.getAct()).getParticipations().get(1).getRole().
										getPlayer().getName().getENS().get(0).getParts();
			
			List<II> patientId = ((ActEx) trim.getAct()).getParticipations().get(1).getRole().
			getPlayer().getId().getIIS();*/
			
			
			PatientIDType patientIdentification = new PatientIDType();
			//TODO: get patient information here
			//String patientPath = (String)evaluator.evaluate("#{trim.act.participation['subject'].role.id.IIS[0].extension}");
			//MenuData patientMd = menuBean.findMenuDataItem(Long.parseLong(patientPath.substring(patientPath.lastIndexOf("-")+1)));
			String ssn = null;
			/*try {
			        if (null!= patientId.get(0) && !patientId.get(0).getExtension().isEmpty()) {
			                ssn = patientId.get(0).getExtension().trim();
			                JAXBElement<String> socialSecNum =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","SocialSecurity"), String.class,  ssn);
			                patientIdentification.getFileIDOrMedicareNumberOrMedicaidNumber().add(0, socialSecNum);
			                patient.setIdentification(patientIdentification);
			        }
			} catch (Exception e) {
			        e.printStackTrace();
			        TolvenLogger.error("SSN not found in trim for the person. ", this.getClass());
			}*/
			/*String fullName = null;
			if(null != surescriptsValues.get(0).getST()){
				fullName = surescriptsValues.get(0)
					.getST().getValue().trim();
			String[] names = fullName.split(" ");
			int i=0;
			for (i = 0; i < names.length; i++) {
				if (names[i].endsWith(",")) {
					names[i] = names[i].substring(0, names[i].length() - 1);
					if(names[i].equals(null)){
						names[i] = "";
					}
				}
			}

			if (null != names[0] && names[0].trim().length() > 0)
				manName.setLastName(names[0].trim());
			else
				manName.setLastName(null);

			if (null != names[1] && names[1].trim().length() > 0)
				manName.setFirstName(names[1].trim());
			else
				manName.setFirstName(null);

			if (i== 3 && null != names[2] && names[2].trim().length() > 0)
				manName.setMiddleName(names[2].trim());
			else
				manName.setMiddleName(null);
			
			}
			try {
				if (!patientName.get(3).getST().getValue().isEmpty()) {
					manName.setPrefix(patientName.get(3).getST().getValue().trim());
				}
				if (!patientName.get(4).getST().getValue().isEmpty()) {
					manName.setSuffix(patientName.get(4).getST().getValue().trim());
				}
			} catch (Exception e) {
				TolvenLogger.error("Prefix/Suffix not found in trim for the person. ", this.getClass());
				e.printStackTrace();
			}
			*/
			manName.setFirstName(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.name.EN['L'].formattedParts['FAM']}"));
			manName.setLastName(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.name.EN['L'].formattedParts['GIV[0]']}"));
			manName.setMiddleName(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.name.EN['L'].formattedParts['GIV[1]']}"));
			manName.setPrefix(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.name.EN['L'].formattedParts['PFX']}"));
			manName.setSuffix(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.name.EN['L'].formattedParts['SFX']}"));
			
			patient.setName(manName); // Patient Name is set to the xsd
			
			AddressType patAddress = new AddressType();
			/*List<ADXPSlot> patientAdd = ((ActEx) trim.getAct()).getParticipation().get("subject").getRole().
									getPlayer().getPerson().getAddr().getADS().get(0).getParts();
			for (ADXPSlot adxpSlot : patientAdd) {
				if (adxpSlot.getType().compareTo(AddressPartType.AL) == 0 &&
						adxpSlot.getST().getValue().trim().length() > 0) {
					patAddress.setAddressLine1(adxpSlot.getST().getValue().trim());
				} else if (adxpSlot.getType().compareTo(AddressPartType.ADL) == 0 &&
						adxpSlot.getST().getValue().trim().length() > 0) {
					patAddress.setAddressLine2(adxpSlot.getST().getValue().trim());
				} else if (adxpSlot.getType().compareTo(AddressPartType.CTY) == 0 &&
						adxpSlot.getST().getValue().trim().length() > 0) {
					patAddress.setCity(adxpSlot.getST().getValue().trim());
				} else if (adxpSlot.getType().compareTo(AddressPartType.STA) == 0 &&
						adxpSlot.getST().getValue().trim().length() > 0) {
					patAddress.setState(adxpSlot.getST().getValue().trim());
				} else if (adxpSlot.getType().compareTo(AddressPartType.ZIP) == 0 &&
						adxpSlot.getST().getValue().trim().length() > 0) {
					patAddress.setZipCode(adxpSlot.getST().getValue().trim());
				}
			}*/
			patAddress.setAddressLine1(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.person.addr.AD['H'].part['AL[0]'].ST.value}"));
			patAddress.setAddressLine2(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.person.addr.AD['H'].part['AL[1]'].ST.value}"));
			patAddress.setCity(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.person.addr.AD['H'].part['CTY[0]'].ST.value}"));
			patAddress.setState(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.person.addr.AD['H'].part['STA'].ST.value}"));
			patAddress.setZipCode(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.person.addr.AD['H'].part['ZIP'].ST.value}"));
			patient.setAddress(patAddress);
			
			PhoneNumbersType patientPhNums = new PhoneNumbersType();
			/*TELSlotEx telSlotPat = (TELSlotEx)((ActEx) trim.getAct()).getParticipation().get("subject").getRole().getPlayer().getTelecom();
			if (telSlotPat.getTEL().get(TelecommunicationAddressUse.HP.value()) != null &&
					!telSlotPat.getTEL().get(TelecommunicationAddressUse.HP.value()).getValue().isEmpty()) {
				PhoneType phoneTypeHome = new PhoneType();
				phoneTypeHome.setNumber(telSlotPat.getTEL().get(TelecommunicationAddressUse.HP.value()).getValue().trim());
				phoneTypeHome.setQualifier(TelecommunicationAddressUse.HP.value());
				patientPhNums.getPhone().add(phoneTypeHome);
			}
			if (telSlotPat.getTEL().get(TelecommunicationAddressUse.WP.value()) != null &&
					!telSlotPat.getTEL().get(TelecommunicationAddressUse.WP.value()).getValue().isEmpty()) {
				PhoneType phoneTypeWork = new PhoneType();
				phoneTypeWork.setNumber(telSlotPat.getTEL().get(TelecommunicationAddressUse.WP.value()).getValue().trim());
				phoneTypeWork.setQualifier(TelecommunicationAddressUse.WP.value());
				patientPhNums.getPhone().add(phoneTypeWork);
			}
			if (patientPhNums.getPhone().size() > 0) {
				patient.setPhoneNumbers(patientPhNums);
			}
			
			// TODO Set Email
			if (telSlotPat.getTEL().get(TelecommunicationAddressUse.DIR.value()) != null &&
					!telSlotPat.getTEL().get(TelecommunicationAddressUse.DIR.value()).getValue().isEmpty()) {
				patient.setEmail(telSlotPat.getTEL().get(TelecommunicationAddressUse.DIR.value()).getValue().trim());
			}
			
			if(null != surescriptsValues.get(29).getST())
				patient.setDateOfBirth(getProcessedDateFromString(surescriptsValues.get(29)
					.getST().getValue() , "dob"));
			if(null != surescriptsValues.get(30).getST()){
				GenderType patientGender = GenderType.fromValue(surescriptsValues.get(30).getST().getValue().substring(0, 1).toUpperCase());
				patient.setGender(patientGender);
			}*/
			PhoneType phoneTypeHome = new PhoneType();
			phoneTypeHome.setNumber(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.telecom.TEL['HP'].value}"));
			phoneTypeHome.setQualifier(TelecommunicationAddressUse.HP.value());
			patientPhNums.getPhone().add(phoneTypeHome);
			PhoneType phoneTypeWork = new PhoneType();
			phoneTypeWork.setNumber(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.telecom.TEL['WP'].value}"));
			phoneTypeWork.setQualifier(TelecommunicationAddressUse.WP.value());
			patientPhNums.getPhone().add(phoneTypeWork);
			patient.setPhoneNumbers(patientPhNums);
			patient.setEmail(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.telecom.TEL['DIR'].value}"));
			patient.setDateOfBirth(getProcessedDateFromString(evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.livingSubject.birthTime.TS.value}"),"written"));
			String gender = evaluateToString(evaluator,"#{trim.act.participation['subject'].role.player.livingSubject.administrativeGenderCode.value}");
			if(!StringUtils.isBlank(gender)){
				GenderType patientGender = GenderType.fromValue(gender);
				patient.setGender(patientGender);
			}
			
			//Pharmacy details
			PharmacyType pharmacy = new PharmacyType();
			PharmacyIDType pharmId = new PharmacyIDType();
			/*List<ObservationValueSlot> pharmacyValues = ((ActEx) trim.getAct()).getRelationship().get("pharmacy").getAct().getObservation().getValues();
			
			for(int i=0; i< pharmacyValues.size(); i++){
			if(pharmacyValues.get(i).getST() != null){
				if(pharmacyValues.get(i).getST().getValue().trim().length() == 0)
					pharmacyValues.get(i).setST(null);
				}
			}*/
			String pharmacyPath = evaluateToString(evaluator,"#{trim.act.participation['pharmacy'].role.id.IIS[0].extension}");
			MenuData pharmacyMd = menuBean.findMenuDataItem(Long.parseLong(pharmacyPath.substring(pharmacyPath.lastIndexOf("-")+1)));
			
			String ncpdpId = (String)pharmacyMd.getField("NCPDPID");
			if(!StringUtils.isBlank(ncpdpId)){
				JAXBElement<String> pharmIds =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","NCPDPID"), String.class,  ncpdpId);
				pharmId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(0,pharmIds);
				pharmacy.setIdentification(pharmId);
			}
			pharmacy.setStoreName((String)pharmacyMd.getField("storeName"));
			
			AddressType pharmAdd = new AddressType();
			/*if(null !=  pharmacyValues.get(2).getST())
				pharmAdd.setAddressLine1(pharmacyValues.get(2).getST().getValue().trim());
			if(null !=  pharmacyValues.get(3).getST())
				pharmAdd.setCity(pharmacyValues.get(3).getST().getValue().trim());
			
			if(null !=  pharmacyValues.get(4).getST())
				pharmAdd.setState(pharmacyValues.get(4).getST().getValue().trim());
			if(null !=  pharmacyValues.get(5).getST())
				pharmAdd.setZipCode(pharmacyValues.get(5).getST().getValue().trim());
			*/
			pharmAdd.setAddressLine1((String)pharmacyMd.getField("addressLine1"));
			pharmAdd.setCity((String)pharmacyMd.getField("city"));
			pharmAdd.setState((String)pharmacyMd.getField("state"));
			pharmAdd.setZipCode((String)pharmacyMd.getField("zip"));
			pharmacy.setAddress(pharmAdd);
			
			PhoneNumbersType phoneNums = new PhoneNumbersType();
			PhoneType phonePharmTe = new PhoneType();
			PhoneType phonePharmFax = new PhoneType();
			/*if(null != pharmacyValues.get(6).getST()){
				phonePharmTe.setNumber(pharmacyValues.get(6).getST().getValue().trim());
				phonePharmTe.setQualifier("TE");
				phoneNums.getPhone().add(phonePharmTe);
			}
			if(null != pharmacyValues.get(7).getST()){
				phonePharmFax.setNumber(pharmacyValues.get(7).getST().getValue().trim());
				phonePharmFax.setQualifier("FX");
				phoneNums.getPhone().add(phonePharmFax);
				pharmacy.setPhoneNumbers(phoneNums);
			}*/
			phonePharmTe.setNumber((String)pharmacyMd.getField("zip"));
			phonePharmTe.setQualifier("primaryPhone");
			phoneNums.getPhone().add(phonePharmTe);
			phonePharmFax.setNumber((String)pharmacyMd.getField("fax"));
			phonePharmFax.setQualifier("FX");
			phoneNums.getPhone().add(phonePharmFax);
			pharmacy.setPhoneNumbers(phoneNums);
			
			//Prescriber details
			MandatoryPrescriberType mandPrescriber = new MandatoryPrescriberType();
			String prescriberPath = evaluateToString(evaluator,"#{trim.act.relationship['prescriber'].act.id.IIS[0].extension}");
			MenuData prescriberMd = menuBean.findMenuDataItem(Long.parseLong(prescriberPath.substring(prescriberPath.lastIndexOf("-")+1)));
		
			String fullPrescriberName = null;
			/*if(null != surescriptsValues.get(1).getST())
				fullPrescriberName = surescriptsValues.get(1).getST().getValue();
			MandatoryNameType manPresName = new MandatoryNameType();
			if(null != fullPrescriberName && fullPrescriberName.trim().length() > 0){
				String[] prescName = fullPrescriberName.split(",");

				if (null != prescName[0] && prescName[0].trim().length() > 0)
					manPresName.setFirstName(prescName[0].trim());
				else
					manPresName.setFirstName(null);
				if(null != prescName[1] && prescName[1].trim().length() > 0)
					manPresName.setMiddleName(prescName[1].trim());
				else
					manPresName.setMiddleName(null);
				if (null != prescName[2] && prescName[2].trim().length() > 0)
					manPresName.setLastName(prescName[2].trim());
				else
					manPresName.setLastName(null);
			}
			if(null != surescriptsValues.get(7).getST())
				mandPrescriber.setEmail(surescriptsValues.get(7)
					.getST().getValue().trim());
			*/
			MandatoryNameType manPresName = new MandatoryNameType();
			manPresName.setFirstName((String)prescriberMd.getField("firstName"));
			manPresName.setMiddleName((String)prescriberMd.getField("middleName"));
			manPresName.setLastName((String)prescriberMd.getField("lastName"));
			mandPrescriber.setName(manPresName);
			mandPrescriber.setEmail((String)prescriberMd.getField("prescriberEmail"));
			
			MandatoryAddressType prescriberAddress = new MandatoryAddressType();
			/*if(null != surescriptsValues.get(2).getST())
				mandAddress.setAddressLine1(surescriptsValues
					.get(2).getST().getValue().trim());
			if(null != surescriptsValues.get(3).getST())
				mandAddress.setAddressLine2(surescriptsValues
					.get(3).getST().getValue().trim());
			if(null != surescriptsValues.get(4).getST())
				mandAddress.setCity(surescriptsValues.get(4)
					.getST().getValue().trim());
			
			if(null != surescriptsValues.get(5).getST())
				mandAddress.setState(surescriptsValues.get(5)
					.getST().getValue().trim());
			if(null != surescriptsValues.get(6).getST())
			mandAddress.setZipCode(surescriptsValues.get(6)
					.getST().getValue().trim());*/
			prescriberAddress.setAddressLine1((String)prescriberMd.getField("addr1"));
			prescriberAddress.setAddressLine2((String)prescriberMd.getField("addr2"));
			prescriberAddress.setCity((String)prescriberMd.getField("city"));
			prescriberAddress.setState((String)prescriberMd.getField("state"));
			prescriberAddress.setZipCode((String)prescriberMd.getField("zip"));
			mandPrescriber.setAddress(prescriberAddress);
			
			PhoneNumbersType phonePrescNums = new PhoneNumbersType();
			PhoneType phoneTe = new PhoneType();
			PhoneType phoneFax = new PhoneType();
			
			/*if(null != surescriptsValues.get(37).getST()){
				phoneFax.setNumber(surescriptsValues.get(37)
					.getST().getValue());
				phoneFax.setQualifier("FX");
				phonePrescNums.getPhone().add(phoneTe);
			}
			if(null != surescriptsValues.get(8).getST()){
				phoneTe.setNumber(surescriptsValues.get(8).getST().getValue());
				phoneTe.setQualifier("TE");
				phonePrescNums.getPhone().add(phoneFax);
			}*/
			phoneFax.setNumber((String)prescriberMd.getField("fax"));
			phoneFax.setQualifier("FX");
			phonePrescNums.getPhone().add(phoneTe);
			phoneTe.setNumber((String)prescriberMd.getField("workTelecom"));
			phoneTe.setQualifier("TE");
			phonePrescNums.getPhone().add(phoneFax);
			mandPrescriber.setPhoneNumbers(phonePrescNums);
			
			PrescriberIDType prescriberId = new PrescriberIDType();
			/* Setting SPI */
			String spi = (String)prescriberMd.getField("spiRoot");
			String npi = (String)prescriberMd.getField("npi");
			int flag = 0;
			if(!StringUtils.isBlank(spi)){
				JAXBElement<String> spiNum =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","SPI"), String.class, spi);
				prescriberId.getSPIOrFileIDOrStateLicenseNumber().add(0,spiNum);
				flag = 1;
			}
			
			if(!StringUtils.isBlank(npi)){
				JAXBElement<String> npiNum =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","NPI"), String.class, npi);
				if(flag == 1)
				 prescriberId.getSPIOrFileIDOrStateLicenseNumber().add(1,npiNum);
				else if(flag == 0)
					prescriberId.getSPIOrFileIDOrStateLicenseNumber().add(0,npiNum);
				flag = 2;
			}
			if(flag == 1 || flag == 2)
				mandPrescriber.setIdentification(prescriberId);
			//Set Clinic Name
			//TODO: get clinic name
			/*if (null != surescriptsValues.get(9).getST()) {
				if(surescriptsValues.get(9).getST().getValue().length() <= 35)
					mandPrescriber.setClinicName(surescriptsValues.get(9).getST().getValue().trim());	
				else
					mandPrescriber.setClinicName(surescriptsValues.get(9).getST().getValue().substring(0,35).trim());
			}*/
			
			/*if(null != surescriptsValues.get(31).getINT())
				orderNum =  String.valueOf(surescriptsValues.get(31).getINT().getValue()).trim();
			*/
			//SupervisorType supervisor = new SupervisorType();
			newRx.setMedicationPrescribed(medType);
			newRx.setPatient(patient);
			newRx.setPharmacy(pharmacy);
			newRx.setPrescriber(mandPrescriber);
//		String rxReferenceNumber = "";
			
			/*if(null != surescriptsValues.get(38).getST())
				newRx.setRxReferenceNumber(surescriptsValues.get(38).getST().getValue().trim());
			*/
			//order number should be the placeholderId
			String orderMenuPath = evaluateToString(evaluator,"#{trim.act.id.IIS[0].extension}");
			newRx.setPrescriberOrderNumber(orderMenuPath.substring(orderMenuPath.lastIndexOf("-")+1));
			//newRx.setSupervisor(supervisor);
			
			String source = "mailto:"+spi+".spi@surescripts.com";
			String destination = "mailto:"+ncpdpId+".ncpdp@surescripts.com";
			
			//mainHeader.setAppVersion(appVersion);
			mainHeader.setFrom(source);
			mainHeader.setTo(destination);
			mainHeader.setMessageID(messageId);
			mainHeader.setSentTime(time);
			mainHeader.setAppVersion(appVersion);
			mainHeader.setSMSVersion(tproperties.getProperty("eprescription.surescripts.SMSVersion"));
			
			/* Main Message Body */
			MessageType message = new MessageType();
			BodyType body = new BodyType();
			body.setNewRx(newRx);
			message.setBody(body);
			message.setHeader(mainHeader);
			message.setVersion(tproperties.getProperty("eprescription.surescripts.appVersion"));
			message.setRelease(tproperties.getProperty("eprescription.surescripts.appRelease"));

			/* Marshalling Process on the way */
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			locationOfMessage = locationOfMessage + "NewRX_" + messageId+ ".xml";
			TolvenLogger.info("NewRx Message Generated with MessageId: "+ messageId, SureScriptsCommunicator.class);
			createMessageFile(message, locationOfMessage, trim.getTolvenEventIds().get(0).getAccountId().trim(),privateKey);
		} catch (Exception e) {
			throw e;
		}
	}
	public void marshallToStringWritter(MessageType message,StringWriter sw) throws JAXBException {
		Marshaller marshaller = getMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
	//	marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
		marshaller.marshal(message, sw);
	}
}
