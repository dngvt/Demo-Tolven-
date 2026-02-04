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
 * @version $Id: DefaultPersister.java,v 1.2 2010/04/24 17:43:40 jchurin Exp $
 */  

package org.tolven.app.bean;

import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.tolven.app.MigrationAdapterLocal;
import org.tolven.app.entity.MenuData;

@Local(MigrationAdapterLocal.class)
public @Stateless class DefaultMigrationAdapter implements MigrationAdapterLocal {

	@PersistenceContext private EntityManager em;
	Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void execute(MenuData md, Map<String, Object> changes) {
		logger.info("Start migration");
		try{
			for(String field:changes.keySet()){
				if(changes.get(field) != null)				
					md.setField(field, changes.get(field));	
					logger.info("In Transform:"+md+" setting  field  "+field+" = "+changes.get(field));
			}
			md.setLatestConfig(md.getMenuStructure().getAccountMenuStructure().getLatestConfig());
		}catch (Exception e) {
			logger.error("Error in migrating"+md,e);
		}		
	}


}
