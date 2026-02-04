
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for ActRelationship complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActRelationship">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:tolven-org:trim:4.0}InfrastructureRoot">
 *       &lt;sequence>
 *         &lt;element name="pauseQuantity" type="{urn:tolven-org:trim:4.0}PQ" minOccurs="0"/>
 *         &lt;element name="negationInd" type="{urn:tolven-org:trim:4.0}BLSlot" minOccurs="0"/>
 *         &lt;element name="act" type="{urn:tolven-org:trim:4.0}Act" minOccurs="0"/>
 *         &lt;element name="choice" type="{urn:tolven-org:trim:4.0}Choice" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}Name" />
 *       &lt;attribute name="direction" use="required" type="{urn:tolven-org:trim:4.0}ActRelationshipDirection" />
 *       &lt;attribute name="typeCode" use="required" type="{urn:tolven-org:trim:4.0}ActRelationshipType" />
 *       &lt;attribute name="optional" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="repeating" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="inversionInd" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="contextControlCode" type="{urn:tolven-org:trim:4.0}ContextControl" />
 *       &lt;attribute name="contextConductionInd" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="sequenceNumber" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="priorityNumber" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="checkpointCode" type="{urn:tolven-org:trim:4.0}ActRelationshipCheckpoint" />
 *       &lt;attribute name="joinCode" type="{urn:tolven-org:trim:4.0}ActRelationshipJoin" />
 *       &lt;attribute name="seperatableInd" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="splitCode" type="{urn:tolven-org:trim:4.0}ActRelationshipSplit" />
 *       &lt;attribute name="conjunctionCode" type="{urn:tolven-org:trim:4.0}RelationshipConjunction" />
 *       &lt;attribute name="localVariableName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="subsetCode" type="{urn:tolven-org:trim:4.0}ActRelationshipSubset" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActRelationship", propOrder = {
    "pauseQuantity",
    "negationInd",
    "act",
    "choice"
})
public class ActRelationship
    extends InfrastructureRoot
{

    protected PQ pauseQuantity;
    protected BLSlot negationInd;
    protected Act act;
    protected List<Choice> choice;
    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "Name")
    protected String name;
    @XmlAttribute(name = "direction", required = true)
    protected ActRelationshipDirection direction;
    @XmlAttribute(name = "typeCode", required = true)
    protected ActRelationshipType typeCode;
    @XmlAttribute(name = "optional")
    protected Boolean optional;
    @XmlAttribute(name = "repeating")
    protected Boolean repeating;
    @XmlAttribute(name = "inversionInd")
    protected Boolean inversionInd;
    @XmlAttribute(name = "contextControlCode")
    protected ContextControl contextControlCode;
    @XmlAttribute(name = "contextConductionInd")
    protected Boolean contextConductionInd;
    @XmlAttribute(name = "sequenceNumber")
    protected Integer sequenceNumber;
    @XmlAttribute(name = "priorityNumber")
    protected Integer priorityNumber;
    @XmlAttribute(name = "checkpointCode")
    protected ActRelationshipCheckpoint checkpointCode;
    @XmlAttribute(name = "joinCode")
    protected ActRelationshipJoin joinCode;
    @XmlAttribute(name = "seperatableInd")
    protected Boolean seperatableInd;
    @XmlAttribute(name = "splitCode")
    protected ActRelationshipSplit splitCode;
    @XmlAttribute(name = "conjunctionCode")
    protected RelationshipConjunction conjunctionCode;
    @XmlAttribute(name = "localVariableName")
    protected String localVariableName;
    @XmlAttribute(name = "subsetCode")
    protected ActRelationshipSubset subsetCode;

    /**
     * Gets the value of the pauseQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link PQ }
     *     
     */
    public PQ getPauseQuantity() {
        return pauseQuantity;
    }

    /**
     * Sets the value of the pauseQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link PQ }
     *     
     */
    public void setPauseQuantity(PQ value) {
        this.pauseQuantity = value;
    }

    /**
     * Gets the value of the negationInd property.
     * 
     * @return
     *     possible object is
     *     {@link BLSlot }
     *     
     */
    public BLSlot getNegationInd() {
        return negationInd;
    }

    /**
     * Sets the value of the negationInd property.
     * 
     * @param value
     *     allowed object is
     *     {@link BLSlot }
     *     
     */
    public void setNegationInd(BLSlot value) {
        this.negationInd = value;
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
     * Gets the value of the choice property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the choice property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChoice().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Choice }
     * 
     * 
     */
    public List<Choice> getChoice() {
        if (choice == null) {
            choice = new ArrayList<Choice>();
        }
        return this.choice;
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
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link ActRelationshipDirection }
     *     
     */
    public ActRelationshipDirection getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActRelationshipDirection }
     *     
     */
    public void setDirection(ActRelationshipDirection value) {
        this.direction = value;
    }

    /**
     * Gets the value of the typeCode property.
     * 
     * @return
     *     possible object is
     *     {@link ActRelationshipType }
     *     
     */
    public ActRelationshipType getTypeCode() {
        return typeCode;
    }

    /**
     * Sets the value of the typeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActRelationshipType }
     *     
     */
    public void setTypeCode(ActRelationshipType value) {
        this.typeCode = value;
    }

    /**
     * Gets the value of the optional property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOptional() {
        return optional;
    }

    /**
     * Sets the value of the optional property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOptional(Boolean value) {
        this.optional = value;
    }

    /**
     * Gets the value of the repeating property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRepeating() {
        return repeating;
    }

    /**
     * Sets the value of the repeating property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRepeating(Boolean value) {
        this.repeating = value;
    }

    /**
     * Gets the value of the inversionInd property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInversionInd() {
        return inversionInd;
    }

    /**
     * Sets the value of the inversionInd property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInversionInd(Boolean value) {
        this.inversionInd = value;
    }

    /**
     * Gets the value of the contextControlCode property.
     * 
     * @return
     *     possible object is
     *     {@link ContextControl }
     *     
     */
    public ContextControl getContextControlCode() {
        return contextControlCode;
    }

    /**
     * Sets the value of the contextControlCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContextControl }
     *     
     */
    public void setContextControlCode(ContextControl value) {
        this.contextControlCode = value;
    }

    /**
     * Gets the value of the contextConductionInd property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isContextConductionInd() {
        return contextConductionInd;
    }

    /**
     * Sets the value of the contextConductionInd property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContextConductionInd(Boolean value) {
        this.contextConductionInd = value;
    }

    /**
     * Gets the value of the sequenceNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the value of the sequenceNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSequenceNumber(Integer value) {
        this.sequenceNumber = value;
    }

    /**
     * Gets the value of the priorityNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPriorityNumber() {
        return priorityNumber;
    }

    /**
     * Sets the value of the priorityNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPriorityNumber(Integer value) {
        this.priorityNumber = value;
    }

    /**
     * Gets the value of the checkpointCode property.
     * 
     * @return
     *     possible object is
     *     {@link ActRelationshipCheckpoint }
     *     
     */
    public ActRelationshipCheckpoint getCheckpointCode() {
        return checkpointCode;
    }

    /**
     * Sets the value of the checkpointCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActRelationshipCheckpoint }
     *     
     */
    public void setCheckpointCode(ActRelationshipCheckpoint value) {
        this.checkpointCode = value;
    }

    /**
     * Gets the value of the joinCode property.
     * 
     * @return
     *     possible object is
     *     {@link ActRelationshipJoin }
     *     
     */
    public ActRelationshipJoin getJoinCode() {
        return joinCode;
    }

    /**
     * Sets the value of the joinCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActRelationshipJoin }
     *     
     */
    public void setJoinCode(ActRelationshipJoin value) {
        this.joinCode = value;
    }

    /**
     * Gets the value of the seperatableInd property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSeperatableInd() {
        return seperatableInd;
    }

    /**
     * Sets the value of the seperatableInd property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSeperatableInd(Boolean value) {
        this.seperatableInd = value;
    }

    /**
     * Gets the value of the splitCode property.
     * 
     * @return
     *     possible object is
     *     {@link ActRelationshipSplit }
     *     
     */
    public ActRelationshipSplit getSplitCode() {
        return splitCode;
    }

    /**
     * Sets the value of the splitCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActRelationshipSplit }
     *     
     */
    public void setSplitCode(ActRelationshipSplit value) {
        this.splitCode = value;
    }

    /**
     * Gets the value of the conjunctionCode property.
     * 
     * @return
     *     possible object is
     *     {@link RelationshipConjunction }
     *     
     */
    public RelationshipConjunction getConjunctionCode() {
        return conjunctionCode;
    }

    /**
     * Sets the value of the conjunctionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelationshipConjunction }
     *     
     */
    public void setConjunctionCode(RelationshipConjunction value) {
        this.conjunctionCode = value;
    }

    /**
     * Gets the value of the localVariableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalVariableName() {
        return localVariableName;
    }

    /**
     * Sets the value of the localVariableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalVariableName(String value) {
        this.localVariableName = value;
    }

    /**
     * Gets the value of the subsetCode property.
     * 
     * @return
     *     possible object is
     *     {@link ActRelationshipSubset }
     *     
     */
    public ActRelationshipSubset getSubsetCode() {
        return subsetCode;
    }

    /**
     * Sets the value of the subsetCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActRelationshipSubset }
     *     
     */
    public void setSubsetCode(ActRelationshipSubset value) {
        this.subsetCode = value;
    }

}
