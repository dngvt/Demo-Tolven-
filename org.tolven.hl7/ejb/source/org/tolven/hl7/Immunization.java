package org.tolven.hl7;

import java.text.Format;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.tolven.app.entity.MenuData;
import ca.uhn.hl7v2.model.v251.group.VXU_V04_ORDER;
import ca.uhn.hl7v2.model.v251.message.VXU_V04;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.model.v251.segment.PID;
import ca.uhn.hl7v2.model.v251.segment.RXA;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;

public class Immunization extends CreateHL7 {
	
	private VXU_V04 vxu;
	private String uniqueIdentifier;
	private MenuData md, mdReceiver;
	
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}


	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	
	
	public Immunization(MenuData md,MenuData mdReceiver) {
		super();
		this.md = md;
		this.uniqueIdentifier="UNIQUE02";
		this.mdReceiver = mdReceiver;
		this.vxu = new VXU_V04();			
	}
	
	
	public String createHL7() {
		getMenuBean();		
		createMSHSegmentType();
		MenuData parent = md.getParent01();
		//MenuData mdPatient = menuBean.findMenuDataItem(md.getAccount().getId(), parent.getPath());
		PID pid = vxu.getPID();
		createPIDSegment(pid,parent);		
		createRXASegment(vxu,md);		
		return encodeToPipeStream();
	}
	
	/**
	 * MSH Segment has the following elements
	  	1. Field Separator
	  		|
	  	2. Encoding Characters
	  		^~\&
	  	3. Sending Application
	  		3.1 Namespace ID
	  	4. Sending Facility
	  		4.1 Namespace ID
	  	5. Receiving Application
	  		5.1 Namespace ID
	  	6. Receiving Facility
	  		6.1 Namespace ID
	  	7. Date/Time of Message
	  		7.1 Time of an Event
	  	8. Security
	  		8.1 Value
	  	9. Message Type
	  		9.1 Message Type
	  		9.2 Trigger Event
	  		9.3 Message Structure
	  	10.Message Control ID
	  		10.1 Value	  		
	  	11.Processing ID
	  		11.1 Processing ID
	  	12.Version ID
	  		12.1 Version ID
	 	
	 */
	
	public void createMSHSegmentType() {
		try {
			MSH msh = vxu.getMSH();
			createMSHSegment(msh,md);
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
			msh.getMsh9_MessageType().getMsg1_MessageCode().setValue("VXU");
			// 9.2 Trigger Event			
			msh.getMsh9_MessageType().getMsg2_TriggerEvent().setValue("V04");
			// 9.3 Message Structure
			msh.getMsh9_MessageType().getMsg3_MessageStructure().setValue("VXU_V04");
			// 12. Set Version ID
			msh.getMsh12_VersionID().getVid1_VersionID().setValue("2.3.1");		
		} catch (Exception e) {
			throw new RuntimeException("Unable to create MSH segment. ", e);
		}
	}

	
	/**
	 * RXA Segment has the following elements
	 * ( Pharmacy / Treatment Administration ) 
	 	1.  Give Sub-Id Counter
	 		1.1 Value
	 	2.  Administration Sub-Id Counter
	 		2.1 Value
	 	3.  Date/Time Start of Administration
	 		3.1 Time of an Event
	 	4.  Date/Time End of Administration
	 		4.1 Time of an Event
	 	5.  Administered Code
	 		5.1 Identifier
	 		5.2 Text
	 		5.3 Name of Coding System
	 	6.  Administered Amount
	 		6.1 Value
	 	7.  Administered Units
	 		7.1 Identifier
	 		7.2 Text
	 		7.3 Name of Coding System
	 	8.  Administered Dosage Form
	 		8.1 Identifier
	 	9.  Administration Notes
	 		9.1 Identifier
	 	10. Administering Provider
	 		10.1 ID Number (ST)
	 	11. Administered-at Location
	 		11.1 Point of Care (IS)
	 	12. Administered Per (time Unit)
	 		12.1 Value
	 	13. Administered Strength
	 		13.1 Value
	 	14. Administered Strength Units
	 		14.1 Identifier
	 	15. Substance Lot Number
	 		15.1 Value
	 	16. Substance Expiration Date
	 		16.1 Time of an Event
	 	17. Substance Manufacturer Name
	 		17.1 Identifier
	 		17.2 Text
	 		17.3 Name of Coding System
	 	18. Substance Refusal Reason
	 		18.1 Identifier
	 	19. Indication
	 		19.1 Identifier
	 	20. Completion Status
	 		20.1 Value
	 	21.	Action Code
	 		21.1 Value
	 */
	
	public void createRXASegment(VXU_V04 vx,MenuData md) {
		try {
			VXU_V04_ORDER rxaGroup = vxu.getORDER();
			RXA rxa = rxaGroup.getRXA();			
			//1. Give Sub-Id Counter-Fixed
			rxa.getRxa1_GiveSubIDCounter().setValue("0");
			
			//2. Administration Sub-Id Counter-Fixed
			rxa.getRxa2_AdministrationSubIDCounter().setValue("1");
			//3. Date/Time Start of Administration
			Date doe = (Date)md.getField("date01");
			Format format = new SimpleDateFormat("yyyyMMddhhmmss");			
			String formattedDOE = format.format(doe);	
			rxa.getRxa3_DateTimeStartOfAdministration().getTs1_Time().setValue(formattedDOE);
			//4.  Date/Time End of Administration
			rxa.getRxa4_DateTimeEndOfAdministration().getTs1_Time().setValue(formattedDOE);
			//5. Administered Code 
			//5.1 Identifier
			Object administerCode = md.getField("code");			
			if(administerCode != null){				
				rxa.getRxa5_AdministeredCode().getCe1_Identifier().setValue(administerCode.toString());
			}
			//5.2 Text
			Object serviceName = md.getField("title");
			rxa.getRxa5_AdministeredCode().getCe2_Text().setValue(serviceName.toString());
			//5.3 Name of Coding System			
			rxa.getRxa5_AdministeredCode().getCe3_NameOfCodingSystem().setValue("CVX");
			//6.  Administered Amount
	 		//6.1 Value
			Object materialAmount = md.getField("dosageQuantity");
			if(materialAmount !=null){
				rxa.getRxa6_AdministeredAmount().setValue(materialAmount.toString());
			}
			
			//7.  Administered Units
	 		//7.1 Identifier
	 		//7.2 Text
	 		//7.3 Name of Coding System
			/*Object units = md.getField("dosagequantityUnit");
			if(units != null){
				//rxa.getAdministeredUnits().getText().setValue(units.toString());
				rxa.getAdministeredUnits().getIdentifier().setValue(units.toString());
			}*/
			Object unitText = md.getField("dosagequantityUnit");
			if(unitText != null){
				rxa.getRxa7_AdministeredUnits().getCe1_Identifier().setValue(unitText.toString());
				rxa.getRxa7_AdministeredUnits().getCe2_Text().setValue(unitText.toString());
			}
			rxa.getRxa7_AdministeredUnits().getCe3_NameOfCodingSystem().setValue("ISO+");
			
			
			//8.  Administered Dosage Form
	 		//8.1 Identifier
			/*Object dosage = md.getField("dosageQuantity");
			if(dosage != null){
				rxa.getAdministeredDosageForm().getIdentifier().setValue(dosage.toString());
			}
			*/
			
			//9.  Administration Notes
	 		//9.1 Identifier
			//10. Administering Provider
	 		//10.1 ID Number (ST)
			//11. Administered-at Location
	 		//11.1 Point of Care (IS)
			//12. Administered Per (time Unit)
	 		//12.1 Value
			//13. Administered Strength
	 		//13.1 Value
			//14. Administered Strength Units
	 		//14.1 Identifier
			
			//15. Substance Lot Number
	 		//15.1 Value
			Object lotNumber = md.getField("lotNumber");
			if(lotNumber != null){
				rxa.getRxa15_SubstanceLotNumber(0).setValue(lotNumber.toString());
			}
					
			//16. Substance Expiration Date
	 		//16.1 Time of an Event
			
			//17. Substance Manufacturer Name
	 		//17.1 Identifier
	 		//17.2 Text
	 		//17.3 Name of Coding System
			Object substanceManufacturer = md.getField("manufacturer");
			if(substanceManufacturer != null){
				String[] manufacturerInfo = substanceManufacturer.toString().split("/");
				String manufacturerName = manufacturerInfo[0];
				String manufacturerId = manufacturerInfo[1];
				rxa.getRxa17_SubstanceManufacturerName(0).getCe1_Identifier().setValue(manufacturerId);
				rxa.getRxa17_SubstanceManufacturerName(0).getCe2_Text().setValue(manufacturerName);
				rxa.getRxa17_SubstanceManufacturerName(0).getCe3_NameOfCodingSystem().setValue("MVX");
			}
			
			//18. Substance Refusal Reason
	 		//18.1 Identifier
     	 	//19. Indication
			//19.1 Identifier
			
			//20. Completion Status
	 		//20.1 Value
			
			//21 Actioncode-Fixed
			rxa.getRxa21_ActionCodeRXA().setValue("A");
			
		} catch (Exception e) {
			throw new RuntimeException("Unable to create RXA segment. ", e);
		}
	}
	
	public String encodeToPipeStream() {
		try {
			return new PipeParser().encode(this.vxu);
		} catch (Exception e) {
			throw new RuntimeException("error encoding HL7 to pipped stream",e);
		}
	}
	
	public String encodeToXML() {
		try {
			return new DefaultXMLParser().encode(this.vxu);
		} catch (Exception e) {
			throw new RuntimeException("error encoding HL7 to xml",e);
		}	
	}
}
