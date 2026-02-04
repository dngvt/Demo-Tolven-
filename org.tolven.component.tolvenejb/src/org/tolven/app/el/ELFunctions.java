package org.tolven.app.el;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.tolven.app.MenuLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountUser;
import org.tolven.gen.util.DateUtil;
import org.tolven.provider.ProviderLocal;
import org.tolven.provider.entity.Provider;

import org.tolven.trim.Act;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.ActStatus;
import org.tolven.trim.CE;
import org.tolven.trim.DataType;
import org.tolven.trim.II;
import org.tolven.trim.SETIISlot;
import org.tolven.trim.TS;
import org.tolven.trim.IVLPQ;
import org.tolven.trim.ex.HL7DateFormatUtility;
import org.tolven.trim.ex.TrimFactory;

public class ELFunctions {
	private static final TrimFactory factory = new TrimFactory();
	public static final String MENUDATA_ID = ".1.";

	// public static final String NEED_ID = ".99999.";

	public static String computeIDRoot(Account account) {
		return System.getProperty("tolven.repository.oid") + MENUDATA_ID
				+ account.getId();
	}

	public static String getAccountAddress(Account account) {
		return account.getProperty().get("clinic_address_line1") + "," +
		account.getProperty().get("clinic_city") + " " +
		account.getProperty().get("clinic_state") + " " + account.getProperty().get("clinic_zip");
	}
	
	// public static String computeNeedRoot( Account account ) {
	// return System.getProperty("tolven.repository.oid")+ NEED_ID +
	// account.getId();
	// }

	public static ProviderLocal getProviderBean() throws NamingException {
		InitialContext ctx = null;
		ProviderLocal providerBean = null;
		if (ctx == null) {
			ctx = new InitialContext();
		}
		if (providerBean == null) {
			providerBean = (ProviderLocal) ctx
					.lookup("java:global/tolven/tolvenEJB/ProviderBean!org.tolven.provider.ProviderLocal");
		}
		return providerBean;
	}

	public static MenuLocal getMenuBean() throws NamingException {
		InitialContext ctx = null;
		MenuLocal menuBean = null;
		if (ctx == null) {
			ctx = new InitialContext();
		}
		if (menuBean == null) {
			menuBean = (MenuLocal) ctx
					.lookup("java:global/tolven/tolvenEJB/MenuBean!org.tolven.app.MenuLocal");
		}
		return menuBean;
	}

	public static MenuData placeholderByPath(Account account, String path)
			throws NamingException {	
		if (path == null)
			return null;
		return getMenuBean().findMenuDataItem(account.getId(), path);
	}

	/**
	 * Return the placeholder identified by the specified ID slot
	 * 
	 * @param account
	 * @param idSlot
	 * @return placeholder
	 * @throws NamingException
	 */
	public static MenuData placeholder(Account account, SETIISlot idSlot)
			throws NamingException {		
		if (idSlot == null)
			return null;
		String path = internalId(account, idSlot);
		return placeholderByPath(account, path);
	}

	/**
	 * Find the appropriate internal id for the given account
	 * 
	 * @param account
	 * @param idSlot
	 *            A Slot containing a set of IIs
	 * @return A String containing the path, eg echr:patient-123
	 */
	public static String internalId(Account account, SETIISlot idSlot) {
		String root = computeIDRoot(account);
		if (idSlot == null)
			return null;
		for (II ii : idSlot.getIIS()) {
			if (root.equals(ii.getRoot())) {
				return ii.getExtension();
			}
		}
		return null;
	}

	/**
	 * given a passed in interpretation code, pass back an appropriate CSS style
	 * class
	 */
	public static String interpCodeClass(String interpCode) {
		if (interpCode.equals("L")) {
			return "Low";
		} else if (interpCode.equals("H")) {
			return "High";
		} else if (interpCode.equals("N")) {
			return "Normal";
		} else {
			return "Normal";
		}
	}

	public static Double lowValueWithGender(Act act, String genderCode) {
		List<ActRelationship> relationships = null;
		ActRelationship selectedRelationship = null;
		Double returnedRange = 0.00;

		if (act == null || act.getObservation() == null
				|| act.getObservation().getValues() == null
				|| act.getObservation().getValues().size() == 0
				|| act.getObservation().getValues().get(0) == null
				|| act.getObservation().getValues().get(0).getPQ() == null
	            || act.getRelationships() == null
	            || act.getRelationships().size() == 0
	        )
			return returnedRange;

		relationships = act.getRelationships();

		// Traverse the relationships, looking for preconditions, if there
		// are
		// and one matches, select it.
		for (ActRelationship lRel : relationships) {
			if (lRel.getName().equals("referenceRange")) {
				if (lRel.getAct().getRelationships().size() > 0) {
					for (ActRelationship pRel : lRel.getAct()
							.getRelationships()) {
						if (pRel.getName().equals("precondition")) {
							if (null != pRel.getAct()) {
								if (pRel.getAct().getObservation().getValues()
										.get(0).getCE().getDisplayName()
										.equals(genderCode)) {
									selectedRelationship = lRel;
								}
							}
						}
					}
				}
			}
		}

		if (null == selectedRelationship) {
			// look for a lab result
			for (ActRelationship lRel : relationships) {
				if ("labresult".equalsIgnoreCase(lRel.getName())) {
					if (lRel.getAct().getRelationships().size() > 0) {
						relationships = lRel.getAct().getRelationships();
					}
				}
			}
		}

		// if selectedRelationship is still null, there were no precons
		// and we need to just select the first referenceRange.
		if (null == selectedRelationship) {
			selectedRelationship = relationships.get(0);
		}

		// Found relationship, extract data.
		if (null != selectedRelationship
				&& null != selectedRelationship.getAct().getObservation()
						.getValues().get(0).getIVLPQ()) {
			IVLPQ selectedIVLPQ = selectedRelationship.getAct()
					.getObservation().getValues().get(0).getIVLPQ();
			if (null != selectedIVLPQ.getLow()) {
				returnedRange = selectedIVLPQ.getLow().getPQ().getValue();
			} else {
				returnedRange = (Double) 0.00;
			}

		}
		return returnedRange;
	}

	public static Double highValueWithGender(Act act, String genderCode) {
		List<ActRelationship> relationships = null;
		ActRelationship selectedRelationship = null;
		Double returnedRange = 0.00;
		if (act == null || act.getObservation() == null
				|| act.getObservation().getValues() == null
				|| act.getObservation().getValues().size() == 0
				|| act.getObservation().getValues().get(0) == null
				|| act.getObservation().getValues().get(0).getPQ() == null
	            || act.getRelationships() == null
	            || act.getRelationships().size() == 0
	        )
			return returnedRange;

		relationships = act.getRelationships();

		// Traverse the relationships, looking for preconditions, if there are
		// and one matches, select it.
		for (ActRelationship lRel : relationships) {
			if (lRel.getName().equals("referenceRange")) {
				if (lRel.getAct().getRelationships().size() > 0) {
					for (ActRelationship pRel : lRel.getAct()
							.getRelationships()) {
						if (pRel.getName().equals("precondition")) {
							if (null != pRel.getAct()) {
								if (pRel.getAct().getObservation().getValues()
										.get(0).getCE().getDisplayName()
										.equals(genderCode)) {
									selectedRelationship = lRel;
								}
							}
						}
					}
				}
			}
		}

		if (null == selectedRelationship) {
			// look for a lab result
			for (ActRelationship lRel : relationships) {
				if ("labresult".equalsIgnoreCase(lRel.getName())) {
					if (lRel.getAct().getRelationships().size() > 0) {
						relationships = lRel.getAct().getRelationships();
					}
				}
			}
		}

		// if selectedRelationship is still null, there were no precons
		// and we need to just select the first referenceRange.
		if (null == selectedRelationship) {
			selectedRelationship = relationships.get(0);
		}

		// Found relationship, extract data.
		if (null != selectedRelationship
				&& null != selectedRelationship.getAct().getObservation()
						.getValues().get(0).getIVLPQ()) {
			IVLPQ selectedIVLPQ = selectedRelationship.getAct()
					.getObservation().getValues().get(0).getIVLPQ();
			if (null != selectedIVLPQ.getHigh()) {
				returnedRange = selectedIVLPQ.getHigh().getPQ().getValue();
			} else {
				returnedRange = (Double) 0.00;
			}

		}
		return returnedRange;
	}

	/*
	 * pass relationships and traverse them to grab the values and put into a
	 * return string
	 */
	public static String reactionSeverity(List<ActRelationship> relationships) {
		List<String> rawReactions = new ArrayList<String>();
		List<String> rawSeverities = new ArrayList<String>();
		String element = null;
		String response = "";

		if (null != relationships) {
			for (int i = 0; i < relationships.size(); i++) {
				ActRelationship aRelation = relationships.get(i);
				List<ActRelationship> reactions = aRelation.getAct()
						.getRelationships();
				for (int x = 0; x < reactions.size(); x++) {
					ActRelationship aReaction = reactions.get(x);
					if ("reaction".equals(aReaction.getName())) {
						element = reactions.get(x).getAct().getObservation()
								.getValues().get(0).getCE().getDisplayName();
						rawReactions.add(element);
					} else if ("severity".equals(aReaction.getName())) {
						element = reactions.get(x).getAct().getObservation()
								.getValues().get(0).getCE().getDisplayName();
						rawSeverities.add(element);
					}

				}
			}
			for (int i = 0; i < rawReactions.size(); i++) {
				response += "(" + rawReactions.get(i) + " / "
						+ rawSeverities.get(i) + ") ";
			}
		}

		return response;
	}

	/*
	 * pass relationships and traverse them to grab the reactions in a comma
	 * separated list
	 */
	public static String reactions(List<ActRelationship> relationships) {
		List<String> rawReactions = new ArrayList<String>();
		String element = null;
		StringBuffer response = new StringBuffer();

		if (null != relationships) {
			for (int i = 0; i < relationships.size(); i++) {
				ActRelationship aRelation = relationships.get(i);
				List<ActRelationship> reactions = aRelation.getAct()
						.getRelationships();
				for (int x = 0; x < reactions.size(); x++) {
					ActRelationship aReaction = reactions.get(x);
					if ("reaction".equals(aReaction.getName())) {
						element = reactions.get(x).getAct().getObservation()
								.getValues().get(0).getCE().getDisplayName();
						rawReactions.add(element);
					}

				}
			}
			for (int i = 0; i < rawReactions.size(); i++) {
				if(response.length()>0)
					response.append(",");
				response.append(rawReactions.get(i));
			}
		}
		return response.toString();
	}

	/**
	 * Return the first non-null parameter. The function name "from" is used
	 * because it functions similar to the &lt;from&gt; element in
	 * x.application.xml
	 * 
	 * @param objects
	 * @return
	 */
	public static Object from(Object object1, Object object2) {
		if (object1 != null)
			return object1;
		if (object2 != null)
			return object2;
		// for (int x = 0; x < objects.length; x++) {
		// if (objects[x]!=null) return objects[x];
		// }
		return null;
	}
	
	private static String fromCEType(Object theObject1, String ceType) {
		String CEcontent = "";
		/*
		 * <displayName>Asian</displayName> <code>2567338</code>
		 * <codeSystem>2.16.840.1.113883.3.26.2</codeSystem>
		 * <codeSystemName>caDSR</codeSystemName>
		 * <codeSystemVersion>2.1</codeSystemVersion>
		 */
		// first grab the CE
		CE ce = new CE();
		List<CE> ces = new ArrayList<CE>();
		if (theObject1 instanceof CE) {
			ce = (CE) theObject1;
			ces.add(ce);
		} else if (theObject1 instanceof List<?>) {
			ces = (List<CE>) theObject1;
		} else {
			ce.setDisplayName(theObject1.toString());
		}
		int count = 0;
		for (CE ace : ces) {
			if (ces.size() > 1) {
				// This is only when it is a list
				count += 1;
			}
			// Next write out all those fancy values
			if (ace.getDisplayName() != null) {
				CEcontent += "<displayName>" + ace.getDisplayName()
						+ "</displayName>\n";
			}
			if (ace.getCode() != null) {
				CEcontent += "<code>" + ace.getCode() + "</code>\n";
			}
			if (ace.getCodeSystem() != null) {
				CEcontent += "<codeSystem>" + ace.getCodeSystem()
						+ "</codeSystem>\n";
			}
			if (ace.getCodeSystemName() != null) {
				CEcontent += "<codeSystemName>" + ace.getCodeSystemName()
						+ "</codeSystemName>\n";
			}
			if (ace.getCodeSystemVersion() != null) {
				CEcontent += "<codeSystemVersion>" + ace.getCodeSystemVersion()
						+ "</codeSystemVersion>\n";
			}
			if (ces.size() > 1 && count < ces.size()) {
				// This is only when it is a list, don't do this on the last
				// element
				CEcontent += "</" + ceType + ">";
				CEcontent += "<" + ceType + ">";
			}
		}

		return CEcontent;
	}

	public static String fromCE(Object object1, Object object2) {
		String CEcontent = "";
		/*
		 * <displayName>Asian</displayName> <code>2567338</code>
		 * <codeSystem>2.16.840.1.113883.3.26.2</codeSystem>
		 * <codeSystemName>caDSR</codeSystemName>
		 * <codeSystemVersion>2.1</codeSystemVersion>
		 */
		if (object1 != null) {
			return fromCEType(object1, "CE");
			
		} else if (object2 != null) {
			return fromCEType(object2, "CE");
		}
		
		return CEcontent;
	}
	
	public static String fromSETCE(Object object1, Object object2) {
		String CEcontent = "";
		/*
		 * <displayName>Asian</displayName> <code>2567338</code>
		 * <codeSystem>2.16.840.1.113883.3.26.2</codeSystem>
		 * <codeSystemName>caDSR</codeSystemName>
		 * <codeSystemVersion>2.1</codeSystemVersion>
		 */
		if (object1 != null) {
			return fromCEType(object1, "SETCE");
			
		} else if (object2 != null) {
			return fromCEType(object2, "SETCE");
		}

		return CEcontent;
	}

	public static Date TStoDate(String time) {
		if (time == null || time.length() == 0)
			return null;
		try {
			String sdfString = "yyyyMMddhhmmss".substring(0,
					Math.min(time.length(), 14));
			SimpleDateFormat sdf = new SimpleDateFormat(sdfString);
			Date date = sdf.parse(time);
			return date;
		} catch (Exception e) {
			throw new RuntimeException("Error parsing date string " + time, e);
		}
	}

	/**
	 * Return a proper TS given a Java date object
	 * 
	 * @param date
	 * @return TS object
	 */
	public static String TS(Date date) {
		if (date == null)
			return null;
		return HL7DateFormatUtility.formatHL7TSFormatL16Date(date);
	}

	public static Provider findProvider(Long id) throws NamingException {
		return getProviderBean().findProvider(id);
	}

	public static String encode(DataType dataType) {
		return factory.dataTypeToString(dataType);
	}

	public static DataType decode(String encoded) {
		return factory.stringToDataType(encoded);
	}

	/**
	 * Calculate a new date based on the passed in date some duration forward or
	 * backwards (negative) and the units
	 * 
	 * @param d
	 *            date to start with
	 * @param duration
	 *            integer duration to move
	 * @param units
	 *            of measure (month, day, hour)
	 * @return newly calculated date
	 */
	public static Date dateCalc(Date d, int duration, String units) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);

		if ("month".equalsIgnoreCase(units)) {
			c.add(Calendar.MONTH, duration);
		} else if ("day".equalsIgnoreCase(units)) {
			c.add(Calendar.DAY_OF_YEAR, duration);
		} else if ("hour".equalsIgnoreCase(units)) {
			c.add(Calendar.HOUR, duration);
		}
		return c.getTime();
	}

	/**
	 * Given the Date of Birth and AccountUser return the age
	 * 
	 * @param dob
	 *            Date of Birth (usually of a patient)
	 * @param accountUser
	 * @return String containing age
	 */
	public static String age(Date dob, AccountUser accountUser) {
		Calendar b = new GregorianCalendar();
		b.setTime(dob);
		Calendar n = new GregorianCalendar();
		n.setTime(new Date());
		Locale locale = accountUser.getLocaleObject();
		return AgeFormat.toAgeString(b, n, locale);
	}

	public static final long SECOND = 1000;
	public static final long MINUTE = 60 * SECOND;
	public static final long HOUR = 60 * MINUTE;
	public static final long DAY = 24 * HOUR;
	public static final long YEAR = 365 * DAY + (DAY / 4);

	/**
	 * Given the Date of Birth and AccountUser return the age
	 * 
	 * @param dob
	 *            Date of Birth (usually of a patient)
	 * @param accountUser
	 * @return String containing age
	 */
	public static long ageInYears(Date dob) {
		if (dob == null)
			return 0;
		return ((new Date().getTime() - dob.getTime()) / YEAR);
	}

	// CCHIT merge
	/**
	 * Function to show elipses ("…") to indicate that the entire source
	 * details could not be displayed.
	 * 
	 * @author Pinky S Added on 02/09/11
	 * @param source
	 */
	public static String processSource(String source) {
		if (source != null && source.length() > 12)
			return source.substring(0, 12).concat("...");
		else
			return source;
	}

	public static String toLowerCaseValue(ActStatus status) {
		if (status != null)
			return status.value().toLowerCase();
		else
			return null;

	}

	/*
	 * public static TolvenPerson tp( String principal ) throws NamingException
	 * { return getLDAPBean().createTolvenPerson(principal); }
	 */
	public static String transDisplString(String transition) {
		if (transition != null
				&& transition.equalsIgnoreCase(ActStatus.ABORTED.value())) {
			return "Discontinued";
		}
		if (transition == null)
			return null;
		return transition.toLowerCase();
	}

	public static Object computeAgeTS(String tsDob, AccountUser accountUser, boolean stringFormat) {
		Long zeroLong = new Long(0);
		if (tsDob == null || tsDob.trim().length() == 0)
			return zeroLong;
		Date dob = null;
		TimeZone tzone = null;		
		try {
			if (accountUser != null)
				tzone = accountUser.getTimeZoneObject();
			else
				tzone = TimeZone.getDefault();
			dob = HL7DateFormatUtility.parseDate(tsDob, tzone);
		} catch (ParseException e) {
			return zeroLong;
		}
		GregorianCalendar dobCal = new GregorianCalendar(tzone);
		GregorianCalendar now = new GregorianCalendar(tzone);
		
		dobCal.setTime(dob);
		if (dobCal.get(Calendar.MONTH) == 0) {
			dobCal.set(Calendar.MONTH, now.get(Calendar.MONTH));
		}
		if (dobCal.get(Calendar.DATE) == 0) {
			// check for leap year here
			if (DateUtil.isLeapYear(now.get(Calendar.YEAR))
					&& !DateUtil.isLeapYear(dobCal.get(Calendar.YEAR))
					&& now.get(Calendar.DATE) == 29) {
				dobCal.set(Calendar.DATE, 28);
			} else {
				dobCal.set(Calendar.DATE, now.get(Calendar.DATE));
			}
		}
		if (dobCal.get(Calendar.HOUR_OF_DAY) == 0) {
			dobCal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
		}
		if (dobCal.get(Calendar.MINUTE) == 0) {
			dobCal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
		}
		if (stringFormat) {
			Locale locale = null;
			if (accountUser != null)
				locale = accountUser.getLocaleObject();
			else
				locale = Locale.getDefault();
			return AgeFormat.toAgeString(dobCal, now, locale);
		} else {		
		   long age = ((now.getTime().getTime() - dobCal.getTime().getTime())/YEAR);
		   return new Long(age);
		} 
	}

	public static long ageInYearsTS(TS tsDob) {
		if (tsDob == null)
			return 0;
		return ageInYearsTS(tsDob.getValue());
	}
	
	public static String age(TS tsDob, AccountUser accountUser) {		
		if (tsDob == null)
			return null;
		return (String)computeAgeTS(tsDob.getValue(), accountUser, true);
	}

	public static long ageInYearsTS(String tsDob) {
		Long ageValue = (Long)computeAgeTS(tsDob, null, false);
		if (ageValue == null)
			return 0;
		return ageValue.longValue();
	}
	
	public static long ageInYearsTS(String tsDob, AccountUser accountUser) {
		Long ageValue = (Long)computeAgeTS(tsDob, accountUser, false);
		if (ageValue == null)
			return 0;
		return ageValue.longValue();
	}	
	public static String status(String source1,String source2){
		if(source1 == null || source1.length() == 0)
			return source2; 
		else
			return source1.toLowerCase();
	}
}
