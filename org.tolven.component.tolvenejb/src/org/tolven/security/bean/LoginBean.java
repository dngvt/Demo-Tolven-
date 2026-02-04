package org.tolven.security.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tolven.core.ActivationLocal;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.Sponsorship;
import org.tolven.core.entity.Status;
import org.tolven.core.entity.TolvenUser;
import org.tolven.security.LoginLocal;
import org.tolven.security.TolvenPerson;
@Stateless
@Local(LoginLocal.class)
public class LoginBean implements LoginLocal {
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private ActivationLocal activationBean;
    
    /**
     * Given the principal's name, get the TolvenUser object. the parameter must be converted to lower case to ensure we find a match.
     */
     public TolvenUser findUser( String principal ) {
    	 //Support both types of active status;    	
  	   String activeStatus = Status.ACTIVE.value();
	   String oldActiveStatus = Status.OLD_ACTIVE.value(); 
  		//Activating should be replaced by New
 		String activatingStatus = Status.fromValue("ACTIVATING").value();
 		String newStatus = Status.NEW.value();
 		String select = "SELECT DISTINCT u FROM TolvenUser u WHERE u.ldapUID = :principal " +
        "and ( u.status = '";
 		select += oldActiveStatus + "' or u.status = '" + activeStatus + "' or u.status = '" + newStatus + "' or u.status = '" + activatingStatus + "' or u.status = '" + "') ";
    	 Query query = em.createQuery(select);
        query.setParameter("principal", principal.toLowerCase());
        query.setMaxResults(2);
        List<TolvenUser> items = query.getResultList();
        if (items.size()==0) return null;
        return items.get(0);
     }

    public AccountUser findAccountUser(long accountUserId) {
        return em.find(AccountUser.class, accountUserId);
    }
    /*
    public void createActivateInvitation( ExpressionEvaluator ee ) throws Exception {
    	TolvenPerson tp = (TolvenPerson) ee.get("tp");
    	Date now = (Date) ee.get("now");
        ActivateInvitation detail = new ActivateInvitation( );
    	String rc = (String) ee.get("referenceCode");
//      detail.setExpirationTime( DatatypeFactory.newInstance().newXMLGregorianCalendar( t));
        detail.setPrincipal( tp.getUid());
        // Create Credentials in LDAP
        CertificateHelper helper = new CertificateHelper();
        helper.createCredentials(tp);
        detail.setReferenceCode( rc );
    	Invitation invitation = invitationBean.createInvitation(tp.getUid(), detail);
    	// Create an invitation
        invitation.setTitle( "New Account Activation");
        invitation.setDispatchAction("activate");
        invitation.setTemplate("/invitation/activate.jsf");
        invitation.setEmailFormat(tp.getEmailFormat());
        invitation.setAccount( null );	// No owner for registrations (could be sponsor)
        String expiration = propertiesBean.getProperty("tolven.register.expiration");
        if (expiration!=null) {
        	long elapsed = Long.parseLong(expiration)*1000;
        	if (elapsed > 0) {
        		invitation.setExpiration( new Date( now.getTime()+ (Long.parseLong(expiration)*1000) ) );
        	}
        }
        logger.info( "Queueing invitation to " + invitation.getTargetEmail());
        invitationBean.followup(invitation);
        // Once sent, the invitation state will be updated to reflect completion. Workflow, man. Workflow.
        invitationBean.queueInvitation( invitation );
    }
    */
    
    /**
     * Activate a user. Return false if invitation has expired or logged in user does not match target user of invitation.
     * While an invitation is marked as used, there is no harm in supplying a used invitation id. 
     * This can happen if the user decides to bookmark the URL from an invitation eMail.
     * @param user An existing Tolven user to be activated
     * @param invitation the invitation boolean indicating if this user has account administration permission
     * @param now A transactional now timestamp
     * @return A new AccountUser object
     */
//    @WebMethod(operationName="activate", action="urn:Activate")
    /*
    public boolean activate( String principal, long invitationId, Date now, PublicKey userPublicKey) {
    	Invitation invitation = invitationBean.findInvitation( invitationId );
        if (invitation==null) return false;
        // If the expiration date is specified but it has past, then fail.
        if (invitation.getExpiration()!=null && invitation.getExpiration().before(now)) {
            return false;
        }
        try {
            invitationBean.executeInvitation(invitation, now, userPublicKey);
        } catch (InvitationException e) {
            throw new RuntimeException("Could not activate invitation", e);
        }
        return true;
    }
    */
    /**
     * Activate a reserved user. The user must first be
     * reserved using {@link #reserveUser(TolvenPerson, Date)}. The effect of calling this method
     * is to create a new TolvenUSer entity in the database.
     * <p>Note: This function requires that the logged in user have the tolvenAdmin role.
     * In other words, the logged-in user cannot self-activate using this method.</p>
	 * <p>This process does not require an invitation.</p>
     * @param principal User's ID.
     * @param now The current time
     * @return The newly create (or updated) TolvenUser entity
     */
    public TolvenUser activateReservedUser( String principal, Date now) {
        TolvenUser user = findUser(principal);
        if(user == null) {
            user = createTolvenUser( principal, now );
        }
        user.setStatus(Status.ACTIVE.value());
        return user;
    }
    
    /**
     * Used for test, demo only. Activate the user without sending an email. The user id does not need to be a valid email address.
     * The demoUser flag is set in the user account.
     * @param tp A TolvenPerson object representing the LDAP attributes of this user (A TolvenPerson is a transient object)
     * @param now A transactional now timestamp
     * @return A new TolvenUser object
     */
    public TolvenUser activate(TolvenPerson tp, Date now) {
        TolvenUser user = findUser(tp.getUid());
        if (user == null) {
            user = createTolvenUser(tp.getUid(), now);
            String rc = tp.getReferenceCode();
            if (rc != null) {
                Sponsorship sponsorship = findSponsorship(rc);
                user.setSponsorship(sponsorship);
            }
            user.setDemoUser(true);
            user.setEmailFormat(tp.getEmailFormat());
            return user;
        } else {
            throw new RuntimeException(tp.getUid() + " is already activated");
        }
    }

    /**
     * Given what is suspected to be a valid sponsorship reference code, return the Sponsorship 
     * This method fails loudly (throws an object not found exception) if the reference code is not found.
     * This should be sufficient to rollback a transaction intended to create a new user 
     * with an invalid reference code.
     * @throws NoResultException if the referenceCode is not found
     * @param referenceCode
     * @return
     */
    public Sponsorship findSponsorship( String referenceCode ) {
    	Query q = em.createQuery("SELECT s FROM Sponsorship s WHERE s.referenceCode = :rc");
    	q.setParameter("rc", referenceCode);
    	Sponsorship s = (Sponsorship) q.getSingleResult();
    	return s;
    }

    /**
     * Create a new Tolven User
     * @param principal
     * @return new TolvenUser object properly initialized and persisted
     */
    public TolvenUser createTolvenUser( String principal, Date now ) {
        return activationBean.createTolvenUser(principal, now);
    }

    public void persistModifiedTolvenUser(TolvenUser user) {
        em.merge(user);
    }

    /**
     * Return true if the user with uid exists in the database
     */
    public boolean userExistsInDB(String uid) {
        return findUser(uid) != null;
    }

}
