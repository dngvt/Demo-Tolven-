/**
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
 * @author Brian Krzeminski
 */

package org.tolven.rules;

import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuStructure;

public interface DynamicRulesLocal {
	public MenuStructure createDynamicList(MenuData md, MenuStructure template);
	
	public void reprocessPlaceholders(MenuData mdPLD, String listPath);
}
