package org.tolven.onc.hcqm;
import java.text.*;  
import java.util.*;

import org.tolven.trim.ex.ObservationValueSlotEx;
import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.process.ComputeBase;
import org.tolven.trim.Act;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.Trim;

public class HCQM1 extends ComputeBase {

	private String destination;
	private String edAdmissionDateTime;
	private String edDischargeDateTime;
	private String admitOrderDateTime;
	private String patientDOB;
	private String patientClass;
	private String dischargestatus;
	
	
	private static final String SECONDS = "SECONDS";
	private static final String MINUTES = "MINUTES";
	private static final String HOURS   = "HOURS";
	private static final String DAYS    = "DAYS";
	private static final String YEARS   = "YEARS";
	
	
	/**
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}


	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}


	/**
	 * @return the edAdmissionDateTime
	 */
	public String getEdAdmissionDateTime() {
		return edAdmissionDateTime;
	}


	/**
	 * @param edAdmissionDateTime the edAdmissionDateTime to set
	 */
	public void setEdAdmissionDateTime(String edAdmissionDateTime) {
		this.edAdmissionDateTime = edAdmissionDateTime;
	}


	/**
	 * @return the edDischargeDateTime
	 */
	public String getEdDischargeDateTime() {
		return edDischargeDateTime;
	}


	/**
	 * @param edDischargeDateTime the edDischargeDateTime to set
	 */
	public void setEdDischargeDateTime(String edDischargeDateTime) {
		this.edDischargeDateTime = edDischargeDateTime;
	}


	/**
	 * @return the admitOrderDateTime
	 */
	public String getAdmitOrderDateTime() {
		return admitOrderDateTime;
	}


	/**
	 * @param admitOrderDateTime the admitOrderDateTime to set
	 */
	public void setAdmitOrderDateTime(String admitOrderDateTime) {
		this.admitOrderDateTime = admitOrderDateTime;
	}


	/**
	 * @return the patientDOB
	 */
	public String getPatientDOB() {
		return patientDOB;
	}


	/**
	 * @param patientDOB the patientDOB to set
	 */
	public void setPatientDOB(String patientDOB) {
		this.patientDOB = patientDOB;
	}


	/**
	 * @return the patientClass
	 */
	public String getPatientClass() {
		return patientClass;
	}


	/**
	 * @param patientClass the patientClass to set
	 */
	public void setPatientClass(String patientClass) {
		this.patientClass = patientClass;
	}

	/**
	 * @return the dischargestatus
	 */
	public String getDischargestatus() {
		return dischargestatus;
	}


	/**
	 * @param dischargestatus the dischargestatus to set
	 */
	public void setDischargestatus(String dischargestatus) {
		this.dischargestatus = dischargestatus;
	}


	@Override
	public void compute() throws Exception {
		
		System.out.println("======================================11111==============================");
		TrimExpressionEvaluator ee = new TrimExpressionEvaluator();
		ee.addVariable("trim", getTrim());
		ee.addVariable("account", getAccountUser().getAccount());
		if (getNode() instanceof Act) {
			ee.addVariable("act", getNode());
		}
		// get the registered fields from the trim
		//ObservationValueSlotEx a = (ObservationValueSlotEx) ee.evaluate(getEdDischargeDateTime()); // ED discharge Date Time
		//System.out.println(getEdDischargeDateTime() + "=========22222===========================================getEdDischargeDateTime===================value:"+ ee.evaluate(getEdDischargeDateTime()));

		//ObservationValueSlotEx d = (ObservationValueSlotEx) ee.evaluate(getEdAdmissionDateTime()); // Admin Order Date Time
		//System.out.printl/n(getEdAdmissionDateTime() + "=========33333=============================================getEdAdmissionDateTime=================value:"+ ee.evaluate(getEdAdmissionDateTime()));

	 //admitOrderDateTime
		//System.out.println(getAdmitOrderDateTime() + "=========4444=============================================getAdmitOrderDateTime=================value:"+ ee.evaluate(getAdmitOrderDateTime()));
		
		//admitOrderDateTime
		//System.out.println(getPatientDOB() + "=========5555================================2=============getPatientDOB=================value:"+ ee.evaluate(getPatientDOB()));
		//admitOrderDateTime
		//System.out.println(getDischargestatus() + "=========6666================================2=============getDischargestatus=================value:"+ ee.evaluate(getDischargestatus()));
				
		//Date edAdmissionDateTime = convertStringToDate((String) ee.evaluate(getEdAdmissionDateTime()), "yyyy-MM-dd HH:mm:ss.0"); 
		//Date edDischargeDateTime = convertStringToDate((String) ee.evaluate(getEdDischargeDateTime()), "yyyy-MM-dd HH:mm:ss.0"); 
		//Date admitOrderDateTime = convertStringToDate((String) ee.evaluate(getAdmitOrderDateTime()), "yyyy-MM-dd HH:mm:ss.0");
		//1976-04-07 00:00:00.0
		//Date patientDOB = convertStringToDate((String) ee.evaluate(getPatientDOB()), "yyyy-MM-dd HH:mm:ss"); 
		
		
		//get the instance of the trim
        Trim trim = getTrim(); 
        //calculate wait1
        long wait1 =  getLengthofStay(trim);
        long lengthofStay =  wait1/ (60 * 24); 
        
        
        //Get the patient age
        long patientAge =  getPatientAge(trim);
		
        //get the discharge status code
        //String dischargeStatusCode  = getDischargeStatus(trim);

        //get the  PatientClass
        //String patientClass = getPatientClass(trim);
        
		System.out.println("** >>>>>>>>>> ****** patientAge : " + patientAge + "\n wait1 : " + wait1 
				+ "\n dischargeStatusCode :" );
		if (trim != null) {
			List<ActRelationship> relList = trim.getAct().getRelationships();
			System.out.println(" relList size " + relList.size());
			for (ActRelationship actRelationship : relList) {
				System.out.println(" actRelationship.getName() " + actRelationship.getName());

				
				/************* IMPLEMENT THE LOGIC FOR VTE1.1,VTE1.2,VTE1.3,VTE2.1,VTE2.2,VTE2.3*******START*********/
				//Set the VTE 1 Measure
				 
				if (actRelationship.getName().equalsIgnoreCase("vte1")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						//All Patients admitted to hospital from VTE (dischargestatus code 09)
						 
							//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in  is less than 2 days or more than 120 days 
							if(lengthofStay < 2 || lengthofStay > 120) {
								observationValueSlot.getST().setValue("X");
							}
					 					
					} 
					 
				}
				
				//Set the VTE 2 
				 
				if (actRelationship.getName().equalsIgnoreCase("vte2")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						 
							//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in VTE is less than 2 days or more than 120 days 
							if(lengthofStay < 2 || lengthofStay > 120) {
								observationValueSlot.getST().setValue("X");
							} 	
					} 
				}
				
				//Set the VTE 3 
				 
				if (actRelationship.getName().equalsIgnoreCase("vte3")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						 	//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in VTE is less than 2 days or more than 120 days 
							if(lengthofStay > 120) {
								observationValueSlot.getST().setValue("X");
							} 
					}
					
					 
				}
				
				//Set the VTE 4 
				 
				if (actRelationship.getName().equalsIgnoreCase("vte4")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						 
							//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in VTE is less than 2 days or more than 120 days 
							if( lengthofStay > 120) {
								observationValueSlot.getST().setValue("X");
							} 					
					} 
				}	
				
				//Set the VTE 5 
				 
				if (actRelationship.getName().equalsIgnoreCase("vte5")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
					 		//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in VTE is less than 2 days or more than 120 days 
							if(lengthofStay > 120) {
								observationValueSlot.getST().setValue("X");
							} 
					}
					
					 
				}		

				//Set the VTE 6 
				 
				if (actRelationship.getName().equalsIgnoreCase("vte6")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						 
							//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in VTE is less than 2 days or more than 120 days 
							if( lengthofStay > 120) {
								observationValueSlot.getST().setValue("X");
							} 
					}
					
					 
				}	
				
				
				/*************  THE LOGIC FOR VTE1.1,VTE1.2,VTE1.3,VTE1.4,VTE1.5,VTE1.6********ENDS********/
			}
		}

		//setDestination(new Long(wait1).toString());
		System.out.println("=========================================3========DONE======================");

	}


	/**
	 * 
	 * @param trim
	 * @return
	 */
	public long getLengthofStay(Trim trim) {
		System.out.println("===============================getLengthofStay===================================");
		String edAdmissionDateTime = new String();
		String edDiscDateTime = new String();
		long diffMinutes = 0l;

		List<ActRelationship> relList = trim.getAct().getRelationships();
		for (ActRelationship actRelationship : relList) {
			if (actRelationship.getName().equalsIgnoreCase("ipDates")) {
				List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();
				for (ObservationValueSlot observationValueSlot : observationValueSlotList) {

					if (observationValueSlot.getLabel().getValue().equalsIgnoreCase("Admission Date and time")) {
						edAdmissionDateTime = observationValueSlot.getTS().getValue();
						System.out.println("Admission Date and time : " + edAdmissionDateTime);
					}
					if (observationValueSlot.getLabel().getValue().equalsIgnoreCase("Discharge Date and time")) {
						edDiscDateTime = observationValueSlot.getTS().getValue();
						System.out.println("Discharge Date and time : " + edDiscDateTime);
					}

				}

			}

		}
		Date edAdmissionDate = convertStringToDate(edAdmissionDateTime, "yyyyMMddHHmmssZ");
		if(edAdmissionDate ==null) {
			edAdmissionDate = convertStringToDate(edAdmissionDateTime, "yyyyMMddHHmm");
                        if(edAdmissionDate ==null) {
                         edAdmissionDate = convertStringToDate(edAdmissionDateTime,"yyyy-MM-dd HH:mm:ss.0");
                        }
		}
		Date edDischDate = convertStringToDate(edDiscDateTime, "yyyyMMddHHmmssZ");
		if(edDischDate ==null) {
			edDischDate = convertStringToDate(edDiscDateTime, "yyyyMMddHHmm");
			if(edDischDate ==null) {
				edDischDate = convertStringToDate(edDiscDateTime, "yyyy-MM-dd HH:mm:ss.0");
			}

		}

		diffMinutes = getDateDifference(edAdmissionDate, edDischDate, HCQM1.MINUTES);
		System.out.println("======Wait1");

		return diffMinutes;
	}
	
	/**
	 * 
	 * @param trim
	 * @return
	 */
	public long getWait2(Trim trim) {
		System.out.println("===============================getWait2===================================");
		String admOrderDateTime = new String();
		String edDiscDateTime = new String();
		long diffMinutes = 0l;

		List<ActRelationship> relList = trim.getAct().getRelationships();
		for (ActRelationship actRelationship : relList) {
			if (actRelationship.getName().equalsIgnoreCase("ipDates")) {
				List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();
				for (ObservationValueSlot observationValueSlot : observationValueSlotList) {

					if (observationValueSlot.getLabel().getValue().equalsIgnoreCase("Admit Order Date and time")) {
						admOrderDateTime = observationValueSlot.getTS().getValue();
						System.out.println("Admit Order Date and time : " + admOrderDateTime);
					}
					if (observationValueSlot.getLabel().getValue().equalsIgnoreCase("ED Discharge Date and time")) {
						edDiscDateTime = observationValueSlot.getTS().getValue();
						System.out.println("ED Discharge Date and time : " + edDiscDateTime);
					}

				}

			}

		}

		Date admOrderDate = convertStringToDate(admOrderDateTime, "yyyyMMddHHmmssZ");
		if(admOrderDate ==null) {
			admOrderDate = convertStringToDate(admOrderDateTime, "yyyyMMddHHmm");
		}
		
		Date edDiscDate = convertStringToDate(edDiscDateTime, "yyyyMMddHHmmssZ");		
		if(edDiscDate ==null) {
			edDiscDate = convertStringToDate(edDiscDateTime, "yyyyMMddHHmm");
			if(edDiscDate ==null) {
				edDiscDate = convertStringToDate(edDiscDateTime, "yyyy-MM-dd HH:mm:ss.0");
			}
		}
		diffMinutes = getDateDifference(admOrderDate,edDiscDate, HCQM1.MINUTES);

		return diffMinutes;
	}
	/**
	 * 
	 * @param trim
	 * @return
	 */
	public long getPatientAge(Trim trim) {
		System.out.println("===============================getPatientAge-Paul===================================");
 
		String adminOderDateTime = new String();
		String patientDob = new String();
		long patientAge = 0l;
		

		List<ActRelationship> relList = trim.getAct().getRelationships();
		for (ActRelationship actRelationship : relList) {
			if (actRelationship.getName().equalsIgnoreCase("ipdates")) {
				List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();
				for (ObservationValueSlot observationValueSlot : observationValueSlotList) {

					if (observationValueSlot.getLabel().getValue().equalsIgnoreCase("Admit Order Date and time")) {
						adminOderDateTime = observationValueSlot.getTS().getValue();
						System.out.println("Admit Order Date and time " + adminOderDateTime);
					}
					if (observationValueSlot.getLabel().getValue().equalsIgnoreCase("Patient DOB Date and time")) {
						patientDob = observationValueSlot.getTS().getValue();
						System.out.println("Patient DOB Date and time " + patientDob);
					}
				}

			}

		}

	 
		Date admOrderDate = convertStringToDate(adminOderDateTime, "yyyyMMddHHmm");
		if(admOrderDate ==null) {
			admOrderDate = convertStringToDate(adminOderDateTime, "yyyyMMddHHmmssZ");
		}
		Date patientDobDate = convertStringToDate(patientDob, "yyyyMMddHHmmssZ");
		if(patientDobDate ==null) {
			patientDobDate = convertStringToDate(patientDob, "yyyyMMddHHmm");
		}		

		patientAge = getDateDifference(patientDobDate,admOrderDate, HCQM1.YEARS);
        System.out.println("=====================patientAge================="+patientAge);

		return patientAge;
	}
	
	/**
	 * Method to get the discharge status
	 * @param trim
	 * @return
*/
	private String getDischargeStatus (Trim trim) {

		System.out.println("===============================getDischargeStatus===================================");
 
		String dischargestatus = new String();;
		 
		List<ActRelationship> relList = trim.getAct().getRelationships();
		for (ActRelationship actRelationship : relList) {
			if (actRelationship.getName().equalsIgnoreCase("dischargestatus")) {
				List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();
				for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
					if(observationValueSlot.getCE() != null) {
                     dischargestatus = observationValueSlot.getCE().getCode();// Selected value in the UI
					}
			 
				}

			}

		}
		return dischargestatus;
	
	}
		 

	/**
	 * Method to get the discharge status
	 * @param trim
	 * @return
	 */
	private String getPatientClass (Trim trim) {

		System.out.println("===============================getPatientClass===================================");
 
		String dischargestatus = new String();;
		 
		List<ActRelationship> relList = trim.getAct().getRelationships();
		for (ActRelationship actRelationship : relList) {
			if (actRelationship.getName().equalsIgnoreCase("patientclass")) {
				List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();
				for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
					if(observationValueSlot.getCE() != null) {
                     dischargestatus = observationValueSlot.getCE().getCode(); // Selected value in the UI 
					}
				}

			}

		}
		return dischargestatus;
	
	}
	/**
	 * 
	 * @param dateBegin
	 * @param dateEnd
	 * @param option
	 * @return
	 */
	public long convertStringToDateDifference(String dateBegin,String dateEnd,String option,String format) {
		SimpleDateFormat sdfm = new SimpleDateFormat(format);
		Date beginDate = new Date();
		Date endDate = new Date(); 
		long difference = 0l; 
		 
		try {
			beginDate = sdfm.parse(dateBegin);
			endDate = sdfm.parse(dateEnd);
			long edDiff = endDate.getTime() - beginDate.getTime();
			
			if(option.equals(HCQM1.SECONDS)){
				difference = edDiff / (1000);
			
			}
			
			if(option.equals(HCQM1.MINUTES)){
				difference = edDiff/(1000*60);
			}
			
			if(option.equals(HCQM1.HOURS)){
				difference   = edDiff/(60*60*1000);
			}
			
			if(option.equals(HCQM1.DAYS)){
				difference    = (edDiff/(24 * 60 * 60 * 1000));
			}
			
			if(option.equals(HCQM1.YEARS)){
				difference    = (edDiff/(24 * 60 * 60 * 1000))/365;
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			difference = 0l;
		}

		return difference;

	}
	
	
	/**
	 * 
	 * @param dateBegin
	 * @param dateEnd
	 * @param option
	 * @return
	 */
	public long getDateDifference(Date beginDate,	Date endDate ,String option) {
		long difference = 0l; 
		 
		try {
			long edDiff = endDate.getTime() - beginDate.getTime();
			
			if(option.equals(HCQM1.SECONDS)){
				difference = edDiff / (1000);
			
			}
			
			if(option.equals(HCQM1.MINUTES)){
				difference = edDiff/(1000*60);
			}
			
			if(option.equals(HCQM1.HOURS)){
				difference   = edDiff/(60*60*1000);
			}
			
			if(option.equals(HCQM1.DAYS)){
				difference    = (edDiff/(24 * 60 * 60 * 1000));
			}
			
			if(option.equals(HCQM1.YEARS)){
				difference    = (edDiff/(24 * 60 * 60 * 1000))/365;
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			difference = 0l;
		}

		return difference;

	}
	
	
	
	/**
	 * 
	 * @param dateBegin
	 * @param dateEnd
	 * @param option
	 * @return
	 */
	public Date convertStringToDate(String dateStr,String format) {
		Date returnDate = new Date();
		if(dateStr != null && dateStr.length() > 0 ) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			try {
				returnDate = sdf.parse(dateStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				System.out.println(" dateStr [" + dateStr + "] format [" + format+"] " +  e.getMessage());
				return null;
			}
		}
		return returnDate;
		
	}	
	
}



