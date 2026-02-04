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
package org.tolven.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.drools.core.FactHandle;
import org.drools.core.WorkingMemory;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.event.ActivationCancelledEvent;
import org.drools.core.event.ActivationCreatedEvent;
import org.drools.core.event.AfterActivationFiredEvent;
import org.drools.core.event.AgendaEventListener;
import org.drools.core.event.AgendaGroupPoppedEvent;
import org.drools.core.event.AgendaGroupPushedEvent;
import org.drools.core.event.BeforeActivationFiredEvent;
import org.drools.core.event.RuleFlowGroupActivatedEvent;
import org.drools.core.event.RuleFlowGroupDeactivatedEvent;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Activation;
import org.drools.core.spi.Tuple;

public class TolvenAgendaEventListener implements AgendaEventListener {

    public static Logger logger = Logger.getLogger(TolvenAgendaEventListener.class);

    private Map<String, Long> ruleTimestamp = new HashMap<String, Long>();

    @Override
    public void activationCancelled(ActivationCancelledEvent event, WorkingMemory workingMemory) {
    }

    @Override
    public void activationCreated(ActivationCreatedEvent event, WorkingMemory workingMemory) {
    }

    @Override
    public void afterActivationFired(AfterActivationFiredEvent event, WorkingMemory workingMemory) {
        Long end = System.currentTimeMillis();
        Activation activation = event.getActivation();
        String ruleName = activation.getRule().getName();
        String packageName = activation.getRule().getPackage();
        Long start = ruleTimestamp.remove(ruleName);
        if (start == null) {
            throw new RuntimeException("Start timestamp not set up for rule: " + packageName + "/" + ruleName);
        }
        logger.debug("fire_rule," + String.valueOf(end - start) + "," + packageName + "/'" + ruleName + "'," + extractDeclarations(activation, workingMemory));
    }

    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent event, WorkingMemory workingMemory) {
    }

    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent event, WorkingMemory workingMemory) {
    }

    @Override
    public void beforeActivationFired(BeforeActivationFiredEvent event, WorkingMemory workingMemory) {
        String ruleName = event.getActivation().getRule().getName();
        ruleTimestamp.put(ruleName, System.currentTimeMillis());
    }

    private String extractDeclarations(final Activation activation, final WorkingMemory workingMemory) {
        final StringBuffer result = new StringBuffer();
        final Tuple tuple = activation.getTuple();
        final Map declarations = activation.getSubRule().getOuterDeclarations();
        for (Iterator<Declaration> it = declarations.values().iterator(); it.hasNext();) {
            final Declaration declaration = it.next();
            final FactHandle handle = tuple.get(declaration);
            if (handle instanceof InternalFactHandle) {
                final InternalFactHandle handleImpl = (InternalFactHandle) handle;
                if (handleImpl.getId() == -1) {
                    // This handle is now invalid, probably due to an fact retraction
                    continue;
                }
                final Object value = declaration.getValue((InternalWorkingMemory) workingMemory, handleImpl.getObject());

                result.append(declaration.getIdentifier());
                result.append("=");
                if (value == null) {
                    // this should never occur
                    result.append("null");
                } else {
                    result.append(value);
                    result.append("(");
                    result.append(handleImpl.getId());
                    result.append(")");
                }
            }
            if (it.hasNext()) {
                result.append("; ");
            }
        }
        return result.toString();
    }

    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent arg0, WorkingMemory arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent arg0, WorkingMemory arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent arg0, WorkingMemory arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent arg0, WorkingMemory arg1) {
        // TODO Auto-generated method stub
        
    }

}
