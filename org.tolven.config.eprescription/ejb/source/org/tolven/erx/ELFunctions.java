package org.tolven.erx;

public class ELFunctions {
	
	public static String trimString(String columnValue, int size) throws Exception{
		String outputValue = null;
		outputValue = columnValue.subSequence(0, size).toString();
		return outputValue;
	}	
}
