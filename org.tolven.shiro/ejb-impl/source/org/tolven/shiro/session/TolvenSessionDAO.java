package org.tolven.shiro.session;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.tolven.shiro.api.session.TolvenSessionLocal;
import org.tolven.shiro.entity.session.DefaultTolvenSession;
import org.tolven.shiro.exception.ShiroSessionNotFoundException;

public class TolvenSessionDAO extends CachingSessionDAO {

    private static Logger logger = Logger.getLogger(TolvenSessionDAO.class);

    @EJB
    private TolvenSessionLocal tolvenSessionBean;

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = getTolvenSessionBean().persistSession((DefaultTolvenSession) session);
        if (logger.isDebugEnabled()) {
            logger.debug("Persisted session:" + sessionId);
        }
        return sessionId;
    }

    @Override
    protected void doDelete(Session session) {
        getTolvenSessionBean().deleteSession((String) session.getId());
        if (logger.isDebugEnabled()) {
            logger.debug("Deleted session: " + session.getId());
        }
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        Session session = getTolvenSessionBean().findSession(sessionId);
        if (session == null) {
            throw new ShiroSessionNotFoundException("Could not find session: " + (String) sessionId);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Read session: " + sessionId);
        }
        return session;
    }

    @Override
    protected void doUpdate(Session session) {
        getTolvenSessionBean().updateSession((DefaultTolvenSession) session);
        if (logger.isDebugEnabled()) {
            logger.debug("Updated session: " + session.getId());
        }
    }

    private TolvenSessionLocal getTolvenSessionBean() {
        if (tolvenSessionBean == null) {
            String jndiName = null;
            try {
                InitialContext ctx = new InitialContext();
                jndiName = "java:app/shiro-ejb/TolvenSessionBean!org.tolven.shiro.api.session.TolvenSessionLocal";
                tolvenSessionBean = (TolvenSessionLocal) ctx.lookup(jndiName);
            } catch (NamingException ex) {
                throw new RuntimeException("Could not look up " + jndiName, ex);
            }
        }
        return tolvenSessionBean;
    }

}
