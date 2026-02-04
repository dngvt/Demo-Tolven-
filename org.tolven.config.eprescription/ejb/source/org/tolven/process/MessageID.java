/*
 *  Copyright (C) 2011 Tolven Inc
 *
 * This library is free software; you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either version 2.1 of the License, or (at your option) 
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 * details.
 *
 * Contact: info@tolvenhealth.com
 */
package org.tolven.process;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.tolven.el.ExpressionEvaluator;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.ST;
import org.tolven.trim.Compute.Property;
import org.tolven.trim.ex.ActEx;

/**
 * Generates unique messageID for each message to be send to Surescripts.
 * @author unni.s@cyrusxp.com
 * @since v0.0.1
 */

public class MessageID extends EprescriptionComputeBase {
	private ActEx act;
	private Long medicationId;
	private String effectiveLowTime;
	private String effectiveHighTime;
	private String action;
	private boolean enabled;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	public void compute() throws Exception {
		super.checkProperties();
		if (isEnabled()) {
			String accountType = getAccountUser().getAccount().getAccountType().getKnownType();
		
			ExpressionEvaluator evaluator = new ExpressionEvaluator();
			evaluator.addVariable("act", this.getAct());
			StringBuffer plain = new StringBuffer();
			if (accountType.equals("echr")) {
				if (getAction().equals("prescription")) {	
					String newRxStatus = evaluator.evaluate("#{act.relationship['status'].act.text.ST.value}").toString();
					if (StringUtils.isBlank(newRxStatus) || (!newRxStatus.equals("Verified") && !newRxStatus.equals("Success"))) {
						//plain.append(act.getRelationship().get("toSureScripts").getAct().getObservation().getValues().get(0).getST().getValue());
						//get patient last name
						plain.append(evaluator.evaluate("#{act.participation['subject'].role.player.name.EN['L'].part['FAM'].ST.value}"));
						//plain.append(act.getRelationship().get("toSureScripts").getAct().getObservation().getValues().get(1).getST().getValue());
						//plain.append(act.getRelationship().get("toSureScripts").getAct().getObservation().getValues().get(9).getST().getValue());
						//plain.append(act.getRelationship().get("medicationDetails").getAct().getObservation().getValues().get(11).getTS().getValue());
						//get medication date
						plain.append(evaluator.evaluate("#{act.effectiveTime.TS.value}"));
						//effectiveHighTime = act.getRelationship().get("medicationDetails").getAct().getEffectiveTime().getIVLTS().getHigh().getTS().getValue();
						//effectiveLowTime = act.getRelationship().get("medicationDetails").getAct().getEffectiveTime().getIVLTS().getLow().getTS().getValue();
						effectiveHighTime = act.getParticipation().get("consumableProduct").getRole().getEffectiveTime().getIVLTS().getHigh().getTS().getValue();
						effectiveLowTime =  act.getParticipation().get("consumableProduct").getRole().getEffectiveTime().getIVLTS().getLow().getTS().getValue();
						
						if (!effectiveLowTime.equals("") && !effectiveHighTime.equals("")) {
							plain.append(effectiveLowTime);
							plain.append(effectiveHighTime);
							plain.append(System.currentTimeMillis());
							evaluator.setValue("#{act.relationship['messageId'].act.text.ST.value}",generateMessageId(new String(plain)));
							disableCompute();
						}
					}	
				} else if (getAction().equals("accountRegistration")) {

					plain.append(act.getRelationship().get("registration")
							.getAct().getObservation().getValues().get(0)
							.getST().getValue());
					plain.append(act.getRelationship().get("registration")
							.getAct().getObservation().getValues().get(8)
							.getST().getValue());
					plain.append(act.getRelationship().get("registration")
							.getAct().getObservation().getValues().get(9)
							.getTS().getValue());
					plain.append(act.getRelationship().get("registration")
							.getAct().getObservation().getValues().get(10)
							.getTS().getValue());
					plain.append(System.currentTimeMillis());
					act.getRelationship().get("registration").getAct()
							.getObservation().getValues().get(12)
							.setST(getST(generateMessageId(new String(plain))));

				} else if (getAction().equals("prescriberRegistration")) {
					List<ObservationValueSlot> prescriberOVS = act.getRelationship().get("prescriber").getAct().getObservation().getValues();
						String activeStartTime = prescriberOVS.get(4).getTS().getValue();
						String activeEndTime = prescriberOVS.get(5).getTS().getValue();
						if (null != activeStartTime && null != activeEndTime) {
							plain.append(prescriberOVS.get(0).getINT().getValue());
							plain.append(prescriberOVS.get(1).getINT().getValue());					
							plain.append(prescriberOVS.get(3).getST().getValue());
							
							plain.append(prescriberOVS.get(7).getST().getValue());					
							plain.append(prescriberOVS.get(8).getST().getValue());
						
							plain.append(activeStartTime);
							plain.append(activeEndTime);
							plain.append(System.currentTimeMillis());
							prescriberOVS.get(10).setST(getST(generateMessageId(new String(plain))));
							disableCompute();
						}
				}else if (getAction().equals("refillRequest")) {
					
					if (!act.getRelationship().get("header").getAct().getObservation().getValues().get(8).getST().getValue().equals(getAccountUser().getOpenMeFirst())){
						act.getRelationship().get("response").getAct().getObservation().getValues().get(0).getCE().setDisplayName("Denied New Prescription");
						act.getRelationship().get("response").getAct().getObservation().getValues().get(0).getCE().setCode("C0024772");
					}

					plain.append(act.getRelationship().get("MedicationPrescribed")
							.getAct().getObservation().getValues().get(0)
							.getST().getValue());
					plain.append(act.getRelationship().get("MedicationPrescribed")
							.getAct().getObservation().getValues().get(1)
							.getST().getValue());
					plain.append(act.getRelationship().get("prescriber")
							.getAct().getObservation().getValues().get(1)
							.getST().getValue());
					plain.append(act.getRelationship().get("prescriber")
							.getAct().getObservation().getValues().get(2)
							.getST().getValue());
					plain.append(System.currentTimeMillis());
					if (act.getRelationship().get("response").getAct()
							.getObservation().getValues().get(4).getST().getValue().trim().isEmpty()) {
					act.getRelationship().get("response").getAct()
							.getObservation().getValues().get(4)
							.setST(getST(generateMessageId(new String(plain))));
					}
					
					disableCompute();

				} else if (getAction().equals("rxChangeRequest")) {

					plain.append(act.getRelationship().get("FinalMedication")
							.getAct().getObservation().getValues().get(0)
							.getST().getValue());
					plain.append(act.getRelationship().get("prescriber")
							.getAct().getObservation().getValues().get(1)
							.getST().getValue());
					plain.append(act.getRelationship().get("prescriber")
							.getAct().getObservation().getValues().get(2)
							.getST().getValue());
					plain.append(act.getRelationship().get("pharmacy")
							.getAct().getObservation().getValues().get(0)
							.getST().getValue());
					plain.append(System.currentTimeMillis());
					act.getRelationship().get("responseType").getAct()
							.getObservation().getValues().get(5)
							.setST(getST(generateMessageId(new String(plain))));

				} 
			} else if (accountType.equals("ephr")) {
				String trimName = getTrim().getName();
				if (trimName.equals("obs/evn/overCounter")) {		
					if (act.getId() != null) {
						String extension = act.getId().getIIS().get(0).getExtension();
						medicationId = Long.valueOf(extension.split("-")[2]);
						act.getRelationship().get("overCounter").getAct()
								.getObservation().getValues().get(10).getINT()
								.setValue(medicationId);
					}
					plain.append(act.getRelationship().get("overCounter")
							.getAct().getObservation().getValues().get(1)
							.getTS().getValue());
					plain.append(act.getRelationship().get("overCounter")
							.getAct().getObservation().getValues().get(5)
							.getTS().getValue());
					plain.append(act.getRelationship().get("overCounter")
							.getAct().getObservation().getValues().get(6)
							.getTS().getValue());
					plain.append(System.currentTimeMillis());
					act.getRelationship().get("overCounter").getAct()
							.getObservation().getValues().get(8)
							.setST(getST(generateMessageId(new String(plain))));
				} else {
					if (act.getId() != null) {
						String extension = act.getId().getIIS().get(0).getExtension();
						medicationId = Long.valueOf(extension.split("-")[2]);
						act.getRelationship().get("medicationRecord").getAct()
								.getObservation().getValues().get(10).getINT()
								.setValue(medicationId);
					}
					plain.append(act.getRelationship().get("medicationRecord")
							.getAct().getObservation().getValues().get(1)
							.getTS().getValue());
					plain.append(act.getRelationship().get("medicationRecord")
							.getAct().getObservation().getValues().get(5)
							.getTS().getValue());
					plain.append(act.getRelationship().get("medicationRecord")
							.getAct().getObservation().getValues().get(6)
							.getTS().getValue());
					plain.append(System.currentTimeMillis());
					act.getRelationship().get("medicationRecord").getAct()
							.getObservation().getValues().get(8)
							.setST(getST(generateMessageId(new String(plain))));
				}
			}// else if ends
		}
	}

	/**
	 * Generates message id.
	 * @param plain
	 * @return unique MessageID
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
	 * Disables the compute function.
	 */
	private void disableCompute() {
		for (Property property : getComputeElement().getProperties()) {
			if ("enabled".equals(property.getName())) {
				property.setValue(Boolean.FALSE);
			}
		}
	}
	
	/**
	 * This function is used to convert string to ST.
	 * @author Valsaraj
	 * @since 07/16/09
	 * @param string
	 * @return ST
	 */
	public ST getST(String str) {
		ST st = new ST();
		st.setValue(str);
		return st;
	}

	/**
	 * @return the medicationId
	 */
	public Long getMedicationId() {
		return medicationId;
	}

	/**
	 * @param medicationId the medicationId to set
	 */
	public void setMedicationId(Long medicationId) {
		this.medicationId = medicationId;
	}
}
