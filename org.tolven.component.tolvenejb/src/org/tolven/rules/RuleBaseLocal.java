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

package org.tolven.rules;

import org.drools.core.StatefulSession;
import org.tolven.core.entity.Account;

public interface RuleBaseLocal {

    /**
     * Get the rule base for the specified account. In general, this pulls together all active rule packages applicable to the specified account 
     * 
     * @param account
     * @return A StatefulSession
     */
    public StatefulSession newStatefulSession(Account account);
    
}
