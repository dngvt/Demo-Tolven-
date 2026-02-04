package org.tolven.surescripts;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.tolven.xml.ParseXML;
import org.w3c.dom.Document;

public class ParseSS extends ParseXML implements Serializable{

	protected static String getParsePackageName() {
		return "org.tolven.surescripts";
	}
	public ParseSS() {
		super(jc);
	}
	private static JAXBContext jc;
	/**
	 * Construct a Tolven application XML parser
	 * @throws Exception
	 */
	static{
		try{
			jc= JAXBContext.newInstance(getParsePackageName());
		}catch (Exception e) {
			throw new RuntimeException("Exception creating JAXBContext",e);
		}
	}
	public NewRx createRx(){
		NewRx rx = new NewRx();
		rx.setRxReferenceNumber("qbtest1234234");
		rx.setPrescriberOrderNumber("qbpres787878979789");
		return rx;
	}
	
	public HeaderType createHeader(){
		HeaderType header = new HeaderType();
		header.setFrom("mailto:1234567.ncpdp@surescripts.com");
		header.setTo("mailto:890.ncpdp@surescripts.com");
		header.setMessageID("qbmsg007");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.F'Z'");
		header.setSentTime(df.format(new Date()));
		AppVersionType app = new AppVersionType();
		app.setApplicationVersion("TolvenRC1");
		app.setAppName("tolven");
		app.setVendorName("ep");
		header.setAppVersion(app);
		return header;
	}
	
	public BodyType createBody(){
		BodyType msgBody = new BodyType();
		msgBody.setNewRx(createRx());
		return msgBody;
	}

	public MessageType createMessage(){
		MessageType msg = new MessageType();
		msg.setHeader(createHeader());
		msg.setBody(createBody());
		return msg;
	}
	
	public static void main(String args[]) throws JAXBException, ParserConfigurationException{
		  
		  ParseSS obj = new ParseSS();
	      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	      dbf.setNamespaceAware(true);
	      DocumentBuilder db = dbf.newDocumentBuilder();
	      Document doc = db.newDocument();
	      MessageType msg = obj.createMessage();
	      obj.getMarshaller().marshal(msg, System.out);
	      obj.getMarshaller().marshal(msg, doc);
		
		MessageType mx = (MessageType) obj.getUnmarshaller().unmarshal(doc);
	}
}
