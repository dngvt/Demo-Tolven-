
package org.tolven.client.examples.ws.document.jaxws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tolven.client.examples.ws.document.jaxws package. 
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

    private final static QName _QueueMessage_QNAME = new QName("http://tolven.org/document", "queueMessage");
    private final static QName _LogoutResponse_QNAME = new QName("http://tolven.org/document", "logoutResponse");
    private final static QName _TestResponse_QNAME = new QName("http://tolven.org/document", "testResponse");
    private final static QName _QueueMessageResponse_QNAME = new QName("http://tolven.org/document", "queueMessageResponse");
    private final static QName _Test_QNAME = new QName("http://tolven.org/document", "test");
    private final static QName _ProcessDocumentResponse_QNAME = new QName("http://tolven.org/document", "processDocumentResponse");
    private final static QName _ProcessDocument_QNAME = new QName("http://tolven.org/document", "processDocument");
    private final static QName _Logout_QNAME = new QName("http://tolven.org/document", "logout");
    private final static QName _QueueMessagePayload_QNAME = new QName("", "payload");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tolven.client.examples.ws.document.jaxws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ProcessDocument }
     * 
     */
    public ProcessDocument createProcessDocument() {
        return new ProcessDocument();
    }

    /**
     * Create an instance of {@link Logout }
     * 
     */
    public Logout createLogout() {
        return new Logout();
    }

    /**
     * Create an instance of {@link ProcessDocumentResponse }
     * 
     */
    public ProcessDocumentResponse createProcessDocumentResponse() {
        return new ProcessDocumentResponse();
    }

    /**
     * Create an instance of {@link Test }
     * 
     */
    public Test createTest() {
        return new Test();
    }

    /**
     * Create an instance of {@link QueueMessageResponse }
     * 
     */
    public QueueMessageResponse createQueueMessageResponse() {
        return new QueueMessageResponse();
    }

    /**
     * Create an instance of {@link TestResponse }
     * 
     */
    public TestResponse createTestResponse() {
        return new TestResponse();
    }

    /**
     * Create an instance of {@link QueueMessage }
     * 
     */
    public QueueMessage createQueueMessage() {
        return new QueueMessage();
    }

    /**
     * Create an instance of {@link LogoutResponse }
     * 
     */
    public LogoutResponse createLogoutResponse() {
        return new LogoutResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueueMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/document", name = "queueMessage")
    public JAXBElement<QueueMessage> createQueueMessage(QueueMessage value) {
        return new JAXBElement<QueueMessage>(_QueueMessage_QNAME, QueueMessage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogoutResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/document", name = "logoutResponse")
    public JAXBElement<LogoutResponse> createLogoutResponse(LogoutResponse value) {
        return new JAXBElement<LogoutResponse>(_LogoutResponse_QNAME, LogoutResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/document", name = "testResponse")
    public JAXBElement<TestResponse> createTestResponse(TestResponse value) {
        return new JAXBElement<TestResponse>(_TestResponse_QNAME, TestResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueueMessageResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/document", name = "queueMessageResponse")
    public JAXBElement<QueueMessageResponse> createQueueMessageResponse(QueueMessageResponse value) {
        return new JAXBElement<QueueMessageResponse>(_QueueMessageResponse_QNAME, QueueMessageResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Test }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/document", name = "test")
    public JAXBElement<Test> createTest(Test value) {
        return new JAXBElement<Test>(_Test_QNAME, Test.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessDocumentResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/document", name = "processDocumentResponse")
    public JAXBElement<ProcessDocumentResponse> createProcessDocumentResponse(ProcessDocumentResponse value) {
        return new JAXBElement<ProcessDocumentResponse>(_ProcessDocumentResponse_QNAME, ProcessDocumentResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessDocument }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/document", name = "processDocument")
    public JAXBElement<ProcessDocument> createProcessDocument(ProcessDocument value) {
        return new JAXBElement<ProcessDocument>(_ProcessDocument_QNAME, ProcessDocument.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Logout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/document", name = "logout")
    public JAXBElement<Logout> createLogout(Logout value) {
        return new JAXBElement<Logout>(_Logout_QNAME, Logout.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "payload", scope = QueueMessage.class)
    public JAXBElement<byte[]> createQueueMessagePayload(byte[] value) {
        return new JAXBElement<byte[]>(_QueueMessagePayload_QNAME, byte[].class, QueueMessage.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "payload", scope = ProcessDocument.class)
    public JAXBElement<byte[]> createProcessDocumentPayload(byte[] value) {
        return new JAXBElement<byte[]>(_QueueMessagePayload_QNAME, byte[].class, ProcessDocument.class, ((byte[]) value));
    }

}
