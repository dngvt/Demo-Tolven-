package org.tolven.gatekeeper.web;

import javax.ejb.EJB;
import org.tolven.gatekeeper.GatekeeperPropertiesLocal;
import org.tolven.gatekeeper.GatekeeperPropertiesMap;

public class PropertyAction {
	 	@EJB private GatekeeperPropertiesLocal propertiesLocal; 
	 	private GatekeeperPropertiesMap propertiesMap;
	 	
		public GatekeeperPropertiesLocal getPropertiesLocal() {
			return propertiesLocal;
		}

		public void setPropertiesLocal(GatekeeperPropertiesLocal propertiesLocal) {
			this.propertiesLocal = propertiesLocal;
		}
		
		public GatekeeperPropertiesMap getPropertiesMap() {
			return propertiesMap;
		}

		public void setPropertiesMap(GatekeeperPropertiesMap propertiesMap) {
			this.propertiesMap = propertiesMap;
		}
		public GatekeeperPropertiesMap getProperties(){
			if(propertiesMap == null){
				propertiesMap = new GatekeeperPropertiesMap(getPropertiesLocal());
			}
			return propertiesMap;
		}
}
