package org.tolven.web;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;

/**
 * JSF managed bean to handle actions with patient clinical history
 * information
 */
@ManagedBean(name = "patHistory", eager = true)
@RequestScoped
public class PatientHistoryAction extends MenuAction{
	/**
	 * Find history data for a list(:problems:active, :medications:active...)
	 * @param listPath
	 * @return
	 */
	private MenuData patient;
	
	public void setPatient(MenuData patient) {
		this.patient = patient;
	}

	public List<MenuData> getMenuDataList(String listPath){
		String patientPath = getPatientPath();
		if(patientPath != null){
			String patientListPath = patientPath+listPath;
			MenuPath menuPath = new MenuPath(patientListPath);
			MenuQueryControl ctrl = new MenuQueryControl();
			AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
			AccountMenuStructure ams = getMenuLocal().findAccountMenuStructure(accountUser.getAccount().getId(), menuPath.getPath());
			if(ams == null)
				return null;
			ctrl.setMenuStructure( ams);
			ctrl.setAccountUser(accountUser);
			ctrl.setOffset( 0 );
			ctrl.setOriginalTargetPath( menuPath );
			ctrl.setRequestedPath( menuPath );
			ctrl.setSortDirection( "ASC");
			return getMenuLocal().findMenuData(ctrl);
		}
		return null;
	}
	
	/**
	 * Method to find patient path
	 * @return patient's menupath
	 */
	public String getPatientPath(){
		String element = this.getElement();
		//check if it's a patient menu path
		if(element == null || element.indexOf(":patient-") == -1){
			return null;
		}else{
			int patientPathIndex = element.indexOf(":patient-");
			return element.substring(0, element.indexOf(":", patientPathIndex+1));
		}
	}
	public MenuData getPatient(){
		if(this.patient == null){
			AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
			this.patient = getMenuLocal().findMenuDataItem(accountUser.getAccount().getId(), getPatientPath());
		}
		return this.patient;
	}

}
