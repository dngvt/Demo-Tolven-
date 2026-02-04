package org.tolven.deploy.valuedobservations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.tolven.deploy.valuedobservations.LoadValuedObservations;
import org.tolven.logging.TolvenLogger;
import org.tolven.restful.client.LoadRESTfulClient;

public class LoadValuedObservations extends LoadRESTfulClient {
    
    public LoadValuedObservations(String userId, char[] password, String appRestfulURL, String authRestfulURL) {
        init(userId, password, appRestfulURL, authRestfulURL);
    }
    
    public void generateTrim(String fields[], XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("trim");
        writer.writeNamespace(null, "urn:tolven-org:trim:4.0");

        writer.writeStartElement("extends");
        writer.writeCharacters("observation/non-menu-item");
        writer.writeEndElement(); // extends

        writer.writeStartElement("name");
        writer.writeCharacters("obs/evn/" + fields[0]);
        writer.writeEndElement(); // name
        
        writer.writeStartElement("page");
        writer.writeCharacters(fields[1].toLowerCase() + "Observation.xhtml");
        writer.writeEndElement(); // page
        
        writer.writeStartElement("drilldown");
        writer.writeCharacters(fields[1].toLowerCase() +"ObservationDD.xhtml");
        writer.writeEndElement(); //  drilldown
   
        writer.writeStartElement("description");
        writer.writeCharacters(fields[2]);
        writer.writeEndElement();//description

        writeAct(fields, writer);

        writer.writeEndElement();// Trim
    }

    public static void writeAct(String fields[], XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("act");
        writer.writeAttribute("classCode", "OBS");
        writer.writeAttribute("modeCode", "EVN");
        writeCode(fields, writer);
        writer.writeStartElement("title");
        writer.writeStartElement("ST");
        writer.writeCharacters(fields[2]);
        writer.writeEndElement(); // ST
        writer.writeEndElement(); // title
        writer.writeStartElement("effectiveTime");
        writer.writeStartElement("TS");
        writer.writeStartElement("value");
        writer.writeEndElement(); // value
        writer.writeEndElement(); // TS
        writer.writeEndElement(); // effectiveTime
 
        writer.writeStartElement("observation");
        writer.writeStartElement("value");
        writer.writeStartElement("label");
        writer.writeCharacters(fields[2]);   
        writer.writeEndElement(); // label
        writer.writeStartElement(fields[1]);
         
         if(!fields[1].contentEquals("ST")) {
             writer.writeStartElement("value");
             writer.writeCharacters(fields[4]);
             writer.writeEndElement(); // value
             writer.writeStartElement("unit");
             writer.writeCharacters(fields[3]);
             writer.writeEndElement(); // value 
         }

        writer.writeEndElement(); // PQ or other valid observation type
        writer.writeEndElement(); // Value
        writer.writeEndElement(); // observation
    
        writer.writeEndElement(); //act
    }

    public static void writeCode(String fields[], XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("code");
        {
            writer.writeStartElement("CD");
            {
                writer.writeStartElement("code");
                writer.writeCharacters(fields[0]);
                writer.writeEndElement();
                writer.writeStartElement("codeSystem");
                writer.writeCharacters("Bupa-Local");
                writer.writeEndElement();
                writer.writeStartElement("codeSystemName");
                writer.writeCharacters("Bupa-Local");
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Load allergies. 
     * @param fileName
     * @throws Exception 
     */
    public void load(String fileName) throws Exception {
        TolvenLogger.info("Uploading valued Observation list from: " + fileName, LoadValuedObservations.class);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8"));
        String record;
        String fields[];
        int rowCount = 0;
        while (reader.ready()) {
            rowCount++;
            record = reader.readLine();
            // Might break out early is requested
            if (rowCount > getIterationLimit()) {
                TolvenLogger.info("Upload stopped early due to " + UPLOAD_LIMIT + " property being set", LoadValuedObservations.class);
                break;
            }
            // Skip the heading line
            if (rowCount == 1)
                continue;
            fields = record.split("\\|", 6);
            StringWriter bos = new StringWriter();
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(bos);
            writer.writeStartDocument("UTF-8", "1.0");
            generateTrim(fields, writer);
            writer.writeEndDocument();
            //			writer.flush();
            writer.close();
            bos.close();
            createTrimHeader(bos.toString());
            TolvenLogger.info(bos.toString(), LoadValuedObservations.class);
        }
        TolvenLogger.info("Count of Observations uploaded: " + (rowCount - 1), LoadValuedObservations.class);
        TolvenLogger.info("Activating headers... ", LoadValuedObservations.class);
        activate();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Arguments: configDirectory");
            return;
        }
        //LoadAllergies la = new LoadAllergies();
        //la.login("admin", "sysadmin");
        //la.load(args[0]);
    }
}
