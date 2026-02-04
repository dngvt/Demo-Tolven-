package org.tolven.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.model.SelectItem;

import org.tolven.app.EPrescriptionLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;

public class EPAction extends MenuAction {
	
	private MenuDataList menuDataList = null;
	private String prescriberOrderNum;
	private @EJB EPrescriptionLocal epBean;
	private ArrayList<SelectItem> gender;
	private ArrayList<SelectItem> spiNumbers;
	private AccountUser activeAccountUser;
	public AccountUser getActiveAccountUser() {
		return activeAccountUser;
	}

	public void setActiveAccountUser(AccountUser activeAccountUser) {
		this.activeAccountUser = activeAccountUser;
	}

	public EPAction() {
		super();
		this.setActiveAccountUser(TolvenRequest.getInstance().getAccountUser());
	}
	
	public EPrescriptionLocal getEpBean() {
		return epBean;
	}
	public void setEpBean(EPrescriptionLocal epBean) {
		this.epBean = epBean;
	}
	public String getPrescriberOrderNum() {
		return prescriberOrderNum;
	}
	public void setPrescriberOrderNum(String prescriberOrderNum) {
		this.prescriberOrderNum = prescriberOrderNum;
	}
	
	/**
	 * This class used to retrieve MenuData List. 
	 */
	class MenuDataList extends HashMap<Object, List<Map<String, Object>>> {
		private MenuLocal menuLocal;
		private long accountId;
		private String contextPath;
		private Date dateNow;
		private AccountUser accountUser;
		public MenuDataList( MenuLocal menuLocal, long accountId, String context, Date dateNow, AccountUser accountUser) {
			this.menuLocal = menuLocal;
			this.accountId = accountId;
			this.contextPath = context;
			this.dateNow = dateNow;
			this.accountUser = accountUser;
		}		
		/**
		 * This function prepare the menudata list and add it to 'MenuDataList'
		 * @param path
		 */ 
		public void buildList(Object path ) {
			try{
				MenuQueryControl ctrl = new MenuQueryControl();
				DateFormat df = new SimpleDateFormat("MM/dd/yy");
				String splitChar =",";
				String parsedPath = path.toString().split(splitChar)[0];			
				
				MenuStructure ms = menuLocal.findMenuStructure(accountId, parsedPath );
				Map<String, Long> nodeValues = new HashMap<String, Long>(10);
				nodeValues.putAll(new MenuPath( contextPath ).getNodeValues());
				ctrl.setMenuStructure( ms );
				ctrl.setNow(dateNow);
				ctrl.setAccountUser(accountUser);
				ctrl.setOriginalTargetPath( new MenuPath(ms.instancePathFromContext ( nodeValues, true )));
				ctrl.setRequestedPath( ctrl.getOriginalTargetPath() );
				if (path.toString().split(splitChar).length>1 && path.toString().split(splitChar)[1]!=""){
					String[] params=path.toString().split(splitChar)[1].split(":");
					String[] param;
					String paramStr;
					long t;
					for (int i = 0; i < params.length; i++) {
						param=params[i].split("=");
						if (param.length>1){
							if (param[0].equals("filter"))
								ctrl.setFilter(param[1]);
							else if (param[0].equals("fromDate"))
								ctrl.setFromDate(df.parse(param[1]));
							else if (param[0].equals("toDate"))
								ctrl.setToDate(df.parse(param[1]));
							else if (param[0].equals("DateFilter")){
								if (param[1].contains("/")){
									ctrl.setFromDate(df.parse(param[1]));
									t = df.parse(param[1]).getTime();
									t += 24 * 60 * 60 * 1000;
									ctrl.setToDate(new Date(t));
								}
								else if (param[1].length()==8){
									paramStr=param[1].substring(4, 6)+"/"+param[1].substring(6, 8)+"/"+param[1].substring(2, 4);
									ctrl.setFromDate(df.parse(paramStr));
									t = df.parse(paramStr).getTime();
									t += 24 * 60 * 60 * 1000;
									ctrl.setToDate(new Date(t));
								}
							}else if (param[0].endsWith("Filter")) {
								ctrl.getFilters().put(param[0].substring(0, param[0].length()-6), param[1]);
							}else if (param[0].endsWith("Sort")) {
								ctrl.setSortOrder(param[0].substring(0, param[0].length()-4));
								ctrl.setSortDirection(param[1]);
							}else if (param[0].toLowerCase().equals("limit"))
								ctrl.setLimit(Integer.parseInt(param[1]));
						}
					}
				}
				List<Map<String, Object>> list =  menuLocal.findMenuDataByColumns(ctrl).getResults();			
				this.put(path, list);
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		public List<Map<String, Object>> get(Object path) {
			if (!this.containsKey(path)) buildList(path);
			return super.get(path);
		}
	}
	
	/**
	 * This function is used to retrieve MenuData List.
	 * Format - <path>,<fieldName1>Filter=<fieldValue1>:<fieldName2>Filter=<fieldValue2>:
	 * 			:fromDate=<FROMDATE>:toDate=<TODATE>:<fieldName>Sort=<sortOrder>:limit=<value>
	 * eg1: echr:patient:specimens:active
	 * eg2: echr:patient:specimens:active,titleFilter=Desc:nameSort=ASC:limit=10
	 * @author Suja
	 * added on 5/29/09
	 * @return MenuDataList
	 * @throws Exception
	 */
	public MenuDataList getAllMenuData() throws Exception {
		if (menuDataList==null) {
			menuDataList = new MenuDataList( this.getMenuLocal(), this.getAccountId(), 
					this.getElement(), getNow(), getAccountUser());
		}
		return menuDataList;
	}
	/**
	 * This function is used to get MenuData List
	 * @param map
	 * @return List<Map<String, Object>>
	 * @throws Exception
	 */
	public List<Map<String, Object>> getAllMenuDataList(Map<String, Object> map) throws Exception {
		menuDataList = new MenuDataList( this.getMenuLocal(), Long.parseLong(map.get("accountId").toString()), 
				map.get("context").toString(),(Date) map.get("tolvenNow"),(AccountUser) map.get("accountUser"));
		return menuDataList.get(map.get("path"));
	}
	/**
	 * @return the gender
	 */
	public ArrayList<SelectItem> getGender() {
		if(null == gender){
			gender = new ArrayList<SelectItem>();
			SelectItem item = new SelectItem("Male","Male");
			SelectItem item1 = new SelectItem("Female","Female");
			gender.add(0,item);
			gender.add(1,item1);
		}
		return gender;
	}
	/**
	 * @param gender the gender to set
	 */
	public void setGender(ArrayList<SelectItem> gender) {
		this.gender = gender;
	}
	/**
	 * @return the spiNumbers
	 */
	public ArrayList<SelectItem> getSpiNumbers() {
		if(null == spiNumbers){
			spiNumbers = new ArrayList<SelectItem>();
			if(null != getAccountUser().getOpenMeFirst()){
				Long id = Long.valueOf(getAccountUser().getOpenMeFirst().split("-")[1]);
				ArrayList<String> spinumbers = epBean.retrieveAllSPIs(id);
				for(String spi : spinumbers){
					if(null != spi)
						spiNumbers.add(new SelectItem(spi,spi));
				}
		    }	
		}
		return spiNumbers;
	}
	/**
	 * @param spiNumbers the spiNumbers to set
	 */
	public void setSpiNumbers(ArrayList<SelectItem> spiNumbers) {
		this.spiNumbers = spiNumbers;
	}
	
	public List<Map<String, Object>> getFavouriteMedicationsList() {
		List<Map<String, Object>> favouriteMedicationList = new ArrayList<Map<String,Object>>();

		try {
			List<Map<String, Object>> favouriteList = getAllMenuData().get("echr:admin:lists:accountLists");
			for (Map<String, Object> map : favouriteList) {
				if (map.get("Type").equals("medications")) {
					favouriteMedicationList.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return favouriteMedicationList;
	}
	
	}
