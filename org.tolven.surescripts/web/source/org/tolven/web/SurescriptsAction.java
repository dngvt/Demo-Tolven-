package org.tolven.web; 

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.tolven.app.MenuLocal;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.DrugQualifier;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.us.states.DemographicsLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;
import org.tolven.surescripts.MessageType;
import org.tolven.surescripts.PharmacyLocal;
import org.tolven.surescripts.SurescriptsLocal;


public class SurescriptsAction extends MenuAction {
	private MessageType newRx;
	private MessageType rxFill;
	private MessageType rxChangeRequest;
	private MessageType reFillRequest;
	private String prescriberOrderNum;
	private @EJB SurescriptsLocal sureBean;
	private @EJB DemographicsLocal demographicsBean;
	private @EJB MenuLocal menuBean;
	
	private @EJB PharmacyLocal pharmacyBean;
	private ArrayList<SelectItem> drugQualifiers;
	private MenuData md;
	private String fdbResponse;
	private String drugDrugResponse;
	private String drugFoodResponse;
	private String drugAllergyResponse;
	private String duplicateTherapyResponse;
	private String patientInstructions;
	private String prescriberInstructions;
	private String overRideComments;
	private ArrayList<MenuData> administrationDetails;
	private MenuData patient;
	private String addressLine1;
	private String addressLine2;
	private String patientState;
	private String patientZip;
	private String controlledDrug;
	private String prescriberState;
	private String refillDirections;
	private String refillRefills;
	private String rxReferenceNumber;
	private String refillMedicine;
	private String daysSupply;
	private Date date;
	private String patientName;
	private String prescriberDea;
	private String prescriberName;
	private String prescriberTel;
	private String prescriberFax;
	private String prescriberEmail;
	private AccountUser accountUser;
	private String patientDob;
	private List<SelectItem> validPharmacy;
	
	
	public AccountUser getAccountUser() {
    	if (accountUser==null) {
    		accountUser = getActivationBean().findAccountUser( getSessionAccountUserId() );
    	}
		return accountUser;
	}
	
	public String getPrescriberOrderNum() {
		return prescriberOrderNum;
	}
	public void setPrescriberOrderNum(String prescriberOrderNum) {
		this.prescriberOrderNum = prescriberOrderNum;
	}
	public MessageType getNewRx() {
		return newRx;
	}
	public void setNewRx(MessageType newRx) {
		this.newRx = newRx;
	}
	public MessageType getRxFill() {
		return rxFill;
	}
	public void setRxFill(MessageType rxFill) {
		this.rxFill = rxFill;
	}
	public MessageType getRxChangeRequest() {
		return rxChangeRequest;
	}
	public void setRxChangeRequest(MessageType rxChangeRequest) {
		this.rxChangeRequest = rxChangeRequest;
	}
	public MessageType getReFillRequest() {
		return reFillRequest;
	}
	public void setReFillRequest(MessageType reFillRequest) {
		this.reFillRequest = reFillRequest;
	}
	
	
	public SurescriptsAction() {
		super();
		
		if (null != FacesContext.getCurrentInstance() && null != FacesContext.getCurrentInstance().getExternalContext()
			&& null != FacesContext.getCurrentInstance().getExternalContext().getRequest()){
			HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			this.prescriberOrderNum = req.getParameter("msgid");
			if (null != prescriberOrderNum) {
				ArrayList<MessageType> patientDetails = getSureBean()
						.retrievePatientPrescriptionReport(this.prescriberOrderNum);
				if (null != patientDetails && patientDetails.size() > 0) {
					ListIterator<MessageType> messageIter = patientDetails.listIterator();
					while (messageIter.hasNext()) {
						MessageType type = (MessageType) messageIter.next();
						if(null != type.getBody().getNewRx()){
							this.newRx = type;
						}
						if(null != type.getBody().getRxFill()){
							this.rxFill = type;
						}
						if(null != type.getBody().getRefillRequest()){
							this.reFillRequest = type;
						}
						if(null != type.getBody().getRxChangeRequest()){
							this.rxChangeRequest = type;
						}
					}
				}
				else{
					this.newRx = new MessageType();
					this.rxFill = new MessageType();
					this.rxChangeRequest = new MessageType();
					this.reFillRequest = new MessageType();
				}
			}else{
				this.prescriberOrderNum = req.getParameter("pod");
				if(null!= req.getParameter("root")){
					String patientId = req.getParameter("root").split("wip")[0].substring(11);
					this.patient = getMenuLocal().findMenuDataItem(Long.parseLong(patientId));
					getAddressLine1();
					getAddressLine2();
					getPatientState();
					getPatientZip();
				}	
				if(null!= req.getParameter("patientId")){
					String patientId = req.getParameter("patientId").split("-")[1];
					this.patient = getMenuLocal().findMenuDataItem(Long.parseLong(patientId));
					getAddressLine1();
					getAddressLine2();
					getPatientState();
					getPatientZip();
				}
				if(null != req.getParameter("directions")){
					this.refillDirections = req.getParameter("directions");
				}
				if(null != req.getParameter("refills")){
					this.refillRefills = req.getParameter("refills");
				}
				if(null != req.getParameter("rxReferenceNumber")){
					this.rxReferenceNumber = req.getParameter("rxReferenceNumber");
				}
				if(null != req.getParameter("refilldrug")){
					this.refillMedicine = req.getParameter("refilldrug");
				}
				this.newRx = new MessageType();
				this.rxFill = new MessageType();
				this.rxChangeRequest = new MessageType();
				this.reFillRequest = new MessageType();
			}
		}
		getMd();
		getControlledDrug();
	}
	/**
	 * Action To guide to the new JSF Page
	 * @return
	 */
	public String  printPatientPrescriptionReport() {
		this.newRx = getSureBean().retrievePatientPrescriptionReport(this.prescriberOrderNum).get(0);
		this.rxFill = getSureBean().retrievePatientPrescriptionReport(this.prescriberOrderNum).get(1);
		this.rxChangeRequest = getSureBean().retrievePatientPrescriptionReport(this.prescriberOrderNum).get(2);
		this.reFillRequest = getSureBean().retrievePatientPrescriptionReport(this.prescriberOrderNum).get(3);
		return "success";
	}
	public SurescriptsLocal getSureBean() {
		return sureBean;
	}
	/**
	 * @param sureBean the sureBean to set
	 */
	public void setSureBean(SurescriptsLocal sureBean) {
		this.sureBean = sureBean;
	}
	/**
	 * @return the drugQualifiers
	 */
	public ArrayList<SelectItem> getDrugQualifiers() {
		if(null == drugQualifiers){
			ArrayList<DrugQualifier> drugs = pharmacyBean.retrieveAllDrugQualifiers();
			this.drugQualifiers = new ArrayList<SelectItem>();
			for(DrugQualifier drug : drugs){
				SelectItem item = new SelectItem(drug.getQualcode(), drug.getQualDesc());
				this.drugQualifiers.add(item);
			}
		}
		return drugQualifiers;
	}
	public DemographicsLocal getDemographicsBean() {
		return demographicsBean;
	}

	/**
	 * @param drugQualifiers the drugQualifiers to set
	 */
	public void setDrugQualifiers(ArrayList<SelectItem> drugQualifiers) {
		this.drugQualifiers = drugQualifiers;
	}
	/**
	 * @return the md
	 */
	public MenuData getMd() {
		if(null == this.md){
			if(null != this.prescriberOrderNum)
				this.md = getMenuLocal().findMenuDataItem(Long.parseLong(this.prescriberOrderNum));
		}
		return md;
	}
	/**
	 * @param md the md to set
	 */
	public void setMd(MenuData md) {
		this.md = md;
	}
	/**
	 * @return the fdbResponse
	 */
	public String getFdbResponse() {
		if(null == this.fdbResponse){
			if(null != this.md){
				this.fdbResponse = (String)md.getExtendedField("fdbReponse");
			}
		}
		return fdbResponse;
	}
	/**
	 * @param fdbResponse the fdbResponse to set
	 */
	public void setFdbResponse(String fdbResponse) {
		this.fdbResponse = fdbResponse;
	}
	/**
	 * @return the drugDrugResponse
	 */
	public String getDrugDrugResponse() {
		if(null == this.drugDrugResponse){
			if(null != this.md){
				this.drugDrugResponse = (String)md.getExtendedField("ddResponse");
			}
		}
		return drugDrugResponse;
	}
	/**
	 * @param drugDrugResponse the drugDrugResponse to set
	 */
	public void setDrugDrugResponse(String drugDrugResponse) {
		this.drugDrugResponse = drugDrugResponse;
	}
	/**
	 * @return the drugFoodResponse
	 */
	public String getDrugFoodResponse() {
		if(null == this.drugFoodResponse){
			if(null != this.md){
				this.drugFoodResponse = (String)md.getExtendedField("drugFoodResp");
			}
		}
		return drugFoodResponse;
	}
	/**
	 * @param drugFoodResponse the drugFoodResponse to set
	 */
	public void setDrugFoodResponse(String drugFoodResponse) {
		this.drugFoodResponse = drugFoodResponse;
	}
	/**
	 * @return the drugAllergyResponse
	 */
	public String getDrugAllergyResponse() {
		if(null == this.drugAllergyResponse){
			if(null != this.md){
				this.drugAllergyResponse = (String)md.getExtendedField("drugAllergy");
			}
		}
		return drugAllergyResponse;
	}
	/**
	 * @param drugAllergyResponse the drugAllergyResponse to set
	 */
	public void setDrugAllergyResponse(String drugAllergyResponse) {
		this.drugAllergyResponse = drugAllergyResponse;
	}
	/**
	 * @return the duplicateTherapyResponse
	 */
	public String getDuplicateTherapyResponse() {
		if(null == this.duplicateTherapyResponse){
			if(null != this.md){
				this.duplicateTherapyResponse = (String)md.getExtendedField("duplicateTherapy");
			}
		}
		return duplicateTherapyResponse;
	}
	/**
	 * @param duplicateTherapyResponse the duplicateTherapyResponse to set
	 */
	public void setDuplicateTherapyResponse(String duplicateTherapyResponse) {
		this.duplicateTherapyResponse = duplicateTherapyResponse;
	}
	/**
	 * @return the patientInstructions
	 */
	public String getPatientInstructions() {
		if(null == this.patientInstructions){
			if(null != this.md){
				this.patientInstructions = (String)md.getExtendedField("patientInstructions");
			}
		}
		return patientInstructions;
	}
	/**
	 * @param patientInstructions the patientInstructions to set
	 */
	public void setPatientInstructions(String patientInstructions) {
		this.patientInstructions = patientInstructions;
	}
	/**
	 * @return the prescriberInstructions
	 */
	public String getPrescriberInstructions() {
		if(null == this.prescriberInstructions){
			if(null != this.md){
				this.prescriberInstructions = (String)md.getExtendedField("prescriberInstructions");
			}
		}
		return prescriberInstructions;
	}
	/**
	 * @param prescriberInstructions the prescriberInstructions to set
	 */
	public void setPrescriberInstructions(String prescriberInstructions) {
		this.prescriberInstructions = prescriberInstructions;
	}
	/**
	 * @return the overRideComments
	 */
	public String getOverRideComments() {
		if(null == this.overRideComments){
			if(null != this.md){
				this.overRideComments = (String)md.getExtendedField("overridecomments");
			}
		}
		return overRideComments;
	}
	/**
	 * @param overRideComments the overRideComments to set
	 */
	public void setOverRideComments(String overRideComments) {
		this.overRideComments = overRideComments;
	}
	/**
	 * @return the administrationDetails
	 */
	public ArrayList<MenuData> getAdministrationDetails() {
		if(this.administrationDetails == null){
		
			MenuData md = menuBean.findMenuDataItem(Long.parseLong(this.prescriberOrderNum));
			List<MenuData> mdList = menuBean.findListContents(TolvenRequest.getInstance().getAccount(), md.getMenuStructure(), md);
			this.administrationDetails = (ArrayList<MenuData>) mdList;
		}
		return administrationDetails;
	}
	/**
	 * @param administrationDetails the administrationDetails to set
	 */
	public void setAdministrationDetails(ArrayList<MenuData> administrationDetails) {
		this.administrationDetails = administrationDetails;
	}
	/**
	 * @return the patient
	 */
	public MenuData getPatient() {
		if(null == patient){
			if(this.md != null){
				this.patient = getMenuLocal().findMenuDataItem(Long.parseLong(this.md.getParentPath01().split("-")[1]));
			}	
		}
		return patient;
	}
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(MenuData patient) {
		this.patient = patient;
	}
	/**
	 * @return the addressLine1
	 */
	public String getAddressLine1() {
		if(null == this.addressLine1){
			if(null != this.patient){
				this.addressLine1 = (String)patient.getExtendedField("homeAddr1");
			}else{
				this.patient = getMenuLocal().findMenuDataItem(Long.parseLong(this.md.getParentPath01().split("-")[1]));
				this.addressLine1 = (String)patient.getExtendedField("homeAddr1");
			}
		}
		return addressLine1;
	}
	/**
	 * @param addressLine1 the addressLine1 to set
	 */
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}
	/**
	 * @return the addressLine2
	 */
	public String getAddressLine2() {
		if(null == this.addressLine2){
			if(null != this.patient){
				this.addressLine2 = (String)patient.getExtendedField("homeAddr2");
			}else{
				this.patient = getMenuLocal().findMenuDataItem(Long.parseLong(this.md.getParentPath01().split("-")[1]));
				this.addressLine1 = (String)patient.getExtendedField("homeAddr2");
			}
		}
		return addressLine2;
	}
	/**
	 * @param addressLine2 the addressLine2 to set
	 */
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}
	/**
	 * @return the patientState
	 */
	public String getPatientState() {
		if(null == this.patientState){
			if(null != this.patient){
				this.patientState = (String)patient.getExtendedField("homeState");
			}else{
				this.patient = getMenuLocal().findMenuDataItem(Long.parseLong(this.md.getParentPath01().split("-")[1]));
				this.addressLine1 = (String)patient.getExtendedField("homeState");
			}
		}
		return patientState;
	}
	/**
	 * @param patientState the patientState to set
	 */
	public void setPatientState(String patientState) {
		this.patientState = patientState;
	}
	/**
	 * @return the patientZip
	 */
	public String getPatientZip() {
		if(null == this.patientZip){
			if(null != this.patient){
				this.patientZip = (String)patient.getExtendedField("homeZip");
			}else{
				this.patient = getMenuLocal().findMenuDataItem(Long.parseLong(this.md.getParentPath01().split("-")[1]));
				this.addressLine1 = (String)patient.getExtendedField("homeZip");
			}
		}
		return patientZip;
	}
	/**
	 * @param patientZip the patientZip to set
	 */
	public void setPatientZip(String patientZip) {
		this.patientZip = patientZip;
	}
	/**
	 * @return the controlledDrug
	 */
	public String getControlledDrug() {
		if(null == this.controlledDrug){
			if(null != this.md)
				this.controlledDrug = (String)md.getExtendedField("isControlled");
		}
		return controlledDrug;
	}
	/**
	 * @param controlledDrug the controlledDrug to set
	 */
	public void setControlledDrug(String controlledDrug) {
		this.controlledDrug = controlledDrug;
	}
	/**
	 * @return the prescriberState
	 */
	public String getPrescriberState() {
		if(null == this.prescriberState){
			if(null != this.md){
				if(null != this.md.getParent04()){
					this.prescriberState = (String)md.getParent04().getExtendedField("state");
				}
			}	
		}
		return prescriberState;
	}
	/**
	 * @param prescriberState the prescriberState to set
	 */
	public void setPrescriberState(String prescriberState) {
		this.prescriberState = prescriberState;
	}
	/**
	 * @return the refillDirections
	 */
	public String getRefillDirections() {
		return refillDirections;
	}
	/**
	 * @param refillDirections the refillDirections to set
	 */
	public void setRefillDirections(String refillDirections) {
		this.refillDirections = refillDirections;
	}
	/**
	 * @return the refillRefills
	 */
	public String getRefillRefills() {
		return refillRefills;
	}
	/**
	 * @param refillRefills the refillRefills to set
	 */
	public void setRefillRefills(String refillRefills) {
		this.refillRefills = refillRefills;
	}
	/**
	 * @return the rxReferenceNumber
	 */
	public String getRxReferenceNumber() {
		return rxReferenceNumber;
	}
	/**
	 * @param rxReferenceNumber the rxReferenceNumber to set
	 */
	public void setRxReferenceNumber(String rxReferenceNumber) {
		this.rxReferenceNumber = rxReferenceNumber;
	}
	/**
	 * @return the refillMedicine
	 */
	public String getRefillMedicine() {
		return refillMedicine;
	}
	/**
	 * @param refillMedicine the refillMedicine to set
	 */
	public void setRefillMedicine(String refillMedicine) {
		this.refillMedicine = refillMedicine;
	}
	/**
	 * @return the daysSupply
	 */
	public String getDaysSupply() {
		if(null == this.daysSupply){
			if(null != this.md){
				this.daysSupply = (String)md.getExtendedField("daySupply");
			}
		}
		return daysSupply;
	}
	/**
	 * @param daysSupply
	 */
	public void setDaysSupply(String daysSupply) {
		this.daysSupply = daysSupply;
	}

	
	public String getPatientName() {
		if(null == this.patientName){
			if(null != this.md){
				this.patientName = (String)md.getParent01().getString01();
			}
		}
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public String getPatientDob() {
		if(null == this.patientDob){
			if(null != this.md){
				this.patientDob = (String)md.getParent01().getDate01().toString();
			}
		}
		return patientDob;
	}
	public void setPatientDob(String patientDob) {
		this.patientDob = patientDob;
	}
	public String getDate() {
		date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
		String currentDate = df.format(date);
		return currentDate;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getPrescriberDea() {
		if(null == this.prescriberDea){
			if(null != this.md){
				this.prescriberDea = (String)md.getParent04().getPqStringVal03();
			}
		}
		return prescriberDea;
	}
	public void setPrescriberDea(String prescriberDea) {
		this.prescriberDea = prescriberDea;
	}
	
	public String getPrescriberName() {
		if(null == this.prescriberName){
			if(null != this.md){
				this.prescriberName = (String)md.getString05();
			}
		}
		return prescriberName;
	}
	public void setPrescriberName(String prescriberName) {
		this.prescriberName = prescriberName;
	}
	public String getPrescriberTel() {
		if(null == this.prescriberTel){
			if(null != this.md){
				this.prescriberTel = (String)md.getParent04().getPqUnits01();
			}
		}
		return prescriberTel;
	}
	public void setPrescriberTel(String prescriberTel) {
		this.prescriberTel = prescriberTel;
	}
	public String getPrescriberFax() {
		if(null == this.prescriberFax){
			if(null != this.md){
				this.prescriberFax = (String)md.getParent04().getPqStringVal01();
			}
		}
		return prescriberFax;
	}
	public void setPrescriberFax(String prescriberFax) {
		this.prescriberFax = prescriberFax;
	}
	public String getPrescriberEmail() {
		return prescriberEmail;
	}
	public void setPrescriberEmail(String prescriberEmail) {
		this.prescriberEmail = prescriberEmail;
	}
	
	/*
	 * This method is used to count total Pharmacies.
	 * 
	 * @author Suja Sundaresan
	 * added on 05/Dec/2010
	 * @return long
	 */
	public long getTotalPharmacies(){
		return pharmacyBean.countPharmacies(null, null, null);
	}
	
	/*
	 * This method is used to count total Preferred Pharmacies.
	 * 
	 * @author Suja Sundaresan
	 * added on 05/Dec/2010
	 * @return long
	 */
	public long getTotalPreferredPharmacies(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("account", getAccountUser().getAccount());
		map.put("patientId", getElement().split("-")[1].split(":")[0]);
		return pharmacyBean.countPreferredPharmacies(map);
	}
	/**
	 * @param validPharmacy the validPharmacy to set
	 */
	public void setValidPharmacy(ArrayList<SelectItem> validPharmacy) {
		this.validPharmacy = validPharmacy;
	}

	
	
	/**
	 * Gets the preferred pharmacies of this patient from the list.
	 * @param path
	 * @return
	 */
	public List<String> getPreferredPharmacy(String path) {
		final String placeholderName = "pharmacy";
		List<String> preferredPharmacyList = new ArrayList<String>();
		MenuQueryControl ctrl = new MenuQueryControl();
		MenuStructure ms = this.getMenuLocal().findMenuStructure(this.getAccountId(), path );
		AccountUser activeAccountUser = TolvenRequest.getInstance().getAccountUser();
		Map<String, Long> nodeValues = new HashMap<String, Long>(10);
		nodeValues.putAll(new MenuPath( this.getElement() ).getNodeValues());
		ctrl.setMenuStructure(ms);
		ctrl.setNow(getNow());
		ctrl.setAccountUser(activeAccountUser);
		ctrl.setOriginalTargetPath( new MenuPath(ms.instancePathFromContext ( nodeValues, true )));
		ctrl.setRequestedPath( ctrl.getOriginalTargetPath() );
		List<Map<String, Object>> pharmacyList =  this.getMenuLocal().findMenuDataByColumns(ctrl).getResults();	
		for (Map<String, Object> pharmacy: pharmacyList) {
			if (pharmacy.get(placeholderName) != null) {
				MenuData pharmacyMd = (MenuData) pharmacy.get(placeholderName);
				preferredPharmacyList.add(pharmacyMd.getString01().trim());
			}
		}
		return preferredPharmacyList;
	}	

	
	
}
