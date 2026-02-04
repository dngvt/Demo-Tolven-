/**
 * 
 */
package org.tolven.app;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.tolven.app.entity.MenuData;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.entity.DocXML;
import org.tolven.trim.Trim;
import org.tolven.trim.ex.TRIMException;
import org.tolven.trim.ex.TrimEx;

/**
 * @author mohammed
 *
 */
public interface EPrescriptionLocal {
	
	/**
	 * Function to retrieve the menu data
	 * @param firstName
	 * @param middleName
	 * @param lastName
	 * @return
	 */
	public Long getPatientIdFromMenuData(String firstName , String middleName , String lastName);
	/**
	 * Function to get the Menu Data by passing the Prescriber Order Number
	 * @param prescriberOrderNumber
	 * @return MenuData
	 */
	public MenuData getMenuDataFromPrescriberOrderNumber(String prescriberOrderNumber);
	/**
	 * This function is used to change the number of refills  into the corresponding prescription menudata
	 * @param prescriberOrderNumber
	 * @param refill
	 * @return
	 */
	public MenuData changeNumberOfRefills(String prescriberOrderNumber, Long refill);
	/**
	 * This function is used to retrieve the AccountID containing the prescriber with a particular SPI
	 * @param spi
	 * @return
	 */
	public String retrieveAccountIdFromSPI (String spi);
	/**
	 * The function is used to retrieve the MenuData based on the relatesToMessageId
	 *  and set the status(Success/Error) accordingly.
	 * @param relatesToMessageId
	 * @param surescriptStatus
	 * @return MenuData
	 */
	public MenuData retrieveMenuData(String relatesToMessageId, String surescriptStatus);
	/**
	 * This function is used to insert the prescriberId into the menudata corresponding to the NewRX
	 * @author mohammed
	 * @param prescriberOrderNumber
	 * @return menuData
	 */
	public MenuData incorporatePrescriberDetailsInNewRX(String prescriberOrderNumber , String prescriberId);
	/**
	 * Marshal the specified Trim object graph back to the specified document.
	 * @param trim
	 * @param docXML
	 * @throws JAXBException
	 * @throws TRIMException
	 */
	public void marshalToDocument( Trim trim, DocXML docXML ) throws JAXBException, TRIMException;
	/**
	 * An existing placeholder is transitioned (updated). The placeholder document is immutable. So we create
	 * a new document.
	 * We won't actually change the placeholder until the change is submitted.
	 */
	public MenuData genTRIMEvent( MenuData mdPlaceholder, AccountUser accountUser, String transitionName, Date now, String messageType,PrivateKey privateKey)throws JAXBException, TRIMException;
	/**
	 * Method to get the MenuData based on relates to message id.
	 * @param relatesToMessageId
	 * @return
	 */
	public MenuData getMdFromMessageId(String relatesToMessageId);

	/**
	 * Method to retrieve all the spi numbers corresponding to a prescriber
	 */
	public ArrayList<String> retrieveAllSPIs(long prescId);
	/**
	 * Method to retrieve all the prescriberLocation Details corresponding to a prescriber
	 */
	public ArrayList<MenuData> retrieveAllPrescriberLocations(long id);
	/**
	 * Method to retrieve all the Administration Details corresponding to a prescriber order number
	 */
	//CCHITMerge:Use CCHITBean.findMenuChildren() instead
	//public ArrayList<MenuData> retrieveAllAdministrationDetails(long id);
	/**
	 * Method to alter the Table entry in admin SurescriptsResponse
	 * @param relatesToMessageId
	 * @return
	 */
	public String retrieveMdFromRefillResponseData(String relatesToMessageId, String status);
	/**
	 * Method to retrieve the Prescriber Name by passing the SPI number
	 * @param spi
	 * @return
	 */
	public String retrievePrescriberNameFromId(String spi);
	/**
	 * Method to check whether the refillRequest was submitted or not.
	 * @param mdRefill
	 * @return
	 */
	public MenuData getMdFromFieldAndValue(String field, String value);
	
	public boolean checkForSubmittedData(MenuData mdRefill);

}
