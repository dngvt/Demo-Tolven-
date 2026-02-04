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
 * @author Joseph Isaac
 * @version $Id: TSFormat.java 7192 2012-10-17 21:55:47Z joe.isaac $
 */
package org.tolven.app.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tolven.trim.TS;
import org.tolven.trim.ex.TrimFactory;

public class TSFormat {

    private static final String HL7TSFormatL16 = "yyyyMMddHHmmssZZ";
    private static Map<String, Integer> styleMap;
    private static TrimFactory trimFactory = new TrimFactory();

    public static void main(String[] args) {
        String[] patterns = {
                "MM/dd/yyyy hh:mm a z",
                "MM/dd/yyyy",
                "MM/yyyy",
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "MM/dd/yyyy HH:mm",
                "MM/dd/yyyy HH:mm",
                "MM/dd/yyyy HH:mm:ss",
                "MM/dd/yyyy HH:mm:ss Z",
                "MM/dd/yyyy HH:mm:ss Z" };
        String[] dates = {
                "12/05/2012 03:15 PM PST",
                "12/05/2012",
                "12/2012",
                "31/12/2012",
                "12/31/2012",
                "12/31/2012 15:04",
                "07/31/2012 15:04",
                "12/31/2012 15:04:52",
                "12/31/2012 00:04:52 -0800",
                "07/31/2012 00:04:52 -0700" };

        String[] styleDates = { "12/05/2012", "12/05/2012 03:15 PM" };
        String[] dateStyles = { "short", "short" };
        String[] timeStyles = { "", "short" };

        Date now = new Date();
        TSFormat tsFormat = new TSFormat();
        System.out.println(tsFormat.format(now));
        System.out.println(tsFormat.format(now, 16));
        System.out.println(tsFormat.format(now, 14));
        System.out.println(tsFormat.format(now, 12));
        System.out.println(tsFormat.format(now, 8));
        System.out.println(tsFormat.format(now, 4));
        for (int i = 0; i < patterns.length; i++) {
            System.out.println("step:" + i);
            System.out.println("Input format: " + patterns[i]);
            System.out.println("Sample input: " + dates[i]);
            TS ts = tsFormat.parse(dates[i], patterns[i]);
            System.out.println("TS0n representation: " + ts);
            String reverse = tsFormat.toString(ts, patterns[i]);
            System.out.println("TS0n reverse: " + reverse + "[match=" + reverse.equals(dates[i]) + "]");
        }
        System.out.println();
        for (int i = 0; i < styleDates.length; i++) {
            System.out.println("style step:" + i);
            System.out.println("Input format: dateStyle=" + dateStyles[i] + ", timeStyle=" + timeStyles[i]);
            System.out.println("Sample input: " + styleDates[i]);
            TS ts = tsFormat.parse(styleDates[i], dateStyles[i], timeStyles[i]);
            System.out.println("TS0n representation: " + ts);
            if (timeStyles[i].trim().length() > 0) {
                ts = tsFormat.parse(styleDates[i], dateStyles[i], timeStyles[i], TimeZone.getDefault(), Locale.getDefault());
                System.out.println("TS0n representation w/tz: " + tsFormat.parse(styleDates[i], dateStyles[i], timeStyles[i], TimeZone.getDefault(), Locale.getDefault()));
            }
            System.out.println("GUI representation: " + tsFormat.toString(ts, dateStyles[i], timeStyles[i], Locale.getDefault()));
        }
    }

    public TSFormat() {
    }

    private Map<String, String> buildSimpleFormatHL7Mappings() {
        Map<String, String> lMap = new HashMap<String, String>();
        lMap.put("yyyy|yy", "yyyy");
        lMap.put("MMMMM|MMM|MM|M", "MM");
        lMap.put("dd|d", "dd");
        lMap.put("hh|h|HH|H", "HH");
        lMap.put("mm", "mm");
        lMap.put("ss", "ss");
        lMap.put("z|Z|ZZ", "ZZ");
        return lMap;
    }

    private List<String> buildSimpleFormatHL7RegEx() {
        List<String> _HL7RegEx = new ArrayList<String>();
        _HL7RegEx.add("yyyy|yy");
        _HL7RegEx.add("MMMMM|MMM|MM|M");
        _HL7RegEx.add("dd|d");
        _HL7RegEx.add("hh|h|HH|H");
        _HL7RegEx.add("mm");
        _HL7RegEx.add("ss");
        _HL7RegEx.add("a");
        _HL7RegEx.add("z|Z|ZZ");
        return _HL7RegEx;
    }

    private Map<String, String> buildSimpleFormatStdMappings() {
        Map<String, String> lMap = new HashMap<String, String>();
        lMap.put("EEEE, ", "");
        lMap.put("MMMM|MMM|M", "MM");
        lMap.put("/", "/");
        lMap.put("dd|d", "dd");
        lMap.put("/", "/");
        lMap.put("yyyy|yy", "yyyy");
        lMap.put("hh|h|HH|H", "hh");
        lMap.put("mm", "mm");
        lMap.put("ss", "ss");
        lMap.put("a", "a");
        return lMap;
    }

    private List<String> buildSimpleFormatStdRegEx() {
        List<String> stdRegEx = new ArrayList<String>();
        stdRegEx.add("EEEE, ");
        stdRegEx.add("MMMM|MMM|M");
        stdRegEx.add("dd|d");
        stdRegEx.add("yyyy|yy");
        stdRegEx.add("hh|h|HH|H");
        stdRegEx.add("mm");
        stdRegEx.add("ss");
        stdRegEx.add("a");
        return stdRegEx;
    }

    public TS format(Date date) {
        return format(date, null, HL7TSFormatL16.length(), Locale.getDefault());
    }

    public TS format(Date date, int tsLength) {
        return format(date, null, tsLength, Locale.getDefault());
    }

    public TS format(Date date, int tsLength, Locale locale) {
        return format(date, null, tsLength, locale);
    }

    public TS format(Date date, int tsLength, TimeZone zone) {
        return format(date, zone, tsLength, Locale.getDefault());
    }

    public TS format(Date date, Locale locale) {
        return format(date, null, HL7TSFormatL16.length(), locale);
    }

    public TS format(Date date, TimeZone zone) {
        return format(date, zone, HL7TSFormatL16.length(), Locale.getDefault());
    }

    public TS format(Date date, TimeZone zone, int tsLength, Locale locale) {
        if (tsLength > HL7TSFormatL16.length()) {
            throw new RuntimeException("tsLength is greater than 16: " + tsLength);
        }
        SimpleDateFormat sdf = new SimpleDateFormat(HL7TSFormatL16.substring(0, tsLength), locale);
        sdf.setLenient(false);
        if (zone != null) {
            sdf.setTimeZone(zone);
        }
        String value = sdf.format(date);
        TS ts = trimFactory.createNewTS(value);
        ts.setValue(value);
        return ts;
    }

    public TS format(Date date, TimeZone zone, Locale locale) {
        return format(date, zone, HL7TSFormatL16.length(), locale);
    }

    private String getHL7Pattern(String stdPattern) {
        Map<String, String> _HL7FormatMaps = buildSimpleFormatHL7Mappings();
        StringBuffer _HL7Format = new StringBuffer();
        for (String _HL7RegEx : buildSimpleFormatHL7RegEx()) {
            Pattern lPattern2 = Pattern.compile(_HL7RegEx);
            Matcher lMatcher2 = lPattern2.matcher(stdPattern);
            if (lMatcher2.find()) {
                String lval = _HL7FormatMaps.get(_HL7RegEx);
                if (lval != null && lval.length() > 0) {
                    _HL7Format.append(lval);
                }
            }
        }
        return _HL7Format.toString();
    }

    private String getStdPattern(String stdPattern) {
        Map<String, String> stdFormatMaps = buildSimpleFormatStdMappings();
        for (String stdRegEx : buildSimpleFormatStdRegEx()) {
            stdPattern = stdPattern.replaceAll(stdRegEx, stdFormatMaps.get(stdRegEx));
        }
        return stdPattern;
    }

    private int getStyle(String style) {
        if (styleMap == null) {
            styleMap = new HashMap<String, Integer>();
            styleMap.put("default", DateFormat.DEFAULT);
            styleMap.put("short", DateFormat.SHORT);
            styleMap.put("medium", DateFormat.MEDIUM);
            styleMap.put("long", DateFormat.LONG);
            styleMap.put("full", DateFormat.FULL);
        }
        Integer result = styleMap.get(style);
        if (result == null) {
            return -1;
        } else {
            return result;
        }

    }

    public TS parse(String source) {
        if (source == null || source.trim().length() == 0) {
            throw new RuntimeException("TS source value is null");
        }
        TS ts = trimFactory.createNewTS(source);
        int sourceLength = source.length();
        String pattern = null;
        if (sourceLength <= 16) {
            pattern = HL7TSFormatL16.substring(0, sourceLength);
        } else {
            pattern = HL7TSFormatL16;
        }
        SimpleDateFormat hl7df = new SimpleDateFormat(pattern);
        try {
            hl7df.parse(ts.getValue());
        } catch (ParseException ex) {
            throw new RuntimeException("Could not parse to TS: " + source, ex);
        }
        return ts;
    }

    public TS parse(String source, String pattern) {
        return parse(source, pattern, null, Locale.getDefault());
    }

    public TS parse(String source, String pattern, Locale locale) {
        return parse(source, pattern, null, locale);
    }

    public TS parse(String source, String dateStyle, String timeStyle) {
        return parse(source, dateStyle, timeStyle, null, Locale.getDefault());
    }

    public TS parse(String source, String dateStyle, String timeStyle, TimeZone zone, Locale locale) {
        int dStyle = getStyle(dateStyle);
        int tStyle = getStyle(timeStyle);
        SimpleDateFormat sdf = null;
        int tsLength = 0;
        TimeZone effectiveTimeZone = null;
        if (tStyle == -1 || zone == null) {
            // do not use timezone
        } else {
            effectiveTimeZone = zone;
        }
        if (tStyle == -1) {
            sdf = (SimpleDateFormat) SimpleDateFormat.getDateInstance(dStyle, locale);
            tsLength = getHL7Pattern(sdf.toPattern()).length();
        } else {
            sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(dStyle, tStyle, locale);
            if (effectiveTimeZone == null) {
                tsLength = getHL7Pattern(sdf.toPattern()).length();
            } else {
                sdf.setTimeZone(effectiveTimeZone);
                tsLength = 16;
            }
        }
        sdf.setLenient(true);
        Date date = null;
        try {
            date = sdf.parse(source);
        } catch (ParseException ex) {
            throw new RuntimeException("Could not parse date: " + source + " into dateStyle: " + dateStyle + ", timeStyle: " + timeStyle + ", timezone: " + zone.getDisplayName() + ", locale: " + locale, ex);
        }
        return format(date, tsLength, effectiveTimeZone);
    }

    public TS parse(String source, String pattern, TimeZone zone) {
        return parse(source, pattern, zone, Locale.getDefault());
    }

    public TS parse(String source, String pattern, TimeZone zone, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
        sdf.setLenient(false);
        if (zone != null) {
            sdf.setTimeZone(zone);
        }
        Date date = null;
        try {
            date = sdf.parse(source);
        } catch (ParseException ex) {
            throw new RuntimeException("Could not parse date: " + source + " into pattern: " + pattern + ", timezone: " + zone.getDisplayName() + ", locale: " + locale, ex);
        }
        String hl7Pattern = getHL7Pattern(pattern);
        if (hl7Pattern.length() > 8) {
            return format(date, zone);
        } else {
            return format(date, getHL7Pattern(pattern).length(), zone);
        }
    }

    public String toString(TS ts) {
        return ts.getValue();
    }

    public String toString(TS ts, String pattern) {
        return toString(ts, pattern, Locale.getDefault());
    }

    public String toString(TS ts, String pattern, Locale locale) {
        return toString(ts, pattern, locale, null);
    }

    public String toString(TS ts, String pattern, Locale locale, TimeZone zone) {
        int patternLength = 0;
        if (ts.getValue() == null || ts.getValue().trim().length() == 0) {
            return null;
        } else if (ts.getValue().length() > 14) {
            patternLength = 16;
        } else {
            patternLength = ts.getValue().length();
        }
        SimpleDateFormat hl7df = new SimpleDateFormat(HL7TSFormatL16.substring(0, patternLength), locale);
        hl7df.setLenient(false);
        Date date = null;
        try {
            date = hl7df.parse(ts.getValue());
        } catch (ParseException ex) {
            throw new RuntimeException("Could not parse TS: " + ts, ex);
        }
        SimpleDateFormat sdf = null;
        if (pattern == null) {
            sdf = new SimpleDateFormat();
        } else {
            sdf = new SimpleDateFormat(pattern);
        }
        sdf.setLenient(false);
        if (zone != null && patternLength > 8) {
            sdf.setTimeZone(zone);
        }
        return sdf.format(date);
    }

    public String toString(TS ts, String dateStyle, String timeStyle, Locale locale) {
        return toString(ts, dateStyle, timeStyle, locale, null);
    }

    public String toString(TS ts, String dateStyle, String timeStyle, Locale locale, TimeZone zone) {
        int patternLength = 0;
        if (ts.getValue() == null || ts.getValue().trim().length() == 0) {
            return null;
        } else if (ts.getValue().length() > 14) {
            patternLength = 16;
        } else {
            patternLength = ts.getValue().length();
        }
        SimpleDateFormat hl7df = new SimpleDateFormat(HL7TSFormatL16.substring(0, patternLength), locale);
        hl7df.setLenient(false);
        Date date = null;
        try {
            date = hl7df.parse(ts.getValue());
        } catch (ParseException ex) {
            throw new RuntimeException("Could not parse TS: " + ts, ex);
        }
        int dStyle = getStyle(dateStyle);
        int tStyle = getStyle(timeStyle);
        SimpleDateFormat sdf = null;
        if (tStyle == -1) {
            sdf = (SimpleDateFormat) SimpleDateFormat.getDateInstance(dStyle, locale);
        } else {
            sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(dStyle, tStyle, locale);
        }
        sdf.applyPattern(getStdPattern(sdf.toPattern()));
        sdf.setLenient(true);
        if (tStyle == -1 || zone == null || patternLength <= 8) {
            //no timezone
        } else {
            sdf.setTimeZone(zone);
        }
        return sdf.format(date);
    }
}
