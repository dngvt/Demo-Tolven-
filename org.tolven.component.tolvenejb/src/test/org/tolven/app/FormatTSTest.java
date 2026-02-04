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
 * @version $Id: MenuDataTest.java,v 1.1 2009/06/04 19:00:30 jchurin Exp $
 */  

package test.org.tolven.app;

import java.util.Locale;
import java.util.TimeZone;

import org.tolven.gen.util.DateUtil;

import junit.framework.TestCase;

public class FormatTSTest extends TestCase {

    
    
	public void testHL7TSformat4( ) {
		assertEquals( "1981", DateUtil.formatTS("dd-MM-yyyy", "1981", Locale.ENGLISH, TimeZone.getDefault()));
	}
	public void testHL7TSformat6( ) {
		assertEquals( "12-1981", DateUtil.formatTS("dd-MM-yyyy", "198112", Locale.ENGLISH, TimeZone.getDefault()));
	}
	public void testHL7TSformat8( ) {
		assertEquals( "18-04-2009", DateUtil.formatTS("dd-MM-yyyy", "20090418", Locale.ENGLISH, TimeZone.getTimeZone("PST")));
	}
	public void testHL7TSformat12( ) {
		assertEquals( "11-12-1981 10:30", DateUtil.formatTS("dd-MM-yyyy HH:mm", "198112111030", Locale.ENGLISH, TimeZone.getDefault()));
	}
	public void testHL7TSformat14( ) {
		assertEquals( "11-12-1981 10:30:29", DateUtil.formatTS("dd-MM-yyyy HH:mm:ss", "19811211103029", Locale.ENGLISH, TimeZone.getDefault()));
	}
	public void testHL7TSformat4V2( ) {
		assertEquals( "1981", DateUtil.formatTS("d MMM yyyy", "1981", Locale.ENGLISH, TimeZone.getTimeZone("PST")));
	}	
	public void testHL7TSformat6V2( ) {
		assertEquals( "Dec 1981", DateUtil.formatTS("d MMM yyyy", "198112", Locale.ENGLISH, TimeZone.getTimeZone("PST")));
	}
	public void testHL7TSformat8V2( ) {
		assertEquals( "11 Dec 1981", DateUtil.formatTS("d MMM yyyy", "19811211", Locale.ENGLISH, TimeZone.getTimeZone("PST")));
	}
	public void testHL7TSformat12V2( ) {
		assertEquals( "11 Dec 1981 10:30", DateUtil.formatTS("d MMM yyyy HH:mm", "198112111030", Locale.ENGLISH, TimeZone.getTimeZone("PST")));
	}
	public void testHL7TSformat14V2( ) {
		assertEquals( "11 Dec 1981 10:30:29", DateUtil.formatTS("d MMM yyyy HH:mm:ss", "19811211103029", Locale.ENGLISH, TimeZone.getDefault()));
	}
}
