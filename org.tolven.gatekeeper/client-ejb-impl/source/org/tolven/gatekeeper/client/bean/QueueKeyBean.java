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
package org.tolven.gatekeeper.client.bean;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.tolven.gatekeeper.client.api.QueueKeyLocal;
import org.tolven.gatekeeper.client.api.UserKeyLocal;
import org.tolven.naming.QueueContext;
import org.tolven.naming.TolvenContext;

@Stateless
@Local(QueueKeyLocal.class)
public class QueueKeyBean implements QueueKeyLocal {

    private static TolvenContext tolvenContext;
    
    @EJB
    private UserKeyLocal userKeyBean;

    private QueueContext getQueueContext(String queueId) {
        return (QueueContext) getTolvenContext().getQueueContext(queueId);
    }

    private TolvenContext getTolvenContext() {
        if(tolvenContext == null) {
            String jndiName = "tolvenContext";
            try {
                InitialContext ictx = new InitialContext();
                tolvenContext = (TolvenContext) ictx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not look up " + jndiName, ex);
            }
        }
        return tolvenContext;
    }

    public PublicKey getUserPublicKey(String queueId) {
        QueueContext queueContext = getQueueContext(queueId);
        String user = queueContext.getUser();
        String realm = queueContext.getRealm();
        char[] password = queueContext.getPassword().toCharArray();
        return userKeyBean.getUserPublicKey(user, realm, user, password);
    }

    public X509Certificate getUserX509Certificate(String queueId) {
        QueueContext queueContext = getQueueContext(queueId);
        String user = queueContext.getUser();
        String realm = queueContext.getRealm();
        char[] password = queueContext.getPassword().toCharArray();
        return userKeyBean.getUserX509Certificate(user, realm, user, password);
    }

    public String getUserX509CertificateString(String queueId) {
        QueueContext queueContext = getQueueContext(queueId);
        String user = queueContext.getUser();
        String realm = queueContext.getRealm();
        char[] password = queueContext.getPassword().toCharArray();
        return userKeyBean.getUserX509CertificateString(user, realm, user, password);
    }

}
