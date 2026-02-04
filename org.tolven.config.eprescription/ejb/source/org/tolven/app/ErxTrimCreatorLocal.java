/*
 *  Copyright (C) 2006 Tolven Inc 
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * Contact: info@tolvenhealth.com
 */
/**
 * This file contains ErxTrimCreatorLocal interface.
 *
 * @package org.tolven.app
 * @author Vineetha George
 * @copyright Tolven Inc 
 * @since File available since Release 0.0.1
 */
package org.tolven.app;

import java.security.PrivateKey;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.tolven.app.CreatorLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.trim.ex.TRIMException;
import org.tolven.trim.ex.TrimEx;

/**
 * This interface is used to create new trim, submit and for other trim related operations.
 * 
 * @author Vineetha George
 * @since File available since Release 0.0.1
 */
public interface ErxTrimCreatorLocal  {
	/**
	 * Creates trim.
	 * 
	 * added on 12/24/2010
	 * @author Suja Sundaresan <suja.sundaresan@cyrusxp.com>
     * @param accountUser - account user
     * @param trimPath - trim path
     * @param context - context
     * @param now - current date
	 */
	public TrimEx createTrim( AccountUser accountUser, String trimPath, String context, Date now) throws JAXBException, TRIMException;
	/**
	 * Submits the trim.
	 * 
	 * added on 12/24/2010
	 * @author Suja Sundaresan <suja.sundaresan@cyrusxp.com>
	 * @param trim - trim object
	 * @param context - context path
     * @param accountUser - account user
     * @param now - current date
	 */
	public void submitTrim(TrimEx trim,String context, AccountUser accountUser,Date now,PrivateKey privateKey) throws Exception;
	/**
	 * Creates trim.
	 * 
	 * added on 12/24/2010
	 * @author Suja Sundaresan <suja.sundaresan@cyrusxp.com>
     * @param accountUser - account user
     * @param trimPath - trim path
     * @param context - context
     * @param now - current date
	 */
	public String addTrimToActivityList(TrimEx trim,String context, AccountUser accountUser,Date now,PrivateKey privateKey) throws Exception;
}
