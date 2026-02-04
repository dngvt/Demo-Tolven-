package org.tolven.app;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.tolven.app.entity.MenuData;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.entity.DocXML;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.Trim;
import org.tolven.trim.ex.TRIMException;

public interface TrimMessageLocal {
	
	public MenuData createTRIMPlaceholder( AccountUser accountUser, String trimPath, String context, Date now, String alias, ArrayList<ActRelationship> RelationshipList,PrivateKey privateKey ) throws JAXBException, TRIMException;
	/**
	 * An existing placeholder is transitioned (updated). The placeholder document is immutable. So we create
	 * a new document.
	 * We won't actually change the placeholder until the change is submitted.
	 * @author Syed
	 * added on 10/28/2009
	 */
	public MenuData createTRIMEvent( MenuData mdPlaceholder, AccountUser accountUser, String transitionName, Date now, Long refillQuantity,PrivateKey privateKey ) throws JAXBException, TRIMException ;
	
	/**
	 * Submit the document associated with this menuData item
	 * @throws Exception 
	 */
	public void submit(MenuData md, AccountUser activeAccountUser,PrivateKey privateKey) throws Exception;

    /**
     * Submit the document associated with this event to be queued for processing on queueOnDate
     * @throws Exception 
     */
    public void submit(MenuData mdEvent, AccountUser activeAccountUser, Date queueOnDate,PrivateKey privateKey) throws Exception;
    /**
	 * Marshal the specified Trim object graph back to the specified document.
	 * @param trim
	 * @param docXML
	 * @throws JAXBException
	 * @throws TRIMException
	 */
	public void marshalToDocument( Trim trim, DocXML docXML ) throws JAXBException, TRIMException;
}
