package org.tolven.shiro.session;

import org.apache.log4j.Logger;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionFactory;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.tolven.shiro.entity.session.DefaultTolvenSession;

public class DefaultTolvenSessionFactory implements SessionFactory {

    private SessionIdGenerator sessionIdGenerator = new JavaUuidSessionIdGenerator();

    private Logger logger = Logger.getLogger(DefaultTolvenSessionFactory.class);

    @Override
    public Session createSession(SessionContext initData) {
        DefaultTolvenSession session = new DefaultTolvenSession();
        if (initData != null) {
            //TODO Is init.Data.getSessionId() ever already set?
            String sessionId = (String) getSessionIdGenerator().generateId(session);
            if (logger.isDebugEnabled()) {
                logger.debug("Generated session Id: " + sessionId);
            }
            session.setId(sessionId);
            session.setHost(initData.getHost());
        }
        return session;
    }

    private SessionIdGenerator getSessionIdGenerator() {
        if (sessionIdGenerator == null) {
            sessionIdGenerator = new JavaUuidSessionIdGenerator();
        }
        return sessionIdGenerator;
    }

}
