package org.tolven.ajax;

import java.io.IOException;
import java.io.Writer;
import java.security.Principal;
import java.text.Format;
import java.text.SimpleDateFormat;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tolven.api.security.GeneralSecurityFilter;
import org.tolven.app.MenuLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.core.entity.AccountUser;

@WebServlet(urlPatterns = { "*.ajaxenc" }, loadOnStartup = 5)
public class EncounterServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	@EJB
    private MenuLocal menuBean;
  
    public void init(ServletConfig config) throws ServletException {
		try
        {

        }
        catch (Exception e)
        {
           throw new RuntimeException(e);
        }
		
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String uri = req.getRequestURI();
    	Writer writer = resp.getWriter();
    	AccountUser activeAccountUser = (AccountUser) req.getAttribute(GeneralSecurityFilter.ACCOUNTUSER);    	
    	StringBuffer response = new StringBuffer("{");
    	if(uri.endsWith("getEncounter.ajaxenc")){
    		try{
    			MenuData md = null;
    			Format format = new SimpleDateFormat("MMM-dd-yyyy hh:mm aaa");
            	String path = req.getParameter("path");
            	if(path != null && path.length() > 0){
            		md = menuBean.findMenuDataItem(activeAccountUser.getAccount().getId(), path);
            		String admitDate="",dischargeDate="",location="";
            		String encounterType = md.getField("Purpose").toString();
            		response.append(String.format("'%s':'%s'", "EncounterType",encounterType));
            		Object startDate = md.getReference().getField("effectiveTimeLow");;
            		Object toDate = md.getReference().getField("effectiveTimeHigh");
            		Object loc = md.getField("Location");
            		if(startDate != null){
            			admitDate = format.format(startDate);
            			response.append(",");
                		response.append(String.format("'%s':'%s'", "AdmitDate",admitDate));
            		}
            		if(toDate !=null){
            			dischargeDate = format.format(toDate);
            			response.append(",");
                		response.append(String.format("'%s':'%s'", "DischargeDate",dischargeDate));
            		}
            		if(loc != null){
            			location = loc.toString();
            			response.append(",");
                		response.append(String.format("'%s':'%s'", "Location",location));
            		}
            		
            	}else{
            		response.append(String.format("'%s':'%s'", "EncounterType",""));
            		response.append(",");
            		response.append(String.format("'%s':'%s'", "AdmitDate",""));
            		response.append(",");
            		response.append(String.format("'%s':'%s'", "DischargeDate",""));
            		response.append(",");
            		response.append(String.format("'%s':'%s'", "Location",""));
            	}
            	response.append("}");
            	writer.write(response.toString());
        		req.setAttribute("activeWriter", writer);
    		}catch (Exception e) {}
    	}
	}

}
