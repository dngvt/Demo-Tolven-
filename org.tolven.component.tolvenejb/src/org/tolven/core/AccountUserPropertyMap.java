package org.tolven.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.AccountUserProperty;

/**
 * A Map that wraps the properties available to an accountUser. 
 * Brand (when available), and Locale are also considered in the search:
 * <code>property-name[.brand][_locale]</code>
 * A total of twelve complete searches are possible - the first one to return a value ends the search. 
 * <ol>
 * <li>property-name.brand_locale</li>
 * <li>property-name.brand</li>
 * <li>property-name_locale</li>
 * <li>property-name</li>
 * </ol>
 * If brand is not provided, then half of the searches are not attempted.
 * Each search in the above list proceeds as follows:
 * <ol>
 * <li>Account - A property defined in the user's account</li>
 * <li>AccountType - A property defined in the template account for the accountType.</li>
 * <li>System property - These properties are defined in the TolvenProperties table in the database</li>
 * </ol>
 * All possible search combinations are enumerated:
 * <ol>
 * <li>Look for a property named <i>property-name.brand_locale</i> in the account</li>
 * <li>Look for a property named <i>property-name.brand_locale</i> in the accountType</li>
 * <li>Look for a property named <i>property-name.brand_locale</i> in the system properties</li>
 * <li>Look for a property named <i>property-name.brand</i> in the account</li>
 * <li>Look for a property named <i>property-name.brand</i> in the accountType</li>
 * <li>Look for a property named <i>property-name.brand</i> in the system properties</li>
 * <li>Look for a property named <i>property-name_locale</i> in the account</li>
 * <li>Look for a property named <i>property-name_locale</i> in the accountType</li>
 * <li>Look for a property named <i>property-name_locale</i> in the system properties</li>
 * <li>Look for a property named <i>property-name</i> in the account</li>
 * <li>Look for a property named <i>property-name</i> in the accountType</li>
 * <li>Look for a property named <i>property-name</i> in the system properties</li>
 * </ol>
 * @author John Churin
 * 
 */
public class AccountUserPropertyMap implements Map<String, String> {

    private Map<String, String> accountPropertyMap;
    private AccountUser accountUser;
    private String brand;
    private String locale;

    public AccountUserPropertyMap(AccountUser accountUser) {
        this(accountUser, null);
    }

    public AccountUserPropertyMap(AccountUser accountUser, String brand) {
        setAccountUser(accountUser);
        setBrand(brand);
        setLocale(accountUser.getLocale());
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Property operation not supported in AccountUser");
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("Property operation not supported in AccountUser");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Property operation not supported in AccountUser");
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        throw new UnsupportedOperationException("Property operation not supported in AccountUser");
    }

    @Override
    public String get(Object key) {
        String result;
        if (getBrand() != null && getLocale() != null) {
            result = getProperty(key + "." + getBrand() + "_" + getLocale());
            if (result != null) {
                return result;
            }
        }
        if (getBrand() != null) {
            result = getProperty(key + "." + getBrand());
            if (result != null) {
                return result;
            }
        }
        if (getLocale() != null) {
            result = getProperty(key + "_" + getLocale());
            if (result != null) {
                return result;
            }
        }
        result = getProperty((String) key);
        return result;
    }

    private Map<String, String> getAccountPropertyMap() {
        if (accountPropertyMap == null) {
            accountPropertyMap = getAccountUser().getAccount().getAccountPropertyMap();
        }
        return accountPropertyMap;
    }

    private AccountUser getAccountUser() {
        return accountUser;
    }

    public AccountUserProperty getAccountUserProperty(Object key) {
        return getAccountUser().getAccountUserProperties().get(key);
    }

    private String getBrand() {
        return brand;
    }

    private String getLocale() {
        return locale;
    }

    protected String getProperty(String name) {
        // Try accountUser first
        AccountUserProperty accountUserProperty = getAccountUserProperty(name);
        if (accountUserProperty != null) {
            return accountUserProperty.getPropertyValue();
        }
        // Look in account
        String result = getAccountPropertyMap().get(name);
        if (result != null) {
            return result;
        }
        result = System.getProperty(name);
        if (result != null) {
            return result;
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Property operation not supported in AccountUser");
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("Property operation not supported in AccountUser");
    }

    /**
     * Put or update a property in this accountUser's list of properties.
     */
    @Override
    public String put(String key, String value) {
        AccountUserProperty accountUserProperty = getAccountUser().getAccountUserProperties().get(key);
        if (accountUserProperty != null) {
            String oldValue = accountUserProperty.getPropertyValue();
            accountUserProperty.setPropertyValue(value);
            return oldValue;
        }
        // Create a new property
        AccountUserProperty property = new AccountUserProperty();
        property.setAccountUser(getAccountUser());
        property.setPropertyName(key);
        property.setPropertyValue(value);
        getAccountUser().getAccountUserProperties().put(key, property);
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException("Property operation not supported in AccountUser");
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException("Property operation not supported in AccountUser");
    }

    private void setAccountUser(AccountUser accountUser) {
        this.accountUser = accountUser;
    }

    private void setBrand(String brand) {
        this.brand = brand;
    }

    private void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Put or update a property in this account's list of properties.
     */
    @Override
    public int size() {
        return getAccountUser().getAccountUserProperties().size();
    }

    @Override
    public Collection<String> values() {
        throw new UnsupportedOperationException("Property operation not supported in AccountUser");
    }

}