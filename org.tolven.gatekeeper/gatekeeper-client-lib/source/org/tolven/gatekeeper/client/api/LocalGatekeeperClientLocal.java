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
package org.tolven.gatekeeper.client.api;

import java.security.cert.X509Certificate;
import java.util.List;

public interface LocalGatekeeperClientLocal {


    public boolean existsTolvenPerson(String uid, String realm, String userId, char[] userIdPassword);

    public List<String> getRealmIds();

    public X509Certificate getUserX509Certificate(String uid, String realm, String requestorId, char[] requestorPassword);

    /**
     * User RESTful to log into the gatekeeper and return the extended session Id
     * @param username
     * @param password
     * @param realm
     * @return
     */
    public String login(String username, char[] password, String realm);

    /**
     * User RESTful to log into the gatekeeper and return the extended session Id
     * When newSession is false, the existing session will be used, otherwise a new one
     * will be created and returned by the gatekeeper.
     * 
     * @param username
     * @param password
     * @param realm
     * @param newSession
     * @return
     */
    public String login(String username, char[] password, String realm, boolean newSession);

    /**
     * Logout the current Subject
     */
    public void logout();
    
    public boolean verifyUserPassword(String uid, char[] password, String realm);
}
