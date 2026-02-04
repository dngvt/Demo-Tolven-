package org.tolven.us.states;

import java.util.Map;


public interface DemographicsLocal {
    
	public boolean createStateNames(String stateCode, String stateName);

    public Map<String, String> retrieveAllStates();
    
}
