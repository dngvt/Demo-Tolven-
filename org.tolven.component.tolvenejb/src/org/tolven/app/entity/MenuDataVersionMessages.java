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
 * @author Joseph Isaac
 */
package org.tolven.app.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MenuDataVersionMessages implements Serializable {

    private List<MenuDataVersionMessage> menuDataVersionMessages;

    public void addMenuDataVersionMessage(MenuDataVersionMessage menuDataVersionMessage) {
        getMenuDataVersionMessages().add(menuDataVersionMessage);
    }

    public List<MenuDataVersionMessage> getMenuDataVersionMessages() {
        if (menuDataVersionMessages == null) {
            menuDataVersionMessages = new ArrayList<MenuDataVersionMessage>();
        }
        return menuDataVersionMessages;
    }

    public void setMenuDataVersionMessages(List<MenuDataVersionMessage> menuDataVersionMessages) {
        this.menuDataVersionMessages = menuDataVersionMessages;
    }

}
