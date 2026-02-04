package org.tolven.ajax;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.resource.spi.IllegalStateException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.tolven.app.CreatorLocal;
import org.tolven.app.EPrescriptionLocal;
import org.tolven.app.ErxTrimCreatorLocal;
import org.tolven.app.FDBInterface;
import org.tolven.app.MenuLocal;
import org.tolven.app.TrimLocal;
import org.tolven.app.TrimMessageLocal;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.el.ELFunctions;
import org.tolven.app.entity.MenuData;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.DocumentLocal;
import org.tolven.doc.entity.DocXML;
import org.tolven.logging.TolvenLogger;
import org.tolven.process.MessageID;
import org.tolven.doc.DocProtectionLocal;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.CE;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.ST;
import org.tolven.trim.TrimMarshaller;
import org.tolven.trim.ValueSet;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.ActRelationshipEx;
import org.tolven.trim.ex.TrimEx;
import org.tolven.trim.ex.TrimFactory;
import org.tolven.web.EPAction;
import org.tolven.web.security.GeneralSecurityFilter;

/**
 * The servlet is used for ePrescription specific tasks.
 * 
 * @author Mohammed Rias
 * @author Vineetha George
 * @since File available since Release 0.0.1
 */
/*
=============================================================================================================================================
No:  	|  Created/Updated Date |    Created/Updated By |     Method name/Comments            
==============================================================================================================================================
1    	|     06/24/2010        |     Mohammed Rias 	|      Initial Version 
==============================================================================================================================================
2    	|     03/16/2011        |     Vineetha George 	|      Changed trimCreatorBean to erxTrimCreatorBean 
==============================================================================================================================================
*/
@WebServlet(urlPatterns = { "*.ajaxerx" }, loadOnStartup = 5)
public class ErxAjaxServlet extends HttpServlet {

	private static final String TRIMns = "urn:tolven-org:trim:4.0";

	private EPrescriptionLocal epBean;
	@EJB
	private CreatorLocal creatorBean;
	@EJB
	private DocumentLocal docBean;
	@EJB
	private DocProtectionLocal docProtectionBean;
	@EJB
	private FDBInterface fdbBean;
	@EJB
	private MenuLocal menuBean;
	@EJB
	private TrimMessageLocal trimMsgBean;
	@EJB
	private TrimLocal trimBean;
	@EJB
	private ErxTrimCreatorLocal erxTrimCreatorBean;
	@EJB
	private TolvenPropertiesLocal tolvenProperties;

    private static TrimMarshaller trimMarshaller;

	/*@EJB
	private CCHITLocal cchitLocal;*/
	
	@EJB
    private TolvenPropertiesLocal propertyBean;
	
	private List<MenuPath> contextList;
	private String contextPath;
	private ActRelationship relationMedicationDetails;
	private ActRelationship relationToSureScripts;
	private ActRelationship relationPharmacy;
	private TrimEx prescriptionTrim;
	private String context;
	private String source = null;
	private String templateId = "";

	/**
	 * Init Method
	 */
	public void init(ServletConfig config) throws ServletException {

	}

	/**
	 * doGet Method
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			String uri = req.getRequestURI();
			AccountUser activeAccountUser = (AccountUser) req.getAttribute(GeneralSecurityFilter.ACCOUNTUSER);
			resp.setContentType("text/xml");
			resp.setCharacterEncoding("UTF-8");
			resp.setHeader("Cache-Control", "no-cache");
			Writer writer = resp.getWriter();
			Date now = (Date) req.getAttribute("tolvenNow");
			String element = req.getParameter("element");
			String action = "";
			String status = "";
			TrimFactory trimFactory = new TrimFactory();
		       String keyAlgorithm = propertyBean.getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
		        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
		        PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);
			if (uri.endsWith("instantiateTrim.ajaxerx")) {
				try {
					String messageId = req.getParameter("messageId");
					if (null != messageId) {
						action = req.getParameter("action");
						status = req.getParameter("status");
						String accountType = activeAccountUser.getAccount().getAccountType().getKnownType();
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("context", req.getParameter("context"));
						if (accountType.equals("echr"))
							params.put("path", accountType+ ":patient:medications:active");
						else
							params.put("path", accountType+ ":patient:medications:current");

						params.put("accountId", activeAccountUser.getAccount().getId());
						params.put("tolvenNow", new Date());
						params.put("accountUser", activeAccountUser);
						EPAction epAction = new EPAction();
						List<Map<String, Object>> medicationList = epAction.getAllMenuDataList(params);
						Map<String, Object> sMap;
						if (null != medicationList && !medicationList.isEmpty()) {
							for (Object myElement : medicationList) {
								sMap = (Map<String, Object>) myElement;
								if ((sMap.get("long04").toString()).equals(messageId)) {
									if (action != null && status != null) {
										// Modify the medication.
										if (action.equals("reviseActive") && status.equals("0")) {
											String menuElement = sMap.get("path").toString();
											if (menuElement == null)
												throw new IllegalArgumentException("Missing element in TRIM request");
											if (action == null)
												throw new IllegalArgumentException("Missing action in TRIM transition request");
											TolvenLogger.info("[instantiateTrim] account="+ activeAccountUser.getAccount().getId()+ ", element="+ menuElement+ ", action: "+ action,ErxAjaxServlet.class);
											MenuData mdPrior = menuBean.findMenuDataItem(activeAccountUser.getAccount().getId(),menuElement);
											MenuData mdNew = creatorBean.createTRIMEvent(mdPrior,activeAccountUser,	action, now,userPrivateKey);
											writer.write(mdPrior.getPath());
											writer.write(",");
											writer.write(mdNew.getPath());
											req.setAttribute("activeWriter",writer);
											return;
										}
										// Discontinue the medication.
										else if (action.equals("reviseActive")&& status.equals("1")) {
											String menuElement = sMap.get("path").toString();
											if (menuElement == null)
												throw new IllegalArgumentException("Missing element in TRIM request");
											if (action == null)
												throw new IllegalArgumentException("Missing action in TRIM transition request");
											TolvenLogger.info("[instantiateTrim] account="+ activeAccountUser.getAccount().getId()
																	+ ", element="+ menuElement+ ", action: "
																	+ action,ErxAjaxServlet.class);
											MenuData mdPrior = menuBean
													.findMenuDataItem(activeAccountUser
																	.getAccount()
																	.getId(),
															menuElement);
											MenuData mdNew = creatorBean
													.createTRIMEvent(mdPrior,
															activeAccountUser,
															action, now,userPrivateKey);

											DocXML docXML = (DocXML) getDocBean()
													.findDocument(
															mdNew
																	.getDocumentId());
											String trimString = getDocProtectionBean()
													.getDecryptedContentString(
															docXML,
															activeAccountUser,userPrivateKey);
											TrimEx trim = (TrimEx) getTrimMarshaller()
													.unmarshal(TRIMns,
															new StringReader(
																	trimString));
											if (trim
													.getName()
													.equals(
															"obs/evn/patientPrescription")) {
												Date date = new Date();
												DateFormat df = new SimpleDateFormat(
														"yyyyMMdd");
												((ActEx) trim.getAct())
														.getRelationship()
														.get(
																"medicationDetails")
														.getAct()
														.getEffectiveTime()
														.getIVLTS()
														.getHigh()
														.getTS()
														.setValue(
																df.format(date));
												ValueSet statusSet = trim
														.getValueSet().get(
																"status");

												StringBuffer plain = new StringBuffer();

												plain.append(((ActEx) trim
														.getAct())
														.getRelationship()
														.get("toSureScripts")
														.getAct()
														.getObservation()
														.getValues().get(0)
														.getST().getValue());
												plain.append(((ActEx) trim
														.getAct())
														.getRelationship()
														.get("toSureScripts")
														.getAct()
														.getObservation()
														.getValues().get(1)
														.getST().getValue());

												plain.append(((ActEx) trim
														.getAct())
														.getRelationship()
														.get("toSureScripts")
														.getAct()
														.getObservation()
														.getValues().get(9)
														.getST().getValue());
												plain
														.append(((ActEx) trim
																.getAct())
																.getRelationship()
																.get(
																		"medicationDetails")
																.getAct()
																.getEffectiveTime()
																.getIVLTS()
																.getHigh()
																.getTS()
																.getValue());

												plain
														.append(((ActEx) trim
																.getAct())
																.getRelationship()
																.get(
																		"medicationDetails")
																.getAct()
																.getEffectiveTime()
																.getIVLTS()
																.getLow()
																.getTS()
																.getValue());
												plain
														.append(((ActEx) trim
																.getAct())
																.getRelationship()
																.get(
																		"medicationDetails")
																.getAct()
																.getObservation()
																.getValues()
																.get(11)
																.getTS()
																.getValue());

												plain.append(System
														.currentTimeMillis());
												String msgId = MessageID
														.generateMessageId(new String(
																plain));
												((ActEx) trim.getAct())
														.getRelationship()
														.get("toSureScripts")
														.getAct()
														.getObservation()
														.getValues().get(19)
														.setST(getST(msgId));

												String discontinuedStatus = "Discontinued";
												List<Object> values = statusSet
														.getBindsAndADSAndCDS();
												for (Object value : values) {
													CE ce = (CE) value;
													if (ce
															.getDisplayName()
															.equals(
																	discontinuedStatus)) {
														((ActEx) trim.getAct())
																.getRelationship()
																.get(
																		"medicationDetails")
																.getAct()
																.getObservation()
																.getValues()
																.get(0).setCE(
																		ce);
													}
												}
												creatorBean.marshalToDocument(
														trim, docXML);
												writer.write(mdPrior.getPath());
												writer.write(",");
												writer.write(mdNew.getPath());
											} else if (trim
													.getName()
													.equals(
															"obs/evn/currentMedicationDetailsForRefillRequest")) {
												// TODO making medication
												// inactive
												Date date = new Date();
												DateFormat df = new SimpleDateFormat(
														"yyyyMMdd");
												((ActEx) trim.getAct())
														.getRelationship()
														.get(
																"CurrentMedication")
														.getAct()
														.getObservation()
														.getValues()
														.get(5)
														.getTS()
														.setValue(
																df.format(date));

												StringBuffer plain = new StringBuffer();
												plain.append("Patient Name");
												plain.append("Prescriber Name");
												plain.append("Clinic Name");
												plain
														.append(((ActEx) trim
																.getAct())
																.getRelationship()
																.get(
																		"CurrentMedication")
																.getAct()
																.getObservation()
																.getValues()
																.get(5).getTS()
																.getValue());
												plain
														.append(((ActEx) trim
																.getAct())
																.getRelationship()
																.get(
																		"CurrentMedication")
																.getAct()
																.getObservation()
																.getValues()
																.get(4).getTS()
																.getValue());
												plain
														.append(((ActEx) trim
																.getAct())
																.getRelationship()
																.get(
																		"CurrentMedication")
																.getAct()
																.getObservation()
																.getValues()
																.get(1).getTS()
																.getValue());
												plain.append(System
														.currentTimeMillis());
												String msgId = MessageID
														.generateMessageId(new String(
																plain));
												((ActEx) trim.getAct())
														.getRelationship()
														.get(
																"CurrentMedication")
														.getAct()
														.getObservation()
														.getValues().get(5)
														.setST(getST(msgId));

												((ActEx) trim.getAct())
														.getRelationship()
														.get(
																"CurrentMedication")
														.getAct()
														.getObservation()
														.getValues().get(10)
														.getST().setValue(
																"Discontinued");
												String patientId = ((ActEx) trim
														.getAct())
														.getRelationship()
														.get(
																"CurrentMedication")
														.getAct()
														.getObservation()
														.getValues().get(13)
														.getST().getValue();
												erxTrimCreatorBean
														.submitTrim(
																trim,
																patientId
																		+ ":medications:inactive",
																activeAccountUser,
																now,userPrivateKey);
												writer.write("shared");
											} else if (trim
													.getName()
													.equals(
															"obs/evn/medicationHistory")) {
												long prescriberOrderNumber = Long
														.parseLong(messageId);
												String discontinuedStatus = "Discontinued";
												List<ActRelationship> meds = ((ActEx) trim
														.getAct())
														.getRelationship().get(
																"medications")
														.getAct()
														.getRelationships();
												ValueSet statusSet = trim
														.getValueSet().get(
																"status");
												List<Object> values = statusSet
														.getBindsAndADSAndCDS();

												for (ActRelationship med : meds) {
													try {
														long orderNo = med
																.getAct()
																.getObservation()
																.getValues()
																.get(10)
																.getINT()
																.getValue();
														System.out
																.println("prescriberOrderNumber: "
																		+ Long
																				.toString(prescriberOrderNumber)
																		+ " orderNo: "
																		+ Long
																				.toString(orderNo));
														if (prescriberOrderNumber == orderNo) {
															TolvenLogger
																	.info(
																			"&&&&&&&&& Moving "
																					+ ((ActEx) med
																							.getAct())
																							.getObservation()
																							.getValues()
																							.get(
																									0)
																							.getST()
																							.getValue()
																					+ "to inactive list &&&&&&",
																			this
																					.getClass());
															((ActEx) med
																	.getAct())
																	.getObservation()
																	.getValues()
																	.get(7)
																	.getST()
																	.setValue(
																			"Y");
															TolvenLogger
																	.info(
																			"Set DC to Y",
																			this
																					.getClass());
															((ActEx) med
																	.getAct())
																	.setStatusCodeValue("inactive");
															TolvenLogger
																	.info(
																			"Set Status code to INACTIVE",
																			this
																					.getClass());
															ObservationValueSlot statusSlot = null;
															if (med
																	.getAct()
																	.getObservation()
																	.getValues()
																	.get(11)
																	.getLabel()
																	.getValue()
																	.equals(
																			"Status")) {
																statusSlot = med
																		.getAct()
																		.getObservation()
																		.getValues()
																		.get(11);
															} else if (med
																	.getAct()
																	.getObservation()
																	.getValues()
																	.get(12)
																	.getLabel()
																	.getValue()
																	.equals(
																			"Status")) {
																statusSlot = med
																		.getAct()
																		.getObservation()
																		.getValues()
																		.get(12);
															}
															statusSlot
																	.getCE()
																	.setDisplayName(
																			"Discontinued");
															statusSlot
																	.getCE()
																	.setCode(
																			"C0024334");
															TolvenLogger
																	.info(
																			"Set Status to Discontinued",
																			this
																					.getClass());
														}
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
												String patientId = menuElement
														.split(":")[0]
														+ ":"
														+ menuElement
																.split(":")[1];
												erxTrimCreatorBean
														.submitTrim(
																trim,
																patientId
																		+ ":medications:inactive",
																activeAccountUser,
																now,userPrivateKey);
												writer.write("shared");
											}
											req.setAttribute("activeWriter",
													writer);
											return;
										}
									}
									// Administer the medication. (Manual Refill
									// also)
									else {
										String templateId = req
												.getParameter("templateId");
										if (templateId == null)
											throw new IllegalArgumentException(
													"Instantiation request has missing templateId");
										context = req.getParameter("context")
												+ ":menu";
										if (context == null)
											throw new IllegalArgumentException(
													"Instantiation request has missing context");
										String source = req
												.getParameter("source");
										if (source != null
												&& source.length() == 0) {
											source = null;
										}
										MenuData md;
										TolvenLogger.info(
												"[ErxAjaxServlet] ms="
														+ templateId
														+ " context: "
														+ context,
												ErxAjaxServlet.class);
										md = creatorBean.createTRIMPlaceholder(
												activeAccountUser, templateId,
												context, now, source);
										TolvenLogger.info("Create new event:"
												+ md.getPath(),
												ErxAjaxServlet.class);
										writer.write(md.getPath());
										req
												.setAttribute("activeWriter",
														writer);
										return;
									}
								}
							}
						}
					}// MessageId Null Check
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (uri.endsWith("instantiateLocationTrim.ajaxerx")) {

				/* This is to instantiate the location trim */
				String messageId = req.getParameter("messageId");
				action = "reviseActive";
				String accountType = activeAccountUser.getAccount()
						.getAccountType().getKnownType();
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("context", req.getParameter("context"));

				/* path needs to be changed */
				params.put("path", accountType
						+ ":admin:surescriptsPrescriberRegistration:all");
				params.put("accountId", activeAccountUser.getAccount().getId());
				params.put("tolvenNow", new Date());
				params.put("accountUser", activeAccountUser);
				EPAction epAction = new EPAction();
				List<Map<String, Object>> prescriberList = epAction
						.getAllMenuDataList(params);
				Map<String, Object> sMap;
				if (!prescriberList.isEmpty()) {
					for (Object myElement : prescriberList) {
						sMap = (Map<String, Object>) myElement;
						if (sMap.get("string05").equals(messageId)) {
							String templateId = req.getParameter("templateId");
							if (templateId == null)
								throw new IllegalArgumentException(
										"Instantiation request has missing templateId");
							String context = req.getParameter("context")
									+ ":menu";
							if (context == null)
								throw new IllegalArgumentException(
										"Instantiation request has missing context");
							String source = req.getParameter("source");
							if (source != null && source.length() == 0) {
								source = null;
							}

							/** ****************************************************************************************** */
							/* need to capture the submitted prescriber trim */

							String menuElement = sMap.get("path").toString();
							if (menuElement == null)
								throw new IllegalArgumentException(
										"Missing element in TRIM request");
							if (action == null)
								throw new IllegalArgumentException(
										"Missing action in TRIM transition request");
							TolvenLogger.info("[wizTransition] account="
									+ activeAccountUser.getAccount().getId()
									+ ", element=" + menuElement + ", action: "
									+ action, ErxAjaxServlet.class);
							MenuData mdPrior = menuBean.findMenuDataItem(
									activeAccountUser.getAccount().getId(),
									menuElement);
							MenuData mdNew = creatorBean.createTRIMEvent(
									mdPrior, activeAccountUser, action, now,userPrivateKey);
							DocXML docXMLPrescriber = (DocXML) getDocBean()
									.findDocument(mdNew.getDocumentId());

							String trimStringPrescriber = getDocProtectionBean()
									.getDecryptedContentString(
											docXMLPrescriber, activeAccountUser,userPrivateKey);
							TrimEx trimPrescriber = (TrimEx) getTrimMarshaller().unmarshal(
									TRIMns, new StringReader(
											trimStringPrescriber));

							/* need to capture the submitted prescriber trim */

							/** ****************************************************************************************** */

							/* location trim */
							MenuData md;

							TolvenLogger.info("[ErxAjaxServlet] ms="
									+ templateId + " context: " + context,
									ErxAjaxServlet.class);
							md = creatorBean.createTRIMPlaceholder(
									activeAccountUser, templateId, context,
									now, source);

							DocXML docXML = (DocXML) getDocBean().findDocument(
									md.getDocumentId());

							String trimString = getDocProtectionBean()
									.getDecryptedContentString(docXML,
											activeAccountUser,userPrivateKey);
							TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
									new StringReader(trimString));
							// ActRelationship ar =
							// ((ActEx)trim.getAct()).getRelationship().get(path);
							// ar.getAct().getRelationships().addAll(newTrim.getAct().getRelationships());
							trim.getAct().getRelationships().addAll(
									trimPrescriber.getAct().getRelationships());
							//						
							/* location trim */
							creatorBean.marshalToDocument(trim, docXML);
							TolvenLogger.info("Create new event:"
									+ md.getPath(), ErxAjaxServlet.class);
							writer.write(md.getPath());
							req.setAttribute("activeWriter", writer);
							return;
						}
					}
				}

			} else if (uri.endsWith("overRideCommentsReq.ajaxerx")) {
				String comments = req.getParameter("comments");
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));

				trim.getAct().getRelationships().get(8).getAct()
						.getObservation().getValues().get(6).getST().setValue(
								"true");

				trim.getAct().getRelationships().get(8).getAct()
						.getObservation().getValues().get(5).getST().setValue(
								comments);

				creatorBean.marshalToDocument(trim, docXML);
				return;

			} else if (uri.endsWith("setRequestedMedicineValueSet.ajaxerx")) {
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				ArrayList<String> reqMedicines = new ArrayList<String>();

				reqMedicines.add(0, trim.getAct().getRelationships().get(5)
						.getAct().getObservation().getValues().get(0).getST()
						.getValue()); // extracted medicine 1
				reqMedicines.add(1, trim.getAct().getRelationships().get(6)
						.getAct().getObservation().getValues().get(0).getST()
						.getValue()); // extracted medicine 2
				reqMedicines.add(2, trim.getAct().getRelationships().get(7)
						.getAct().getObservation().getValues().get(0).getST()
						.getValue()); // extracted medicine 3

				((CE) trim.getValueSets().get(2).getBindsAndADSAndCDS().get(1))
						.setDisplayName(reqMedicines.get(0));
				((CE) trim.getValueSets().get(2).getBindsAndADSAndCDS().get(2))
						.setDisplayName(reqMedicines.get(1));
				((CE) trim.getValueSets().get(2).getBindsAndADSAndCDS().get(3))
						.setDisplayName(reqMedicines.get(2));

				creatorBean.marshalToDocument(trim, docXML);
				req.setAttribute("success", writer);
				return;
			} else if (uri.endsWith("gatherRefillQuantity.ajaxerx")) {
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				String drugname = req.getParameter("drug_name");
				if (null != drugname && drugname.trim().length() > 0
						&& !drugname.equals("Select")) {
					drugname = drugname.replace('_', '%');

					List<ActRelationship> relationships = trim.getAct()
							.getRelationships();
					for (ActRelationship relationship : relationships) {
						if (drugname.equals(relationship.getAct()
								.getObservation().getValues().get(0).getST()
								.getValue().toString())) {
							writer.write(relationship.getAct().getObservation()
									.getValues().get(1).getINT().getValue()
									+ "|");
							writer.write(relationship.getAct().getObservation()
									.getValues().get(2).getINT().getValue()
									+ "|");
							writer.write(relationship.getAct().getObservation()
									.getValues().get(3).getST().getValue()
									+ "");
							break;
						}
					}
				} else {
					writer.write("" + "|" + "" + "|" + "");
				}
				req.setAttribute("success", writer);
				return;
			} else if (uri.endsWith("gatherPatientInformation.ajaxerx")) {
				String pharmacyNCPCPID = null;
				String pharmacyName = null;
				String pharmacyAddressLine1 = null;
				String pharmacyCity = null;
				String pharmacyState = null;
				String pharmacyZipCode = null;
				String pharmacyTE = null;
				String pharmacyFX = null;
				String medicationRequested = null;

				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));

				ActRelationshipEx patientEx = (ActRelationshipEx) ((ActEx) trim
						.getAct()).getRelationship().get("patient");
				ActRelationshipEx responseEx = (ActRelationshipEx) ((ActEx) trim
						.getAct()).getRelationship().get("response");
				ActRelationshipEx headerEx = (ActRelationshipEx) ((ActEx) trim
						.getAct()).getRelationship().get("header");
				ActRelationshipEx pharmacyEx = (ActRelationshipEx) ((ActEx) trim
						.getAct()).getRelationship().get("pharmacy");
				ActRelationshipEx medicationEx = (ActRelationshipEx) ((ActEx) trim
						.getAct()).getRelationship()
						.get("MedicationPrescribed");
				if (responseEx.getAct().getObservation().getValues().get(0)
						.getCE().getDisplayName().equals(
								"Denied New Prescription")) {
					String path = trim.getTolvenEventIds().get(0).getPath()
							.toString();
					String patientId = patientEx.getAct().getObservation()
							.getValues().get(5).getST().getValue();
					String rxReferenceNumber = headerEx.getAct()
							.getObservation().getValues().get(3).getST()
							.getValue();

					for (ObservationValueSlot pharmacyOvs : pharmacyEx.getAct()
							.getObservation().getValues()) {
						if (pharmacyOvs.getLabel().getValue().trim().equals(
								"NCPDPID")) {
							pharmacyNCPCPID = pharmacyOvs.getST().getValue();
						} else if (pharmacyOvs.getLabel().getValue().trim()
								.equals("StoreName")) {
							pharmacyName = pharmacyOvs.getST().getValue();
						} else if (pharmacyOvs.getLabel().getValue().trim()
								.equals("AddressLine1")) {
							pharmacyAddressLine1 = pharmacyOvs.getST()
									.getValue();
						} else if (pharmacyOvs.getLabel().getValue().trim()
								.equals("City")) {
							pharmacyCity = pharmacyOvs.getST().getValue();
						} else if (pharmacyOvs.getLabel().getValue().trim()
								.equals("State")) {
							pharmacyState = pharmacyOvs.getST().getValue();
						} else if (pharmacyOvs.getLabel().getValue().trim()
								.equals("ZipCode")) {
							pharmacyZipCode = pharmacyOvs.getST().getValue();
						} else if (pharmacyOvs.getLabel().getValue().trim()
								.equals("TE")) {
							pharmacyTE = pharmacyOvs.getST().getValue();
						} else if (pharmacyOvs.getLabel().getValue().trim()
								.equals("FX")) {
							pharmacyFX = pharmacyOvs.getST().getValue();
						}
					}
					for (ObservationValueSlot medicationOvs : medicationEx
							.getAct().getObservation().getValues()) {
						if (medicationOvs.getLabel().getValue().trim().equals(
								"DrugDescription")) {
							medicationRequested = medicationOvs.getST()
									.getValue();
						}
					}

					writer.write(path + "|" + patientId + "|"
							+ rxReferenceNumber + "|" + pharmacyName + "|"
							+ pharmacyNCPCPID + "|" + medicationRequested + "|"
							+ pharmacyAddressLine1 + "|" + pharmacyCity + "|"
							+ pharmacyState + "|" + pharmacyZipCode + "|"
							+ pharmacyTE + "|" + pharmacyFX);
					TolvenLogger
							.info(
									"[REFILL REQUEST] Refill Request Denied with NewRx to Follow.",
									ErxAjaxServlet.class);
				}
				req.setAttribute("success", writer);
				return;
			} else if (uri.endsWith("updateRefill.ajaxerx")) {
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);

				/* RefillRequest Trim */
				TrimEx refillRequestTrim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				CE responseStatus = ((ActEx) refillRequestTrim.getAct())
						.getRelationship().get("response").getAct()
						.getObservation().getValues().get(0).getCE();

				/* Prescription Trim */
				Long prescriberOrderNo = null;
				if (((ActEx) refillRequestTrim.getAct()).getRelationship().get(
						"header").getAct().getObservation().getValues().get(4)
						.getST().getValue() != "") {
					prescriberOrderNo = Long.valueOf(((ActEx) refillRequestTrim
							.getAct()).getRelationship().get("header").getAct()
							.getObservation().getValues().get(4).getST()
							.getValue());

					MenuData mdi = menuBean.findMenuDataItem(prescriberOrderNo);
					
					if (mdi != null){
						DocXML docXMLFrom = (DocXML) docBean.findDocument(mdi.getDocumentId());
						TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(docXMLFrom, activeAccountUser,userPrivateKey);
						
						prescriptionTrim = trim;
					} else
						prescriptionTrim = null;
					/*prescriptionTrim = cchitLocal.findTrimData(prescriberOrderNo,
							activeAccountUser,userPrivateKey);*/
				}
				relationMedicationDetails = prescriptionTrim.getAct()
						.getRelationships().get(1);
				MenuData mdNew = null;
				if (responseStatus.toString().equals("Approved")) {
					mdNew = epBean.changeNumberOfRefills(prescriberOrderNo
							.toString(), ((ActEx) refillRequestTrim.getAct())
							.getRelationship().get("MedicationRequested")
							.getAct().getObservation().getValues().get(0)
							.getINT().getValue() - 1);
					/*
					 * prescriptionTrim.getAct()
					 * .getRelationships().get(1).getAct().getObservation().getValues().get(4)
					 * .getINT().setValue(
					 * ((ActEx)refillRequestTrim.getAct()).getRelationship().get("MedicationRequested").getAct()
					 * .getObservation().getValues().get(0).getINT()
					 * .getValue()-1);
					 */
				} else if (responseStatus.toString().equals("Denied")) {
					mdNew = epBean.changeNumberOfRefills(prescriberOrderNo
							.toString(), new Long(0));
					/*
					 * prescriptionTrim.getAct()
					 * .getRelationships().get(1).getAct().getObservation().getValues().get(4)
					 * .getINT().setValue(0);
					 */
				}
				if (null != mdNew)
					creatorBean.submit(mdNew, activeAccountUser,userPrivateKey);
				req.setAttribute("success", writer);
				return;
			} else if (uri.endsWith("setResponseField.ajaxerx")) {
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);

				/* RefiilRequest Trim */
				TrimEx refillRequestTrim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));

				Long priorRefill = null;
				String priorRefillQualifier = "";
				/* Obtaining priorRefill */
				ActRelationship medicationPrescribedRelation = ((ActEx) refillRequestTrim
						.getAct()).getRelationship()
						.get("MedicationPrescribed");
				if (medicationPrescribedRelation.getAct().getObservation()
						.getValues() != null) {
					List<ObservationValueSlot> medicationPrescribedValues = medicationPrescribedRelation
							.getAct().getObservation().getValues();
					for (int i = 0; i < medicationPrescribedValues.size(); i++) {
						if (!medicationPrescribedValues.get(i).getLabel()
								.getValue().equals(null)) {
							if (medicationPrescribedValues.get(i).getLabel()
									.getValue().equals("Refills(value)")) {
								priorRefill = Long
										.valueOf(medicationPrescribedValues
												.get(i).getST().getValue());
							}
							if (medicationPrescribedValues.get(i).getLabel()
									.getValue().equals("Refills(qualifier)")) {
								priorRefillQualifier = medicationPrescribedValues
										.get(i).getST().getValue();
							}
						}// if label is null
					}// for
				}// if medicationPrescribedobservtationslot null

				CE responseStatus = ((ActEx) refillRequestTrim.getAct())
						.getRelationship().get("response").getAct()
						.getObservation().getValues().get(0).getCE();
				ValueSet statusSet = refillRequestTrim.getValueSet().get(
						"response");
				List<Object> values = statusSet.getBindsAndADSAndCDS();
				if (responseStatus.toString().equals("Approved")) {
					/* Obtaining newRefill */
					Long newRefill = ((ActEx) refillRequestTrim.getAct())
							.getRelationship().get("MedicationRequested")
							.getAct().getObservation().getValues().get(0)
							.getINT().getValue();
					if (priorRefill != newRefill) {
						for (Object value : values) {
							CE ce = (CE) value;
							if (ce.getDisplayName().equals(
									"Approved with Change")) {
								((ActEx) refillRequestTrim.getAct())
										.getRelationship().get("response")
										.getAct().getObservation().getValues()
										.get(0).setCE(ce);
							}
						}
					} else if (priorRefill == 0 || priorRefill == null
							|| priorRefill == newRefill
							|| priorRefillQualifier == "PRN") {
						for (Object value : values) {
							CE ce = (CE) value;
							if (ce.getDisplayName().equals("Approved")) {
								((ActEx) refillRequestTrim.getAct())
										.getRelationship().get("response")
										.getAct().getObservation().getValues()
										.get(0).setCE(ce);
							}
						}
					}
					creatorBean.marshalToDocument(refillRequestTrim, docXML);
					return;
				}

			} else if (uri.endsWith("submitPrescription.ajaxerx")) {
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);

				/* RXChangeRequest Trim */
				TrimEx rxChangeRequestTrim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				CE responseStatus = rxChangeRequestTrim.getAct()
						.getRelationships().get(9).getAct().getObservation()
						.getValues().get(0).getCE();
				if (!responseStatus.toString().equals("Denied")) {
					/* Prescription Trim */
					ArrayList<ActRelationship> responseList = new ArrayList<ActRelationship>();
					templateId = "obs/evn/patientPrescription";
					prescriptionTrim = trimBean.findTrim(templateId);
					System.out
							.println("<<<<<<Obtained New Prescription Trim>>>>>>>>>");
					relationMedicationDetails = prescriptionTrim.getAct()
							.getRelationships().get(1);
					relationToSureScripts = prescriptionTrim.getAct()
							.getRelationships().get(0);
					relationPharmacy = prescriptionTrim.getAct()
							.getRelationships().get(5);

					if (responseStatus.toString().equals("Approved")) {
						// Setting new Medication
						relationToSureScripts.getAct().getObservation()
								.getValues().get(21).getST().setValue(
										rxChangeRequestTrim.getAct()
												.getRelationships().get(10)
												.getAct().getObservation()
												.getValues().get(0).getST()
												.getValue());

						// Setting Dispense Amount
						relationToSureScripts.getAct().getObservation()
								.getValues().get(13).getINT().setValue(
										rxChangeRequestTrim.getAct()
												.getRelationships().get(10)
												.getAct().getObservation()
												.getValues().get(1).getINT()
												.getValue());

						// Setting refill Amount
						relationMedicationDetails.getAct().getObservation()
								.getValues().get(4).getINT().setValue(
										rxChangeRequestTrim.getAct()
												.getRelationships().get(10)
												.getAct().getObservation()
												.getValues().get(2).getINT()
												.getValue());
					} else if (responseStatus.toString().equals(
							"Approved With Change")) {
						// Setting new Medication
						relationToSureScripts.getAct().getObservation()
								.getValues().get(21).getST().setValue(
										rxChangeRequestTrim.getAct()
												.getRelationships().get(8)
												.getAct().getObservation()
												.getValues().get(0).getST()
												.getValue());

						// Setting Dispense Amount
						if (!rxChangeRequestTrim.getAct().getRelationships()
								.get(8).getAct().getObservation().getValues()
								.get(8).getST().getValue().equals(""))
							relationToSureScripts.getAct().getObservation()
									.getValues().get(13).getINT().setValue(
											Long.valueOf(rxChangeRequestTrim
													.getAct()
													.getRelationships().get(8)
													.getAct().getObservation()
													.getValues().get(8).getST()
													.getValue()));

						// Setting refill Amount
						if (!rxChangeRequestTrim.getAct().getRelationships()
								.get(8).getAct().getObservation().getValues()
								.get(9).getST().getValue().equals(""))
							relationMedicationDetails.getAct().getObservation()
									.getValues().get(4).getINT().setValue(
											Long.valueOf(rxChangeRequestTrim
													.getAct()
													.getRelationships().get(8)
													.getAct().getObservation()
													.getValues().get(9).getST()
													.getValue()));

					}
					// Setting order Date as today's date
					Date date = new Date();
					DateFormat df = new SimpleDateFormat("yyyyMMdd");
					relationMedicationDetails.getAct().getObservation()
							.getValues().get(11).getTS().setValue(
									df.format(date));

					// Setting start date
					relationMedicationDetails.getAct().getEffectiveTime()
							.getIVLTS().getLow().getTS().setValue(
									df.format(date));

					// Setting end date
					relationMedicationDetails.getAct().getEffectiveTime()
							.getIVLTS().getHigh().getTS().setValue(
									df.format(date));

					// Setting Pharmacy
					relationPharmacy.getAct().getObservation().getValues().get(
							0).getST().setValue(
							rxChangeRequestTrim.getAct().getRelationships()
									.get(1).getAct().getObservation()
									.getValues().get(0).getST().getValue());

					// Setting status as active
					ValueSet statusSet = prescriptionTrim.getValueSets().get(0);
					List<Object> values = statusSet.getBindsAndADSAndCDS();
					for (Object value : values) {
						CE ce = (CE) value;
						if (ce.getDisplayName().equals("Active")) {
							relationMedicationDetails.getAct().getObservation()
									.getValues().get(0).setCE(ce);
						}
					}

					// Setting SureScriptStatus as Changed
					relationToSureScripts.getAct().getObservation().getValues()
							.get(34).getST().setValue("Changed");
					responseList.add(0, relationToSureScripts);
					responseList.add(1, relationMedicationDetails);
					responseList.add(2, relationPharmacy);

					/* Getting patientId for contextPath */

					ActRelationshipEx relatiEx = (ActRelationshipEx) rxChangeRequestTrim
							.getAct().getRelationships().get(3);
					String lastName = relatiEx.getAct().getObservation()
							.getValues().get(0).getST().getValue().toString();
					String firstName = relatiEx.getAct().getObservation()
							.getValues().get(1).getST().getValue().toString();
					String middleName = relatiEx.getAct().getObservation()
							.getValues().get(2).getST().getValue().toString();
					try {
						Long patientId = epBean.getPatientIdFromMenuData(
								firstName, middleName, lastName);
						System.out.println("<<<<<<<PatientID>>>>>>>>>"
								+ patientId);

						contextPath = "echr:patient-" + patientId
								+ ":medications:active";
					} catch (Exception e) {
						// TODO: handle exception
					}
					MenuData mdResponse = trimMsgBean.createTRIMPlaceholder(
							activeAccountUser, templateId, contextPath,
							new Date(), source, responseList,userPrivateKey);
					
					MenuData mdi = menuBean.findMenuDataItem(mdResponse.getId());
					TrimEx trim = null;
					if (mdi != null){
						DocXML docXMLFrom = (DocXML) docBean.findDocument(mdi.getDocumentId());
						trim = (TrimEx) getTrimMarshaller().unmarshal(docXMLFrom, activeAccountUser,userPrivateKey);
						
					}
					/*TrimEx trim = cchitLocal.findTrimData(mdResponse.getId(),
							activeAccountUser,userPrivateKey);*/
					((ActEx) trim.getAct())
							.getRelationship()
							.get("toSureScripts")
							.getAct()
							.getObservation()
							.getValues()
							.get(31)
							.getINT()
							.setValue(
									Long
											.valueOf(trim.getAct().getId()
													.getIIS().get(0)
													.getExtension().split("-")[2]));
					DocXML docXMLNew = (DocXML) getDocBean().findDocument(
							mdResponse.getDocumentId());
					creatorBean.marshalToDocument(trim, docXMLNew);
					TolvenLogger.info("Create new event:"
							+ mdResponse.getPath(), ErxAjaxServlet.class);
					System.out.println("<<<<<<SureScript Status:>>>>>>"
							+ mdResponse.getString08());
					creatorBean.submit(mdResponse, activeAccountUser,userPrivateKey);
					TolvenLogger.info("<<<<<<<Functionality Over>>>>>>>>>",
							ErxAjaxServlet.class);
				}
				req.setAttribute("success", writer);
				return;
			} else if (uri.endsWith("changeRefillInPrescription.ajaxerx")) {

				// String elt = req.getParameter( "element");
				String actionNew = req.getParameter("action");

				/*-----------------------------------------------------------------*/
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);

				/* RefillRequest Trim */
				TrimEx refillRequestTrim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				CE responseStatus = ((ActEx) refillRequestTrim.getAct())
						.getRelationship().get("response").getAct()
						.getObservation().getValues().get(0).getCE();
				Long refillQuantity = null;
				/* Prescription Trim */
				if (responseStatus.toString().equals("Approved")
						|| responseStatus.toString().equals(
								"Approved with Change")) {
					refillQuantity = ((ActEx) refillRequestTrim.getAct())
							.getRelationship().get("MedicationRequested")
							.getAct().getObservation().getValues().get(0)
							.getINT().getValue() - 1;
				} else if (responseStatus.toString().equals("Denied")) {
					refillQuantity = new Long(0);
				}

				/*-----------------------------------------------------------------*/
				Long prescriberOrderNo = null;
				if (!((ActEx) refillRequestTrim.getAct()).getRelationship()
						.get("header").getAct().getObservation().getValues()
						.get(4).getST().getValue().equals("")) {
					prescriberOrderNo = Long.valueOf(((ActEx) refillRequestTrim
							.getAct()).getRelationship().get("header").getAct()
							.getObservation().getValues().get(4).getST()
							.getValue());
				}
				MenuData mdPrior = menuBean.findMenuDataItem(prescriberOrderNo);
				MenuData mdNew = trimMsgBean.createTRIMEvent(mdPrior,
						activeAccountUser, actionNew, now, refillQuantity,userPrivateKey);
				writer.write(mdPrior.getPath());
				writer.write(",");
				writer.write(mdNew.getPath());
				req.setAttribute("activeWriter", writer);
				return;
			} else if (uri.endsWith("instantiatePrescriptionTrim.ajaxerx")) {
				String templateId = req.getParameter("templateId");
				String pharmacyName = req.getParameter("pharmName");
				String pharmacyNCPDPID = req.getParameter("ncpdpid");
				String medication = req.getParameter("med");
				String pharmacyAddressLine1 = req.getParameter("pharmAdd");
				String pharmacyCity = req.getParameter("city");
				String pharmacyState = req.getParameter("state");
				String pharmacyZipCode = req.getParameter("zip");
				String pharmacyTE = req.getParameter("tel");
				String pharmacyFX = req.getParameter("fax");
				if (templateId == null)
					throw new IllegalArgumentException(
							"Instantiation request has missing templateId");
				// Where called from
				String context = req.getParameter("context");
				if (context == null)
					throw new IllegalArgumentException(
							"Instantiation request has missing context");
				String source = req.getParameter("source");
				if (source != null && source.length() == 0) {
					source = null;
				}
				MenuData md;
				TolvenLogger.info("[ErxAjaxServlet] ms=" + templateId
						+ " context: " + context, ErxAjaxServlet.class);
				md = creatorBean.createTRIMPlaceholder(activeAccountUser,
						templateId, context, now, source);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				trim.getAct().getRelationships().get(0).getAct()
						.getObservation().getValues().get(38).getST().setValue(
								req.getParameter("rxReferenceNumber"));
				((ActEx) trim.getAct()).getRelationship().get("pharmacy")
						.getAct().getObservation().getValues().get(0).getST()
						.setValue(pharmacyNCPDPID); // Setting the id of the
													// pharmacy in the trim
				((ActEx) trim.getAct()).getRelationship().get("pharmacy")
						.getAct().getObservation().getValues().get(1).getST()
						.setValue(pharmacyName); // Setting the name of the
													// pharmacy in the trim
				((ActEx) trim.getAct()).getRelationship().get("pharmacy")
						.getAct().getObservation().getValues().get(2).getST()
						.setValue(pharmacyAddressLine1); // Setting the
															// addressline1 of
															// the pharmacy in
															// the trim
				((ActEx) trim.getAct()).getRelationship().get("pharmacy")
						.getAct().getObservation().getValues().get(3).getST()
						.setValue(pharmacyCity); // Setting the city of the
													// pharmacy in the trim
				((ActEx) trim.getAct()).getRelationship().get("pharmacy")
						.getAct().getObservation().getValues().get(4).getST()
						.setValue(pharmacyState); // Setting the state of the
													// pharmacy in the trim
				((ActEx) trim.getAct()).getRelationship().get("pharmacy")
						.getAct().getObservation().getValues().get(5).getST()
						.setValue(pharmacyZipCode); // Setting the zip code of
													// the pharmacy in the trim
				((ActEx) trim.getAct()).getRelationship().get("pharmacy")
						.getAct().getObservation().getValues().get(6).getST()
						.setValue(pharmacyTE); // Setting the tel of the
												// pharmacy in the trim
				((ActEx) trim.getAct()).getRelationship().get("pharmacy")
						.getAct().getObservation().getValues().get(7).getST()
						.setValue(pharmacyFX); // Setting the fax of the
												// pharmacy in the trim

				trim.getAct().getRelationships().get(0).getAct()
						.getObservation().getValues().get(59).getST().setValue(
								pharmacyName); // Setting the name of the
												// pharmacy for display in the
												// front.
				trim.getAct().getRelationships().get(0).getAct()
						.getObservation().getValues().get(60).getST().setValue(
								medication); // Setting the name of the
												// medication in RefillRequest
				creatorBean.marshalToDocument(trim, docXML);
				TolvenLogger.info("Create new event:" + md.getPath(),
						ErxAjaxServlet.class);
				writer.write(md.getPath());
				req.setAttribute("activeWriter", writer);
				return;
			} else if (uri.endsWith("loadMedicationDetails.ajaxerx")) {

				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				String knownAccountType = activeAccountUser.getAccount()
						.getAccountType().getKnownType().toString();
				MenuData medication;
				if (knownAccountType.equals("echr")) {
					medication = menuBean.findMenuDataItem(Long.valueOf(element
							.split("-")[2].split(":")[0]));
				} else {
					medication = menuBean.findMenuDataItem(Long.valueOf(element
							.split("-")[2].split(":")[0]));
				}

				if (medication != null) {
					if (trim.getName().equals("obs/evn/administration")) {
						if (null != medication.getString07()) {
							trim.getAct().getRelationships().get(0).getAct()
									.getObservation().getValues().get(9)
									.getST().setValue(medication.getString07());
						}
						if (null != medication.getString01()) {
							trim.getAct().getRelationships().get(0).getAct()
									.getObservation().getValues().get(0)
									.getST().setValue(medication.getString01());
						}
						if (null != medication.getString02()) {
							trim.getAct().getRelationships().get(0).getAct()
									.getObservation().getValues().get(1)
									.getST().setValue(medication.getString02());
						}
						if (null != medication.getPqUnits03()) {
							trim.getAct().getRelationships().get(0).getAct()
									.getObservation().getValues().get(2)
									.getST()
									.setValue(medication.getPqUnits03());
						}
						if (null != medication.getString04()) {
							trim.getAct().getRelationships().get(0).getAct()
									.getObservation().getValues().get(3)
									.getST().setValue(medication.getString04());
						}
						if (null != medication.getDate03()) {
							trim.getAct().getRelationships().get(0).getAct()
									.getObservation().getValues().get(4)
									.getST().setValue(
											medication.getDate03().toString());
						}
						if (null != medication.getDate02()) {
							trim.getAct().getRelationships().get(0).getAct()
									.getObservation().getValues().get(5)
									.getST().setValue(
											medication.getDate02().toString());
						}
					} else if (trim.getName().equals("obs/evn/dispense")) {
						((ActEx) trim.getAct()).getRelationship().get(
								"dispense").getAct().getObservation()
								.getValues().get(1).getST().setValue(
										medication.getString01());
						if (medication.getString02() != null)
							((ActEx) trim.getAct()).getRelationship().get(
									"dispense").getAct().getObservation()
									.getValues().get(3).getST().setValue(
											medication.getString02());
						if (medication.getLong01() != null)
							((ActEx) trim.getAct()).getRelationship().get(
									"dispense").getAct().getObservation()
									.getValues().get(4).getINT().setValue(
											medication.getLong01());
						((ActEx) trim.getAct()).getRelationship().get(
								"dispense").getAct().getObservation()
								.getValues().get(8).getST().setValue(
										medication.getDate03().toString());
						((ActEx) trim.getAct()).getRelationship().get(
								"dispense").getAct().getObservation()
								.getValues().get(9).getST().setValue(
										medication.getDate02().toString());
						((ActEx) trim.getAct()).getRelationship().get(
								"dispense").getAct().getObservation()
								.getValues().get(5).getST().setValue(
										medication.getString04());
						if (medication.getLong02() != null)
							((ActEx) trim.getAct()).getRelationship().get(
									"dispense").getAct().getObservation()
									.getValues().get(7).getINT().setValue(
											medication.getLong02());
						if (medication.getPqUnits03() != null)
							((ActEx) trim.getAct()).getRelationship().get(
									"dispense").getAct().getObservation()
									.getValues().get(6).getST().setValue(
											medication.getPqUnits03());
					} else if (trim.getName()
							.equals("obs/evn/medicationRefill")) {
						/* Setting Medication Name */
						if (medication.getString01() != null) {
							((ActEx) trim.getAct()).getRelationship().get(
									"medicationDetails").getAct()
									.getObservation().getValues().get(0)
									.getST().setValue(medication.getString01());
						}
						/* Setting Strength */
						if (medication.getString02() != null) {
							((ActEx) trim.getAct()).getRelationship().get(
									"medicationDetails").getAct()
									.getObservation().getValues().get(1)
									.getST().setValue(medication.getString02());
						}
						/* Setting Dose */
						if (medication.getString03() != null) {
							((ActEx) trim.getAct()).getRelationship().get(
									"medicationDetails").getAct()
									.getObservation().getValues().get(2)
									.getST().setValue(medication.getString03());
						}
						/* Setting Frequency */
						if (medication.getLong01() != null) {
							((ActEx) trim.getAct()).getRelationship().get(
									"medicationDetails").getAct()
									.getObservation().getValues().get(3)
									.getINT().setValue(medication.getLong01());

						}
						/* Setting Start Date */
						if (medication.getDate02() != null) {
							((ActEx) trim.getAct()).getRelationship().get(
									"medicationDetails").getAct()
									.getObservation().getValues().get(4)
									.getST().setValue(
											medication.getDate02().toString()
													.split(" ")[0].trim());

						}
						/* Setting End Date */
						if (medication.getDate03() != null) {
							((ActEx) trim.getAct()).getRelationship().get(
									"medicationDetails").getAct()
									.getObservation().getValues().get(5)
									.getST().setValue(
											medication.getDate03().toString()
													.split(" ")[0].trim());
						}
						/* Setting Route */
						if (medication.getString04() != null) {
							((ActEx) trim.getAct()).getRelationship().get(
									"medicationDetails").getAct()
									.getObservation().getValues().get(6)
									.getST().setValue(medication.getString04());
						}
						/* Setting Dispense Amount */
						if (medication.getPqUnits04() != null) {
							((ActEx) trim.getAct()).getRelationship().get(
									"medicationDetails").getAct()
									.getObservation().getValues().get(7)
									.getST()
									.setValue(medication.getPqUnits04());
						}
						/* Setting Refills */
						if (medication.getLong03() != null) {
							((ActEx) trim.getAct()).getRelationship().get(
									"medicationDetails").getAct()
									.getObservation().getValues().get(8)
									.getST()
									.setValue(medication.getPqUnits03());
						}
					}

				}
				creatorBean.marshalToDocument(trim, docXML);
				return;
			} else if (uri.endsWith("getMedicationDetails.ajaxerx")) {
				String pod = req.getParameter("pod");
				menuBean.findMenuDataItem(Long.parseLong(pod));

			} else if (uri.endsWith("displayErrorIfFoundWrong.ajaxerx")) {
				if (null != req.getParameter("days")
						&& !req.getParameter("days").equals("")) {
					Integer days = Integer.parseInt(req.getParameter("days"));
					try {
						String startDate = req.getParameter("start");
						String endDate = req.getParameter("end");
						SimpleDateFormat sdf = new SimpleDateFormat(
								"MM/dd/yyyy");
						Calendar c = Calendar.getInstance();
						c.setTime(sdf.parse(startDate));
						if (days > 1) {
							c.add(Calendar.DATE, (days - 1)); // number of
																// days to add
						}
						Calendar c1 = Calendar.getInstance();
						c1.setTime(sdf.parse(endDate));
						if (c.getTime().equals(c1.getTime())) {
							writer.write("true");
						} else {
							writer.write("false");
						}

					} catch (Exception e) {
						writer.write("Error");
						req.setAttribute("valid", writer);
						return;
					}
				} else {
					writer.write("false");
				}
				req.setAttribute("valid", writer);
				return;
			} else if (uri
					.endsWith("submitMedicationDetailsInBackground.ajaxerx")) {
				try {
					MenuData md = menuBean.findMenuDataItem(activeAccountUser
							.getAccount().getId(), element);
					DocXML docXML = (DocXML) getDocBean().findDocument(
							md.getDocumentId());
					String trimString = this.getDocProtectionBean()
							.getDecryptedContentString(docXML,
									activeAccountUser,userPrivateKey);
					TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
							new StringReader(trimString));
					String patientId = req.getParameter("patientId");
					if (patientId.length() > 0) {
						TolvenLogger
								.info(
										"PATIENT ID MISSING, Unable to Assosiate the Medication with Patient",
										ErxAjaxServlet.class);
					}

					List<ObservationValueSlot> medicationPrescribed = ((ActEx) trim
							.getAct()).getRelationship().get(
							"MedicationPrescribed").getAct().getObservation()
							.getValues();

					List<ObservationValueSlot> medicationRequested = ((ActEx) trim
							.getAct()).getRelationship().get(
							"MedicationRequested").getAct().getObservation()
							.getValues();

					List<ObservationValueSlot> header = ((ActEx) trim.getAct())
							.getRelationship().get("header").getAct()
							.getObservation().getValues();

					List<ObservationValueSlot> prescriber = ((ActEx) trim
							.getAct()).getRelationship().get("prescriber")
							.getAct().getObservation().getValues();

					List<ObservationValueSlot> response = ((ActEx) trim
							.getAct()).getRelationship().get("response")
							.getAct().getObservation().getValues();

					List<ObservationValueSlot> patient = ((ActEx) trim.getAct())
							.getRelationship().get("patient").getAct()
							.getObservation().getValues();

					List<ObservationValueSlot> originalPrescription = ((ActEx) trim
							.getAct()).getRelationship().get(
							"originalPrescription").getAct().getObservation()
							.getValues();

					if (null != header.get(4).getST()
							&& null != header.get(4).getST().getValue()
							&& !header.get(4).getST().getValue().equals("")) {
						MenuData mdMedicine = epBean
								.getMenuDataFromPrescriberOrderNumber(header
										.get(4).getST().getValue());
						if (!originalPrescription
								.get(5)
								.getST()
								.getValue()
								.equals(
										"No Prescriptions with the Mentioned PON found in the Account.")) {
							try {
								TrimEx templateTrim = erxTrimCreatorBean
										.createTrim(
												activeAccountUser,
												"obs/evn/currentMedicationDetailsForRefillRequest",
												patientId + ":medications:all",
												now);
								if (null != header.get(4).getST())
									templateTrim.getAct().getRelationships()
											.get(0).getAct().getObservation()
											.getValues().get(0).getST()
											.setValue(
													String.valueOf(mdMedicine
															.getId())); // Prescriber
																		// Order
																		// Number
																		// is
																		// set.

								if (null != header.get(7).getTS())
									templateTrim
											.getAct()
											.getRelationships()
											.get(0)
											.getAct()
											.getObservation()
											.getValues()
											.get(1)
											.setTS(
													trimFactory
															.createNewTS(mdMedicine
																	.getDate03())); // Date
																					// of
																					// Prescription

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(2).getST().setValue(
												medicationPrescribed.get(0)
														.getST().getValue()); // Medication
																				// is
																				// set

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(3).getST().setValue(
												mdMedicine.getString02()); // Strength
																			// is
																			// set

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(4).setTS(
												trimFactory
														.createNewTS(mdMedicine
																.getDate01())); // Start
																				// Date
																				// is
																				// set

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(5).setTS(
												trimFactory
														.createNewTS(mdMedicine
																.getDate02())); // End
																				// Date
																				// is
																				// set

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(6).getST().setValue(
												mdMedicine.getString04()); // Route
																			// is
																			// set

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(7).getST().setValue(
												mdMedicine.getPqUnits04()); // Dispense
																			// Amount
																			// is
																			// set

								if (medicationRequested.get(2).getCE()
										.getDisplayName().equals("R")) {
									templateTrim
											.getAct()
											.getRelationships()
											.get(0)
											.getAct()
											.getObservation()
											.getValues()
											.get(8)
											.getST()
											.setValue(
													medicationRequested.get(0)
															.getST().getValue()); // Refills
																					// is
																					// set
								} else {
									templateTrim.getAct().getRelationships()
											.get(0).getAct().getObservation()
											.getValues().get(8).getST()
											.setValue("PRN"); // Refills is
																// set
								}

								String prescriberName = "";
								if (null != mdMedicine.getParent04()
										.getString02()
										&& !mdMedicine.getParent04()
												.getString02().equals(""))
									prescriberName = prescriberName
											+ mdMedicine.getParent04()
													.getString02() + ", ";
								if (null != mdMedicine.getParent04()
										.getString03()
										&& !mdMedicine.getParent04()
												.getString03().equals(""))
									prescriberName = prescriberName
											+ mdMedicine.getParent04()
													.getString03() + ", ";
								if (null != mdMedicine.getParent04()
										.getString01()
										&& !mdMedicine.getParent04()
												.getString01().equals(""))
									prescriberName = prescriberName
											+ mdMedicine.getParent04()
													.getString01();

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(9).getST()
										.setValue(prescriberName); // Prescribed
																	// By

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(10).getST().setValue(
												"Refill Response"); // Status

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(11).getST().setValue("Pending"); // Surescripts
																				// Response

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(12).getST().setValue(
												response.get(4).getST()
														.getValue()); // Message
																		// Id is
																		// set

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(13).getST().setValue(
												patient.get(5).getST()
														.getValue()); // Patient
																		// Id is
																		// set

								// Submit this trim in the backend
								erxTrimCreatorBean.submitTrim(templateTrim,
										patientId + ":medications:all",
										activeAccountUser, now,userPrivateKey);
								writer.write("success");
								req.setAttribute("activeWriter", writer);
								return;
							} catch (JAXBException e) {
								e.printStackTrace();
							}
						} else {

							try {
								TrimEx templateTrim = erxTrimCreatorBean
										.createTrim(
												activeAccountUser,
												"obs/evn/currentMedicationDetailsForRefillRequest",
												patientId + ":medications:all",
												now);
								if (null != header.get(4).getST()) {
									try {
										Long.parseLong(header.get(4).getST()
												.getValue().trim());
										templateTrim.getAct()
												.getRelationships().get(0)
												.getAct().getObservation()
												.getValues().get(0).getST()
												.setValue(
														header.get(4).getST()
																.getValue()); // Prescriber
																				// Order
																				// Number
																				// is
																				// set.
									} catch (NumberFormatException e) {
										TolvenLogger
												.info(
														"Incoming PON is not of valid type, will be set to 0",
														ErxAjaxServlet.class);
										templateTrim.getAct()
												.getRelationships().get(0)
												.getAct().getObservation()
												.getValues().get(0).getST()
												.setValue("0"); // Prescriber
																// Order Number
																// is set to 0
																// since invalid
																// format.
									}
								}

								if (null != header.get(7).getTS())
									templateTrim.getAct().getRelationships()
											.get(0).getAct().getObservation()
											.getValues().get(1).setTS(
													header.get(7).getTS()); // Date
																			// of
																			// Prescription

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(2).getST().setValue(
												medicationPrescribed.get(0)
														.getST().getValue()); // Medication
																				// is
																				// set

								if (null != medicationPrescribed.get(3).getST()
										&& null != medicationPrescribed.get(4)
												.getST())
									templateTrim
											.getAct()
											.getRelationships()
											.get(0)
											.getAct()
											.getObservation()
											.getValues()
											.get(3)
											.getST()
											.setValue(
													medicationPrescribed.get(3)
															.getST().getValue()
															+ " "
															+ medicationPrescribed
																	.get(4)
																	.getST()
																	.getValue()); // Strength
																					// is
																					// set

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(4).setTS(null); // Start Date is
																// set

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(5).setTS(null); // End Date is
																// set

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(6).getST().setValue(""); // Route
																		// is
																		// set

								if (null != medicationPrescribed.get(3).getST()
										&& null != medicationPrescribed.get(3)
												.getST().getValue())
									templateTrim
											.getAct()
											.getRelationships()
											.get(0)
											.getAct()
											.getObservation()
											.getValues()
											.get(7)
											.getST()
											.setValue(
													medicationPrescribed.get(6)
															.getST().getValue()); // Dispense
																					// Amount
																					// is
																					// set

								if (medicationRequested.get(2).getCE()
										.getDisplayName().equals("R")) {
									templateTrim
											.getAct()
											.getRelationships()
											.get(0)
											.getAct()
											.getObservation()
											.getValues()
											.get(8)
											.getST()
											.setValue(
													medicationRequested.get(0)
															.getST().getValue()); // Refills
																					// is
																					// set
								} else {
									templateTrim.getAct().getRelationships()
											.get(0).getAct().getObservation()
											.getValues().get(8).getST()
											.setValue("PRN"); // Refills is
																// set
								}

								if (null != prescriber.get(0).getST()
										&& null != prescriber.get(0).getST()
												.getValue())
									templateTrim
											.getAct()
											.getRelationships()
											.get(0)
											.getAct()
											.getObservation()
											.getValues()
											.get(9)
											.getST()
											.setValue(
													epBean
															.retrievePrescriberNameFromId(prescriber
																	.get(0)
																	.getST()
																	.getValue())); // Prescribed
																					// By

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(10).getST().setValue(
												"Invalid RefREQ"); // Status

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(11).getST().setValue("Pending"); // Surescripts
																				// Response

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(12).getST().setValue(
												response.get(4).getST()
														.getValue()); // Message
																		// Id is
																		// set

								templateTrim.getAct().getRelationships().get(0)
										.getAct().getObservation().getValues()
										.get(13).getST().setValue(
												patient.get(5).getST()
														.getValue()); // Patient
																		// Id is
																		// set

								// Submit this trim in the backend
								erxTrimCreatorBean.submitTrim(templateTrim,
										patientId + ":medications:all",
										activeAccountUser, now,userPrivateKey);
								writer.write("success");
								req.setAttribute("activeWriter", writer);
								return;
							} catch (JAXBException e) {
								e.printStackTrace();
							}

							writer.write("invalid PON");
							req.setAttribute("activeWriter", writer);
							return;
						}
					} else { // RefReq associated to patient with no
								// prescription.
						try {
							TrimEx templateTrim = erxTrimCreatorBean
									.createTrim(
											activeAccountUser,
											"obs/evn/currentMedicationDetailsForRefillRequest",
											patientId + ":medications:all", now);
							List<ObservationValueSlot> templateTrimOVS = ((ActEx) templateTrim
									.getAct()).getRelationship().get(
									"CurrentMedication").getAct()
									.getObservation().getValues();
							if (null != header.get(4).getST()) {
								templateTrimOVS.get(0).getST().setValue("0"); // Prescriber
																				// Order
																				// Number
																				// is
																				// set.
							}
							if (null != header.get(7).getTS()) {
								templateTrimOVS.get(1).setTS(
										header.get(7).getTS()); // Date of
																// Prescription
							}
							templateTrimOVS.get(2).getST().setValue(
									medicationPrescribed.get(0).getST()
											.getValue()); // Medication is set
							if (null != medicationPrescribed.get(3).getST()
									&& null != medicationPrescribed.get(4)
											.getST()) {
								templateTrimOVS.get(3).getST().setValue(
										medicationPrescribed.get(3).getST()
												.getValue()
												+ " "
												+ medicationPrescribed.get(4)
														.getST().getValue()); // Strength
																				// is
																				// set
							}
							templateTrimOVS.get(4).setTS(null); // Start Date is
																// set
							templateTrimOVS.get(5).setTS(null); // End Date is
																// set
							templateTrimOVS.get(6).getST().setValue(""); // Route
																			// is
																			// set

							if (null != medicationPrescribed.get(3).getST()
									&& null != medicationPrescribed.get(3)
											.getST().getValue()) {
								templateTrimOVS.get(7).getST().setValue(
										medicationPrescribed.get(6).getST()
												.getValue()); // Dispense
																// Amount is set
							}
							if (medicationRequested.get(2).getCE()
									.getDisplayName().equals("R")) {
								templateTrimOVS.get(8).getST().setValue(
										medicationRequested.get(0).getST()
												.getValue()); // Refills is
																// set
							} else {
								templateTrimOVS.get(8).getST().setValue("PRN"); // Refills
																				// is
																				// set
							}

							if (null != prescriber.get(0).getST()
									&& null != prescriber.get(0).getST()
											.getValue()) {
								templateTrimOVS
										.get(9)
										.getST()
										.setValue(
												epBean
														.retrievePrescriberNameFromId(prescriber
																.get(0).getST()
																.getValue())); // Prescribed
																				// By
							}
							templateTrimOVS.get(10).getST().setValue(
									"RefResp with no PON"); // Status
							templateTrimOVS.get(11).getST().setValue("Pending"); // Surescripts
																					// Response
							templateTrimOVS.get(12).getST().setValue(
									response.get(4).getST().getValue()); // Message
																			// Id
																			// is
																			// set
							templateTrimOVS.get(13).getST().setValue(
									patient.get(5).getST().getValue()); // Patient
																		// Id is
																		// set

							// Submit this trim in the back end.
							erxTrimCreatorBean.submitTrim(templateTrim,
									patientId + ":medications:all",
									activeAccountUser, now,userPrivateKey);
							writer.write("success");
							req.setAttribute("activeWriter", writer);
							return;
						} catch (JAXBException e) {
							e.printStackTrace();
						}
						writer.write("No PON");
						req.setAttribute("activeWriter", writer);
						return;
					}
					// else {
					// writer.write("no PON");
					// req.setAttribute("activeWriter", writer);
					// return;
					// }
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (uri.endsWith("submitSharedMedication.ajaxerx")) {
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = this.getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				String patientId = trim.getAct().getParticipations().get(0)
						.getRole().getId().getIIS().get(1).getExtension();

				for (int i = 0; i < (trim.getAct().getRelationships().size()); i++) {
					if (trim.getAct().getRelationships().get(i).getName()
							.equals("medication")) {
						ActRelationship medRel = trim.getAct()
								.getRelationships().get(i);
						TrimEx templateTrim = erxTrimCreatorBean
								.createTrim(
										activeAccountUser,
										"obs/evn/currentMedicationDetailsForRefillRequest",
										patientId + ":medications:all", now);
						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(0)
								.getST().setValue(
										medRel.getAct().getObservation()
												.getValues().get(8).getST()
												.getValue()); // Prescriber
																// Order Number
																// is set.

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(1)
								.setTS(
										medRel.getAct().getObservation()
												.getValues().get(7).getTS()); // Date
																				// of
																				// Prescription

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(2)
								.getST().setValue(
										medRel.getAct().getText().getST()
												.getValue()); // Medication is
																// set

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(3)
								.getST().setValue(
										medRel.getAct().getObservation()
												.getValues().get(0).getST()
												.getValue()); // Strength is
																// set

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(4)
								.setTS(
										medRel.getAct().getObservation()
												.getValues().get(3).getTS()); // Start
																				// Date
																				// is
																				// set

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(5)
								.setTS(
										medRel.getAct().getObservation()
												.getValues().get(4).getTS()); // End
																				// Date
																				// is
																				// set

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(6)
								.getST().setValue(
										medRel.getAct().getObservation()
												.getValues().get(1).getST()
												.getValue()); // Route is set

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(7)
								.getST().setValue(""); // Dispense Amount is
														// set

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(8)
								.getST().setValue("0"); // Refills is set to 0
														// when shared

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(9)
								.getST().setValue(
										trim.getMessage().getReceiver()
												.getProviderName()); // Prescribed
																		// By
																		// Provider
																		// Name

						if (medRel.getAct().getObservation().getValues().get(5)
								.getST().getValue().equalsIgnoreCase("Active")) {
							templateTrim.getAct().getRelationships().get(0)
									.getAct().getObservation().getValues().get(
											10).getST().setValue("Active"); // Status
																			// Depends
																			// on
																			// Active
																			// /
																			// Discontinue
						} else if (medRel.getAct().getObservation().getValues()
								.get(5).getST().getValue().equalsIgnoreCase(
										"Discontinued")) {
							templateTrim.getAct().getRelationships().get(0)
									.getAct().getObservation().getValues().get(
											10).getST()
									.setValue("Discontinued"); // Status
																// Depends on
																// Active /
																// Discontinue
						}

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(11)
								.getST().setValue("Shared"); // Surescripts
																// Response

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(12)
								.getST().setValue(
										medRel.getAct().getObservation()
												.getValues().get(6).getST()
												.getValue()); // Message Id is
																// set

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(13)
								.getST().setValue(patientId); // Patient Id is
																// set
						/*
						 * Before submission we need to check for duplicate
						 * entry in the corresponding list
						 */
						boolean isActive = true;
						boolean isInActive = true;
						boolean isAll = true;
						if (medRel.getAct().getId().getIIS().size() == 1) {
							erxTrimCreatorBean.submitTrim(templateTrim,
									patientId + ":medications:all",
									activeAccountUser, now,userPrivateKey);// All Activity List
							if (medRel.getAct().getObservation().getValues()
									.get(5).getST().getValue()
									.equalsIgnoreCase("Active")) {
								// Submit this trim in the backend in both the
								// Active List
								erxTrimCreatorBean.submitTrim(templateTrim,
										patientId + ":medications:active",
										activeAccountUser, now,userPrivateKey);
							} else if (medRel.getAct().getObservation()
									.getValues().get(5).getST().getValue()
									.equalsIgnoreCase("Discontinued")) {
								// Submit this trim in the backend in both the
								// Inactive List
								erxTrimCreatorBean.submitTrim(templateTrim,
										patientId + ":medications:inactive",
										activeAccountUser, now,userPrivateKey);
							}
						} else {
							String context = medRel.getAct().getId().getIIS()
									.get(1).getExtension();
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("context", context);
							params.put("path", "echr:patient:medications:all");
							params.put("accountId", activeAccountUser
									.getAccount().getId());
							params.put("tolvenNow", new Date());
							params.put("accountUser", activeAccountUser);
							EPAction epAction = new EPAction();
							List<Map<String, Object>> medicationList = epAction
									.getAllMenuDataList(params);
							Map<String, Object> sMap;
							if (null != medicationList
									&& !medicationList.isEmpty()) {
								for (Object myElement : medicationList) {
									sMap = (Map<String, Object>) myElement;
									if ((sMap.get("long04").toString())
											.equals(medRel.getAct()
													.getObservation()
													.getValues().get(8).getST()
													.getValue())) {
										isAll = false;
										break;
									}
								}
								if (isAll)
									erxTrimCreatorBean.submitTrim(templateTrim,
											patientId + ":medications:all",
											activeAccountUser, now,userPrivateKey);// All
																	// Activity
																	// List
							} else {
								// All Activity List
								erxTrimCreatorBean.submitTrim(templateTrim,
										patientId + ":medications:all",
										activeAccountUser, now,userPrivateKey);
							}

							if (medRel.getAct().getObservation().getValues()
									.get(5).getST().getValue()
									.equalsIgnoreCase("Active")) {
								Map<String, Object> params1 = new HashMap<String, Object>();
								params1.put("context", context);
								params1.put("path",
										"echr:patient:medications:active");
								params1.put("accountId", activeAccountUser
										.getAccount().getId());
								params1.put("tolvenNow", new Date());
								params1.put("accountUser", activeAccountUser);
								List<Map<String, Object>> medicationList1 = epAction
										.getAllMenuDataList(params1);
								Map<String, Object> sMap1;
								if (null != medicationList1
										&& !medicationList1.isEmpty()) {
									for (Object myElement : medicationList1) {
										sMap1 = (Map<String, Object>) myElement;
										if ((sMap1.get("long04").toString())
												.equals(medRel.getAct()
														.getObservation()
														.getValues().get(8)
														.getST().getValue())) {
											isActive = false;
											break;
										}
									}
									if (isActive)
										erxTrimCreatorBean
												.submitTrim(
														templateTrim,
														patientId
																+ ":medications:active",
														activeAccountUser, now,userPrivateKey);// Submit
																				// this
																				// trim
																				// in
																				// the
																				// backend
																				// in
																				// both
																				// the
																				// Active
																				// List
								} else {
									// Submit this trim in the backend in both
									// the Active List
									erxTrimCreatorBean.submitTrim(templateTrim,
											patientId + ":medications:active",
											activeAccountUser, now,userPrivateKey);
								}
							} else if (medRel.getAct().getObservation()
									.getValues().get(5).getST().getValue()
									.equalsIgnoreCase("Discontinued")) {
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("context", context);
								params2.put("path",
										"echr:patient:medications:inactive");
								params2.put("accountId", activeAccountUser
										.getAccount().getId());
								params2.put("tolvenNow", new Date());
								params2.put("accountUser", activeAccountUser);
								List<Map<String, Object>> medicationList1 = epAction
										.getAllMenuDataList(params2);
								Map<String, Object> sMap1;
								if (null != medicationList1
										&& !medicationList1.isEmpty()) {
									for (Object myElement : medicationList1) {
										sMap1 = (Map<String, Object>) myElement;
										if ((sMap1.get("long04").toString())
												.equals(medRel.getAct()
														.getObservation()
														.getValues().get(8)
														.getST().getValue())) {
											isInActive = false;
											break;
										}
									}
									if (isInActive)
										erxTrimCreatorBean
												.submitTrim(
														templateTrim,
														patientId
																+ ":medications:inactive",
														activeAccountUser, now,userPrivateKey);// Submit
																				// this
																				// trim
																				// in
																				// the
																				// backend
																				// in
																				// both
																				// the
																				// Active
																				// List
								} else {
									// Submit this trim in the backend in both
									// the Active List
									erxTrimCreatorBean
											.submitTrim(templateTrim, patientId
													+ ":medications:inactive",
													activeAccountUser, now,userPrivateKey);
								}
							}
						}
					}// If medication relationship
				}
				return;

			} else if (uri.endsWith("submitSharedMedicationHistory.ajaxerx")) {
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = this.getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				String patientId = trim.getAct().getParticipations().get(0)
						.getRole().getId().getIIS().get(1).getExtension();

				for (int i = 0; i < (trim.getAct().getRelationships().size()); i++) {
					if (trim.getAct().getRelationships().get(i).getName()
							.equals("medication")) {
						ActRelationship medRel = trim.getAct()
								.getRelationships().get(i);
						TrimEx templateTrim = erxTrimCreatorBean
								.createTrim(
										activeAccountUser,
										"obs/evn/currentMedicationHistory",
										patientId + ":medications:all", now);

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(0)
								.getST().setValue(
										medRel.getAct().getText().getST()
												.getValue()); // Medication is
																// set

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(1)
								.setTS(
										medRel.getAct().getObservation()
												.getValues().get(2).getTS()); // Start
																				// Date
																				// is
																				// set

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(2)
								.setTS(
										medRel.getAct().getObservation()
												.getValues().get(3).getTS()); // End
																				// Date
																				// is
																				// set

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(3)
								.getST().setValue(
										medRel.getAct().getObservation()
												.getValues().get(0).getST()
												.getValue()); // Route is set


						templateTrim.getAct().getRelationships().get(0)
						.getAct().getObservation().getValues().get(6)
						.getST().setValue(
										medRel.getAct().getObservation()
												.getValues().get(1).getST()
												.getValue()); // Frequency
																
						
						templateTrim.getAct().getRelationships().get(0)
						.getAct().getObservation().getValues().get(7)
						.getST().setValue(
										medRel.getAct().getObservation()
												.getValues().get(5).getST()
												.getValue()); // Instructions
						
						templateTrim.getAct().getRelationships().get(0)
						.getAct().getObservation().getValues().get(8)
						.getST().setValue(
										medRel.getAct().getObservation()
												.getValues().get(6).getST()
												.getValue()); // Comments						
						

						templateTrim.getAct().getRelationships().get(0)
						.getAct().getObservation().getValues().get(9)
						.getST().setValue(
								trim.getMessage().getReceiver()
										.getProviderName()); // Prescribed
																// By
																// Provider
																// Name						
						
						if (medRel.getAct().getObservation().getValues().get(4)
								.getST().getValue().equalsIgnoreCase("Active")) {
							templateTrim.getAct().getRelationships().get(0)
									.getAct().getObservation().getValues().get(
											5).getST().setValue("active"); // Status
																			// Depends
																			// on
																			// Active
																			// /
																			// Discontinue
						} else if (medRel.getAct().getObservation().getValues().get(4)
								.getST().getValue().equalsIgnoreCase("Completed")) {
							templateTrim.getAct().getRelationships().get(0)
									.getAct().getObservation().getValues().get(
											5).getST().setValue("completed"); // Status
																			// Depends
																			// on
																			// Active
																			// /
																			// Discontinue
						} else if (medRel.getAct().getObservation().getValues()
								.get(4).getST().getValue().equalsIgnoreCase(
										"Discontinued")) {
							templateTrim.getAct().getRelationships().get(0)
									.getAct().getObservation().getValues().get(
											5).getST()
									.setValue("Discontinued"); // Status
																// Depends on
																// Active /
																// Discontinue
						}

						templateTrim.getAct().getRelationships().get(0)
								.getAct().getObservation().getValues().get(6)
								.getST().setValue(patientId); // Patient Id is
																// set
						/*
						 * Before submission we need to check for duplicate
						 * entry in the corresponding list
						 */
						boolean isActive = true;
						boolean isInActive = true;
						boolean isAll = true;
						if (medRel.getAct().getId().getIIS().size() == 1) {
							erxTrimCreatorBean.submitTrim(templateTrim,
									patientId + ":medications:all",
									activeAccountUser, now,userPrivateKey);// All Activity List
							if (medRel.getAct().getObservation().getValues()
									.get(4).getST().getValue()
									.equalsIgnoreCase("Active")) {
								// Submit this trim in the backend in both the
								// Active List
								erxTrimCreatorBean.submitTrim(templateTrim,
										patientId + ":medications:active",
										activeAccountUser, now,userPrivateKey);
							} else if (medRel.getAct().getObservation()
									.getValues().get(4).getST().getValue()
									.equalsIgnoreCase("Discontinued")) {
								// Submit this trim in the backend in both the
								// Inactive List
								erxTrimCreatorBean.submitTrim(templateTrim,
										patientId + ":medications:inactive",
										activeAccountUser, now,userPrivateKey);
							}
						} else {
							String context = medRel.getAct().getId().getIIS()
									.get(1).getExtension();
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("context", context);
							params.put("path", "echr:patient:medications:all");
							params.put("accountId", activeAccountUser
									.getAccount().getId());
							params.put("tolvenNow", new Date());
							params.put("accountUser", activeAccountUser);
							EPAction epAction = new EPAction();
							List<Map<String, Object>> medicationList = epAction
									.getAllMenuDataList(params);
							Map<String, Object> sMap;
							if (null != medicationList
									&& !medicationList.isEmpty()) {
								for (Object myElement : medicationList) {
									sMap = (Map<String, Object>) myElement;
									if ((sMap.get("string01").toString())
											.equals(medRel.getAct()
													.getText().getST()
													.getValue())) {
										isAll = false;
										break;
									}
								}
								if (isAll)
									erxTrimCreatorBean.submitTrim(templateTrim,
											patientId + ":medications:all",
											activeAccountUser, now,userPrivateKey);// All
																	// Activity
																	// List
							} else {
								// All Activity List
								erxTrimCreatorBean.submitTrim(templateTrim,
										patientId + ":medications:all",
										activeAccountUser, now,userPrivateKey);
							}
		
							if (medRel.getAct().getObservation().getValues()
									.get(4).getST().getValue()
									.equalsIgnoreCase("Active")) {
								Map<String, Object> params1 = new HashMap<String, Object>();
								params1.put("context", context);
								params1.put("path",
										"echr:patient:medications:active");
								params1.put("accountId", activeAccountUser
										.getAccount().getId());
								params1.put("tolvenNow", new Date());
								params1.put("accountUser", activeAccountUser);
								List<Map<String, Object>> medicationList1 = epAction
										.getAllMenuDataList(params1);
								Map<String, Object> sMap1;
								if (null != medicationList1
										&& !medicationList1.isEmpty()) {
									for (Object myElement : medicationList1) {
										sMap1 = (Map<String, Object>) myElement;
										if ((sMap1.get("string01").toString())
												.equals(medRel.getAct().getText()
														.getST().getValue())) {
											isActive = false;
											break;
										}
									}
									if (isActive)
										erxTrimCreatorBean
												.submitTrim(
														templateTrim,
														patientId
																+ ":medications:active",
														activeAccountUser, now,userPrivateKey);// Submit
																				// this
																				// trim
																				// in
																				// the
																				// backend
																				// in
																				// both
																				// the
																				// Active
																				// List
								} else {
									// Submit this trim in the backend in both
									// the Active List
									erxTrimCreatorBean.submitTrim(templateTrim,
											patientId + ":medications:active",
											activeAccountUser, now,userPrivateKey);
								}
							} else if (medRel.getAct().getObservation()
									.getValues().get(4).getST().getValue()
									.equalsIgnoreCase("Discontinued")) {
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("context", context);
								params2.put("path",
										"echr:patient:medications:inactive");
								params2.put("accountId", activeAccountUser
										.getAccount().getId());
								params2.put("tolvenNow", new Date());
								params2.put("accountUser", activeAccountUser);
								List<Map<String, Object>> medicationList1 = epAction
										.getAllMenuDataList(params2);
								Map<String, Object> sMap1;
								if (null != medicationList1
										&& !medicationList1.isEmpty()) {
									for (Object myElement : medicationList1) {
										sMap1 = (Map<String, Object>) myElement;
										if ((sMap1.get("string01").toString())
												.equals(medRel.getAct().getText()
														.getST().getValue())) {
											isInActive = false;
											break;
										}
									}
									if (isInActive)
										erxTrimCreatorBean
												.submitTrim(
														templateTrim,
														patientId
																+ ":medications:inactive",
														activeAccountUser, now,userPrivateKey);// Submit
																				// this
																				// trim
																				// in
																				// the
																				// backend
																				// in
																				// both
																				// the
																				// Active
																				// List
								} else {
									// Submit this trim in the backend in both
									// the Active List
									erxTrimCreatorBean
											.submitTrim(templateTrim, patientId
													+ ":medications:inactive",
													activeAccountUser, now,userPrivateKey);
								}
							}
						}						
					}// If medication relationship
				}
				return;

			}
			
			else if (uri.endsWith("fetchPatientSpecificRXs.ajaxerx")) {
				try {
					String patElement = req.getParameter("patElement");
					if (!patElement.equals("#{others}")
							&& !patElement.equals("#{null}")
							&& !patElement.equals("")) {
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("context", req.getParameter("patElement"));
						params.put("path", "echr:patient:medications:active");
						params.put("accountId", activeAccountUser.getAccount()
								.getId());
						params.put("tolvenNow", new Date());
						params.put("accountUser", activeAccountUser);
						EPAction epAction = new EPAction();
						Map<String, Object> sMap = null;
						MenuData mdPat = epBean.getMdFromFieldAndValue("path",
								patElement);
						mdPat.getString02();
						mdPat.getString03();
						mdPat.getString01();

						List<Map<String, Object>> medicationList = epAction
								.getAllMenuDataList(params);
						if (null != medicationList && !medicationList.isEmpty()) {
							for (Object myElement : medicationList) {
								sMap = (Map<String, Object>) myElement;
								writer.write(sMap.get("string01").toString()
										+ ":" + sMap.get("long04").toString()
										+ "|");
							}
						}
						// writer.write(mdPat.getString02()+"|"+
						// mdPat.getString01());
						writer.write(mdPat.getString02() + "|"
								+ mdPat.getString03() + "|"
								+ mdPat.getString01());
					}
					req.setAttribute("activeWriter", writer);
					return;
				} catch (Exception e) {
					TolvenLogger
							.error(
									"Error encountered while fetching patient specific ERX.",
									ErxAjaxServlet.class);
					e.printStackTrace();
				}
			} else if (uri.endsWith("insertPON.ajaxerx")) {
				try {
					MenuData md = menuBean.findMenuDataItem(activeAccountUser
							.getAccount().getId(), element);
					DocXML docXML = (DocXML) getDocBean().findDocument(
							md.getDocumentId());
					String trimString = this.getDocProtectionBean()
							.getDecryptedContentString(docXML,
									activeAccountUser,userPrivateKey);
					TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
							new StringReader(trimString));
					try {
						if (Long.parseLong(req.getParameter("pon")) > 0)
							((ActEx) trim.getAct()).getRelationship().get(
									"header").getAct().getObservation()
									.getValues().get(4).getST().setValue(
											req.getParameter("pon"));
						else
							((ActEx) trim.getAct()).getRelationship().get(
									"header").getAct().getObservation()
									.getValues().get(4).getST().setValue("0");
					} catch (Exception e) {
						((ActEx) trim.getAct()).getRelationship().get("header")
								.getAct().getObservation().getValues().get(4)
								.getST().setValue("0");
					}

					/* Do Processing for Inserting original Prescription details */

					ActRelationshipEx originalPrescription = (ActRelationshipEx) ((ActEx) trim
							.getAct()).getRelationship().get(
							"originalPrescription");
					if (null != (((ActEx) trim.getAct()).getRelationship()
							.get("header")).getAct().getObservation()
							.getValues().get(4).getST()
							&& null != (((ActEx) trim.getAct())
									.getRelationship().get("header")).getAct()
									.getObservation().getValues().get(4)
									.getST().getValue()
							&& !(((ActEx) trim.getAct()).getRelationship()
									.get("header")).getAct().getObservation()
									.getValues().get(4).getST().getValue()
									.equals("")) {
						MenuData orgRxMd = epBean
								.getMenuDataFromPrescriberOrderNumber((((ActEx) trim
										.getAct()).getRelationship()
										.get("header")).getAct()
										.getObservation().getValues().get(4)
										.getST().getValue());
						if (null != orgRxMd
								&& null != orgRxMd.getParentPath01()
								&& null != orgRxMd.getPath()
								&& orgRxMd.getPath().contains(
										"currentMedication")) {
							MenuData patMd = epBean
									.getMenuDataFromPrescriberOrderNumber(orgRxMd
											.getParentPath01().split("-")[1]);
							ELFunctions elf = new ELFunctions();
							if (null != patMd) {
								String patientDetails = "";
								if (null != patMd.getString02())
									patientDetails = patientDetails
											+ patMd.getString02() + " ";
								if (null != patMd.getString03())
									patientDetails = patientDetails
											+ patMd.getString03() + " ";
								if (null != patMd.getString01())
									patientDetails = patientDetails
											+ patMd.getString01() + ", ";
								if (null != patMd.getDate01())
									patientDetails = patientDetails
											+ elf.ageInYears(patMd.getDate01())
											+ "Yrs, ";
								if (null != patMd.getString04())
									patientDetails = patientDetails
											+ patMd.getString04();
								originalPrescription.getAct().getObservation()
										.getValues().get(0).getST().setValue(
												patientDetails); // Patient
																	// Details
																	// setting
								writer.write(patientDetails + "|HRF|");
							} else {
								writer.write("No Details about the patient.");
								writer.write("|HRF|");
							}

							if (null != orgRxMd.getString01()) {
								originalPrescription.getAct().getObservation()
										.getValues().get(1).getST().setValue(
												orgRxMd.getString01()); // Setting
																		// the
																		// medication
																		// name
								writer.write(orgRxMd.getString01() + "|HRF|");
							} else {
								writer
										.write("No Details about the medication.");
								writer.write("|HRF|");
							}

							String schedule = fdbBean.checkForDrugValidity(orgRxMd.getString01());

							/*
							 * Overwrite the Schedule Drug Information in
							 * original Trim
							 */
							(((ActEx) trim.getAct()).getRelationship()
									.get("MedicationRequested")).getAct()
									.getObservation().getValues().get(3)
									.getST().setValue(schedule);

							String drugSchedule = "Normal";
							if (schedule.equals("3")) {
								drugSchedule = "Schedule III";
							} else if (schedule.equals("4")) {
								drugSchedule = "Schedule IV";
							} else if (schedule.equals("5")) {
								drugSchedule = "Schedule V";
							} else if (schedule.equals("0")) {
								drugSchedule = "Non-drug Item";
							} else if (schedule.equals("9")) {
								drugSchedule = "No Value";
							} else if (schedule.equals("6")) {
								drugSchedule = "Multiple DEA Class Codes";
							} else if (schedule.equals("1")) {
								drugSchedule = "Schedule I";
							}

							originalPrescription.getAct().getObservation()
									.getValues().get(2).getST().setValue(
											drugSchedule); // Controlled Drug
															// Information
							writer.write(drugSchedule + "|HRF|");

							if (null != orgRxMd.getPqUnits03()) {
								originalPrescription.getAct().getObservation()
										.getValues().get(3).getST().setValue(
												orgRxMd.getPqUnits03()); // Original
																			// Number
																			// of
																			// Refills
								writer.write(orgRxMd.getPqUnits03() + "|HRF|");
							} else {
								writer.write("No Refills");
								writer.write("|HRF|");
							}

							if (null != orgRxMd.getDate01()) {
								originalPrescription.getAct().getObservation()
										.getValues().get(4).setTS(
												trimFactory.createNewTS(orgRxMd
														.getDate01())); // Date
																		// of
																		// Prescription
								writer.write(String.valueOf(orgRxMd.getDate01()
										.getMonth() + 1)
										+ "/"
										+ orgRxMd.getDate01().getDate()
										+ "/"
										+ String.valueOf(1900 + orgRxMd
												.getDate01().getYear()) + "");
								writer.write("|HRF|");
							}
							originalPrescription.getAct().getObservation()
									.getValues().get(5).getST().setValue("");

							writer.write(schedule);
							writer.write("|HRF|");

						} else {
							writer
									.write("No Prescriptions with the Mentioned PON found in the Account.");
							originalPrescription
									.getAct()
									.getObservation()
									.getValues()
									.get(5)
									.getST()
									.setValue(
											"No Prescriptions with the Mentioned PON found in the Account.");
						}
					}

					req.setAttribute("activeWriter", writer);
					/* Processing ends */
					creatorBean.marshalToDocument(trim, docXML);
					return;
				} catch (Exception e) {
					TolvenLogger.error("Error encountered while inserting PON",
							ErxAjaxServlet.class);
					e.printStackTrace();
				}
			} else if (uri.endsWith("removePON.ajaxerx")) {
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = this.getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				((ActEx) trim.getAct()).getRelationship().get("header")
						.getAct().getObservation().getValues().get(4).getST()
						.setValue("0");
				creatorBean.marshalToDocument(trim, docXML);
				return;
			} else if (uri.endsWith("getAllPatients.ajaxerx")) {
				boolean patMatch = true;
				boolean patInApp = false;
				boolean validPON = false;
				String lastName_pon = "";
				String firstName_pon = "";
				String ponPatient = "";
				MenuData md = menuBean.findMenuDataItem(activeAccountUser.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(md.getDocumentId());
				String trimString = this.getDocProtectionBean().getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,new StringReader(trimString));
				String lastName_Refill = ((ActEx) trim.getAct())
						.getRelationship().get("patient").getAct()
						.getObservation().getValues().get(0).getST().getValue();
				String firstName_Refill = ((ActEx) trim.getAct())
						.getRelationship().get("patient").getAct()
						.getObservation().getValues().get(1).getST().getValue();
				MenuData orgRxMd = epBean.getMenuDataFromPrescriberOrderNumber((((ActEx) trim
								.getAct()).getRelationship().get("header"))
								.getAct().getObservation().getValues().get(4).getST().getValue());
				Long prescriberAccountId = null;//
				/*sureBean.getMdFromsSpi((((ActEx) trim.getAct()).getRelationship()
												.get("prescriber")).getAct().getObservation().getValues()
												.get(0).getST().getValue()).getAccount().getId();
				*/
				if(prescriberAccountId == null)
					throw new IllegalStateException("Fix commented code in"+this.getClass()+" at line 2880");
				if (null != orgRxMd && null != orgRxMd.getParentPath01()&& null != orgRxMd.getPath()
											&& orgRxMd.getPath().contains("currentMedication")) {
					MenuData patMd = epBean.getMenuDataFromPrescriberOrderNumber(orgRxMd.getParentPath01().split("-")[1]);
					if (null != patMd) {
						firstName_pon = patMd.getString02();
						lastName_pon = patMd.getString01();
						ponPatient = patMd.getPath();
					}
				}
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("context", req.getParameter("element"));
				params.put("path", "echr:patients:all");
				params.put("accountId", activeAccountUser.getAccount().getId());
				params.put("tolvenNow", new Date());
				params.put("accountUser", activeAccountUser);
				EPAction epAction = new EPAction();
				List<Map<String, Object>> patientList = epAction
						.getAllMenuDataList(params);

				if (orgRxMd != null && orgRxMd.getParentPath01() != null
						&& orgRxMd.getAccount() != null
						&& orgRxMd.getAccount().getId() == prescriberAccountId) { // Valid PON
					// Check if RefillReq_Pat = PON_Pat..?
					if (lastName_Refill.equals(lastName_pon)
							&& firstName_Refill.equals(firstName_pon)) {
						// Filter App_Pat that match RefillReq_Pat. //TODO what
						// if the patient is terminated???
						Map<String, Object> sMap;
						if (null != patientList && !patientList.isEmpty()) {
							for (Object myElement : patientList) {
								sMap = (Map<String, Object>) myElement;
								if (ponPatient
										.equals(sMap.get("referencePath"))) {
									writer.write(sMap.get("referencePath")
											+ "|SEPARATE|");
									if (null != sMap.get("string01")) // Last
																		// Name
										writer
												.write(sMap.get("string01")
														+ ",");
									if (null != sMap.get("string03")) // Middle
																		// Name
										writer
												.write(sMap.get("string03")
														+ ",");
									if (null != sMap.get("string02")) { // First
																		// Name
										writer.write(sMap.get("string02") + "");
									}
									writer.write("|HRF|");
								}
							}
						}
					} else {
						// Check if RefillReq_Pat == App_Pat..?
						validPON = true;
						Map<String, Object> sMap;
						if (null != patientList && !patientList.isEmpty()) {
							for (Object myElement : patientList) {
								sMap = (Map<String, Object>) myElement;
								if (ponPatient
										.equals(sMap.get("referencePath"))) {
									// if
									// (lastName_Refill.equals(sMap.get("string01"))
									// &&
									// firstName_Refill.equals(sMap.get("string02")))
									// {
									patInApp = true;
									writer.write(sMap.get("referencePath")
											+ "|SEPARATE|");
									if (null != sMap.get("string01"))
										writer
												.write(sMap.get("string01")
														+ ",");
									if (null != sMap.get("string03"))
										writer
												.write(sMap.get("string03")
														+ ",");
									if (null != sMap.get("string02"))
										writer.write(sMap.get("string02") + "");
									writer.write("|HRF|");
								}
							}
						}
					}
				} else { // Invalid or No PON
					// Check if RefillReq_Pat == App_Pat..?
					Map<String, Object> sMap;
					patMatch = false;
					if (null != patientList && !patientList.isEmpty()) {
						for (Object myElement : patientList) {
							sMap = (Map<String, Object>) myElement;
							if (lastName_Refill.equals(sMap.get("string01"))
									&& firstName_Refill.equals(sMap
											.get("string02"))) {
								patInApp = true;
								writer.write(sMap.get("referencePath")
										+ "|SEPARATE|");
								if (null != sMap.get("string01"))
									writer.write(sMap.get("string01") + ",");
								if (null != sMap.get("string03"))
									writer.write(sMap.get("string03") + ",");
								if (null != sMap.get("string02"))
									writer.write(sMap.get("string02") + "");
								writer.write("|HRF|");
							}
						}
					}
				}

				if (!patMatch) {
					if (patInApp) {
						writer
								.write("Invalid or no PON with refReq patient in app");
					} else {
						writer
								.write("Invalid or no PON with refReq patient not in app");
					}
				} else {
					if (validPON) {
						if (patInApp) {
							writer
									.write("Valid PON with mismatch and refillReq patient in app");
						} else {
							writer
									.write("Valid PON with mismatch and refillReq patient not in app");
						}
					} else {
						writer
								.write("Valid PON with RefReq equals PON patient");
					}
				}

				req.setAttribute("activeWriter", writer);
				return;
			} else if (uri.endsWith("changeRefillsAmountWhenPRN.ajaxerx")) {
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = this.getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				((ActEx) trim.getAct()).getRelationship().get(
						"medicationDetails").getAct().getObservation()
						.getValues().get(4).getST().setValue("PRN");
				creatorBean.marshalToDocument(trim, docXML);
				req.setAttribute("activeWriter", writer);
				return;
			} else if (uri.endsWith("gatherOriginalPrescriptionInfo.ajaxerx")) {
				String lastName_pon = null;
				String firstName_pon = null;
				MenuData md = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				DocXML docXML = (DocXML) getDocBean().findDocument(
						md.getDocumentId());
				String trimString = this.getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
				TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,
						new StringReader(trimString));
				String lastName_Refill = ((ActEx) trim.getAct())
						.getRelationship().get("patient").getAct()
						.getObservation().getValues().get(0).getST().getValue();
				String firstName_Refill = ((ActEx) trim.getAct())
						.getRelationship().get("patient").getAct()
						.getObservation().getValues().get(1).getST().getValue();
				/* Do the processing*/
				ActRelationshipEx originalPrescription = (ActRelationshipEx) ((ActEx) trim
						.getAct()).getRelationship()
						.get("originalPrescription");
				if (null != (((ActEx) trim.getAct()).getRelationship()
						.get("header")).getAct().getObservation().getValues()
						.get(4).getST()
						&& null != (((ActEx) trim.getAct()).getRelationship()
								.get("header")).getAct().getObservation()
								.getValues().get(4).getST().getValue()
						&& !(((ActEx) trim.getAct()).getRelationship()
								.get("header")).getAct().getObservation()
								.getValues().get(4).getST().getValue().equals(
										"")) {
					MenuData orgRxMd = epBean
							.getMenuDataFromPrescriberOrderNumber((((ActEx) trim
									.getAct()).getRelationship().get("header"))
									.getAct().getObservation().getValues().get(
											4).getST().getValue());
					if (null != orgRxMd && null != orgRxMd.getParentPath01()
							&& null != orgRxMd.getPath()
							&& orgRxMd.getPath().contains("currentMedication")) {
						MenuData patMd = epBean
								.getMenuDataFromPrescriberOrderNumber(orgRxMd
										.getParentPath01().split("-")[1]);
						ELFunctions elf = new ELFunctions();
						if (null != patMd) {
							firstName_pon = patMd.getString02();
							lastName_pon = patMd.getString01();
							String patientDetails = "";
							if (null != patMd.getString02())
								patientDetails = patientDetails
										+ patMd.getString02() + " ";
							if (null != patMd.getString03())
								patientDetails = patientDetails
										+ patMd.getString03() + " ";
							if (null != patMd.getString01())
								patientDetails = patientDetails
										+ patMd.getString01() + ", ";
							if (null != patMd.getDate01())
								patientDetails = patientDetails
										+ elf.ageInYears(patMd.getDate01())
										+ "Yrs, ";
							if (null != patMd.getString04())
								patientDetails = patientDetails
										+ patMd.getString04();
							originalPrescription.getAct().getObservation()
									.getValues().get(0).getST().setValue(
											patientDetails); //Patient Details setting

							writer.write(patientDetails + "|HRF|");
						} else {
							writer.write("No Details about the patient.");
							writer.write("|HRF|");
						}

						if (null != orgRxMd.getString01()) {
							originalPrescription.getAct().getObservation()
									.getValues().get(1).getST().setValue(
											orgRxMd.getString01()); // Setting the medication name
							writer.write(orgRxMd.getString01() + "|HRF|");
						} else {
							writer.write("No Details about the medication.");
							writer.write("|HRF|");
						}

						String schedule = fdbBean.checkForDrugValidity(orgRxMd
								.getString01());

						/* Overwrite the Schedule Drug Information in original Trim */
						(((ActEx) trim.getAct()).getRelationship()
								.get("MedicationRequested")).getAct()
								.getObservation().getValues().get(3).getST()
								.setValue(schedule);

						String drugSchedule = "Normal";
						if (schedule.equals("3")) {
							drugSchedule = "Schedule III";
						} else if (schedule.equals("4")) {
							drugSchedule = "Schedule IV";
						} else if (schedule.equals("5")) {
							drugSchedule = "Schedule V";
						} else if (schedule.equals("0")) {
							drugSchedule = "Non-drug Item";
						} else if (schedule.equals("9")) {
							drugSchedule = "No Value";
						} else if (schedule.equals("6")) {
							drugSchedule = "Multiple DEA Class Codes";
						} else if (schedule.equals("1")) {
							drugSchedule = "Schedule I";
						}

						originalPrescription.getAct().getObservation()
								.getValues().get(2).getST().setValue(
										drugSchedule); // Controlled Drug Information
						writer.write(drugSchedule + "|HRF|");

						if (null != orgRxMd.getPqUnits03()) {
							originalPrescription.getAct().getObservation()
									.getValues().get(3).getST().setValue(
											orgRxMd.getPqUnits03()); // Original Number of Refills
							writer.write(orgRxMd.getPqUnits03() + "|HRF|");
						} else {
							writer.write("No Refills");
							writer.write("|HRF|");
						}

						if (null != orgRxMd.getDate01()) {
							originalPrescription.getAct().getObservation()
									.getValues().get(4).setTS(
											trimFactory.createNewTS(orgRxMd
													.getDate01())); // Date of Prescription
							writer.write(String.valueOf(orgRxMd.getDate01()
									.getMonth() + 1)
									+ "/"
									+ orgRxMd.getDate01().getDate()
									+ "/"
									+ String.valueOf(1900 + orgRxMd.getDate01()
											.getYear()) + "");
							writer.write("|HRF|");
						}
						originalPrescription.getAct().getObservation()
								.getValues().get(5).getST().setValue("");
						writer.write(schedule);
						if (firstName_Refill.equals(firstName_pon)
								&& lastName_Refill.equals(lastName_pon)) {
							writer.write("|HRF|" + "MATCH");
						} else {
							writer.write("|HRF|" + "MISMATCH");
						}
						//						writer.write("|HRF|");
					} else {
						writer
								.write("No Prescriptions with the Mentioned PON found in the Account.");
						originalPrescription
								.getAct()
								.getObservation()
								.getValues()
								.get(5)
								.getST()
								.setValue(
										"No Prescriptions with the Mentioned PON found in the Account.");
					}
				}
				creatorBean.marshalToDocument(trim, docXML);
				req.setAttribute("activeWriter", writer);
				return;
			} else if (uri.endsWith("disablePhysician.ajaxerx")) {
				try {
					String templateId = req.getParameter("templateId");
					if (templateId == null)
						throw new IllegalArgumentException(
								"Instantiation request has missing templateId");
					String context = req.getParameter("context");
					if (context == null)
						throw new IllegalArgumentException(
								"Instantiation request has missing context");
					TolvenLogger.info("[InstantiateServlet] ms=" + templateId
							+ " context: " + context, InstantiateServlet.class);

					TrimEx trim = erxTrimCreatorBean.createTrim(
							activeAccountUser, templateId, context, now);
					List<ObservationValueSlot> prescriberOVS = ((ActEx) trim
							.getAct()).getRelationship().get("prescriber")
							.getAct().getObservation().getValues();
					// Set to current time.
					prescriberOVS.get(5).setTS(
							trimFactory.createNewTS(new Date()));
					prescriberOVS.get(3).getST().setValue("0");
					erxTrimCreatorBean.submitTrim(trim, context,
							activeAccountUser, now,userPrivateKey);
					writer.write("success");
				} catch (Exception e) {
					e.printStackTrace();
				}
				req.setAttribute("response", writer);
				return;
			} else if (uri.endsWith("getSupportedServiceLevel.ajaxerx")) {
				String supportedServiceLevel = getTolvenProperties()
						.getProperty("surescripts.supported.servicelevel");
				if (supportedServiceLevel == null) {
					supportedServiceLevel = "0";
				}
				writer.write(supportedServiceLevel);
				req.setAttribute("response", writer);
				return;
			}
			/**
			 * On Active Medication 'Revise' action - setting the current trim as nullified and created a new trim with active status. 
			 */
			else if (uri.endsWith("reviseActiveMedication.ajaxerx")) {
				action = req.getParameter("action");
				String patientId = element.split("-")[1].split(":")[0];
				String path = "echr:patient-" + patientId
						+ ":medications:active";
				if (element == null)
					throw new IllegalArgumentException(
							"Missing element in TRIM request");
				if (action == null)
					throw new IllegalArgumentException(
							"Missing action in TRIM transition request");
				TolvenLogger.info("[wizTransition] account="
						+ activeAccountUser.getAccount().getId() + ", element="
						+ element + ", action: " + action,
						InstantiateServlet.class);
				// Setting the currrent trim as nullified
				MenuData mdPrior = menuBean.findMenuDataItem(activeAccountUser
						.getAccount().getId(), element);
				MenuData mdNew = creatorBean.createTRIMEvent(mdPrior,
						activeAccountUser, action, now,userPrivateKey);
				creatorBean.submit(mdNew, activeAccountUser,userPrivateKey);

				// creating new trim with all data from nullified trim
				if (element.split("-").length >= 2) {
					MenuData md = menuBean.findMenuDataItem(new Long(mdNew
							.getPath().split("-")[2]));
					TrimEx nulltrim = null;
					if (md != null){
						DocXML docXMLFrom = (DocXML) docBean.findDocument(md.getDocumentId());
						nulltrim = (TrimEx) getTrimMarshaller().unmarshal(docXMLFrom, activeAccountUser,userPrivateKey);
					}
					/*TrimEx nulltrim = cchitLocal.findTrimData(new Long(mdNew
							.getPath().split("-")[2]), activeAccountUser,userPrivateKey);*/
					TrimEx newtrim = erxTrimCreatorBean.createTrim(
							activeAccountUser, "obs/evn/patientPrescription",
							path, now);
					//effectiveTime
					newtrim.getAct().getEffectiveTime().setTS(
							nulltrim.getAct().getEffectiveTime().getTS());
					//toSureScripts
					if (((ActEx) nulltrim.getAct()).getRelationship().get(
							"toSureScripts") != null)
						((ActEx) newtrim.getAct()).getRelationship().get(
								"toSureScripts").setAct(
								((ActEx) nulltrim.getAct()).getRelationship()
										.get("toSureScripts").getAct());
					//medicationDetails
					if (((ActEx) nulltrim.getAct()).getRelationship().get(
							"medicationDetails") != null)
						((ActEx) newtrim.getAct()).getRelationship().get(
								"medicationDetails").setAct(
								((ActEx) nulltrim.getAct()).getRelationship()
										.get("medicationDetails").getAct());
					//encounter
					if (((ActEx) nulltrim.getAct()).getRelationship().get(
							"encounter") != null)
						((ActEx) newtrim.getAct()).getRelationship().get(
								"encounter").setAct(
								((ActEx) nulltrim.getAct()).getRelationship()
										.get("encounter").getAct());
					//favoriteMedications
					if (((ActEx) nulltrim.getAct()).getRelationship().get(
							"favoriteMedications") != null)
						((ActEx) newtrim.getAct()).getRelationship().get(
								"favoriteMedications").setAct(
								((ActEx) nulltrim.getAct()).getRelationship()
										.get("favoriteMedications").getAct());
					//selectFormulary
					if (((ActEx) nulltrim.getAct()).getRelationship().get(
							"selectFormulary") != null)
						((ActEx) newtrim.getAct()).getRelationship().get(
								"selectFormulary").setAct(
								((ActEx) nulltrim.getAct()).getRelationship()
										.get("selectFormulary").getAct());
					//pharmacy
					if (((ActEx) nulltrim.getAct()).getRelationship().get(
							"pharmacy") != null)
						((ActEx) newtrim.getAct()).getRelationship().get(
								"pharmacy").setAct(
								((ActEx) nulltrim.getAct()).getRelationship()
										.get("pharmacy").getAct());
					//ndcDetails
					if (((ActEx) nulltrim.getAct()).getRelationship().get(
							"ndcDetails") != null)
						((ActEx) newtrim.getAct()).getRelationship().get(
								"ndcDetails").setAct(
								((ActEx) nulltrim.getAct()).getRelationship()
										.get("ndcDetails").getAct());

					String newPath = erxTrimCreatorBean.addTrimToActivityList(
							newtrim, path, activeAccountUser, now,userPrivateKey);

					if (newPath != null)
						writer.write(newPath);

				}
				req.setAttribute("activeWriter", writer);
				return;
			}
		} catch (Exception e) {
			throw new ServletException(
					"New Exception thrown in ErxAjaxServlet", e);
		}
	}

	public ST getST(String str) {
		ST st = new ST();
		st.setValue(str);
		return st;
	}

	public List<MenuPath> getContextList() {
		if (contextList == null) {
			contextList = new ArrayList<MenuPath>();
		}
		return contextList;
	}

	/**
	 * @return the docProtectionBean
	 */
	public DocProtectionLocal getDocProtectionBean() {
		return docProtectionBean;
	}

	/**
	 * @return the docBean
	 */
	public DocumentLocal getDocBean() {
		return docBean;
	}

	/**
	 * @return the tolvenProperties
	 */
	public TolvenPropertiesLocal getTolvenProperties() {
		return tolvenProperties;
	}

	/**
	 * @param tolvenProperties the tolvenProperties to set
	 */
	public void setTolvenProperties(TolvenPropertiesLocal tolvenProperties) {
		this.tolvenProperties = tolvenProperties;
	}

    private TrimMarshaller getTrimMarshaller() {
        if(trimMarshaller == null) {
            trimMarshaller = new TrimMarshaller();
        }
        return trimMarshaller;
    }
    
}
