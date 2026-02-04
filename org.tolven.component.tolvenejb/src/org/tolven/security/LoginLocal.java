package org.tolven.security;

import java.security.GeneralSecurityException;
import java.util.Date;

import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.TolvenUser;
/**
 * Services that provide LDAP-related user management, including registration.
 * @author John Churin
 *
 */
public interface LoginLocal {

    /**
     * Given the principal's name, get the TolvenUser.
     */
     public TolvenUser findUser( String principal );

     /**
      * Return an AccountUser given an AccountUserId
      * @param anAccountUserId
      * @return
      */
     public AccountUser findAccountUser(long accountUserId);
     
     public TolvenUser activate(TolvenPerson tp, Date now);

     /**
      * Activate a user. At this point, a user will have an entry in LDAP, they will have received an invitation
      * via email and now this method will create their Tolven user account.
      */
     //public boolean activate( String principal, long invitationId, Date now, PublicKey userPublicKey) throws InvitationException;
     
     /**
      * Create a TolvenUser only, because the associated LDAP user is created independently
      * @param principal
      * @param now
      * @return
      */
     public TolvenUser createTolvenUser( String principal, Date now );

     /**
      * Persist a modified TolvenUser
      * @return
      */
     public void persistModifiedTolvenUser(TolvenUser user);
    
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
      * @throws GeneralSecurityException
      */
     public TolvenUser activateReservedUser( String principal, Date now) throws GeneralSecurityException;
     
}
