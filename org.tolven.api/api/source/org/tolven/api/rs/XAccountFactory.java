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
 * @version $Id: XAccountFactory.java 6180 2012-03-29 10:25:16Z joe.isaac $
 */
package org.tolven.api.rs;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.tolven.api.rs.accountuser.Accounts;
import org.tolven.api.rs.accountuser.ObjectFactory;
import org.tolven.api.rs.accountuser.XAccount;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.TolvenUser;

/**
 * A class to assist with converting AccountUser entity objects into XFacadeAccountUser POJOs
 * 
 * @author Joseph Isaac
 *
 */

public class XAccountFactory {

    private static DatatypeFactory datatypeFactory;

    private static ObjectFactory uaFactory;

    private static DatatypeFactory getDatatypeFactory() {
        if (datatypeFactory == null) {
            try {
                datatypeFactory = DatatypeFactory.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Could not create instance of DatatypeFactory", ex);
            }
        }
        return datatypeFactory;
    }

    private static ObjectFactory getUaFactory() {
        if (uaFactory == null) {
            try {
                uaFactory = new ObjectFactory();
            } catch (Exception ex) {
                throw new RuntimeException("Could not create instance of XFacadeAccountUser ObjectFactory", ex);
            }
        }
        return uaFactory;
    }

    public static Accounts createAccounts(List<AccountUser> accountUsers, TolvenUser user, Date now) {
        Accounts uas = getUaFactory().createAccounts();
        uas.setUser(user.getLdapUID());
        GregorianCalendar nowGC = new GregorianCalendar();
        nowGC.setTime(now);
        XMLGregorianCalendar ts = getDatatypeFactory().newXMLGregorianCalendar(nowGC);
        uas.setTimestamp(ts);
        for (AccountUser accountUser : accountUsers) {
            XAccount acc = createXAccount(accountUser);
            uas.getAccounts().add(acc);
        }
        return uas;
    }

    public static XAccount createXAccount(AccountUser accountUser) {
        XAccount acc = getUaFactory().createXAccount();
        acc.setId(accountUser.getAccount().getId());
        acc.setTitle(accountUser.getAccount().getTitle());
        acc.setType(accountUser.getAccount().getAccountType().getKnownType());
        if (accountUser.getLastLoginTime() != null) {
            GregorianCalendar lastLoginTime = new GregorianCalendar();
            lastLoginTime.setTime(accountUser.getLastLoginTime());
            XMLGregorianCalendar lastLoginTS = getDatatypeFactory().newXMLGregorianCalendar(lastLoginTime);
            acc.setLastLogin(lastLoginTS);
        }
        return acc;
    }

}
