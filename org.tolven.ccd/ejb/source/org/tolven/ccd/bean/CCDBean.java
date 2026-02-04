package org.tolven.ccd.bean;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.tolven.app.AppResolverLocal;
import org.tolven.ccd.CCDLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;

@Local(CCDLocal.class)
@Stateless
public class CCDBean implements CCDLocal  {

	@EJB
	private AppResolverLocal appResolver;
	private static final String CCD_XSLT = "org.tolven.xslt.ccd";
	
	public String getCCDXml(String context) throws ParserConfigurationException, TransformerException{
		AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
		String xslt = accountUser.getProperty().get(CCD_XSLT);
		Writer writer = new StringWriter();
		Source source = new StreamSource( new StringReader(xslt) );
		writer = new StringWriter();
		Result outputTarget = new StreamResult( writer);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		appResolver.setAccountUser(accountUser);
		transformerFactory.setURIResolver(appResolver);
	    Transformer transformer = transformerFactory.newTransformer(source);
		//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		//transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		DOMSource domsrc = new DOMSource(db.newDocument());
		if(context != null) {
			transformer.setParameter("context", context);
		}
		transformer.transform(domsrc, outputTarget);
		return writer.toString();
	}
}
