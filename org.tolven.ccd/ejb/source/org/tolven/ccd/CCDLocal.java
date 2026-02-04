package org.tolven.ccd;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public interface CCDLocal {
	public String getCCDXml(String context) throws ParserConfigurationException, TransformerException;
}
