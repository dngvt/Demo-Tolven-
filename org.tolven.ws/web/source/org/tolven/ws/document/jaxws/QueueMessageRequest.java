
package org.tolven.ws.document.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "queueMessage", namespace = "http://tolven.org/document")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "queueMessage", namespace = "http://tolven.org/document", propOrder = {
    "payload",
    "ns",
    "accountId"
})
public class QueueMessageRequest {

    @XmlElement(name = "payload", namespace = "", nillable = true)
    private byte[] payload;
    @XmlElement(name = "ns", namespace = "")
    private String ns;
    @XmlElement(name = "accountId", namespace = "")
    private long accountId;

    /**
     * 
     * @return
     *     returns byte[]
     */
    public byte[] getPayload() {
        return this.payload;
    }

    /**
     * 
     * @param payload
     *     the value for the payload property
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * 
     * @return
     *     returns String
     */
    public String getNs() {
        return this.ns;
    }

    /**
     * 
     * @param ns
     *     the value for the ns property
     */
    public void setNs(String ns) {
        this.ns = ns;
    }

    /**
     * 
     * @return
     *     returns long
     */
    public long getAccountId() {
        return this.accountId;
    }

    /**
     * 
     * @param accountId
     *     the value for the accountId property
     */
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

}
