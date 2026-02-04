
package org.tolven.client.examples.ws.trim.jaxws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BindCardinality.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BindCardinality">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="single"/>
 *     &lt;enumeration value="multiple"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BindCardinality")
@XmlEnum
public enum BindCardinality {

    @XmlEnumValue("single")
    SINGLE("single"),
    @XmlEnumValue("multiple")
    MULTIPLE("multiple");
    private final String value;

    BindCardinality(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BindCardinality fromValue(String v) {
        for (BindCardinality c: BindCardinality.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
