package org.tolven.app.bean;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.tolven.app.AppResolverLocal;
import org.tolven.app.DataExtractLocal;
import org.tolven.app.DataQueryResults;
import org.tolven.app.MenuLocal;
import org.tolven.app.PlaceholderAssociationLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.PlaceholderAssociation;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountUser;
import org.tolven.core.entity.TolvenUser;


@Local(AppResolverLocal.class)
public @Stateless
class AppResolverBean implements URIResolver,AppResolverLocal {

	@EJB
    private DataExtractLocal dataExtractBean;
	@EJB
    private MenuLocal menuBean;
	@EJB
	private PlaceholderAssociationLocal associationBean;
	private AccountUser accountUser;
	private String appSchema;
	
	public String getAppSchema() {
		this.accountUser = getAccountUser();
		this.appSchema = this.accountUser.getAccount().getAccountType().getKnownType() + ":";
		return appSchema;
	}

	public void setAppSchema(String appSchema) {
		this.appSchema = appSchema;
	}
	
	Date now;
	private Logger logger = Logger.getLogger(this.getClass());
	
	public AppResolverBean() {
		this.now = new Date();
	}  
	

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		logger.info("href: " + href + " base: " + base);
		// See if URL starts with the account type + ":"
		
		try {
			if (href.startsWith(getAppSchema())) {
				DataQueryResults dataQueryResults = dataExtractBean.setupQuery(href, getAccountUser()); //.substring(appSchema.length()
		        dataQueryResults.setNow(now);
		        dataQueryResults.enableAllFields();
		        dataQueryResults.setReturnTotalCount(true);
		        dataQueryResults.setReturnFilterCount(false);
		        dataQueryResults.setLimit(10);
		        
		        boolean isItemQuery = true;  //works for Patient data and info
		        if(href.split(":").length > 3) {
		        	isItemQuery = false;  //works for lists like Allergies and problems
		        }
		        dataQueryResults.setItemQuery(isItemQuery);
		        StringWriter buffer = new StringWriter();
				dataExtractBean.streamResultsXML(buffer, dataQueryResults);
				Reader reader = new StringReader(buffer.toString());
				return new StreamSource( reader );
			} else if (href.equalsIgnoreCase("context")) {
				return createContext();
			} else if (href.startsWith("sql:")) { // SQL string from xslt
				String sql = href.substring("sql:".length());
				StringWriter buffer = new StringWriter();
				dataExtractBean.streamResultsSQL(buffer, sql);
				Reader reader = new StringReader(buffer.toString());
				if(logger.isDebugEnabled())
					logger.debug(" SQL: "+sql+"    result:"+buffer.toString());
				return new StreamSource( reader );
			}else if (href.startsWith("include:")) {
				return new StreamSource( new URL(href.substring("include:".length())).openStream());
			} else if (href.startsWith("property:")) {
				String propertyName = href.substring("property:".length());
				String property = TolvenRequest.getInstance().getAccountUser().getProperty().get(propertyName);
				return new StreamSource(new StringReader(property));
			} else if (href.startsWith("associations:")) {
				String propertyName = href.substring("associations:".length());
				String[] params = propertyName.split("~");
				if(params.length == 2){
				MenuData placeholder = menuBean.findMenuDataItem(TolvenRequest.getInstance().getAccountUser().getAccount().getId(), params[0]);
				List<PlaceholderAssociation> associations = associationBean.findPlaceholderAssociations(placeholder,params[1]);
				List<Long> list = new ArrayList<Long>();
				for(PlaceholderAssociation association:associations){
					list.add(association.getAssociatedPlaceholder().getId());
				}
				if(list.size() > 0){
					MenuData md = (MenuData)associations.get(0).getAssociatedPlaceholder();							
					DataQueryResults dataQueryResults = new DataQueryResults(md.getMenuStructure());
					dataQueryResults.setMenuPath(new MenuPath(md.getMenuStructure().getPath()));
					dataQueryResults.setNow(now);
			        dataQueryResults.enableAllFields();
			        dataQueryResults.setReturnTotalCount(false);
			        dataQueryResults.setReturnFilterCount(false);
			        dataQueryResults.setLimit(100);
			        dataQueryResults.setFilteredList(list);
			        dataQueryResults.setItemQuery(false);
			        StringWriter buffer = new StringWriter();
			 		dataExtractBean.streamResultsXML(buffer, dataQueryResults);
					Reader reader = new StringReader(buffer.toString());
					return new StreamSource( reader );
				}
				}
			}else {
				return new StreamSource( new URL(href).openStream());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	

	public AccountUser getAccountUser() {
		return TolvenRequest.getInstance().getAccountUser();
	}

	public void setAccountUser(AccountUser accountUser) {
		this.accountUser = getAccountUser();
		this.appSchema = accountUser.getAccount().getAccountType().getKnownType() + ":";
		this.now = new Date();
	}
	
	public Date getNow() {
		return now;
	}

	public void setNow(Date now) {
		this.now = now;
	}

	protected void sendAccount(XMLStreamWriter xmlStreamWriter, Account account) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("account");
        xmlStreamWriter.writeAttribute("id", String.valueOf(account.getId()));
        xmlStreamWriter.writeAttribute("title", account.getTitle());
        xmlStreamWriter.writeEndElement();
	}

	protected void sendAccountUser(XMLStreamWriter xmlStreamWriter, AccountUser accountUser) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("accountUser");
        xmlStreamWriter.writeAttribute("id", String.valueOf(accountUser.getId()));
        xmlStreamWriter.writeEndElement();
	}

	protected void sendUser(XMLStreamWriter xmlStreamWriter, TolvenUser user) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("user");
        xmlStreamWriter.writeAttribute("id", String.valueOf(user.getId()));
        xmlStreamWriter.writeAttribute("uid", user.getLdapUID());
//        xmlStreamWriter.writeAttribute("sn", user.getXXX());
        xmlStreamWriter.writeEndElement();
	}
	
	protected void sendAccountIdRoot(XMLStreamWriter xmlStreamWriter, AccountUser accountUser) throws XMLStreamException {
		String accountIdRoot = System.getProperty("tolven.repository.oid")+ ".1." + accountUser.getAccount().getId();
        xmlStreamWriter.writeStartElement("accountIdRoot");
        xmlStreamWriter.writeAttribute("id", accountIdRoot);
        xmlStreamWriter.writeEndElement();
	}
	
	
	
	protected Source createContext() {
        StringWriter buffer = new StringWriter();
    	try {
            XMLStreamWriter xmlStreamWriter = null;
            try {
                XMLOutputFactory factory = XMLOutputFactory.newInstance();
                xmlStreamWriter = factory.createXMLStreamWriter(buffer);
                xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
                xmlStreamWriter.writeStartElement("context");
				GregorianCalendar nowCal = new GregorianCalendar();
				nowCal.setTime(getNow());
				DatatypeFactory xmlFactory = DatatypeFactory.newInstance();
				XMLGregorianCalendar ts = xmlFactory.newXMLGregorianCalendar(nowCal);
				xmlStreamWriter.writeAttribute("now", ts.toXMLFormat());
				sendUser(xmlStreamWriter, accountUser.getUser());
				sendAccount(xmlStreamWriter, accountUser.getAccount());
				sendAccountUser(xmlStreamWriter, accountUser);
				sendAccountIdRoot(xmlStreamWriter, accountUser);
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeEndDocument();
            } finally {
                if (xmlStreamWriter != null) {
                    xmlStreamWriter.close();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not create context element", ex);
        }
        Reader reader = new StringReader(buffer.toString());
		return new StreamSource( reader );
	}

}
