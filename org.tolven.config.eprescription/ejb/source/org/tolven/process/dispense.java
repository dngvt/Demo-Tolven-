package org.tolven.process;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.logging.TolvenLogger;
import org.tolven.trim.ex.ActEx;

public class dispense extends InsertAct {
	private ActEx act;
	private static final String path = "echr:patient:medications:active";
	private String id;
	private String medicationName;
	private String strength;
	private Long frequency;
	private Date startDate;
	private Date endDate;
	private String route;
	private Long dispenseAmount;
	private Long refills;

	public void compute() {
		act = (ActEx) this.getAct();
		id = act.getRelationship().get("dispense").getAct().getObservation()
				.getValues().get(13).getST().getValue();
		if (id != null && !id.equalsIgnoreCase("")) {
			if (getList(path)) {
				act.getRelationship().get("dispense").getAct().getObservation()
						.getValues().get(1).getST().setValue(medicationName);
				act.getRelationship().get("dispense").getAct().getObservation()
						.getValues().get(3).getST().setValue(strength);
				act.getRelationship().get("dispense").getAct().getObservation()
						.getValues().get(4).getINT().setValue(frequency);
				act.getRelationship().get("dispense").getAct().getObservation()
						.getValues().get(8).getST().setValue(
								startDate.toString());
				act.getRelationship().get("dispense").getAct().getObservation()
						.getValues().get(9).getST()
						.setValue(endDate.toString());
				act.getRelationship().get("dispense").getAct().getObservation()
						.getValues().get(5).getST().setValue(route);
				act.getRelationship().get("dispense").getAct().getObservation()
						.getValues().get(7).getINT().setValue(dispenseAmount);
				act.getRelationship().get("dispense").getAct().getObservation()
						.getValues().get(6).getINT().setValue(refills);

			}
		} else {
			act.getRelationship().get("dispense").getAct().getObservation()
					.getValues().get(1).getST().setValue("");
			act.getRelationship().get("dispense").getAct().getObservation()
					.getValues().get(3).getST().setValue("");
			act.getRelationship().get("dispense").getAct().getObservation()
					.getValues().get(4).getINT().setValue(0);
			act.getRelationship().get("dispense").getAct().getObservation()
					.getValues().get(8).getST().setValue("");
			act.getRelationship().get("dispense").getAct().getObservation()
					.getValues().get(9).getST().setValue("");
			act.getRelationship().get("dispense").getAct().getObservation()
					.getValues().get(5).getST().setValue("");
			act.getRelationship().get("dispense").getAct().getObservation()
					.getValues().get(7).getINT().setValue(0);
			act.getRelationship().get("dispense").getAct().getObservation()
					.getValues().get(6).getINT().setValue(0);

		}
	}

	/**
	 * This function is used to retrieve the list by specifying a path
	 * 
	 * @param path
	 * 
	 */
	private boolean getList(String path) {
		boolean status = false;
		try {
			MenuStructure ms = getMenuBean().findMenuStructure(
					getAccountUser().getAccount().getId(), path);
			MenuQueryControl ctrl = new MenuQueryControl();
			ctrl.setMenuStructure(ms);
			ctrl.setNow(new Date());
			ctrl.setAccountUser(getAccountUser());
			Map<String, Long> nodeValues = new HashMap<String, Long>(10);
			nodeValues = getContextList().get(0).getNodeValues();
			ctrl.setOriginalTargetPath(new MenuPath(ms.instancePathFromContext(
					nodeValues, true)));
			ctrl.setRequestedPath(ctrl.getOriginalTargetPath());
			ctrl.setActualMenuStructure(ms);
			List<MenuData> items = getMenuBean().findMenuData(ctrl);

			for (MenuData item : items) {
				if (item.getString07().equals(id)) {
					medicationName = item.getString01();
					strength = item.getString02();
					frequency = item.getLong01();
					startDate = item.getDate03();
					endDate = item.getDate02();
					route = item.getString04();
					dispenseAmount = item.getLong02();
					refills = item.getLong03();
					status = true;
				}

			}
		} catch (Exception e) {
			TolvenLogger.info(e.getMessage(), InsertAct.class);
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the strength
	 */
	public String getStrength() {
		return strength;
	}

	/**
	 * @param strength
	 *            the strength to set
	 */
	public void setStrength(String strength) {
		this.strength = strength;
	}

	/**
	 * @return the frequency
	 */
	public Long getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency
	 *            the frequency to set
	 */
	public void setFrequency(Long frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the route
	 */
	public String getRoute() {
		return route;
	}

	/**
	 * @param route
	 *            the route to set
	 */
	public void setRoute(String route) {
		this.route = route;
	}

	/**
	 * @return the dispenseAmount
	 */
	public Long getDispenseAmount() {
		return dispenseAmount;
	}

	/**
	 * @param dispenseAmount
	 *            the dispenseAmount to set
	 */
	public void setDispenseAmount(Long dispenseAmount) {
		this.dispenseAmount = dispenseAmount;
	}

	/**
	 * @return the refills
	 */
	public Long getRefills() {
		return refills;
	}

	/**
	 * @param refills
	 *            the refills to set
	 */
	public void setRefills(Long refills) {
		this.refills = refills;
	}

	/**
	 * @return the medicationName
	 */
	public String getMedicationName() {
		return medicationName;
	}

	/**
	 * @param medicationName
	 *            the medicationName to set
	 */
	public void setMedicationName(String medicationName) {
		this.medicationName = medicationName;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

}
