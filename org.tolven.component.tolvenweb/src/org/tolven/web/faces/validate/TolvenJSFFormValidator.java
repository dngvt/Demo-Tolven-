package org.tolven.web.faces.validate;

import java.io.Serializable;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;

public abstract class TolvenJSFFormValidator  implements Validator, Serializable {
	public boolean isCheckValidations(UIComponent component, FacesContext context){		
		Map<String, String> parameterMap = (Map<String, String>) context.getExternalContext().getRequestParameterMap();
		String param = parameterMap.get("checkValidations");
		if (param != null) 
			 return true;
		else 
			return false;				 
	}	

}
