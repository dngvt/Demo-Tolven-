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
 * @version $Id: Patient.java,v 1.1 2009/05/24 22:39:32 jchurin Exp $
 */  

package org.tolven.model;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.tolven.app.entity.MenuData;
import org.tolven.trim.TS;
import org.tolven.trim.ex.HL7DateFormatUtility;


/**
 * Dynamic Patient wrapper used for rule processing (not a persistent entity - see underlying placeholder)
 * @author John Churin
 *
 */
public class Patient extends ModelObject {
	
	public Patient( MenuData placeholder, java.util.Date now) {
		super( placeholder, now);
	}
	
	public String getFirstName( ) {
		return (String) getPlaceholder().getField("firstName");
	}

	public String getLastName( ) {
		return (String) getPlaceholder().getField("lastName");
	}
	
	public String getMiddleName( ) {
		return (String) getPlaceholder().getField("middleName");
	}

	public String getSex( ) {
		return (String) getPlaceholder().getField("sex");
	}
	
	public String getReminderCheckBox() {
		return (String) getPlaceholder().getField("reminderCheckBox");
	}

	public int getAgeInMonths( ) {
		Object birthDate =  getPlaceholder().getField("dob");
		if (birthDate==null) return 0;
		GregorianCalendar dob = new GregorianCalendar( );
		if (birthDate instanceof Date) {
		   dob.setTime((Date)birthDate);
		} else if (birthDate instanceof TS) {
			try {
				Date d = HL7DateFormatUtility.parseDate(((TS)birthDate).getValue());
				if (null != d) {
					dob.setTime(d);
				}
				else {
					return 0;
				}
			} catch(ParseException e) {
			   return 0;
			}
		} else {
			return 0;
		}
		GregorianCalendar now = new GregorianCalendar( );
		now.setTime(getNow());
        int monthsOld = (now.get(Calendar.YEAR)*12+now.get(Calendar.MONTH)) - (dob.get(Calendar.YEAR)*12+dob.get(Calendar.MONTH));
        return monthsOld;
	}
	
	public int getAgeInYears( ) {
        return getAgeInMonths()/12;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!( obj instanceof Patient )) return false;
		Patient other = (Patient)obj;
		return (getPlaceholder().getId()==other.getPlaceholder().getId());
	}

	@Override
	public int hashCode() {
		return Long.valueOf(getPlaceholder().getId()).hashCode();
	}
	
}
