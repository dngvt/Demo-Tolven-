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
 * @author <your name>
 * @version $Id: PersisterLocal.java,v 1.2 2010/03/17 20:10:23 jchurin Exp $
 */  

package org.tolven.app;

import java.util.Map;

import org.tolven.app.entity.MenuData;

/**
 * Implementations of this interface determine how a MenuData item is migrated when it's menustructure is changed. 
 * Implementations have the ability to control of the migration operation and where data is persisted. MigrationAdapters are configured in the Tolven Plugin Framework.
 * @author skandula
 */
public interface MigrationAdapterLocal {
	public void execute(MenuData md,Map<String,Object> changes);	
}
