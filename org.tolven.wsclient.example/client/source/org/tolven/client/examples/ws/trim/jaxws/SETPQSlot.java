
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SET_PQSlot complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SET_PQSlot">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:tolven-org:trim:4.0}Slot">
 *       &lt;sequence>
 *         &lt;element name="INT" type="{urn:tolven-org:trim:4.0}INT" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="REAL" type="{urn:tolven-org:trim:4.0}REAL" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PQ" type="{urn:tolven-org:trim:4.0}PQ" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="null" type="{urn:tolven-org:trim:4.0}NullFlavor" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SET_PQSlot", propOrder = {
    "_int",
    "real",
    "pq",
    "_null"
})
public class SETPQSlot
    extends Slot
{

    @XmlElement(name = "INT")
    protected List<INT> _int;
    @XmlElement(name = "REAL")
    protected List<REAL> real;
    @XmlElement(name = "PQ")
    protected List<PQ> pq;
    @XmlElement(name = "null")
    protected NullFlavor _null;

    /**
     * Gets the value of the int property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the int property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getINT().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link INT }
     * 
     * 
     */
    public List<INT> getINT() {
        if (_int == null) {
            _int = new ArrayList<INT>();
        }
        return this._int;
    }

    /**
     * Gets the value of the real property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the real property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getREAL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link REAL }
     * 
     * 
     */
    public List<REAL> getREAL() {
        if (real == null) {
            real = new ArrayList<REAL>();
        }
        return this.real;
    }

    /**
     * Gets the value of the pq property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pq property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPQ().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PQ }
     * 
     * 
     */
    public List<PQ> getPQ() {
        if (pq == null) {
            pq = new ArrayList<PQ>();
        }
        return this.pq;
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

}
