
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TELSlot complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TELSlot">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:tolven-org:trim:4.0}Slot">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="TEL" type="{urn:tolven-org:trim:4.0}TEL"/>
 *           &lt;element name="URL" type="{urn:tolven-org:trim:4.0}URL"/>
 *           &lt;element name="null" type="{urn:tolven-org:trim:4.0}NullFlavor"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TELSlot", propOrder = {
    "telOrURLOrNull"
})
public class TELSlot
    extends Slot
{

    @XmlElements({
        @XmlElement(name = "TEL", type = TEL.class),
        @XmlElement(name = "URL", type = URL.class),
        @XmlElement(name = "null", type = NullFlavor.class)
    })
    protected List<DataType> telOrURLOrNull;

    /**
     * Gets the value of the telOrURLOrNull property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the telOrURLOrNull property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTELOrURLOrNull().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TEL }
     * {@link URL }
     * {@link NullFlavor }
     * 
     * 
     */
    public List<DataType> getTELOrURLOrNull() {
        if (telOrURLOrNull == null) {
            telOrURLOrNull = new ArrayList<DataType>();
        }
        return this.telOrURLOrNull;
    }

}
