
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AD complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AD">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:tolven-org:trim:4.0}DataType">
 *       &lt;sequence>
 *         &lt;element name="use" type="{urn:tolven-org:trim:4.0}AddressUse" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="part" type="{urn:tolven-org:trim:4.0}ADXPSlot" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="null" type="{urn:tolven-org:trim:4.0}NullFlavor" minOccurs="0"/>
 *         &lt;element name="useablePeriod" type="{urn:tolven-org:trim:4.0}GTSSlot" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AD", propOrder = {
    "use",
    "part",
    "_null",
    "useablePeriod"
})
public class AD
    extends DataType
{

    protected List<AddressUse> use;
    protected List<ADXPSlot> part;
    @XmlElement(name = "null")
    protected NullFlavor _null;
    protected GTSSlot useablePeriod;

    /**
     * Gets the value of the use property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the use property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUse().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AddressUse }
     * 
     * 
     */
    public List<AddressUse> getUse() {
        if (use == null) {
            use = new ArrayList<AddressUse>();
        }
        return this.use;
    }

    /**
     * Gets the value of the part property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the part property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPart().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ADXPSlot }
     * 
     * 
     */
    public List<ADXPSlot> getPart() {
        if (part == null) {
            part = new ArrayList<ADXPSlot>();
        }
        return this.part;
    }

    /**
     * Gets the value of the null property.
     * 
     * @return
     *     possible object is
     *     {@link NullFlavor }
     *     
     */
    public NullFlavor getNull() {
        return _null;
    }

    /**
     * Sets the value of the null property.
     * 
     * @param value
     *     allowed object is
     *     {@link NullFlavor }
     *     
     */
    public void setNull(NullFlavor value) {
        this._null = value;
    }

    /**
     * Gets the value of the useablePeriod property.
     * 
     * @return
     *     possible object is
     *     {@link GTSSlot }
     *     
     */
    public GTSSlot getUseablePeriod() {
        return useablePeriod;
    }

    /**
     * Sets the value of the useablePeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link GTSSlot }
     *     
     */
    public void setUseablePeriod(GTSSlot value) {
        this.useablePeriod = value;
    }

}
