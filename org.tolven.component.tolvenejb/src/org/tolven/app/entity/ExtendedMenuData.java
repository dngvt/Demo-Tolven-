/*
 * Copyright (C) 2009 Tolven Inc

 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;  
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 *
 * Contact: info@tolvenhealth.com 
 *
 * @author John Churin
 * @version $Id: ExtendedMenuData.java,v 1.3.8.1 2010/05/03 19:10:30 joseph_isaac Exp $
 */  

package org.tolven.app.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.tolven.app.MenuLocal;
import org.tolven.core.KeyUtility;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;
import org.tolven.security.key.DocumentSecretKey;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;
import org.tolven.trim.CE;
import org.tolven.trim.DataType;
import org.tolven.trim.ex.TrimFactory;

/**
 * This EntityListener knows how to pack and unpack extended MenuData fields. 
 *
 */
public class ExtendedMenuData {
	private MenuLocal menuBean;
	private AccountUser accountUser;
	
    @EJB TolvenPropertiesLocal propertyBean;
    private static final TrimFactory trimFactory = new TrimFactory( );
	
	public static final String ENCRYPT_XML01_PROP = "tolven.app.encrypt.xml01";
	Logger logger = Logger.getLogger(this.getClass());
	public ExtendedMenuData() {
		super();
	}
	
	protected TolvenPropertiesLocal getPropertyBean() {
		if(propertyBean == null){
			InitialContext ctx;
			try {
				ctx = new InitialContext();
				propertyBean = (TolvenPropertiesLocal) ctx.lookup("java:global/tolven/tolvenEJB/TolvenProperties!org.tolven.core.TolvenPropertiesLocal");
			} catch (NamingException e) {
			    throw new RuntimeException("Could not load beans from ExtendedMenuData", e);
			}
		}
		return propertyBean;
	}	
	/**
	 * When a MenuData item is read, if there are extended fields, read them into a transient map stored in MenuData.
	 * @param md
	 */
	@PostLoad
	public void unmarshalExtendedFields(MenuData md ) {
		try {
			unmarshalExtendedFields( md, md.getXml01(), md.getExtendedFieldsMap());
			logger.debug("unmarshalled exetended fields for "+md +" are "+md.getExtendedFieldsMap());
		} catch (Exception e) {
			throw new RuntimeException( "Error aquiring extended fields from menuData: " + md.getId(), e);
		}
	}
	
	protected MenuLocal getMenuBean() {
		if (menuBean==null) {
            try {
            	InitialContext ctx = new InitialContext();
                menuBean = (MenuLocal) ctx.lookup("java:global/tolven/tolvenEJB/MenuBean!org.tolven.app.MenuLocal");
            } catch (Exception ex) {
                throw new RuntimeException("Could not access MenuBean from ExtendedMenuData", ex);
            }
		}
		return menuBean;
	}
	
    /**
     * When a MenuData item is persisted, see if the extended fields need to be unmarshalled.
     * @param md
     */
	@PrePersist
	@PreUpdate
	void marshalExtendedFields( MenuData md ) {
		if (md.saveExtendedFields) {
			try {
				ByteArrayOutputStream baos  = new ByteArrayOutputStream();
				XMLOutputFactory factory = XMLOutputFactory.newInstance();
				XMLStreamWriter writer = factory.createXMLStreamWriter(baos, "UTF-8");
				writer.writeStartDocument("UTF-8", "1.0" );
				writer.writeStartElement("fields");
				for (Entry<String, Object> entry : md.getExtendedFieldsMap().entrySet()) {
					writer.writeStartElement("field");
					writer.writeAttribute("name", entry.getKey());
					writeExtendedFieldValue( writer, entry.getValue() );
					writer.writeEndElement(); // extends
				}
				writer.writeEndElement(); // fields
				writer.writeEndDocument();
				writer.close();
				baos.close();
				logger.debug("Saving extended fields for"+md+" - "+baos.toString());
				byte[] content = encryptExtendedFields(baos.toByteArray(),md);  // call the encryption method			
				md.setXml01(content);
				md.saveExtendedFields = false;
			} catch (Exception e) {
				logger.error(e);
				throw new RuntimeException( "Error saving extended fields to menuData: " + md.getId(), e);
			}
		}
	}

    private void readExtendedFieldValue( MenuData md, Map<String, Object> extendedFields, XMLStreamReader reader ) throws XMLStreamException {
		String name = null;
		String type = null;
		String text = null;
		if (!"field".equals(reader.getName().getLocalPart())) return;
		for (int a = 0; a < reader.getAttributeCount();a++) {
			if ("type".equals(reader.getAttributeName(a).getLocalPart())) {
				type=reader.getAttributeValue(a);
			}
			if ("name".equals(reader.getAttributeName(a).getLocalPart())) {
				name=reader.getAttributeValue(a);
			}
		}
		
		Object value = null;
		if("ListCE".equals(type)) {
			List<CE> vals = new ArrayList<CE>();
			while ( reader.hasNext() ) {				
				reader.next();
				if (reader.getEventType()==XMLStreamReader.END_ELEMENT) {
					break;
				}
				if (reader.getEventType()==XMLStreamReader.START_ELEMENT) {
					String textval = reader.getElementText();
					vals.add((CE)trimFactory.stringToDataType(textval));
					//reader.next();
				}				
			}
			
			value = vals;
		} else {
			text = reader.getElementText();
	//		TolvenLogger.info( "Read extendedField: " + name + " (" + type + ")=" + text, MenuData.class);
			
			if (type==null || "null".equals(type)) {
				extendedFields.put(name, null);
			}
			else if ("String".equals(type)) {
				value = text;
				extendedFields.put(name, text);
			}
			else if ("Date".equals(type)) {
				value = new Date(Long.parseLong(text));
			}
			else if ("Long".equals(type)) {
				value = Long.parseLong(text);
			}
			else if ("Boolean".equals(type)) {
				value = Boolean.parseBoolean(text);
			}
			else if ("Double".equals(type)) {
				value = Double.parseDouble(text);
			}
			else if ("MenuData".equals(type)) {
				value = getMenuBean().findMenuDataItem(Long.parseLong(text));
			}
			else if ("DataType".equals(type)) {
				value = trimFactory.stringToDataType(text);
			} 
			else {		
				throw new RuntimeException( "Unknown Extended field type: " + type + " in field " + name);
			}
		}
		if (value==null) {
			value = genderHack( md, name );
		}
		extendedFields.put(name, value);
	}
    /*
     * Unmarshal an XML string into the specified extended fields map.
     */
	public void unmarshalExtendedFields(byte[] xml, Map<String, Object> extendedFields) {
		unmarshalExtendedFields( null, xml, extendedFields);
	}
	
	private void unmarshalExtendedFields(MenuData md, byte[] xml, Map<String, Object> extendedFields) {
		try {
			xml = decryptExtendedFields(md);
			if (xml!=null && xml.length > 0) {
				ByteArrayInputStream bais  = new ByteArrayInputStream(xml);
				XMLInputFactory factory = XMLInputFactory.newInstance();
				XMLStreamReader reader = factory.createXMLStreamReader(bais);
				while ( reader.hasNext() ) {
					reader.next();
					if (reader.getEventType()==XMLStreamReader.START_ELEMENT) {
						readExtendedFieldValue( md, extendedFields, reader );
					}
				}
				bais.close();
			}
		} catch (Exception e) {
			throw new RuntimeException( "Error unmarshalling extended fields in " + md, e );
		}
	}
	
    void writeExtendedFieldValue(XMLStreamWriter writer, Object value) throws XMLStreamException {
        if (value == null || (value instanceof String && ((String) value).length() == 0)) {
            writer.writeAttribute("type", "null");
            return;
        } else if (value instanceof String) {
            writer.writeAttribute("type", "String");
            writer.writeCData(value.toString());
            return;
        } else if (value instanceof Date) {
            writer.writeAttribute("type", "Date");
            writer.writeCData((Long.toString(((Date) value).getTime())));
            return;
        } else if (value instanceof Long) {
            writer.writeAttribute("type", "Long");
            writer.writeCData(value.toString());
            return;
        }else if (value instanceof Boolean) {
            writer.writeAttribute("type", "Boolean");
            writer.writeCData(value.toString());
            return;
        }else if (value instanceof Double) {
            writer.writeAttribute("type", "Double");
            writer.writeCData(value.toString());
            return;
        }else if (value instanceof MenuData) {
            writer.writeAttribute("type", "MenuData");
            MenuData md =  (MenuData)value;
            if (md.getId()==0) {
            	throw new RuntimeException( "MenuData referenced in extended fields must be persisted"); 
            }
            writer.writeCData(Long.toString(md.getId()));
            return;
        } else if (value instanceof DataType) {
            writer.writeAttribute("type", "DataType");
            writer.writeCData(trimFactory.dataTypeToString((DataType) value));
            return;
        } else if (value instanceof List<?>) {
        	writer.writeAttribute("type", "ListCE");
        	for(CE val : ((List<CE>) value)) {
        		writer.writeStartElement("item");
        		writer.writeCData(trimFactory.dataTypeToString(val));
        		writer.writeEndElement();
        	}
        } else {
            throw new RuntimeException(value.getClass() + " is not a recognized extended field type");
        }
    }
    
	private Object genderHack( MenuData md, String fieldName ) {
		if (md != null && "gender".equals(fieldName) && null!=md.getColumn( "sex" )) {
			String sex =  md.getStringField( "sex");
			if (sex!=null) {
				CE gender = trimFactory.createCE();
				if (sex.startsWith("F")) {
					gender.setDisplayName("Female");
					gender.setCode("C0015780"); // Female
				} else {
					gender.setDisplayName("Male");
					gender.setCode("C0024554"); // Male
				}
				gender.setCodeSystem("2.16.840.1.113883.6.56");
				gender.setCodeSystemVersion("2007AA");
				return gender;
			}
		}
		return null;
	}
	
	/** This methods encrypts the XML data passed as argument, using a generated secret key.
	 *  The secret key is encrypted with the Account public key, and stored in MenuData.secretKey.
	 *  The encrypted XML data is returned. 
	 *
	 *  Encryption is controlled by the property "tolven.app.encrypt.xml01". 
	 *  It encrypts if the property is set to 'true', and doesn't encrypt if the property
	 *  is 'false' or missing.
	 */
	private byte[] encryptExtendedFields(byte[] content, MenuData md)
	{
		byte[] encryptedSecretKey = null;
		try {
			if(Boolean.parseBoolean(getPropertyBean().getProperty(ENCRYPT_XML01_PROP)))			{
		        PublicKey publicKey = md.getAccount().getPublicKey(); // publickey for encryption		        
		        DocumentSecretKey documentSecretKey = DocumentSecretKey.getInstance();	        
		        String kbeKeyAlgorithm = getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
		        int kbeKeyLength = Integer.parseInt(getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH))	;	        
				SecretKey secretKey = documentSecretKey.init(publicKey, kbeKeyAlgorithm, kbeKeyLength);				
				content = encryptContent(content,kbeKeyAlgorithm,secretKey);				
				encryptedSecretKey = documentSecretKey.getEncryptedKey();			
			}	
			md.setSecretKey(encryptedSecretKey);
			return content;
		} catch (Exception e) {
			throw new RuntimeException( "Error saving extended fields to menuData: " + md.getId(), e);
		}
	}
	
	
	public byte[] encryptContent(byte[] content,String kbeKeyAlgorithm,SecretKey secretKey)
	{
		try{
			Cipher cipher = Cipher.getInstance(kbeKeyAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);								
			content = cipher.doFinal(content);
		} catch (Exception e) {
			throw new RuntimeException( "Error in encrypting extended fields ", e);
		}
		return content;
	}
	
	/**
	 * This method decrypts the extended fileds if it is encrypted. The encryption is identified by 
	 * checking the stored secretKey in the Menu_Data table.
	 */
	
	private byte[] decryptExtendedFields(MenuData md)
	{
		try {
			byte[] content = null;			
			if (md != null) {
				// Get the username from the ejbContext
				accountUser = TolvenRequest.getInstance().getAccountUser();		
				// decrypt if an accountUser exists
				if(accountUser!=null)
				{
					content = md.getXml01();	// extended fields data
					byte[] encryptedKey = md.getSecretKey();  // secret key
					if(encryptedKey!=null){
						String keyAlgorithm = getPropertyBean().getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
					    TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
					    PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);
						// Get the AccountUserPrivateKey
						PrivateKey privateKey = KeyUtility.getAccountPrivateKey(accountUser,userPrivateKey);	
						String algorithm = getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
						DocumentSecretKey documentSecretKey = DocumentSecretKey.getInstance();
						documentSecretKey.init(encryptedKey, algorithm);		
						SecretKey secretKey = documentSecretKey.getSecretKey(privateKey);
						content = decryptContent(content,secretKey);
					}
				}
			}
			return content;			
		}catch (Exception e) {
			throw new RuntimeException( "Error aquiring extended fields from menuData: " + md.getId(), e);
		}
	}

	public byte[] decryptContent(byte[] content,SecretKey secretKey)
	{
		try{
			Cipher cipher;
			cipher = Cipher.getInstance(secretKey.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			content = cipher.doFinal(content);
		} catch (Exception e) {
			throw new RuntimeException( "Error in decrypting extended fields ", e);
		}
		return content;
	}
}
