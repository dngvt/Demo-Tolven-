package org.tolven.trim;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.security.PrivateKey;

import javax.naming.InitialContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.tolven.core.entity.AccountUser;
import org.tolven.doc.DocProtectionLocal;
import org.tolven.doc.entity.DocXML;
import org.tolven.trim.ex.TrimFactory;

public class TrimMarshaller {
    
    private static JAXBContext jc;
    private static final TrimFactory trimFactory = new TrimFactory();
    private static final String BINDING_CTX = "org.tolven.trim";

    /**
     * Create or use a JAXB context. We keep a map of already-used bindings in a static variable. 
     * @return A JAXB context.
     * @throws JAXBException
     */
    private JAXBContext getJAXBContext() throws JAXBException {
        if (jc == null) {
            jc = JAXBContext.newInstance(BINDING_CTX, TrimMarshaller.class.getClassLoader());
        }
        return jc;
    }

    public void marshalTRIM(Trim trim, OutputStream output) throws JAXBException {
        JAXBContext jc = getJAXBContext();
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        //        m.setProperty(  "com.sun.xml.bind.namespacePrefixMapper",new NamespacePrefixMapperImpl());
        m.marshal(trim, output);
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
        u.setProperty("com.sun.xml.bind.ObjectFactory", trimFactory);
        return u.unmarshal(source);
    }

}
