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
 * @version $Id: DefaultTolvenSessionAttributePK.java 6218 2012-04-05 01:35:19Z joe.isaac $
 */
package org.tolven.shiro.entity.session;

import java.io.Serializable;

public class DefaultTolvenSessionAttributePK implements Serializable {

    private String name;

    private DefaultTolvenSession session;

    public boolean equals(Object otherOb) {
        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof DefaultTolvenSessionAttributePK)) {
            return false;
        }
        DefaultTolvenSessionAttributePK other = (DefaultTolvenSessionAttributePK) otherOb;
        return session == null ? other.session == null : session.getId().equals(other.session.getId());
    }

    public String getName() {
        return name;
    }

    public DefaultTolvenSession getSession() {
        return session;
    }

    public int hashCode() {
        return ((name == null ? 0 : name.hashCode()) ^ (session == null ? 0 : session.getId().hashCode()));
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSession(DefaultTolvenSession session) {
        this.session = session;
    }

}
