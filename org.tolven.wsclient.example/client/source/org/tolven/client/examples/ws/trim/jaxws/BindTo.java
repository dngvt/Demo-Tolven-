
package org.tolven.client.examples.ws.trim.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BindTo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BindTo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{urn:tolven-org:trim:4.0}IISlot" minOccurs="0"/>
 *         &lt;element name="list" type="{urn:tolven-org:trim:4.0}ListBind" minOccurs="0"/>
 *         &lt;element name="placeholder" type="{urn:tolven-org:trim:4.0}PlaceholderBind" minOccurs="0"/>
 *         &lt;element name="valueSet" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="include" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="application" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="phase" type="{urn:tolven-org:trim:4.0}BindPhase" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BindTo", propOrder = {
    "id",
    "list",
    "placeholder",
    "valueSet",
    "include"
})
public class BindTo {

    protected IISlot id;
    protected ListBind list;
    protected PlaceholderBind placeholder;
    protected String valueSet;
    protected String include;
    @XmlAttribute(name = "application")
    protected String application;
    @XmlAttribute(name = "phase")
    protected BindPhase phase;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link IISlot }
     *     
     */
    public IISlot getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link IISlot }
     *     
     */
    public void setId(IISlot value) {
        this.id = value;
    }

    /**
     * Gets the value of the list property.
     * 
     * @return
     *     possible object is
     *     {@link ListBind }
     *     
     */
    public ListBind getList() {
        return list;
    }

    /**
     * Sets the value of the list property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListBind }
     *     
     */
    public void setList(ListBind value) {
        this.list = value;
    }

    /**
     * Gets the value of the placeholder property.
     * 
     * @return
     *     possible object is
     *     {@link PlaceholderBind }
     *     
     */
    public PlaceholderBind getPlaceholder() {
        return placeholder;
    }

    /**
     * Sets the value of the placeholder property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlaceholderBind }
     *     
     */
    public void setPlaceholder(PlaceholderBind value) {
        this.placeholder = value;
    }

    /**
     * Gets the value of the valueSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueSet() {
        return valueSet;
    }

    /**
     * Sets the value of the valueSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueSet(String value) {
        this.valueSet = value;
    }

    /**
     * Gets the value of the include property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInclude() {
        return include;
    }

    /**
     * Sets the value of the include property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInclude(String value) {
        this.include = value;
    }

    /**
     * Gets the value of the application property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplication() {
        return application;
    }

    /**
     * Sets the value of the application property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplication(String value) {
        this.application = value;
    }

    /**
     * Gets the value of the phase property.
     * 
     * @return
     *     possible object is
     *     {@link BindPhase }
     *     
     */
    public BindPhase getPhase() {
        return phase;
    }

    /**
     * Sets the value of the phase property.
     * 
     * @param value
     *     allowed object is
     *     {@link BindPhase }
     *     
     */
    public void setPhase(BindPhase value) {
        this.phase = value;
    }

}
