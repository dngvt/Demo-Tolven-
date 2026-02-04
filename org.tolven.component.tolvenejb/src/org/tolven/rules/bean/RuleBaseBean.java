/**
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
 */

package org.tolven.rules.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.drools.core.RuleBase;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.RuleBaseConfiguration.AssertBehaviour;
import org.drools.core.RuleBaseFactory;
import org.drools.core.StatefulSession;
import org.drools.core.rule.Package;
import org.tolven.app.MenuLocal;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.entity.Account;
import org.tolven.doc.entity.RulePackage;
import org.tolven.rules.PackageCompiler;
import org.tolven.rules.RuleBaseLocal;
import org.tolven.rules.RulesLocal;

@Stateless
@Local(RuleBaseLocal.class)
public class RuleBaseBean implements RuleBaseLocal {

    private static Map<Long, Long> accountTemplateIdMap = new HashMap<Long, Long>();
    private static PackageCompiler compiler = new PackageCompiler();
    private static final Logger logger = Logger.getLogger(RuleBaseBean.class);
    private static Map<Long, List<AccountMenuStructure>> menuStructuresMap = new HashMap<Long, List<AccountMenuStructure>>();
    private static Map<String, Date> packageTimestampMap = new HashMap<String, Date>();
    private static Map<String, RuleBase> ruleBasesMap = new HashMap<String, RuleBase>();

    /**
     * Compile rules and add to rule base without using serialized-compiled rules from DB. Just used for debugging.
     * @param p
     * @param ruleBase
     * @throws Exception
     */
    private static void addRulePackageToRuleBase2(RulePackage p, RuleBase ruleBase) {
        //commenting for antlr fix and drools5 upgrade
        /*
        String packageName = compiler.extractPackageName(p.getPackageBody());
        Package initialPackage = new Package(packageName);
        for (String knownType : compiler.extractPlaholderAccountType( p.getPackageBody() )) {
            logger.info("Add " + knownType + " placeholders to " + packageName + " package");
            addKnownTypeToPackage(initialPackage, knownType);
        }*/
        // Compile the package
        Package pkg = compiler.compile(p.getPackageBody(), null);
        ruleBase.addPackage(pkg);
    }

    private static MenuLocal getMenuBean() {
        String jndiName = "java:app/tolvenEJB/MenuBean!org.tolven.app.MenuLocal";
        try {
            InitialContext ctx = new InitialContext();
            return (MenuLocal) ctx.lookup(jndiName);
        } catch (Exception ex) {
            throw new RuntimeException("Could not lookup " + jndiName);
        }
    }

    private static synchronized List<AccountMenuStructure> getMenuStructures(Account account, String knownType) {
        //The method can only be called one thread at a time, but many threads may be waiting, so check if ruleBase was created by the first caller
        long accountId = account.getId();
        Long accountTemplateId = account.getAccountTemplate().getId();
        try {
            List<AccountMenuStructure> menuStructures = menuStructuresMap.get(accountId);
            Long cachedAccountTemplateId = accountTemplateIdMap.get(accountId);
            if (menuStructures == null || !accountTemplateId.equals(cachedAccountTemplateId)) {
                long msStart = System.currentTimeMillis();
                AccountMenuStructure rootMS = getMenuBean().getRootMenuStructure(account);
                rootMS.prepareForRuleEngine();
                menuStructures = new ArrayList<AccountMenuStructure>();
                populateMenuStructuresList(rootMS, menuStructures);
                menuStructuresMap.put(accountId, menuStructures);
                accountTemplateIdMap.put(accountId, accountTemplateId);
                long msEnd = System.currentTimeMillis();
                logger.info("Found: " + menuStructures.size() + " menustructures for account: " + accountId + ", type: " + knownType + " [ " + (msEnd - msStart) + " ms]");
            }
            return menuStructures;
        } catch (Exception ex) {
            throw new RuntimeException("Error loading menustructures for account: " + accountId, ex);
        }
    }

    private static synchronized RuleBase getRuleBase(String knownType) {
        try {
            RuleBase ruleBase = ruleBasesMap.get(knownType);
            RulePackage rulePackage = getRulesBean().findActivePackage(knownType);
            if (rulePackage == null) {
                throw new RuntimeException("Could not find active rule package for account type: " + knownType);
            }
            Date packageTimestamp = rulePackage.getTimestamp();
            Date cachedPackageTimestamp = packageTimestampMap.get(knownType);
            if (ruleBase == null || !packageTimestamp.equals(cachedPackageTimestamp)) {
                long ruleBaseStart = System.currentTimeMillis();
                RuleBaseConfiguration confRuleBase = new RuleBaseConfiguration();
                // Be sure we use equality, not identity for facts.
                confRuleBase.setAssertBehaviour(AssertBehaviour.EQUALITY);
                //confRuleBase.setShadowProxy(false);
                // Create a rule base
                ruleBase = RuleBaseFactory.newRuleBase(confRuleBase);
                // Get the applicable packages
                List<RulePackage> packages = getRulesBean().findActivePackages(knownType);
                for (RulePackage p : packages) {
                    //              addRulePackageToRuleBase(p, ruleBase, true);
                    addRulePackageToRuleBase2(p, ruleBase);
                }
                ruleBasesMap.put(knownType, ruleBase);
                packageTimestampMap.put(knownType, packageTimestamp);
                long ruleBaseEnd = System.currentTimeMillis();
                logger.info("Created_RuleBase: " + knownType + " [ " + (ruleBaseEnd - ruleBaseStart) + " ms]");
            }
            return ruleBase;
        } catch (Exception ex) {
            throw new RuntimeException("Error building rule base", ex);
        }
    }

    private static RulesLocal getRulesBean() {
        String jndiName = "java:app/tolvenEJB/RulesBean!org.tolven.rules.RulesLocal";
        try {
            InitialContext ctx = new InitialContext();
            return (RulesLocal) ctx.lookup(jndiName);
        } catch (Exception ex) {
            throw new RuntimeException("Could not lookup " + jndiName);
        }
    }

    private static void populateMenuStructuresList(AccountMenuStructure ms, List<AccountMenuStructure> menuStructures) {
        menuStructures.add(ms);
        Collection<AccountMenuStructure> childMenuStructures = ms.getChildren();
        if (childMenuStructures.size() == 0) {
            return;
        }
        for (AccountMenuStructure ams : childMenuStructures) {
            populateMenuStructuresList(ams, menuStructures);
        }
    }

    private StatefulSession getOrCreateNewStatefulSession(Account account) {
        try {
            String knownType = account.getAccountType().getKnownType();
            RuleBase ruleBase = getRuleBase(knownType);
            List<AccountMenuStructure> menuStructures = getMenuStructures(account, knownType);
            StatefulSession statefulSession = ruleBase.newStatefulSession();
            for (MenuStructure ms : menuStructures) {
                statefulSession.insert(ms);
            }
            return statefulSession;
        } catch (Exception e) {
            throw new RuntimeException("Error building rule base", e);
        }
    }

    /**
     * Get the rule base for the specified account. In general, this pulls together all active rule packages applicable to the specified account 
     * @param account
     * @return A RuleBase
     */
    @Override
    public StatefulSession newStatefulSession(Account account) {
        return getOrCreateNewStatefulSession(account);
    }

}
