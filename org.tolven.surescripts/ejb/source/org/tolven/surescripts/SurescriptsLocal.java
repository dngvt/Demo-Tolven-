/**
 * 
 */
package org.tolven.surescripts;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Map;

import org.tolven.app.entity.DrugQualifier;
import org.tolven.app.entity.MenuData;
import org.tolven.surescripts.entity.MessageDetails;

/**
 * @author mohammed
 *
 */
public interface SurescriptsLocal {

	/**
	 * Method to persist all incoming and outgoing message details into the surescripts Database
	 * @param messageId
	 * @param messageType
	 * @param message
	 * @param direction
	 * @param messageInfoId
	 * @param prescriberOrderNumber
	 * @param accountId 
	 * @return
	 */
	public ArrayList<String> persistSurescriptsMessages(String messageId, String messageType, String message,
			String direction, String messageInfoId, String prescriberOrderNum, String accountId, String relatesToMessageID);
	
	/**
	 * The function is used to retrieve all the unprocessed messages from surescripts
	 * @return
	 */
	public ArrayList<SurescriptsVO> retrieveUnprocessedMessages();
	
	/**
	 * The function is used to process all the status and error messages received from the surescripts
	 * @param condition
	 * @param messageId
	 */
	public void processStatusErrorMessages(String condition , String messageId);
	

	/**
	 * Method to authenticate the username and password from the messages
	 * @param username
	 * @param password
	 * @return boolean
	 */
	public boolean authenticateUserCredentials (String username , String password);
	/**
	 * Method to process the Received Message from the surescripts
	 * @param messageString
	 * @param needCredentialsCheck
	 * @return
	 */
	public String convertToMessage(String messageString , boolean needCredentialsCheck, String accountId,PrivateKey privateKey);
	/**
	 * Method to generate the getDirectory Download message
	 * @return
	 */
	public String generateDirectoryDownloadMessage(PrivateKey privateKey);
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
	 * @param prescriberOrderNumber
	 * @return
	 */
	public String persistSurescriptsInfo(String SPI, String ncpdpid,
			String lastName, String firstName, String dateOfBirth,
			GenderType gender, String drugDescription, String value, String prescriberOrderNumber);
    /**
	 * Method used to fetch the message based on the Relates to messageId
	 * @param messageId
	 * @return Message
	 */
	public String getMessageData(String messageId);	
	/**
	 * Matching Algorithm method to find out the previous parent message
	 * @param recievedMessage
	 * @return
	 */
	public String matchingAlgorithm(MessageType recievedMessage);
	/**
	 * Method to return to all messageTypes corresponding to a prescription.
	 * @param messageName
	 * @return ArrayList<MessageType>
	 */
	public ArrayList<MessageType> retrievePatientPrescriptionReport(String prescriberOrderNum);
	/**
     * Method to retrieve the
     * @param relatesToMessageId
     * @return
     */
    public String getMessageTypeFromErrorStatus(String relatesToMessageId);
    /**
     * Method to retrieve all the states in USA with their codes
     * @return
     */
    //public Map<String , String> retrieveAllStates();
    /**
	 * Method to retrieve all the Drug Qualifiers
	 * @return
	 */
	//public ArrayList<DrugQualifier> retrieveAllDrugQualifiers();
    
	/**
	 * Method to change the status of the processsed message
	 */
	public void changeStatusOfProcessedMessage(String messageId , String messageType , String finalStatus);
	/**
	 * Method to get the MenuData based on SPI id of prescriber.
	 * @param SPI
	 * @return
	 */
	public MenuData getMdFromsSpi(String spi);
	/**
	 * Method to retrieve MenuData from last name and first name
	 * @param lastName
	 * @param firstName
	 * @return
	 */
	public MenuData getMdFromPatientName(String lastName, String firstName);
	/**
	 * Method to retrieve the Qualifier Description When the QualCode is in Hand
	 * @param qualCode
	 * @return String
	 */
	public String retrieveDrugQualDescription(String qualCode);
	/**
	 * Method to retrieve the Qualifier Code When the QualDesc is in Hand
	 * @param qualDesc
	 * @return String
	 */
	public String retrieveDrugQualCode(String qualDesc);
	/**
	 * This method is called upon for generating the Nightly Download Message
	 * @return
	 */
	public String generateNightlyDirectoryDownloadMessage(PrivateKey privateKey);
	/**
	 * This method is to retreive all the Surescripts Messages
	 * @param accountId
	 * @return
	 */
	public ArrayList<MessageDetails> retreiveAllSurescriptsMessages(String rowId, long accountId);
	/**
	 * Method to generate Error Messsage When Schedule II Drug is obtained in RefillRequest
	 * @param message
	 */
	public void generateErrorMessageForScheduleII(MessageType message,PrivateKey privateKey);
	/**
	 * Method to retrieve filtered Surescripts Messages
	 * @param messageType
	 * @param date
	 * @param messageId
	 * @param accountId 
	 * @return
	 */
	public ArrayList<MessageDetails> retrieveFilteredSurescriptsMessages(String messageType, String date ,String messageId , String rowId, long accountId);
	/**
	 * Method to retrieve the xml based on the id.
	 * @param id
	 * @return
	 */
	public String retrieveXml(String id);
	/**
	 * Checks whether the master user is associated with account being used.
	 * @param accountId
	 * @return true if Master user is added to the account.
	 */
	public boolean checkMasterAccountAssociation(Long accountId);
	/**
	 * Method to find the dosageForm id/desc when desc/id is provided.
	 * @param input
	 * @param isId
	 * @return
	 */
	//public String getDosageForm(String input, boolean isId);
	//Moved to component.tolvenejb
	/**
	 * Posts the REFREQ into the application when a status(000) or error message is received.
	 * @param recievedMessage
	 * @param status
	 */
	public void postRefillRequestInActivityList(MessageType recievedMessage, String status,PrivateKey privateKey);
	/**
	 * Checks whether the Status/Error's relatesTomessageId corresponds to a REFRES.
	 * @param messageId
	 * @return true if it is a REFRES.
	 */
	public boolean isRefillResponse(String messageId);
	/**
	 * With the relatesToMessageId of Status/Error gets the corresponding REFRES and the 
	 * associated REFREQ
	 * @param relatesToMessageId
	 * @return MessaegeType REFREQ
	 */
	public MessageType getRefillRequestMessage(String relatesToMessageId);
	/**
	 * Removes REFREQ with messageId from activity list. 
	 * @param messageId
	 */
	public void removeRefillRequestFromActivityList(String messageId);
	/**
	 * Method used to get the Error desc for REFRES message.
	 * @param messageId
	 * @return Message
	 */
	public String getRefillErrorDesc(String messageId);
	/**
	 * Checks whether the communication with SureScripts was successful. 
	 * @param messageId
	 * @return true only if the communication was successful.
	 */
	public boolean isSuccessfulCommunication(String messageId);
	/**
	 * @return the messageCount
	 */
	public int getMessageCount();
	
	/**
	 * Method to load state names into StateNames table in Surescripts schema.
	 * @param stateCode
	 * @param name
	 */
	public boolean createStateNames(String stateCode, String name);
	/**
	 * Method to load drug qualifiers into DrugQualifier table in Surescripts schema.
	 * @param stateCode
	 * @param name
	 */
	public boolean createDrugQualifiers(String qualifierCode, String description);
	/**
	 * Method to load error codes into ErrorCodes table in Surescripts schema.
	 * @param stateCode
	 * @param name
	 */
	public boolean createErrorCodes(String errorCode, String description);
	/**
	 * Method to get MenuData provided a field and value.
	 * @param field eg: string01, pqUnits03
	 * @param value only string
	 * @return
	 */
	
}
