
package org.tolven.trim;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BindPhase.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BindPhase">
 *   &lt;restriction base="{urn:tolven-org:trim:4.0}cs">
 *     &lt;enumeration value="parse"/>
 *     &lt;enumeration value="create"/>
 *     &lt;enumeration value="request"/>
 *     &lt;enumeration value="receive"/>
 *     &lt;enumeration value="send"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BindPhase")
@XmlEnum
public enum BindPhase {


    /**
     * 
     * 						"parse" phase does binding during the parse, actually, right after parse. 
     * 						Parse is typically when includes are processed. 
     * 					
     * 
     */
    @XmlEnumValue("parse")
    PARSE("parse"),

    /**
     * 
     * 						"create" phase does the binding during trim instantiation. 
     * 					
     * 
     */
    @XmlEnumValue("create")
    CREATE("create"),

    /**
     * 
     * 						"request" phase means to bind upon request, which usually occurs during a wizard. 
     * 					
     * 
     */
    @XmlEnumValue("request")
    REQUEST("request"),

    /**
     * 
     * 						"receive" phase indicates that binding is done when a trim is received in an account. 
     * 						This is often necessary to rebind this trim to the current account. For example, the
     * 						patient will have a different id in the new account.
     * 					
     * 
     */
    @XmlEnumValue("receive")
    RECEIVE("receive"),

    /**
     * 
     * 						"send" phase binding is special in that it instructs the sender to remove bindings
     * 						prior to sending a trim. 
     * 					
     * 
     */
    @XmlEnumValue("send")
    SEND("send");
    private final String value;

    BindPhase(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BindPhase fromValue(String v) {
        for (BindPhase c: BindPhase.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
