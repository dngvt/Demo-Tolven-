
package org.tolven.trim;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Placeholder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Placeholder">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="codeField" type="{urn:tolven-org:trim:4.0}PlaceholderField" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Placeholder", propOrder = {
    "path",
    "codeField"
})
public class Placeholder
    implements Serializable
{

    @XmlElement(required = true)
    protected String path;
    protected PlaceholderField codeField;

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Gets the value of the codeField property.
     * 
     * @return
     *     possible object is
     *     {@link PlaceholderField }
     *     
     */
    public PlaceholderField getCodeField() {
        return codeField;
    }

    /**
     * Sets the value of the codeField property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlaceholderField }
     *     
     */
    public void setCodeField(PlaceholderField value) {
        this.codeField = value;
    }

}
