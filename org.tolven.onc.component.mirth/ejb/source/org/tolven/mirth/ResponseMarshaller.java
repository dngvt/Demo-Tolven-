package org.tolven.mirth;

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;

import org.tolven.xml.ParseXML;
//import org.tolven.xml.TranscendNameSpacePrefixMapper;

/**
	 * Class for marshalling Trim objects
	 * @author syed
	 * added on 10/28/2009
	 */
	public class ResponseMarshaller extends ParseXML {
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
		
		public ResponseMarshaller() {
		super(jc);
		}

		protected static String getParsePackageName() {
			return "org.tolven.mirth";
		}
		
		/**
		 * Method to convert Trim to a payload
		 * @author syed
		 * added on 10/28/2009
		 */
		public OutputStream marshalToStream(Response response,OutputStream output) throws JAXBException {
	        Marshaller m = getMarshaller();
	        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	        //m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new TranscendNameSpacePrefixMapper());
	        m.marshal(response, output );
	        
	        return output;
		}
		
		public  JAXBElement<Response> UnmarshalFromStream(Source is) throws JAXBException{
			return  (JAXBElement<Response>)getUnmarshaller().unmarshal(is);
		}
	}