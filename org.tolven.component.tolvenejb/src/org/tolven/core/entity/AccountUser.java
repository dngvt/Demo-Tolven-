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
package org.tolven.core.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.tolven.core.AccountUserPropertyMap;
import org.tolven.security.key.AccountPrivateKey;

/**
 * <p>Specifies which users can participate in a given account. An AccountAdministrator can add and remove Users from an Account. This ability may be limited 
 * by a Sponsor. Users and accounts can be associated more than once, but only one at a time.</p>
 * 
 * @author John Churin
 */
@Entity
@Table
public class AccountUser implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    private Account account;

    @ManyToOne
    private TolvenUser user;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CORE_SEQ_GEN")
    private long id;

    private boolean defaultAccount;

    @OneToMany(mappedBy = "accountUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AccountUserRole> roles = null;

    //    /**
    //     * References the document authorizing the current state of this setting.
    //     */
    //    @ManyToOne
    //    private DocBase authority;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date effectiveDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date expirationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date lastLoginTime;

    @Column
    private boolean accountPermission;

    @Column
    private String status;

    @Column
    private String openMeFirst;

    @Embedded
    private AccountPrivateKey accountPrivateKey;

    @OneToMany(mappedBy = "accountUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MapKey(name = "propertyName")
    private Map<String, AccountUserProperty> accountUserProperties = null;

    @Transient
    private AccountUserPropertyMap propertyMap;

    /**
     * Construct an empty AccountUser. 
     */
    public AccountUser() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        AccountUser clone = (AccountUser) super.clone();
        return clone;
    }

    /**
     * Get the appropriate logo for this account user (by default, derived from the AccountType).
     * @return
     */
    public String getLogo() {
        return getAccount().getAccountType().getLogo();
    }

    /**
     * Get the appropriate CSS for this account user (by default, derived from the AccountType).
     * @return
     */
    public String getCSS() {
        return getAccount().getAccountType().getCSS();
    }

    /**
     * Get the timezone. Timezone is not stored in this entity. User and account may have timezones and
     * if they don't, we look for a system default.
     * @return
     */
    public String getTimeZone() {
        String timeZone;
        timeZone = getUser().getTimeZone();
        if (timeZone!=null) return timeZone;
        timeZone = getAccount().getTimeZone();
        if (timeZone!=null) return timeZone;
        timeZone = System.getProperty("tolven.timezone");
        if (timeZone!=null) return timeZone;
        timeZone = java.util.TimeZone.getDefault().getID();
        return timeZone;
    }

    public TimeZone getTimeZoneObject() {
        return TimeZone.getTimeZone(getTimeZone());
    }

    /**
     * Get the default locale for this user, The value contained here is based on the following precedence:
     * <ol>
     * <li>The user's locale if not null</li>
     * <li>The account's locale if not null</li>
     * <li>From Java Locale.getDefault()</li>
     * </ol>
     * @return
     * @throws IOException
     */
    public String getLocale() {
        String locale = getUser().getLocale();
        if (locale == null || locale.isEmpty()) {
            locale = getAccount().getLocale();
        }
        if (locale == null || locale.isEmpty()) {
            locale = Locale.getDefault().toString();
        }
        return locale;
    }

    /**
     * Return a Locale Object based on the locale returned by getLocale
     * @return
     */
    public Locale getLocaleObject() {
        Locale locale = new Locale(getLocale());
        return locale;
    }

    /**
     * The account to which this user is associated. By this means, a TolvenUser can be associated with any number of Accounts and will normally be 
     * associated with only one at a time while logged in. A TolvenUser with more than one account will be provided a selection of which account they
     * want to be connected to as they login. 
     * 
     * @see #isDefaultAccount().
     */
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account val) {
        this.account = val;
    }

    /**
     * The TolvenUser to be granted access to the specified Account. Although allowed, the Author of the assignement is usually not the same TolvenUser as 
     * the TolvenUser being granted access.
     */
    public TolvenUser getUser() {
        return user;
    }

    public void setUser(TolvenUser val) {
        this.user = val;
    }

    /**
     * The meaningless, unique ID for this association. Leave Id null for a new record. The EntityManager will assign a unique Id when it is persisted. There may be 
     * more than one AccountUser association for a single combination of Account and TolvenUser. Typically only one is valid at any given point in time.
     */
    public long getId() {
        return id;
    }

    public void setId(long val) {
        this.id = val;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date val) {
        this.effectiveDate = val;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date val) {
        this.expirationDate = val;
    }

    public boolean isAccountPermission() {
        return accountPermission;
    }

    public void setAccountPermission(boolean accountPermission) {
        this.accountPermission = accountPermission;
    }

    public void update() {
        setAccountPermission(isAccountPermission());
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AccountUser)) return false;
        if (this.getId()==((AccountUser)obj).getId()) return true;
        return false;
    }

    public String toString() {
        return "AccountUser: " + getId();
    }

    public int hashCode() {
        if (getId()==0) throw new IllegalStateException( "id not yet established in Account object");
        return Long.valueOf(getId()).hashCode();
    }

    //    /**
    //     * This indicates the document, and thus the user, authorizing the assignment of this user to this account. This 
    //     * information is visible to all AccountAdministrators and cannot be erased or hidden from their view.
    //     */
    //    public DocBase getAuthority() {
    //        return authority;
    //    }
    //
    //    public void setAuthority(DocBase authority) {
    //        this.authority = authority;
    //    }

    /**
     * The user can provide a default account in order to shorten the sign-on process when they select the "default-login" 
     * button instead of the normal Login button. If the user only has one account, both the default-login and regular login buttons
     * take the user to the Account.
     */
    public boolean isDefaultAccount() {
        return defaultAccount;
    }

    public void setDefaultAccount(boolean defaultAccount) {
        this.defaultAccount = defaultAccount;
    }

    /**
     * The status of this account-user relationship.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AccountPrivateKey getAccountPrivateKey() {
        return accountPrivateKey;
    }

    public void setAccountPrivateKey(AccountPrivateKey privateKey) {
        this.accountPrivateKey = privateKey;
    }

    public boolean hasAccountPrivateKey() {
        return accountPrivateKey != null;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Set<AccountUserRole> getRoles() {
        if (roles==null) roles = new HashSet<AccountUserRole>( 5 );
        return roles;
    }

    public void setRoles(Set<AccountUserRole> roles) {
        this.roles = roles;
    }

    public List<AccountUserRole> getRoleList() {
        List<AccountUserRole> list = new ArrayList<AccountUserRole>();
        list.addAll(getRoles());
        return list;
    }

    public String getRoleListString() {
        StringBuffer sb = new StringBuffer();
        for (AccountUserRole role : getRoleList()) {
            if (sb.length()!=0) sb.append(",");
            sb.append(role.getRole());
        }
        return sb.toString();
    }

    public Map<String, String> getProperty() {
        return getAccountUserPropertyMap();
    }

    public Map<String, String> getBrandedProperty(String brand) {
        return getAccountUserPropertyMap(brand);
    }

    public Map<String, AccountUserProperty> getAccountUserProperties() {
        if (accountUserProperties == null) {
            accountUserProperties = new HashMap<String, AccountUserProperty>();
        }
        return accountUserProperties;
    }

    public AccountUserPropertyMap getAccountUserPropertyMap() {
        return getAccountUserPropertyMap(null);
    }

    public AccountUserPropertyMap getAccountUserPropertyMap(String brand) {
        if (propertyMap == null) {
            propertyMap = new AccountUserPropertyMap(this, brand);
        }
        return propertyMap;
    }

    public String getOpenMeFirst() {
        return openMeFirst;
    }

    public void setOpenMeFirst(String openMeFirst) {
        this.openMeFirst = openMeFirst;
    }

}
