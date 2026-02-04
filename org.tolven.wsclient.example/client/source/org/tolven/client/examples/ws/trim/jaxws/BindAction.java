
package org.tolven.client.examples.ws.trim.jaxws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BindAction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BindAction">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="exist"/>
 *     &lt;enumeration value="merge"/>
 *     &lt;enumeration value="create"/>
 *     &lt;enumeration value="update"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BindAction")
@XmlEnum
public enum BindAction {

    @XmlEnumValue("exist")
    EXIST("exist"),
    @XmlEnumValue("merge")
    MERGE("merge"),
    @XmlEnumValue("create")
    CREATE("create"),
    @XmlEnumValue("update")
    UPDATE("update");
    private final String value;

    BindAction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BindAction fromValue(String v) {
        for (BindAction c: BindAction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
