package org.tolven.surescripts;
/*
 * 
 * 
 * */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.tolven.xml.ParseXML;


public class UnMarshallXMLFile extends ParseXML{
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
	
	/*
	 *Default Constructor
	 */
	public UnMarshallXMLFile(){
		super(jc);
	}
	
	protected static String getParsePackageName() {
		// TODO Auto-generated method stub
		return "org.tolven.surescripts";
	}
	
	public ArrayList<MessageType> fileReader() throws JAXBException, IOException{
		
		File folder =new File("/usr/surescriptsmessages/inbox/");
	    File[] listOfFiles = folder.listFiles();
	    ArrayList<MessageType> msgList = new ArrayList<MessageType>();
	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	        Unmarshaller u =getUnmarshaller();
			MessageType  msg = (MessageType)((JAXBElement) u.unmarshal(listOfFiles[i])).getValue();
			msgList.add(msg);
			listOfFiles[i].delete();
			
	      }
	    }
		
		return msgList;
		
		
	}
	
	public static void main(String args[]) throws JAXBException, IOException{
		UnMarshallXMLFile unmarsh = new UnMarshallXMLFile();
		ArrayList<MessageType> messageList = unmarsh.fileReader();
		ListIterator itr = messageList.listIterator();
		while(itr.hasNext()){
			
			MessageType msgs =(MessageType) itr.next();
			if(null != msgs.getBody().getError()){
				System.out.println("code"+msgs.getBody().getError().getCode());
				System.out.println("DescriptionCode"+msgs.getBody().getError().getDescriptionCode());
				System.out.println(	"Description"+msgs.getBody().getError().getDescription());
				
			}
			else if(null != msgs.getBody().getStatus()){
				System.out.println("Code"+msgs.getBody().getStatus().getCode());
			}
		}
	}
	
	
	

}
