
package org.tolven.trim;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BindMapDefault complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BindMapDefault">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="bind" type="{urn:tolven-org:trim:4.0}BindTo"/>
 *         &lt;element name="AD" type="{urn:tolven-org:trim:4.0}AD"/>
 *         &lt;element name="CD" type="{urn:tolven-org:trim:4.0}CD"/>
 *         &lt;element name="CE" type="{urn:tolven-org:trim:4.0}CE"/>
 *         &lt;element name="CS" type="{urn:tolven-org:trim:4.0}CS"/>
 *         &lt;element name="CV" type="{urn:tolven-org:trim:4.0}CV"/>
 *         &lt;element name="ED" type="{urn:tolven-org:trim:4.0}ED"/>
 *         &lt;element name="EN" type="{urn:tolven-org:trim:4.0}EN"/>
 *         &lt;element name="II" type="{urn:tolven-org:trim:4.0}II"/>
 *         &lt;element name="INT" type="{urn:tolven-org:trim:4.0}INT"/>
 *         &lt;element name="IVLPQ" type="{urn:tolven-org:trim:4.0}IVL_PQ"/>
 *         &lt;element name="null" type="{urn:tolven-org:trim:4.0}NullFlavor"/>
 *         &lt;element name="PQ" type="{urn:tolven-org:trim:4.0}PQ"/>
 *         &lt;element name="RTO" type="{urn:tolven-org:trim:4.0}RTO"/>
 *         &lt;element name="ST" type="{urn:tolven-org:trim:4.0}ST"/>
 *         &lt;element name="TS" type="{urn:tolven-org:trim:4.0}TS"/>
 *         &lt;element name="URG_TS" type="{urn:tolven-org:trim:4.0}URG_TS"/>
 *         &lt;element name="ActStatus" type="{urn:tolven-org:trim:4.0}ActStatus"/>
 *         &lt;element name="EntityStatus" type="{urn:tolven-org:trim:4.0}EntityStatus"/>
 *         &lt;element name="RoleStatus" type="{urn:tolven-org:trim:4.0}RoleStatus"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BindMapDefault", propOrder = {
    "roleStatus",
    "entityStatus",
    "actStatus",
    "urgts",
    "ts",
    "st",
    "rto",
    "pq",
    "_null",
    "ivlpq",
    "_int",
    "ii",
    "en",
    "ed",
    "cv",
    "cs",
    "ce",
    "cd",
    "ad",
    "bind"
})
public class BindMapDefault
    implements Serializable
{

    @XmlElement(name = "RoleStatus")
    protected RoleStatus roleStatus;
    @XmlElement(name = "EntityStatus")
    protected EntityStatus entityStatus;
    @XmlElement(name = "ActStatus")
    protected ActStatus actStatus;
    @XmlElement(name = "URG_TS")
    protected URGTS urgts;
    @XmlElement(name = "TS")
    protected TS ts;
    @XmlElement(name = "ST")
    protected ST st;
    @XmlElement(name = "RTO")
    protected RTO rto;
    @XmlElement(name = "PQ")
    protected PQ pq;
    @XmlElement(name = "null")
    protected NullFlavor _null;
    @XmlElement(name = "IVLPQ")
    protected IVLPQ ivlpq;
    @XmlElement(name = "INT")
    protected INT _int;
    @XmlElement(name = "II")
    protected II ii;
    @XmlElement(name = "EN")
    protected EN en;
    @XmlElement(name = "ED")
    protected ED ed;
    @XmlElement(name = "CV")
    protected CV cv;
    @XmlElement(name = "CS")
    protected CS cs;
    @XmlElement(name = "CE")
    protected CE ce;
    @XmlElement(name = "CD")
    protected CD cd;
    @XmlElement(name = "AD")
    protected AD ad;
    protected BindTo bind;

    /**
     * Gets the value of the roleStatus property.
     * 
     * @return
     *     possible object is
     *     {@link RoleStatus }
     *     
     */
    public RoleStatus getRoleStatus() {
        return roleStatus;
    }

    /**
     * Sets the value of the roleStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link RoleStatus }
     *     
     */
    public void setRoleStatus(RoleStatus value) {
        this.roleStatus = value;
    }

    /**
     * Gets the value of the entityStatus property.
     * 
     * @return
     *     possible object is
     *     {@link EntityStatus }
     *     
     */
    public EntityStatus getEntityStatus() {
        return entityStatus;
    }

    /**
     * Sets the value of the entityStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityStatus }
     *     
     */
    public void setEntityStatus(EntityStatus value) {
        this.entityStatus = value;
    }

    /**
     * Gets the value of the actStatus property.
     * 
     * @return
     *     possible object is
     *     {@link ActStatus }
     *     
     */
    public ActStatus getActStatus() {
        return actStatus;
    }

    /**
     * Sets the value of the actStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActStatus }
     *     
     */
    public void setActStatus(ActStatus value) {
        this.actStatus = value;
    }

    /**
     * Gets the value of the urgts property.
     * 
     * @return
     *     possible object is
     *     {@link URGTS }
     *     
     */
    public URGTS getURGTS() {
        return urgts;
    }

    /**
     * Sets the value of the urgts property.
     * 
     * @param value
     *     allowed object is
     *     {@link URGTS }
     *     
     */
    public void setURGTS(URGTS value) {
        this.urgts = value;
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
     * Gets the value of the cv property.
     * 
     * @return
     *     possible object is
     *     {@link CV }
     *     
     */
    public CV getCV() {
        return cv;
    }

    /**
     * Sets the value of the cv property.
     * 
     * @param value
     *     allowed object is
     *     {@link CV }
     *     
     */
    public void setCV(CV value) {
        this.cv = value;
    }

    /**
     * Gets the value of the cs property.
     * 
     * @return
     *     possible object is
     *     {@link CS }
     *     
     */
    public CS getCS() {
        return cs;
    }

    /**
     * Sets the value of the cs property.
     * 
     * @param value
     *     allowed object is
     *     {@link CS }
     *     
     */
    public void setCS(CS value) {
        this.cs = value;
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
     * Gets the value of the bind property.
     * 
     * @return
     *     possible object is
     *     {@link BindTo }
     *     
     */
    public BindTo getBind() {
        return bind;
    }

    /**
     * Sets the value of the bind property.
     * 
     * @param value
     *     allowed object is
     *     {@link BindTo }
     *     
     */
    public void setBind(BindTo value) {
        this.bind = value;
    }

}
