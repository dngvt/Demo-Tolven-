package org.tolven.surescripts;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.surescripts.MedicationType.Diagnosis;
import org.tolven.trim.CE;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.TrimEx;
import org.tolven.xml.ParseXML;
import org.w3c.dom.Document;

/**
 * 
 * This class is used to generate status or error messages
 */
public class SureScriptsResponse extends ParseXML {
	@EJB private TolvenPropertiesLocal tproperties;
	/**
	 * Default Constructor
	 */
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
	public SureScriptsResponse() {
		super(jc);
		try {
			InitialContext ctx = new InitialContext();
			if(tproperties == null){
				tproperties = (TolvenPropertiesLocal) ctx.lookup("java:global/tolven/tolvenEJB/TolvenProperties!org.tolven.core.TolvenPropertiesLocal");
			}
		}catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Method to create status message 
	 */
	public void createStatusMessage(String relatesToMessageId) {
		
		String locationOfResponseMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/inbox/");
		File messageInbox =  new File(locationOfResponseMessage);
		if (!messageInbox.exists()) {
			messageInbox.mkdirs();
		}
		
		/* Main Message Body */
		MessageType message = new MessageType();

		/* Header Body */
		HeaderType mainHeader = new HeaderType();

		String destination = "mailto:xxx@qburst.com";
		String source = "mailto:yyy@qburst.com";
		String messageId = "0";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy.MMMMM.dd GGG hh:mm aaa");
		String time = sdf.format(cal.getTime());

		mainHeader.setTo(destination);
		mainHeader.setFrom(source);
		mainHeader.setMessageID(messageId);
		mainHeader.setRelatesToMessageID(relatesToMessageId);
		mainHeader.setSentTime(time);
		
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername("ssuser");
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeInformativeCredentials("ssuser","goffit1"));
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		mainHeader.setSecurity(security);
		
		message.setHeader(mainHeader);

		/* Body */
		BodyType body = new BodyType();

		/* Status */
		Status status = new Status();
		status.setCode("010");
		body.setStatus(status);

		message.setBody(body);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			locationOfResponseMessage = locationOfResponseMessage + "Status_"
					+ relatesToMessageId + ".xml";
			File file = new File(locationOfResponseMessage);
			Document doc = db.newDocument();
			getMarshaller().marshal(message, file);

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	
	/*
	 * Method to create error message 
	 */
	public void createErrorMessage(String relatesToMessageId) {
		
		String locationOfResponseMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"inbox/");
		File messageInbox =  new File(locationOfResponseMessage);
		if (!messageInbox.exists()) {
			messageInbox.mkdirs();
		}
		
		/* Main Message Body */
		MessageType message = new MessageType();

		/* Header Body */
		HeaderType mainHeader = new HeaderType();

		String destination = "mailto:xxx@qburst.com";
		String source = "mailto:yyy@qburst.com";
		String messageId = "0";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy.MMMMM.dd GGG hh:mm aaa");
		String time = sdf.format(cal.getTime());

		mainHeader.setTo(destination);
		mainHeader.setFrom(source);
		mainHeader.setMessageID(messageId);
		mainHeader.setRelatesToMessageID(relatesToMessageId);
		mainHeader.setSentTime(time);

		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername("ssuser");
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeInformativeCredentials("ssuser","goffit1"));
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		mainHeader.setSecurity(security);
		
		message.setHeader(mainHeader);

		/* Body */
		BodyType body = new BodyType();

		/* Error */
		Error error =new Error();
		error.setCode("900");
		error.setDescription("AccountID or PortalID does not match user credentials");
		error.setDescriptionCode("902");
		body.setError(error);

		message.setBody(body);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			locationOfResponseMessage = locationOfResponseMessage + "Error_"
					+ relatesToMessageId + ".xml";
			File file = new File(locationOfResponseMessage);
			Document doc = db.newDocument();
			getMarshaller().marshal(message, file);

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
	
	/*
	 * Method to generate an RxChange Request from SureScripts 
	 */
	public void createRXChangeRequest(TrimEx trim,String relatesToMessageId){
		
		String locationOfResponseMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"inbox/");
		File messageInbox =  new File(locationOfResponseMessage);
		if (!messageInbox.exists()) {
			messageInbox.mkdirs();
		}
		
		List<ObservationValueSlot> surescriptsValues = ((ActEx) trim.getAct()).getRelationship().get(
		"toSureScripts").getAct().getObservation().getValues();
		
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
			}
		} 
		
		List<ObservationValueSlot> pharmacyValues = ((ActEx) trim.getAct()).getRelationship().get("pharmacy")
		.getAct().getObservation().getValues();
		
		for(int i=0; i< pharmacyValues.size(); i++){
			if(pharmacyValues.get(i).getST() != null){
				if(pharmacyValues.get(i).getST().getValue().trim().length() == 0)
					pharmacyValues.get(i).setST(null);
			}else if(pharmacyValues.get(i).getTS() != null){
				if(pharmacyValues.get(i).getTS().getValue().trim().length() == 0)
					pharmacyValues.get(i).setTS(null);
			}else if(pharmacyValues.get(i).getCE() != null){
				if(pharmacyValues.get(i).getCE().getDisplayName().trim().length() == 0)
					pharmacyValues.get(i).setCE(null);
			}
		} 
		
		List<ObservationValueSlot> medicalValues = ((ActEx) trim.getAct()).getRelationship().get(
		"medicationDetails").getAct().getObservation().getValues();
		
		for(int i=0; i< medicalValues.size(); i++){
			if(medicalValues.get(i).getST() != null){
				if(medicalValues.get(i).getST().getValue().trim().length() == 0)
					medicalValues.get(i).setST(null);
			}else if(medicalValues.get(i).getTS() != null){
				if(medicalValues.get(i).getTS().getValue().trim().length() == 0)
					medicalValues.get(i).setTS(null);
			}else if(medicalValues.get(i).getCE() != null){
				if(medicalValues.get(i).getCE().getDisplayName().trim().length() == 0)
					medicalValues.get(i).setCE(null);
			}
		} 
		
		/* Main Message Body */
		MessageType message = new MessageType();

		/* Header Body */
		HeaderType mainHeader = new HeaderType();

		String destination = "mailto:xxx@qburst.com";
		String source = "mailto:yyy@qburst.com";
		String messageId = "12345";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy.MMMMM.dd GGG hh:mm aaa");
		String time = sdf.format(cal.getTime());
		
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername("ssuser");
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeInformativeCredentials("ssuser","goffit1"));
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
	
		
		String smsVersion = "1.0";
		AppVersionType appVersion = new AppVersionType();

		mainHeader.setTo(destination);
		mainHeader.setFrom(source);
		mainHeader.setMessageID(messageId);
		mainHeader.setRelatesToMessageID(relatesToMessageId);
		mainHeader.setSentTime(time);
		mainHeader.setSecurity(security);
		mainHeader.setSMSVersion(smsVersion);
		mainHeader.setAppVersion(appVersion);
		
		

		message.setHeader(mainHeader);

		/* Body */
		BodyType body = new BodyType();
		RxChangeRequest changeRequest = new RxChangeRequest();
		
		String rxReferenceNumber = "RX007";
		changeRequest.setRxReferenceNumber(rxReferenceNumber);
		String orderNum =  null;
		if(null != surescriptsValues.get(31).getINT()){
			orderNum = String.valueOf(surescriptsValues.get(31).getINT().getValue());
		changeRequest.setPrescriberOrderNumber(orderNum);
		}
		/*coding for Pharmacy  */
		MandatoryPharmacyType pharmacy = new MandatoryPharmacyType();
		
		/*Pharmacy Id*/
		PharmacyIDType pharmId = new PharmacyIDType();
		String ncpdpId = null;
		if(null != pharmacyValues.get(0).getST()){
			ncpdpId = pharmacyValues.get(0).getST().getValue();
			JAXBElement<String> pharmIds =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","NCPDPID"), String.class,  ncpdpId);
			pharmId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(0,pharmIds);
			pharmacy.setIdentification(pharmId);
		}
		/*Pharmacy Store Name(not Mandatory)*/
		if(null != pharmacyValues.get(1).getST())
		pharmacy.setStoreName(pharmacyValues.get(1).getST().getValue());
		
		/*Pharmacy Address*/
		MandatoryAddressType pharmAdd = new MandatoryAddressType();
		
		if(null != pharmacyValues.get(2).getST())
		pharmAdd.setAddressLine1(pharmacyValues.get(2).getST().getValue());
		
		if(null != pharmacyValues.get(3).getST())
		pharmAdd.setCity(pharmacyValues.get(3).getST().getValue());
		
		if(null != pharmacyValues.get(4).getST())
		pharmAdd.setState(pharmacyValues.get(4).getST().getValue());
		
		if(null != pharmacyValues.get(5).getST())
		pharmAdd.setZipCode(pharmacyValues.get(5).getST().getValue());
		
		pharmacy.setAddress(pharmAdd);
		
		changeRequest.setPharmacy(pharmacy);
		
		/*coding for prescriber */
		
		//TODO prescriber SPI
		PrescriberType prescriber = new PrescriberType();
		String fullPrescriberName = null;
		if(null != surescriptsValues.get(1).getST()){
			fullPrescriberName = surescriptsValues.get(1).getST().getValue();
			String[] prescName = fullPrescriberName.split(",");
			MandatoryNameType manPresName = new MandatoryNameType();
			if (null != prescName[0])
				manPresName.setFirstName(prescName[0]);
			
			if (null != prescName[1])
				manPresName.setMiddleName(prescName[1]);
	
			if (null != prescName[2])
				manPresName.setLastName(prescName[2]);
		prescriber.setName(manPresName);
		}
		
		
		if(null != surescriptsValues.get(7).getST())
		prescriber.setEmail(surescriptsValues.get(7).getST().getValue());
		
		AddressType presAddress = new AddressType();
		
		if(null != surescriptsValues.get(2).getST())
		presAddress.setAddressLine1(surescriptsValues.get(2).getST().getValue());
		
		if(null != surescriptsValues.get(3).getST())
		presAddress.setAddressLine2(surescriptsValues.get(3).getST().getValue());
		
		if(null != surescriptsValues.get(4).getST())
		presAddress.setCity(surescriptsValues.get(4).getST().getValue());
		
		if(null != surescriptsValues.get(5).getST())
		presAddress.setState(surescriptsValues.get(5).getST().getValue());
		
		if(null != surescriptsValues.get(6).getST())
		presAddress.setZipCode(surescriptsValues.get(6).getST().getValue());
		
		prescriber.setAddress(presAddress);
		changeRequest.setPrescriber(prescriber);
		
		/*coding for Patient*/
		PatientType patient = new PatientType();
		MandatoryPatientNameType manName = new MandatoryPatientNameType();
		String fullName =  null;
		if(null != surescriptsValues.get(0).getST()){
			fullName = surescriptsValues.get(0).getST().getValue();
			String[] names = fullName.split(" ");
			for (int i = 0; i < names.length; i++) {
				if (names[i].endsWith(",")) {
					names[i] = names[i].substring(0, names[i].length() - 1);
				}
			}

			if (null != names[0])
				manName.setLastName(names[0]);
	
			if (null != names[1])
				manName.setFirstName(names[1]);
	
			if (null != names[2])
				manName.setMiddleName(names[2]);

			patient.setName(manName); // Patient Name is set to the xsd
		}
		
		if(null != surescriptsValues.get(29).getST())
		patient.setDateOfBirth(surescriptsValues.get(29).getST().getValue());
		
		if(null != surescriptsValues.get(30).getST()){
		GenderType patientGender = GenderType.fromValue(surescriptsValues.get(30).getST().getValue().substring(0, 1).toUpperCase());
		patient.setGender(patientGender);
		}
		
		changeRequest.setPatient(patient);
		
		/*coding for Medication prescribed*/
		
		MedicationType medType = new MedicationType();
		
		if(null != surescriptsValues.get(14).getINT())
		medType.setDaysSupply(String.valueOf(surescriptsValues.get(14).getINT().getValue()));
		
		if(null != surescriptsValues.get(15).getST())
		medType.setDirections(surescriptsValues.get(15).getST().getValue());
		
		/*Drug Description is mandatory*/
		if(null != surescriptsValues.get(21).getST())
		medType.setDrugDescription(surescriptsValues.get(21).getST().getValue());
		
		if(null != surescriptsValues.get(16).getST())
		medType.setNote(surescriptsValues.get(16).getST().getValue());
		
		QuantityType quantity = new QuantityType();
		//TODO	quantity.setQualifier(value);
		
		/*quantity value is mandatory*/
		
		if(null != surescriptsValues.get(13).getST())
		quantity.setValue(surescriptsValues.get(13).getST().getValue().trim());
		
		medType.setQuantity(quantity);
		if(null != medicalValues.get(11).getTS())
		medType.setWrittenDate(medicalValues.get(11).getTS().getValue());
		
		RefillsType refills = new RefillsType();
		String refill= null;
		if(null != medicalValues.get(4).getINT()){
			refill= String.valueOf(medicalValues.get(4).getINT().getValue());
			
			if(refill.equals("0"))
				refills.setQualifier("PRN");
			else
				refills.setQualifier("R");
		
		refills.setQuantity(String.valueOf(refill));
		medType.setRefills(refills);
		
		}
		String substitution = null;
		if(null != medicalValues.get(6).getCE()){
		substitution = medicalValues.get(6).getCE().getDisplayName();
		
		if(substitution.equals("No"))
			medType.setSubstitutions("0");
		else if(substitution.equals("Yes"))
			medType.setSubstitutions("1");
		
		}
		
		changeRequest.setMedicationPrescribed(medType);
		body.setRxChangeRequest(changeRequest);
		message.setBody(body);
		MedicationType newMedication = new MedicationType();
		newMedication.setDrugDescription("Toprol XL 200 mg Tab");
		QuantityType qty = new QuantityType();
		qty.setValue("20");
		newMedication.setQuantity(qty);
		RefillsType refill0 = new RefillsType();
		refill0.setQuantity("1");
		newMedication.setRefills(refill0);
		
		message.getBody().getRxChangeRequest().medicationRequested =  new ArrayList<MedicationType>();
		message.getBody().getRxChangeRequest().getMedicationRequested().add(0, newMedication);
		
		MedicationType newMedication1 = new MedicationType();
		newMedication1.setDrugDescription("Decohistine DH 2 mg-10 mg-30 mg/5mL Oral Liquid");
		QuantityType qty1 = new QuantityType();
		qty1.setValue("15");
		newMedication1.setQuantity(qty1);
		RefillsType refill1 = new RefillsType();
		refill1.setQuantity("2");
		newMedication1.setRefills(refill1);
		message.getBody().getRxChangeRequest().getMedicationRequested().add(1, newMedication1);
		
		MedicationType newMedication2 = new MedicationType();
		newMedication2.setDrugDescription("Metyrosine 250 mg Cap");
		QuantityType qty2 = new QuantityType();
		qty2.setValue("10");
		newMedication2.setQuantity(qty2);
		RefillsType refill2 = new RefillsType();
		refill2.setQuantity("3");
		newMedication2.setRefills(refill2);
		message.getBody().getRxChangeRequest().getMedicationRequested().add(2, newMedication2);
		
		

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			locationOfResponseMessage = locationOfResponseMessage + "RXChangeRequest_"
					+ relatesToMessageId + ".xml";
			File file = new File(locationOfResponseMessage);
			Document doc = db.newDocument();
			getMarshaller().marshal(message, file);

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();

	}
	}
	
	/*
	 * Method to generate a refill request from SureScripts
	 */
	public void createRefillRequest(TrimEx trim, String relatesToMessageId){
		
		String locationOfResponseMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"inbox/");
		File messageInbox =  new File(locationOfResponseMessage);
		if (!messageInbox.exists()) {
			messageInbox.mkdirs();
		}
		
		List<ObservationValueSlot> surescriptsValues = ((ActEx) trim.getAct()).getRelationship().get(
		"toSureScripts").getAct().getObservation().getValues();
		
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
			}
		}
		
		List<ObservationValueSlot> pharmacyValues = ((ActEx) trim.getAct()).getRelationship().get("pharmacy")
		.getAct().getObservation().getValues();
		
		for(int i=0; i< pharmacyValues.size(); i++){
			if(pharmacyValues.get(i).getST() != null){
				if(pharmacyValues.get(i).getST().getValue().trim().length() == 0)
					pharmacyValues.get(i).setST(null);
			}else if(pharmacyValues.get(i).getTS() != null){
				if(pharmacyValues.get(i).getTS().getValue().trim().length() == 0)
					pharmacyValues.get(i).setTS(null);
			}else if(pharmacyValues.get(i).getCE() != null){
				if(pharmacyValues.get(i).getCE().getDisplayName().trim().length() == 0)
					pharmacyValues.get(i).setCE(null);
			}
		} 
		
		
		List<ObservationValueSlot> medicalValues = ((ActEx) trim.getAct()).getRelationship().get(
		"medicationDetails").getAct().getObservation().getValues();
		
		for(int i=0; i< medicalValues.size(); i++){
			if(medicalValues.get(i).getST() != null){
				if(medicalValues.get(i).getST().getValue().trim().length() == 0)
					medicalValues.get(i).setST(null);
			}else if(medicalValues.get(i).getTS() != null){
				if(medicalValues.get(i).getTS().getValue().trim().length() == 0)
					medicalValues.get(i).setTS(null);
			}else if(medicalValues.get(i).getCE() != null){
				if(medicalValues.get(i).getCE().getDisplayName().trim().length() == 0)
					medicalValues.get(i).setCE(null);
			}
		} 
		
		/* Main Message Body */
		MessageType message = new MessageType();

		/* Header Body */
		HeaderType mainHeader = new HeaderType();

		String destination = "mailto:xxx@qburst.com";
		String source = "mailto:yyy@qburst.com";
		String messageId = "12345";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy.MMMMM.dd GGG hh:mm aaa");
		String time = sdf.format(cal.getTime());
		
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername("ssuser");
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeInformativeCredentials("ssuser","goffit1"));
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		
		String smsVersion = "1.0";
		AppVersionType appVersion = new AppVersionType();

		mainHeader.setTo(destination);
		mainHeader.setFrom(source);
		mainHeader.setMessageID(messageId);
		mainHeader.setRelatesToMessageID(relatesToMessageId);
		mainHeader.setSentTime(time);
		mainHeader.setSecurity(security);
		mainHeader.setSMSVersion(smsVersion);
		mainHeader.setAppVersion(appVersion);
		
		

		message.setHeader(mainHeader);
		
		/*Body */
		BodyType body = new BodyType();
		
		RefillRequest refillRequest = new RefillRequest();
		
		String rxRefNumber = "RX003" ;
		refillRequest.setRxReferenceNumber(rxRefNumber);
	
		String orderNum =  String.valueOf(surescriptsValues.get(31).getINT().getValue());
		refillRequest.setPrescriberOrderNumber(orderNum);
		
		/*coding for Pharmacy  */
		MandatoryPharmacyType pharmacy = new MandatoryPharmacyType();
		
		/*Pharmacy Id*/
		PharmacyIDType pharmId = new PharmacyIDType();
		String ncpdpId = null;
		if(null != pharmacyValues.get(0).getST()){
			ncpdpId = pharmacyValues.get(0).getST().getValue();
			JAXBElement<String> pharmIds =new JAXBElement<String>(new javax.xml.namespace.QName("http://www.surescripts.com/messaging","NCPDPID"), String.class,  ncpdpId);
			pharmId.getNCPDPIDOrFileIDOrStateLicenseNumber().add(0,pharmIds);
			pharmacy.setIdentification(pharmId);
		}
		/*Pharmacy Store Name(not Mandatory)*/
		if(null != pharmacyValues.get(1).getST())
		pharmacy.setStoreName(pharmacyValues.get(1).getST().getValue());
		
		/*Pharmacy Address*/
		MandatoryAddressType pharmAdd = new MandatoryAddressType();
		if(null != pharmacyValues.get(2).getST())
		pharmAdd.setAddressLine1(pharmacyValues.get(2).getST().getValue());
		
		if(null != pharmacyValues.get(3).getST())
		pharmAdd.setCity(pharmacyValues.get(3).getST().getValue());
		
		if(null != pharmacyValues.get(4).getST())
		pharmAdd.setState(pharmacyValues.get(4).getST().getValue());
		
		if(null != pharmacyValues.get(5).getST())
		pharmAdd.setZipCode(pharmacyValues.get(5).getST().getValue());
		
		pharmacy.setAddress(pharmAdd);
		
		refillRequest.setPharmacy(pharmacy);
		
		/*coding for prescriber */
		
		//TODO prescriber SPI
		PrescriberType prescriber = new PrescriberType();
		
		if(null != surescriptsValues.get(1).getST()){
		String fullPrescriberName = surescriptsValues.get(1).getST().getValue();
		MandatoryNameType manPresName = new MandatoryNameType();
			if(!fullPrescriberName.equals("")){
			String[] prescName = fullPrescriberName.split(",");
			if (null != prescName[0])
				manPresName.setFirstName(prescName[0]);
			if (null != prescName[1])
				manPresName.setMiddleName(prescName[1]);
			if (null != prescName[2])
				manPresName.setLastName(prescName[2]);
			}
		prescriber.setName(manPresName);
		
		}
		
		if(null != surescriptsValues.get(7).getST())
		prescriber.setEmail(surescriptsValues.get(7).getST().getValue());
		AddressType presAddress = new AddressType();
		if(null != surescriptsValues.get(2).getST())
		presAddress.setAddressLine1(surescriptsValues.get(2).getST().getValue());
		
		if(null != surescriptsValues.get(3).getST())
		presAddress.setAddressLine2(surescriptsValues.get(3).getST().getValue());
		
		if(null != surescriptsValues.get(4).getST())
		presAddress.setCity(surescriptsValues.get(4).getST().getValue());
		
		if(null != surescriptsValues.get(5).getST())
		presAddress.setState(surescriptsValues.get(5).getST().getValue());
		
		if(null != surescriptsValues.get(6).getST())
		presAddress.setZipCode(surescriptsValues.get(6).getST().getValue());
		
		prescriber.setAddress(presAddress);
		
		if(null != surescriptsValues.get(9).getST())
		prescriber.setClinicName(surescriptsValues.get(9).getST().getValue());
		
		refillRequest.setPrescriber(prescriber);
		
		/* Setting supervisor */
		SupervisorType supervisor = new SupervisorType();
		MandatoryNameType supervisorName = new MandatoryNameType();
		supervisorName.setFirstName("Lara");
		supervisorName.setMiddleName("Julia");
		supervisorName.setLastName("Cameron");
		supervisor.setName(supervisorName);
		refillRequest.setSupervisor(supervisor);
		
		
		/*coding for Patient*/
		PatientType patient = new PatientType();
		MandatoryPatientNameType manName = new MandatoryPatientNameType();
		String fullName = null;
		if(null != surescriptsValues.get(0).getST()){
			fullName = surescriptsValues.get(0).getST().getValue();
			String[] names = fullName.split(" ");
			for (int i = 0; i < names.length; i++) {
				if (names[i].endsWith(",")) {
					names[i] = names[i].substring(0, names[i].length() - 1);
				}
			}
	
			if (null != names[0])
				manName.setLastName(names[0]);
	
			if (null != names[1])
				manName.setFirstName(names[1]);
	
			if (null != names[2])
				manName.setMiddleName(names[2]);
			patient.setName(manName); // Patient Name is set to the xsd
		}
		
		
		if(null != surescriptsValues.get(29).getST())
		patient.setDateOfBirth(surescriptsValues.get(29).getST().getValue());
		
		if(null != surescriptsValues.get(30).getST()){
		GenderType patientGender = GenderType.fromValue(surescriptsValues.get(30).getST().getValue().substring(0, 1).toUpperCase());
		patient.setGender(patientGender);
		}
		
		refillRequest.setPatient(patient);
		
		/*coding for Medication prescribed*/
		
		MedicationType medType = new MedicationType();
		
		if(null != surescriptsValues.get(14).getINT())
		medType.setDaysSupply(String.valueOf(surescriptsValues.get(14).getINT().getValue()));
		
		if(null != surescriptsValues.get(15).getST())
		medType.setDirections(surescriptsValues.get(15).getST().getValue());
		
		/*Drug Description is mandatory*/
		if(null != surescriptsValues.get(21).getST())
		medType.setDrugDescription(surescriptsValues.get(21).getST().getValue());
		
		if(null != surescriptsValues.get(16).getST())
		medType.setNote(surescriptsValues.get(16).getST().getValue());
		QuantityType quantity = new QuantityType();
		//TODO	quantity.setQualifier(value);
		
		/*quantity value is mandatory*/
		if(null != surescriptsValues.get(13).getST().getValue())
			quantity.setValue(surescriptsValues.get(13).getST().getValue().trim());
		medType.setQuantity(quantity);
		
		if(null != medicalValues.get(11).getTS())
		medType.setWrittenDate(medicalValues.get(11).getTS().getValue());
		
		RefillsType refills = new RefillsType();
		
		if(null != medicalValues.get(12).getCE()){
		CE refillStatus = medicalValues.get(12).getCE();
		
			if (refillStatus.toString().equals("PRN"))
				refills.setQualifier("PRN");
			else if (refillStatus.toString().equals("R")) {
				refills.setQualifier("R");
				String refill= String.valueOf(((ActEx) trim.getAct())
						.getRelationship().get("medicationDetails").getAct()
						.getObservation().getValues().get(4).getINT().getValue()).trim();
				refills.setQuantity(String.valueOf(refill));
			}
			medType.setRefills(refills);
		}
		String substitution = null;
		if(null != medicalValues.get(6).getCE()){
			substitution = medicalValues.get(6).getCE().getDisplayName();
			if(substitution.equals("No"))
				medType.setSubstitutions("0");
			else if(substitution.equals("Yes"))
				medType.setSubstitutions("1");
		}
		
		/*Setting Prior Authorization*/
		PriorAuthorizationType priorAuthorization = new PriorAuthorizationType();
			// Values can be G1/PD
		priorAuthorization.setQualifier("PD");
		priorAuthorization.setValue("xxxxxxxx");
		medType.setPriorAuthorization(priorAuthorization);
		
		/*Setting Diagnosis
		*/
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setClinicalInformationQualifier("Pharmacy Inferred");
		DiagnosisType primary = new DiagnosisType();
		primary.setQualifier("ICD-9");
		primary.setValue("XXXXXXXX");
		diagnosis.setPrimary(primary);
		List<Diagnosis> diagnosisList = new ArrayList<Diagnosis>();
		diagnosisList.add(diagnosis);
		medType.diagnosis = diagnosisList;
		
		refillRequest.setMedicationPrescribed(medType);
		body.setRefillRequest(refillRequest);
		message.setBody(body);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			locationOfResponseMessage = locationOfResponseMessage + "RefillRequest_"
					+ relatesToMessageId + ".xml";
			File file = new File(locationOfResponseMessage);
			Document doc = db.newDocument();
			getMarshaller().marshal(message, file);

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();

	}
		
	}
	
	/*
	 * Method to generate a CancelRXResponse from SureScripts
	 */
	public void createCancelRXResponse(String relatesToMessageId){
		
		String locationOfResponseMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"inbox/");
		File messageInbox =  new File(locationOfResponseMessage);
		if (!messageInbox.exists()) {
			messageInbox.mkdirs();
		}
		
		/* Main Message Body */
		MessageType message = new MessageType();

		/* Header Body */
		HeaderType mainHeader = new HeaderType();

		String destination = "mailto:xxx@qburst.com";
		String source = "mailto:yyy@qburst.com";
		String messageId = "12345";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy.MMMMM.dd GGG hh:mm aaa");
		String time = sdf.format(cal.getTime());

		mainHeader.setTo(destination);
		mainHeader.setFrom(source);
		mainHeader.setMessageID(messageId);
		mainHeader.setRelatesToMessageID(relatesToMessageId);
		mainHeader.setSentTime(time);

		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername("ssuser");
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeInformativeCredentials("ssuser","goffit1"));
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		
		message.setHeader(mainHeader);

		/* Body */
		BodyType body = new BodyType();

		/* Status */
		CancelRxResponse cancelRX = new CancelRxResponse();
		CancelRxResponseType response = new CancelRxResponseType();
		ApprovedType app = new ApprovedType();
		response.setApproved(app);
//		DeniedType denied = new DeniedType();
//		response.setDenied(denied);
		
		cancelRX.setResponse(response);
		body.setCancelRxResponse(cancelRX);
		message.setBody(body);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			locationOfResponseMessage = locationOfResponseMessage + "CancelRX_"
					+ relatesToMessageId + ".xml";
			File file = new File(locationOfResponseMessage);
			Document doc = db.newDocument();
			getMarshaller().marshal(message, file);

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		
		
	}
	

	/**
	 * Method to encode the username and password for informative messages
	 * @param username
	 * @param password
	 * @return
	 */
	private String encodeInformativeCredentials(String username , String password){
		String joint = username+":"+password;
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
	private String encodeDirectoryCredentials(String password){
		String result = "Basic ";
		try {
			result = result + new String (new Base64().encode(MessageDigest.getInstance("SHA1").digest(password.toUpperCase().getBytes("UTF-16LE"))));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
