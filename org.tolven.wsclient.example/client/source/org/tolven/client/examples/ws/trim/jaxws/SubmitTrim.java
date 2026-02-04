
package org.tolven.client.examples.ws.trim.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for submitTrim complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="submitTrim">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="webServiceTrim" type="{http://tolven.org/trim}webServiceTrim" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "submitTrim", namespace = "http://tolven.org/trim", propOrder = {
    "webServiceTrim"
})
public class SubmitTrim {

    @XmlElement(namespace = "")
    protected WebServiceTrim webServiceTrim;

    /**
     * Gets the value of the webServiceTrim property.
     * 
     * @return
     *     possible object is
     *     {@link WebServiceTrim }
     *     
     */
    public WebServiceTrim getWebServiceTrim() {
        return webServiceTrim;
    }

    /**
     * Sets the value of the webServiceTrim property.
     * 
     * @param value
     *     allowed object is
     *     {@link WebServiceTrim }
     *     
     */
    public void setWebServiceTrim(WebServiceTrim value) {
        this.webServiceTrim = value;
    }

}
