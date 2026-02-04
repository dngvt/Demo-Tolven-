package test.org.tolven.surescripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;

import org.tolven.menuStructure.Application;
import org.tolven.menuStructure.parse.ParseMenuStructure;
import org.tolven.surescripts.BodyType;
import org.tolven.surescripts.MessageType;
import org.tolven.surescripts.SureScriptsCommunicator;


public class MashallingTest {
	public static void main(String[] args) {
		try{
		ParseMenuStructure  parseMenuStructure = new ParseMenuStructure();
		SureScriptsCommunicator communicator = new SureScriptsCommunicator();
		Application application = parseMenuStructure.parseStream( new FileInputStream("C:/Tolven_skandula/org.tolven.component.tolvenejb/src/test/org/tolven/xml/global.application.xml"));
		FileOutputStream fos = new FileOutputStream(new File("C:/Tolven_skandula/org.tolven.component.tolvenejb/src/test/org/tolven/xml/global.application_copy.xml"));
		parseMenuStructure.marshalToStream(application,fos);
		MessageType message = new MessageType();
		BodyType body = new BodyType();
		message.setBody(body);
		StringWriter sw = new StringWriter();
		communicator.marshallToStringWritter(message, sw);
		System.out.println(application);
		System.out.println(sw.toString());
		
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
