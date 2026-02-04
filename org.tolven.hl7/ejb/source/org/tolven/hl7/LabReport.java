package org.tolven.hl7;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.entity.AccountUser;

import org.tolven.trim.ex.HL7DateFormatUtility;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.model.v251.segment.OBR;
import ca.uhn.hl7v2.model.v251.segment.OBX;
import ca.uhn.hl7v2.model.v251.segment.ORC;
import ca.uhn.hl7v2.model.v251.segment.PID;
import ca.uhn.hl7v2.model.v251.segment.SFT;
import ca.uhn.hl7v2.model.v251.segment.SPM;
import ca.uhn.hl7v2.model.v251.datatype.NM;
import ca.uhn.hl7v2.model.v251.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v251.message.ORU_R01;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;

public class LabReport extends CreateHL7 {
	private ORU_R01 oru;
	private ORU_R01_ORDER_OBSERVATION orderObservation;
	private OBR obr;
	AccountUser accountUser;
	private MenuData mdOrder, mdReceiver;
	private MenuData account;
    Logger logger = Logger.getLogger(this.getClass());
	
	public LabReport(MenuData mdOrder, MenuData mdReceiver) {
		
		super();
		oru = new ORU_R01();
		this.mdOrder = mdOrder;
		this.mdReceiver = mdReceiver;
		
		this.accountUser = getAccountUser();
		if(!accountUser.getOpenMeFirst().isEmpty()) {
			account = this.menuBean.findMenuDataItem(accountUser.getAccount().getId(), accountUser.getOpenMeFirst());
		} else {
			throw new RuntimeException("Message generation failed. Configure a staff to account");
		}
		
	}
	
	public String createHL7() {
		// MSG Segment
		createMSHSegmentType();
		
		// SFT segement
		createSFTSegment(oru.getSFT());
		
		// PID segment
		MenuData parent = mdOrder.getParent01();
		PID pid = oru.getPATIENT_RESULT(0).getPATIENT().getPID();
		createPIDSegment(pid, parent);
		
		// Order segment
		this.orderObservation = oru.getPATIENT_RESULT().getORDER_OBSERVATION();
		createORCSegment();
		createOBRSegment();
		createSPMSegment();
		
		return encodeToPipeStream();
	}


	private void createORCSegment() {
		
		ORC orc = orderObservation.getORC();
		
		try {
			// 1. Order control
			orc.getOrc1_OrderControl().setValue("RE");
			
			// 12. Ordering provider
			orc.getOrc12_OrderingProvider(0).getXcn1_IDNumber().setValue((String)mdOrder.getField("enteredBy"));
			orc.getOrc12_OrderingProvider(0).getXcn2_FamilyName().getFn1_Surname().setValue((String)account.getField("lastName"));
			orc.getOrc12_OrderingProvider(0).getXcn3_GivenName().setValue((String)account.getField("firstName"));
			orc.getOrc12_OrderingProvider(0).getXcn9_AssigningAuthority().getHd1_NamespaceID().setValue("Tolven");
			orc.getOrc12_OrderingProvider(0).getXcn9_AssigningAuthority().getHd2_UniversalID().setValue("1.2.1." + account.getId() );
			orc.getOrc12_OrderingProvider(0).getXcn9_AssigningAuthority().getHd3_UniversalIDType().setValue("ISO");
			
			// 21. Ordering facility
			orc.getOrc21_OrderingFacilityName(0).getXon1_OrganizationName().setValue(mdOrder.getAccount().getTitle());
			orc.getOrc21_OrderingFacilityName(0).getXon2_OrganizationNameTypeCode().setValue("D");
			orc.getOrc21_OrderingFacilityName(0).getXon6_AssigningAuthority().getHd1_NamespaceID().setValue("Tolven");
			orc.getOrc21_OrderingFacilityName(0).getXon6_AssigningAuthority().getHd2_UniversalID().setValue("1.2.1." + account.getId() );
			orc.getOrc21_OrderingFacilityName(0).getXon6_AssigningAuthority().getHd3_UniversalIDType().setValue("ISO");
			orc.getOrc21_OrderingFacilityName(0).getXon7_IdentifierTypeCode().setValue("AN");
			orc.getOrc21_OrderingFacilityName(0).getXon10_OrganizationIdentifier().setValue("2.1");
			
			// 22. Ordering facility address
			orc.getOrc22_OrderingFacilityAddress(0).getXad1_StreetAddress().getSad1_StreetOrMailingAddress().setValue(mdOrder.getAccount().getProperty().get("clinic_address_line1") + " , " + mdOrder.getAccount().getProperty().get("clinic_address_line2"));
			orc.getOrc22_OrderingFacilityAddress(0).getXad3_City().setValue(mdOrder.getAccount().getProperty().get("clinic_city"));
			orc.getOrc22_OrderingFacilityAddress(0).getXad4_StateOrProvince().setValue(mdOrder.getAccount().getProperty().get("clinic_state"));
			orc.getOrc22_OrderingFacilityAddress(0).getXad5_ZipOrPostalCode().setValue(mdOrder.getAccount().getProperty().get("clinic_state_zip"));
			orc.getOrc22_OrderingFacilityAddress(0).getXad7_AddressType().setValue("B");
			
			// 23. Ordering facility phone number
			orc.getOrc23_OrderingFacilityPhoneNumber(0).getAreaCityCode().setValue(mdOrder.getAccount().getProperty().get("clinic_area_code"));
			orc.getOrc23_OrderingFacilityPhoneNumber(0).getLocalNumber().setValue(mdOrder.getAccount().getProperty().get("clinic_phone_number"));

			// 24. Ordering provider address
			orc.getOrc24_OrderingProviderAddress(0).getXad1_StreetAddress().getSad1_StreetOrMailingAddress().setValue((String)account.getField("addr1") + " " + (String)account.getField("addr2"));
			orc.getOrc24_OrderingProviderAddress(0).getXad3_City().setValue((String)account.getField("city"));
			orc.getOrc24_OrderingProviderAddress(0).getXad4_StateOrProvince().setValue((String)account.getField("state"));
			orc.getOrc24_OrderingProviderAddress(0).getXad5_ZipOrPostalCode().setValue((String)account.getField("zip"));
			orc.getOrc24_OrderingProviderAddress(0).getXad7_AddressType().setValue("B");

		} catch (DataTypeException e) {
			throw new RuntimeException("Unable to create ORC segment. ", e);
		}
		
	}

	private void createSFTSegment(SFT sft) {
		try {
			
			// 1.1 Organization name
			sft.getSft1_SoftwareVendorOrganization().getXon1_OrganizationName().setValue("Tolven");
			
			// 2. Software certified version or release number
			sft.getSft2_SoftwareCertifiedVersionOrReleaseNumber().setValue("2.1");
			
			// 3. Software product name
			sft.getSft3_SoftwareProductName().setValue("Tolven");
			
			// 4. Software Binary ID
			sft.getSft4_SoftwareBinaryID().setValue("2.1");
			
			// 6. Software install date
			sft.getSft6_SoftwareInstallDate().getTs1_Time().setValue(HL7DateFormatUtility.formatHL7TSFormatL14Date(new Date()));
			
		} catch(DataTypeException e) {
			throw new RuntimeException("Unable to create SFT segment. ", e);
		}

	}

	@Override
	public void createMSHSegmentType() {
		try {
			MSH msh = oru.getMSH();
			
			createMSHSegment(msh,mdOrder);
			
			msh.getMsh3_SendingApplication().getHd2_UniversalID().setValue("1.2.1." + mdOrder.getAccount().getId());
			msh.getMsh3_SendingApplication().getHd3_UniversalIDType().setValue("ISO");
			
			msh.getMsh4_SendingFacility().getHd2_UniversalID().setValue("1.2.1." + mdOrder.getAccount().getId());
			msh.getMsh4_SendingFacility().getHd3_UniversalIDType().setValue("ISO");
			
			// 5. Set Receiving Application
			msh.getMsh5_ReceivingApplication().getHd1_NamespaceID().setValue((String)mdReceiver.getField("facilityName"));
			msh.getMsh5_ReceivingApplication().getHd2_UniversalID().setValue((String)mdReceiver.getField("facilityOID"));
			msh.getMsh5_ReceivingApplication().getHd3_UniversalIDType().setValue((String)mdReceiver.getField("facilityOIDType"));

			// 6. Set Receiving Facility
			msh.getMsh6_ReceivingFacility().getHd1_NamespaceID().setValue((String)mdReceiver.getField("applicationName"));
			msh.getMsh6_ReceivingFacility().getHd2_UniversalID().setValue((String)mdReceiver.getField("applicationOID"));
			msh.getMsh6_ReceivingFacility().getHd3_UniversalIDType().setValue((String)mdReceiver.getField("applicationOIDType"));
			
			// 9. Set Message Type 
			// 9.1 Message Type
			msh.getMsh9_MessageType().getMsg1_MessageCode().setValue("ORU");
			// 9.2 Trigger Event
			msh.getMsh9_MessageType().getMsg2_TriggerEvent().setValue("R01");
			// 9.3 Message Structure
			msh.getMsh9_MessageType().getMsg3_MessageStructure().setValue("ORU_R01");
			
			// 12. Set Version ID
			msh.getMsh12_VersionID().getVid1_VersionID().setValue("2.5.1");
			
			// 21. Message Profile Identifier
			msh.getMsh21_MessageProfileIdentifier(0).getEi1_EntityIdentifier().setValue("PHLabReport-Ack");
			msh.getMsh21_MessageProfileIdentifier(0).getEi2_NamespaceID().setValue("");
			msh.getMsh21_MessageProfileIdentifier(0).getEi3_UniversalID().setValue("1.2.1." + mdOrder.getAccount().getId());
			msh.getMsh21_MessageProfileIdentifier(0).getEi4_UniversalIDType().setValue("ISO");
			
		} catch (DataTypeException e) {
			throw new RuntimeException("Unable to create MSH segment. ", e);
		}
	}	

	
	void createOBRSegment() {
		try {
			this.orderObservation = oru.getPATIENT_RESULT().getORDER_OBSERVATION();
			obr = orderObservation.getOBR(); 
			
			// 1. Set ID - OBR
			obr.getObr1_SetIDOBR().setValue("1");
			
			// 3. Filler Order Number (EI)
			obr.getObr3_FillerOrderNumber().getEi1_EntityIdentifier().setValue(String.valueOf(mdOrder.getId()));
			obr.getObr3_FillerOrderNumber().getEi2_NamespaceID().setValue("Tolven");
			obr.getObr3_FillerOrderNumber().getEi3_UniversalID().setValue("1.2.1." + mdOrder.getAccount().getId());
			obr.getObr3_FillerOrderNumber().getEi4_UniversalIDType().setValue("ISO");
			
			// 4. Universal Service Identifier (CE)
			Object serviceNameCode = mdOrder.getField("serviceNameCode");
			if(serviceNameCode!=null) {
				obr.getObr4_UniversalServiceIdentifier().getCe1_Identifier().setValue(serviceNameCode.toString());
				obr.getObr4_UniversalServiceIdentifier().getCe4_AlternateIdentifier().setValue(serviceNameCode.toString());
			}
			Object serviceName = mdOrder.getField("serviceName");
			if(serviceName!=null) {
				obr.getObr4_UniversalServiceIdentifier().getCe2_Text().setValue(serviceName.toString());
				obr.getObr4_UniversalServiceIdentifier().getCe5_AlternateText().setValue(serviceName.toString());
			}
			obr.getObr4_UniversalServiceIdentifier().getCe3_NameOfCodingSystem().setValue("LN");
			obr.getObr4_UniversalServiceIdentifier().getCe6_NameOfAlternateCodingSystem().setValue("LN");
			
			// 7. Observation Date/Time (TS)
			Date observationDate = (Date)mdOrder.getField("date");
			if(observationDate!=null) {
				obr.getObr7_ObservationDateTime().getTs1_Time().setValue(HL7DateFormatUtility.formatHL7TSFormatL14Date(observationDate));
			}
			
			// 13. Relevant Clinical Information (ST)
			Object reason = mdOrder.getField("reason");
			if(reason!=null) {
				obr.getObr13_RelevantClinicalInformation().setValue(reason.toString());
			}
			
			// 16. Ordering Provider
			obr.getObr16_OrderingProvider(0).getXcn1_IDNumber().setValue(account.getAccount().getTitle());
			obr.getObr16_OrderingProvider(0).getXcn2_FamilyName().getFn1_Surname().setValue((String)account.getField("lastName"));
			obr.getObr16_OrderingProvider(0).getXcn3_GivenName().setValue((String)account.getField("firstName"));			
			obr.getObr16_OrderingProvider(0).getXcn9_AssigningAuthority().getHd1_NamespaceID().setValue("Tolven");
			obr.getObr16_OrderingProvider(0).getXcn9_AssigningAuthority().getHd2_UniversalID().setValue("1.2.1." + mdOrder.getAccount().getId());
			obr.getObr16_OrderingProvider(0).getXcn9_AssigningAuthority().getHd3_UniversalIDType().setValue("ISO");
			
			// 22. Results Rpt/ Status Change Date/Time
			Date reportDate = (Date)mdOrder.getField("reportDate");
			obr.getObr22_ResultsRptStatusChngDateTime().getTs1_Time().setValue(HL7DateFormatUtility.formatHL7TSFormatL14Date(reportDate));
			
			// 25 Result status
			obr.getObr25_ResultStatus().setValue("F");
			
			// 31. Reason for Study (CE)
			
			// Get the associated diagnosis
			String path = "echr:patient:labOrder:associatedDiagnosis";
			MenuStructure msList = menuBean.findMenuStructure(mdOrder.getAccount(), path); 
			List<MenuData> mdDiagnosisList = menuBean.findListContents(mdOrder.getAccount(), msList, mdOrder);
			Iterator<MenuData> iterator = mdDiagnosisList.iterator();
			int i = 0;
			while(iterator.hasNext()) {
				MenuData md = iterator.next();
				MenuData mdDiagnosis = md.getParent02();
				
				Object identifier = mdDiagnosis.getField("code");
				if(identifier!=null) {
					obr.getObr31_ReasonForStudy(i).getCe1_Identifier().setValue(identifier.toString());
				}
				Object text = mdDiagnosis.getField("displayName");
				if(text!=null) {
					obr.getObr31_ReasonForStudy(i).getCe2_Text().setValue(text.toString());
				}
				Object codingSystem = mdDiagnosis.getField("codeSystemName");
				if(codingSystem!=null) {
					// obr.getObr31_ReasonForStudy(i).getCe3_NameOfCodingSystem().setValue(codingSystem.toString());
					obr.getObr31_ReasonForStudy(i).getCe3_NameOfCodingSystem().setValue("SNM");
				}
				i++;
			}
			// Get the associated problems
			path = "echr:patient:labOrder:associatedProblem";
			msList = menuBean.findMenuStructure(mdOrder.getAccount(), path); 
			List<MenuData> mdProblemList = menuBean.findListContents(mdOrder.getAccount(), msList, mdOrder);
			iterator = mdProblemList.iterator();

			while(iterator.hasNext()) {
				MenuData md = iterator.next();
				MenuData mdProblem = md.getParent02();
				
				Object identifier = mdProblem.getField("code");
				if(identifier!=null) {
					obr.getObr31_ReasonForStudy(i).getCe1_Identifier().setValue(identifier.toString());
				}
				Object text = mdProblem.getField("title");
				if(text!=null) {
					obr.getObr31_ReasonForStudy(i).getCe2_Text().setValue(text.toString());
				}
				Object codingSystem = mdProblem.getField("codeSystemName");
				if(codingSystem!=null) {
					//obr.getObr31_ReasonForStudy(i).getCe3_NameOfCodingSystem().setValue(codingSystem.toString());
					obr.getObr31_ReasonForStudy(i).getCe3_NameOfCodingSystem().setValue("SNM");
				}
				i++;
			}			
			
			// Get the associated lab results
			path = "echr:patient:labOrder:results";
			msList = menuBean.findMenuStructure(mdOrder.getAccount(), path); 
			List<MenuData> mdResultList = menuBean.findListContents(mdOrder.getAccount(), msList, mdOrder);
			iterator = mdResultList.iterator();
			i = 1 ;
			while(iterator.hasNext()) {
				MenuData md = iterator.next();
				MenuData mdResult = md.getReference();
				Integer id = new Integer(i);
				createOBXSegment(mdResult, id);
				i++;
			}				
			
		} catch (DataTypeException e) {
			throw new RuntimeException("Unable to create OBR segment. ", e);
		}		
	}
	
	void createOBXSegment(MenuData mdResult, Integer id) {
		try {
			OBX obx = orderObservation.getOBSERVATION(id-1).getOBX();
			
			// 1. Set ID - OBX
			obx.getObx1_SetIDOBX().setValue(id.toString());
			
			// 2. Value Type (ID)
			obx.getObx2_ValueType().setValue("NM");
			
			// 3. Observation ID (CE)
			Object identifier = mdResult.getField("code");
			if(identifier!=null) {
				obx.getObx3_ObservationIdentifier().getCe1_Identifier().setValue(identifier.toString());
			}
			Object text = mdResult.getField("title");
			if(text!=null) {
				obx.getObx3_ObservationIdentifier().getCe2_Text().setValue(text.toString());
			}
			Object codingSystem = mdResult.getField("codeSystemName");
			if(codingSystem!=null) {
				//obx.getObx3_ObservationIdentifier().getCe3_NameOfCodingSystem().setValue(codingSystem.toString());
				obx.getObx3_ObservationIdentifier().getCe3_NameOfCodingSystem().setValue("LN");
			}			
			
			// 4. Observation Sub ID (ST)
			obx.getObx4_ObservationSubID().setValue(id.toString());
			
			// 5. Observation Value (Varies)
			Object value = mdResult.getField("value");
			double orgValue = 0;
			if(value!=null) {
				NM nm = new NM(oru);
				nm.setValue(value.toString());
				orgValue = Double.parseDouble(nm.getValue());
				obx.getObx5_ObservationValue(0).setData(nm);
			}
			
			// 6. Units (CE)
			Object unit = mdResult.getField("units");
			if(unit!=null) {
				obx.getObx6_Units().getCe1_Identifier().setValue(unit.toString());
				obx.getObx6_Units().getCe2_Text().setValue(unit.toString());
			}			
			obx.getObx6_Units().getCe3_NameOfCodingSystem().setValue("UCUM");
			
			String referenceRange = "";
			String abnormalFlag = "N";			
			boolean hasLow = false;
			
			Object lowVal = mdResult.getField("lowvalue");			
			if(lowVal!=null) {
				hasLow = true;
				String lowValue = lowVal.toString();
				referenceRange = lowValue;
				Double low = Double.parseDouble(lowValue);
				if(orgValue < low)
					abnormalFlag = "L";
			}
			
			Object lowValUnit = mdResult.getField("lowunits");
			if(lowValUnit!=null) {
				referenceRange += lowValUnit.toString();
			}
			if(hasLow) {
				referenceRange += " < ";	
			}
			referenceRange += " Value ";
			
			Object highVal = mdResult.getField("highvalue");
			if(highVal!=null) {
				String highValue = lowVal.toString();				
				referenceRange += " < " + highValue;
				Double high = Double.parseDouble(highValue);
				if(orgValue > high)
					abnormalFlag = "H";
			}			
			Object highValUnit = mdResult.getField("highunits");
			if(highValUnit!=null) {
				referenceRange += highValUnit.toString();
			}
			
			
			// 7. Reference Range (ST)
			obx.getObx7_ReferencesRange().setValue(referenceRange);
			
			// 8. Abnormal Flags (IS)
			obx.getObx8_AbnormalFlags(0).setValue(abnormalFlag);
			
			// 11. Observation Result Status (ID)
			obx.getObx11_ObservationResultStatus().setValue("F");
			
			// 14. Date/Time of the Observation (TS)
			Date observationDate = (Date)mdResult.getField("effectiveTime");
			obx.getObx14_DateTimeOfTheObservation().getTs1_Time().setValue(HL7DateFormatUtility.formatHL7TSFormatL14Date(observationDate));
			
			// 19. Date/Time of Analysis (TS)
			obx.getObx19_DateTimeOfTheAnalysis().getTs1_Time().setValue(HL7DateFormatUtility.formatHL7TSFormatL14Date(observationDate));
			
			// 23. Performing Organization Name (XON)
			obx.getObx23_PerformingOrganizationName().getXon1_OrganizationName().setValue(mdOrder.getAccount().getTitle());
			obx.getObx23_PerformingOrganizationName().getXon2_OrganizationNameTypeCode().setValue("HL70204");
			obx.getObx23_PerformingOrganizationName().getXon6_AssigningAuthority().getHd1_NamespaceID().setValue("Tolven");
			obx.getObx23_PerformingOrganizationName().getXon6_AssigningAuthority().getHd2_UniversalID().setValue("1.2.1." + mdOrder.getAccount().getId());
			obx.getObx23_PerformingOrganizationName().getXon6_AssigningAuthority().getHd3_UniversalIDType().setValue("ISO");
			obx.getObx23_PerformingOrganizationName().getXon7_IdentifierTypeCode().setValue("AN");
			obx.getObx23_PerformingOrganizationName().getXon10_OrganizationIdentifier().setValue("2.1");
			
			// 24. Performing Organization Address (XAD)
			obx.getObx24_PerformingOrganizationAddress().getXad1_StreetAddress().getSad1_StreetOrMailingAddress().setValue(mdOrder.getAccount().getProperty().get("clinic_address_line1") + " , " + mdOrder.getAccount().getProperty().get("clinic_address_line2"));
			obx.getObx24_PerformingOrganizationAddress().getXad3_City().setValue(mdOrder.getAccount().getProperty().get("clinic_city"));
			obx.getObx24_PerformingOrganizationAddress().getXad4_StateOrProvince().setValue(mdOrder.getAccount().getProperty().get("clinic_state"));
			obx.getObx24_PerformingOrganizationAddress().getXad5_ZipOrPostalCode().setValue(mdOrder.getAccount().getProperty().get("clinic_state_zip"));
			obx.getObx24_PerformingOrganizationAddress().getXad7_AddressType().setValue("B");
			
		} catch (DataTypeException e) {
			throw new RuntimeException("Unable to create OBX segment. ", e);
		}		
	}
		

	void createSPMSegment() {
		try {
			SPM spm = orderObservation.getSPECIMEN().getSPM();
			
			// 4. Specimen Type
			Object identifier = mdOrder.getField("specimenCode");
			if(identifier!=null) {
				spm.getSpm4_SpecimenType().getCwe1_Identifier().setValue(identifier.toString());
				spm.getSpm4_SpecimenType().getCwe4_AlternateIdentifier().setValue(identifier.toString());
			}
			Object text = mdOrder.getField("specimenType");
			if(text!=null) {
				spm.getSpm4_SpecimenType().getCwe2_Text().setValue(text.toString());
				spm.getSpm4_SpecimenType().getCwe5_AlternateText().setValue(text.toString());
			}
			Object codingSystem = mdOrder.getField("specimenCodeSystem");
			if(codingSystem!=null) {
				spm.getSpm4_SpecimenType().getCwe3_NameOfCodingSystem().setValue("HL70301");
				spm.getSpm4_SpecimenType().getCwe6_NameOfAlternateCodingSystem().setValue("HL70301");
			}
			Object version = mdOrder.getField("specimenCodeSystemVersion");
			if(version!=null) {
				spm.getSpm4_SpecimenType().getCwe7_CodingSystemVersionID().setValue(version.toString());
				spm.getSpm4_SpecimenType().getCwe8_AlternateCodingSystemVersionID().setValue(version.toString());
			}			
		
		} catch (DataTypeException e) {
			throw new RuntimeException("Unable to create SPM segment. ", e);
		}		
	}
	
	@Override
	public String encodeToPipeStream() {
		try {
			return new PipeParser().encode(oru);
		} catch (Exception e) {
			throw new RuntimeException("Error in encodeToHL7 ",e);
		}
	}

	@Override
	public String encodeToXML() {
		try {
			return new DefaultXMLParser().encode(oru);
		} catch (Exception e) {
			throw new RuntimeException("error encoding HL7 to xml",e);
		}
	}
	
	private AccountUser getAccountUser() {
		String principal = (String)mdOrder.getField("enteredBy");
		long accountId = mdOrder.getAccount().getId();
		AccountUser accountUser = accountBean.findAccountUser(principal, accountId);
		return accountUser;
	}

}
