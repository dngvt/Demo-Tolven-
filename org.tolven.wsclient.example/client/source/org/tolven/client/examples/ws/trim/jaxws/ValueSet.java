
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for ValueSet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ValueSet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="TS" type="{urn:tolven-org:trim:4.0}TS"/>
 *           &lt;element name="ED" type="{urn:tolven-org:trim:4.0}ED"/>
 *           &lt;element name="CS" type="{urn:tolven-org:trim:4.0}CS"/>
 *           &lt;element name="bind" type="{urn:tolven-org:trim:4.0}BindTo"/>
 *           &lt;element name="II" type="{urn:tolven-org:trim:4.0}II"/>
 *           &lt;element name="null" type="{urn:tolven-org:trim:4.0}NullFlavor"/>
 *           &lt;element name="CE" type="{urn:tolven-org:trim:4.0}CE"/>
 *           &lt;element name="INT" type="{urn:tolven-org:trim:4.0}INT"/>
 *           &lt;element name="IVLPQ" type="{urn:tolven-org:trim:4.0}IVL_PQ"/>
 *           &lt;element name="PQ" type="{urn:tolven-org:trim:4.0}PQ"/>
 *           &lt;element name="RTO" type="{urn:tolven-org:trim:4.0}RTO"/>
 *           &lt;element name="CV" type="{urn:tolven-org:trim:4.0}CV"/>
 *           &lt;element name="URG_TS" type="{urn:tolven-org:trim:4.0}URG_TS"/>
 *           &lt;element name="ST" type="{urn:tolven-org:trim:4.0}ST"/>
 *           &lt;element name="EN" type="{urn:tolven-org:trim:4.0}EN"/>
 *           &lt;element name="AD" type="{urn:tolven-org:trim:4.0}AD"/>
 *           &lt;element name="CD" type="{urn:tolven-org:trim:4.0}CD"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}Name" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValueSet", propOrder = {
    "tsOrEDOrCS"
})
public class ValueSet {

    @XmlElements({
        @XmlElement(name = "TS", type = TS.class),
        @XmlElement(name = "ED", type = ED.class),
        @XmlElement(name = "CS", type = CS.class),
        @XmlElement(name = "bind", type = BindTo.class),
        @XmlElement(name = "II", type = II.class),
        @XmlElement(name = "null", type = NullFlavor.class),
        @XmlElement(name = "CE", type = CE.class),
        @XmlElement(name = "INT", type = INT.class),
        @XmlElement(name = "IVLPQ", type = IVLPQ.class),
        @XmlElement(name = "PQ", type = PQ.class),
        @XmlElement(name = "RTO", type = RTO.class),
        @XmlElement(name = "CV", type = CV.class),
        @XmlElement(name = "URG_TS", type = URGTS.class),
        @XmlElement(name = "ST", type = ST.class),
        @XmlElement(name = "EN", type = EN.class),
        @XmlElement(name = "AD", type = AD.class),
        @XmlElement(name = "CD", type = CD.class)
    })
    protected List<Object> tsOrEDOrCS;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "Name")
    protected String name;

    /**
     * Gets the value of the tsOrEDOrCS property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tsOrEDOrCS property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTSOrEDOrCS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TS }
     * {@link ED }
     * {@link CS }
     * {@link BindTo }
     * {@link II }
     * {@link NullFlavor }
     * {@link CE }
     * {@link INT }
     * {@link IVLPQ }
     * {@link PQ }
     * {@link RTO }
     * {@link CV }
     * {@link URGTS }
     * {@link ST }
     * {@link EN }
     * {@link AD }
     * {@link CD }
     * 
     * 
     */
    public List<Object> getTSOrEDOrCS() {
        if (tsOrEDOrCS == null) {
            tsOrEDOrCS = new ArrayList<Object>();
        }
        return this.tsOrEDOrCS;
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

}
