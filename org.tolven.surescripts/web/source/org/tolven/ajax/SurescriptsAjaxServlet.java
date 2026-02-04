package org.tolven.ajax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.tolven.app.CreatorLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.TrimLocal;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.MenuData;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.DocumentLocal;
import org.tolven.doc.entity.DocXML;
import org.tolven.el.ExpressionEvaluator;
import org.tolven.logging.TolvenLogger;
import org.tolven.doc.DocProtectionLocal;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.surescripts.BodyType;
import org.tolven.surescripts.GetPrescriber;
import org.tolven.surescripts.HeaderType;
import org.tolven.surescripts.MessageType;
import org.tolven.surescripts.PasswordType;
import org.tolven.surescripts.PharmacyLocal;
import org.tolven.surescripts.PharmacyVO;
import org.tolven.surescripts.SecurityType;
import org.tolven.surescripts.SureScriptsCommunicator;
import org.tolven.surescripts.SurescriptsLocal;
import org.tolven.surescripts.UsernameTokenType;
import org.tolven.surescripts.entity.Pharmacy;
import org.tolven.surescripts.entity.PreferredPharmacy;
import org.tolven.trim.Act;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.Observation;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.ST;
import org.tolven.trim.TrimMarshaller;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.ActRelationshipEx;
import org.tolven.trim.ex.TrimEx;
import org.tolven.trim.ex.TrimFactory;
import org.tolven.web.util.FormattedOutputWriter;
import org.tolven.xml.NamespacePrefixMapperImpl;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author mohammed
 * 
 */
@WebServlet(urlPatterns = { "*.ajaxsure" }, loadOnStartup = 5)
public class SurescriptsAjaxServlet extends HttpServlet {

	private static final String TRIMns = "urn:tolven-org:trim:4.0";
	private static final String FROM_ADDRESS = "mailto:TOLVEN.dp@surescripts.com";
	private static final String TO_ADDRESS = "mailto:SSSDIR.dp@surescripts.com";
	private static final TrimFactory trimFactory = new TrimFactory();

	@EJB private CreatorLocal creatorBean;
	@EJB private DocumentLocal docBean;
	@EJB private DocProtectionLocal docProtectionBean;
	@EJB private MenuLocal menuBean;
	@EJB private SurescriptsLocal sureBean;
	@EJB private TrimLocal trimBean;
	@EJB private TolvenPropertiesLocal tproperties;
	@EJB private PharmacyLocal pharmacyBean;
	@EJB
    private TolvenPropertiesLocal propertyBean;
    private static TrimMarshaller trimMarshaller;

	/**
	 * Init Method
	 */
	public void init(ServletConfig config) throws ServletException {
	}

	/**
	 * doGet Method
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			String uri = req.getRequestURI();
			AccountUser activeAccountUser = (AccountUser) req
					.getAttribute("accountUser");
			resp.setContentType("text/xml");
			resp.setCharacterEncoding("UTF-8");
			resp.setHeader("Cache-Control", "no-cache");
			Writer writer = resp.getWriter();
			Date now = (Date) req.getAttribute("tolvenNow");
			String element = req.getParameter("element");
			
			String keyAlgorithm = propertyBean.getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
	        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
	        PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);

			   
			if (uri.endsWith("updateDirectory.ajaxsure")) {
				String downloadFileName = sureBean.generateDirectoryDownloadMessage(userPrivateKey);
				System.getProperties().put("proxySet","true");
			    System.getProperties().put("proxyPort","3129");
			    System.getProperties().put("proxyHost","localhost");
				URLConnection con = null;
				URL url = new URL(tproperties.getProperty("surescripts.download.url")+"/"+downloadFileName);
				con = url.openConnection();
				con.setDoOutput(true);
				con.setDoInput(true);
				con.setRequestProperty("content-type", "binary/data");
				InputStream in = con.getInputStream();
				FileOutputStream fout = new FileOutputStream(downloadFileName);
				byte buffer1[] = new byte[1024*128];
				int k=0;
				while( (k = in.read(buffer1)) != -1 ){
				fout.write(buffer1,0,k);
				}
				fout.close();
				fout = null;
				getZipFiles(downloadFileName);
				TolvenLogger.info("Download Successfully Completed", SurescriptsAjaxServlet.class);
				return;

			}else if (uri.endsWith("showError.ajaxsure")) {
				String messageId = req.getParameter("messageId");
				String messageType = req.getParameter("messageType");
				String descriptionCode = null;
				String description = null;
				String message = "";
				if (messageType != null && messageType.equals("Refill Request")) {
					message = sureBean.getRefillErrorDesc(messageId);
				} else {
					message = sureBean.getMessageData(messageId);
				}
				
				if (message.contains("<Error>")) {
					if(message.contains("<DescriptionCode>"))
						descriptionCode = message.split("<DescriptionCode>")[1].split("</DescriptionCode>")[0];
					else
						descriptionCode = "Description Code is missing in the Error Message.";
					if(message.contains("<Description>"))
						description = message.split("<Description>")[1].split("</Description>")[0];
					else
						description = "Description is missing in the Error Message.";
		        }
				writer.write(descriptionCode+"|"+description);
				req.setAttribute("errorDesc", writer);
				return;
			}else if(uri.endsWith("prescReport.ajaxsure")){
			   String messageId = req.getParameter("messageId");
			   ArrayList<MessageType> messages = sureBean.retrievePatientPrescriptionReport(messageId);
			   String elmnt = req.getParameter("element");
			   MenuData md = menuBean.findMenuDataItem(activeAccountUser.getAccount().getId(), elmnt);
			   MenuData mdNew = creatorBean.createTRIMEvent(md, activeAccountUser, "reviseActive", now,userPrivateKey);
			   DocXML docXML = (DocXML) getDocBean().findDocument(
					   mdNew.getDocumentId());
			   String trimString = getDocProtectionBean()
						.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
			   TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns,	new StringReader(trimString));
			   ActRelationship  rxfill = new ActRelationshipEx();
			   rxfill.setName("RxFill");
			   Act rxFillAct = new Act();
			   ObservationValueSlot rxobsrv = new ObservationValueSlot();
			   ST st = new ST();
			   st.setLabel("RxFill relationship Testing");
			   rxobsrv.setST(st);
			   List<ObservationValueSlot> list = new ArrayList<ObservationValueSlot>();
			   list.add(rxobsrv);
			   Observation observation = new Observation();
			   rxFillAct.setObservation(observation);
			   rxFillAct.getObservation().getValues().addAll(list);
			   rxfill.setAct(rxFillAct);
			   
			   
			   ActRelationship  rxChangeRequest = new ActRelationshipEx();
			   rxChangeRequest.setName("RxChangeRequest");
			   Act rxChangeRequestAct = new Act();
			   ObservationValueSlot rxchngobsrv = new ObservationValueSlot();
			   ST st2 = new ST();
			   st2.setLabel("RxChangeRequest relationship Testing");
			   rxchngobsrv.setST(st2);
			   List<ObservationValueSlot> list1 = new ArrayList<ObservationValueSlot>();
			   list1.add(rxchngobsrv);
			   Observation observation1 = new Observation();
			   rxChangeRequestAct.setObservation(observation1);
			   rxChangeRequestAct.getObservation().getValues().addAll(list1);
			   rxChangeRequest.setAct(rxChangeRequestAct);
			   
			   
			   
			   ActRelationship  refillRequest = new ActRelationshipEx();
			   refillRequest.setName("RefillRequest");
			   Act refillRequestAct = new Act();
			   ObservationValueSlot refilreqobsrv = new ObservationValueSlot();
			   ST st1 = new ST();
			   st1.setLabel("RefillRequest relationship Testing");
			   refilreqobsrv.setST(st1);
			   List<ObservationValueSlot> list2 = new ArrayList<ObservationValueSlot>();
			   list2.add(refilreqobsrv);
			   Observation observation2 = new Observation();
			   refillRequestAct.setObservation(observation2);
			   refillRequestAct.getObservation().getValues().addAll(list2);
			   refillRequest.setAct(refillRequestAct);
			   
			   int num = trim.getAct().getRelationships().size();
			   trim.getAct().getRelationships().add(num,rxfill);
			   trim.getAct().getRelationships().add(num + 1,rxChangeRequest);
			   trim.getAct().getRelationships().add(num + 2,refillRequest);
			   /*Do the processing ..  */
			   creatorBean.marshalToDocument(trim, docXML);
			   return;
			} else if(uri.endsWith("getPrescriber.ajaxsure")){
				
				String accountId = req.getParameter("accountId").trim();
				if (sureBean.checkMasterAccountAssociation(Long.parseLong(accountId))) {
					String spi = req.getParameter("spi");
					TolvenLogger.info("SPI Number is received : "+spi+"-"+accountId+".", SurescriptsAjaxServlet.class);
					File file = createGetPrescriberMessageFile(spi, accountId);
					String result = postDirectoryMessage(file, accountId,userPrivateKey);
					if(null != result && !result.isEmpty()){
						/*Creating a result MessageType */
						DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
						factory.setNamespaceAware(true);
						Document messageDoc = null;
						DocumentBuilder builder = null;
						try {
							builder = factory.newDocumentBuilder();
							messageDoc = builder.parse(new InputSource(new StringReader(result)));
						} catch (ParserConfigurationException e1) {
						} catch (SAXException e1) {
						} catch (IOException e1) {
						}
						messageDoc.getDocumentElement().normalize();
						MessageType recievedMessage = new MessageType();
						JAXBElement< MessageType> messageJAXB = null;
						ParseXML parse = new ParseXML();
						try {
							messageJAXB = (JAXBElement< MessageType>) parse.getUnmarshaller().unmarshal(messageDoc);
							recievedMessage = messageJAXB.getValue();
						} catch (JAXBException e1) {
							e1.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						if(null != recievedMessage.getBody() && null != recievedMessage.getBody().getError()){
							writer.write("false");
						}else{
							/* Add Values in the trim and populate them in the wizard */ 
							MenuData md = menuBean.findMenuDataItem(activeAccountUser
									.getAccount().getId(), element);
							DocXML docXML = (DocXML) getDocBean().findDocument(
									md.getDocumentId());
							String trimString = getDocProtectionBean()
									.getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
							TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns, new StringReader(trimString));
							/* Set all the values in the trim. and response  */
							/* Setting Original Address*/
							if(null != recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getAddress().getAddressLine1()){
								((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(15).getST().setValue(
										recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getAddress().getAddressLine1()	);
							}
							/* Setting Original City*/
							if(null != recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getAddress().getCity()){
								((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(16).getST().setValue(
										recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getAddress().getCity()	);
							}
							
							/* Setting Original State*/
							if(null != recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getAddress().getState()){
								((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(17).getST().setValue(
										recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getAddress().getState()	);
							}
							
							/* Setting Original Zip*/
							if(null != recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getAddress().getZipCode()){
								((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(18).getST().setValue(
										recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getAddress().getZipCode());
							}
							/* First Name is set*/
							((ActEx)trim.getAct()).getParticipation().get("subject").getRole().getPlayer().getName().getENS().get(0).getParts().get(0).getST().setValue
								(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getName().getFirstName());
							/* Middle Name is set*/
							((ActEx)trim.getAct()).getParticipation().get("subject").getRole().getPlayer().getName().getENS().get(0).getParts().get(1).getST().setValue
							(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getName().getMiddleName());
							/* Last Name is set*/
							((ActEx)trim.getAct()).getParticipation().get("subject").getRole().getPlayer().getName().getENS().get(0).getParts().get(2).getST().setValue
							(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getName().getLastName());
						    
							/* EMail is set*/
							((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(11).getST().setValue
							(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getEmail());
							
							/* Telephone is set*/
							if(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getPhoneNumbers().getPhone().get(0).getNumber().length() > 10){
								((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(12).getST().setValue
								(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getPhoneNumbers().getPhone().get(0).getNumber().substring(0,10));
								
								/* Extension is set*/
								((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(13).getST().setValue
								(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getPhoneNumbers().getPhone().get(0).getNumber().substring(11));
								
								//writer.write(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getPhoneNumbers().getPhone().get(0).getNumber().substring(0,10)+"|"+recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getPhoneNumbers().getPhone().get(0).getNumber().substring(11)+"|");
							}else{
								((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(12).getST().setValue
								(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getPhoneNumbers().getPhone().get(0).getNumber());
								//writer.write(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getPhoneNumbers().getPhone().get(0).getNumber()+"|"+""+"|");
							}	
							/* Fax is set */
							((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(8).getST().setValue
							(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getPhoneNumbers().getPhone().get(1).getNumber());
							writer.write(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getPhoneNumbers().getPhone().get(1).getNumber()+"|");
							
							
							/* Account ID is set*/
							((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(0).getINT().setValue
							(Long.parseLong(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getDirectoryInformation().getAccountID()));
							
							/* Portal ID is set*/
							((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(1).getINT().setValue
							(Long.parseLong(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getDirectoryInformation().getPortalID()));
									
							/* DEA Number and SPI Number are set */
							for(int i=0; i< recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().size(); i++){
					        	  if(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("DEANumber")){
					        		  ((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(7).getST().setValue(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue());
					        		//  writer.write(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue()+"|");
					        	  }else if(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getName().getLocalPart().equals("SPI")){
					        		  ((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(6).getST().setValue(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue());
					        		//  writer.write(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getIdentification().getSPIOrFileIDOrStateLicenseNumber().get(i).getValue()+"|");
					        	  }
				      	    }	
							
							/* Service Level is set */
							((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(3).getST().setValue
							(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getDirectoryInformation().getServiceLevel());
							
							/* Active Start Time */
							((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(4).setTS
							(trimFactory.createNewTS(convertToWizardTime(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getDirectoryInformation().getActiveStartTime())));
							//writer.write(convertToWizardTime(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getDirectoryInformation().getActiveStartTime())+"|");
							/* Active End Time */
							((ActEx)trim.getAct()).getRelationship().get("prescriber").getAct().getObservation().getValues().get(5).setTS
							(trimFactory.createNewTS(convertToWizardTime(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getDirectoryInformation().getActiveEndTime())));
							//writer.write(convertToWizardTime(recievedMessage.getBody().getGetPrescriberResponse().getPrescriber().getDirectoryInformation().getActiveEndTime())+"|");
							creatorBean.marshalToDocument(trim, docXML);
							
						}	
					}
				} else {
					TolvenLogger.error("Master User Account Not Associated with this Account: " +
							accountId, SurescriptsAjaxServlet.class);
					writer.write("No Master Account");
				} 
				
				req.setAttribute("response", writer);
				return;
			} else if(uri.endsWith("changeQualDescription.ajaxsure")){
				MenuData md  = menuBean.findMenuDataItem(activeAccountUser.getAccount().getId(), element);
                DocXML docXML = (DocXML) getDocBean().findDocument(md.getDocumentId());
                String trimString = this.getDocProtectionBean().getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
                TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns, new StringReader(trimString));
                String drugDesc = sureBean.retrieveDrugQualDescription(req.getParameter("qualCode"));
				((ActEx)trim.getAct()).getRelationship().get("toSureScripts").getAct().getObservation().getValues().get(61).getST().setValue(drugDesc);
				creatorBean.marshalToDocument(trim, docXML);
				writer.write(drugDesc);
                req.setAttribute("activeWriter", writer);
                return;
			}else if (uri.endsWith("retrieveXML.ajaxsure")) { // To show the SS message(s) to end user.
				String xml = null;
				String id = req.getParameter("id");
				TolvenLogger.info("Id : " + id, ErxAjaxServlet.class);
				xml = sureBean.retrieveXml(id);
				writer.write(xml);
				return;
			}  else if (uri.endsWith("insertPharmacyData.ajaxsure")) {
				String ncpdpId = req.getParameter("ncpdpId");
				String divId = req.getParameter("divId");
				
				if (ncpdpId.length() > 0) {
					PharmacyVO pharmacyVO = pharmacyBean.findPharmacyById(ncpdpId);
					
					if (divId.equals("pharmacyWiz")) {
						MenuData md = menuBean.findMenuDataItem(activeAccountUser.getAccount().getId(), element);
						DocXML docXML = (DocXML) getDocBean().findDocument(md.getDocumentId());
						String trimString = getDocProtectionBean().getDecryptedContentString(docXML, activeAccountUser,userPrivateKey);
						TrimEx trim = (TrimEx) getTrimMarshaller().unmarshal(TRIMns, new StringReader(trimString));
						DateFormat df = new SimpleDateFormat("yyyyMMdd");
						
						((ActEx)trim.getAct()).getRelationship().get("pharmacyId").getAct().getObservation().
						getValues().get(0).getST().setValue(pharmacyVO.getNcpdpid());
						
						List<ObservationValueSlot> pharmacyDetails = ((ActEx)trim.getAct()).getRelationship().
							get("pharmacyDetails").getAct().getObservation().getValues();
						
						if (pharmacyVO.getStoreName() != null) {
							pharmacyDetails.get(0).getST().setValue(pharmacyVO.getStoreName());
						}
						if (pharmacyVO.getAddressLine1() != null) {
							pharmacyDetails.get(1).getST().setValue(pharmacyVO.getAddressLine1());
						}
						if (pharmacyVO.getCity() != null) {
							pharmacyDetails.get(2).getST().setValue(pharmacyVO.getCity());
						}
						if (pharmacyVO.getState() != null) {
							pharmacyDetails.get(3).getST().setValue(pharmacyVO.getState());
						}
						if (pharmacyVO.getZip() != null) {
							pharmacyDetails.get(4).getST().setValue(String.valueOf(pharmacyVO.getZip()));
						}
						if (pharmacyVO.getPhonePrimary() != null) {
							pharmacyDetails.get(5).getST().setValue(pharmacyVO.getPhonePrimary());
						}
						if (pharmacyVO.getFax() != null) {
							pharmacyDetails.get(6).getST().setValue(pharmacyVO.getFax());
						}
						if (pharmacyVO.getEmail() != null) {
							pharmacyDetails.get(7).getST().setValue(pharmacyVO.getEmail());
						}
						if (pharmacyVO.getPhoneAlt1() != null) {
							pharmacyDetails.get(8).getST().setValue(pharmacyVO.getPhoneAlt1());
						}
						if (pharmacyVO.getPhoneAlt1Qualifier() != null) {
							pharmacyDetails.get(9).getST().setValue(pharmacyVO.getPhoneAlt1Qualifier());
						}
						if (pharmacyVO.getActiveStartTime() != null) {
							pharmacyDetails.get(10).getTS().setValue(df.format(pharmacyVO.getActiveStartTime()));
						}
						if (pharmacyVO.getActiveEndTime() != null) {
							pharmacyDetails.get(11).getTS().setValue(df.format(pharmacyVO.getActiveEndTime()));
						}
						if (pharmacyVO.getServiceLevel() != null) {
							pharmacyDetails.get(12).getST().setValue(String.valueOf(pharmacyVO.getServiceLevel()));
						}
						if (pharmacyVO.getPartnerAccount() != null) {
							pharmacyDetails.get(13).getST().setValue(pharmacyVO.getPartnerAccount());
						}
						if (pharmacyVO.getLastModifiedDate() != null) {
							pharmacyDetails.get(14).getTS().setValue(df.format(pharmacyVO.getLastModifiedDate()));
						}
						if (pharmacyVO.getNpi() != null) {
							pharmacyDetails.get(15).getST().setValue(String.valueOf(pharmacyVO.getNpi()));
						}
						creatorBean.marshalToDocument(trim, docXML);
					}
					
					DateFormat dateForm = new SimpleDateFormat("dd/MM/yyyy");
					writer.write("<table class=\"pharmacyTable\">");
					if (pharmacyVO.getNcpdpid() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">NCPDP ID:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getNcpdpid()+"</td></tr>");
					}
					if (pharmacyVO.getStoreName() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Store Name:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getStoreName()+"</td></tr>");
					}
					if (pharmacyVO.getAddressLine1() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Address Line1:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getAddressLine1()+"</td></tr>");
					}
					if (pharmacyVO.getCity() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">City:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getCity()+"</td></tr>");
					}
					if (pharmacyVO.getState() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">State:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getState()+"</td></tr>");
					}
					if (pharmacyVO.getZip() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Zip:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getZip()+"</td></tr>");
					}
					if (pharmacyVO.getPhonePrimary() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Primary Phone:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getPhonePrimary()+"</td></tr>");
					}
					if (pharmacyVO.getFax() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Fax:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getFax()+"</td></tr>");
					}
					if (pharmacyVO.getEmail() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Email:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getEmail()+"</td></tr>");
					}
					if (pharmacyVO.getActiveStartTime() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Active Start Time:</td>" +
								"<td class=\"pharmValue\">"+ dateForm.format(pharmacyVO.getActiveStartTime())+"</td></tr>");
					}
					if (pharmacyVO.getActiveEndTime() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Active End Time:</td>" +
								"<td class=\"pharmValue\">"+ dateForm.format(pharmacyVO.getActiveEndTime())+"</td></tr>");
					}
					if (pharmacyVO.getServiceLevel() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Service Level:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getServiceLevel()+"</td></tr>");
					}
					if (pharmacyVO.getPartnerAccount() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Partner Account:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getPartnerAccount()+"</td></tr>");
					}
					if (pharmacyVO.getLastModifiedDate() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">Last Modified Date:</td>" +
								"<td class=\"pharmValue\">"+ dateForm.format(pharmacyVO.getLastModifiedDate())+"</td></tr>");
					}
					if (pharmacyVO.getNpi() != null) {
						writer.write("<tr class=\"pharmRow\"><td class=\"pharmLabel\">NPI:</td>" +
								"<td class=\"pharmValue\">"+ pharmacyVO.getNpi()+"</td></tr>");
					}
					writer.write("</table>");
				}
				
				req.setAttribute("response", writer);
				return;
			}
			/*
	    	 * This block is used to get pharmacy details
	    	 * 
	    	 * @author Suja Sundaresan
	    	 * added on 05/Dec/2010
	    	 */
			else if(uri.endsWith("pharmacyData.ajaxsure")){
				String listPath = req.getParameter("path");
				MenuPath path = new MenuPath( listPath );
				FormattedOutputWriter fow = new HTMLOutputWriter(writer, "_default", activeAccountUser, now);
				ExpressionEvaluator ee = fow.getExpressionEvaluator();
				ee.pushContext();
				Enumeration names = req.getParameterNames();
				while (names.hasMoreElements()) {
					String key = (String) names.nextElement();
					ee.addVariable(key, req.getParameter(key));
				}
				
				writer.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
				writer.write( "<ajax-response>\n" );
				writer.write( "<response type=\"object\" id=\"" + listPath + "-LG" + "_updater\">\n" );
				writer.write( "<rows update_ui='true'>\n" );
				Map<String, Object> params = new HashMap<String, Object>();
				if (ee.get("offset")!=null)
					params.put("offset", ee.get("offset").toString());
				if (ee.get("page_size")!=null)
					params.put("page_size", ee.get("page_size").toString());
				if (ee.get("sort_col")!=null && !ee.get("sort_col").toString().equals(""))
					params.put("sort_col", ee.get("sort_col").toString());
				if (ee.get("sort_dir")!=null && !ee.get("sort_dir").toString().equals(""))
					params.put("sort_dir", ee.get("sort_dir").toString());
				if (ee.get("filter")!=null && !ee.get("filter").toString().equals(""))
					params.put("filter", ee.get("filter").toString());
				
				List<Pharmacy> pharmacies = pharmacyBean.findPharmacies(params);
				String serviceLevel = "";
				for (Pharmacy pharmacy : pharmacies) {
					serviceLevel = "";
					if (pharmacy.getNewRx())
						serviceLevel += "<label style=\"background:#4E9689;float:left;height: 6px; width: 14px;margin:2px;\" title=\"NewRx\"> </label>";
					if (pharmacy.getRefReq())
						serviceLevel += "<label style=\"background:#87D69B;float:left;height: 6px; width: 14px;margin:2px;\" title=\"RefReq/RefRes\"> </label>";
					if (pharmacy.getRxChg())
						serviceLevel += "<label style=\"background:#FDCDB9;float:left;height: 6px; width: 14px;margin:2px;\" title=\"RxChg/ChgRes\"> </label>";
					if (pharmacy.getRxFill())
						serviceLevel += "<label style=\"background:#EDB24C;float:left;height: 6px; width: 14px;margin:2px;\" title=\"RxFill\"> </label>";
					if (pharmacy.getCanRx())
						serviceLevel += "<label style=\"background:#ED8A4C;float:left;height: 6px; width: 14px;margin:2px;\" title=\"CanRx/CanRxRes\"> </label>";
					if (pharmacy.getMedicatioHistory())
						serviceLevel += "<label style=\"background:#FF9CA8;float:left;height: 6px; width: 14px;margin:2px;\" title=\"Medication History\"> </label>";
					if (pharmacy.getEligibility())
						serviceLevel += "<label style=\"background:#FA7089;float:left;height: 6px; width: 14px;margin:2px;\" title=\"Eligibility\"> </label>";
					if (pharmacy.getActiveEndTime().before(now))
						serviceLevel += "<label style=\"background:#C12F70;float:left;height: 6px; width: 14px;margin:2px;\" title=\"Inactive\"> </label>";
					if (pharmacy.getActiveEndTime().before(now) && pharmacy.getServiceLevel().longValue()==0)
						serviceLevel += "<label style=\"background:#731E45;float:left;height: 6px; width: 14px;margin:2px;\" title=\"Disabled\"> </label>";
					
					fow.writeVerbatim( "<tr>");
					fow.writeVerbatim( "<td>" + serviceLevel + "</td>");	
					if (pharmacy.getActiveEndTime().before(now) || (pharmacy.getActiveEndTime().before(now) && pharmacy.getServiceLevel().longValue()==0)) 
						fow.writeVerbatim( "<td>" + pharmacy.getStoreName().replace("&", "&amp;") + "</td>");
					else
						fow.writeVerbatim( "<td><a href=\"javascript:insertPharmacyDetails('" + pharmacy.getId() + "', '" + element + "', 'pharmacyWiz', 1);\">" + pharmacy.getStoreName().replace("&", "&amp;") + "</a></td>");
					fow.writeVerbatim( "<td>" + pharmacy.getAddressLine1().replace("&", "&amp;")  + "</td>");
					fow.writeVerbatim( "<td>" + pharmacy.getCity() + "</td>");
					fow.writeVerbatim( "<td>" + pharmacy.getState() + "</td>");
					fow.writeVerbatim( "<td>" + pharmacy.getZip() + "</td>");
					fow.writeVerbatim( "</tr>\n");
				}
				ee.popContext();
				writer.write( "</rows>\n");
				writer.write( "</response>\n");
				writer.write( "</ajax-response>\n");
				req.setAttribute("activeWriter", writer);
			}
			/*
	    	 * This block is used to count pharmacies
	    	 * 
	    	 * @author Suja Sundaresan
	    	 * added on 05/Dec/2010
	    	 */
		    else if (uri.endsWith("countPharmacy.ajaxsure")) {
		    	FormattedOutputWriter fow = new HTMLOutputWriter(writer, "_default", activeAccountUser, now);
		    	ExpressionEvaluator ee = fow.getExpressionEvaluator();
				ee.pushContext();
				Enumeration names = req.getParameterNames();
				while (names.hasMoreElements()) {
					String key = (String) names.nextElement();
					ee.addVariable(key, req.getParameter(key));
				}
		    	Long offest = new Long(0);
				if (ee.get("offset")!=null)
					offest = new Long(ee.get("offset").toString());
				Long limit = new Long(10);
				if (ee.get("page_size")!=null)
					limit = new Long(ee.get("page_size").toString());
				String filterValue = null;
				if (ee.get("filter")!=null && !ee.get("filter").toString().equals(""))
					filterValue = ee.get("filter").toString();
				
				long count = pharmacyBean.countPharmacies(offest, limit, filterValue);
				writer.write(Long.toString(count));
	    		req.setAttribute("activeWriter", writer);
				return;
		    }
			/*
	    	 * This block is used to get preffered pharmacy details
	    	 * 
	    	 * @author Suja Sundaresan
	    	 * added on 05/Dec/2010
	    	 */
		    else if(uri.endsWith("preferredPharmacyData.ajaxsure")){
				MenuPath path = new MenuPath( element );

				FormattedOutputWriter fow = new HTMLOutputWriter(writer, "_default", activeAccountUser, now);
				ExpressionEvaluator ee = fow.getExpressionEvaluator();
				ee.pushContext();
				Enumeration names = req.getParameterNames();
				while (names.hasMoreElements()) {
					String key = (String) names.nextElement();
					ee.addVariable(key, req.getParameter(key));
				}
				writer.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
				writer.write( "<ajax-response>\n" );
				writer.write( "<response type=\"object\" id=\"" + element + "-LG" + "_updater\">\n" );
				writer.write( "<rows update_ui='true'>\n" );
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("account", activeAccountUser.getAccount());
				params.put("patientId", element.split("-")[1].split(":")[0]);
				if (ee.get("offset")!=null)
					params.put("offset", ee.get("offset").toString());
				if (ee.get("page_size")!=null)
					params.put("page_size", ee.get("page_size").toString());
				if (ee.get("sort_col")!=null && !ee.get("sort_col").toString().equals(""))
					params.put("sort_col", ee.get("sort_col").toString());
				if (ee.get("sort_dir")!=null && !ee.get("sort_dir").toString().equals(""))
					params.put("sort_dir", ee.get("sort_dir").toString());
				if (ee.get("filter")!=null && !ee.get("filter").toString().equals(""))
					params.put("filter", ee.get("filter").toString());
				
				List<PreferredPharmacy> pharmacies = pharmacyBean.findPreferredPharmacies(params);
				String serviceLevel = "";
				for (PreferredPharmacy pharmacy : pharmacies) {
					serviceLevel = "";
					if (pharmacy.getNewRx())
						serviceLevel += "<label style=\"background:#4E9689;float:left;height: 6px; width: 14px;margin:2px;\" title=\"NewRx\"> </label>";
					if (pharmacy.getRefReq())
						serviceLevel += "<label style=\"background:#87D69B;float:left;height: 6px; width: 14px;margin:2px;\" title=\"RefReq/RefRes\"> </label>";
					if (pharmacy.getRxChg())
						serviceLevel += "<label style=\"background:#FDCDB9;float:left;height: 6px; width: 14px;margin:2px;\" title=\"RxChg/ChgRes\"> </label>";
					if (pharmacy.getRxFill())
						serviceLevel += "<label style=\"background:#EDB24C;float:left;height: 6px; width: 14px;margin:2px;\" title=\"RxFill\"> </label>";
					if (pharmacy.getCanRx())
						serviceLevel += "<label style=\"background:#ED8A4C;float:left;height: 6px; width: 14px;margin:2px;\" title=\"CanRx/CanRxRes\"> </label>";
					if (pharmacy.getMedicatioHistory())
						serviceLevel += "<label style=\"background:#FF9CA8;float:left;height: 6px; width: 14px;margin:2px;\" title=\"Medication History\"> </label>";
					if (pharmacy.getEligibility())
						serviceLevel += "<label style=\"background:#FA7089;float:left;height: 6px; width: 14px;margin:2px;\" title=\"Eligibility\"> </label>";
					if (pharmacy.getInactive())
						serviceLevel += "<label style=\"background:#C12F70;float:left;height: 6px; width: 14px;margin:2px;\" title=\"Inactive\"> </label>";
					if (pharmacy.getDisabled())
						serviceLevel += "<label style=\"background:#731E45;float:left;height: 6px; width: 14px;margin:2px;\" title=\"Disabled\"> </label>";
					
					fow.writeVerbatim( "<tr>");
					fow.writeVerbatim( "<td>" + serviceLevel + "</td>");	
					fow.writeVerbatim( "<td>" + pharmacy.getId() + "</td>");	
					if (pharmacy.getInactive() || pharmacy.getDisabled()) 
							fow.writeVerbatim( "<td>" + pharmacy.getStoreName().replace("&", "&amp;") + "</td>");
					else
						fow.writeVerbatim( "<td><a href=\"javascript:showPane('" + pharmacy.getParentPath() + ":pharmacy-" +pharmacy.getMenuDataId() + "',false,'" + pharmacy.getParentPath() + ":pharmacies:all');\">" + pharmacy.getStoreName().replace("&", "&amp;") + "</a></td>");
					fow.writeVerbatim( "<td>" + pharmacy.getAddressLine1().replace("&", "&amp;")  + "</td>");
					fow.writeVerbatim( "<td>" + pharmacy.getCity() + "</td>");
					fow.writeVerbatim( "<td>" + pharmacy.getState() + "</td>");
					fow.writeVerbatim( "<td>" + pharmacy.getZip() + "</td>");
					fow.writeVerbatim( "</tr>\n");
				}
				ee.popContext();
				writer.write( "</rows>\n");
				writer.write( "</response>\n");
				writer.write( "</ajax-response>\n");
				req.setAttribute("activeWriter", writer);
			}
			/*
	    	 * This block is used to count preferred pharmacies
	    	 * 
	    	 * @author Suja Sundaresan
	    	 * added on 05/Dec/2010
	    	 */
		    else if (uri.endsWith("countPreferredPharmacy.ajaxsure")) {
		    	FormattedOutputWriter fow = new HTMLOutputWriter(writer, "_default", activeAccountUser, now);
		    	ExpressionEvaluator ee = fow.getExpressionEvaluator();
				ee.pushContext();
				Enumeration names = req.getParameterNames();
				while (names.hasMoreElements()) {
					String key = (String) names.nextElement();
					ee.addVariable(key, req.getParameter(key));
				}
		    	Long offest = new Long(0);
				if (ee.get("offset")!=null)
					offest = new Long(ee.get("offset").toString());
				Long page_size = new Long(10);
				if (ee.get("page_size")!=null)
					page_size = new Long(ee.get("page_size").toString());
				String filter = null;
				if (ee.get("filter")!=null && !ee.get("filter").toString().equals(""))
					filter = ee.get("filter").toString();
				
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("account", activeAccountUser.getAccount());
				params.put("patientId", element.split("-")[1].split(":")[0]);
				if (ee.get("offset")!=null)
					params.put("offset", ee.get("offset").toString());
				if (ee.get("page_size")!=null)
					params.put("page_size", ee.get("page_size").toString());
				if (ee.get("filter")!=null && !ee.get("filter").toString().equals(""))
					params.put("filter", ee.get("filter").toString());
				
				long count = pharmacyBean.countPreferredPharmacies(params);
				writer.write(Long.toString(count));
	    		req.setAttribute("activeWriter", writer);
				return;
		    }
		} catch (Exception e) {
			throw new ServletException(
					"New Exception thrown in SurescriptsAjaxServlet", e);
		}
	}
	

	public class HTMLOutputWriter extends FormattedOutputWriter {
		public HTMLOutputWriter(Writer writer, String type, AccountUser accountUser, Date now) {
			super(writer, type, accountUser, now);
		}

		/**
		 * Write a character adding escape characters for things like < >
		 * @param writer
		 * @param val
		 * @throws IOException 
		 */
		@Override
		public void writeEscape( char c ) throws IOException {
		    	switch ( c ) {
		    	case '<' : writer.write( "&lt;");break;
		    	case '>' : writer.write( "&gt;");break;
		    	case '&' : writer.write( "&amp;");break;
		    	case '"' : writer.write( "&quot;");break;
		    	default : writer.write(c);
			}
		}
	}
	private Date convertToWizardTime(String activeStartTime) {
		Date wizardTime = new Date();
		String year = activeStartTime.substring(0,4);
		String month = activeStartTime.substring(5,7);
		String day = activeStartTime.substring(8,10);
		int hour = Integer.parseInt(activeStartTime.substring(11,13));
		String minute = activeStartTime.substring(14,16);
//		wizardTime = wizardTime + day + "/" + month + "/" + year + " ";
//		if(hour <= 12){
//			wizardTime = wizardTime	+ hour + ":" +minute + " AM";
//		}else{
//			hour = hour - 12;
//			wizardTime = wizardTime	+ hour+ ":" +minute + " PM";
//		}	
		wizardTime.setYear(Integer.parseInt(year) - 1900); 
		wizardTime.setMonth(Integer.parseInt(month)); 
		wizardTime.setDate(Integer.parseInt(day));
		return wizardTime;
	}

	/**
	 * Method to create the file to be sent for Get Prescriber
	 * @param spi
	 * @param accountId 
	 */
	private File createGetPrescriberMessageFile(String spi, String accountId) {
		String locationOfNewPrescriberMessage = new String(tproperties.getProperty("eprescription.surescripts.messages.directory")+"/outbox/");
		File messageOutbox = new File(locationOfNewPrescriberMessage);
		if (!messageOutbox.exists())
			messageOutbox.mkdirs();
		/* Header Body */
		HeaderType mainHeader = new HeaderType();
//		AppVersionType appVersion = new AppVersionType();
		String source = FROM_ADDRESS;
		String destination = TO_ADDRESS;
		SecurityType security = new SecurityType();
		UsernameTokenType userToken = new UsernameTokenType();
		userToken.setUsername(tproperties.getProperty("eprescription.surescripts.adminconsole.username"));
		PasswordType password = new PasswordType();
		password.setType("PasswordDigest");
		password.setValue(encodeDirectoryCredentials(tproperties.getProperty("eprescription.surescripts.adminconsole.password")));
		userToken.setPassword(password);
		security.setUsernameToken(userToken);
		String messageId = generateMessageId(String.valueOf((new Date().getTime())));
		mainHeader.setFrom(source);
		mainHeader.setTo(destination);
		mainHeader.setMessageID(messageId);
		mainHeader.setSecurity(security);
		mainHeader.setSentTime(getCurrentUTC());
		
		MessageType message = new MessageType();
		BodyType body = new BodyType();
		GetPrescriber getPrescriber = new GetPrescriber();
		getPrescriber.setSPI(spi);
		body.setGetPrescriber(getPrescriber);
		message.setBody(body);
		message.setHeader(mainHeader);
		
		locationOfNewPrescriberMessage = locationOfNewPrescriberMessage + "GetPrescriber_"+spi+ ".xml";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		File file = new File(locationOfNewPrescriberMessage);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			ParseXML parse = new ParseXML();
			parse.getMarshaller().marshal(message, file); // Written to the File
			parse.getMarshaller().marshal(message, doc);
			
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans;
			StringWriter sw = new StringWriter();
			try {
				trans = tf.newTransformer();
				trans.transform(new DOMSource(doc), new StreamResult(sw));
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			String messageString = sw.toString().replaceAll("'","''");
			ArrayList<String> result = new ArrayList<String>();
			sureBean.persistSurescriptsMessages(messageId, "GetPrescriber", messageString, "outgoing", "0" , "0", accountId, null);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return file;
	}

	/**
	 * Method that extracts the downloaded Zip File and copies the contents to FlatFile.txt
	 * @param filename
	 */
	public void getZipFiles(String filename){
		  try{
				 try
			        {
					    String entryName;
					    String destinationname = tproperties.getProperty("eprescription.surescripts.download.directory");
					 	File downloadDir = new File(destinationname);
					 	if (!downloadDir.exists()) 
					 		downloadDir.mkdirs();
			            byte[] buf = new byte[1024];
			            ZipInputStream zipinputstream = null;
			            ZipEntry zipentry;
			            zipinputstream = new ZipInputStream(
			                new FileInputStream(filename));

			            zipentry = zipinputstream.getNextEntry();
			            while (zipentry != null) 
			            { 
			                //for each entry to be extracted
			                entryName = zipentry.getName();
			                int n;
			                FileOutputStream fileoutputstream;
			                File newFile = new File(entryName);
			                String directory = newFile.getParent();
			                
			                if(directory == null)
			                {
			                    if(newFile.isDirectory())
			                        break;
			                }
			                
			                if(!destinationname.endsWith("/")){
			                	destinationname = destinationname+"/";
			                }
			                fileoutputstream = new FileOutputStream(
			                   destinationname+"FlatFile.txt");             

			                while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
			                    fileoutputstream.write(buf, 0, n);

			                fileoutputstream.close(); 
			                zipinputstream.closeEntry();
			                zipentry = zipinputstream.getNextEntry();

			            }//while

			            zipinputstream.close();
			        }
			        catch (Exception e)
			        {
			            e.printStackTrace();
			        }
			    

			}catch (Exception et){
		            et.printStackTrace();
		    }
	  }

	/**
	 * @return the sureBean
	 */
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
	 * @return the docBean
	 */
	public DocumentLocal getDocBean() {
		return docBean;
	}

	/**
	 * @param docBean the docBean to set
	 */
	public void setDocBean(DocumentLocal docBean) {
		this.docBean = docBean;
	}

	/**
	 * @return the docProtectionBean
	 */
	public DocProtectionLocal getDocProtectionBean() {
		return docProtectionBean;
	}

	/**
	 * @param docProtectionBean the docProtectionBean to set
	 */
	public void setDocProtectionBean(DocProtectionLocal docProtectionBean) {
		this.docProtectionBean = docProtectionBean;
	}
	
	/**
	 * Method to post Directory messages to surescripts.
	 * @param file
	 */
	private String postDirectoryMessage(File file, String accountId,PrivateKey privateKey) {
		String resultString = "";
		try {
			 	System.getProperties().put("proxySet","true");
			    System.getProperties().put("proxyPort","3129");
			    System.getProperties().put("proxyHost","localhost");
			    URL url = new URL(tproperties.getProperty("surescripts.post.directory.url"));
			FileReader fr = new FileReader(file);
			char[] buffer = new char[1024*10];
			int b_read = 0;
			if ((b_read = fr.read(buffer)) != -1) {
				URLConnection urlc = url.openConnection();
				urlc.setConnectTimeout(30000);
				urlc.setReadTimeout(30000);
				urlc.setRequestProperty("Host","localhost");
//				urlc.setRequestProperty("Authorization","Tolven");
				urlc.setRequestProperty("Content-Type","text/xml");
				urlc.setDoOutput(true);
				urlc.setDoInput(true);
				TolvenLogger.info("Message being sent to SURESCRIPTS......Awaiting Response", SurescriptsAjaxServlet.class);
				PrintWriter pw = new PrintWriter(urlc.getOutputStream());
				pw.write(buffer, 0, b_read);
				pw.close();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
				String res_line;
				StringBuffer result = new StringBuffer();
				while ((res_line = in.readLine()) != null){
					result.append(res_line);
				}
				in.close();
				
				if(result.length() > 0){
					resultString = result.toString();
					TolvenLogger.info("SURESCRIPTS Response Recieved Successfully", SureScriptsCommunicator.class);
					sureBean.convertToMessage(resultString , false, accountId,privateKey);
				}
			}
		}
		catch(Exception ee) {
			System.out.println(ee.getMessage());
		}
		
		return resultString;
	}
	
	/**
	 * Generates message id.
	 * 
	 * @param plain
	 * @return
	 */
	public static String generateMessageId(String plain) {
		byte[] cipher = new byte[35];
		String messageId = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(plain.getBytes());
			cipher = md5.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < cipher.length; i++) {
				String hex = Integer.toHexString(0xff & cipher[i]);
				if (hex.length() == 1)
					sb.append('0');
				sb.append(hex);
			}
			StringBuffer pass = new StringBuffer();
			pass.append(sb.substring(0, 6));
			pass.append("H");
			pass.append(sb.substring(6, 11));
			pass.append("H");
			pass.append(sb.substring(11, 21));
			pass.append("H");
			pass.append(sb.substring(21));
			messageId = new String(pass);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return messageId;
	}
	
	/**
     * Method to return the current time in UTC Format
     * @return UTC(Coordinated Universal Time) eg: 09-04-2010T21:37:27.2Z
     */
    private String getCurrentUTC(){
        Date now = Calendar.getInstance().getTime();       
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(now);       
        formatter = new SimpleDateFormat("HH:mm:ss.F");
        String time = formatter.format(now);       
        return date+"T"+time+"Z";
    }

    /**
	 * Method to encode the password for Directory / Administrative Messages
	 * @param password
	 * @return
	 */
	private String encodeDirectoryCredentials(String password){
		String result = "";
		try {
			result = result + new String (new Base64().encode(MessageDigest.getInstance("SHA1").digest(password.trim().toUpperCase().getBytes("UTF-16LE"))));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
    /**
     * Private Class to be used for creating a file
     * @author root
     *
     */
    private class ParseXML {
        private JAXBContext jc;
      
       
        protected String getParsePackageName(){
            return "org.tolven.surescripts";
        }
      
        protected JAXBContext getJAXBContext() throws JAXBException {
            if (jc==null) {
                jc = JAXBContext.newInstance(getParsePackageName(), getClass().getClassLoader());
            }
            return jc;
        }
      
        protected Unmarshaller getUnmarshaller() throws JAXBException {
            return getJAXBContext().createUnmarshaller();
        }

        protected Marshaller getMarshaller() throws JAXBException {
            Marshaller m = getJAXBContext().createMarshaller();
            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            m.setProperty(  "com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
            return m;

        }
    }

    private TrimMarshaller getTrimMarshaller() {
        if(trimMarshaller == null) {
            trimMarshaller = new TrimMarshaller();
        }
        return trimMarshaller;
    }
    
}
