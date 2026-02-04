package org.tolven.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.logging.TolvenLogger;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.Compute.Property;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.TrimEx;
import org.tolven.trim.ex.TrimFactory;

/**
 * This class is used to load active medications to trim on startup of medication reconciliation
 * 
 * @author Nevin
 * added on 12/29/2010
 */
public class InsertActiveMedications extends InsertAct {
	private static final String path = "echr:patient:medications:active";
	private static final TrimFactory trimFactory = new TrimFactory();
	private TrimEx trim; 
	/**
	 * This method fetches the list of Active medications and lists in Medication reconciliation wizard
	 * @author Nevin
	 * added on 12/29/2010
	 */	
	public void compute() throws Exception {
		super.checkProperties();
		if (isEnabled()) {
			trim = (TrimEx) this.getTrim();
			List<MenuData> activeMedicationsList = getActiveMedicationsList(path);
			for (MenuData item : activeMedicationsList) {
				String medicationName = item.getString01();
				Long mdId = item.getId();
				TrimEx medReconciliationTrim = (TrimEx) getTrimBean().findTrim("obs/evn/medicationReconciliation");
				ActRelationship activeMedRel = ((ActEx)medReconciliationTrim.getAct()).getRelationship().get("medication");
				activeMedRel.getAct().getTitle().setST(trimFactory.createNewST(medicationName));
				activeMedRel.getAct().getObservation().getValues().get(0).setST(trimFactory.createNewST(mdId.toString()));
				((ActEx)trim.getAct()).getRelationship().get("activeMedications").getAct().getRelationships().add(activeMedRel);
			}
		}
		disableCompute();
	}
	/**
	 * This function is used to retrieve the list by specifying a path
	 * @author Nevin
	 * added on 12/29/2010
	 */	
	private List<MenuData> getActiveMedicationsList(String path) {
		List<MenuData> items = new ArrayList<MenuData>();
		try {
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
			items = getMenuBean().findMenuData(ctrl);
		} catch (Exception e) {
			TolvenLogger.info(e.getMessage(), InsertAct.class);
			e.printStackTrace();
		}
		return items;
	}
	/**
	 * This function is used disable compute
	 * @author Nevin
	 * added on 12/29/2010
	 */
	private void disableCompute() {
		for (Property property : getComputeElement().getProperties()) {
			if ("enabled".equals(property.getName())) {
				property.setValue(Boolean.FALSE);
			}
		}
	}
}
