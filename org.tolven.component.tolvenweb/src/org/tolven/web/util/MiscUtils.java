package org.tolven.web.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.tolven.app.MenuEventHandler;
import org.tolven.app.MenuLocal;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.MDQueryResults;
import org.tolven.app.entity.MSAction;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;
import org.tolven.logging.TolvenLogger;
import org.tolven.web.MenuAction;

public class MiscUtils {
    
    public static String createActionButtons(List<MSAction> actions, String element) {
        /*
         * Use a Writer which more closely matches the writer on a servlet response (which does not make it to this method yet)
         */
        Writer writer = null;
        try {
            writer = new StringWriter();
            for (MSAction action : actions) {
                if (action.getMenuEventHandlerFactory() == null) {
                    createActionButtons(action, element, writer);
                } else {
                    createHandlerActionButtons(action, element, writer);
                }
            }
        } catch (IOException ex) {
            //TODO: Needs to be declared in the signature at some point
            throw new RuntimeException(ex);
        }
        return writer.toString();
    }

    public static void createActionButtons(MSAction action, String element, Writer writer) throws IOException {
        MenuPath menuPath = new MenuPath(action.getPath(), new MenuPath(element));
        String path = menuPath.getPathString();
        writer.write("<button id='showDropDown' onclick=\"javascript:showActionOptions('");
        writer.write(path);
        writer.write("_dropdown_loc','");
        writer.write(path);
        writer.write("_drpDwn')\">");
        writer.write(action.getLocaleText(TolvenRequest.getInstance().getResourceBundle()));
        writer.write("</button>");
        writer.write("<element id='");
        writer.write(path);
        writer.write("_dropdown_loc' style='position:relative;'/>");
        writer.write("<div id='");
        writer.write(path);
        writer.write("_drpDwn' class='drpDwn' style='display:none;'>");
        writer.write("</div>");
    }

    public static void createHandlerActionButtons(MSAction action, String element, Writer writer) {
        String menuEventHandlerFactoryClassname = action.getMenuEventHandlerFactory();
        Class<?> menuEventHandlerFactoryClass = null;
        try {
            menuEventHandlerFactoryClass = Class.forName(menuEventHandlerFactoryClassname);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            //Print the stack trace, dump the button and return adding no button
            return;
        }
        try {
            MenuEventHandler menuEventHandler = MenuEventHandler.createMenuEventHandler(menuEventHandlerFactoryClass, action);
            menuEventHandler.setElement(element);
            menuEventHandler.setResourceBundle(TolvenRequest.getInstance().getResourceBundle());
            menuEventHandler.setWriter(writer);
            menuEventHandler.initializeAction();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
    /** Method to find the favorites Lists
   	 * @param path - the menuStructure path of the favorites
   	 * @return
   	 */
   	public static List<Map<String, Object>> findFavorites(String element,AccountUser accountUser,MenuLocal menuLocal,MenuPath targetMenuPath) {		
   		List<Map<String, Object>> favorites = new ArrayList<Map<String, Object>>();
   		//check if the query of MenuStructure matches
   		//MenuStructure contextMS = menuLocal.findMenuStructure(accountUser, element);
   		
   		String favoritePathString = accountUser.getProperty().get("favoritePath");
   		if(favoritePathString != null){
   			String[] favoritesPaths = favoritePathString.split(",") ; 
   			for(int i=0;i<favoritesPaths.length;i++){
   				MenuPath favPath = new MenuPath(favoritesPaths[i]);
   				MenuStructure ms;
   				String menuPath = targetMenuPath.getPath();
   				String node = menuPath.substring(menuPath.lastIndexOf(':')+1);
   				//find the target MenuStructure's query to compare it with the favorites available 
   				MenuStructure targetMS = null;
   				if(targetMenuPath.getPath().startsWith("global:"))
   					targetMS =  menuLocal.findGlobalMenuStructure(targetMenuPath.getPath() );
    			else
    				targetMS =  menuLocal.findMenuStructure(accountUser, targetMenuPath.getPath() );
   				String targetQuery = null; 
   				if(targetMS != null && targetMS.getQuery() != null)
   					targetQuery = targetMS.getQuery();
   				
   				try {
   					ms = menuLocal.findMenuStructure( accountUser.getAccount().getId(), favPath.getPath() );
   					MenuQueryControl ctrl = new MenuQueryControl();
   					ctrl.setMenuStructure( ms.getAccountMenuStructure() );
   					ctrl.setAccountUser(accountUser);
   					ctrl.setNow( new Date() );
   					ctrl.setOriginalTargetPath( favPath);
   					ctrl.setRequestedPath( favPath );
   					MDQueryResults data= menuLocal.findMenuDataByColumns(ctrl); 
   					
   					if(data != null){
   						//identify the favorites type
   						for(Map<String, Object> result:data.getResults()){
   							if(element.indexOf((String)result.get("referencePath"))> -1){	
   								node = (String)result.get("Type");
   								break;
   							}else{// check if the query attribute on menustructures of favorite list and requested menustructure matches
   								String accountType = accountUser.getAccount().getAccountType().getKnownType();
   								MenuStructure favoriteTypeMS = menuLocal.findMenuStructure(accountUser, accountType+":"+result.get("Type"));
   								if(targetQuery != null && favoriteTypeMS != null && favoriteTypeMS.getQuery() != null && favoriteTypeMS.getQuery().equals(targetQuery)){
   									node = (String)result.get("Type");
   								}
   							}
   						}				
   						// pick only the elligible favorites
   						for(Map<String, Object> result:data.getResults()){
   							if(result.get("Type").equals(node) )
   								favorites.add(result);
   						}
   					}
   				} catch (Exception e) {
   					TolvenLogger.info( "Error finding favorties for "+targetMenuPath.getPath(), MenuAction.class);
   					e.printStackTrace();
   				}
   			}
   			
   		}		
   		return favorites;
   	}

}
