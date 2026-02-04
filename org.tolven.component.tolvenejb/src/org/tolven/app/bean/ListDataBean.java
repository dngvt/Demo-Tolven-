package org.tolven.app.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.tolven.app.GenericListDataLocal;
import org.tolven.app.ListDataLocal;
import org.tolven.app.entity.ListQueryControl;
import org.tolven.app.entity.MDQueryResults;
import org.tolven.app.entity.MSColumn;
import org.tolven.app.entity.MenuStructure;

@Local(ListDataLocal.class)
@Stateless
public class ListDataBean extends GenericListDataBean implements ListDataLocal {
	Logger logger = Logger.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;

	public EntityManager getEm() {
		return em;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

	 /**
     * Return a count of total items specified in the criteria. Offset, limit and sortOrder are ignored.
	 * @throws NamingException 
     */
    public long countListData(ListQueryControl ctrl)  {
    	if(ctrl.getMenuStructure().getQuery().startsWith("custom:")){
    		String beanName = ctrl.getMenuStructure().getQuery().substring(7);
    		try {
    			InitialContext context = new InitialContext();
    			GenericListDataLocal listDataLocal = (GenericListDataLocal)context.lookup(beanName);
	    		return listDataLocal.countListData(ctrl);
			} catch (NamingException e) {
				logger.error(e);
				throw new RuntimeException("Error in finding countData using beanName:"+beanName,e);
			}
    	}
    	else{
    		List<MSColumn> columns = ctrl.getMenuStructure().getSortedColumns();
    		StringBuffer msQuery = new StringBuffer(ctrl.getMenuStructure().getQuery());
    		StringBuffer whereQuery = new StringBuffer();
    		if(!StringUtils.isBlank(ctrl.getFilter())){
    			whereQuery.append("upper("+columns.get(0).getInternal()+") like '%"+ctrl.getFilter().toUpperCase().trim()+"%'");
    		}
    		if(whereQuery.length() > 0){
	    		if(msQuery.indexOf(" where ") > 0){
	    			msQuery.append(" and "+whereQuery.toString());
	    		}else{
	    			msQuery.append(" where "+whereQuery.toString());
	    		}
    		}
    		String fromQuery = msQuery.substring(msQuery.indexOf(" from "));
    		Query query = em.createNativeQuery("select count("+columns.get(0).getInternal()+") "+fromQuery);
    	    Object rslt = query.getSingleResult();
    	    if(rslt == null)
    	    	return 0;
    	    return Long.parseLong(rslt.toString());    	    
    	}
    }

	

	@Override
	public MDQueryResults findDataByColumns(ListQueryControl ctrl) {
		 MDQueryResults mdQueryResults = new MDQueryResults(ctrl);
		if(ctrl.getMenuStructure().getQuery().startsWith("custom:")){
    		String beanName = ctrl.getMenuStructure().getQuery().substring(7);
    		try {
    			InitialContext context = new InitialContext();
    			GenericListDataLocal listDataLocal = (GenericListDataLocal)context.lookup(beanName);
	    		return listDataLocal.findDataByColumns(ctrl);
			} catch (NamingException e) {
				logger.error(e);
				throw new RuntimeException("Error in finding countData using beanName:"+beanName,e);
			}
    	}
    	else{
    		MenuStructure ms = ctrl.getMenuStructure();
    		StringBuffer queryStr = new StringBuffer(ms.getQuery().substring(6));
    		StringBuffer whereQuery = new StringBuffer();
    		StringBuffer orderBy = new StringBuffer();
    		List<MSColumn> columns = ctrl.getSortedColumns();
    		if(!StringUtils.isBlank(ctrl.getFilter())){
    			whereQuery.append("upper("+columns.get(0).getInternal()+") like '%"+ctrl.getFilter().toUpperCase().trim()+"%'");
    		}
    		if(whereQuery.length() > 0){
	    		if(queryStr.indexOf(" where ") > 0){
	    			queryStr.append(" and "+whereQuery.toString());
	    		}else{
	    			queryStr.append(" where "+whereQuery.toString());
	    		}
    		}
    		if(!StringUtils.isBlank(ctrl.getSortOrder())){
    			orderBy.append(ctrl.getSortOrder());
    		}else if(!StringUtils.isBlank(ms.getInitialSort())){
    			orderBy.append(ms.getInitialSort());
    		}
    		if(!StringUtils.isBlank(ctrl.getSortDirection())){
    			orderBy.append(" "+ctrl.getSortDirection());
    		}
    		if(orderBy.length() > 0){
    			queryStr.append(" order by "+orderBy.toString());
    		}
    		
			Set<String> limitColumns = null;
			if (ctrl.getColumns() != null && ctrl.getColumns().size() > 0) {
				limitColumns = new HashSet<String>(ctrl.getColumns());
			}
			String[] columnNames = extractColumnNamesFromMSColumns(columns, limitColumns);
    		Query query = em.createNativeQuery(queryStr.toString());
    		query.setFirstResult(ctrl.getOffset());
    		query.setMaxResults(ctrl.getLimit());
    		List<Object[]> results = query.getResultList();
    		for (Object[] row : results) {
    			Map<String, Object> rowMap = new HashMap<String, Object>(columnNames.length +ms.getColumns().size());
    			int c1 = 0;
    			for (String field : columnNames) {
    				rowMap.put(field, row[c1]);
    				rowMap.put(columns.get(c1).getHeading(), row[c1]);
        			c1++;
    			}
    			mdQueryResults.addRow(rowMap);
    		}
    	}
		return mdQueryResults;
	}
    
}
