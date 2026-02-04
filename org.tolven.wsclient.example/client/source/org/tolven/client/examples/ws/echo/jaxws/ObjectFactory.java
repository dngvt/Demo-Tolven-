
package org.tolven.client.examples.ws.echo.jaxws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tolven.client.examples.ws.echo.jaxws package. 
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

    private final static QName _LogoutResponse_QNAME = new QName("http://tolven.org/echo", "logoutResponse");
    private final static QName _Echo_QNAME = new QName("http://tolven.org/echo", "echo");
    private final static QName _EchoResponse_QNAME = new QName("http://tolven.org/echo", "echoResponse");
    private final static QName _Logout_QNAME = new QName("http://tolven.org/echo", "logout");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tolven.client.examples.ws.echo.jaxws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Logout }
     * 
     */
    public Logout createLogout() {
        return new Logout();
    }

    /**
     * Create an instance of {@link EchoResponse }
     * 
     */
    public EchoResponse createEchoResponse() {
        return new EchoResponse();
    }

    /**
     * Create an instance of {@link Echo_Type }
     * 
     */
    public Echo_Type createEcho_Type() {
        return new Echo_Type();
    }

    /**
     * Create an instance of {@link LogoutResponse }
     * 
     */
    public LogoutResponse createLogoutResponse() {
        return new LogoutResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogoutResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/echo", name = "logoutResponse")
    public JAXBElement<LogoutResponse> createLogoutResponse(LogoutResponse value) {
        return new JAXBElement<LogoutResponse>(_LogoutResponse_QNAME, LogoutResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Echo_Type }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/echo", name = "echo")
    public JAXBElement<Echo_Type> createEcho(Echo_Type value) {
        return new JAXBElement<Echo_Type>(_Echo_QNAME, Echo_Type.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EchoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/echo", name = "echoResponse")
    public JAXBElement<EchoResponse> createEchoResponse(EchoResponse value) {
        return new JAXBElement<EchoResponse>(_EchoResponse_QNAME, EchoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Logout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/echo", name = "logout")
    public JAXBElement<Logout> createLogout(Logout value) {
        return new JAXBElement<Logout>(_Logout_QNAME, Logout.class, null, value);
    }

}
