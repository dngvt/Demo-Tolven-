package org.tolven.process;

import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.xml.bind.JAXBException;

import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.ccr.CCRMarshaller;
import org.tolven.core.AccountDAOLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.entity.DocXML;
import org.tolven.logging.TolvenLogger;
import org.tolven.security.key.DocumentSecretKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.trim.CE;
import org.tolven.trim.Compute.Property;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.TrimMarshaller;
import org.tolven.trim.ValueSet;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.TrimEx;
import org.tolven.trim.ex.ValueSetEx;

public class SmkAnalysisOrderCompute extends ComputeBase {
	
	private TrimEx trim;
	private ValueSetEx encounterTemplate,contactTemplate;
	private ValueSetEx encounter, contact;
	private List<String> trimPaths = new ArrayList<String>(1);
	private List<String> trimTimestamps = new ArrayList<String>(1);
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddhhmm");
    private static CCRMarshaller ccrMarshaller;
    private static TrimMarshaller trimMarshaller;

	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}

	private boolean enabled;
	private String action;

	/**
	 * Function to retrieve menu data items corresponding to a path.
	 * @param path
	 * @param contextPath
	 * @param condition
	 * @param accountUser
	 * @return
	 * @throws ParseException
	 */
	public List<Map<String, Object>> findAllMenuDataList(String path, String contextPath, String condition, AccountUser accountUser) throws ParseException{
		List<Map<String, Object>> list = null;
		MenuQueryControl ctrl = new MenuQueryControl();
		DateFormat df = new SimpleDateFormat("MM/dd/yy");
		String splitChar = ":";
		MenuStructure ms = menuBean.findMenuStructure(accountUser.getAccount().getId(), path );
		Map<String, Long> nodeValues = new HashMap<String, Long>(10);
		nodeValues.putAll(new MenuPath( contextPath ).getNodeValues());
		ctrl.setMenuStructure( ms );
		ctrl.setNow(new Date());
		ctrl.setAccountUser(accountUser);
		ctrl.setOriginalTargetPath( new MenuPath(ms.instancePathFromContext(nodeValues, true)));
		ctrl.setRequestedPath( ctrl.getOriginalTargetPath() );
		
		if (condition != null && condition != "") {
			String[] condnParams=condition.split(splitChar);
			String[] param;
			String paramStr;
			long t;
			
			for (int i = 0; i < condnParams.length; i++) {
				param=condnParams[i].split("=");
				
				if (param.length > 1){
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
						} else if (param[1].length()==8){
							paramStr=param[1].substring(4, 6)+"/"+param[1].substring(6, 8)+"/"+param[1].substring(2, 4);
							ctrl.setFromDate(df.parse(paramStr));
							t = df.parse(paramStr).getTime();
							t += 24 * 60 * 60 * 1000;
							ctrl.setToDate(new Date(t));
						}						
					} else if (param[0].endsWith("Filter")) {
						ctrl.getFilters().put(param[0].substring(0, param[0].length()-6), param[1]);
					} else if (param[0].endsWith("Sort")) {
						ctrl.setSortOrder(param[0].substring(0, param[0].length()-4));
						ctrl.setSortDirection(param[1]);
					} else if (param[0].toLowerCase().equals("limit"))
						ctrl.setLimit(Integer.parseInt(param[1]));
				}
			}
		}
		
		list =  menuBean.findMenuDataByColumns(ctrl).getResults();
		
		return list;
	}
	
	protected TrimEx findTrimData(String menuPath, String contextPath, String condition) throws ParseException, JAXBException {
		TrimEx trimEx = null; //getCCHITBean().findTrimData(params, null);
		Long id = new Long(0);
		
		List<Map<String, Object>> list = findAllMenuDataList(menuPath, contextPath, condition, getAccountUser());
		
		if (list != null && list.size() > 0) {
			Map<String, Object> map=list.get(0);
			id = new Long(map.get("id").toString());
		}		
		
		MenuData md = menuBean.findMenuDataItem(id);
		
		if (md != null){
			DocXML docXMLFrom = (DocXML) getDocumentBean().findDocument(md.getDocumentId());
			String keyAlgorithm = getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
			TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
			PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);						
			trimEx = (TrimEx) getTrimMarshaller().unmarshal(docXMLFrom, getAccountUser(), userPrivateKey);						
		} 	
		return trimEx;
	}
	
	private Date convertDateToTimeFormat(String theDate) {
		Date convertDate = null;
		
		try {
			convertDate = dateTimeFormat.parse(theDate);
		} catch (Exception e) {
			new Exception("The type of date cant be converted");
		}
		return convertDate;
	}

	public void compute() throws Exception {
		
		if (isEnabled()) {
			TolvenLogger.info("-------------> Compute enabled for SmkAnalysisOrder:",SmkAnalysisOrderCompute.class);
			setTrim((TrimEx) this.getTrim());
			
			try {
				if (getAction().equals("labOrder")
						|| getAction().equals("imageOrder")
						|| getAction().equals("referralRequest")
						|| getAction().equals("pxDoc")
						|| getAction().equals("immunizationOrder")
						|| getAction().equals("Problems")
						|| getAction().equals("dxDoc")) {

					encounterTemplate = getValueSetEx(trim, "encounterTemplate");
					encounter = getValueSetEx(trim, "encounter");
					insertPriorData("echr:patient:encounters:active","DateSort=DESC", "Encounter", encounterTemplate,encounter);
				}

				if (getAction().equals("smokingAssmt")|| getAction().equals("vitalAssessment")) {
					encounterTemplate = getValueSetEx(trim, "encounterTemplate");
					encounter = getValueSetEx(trim, "encounter");
					insertPriorData("echr:patient:encounters:active","DateSort=DESC", "Encounter", encounterTemplate,encounter);
				}

				if (getAction().equals("Progressnotes")) {
					checkAndSetSharingInfo();
					encounterTemplate = getValueSetEx(trim, "encounterTemplate");
					encounter = getValueSetEx(trim, "encounter");
					insertPriorData("echr:patient:encounters:active","DateSort=DESC", "Encounter", encounterTemplate,encounter);

					MenuStructure ms = menuBean.findMenuStructure(getAccountUser().getAccount().getId(), "echr:patient:observations:all");
					MenuQueryControl ctrl = new MenuQueryControl();
					ctrl.setMenuStructure(ms);
					ctrl.setNow(new Date());
					ctrl.setAccountUser(getAccountUser());
					Map<String, Long> nodeValues = new HashMap<String, Long>(10);
					nodeValues = ((MenuPath) getContextList().get(0)).getNodeValues();
					ctrl.setOriginalTargetPath(new MenuPath(ms.instancePathFromContext(nodeValues, true)));
					ctrl.setRequestedPath(ctrl.getOriginalTargetPath());
					ctrl.setActualMenuStructure(ms);
					
					ctrl.setSortOrder("date01");
					ctrl.setSortDirection("DESC");
					List<MenuData> items = getMenuBean().findMenuData(ctrl);
					String contextPath = getContextList().get(0).getPathString();

					TrimEx trimEx = findTrimData("echr:patient:observations:all", contextPath, "TestFilter=Temperature:DateSort=DESC");
					
					
					if (trimEx != null) {
						trimEx.getAct();
						Double PQ = trimEx.getAct().getObservation().getValues().get(0).getPQ().getValue();
						String theDate = trimEx.getAct().getEffectiveTime().getTS().getValue();
						Calendar cal = Calendar.getInstance();
						

						Date compareDate = convertDateToTimeFormat(theDate);
							
						/*
						 * Temperature data will be pre-populated from data
						 * captured in the patient intake assessment if one was
						 * undertaken on the same day.
						 */
						if (items != null && items.size() > 0 && ((dateFormat.format(cal.getTime())).equals(dateFormat.format(compareDate)))) {
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("temperature")
									.setEnabled(true);
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("temperature")
									.getAct().getObservation().getValues().get(
											0).getPQ().setValue(PQ);
						} else {
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("temperature")
									.setEnabled(false);
						}
					} else {
						((ActEx) ((ActEx) this.getAct()).getRelationship().get(
								"objective").getAct()).getRelationship().get(
								"temperature").setEnabled(false);
					}
					
					
					TrimEx trimEx1 = findTrimData("echr:patient:observations:all", contextPath, "TestFilter=Pulse:DateSort=DESC");
					
					if (trimEx1 != null) {
						trimEx1.getAct();
						Double PQ1 = trimEx1.getAct().getObservation().getValues().get(0).getPQ().getValue();
						String date1 = trimEx1.getAct().getEffectiveTime().getTS().getValue();
						Calendar cal1 = Calendar.getInstance();
						
						Date compareDate = convertDateToTimeFormat(date1);
						
						/*
						 * Pulse data will be pre-populated from data captured
						 * in the patient intake assessment if one was
						 * undertaken on the same day.
						 */
						if (items != null
								&& items.size() > 0
								&& ((dateFormat.format(cal1.getTime())).equals(dateFormat.format(compareDate)))) {
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("pulse").setEnabled(
											true);
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("pulse").getAct()
									.getObservation().getValues().get(1)
									.getST().setValue(PQ1.toString());
						} else {
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("pulse").setEnabled(
											false);
						}
					} else {
						((ActEx) ((ActEx) this.getAct()).getRelationship().get(
								"objective").getAct()).getRelationship().get(
								"pulse").setEnabled(false);
					}
					
					TrimEx trimEx2 = findTrimData("echr:patient:observations:all", contextPath, "TestFilter=Respiration:DateSort=DESC");
					
					if (trimEx2 != null) {
						trimEx2.getAct();
						Double PQ2 = trimEx2.getAct().getObservation().getValues().get(0).getPQ().getValue();
						String date2 = trimEx2.getAct().getEffectiveTime().getTS().getValue();
						Calendar cal2 = Calendar.getInstance();
						
						Date compareDate = convertDateToTimeFormat(date2);
						
							/*
						 * Respiration Rate data will be pre-populated from data
						 * captured in the patient intake assessment if one was
						 * undertaken on the same day.
						 */
						if (items != null
								&& items.size() > 0
								&& ((dateFormat.format(cal2.getTime()))
										.equals(dateFormat.format(compareDate)))) {
							ActEx act = (ActEx) this.getAct();
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("respirationrate")
									.setEnabled(true);
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("respirationrate")
									.getAct().getObservation().getValues().get(
											0).getPQ().setValue(PQ2);
						} else {
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("respirationrate")
									.setEnabled(false);
						}
					} else {
						((ActEx) ((ActEx) this.getAct()).getRelationship().get(
								"objective").getAct()).getRelationship().get(
								"respirationrate").setEnabled(false);
					}
					
					TrimEx trimEx3 = findTrimData("echr:patient:observations:all", contextPath, "TestFilter=Blood:DateSort=DESC");
					
						if (trimEx3 != null) {
						trimEx3.getAct();
						Double PQ3 = ((ActEx) trimEx3.getAct())
								.getRelationship().get("systolic").getAct()
								.getObservation().getValues().get(0).getPQ()
								.getValue();
						String date3 = trimEx3.getAct().getEffectiveTime().getTS().getValue();
						Calendar cal3 = Calendar.getInstance();
						
						Date compareDate = convertDateToTimeFormat(date3);
						
						/*
						 * Blood Pressure-Systolic data will be pre-populated
						 * from data captured in the patient intake assessment
						 * if one was undertaken on the same day.
						 */
						if (items != null
								&& items.size() > 0
								&& ((dateFormat.format(cal3.getTime())).equals(dateFormat.format(compareDate)))) {
							ActEx act = (ActEx) this.getAct();
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("systolic")
									.setEnabled(true);
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("systolic").getAct()
									.getObservation().getValues().get(1)
									.getST().setValue(PQ3.toString());
						} else {
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("systolic")
									.setEnabled(false);
						}
					} else {
						((ActEx) ((ActEx) this.getAct()).getRelationship().get(
								"objective").getAct()).getRelationship().get(
								"systolic").setEnabled(false);
					}
						
					if (trimEx3 != null) {
						trimEx3.getAct();
						Double PQ4 = ((ActEx) trimEx3.getAct())
								.getRelationship().get("diastolic").getAct()
								.getObservation().getValues().get(0).getPQ()
								.getValue();
						String date4 = trimEx3.getAct().getEffectiveTime().getTS().getValue();
						Calendar cal4 = Calendar.getInstance();
						

						Date compareDate = convertDateToTimeFormat(date4);
						
						
						/*
						 * Blood Pressure-Diastolic data will be pre-populated
						 * from data captured in the patient intake assessment
						 * if one was undertaken on the same day.
						 */
						if (items != null
								&& items.size() > 0
								&& ((dateFormat.format(cal4.getTime()))
										.equals(dateFormat.format(compareDate)))) {
							ActEx act = (ActEx) this.getAct();
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("diastolic")
									.setEnabled(true);
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("diastolic")
									.getAct().getObservation().getValues().get(
											1).getST().setValue(PQ4.toString());
						} else {
							((ActEx) ((ActEx) this.getAct()).getRelationship()
									.get("objective").getAct())
									.getRelationship().get("diastolic")
									.setEnabled(false);
						}
					} else {
						((ActEx) ((ActEx) this.getAct()).getRelationship().get(
								"objective").getAct()).getRelationship().get(
								"diastolic").setEnabled(false);
					}
				}

				if (getAction().equals("AdvanceDirectives")) {

					// To populate contacts to the wizard in a drop-down in the
					// ephr account.
					contactTemplate = getValueSetEx(trim, "contactTemplate");
					contact = getValueSetEx(trim, "contact");
					insertPriorData("ephr:patient:personal:contacts",
							"DateSort=DESC", "Contact", contactTemplate,
							contact);

					// To get the patient trim.
					String patientId = (trim.getAct().getParticipations()
							.get(0).getRole().getId().getIIS().get(0)
							.getExtension()).split(":")[1].split("-")[1];
					TrimEx priorPatientTrim = null;
					Long longPatientId = new Long(0);
					longPatientId = Long.parseLong(patientId);
					
					
					 
					MenuData md = menuBean.findMenuDataItem(longPatientId);
					
					if (md != null){
						DocXML docXMLFrom = (DocXML) getDocumentBean().findDocument(md.getDocumentId());
						String keyAlgorithm = getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
						TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
						PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);						
						priorPatientTrim = (TrimEx) getTrimMarshaller().unmarshal(docXMLFrom, getAccountUser(),userPrivateKey);
						
					} 
					

					// To show the patient name on advance directive wizard.
					String name = priorPatientTrim.getAct().getParticipations()
							.get(0).getRole().getPlayer().getName().getENS()
							.get(0).getParts().get(2).getST().getValue()
							+ ", "
							+ priorPatientTrim.getAct().getParticipations()
									.get(0).getRole().getPlayer().getName()
									.getENS().get(0).getParts().get(0).getST()
									.getValue()
							+ " "
							+ priorPatientTrim.getAct().getParticipations()
									.get(0).getRole().getPlayer().getName()
									.getENS().get(0).getParts().get(1).getST()
									.getValue();
					((ActEx) trim.getAct()).getRelationship().get(
							"powerOfAtterny").getAct().getObservation()
							.getValues().get(0).getST().setValue(name);

					// To show the patient's county on advance directive wizard.
					String county = priorPatientTrim.getAct()
							.getParticipations().get(0).getRole().getPlayer()
							.getPerson().getAddr().getADS().get(0).getParts()
							.get(2).getST().getValue();
					((ActEx) trim.getAct()).getRelationship().get(
							"powerOfAtterny").getAct().getObservation()
							.getValues().get(1).getST().setValue(county);

				}
			} catch (Exception e) {
				TolvenLogger.error(
						"-------------> Error in SmkAnalysisOrderCompute compute. Error message: "
								+ e.getMessage(), SmkAnalysisOrderCompute.class);
				TolvenLogger.error(e.getMessage() + "<-->" + e.toString(),
						SmkAnalysisOrderCompute.class);
			}

			TolvenLogger.info(
					"-------------> Disabling compute for SmkAnalysisOrders:",
					SmkAnalysisOrderCompute.class);
		}
		disableCompute();
	}
	/**
	 * To write each ce to valueset
	 * @param newCode
	 * @param newDisplayName
	 * @param codeSystem
	 * @param codeSystemVersion
	 * @param vSet
	 */
	void writeCE(String newCode, String newDisplayName, String codeSystem, String codeSystemVersion,ValueSet vSet) {
		CE newCE = new CE();
		newCE.setCode(newCode);
		newCE.setDisplayName(newDisplayName);
		newCE.setCodeSystem(codeSystem);
		newCE.setCodeSystemVersion(codeSystemVersion);
		vSet.getBindsAndADSAndCDS().add(newCE);
		newCE = null;
	}

	/**
	 * To set receiver account information if the patient wants to receive
	 * information.
	 * 
	 * @author valsaraj added on 06/18/2010
	 * @param void
	 * @return void
	 */
	public void checkAndSetSharingInfo() {
		try {
			Object patDoc = null;
			MenuData md = menuBean.findMenuDataItem(getContextList().get(0).getNodeKeys()[1]);
			
			if (md != null) {
				DocXML docXMLFrom = (DocXML) getDocumentBean().findDocument(md.getDocumentId());
				String keyAlgorithm = getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
				TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
				PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);						
				String ns = docXMLFrom.getXmlNS();
		        if (ns.equals("urn:tolven-org:trim:4.0")) {
	                patDoc = getTrimMarshaller().unmarshal(docXMLFrom, getAccountUser(), userPrivateKey);
		        } else if (ns.equals("urn:astm-org:CCR")) {
	                patDoc = getCCRMarshaller().unmarshal(docXMLFrom, getAccountUser(), userPrivateKey);
		        }
				
			}
			

			if (patDoc instanceof TrimEx) {
				TrimEx patTrim = (TrimEx) patDoc;

				if (((ActEx) ((ActEx) patTrim.getAct()).getRelationship().get(
						"preference").getAct()).getRelationship().get(
						"sendEncounter").isEnabled()) {
					List<ObservationValueSlot> obsSource = ((ActEx) patTrim
							.getAct()).getRelationship().get("preference")
							.getAct().getRelationships().get(0).getAct()
							.getRelationships().get(2).getAct()
							.getObservation().getValues();
					List<ObservationValueSlot> obsTarget = ((ActEx) getTrim()
							.getAct()).getRelationship().get("receiverInfo")
							.getAct().getObservation().getValues();
					obsTarget.get(0).setST(obsSource.get(0).getST());
					obsTarget.get(1).setST(obsSource.get(1).getST());
					((ActEx) getTrim().getAct()).getRelationship().get(
							"shareFields").getAct().getObservation()
							.getValues().get(0).getST()
							.setValue(getShareInfo());
				}
			}
		} catch (Exception e) {

		}
	}

	/**
	 * Returns share information.
	 * 
	 * @author valsaraj added on 06/22/2010
	 * @param void
	 * @return String
	 */
	public String getShareInfo() {
		String shareInfo = "";

		try {
			
			TrimEx trimEx = findTrimData("echr:admin:providers:providerSettings", "echr:admin:providers:providerSettings", "TitleFilter=Provider Settings:DateSort=DESC");
		
			try {
				shareInfo += ((ActEx) trimEx.getAct()).getRelationship().get("diagnosis").isEnabled() != false ? "1" : "0";
			} catch (Exception e) {
				shareInfo += "1";
			}

			try {
				shareInfo += ((ActEx) trimEx.getAct()).getRelationship().get("treatments").isEnabled() != false ? "1" : "0";
			} catch (Exception e) {
				shareInfo += "1";
			}

			try {
				shareInfo += ((ActEx) trimEx.getAct()).getRelationship().get("medications").isEnabled() != false ? "1" : "0";
			} catch (Exception e) {
				shareInfo += "1";
			}

			try {
				shareInfo += ((ActEx) trimEx.getAct()).getRelationship().get("referrals").isEnabled() != false ? "1" : "0";
			} catch (Exception e) {
				shareInfo += "1";
			}

			try {
				shareInfo += ((ActEx) trimEx.getAct()).getRelationship().get("testOrdersAndResults").isEnabled() != false ? "1" : "0";
			} catch (Exception e) {
				shareInfo += "1";
			}
		} catch (Exception e) {

		}

		return shareInfo;
	}

	private ValueSetEx getValueSetEx(TrimEx trim, String name) {
		return (ValueSetEx) trim.getValueSet().get(name);
	}

	/**
	 * Returns latest trim in the given path with the given wizard name
	 * 
	 * @param path
	 * @param conditions
	 * @param wizardName
	 * @return
	 */
	private List<Map<String, Object>> getAllTrimData(String path,
			String conditions, String wizardName) {
		List<Map<String, Object>> list = null;
		try {
			list = findAllMenuDataList(path,
					getContextList().get(0).getPathString(), conditions,
					getAccountUser());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	private void insertPriorData(String path, String conditions,
			String wizardName, ValueSetEx template, ValueSetEx target) {
		List<Map<String, Object>> priorTrims = null;
		TrimEx priorTrim = null;
		priorTrims = getAllTrimData(path, conditions, wizardName);
		String displayName = null;
		if(priorTrims.size()==0 && !wizardName.equals("Contact")){
			displayName = "None";
			int cnt = 0;
			addToValueSet(template, target, displayName, cnt);
		}
		Long id = new Long(0);
		int count = 0;
		try {
			for (Map<String, Object> map : priorTrims) {
				id = new Long(map.get("id").toString());
				MenuData md = menuBean.findMenuDataItem(id);
				
				if (md != null) {
					DocXML docXMLFrom = (DocXML) getDocumentBean().findDocument(md.getDocumentId());
					String keyAlgorithm = getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
					TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
					PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);						
					
					priorTrim = (TrimEx) getTrimMarshaller().unmarshal(docXMLFrom, getAccountUser(), userPrivateKey);
					
				}
				
				if (priorTrim != null && validateTrims(priorTrim)) {
					getCEDisplayName(priorTrim, wizardName, count, template,
							target);
					count++;
				}
				
			}
		} catch (Exception e) {
			TolvenLogger
					.error(
							"-------------> Error in loading prior datas for "
									+ wizardName + ". Error message: "
									+ e.getMessage(), SmkAnalysisOrderCompute.class);
			TolvenLogger.error(e.getMessage() + "<-->" + e.toString(),
					SmkAnalysisOrderCompute.class);
		}

	}

	/**
	 * Returns name to be displayed in valuesets in trim.
	 * 
	 * @param trim
	 * @param wizardName
	 * @return
	 */
	private String getCEDisplayName(TrimEx trim, String wizardName, int count,
			ValueSetEx template, ValueSetEx target) {
		String displayName = null;
		try {
			if (wizardName.equals("Encounter")) {
				String encYear = null;
				String encMonth = null;
				String encDate = null;
				if(trim.getDescription().equals("Ambulatory Encounter")){
					encYear = trim.getAct().getEffectiveTime().getTS().getValue().substring(2, 4);
					encMonth = trim.getAct().getEffectiveTime().getTS().getValue().substring(4, 6);
					encDate = trim.getAct().getEffectiveTime().getTS().getValue().substring(6,8);
				}
				else if(trim.getDescription().equals("Inpatient Encounter")){
					encYear = trim.getAct().getEffectiveTime().getIVLTS().getLow().getTS().getValue().substring(2, 4);
					encMonth = trim.getAct().getEffectiveTime().getIVLTS().getLow().getTS().getValue().substring(4, 6);
					encDate = trim.getAct().getEffectiveTime().getIVLTS().getLow().getTS().getValue().substring(6,8);
				}
				String time = encMonth+"/"+encDate+"/"+encYear;
				String encounter = time
						+ " "
						+ trim.getAct().getParticipations().get(1).getRole()
								.getPlayer().getName().getENS().get(0)
								.getParts().get(0).getST().getValue()
						+ " "
						+ trim.getAct().getParticipations().get(2).getRole()
								.getPlayer().getName().getENS().get(0)
								.getParts().get(0).getST().getValue();
				
				displayName = encounter;
				addToValueSet(template, target, displayName, count);
			} else if (wizardName.equals("Contact")) {
				String contact = trim.getAct().getRelationships().get(0)
						.getAct().getObservation().getValues().get(0).getST()
						.toString();
				displayName = contact;
				addToValueSet(template, target, displayName, count);
			}
		} catch (Exception e) {
			TolvenLogger.error(
					"-------------> Error in getting prior values for "
							+ wizardName, SmkAnalysisOrderCompute.class);
			TolvenLogger.error(e.getMessage() + "<-->" + e.toString(),
					SmkAnalysisOrderCompute.class);
		}
		return displayName;
	}

	/**
	 * 
	 * @param template
	 * @param destination
	 * @param displayName
	 * @param count
	 */
	private void addToValueSet(ValueSetEx template, ValueSetEx destination,
			String displayName, int count) {

		CE tempCE = (CE) template.getBindsAndADSAndCDS().get(0);
		String codeSystem = tempCE.getCodeSystem();
		String codeSystemVersion = tempCE.getCodeSystemVersion();
		String code = tempCE.getCode();
		String sCode = code.substring(0, 2);
		String iCode = code.substring(2, code.length());
		tempCE = null;

		String newCode = sCode + (Integer.parseInt(iCode) + count);

		CE destinationCE = new CE();
		destinationCE.setCode(newCode);
		destinationCE.setDisplayName(displayName);
		destinationCE.setCodeSystem(codeSystem);
		destinationCE.setCodeSystemVersion(codeSystemVersion);
		destination.getBindsAndADSAndCDS().add(destinationCE);
		destinationCE = null;
	}

	/**
	 * Checks weather the trim has already been processed
	 * 
	 * @param priorTrim
	 * @return
	 */
	private boolean validateTrims(TrimEx priorTrim) {
		boolean repeat = false;
		String trimPath = priorTrim.getTolvenEventIds().get(0).getId();
		String trimTimestamp = priorTrim.getTolvenEventIds().get(0)
				.getTimestamp();
		if (!trimPaths.isEmpty()) {
			for (String path : trimPaths) {
				if (path.equalsIgnoreCase(trimPath)) {
					for (String timeStamp : trimTimestamps) {
						if (timeStamp.equalsIgnoreCase(trimTimestamp)) {
							repeat = true;
						}
					}
				}
			}
		}
		if (repeat) {
			return false;
		} else {
			trimPaths.add(trimPath);
			trimTimestamps.add(trimTimestamp);
			return true;
		}
	}

	/**
	 * This function is used to disable compute.
	 */
	private void disableCompute() {
		for (Property property : getComputeElement().getProperties()) {
			if ("enabled".equals(property.getName())) {
				property.setValue(Boolean.FALSE);
			}
		}
	}

	public TrimEx getTrim() {
		return trim;
	}

	public void setTrim(TrimEx trim) {
		this.trim = trim;
	}
	
	protected static AccountDAOLocal getAccountBean() {
        try {
            InitialContext ctx = new InitialContext();
            return (AccountDAOLocal) ctx.lookup("java:global/tolven/tolvenEJB/AccountDAOBean!org.tolven.core.AccountDAOLocal");
        } catch (Exception ex) {
            throw new RuntimeException("Could not lookup java:global/tolven/tolvenEJB/AccountDAOBean!org.tolven.core.AccountDAOLocal", ex);
        }
    }

    private CCRMarshaller getCCRMarshaller() {
        if(ccrMarshaller == null) {
            ccrMarshaller = new CCRMarshaller();
        }
        return ccrMarshaller;
    }

    private TrimMarshaller getTrimMarshaller() {
        if(trimMarshaller == null) {
            trimMarshaller = new TrimMarshaller();
        }
        return trimMarshaller;
    }
    
}
