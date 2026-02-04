
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for tolvenMessage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tolvenMessage">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="accountId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="authorId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="decrypted" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="deleted" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="documentId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="encryptedKey" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="encryptionX509Certificate" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="fromAccountId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="mediaType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="payload" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="processDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="properties" type="{http://tolven.org/trim}tolvenMessageProperty" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="queueDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="queueOnDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="recipient" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scheduleDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="sender" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="xmlNS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="xmlName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tolvenMessage", namespace = "http://tolven.org/trim", propOrder = {
    "accountId",
    "authorId",
    "decrypted",
    "deleted",
    "documentId",
    "encryptedKey",
    "encryptionX509Certificate",
    "fromAccountId",
    "id",
    "mediaType",
    "payload",
    "processDate",
    "properties",
    "queueDate",
    "queueOnDate",
    "recipient",
    "scheduleDate",
    "sender",
    "version",
    "xmlNS",
    "xmlName"
})
@XmlSeeAlso({
    TolvenMessageWithAttachments.class
})
public class TolvenMessage {

    @XmlElement(namespace = "")
    protected long accountId;
    @XmlElement(namespace = "")
    protected long authorId;
    @XmlElement(namespace = "")
    protected boolean decrypted;
    @XmlElement(namespace = "")
    protected Boolean deleted;
    @XmlElement(namespace = "")
    protected long documentId;
    @XmlElement(namespace = "")
    protected byte[] encryptedKey;
    @XmlElement(namespace = "")
    protected byte[] encryptionX509Certificate;
    @XmlElement(namespace = "")
    protected long fromAccountId;
    @XmlElement(namespace = "")
    protected long id;
    @XmlElement(namespace = "")
    protected String mediaType;
    @XmlElement(namespace = "")
    protected byte[] payload;
    @XmlElement(namespace = "")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar processDate;
    @XmlElement(namespace = "", nillable = true)
    protected List<TolvenMessageProperty> properties;
    @XmlElement(namespace = "")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar queueDate;
    @XmlElement(namespace = "")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar queueOnDate;
    @XmlElement(namespace = "")
    protected String recipient;
    @XmlElement(namespace = "")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar scheduleDate;
    @XmlElement(namespace = "")
    protected String sender;
    @XmlElement(namespace = "")
    protected Integer version;
    @XmlElement(namespace = "")
    protected String xmlNS;
    @XmlElement(namespace = "")
    protected String xmlName;

    /**
     * Gets the value of the accountId property.
     * 
     */
    public long getAccountId() {
        return accountId;
    }

    /**
     * Sets the value of the accountId property.
     * 
     */
    public void setAccountId(long value) {
        this.accountId = value;
    }

    /**
     * Gets the value of the authorId property.
     * 
     */
    public long getAuthorId() {
        return authorId;
    }

    /**
     * Sets the value of the authorId property.
     * 
     */
    public void setAuthorId(long value) {
        this.authorId = value;
    }

    /**
     * Gets the value of the decrypted property.
     * 
     */
    public boolean isDecrypted() {
        return decrypted;
    }

    /**
     * Sets the value of the decrypted property.
     * 
     */
    public void setDecrypted(boolean value) {
        this.decrypted = value;
    }

    /**
     * Gets the value of the deleted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDeleted() {
        return deleted;
    }

    /**
     * Sets the value of the deleted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDeleted(Boolean value) {
        this.deleted = value;
    }

    /**
     * Gets the value of the documentId property.
     * 
     */
    public long getDocumentId() {
        return documentId;
    }

    /**
     * Sets the value of the documentId property.
     * 
     */
    public void setDocumentId(long value) {
        this.documentId = value;
    }

    /**
     * Gets the value of the encryptedKey property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    /**
     * Sets the value of the encryptedKey property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setEncryptedKey(byte[] value) {
        this.encryptedKey = value;
    }

    /**
     * Gets the value of the encryptionX509Certificate property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getEncryptionX509Certificate() {
        return encryptionX509Certificate;
    }

    /**
     * Sets the value of the encryptionX509Certificate property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setEncryptionX509Certificate(byte[] value) {
        this.encryptionX509Certificate = value;
    }

    /**
     * Gets the value of the fromAccountId property.
     * 
     */
    public long getFromAccountId() {
        return fromAccountId;
    }

    /**
     * Sets the value of the fromAccountId property.
     * 
     */
    public void setFromAccountId(long value) {
        this.fromAccountId = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the mediaType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Sets the value of the mediaType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMediaType(String value) {
        this.mediaType = value;
    }

    /**
     * Gets the value of the payload property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Sets the value of the payload property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setPayload(byte[] value) {
        this.payload = value;
    }

    /**
     * Gets the value of the processDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getProcessDate() {
        return processDate;
    }

    /**
     * Sets the value of the processDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setProcessDate(XMLGregorianCalendar value) {
        this.processDate = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the properties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TolvenMessageProperty }
     * 
     * 
     */
    public List<TolvenMessageProperty> getProperties() {
        if (properties == null) {
            properties = new ArrayList<TolvenMessageProperty>();
        }
        return this.properties;
    }

    /**
     * Gets the value of the queueDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getQueueDate() {
        return queueDate;
    }

    /**
     * Sets the value of the queueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setQueueDate(XMLGregorianCalendar value) {
        this.queueDate = value;
    }

    /**
     * Gets the value of the queueOnDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getQueueOnDate() {
        return queueOnDate;
    }

    /**
     * Sets the value of the queueOnDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setQueueOnDate(XMLGregorianCalendar value) {
        this.queueOnDate = value;
    }

    /**
     * Gets the value of the recipient property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Sets the value of the recipient property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecipient(String value) {
        this.recipient = value;
    }

    /**
     * Gets the value of the scheduleDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getScheduleDate() {
        return scheduleDate;
    }

    /**
     * Sets the value of the scheduleDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setScheduleDate(XMLGregorianCalendar value) {
        this.scheduleDate = value;
    }

    /**
     * Gets the value of the sender property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSender() {
        return sender;
    }

    /**
     * Sets the value of the sender property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSender(String value) {
        this.sender = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVersion(Integer value) {
        this.version = value;
    }

    /**
     * Gets the value of the xmlNS property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlNS() {
        return xmlNS;
    }

    /**
     * Sets the value of the xmlNS property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlNS(String value) {
        this.xmlNS = value;
    }

    /**
     * Gets the value of the xmlName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlName() {
        return xmlName;
    }

    /**
     * Sets the value of the xmlName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlName(String value) {
        this.xmlName = value;
    }

}
