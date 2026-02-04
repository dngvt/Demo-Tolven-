package org.tolven.client;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;



public  class TolvenClientEx extends TolvenClient {
	
	//private Properties contextProperties;
	private InitialContext ctx;
	
	public void setContextProperties() {
        this.setContextProperties(new Properties());       
        this.getContextProperties().put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory"); 
        this.getContextProperties().put(Context.PROVIDER_URL, "jnp://localhost:1099"); 
        this.getContextProperties().put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");

    }
	
	public InitialContext getCtx() {
        if (ctx == null) {
            if (getContextProperties() == null) {
            	//setContextProperties(loadProperties(new File(DEFAULT_PROPERTIES)));
                setContextProperties();
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
}