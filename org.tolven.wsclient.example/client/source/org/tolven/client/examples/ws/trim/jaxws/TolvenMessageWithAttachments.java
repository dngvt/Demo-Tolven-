
package org.tolven.client.examples.ws.trim.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tolvenMessageWithAttachments complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tolvenMessageWithAttachments">
 *   &lt;complexContent>
 *     &lt;extension base="{http://tolven.org/trim}tolvenMessage">
 *       &lt;sequence>
 *         &lt;element name="attachments" type="{http://tolven.org/trim}tolvenMessageAttachment" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tolvenMessageWithAttachments", namespace = "http://tolven.org/trim", propOrder = {
    "attachments"
})
public class TolvenMessageWithAttachments
    extends TolvenMessage
{

    @XmlElement(namespace = "", nillable = true)
    protected List<TolvenMessageAttachment> attachments;

    /**
     * Gets the value of the attachments property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attachments property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttachments().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TolvenMessageAttachment }
     * 
     * 
     */
    public List<TolvenMessageAttachment> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<TolvenMessageAttachment>();
        }
        return this.attachments;
    }

}
