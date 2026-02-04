package com.myorg.trim;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.process.ComputeBase;
import org.tolven.trim.Act;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.Trim;

public class HCQM3 extends ComputeBase {

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
        long wait1 =  getWait1(trim);
        long wait1Days =  wait1/ (60 * 24); 
        
        //calculate wait2
        long wait2 = getWait2(trim); 
        long wait2Days =  wait2/(60 * 24); 
        
        //Get the patient age
        long patientAge =  getPatientAge(trim);
		
        //get the discharge status code
        String dischargeStatusCode  = getDischargeStatus(trim);

        //get the  PatientClass
        String patientClass = getPatientClass(trim);
        
		System.out.println("** >>>>>>>>>> ****** patientAge : " + patientAge + "\n wait1 : " + wait1 + " \n wait2  : " + wait2
				+ "\n dischargeStatusCode :" 
				+ dischargeStatusCode + "\n patientClass : " + patientClass);
		if (trim != null) {
			List<ActRelationship> relList = trim.getAct().getRelationships();
			System.out.println(" relList size " + relList.size());
			for (ActRelationship actRelationship : relList) {
				System.out.println(" actRelationship.getName() " + actRelationship.getName());

				//set Wait 1 
				if (actRelationship.getName().equalsIgnoreCase("wait1")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						observationValueSlot.getPQ().setValue(new Double(wait1));
					}
				}
				
				//Set Wait 2
				if (actRelationship.getName().equalsIgnoreCase("wait2")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						observationValueSlot.getPQ().setValue(new Double(wait2));
					}
				}
				/************* IMPLEMENT THE LOGIC FOR ED1.1,ED1.2,ED1.3,ED2.1,ED2.2,ED2.3*******START*********/
				//Set the ED 1.1 Measure
				 
				if (actRelationship.getName().equalsIgnoreCase("ed1.1")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						//All Patients admitted to hospital from ED (dischargestatus code 09)
						if(dischargeStatusCode.equalsIgnoreCase("09")) {
							// Logic to Exclude  Observation Patient and Mental Health Patient
							if(patientClass.equalsIgnoreCase("MS")) {
								System.out.println("**********ed1.1******MS***D");
								//if Others
								observationValueSlot.getST().setValue("D");	
							} else {
								System.out.println("**********ed1.1******MS***X");
								//if Mental Health or Observation Patient set value to X
								observationValueSlot.getST().setValue("X");
							} 
							/*
							//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in ED is less than 2 days or more than 120 days 
							if(wait1Days < 2 || wait1Days > 120) {
								observationValueSlot.getST().setValue("X");
							}*/
							
						} else {
							//All Patients admitted to hospital from ED OTHER THAN (dischargestatus code 09)
							observationValueSlot.getST().setValue("I");
						}
					}
					
					//Get the list of relationships with in ED1.1
					List<ActRelationship> ed11ActRelationshipList = actRelationship.getAct().getRelationships();
					
					for (ActRelationship ed11ActRelationship : ed11ActRelationshipList) {
						System.out.println(" actRelationship.getName()111111 " + ed11ActRelationship.getName());
						//set Wait 1 
						if (ed11ActRelationship.getName().equalsIgnoreCase("edvalue")) {
							System.out.println(" actRelationship.getName() 22222" + ed11ActRelationship.getName());
							List<ObservationValueSlot> ed11bservationValueSlotList = ed11ActRelationship.getAct().getObservation().getValues();
							System.out.println(" ed11bservationValueSlotList.size 22222" + ed11bservationValueSlotList.size());
							for (ObservationValueSlot observationValueSlot : ed11bservationValueSlotList) {
								System.out.println(" setting value..ed1.1.. " + wait1);
								observationValueSlot.getPQ().setValue(new Double(wait1));
							}
						}
					}
				}
				
				//Set the ED 1.2 Measure for Observation Patient ONLY
				 
				if (actRelationship.getName().equalsIgnoreCase("ed1.2")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						//All Patients admitted to hospital from ED (dischargestatus code 09)
						if(dischargeStatusCode.equalsIgnoreCase("09")) {
							// Logic to Include  Observation Patient ONLY
							if(patientClass.equalsIgnoreCase("OBSV")) {
								System.out.println("**********ed1.2******OBSV***D");
								//if Others
								observationValueSlot.getST().setValue("D");	
							} else {
								System.out.println("**********ed1.2******OBSV***X");
								//if Mental Health or other  Patient set value to X
								observationValueSlot.getST().setValue("X");
							} 
							/*
							//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in ED is less than 2 days or more than 120 days 
							if(wait1Days < 2 || wait1Days > 120) {
								observationValueSlot.getST().setValue("X");
							}*/
							
						} else {
							//All Patients admitted to hospital from ED OTHER THAN (dischargestatus code 09)
							observationValueSlot.getST().setValue("I");
						}		
					}
					
					//Get the list of relationships with in ED1.2
					List<ActRelationship> ed12ActRelationshipList = actRelationship.getAct().getRelationships();
					
					for (ActRelationship ed12ActRelationship : ed12ActRelationshipList) {
						System.out.println(" actRelationship.getName() " + ed12ActRelationship.getName());

						//set Wait 1 
						if (ed12ActRelationship.getName().equalsIgnoreCase("edvalue")) {
							List<ObservationValueSlot> ed12bservationValueSlotList = ed12ActRelationship.getAct().getObservation().getValues();

							for (ObservationValueSlot observationValueSlot : ed12bservationValueSlotList) {
								observationValueSlot.getPQ().setValue(new Double(wait1));
							}
						}
					}
				}
				
				//Set the ED 1.3 Measure Mental Health ONLY
				 
				if (actRelationship.getName().equalsIgnoreCase("ed1.3")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						//All Patients admitted to hospital from ED (dischargestatus code 09)
						if(dischargeStatusCode.equalsIgnoreCase("09")) {
							// Logic to Include Mental Health  ONLY
							if(patientClass.equalsIgnoreCase("MH")) {
								System.out.println("**********ed1.3******MH***D");
								//if Others
								observationValueSlot.getST().setValue("D");	
							} else {
								System.out.println("**********ed1.3******MH***X");
								//if Observation Patient or other  Patient set value to X
								observationValueSlot.getST().setValue("X");
							} 
							/*
							//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in ED is less than 2 days or more than 120 days 
							if(wait1Days < 2 || wait1Days > 120) {
								observationValueSlot.getST().setValue("X");
							}*/
							
						} else {
							//All Patients admitted to hospital from ED OTHER THAN (dischargestatus code 09)
							observationValueSlot.getST().setValue("I");
						}	
					}
					
					//Get the list of relationships with in ED1.3
					List<ActRelationship> ed13ActRelationshipList = actRelationship.getAct().getRelationships();
					
					for (ActRelationship ed13ActRelationship : ed13ActRelationshipList) {
						System.out.println(" actRelationship.getName() " + ed13ActRelationship.getName());

						//set Wait 1 
						if (ed13ActRelationship.getName().equalsIgnoreCase("edvalue")) {
							List<ObservationValueSlot> ed13bservationValueSlotList = ed13ActRelationship.getAct().getObservation().getValues();

							for (ObservationValueSlot observationValueSlot : ed13bservationValueSlotList) {
								observationValueSlot.getPQ().setValue(new Double(wait1));
							}
						}
					}
				}
				
				//Set the ED 2.1 Measure
				 
				if (actRelationship.getName().equalsIgnoreCase("ed2.1")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						//All Patients admitted to hospital from ED (dischargestatus code 09)
						if(dischargeStatusCode.equalsIgnoreCase("09")) {
							// Logic to Exclude  Observation Patient and Mental Health Patient
							if(patientClass.equalsIgnoreCase("MS")) {
								//if Others
								observationValueSlot.getST().setValue("D");	
							} else {
								//if Mental Health or Observation Patient set value to X
								observationValueSlot.getST().setValue("X");
							} 
							/*
							//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in ED is less than 2 days or more than 120 days 
							if(wait2Days < 2 || wait2Days > 120) {
								observationValueSlot.getST().setValue("X");
							}*/
						} else {
							//All Patients admitted to hospital from ED OTHER THAN (dischargestatus code 09)
							observationValueSlot.getST().setValue("I");
						}						
					}
					
					//Get the list of relationships with in ED2.1
					List<ActRelationship> ed21ActRelationshipList = actRelationship.getAct().getRelationships();
					
					for (ActRelationship ed21ActRelationship : ed21ActRelationshipList) {
						System.out.println(" actRelationship.getName() " + ed21ActRelationship.getName());

						//set Wait 1 
						if (ed21ActRelationship.getName().equalsIgnoreCase("edvalue")) {
							List<ObservationValueSlot> ed21bservationValueSlotList = ed21ActRelationship.getAct().getObservation().getValues();

							for (ObservationValueSlot observationValueSlot : ed21bservationValueSlotList) {
								observationValueSlot.getPQ().setValue(new Double(wait2));
							}
						}
					}
				}	
				
				//Set the ED 2.2 Measure
				 
				if (actRelationship.getName().equalsIgnoreCase("ed2.2")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						//All Patients admitted to hospital from ED (dischargestatus code 09)
						if(dischargeStatusCode.equalsIgnoreCase("09")) {
							// Logic to Include  Observation Patient ONLY
							if(patientClass.equalsIgnoreCase("OBSV")) {
								//if Others
								observationValueSlot.getST().setValue("D");	
							} else {
								//if Mental Health or other  Patient set value to X
								observationValueSlot.getST().setValue("X");
							} 
							/*
							//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in ED is less than 2 days or more than 120 days 
							if(wait2Days < 2 || wait2Days > 120) {
								observationValueSlot.getST().setValue("X");
							}*/
						} else {
							//All Patients admitted to hospital from ED OTHER THAN (dischargestatus code 09)
							observationValueSlot.getST().setValue("I");
						}		
					}
					
					//Get the list of relationships with in ED2.2
					List<ActRelationship> ed22ActRelationshipList = actRelationship.getAct().getRelationships();
					
					for (ActRelationship ed22ActRelationship : ed22ActRelationshipList) {
						System.out.println(" actRelationship.getName() " + ed22ActRelationship.getName());

						//set Wait 1 
						if (ed22ActRelationship.getName().equalsIgnoreCase("edvalue")) {
							List<ObservationValueSlot> ed22bservationValueSlotList = ed22ActRelationship.getAct().getObservation().getValues();

							for (ObservationValueSlot observationValueSlot : ed22bservationValueSlotList) {
								observationValueSlot.getPQ().setValue(new Double(wait2));
							}
						}
					}
				}		

				//Set the ED 2.3 Measure
				 
				if (actRelationship.getName().equalsIgnoreCase("ed2.3")) {
					List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();

					for (ObservationValueSlot observationValueSlot : observationValueSlotList) {
						//All Patients admitted to hospital from ED (dischargestatus code 09)
						if(dischargeStatusCode.equalsIgnoreCase("09")) {
							// Logic to Include Mental Health  ONLY
							if(patientClass.equalsIgnoreCase("MH")) {
								//if Others
								observationValueSlot.getST().setValue("D");	
							} else {
								//if Observation Patient or other  Patient set value to X
								observationValueSlot.getST().setValue("X");
							} 
							/*
							//if Patient is less than 18 years old
							if(patientAge < 18 ) {
								observationValueSlot.getST().setValue("X");
							}
							
							//check if the length of stay in ED is less than 2 days or more than 120 days 
							if(wait2Days < 2 || wait2Days > 120) {
								observationValueSlot.getST().setValue("X");
							}*/
							
						} else {
							//All Patients admitted to hospital from ED OTHER THAN (dischargestatus code 09)
							observationValueSlot.getST().setValue("I");
						}	
					}
					
					//Get the list of relationships with in ED2.3
					List<ActRelationship> ed23ActRelationshipList = actRelationship.getAct().getRelationships();
					
					for (ActRelationship ed23ActRelationship : ed23ActRelationshipList) {
						System.out.println(" actRelationship.getName() " + ed23ActRelationship.getName());

						//set Wait 1 
						if (ed23ActRelationship.getName().equalsIgnoreCase("edvalue")) {
							List<ObservationValueSlot> ed23bservationValueSlotList = ed23ActRelationship.getAct().getObservation().getValues();

							for (ObservationValueSlot observationValueSlot : ed23bservationValueSlotList) {
								observationValueSlot.getPQ().setValue(new Double(wait2));
							}
						}
					}
				}	
				
				
				/*************  THE LOGIC FOR ED1.1,ED1.2,ED1.3,ED2.1,ED2.2,ED2.3********ENDS********/
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
	public long getWait1(Trim trim) {
		System.out.println("===============================getWait1===================================");
		String edAdmissionDateTime = new String();
		String edDiscDateTime = new String();
		long diffMinutes = 0l;

		List<ActRelationship> relList = trim.getAct().getRelationships();
		for (ActRelationship actRelationship : relList) {
			if (actRelationship.getName().equalsIgnoreCase("ipDates")) {
				List<ObservationValueSlot> observationValueSlotList = actRelationship.getAct().getObservation().getValues();
				for (ObservationValueSlot observationValueSlot : observationValueSlotList) {

					if (observationValueSlot.getLabel().getValue().equalsIgnoreCase("ED Admission Date and time")) {
						edAdmissionDateTime = observationValueSlot.getTS().getValue();
						System.out.println("ED Admission Date and time : " + edAdmissionDateTime);
					}
					if (observationValueSlot.getLabel().getValue().equalsIgnoreCase("ED Discharge Date and time")) {
						edDiscDateTime = observationValueSlot.getTS().getValue();
						System.out.println("ED Discharge Date and time : " + edDiscDateTime);
					}

				}

			}

		}
		
		/**
		 * Check the date formats for ED admin date time and ED discharge date time using the following formats
		 * 
		 * 1. yyyyMMddHHmmssZ
		 * 2. yyyyMMddHHmm
		 * 3. yyyy-MM-dd HH:mm:ss.0
		 *
		 */
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

		diffMinutes = getDateDifference(edAdmissionDate, edDischDate, HCQM3.MINUTES);
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

		/**
		 * Check the date formats for ED admin order date time and ED discharge date time using the following formats
		 * 
		 * 1. yyyyMMddHHmmssZ
		 * 2. yyyyMMddHHmm
		 * 3. yyyy-MM-dd HH:mm:ss.0
		 *
		 */
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
		diffMinutes = getDateDifference(admOrderDate,edDiscDate, HCQM3.MINUTES);

		return diffMinutes;
	}
	/**
	 * 
	 * @param trim
	 * @return
	 */
	public long getPatientAge(Trim trim) {
		System.out.println("===============================getPatientAge===================================");
 
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

		/**
		 * Check the date formats for ED Admit date time and Patient DOB using the following formats
		 * 
		 * 1. yyyyMMddHHmmssZ
		 * 2. yyyyMMddHHmm
		 * 
		 *
		 */
	 
		Date admOrderDate = convertStringToDate(adminOderDateTime, "yyyyMMddHHmmssZ");
		if(admOrderDate ==null) {
			admOrderDate = convertStringToDate(adminOderDateTime, "yyyyMMddHHmm");
		}
		
		Date patientDobDate = convertStringToDate(patientDob, "yyyyMMddHHmmssZ");
		if(patientDobDate ==null) {
			patientDobDate = convertStringToDate(patientDob, "yyyyMMddHHmm");
		}	
		

		patientAge = getDateDifference(patientDobDate,admOrderDate, HCQM3.YEARS);
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
			
			if(option.equals(HCQM3.SECONDS)){
				difference = edDiff / (1000);
			
			}
			
			if(option.equals(HCQM3.MINUTES)){
				difference = edDiff/(1000*60);
			}
			
			if(option.equals(HCQM3.HOURS)){
				difference   = edDiff/(60*60*1000);
			}
			
			if(option.equals(HCQM3.DAYS)){
				difference    = (edDiff/(24 * 60 * 60 * 1000));
			}
			
			if(option.equals(HCQM3.YEARS)){
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
			
			if(option.equals(HCQM3.SECONDS)){
				difference = edDiff / (1000);
			
			}
			
			if(option.equals(HCQM3.MINUTES)){
				difference = edDiff/(1000*60);
			}
			
			if(option.equals(HCQM3.HOURS)){
				difference   = edDiff/(60*60*1000);
			}
			
			if(option.equals(HCQM3.DAYS)){
				difference    = (edDiff/(24 * 60 * 60 * 1000));
			}
			
			if(option.equals(HCQM3.YEARS)){
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




