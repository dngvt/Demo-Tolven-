
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SET_CDSlot complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SET_CDSlot">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:tolven-org:trim:4.0}Slot">
 *       &lt;sequence>
 *         &lt;element name="CE" type="{urn:tolven-org:trim:4.0}CE" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="CD" type="{urn:tolven-org:trim:4.0}CD" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="flavor" type="{urn:tolven-org:trim:4.0}NullFlavor" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SET_CDSlot", propOrder = {
    "ce",
    "cd",
    "flavor"
})
public class SETCDSlot
    extends Slot
{

    @XmlElement(name = "CE")
    protected List<CE> ce;
    @XmlElement(name = "CD")
    protected List<CD> cd;
    protected NullFlavor flavor;

    /**
     * Gets the value of the ce property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ce property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CE }
     * 
     * 
     */
    public List<CE> getCE() {
        if (ce == null) {
            ce = new ArrayList<CE>();
        }
        return this.ce;
    }

    /**
     * Gets the value of the cd property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cd property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCD().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CD }
     * 
     * 
     */
    public List<CD> getCD() {
        if (cd == null) {
            cd = new ArrayList<CD>();
        }
        return this.cd;
    }

    /**
     * Gets the value of the flavor property.
     * 
     * @return
     *     possible object is
     *     {@link NullFlavor }
     *     
     */
    public NullFlavor getFlavor() {
        return flavor;
    }

    /**
     * Sets the value of the flavor property.
     * 
     * @param value
     *     allowed object is
     *     {@link NullFlavor }
     *     
     */
    public void setFlavor(NullFlavor value) {
        this.flavor = value;
    }

}
