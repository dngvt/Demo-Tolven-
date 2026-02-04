
package org.tolven.client.examples.ws.generator.jaxws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tolven.client.examples.ws.generator.jaxws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GenerateCCRXMLResponse_QNAME = new QName("http://tolven.org/generator", "generateCCRXMLResponse");
    private final static QName _Logout_QNAME = new QName("http://tolven.org/generator", "logout");
    private final static QName _GenerateCCRXML_QNAME = new QName("http://tolven.org/generator", "generateCCRXML");
    private final static QName _LogoutResponse_QNAME = new QName("http://tolven.org/generator", "logoutResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tolven.client.examples.ws.generator.jaxws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GenerateCCRXMLResponse }
     * 
     */
    public GenerateCCRXMLResponse createGenerateCCRXMLResponse() {
        return new GenerateCCRXMLResponse();
    }

    /**
     * Create an instance of {@link Logout }
     * 
     */
    public Logout createLogout() {
        return new Logout();
    }

    /**
     * Create an instance of {@link LogoutResponse }
     * 
     */
    public LogoutResponse createLogoutResponse() {
        return new LogoutResponse();
    }

    /**
     * Create an instance of {@link GenerateCCRXML }
     * 
     */
    public GenerateCCRXML createGenerateCCRXML() {
        return new GenerateCCRXML();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenerateCCRXMLResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/generator", name = "generateCCRXMLResponse")
    public JAXBElement<GenerateCCRXMLResponse> createGenerateCCRXMLResponse(GenerateCCRXMLResponse value) {
        return new JAXBElement<GenerateCCRXMLResponse>(_GenerateCCRXMLResponse_QNAME, GenerateCCRXMLResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Logout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/generator", name = "logout")
    public JAXBElement<Logout> createLogout(Logout value) {
        return new JAXBElement<Logout>(_Logout_QNAME, Logout.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenerateCCRXML }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/generator", name = "generateCCRXML")
    public JAXBElement<GenerateCCRXML> createGenerateCCRXML(GenerateCCRXML value) {
        return new JAXBElement<GenerateCCRXML>(_GenerateCCRXML_QNAME, GenerateCCRXML.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogoutResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/generator", name = "logoutResponse")
    public JAXBElement<LogoutResponse> createLogoutResponse(LogoutResponse value) {
        return new JAXBElement<LogoutResponse>(_LogoutResponse_QNAME, LogoutResponse.class, null, value);
    }

}
