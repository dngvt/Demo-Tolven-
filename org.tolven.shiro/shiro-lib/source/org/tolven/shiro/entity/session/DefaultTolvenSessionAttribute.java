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
 * @version $Id: DefaultTolvenSessionAttribute.java 6218 2012-04-05 01:35:19Z joe.isaac $
 */
package org.tolven.shiro.entity.session;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;

/**
 * Represents an attribute of a DefaultTolvenSession
 * 
 * @author Joseph Isaac
 *
 */
@Entity
@IdClass(DefaultTolvenSessionAttributePK.class)
public class DefaultTolvenSessionAttribute implements Serializable {

    private boolean encrypted;

    @Id
    private String name;

    @Id
    @ManyToOne
    private DefaultTolvenSession session;

    private String value;

    public String getName() {
        return name;
    }

    public DefaultTolvenSession getSession() {
        return session;
    }

    public String getValue() {
        return value;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSession(DefaultTolvenSession session) {
        this.session = session;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
