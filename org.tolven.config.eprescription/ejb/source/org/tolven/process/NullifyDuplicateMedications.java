package org.tolven.process;

import java.util.List;

import javax.ejb.EJB;

import org.tolven.app.AppEvalAdaptor;
import org.tolven.app.CreatorLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.TrimEx;

public class NullifyDuplicateMedications extends InsertAct{
	@EJB private static MenuLocal menuBean;
	@EJB private static CreatorLocal creatorBean;
	/**
	 * This method nullifies and remove the active medications choosen in Medication Reconciliation 
	 * @author Nevin
	 * added on 12/29/2010
	 */	
	public static void nullifyMedication(TrimEx trim, AppEvalAdaptor app) throws Exception {
		ActRelationship activeMedicationsRel = ((ActEx)trim.getAct()).getRelationship().get("activeMedications");
		List<ActRelationship> activeMedsList = activeMedicationsRel.getAct().getRelationships();
		for (ActRelationship activeMedicationRel : activeMedsList) {
			if (activeMedicationRel.isEnabled()) {
				String menuDataId = activeMedicationRel.getAct().getObservation().getValues().get(0).getST().getValue();
				MenuData mdPrior = getMBean().findMenuDataItem(Long.parseLong(menuDataId));
				mdPrior.setDeleted(true);
			}
		}
	}
	
	public static MenuLocal getMBean() {
		return menuBean;
	}
	
	public static CreatorLocal getCreatorBean() {
        return creatorBean;
    }
}
