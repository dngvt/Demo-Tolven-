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
 * @version $Id$
 */  
package org.tolven.msg;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jms.ObjectMessage;

import org.tolven.doc.bean.TolvenMessage;

/**
 * APIs that provide deferred message delivery.
 * @author Joe Isaac
 *
 */
public interface TolvenMessageSchedulerLocal {

    /**
     * Delete a TolvenMessage (after it has been processed)
     * @param tm
     */
    public void delete(TolvenMessage tm);

    public void setScheduler(long intervalDuration);

    public void stopScheduler();

    public Date getNextTimeout();

    /**
     * Find a TolvenMessage by id
     * 
     * @param id
     * @return
     */
    public TolvenMessage findTolvenMessage(long id);

    /**
     * Queue a TolvenMessage.
     * @param tm
     */
    public void queue(TolvenMessage tm);

    /**
     * Queue a list of TolvenMessages
     * One connection is used to send the List of TolvenMessages
     * @param tms
     */
    public void queue(List<TolvenMessage> tms);

    /**
     * Queue a TolvenMessage along with JMS properties
     * A separate connection is used for each payload
     * @param tm
     * @param properties
     */
    public void queue(TolvenMessage tm, Map<String, Object> properties);

    /**
     * Queue a list of TolvenMessages along with their JMS properties. Each payload must have a corresponding
     * properties map, even if that map is empty
     * A separate connection is used for each payload
     * @param tm
     * @param properties
     */
    public void queue(List<TolvenMessage> tm, List<Map<String, Object>> properties);
    
    /**
     * Schedule a TolvenMessage to be queued on queueOnDate
     * @param tm
     * @param queueOnDate
     */
    public void schedule(TolvenMessage tm, Date queueOnDate);

    /**
     * Schedule a TolvenMessage to be queued on queueOnDate along with its JMS properties
     * @param tm
     * @param properties
     * @param queueOnDate
     */
    public void schedule(TolvenMessage tm, Map<String, Object> properties, Date queueOnDate);

    /**
     * Unwrap TolvenMessage from an objectMessage
     * @param objectMessage
     */
    public TolvenMessage unwrapTolvenMessage(ObjectMessage objectMessage);

    /**
     * Queue a document to be processed by Tolven. The payload should be in cleartext and will
     * be encrypted prior to being added to the queue.
     */
    public void queueTolvenMessage(TolvenMessage tm) throws Exception;

    /**
     * Queue a document to be processed by Tolven on the queueOnDate. The payload should be in cleartext and will
     * be encrypted prior to being queued.
     */
    public void queueTolvenMessage(TolvenMessage tm, Date queueOnDate) throws Exception;
    
    /**
     * Queue a message by providing only primitive parameters
     * @param payload
     * @param xmlns
     * @param accountId
     * @param userId
     * @return The string "success"
     */
    public String queueWSMessage( byte[] payload, String xmlns, long accountId, long userId);

    public String testWS();

}
