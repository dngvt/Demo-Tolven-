package org.tolven.app.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.tolven.app.GenericListDataLocal;
import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.app.entity.ListQueryControl;
import org.tolven.app.entity.MDQueryResults;
import org.tolven.app.entity.MSColumn;

public abstract class GenericListDataBean implements GenericListDataLocal{
	Logger logger = Logger.getLogger(this.getClass());
	
	protected String buildSelectClause(String[] columnNames) {
		StringBuffer selectClause = new StringBuffer(350);
		// Build the query string
		for (String column : columnNames) {
			if (selectClause.length() != 0)
				selectClause.append(", ");
			selectClause.append(column);
		}
		return selectClause.toString();
	}

	protected Query prepareCriteria(EntityManager em, ListQueryControl ctrl, String selectClause,String queryClause,boolean includeSortOrder) {
		StringBuffer sbOrder = new StringBuffer(350);
		Map<String, Object> params = new HashMap<String, Object>(5);
	
		// Assemble the query string
		// String qs = String.format(Locale.US, "SELECT %s FROM %s WHERE %s %s",
		// init, sbFrom, sbWhere, sbOrder);
		System.out.println("Initialsort = "+ctrl.getMenuStructure().getInitialSort());
		if(!StringUtils.isBlank(ctrl.getMenuStructure().getInitialSort())){
			sbOrder.append(String.format("ORDER BY %s",ctrl.getMenuStructure().getInitialSort()));
		}
		String qs = null;
		if(includeSortOrder)
			qs = String.format(Locale.US, "SELECT %s %s %s", selectClause,queryClause,sbOrder);
		else
			qs = String.format(Locale.US, "SELECT %s %s", selectClause,queryClause);
		logger.debug("ListData Query: " + qs.toString());
		Query query = em.createQuery(qs);
		logger.debug("Query params: ");
		for (Map.Entry<String, Object> e : params.entrySet()) {
			query.setParameter(e.getKey(), e.getValue());
			logger.debug("   key:" + e.getKey() + ", value:" + e.getValue());
		}
		return query;
	}
	
	
	protected String[] extractColumnNamesFromMSColumns( Collection<MSColumn> cols, Set<String> limitColumns ) {
		Set<String> columns = new HashSet<String>(20);
		// Get the column names (from clause)
		// But if limit columns specified, then only those columns
		for( MSColumn col : cols ){
		    if (limitColumns!=null && !limitColumns.contains(col.getHeading())) 
		    	continue; 
		    // Ignore computed columns
		    if (!col.isComputed()) {
		        columns.add(col.getInternal());
		    }
		}
		return columns.toArray(new String[columns.size()]);
	}
	
	/** Module to construct the result collection using JPA query output
	 * @param results - JPA query output
	 * @param columnNames - internal field names
	 * @param columns - MSColumns
	 * @param fieldGetter
	 * @return
	 */
	protected MDQueryResults getResultsForList(List<Object[]> results,String[] columnNames,Collection<MSColumn> columns,MSColumn.RowMapFieldGetter fieldGetter,MDQueryResults queryResults) {
		for (Object[] row : results) {
			Map<String, Object> rowMap = new HashMap<String, Object>(columnNames.length + columns.size());
			int c1 = 0;
			// Load the raw internal fields into the map
			for (String field : columnNames) {
				rowMap.put(field, row[c1]);
				c1++;
			}
			// Make the rowMap available to fieldGetter (this will collect
			// the formatted field values)
			fieldGetter.setRowMap(rowMap);
			// Now create a separate entry for each of the column entries,
			// including the formatting functions
			// performed by those columns. If the column is not an internal
			// field, then the only thing we can do is formatting.
			for (MSColumn col : columns) {
				// Ignore computed columns
				if (!col.isComputed()) {
					rowMap.put(col.getHeading(),col.getFormattedColumn(fieldGetter));
				}
			}
			queryResults.addRow(rowMap);
		}
		return queryResults;
	}
	
	public MDQueryResults findDataByColumns(ListQueryControl ctrl,EntityManager em) {
		 MDQueryResults mdQueryResults = new MDQueryResults(ctrl);
		try {
			Collection<MSColumn> columns = ctrl.getSortedColumns();
			Set<String> limitColumns = null;
			if (ctrl.getColumns() != null && ctrl.getColumns().size() > 0) {
				limitColumns = new HashSet<String>(ctrl.getColumns());
			}
			String[] columnNames = extractColumnNamesFromMSColumns(columns, limitColumns);
			String selectClause = buildSelectClause(columnNames);
			Query query = prepareCriteria(em,ctrl, selectClause.toString(),ctrl.getMenuStructure().getQuery().substring(6),true);
			if (ctrl.getLimit() > 0) {
				query.setMaxResults(ctrl.getLimit());
			}
			query.setFirstResult(ctrl.getOffset());
			// Setup a FieldGetter which holds common attributes used by the
			// formatter.
			MSColumn.RowMapFieldGetter fieldGetter = new MSColumn.RowMapFieldGetter();
			// Some common attributes for all rows and columns
			fieldGetter.setNow(ctrl.getNow());
			fieldGetter.setTimeZone(ctrl.getTimeZone());
			fieldGetter.setLocale(ctrl.getLocale());
			// Setup for fancy "from" behavior as well
			TrimExpressionEvaluator evaluator = new TrimExpressionEvaluator();
			evaluator.addVariable("account", ctrl.getAccountUser().getAccount());
			evaluator.addVariable("accountUser", ctrl.getAccountUser());
			fieldGetter.setEvaluator(evaluator);
		
			// Get the results of the query
			List<Object[]> results = query.getResultList();
			return getResultsForList(results, columnNames, columns, fieldGetter,mdQueryResults);
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException("Error in findMenuDataByColumn for path "+ ctrl.getActualMenuStructurePath(), e);
		}		
	}
	
	 /**
     * Return a count of total items specified in the criteria. Offset, limit and sortOrder are ignored.
     */
    public long countListData(ListQueryControl ctrl,EntityManager em ) {
        try {
        	List<MSColumn> columns = ctrl.getSortedColumns();
            Query query = prepareCriteria(em, ctrl, "COUNT("+columns.get(0).getInternal()+")",ctrl.getMenuStructure().getQuery().substring(6),false);
            Long rslt = (Long) query.getSingleResult();
            return rslt.longValue();
        } catch (RuntimeException e) {
        	throw new RuntimeException( "Error in countListData for path " +  ctrl.getRequestedPath(), e); 
        }
    }
}
