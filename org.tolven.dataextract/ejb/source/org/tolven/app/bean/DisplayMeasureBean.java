package org.tolven.app.bean;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.tolven.app.MenuLocal;
import org.tolven.app.entity.MSColumn;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.entity.AccountUser;

@Stateless()
public class DisplayMeasureBean {
	/** Entity Manager Variable */
	@PersistenceContext
	private EntityManager em;

	@EJB 
	private MenuLocal menuBean;
	
	private Logger log = Logger.getLogger(this.getClass());


	
	/*
	 * it is very important to include "parent" placholder ids, when
	 * appropriate. For example, even if not explicitly defined in the
	 * application.xml, an echr:patient:allergy entry should include a column
	 * with the id of the patient which would look something like: parent01_id
	 * AS patient_id
	 */
	protected void addPatientId(String path, StringBuffer buff) {
		if(path.contains(":patient:")){
			buff.append(", parent01_id AS patient_id");
		}
	}
	/*
	 * crate SQL query using fields defined in the placeholder definition
	 */
	protected void createSelectSQL(MenuStructure ms,Map<String,String> sqlMap,AccountUser activeAccountUser,Pattern parentPattern ){
		log.debug("creating sql for "+ms.getPath());
		if(ms.getRole().equalsIgnoreCase("placeholder")){
			createSQLForPlaceholder(ms,sqlMap,activeAccountUser,parentPattern);
		}else{
			List<MenuStructure> list = getMenuBean().findMenuChildren( activeAccountUser,ms );
			for(MenuStructure child:list){			
				if(child.getRole().equalsIgnoreCase("placeholder"))	{
					createSQLForPlaceholder(child,sqlMap,activeAccountUser,parentPattern);
				}			
			}
		}
	}
	
	public void createSQLForPlaceholder(MenuStructure palceholderMS,Map<String,String> sqlMap,AccountUser activeAccountUser,Pattern parentPattern){

		StringBuffer sql = new StringBuffer("select id as id, account_id AS account_id, updateTime AS update_time, " +
				"actStatus AS act_status, status AS status, document_id AS document_id ");
		addPatientId(palceholderMS.getPath(),sql);
		Collection<MSColumn> columns = palceholderMS.getColumns();
		for(MSColumn column:columns){		
			//ignore non-internal, _extended and _computed fields
			if(column.getInternal() != null && !column.getInternal().equalsIgnoreCase("_extended") && !column.getInternal().equalsIgnoreCase("_computed") ){
				//parent01 should masked as parent01_id
				if(parentPattern.matcher(column.getInternal()).matches()){
					sql.append (", "+column.getInternal()+"_id"+" as "+column.getHeading());
				}else{
					sql.append (", "+column.getInternal()+" as "+column.getHeading());
				}
			}						
		}
		sql.append(" from app.menu_data where menustructure_id = "+palceholderMS.getId() +"  AND (deleted IS NULL OR deleted = FALSE) ");	
		if(palceholderMS.getPath().contains(":")){
			sqlMap.put(palceholderMS.getPath().substring(palceholderMS.getPath().lastIndexOf(":")+1), sql.toString());
		}else{
			sqlMap.put(palceholderMS.getPath(), sql.toString());
		}
		//build sql for placeholder children
		List<MenuStructure> subList = getMenuBean().findMenuChildren( activeAccountUser,palceholderMS );
		for(MenuStructure subChild:subList){
			 createSelectSQL(subChild,sqlMap,activeAccountUser,parentPattern);
		}	
	}

	public EntityManager getEm() {
		return em;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}
	
	
	public MenuLocal getMenuBean() {
		return menuBean;
	}

	public void setMenuBean(MenuLocal menuBean) {
		this.menuBean = menuBean;
	}

}
