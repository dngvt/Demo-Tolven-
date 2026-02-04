package org.tolven.api.rs.resource;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.MSColumn;
import org.tolven.app.entity.MenuData;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;
import org.tolven.trim.CD;
import org.tolven.trim.CE;
import org.tolven.trim.CESlot;
import org.tolven.trim.DataType;
import org.tolven.trim.ED;
import org.tolven.trim.GTSSlot;
import org.tolven.trim.INT;
import org.tolven.trim.ObservationValueSlot;
import org.tolven.trim.PQ;
import org.tolven.trim.SETCESlot;
import org.tolven.trim.TS;
import org.tolven.trim.ex.EDEx;
import org.tolven.trim.ex.HL7DateFormatUtility;
import org.tolven.trim.ex.SETCESlotEx;
import org.tolven.trim.ex.TRIMException;
import org.tolven.trim.ex.TrimEx;
import org.tolven.trim.ex.TrimFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

public class JSONTrimProcessor extends TolvenResources {
	private static Logger logger = Logger.getLogger(JSONTrimProcessor.class);
	private TrimFactory factory = new TrimFactory();
	public TrimEx createPlaceholderAct(String payload) throws Exception{
		JsonObject data = null;
        try{
        	data = new Gson().fromJson(payload, JsonObject.class);
        }catch (JsonSyntaxException e) {
        	throw new RuntimeException("Invalid JSON string "+payload);
		}        
		return createPlaceholderAct(data);
	}
	
	public TrimEx createPlaceholderAct(JsonObject data) throws Exception{
		AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
		if(data.get("id") == null && data.get("trimName")  == null){
        	throw new RuntimeException("id or trimName not found");
        }
        if(data.get("placeholder") == null){
        	throw new RuntimeException("placeholder not found");
        }
        if (data.get("patientPath") == null) {
        	throw new RuntimeException("patientPath not found");
        }
        
        JsonElement trimMenuDataId = data.get("id");
        JsonElement trimName = data.get("trimName");

        String placeholder= data.get("placeholder").getAsString();
        String patientPath = data.get("patientPath").getAsString();  
        Object trimNameField = null;
        //if trimName is not sent check for the menudata id to find the trim name
        if(trimName == null ){
            MenuData md = getMenuBean().findMenuDataItem(Long.parseLong(trimMenuDataId.getAsString()));
	        try{
	        	trimNameField = md.getField("trimName");
	        }catch (Exception e) {
	        	throw new RuntimeException("trimName not found for id:"+trimMenuDataId);
			}
        }
        
        TrimEx trim = null;
        //for medication, allergy and problem the menudata item has to be used to finding the trim name
        if(placeholder.equals("echr:patient:problem") || placeholder.equals("echr:patient:medication") 
        		|| placeholder.equals("echr:patient:allergy")|| placeholder.equals("echr:patient:immunization")){
			try {
				trim = getTrimCreatorBean().createTrim(accountUser,trimNameField.toString(), patientPath, new Date());
			} catch (JAXBException e1) {  
				throw new RuntimeException("error creating trim:"+trimName.getAsString()+" for patient:"+patientPath);
			}catch (TRIMException e2) {  
				throw new RuntimeException("error creating trim:"+trimName.getAsString()+" for patient:"+patientPath);
			}
        }else{
        	try {
        		trim = getTrimCreatorBean().createTrim(accountUser,trimName.getAsString(), patientPath, new Date());
			} catch (JAXBException e1) {  
				throw new RuntimeException("error creating trim:"+trimName.getAsString()+" for patient:"+patientPath);
			}catch (TRIMException e2) {  
				throw new RuntimeException("error creating trim:"+trimName.getAsString()+" for patient:"+patientPath);
			}
        }
		if(trim == null)
			throw new RuntimeException("trim not found for name:"+trimName.getAsString());
        
        AccountMenuStructure ams = null;
        try{
        	ams = getMenuBean().findAccountMenuStructure(accountUser.getAccount().getId(), placeholder);
        }catch (Exception e) {
        	throw new RuntimeException("Error looking up placeholder for path:"+placeholder);
		}
        if(ams == null)
        	throw new RuntimeException("No placeholder found with path:"+placeholder);
        
        TrimExpressionEvaluator ee = new TrimExpressionEvaluator();
        Date now = new Date();
        ee.addVariable("trim", trim);
        ee.addVariable("act", trim.getAct());
        
        //add dateformat to evaluator
        if(data.has("dateFormat")){
        	ee.addVariable("dateFormat", data.get("dateFormat").getAsString());
        }
     	MenuPath context = new MenuPath(patientPath);
		getCreatorBean().computeScan( trim, accountUser, context, now, null);
	
		populateAct(ams, ee,data);
		
		// Bind to placeholders
	    //getCreatorBean().placeholderBindScan( accountUser, trim, mdTrim, contextPath, now, BindPhase.CREATE, docXML);
			
		return trim;
	}
	/**
	 * Method to populate the Act with information sent via JSON
	 * 
	 */
	public void populateAct(AccountMenuStructure ams, TrimExpressionEvaluator ee,JsonObject data){
		//add dateformat to evaluator
        if(data.has("dateFormat")){
        	ee.addVariable("dateFormat", data.get("dateFormat").getAsString());
        }
		//fill the trimdata i.e. data not stored in the placeholders
		Object trimData = data.get("trimdata");
		if(trimData != null){
			JsonArray items = (JsonArray) trimData;
			for(int i=0;i<items.size();i++){
				String path = items.get(i).getAsJsonObject().get("path").getAsString();				
				Object element = ee.evaluate(path);
				if(element != null){
					setTrimElement(element,items.get(i).getAsJsonObject().get("value").getAsString(),path,ee);				
				}
			}
		}		
		//populate the data in to trim using placeholder fields
		if(ams != null){
			for(MSColumn column : ams.getColumns()){
				Object obj = data.get(column.getHeading());
				if(obj == null){
					continue;
				}else{
					if(obj instanceof JsonPrimitive){
						setMSColumnValue(column,data.get(column.getHeading()).getAsString(),ee);	
					}else if(obj instanceof JsonObject){
						JsonObject columnJson = (JsonObject) obj;
						if(columnJson.get("type") != null){ //of the column is not of type String
							String columnDataType = columnJson.get("type").getAsString();
							if(columnDataType.equalsIgnoreCase("CE")){
								setMSColumnValue(column,columnJson.get("value").getAsString(),ee);						
							}else if(columnDataType.equalsIgnoreCase("CD")){
								setMSColumnValue(column,columnJson.get("value").getAsString(),ee);						
							}
						}
					}
				}					
			}
		}
	}
	
	/**
     * Method to set Trim DataType columns on the placeholder
     */
    private void setMSColumnValue(MSColumn column,Object value,TrimExpressionEvaluator ee){
    	for(String from: column.getFroms()){
			Object obj = ee.evaluate(from);
			if(obj != null){
				setTrimElement(obj,value,from,ee);
				break;
			}
		}
    }
    /**
     * @param element - The trim element
     * @param value - - The value to be set
     * @param path - EL expression of the path to the trim element
     * @param ee - TrimExpressionEvaluator
     */
    private void setTrimElement(Object element,Object value,String path,TrimExpressionEvaluator ee){
    	if(element instanceof Long){
    		boolean done = ee.setValue(path,new Long(value.toString()));
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to Long %s : %s",path,value,done));			
		}else if(element instanceof Double){
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to Double %s",path,value));
			ee.setValue(path, new Double(value.toString()));
		}else if(element instanceof Integer){
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to Integer %s",path,value));
			ee.setValue(path,new Integer(value.toString()));
		}else if(element instanceof Float){
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to Float %s",path,value));
			ee.setValue(path,new Float(value.toString()));
		}if(element instanceof CE){
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to CE %s",path,value));
			CE columnDataValue = (CE)factory.stringToDataType(value.toString());
			ee.setValue(path,columnDataValue);				
		}else if(element instanceof CD){			
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to CD %s",path,value));
			CD columnDataValue = (CD)factory.stringToDataType(value.toString());
			ee.setValue(path,columnDataValue);						
		}else if(element instanceof PQ){			
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to PQ %s",path,value));
			PQ pq= new PQ();
			pq.setValue(new Double(value.toString()));
			pq.setOriginalText(value.toString());
			ee.setValue(path,pq);						
		}else if(element instanceof INT){			
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to INT %s",path,value));
			INT intVal = new INT();
			intVal.setValue(Long.parseLong(value.toString()));
			ee.setValue(path,intVal);						
		}else if(element instanceof ObservationValueSlot){
			ObservationValueSlot slot = (ObservationValueSlot)element;
			if(value.toString().startsWith("CE")){
				if(logger.isDebugEnabled())
					logger.debug(String.format("Setting %s to CE %s",path,value));
				CE columnDataValue = (CE)factory.stringToDataType(value.toString());
				slot.setCE(columnDataValue);
			}else if(value.toString().startsWith("CD")){
				if(logger.isDebugEnabled())
					logger.debug(String.format("Setting %s to CD %s",path,value));
				CD columnDataValue = (CD)factory.stringToDataType(value.toString());
				slot.setCD(columnDataValue);				
			}else if(logger.isDebugEnabled())
				logger.debug("Unknown datatype for Observation path:"+path+"  value:"+value);
		}else if(element instanceof SETCESlot){
			SETCESlotEx slot = (SETCESlotEx) element;
			String setCEs[] = value.toString().split(",");
			List<DataType> set = new ArrayList<DataType>();
			for(String ceString:setCEs){
				set.add((CE)factory.stringToDataType(ceString));
			}
			slot.setValues(set);
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to SETCESlot %s",path,value));
		}else if(element instanceof ED){
			EDEx slot = (EDEx) element;
			slot.setStringValue(value.toString());
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to ED %s",path,value));
		}else if(element instanceof CESlot){
			CESlot slot = (CESlot)element;
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to CE %s",path,value));
			CE columnDataValue = (CE)factory.stringToDataType(value.toString());
			slot.setCE(columnDataValue);									
		}else if(element instanceof GTSSlot){ //handle date parsing for trim
			GTSSlot slot = (GTSSlot) element;
			if(slot.getNew() != null && slot.getNew().getDatatype().name().equals("TS")){
				TS tsDate = factory.createTS();
				slot.setTS(tsDate);
				Object obj = ee.evaluate("#{dateFormat}");
				if(obj != null){
					try {
						Date date = HL7DateFormatUtility.parseDate(value.toString(), obj.toString());
						tsDate.setValue(HL7DateFormatUtility.formatHL7TSFormatL16Date(date));
						if(logger.isDebugEnabled())
							logger.debug(String.format("Setting %s to TS %s",path,tsDate.getValue()));
					} catch (ParseException e) {
						throw new RuntimeException("Error parsing date:"+value.toString(),e);
					}
				}
			}			
		}else if(element instanceof TS){ //handle date parsing for trim
			TS slot = (TS) element;
			Object obj = ee.evaluate("#{dateFormat}");
			if(obj != null){
				try {
					Date date = HL7DateFormatUtility.parseDate(value.toString(), obj.toString());
					slot.setValue(HL7DateFormatUtility.formatHL7TSFormatL16Date(date));
					if(logger.isDebugEnabled())
						logger.debug(String.format("Setting %s to TS %s",path,slot.getValue()));
				} catch (ParseException e) {
					throw new RuntimeException("Error parsing date:"+value.toString(),e);
				}
			}
		}else{
			if(logger.isDebugEnabled())
				logger.debug(String.format("Setting %s to String %s",path,value.toString()));
			ee.setValue(path,value.toString());
		}	
    }
}
