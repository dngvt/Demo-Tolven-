
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ObservationValueSlot complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ObservationValueSlot">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:tolven-org:trim:4.0}Slot">
 *       &lt;sequence>
 *         &lt;element name="SETTS" type="{urn:tolven-org:trim:4.0}TS" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="TS" type="{urn:tolven-org:trim:4.0}TS" minOccurs="0"/>
 *         &lt;element name="SETST" type="{urn:tolven-org:trim:4.0}ST" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ST" type="{urn:tolven-org:trim:4.0}ST" minOccurs="0"/>
 *         &lt;element name="SETIVLPQ" type="{urn:tolven-org:trim:4.0}IVL_PQ" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="IVLPQ" type="{urn:tolven-org:trim:4.0}IVL_PQ" minOccurs="0"/>
 *         &lt;element name="SETPQ" type="{urn:tolven-org:trim:4.0}PQ" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="RTO" type="{urn:tolven-org:trim:4.0}RTO" minOccurs="0"/>
 *         &lt;element name="REAL" type="{urn:tolven-org:trim:4.0}REAL" minOccurs="0"/>
 *         &lt;element name="PQ" type="{urn:tolven-org:trim:4.0}PQ" minOccurs="0"/>
 *         &lt;element name="SETINT" type="{urn:tolven-org:trim:4.0}INT" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="INT" type="{urn:tolven-org:trim:4.0}INT" minOccurs="0"/>
 *         &lt;element name="SETII" type="{urn:tolven-org:trim:4.0}II" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="II" type="{urn:tolven-org:trim:4.0}II" minOccurs="0"/>
 *         &lt;element name="SETEN" type="{urn:tolven-org:trim:4.0}EN" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="EN" type="{urn:tolven-org:trim:4.0}EN" minOccurs="0"/>
 *         &lt;element name="SETED" type="{urn:tolven-org:trim:4.0}ED" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ED" type="{urn:tolven-org:trim:4.0}ED" minOccurs="0"/>
 *         &lt;element name="SETCE" type="{urn:tolven-org:trim:4.0}CE" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="CE" type="{urn:tolven-org:trim:4.0}CE" minOccurs="0"/>
 *         &lt;element name="SETCD" type="{urn:tolven-org:trim:4.0}CD" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="CD" type="{urn:tolven-org:trim:4.0}CD" minOccurs="0"/>
 *         &lt;element name="BL" type="{urn:tolven-org:trim:4.0}BL" minOccurs="0"/>
 *         &lt;element name="SETAD" type="{urn:tolven-org:trim:4.0}AD" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AD" type="{urn:tolven-org:trim:4.0}AD" minOccurs="0"/>
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
@XmlType(name = "ObservationValueSlot", propOrder = {
    "setts",
    "ts",
    "setst",
    "st",
    "setivlpq",
    "ivlpq",
    "setpq",
    "rto",
    "real",
    "pq",
    "setint",
    "_int",
    "setii",
    "ii",
    "seten",
    "en",
    "seted",
    "ed",
    "setce",
    "ce",
    "setcd",
    "cd",
    "bl",
    "setad",
    "ad",
    "_null"
})
public class ObservationValueSlot
    extends Slot
{

    @XmlElement(name = "SETTS")
    protected List<TS> setts;
    @XmlElement(name = "TS")
    protected TS ts;
    @XmlElement(name = "SETST")
    protected List<ST> setst;
    @XmlElement(name = "ST")
    protected ST st;
    @XmlElement(name = "SETIVLPQ")
    protected List<IVLPQ> setivlpq;
    @XmlElement(name = "IVLPQ")
    protected IVLPQ ivlpq;
    @XmlElement(name = "SETPQ")
    protected List<PQ> setpq;
    @XmlElement(name = "RTO")
    protected RTO rto;
    @XmlElement(name = "REAL")
    protected REAL real;
    @XmlElement(name = "PQ")
    protected PQ pq;
    @XmlElement(name = "SETINT")
    protected List<INT> setint;
    @XmlElement(name = "INT")
    protected INT _int;
    @XmlElement(name = "SETII")
    protected List<II> setii;
    @XmlElement(name = "II")
    protected II ii;
    @XmlElement(name = "SETEN")
    protected List<EN> seten;
    @XmlElement(name = "EN")
    protected EN en;
    @XmlElement(name = "SETED")
    protected List<ED> seted;
    @XmlElement(name = "ED")
    protected ED ed;
    @XmlElement(name = "SETCE")
    protected List<CE> setce;
    @XmlElement(name = "CE")
    protected CE ce;
    @XmlElement(name = "SETCD")
    protected List<CD> setcd;
    @XmlElement(name = "CD")
    protected CD cd;
    @XmlElement(name = "BL")
    protected BL bl;
    @XmlElement(name = "SETAD")
    protected List<AD> setad;
    @XmlElement(name = "AD")
    protected AD ad;
    @XmlElement(name = "null")
    protected NullFlavor _null;

    /**
     * Gets the value of the setts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the setts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETTS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TS }
     * 
     * 
     */
    public List<TS> getSETTS() {
        if (setts == null) {
            setts = new ArrayList<TS>();
        }
        return this.setts;
    }

    /**
     * Gets the value of the ts property.
     * 
     * @return
     *     possible object is
     *     {@link TS }
     *     
     */
    public TS getTS() {
        return ts;
    }

    /**
     * Sets the value of the ts property.
     * 
     * @param value
     *     allowed object is
     *     {@link TS }
     *     
     */
    public void setTS(TS value) {
        this.ts = value;
    }

    /**
     * Gets the value of the setst property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the setst property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETST().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ST }
     * 
     * 
     */
    public List<ST> getSETST() {
        if (setst == null) {
            setst = new ArrayList<ST>();
        }
        return this.setst;
    }

    /**
     * Gets the value of the st property.
     * 
     * @return
     *     possible object is
     *     {@link ST }
     *     
     */
    public ST getST() {
        return st;
    }

    /**
     * Sets the value of the st property.
     * 
     * @param value
     *     allowed object is
     *     {@link ST }
     *     
     */
    public void setST(ST value) {
        this.st = value;
    }

    /**
     * Gets the value of the setivlpq property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the setivlpq property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETIVLPQ().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IVLPQ }
     * 
     * 
     */
    public List<IVLPQ> getSETIVLPQ() {
        if (setivlpq == null) {
            setivlpq = new ArrayList<IVLPQ>();
        }
        return this.setivlpq;
    }

    /**
     * Gets the value of the ivlpq property.
     * 
     * @return
     *     possible object is
     *     {@link IVLPQ }
     *     
     */
    public IVLPQ getIVLPQ() {
        return ivlpq;
    }

    /**
     * Sets the value of the ivlpq property.
     * 
     * @param value
     *     allowed object is
     *     {@link IVLPQ }
     *     
     */
    public void setIVLPQ(IVLPQ value) {
        this.ivlpq = value;
    }

    /**
     * Gets the value of the setpq property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the setpq property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETPQ().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PQ }
     * 
     * 
     */
    public List<PQ> getSETPQ() {
        if (setpq == null) {
            setpq = new ArrayList<PQ>();
        }
        return this.setpq;
    }

    /**
     * Gets the value of the rto property.
     * 
     * @return
     *     possible object is
     *     {@link RTO }
     *     
     */
    public RTO getRTO() {
        return rto;
    }

    /**
     * Sets the value of the rto property.
     * 
     * @param value
     *     allowed object is
     *     {@link RTO }
     *     
     */
    public void setRTO(RTO value) {
        this.rto = value;
    }

    /**
     * Gets the value of the real property.
     * 
     * @return
     *     possible object is
     *     {@link REAL }
     *     
     */
    public REAL getREAL() {
        return real;
    }

    /**
     * Sets the value of the real property.
     * 
     * @param value
     *     allowed object is
     *     {@link REAL }
     *     
     */
    public void setREAL(REAL value) {
        this.real = value;
    }

    /**
     * Gets the value of the pq property.
     * 
     * @return
     *     possible object is
     *     {@link PQ }
     *     
     */
    public PQ getPQ() {
        return pq;
    }

    /**
     * Sets the value of the pq property.
     * 
     * @param value
     *     allowed object is
     *     {@link PQ }
     *     
     */
    public void setPQ(PQ value) {
        this.pq = value;
    }

    /**
     * Gets the value of the setint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the setint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETINT().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link INT }
     * 
     * 
     */
    public List<INT> getSETINT() {
        if (setint == null) {
            setint = new ArrayList<INT>();
        }
        return this.setint;
    }

    /**
     * Gets the value of the int property.
     * 
     * @return
     *     possible object is
     *     {@link INT }
     *     
     */
    public INT getINT() {
        return _int;
    }

    /**
     * Sets the value of the int property.
     * 
     * @param value
     *     allowed object is
     *     {@link INT }
     *     
     */
    public void setINT(INT value) {
        this._int = value;
    }

    /**
     * Gets the value of the setii property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the setii property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETII().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link II }
     * 
     * 
     */
    public List<II> getSETII() {
        if (setii == null) {
            setii = new ArrayList<II>();
        }
        return this.setii;
    }

    /**
     * Gets the value of the ii property.
     * 
     * @return
     *     possible object is
     *     {@link II }
     *     
     */
    public II getII() {
        return ii;
    }

    /**
     * Sets the value of the ii property.
     * 
     * @param value
     *     allowed object is
     *     {@link II }
     *     
     */
    public void setII(II value) {
        this.ii = value;
    }

    /**
     * Gets the value of the seten property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the seten property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETEN().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EN }
     * 
     * 
     */
    public List<EN> getSETEN() {
        if (seten == null) {
            seten = new ArrayList<EN>();
        }
        return this.seten;
    }

    /**
     * Gets the value of the en property.
     * 
     * @return
     *     possible object is
     *     {@link EN }
     *     
     */
    public EN getEN() {
        return en;
    }

    /**
     * Sets the value of the en property.
     * 
     * @param value
     *     allowed object is
     *     {@link EN }
     *     
     */
    public void setEN(EN value) {
        this.en = value;
    }

    /**
     * Gets the value of the seted property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the seted property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETED().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ED }
     * 
     * 
     */
    public List<ED> getSETED() {
        if (seted == null) {
            seted = new ArrayList<ED>();
        }
        return this.seted;
    }

    /**
     * Gets the value of the ed property.
     * 
     * @return
     *     possible object is
     *     {@link ED }
     *     
     */
    public ED getED() {
        return ed;
    }

    /**
     * Sets the value of the ed property.
     * 
     * @param value
     *     allowed object is
     *     {@link ED }
     *     
     */
    public void setED(ED value) {
        this.ed = value;
    }

    /**
     * Gets the value of the setce property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the setce property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETCE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CE }
     * 
     * 
     */
    public List<CE> getSETCE() {
        if (setce == null) {
            setce = new ArrayList<CE>();
        }
        return this.setce;
    }

    /**
     * Gets the value of the ce property.
     * 
     * @return
     *     possible object is
     *     {@link CE }
     *     
     */
    public CE getCE() {
        return ce;
    }

    /**
     * Sets the value of the ce property.
     * 
     * @param value
     *     allowed object is
     *     {@link CE }
     *     
     */
    public void setCE(CE value) {
        this.ce = value;
    }

    /**
     * Gets the value of the setcd property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the setcd property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETCD().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CD }
     * 
     * 
     */
    public List<CD> getSETCD() {
        if (setcd == null) {
            setcd = new ArrayList<CD>();
        }
        return this.setcd;
    }

    /**
     * Gets the value of the cd property.
     * 
     * @return
     *     possible object is
     *     {@link CD }
     *     
     */
    public CD getCD() {
        return cd;
    }

    /**
     * Sets the value of the cd property.
     * 
     * @param value
     *     allowed object is
     *     {@link CD }
     *     
     */
    public void setCD(CD value) {
        this.cd = value;
    }

    /**
     * Gets the value of the bl property.
     * 
     * @return
     *     possible object is
     *     {@link BL }
     *     
     */
    public BL getBL() {
        return bl;
    }

    /**
     * Sets the value of the bl property.
     * 
     * @param value
     *     allowed object is
     *     {@link BL }
     *     
     */
    public void setBL(BL value) {
        this.bl = value;
    }

    /**
     * Gets the value of the setad property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the setad property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSETAD().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AD }
     * 
     * 
     */
    public List<AD> getSETAD() {
        if (setad == null) {
            setad = new ArrayList<AD>();
        }
        return this.setad;
    }

    /**
     * Gets the value of the ad property.
     * 
     * @return
     *     possible object is
     *     {@link AD }
     *     
     */
    public AD getAD() {
        return ad;
    }

    /**
     * Sets the value of the ad property.
     * 
     * @param value
     *     allowed object is
     *     {@link AD }
     *     
     */
    public void setAD(AD value) {
        this.ad = value;
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
