package org.tolven.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public abstract class ParseXML {
	private JAXBContext jc;
	public ParseXML(JAXBContext jc) {
		this.jc = jc;
	}
	
	protected Unmarshaller getUnmarshaller() throws JAXBException {
        return this.jc.createUnmarshaller();
	}

	protected Marshaller getMarshaller() throws JAXBException {
        Marshaller m = this.jc.createMarshaller();
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.setProperty(  "com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl());
        return m;

	}
}
