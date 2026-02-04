package org.tolven.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.el.ELFunctions;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenRequest;
import org.tolven.el.ExpressionEvaluator;
import org.tolven.logging.TolvenLogger;
import org.tolven.surescripts.PharmacyVO;
import org.tolven.trim.CE;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.SETIISlot;
import org.tolven.trim.ST;
import org.tolven.trim.ex.ActEx;

/**
 * This class is used to set the details of Prior allegy&medication details, Pharmacy and Prescriber details in the newRx trim that needs
 * to be send to SureScripts.
 */
public class Prescription extends SurescriptsComputeBase {
	
	

	private String name;
	private String allergy;
	private String priorMedication;
	private String medicationPath;
	private String allergyPath;
	private String prescriberPath ;
	private long daysSupply;
	private long frequency = 0;
	private long dispenseAmount = 0;

	private long prescriberOrderNumber;
	private boolean enabled;
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}	
	public Prescription(){
		super();
	}

	@SuppressWarnings("unchecked")
	public void compute() throws Exception {
		ActEx act = (ActEx) this.getAct();
		if (isEnabled()) {
			ExpressionEvaluator evaluator = new ExpressionEvaluator();
			evaluator.addVariable("act", this.getAct());
			boolean allergiesOk = false;
			boolean priorMedicationsOk = false;
		
			if(getAccountUser().getOpenMeFirst() != null){
				String[] providerSplit = getAccountUser().getOpenMeFirst().split("-");
				if(providerSplit.length == 2)
					prescriberPath = providerSplit[1];
				else{
					//provider is not selected
					return;
				}
			}else{
				return;// account level prescriber is not selected
			}
			//set the notification preferences value in to Trim
			Long id = Long.valueOf(prescriberPath);
			MenuData prescriber = getMenuBean().findMenuDataItem(id);
			evaluator.addVariable("prescriber", prescriber);
			evaluator.setValue("#{act.relationship['physicianNotificationPreference'].act.observation.value.ST}",(String)prescriber.getExtendedField("notification"));
		
			List<ObservationValueSlot> surescriptOVS = (List<ObservationValueSlot>)evaluator.evaluate("#{act.relationship['toSureScripts'].act.observation.values}");
			List<ObservationValueSlot> medicationOVS = (List<ObservationValueSlot>)evaluator.evaluate("#{act.relationship['medicationDetails'].act.observation.values}");
			
			
			
			
			//Setting radio buttons at the time of re-submission.
			createCEValues(medicationOVS, 5);
			createCEValues(medicationOVS, 6);
			createCEValues(medicationOVS, 12);
			/*//correct this
			//TODO Set status to pending on discontinue medication.
			String newRxStatus = null;
			String isControlledSub = null;
			newRxStatus = surescriptOVS.get(40).getST().getValue();
			isControlledSub = surescriptOVS.get(50).getST().getValue();
			//If not dispensing form office.
			if (null != isControlledSub && isControlledSub.equals("true")) {
				surescriptOVS.get(40).setST(new MessageID().getST("Controlled Substance"));
			}else{
				if (!newRxStatus.equals("Verified") && !medicationOVS.get(5).getCE().getDisplayName().equals("") &&
						medicationOVS.get(5).getCE().getDisplayName().equals("Yes")) {
					surescriptOVS.get(40).setST(new MessageID().getST("From Office"));				
				}else if(!newRxStatus.equals("Verified") && !medicationOVS.get(5).getCE().getDisplayName().equals("") &&
						medicationOVS.get(5).getCE().getDisplayName().equals("No")) {
					if (newRxStatus.equals("") || newRxStatus.equals("Error") || newRxStatus.equals("From Office")){
						surescriptOVS.get(40).setST(new MessageID().getST("Pending"));
					}
				}
			}
			
			// Setting Prescriber Order Number in the trim 
			if (!act.getId().getIIS().get(0).getExtension().equals("")) {
				String extension = null;
				extension = act.getId().getIIS().get(0).getExtension();
				if (extension != null) {
					prescriberOrderNumber = Long.valueOf(extension.split("-")[2]);
					surescriptOVS.get(31).getINT().setValue(prescriberOrderNumber);
				}
			}
			*/	
		
			String patientPath = ELFunctions.internalId(getAccountUser().getAccount(), (SETIISlot)evaluator.evaluate("#{act.participation['subject'].role.id}"));
			MenuData patientMd = getMenuBean().findMenuDataItem(Long.parseLong(patientPath.substring(patientPath.lastIndexOf('-'))));
			evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[0].ST}",patientMd.getField("Name"));
			evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[29].ST}",patientMd.getField("DOB"));
			evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[30].ST}",patientMd.getField("Sex"));
			
			
			if (!StringUtils.isBlank(getAccountUser().getOpenMeFirst()) ) {
				String prescName= (String)evaluator.evaluate("#{prescriber.string01}, #{prescriber.string02} #{prescriber.string03}");
				
				//For setting parent04 to establish medication-prescriber link.
				if (null != act.getId()) {
					act.getParticipation().get("attender").getRole().getId()
							.getIIS().get(0).setRoot(act.getId().getIIS().get(0).getRoot());
					act.getParticipation().get("attender").getRole().getId()
							.getIIS().get(0).setExtension(getAccountUser().getOpenMeFirst());
				}

				//Entity prescriberEntity = prescriberTrim.getAct().getParticipations().get(0).getRole().getPlayer();
				
				/* Setting first name middle name last name without space */
				//List<ENXPSlot> prescriberName = prescriberEntity.getName().getENS().get(0).getParts();
				//prescName = prescName + prescriberName.get(0).getST().getValue() + ",";
				//if(null != prescriberName.get(1).getST().getValue() && !prescriberName.get(1).getST().getValue().equals(""))
					//prescName = prescName + prescriberName.get(1).getST().getValue();
				evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[1].ST}",prescName);
									
				/* Setting SPI AND NPI*/
				String SPI = (String)evaluator.evaluate("#{act.relationship['toSureScripts'].act.observation.values[28].ST}");//surescriptOVS.get(28).getST().getValue();
				
				//surescriptOVS.get(52).getST().setValue((String)prescriber.getExtendedField("npi"));
				evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[52].ST}",(String)prescriber.getExtendedField("npi"));
				
				boolean isDifferentLocation = false;
				for(MenuData assigned : getPrescriberLocations(id)){
					if(SPI.equals(assigned.getField("spiRoot").toString())){
						evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[2].ST}",assigned.getField("addr1"));
						evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[4].ST}",assigned.getField("city"));
						evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[5].ST}",assigned.getField("state"));
						evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[6].ST}",assigned.getField("zip"));
						evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[8].ST}",assigned.getField("phoneNumber"));
						evaluator.setValue("#{act.relationship['toSureScripts'].act.observation.values[7].ST}",assigned.getField("prescriberEmail"));
						//prescriberTrim.getAct().getRelationships().get(0).getAct().getObservation().getValues().get(11).getST().getValue()
						isDifferentLocation = true;
						break;
					}
				}
				if(!isDifferentLocation){
					/* Setting address*/
					surescriptOVS.get(2).getST().setValue(prescriber.getString05());
					surescriptOVS.get(3).getST().setValue((String)prescriber.getExtendedField("addr2"));
					// Setting city 
					surescriptOVS.get(4).getST().setValue((String)prescriber.getExtendedField("city"));
					 //Setting state 
					surescriptOVS.get(5).getST().setValue((String)prescriber.getExtendedField("state"));
					// Setting ZIP code
					surescriptOVS.get(6).getST().setValue((String)prescriber.getExtendedField("zip"));
					 //Setting email 
					surescriptOVS.get(7).getST().setValue((String)prescriber.getExtendedField("prescriberEmail"));
					 //Setting Fax  
					surescriptOVS.get(37).getST().setValue((String)prescriber.getExtendedField("fax"));
					surescriptOVS.get(62).getST().setValue((String)prescriber.getExtendedField("dea"));
					
				
				
					/* Setting phone number */
				// TELSlotEx telSlot = (TELSlotEx) prescriberTrim.getAct().getParticipations()
				//.get(0).getRole().getPlayer().getTelecom();
				String telNum =(String)prescriber.getExtendedField("phoneNumber");
				String exten = (String)prescriber.getExtendedField("extension");
				if(!StringUtils.isBlank(telNum)&& !StringUtils.isBlank(exten))
					surescriptOVS.get(8).getST().setValue(telNum +"x"+exten);				 
				 else
					 surescriptOVS.get(8).getST().setValue(telNum);
				 
				// Setting Prescriber Fax Number 
				 surescriptOVS.get(37).getST().setValue((String)prescriber.getExtendedField("fax"));
				}   
				
			   //Setting pharmacy value.
			   List<ObservationValueSlot> pharmacyOVS = act.getRelationship().get("pharmacy").getAct().getObservation().getValues();
				if (null != pharmacyOVS.get(0).getST().getValue() &&  pharmacyOVS.get(0).getST().getValue().trim().length() > 0) {
					PharmacyVO pharmacyVO = getPharmBean().findPharmacyById(pharmacyOVS.get(0).getST().getValue().trim());
					
					if (pharmacyVO.getStoreName() != null) {
						pharmacyOVS.get(1).setST(getST(pharmacyVO.getStoreName()));
					} else {
						pharmacyOVS.get(1).getST().setValue("");
					}
					if (pharmacyVO.getAddressLine1() != null) {
						pharmacyOVS.get(2).setST(getST(pharmacyVO.getAddressLine1()));
					} else {
						pharmacyOVS.get(2).getST().setValue("");
					}
					if (pharmacyVO.getCity() != null) {
						pharmacyOVS.get(3).setST(getST(pharmacyVO.getCity()));
					} else {
						pharmacyOVS.get(3).getST().setValue("");
					}
					if (pharmacyVO.getState() != null) {
						pharmacyOVS.get(4).setST(getST(pharmacyVO.getState()));
					} else {
						pharmacyOVS.get(4).getST().setValue("");
					}
					if (pharmacyVO.getZip() != null) {
						pharmacyOVS.get(5).setST(getST(String.valueOf(pharmacyVO.getZip())));
					} else {
						pharmacyOVS.get(5).getST().setValue("");
					}
					if (pharmacyVO.getPhonePrimary() != null) {
						pharmacyOVS.get(6).setST(getST(pharmacyVO.getPhonePrimary()));
					} else {
						pharmacyOVS.get(6).getST().setValue("");
					}
					if (pharmacyVO.getFax() != null) {
						pharmacyOVS.get(7).setST(getST(pharmacyVO.getFax()));
					} else {
						pharmacyOVS.get(7).getST().setValue("");
					}
				}
			}
			
			
			List<MenuData> priorallergies = getList(getAllergyPath());
			List<MenuData> priorMedications = getList(getMedicationPath());
			if (priorallergies != null && priorallergies.size() > 0) {
				allergy = getNames(priorallergies);
				surescriptOVS.get(10).setST(getST(allergy));
			}

			if (priorMedications != null) {
				priorMedication = getNames(priorMedications);
				surescriptOVS.get(11).setST(getST(priorMedication));
			}

			

			surescriptOVS.get(9).setST(getST(getAccountUser().getAccount().getTitle()));
			/*if (null != surescriptOVS.get(13).getST().getValue() 
					&& !surescriptOVS.get(13).getST().getValue().equals("")) {
				dispenseAmt = surescriptOVS.get(13).getST().getValue().trim();
				dispenseAmount = Integer.parseInt(dispenseAmt);
			}	*/		
		}	
		
	}
	/**
	 * Method to set Physician's Notification preferences in trim
	 * 
	 * @author Nevin added on 03/04/2011
	 * @param void
	 * @return void
	 *//*
	private void setPhysicianNotificationPreferences(String preferences,ActEx act) {
		Map<String, String> preferenceMap = new HashMap<String, String>();
		preferenceMap.put("Drug - Drug", "false");
		preferenceMap.put("Drug - Food", "false");
		preferenceMap.put("Drug - Allergy", "false");
		if(preferences.trim().length() == 0)
			return; // no preferences selected
		String[] notificationPreferencesList = preferences.split(",");
		for (String preference : notificationPreferencesList) {
			if (preference.trim().equals("Drug - Drug")) {
				preferenceMap.put("Drug - Drug", "true");
			} 
			else if (preference.trim().equals("Drug - Food")) {
				preferenceMap.put("Drug - Food", "true");
			} 	
			else if (preference.trim().equals("Drug - Allergy")) {
				preferenceMap.put("Drug - Allergy", "true");
			}
		}
		ActRelationship physicianPreferenceRel = act.getRelationship().get("physicianPreference");
		((ActEx)physicianPreferenceRel.getAct()).getRelationship().get("drugDrug").getAct().getObservation().getValues().get(0).setST(trimFactory.createNewST(preferenceMap.get("Drug - Drug")));
		((ActEx)physicianPreferenceRel.getAct()).getRelationship().get("drugFood").getAct().getObservation().getValues().get(0).setST(trimFactory.createNewST(preferenceMap.get("Drug - Food")));
		((ActEx)physicianPreferenceRel.getAct()).getRelationship().get("drugAllergy").getAct().getObservation().getValues().get(0).setST(trimFactory.createNewST(preferenceMap.get("Drug - Allergy")));
	
	}*/

	/**
	 * Method to set the CE value at the time of re-submission.
	 * @param medicationOVS
	 * @param index
	 */
	private void createCEValues(List<ObservationValueSlot> medicationOVS, int i) {
		CE _ce = new CE();
		if (!medicationOVS.get(i).getNew().getEncoded().equals("") 
				&& null == medicationOVS.get(i).getCE()) {
			String choice = medicationOVS.get(i).getNew().getEncoded().trim();
			if (choice.equals("Yes")) {
				_ce.setDisplayName(choice);
				_ce.setCode("C0024444");
			}else if (choice.equals("No")) {
				_ce.setDisplayName(choice);
				_ce.setCode("C0024554");
			}else if (choice.equals("R")) {
				_ce.setDisplayName(choice);
				_ce.setCode("C0034444");
			}else if (choice.equals("PRN")) {
				_ce.setDisplayName(choice);
				_ce.setCode("C0044554");
			}
			_ce.setCodeSystem("2.16.840.1.113883.6.56");
			_ce.setCodeSystemVersion("2007AA");
			medicationOVS.get(i).setCE(_ce);
		}else if(medicationOVS.get(i).getNew().getEncoded().equals("") 
				&& null == medicationOVS.get(i).getCE()){
			if (i==12) {
				_ce.setDisplayName("R");
				_ce.setCode("C0034444");
				_ce.setCodeSystem("2.16.840.1.113883.6.56");
				_ce.setCodeSystemVersion("2007AA");
				medicationOVS.get(i).setCE(_ce);
			}else if(i==5){
				_ce.setDisplayName("No");
				_ce.setCode("C0024554");
				_ce.setCodeSystem("2.16.840.1.113883.6.56");
				_ce.setCodeSystemVersion("2007AA");
				medicationOVS.get(i).setCE(_ce);
			}else if(i==6){
				_ce.setDisplayName("Yes");
				_ce.setCode("C0024444");
				_ce.setCodeSystem("2.16.840.1.113883.6.56");
				_ce.setCodeSystemVersion("2007AA");
				medicationOVS.get(i).setCE(_ce);
			}
		}
	}

	/**
	 * This function is used to format the array into CSV format
	 * 
	 * @param values
	 * @return
	 */
	private String getNames(List<MenuData> list) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			buff.append(list.get(i).getField("Name"));
			if (i < list.size() - 1) {
				buff.append(',');
			}
		}
		return buff.toString();
	}

	/**
	 * This function is used to retrieve the list by specifying a path
	 * 
	 * @param path
	 * @return
	 */
	private List<MenuData> getList(String path) {
		MenuStructure ms = getMenuBean().findMenuStructure(getAccountUser().getAccount().getId(), path);
		MenuQueryControl ctrl = new MenuQueryControl();
		ctrl.setMenuStructure(ms);
		ctrl.setNow(new Date());
		ctrl.setAccountUser(getAccountUser());
		Map<String, Long> nodeValues = new HashMap<String, Long>(10);
		nodeValues = getContextList().get(0).getNodeValues();
		ctrl.setOriginalTargetPath(new MenuPath(ms.instancePathFromContext(nodeValues, true)));
		ctrl.setRequestedPath(ctrl.getOriginalTargetPath());
		ctrl.setActualMenuStructure(ms);
		return getMenuBean().findMenuData(ctrl);
	}

	

	/**
	 * This function is used to convert string to ST.
	 * 
	 * @author Valsaraj added on 07/16/09
	 * @param str
	 * @return st
	 */
	public ST getST(String str) {
		ST st = new ST();
		st.setValue(str);
		return st;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the allergy
	 */
	public String getAllergy() {
		return allergy;
	}

	/**
	 * @param allergy
	 *            the allergy to set
	 */
	public void setAllergy(String allergy) {
		this.allergy = allergy;
	}

	/**
	 * @return the medicationPath
	 */
	public String getMedicationPath() {
		return medicationPath;
	}

	/**
	 * @param medicationPath
	 *            the medicationPath to set
	 */
	public void setMedicationPath(String medicationPath) {
		this.medicationPath = medicationPath;
	}

	/**
	 * @return the allergyPath
	 */
	public String getAllergyPath() {
		return allergyPath;
	}

	/**
	 * @param allergyPath
	 *            the allergyPath to set
	 */
	public void setAllergyPath(String allergyPath) {
		this.allergyPath = allergyPath;
	}

	/**
	 * @return the priorMedication
	 */
	public String getPriorMedication() {
		return priorMedication;
	}

	/**
	 * @param priorMedication
	 *            the priorMedication to set
	 */
	public void setPriorMedication(String priorMedication) {
		this.priorMedication = priorMedication;
	}

	//public void getDaysSupply() {
	//	daysSupply = (dispenseAmount / frequency);
	//}

	/**
	 * @return the frequency
	 */
	public long getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency
	 *            the frequency to set
	 */
	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the dispenseAmount
	 */
	public long getDispenseAmount() {
		return dispenseAmount;
	}

	/**
	 * @param dispenseAmount
	 *            the dispenseAmount to set
	 */
	public void setDispenseAmount(long dispenseAmount) {
		this.dispenseAmount = dispenseAmount;
	}

	/**
	 * @param daysSupply
	 *            the daysSupply to set
	 */
	public void setDaysSupply(long daysSupply) {
		this.daysSupply = daysSupply;
	}

	public static void obtainValueOfMenuData(MenuData md) {
		TolvenLogger.info(">>>>>>>>>>>>>>>>  Medication Name <<<<<<<<<<<<  :"
				+ md.getString01(), Prescription.class);
	}

	/**
	 * @return the prescriberOrderNumber
	 */
	public long getPrescriberOrderNumber() {
		return prescriberOrderNumber;
	}

	/**
	 * @param prescriberOrderNumber the prescriberOrderNumber to set
	 */
	public void setPrescriberOrderNumber(long prescriberOrderNumber) {
		this.prescriberOrderNumber = prescriberOrderNumber;
	}
	/**
	 * Method to get the prescriber Location
	 */
	private List<MenuData> getPrescriberLocations(Long id){
		ArrayList<PrescriberLocationVO> prescLocationDetails = new ArrayList<PrescriberLocationVO>();
		if(null != id){
			MenuData md = getMenuBean().findMenuDataItem(id);
			List<MenuData> items = getMenuBean().findListContents(TolvenRequest.getInstance().getAccount(), md.getMenuStructure(), md);
			return items;
		}
		return new ArrayList<MenuData>();
	}
	
	private class PrescriberLocationVO implements Serializable{
		private String locationName;
		private String addressLine1;
		private String city;
		private String status;
		private String phoneNumber;
		private String state;
		private String zip;
		private String spiLocation;
		/**
		 * @return the locationName
		 */
		public String getLocationName() {
			return locationName;
		}
		/**
		 * @param locationName the locationName to set
		 */
		public void setLocationName(String locationName) {
			this.locationName = locationName;
		}
		/**
		 * @return the addressLine1
		 */
		public String getAddressLine1() {
			return addressLine1;
		}
		/**
		 * @param addressLine1 the addressLine1 to set
		 */
		public void setAddressLine1(String addressLine1) {
			this.addressLine1 = addressLine1;
		}
		/**
		 * @return the city
		 */
		public String getCity() {
			return city;
		}
		/**
		 * @param city the city to set
		 */
		public void setCity(String city) {
			this.city = city;
		}
		/**
		 * @return the status
		 */
		public String getStatus() {
			return status;
		}
		/**
		 * @param status the status to set
		 */
		public void setStatus(String status) {
			this.status = status;
		}
		/**
		 * @return the phoneNumber
		 */
		public String getPhoneNumber() {
			return phoneNumber;
		}
		/**
		 * @param phoneNumber the phoneNumber to set
		 */
		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}
		/**
		 * @return the state
		 */
		public String getState() {
			return state;
		}
		/**
		 * @param state the state to set
		 */
		public void setState(String state) {
			this.state = state;
		}
		/**
		 * @return the zip
		 */
		public String getZip() {
			return zip;
		}
		/**
		 * @param zip the zip to set
		 */
		public void setZip(String zip) {
			this.zip = zip;
		}
		/**
		 * @return the spiLocation
		 */
		public String getSpiLocation() {
			return spiLocation;
		}
		/**
		 * @param spiLocation the spiLocation to set
		 */
		public void setSpiLocation(String spiLocation) {
			this.spiLocation = spiLocation;
		}
	}
}