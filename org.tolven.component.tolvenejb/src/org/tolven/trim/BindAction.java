
package org.tolven.trim;

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
 *   &lt;restriction base="{urn:tolven-org:trim:4.0}cs">
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


    /**
     * 
     * 						The placeholder must exist in the local account. For example, a trim
     * 						for a patient weight will usually expect the patient placeholder to
     * 						already exist. If the "optional" modifier is true, then the placeholder can be null. 
     * 					
     * 
     */
    @XmlEnumValue("exist")
    EXIST("exist"),

    /**
     * 
     * 						If the placeholder exists, it is updated. If not, it is created.
     * 					
     * 
     */
    @XmlEnumValue("merge")
    MERGE("merge"),

    /**
     * 
     * 						The placeholder is created if it does not exists but not updated if it already exists.
     * 					
     * 
     */
    @XmlEnumValue("create")
    CREATE("create"),

    /**
     * 
     * 						The placeholder must exist. It is updated.
     * 					
     * 
     */
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
