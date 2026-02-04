package org.tolven.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountProperty;

/**
 * A Map that wraps the list of properties for an account.
 * @author John Churin
 *
 */
public class AccountPropertyMap implements Map<String, String> {

    private Account account;
    private String brand;

    public AccountPropertyMap(Account account) {
        setAccount(account);
    }

    public AccountPropertyMap(Account account, String brand) {
        setAccount(account);
        setBrand(brand);
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean containsKey(Object key) {
        if (getAccount().getAccountProperties().keySet().contains(key)) {
            return true;
        }
        Account accountTemplate = getAccount().getAccountTemplate();
        if (accountTemplate != null && accountTemplate != getAccount()) {
            return accountTemplate.getAccountProperties().keySet().contains(key);
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        return null;
    }

    @Override
    public String get(Object key) {
        if (getBrand() != null) {
            String rslt = getProperty(key + "." + getBrand());
            if (rslt == null) {
                rslt = getProperty(key);
            }
            return rslt;
        }
        return getProperty(key);
    }

    private Account getAccount() {
        return account;
    }

    public AccountProperty getAccountProperty(Object key) {
        return getAccount().getAccountProperties().get(key);
    }

    public AccountProperty getAccountTemplateProperty(Object key) {
        Account accountTemplate = getAccount().getAccountTemplate();
        if (accountTemplate == null || accountTemplate == getAccount()) {
            return null;
        }
        return accountTemplate.getAccountProperties().get(key);
    }

    public String getAccountTemplatePropertyValue(Object key) {
        AccountProperty accountTemplateProperty = getAccountTemplateProperty(key);
        if (accountTemplateProperty == null) {
            return null;
        } else {
            return accountTemplateProperty.getPropertyValue();
        }
    }

    private String getBrand() {
        return brand;
    }

    public String getProperty(Object key) {
        AccountProperty accountProperty = getAccountProperty(key);
        if (accountProperty != null) {
            return accountProperty.getPropertyValue();
        }
        AccountProperty accountTemplateProperty = getAccountTemplateProperty(key);
        if (accountTemplateProperty != null) {
            return accountTemplateProperty.getPropertyValue();
        }
        // Finally, try to get it from the system properties
        return System.getProperty(key.toString());

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Set<String> keySet() {
        return null;
    }

    /**
     * Put or update a property in this account's list of properties.
     */
    @Override
    public String put(String key, String value) {
        AccountProperty accountProperty = getAccount().getAccountProperties().get(key);
        if (accountProperty != null) {
            String oldValue = accountProperty.getPropertyValue();
            accountProperty.setPropertyValue(value);
            return oldValue;
        }
        // Create a new property
        AccountProperty property = new AccountProperty();
        property.setAccount(getAccount());
        property.setPropertyName(key);
        property.setPropertyValue(value);
        getAccount().getAccountProperties().put(key, property);
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
    }

    @Override
    public String remove(Object key) {
        return null;
    }

    private void setAccount(Account account) {
        this.account = account;
    }

    private void setBrand(String brand) {
        this.brand = brand;
    }

    @Override
    public int size() {
        return getAccount().getAccountProperties().size();
    }

    @Override
    public Collection<String> values() {
        return null;
    }

}