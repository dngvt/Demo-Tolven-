package org.tolven.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.TolvenUser;

public abstract class TolvenClient {
    private static final String JAAS_LOGIN_CONFIG_PROPERTY = "java.security.auth.login.config";
    public static final String TOLVEN_LOGINCONTEXT = "tolvenLDAP";

    private Properties contextProperties;
    public LoginContext lc;

    protected AccountUser accountUser;
    protected String uid;
    private InitialContext ctx;
    private UserTransaction ut;
    protected static String DEFAULT_PROPERTIES = "tolvenClient-default.properties";
    private Logger logger = Logger.getLogger(TolvenClient.class);

    /**
     * Load properties from filename CONFIG_PROPERTIES_FILENAME located in the configDir
     * @param configDir
     * @throws IOException
     */
    public TolvenClient() {
    }

    public TolvenClient(String configDir) {
    }

    public Properties getContextProperties() {
        return contextProperties;
    }

    public void setContextProperties(Properties contextProperties) {
        this.contextProperties = contextProperties;
    }

    public InitialContext getCtx() {
        if (ctx == null) {
            if (getContextProperties() == null) {
                setContextProperties(loadProperties(new File(DEFAULT_PROPERTIES)));
                if (getContextProperties() == null) {
                    throw new RuntimeException(getClass() + " does not have initial context properties");
                }
            }
            try {
                ctx = new InitialContext(getContextProperties());
            } catch (NamingException ex) {
                throw new RuntimeException("Could not create initial context using supplied context properties", ex);
            }
        }
        return ctx;
    }

    public void login(final String userId, final char[] password, String authURL, Properties jndiProperties) {
        for (Object obj : jndiProperties.keySet()) {
            String key = (String) obj;
            String value = jndiProperties.getProperty(key);
            logger.info("jndi: " + key + "=" + value);
        }
        setContextProperties(jndiProperties);
        System.setProperty(JAAS_LOGIN_CONFIG_PROPERTY, authURL);
        //System.setProperty(Context.PROVIDER_URL, providerURL);
        try {
            lc = new LoginContext(TOLVEN_LOGINCONTEXT, new CallbackHandler() {
                public void handle(Callback[] callbacks) {
                    int len = callbacks.length;
                    Callback cb;
                    for (int i = 0; i < len; i++) {
                        cb = callbacks[i];
                        if (cb instanceof NameCallback) {
                            NameCallback ncb = (NameCallback) cb;
                            ncb.setName(userId);
                        } else if (cb instanceof PasswordCallback) {
                            PasswordCallback pcb = (PasswordCallback) cb;
                            pcb.setPassword(password);
                        }
                    }
                }
            });
            lc.login();
        } catch (LoginException ex) {
            throw new RuntimeException("Failed to login: " + userId + " with authorization file " + authURL, ex);
        }
    }

    /**
     * Login a user.
     * @param uid
     * @param password
     * @return The TolvenUser object for the logged in user.
     * @throws GeneralSecurityException 
     * @throws Exception
     */
    public TolvenUser login(String uid, String password) {
        throw new RuntimeException("Not Supported");
    }

    public void logout() {
        if (lc != null) {
            try {
                lc.logout();
            } catch (LoginException ex) {
                throw new RuntimeException("Failed to logout: " + uid, ex);
            }
            lc = null;
        }
    }
/*/
    public AccountUser loginToAccount(long accountId) {
        if (uid == null)
            throw new IllegalStateException("UID must be set before connecting to an account");
        accountUser = getAccountBean().findAccountUser(uid, accountId);
        return accountUser;
    }
*/
    public void beginTransaction() {
        try {
            ut = (UserTransaction) getCtx().lookup("UserTransaction");
            ut.begin();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to begin UserTransactiion", ex);
        }
    }

    public void commitTransaction() {
        if (ut == null) {
            throw new RuntimeException("User Transaction is null");
        }
        try {
            ut.commit();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to commit UserTransactiion", ex);
        }
    }

    public void rollbackTransaction() {
        try {
            if (ut != null) {
                ut.rollback();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to rollback UserTransactiion", ex);
        }
    }

    protected static class UsernamePasswordHandler implements CallbackHandler {
        String username;
        char[] password;

        public UsernamePasswordHandler(String username, char[] password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            int len = callbacks.length;
            Callback cb;
            for (int i = 0; i < len; i++) {
                cb = callbacks[i];
                if (cb instanceof NameCallback) {
                    NameCallback ncb = (NameCallback) cb;
                    ncb.setName(username);
                } else if (cb instanceof PasswordCallback) {
                    PasswordCallback pcb = (PasswordCallback) cb;
                    pcb.setPassword(password);
                } else {
                    throw new UnsupportedCallbackException(cb, "Unknown callback request");
                }
            }
        }
    }

    /**
     * This will be null if loginToAccount has not been called 
     * @return
     */
    public AccountUser getAccountUser() {
        return accountUser;
    }

    public static void expandVariables(Properties properties) {
        Enumeration<Object> keys = properties.keys();
        String key = null;
        String value = null;
        String expandedValue = null;
        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement();
            value = (String) properties.get(key);
            do {
                expandedValue = getVariable(value, properties);
                value = expandedValue;
            } while (expandedValue.indexOf("${") != -1);
            properties.put(key, expandedValue);
        }
    }

    public static String getVariable(String propertyValue, Properties properties) {
        if (propertyValue == null)
            return null;
        StringTokenizer tokenizer = new StringTokenizer(propertyValue, "${");
        String token = null;
        String variableKey = null;
        String variableValue = null;
        StringBuffer buff = new StringBuffer();
        int lastIndex = 0;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            if (token.indexOf("}") == -1) {
                buff.append(token);
            } else {
                lastIndex = token.lastIndexOf('}');
                variableKey = token.substring(0, lastIndex);
                variableValue = (String) properties.get(variableKey);
                if (variableValue == null) {
                    variableValue = System.getProperty(variableKey);
                    if (variableValue == null)
                        throw new RuntimeException("Properties file contains an undefined property: ${" + variableKey + "}");
                }
                buff.append(variableValue);
                buff.append(token.substring(lastIndex + 1));
            }
        }
        return buff.toString();
    }

    private Properties loadProperties(File propertiesFile) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(propertiesFile);
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException ex) {
            throw new RuntimeException("Could not load properties file: " + propertiesFile.getPath(), ex);
        }
    }

}
