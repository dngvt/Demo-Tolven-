/*
 * Copyright (C) 2009 Tolven Inc

 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;  
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 *
 * Contact: info@tolvenhealth.com 
 *
 * @author Joseph Isaac
 */
package org.tolven.config.api;

public class TolvenConstants {

    public static final String ACCOUNT = "account";
    public static final String ACCOUNT_ID = "accountId";
    public static final String ACCOUNTUSER = "accountUser";
    public static final String ACCOUNTUSER_ID = "accountUserId";
    public static final String INVITATION_ID = "invitationId";
    public static final String PROPOSED_ACCOUNT_ID = "proposedAccountId";
    public static final String REMEMBER_DEFAULT_ACCOUNT = "rememberDefaultAccount"; // true | false
    public static final String SWITCH_ACCOUNT = "switchAccount"; // true | false
    public static final String TIME_NOW = "tolvenNow";
    public static final String TOLVEN_REDIRECT = "tolvenRedirect";
    public static final String TOLVEN_RESOURCEBUNDLE = "tolvenResourceBundle";
    public static final String TOLVENUSER = "tolvenUser";
    public static final String TOLVENUSER_ID = "TolvenUserId";
    public static final String USER_CONTEXT = "userContext";
    public static final String VESTIBULE = "vestibule";

    public static final String ACCOUNT_USER_KBE_KEY_ALGORITHM_PROP = "tolven.security.accountUser.kbeKeyAlgorithm";
    public static final String ACCOUNT_USER_KBE_KEY_LENGTH_PROP = "tolven.security.accountUser.kbeKeyLength";
    public static final String ACCOUNT_PRIVATE_KEY_ALGORITHM_PROP = "tolven.security.account.privateKeyAlgorithm";
    public static final String ACCOUNT_PRIVATE_KEY_LENGTH_PROP = "tolven.security.account.keyLength";
    public static final String DOC_KBE_KEY_ALGORITHM_PROP = "tolven.security.doc.kbeKeyAlgorithm";
    public static final String DOC_KBE_KEY_LENGTH_PROP = "tolven.security.doc.kbeKeyLength";
    public static final String DOC_SIGNATURE_ALGORITHM_PROP = "tolven.security.doc.signatureAlgorithm";
    public static final String PBE_KEY_ALGORITHM_PROP = "tolven.security.user.pbeKeyAlgorithm";
    public static final String SESSION_SECRET_KEY_ALGORITHM_PROP = "tolven.security.session.kbeKeyAlgorithm";
    public static final String SESSION_SECRETY_KEY_LENGTH_PROP = "tolven.security.session.kbeKeyLength";
    public static final String SSL_KEY_ALGORITHM_PROP = "tolven.security.ssl.keyAlgorithm";
    public static final String SSL_KEY_LENGTH_PROP = "tolven.security.ssl.keyLength";
    public static final String USER_PRIVATE_KEY_ALGORITHM_PROP = "tolven.security.user.privateKeyAlgorithm";
    public static final String USER_PRIVATE_KEY_LENGTH_PROP = "tolven.security.user.privateKeyLength";
    public static final String USER_PASSWORD_SALT_LENGTH_PROP = "tolven.security.user.passwordSaltLength";
    public static final String USER_PASSWORD_ITERATION_COUNT_PROP = "tolven.security.user.passwordIterationCount";

    public static final String TOLVEN_CREDENTIAL_FORMAT_JKS = "jks";
    public static final String TOLVEN_CREDENTIAL_FORMAT_PKCS12 = "pkcs12";

    public static final String OPENJPA_CONTEXT = "openjpaContext";

    public static final String PERSISTENCEUNITCONTEXT = "persistenceUnitContext";

    public static final String DB_CODE_PROP = "db.code";
    public static final String DBCONTEXT = "db";
    public static final String DBSCHEMAS = "dbSchemas";
    public static final String DBINDEXES = "dbIndexes";

    public static final String DEFAULT_SECURITY_CONTEXT_PROP = "_securityContextDefault";
    public static final String DEFAULT_QUEUE_SECURITY_CONTEXT_PROP = "_queueSecurityContextDefault";
    public static final String DEFAULT_WEB_SECURITY_CONTEXT_PROP = "_webSecurityContextDefault";
    public static final String DEFAULT_WS_SECURITY_CONTEXT_PROP = "_wsSecurityContextDefault";

    public static final String CCR_NAMESPACE = "urn:astm-org:CCR";
    public static final String TRIM_NAMESPACE = "urn:tolven-org:trim:4.0";

    public static final String LOGOUT = "logout";

    public static final String SECURITYCONTEXTS = "securityContexts";
    public static final String SSOCONTEXT = "ssoContext";
    public static final String WEBCONTEXTS = "webContexts";
    public static final String WEBCONTEXT_ID = "webContextId";
    public static final String GATEKEEPERHTML_WEBCONTEXT = "gatekeeperhtml";
    public static final String GATEKEEPERRS_WEBCONTEXT = "gatekeeperrs";
    public static final String GATEKEEPERWS_WEBCONTEXT = "gatekeeperws";
    public static final String TOLVENHTML_WEBCONTEXT = "tolvenweb";
    public static final String TOLVENRS_WEBCONTEXT = "tolvenrs";
    public static final String TOLVENWS_WEBCONTEXT = "tolvenws";
    public static final String BUILTIN_CHAINFILTERS = "chainFilters.builtin";
    public static final String SHARED_CHAINFILTERS = "chainFilters.shared";
    public static final String QUEUECONTEXTS = "queueContexts";
    public static final String QUEUEUSERS = "queueUsers";
    public static final String ADMINAPP_QUEUE_ID = "adminApp";
    public static final String GENERATOR_QUEUE_ID = "generator";
    public static final String RULE_QUEUE_ID = "rule";

    public static final String URL_AUTHORIZATIONS_DEFAULT = "urlAuthorizations.default";

    public static final String REALMS = "realms";
    public static final String REALM = "realm";
    public static final String BASE_ROLES_NAME = "baseRolesName";
    public static final String ROLE_DN_PREFIX = "roleDNPrefix";
    public static final String DEFAULT_TOLVEN_REALM = "tolven_REALM";

    public static final String REQUIRED_ROLES = "requiredRoles";

    public static final String TESTCONTEXT = "testContext";

    public static final String APPLOADERS = "appLoaders";

    public static final String PLUGINSXML_FILENAME = "plugins.xml";

    public static final String PKI_CONTEXT = "pkiContext";

    public static final String SESSION_LOGIN_PATH = "sessionLoginPath";
    public static final String SESSION_GATEKEEPER_HOME_PATH = "sessionGatekeeperHomePath";
    public static final String SESSION_LOGIN_FAILURE_MESSAGE = "sessionLoginFailureMessage";

    public static final String USER_ROLES_PROP = "org.tolven.session.attribute.roles";
    public static final String ADMIN_ROLE_PROP = "adminRole";
    
}
