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
package org.tolven.gatekeeper.client.realm;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;

public class GatekeeperClientRealm extends AuthorizingRealm {

    public static final String USER_ROLES = "org.tolven.session.attribute.roles";

    private Logger logger = Logger.getLogger(GatekeeperClientRealm.class);

    public GatekeeperClientRealm() {
    }

    @Override
    public void onLogout(PrincipalCollection principals) {
        logger.info("User: " + principals.getPrimaryPrincipal() + " realm: " + principals.getRealmNames() + " LOGGED_OUT");
        super.onLogout(principals);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
        Session session = SecurityUtils.getSubject().getSession();
        Set<String> roles = (Set<String>)  session.getAttribute(USER_ROLES);
        if(roles == null) {
            roles = new HashSet<String>();
        }
        return new SimpleAuthorizationInfo(roles);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken arg0) throws AuthenticationException {
        return null;
    }

}
