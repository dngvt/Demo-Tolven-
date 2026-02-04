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
package org.tolven.web.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;
import org.tolven.shiro.filter.TolvenShiroFilter;

/**
 * This class indicates that the Tolven module is available
 * 
 * @author Joseph Isaac
 *
 */
@WebListener
public class TolvenServletContextListener implements ServletContextListener {

    private static String[] shiroRealms = { "tolven,org.tolven.gatekeeper.client.realm.GatekeeperClientRealm" };
    private static String[] shiroFilters = {
            "af,org.tolven.web.security.AccountFilter",
            "entervf,org.tolven.web.security.EnterVestibuleFilter",
            "exitvf,org.tolven.web.security.ExitVestibuleFilter",
            "selectvf,org.tolven.web.security.SelectAccountVestibuleFilter" };

    private Logger logger = Logger.getLogger(TolvenServletContextListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        for (String builtInRealm : shiroRealms) {
            String[] nameAndClass = builtInRealm.split(",");
            String name = nameAndClass[0];
            String clazz = nameAndClass[1];
            TolvenShiroFilter.addRealm(name, clazz, servletContext);
            logger.debug("Added realm: " + clazz + "=" + clazz + " to: " + servletContext.getContextPath());
        }
        for (String builtInFilter : shiroFilters) {
            String[] nameAndClass = builtInFilter.split(",");
            String name = nameAndClass[0];
            String clazz = nameAndClass[1];
            TolvenShiroFilter.addChainFilter(name, clazz, servletContext);
            logger.debug("Added filter: " + clazz + "=" + clazz + " to: " + servletContext.getContextPath());
        }
        logger.info("Module initialized: org.tolven.component.tolvenweb ContextPath: " + servletContext.getContextPath());
    }

}
