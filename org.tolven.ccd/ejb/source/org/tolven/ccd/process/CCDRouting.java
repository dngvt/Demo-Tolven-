package org.tolven.ccd.process;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.tolven.app.entity.MenuData;
import org.tolven.ccd.CCDLocal;
import org.tolven.core.AccountDAOLocal;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountUser;
import org.tolven.doc.entity.DocBase;
import org.tolven.logging.TolvenLogger;
import org.tolven.process.ComputeBase;
import org.tolven.provider.ProviderLocal;
import org.tolven.provider.entity.Provider;
import org.tolven.security.key.DocumentSecretKey;
import org.tolven.trim.Act;
import org.tolven.trim.Compute.Property;
import org.tolven.trim.Party;
import org.w3c.dom.Element;

public class CCDRouting  extends ComputeBase {
	private static final String HL7_NAMESPACE = "urn:hl7-org:v3";
	private Element extract;
	private boolean enabled;
	
	private ProviderLocal providerBean;

	@EJB
    private AccountDAOLocal accountBean;
	
	@EJB
	private CCDLocal ccdLocal;
	
	
	
	public ProviderLocal getProviderBean() throws NamingException {
		if (providerBean==null) {
		    providerBean = (ProviderLocal) getCtx().lookup("java:global/tolven/tolvenEJB/ProviderBean!org.tolven.provider.ProviderLocal");
		}
		return providerBean;
	}
	/*
	private void submitDocument(String document) {
		AccountUser accountUser = getAccountUser();		
       
        long accountId = accountUser.getAccount().getId();
		long accountUserId = accountUser.getUser().getId();
		getDocumentBean().queueWSMessage(document.getBytes(), HL7_NAMESPACE, accountId, accountUserId);        
	}
	*/
	@Override
	public void compute() throws Exception {
		TolvenLogger.info( "Compute enabled=" + isEnabled(), CCDRouting.class);
		super.checkProperties();		
		if (isEnabled()) {
			//Generate the CCD
			//1. Set account properties
			// a. retrieve stored value ccd/xslt/WebViewLayout_CDA.xsl from property:tolven.xslt/2.16.840.1.113883.3.27.1776 .. for some reason for this it needs to be in: tolven.xslt/urn:hl7-org:v3
			////This just had to be set ahead of time, which has been taken care of
			//retrieveCCDXSLAccountProperty(CCD_XSLT_WEBVIEW);
			// b. retrieve stored value ccd/xslt/ccd.xsl from property: org.tolven.xslt.ccd
			//This just had to be set ahead of time, which has been taken care of
			
			//2. Grab current patient for context
			//String context = getPatientPath(); //i.e. echr:patient-1129000
			//String context = "echr:patient-1129000";
			Act act = getAct();
			String context = act.getParticipations().get(1).getRole().getId().getIIS().get(0).getExtension();
			//3. getXSLT and create a document out of this
			String xmloutput = getCcdLocal().getCCDXml(context);
			
			AccountUser accountUser = getAccountUser();		       
			DocBase xmlDoc = getDocumentBean().createNewDocument("text/xml", HL7_NAMESPACE, accountUser);
			String kbeKeyAlgorithm = getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_ALGORITHM_PROP);
		    int kbeKeyLength = Integer.parseInt(getPropertyBean().getProperty(DocumentSecretKey.DOC_KBE_KEY_LENGTH));
			xmlDoc.setAsEncryptedContentString(xmloutput, kbeKeyAlgorithm, kbeKeyLength);
			//4. attach this to the trim document
			getDocumentBean().createAttachment(getDocumentType().getDocument(), xmlDoc, "CCD", accountUser, getNow());
			
			//with xml output, Submit as a document with namespace urn:hl7-org:v3
			//submitDocument(xmloutput);
			
			// Disable the Compute since its job is done.
	    	for (Property property : getComputeElement().getProperties()) {
				if (property.getName().equals("enabled")) {
					property.setValue(Boolean.FALSE);
					//break;
				}
			}
		}
		//Populate receivers account id and name
	    populateReceivingAccountInfo();
	}
	
	private void populateReceivingAccountInfo() throws NamingException {
		// The Account of this user is intrinsic (safer than getting it from Trim)
		Account account = getAccountUser().getAccount();
		// Get the selected provider, if any
		String path = getTrim().getMessage().getReceiver().getAccountPath();
		if (path==null || path.length()==0) {
			return;
 		}
		MenuData md = getMenuBean().findMenuDataItem(account.getId(), path);
		if (md==null) {
			return;
			//throw new RuntimeException( "Receiver path [" + path + "] not found in ShareSetup for account " + getAccountUser().getAccount().getId() );
		}
		Account destinationAccount;
		Long providerId;
		Provider provider = null;
		if (md.getMenuStructure().getPath().endsWith("accountShares")) {
			Long accountId = (Long) md.getField("AccountId");
			if (accountId==null) {
				throw new RuntimeException( "Missing accountId in accountShares " + md.getPath());
			}
			destinationAccount = this.getTrimBean().findAccount(accountId);
			providerId = null;
		} else {
			providerId = (Long) md.getField("providerId");
			if (providerId==null) {
				throw new RuntimeException( "Missing providerId in provider placeholder " + md.getPath());
			}
			provider = getProviderBean().findProvider(providerId);
			destinationAccount = provider.getOwnerAccount();
		}
		// Set destination accountId in message block
		Party party = getTrim().getMessage().getReceiver();
		party.setAccountId(Long.toString(destinationAccount.getId()));
		party.setProviderId(providerId);
		if (provider != null)
			party.setProviderName(provider.getName());
		party.setAccountName(destinationAccount.getTitle());
	}
	
	public Element getExtract() {
		return extract;
	}
	public void setExtract(Element extract) {
		this.extract = extract;
	}

	 protected AccountDAOLocal getAccountBean() {
        if (accountBean == null) {
            String jndiName = "java:app/tolvenEJB/AccountDAOBean!org.tolven.core.AccountDAOLocal";
            try {
                InitialContext ctx = new InitialContext();
                accountBean = (AccountDAOLocal) ctx.lookup(jndiName);
            } catch (Exception ex) {
                throw new RuntimeException("Could not lookup " + jndiName);
            }
        }
        return accountBean;
    }
	 
	protected CCDLocal  getCcdLocal() {
		 if (ccdLocal == null) {
			 String jndiName = "java:app/tolvenEJB/CCDBean!org.tolven.ccd.CCDLocal";
			 try {
				 InitialContext ctx = new InitialContext();
				 ccdLocal = (CCDLocal) ctx.lookup(jndiName);
			 }catch (Exception ex) {
				 throw new RuntimeException("Could not lookup " + jndiName);
			 }
		 }
		 return ccdLocal;
	}
 
	public void setCcdLocal(CCDLocal ccdLocal) {
		this.ccdLocal = ccdLocal;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}

