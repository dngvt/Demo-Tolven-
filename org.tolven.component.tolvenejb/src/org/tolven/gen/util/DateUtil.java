package org.tolven.gen.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;


import org.tolven.trim.ex.HL7DateFormatUtility;

public class DateUtil {
	public static final long DAYS = 1000*60*60*24;
	public static final long YEARS = DAYS*365;
	public static boolean isLeapYear(int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
	}
	
	public static String formatTS(String dateFormatString, Calendar calendar, Locale locale, TimeZone timeZone){
		boolean monthExists = true, dateExists = true, hrExists = true, secExists = true, ampmExists = true;
		calendar.setTimeZone(timeZone);
		String source = dateFormatString;
		if (source == null) { 
			source = "d MMM yyyy hh:mm:ss"; 
		}
		return formatValue(source, monthExists, dateExists, hrExists, secExists, ampmExists, calendar, locale);
	}
	
	public static String formatTS(String dateFormatString, String ts, Locale locale, TimeZone timeZone) {
		boolean monthExists = true, dateExists = true, hrExists = true, secExists = true, ampmExists = true;
		int dateLength = ts.length();
		Calendar calendar = new GregorianCalendar(timeZone);		
		Date tsDate = null;
		
		try {
			tsDate = HL7DateFormatUtility.parseDate(ts, timeZone);
			calendar.setTime(tsDate);
		} catch(ParseException e) {
			throw new RuntimeException( "Error parsing TS in hl7TSToDate " +  e);
		}
		
		
		String source = dateFormatString;
		if (source == null) { 
			source = "d MMM yyyy hh:mm:ss"; 
		}
		
		switch(dateLength) {
		case 4: 
			monthExists = false;
		case 6: 
			dateExists = false;
		case 8: 
			hrExists = false;
		case 10:   
			secExists = false;
			calendar.set(Calendar.MINUTE, 0);
		case 12: 
			ampmExists = false;			
		default:
			break;
		}
		return formatValue(source, monthExists, dateExists, hrExists, secExists, ampmExists, calendar, locale);
	}
	private static String formatValue(String source, boolean monthExists,boolean dateExists,boolean hrExists,
			boolean secExists,boolean ampmExists,Calendar calendar,Locale locale){		
		StringBuffer sb = new StringBuffer(30);
		boolean first = true;
		while (source.length() > 0) {
			if (source.startsWith("yyyy")) {
				source = source.substring(4);
				if (first) {
					sb.append("%tY");
					first = false;
				} else {
					sb.append("%<tY");
				}
				continue;
			}
			if (source.startsWith("yy")) {
				source = source.substring(2);
				if (first) {
					sb.append("%ty");
					first = false;
				} else {
					sb.append("%<ty");
				}
				continue;
			}
			if (source.startsWith("MMMM")) {
				source = source.substring(4);
				if (!monthExists ) continue;  
				if (first) {
					sb.append("%tB");
					first = false;
				} else {
					sb.append("%<tB");
				}
				continue;
			}
			if (source.startsWith("MMM")) {
				source = source.substring(3);
				if (!monthExists ) continue;				
				if (first) {
					sb.append("%tb");
					first = false;
				} else {
					sb.append("%<tb");
				}
				continue;
			}
			if (source.startsWith("MM")) {
				source = source.substring(2);
				if (!monthExists ) continue;				
				if (first) {
					sb.append("%tm");
					first = false;
				} else {
					sb.append("%<tm");
				}
				continue;
			}
			if (source.startsWith("dd")) {
				source = source.substring(2);
				if (!dateExists) continue;				
				if (first) {
					sb.append("%td");
					first = false;
				} else {
					sb.append("%<td");
				}
				continue;
			}
			if (source.startsWith("d")) {
				source = source.substring(1);
				if (!dateExists) continue;
				if (first) {
					sb.append("%te");
					first = false;
				} else {
					sb.append("%<te");
				}
				continue;
			}
			if (source.startsWith("hh") || source.startsWith("HH")) {
				source = source.substring(2);
				if (!hrExists) continue;
				if (first) {
					sb.append("%tk");
					first = false;
				} else {
					sb.append("%<tk");
				}
				continue;
			}
			if (source.startsWith("mm")) {
				source = source.substring(2);
				if (!hrExists) continue;
				if (first) {
					sb.append("%tM");
					first = false;
				} else {
					sb.append("%<tM");
				}
				continue;
			}
			if (source.startsWith("ss")) {
				source = source.substring(2);
				if (!secExists) continue;				
				if (first) {
					sb.append("%tS");
					first = false;
				} else {
					sb.append("%<tS");
				}
				continue;
			}
			if (ampmExists && source.startsWith("a")) {
				source = source.substring(1);
				if (!ampmExists) continue;
				if (first) {
					sb.append("%tp");
					first = false;
				} else {
					sb.append("%<tp");
				}
				continue;
			}
			if (!first) {
			  // this will address format like dd-MM-yyyy with values being 1981 and 198112
			  sb.append(source.charAt(0));
			}  
			source = source.substring(1);
		}
		
		return String.format(sb.toString(), calendar, locale);
	}

}
