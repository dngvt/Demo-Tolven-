package org.tolven.surescripts;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.tolven.app.entity.DrugQualifier;
import org.tolven.surescripts.entity.Pharmacy;
import org.tolven.surescripts.entity.PreferredPharmacy;


public interface PharmacyLocal {
	/**
	 * Method to download the flat file from SureScripts.
	 */
	public String downloadFlatFile(PrivateKey privateKey);
	/**
	 * Method to download the flat file from SureScripts.
	 */
	public String downloadNightlyFlatFile(PrivateKey privateKey);
	/**
	 * Adds a pharmacy to the database.
	 */
	public void addPharmacy(PharmacyVO pharmacyVO);
	/**
	 * Applicable only for full weekly upload.Changes the status of all existing
	 * pharmacies to deleted.
	 */
	public void changeStatusToDeleted();
	/**
	 * Finds the pharmacy with the NCPDP ID specified
	 * @param ncpdpId
	 * @return PharmacyVO which contains pharmacy data
	 */
	public PharmacyVO findPharmacyById(String ncpdpId);
	/**
     * Method to retrieve all supported pharmacies
     * @return
     */
	public Map<String , String> findSupportedPharmacies(List<String> pharmacyList);
	
	/**
	 * Function used to count pharmacies
	 * 
	 * @author Suja Sundaresan
	 * added on 05/Dec/2010
	 * @param Long offest
	 * @param Long limit
	 * @param String filterValue
	 * @return long
	 */
	public long countPharmacies(Long offest, Long limit, String filterValue);
	
	/**
	 * Function used to get all pharmacies
	 * 
	 * @author Suja Sundaresan
	 * added on 05/Dec/2010
	 * @param Map<String, Object> map
	 * @return List<Pharmacy>
	 */
	public List<Pharmacy> findPharmacies(Map<String, Object> map);
	
	/**
	 * Function used to count preferred pharmacies
	 * 
	 * @author Suja Sundaresan
	 * added on 05/Dec/2010
	 * @param Map<String, Object> map
	 * @return long
	 */
	public long countPreferredPharmacies(Map<String, Object> map);
	
	/**
	 * Function used to get all preferred pharmacies
	 * 
	 * @author Suja Sundaresan
	 * added on 05/Dec/2010
	 * @param Map<String, Object> map
	 * @return List<PreferredPharmacy>
	 */
	public List<PreferredPharmacy> findPreferredPharmacies(Map<String, Object> map);

    public String getDosageForm(String input, boolean isId);
    
    public ArrayList<DrugQualifier> retrieveAllDrugQualifiers();
}
