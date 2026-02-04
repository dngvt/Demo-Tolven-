package org.tolven.us.states.bean;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.tolven.us.states.DemographicsLocal;
import org.tolven.us.states.entity.StateNames;

@Stateless
@Local(DemographicsLocal.class)
public class DemographicsBean implements DemographicsLocal{
	@PersistenceContext
    private EntityManager em;
  
	/**
     * Method to check whether state name exists.
     * @param mdRefill
     * @return
     */
    private boolean checkForStateName(String stateCode){
        String qs = null;
        Query query = null;
        qs = String.format(Locale.US, "SELECT sn FROM StateNames sn WHERE sn.stateCode = :code");
        query = em.createQuery( qs );
        query.setParameter( "code", stateCode);
        if(query.getResultList() != null && query.getResultList().size() > 0){
            return true;
        }
        return false;   
    }

    /**
     * Method to load state names into StateNames table in Surescripts schema.
     * @param stateCode
     * @param name
     */
    public boolean createStateNames(String stateCode, String name) {
        if (!checkForStateName(stateCode)) {
            StateNames stateName = new StateNames();
            stateName.setStateCode(stateCode);
            stateName.setStateName(name);
            persistStateName(stateName);
            return true;
        }
        return false;
    }
    
    public void persistStateName( StateNames stateName ) {
        em.persist(stateName);
    }
    
    /**
     * Method to retrieve all the states in USA with their codes
     * @return
     */
    @SuppressWarnings("unchecked")
	public Map<String, String> retrieveAllStates(){
    	Map<String, String> usaStates = null;
    	String qs = null;
		Query query = null;
		qs = String.format(Locale.US, "SELECT st FROM StateNames st ORDER BY st.stateName");
		query = em.createQuery( qs );
		if(null != query.getResultList()){
			usaStates = new TreeMap<String, String>();
			ArrayList<StateNames> stateDetailsList = (ArrayList<StateNames>)query.getResultList();
			for(StateNames states : stateDetailsList){
				usaStates.put(states.getStateCode(),states.getStateName());
			}
		}
    	return usaStates;
    }
    
}
