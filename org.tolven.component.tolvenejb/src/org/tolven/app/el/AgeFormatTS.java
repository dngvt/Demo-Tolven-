/*
 *  Copyright (C) 2006 Tolven Inc 
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * Contact: info@tolvenhealth.com
 */
package org.tolven.app.el;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

import org.tolven.gen.util.DateUtil;
import org.tolven.locale.ResourceBundleHelper;
import org.tolven.trim.TS;
import org.tolven.trim.ex.HL7DateFormatUtility;

public class AgeFormatTS {
	/**
	 * Using "now" and "date of birth", compute a readable age string.
	 * STILL NEEDS I18N support.
	 * @param dob
	 * @param now
	 * @return
	 */
	public static String toAgeString( TS tsDob, Calendar now, Locale locale ) {
		ResourceBundle bundle;
		if (tsDob == null || tsDob.getValue() == null || tsDob.getValue().trim().length() == 0 )
			return null;
		if (locale==null) {
			bundle = ResourceBundle.getBundle(ResourceBundleHelper.GLOBALBUNDLENAME, Locale.getDefault());
		} else {
			bundle = ResourceBundle.getBundle(ResourceBundleHelper.GLOBALBUNDLENAME, locale);			
		}
		Date dob = null;
		try {
			dob = HL7DateFormatUtility.parseDate(tsDob.getValue());
		} catch (ParseException e) {
			throw new RuntimeException( "Error parsing TS in toAgeString " +  e);
		}
		GregorianCalendar dobCal = new GregorianCalendar();
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
		
	    if (dobCal.after(now)) return bundle.getString("ageUnborn");
	    // Get age in years
	    int years = now.get(Calendar.YEAR)- dobCal.get(Calendar.YEAR);
	    int days = now.get( Calendar.DAY_OF_YEAR ) - dobCal.get( Calendar.DAY_OF_YEAR );
	    if (days < 0) 
	      {
	        years--;
	        days = days + 365;
	      }
	    if (years > 1) return Integer.toString( years ) + bundle.getString("ageInYears");
	    if (years == 0 && days < 30) 
	     {
	        return Integer.toString( days ) + bundle.getString("ageInDays");
	     }
	    return Integer.toString( years * 12 + (days/30) ) + bundle.getString("ageInMonths");
	}
	
	/**
	 * Simple version that does not contain timezone awareness
	 * @param dob
	 * @param now
	 * @return
	 */
	public static String toAgeString( TS tsDob, Date now ) {
	    Calendar n = new GregorianCalendar();
	    n.setTime( now );
	    return toAgeString( tsDob, n, null);
	}
}
