package org.tolven.app;

import java.util.Date;

import javax.xml.bind.JAXBException;

import org.tolven.app.entity.MenuData;
import org.tolven.core.entity.AccountUser;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.ex.TRIMException;
import org.tolven.trim.ex.TrimEx;

public interface TrimCreatorRemote {
	public MenuData createTRIMPlaceholder( AccountUser accountUser, String trimPath, String context, Date now, String alias, ActRelationship responseRelation ) throws JAXBException, TRIMException;
	
	public TrimEx createTrim( AccountUser accountUser, String trimPath, String context, Date now ) throws JAXBException, TRIMException;
	public void submitTrim(TrimEx trim,String context, AccountUser accountUser,Date now) throws Exception;

	/**
	 * Submit the document associated with this menuData item
	 * @throws Exception 
	 */
	public void submitTrim(MenuData md, AccountUser activeAccountUser) throws Exception;

}
