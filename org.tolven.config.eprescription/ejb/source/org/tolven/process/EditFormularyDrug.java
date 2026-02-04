package org.tolven.process;
import java.util.Map;
import org.tolven.el.ExpressionEvaluator;
import org.tolven.trim.Compute.Property;

public class EditFormularyDrug extends EprescriptionComputeBase {

	private boolean enabled;
	private String fdbDrugCode;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getFdbDrugCode() {
		return fdbDrugCode;
	}

	public void setFdbDrugCode(String fdbDrugCode) {
		this.fdbDrugCode = fdbDrugCode;
	}

	@Override
	public void compute() throws Exception {
		if(isEnabled()){
			Map<String, String> ndcInfo = getFdbInterface().retrieveNDCInformation(Long.parseLong(getFdbDrugCode()));
			// Get the drugFormularyID here.
			/*for (II formularyID : trim.getAct().getId().getIIS()) {
				drugFormularyID = formularyID.getExtension();
			}*/
			ExpressionEvaluator evaluator = new ExpressionEvaluator();
			evaluator.addVariable("trim", getTrim());
			evaluator.setValue("#{trim.act.relationship['drugDetails'].act.observation.value.CE.displayName}", ndcInfo.get("drugName"));
			evaluator.setValue("#{trim.act.relationship['drugDetails'].act.observation.value.CE.code}", ndcInfo.get("drugCode"));
			evaluator.setValue("#{trim.act.relationship['drugDetails'].act.observation.value.CE.codeSystem}", ndcInfo.get("codeSystem"));
			evaluator.setValue("#{trim.act.relationship['drugDetails'].act.observation.value.CE.codeSystemVersion}", ndcInfo.get("ndcCodeSystemVersion"));
			evaluator.setValue("#{trim.act.relationship['drugDetails'].act.observation.value.CE.translations[0].code}", ndcInfo.get("ndcCode"));
			if(ndcInfo.get("ndcStrength") != null){
				StringBuffer strength = new StringBuffer(ndcInfo.get("ndcStrength"));
				if(strength.length()>0){
					strength.append(String.format("(%s)", ndcInfo.get("ndcStrengthUnit")));
					evaluator.setValue("#{trim.act.relationship['strength'].act.observation.value.ST.value}", strength.toString());
				}else{
					evaluator.setValue("#{trim.act.relationship['strength'].act.observation.value.ST.value}", "");
				}
			}
			
			/*for (ActRelationship relations : ((ActEx) trim.getAct()).getRelationships()) {
				if (relations.getName().equals("drugDetails")) {
					for (ObservationValueSlot ovsDrug : relations.getAct().getObservation().getValues()) {
						if (ovsDrug.getLabel().getValue().equals("Drug Details")) {
							ovsDrug.getCE().setDisplayName(drugName);
							ovsDrug.getCE().setCode(ndcInfo.get("ndcCode"));
							ovsDrug.getCE().setCodeSystem(ndcInfo.get("codeSystem"));
							ovsDrug.getCE().setCodeSystemName(ndcInfo.get("ndcCodeQual"));
							ovsDrug.getCE().setCodeSystemVersion(ndcInfo.get("ndcCodeSystemVersion"));
							ovsDrug.getCE().getTranslations().get(0).setCode(ndcInfo.get("medId"));
						} else if (ovsDrug.getLabel().getValue().equals("MedID")) {
							ovsDrug.getST().setValue(drugCode);
						}
					}
				} else if (relations.getName().equals("strength")) {
					for (ObservationValueSlot ovsStrength : relations.getAct().getObservation().getValues()) {
						if (ovsStrength.getLabel().getValue().equals("Strength") && 
								ndcInfo.get("ndcStrength") != null && !ndcInfo.get("ndcStrength").isEmpty()) {
							strengthBuffer.append(ndcInfo.get("ndcStrength"));
							if (ndcInfo.get("ndcStrengthUnit") != null && !ndcInfo.get("ndcStrengthUnit").isEmpty()) {
								strengthBuffer.append(" " + ndcInfo.get("ndcStrengthUnit"));
							}
							ovsStrength.getST().setValue(strengthBuffer.toString());
						} else {
							ovsStrength.getST().setValue("");
						}
					}
				} else if (relations.getName().equals("elementLabel")) {
					for (ObservationValueSlot ovsStrength : relations.getAct().getObservation().getValues()) {
						if (ovsStrength.getLabel().getValue().equals("ElementLabel") && drugFormularyID != null) {
							ovsStrength.getST().setValue(drugFormularyID);
						}
					}
				}
			}*/			
		}
		// Disable the Compute since its job is done.
    	for (Property property : getComputeElement().getProperties()) {
			if (property.getName().equals("enabled")) {
				property.setValue(Boolean.FALSE);
				break;
			}
		}
	}
}
