/*
 *  Copyright (C) 2006 Tolven Inc 
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * Contact: info@tolvenhealth.com
 */
package org.tolven.gatekeeper.web;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.tolven.exception.TolvenSecurityException;
import org.tolven.gatekeeper.LdapLocal;
import org.tolven.gatekeeper.LoginPasswordLocal;
import org.tolven.session.TolvenSessionWrapperFactory;

/**
 * Faces Action for reset a login password.
 * 
 * @author Joseph Isaac
 */
public class ResetLoginPasswordAction {

    private String admin;
    private String adminPassword;

    @EJB
    private LdapLocal ldapLocal;

    @EJB
    private LoginPasswordLocal loginPasswordLocal;

    private String uid;
    private String uidPassword;

    public ResetLoginPasswordAction() {
    }

    public String getAdmin() {
        return admin;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public LdapLocal getLdapLocal() {
        return ldapLocal;
    }

    public LoginPasswordLocal getLoginPasswordLocal() {
        return loginPasswordLocal;
    }

    public String getRealm() {
        return TolvenSessionWrapperFactory.getInstance().getRealm();
    }

    public String getUid() {
        return uid;
    }

    public String getUidPassword() {
        return uidPassword;
    }

    public String navigateHome() {
        return "navigateHome";
    }

    public String resetLoginPassword() {
        try {
            //TODO The resetPassword will be emailed to a user, once email functionality is ported to gatekeeper
            uidPassword = new String(getLoginPasswordLocal().resetPassword(getUid(), getRealm(), getAdmin(), getAdminPassword().toCharArray()));
        } catch (Exception ex) {
            TolvenSecurityException gex = TolvenSecurityException.getRootGatekeeperException(ex);
            if (gex == null) {
                throw new RuntimeException("Could not reset login password for user " + getUid() + " in realm: " + getRealm(), ex);
            } else {
                ex.printStackTrace();
                FacesContext.getCurrentInstance().addMessage("resetpasswdForm", new FacesMessage(gex.getFormattedMessage()));
                return "error";
            }
        }
        FacesContext.getCurrentInstance().addMessage("resetpasswdForm", new FacesMessage("Password reset for user " + getUid() + " in realm: " + getRealm() + ": " + getUidPassword()));
        return "success";
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUidPassword(String uidPassword) {
        this.uidPassword = uidPassword;
    }

}
