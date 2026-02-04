package org.tolven.app.bean;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.tolven.app.GeneratePQRIXMLInterface;
import org.tolven.app.DisplayMeasure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Stateless()
@Local(GeneratePQRIXMLInterface.class)
public class GeneratePQRIXMLBean implements GeneratePQRIXMLInterface {
	
	private final String X = "X";
	private final String PQRI_REGISTRY_ID = "67777788";
	private final String PQRI_NPI = "4565478799";
	private final String PQRI_TIN = "6787898999";
	
	
	public String generatePQRI_XML(List<DisplayMeasure> measures) {
		
		Writer writer = new StringWriter();
		
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
	
			Document newDoc = domBuilder.newDocument();
			Element rootElement = newDoc.createElement("submission");
			rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			rootElement.setAttribute("type", "PQRI-REGISTRY");
			rootElement.setAttribute("option", "TEST");
			rootElement.setAttribute("version", "4.0");
			rootElement.setAttribute("xsi:noNamespaceSchemaLocation","Registry_Payment.xsd");
	
			newDoc.appendChild(rootElement);
					
			// Adding file-audit-data XML elements
			Element rowElement = createFileAuditData(newDoc);
			rootElement.appendChild(rowElement);
	
			// Add registry XML elements
			rowElement = createRegistryData(newDoc);
			rootElement.appendChild(rowElement);
	
			// Add MeasureGroupData XML elements
			rowElement = createMeasureGroupData(newDoc, measures);
			rootElement.appendChild(rowElement);
	
			// Transform the XML Document elements into XML
			TransformerFactory tranFactory = TransformerFactory.newInstance();
			Transformer aTransformer = tranFactory.newTransformer();
			aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
	
			Source src = new DOMSource(newDoc);
			
			writer = new StringWriter();
			Result outputTarget = new StreamResult(writer);
			aTransformer.transform(src, outputTarget);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return writer.toString();
	}



	/**
	 * Build the XML elements for file-audit-date
	 * 
	 * @param newDoc
	 * @return
	 * 
	 */
	private Element createFileAuditData(Document newDoc) {
		// Adding file-audit-data - START
		Element rowElement = newDoc.createElement("file-audit-data");

		Element curElement = newDoc.createElement("create-date");
		curElement.appendChild(newDoc.createTextNode(getFormattedDate(new Date(), "MM-dd-yyyy")));
		rowElement.appendChild(curElement);

		curElement = newDoc.createElement("create-time");
		curElement.appendChild(newDoc.createTextNode(getFormattedDate( new Date(), "HH:mm")));
		rowElement.appendChild(curElement);

		curElement = newDoc.createElement("create-by");
		curElement.appendChild(newDoc.createTextNode("Tolven Hospital"));
		rowElement.appendChild(curElement);

		curElement = newDoc.createElement("version");
		curElement.appendChild(newDoc.createTextNode("1"));
		rowElement.appendChild(curElement);

		curElement = newDoc.createElement("file-number");
		curElement.appendChild(newDoc.createTextNode("1"));
		rowElement.appendChild(curElement);

		curElement = newDoc.createElement("number-of-files");
		curElement.appendChild(newDoc.createTextNode("1"));
		rowElement.appendChild(curElement);

		return rowElement;

	}

	/**
	 * Date Utility function
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	private String getFormattedDate(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	/**
	 * Get past Date
	 * 
	 * @param currentDate
	 * @param days
	 * @return
	 */
	private Date getPastDate(Date currentDate, long days) {
		Date pastDate = new Date(currentDate.getTime() - days * 24 * 60 * 60 * 1000);
		return pastDate;
	}
	/**
	 * Build the XML elements for file-audit-date
	 * 
	 * @param newDoc
	 * @return
	 * 
	 */
	private Element createRegistryData(Document newDoc) {

		// Get the registry ID from the pqri_reporting.properties
		Element rowElement = newDoc.createElement("registry");

		Element curElement = newDoc.createElement("registry-name");
		curElement.appendChild(newDoc.createTextNode("Model Registry"));
		rowElement.appendChild(curElement);

		curElement = newDoc.createElement("registry-id");
		curElement.appendChild(newDoc.createTextNode(PQRI_REGISTRY_ID));
		rowElement.appendChild(curElement);

		curElement = newDoc.createElement("submission-method");
		curElement.appendChild(newDoc.createTextNode("A"));
		rowElement.appendChild(curElement);

		return rowElement;
	}


	
	private Element createMeasureGroupData(Document newDoc, List<DisplayMeasure> measures) {
		// Adding file-audit-data - START
		Element measureGroupElement = newDoc.createElement("measure-group");
		measureGroupElement.setAttribute("ID", X);
		// Create Provider element
		Element providerElement = newDoc.createElement("provider");

		// add other elements to the provider
		Element providerElementChild = newDoc.createElement("npi");
		providerElementChild.appendChild(newDoc.createTextNode(PQRI_NPI));
		providerElement.appendChild(providerElementChild);

		providerElementChild = newDoc.createElement("tin");
		providerElementChild.appendChild(newDoc.createTextNode(PQRI_TIN));
		providerElement.appendChild(providerElementChild);

		providerElementChild = newDoc.createElement("waiver-signed");
		providerElementChild.appendChild(newDoc.createTextNode("Y"));
		providerElement.appendChild(providerElementChild);

		providerElementChild = newDoc.createElement("encounter-from-date");
		Date pastDate = getPastDate(new Date(), 90);
		providerElementChild.appendChild(newDoc.createTextNode(getFormattedDate(pastDate, "MM-dd-yyyy")));
		providerElement.appendChild(providerElementChild);

		providerElementChild = newDoc.createElement("encounter-to-date");
		providerElementChild.appendChild(newDoc.createTextNode(getFormattedDate(new Date(), "MM-dd-yyyy")));
		providerElement.appendChild(providerElementChild);

		// Create Measure group stat
		Element measureGroupstat = newDoc.createElement("measure-group-stat");

		Element measureGroupstatChild = newDoc.createElement("ffs-patient-count");
		measureGroupstatChild.appendChild(newDoc.createTextNode("0"));
		measureGroupstat.appendChild(measureGroupstatChild);

		measureGroupstatChild = newDoc.createElement("group-reporting-rate-numerator");
		measureGroupstatChild.appendChild(newDoc.createTextNode("0"));
		measureGroupstat.appendChild(measureGroupstatChild);

		// Sum of X's, D's and N's for each measure type
		measureGroupstatChild = newDoc.createElement("group-eligible-instances");
		measureGroupstatChild.appendChild(newDoc.createTextNode(Integer.valueOf(measures.size()).toString()));
		measureGroupstat.appendChild(measureGroupstatChild);

		// group-reporting-rate
		measureGroupstatChild = newDoc.createElement("group-reporting-rate");
		measureGroupstatChild.appendChild(newDoc.createTextNode("100"));
		measureGroupstat.appendChild(measureGroupstatChild);

		// Append all child elements of measure stats
		providerElement.appendChild(measureGroupstat);

		
		// Build pqri-measure elements dynamically here - Start
		generatePQRIMeasureElements(newDoc, providerElement, measures);
		
		// Add provider element
		measureGroupElement.appendChild(providerElement);

		return measureGroupElement;
	}
	

	
    
    private void generatePQRIMeasureElements(Document newDoc, Element providerElement, List<DisplayMeasure> measures) {

    	for(DisplayMeasure measure : measures) {
    	
    		if(measure.getMeasureName().contains("VTE7") || measure.getMeasureName().contains("VTE8")) {
    			//Do not report on these measures
    			continue;
    		}
    		
			Element PQRIMeasureElement = newDoc.createElement("pqri-measure");
 	
 		    Element PQRIMeasureElementChild = newDoc.createElement("pqri-measure-number");
 		    String measureId = measure.getMeasureNumber();
 		    if(measureId == null) {
 		    	measureId = measure.getMeasureName();
 		    } else if(measure.getMeasureName().contains("VTE") || measure.getMeasureName().contains("STK") || measure.getMeasureName().contains("ED")) {
 		    	measureId = measureId.concat("(" + measure.getMeasureName() + ")");
 		    }
 		    PQRIMeasureElementChild.appendChild(newDoc.createTextNode(measureId));
 		   //PQRIMeasureElementChild.appendChild(newDoc.createTextNode("PQRI ?"));
 		    PQRIMeasureElement.appendChild(PQRIMeasureElementChild);

  		    //eligible-instances
 		    PQRIMeasureElementChild = newDoc.createElement("eligible-instances");
 		    PQRIMeasureElementChild.appendChild(newDoc.createTextNode(measure.getDenominator()));
 		    PQRIMeasureElement.appendChild(PQRIMeasureElementChild);
			    	
 		   
 		   // meets-performance-instances = eligible-instances – excluded instances
			PQRIMeasureElementChild = newDoc.createElement("meets-performance-instances");
			PQRIMeasureElementChild.appendChild(newDoc.createTextNode(measure.getNumerator()));
			PQRIMeasureElement.appendChild(PQRIMeasureElementChild);
			    
 		    //performance-exclusion-instances
  		    PQRIMeasureElementChild = newDoc.createElement("performance-exclusion-instances");
 		    PQRIMeasureElementChild.appendChild(newDoc.createTextNode(String.valueOf(measure.getExclusions())));
 		    PQRIMeasureElement.appendChild(PQRIMeasureElementChild);

 		   //performance-not-met-instances
 		    PQRIMeasureElementChild = newDoc.createElement("performance-not-met-instances");
 		    PQRIMeasureElementChild.appendChild(newDoc.createTextNode(String.valueOf(measure.getPerformanceNotMet())));
 		    PQRIMeasureElement.appendChild(PQRIMeasureElementChild);
 		   
 		   //reporting-rate
 		    PQRIMeasureElementChild = newDoc.createElement("reporting-rate");
 		    PQRIMeasureElementChild.appendChild(newDoc.createTextNode(String.valueOf(100)));
 		    PQRIMeasureElement.appendChild(PQRIMeasureElementChild);

 		   
 		   //performance-rate
            PQRIMeasureElementChild = newDoc.createElement("performance-rate");
		    PQRIMeasureElementChild.appendChild(newDoc.createTextNode(measure.getPercentage().replace("%", "")));
		    PQRIMeasureElement.appendChild(PQRIMeasureElementChild);
 		   
 		   //Append all child elements of measure stats
			providerElement.appendChild(PQRIMeasureElement);
  	    }
    }
    
       
  
    

}
