package org.tolven.deploy.observations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.tolven.logging.TolvenLogger;
import org.tolven.restful.client.LoadRESTfulClient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class LoadObservations extends LoadRESTfulClient {
    
    public LoadObservations(String userId, char[] password, String appRestfulURL, String authRestfulURL) {
        init(userId, password, appRestfulURL, authRestfulURL);
    }
    
    public void generateTrim(JsonObject json, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("trim");
        writer.writeNamespace(null, "urn:tolven-org:trim:4.0");

        writer.writeStartElement("extends");
        writer.writeCharacters("observation/non-menu-item");
        writer.writeEndElement(); // extends

        writer.writeStartElement("name");
        writer.writeCharacters("obs/evn/" + stripQuotes(json.get("name").toString()));
        writer.writeEndElement(); // name
        
        writeAct(json, writer);

        writer.writeEndElement();// Trim
    }

    public static void writeAct(JsonObject json, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("act");
        writer.writeAttribute("classCode", "OBS");
        writer.writeAttribute("modeCode", "EVN");
        writeCode(json, writer); 
        JsonObject valueSet = (JsonObject)json.get("valueset");
        String valueSetName = stripQuotes(json.get("name").toString())+"/"+stripQuotes(valueSet.get("name").toString());
        writeObservation(valueSetName,writer);
        writer.writeEndElement();
        //write the valuesets in to the trim
        writeValueset(valueSetName, valueSet.getAsJsonArray("values"), writer);
    }

    private static void writeCode(JsonObject json, XMLStreamWriter writer) throws XMLStreamException {
    	JsonObject code = (JsonObject)json.get("code");
        writer.writeStartElement("code");
        writeCD(code,writer);
        writer.writeEndElement();
    }
    private static void writeObservation(String valueSetName, XMLStreamWriter writer) throws XMLStreamException {
    	writer.writeStartElement("observation");
    	writer.writeStartElement("value");
    	writer.writeStartElement("CE");
    	writer.writeEndElement();        
    	writer.writeStartElement("valueSet");
    	writer.writeCharacters(valueSetName);  
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }
    private static void writeValueset(String valueSetName, JsonArray values, XMLStreamWriter writer) throws XMLStreamException {
    	writer.writeStartElement("valueSet");
    	writer.writeAttribute("name", valueSetName);
    	for(int i=0;i<values.size();i++){
    		writeCE((JsonObject)values.get(i),writer);
    	}
        writer.writeEndElement();
    }
    private static void writeCE(JsonObject code, XMLStreamWriter writer) throws XMLStreamException{
		writer.writeStartElement("CE");
        writer.writeStartElement("displayName");
        writer.writeCharacters(stripQuotes(code.get("displayName").toString()));
        writer.writeEndElement();
        writer.writeStartElement("code");
        writer.writeCharacters(stripQuotes(code.get("code").toString()));
        writer.writeEndElement();
        writer.writeStartElement("codeSystem");
        writer.writeCharacters(stripQuotes(code.get("codeSystem").toString()));
        writer.writeEndElement();
        writer.writeStartElement("codeSystemName");
        writer.writeCharacters(stripQuotes(code.get("codeSystemName").toString()));
        writer.writeEndElement();
        writer.writeEndElement();
    }
    private static void writeCD(JsonObject code, XMLStreamWriter writer) throws XMLStreamException{
		writer.writeStartElement("CD");
        writer.writeStartElement("displayName");
        writer.writeCharacters(stripQuotes(code.get("displayName").toString()));
        writer.writeEndElement();
        writer.writeStartElement("code");
        writer.writeCharacters(stripQuotes(code.get("code").toString()));
        writer.writeEndElement();
        writer.writeStartElement("codeSystem");
        writer.writeCharacters(stripQuotes(code.get("codeSystem").toString()));
        writer.writeEndElement();
        writer.writeStartElement("codeSystemName");
        writer.writeCharacters(stripQuotes(code.get("codeSystemName").toString()));
        writer.writeEndElement();
        writer.writeEndElement();
    }
    /**
     * Load observations. 
     * @param fileName
     * @throws Exception 
     */
    public void load(String fileName) throws Exception {
        TolvenLogger.info("Uploading observations list from: " + fileName, LoadObservations.class);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"));
        String record;
        int rowCount = 0;
        while (reader.ready()) {
            rowCount++;
            record = reader.readLine();
            // Might break out early is requested
            if (rowCount > getIterationLimit()) {
                TolvenLogger.info("Upload stopped early due to " + UPLOAD_LIMIT + " property being set", LoadObservations.class);
                break;
            }
            if(record == null || record.trim().length() == 0)
            	continue;
            // Skip the heading line
            //if (rowCount == 1)
             //   continue;
            Gson parser = new Gson();
    		JsonObject result = parser.fromJson(record, JsonObject.class);
            StringWriter bos = new StringWriter();
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(bos);
            writer.writeStartDocument("UTF-8", "1.0");
            generateTrim(result, writer);
            writer.writeEndDocument();
            //			writer.flush();
            writer.close();
            bos.close();
            createTrimHeader(bos.toString());
            TolvenLogger.info(bos.toString(), LoadObservations.class);
        }
        TolvenLogger.info("Count of observations uploaded: " + (rowCount), LoadObservations.class);
        TolvenLogger.info("Activating headers... ", LoadObservations.class);
        activate();
    }
    private static String stripQuotes(String  element){
    	return element.toString().replaceAll("\"", "");    	
    }
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Arguments: configDirectory");
            return;
        }
    }
}
