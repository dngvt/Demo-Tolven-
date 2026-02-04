
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Trim complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Trim">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extends" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="abstract" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="author" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="searchPhrase" type="{urn:tolven-org:trim:4.0}SearchPhrase" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="page" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="drilldown" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="menu" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="element" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tolvenId" type="{urn:tolven-org:trim:4.0}TolvenId" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="tolvenEventId" type="{urn:tolven-org:trim:4.0}TolvenId" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="replacesTolvenId" type="{urn:tolven-org:trim:4.0}TolvenId" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="reference" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="application" type="{urn:tolven-org:trim:4.0}Application" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="origin" type="{urn:tolven-org:trim:4.0}CopyTo" minOccurs="0"/>
 *         &lt;element name="copyTo" type="{urn:tolven-org:trim:4.0}CopyTo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="message" type="{urn:tolven-org:trim:4.0}Message" minOccurs="0"/>
 *         &lt;element name="transitions" type="{urn:tolven-org:trim:4.0}Transitions" minOccurs="0"/>
 *         &lt;element name="transition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="act" type="{urn:tolven-org:trim:4.0}Act" minOccurs="0"/>
 *         &lt;element name="valueSet" type="{urn:tolven-org:trim:4.0}ValueSet" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="field" type="{urn:tolven-org:trim:4.0}Field" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="formData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="confirm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="patientLinkId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="unused" type="{urn:tolven-org:trim:4.0}Unused" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Trim", propOrder = {
    "_extends",
    "_abstract",
    "name",
    "description",
    "author",
    "searchPhrase",
    "page",
    "drilldown",
    "menu",
    "element",
    "tolvenId",
    "tolvenEventId",
    "replacesTolvenId",
    "reference",
    "application",
    "origin",
    "copyTo",
    "message",
    "transitions",
    "transition",
    "act",
    "valueSet",
    "field",
    "formData",
    "confirm",
    "patientLinkId",
    "unused"
})
public class Trim_Type {

    @XmlElement(name = "extends")
    protected String _extends;
    @XmlElement(name = "abstract")
    protected Boolean _abstract;
    protected String name;
    protected String description;
    protected String author;
    protected List<SearchPhrase> searchPhrase;
    protected String page;
    protected String drilldown;
    protected List<String> menu;
    protected String element;
    protected List<TolvenId> tolvenId;
    protected List<TolvenId> tolvenEventId;
    protected List<TolvenId> replacesTolvenId;
    @XmlSchemaType(name = "anyURI")
    protected String reference;
    protected List<Application> application;
    protected CopyTo origin;
    protected List<CopyTo> copyTo;
    protected Message message;
    protected Transitions transitions;
    protected String transition;
    protected Act act;
    protected List<ValueSet> valueSet;
    protected List<Field> field;
    protected byte[] formData;
    protected String confirm;
    protected Long patientLinkId;
    protected Unused unused;

    /**
     * Gets the value of the extends property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtends() {
        return _extends;
    }

    /**
     * Sets the value of the extends property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtends(String value) {
        this._extends = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAbstract(Boolean value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Gets the value of the searchPhrase property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the searchPhrase property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSearchPhrase().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SearchPhrase }
     * 
     * 
     */
    public List<SearchPhrase> getSearchPhrase() {
        if (searchPhrase == null) {
            searchPhrase = new ArrayList<SearchPhrase>();
        }
        return this.searchPhrase;
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
     * Gets the value of the menu property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the menu property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMenu().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMenu() {
        if (menu == null) {
            menu = new ArrayList<String>();
        }
        return this.menu;
    }

    /**
     * Gets the value of the element property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElement() {
        return element;
    }

    /**
     * Sets the value of the element property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElement(String value) {
        this.element = value;
    }

    /**
     * Gets the value of the tolvenId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tolvenId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTolvenId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TolvenId }
     * 
     * 
     */
    public List<TolvenId> getTolvenId() {
        if (tolvenId == null) {
            tolvenId = new ArrayList<TolvenId>();
        }
        return this.tolvenId;
    }

    /**
     * Gets the value of the tolvenEventId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tolvenEventId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTolvenEventId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TolvenId }
     * 
     * 
     */
    public List<TolvenId> getTolvenEventId() {
        if (tolvenEventId == null) {
            tolvenEventId = new ArrayList<TolvenId>();
        }
        return this.tolvenEventId;
    }

    /**
     * Gets the value of the replacesTolvenId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the replacesTolvenId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReplacesTolvenId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TolvenId }
     * 
     * 
     */
    public List<TolvenId> getReplacesTolvenId() {
        if (replacesTolvenId == null) {
            replacesTolvenId = new ArrayList<TolvenId>();
        }
        return this.replacesTolvenId;
    }

    /**
     * Gets the value of the reference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the value of the reference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReference(String value) {
        this.reference = value;
    }

    /**
     * Gets the value of the application property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the application property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplication().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Application }
     * 
     * 
     */
    public List<Application> getApplication() {
        if (application == null) {
            application = new ArrayList<Application>();
        }
        return this.application;
    }

    /**
     * Gets the value of the origin property.
     * 
     * @return
     *     possible object is
     *     {@link CopyTo }
     *     
     */
    public CopyTo getOrigin() {
        return origin;
    }

    /**
     * Sets the value of the origin property.
     * 
     * @param value
     *     allowed object is
     *     {@link CopyTo }
     *     
     */
    public void setOrigin(CopyTo value) {
        this.origin = value;
    }

    /**
     * Gets the value of the copyTo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the copyTo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCopyTo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CopyTo }
     * 
     * 
     */
    public List<CopyTo> getCopyTo() {
        if (copyTo == null) {
            copyTo = new ArrayList<CopyTo>();
        }
        return this.copyTo;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link Message }
     *     
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link Message }
     *     
     */
    public void setMessage(Message value) {
        this.message = value;
    }

    /**
     * Gets the value of the transitions property.
     * 
     * @return
     *     possible object is
     *     {@link Transitions }
     *     
     */
    public Transitions getTransitions() {
        return transitions;
    }

    /**
     * Sets the value of the transitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Transitions }
     *     
     */
    public void setTransitions(Transitions value) {
        this.transitions = value;
    }

    /**
     * Gets the value of the transition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransition() {
        return transition;
    }

    /**
     * Sets the value of the transition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransition(String value) {
        this.transition = value;
    }

    /**
     * Gets the value of the act property.
     * 
     * @return
     *     possible object is
     *     {@link Act }
     *     
     */
    public Act getAct() {
        return act;
    }

    /**
     * Sets the value of the act property.
     * 
     * @param value
     *     allowed object is
     *     {@link Act }
     *     
     */
    public void setAct(Act value) {
        this.act = value;
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
     * Gets the value of the field property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the field property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Field }
     * 
     * 
     */
    public List<Field> getField() {
        if (field == null) {
            field = new ArrayList<Field>();
        }
        return this.field;
    }

    /**
     * Gets the value of the formData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getFormData() {
        return formData;
    }

    /**
     * Sets the value of the formData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setFormData(byte[] value) {
        this.formData = value;
    }

    /**
     * Gets the value of the confirm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConfirm() {
        return confirm;
    }

    /**
     * Sets the value of the confirm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConfirm(String value) {
        this.confirm = value;
    }

    /**
     * Gets the value of the patientLinkId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPatientLinkId() {
        return patientLinkId;
    }

    /**
     * Sets the value of the patientLinkId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPatientLinkId(Long value) {
        this.patientLinkId = value;
    }

    /**
     * Gets the value of the unused property.
     * 
     * @return
     *     possible object is
     *     {@link Unused }
     *     
     */
    public Unused getUnused() {
        return unused;
    }

    /**
     * Sets the value of the unused property.
     * 
     * @param value
     *     allowed object is
     *     {@link Unused }
     *     
     */
    public void setUnused(Unused value) {
        this.unused = value;
    }

}
