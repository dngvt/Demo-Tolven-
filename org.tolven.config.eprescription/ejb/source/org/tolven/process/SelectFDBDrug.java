package org.tolven.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.MDQueryResults;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.el.ExpressionEvaluator;
import org.tolven.fdb.entity.FdbDispensable;
import org.tolven.logging.TolvenLogger;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.Compute.Property;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.TrimEx;

/*
 * Compute to select a Drug from FDB for eprescription
 * */
public class SelectFDBDrug extends EprescriptionComputeBase {
	private String drugName;
	private String drugCode;
	private boolean enabled;
	private String medicationPath;
	private String allergyPath;
	private String drugFormularyPath;
	Logger log = Logger.getLogger(this.getClass());
	public String getMedicationPath() {
		return medicationPath;
	}

	public void setMedicationPath(String medicationPath) {
		this.medicationPath = medicationPath;
	}

	
	public String getDrugFormularyPath() {
		return drugFormularyPath;
	}

	public void setDrugFormularyPath(String drugFormularyPath) {
		this.drugFormularyPath = drugFormularyPath;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	

	public String getDrugName() {
		return drugName;
	}

	public void setDrugName(String drugName) {
		this.drugName = drugName;
	}

	public String getDrugCode() {
		return drugCode;
	}

	public void setDrugCode(String drugCode) {
		this.drugCode = drugCode;
	}

	public SelectFDBDrug() {

	}

	@Override
	public void compute() throws Exception {
		if(isEnabled()){
			log.debug("Start:SelectFDBDrug.compute "+new Date());
			String isDrugInFormularyList = "No";
			ExpressionEvaluator evaluator = new ExpressionEvaluator();
			evaluator.addVariable("trim", getTrim());
			String drugCode = getDrugCode();
			StringBuffer priorallergies = new StringBuffer();
			StringBuffer priorMedications = new StringBuffer();
			String[] medicationsPath = getMedicationPath().split(",");
			for(String path:medicationsPath){
				priorMedications.append(getNames(getList(path),path)+",");
			}
			String[] allergiesPath = getAllergyPath().split(",");
			for(String path:allergiesPath){
				priorallergies.append(getNames(getList(path),path)+",");
			}
			
			String dob = (String)evaluator.evaluate("#{trim.act.participation['subject'].role.player.livingSubject.birthTime.TS.value}");
	
			FdbDispensable dispensable = getFdbInterface().findFdbDispensable(new Integer(drugCode));
			Map<String,String> resultList = getFdbInterface().searchDrugExhaustive(Long.parseLong(drugCode) ,  priorMedications.toString(), priorallergies.toString() , dob);
			//check the drug availability in formulary
			MenuPath path = new MenuPath( getDrugFormularyPath() );
			MenuStructure ms = getMenuBean().findMenuStructure( getAccountUser().getAccount().getId(), path.getPath() );
			MenuQueryControl ctrl = new MenuQueryControl();
			ctrl.setAccountUser(getAccountUser());
			ctrl.setMenuStructure( ms );
			ctrl.setNow( new Date() );
			ctrl.setOriginalTargetPath( path );
			ctrl.setRequestedPath( path );
			List<Map<String, Object>> items =getMenuBean().findMenuDataByColumns(ctrl).getResults();
			if (evaluator.evaluate("#{trim.act.relationship['isInDrugFromulary']}") != null ) {
				for (Map<String, Object> map : items) {
					// Using medID(drugCode) check whether the drug is in formulary list.
					if (map.get("Code") != null && map.get("Code").equals(drugCode)) {
						isDrugInFormularyList = "Yes";
						break;
					} 
				}
				evaluator.setValue("#{trim.act.relationship['isInDrugFromulary'].act.observation.value.ST.value}", isDrugInFormularyList);
			}
			
			// Setting the drug code in the trim.
			//if (evaluator.evaluate("#{trim.act.relationship['medID']}") != null ) {
			//	evaluator.setValue("#{trim.act.relationship['medID'].act.observation.value.ST.value}", drugCode);
			//}
			//change this to expression evaluator
			
			if(null != resultList & resultList.size() > 0){
				String fdbResponce = StringUtils.isBlank(resultList.get("fdbResponse"))?"No Information found in FDB.":resultList.get("fdbResponse");
				//evaluator.setValue("#{trim.act.relationship['toSureScripts'].act.observation.values[20].ST.value}", fdbResponce);
				evaluator.setValue("#{trim.act.relationship['fdbResponse'].act.observation.value.ST.value}", fdbResponce);				
			}
			
			// Set the drug name
			evaluator.setValue("#{trim.act.participation['consumableProduct'].role.player.code.CE.displayName}", dispensable.getDescdisplay());
			evaluator.setValue("#{trim.act.participation['consumableProduct'].role.player.code.CE.code}", drugCode);
			evaluator.setValue("#{trim.act.participation['consumableProduct'].role.player.code.CE.codeSystemName}", "FDB");
			evaluator.setValue("#{trim.act.participation['consumableProduct'].role.player.code.CE.codeSystemVersion}", fdbInterface.getFdbVersion().getDbversion());
			
			Map<String,String> ndcDetails = fdbInterface.retrieveNDCInformation(Long.parseLong(drugCode));
			evaluator.setValue("#{trim.act.relationship['ndcDetails'].act.observation.value.CE.codeSystem}", ndcDetails.get("ndcCodeQual"));//ND
			evaluator.setValue("#{trim.act.relationship['ndcDetails'].act.observation.value.CE.codeSystemName}", ndcDetails.get("codeSystem"));//FI or FM
			evaluator.setValue("#{trim.act.relationship['ndcDetails'].act.observation.value.CE.label}", ndcDetails.get("ndcStrengthUnit"));
			evaluator.setValue("#{trim.act.relationship['ndcDetails'].act.observation.value.CE.code}", ndcDetails.get("ndcCode"));
			//trim.getAct().getRelationships().get(0).getAct().getObservation().getValues().get(25).getST().setValue(resultList.get("drugDrugResponse"));  
				
			// Getting the response for Drug Food Interaction
			
			// Getting the response for Drug Drug Interaction
			evaluator.setValue("#{trim.act.relationship['duplicateTherapyResponse'].act.observation.value.ST.value}", resultList.get("duplicateTherapyResponse"));	
			// Getting the response for Duplicate Therapy Interaction
			
			
			//evaluator.setValue("#{trim.act.relationship['medicationDetails'].act.observation.values[1].ST.value}",resultList.get("strength"));
			evaluator.setValue("#{trim.act.participation['consumableProduct'].role.player.quantity.PQ.originalText}",resultList.get("strength"));
			evaluator.setValue("#{trim.act.relationship['AHFSClassification'].act.observation.value.ST.value}",resultList.get("AHFSClassification"));
			evaluator.setValue("#{trim.act.relationship['prescriberInstructions'].act.observation.value.ST.value}",resultList.get("prescriberInstructions"));
			evaluator.setValue("#{trim.act.relationship['patientInstructions'].act.observation.value.ST.value}",resultList.get("patientInstructions"));
			evaluator.setValue("#{trim.act.relationship['CTActionGroupClassification'].act.observation.value.ST.value}",resultList.get("CTActionGroupClassification"));
			evaluator.setValue("#{trim.act.relationship['CTETCClassification'].act.observation.value.ST.value}",resultList.get("CTETCClassification"));
			evaluator.setValue("#{trim.act.relationship['CTFDBClassification'].act.observation.value.ST.value}",resultList.get("CTFDBClassification"));
			evaluator.setValue("#{trim.act.relationship['isControlled'].enableRelationship}",Boolean.parseBoolean(resultList.get("isControlled")));
			if(!StringUtils.isBlank(resultList.get("drugDrugResponse"))){
				evaluator.setValue("#{trim.act.relationship['drugDrugInteraction'].act.observation.value.ST.value}", resultList.get("drugDrugResponse"));
			}
			if(!StringUtils.isBlank(resultList.get("drugFoodResponse"))){
				evaluator.setValue("#{trim.act.relationship['drugFoodResponse'].act.observation.value.ST.value}", resultList.get("drugFoodResponse"));
			}
			if(!StringUtils.isBlank(resultList.get("drugAllergyResponse"))){
				evaluator.setValue("#{trim.act.relationship['drugAllergyResponse'].act.observation.value.ST.value}", resultList.get("drugAllergyResponse"));
			}
			String assignedStaffPath = null;
			StringBuffer interactionsText = new StringBuffer();
			if(!StringUtils.isBlank(getAccountUser().getOpenMeFirst())){
				//set the prescriber
				//evaluator.setValue("#{trim.act.relationship['prescriber'].act.id.IIS[0].extension}", getAccountUser().getOpenMeFirst());
				String[] providerSplit =  getAccountUser().getOpenMeFirst().split("-");
				if(providerSplit.length == 2)
					assignedStaffPath  = providerSplit[1];
				if(assignedStaffPath != null){
					MenuData prescriber = getMenuBean().findMenuDataItem(Long.parseLong(assignedStaffPath));
					String notificationPref = (String)prescriber.getExtendedField("notification");
					if(!StringUtils.isBlank(notificationPref)){
						for(String pref:notificationPref.split(",")){
							if(pref.trim().equalsIgnoreCase("Drug - Drug") && !StringUtils.isBlank(resultList.get("drugDrugResponse"))){
								interactionsText.append("Drug Drug Interactions:"+resultList.get("drugDrugResponse")+"<br/><br/>");
							}else if(pref.trim().equalsIgnoreCase("Drug - Food") && !StringUtils.isBlank(resultList.get("drugFoodResponse"))){
								interactionsText.append("Drug Food Interactions: "+resultList.get("drugFoodResponse")+"<br/><br/>");
							}else if(pref.trim().equalsIgnoreCase("Drug - Allergy")&& !StringUtils.isBlank(resultList.get("drugAllergyResponse"))){
								interactionsText.append("Drug Allergy Interactions: "+resultList.get("drugAllergyResponse")+"<br/>");
							}
						}						
					}
				}
			}
			if(!StringUtils.isBlank(interactionsText.toString())){
				//add html for override button
				//interactionsText.append("<input type='button' value='OverWrite' onclick='overwriteInteractionText(#{menu.elementLabel})'/>");
				evaluator.setValue("#{trim.act.relationship['interactions'].act.observation.value.ST.value}",StringUtils.escape(interactionsText.toString()));
			}
			
			// Disable the Compute since its job is done.				
			for (Property property : getComputeElement().getProperties()) {
				if (property.getName().equals("enabled")) {
					property.setValue(Boolean.FALSE);
					break;
				}
			}
			log.debug("END:SelectFDBDrug.compute "+new Date());
		}
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
	 * This function is used to format the array into CSV format
	 * 
	 * @param values
	 * @return
	 */
	private String getNames(MDQueryResults list,String path) {
		StringBuffer buff = new StringBuffer();
		if(list == null)
			return buff.toString();
		for (int i = 0; i < list.getResults().size(); i++) {
			buff.append(list.getResults().get(i).get(path.substring(path.indexOf("~")+1)));
			if (i < list.getResults().size() - 1) {
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
	private MDQueryResults getList(String path) {
		if(StringUtils.isBlank(path)){
			return null;
		}
		MenuStructure ms = getMenuBean().findMenuStructure(getAccountUser().getAccount().getId(), path.substring(0, path.indexOf("~")));
		MenuQueryControl ctrl = new MenuQueryControl();
		ctrl.setMenuStructure(ms);
		ctrl.setNow(new Date());
		ctrl.setAccountUser(getAccountUser());
		Map<String, Long> nodeValues = new HashMap<String, Long>(10);
		nodeValues = getContextList().get(0).getNodeValues();
		ctrl.setOriginalTargetPath(new MenuPath(ms.instancePathFromContext(nodeValues, true)));
		ctrl.setRequestedPath(ctrl.getOriginalTargetPath());
		ctrl.setActualMenuStructure(ms);
		return getMenuBean().findMenuDataByColumns(ctrl);
	}
	
}
