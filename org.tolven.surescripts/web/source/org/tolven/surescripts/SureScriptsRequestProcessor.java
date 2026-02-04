package org.tolven.surescripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivateKey;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tolven.core.TolvenPropertiesLocal;
import org.tolven.logging.TolvenLogger;
import org.tolven.security.key.UserPrivateKey;
import org.tolven.session.TolvenSessionWrapper;
import org.tolven.session.TolvenSessionWrapperFactory;


@WebServlet(urlPatterns = { "/ClientInbox" }, loadOnStartup = 5)
public class SureScriptsRequestProcessor extends HttpServlet{

	private static final long serialVersionUID = -4065992237245968776L;
	@EJB private SurescriptsLocal sureBean;
	@EJB
    private TolvenPropertiesLocal propertyBean;

	/**
	 * Init Method
	 */
	public void init(ServletConfig config) throws ServletException {
		
	}	
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		TolvenLogger.info("Request received from SURESCRIPTS. SurescriptsRequestProcessor", SureScriptsRequestProcessor.class);
		BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
		String inputLine;
		String keyAlgorithm = propertyBean.getProperty(UserPrivateKey.USER_PRIVATE_KEY_ALGORITHM_PROP);
        TolvenSessionWrapper sessionWrapper = TolvenSessionWrapperFactory.getInstance();
        PrivateKey userPrivateKey = sessionWrapper.getUserPrivateKey(keyAlgorithm);


        PrintWriter pwr = response.getWriter();
        StringBuffer xmlMessage = new StringBuffer();
        Timer timer = new Timer(24000);						//ADDED FOR CONNECTION RESET
		timer.setServletThread(Thread.currentThread());		//ADDED FOR CONNECTION RESET
		timer.start();										//ADDED FOR CONNECTION RESET
		while ((inputLine = in.readLine()) != null) {
			xmlMessage.append(inputLine);
			if(!timer.isAlive()) {							//ADDED FOR CONNECTION RESET
				break;										//ADDED FOR CONNECTION RESET
			}
		}
		in.close();
		//TODO pass accountId
		String result = sureBean.convertToMessage(xmlMessage.toString() , false, "0",userPrivateKey);
		TolvenLogger.info("Result from the validation."+ result, SureScriptsRequestProcessor.class);
		pwr.write(result);
		pwr.close();
		return;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		TolvenLogger.info("Inside GET.", SureScriptsRequestProcessor.class);
		doPost(request, response);
	}
	
}
