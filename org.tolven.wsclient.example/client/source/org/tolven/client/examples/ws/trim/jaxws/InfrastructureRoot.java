
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InfrastructureRoot complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InfrastructureRoot">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="label" type="{urn:tolven-org:trim:4.0}LabelFacet" minOccurs="0"/>
 *         &lt;element name="from" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="bind" type="{urn:tolven-org:trim:4.0}BindTo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="realmCode" type="{urn:tolven-org:trim:4.0}RealmCode" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="typeId" type="{urn:tolven-org:trim:4.0}IISlot" minOccurs="0"/>
 *         &lt;element name="update" type="{urn:tolven-org:trim:4.0}UpdateCode" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="internalId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="page" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="drilldown" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="error" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="accountShare" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="valueSet" type="{urn:tolven-org:trim:4.0}ValueSet" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="sourceTrim" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InfrastructureRoot", propOrder = {
    "label",
    "from",
    "bind",
    "realmCode",
    "typeId",
    "update",
    "internalId",
    "page",
    "drilldown",
    "error",
    "accountShare",
    "valueSet"
})
@XmlSeeAlso({
    Participation.class,
    ActRelationship.class,
    Act.class,
    Role.class,
    Entity.class
})
public abstract class InfrastructureRoot {

    protected LabelFacet label;
    protected List<String> from;
    protected List<BindTo> bind;
    protected List<RealmCode> realmCode;
    protected IISlot typeId;
    protected List<UpdateCode> update;
    protected String internalId;
    protected String page;
    protected String drilldown;
    protected List<String> error;
    protected List<String> accountShare;
    protected List<ValueSet> valueSet;
    @XmlAttribute(name = "enabled")
    protected Boolean enabled;
    @XmlAttribute(name = "sourceTrim")
    protected String sourceTrim;

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link LabelFacet }
     *     
     */
    public LabelFacet getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link LabelFacet }
     *     
     */
    public void setLabel(LabelFacet value) {
        this.label = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the from property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFrom().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFrom() {
        if (from == null) {
            from = new ArrayList<String>();
        }
        return this.from;
    }

    /**
     * Gets the value of the bind property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bind property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBind().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BindTo }
     * 
     * 
     */
    public List<BindTo> getBind() {
        if (bind == null) {
            bind = new ArrayList<BindTo>();
        }
        return this.bind;
    }

    /**
     * Gets the value of the realmCode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the realmCode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRealmCode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RealmCode }
     * 
     * 
     */
    public List<RealmCode> getRealmCode() {
        if (realmCode == null) {
            realmCode = new ArrayList<RealmCode>();
        }
        return this.realmCode;
    }

    /**
     * Gets the value of the typeId property.
     * 
     * @return
     *     possible object is
     *     {@link IISlot }
     *     
     */
    public IISlot getTypeId() {
        return typeId;
    }

    /**
     * Sets the value of the typeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link IISlot }
     *     
     */
    public void setTypeId(IISlot value) {
        this.typeId = value;
    }

    /**
     * Gets the value of the update property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the update property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUpdate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UpdateCode }
     * 
     * 
     */
    public List<UpdateCode> getUpdate() {
        if (update == null) {
            update = new ArrayList<UpdateCode>();
        }
        return this.update;
    }

    /**
     * Gets the value of the internalId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInternalId() {
        return internalId;
    }

    /**
     * Sets the value of the internalId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInternalId(String value) {
        this.internalId = value;
    }

    /**
     * Gets the value of the page property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPage() {
        return page;
    }

    /**
     * Sets the value of the page property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPage(String value) {
        this.page = value;
    }

    /**
     * Gets the value of the drilldown property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDrilldown() {
        return drilldown;
    }

    /**
     * Sets the value of the drilldown property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDrilldown(String value) {
        this.drilldown = value;
    }

    /**
     * Gets the value of the error property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the error property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getError().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getError() {
        if (error == null) {
            error = new ArrayList<String>();
        }
        return this.error;
    }

    /**
     * Gets the value of the accountShare property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accountShare property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccountShare().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAccountShare() {
        if (accountShare == null) {
            accountShare = new ArrayList<String>();
        }
        return this.accountShare;
    }

    /**
     * Gets the value of the valueSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valueSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValueSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ValueSet }
     * 
     * 
     */
    public List<ValueSet> getValueSet() {
        if (valueSet == null) {
            valueSet = new ArrayList<ValueSet>();
        }
        return this.valueSet;
    }

    /**
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the sourceTrim property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceTrim() {
        return sourceTrim;
    }

    /**
     * Sets the value of the sourceTrim property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceTrim(String value) {
        this.sourceTrim = value;
    }

}
