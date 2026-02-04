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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tolven.app.MenuLocal;
import org.tolven.app.entity.DosageForm;
import org.tolven.app.entity.DrugQualifier;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.entity.Account;
import org.tolven.logging.TolvenLogger;
import org.tolven.surescripts.PharmacyLocal;
import org.tolven.surescripts.PharmacyVO;
import org.tolven.surescripts.SurescriptsLocal;
import org.tolven.surescripts.entity.Pharmacy;
import org.tolven.surescripts.entity.PreferredPharmacy;

/**
 * <p>The bean class performs function related to pharmacy.</p>
 * @author unni.s@cyrusxp.com
 */
/*
	===========================================================================================================================
	No:  	|Created/Updated Date   |Created/Updated By      |Method name/Comments             									|
	===========================================================================================================================
	1    	| 02/23/2011           	|Unnikrishnan Skandappan |Functionality to search pharmacy by addLine1, city, state and zip.|
	============================================================================================================================
*/

@Stateless()
@Local(PharmacyLocal.class)

public class PharmacyBean implements PharmacyLocal{

	@EJB MenuLocal menuBean;
	@EJB SurescriptsLocal surescriptsBean;
	@EJB TolvenPropertiesLocal tproperties;
	@PersistenceContext private EntityManager em;
	
	public TolvenPropertiesLocal getTproperties() {
		return tproperties;
	}
	public void setTproperties(TolvenPropertiesLocal tproperties) {
		this.tproperties = tproperties;
	}
	public SurescriptsLocal getSurescriptsBean() {
		return surescriptsBean;
	}
	public void setSurescriptsBean(SurescriptsLocal surescriptsBean) {
		this.surescriptsBean = surescriptsBean;
	}
	/**
	 * Method to download the flat file from SureScripts.
	 */
	public String downloadFlatFile(PrivateKey privateKey) {
		String status="no";
		try {
			String downloadFileName = getSurescriptsBean().generateDirectoryDownloadMessage(privateKey); 
			System.out.println("Inside pharmacy bean: "+downloadFileName);
			if(null != downloadFileName && !downloadFileName.equals("")){
				
				
				URLConnection con = null;
//				URL url = new URL(tproperties.getProperty("surescripts.download.url")+"/"+downloadFileName);
				
				URL url = new URL("https://erxtest.tolven.org/Tolven/eRxOutbox");
				con = url.openConnection();
				con.setDoOutput(true);
				con.setDoInput(true);
				con.setRequestProperty("RequestType", "DOWNLOAD");
				con.setRequestProperty("FileName", downloadFileName);
				con.setRequestProperty("content-type", "binary/data");
//				String fileName = URLEncoder.encode(downloadFileName, "UTF-8");

				String data = URLEncoder.encode("fileName", "UTF-8") + "=" + URLEncoder.encode(downloadFileName, "UTF-8");

				OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
				out.write(data);
				out.flush();
				InputStream in = con.getInputStream();
				FileOutputStream fout = new FileOutputStream(downloadFileName);
				byte buffer1[] = new byte[1024*128];
				int k=0;
				while( (k = in.read(buffer1)) != -1 ){
				fout.write(buffer1,0,k);
				}
				out.close();
				getZipFiles(downloadFileName, "WEEK_FULL");
				TolvenLogger.info("Download Completed Successfully.", PharmacyBean.class);
				status="yes";
			}else{
				status="no";
				TolvenLogger.info("No Updates Available for the Time Being.", PharmacyBean.class);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}
	/**
	 * Method to download the flat file from SureScripts.
	 */
	public String downloadNightlyFlatFile(PrivateKey privateKey) {
		String status="no";
		try {
			String downloadFileName = getSurescriptsBean().generateNightlyDirectoryDownloadMessage(privateKey); 
			System.out.println(downloadFileName);
			if(null != downloadFileName && !downloadFileName.equals("")){
				URLConnection con = null;
//				URL url = new URL(tproperties.getProperty("surescripts.download.url")+"/"+downloadFileName);
				
				URL url = new URL("https://erxtest.tolven.org/Tolven/eRxOutbox");
				con = url.openConnection();
				con.setDoOutput(true);
				con.setDoInput(true);
				con.setRequestProperty("RequestType", "DOWNLOAD");
				con.setRequestProperty("FileName", downloadFileName);
				con.setRequestProperty("content-type", "binary/data");

				String data = URLEncoder.encode("fileName", "UTF-8") + "=" + URLEncoder.encode(downloadFileName, "UTF-8");
				
				OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
				out.write(data);
				out.flush();
				InputStream in = con.getInputStream();
				FileOutputStream fout = new FileOutputStream(downloadFileName);
				byte buffer1[] = new byte[1024*128];
				int k=0;
				while( (k = in.read(buffer1)) != -1 ){
				fout.write(buffer1,0,k);
				}
				out.close();
				
//				InputStream in = con.getInputStream();
//				FileOutputStream fout = new FileOutputStream(downloadFileName);
//				byte buffer1[] = new byte[1024*128];
//				int k=0;
//				while( (k = in.read(buffer1)) != -1 ){
//				fout.write(buffer1,0,k);
//				}
//				fout.close();
//				fout = null;
				getZipFiles(downloadFileName, "DAILY_NIGHT");
				TolvenLogger.info("Download Completed Successfully.", PharmacyBean.class);
				status="yes";
			}else{
				TolvenLogger.info("No Updates Available for the Time Being.", PharmacyBean.class);
				status="no";
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}
	/**
	 * Method that extracts the downloaded Zip File and copies the contents to FlatFile.txt
	 * @param filename
	 */
	private void getZipFiles(String filename, String downloadType) {
		try {
			try {
				String entryName;
				String destinationname = null;
				if (downloadType.equals("WEEK_FULL")) {
					destinationname = tproperties.getProperty("eprescription.surescripts.download.directory")+"/weekly_full";
				} else if (downloadType.equals("DAILY_NIGHT")){
					destinationname = tproperties.getProperty("eprescription.surescripts.download.directory")+"/daily_night";
				}
				 
				File downloadDir = new File(destinationname);
				if (!downloadDir.exists())
					downloadDir.mkdirs();
				byte[] buf = new byte[1024];
				ZipInputStream zipinputstream = null;
				ZipEntry zipentry;
				zipinputstream = new ZipInputStream(new FileInputStream(
						filename));

				zipentry = zipinputstream.getNextEntry();
				while (zipentry != null) {
					// for each entry to be extracted
					entryName = zipentry.getName();
					int n;
					FileOutputStream fileoutputstream;
					File newFile = new File(entryName);
					String directory = newFile.getParent();

					if (directory == null) {
						if (newFile.isDirectory())
							break;
					}

					fileoutputstream = new FileOutputStream(destinationname
							+ "/FlatFile.txt");

					while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
						fileoutputstream.write(buf, 0, n);

					fileoutputstream.close();
					zipinputstream.closeEntry();
					zipentry = zipinputstream.getNextEntry();

				}// while

				zipinputstream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception et) {
			et.printStackTrace();
		}
	}
	
	/**
	 * Adds a pharmacy to the database.
	 */
	public void addPharmacy(PharmacyVO pharmacyVO) {
		try {
			String qs = null;
			Query query = null;
			qs = String.format(Locale.US, "SELECT pharmacy FROM Pharmacy pharmacy WHERE pharmacy.id = :id");
			query = em.createQuery( qs );
			query.setParameter("id", pharmacyVO.getNcpdpid());
			if(query.getResultList() != null && query.getResultList().size() > 0){
				Pharmacy pharmacy = (Pharmacy) query.getResultList().get(0);
				setPharmacyRecord(pharmacyVO, pharmacy);
				em.merge(pharmacy);
			} else {
				Pharmacy pharmacyNew =  new Pharmacy();
				setPharmacyRecord(pharmacyVO, pharmacyNew);
				em.persist(pharmacyNew);
			}
		} catch (Exception e) {
			TolvenLogger.error("Exception occured while adding pharmacy", PharmacyBean.class);
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the Pharmacy object with the input.
	 * @param pharmacyVO
	 * @param pharmacy
	 */
	private void setPharmacyRecord(PharmacyVO pharmacyVO, Pharmacy pharmacy) {
		
		if (pharmacy.getId() == null) {
			pharmacy.setId(pharmacyVO.getNcpdpid());
		}
		
		if (pharmacyVO.getStoreNumber() != null) {
			pharmacy.setStoreNumber(pharmacyVO.getStoreNumber());
		}
		if (pharmacyVO.getReferenceNumberAlt1() != null) {
			pharmacy.setReferenceNumberAlt1(pharmacyVO.getReferenceNumberAlt1());
		}
		if (pharmacyVO.getReferenceNumberAlt1Qualifier() != null) {
			pharmacy.setReferenceNumberAlt1Qualifier(pharmacyVO.getReferenceNumberAlt1Qualifier());
		}
		if (pharmacyVO.getStoreName() != null) {
			pharmacy.setStoreName(pharmacyVO.getStoreName());
		}
		if (pharmacyVO.getAddressLine1() != null) {
			pharmacy.setAddressLine1(pharmacyVO.getAddressLine1());
		}
		if (pharmacyVO.getAddressLine2() != null) {
			pharmacy.setAddressLine2(pharmacyVO.getAddressLine2());
		}
		if (pharmacyVO.getCity() != null) {
			pharmacy.setCity(pharmacyVO.getCity());
		}
		if (pharmacyVO.getState() != null) {
			pharmacy.setState(pharmacyVO.getState());
		}
		if (pharmacyVO.getZip() != null) {
			pharmacy.setZip(pharmacyVO.getZip());
		}
		if (pharmacyVO.getPhonePrimary() != null) {
			pharmacy.setPhonePrimary(pharmacyVO.getPhonePrimary());
		}
		if (pharmacyVO.getFax() != null) {
			pharmacy.setFax(pharmacyVO.getFax());
		}
		if (pharmacyVO.getEmail() != null) {
			pharmacy.setEmail(pharmacyVO.getEmail());
		}
		if (pharmacyVO.getPhoneAlt1() != null) {
			pharmacy.setPhoneAlt1(pharmacyVO.getPhoneAlt1());
		}
		if (pharmacyVO.getPhoneAlt1Qualifier() != null) {
			pharmacy.setPhoneAlt1Qualifier(pharmacyVO.getPhoneAlt1Qualifier());
		}
		if (pharmacyVO.getPhoneAlt2() != null) {
			pharmacy.setPhoneAlt2(pharmacyVO.getPhoneAlt2());
		}
		if (pharmacyVO.getPhoneAlt2Qualifier() != null) {
			pharmacy.setPhoneAlt2Qualifier(pharmacyVO.getPhoneAlt2Qualifier());
		}
		if (pharmacyVO.getPhoneAlt3() != null) {
			pharmacy.setPhoneAlt3(pharmacyVO.getPhoneAlt3());
		}
		if (pharmacyVO.getPhoneAlt3Qualifier() != null) {
			pharmacy.setPhoneAlt3Qualifier(pharmacyVO.getPhoneAlt3Qualifier());
		}
		if (pharmacyVO.getPhoneAlt4() != null) {
			pharmacy.setPhoneAlt4(pharmacyVO.getPhoneAlt4());
		}
		if (pharmacyVO.getPhoneAlt4Qualifier() != null) {
			pharmacy.setPhoneAlt4Qualifier(pharmacyVO.getPhoneAlt4Qualifier());
		}
		if (pharmacyVO.getPhoneAlt5() != null) {
			pharmacy.setPhoneAlt5(pharmacyVO.getPhoneAlt5());
		}
		if (pharmacyVO.getPhoneAlt5Qualifier() != null) {
			pharmacy.setPhoneAlt5Qualifier(pharmacyVO.getPhoneAlt5Qualifier());
		}
		if (pharmacyVO.getActiveStartTime() != null) {
			pharmacy.setActiveStartTime(pharmacyVO.getActiveStartTime());
		}
		if (pharmacyVO.getActiveEndTime() != null) {
			pharmacy.setActiveEndTime(pharmacyVO.getActiveEndTime());
		}
		if (pharmacyVO.getServiceLevel() != null) {
			pharmacy.setServiceLevel(pharmacyVO.getServiceLevel());
		}
		if (pharmacyVO.getPartnerAccount() != null) {
			pharmacy.setPartnerAccount(pharmacyVO.getPartnerAccount());
		}
		if (pharmacyVO.getLastModifiedDate() != null) {
			pharmacy.setLastModifiedDate(pharmacyVO.getLastModifiedDate());
		}
		if (pharmacyVO.getTwentyFourHourFlag() != null) {
			pharmacy.setTwentyFourHourFlag(pharmacyVO.getTwentyFourHourFlag());
		}
		if (pharmacyVO.getCrossStreet() != null) {
			pharmacy.setCrossStreet(pharmacyVO.getCrossStreet());
		}
		if (pharmacyVO.getRecordChange() != null) {
			pharmacy.setRecordChange(pharmacyVO.getRecordChange());
		}
		if (pharmacyVO.getOldServiceLevel() != null) {
			pharmacy.setOldServiceLevel(pharmacyVO.getOldServiceLevel());
		}
		if (pharmacyVO.getTextServiceLevel() != null) {
			pharmacy.setTextServiceLevel(pharmacyVO.getTextServiceLevel());
		}
		if (pharmacyVO.getTextServiceLevelChange() != null) {
			pharmacy.setTextServiceLevelChange(pharmacyVO.getTextServiceLevelChange());
		}
		if (pharmacyVO.getVersion() != null) {
			pharmacy.setVersion(pharmacyVO.getVersion());
		}
		if (pharmacyVO.getNpi() != null) {
			pharmacy.setNpi(pharmacyVO.getNpi());
		}
		
		pharmacy.setNewRx(pharmacyVO.getNewRx());
		pharmacy.setRefReq(pharmacyVO.getRefReq());
		pharmacy.setRxChg(pharmacyVO.getRxChg());
		pharmacy.setRxFill(pharmacyVO.getRxFill());
		pharmacy.setCanRx(pharmacyVO.getCanRx());
		pharmacy.setMedicatioHistory(pharmacyVO.getMedicatioHistory());
		pharmacy.setEligibility(pharmacyVO.getEligibility());
		
		if (pharmacyVO.getStatus() != null) {
			pharmacy.setStatus(pharmacyVO.getStatus());
		}
		
	}
	
	/**
	 * Applicable only for full weekly upload.Changes the status of all existing
	 * pharmacies to deleted.
	 */
	@SuppressWarnings("unchecked")
	public void changeStatusToDeleted() {
		int count = 0;
		String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT pharmacy FROM Pharmacy pharmacy WHERE pharmacy.status = 'new'");
		query = em.createQuery( qs );
		if (query.getResultList() != null && query.getResultList().size() > 0) {
			List<Pharmacy> pharmacies = (ArrayList<Pharmacy>) query.getResultList();
			for(Pharmacy pharmacy : pharmacies){
				if (pharmacy.getStatus().equals("new")) {
					pharmacy.setStatus("deleted");
					em.persist(pharmacy);
					count++;
				}
			}
		}
		TolvenLogger.info("Changed status of Pharmacies to deleted. Count: "+ count, PharmacyBean.class);
	}
	
	/**
	 * Finds the pharmacy with the NCPDP ID specified
	 * @param ncpdpId
	 * @return PharmacyVO which contains pharmacy data
	 */
	public PharmacyVO findPharmacyById(String ncpdpId) {
		try {
			PharmacyVO pharmacyVO = new PharmacyVO();
			String qs = null;
			Query query = null;
			qs = String.format(Locale.US, "SELECT pharmacy FROM Pharmacy pharmacy WHERE pharmacy.id = :id");
			query = em.createQuery( qs );
			query.setParameter("id", ncpdpId);
			if (query.getResultList() != null && query.getResultList().size() > 0) {
				Pharmacy pharmacy = (Pharmacy)query.getResultList().get(0);
				
				pharmacyVO.setNcpdpid(pharmacy.getId());
				if (pharmacy.getStoreName() != null) {
					pharmacyVO.setStoreName(pharmacy.getStoreName());
				}
				if (pharmacy.getAddressLine1() != null) {
					pharmacyVO.setAddressLine1(pharmacy.getAddressLine1());
				}
				if (pharmacy.getCity() != null) {
					pharmacyVO.setCity(pharmacy.getCity());					
				}
				if (pharmacy.getState() != null) {
					pharmacyVO.setState(pharmacy.getState());	
				}
				if (pharmacy.getZip() != null) {
					pharmacyVO.setZip(pharmacy.getZip());
				}
				if (pharmacy.getPhonePrimary() != null) {
					pharmacyVO.setPhonePrimary(pharmacy.getPhonePrimary());
				}
				if (pharmacy.getFax() != null) {
					pharmacyVO.setFax(pharmacy.getFax());
				}
				if (pharmacy.getEmail() != null) {
					pharmacyVO.setEmail(pharmacy.getEmail());
				}
				if (pharmacy.getFax() != null) {
					pharmacyVO.setFax(pharmacy.getFax());
				}
				if (pharmacy.getActiveStartTime() != null) {
					pharmacyVO.setActiveStartTime(pharmacy.getActiveStartTime());
				}
				if (pharmacy.getActiveEndTime() != null) {
					pharmacyVO.setActiveEndTime(pharmacy.getActiveEndTime());
				}
				if (pharmacy.getServiceLevel() != null) {
					pharmacyVO.setServiceLevel(pharmacy.getServiceLevel());
				}
				if (pharmacy.getLastModifiedDate() != null) {
					pharmacyVO.setLastModifiedDate(pharmacy.getLastModifiedDate());
				}
				if (pharmacy.getNpi() != null) {
					pharmacyVO.setNpi(pharmacy.getNpi());
				}
				
				return pharmacyVO;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
     * Method to retrieve all supported pharmacies
     * @return
     */
    @SuppressWarnings("unchecked")
	public Map<String , String> findSupportedPharmacies(List<String> pharmacyList) {
    	   	
    	Map<String, String> pharmacies = new TreeMap<String, String>();
    	for(String pharmacyId : pharmacyList) {
        	String qs = null;
    		Query query = null;
    		qs = String.format(Locale.US, "SELECT pharmacy FROM Pharmacy pharmacy WHERE" +
    				" pharmacy.id = :id AND pharmacy.newRx = true AND pharmacy.status = 'new' AND pharmacy.serviceLevel != 0");
    		query = em.createQuery(qs);
    		query.setParameter("id", pharmacyId);
    		if(query.getResultList() != null){
    			List<Pharmacy> filteredPharmacies = (ArrayList<Pharmacy>)query.getResultList();
    			for(Pharmacy pharmacy : filteredPharmacies) {
    				Date now = new Date();
    				if (pharmacy.getActiveStartTime().before(now) && pharmacy.getActiveEndTime().after(now)) {
            			pharmacies.put(pharmacy.getId(), pharmacy.getStoreName());
					}
    			}
    		}
    	}
    	return pharmacies;
    }
    

	/**
	 * Function used to count pharmacies
	 * @author Suja Sundaresan
	 * added on 05/Dec/2010
	 * Modified on 02/23/2011
	 * @author unni.s@cyrusxp.com
	 * @param Long offest
	 * @param Long limit
	 * @param String filterValue
	 * @return long
	 */
	public long countPharmacies(Long offest, Long limit, String filterValue) {
		long count = 0;
		try {
			StringBuilder queryBuilder = new StringBuilder("SELECT count(p) from Pharmacy p WHERE status='new'");
			String storeName = "";
			long zip = -1;
			if (filterValue!=null && !filterValue.equals("")){
				queryBuilder.append(" AND (lower(p.storeName) like :storeName");
				queryBuilder.append(" OR lower(p.addressLine1) like :storeName OR lower(p.city) like :storeName");
				queryBuilder.append(" OR lower(p.state) like :storeName");
				storeName = "%" + filterValue.toLowerCase() + "%";
				try {
					zip = Long.parseLong(filterValue);
					queryBuilder.append(" OR p.zip =:zip) ");
				} catch (NumberFormatException e) {
					queryBuilder.append(")");
					TolvenLogger.warn("Search input is String cannot match for Zip code(Long).", PharmacyBean.class);
				}
			}
			Query query = em.createQuery(queryBuilder.toString());
			if (zip != -1) {
				query.setParameter("zip", zip);
			}
			if (!storeName.equals(""))
				query.setParameter("storeName", storeName);
			if (offest != null && offest > 0)
				query.setFirstResult(offest.intValue());
			if (limit != null && limit>0)
				query.setMaxResults(limit.intValue());
			Long rslt = (Long) query.getSingleResult();
			return rslt.longValue();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * Filters pharmacy based on StoreName, AddLine1, City, State or Zip.
	 * @author Suja Sundaresan
	 * added on 05/Dec/2010
	 * @Modified on 02/23/2011
	 * @author unni.s@cyrusxp.com
	 * @param Map<String, Object> map
	 * @return List<Pharmacy>
	 */
	public List<Pharmacy> findPharmacies(Map<String, Object> map) {
		try {
			StringBuilder queryBuilder = new StringBuilder("SELECT p from Pharmacy p WHERE status='new'");
			String storeName = "";
			long zip = -1;
			if (map.get("filter")!=null && !map.get("filter").toString().equals("")) {
				queryBuilder.append(" AND (lower(p.storeName) like :storeName");
				queryBuilder.append(" OR lower(p.addressLine1) like :storeName OR lower(p.city) like :storeName");
				queryBuilder.append(" OR lower(p.state) like :storeName");
				storeName = "%" + map.get("filter").toString().toLowerCase() + "%";
				try {
					zip = Long.parseLong(map.get("filter").toString());
					queryBuilder.append(" OR p.zip =:zip) ");
				} catch (NumberFormatException e) {
					queryBuilder.append(")");
					TolvenLogger.warn("Search input is String cannot match for Zip code(Long).", PharmacyBean.class);
				}
			}
			if (map.get("sort_col") != null && !map.get("sort_col").toString().equals("")) {
				String sortCol = map.get("sort_col").toString();
				if (sortCol.contains("pharmacy"))
					queryBuilder.append(" ORDER BY p.storeName");
				else if (sortCol.contains("address"))
					queryBuilder.append(" ORDER BY p.addressLine1");
				else
					queryBuilder.append(" ORDER BY p.").append(sortCol);
				if (map.get("sort_dir") != null && !map.get("sort_dir").toString().equals(""))
					queryBuilder.append(" " + map.get("sort_dir").toString());
			}
			Query query = em.createQuery(queryBuilder.toString());
			if (!storeName.equals(""))
				query.setParameter("storeName", storeName);
			if (zip != -1) {
				query.setParameter("zip", zip);
			}
			if (map.get("offset") != null && new Integer(map.get("offset").toString()).intValue() > 0)
				query.setFirstResult(new Integer(map.get("offset").toString()).intValue());
			if (map.get("page_size") != null && new Integer(map.get("page_size").toString()).intValue()>0)
				query.setMaxResults(new Integer(map.get("page_size").toString()).intValue());
				
			List<Pharmacy> pharmacies = query.getResultList();
			return pharmacies;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Function used to count preferred pharmacies
	 * @author Suja Sundaresan
	 * added on 05/Dec/2010
	 * @param Map<String, Object> map
	 * @return long
	 */
	public long countPreferredPharmacies(Map<String, Object> map) {
		long count = 0;
		try {
			Account account = (Account) map.get("account");
			MenuStructure ms = menuBean.findAccountMenuStructure(account.getId(), "echr:patient:pharmacy");
			String path = "echr:patient-" +  map.get("patientId").toString();
			
			String queryString = "SELECT count(md) from MenuData md, Pharmacy p WHERE p.status='new' " +
								"AND md.status='ACTIVE' AND md.menuStructure=:ms AND md.account=:account " +
								"AND md.parentPath01=:path AND md.string01=p.id ";
			String storeName = "";
			if (map.get("filter")!=null && !map.get("filter").toString().equals("")) {
				queryString += " AND lower(p.storeName) like :storeName";
				storeName = "%" + map.get("filter").toString().toLowerCase() + "%";
			}
			Query query = em.createQuery(queryString);
			query.setParameter("ms", ms);
			query.setParameter("account", account);
			query.setParameter("path", path);
			if (!storeName.equals(""))
				query.setParameter("storeName", storeName);
			
			if (map.get("offset") != null && new Integer(map.get("offset").toString()).intValue() > 0)
				query.setFirstResult(new Integer(map.get("offset").toString()).intValue());
			if (map.get("page_size") != null && new Integer(map.get("page_size").toString()).intValue()>0)
				query.setMaxResults(new Integer(map.get("page_size").toString()).intValue());
			Long rslt = (Long) query.getSingleResult();
			return rslt.longValue();
		}
		catch (Exception e) {
			e.printStackTrace();
			return count;
		}
	}
	
	/**
	 * Function used to get all preferred pharmacies
	 * @author Suja Sundaresan
	 * added on 05/Dec/2010
	 * @param Map<String, Object> map
	 * @return List<PreferredPharmacy>
	 */
	public List<PreferredPharmacy> findPreferredPharmacies(Map<String, Object> map) {
		try {
			Account account = (Account) map.get("account");
			MenuStructure ms = menuBean.findAccountMenuStructure(account.getId(), "echr:patient:pharmacy");
			String path = "echr:patient-" +  map.get("patientId").toString();
			
			String queryString = "SELECT p.id, p.storeName, p.addressLine1, p.city, p.state, p.zip, " +
								"p.activeEndTime, p.serviceLevel, p.newRx, p.refReq, p.rxChg, p.rxFill, " +
								"p.canRx, p.medicatioHistory, p.eligibility, md.id as menuDataId, " +
								"md.parentPath01 as parentPath from MenuData md, Pharmacy p WHERE p.status='new' " +
								"AND md.status='ACTIVE' AND md.menuStructure=:ms AND md.account=:account " +
								"AND md.parentPath01=:path AND md.string01=p.id ";
			String storeName = "";
			if (map.get("filter")!=null && !map.get("filter").toString().equals("")) {
				queryString += " AND lower(p.storeName) like :storeName";
				storeName = "%" + map.get("filter").toString().toLowerCase() + "%";
			}
			if (map.get("sort_col") != null && !map.get("sort_col").toString().equals("")) {
				String sortCol = map.get("sort_col").toString();
				if (sortCol.contains("pharmacy") || sortCol.contains("store_name"))
					queryString += " ORDER BY p.storeName";
				else if (sortCol.contains("address"))
					queryString += " ORDER BY p.addressLine1";
				else if (sortCol.contains("NCPDPID") || sortCol.contains("ncpdpid"))
					queryString += " ORDER BY p.id";
				else
					queryString += " ORDER BY p." + sortCol;
				if (map.get("sort_dir") != null && !map.get("sort_dir").toString().equals("")) 
					queryString += " " + map.get("sort_dir").toString();
			}
			
			Query query = em.createQuery(queryString);
			query.setParameter("ms", ms);
			query.setParameter("account", account);
			query.setParameter("path", path);
			if (!storeName.equals(""))
				query.setParameter("storeName", storeName);
			
			if (map.get("offset") != null && new Integer(map.get("offset").toString()).intValue() > 0)
				query.setFirstResult(new Integer(map.get("offset").toString()).intValue());
			if (map.get("page_size") != null && new Integer(map.get("page_size").toString()).intValue()>0)
				query.setMaxResults(new Integer(map.get("page_size").toString()).intValue());
			
			List<PreferredPharmacy> pharmacies = new ArrayList<PreferredPharmacy>();
			List<Object> relt = query.getResultList();
			Iterator<Object> it = relt.iterator();
			while ( it.hasNext() ) {
			    Object[] obj = (Object[]) it.next();
			    PreferredPharmacy preferredPharmacy = new PreferredPharmacy();
			    preferredPharmacy.setId(obj[0].toString());
			    preferredPharmacy.setStoreName(obj[1].toString());
			    preferredPharmacy.setAddressLine1(obj[2].toString());
			    preferredPharmacy.setCity(obj[3].toString());
			    preferredPharmacy.setState(obj[4].toString());
			    preferredPharmacy.setZip(new Long(obj[5].toString()));
			    preferredPharmacy.setActiveEndTime((Date)(obj[6]));
			    preferredPharmacy.setServiceLevel(new Long(obj[7].toString()));
			    preferredPharmacy.setNewRx(new Boolean(obj[8].toString()));
			    preferredPharmacy.setRefReq(new Boolean(obj[9].toString()));
			    preferredPharmacy.setRxChg(new Boolean(obj[10].toString()));
			    preferredPharmacy.setRxFill(new Boolean(obj[11].toString()));
			    preferredPharmacy.setCanRx(new Boolean(obj[12].toString()));
			    preferredPharmacy.setMedicatioHistory(new Boolean(obj[13].toString()));
			    preferredPharmacy.setEligibility(new Boolean(obj[14].toString()));
			    preferredPharmacy.setMenuDataId(new Long(obj[15].toString()).longValue());
			    preferredPharmacy.setParentPath(obj[16].toString());
			    if (preferredPharmacy.getActiveEndTime().before(new Date()))
			    	preferredPharmacy.setInactive(true);
			    else
			    	preferredPharmacy.setInactive(false);
			    if (preferredPharmacy.getInactive() && (preferredPharmacy.getServiceLevel()!=null && preferredPharmacy.getServiceLevel().longValue()==0))
			    	preferredPharmacy.setDisabled(true);
			    else
			    	preferredPharmacy.setDisabled(false);
			    pharmacies.add(preferredPharmacy);
			}
			return pharmacies;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

    /**
     * Method to find the dosageForm id/desc when desc/id is provided.
     * @param input
     * @param isId
     * @return
     */
	@Override
    public String getDosageForm(String input, boolean isId){
        String output = "";
        if (input!=null) {
            String qs = null;
            Query query = null;
            if (isId) {
                qs = String.format(Locale.US, "SELECT df FROM DosageForm df WHERE id="+input);
                query = em.createQuery( qs );
            } else {
                qs = String.format(Locale.US, "SELECT df FROM DosageForm df WHERE df.definition =:input");
                query = em.createQuery( qs );
                query.setParameter("input", input);
            }
            if(query.getResultList() != null && query.getResultList().size() > 0){
                if (isId) {
                    output = ((DosageForm)query.getResultList().get(0)).getDefinition();
                } else {
                    output = String.valueOf(((DosageForm)query.getResultList().get(0)).getId());
                }
            }
        }
        return output;
    }
    /**
     * Method to retrieve all the Drug Qualifiers
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<DrugQualifier> retrieveAllDrugQualifiers(){
        ArrayList<DrugQualifier> drugQualifiers = null;
        String qs = null;
        Query query = null;
        qs = String.format(Locale.US, "SELECT dr FROM DrugQualifier dr");
        query = em.createQuery( qs );
        if(null != query.getResultList()){
            drugQualifiers = (ArrayList<DrugQualifier>)query.getResultList();
        }
        return drugQualifiers;
    }

}
