/*
 *  Copyright (C) 2013 Tolven Inc 
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
package org.tolven.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;

import org.apache.log4j.Logger;
import org.tolven.app.PlaceholderAssociationLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.PlaceholderAssociation;

/*
 * JSF managed bean to find the placeholder associations created in Tolven.
 */
public class PlaceholderAssociationAction extends MenuAction {
	
	@EJB
	private PlaceholderAssociationLocal associationLocal;
	private Map<String,List<MenuData>> placeholders;
	
	private Logger logger = Logger.getLogger(PlaceholderAssociationAction.class);
	public Map<String,List<MenuData>> getPlaceholders() throws Exception {
		try{
			if(this.placeholders == null){
				/*find all the placeholder associations
				 * and arrange them in a map by the name
				*/
				List<PlaceholderAssociation> associations = 
						associationLocal.findPlaceholderAssociations(getDrilldownItem());
				placeholders = new HashMap<String,List<MenuData>>();
				for(PlaceholderAssociation association:associations){
					if(placeholders.get(association.getAssociationName()) == null){
						placeholders.put(association.getAssociationName(), new ArrayList<MenuData>());
					}
					placeholders.get(association.getAssociationName()).add(association.getAssociatedPlaceholder());
				}			
			}
		}catch(Exception e){
			logger.error("Error finding placeholder associations for "+getDrilldownItem(), e);
			throw e;
		}
		return placeholders;
	}
	public void setPlaceholders(Map<String,List<MenuData>> placeholders) {
		this.placeholders = placeholders;
	}		
}
