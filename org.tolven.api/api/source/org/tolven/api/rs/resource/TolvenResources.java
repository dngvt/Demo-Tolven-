package org.tolven.api.rs.resource;

import java.security.PrivateKey;

import javax.ejb.EJB;
import javax.naming.InitialContext;

import org.tolven.app.AppResolverLocal;
import org.tolven.app.CreatorLocal;
import org.tolven.app.DataExtractLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.TrimCreatorLocal;
import org.tolven.core.ActivationLocal;
import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.doc.DocProtectionLocal;
import org.tolven.doc.DocumentLocal;
import org.tolven.msg.ProcessLocal;
import org.tolven.msg.TolvenMessageSchedulerLocal;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;

public class TolvenResources {
	@EJB
	private ActivationLocal activationBean;
	@EJB
	private AppResolverLocal appResolver;
	    
	@EJB
	private MenuLocal menuBean;
	
	@EJB
	private DataExtractLocal dataExtractBean;

	@EJB
	private DocProtectionLocal docProtectionBean;
	    
	@EJB
	private DocumentLocal documentBean;

	@EJB
	 private ProcessLocal processBean;

	@EJB
	private TolvenPropertiesLocal propertyBean;

	private @EJB
	TolvenMessageSchedulerLocal tmSchedulerBean;
	    
	@EJB
	private TrimCreatorLocal trimCreatorBean;
	    
	@EJB
	private CreatorLocal creatorBean;
    
    protected CreatorLocal getCreatorBean() {
        if (creatorBean == null) {
            String jndiName = "java:app/tolvenEJB/CreatorBean!org.tolven.app.CreatorLocal";
            try {
                InitialContext ctx = new InitialContext();
                creatorBean = (CreatorLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return creatorBean;
    }
    
    protected ActivationLocal getActivationBean() {
        if (activationBean == null) {
            String jndiName = "java:app/tolvenEJB/ActivationBean!org.tolven.core.ActivationLocal";
            try {
                InitialContext ctx = new InitialContext();
                activationBean = (ActivationLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return activationBean;
    }
    protected DataExtractLocal getDataExtractBean() {
        if (dataExtractBean == null) {
            String jndiName = "java:app/tolvenEJB/DataExtractBean!org.tolven.app.DataExtractLocal";
            try {
                InitialContext ctx = new InitialContext();
                dataExtractBean = (DataExtractLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return dataExtractBean;
    }
    
    protected AppResolverLocal  getAppResolver() {
        if (appResolver == null) {
            String jndiName = "java:app/tolvenEJB/AppResolverBean!org.tolven.app.AppResolverLocal";
            try {
                InitialContext ctx = new InitialContext();
                appResolver = (AppResolverLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return appResolver;
    }

    protected MenuLocal getMenuBean() {
        if (menuBean == null) {
            String jndiName = "java:app/tolvenEJB/MenuBean!org.tolven.app.MenuLocal";
            try {
                InitialContext ctx = new InitialContext();
                menuBean = (MenuLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return menuBean;
    }
    protected TrimCreatorLocal getTrimCreatorBean() {
    	if (trimCreatorBean == null) {
            String jndiName = "java:app/tolvenEJB/TrimCreatorBean!org.tolven.app.TrimCreatorLocal";
            try {
                InitialContext ctx = new InitialContext();
                trimCreatorBean = (TrimCreatorLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
    	return trimCreatorBean;
	}

    protected void setTrimCreatorBean(TrimCreatorLocal trimCreatorBean) {
		this.trimCreatorBean = trimCreatorBean;
	}

    
    protected DocProtectionLocal getDocProtectionBean() {
    	if (docProtectionBean == null) {
            String jndiName = "java:app/tolvenEJB/DocProtectionBean!org.tolven.doc.DocProtectionLocal";
            try {
                InitialContext ctx = new InitialContext();
                docProtectionBean = (DocProtectionLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
  		return docProtectionBean;
  	}

    protected DocumentLocal getDocumentBean() {
        if (documentBean == null) {
            String jndiName = "java:app/tolvenEJB/DocumentBean!org.tolven.doc.DocumentLocal";
            try {
                InitialContext ctx = new InitialContext();
                documentBean = (DocumentLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return documentBean;
    }

    protected TolvenMessageSchedulerLocal getTolvenMessageSchedulerBean() {
        if (tmSchedulerBean == null) {
            String jndiName = "java:app/tolvenEJB/TolvenMessageScheduler!org.tolven.msg.TolvenMessageSchedulerLocal";
            try {
                InitialContext ctx = new InitialContext();
                tmSchedulerBean = (TolvenMessageSchedulerLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return tmSchedulerBean;
    }
    protected ProcessLocal getProcessLocal() {
  		if (processBean == null) {
            String jndiName = "java:app/tolvenEJB/ProcessBean!org.tolven.msg.ProcessLocal";
            try {
                InitialContext ctx = new InitialContext();
                processBean = (ProcessLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
  		return processBean;
  	}

    protected TolvenPropertiesLocal getPropertyBean() {
  		if (propertyBean == null) {
            String jndiName = "java:app/tolvenEJB/TolvenProperties!org.tolven.core.TolvenPropertiesLocal";
            try {
                InitialContext ctx = new InitialContext();
                propertyBean = (TolvenPropertiesLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
  		return propertyBean;
  	}
    
    protected PrivateKey getUserPrivateKey(){
		String keyAlgorithm = getPropertyBean().getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
	    TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
	    PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);
	    return userPrivateKey;
	}
}
