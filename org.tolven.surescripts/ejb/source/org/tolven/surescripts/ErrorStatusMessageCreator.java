/**
 * 
 */
package org.tolven.surescripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.xml.ParseXML;
import org.w3c.dom.Document;

/**
 * @author root
 *
 */
public class ErrorStatusMessageCreator extends ParseXML implements Serializable{
	/**
	 * Variable to hold the value of the message version
	 */
	private static final String MESSAGE_VERSION = "1.0";
	/**
	 * Variable to hold the value of the username
	 */
	private static final String USERNAME = "To3%lS1#";
	/**
	 * Variable to hold the value of the password
	 */
	private static final String PASSWORD = "Basic VG8zJWxTMSM6cnRnQCpkdzI=";
	private TolvenPropertiesLocal tproperties;
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

	public ErrorStatusMessageCreator() {
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
	/**
	 * Method to Generate the Error Message in response to the surescripts
	 * @param messageDoc
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public String generateErrorMessage(Document messageDoc, String code,
			String messageDescription, String errorCode) {

		if (null != messageDoc) {

			MessageType errorMessage = new MessageType();
			JAXBElement<MessageType> errorJAXB = null;
			try {
				errorJAXB = (JAXBElement<MessageType>) getUnmarshaller()
						.unmarshal(messageDoc);
				errorMessage = errorJAXB.getValue();
			} catch (JAXBException e1) {
				e1.printStackTrace();
			}
			HeaderType errorHeader = errorMessage.getHeader();
			String fromId = errorHeader.getFrom();
			String toId = errorHeader.getTo();
			String msgId = errorHeader.getMessageID();
			String rltMsgId = errorHeader.getRelatesToMessageID();

			SecurityType security = new SecurityType();
			UsernameTokenType userToken = new UsernameTokenType();
			userToken.setUsername(USERNAME);
			PasswordType password = new PasswordType();
			password.setType("PasswordDigest");
			password.setValue(PASSWORD);
			userToken.setPassword(password);
			security.setUsernameToken(userToken);

			errorHeader.setFrom(toId);
			errorHeader.setTo(fromId);
			errorHeader.setMessageID("0");
			errorHeader.setRelatesToMessageID(msgId);
			errorHeader.setSentTime(getPresentDate());
			errorHeader.setSecurity(security);
			errorMessage.setHeader(errorHeader);

			BodyType errorBody = new BodyType();
			Error error = new Error();
			error.setCode(code);
			error.setDescription(messageDescription);
			error.setDescriptionCode(errorCode);

			errorBody.setError(error);
			errorMessage.setHeader(errorHeader);
			errorMessage.setBody(errorBody);
			errorMessage.setVersion(MESSAGE_VERSION);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = null;
			try {
				db = dbf.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			OutputStream os = null;
			String locationOfErrorMessage = new String(
					tproperties.getProperty("eprescription.surescripts.messages.directory")+ "/outbox/error/");
			File messageOutbox = new File(locationOfErrorMessage);
			if (!messageOutbox.exists())
				messageOutbox.mkdirs();
			Date d = new Date();
			try {
				os = new FileOutputStream(locationOfErrorMessage + "Error_"
						+ d.getYear() + "_" + d.getMonth() + "_" + d.getDay()
						+ "_" + d.getMinutes() + "_" + d.getSeconds() + ".xml");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			Document doc = db.newDocument();
			try {
				/* Message matter is written into the Doc which is sent as reply message */
				getMarshaller().marshal(errorMessage, doc);

				/* Message matter is written into the File which is saved in the outbox folder for future reference */
				getMarshaller().marshal(errorMessage, os);

			} catch (JAXBException e) {
				e.printStackTrace();
			}

			File file = new File(locationOfErrorMessage + "Error_"
					+ d.getYear() + "_" + d.getMonth() + "_" + d.getDay() + "_"
					+ d.getMinutes() + "_" + d.getSeconds() + ".xml");
			StringBuilder contents = new StringBuilder();
			try {
				BufferedReader input = new BufferedReader(new FileReader(file));
				try {
					String line = null;
					while ((line = input.readLine()) != null) {
						contents.append(line);
						contents.append(System.getProperty("line.separator"));
					}
				} finally {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return contents.toString();
		} else {
			return "Invalid Content posted.";
		}
	}
	
	/**
	 * Method to generate the latest date in the NIST format
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getPresentDate() {
		 Date now = Calendar.getInstance().getTime();       
	        //Date Format: 07-04-2010
	        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	        String date = formatter.format(now);       
	        //Time Format: 23:01:56
	        formatter = new SimpleDateFormat("HH:mm:ss.F");
	        String time = formatter.format(now);       
	        return date+"T"+time+"Z";
	}
	
	
	/**
	 * Method to generate automatic status message
	 * @param messageDoc
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public String generateStatusMessage(Document messageDoc) {
		MessageType statusMessage = new MessageType();
		JAXBElement< MessageType> statusJAXB = null;
		try {
			statusJAXB = (JAXBElement< MessageType>) getUnmarshaller().unmarshal(messageDoc);
			statusMessage = statusJAXB.getValue();
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}
		HeaderType statusHeader = statusMessage.getHeader();
		String fromId = statusHeader.getFrom();
		String toId = statusHeader.getTo();
		String msgId = statusHeader.getMessageID();
		String rltMsgId = statusHeader.getRelatesToMessageID();
		
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername(USERNAME);
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(PASSWORD);
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		
		statusHeader.setFrom(toId);
		statusHeader.setTo(fromId);
		statusHeader.setMessageID("0");
		statusHeader.setRelatesToMessageID(msgId);
		statusHeader.setSentTime(getPresentDate());
		statusHeader.setSecurity(security);
		statusMessage.setHeader(statusHeader);
		
		
		BodyType statusBody = new BodyType();
		Status status = new Status();
		status.setCode("010");
		statusBody.setStatus(status);
		statusMessage.setBody(statusBody);
		statusMessage.setVersion(MESSAGE_VERSION);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		OutputStream os = null;
		Date d = new Date();
		String locationOfStatusMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/status/");
		File messageOutbox =  new File(locationOfStatusMessage);
		if (!messageOutbox.exists())
			messageOutbox.mkdirs();
		
		try {
			os = new FileOutputStream(locationOfStatusMessage+ "Status_"+d.getYear()+"_"+d.getMonth()+"_"+d.getDay()+"_"+d.getMinutes()+"_"+d.getSeconds()+".xml" );
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		Document doc = db.newDocument();
		try {
			/*  Message matter is written into the Doc which is sent as reply message*/
			getMarshaller().marshal(statusMessage, doc);
			
			/* Message matter is written into the File which is saved in the outbox folder for future reference */
			getMarshaller().marshal(statusMessage, os);
			
		} catch (JAXBException e) {
			e.printStackTrace();
		} 
		
		File file = new File(locationOfStatusMessage+"Status_"+d.getYear()+"_"+d.getMonth()+"_"+d.getDay()+"_"+d.getMinutes()+"_"+d.getSeconds()+".xml");
		 StringBuilder contents = new StringBuilder();
         try {
           BufferedReader input =  new BufferedReader(new FileReader(file));
           try {
             String line = null;
             while (( line = input.readLine()) != null){
                 contents.append(line);
                 contents.append(System.getProperty("line.separator"));
             }
           }
           finally {
             input.close();
           }
         }
         catch (IOException ex){
           ex.printStackTrace();
         }
	  return contents.toString();
	}

	public MessageType createErrorMessageForScheduleIIDrug(MessageType refillMessage){
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername(USERNAME);
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(PASSWORD);
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		
		
		MessageType errorMessage = new MessageType();
		HeaderType refillHeader = refillMessage.getHeader();
		HeaderType errorHeader = new HeaderType();
		errorHeader.setFrom(refillHeader.getTo());
		errorHeader.setTo(refillHeader.getFrom());
		errorHeader.setRelatesToMessageID(refillHeader.getMessageID());
		errorHeader.setMessageID("0");
		errorHeader.setSentTime(getPresentDate());
		errorHeader.setSecurity(security);
		errorMessage.setHeader(errorHeader);
		
		Error error = new Error();
		error.setCode("601");
		error.setDescription("Schedule II Drug cannot be sent for Refill Request");
		error.setDescriptionCode("126");
		BodyType errorBody = new BodyType();
		errorBody.setError(error);
		errorMessage.setBody(errorBody);
		errorMessage.setVersion(MESSAGE_VERSION);
		
		return errorMessage;
	}
}
