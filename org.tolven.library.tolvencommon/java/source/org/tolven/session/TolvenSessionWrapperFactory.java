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
package org.tolven.session;

import java.lang.reflect.Method;

public class TolvenSessionWrapperFactory {

    private static Method method;

    public static TolvenSessionWrapper getInstance() {
        if (method == null) {
            //TODO This should be placed in the tolven properties
            String className = "org.tolven.shiro.session.ShiroSessionWrapper";
            try {
                Class<?> clazz = Class.forName(className);
                method = clazz.getDeclaredMethod("getInstance", new Class[] {});
            } catch (Exception ex) {
                throw new RuntimeException("Could not load class: " + className, ex);
            }
        }
        try {
            return (TolvenSessionWrapper) method.invoke(null, (Object[]) null);
        } catch (Exception ex) {
            throw new RuntimeException("Could not invoke method: " + method.getName(), ex);
        }
    }

}
