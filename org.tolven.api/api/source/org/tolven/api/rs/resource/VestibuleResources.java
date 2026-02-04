package org.tolven.api.rs.resource;

import java.security.PublicKey;
import java.util.List;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.tolven.api.rs.XAccountFactory;
import org.tolven.api.rs.accountuser.Accounts;
import org.tolven.api.security.GeneralSecurityFilter;
import org.tolven.core.AccountDAOLocal;
import org.tolven.core.ActivationLocal;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountType;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.TolvenUser;
import org.tolven.gen.model.GenSource;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;

import com.google.gson.Gson;

@Path("vestibule")
public class VestibuleResources {

    public static final String DEFAULT_ACCOUNT_HOME = "/private/application.jsf";

    @EJB
    private ActivationLocal activationBean;

    @EJB
    private AccountDAOLocal accountBean;

    private Logger logger = Logger.getLogger(VestibuleResources.class);

    public VestibuleResources() {
    }

    /**
     * Create an account.
     * 
     * @param type
     * @param title
     * @param timezone
     * @return
     */
    @Path("createAccount")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createAccount(
            @FormParam("type") String type,
            @FormParam("title") String title,
            @FormParam("timezone") String timezone) {
        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
        TolvenUser user = TolvenRequest.getInstance().getTolvenUser();
        if (user == null) {
            return Response.status(Status.NOT_FOUND).entity("TolvenUser not found").build();
        }
        if (type == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Account type param is required").build();
        }
        if (title == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Account title param is required").build();
        }
        AccountType accountType = getAccountBean().findAccountTypebyKnownType(type);
        if (accountType == null) {
            return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Unknown account type: " + type).build();
        }
        Account account = null;
        try {
            account = getAccountBean().createAccount2(title, timezone, accountType);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        logger.info("Created account: " + account.getId() + ", acct type " + accountType.getKnownType());
        // Note, the user automatically gets administrator permission because they are the only user on that new account.
        PublicKey userPublicKey = sessionWrapper.getUserPublicKey();
        getAccountBean().addAccountUser(account, user, TolvenRequest.getInstance().getNow(), true, userPublicKey);
        logger.info("Added first admin to: " + account.getId() + ", acct type " + accountType.getKnownType());
        return Response.ok().type(MediaType.TEXT_PLAIN).entity(String.valueOf(account.getId())).build();
    }

    private AccountDAOLocal getAccountBean() {
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
    
    private ActivationLocal getActivationBean() {
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

    /**
     * Get a list of accounts that the current user is allowed to select
     * @return
     */
    @Path("accountList")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getUserAccountListXML() throws Exception {
        TolvenUser user = TolvenRequest.getInstance().getTolvenUser();
        if (user == null) {
            return Response.status(Status.NOT_FOUND).entity("TolvenUser not found").build();
        }
        List<AccountUser> accountUsers = getActivationBean().findUserAccounts(user);
        Accounts uas = XAccountFactory.createAccounts(accountUsers, user, TolvenRequest.getInstance().getNow());
        Response response = Response.ok().entity(uas).build();
        return response;
    }

    /**
     * Get a list of accounts that the current user is allowed to select
     * @return
     */
    @Path("accountListJSON")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserAccountListJSON() throws Exception {
        TolvenUser user = TolvenRequest.getInstance().getTolvenUser();
        if (user == null) {
            return Response.status(Status.NOT_FOUND).entity("TolvenUser not found").build();
        }
        List<AccountUser> accountUsers = getActivationBean().findUserAccounts(user);
        Accounts uas = XAccountFactory.createAccounts(accountUsers, user, TolvenRequest.getInstance().getNow());
        Gson gson = new Gson();        
        Response response = Response.ok().entity(gson.toJson(uas)).build();
        return response;
    }

    /**
     * Select an account
     * @param account
     */
    @Path("selectAccount")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response selectAccount(@QueryParam("accountId") String accountId) {
        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
        // Proposed accountId is detected by filters and should be set by this point
        String accountUserIdString = (String) sessionWrapper.getAttribute(GeneralSecurityFilter.ACCOUNTUSER_ID);
        if (accountUserIdString == null) {
            return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN).entity("Account not found: " + accountId).build();
        }
        return Response.ok().type(MediaType.TEXT_PLAIN).entity(accountId).build();
    }
    
}
