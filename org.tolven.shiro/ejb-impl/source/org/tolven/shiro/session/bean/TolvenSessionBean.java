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
package org.tolven.shiro.session.bean;

import java.io.Serializable;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.tolven.shiro.api.session.TolvenSessionLocal;
import org.tolven.shiro.entity.session.DefaultTolvenSession;

@Stateless
@Local(TolvenSessionLocal.class)
public class TolvenSessionBean implements TolvenSessionLocal {

    @PersistenceContext
    private EntityManager em;

    private static Logger logger = Logger.getLogger(TolvenSessionBean.class);

    @Override
    public DefaultTolvenSession createSession() {
        return new DefaultTolvenSession();
    }

    @Override
    public void deleteSession(String sessionId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting session: " + sessionId);
        }
        Query query = em.createQuery("DELETE FROM DefaultTolvenSessionAttribute attribute WHERE attribute.session.id = :id");
        query.setParameter("id", sessionId);
        query.executeUpdate();
        query = em.createQuery("DELETE FROM DefaultTolvenSession session WHERE session.id = :id");
        query.setParameter("id", sessionId);
        query.executeUpdate();
        if (logger.isDebugEnabled()) {
            logger.debug("Deleted session: " + sessionId);
        }
    }

    @Override
    public DefaultTolvenSession findSession(Serializable sessionId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Finding session: " + sessionId);
        }
        DefaultTolvenSession session = em.find(DefaultTolvenSession.class, sessionId);
        if (logger.isDebugEnabled()) {
            if (session == null) {
                logger.debug("Session not found: " + sessionId);
            } else {
                logger.debug("Found session: " + sessionId);
            }
        }
        return session;
    }

    @Override
    public Serializable persistSession(DefaultTolvenSession session) {
        if (logger.isDebugEnabled()) {
            logger.debug("Persisting session: " + session.getId());
        }
        em.persist(session);
        if (logger.isDebugEnabled()) {
            logger.debug("Persisted session: " + session.getId());
        }
        return session.getId();
    }

    @Override
    public void updateSession(DefaultTolvenSession session) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating session: " + session.getId());
        }
        em.merge(session);
        if (logger.isDebugEnabled()) {
            logger.debug("Updated session: " + session.getId());
        }
    }

}
