/*
 * Copyright (C) 2009 Tolven Inc

 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;  
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 *
 * Contact: info@tolvenhealth.com 
 *
 * @author <your name>
 * @version $Id: ProcessHL7.java,v 1.1 2013/05/24 06:35:44 schillingerExp $
 */  

package org.tolven.HL7PatientExample.process;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.drools.StatefulSession;
import org.tolven.app.AppEvalAdaptor;
import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuStructure;

import org.tolven.core.entity.Account;
import org.tolven.core.entity.Status;
import org.tolven.doc.bean.TolvenMessage;
import org.tolven.doc.bean.DocumentBean;
import org.tolven.doc.bean.TolvenMessageWithAttachments;
import org.tolven.doc.entity.CCRException;
import org.tolven.doc.entity.DocBase;
import org.tolven.doctype.DocumentType;
import org.tolven.el.ExpressionEvaluator;
import org.tolven.trim.ex.HL7DateFormatUtility;
import org.tolven.trim.TS;

import org.tolven.HL7PatientExample.api.ProcessHL7Local;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;

import ca.uhn.hl7v2.validation.builder.support.DefaultValidationBuilder;
import ca.uhn.hl7v2.validation.builder.support.NoValidationBuilder;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.model.GenericMessage;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.AL1;
import ca.uhn.hl7v2.model.v25.segment.DG1;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.GenericModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;


@Stateless
@Local( ProcessHL7Local.class )
public class ProcessHL7 extends AppEvalAdaptor implements ProcessHL7Local {
	
    private static final String DOCns = "urn:hl7-org:v2";	
	private static Logger logger = Logger.getLogger(ProcessHL7.class);
	//see method cleanup() for instVars
    private ExpressionEvaluator hl7ee;
    private HapiContext context;
    private CanonicalModelClassFactory mcf;
    private PipeParser parser;
	private String s;
//	private ContinuityOfCareRecordEx ccr;
//	private static CCRMarshaller ccrMarshaller;
	
	public ProcessHL7() {
	}
	
	@Override
	
	public void process(Object message, Date now ) {
//	logger.info( "HL7 PROCESSOR:  Attempting to process ");
		try {
			if (message instanceof TolvenMessage || 
					message instanceof TolvenMessageWithAttachments) { 
				TolvenMessage tm = (TolvenMessage) message;
					logger.info( "HL7 PROCESSOR:  XMLNS " + DOCns + tm.getXmlNS()  );
				// We're only interested in HL7 messages
				if (DOCns.equals(tm.getXmlNS())) {
					associateHL7Document( tm, now );
					// Run the rules now
					runRules( );
					logger.info( "Processed HL7 document for account: " + tm.getAccountId());
//					logger.info( "Processed HL7 document " + getDocument().getId() + " for account: " + tm.getAccountId());
				}  else {
//				logger.info( "HL7 Processor didn't detect HL7 document");
				}
			} else {
//				logger.info( "HL7 Processor received something other than tolven message");

			}
		} catch (Exception e) {
			throw new RuntimeException( "Exception in HL7 processor", e);
		} finally {
		    cleanup();
		}
	}
	@Override
		protected void loadWorkingMemory(StatefulSession workingMemory) throws Exception {
	logger.info( "HL7 PROCESSOR:  load working mem ");
		try {
			processMessage(workingMemory);

//			bindToPatients( );
		} catch (Exception e) {
			throw new RuntimeException( "Unable to process HL7 message document", e );
		}
	}
	
	/**
	 * Process a single HL7 message. 
	 * @param tm A TolvenMessage
	 * @throws JAXBException 
	 * @throws CCRException 
	 * @throws ParseException 
	 * @throws CCRException 
	 * @throws IOException 
	 * @throws IOException 
	 * 
	 * This is a simple POC HL7 processor that uses the TERSER style parsing to pick out specific items from the pipe style message and a generic message model.
	 * If more information is to be extracted and the source messages include repeating segments use the first approach as described here:
	 * http://hl7api.sourceforge.net/xref/ca/uhn/hl7v2/examples/HandlingMultipleVersions.html
	 */
	

	public void processMessage(StatefulSession workingMemory) throws JAXBException, ParseException, CCRException, IOException {

			String msgType;


		try {		
//			logger.info ("Entered processMessage");
			InputStream bis = new ByteArrayInputStream( tm.getPayload() );
			s = convertStreamToString(bis);		

			this.context = new DefaultHapiContext();
			this.mcf = new CanonicalModelClassFactory("2.5"); // in example this was generic.  Will this work?
			context.setModelClassFactory(mcf);

         // Pass the MCF to the parser in its constructor
			this.parser = context.getPipeParser();
		
			Message message = parser.parse(s);
			if (message instanceof ADT_A01) {

		    	processADTMessage(workingMemory);

		    } else if (message instanceof ORU_R01) {

		    	processLabMessage(workingMemory);

			}
		
			} catch (Exception e) {
			throw new RuntimeException( "UNknown exception in process message", e );
		}
	}
	public void processADTMessage(StatefulSession workingMemory) throws JAXBException, ParseException, CCRException, IOException {
		Account account = getAccount();
		String knownType = account.getAccountType().getKnownType();
		MenuStructure msPatient = menuBean.findMenuStructure(account.getId(), knownType + ":patient");
		MenuData mdPatient = null;
		 
		try {	
			logger.info ("Entered ProcessADTMessage");
		 // If we get this far, we know it is an ADT message or will treat it as such.

			this.mcf = new CanonicalModelClassFactory("2.5");   //  move this up?   maybe this whole thing is 2.5...
			this.context.setModelClassFactory(mcf);
			
		 // The parser parses the message to a "v25" ORU_R01 specific structure
         ca.uhn.hl7v2.model.v25.message.ADT_A01 msg = (ca.uhn.hl7v2.model.v25.message.ADT_A01) parser.parse(s);
       
         PID myPid = msg.getPID();

         mdPatient = processPID(workingMemory, myPid,  msPatient); //  Process PID also sets your pointer to mdPatient by searching for or creating.	

 //		processPatientEncounter(mdPatient, workingMemory); // might need to do something here for encounter.  May need to match up w/ existing or create?

        List<DG1> diagnosisList = msg.getDG1All();
        processDiagnosisList (workingMemory, mdPatient, diagnosisList);
       
	    List<AL1> allergiesList = msg.getAL1All();
        processAllergiesList (workingMemory, mdPatient, allergiesList);
        
        unsuspendMenuData(mdPatient);
		
		} catch (Exception e){
			throw new RuntimeException( "Exception processing HL7 Result in ProcessADTResult", e);
        }
        

	} 
	
	private void processDiagnosisList (StatefulSession workingMemory, MenuData mdPatient, List<DG1> diagnosisList) {
		MenuStructure msDiagnosis  = menuBean.findDescendentMenuStructure(mdPatient.getAccount().getId(), mdPatient.getMenuStructure(), "diagnosis");
		
		for (DG1 diagnosis : diagnosisList) {
			try {
					MenuData mdDiagnosis = new MenuData();
					TS diagnosisTs = trimFactory.createTS();
					// set the OBX values...

					logger.info("date" + diagnosis.getDg15_DiagnosisDateTime().toString());
// Note that effective time is a date, not a TS...				
					mdDiagnosis.setMenuStructure(msDiagnosis.getAccountMenuStructure());
					mdDiagnosis.setDocumentId(getDocument().getId());
					mdDiagnosis.setAccount(mdPatient.getAccount());
					mdDiagnosis.setParent01(mdPatient);
					
					mdDiagnosis.setField("title", diagnosis.getDg14_DiagnosisDescription().getValue());
					mdDiagnosis.setField("code", diagnosis.getDg13_DiagnosisCodeDG1().getCe1_Identifier().getValue());
					
					mdDiagnosis.setField("status", "ACTIVE");
					mdDiagnosis.setActStatus("completed");
					menuBean.persistMenuData(mdDiagnosis);
//					workingMemory.insert(mdDiagnosis);
					assertPlaceholder(mdDiagnosis);
					
				} catch (Exception e) {
					throw new RuntimeException( "Exception processing ADT Diagnosis " + diagnosis.getName(), e);
				}
			}
	}

	//  This method should probably check for existing allergies for each type.   Right now it blindly adds
	public void processAllergiesList (StatefulSession workingMemory, MenuData mdPatient, List<AL1> allergiesList) {
		MenuStructure msAllergy = menuBean.findDescendentMenuStructure(mdPatient.getAccount().getId(), mdPatient.getMenuStructure(), "allergy");
		 
		for (AL1 allergy : allergiesList) {
			try {
				// Create the top-level request (order)
				MenuData mdAllergy = new MenuData();
				mdAllergy.setMenuStructure(msAllergy.getAccountMenuStructure());
				mdAllergy.setDocumentId(getDocument().getId());
				mdAllergy.setAccount(mdPatient.getAccount());
				mdAllergy.setParent01(mdPatient);

				mdAllergy.setField("title", allergy.getAl13_AllergenCodeMnemonicDescription().getCe2_Text().getValue());
				mdAllergy.setField("code" , allergy.getAl13_AllergenCodeMnemonicDescription().getCe1_Identifier().getValue());
				mdAllergy.setField("reactionseverity" ,  allergy.getAl14_AllergySeverityCode().getCe2_Text().getValue());
				
				String reactionstr = "";
				for (ca.uhn.hl7v2.model.v25.datatype.ST reactions : allergy.getAl15_AllergyReactionCode()){
					reactionstr += reactions.getValue().toString();
				}
				mdAllergy.setField("reactions",reactionstr); 

					
				mdAllergy.setStatus(Status.ACTIVE);
				mdAllergy.setActStatus("active");
				mdAllergy.setDate01(HL7DateFormatUtility.parseDate( allergy.getAl16_IdentificationDate().getValue().toString()));

				menuBean.persistMenuData(mdAllergy);
				assertPlaceholder(mdAllergy);
			} catch (Exception e) {
				throw new RuntimeException( "Exception processing ADT Allergy " + allergy.getName(), e);
			}
		}	
	}
	
    private MenuData processPID(StatefulSession workingMemory, PID myPid, MenuStructure msPatient) throws JAXBException, ParseException, CCRException, IOException {
		Account account = getAccount();
		MenuData mdPatient = null;
		List<MenuData> mdPatients = null;
		String mrn = null;
		String phone = null;
		String lastIdVal = null;
		boolean rslt;
		
		try { 
			mrn = myPid.getPatientID().getIDNumber().getValue();
         
 			// See if this patient is a current patient	
			mdPatients = menuBean.findMenuDataById(account, "MRN", mrn);
			if (mdPatients.size() > 0) {
				logger.info("found match" + mrn);
				mdPatient = mdPatients.get(0);   //Just get the first one?
 			}
		
			if (mdPatient == null) {
  logger.info("Creating New mdPatient");
				mdPatient = new MenuData();
				mdPatient.setMenuStructure(msPatient.getAccountMenuStructure());
				mdPatient.setAccount(account);
				mdPatient.addPlaceholderID("MRN", mrn, "HL7 FEED");
			}
		
			mdPatient.setDocumentId(getDocument().getId()); 
			
			logger.info("DocumentID " + getDocument().getId());
			
			if (!myPid.getDateTimeOfBirth().isEmpty()) {
				TS ts = trimFactory.createTS();
				ts.setValue(HL7DateFormatUtility.formatHL7TSFormatL8Date(HL7DateFormatUtility.parseDate(myPid.getDateTimeOfBirth().getTs1_Time().toString())));
				mdPatient.setTs01(ts);
			}

			mdPatient.setString01(myPid.getPatientName(0).getFamilyName().getSurname().getValue());  //Family Name
			mdPatient.setString02(myPid.getPatientName(0).getGivenName().getValue());  //Given Name
			mdPatient.setString03(myPid.getPatientName(0).getSecondAndFurtherGivenNamesOrInitialsThereof().getValue());  //Second Given Name
	
			mdPatient.setField("sex", myPid.getAdministrativeSex().getValue());

			mdPatient.setField("gender", myPid.getAdministrativeSex().getValue());
			
			mdPatient.setString05(mrn);
		
			mdPatient.setField("homeAddr1", myPid.getPatientAddress(0).getStreetAddress().getSad1_StreetOrMailingAddress().getValue());
			mdPatient.setField("homeAddr2", myPid.getPatientAddress(0).getXad2_OtherDesignation().getValue());
			mdPatient.setField("homeCity", myPid.getPatientAddress(0).getCity().getValue());
			mdPatient.setField("homeState", myPid.getPatientAddress(0).getStateOrProvince().getValue());
			mdPatient.setField("homeZip", myPid.getPatientAddress(0).getZipOrPostalCode().getValue());
			mdPatient.setField("homeCountry", myPid.getPatientAddress(0).getCountry().getValue());
			
			// get the phone numbers
			mdPatient.setField("homeTelecom", myPid.getPhoneNumberHome(0).getTelephoneNumber().getValue().trim());
				
			rslt = menuBean.persistMenuData(mdPatient);
			if (rslt)
				logger.info("mdPatient persistMD Succeeded");
			else 
				logger.info("mdPatient persistMD Failed");
			
			assertPlaceholder(mdPatient);
//			workingMemory.insert(mdPatient);
//			touched(mdPatient);
			}  catch (Exception e)  {
				throw new RuntimeException( "Exception processing HL7 PID", e);
        }	
		return mdPatient;
	}
	
	public void processLabMessage(StatefulSession workingMemory) throws JAXBException, ParseException, CCRException, IOException {
		Account account = getAccount();
		String knownType = account.getAccountType().getKnownType();
		MenuStructure msPatient = menuBean.findMenuStructure(account.getId(), knownType + ":patient");

		MenuData mdPatient = null;
		List<MenuData> mdPatients = null;
		String mrn = null;
		String phone = null;
		String lastIdVal = null;
		String msgType;
		 
		try {			 

			this.mcf = new CanonicalModelClassFactory("2.5");   //  move this up?   maybe this whole thing is 2.5...
			this.context.setModelClassFactory(mcf);
			
	// The parser parses the message to a "v25" ORU_R01 specific structure
         ca.uhn.hl7v2.model.v25.message.ORU_R01 msg = (ca.uhn.hl7v2.model.v25.message.ORU_R01) parser.parse(s);
         
         ORU_R01_PATIENT_RESULT PatResult = msg.getPATIENT_RESULTAll().get(0); // assumes only one will be included
         
         ORU_R01_PATIENT pat = PatResult.getPATIENT();
         PID myPid = pat.getPID();
  
         mdPatient = processPID(workingMemory, myPid, msPatient); //  Process PID also sets your pointer to mdPatient by searching for or creating.	

		MenuStructure msRequest = menuBean.findDescendentMenuStructure(mdPatient.getAccount().getId(), mdPatient.getMenuStructure(), "labOrder");
		MenuStructure msResult  = menuBean.findDescendentMenuStructure(mdPatient.getAccount().getId(), mdPatient.getMenuStructure(), "labresult");
		
        List<ORU_R01_ORDER_OBSERVATION> resultsList = PatResult.getORDER_OBSERVATIONAll();      
  

        for (ORU_R01_ORDER_OBSERVATION result : resultsList) {
			try {
				// Create the top-level request (order)
				MenuData mdResult = new MenuData();
				mdResult.setMenuStructure(msRequest.getAccountMenuStructure());
				mdResult.setDocumentId(getDocument().getId());
				mdResult.setAccount(mdPatient.getAccount());
				mdResult.setParent01(mdPatient);
				
				mdResult.setField("serviceNameCode", result.getOBR().getObr4_UniversalServiceIdentifier().getCe1_Identifier().getValue());
				mdResult.setField("serviceName", result.getOBR().getObr4_UniversalServiceIdentifier().getCe2_Text().getValue());
				mdResult.setField("longName", result.getOBR().getObr4_UniversalServiceIdentifier().getCe4_AlternateIdentifier().getValue());
				mdResult.setField("status", "ACTIVE");
				mdResult.setField("placerOrderNumber", result.getOBR().getObr2_PlacerOrderNumber().getEntityIdentifier().getValue());

				TS ts = trimFactory.createTS();
				ts.setValue(result.getOBR().getObr7_ObservationDateTime().getTime().toString());
//				ts.setValue(HL7DateFormatUtility.formatHL7TSFormatL8Date(HL7DateFormatUtility.parseDate(result.getOBR().getObr6_RequestedDateTime().getTime())));
				mdResult.setTs01(ts);

				menuBean.persistMenuData(mdResult);
				assertPlaceholder(mdResult);

				
				for (ORU_R01_OBSERVATION test : result.getOBSERVATIONAll()) {
					String testResultType;
					MenuData mdTest = new MenuData();
					TS testTs = trimFactory.createTS();
					// set the OBX values...
				
					mdTest.setMenuStructure(msResult.getAccountMenuStructure());
					mdTest.setDocumentId(getDocument().getId());
					mdTest.setAccount(mdPatient.getAccount());
					mdTest.setParent01(mdPatient);
					mdTest.setParent02(mdResult);
									
					if (test.getOBX().getObx3_ObservationIdentifier().isEmpty()) {
						mdTest.setField("title", result.getOBR().getObr4_UniversalServiceIdentifier().getCe4_AlternateIdentifier().getValue());
					} else {
						mdTest.setField("title", test.getOBX().getObx3_ObservationIdentifier().getCe2_Text().getValue());
//						mdTest.setField("code", test.getOBX().getObx3_ObservationIdentifier().getCe1_Identifier().getValue());
						
					}
					testResultType = test.getOBX().getObx2_ValueType().getValue();
					mdTest.setField("status", "COMPLETED");
					mdTest.setString02("LOINC"); // Maybe....  
					// Note date is copied from the result (parent), not the
					// test (child)
					testTs.setValue(test.getOBX().getDateTimeOfTheObservation().getTime().toString());
//					mdTest.setTs01(test.getOBX().getDateTimeOfTheObservation();

					// Value+units
					if (!test.getOBX().getObx6_Units().isEmpty()) {
						String resultValue = test.getOBX().getObx5_ObservationValue(0).getData().toString();
						if (resultValue != null) {
							try {
								mdTest.setPqValue01(Double.parseDouble(resultValue));
							} catch (NumberFormatException nfe) {
								mdTest.setPqValue01(0.0);
							}
							if (resultValue.length()>30) {
								mdTest.setPqStringVal01("see drilldown");
								mdTest.setField("comment", resultValue);
							} else {
								mdTest.setPqStringVal01(resultValue);
							}
//							if (test.getTestResult().getUnits() != null) {
								mdTest.setPqUnits01(test.getOBX().getObx6_Units().getText().getValue());
//							} else {
//								mdTest.setPqUnits01("");
//							}
						} else {
							mdTest.setField("nonrangeresult", resultValue);
						}
					}
					mdTest.setActStatus("completed");
//					logger.info("saving labresult the old way with workingMemory.insert");
					menuBean.persistMenuData(mdTest);
					assertPlaceholder(mdTest);
					
				}
			
			} catch (Exception e) {
				throw new RuntimeException( "Exception processing HL7 Result " + result.getName(), e);
			}
		}  // close loop to iterate over order.
        } catch (Exception e){
			throw new RuntimeException( "Exception processing HL7 Result in ProcessLabResult", e);
        }
        

	}


	
	@Override
	protected ExpressionEvaluator getExpressionEvaluator() {
		if (hl7ee==null) {
			hl7ee = new TrimExpressionEvaluator();
			hl7ee.addVariable( "now", getNow());
			hl7ee.addVariable( "doc", getDocument());
			hl7ee.addVariable(TrimExpressionEvaluator.ACCOUNT, getAccount());
			hl7ee.addVariable(DocumentType.DOCUMENT, getDocument());
		}
		return hl7ee;
	}

	/**
	 * If we get a CCR from another account, there's nothing we can do special - except to process the ccr as normal.
	 */
	@Override
	protected DocBase scanInboundDocument(DocBase doc) throws Exception {
		logger.info( "HL7 PROCESSOR:  Scan inbound ");

		return doc;
	}



    /**
     * This can be called as the last expression (finally) by process() since all instVars are
     * related to the actual process method call and not used by subsequent calls
     */
    protected void cleanup() {
        super.cleanup();
    }

	protected static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
	
    
}
