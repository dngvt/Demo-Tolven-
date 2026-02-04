package org.tolven.xml;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.tolven.core.TolvenRequest;

public class XMLResolver implements URIResolver {

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		//logger.info("href: " + href + " base: " + base);
		// See if URL starts with the account type + ":"
		
		try {
			if (href.startsWith("include:")) {
				return new StreamSource( new URL(href.substring("include:".length())).openStream());
			} else if (href.startsWith("property:")) {
				String propertyName = href.substring("property:".length());
				String property = TolvenRequest.getInstance().getAccountUser().getProperty().get(propertyName);
				return new StreamSource(new StringReader(property));
			} else {
				return new StreamSource( new URL(href).openStream());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
