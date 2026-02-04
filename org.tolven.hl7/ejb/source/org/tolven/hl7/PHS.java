package org.tolven.hl7;

import java.util.Date;

import org.tolven.app.entity.MenuData;
import org.tolven.trim.ex.HL7DateFormatUtility;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v251.message.ADT_A01;
import ca.uhn.hl7v2.model.v251.segment.DG1;
import ca.uhn.hl7v2.model.v251.segment.EVN;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.model.v251.segment.PID;
import ca.uhn.hl7v2.model.v251.segment.PV1;
import ca.uhn.hl7v2.model.v251.segment.PV2;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;

public class PHS extends CreateHL7 {
	
	private ADT_A01 adt_a01;

	private MenuData md,mdReceiver,parent;
	
	private MSH msh;
	private PID pid;
	private EVN evn;
	private PV1 pv1;
	private DG1 dg1;
	
	public PHS(MenuData md,MenuData mdReceiver) {
		super();
		try {
			this.md = md;
			this.mdReceiver = mdReceiver;

			adt_a01 = new ADT_A01();
			msh = adt_a01.getMSH();
			pid = adt_a01.getPID();
			evn = adt_a01.getEVN();
			pv1 = adt_a01.getPV1();
			dg1 = adt_a01.getDG1();
		} catch (Exception e) {
			throw new RuntimeException("Unable to create Encounter segments. ", e);
		}
	}
	
	public String createHL7() {
		getMenuBean();			
		createMSHSegmentType();
		parent = md.getParent01();
		//mdPatient = menuBean.findMenuDataItem(md.getAccount().getId(),parent.getPath());
		createEVNSegment();
		createPIDSegment(pid,parent);		
		createPV1Segment();
		createPV2Segment();		
		createDG1Segment();
		return encodeToPipeStream();
	}
	

	/**
	 * MSH Segment has the following elements

	  	5. Receiving Application
	  		5.1 Namespace ID
	  	6. Receiving Facility
	  		6.1 Namespace ID

	  	9. Message Type
	  		9.1 Message Type
	  		9.2 Trigger Event
	  		9.3 Message Structure
	 	
	 */
	
	public void createMSHSegmentType() {
		try {
			createMSHSegment(msh,md);
			
			// 4.2 Sending Facility ID
			msh.getMsh4_SendingFacility().getHd2_UniversalID().setValue("1.2.1." + md.getAccount().getId());
			// 4.3 Sending Facility Type
			msh.getMsh4_SendingFacility().getHd3_UniversalIDType().setValue("ISO");

			// 5. Set Receiving Application
			String receivingApplication = mdReceiver.getString04();
			msh.getMsh5_ReceivingApplication().getHd1_NamespaceID().setValue(receivingApplication);
			// 6. Set Receiving Facility
			String receivingFacility = mdReceiver.getString01();
			msh.getMsh6_ReceivingFacility().getHd1_NamespaceID().setValue(receivingFacility);
			
			// 9. Set Message Type 
			// 9.1 Message Type
			msh.getMsh9_MessageType().getMsg1_MessageCode().setValue("ADT");
			// 9.2 Trigger Event
			msh.getMsh9_MessageType().getMsg2_TriggerEvent().setValue("A01");
			// 9.3 Message Structure
			msh.getMsh9_MessageType().getMsg3_MessageStructure().setValue("ADT_A01");
			// 12. Set Version ID
			msh.getMsh12_VersionID().getVid1_VersionID().setValue("2.3.1");		
		} catch (DataTypeException e) {
			throw new RuntimeException("Unable to create MSH segment. ", e);
		}
	}	
	
	/**
	 * EVN Segment has the following elements
	  	1. Event Type Code
	  		1.1 Value
	  	2. Recorded Date/Time
	  		2.1 Time of An Event
	 */	
	private void createEVNSegment() {
		try {
			// 1. Event Type Code
			evn.getEvn1_EventTypeCode().setValue("A01");
			// 2. Recorded Date/Time
			Date effectiveDateTime = (Date)md.getField("effectiveTime");
			
			if(effectiveDateTime != null ){
				evn.getEvn2_RecordedDateTime().getTs1_Time().setValue(HL7DateFormatUtility.formatHL7TSFormatL14Date(effectiveDateTime));
			}
		} catch(DataTypeException e) {
			throw new RuntimeException("Unable to create EVN segment. ", e);
		}
	}
	
	/**
	 * PV1 Segment has the following elements
	  	1. Set ID
	  		1.1 Value
	  	2. Patient Class
	  		2.1 Value
	  	4. Admission Type
	  		4.1 Value
	  	14. Admit Source
	  		14.1 Value
	  	19. Visit Number
	  		19.1 ID
	  	44. Admin Date/Time
	  		44.1 Time of An Event
	 */
	private void createPV1Segment() {
		try {
			//Set ID
			pv1.getPv11_SetIDPV1().setValue("1");
			// 2. Patient Class
			pv1.getPv12_PatientClass().setValue("I");
			// 4. Admission Type
			pv1.getPv14_AdmissionType().setValue("R");
			// 14. Admit Source
			pv1.getPv114_AdmitSource().setValue("9");
			// 19. Visit Number
			//19.1 Visit Identifier
			Object encounter = md.getField("encounter");
			if(encounter != null){
				MenuData encounterMd = (MenuData) encounter;
				Object visitID = encounterMd.getId();
				if(visitID != null){
					pv1.getPv119_VisitNumber().getCx1_IDNumber().setValue(visitID.toString());
				}
				// 44. Admit Date/Time
				Date admitdate = (Date)encounterMd.getField("effectiveTimeLow");
				if(admitdate != null){
					//DateFormat format = sdf;					
					pv1.getPv144_AdmitDateTime().getTs1_Time().setValue(HL7DateFormatUtility.formatHL7TSFormatL14Date(admitdate));
				}		
			}
				
		} catch(Exception e) {
			throw new RuntimeException("Unable to create PV1 segment. ", e);
		}
	}
	
	/**
	 * PV2 Segment has the following elements
	  	3. Admit Reason
	  		3.1 Identifier
	  		3.2 Text
	  		3.3 Name of Coding System
	 */	
	private void createPV2Segment() {
		try {

		} catch(Exception e) {
			
		}
	}
	/**
	 * DG1 has the following elements
	   		1.SetID
	   		2.Diagnosis Coding Method
	   		3.Diagnosis Code
	   			3.1 Identifier
	   			3.2 Text
	   			3.3 Coding System
	   		4.Diagnosis Description
	   		6.Diagnosis Type	   			
	 */
	private void createDG1Segment() {
		try {
			//1.SetID
			//First Occurrence
			dg1.getDg11_SetIDDG1().setValue("1");
			//3.1 Identifier
			Object identifier = md.getField("code");
			if(identifier != null){
				dg1.getDg13_DiagnosisCodeDG1().getCe1_Identifier().setValue(identifier.toString());
			}
			//3.2 Text
			Object diagnosis = md.getField("title");
			if(diagnosis != null){
				dg1.getDg13_DiagnosisCodeDG1().getCe2_Text().setValue(diagnosis.toString());
		    }
			//3.3 Coding System
			dg1.getDg13_DiagnosisCodeDG1().getCe3_NameOfCodingSystem().setValue(md.getField("codeSystemName").toString());
			//6.Diagnosis Type
			dg1.getDg16_DiagnosisType().setValue("W");
		} catch(Exception e) {		
			throw new RuntimeException("Unable to create DG1 segment. ", e);
		}
	}	
	
	public String encodeToPipeStream() {
		try {
			String hl7Message=null;
			Parser parser = new PipeParser();
			hl7Message = parser.encode(adt_a01);
			return hl7Message;			
		} catch (Exception e) {
			throw new RuntimeException("Error in encodeToHL7 ",e);
		}
	}
	
	public String encodeToXML() {
		try {
			String hl7Message=null;
			Parser parser = new DefaultXMLParser();
			hl7Message = parser.encode(adt_a01);
			return hl7Message;	
		} catch (HL7Exception e) {
			throw new RuntimeException("Error in encodeToXML ",e);
		}	
	}

}
