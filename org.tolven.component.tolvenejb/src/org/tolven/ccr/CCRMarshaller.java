package org.tolven.ccr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.security.PrivateKey;

import javax.naming.InitialContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.tolven.ccr.ex.CCRFactory;
import org.tolven.ccr.ex.ContinuityOfCareRecordEx;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.DocProtectionLocal;
import org.tolven.doc.entity.CCRException;
import org.tolven.doc.entity.DocXML;

public class CCRMarshaller {

    private static final CCRFactory ccrFactory = new CCRFactory();
    private static final String CCRns = "urn:astm-org:CCR";
    private static final String BINDING_CTX = "org.tolven.ccr";
    private static JAXBContext jc;

    /**
     * Create or use a JAXB context. We keep a map of already-used bindings in a static variable. 
     * @return A JAXB context.
     * @throws JAXBException
     */
    private JAXBContext getJAXBContext() throws JAXBException {
        if (jc == null) {
            jc = JAXBContext.newInstance(BINDING_CTX, CCRMarshaller.class.getClassLoader());
        }
        return jc;
    }

    public void marshalCCR(ContinuityOfCareRecordEx ccr, OutputStream output) throws JAXBException, CCRException {
        // Reset the actors list with the actors we now have in the transient map
        ContinuityOfCareRecordEx.Actors actors = new ContinuityOfCareRecordEx.Actors();
        actors.getActor().addAll(ccr.getActorMap().values());
        ccr.setActors(actors);
        if (ccr.getPatient().size() > 2) {
            throw new CCRException("No more than two patients allowed A2.5.2.6(3) ");
        }
        if (ccr.getPatient().size() == 0) {
            throw new CCRException("Patient required A2.5.2.6(1)");
        }
        JAXBContext jc = getJAXBContext();
        JAXBElement<ContinuityOfCareRecord> root = new JAXBElement<ContinuityOfCareRecord>(new QName(CCRns, "ContinuityOfCareRecord"), ContinuityOfCareRecord.class, ccr);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(root, output);
        //        StringWriter result = new StringWriter( 10000 ); 
        //        return result.toString();
    }

    /**
     * <p>This method will unmarshal the XML into an object graph and return the head of that graph. </p>
     * @return the object graph
     * @throws JAXBException 
     */
    public Object unmarshal(String ns, InputStream input) throws JAXBException {
        return unmarshal(new StreamSource(input));
    }

    public Object unmarshal(String ns, Reader reader) throws JAXBException {
        return unmarshal(new StreamSource(reader));
    }

    public Object unmarshal(DocXML doc, AccountUser activeAccountUser, PrivateKey userPrivateKey) throws JAXBException {
        DocProtectionLocal docProtectionBean = null;
        String jndiName = "java:app/tolvenEJB/DocProtectionBean!org.tolven.doc.DocProtectionLocal";
        try {
            InitialContext ctx = new InitialContext();
            docProtectionBean = (DocProtectionLocal) ctx.lookup(jndiName);
        } catch (Exception ex) {
            throw new RuntimeException("Could not lookup " + jndiName);
        }
        byte[] c = docProtectionBean.getDecryptedContent(doc, activeAccountUser, userPrivateKey);
        if (c == null)
            return null;
        return unmarshal(doc.getXmlNS(), new ByteArrayInputStream(c));
    }

    private Object unmarshal(StreamSource source) throws JAXBException {
        JAXBContext jc = getJAXBContext();
        Unmarshaller u = jc.createUnmarshaller();
        u.setProperty("com.sun.xml.bind.ObjectFactory", ccrFactory);
        return u.unmarshal(source);
    }

}
