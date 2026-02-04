/*
 *  Copyright (C) 2009 Tolven Inc 
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * Contact: info@tolvenhealth.com
 */
package org.tolven.ajax;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tolven.app.ListDataLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.ListQueryControl;
import org.tolven.app.entity.MDQueryResults;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;
import org.tolven.logging.TolvenLogger;
import org.tolven.web.MenuAction;
import org.tolven.web.util.MiscUtils;
/*
 * Servlet to create Live Grids with a custom defined html element id to them and allow accessing favorites lists  
 */
@SuppressWarnings("serial")
public class FavoritesServlet extends HttpServlet {
    @EJB
    private MenuLocal menuBean;
	@EJB
    public ListDataLocal listDataLocal;
        
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	String uri = req.getRequestURI();
    	AccountUser activeAccountUser = TolvenRequest.getInstance().getAccountUser();
    	resp.setContentType("text/xml");
	    resp.setHeader("Cache-Control", "no-cache");
	    Writer writer=resp.getWriter();
    	try {
    		if( uri.endsWith( "createGrid.ajaxf" )){
    	    	String path = req.getParameter("element");
    	    	String gridId = req.getParameter("gridId");	    	
    	    	String gridType = req.getParameter("gridType");	 
    	    	// incase if we don't need to show the favorites
    	    	String disableFavorites = req.getParameter("disableFavorites");
    			MenuPath menuPath = new MenuPath(path);	
    			MenuStructure ms=null;
    			
    			if(menuPath.getPath().startsWith("global:"))
    				ms =  menuBean.findGlobalMenuStructure(menuPath.getPath() );
    			else
    				ms =  menuBean.findMenuStructure(activeAccountUser, menuPath.getPath() );
    			
    			// java script method for grid rows on-click
    			String scriptMethodName = req.getParameter("methodName");
    			// Form id for .   
    			String scriptMethodArgs = req.getParameter("methodArgs");
    			
    			// Setup a query control
    			ListQueryControl ctrl = new ListQueryControl();
    			ctrl.setLimit( 0 );
    			ctrl.setAccountUser(activeAccountUser);
    			ctrl.setMenuStructure( ms.getAccountMenuStructure() );
    			ctrl.setNow( new Date());
    			ctrl.setOffset( 0 );
    			ctrl.setOriginalTargetPath(menuPath);
    			ctrl.setRequestedPath(menuPath);
    			ctrl.setSortDirection( "ASC");
    			ctrl.setSortOrder( "" );
    			Long menuDataCount = null;
    			GridBuilder grid  = null;
    			// Get the number of rows
    			if(ms.getQuery() != null && (ms.getQuery().startsWith("query:")
    					||ms.getQuery().startsWith("custom:"))){
    				menuDataCount = new Long( listDataLocal.countListData(ctrl));
    				grid = new GridBuilder(ctrl, menuBean,menuDataCount);
    			}else{
    				menuDataCount = new Long( menuBean.countMenuData( ctrl ) );
    				grid = new GridBuilder(ctrl, menuBean,menuDataCount);
    				menuBean.prepareMQC( ctrl );
        		}
    			if(null == disableFavorites || disableFavorites.equals("false")){
	    			grid.setFavorites(MiscUtils.findFavorites(path, activeAccountUser, menuBean, menuPath));
	    			// need this to add 'All' on the favorites tabs
	    			if(grid.getFavorites().size() > 0)
	    				grid.setFavoritesType(activeAccountUser.getAccount().getAccountType().getKnownType()+
	    						":"+(String)grid.getFavorites().get(0).get("Type"));
    			}
    			if(gridId != null)
    				grid.setGridId(gridId);
    			if(gridType != null)
    				grid.setGridType(gridType);
    			grid.createGrid(scriptMethodName, scriptMethodArgs );
    			writer.write(grid.toString());
    	    }
    	}catch (Exception e) {
    		throw new ServletException( "Exception thrown in FavoritesServlet", e);
		}
    }
   
}
