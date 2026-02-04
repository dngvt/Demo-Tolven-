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
 * @version $Id: UserPlugin.java 1870 2011-07-27 07:21:54Z joe.isaac $
 */
package org.tolven.user;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.tolven.plugin.TolvenCommandPlugin;

/**
 * This plugin creates users via RESTful API
 * 
 * @author Joseph Isaac
 *
 */
public class UserPlugin extends TolvenCommandPlugin {

    public static final String CMD_ADD_ROLE_OPTION = "addRole";
    public static final String CMD_COUNTRY_OPTION = "country";
    public static final String CMD_CREATE_OPTION = "create";
    public static final String CMD_CREATE_ROLE_OPTION = "createRole";
    public static final String CMD_EMAILS_OPTION = "emails";
    public static final String CMD_GIVENNAME_OPTION = "givenName";
    public static final String CMD_ORGANIZATION_OPTION = "organization";
    public static final String CMD_ORGANIZATION_UNIT_OPTION = "organizationUnit";
    public static final String CMD_REALM_OPTION = "realm";
    public static final String CMD_REMOVE_ROLE_OPTION = "removeRole";
    public static final String CMD_ROLE_OPTION = "role";
    public static final String CMD_STATE_OR_PROVINCE_OPTION = "stateOrProvince";
    public static final String CMD_SURNAME_OPTION = "surname";
    public static final String CMD_UID_OPTION = "uid";
    public static final String CMD_USERPASSWORD_OPTION = "userPassword";
    public static final String CMD_USERPCKS12_OPTION = "userPKCS12";
    public static final String TOLVEN_CREDENTIAL_FORMAT_PKCS12 = "pkcs12";

    private void addRole(String uid, String realm, CommandLine commandLine) {
        String userId = getTolvenConfigWrapper().getAdminId();
        char[] userIdPassword = getTolvenConfigWrapper().getAdminPassword();
        String role = commandLine.getOptionValue(CMD_ROLE_OPTION);
        UserLoader userLoader = new DefaultUserLoader(userId, userIdPassword);
        userLoader.addRole(role, uid, realm);
    }

    private void createRole(CommandLine commandLine) {
        String userId = getTolvenConfigWrapper().getAdminId();
        char[] userIdPassword = getTolvenConfigWrapper().getAdminPassword();
        String role = commandLine.getOptionValue(CMD_ROLE_OPTION);
        UserLoader userLoader = new DefaultUserLoader(userId, userIdPassword);
        userLoader.createRole(role);
    }

    private void createUser(String uid, String realm, CommandLine commandLine) {
        Properties props = new Properties();
        props.setProperty("uid", uid);
        props.setProperty("realm", realm);
        String givenName = commandLine.getOptionValue(CMD_GIVENNAME_OPTION);
        if(givenName == null) {
            throw new RuntimeException(CMD_GIVENNAME_OPTION + " option is missing");
        }
        props.setProperty("givenName", givenName);
        String surname = commandLine.getOptionValue(CMD_SURNAME_OPTION);
        if(surname == null) {
            throw new RuntimeException(CMD_SURNAME_OPTION + " option is missing");
        }
        props.setProperty("surname", surname);
        String organizationUnit = commandLine.getOptionValue(CMD_ORGANIZATION_UNIT_OPTION);
        if (organizationUnit != null) {
            props.setProperty("organizationUnit", organizationUnit);
        }
        String organization = commandLine.getOptionValue(CMD_ORGANIZATION_OPTION);
        if (organization != null) {
            props.setProperty("organization", organization);
        }
        String stateOrProvince = commandLine.getOptionValue(CMD_STATE_OR_PROVINCE_OPTION);
        if (stateOrProvince != null) {
            props.setProperty("stateOrProvince", stateOrProvince);
        }
        String country = commandLine.getOptionValue(CMD_COUNTRY_OPTION);
        if (country != null) {
            props.setProperty("country", country);
        }
        String emails = commandLine.getOptionValue(CMD_EMAILS_OPTION);
        if (emails != null) {
            props.setProperty("emails", emails);
        }
        String userPKCS12 = commandLine.getOptionValue(CMD_USERPCKS12_OPTION);
        String keyStorePassword = commandLine.getOptionValue(CMD_USERPASSWORD_OPTION);
        if(keyStorePassword == null) {
            throw new RuntimeException(CMD_USERPASSWORD_OPTION + " is missing");
        }
        char[] uidPassword = keyStorePassword.toCharArray();
        props.setProperty("uidPassword", new String(uidPassword));
        if (userPKCS12 != null) {
            String encodedUserPkCS12 = getBase64EnocdedKeyStore(userPKCS12, uidPassword);
            props.setProperty("userPKCS12", encodedUserPkCS12);
        }
        String userId = getTolvenConfigWrapper().getAdminId();
        props.setProperty("userId", new String(userId));
        char[] userIdPassword = getTolvenConfigWrapper().getAdminPassword();
        props.setProperty("userIdPassword", new String(userIdPassword));
        UserLoader userLoader = new DefaultUserLoader(userId, userIdPassword);
        userLoader.createUser(uid, props);
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
    }

    @Override
    public void execute(String[] args) throws Exception {
        CommandLine commandLine = getCommandLine(args);
        String uid = commandLine.getOptionValue(CMD_UID_OPTION);
        String realm = commandLine.getOptionValue(CMD_REALM_OPTION);
        if (commandLine.hasOption(CMD_CREATE_OPTION)) {
            createUser(uid, realm, commandLine);
        } else if(commandLine.hasOption(CMD_CREATE_ROLE_OPTION)) {
            createRole(commandLine);
        } else if(commandLine.hasOption(CMD_ADD_ROLE_OPTION)) {
            addRole(uid, realm, commandLine);
        } else if(commandLine.hasOption(CMD_REMOVE_ROLE_OPTION)) {
            removeRole(uid, realm, commandLine);
        }
    }

    private String getBase64EnocdedKeyStore(String filename, char[] password) {
        byte[] bytes = null;
        try {
            bytes = FileUtils.readFileToByteArray(new File(filename));
        } catch (Exception e) {
            throw new RuntimeException("Could not load keyStore bytes from: " + filename);
        }
        vaildateKeyStore(bytes, password);
        try {
            String encodedKeyStore = new String(Base64.encodeBase64(bytes), "UTF-8");
            return encodedKeyStore;
        } catch (Exception ex) {
            throw new RuntimeException("Could not base64 encode keyStore", ex);
        }
    }

    private CommandLine getCommandLine(String[] args) {
        GnuParser parser = new GnuParser();
        try {
            return parser.parse(getCommandOptions(), args, true);
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(getClass().getName(), getCommandOptions());
            throw new RuntimeException("Could not parse command line for: " + getClass().getName(), ex);
        }
    }

    private Options getCommandOptions() {
        Options cmdLineOptions = new Options();
        OptionGroup optionGroup = new OptionGroup();
        Option createOption = new Option(CMD_CREATE_OPTION, CMD_CREATE_OPTION, false, "\"" + CMD_CREATE_OPTION + "\"");
        optionGroup.addOption(createOption);
        Option createRoleOption = new Option(CMD_CREATE_ROLE_OPTION, CMD_CREATE_ROLE_OPTION, false, "\"" + CMD_CREATE_ROLE_OPTION + "\"");
        optionGroup.addOption(createRoleOption);
        Option addRole = new Option(CMD_ADD_ROLE_OPTION, CMD_ADD_ROLE_OPTION, false, "\"" + CMD_ADD_ROLE_OPTION + "\"");
        optionGroup.addOption(addRole);
        Option removeRole = new Option(CMD_REMOVE_ROLE_OPTION, CMD_REMOVE_ROLE_OPTION, false, "\"" + CMD_REMOVE_ROLE_OPTION + "\"");
        optionGroup.addOption(removeRole);
        optionGroup.setRequired(true);
        cmdLineOptions.addOptionGroup(optionGroup);
        Option uidOption = new Option(CMD_UID_OPTION, CMD_UID_OPTION, true, "\"" + CMD_UID_OPTION + "\"");
        cmdLineOptions.addOption(uidOption);
        Option realmOption = new Option(CMD_REALM_OPTION, CMD_REALM_OPTION, true, "\"" + CMD_REALM_OPTION + "\"");
        realmOption.setRequired(true);
        cmdLineOptions.addOption(realmOption);
        Option givenNameOption = new Option(CMD_GIVENNAME_OPTION, CMD_GIVENNAME_OPTION, true, "\"" + CMD_GIVENNAME_OPTION + "\"");
        cmdLineOptions.addOption(givenNameOption);
        Option surnameOption = new Option(CMD_SURNAME_OPTION, CMD_SURNAME_OPTION, true, "\"" + CMD_SURNAME_OPTION + "\"");
        cmdLineOptions.addOption(surnameOption);
        Option keystorePasswordOption = new Option(CMD_USERPASSWORD_OPTION, CMD_USERPASSWORD_OPTION, true, "\"" + CMD_USERPASSWORD_OPTION + "\"");
        cmdLineOptions.addOption(keystorePasswordOption);
        Option organizationUnitOption = new Option(CMD_ORGANIZATION_UNIT_OPTION, CMD_ORGANIZATION_UNIT_OPTION, true, "\"" + CMD_ORGANIZATION_UNIT_OPTION + "\"");
        cmdLineOptions.addOption(organizationUnitOption);
        Option organizationOption = new Option(CMD_ORGANIZATION_OPTION, CMD_ORGANIZATION_OPTION, true, "\"" + CMD_ORGANIZATION_OPTION + "\"");
        cmdLineOptions.addOption(organizationOption);
        Option stateOption = new Option(CMD_STATE_OR_PROVINCE_OPTION, CMD_STATE_OR_PROVINCE_OPTION, true, "\"" + CMD_STATE_OR_PROVINCE_OPTION + "\"");
        cmdLineOptions.addOption(stateOption);
        Option countryOption = new Option(CMD_COUNTRY_OPTION, CMD_COUNTRY_OPTION, true, "\"" + CMD_COUNTRY_OPTION + "\"");
        cmdLineOptions.addOption(countryOption);
        Option emailsOption = new Option(CMD_EMAILS_OPTION, CMD_EMAILS_OPTION, true, "\"" + CMD_EMAILS_OPTION + "\"");
        cmdLineOptions.addOption(emailsOption);
        Option keystoreOption = new Option(CMD_USERPCKS12_OPTION, CMD_USERPCKS12_OPTION, true, "\"" + CMD_USERPCKS12_OPTION + "\"");
        cmdLineOptions.addOption(keystoreOption);
        Option roleOption = new Option(CMD_ROLE_OPTION, CMD_ROLE_OPTION, true, "\"" + CMD_ROLE_OPTION + "\"");
        cmdLineOptions.addOption(roleOption);
        return cmdLineOptions;
    }

    private void removeRole(String uid, String realm, CommandLine commandLine) {
        String userId = getTolvenConfigWrapper().getAdminId();
        char[] userIdPassword = getTolvenConfigWrapper().getAdminPassword();
        String role = commandLine.getOptionValue(CMD_ROLE_OPTION);
        UserLoader userLoader = new DefaultUserLoader(userId, userIdPassword);
        userLoader.removeRole(role, uid, realm);
    }

    private void vaildateKeyStore(byte[] bytes, char[] password) {
        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            try {
                KeyStore keyStore = KeyStore.getInstance(TOLVEN_CREDENTIAL_FORMAT_PKCS12);
                keyStore.load(bais, password);
            } catch (Exception ex) {
                throw new RuntimeException("Could not load keyStore", ex);
            }
        } finally {
            if (bais != null)
                try {
                    bais.close();
                } catch (IOException ex) {
                    throw new RuntimeException("Could not close bytearrayinputstream after loading keyStore", ex);
                }
        }
    }

}
