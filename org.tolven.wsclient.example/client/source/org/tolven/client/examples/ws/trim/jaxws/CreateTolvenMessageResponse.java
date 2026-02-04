
package org.tolven.client.examples.ws.trim.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createTolvenMessageResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createTolvenMessageResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://tolven.org/trim}tolvenMessageWithAttachments" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createTolvenMessageResponse", namespace = "http://tolven.org/trim", propOrder = {
    "_return"
})
public class CreateTolvenMessageResponse {

    @XmlElement(name = "return", namespace = "")
    protected TolvenMessageWithAttachments _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link TolvenMessageWithAttachments }
     *     
     */
    public TolvenMessageWithAttachments getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link TolvenMessageWithAttachments }
     *     
     */
    public void setReturn(TolvenMessageWithAttachments value) {
        this._return = value;
    }

}
