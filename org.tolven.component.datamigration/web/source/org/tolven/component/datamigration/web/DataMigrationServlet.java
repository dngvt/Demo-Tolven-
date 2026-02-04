package org.tolven.component.datamigration.web;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.tolven.app.ConfigChangeLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.MigrateMenuDataMessage;
import org.tolven.app.RollbackMigrationMessage;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.ConfigChange;
import org.tolven.app.entity.MSColumn;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;


@WebServlet(urlPatterns = { "*.migration" }, loadOnStartup = 5)
public class DataMigrationServlet extends HttpServlet {
	@EJB
	private MenuLocal menuBean;
	
	@EJB
	private ConfigChangeLocal configChangeBean;

	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String uri = request.getRequestURI();
		AccountUser accountUser = TolvenRequest.getInstance().getAccountUser();
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache");
	    Writer writer=response.getWriter();
    	
    	String msPath = request.getParameter("msPath");
    	String oldHeading = request.getParameter("oldHeading");
    	String newHeading = request.getParameter("newHeading");
    	
		if (uri.endsWith("checkMenuStructure.migration") && msPath != null) {
			MenuStructure ms = findMenuStructure(accountUser, msPath);
			if(ms == null)
				writer.write("MenuStructure not found with path:"+msPath+ " in account:"+accountUser.getAccount());
			//else
			//	writer.write("Records Found:"+countMenuData(accountUser, msPath));
		} else  if (uri.endsWith("checkOldMenuColumn.migration") && !StringUtils.isBlank(newHeading)  ){
			boolean found = findMenuColumn(accountUser, msPath, newHeading);
			if(!found)
				writer.write("External field/column with name:"+newHeading+" not found in menustructure:"+msPath);
		 } else if(uri.endsWith("triggerMigration.migration")){
			 AccountMenuStructure ms = (AccountMenuStructure)findMenuStructure(accountUser, msPath);
			 if(ms == null)
				 return;
			 ConfigChange change = new ConfigChange();
			 change.setMenuStructure(ms);
			 change.setCreatedDate(new Date());
			 change.setHeading(oldHeading);
			 change.setOldLocation("_extended");
			 change.setNewHeading(newHeading);
			 change.setSkipMigration(false);
			 change = configChangeBean.saveConfigChange(change);
			 ms.setLatestConfig(change.getId());
			 configChangeBean.startDataMigration(new MigrateMenuDataMessage(ms));
			 writer.write("Migration is started. You can check the changes using SQL: select * from app.data_migration_log where change_id="+change.getId());
		 }else if(uri.endsWith("findExtededFields.migration")){
			 MenuData md = menuBean.findMenuDataItem(Long.parseLong(request.getParameter("id")));
			 writer.write(md.getExtendedFields().toString());
		 }else if(uri.endsWith("findExtededFields.migration")){
			 MenuData md = menuBean.findMenuDataItem(Long.parseLong(request.getParameter("id")));
			 writer.write(md.getExtendedFields().toString());
		 }else if(uri.endsWith("rollBackMigrationChanges.migration")){
			 String changeId = request.getParameter("changeId");
			 configChangeBean.startRollBackMigration(new RollbackMigrationMessage(Long.parseLong(changeId), accountUser.getAccount().getId()));
			 writer.write("Migration changes will be rolledback");
		 }
		
		
	}
	private long countMenuData(AccountUser accountUser,String msPath){
		MenuStructure ms = findMenuStructure(accountUser, msPath);
		if(ms == null)
			return 0;
		MenuQueryControl ctrl = new MenuQueryControl();
		ctrl.setLimit( 0 );
		ctrl.setAccountUser(accountUser);
		ctrl.setMenuStructure( ms.getAccountMenuStructure() );
		ctrl.setNow( new Date());
		ctrl.setOffset( 0 );
		ctrl.setOriginalTargetPath(new MenuPath(msPath));
		ctrl.setRequestedPath(new MenuPath(msPath));
		List<MenuData> list = menuBean.findMenuData(ctrl);
		if(list == null)
			return 0;
		return list.size();
	}
	private MenuStructure findMenuStructure(AccountUser accountUser,String msPath){
		 try{
			 MenuStructure ams = menuBean.findAccountMenuStructure(accountUser.getAccount().getId(), msPath);
			 return ams;				 
		 }catch (Exception e) {
			return null;
		}	
	}
	private boolean findMenuColumn(AccountUser accountUser,String msPath,String columnName){
		MenuStructure ms = findMenuStructure(accountUser, msPath);
		if(ms == null)
			return false;
		 boolean found = false;
		 if(ms != null){
			 for(MSColumn column:ms.getColumns()){
				 if(column.getHeading().equals(columnName) && column.getInternal().equals("_extended")){
					 found = true;
					 break;
				 }
			 }
		 }
		 return found;
	}
}
