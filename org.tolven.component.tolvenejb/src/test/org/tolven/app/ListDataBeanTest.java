package test.org.tolven.app;

import java.util.Date;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.tolven.app.bean.ListDataBean;
import org.tolven.app.bean.MenuBean;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.ListQueryControl;
import org.tolven.app.entity.MDQueryResults;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.bean.AccountDAOBean;
import org.tolven.core.bean.TolvenProperties;
import org.tolven.core.entity.Account;
import org.tolven.core.entity.AccountUser;

public class ListDataBeanTest extends TestCase {
	
	private ListDataBean listDataBean;
	private MenuBean menuBean;
	private AccountDAOBean accountBean;
	private TolvenProperties properties;
	private EntityManager em;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("jpa-pu");
        this.em = factory.createEntityManager();
        listDataBean = new ListDataBean();
        listDataBean.setEm(em);
        menuBean = new MenuBean();
        menuBean.setEm(em);
        accountBean = new AccountDAOBean();
        accountBean.setEm(em);
        properties = new TolvenProperties();
        properties.setEm(em);
        accountBean.setPropertyBean(properties);
       
	}
	public void testFindListDataByColumns(){
		long accountId = 1071;
		String userName = "skandula";
		String msPath = "echr:patient:fdbDrugs:all";
		ListQueryControl control = new ListQueryControl();
		Account account = accountBean.findAccount(accountId);
		assertNotNull("No account was found with accountId:"+accountId, account);
		AccountUser accountUser = accountBean.findAccountUser(userName, account.getId());
		assertNotNull("No accountUser was found with accountId:"+accountId +" and username:"+userName, accountUser);
		MenuStructure ms = menuBean.findMenuStructure(account, msPath);
		assertNotNull("No menustructure was found with path:"+msPath +" in account:"+accountId, ms);
		System.out.println("MenuStructure found "+ms);
		MenuPath path = new MenuPath();
		control.setAccountUser(accountUser);
		control.setLimit( 15);
		control.setMenuStructure( ms );
		control.setNow( new Date() );
		control.setOffset( 0 );
		control.setOriginalTargetPath(path);
		control.setRequestedPath(path);
		MDQueryResults results = listDataBean.findDataByColumns(control);
	
		for(Map<String,Object> row:results.getResults()){
			System.out.println(row);
		}
		System.out.println("***************************************");
		
		
	}
	public void testCountListData(){
		long accountId = 1078;
		String userName = "skandula";
		String msPath = "echr:patient:fdbDrugs:all";
		ListQueryControl control = new ListQueryControl();
		Account account = accountBean.findAccount(accountId);
		assertNotNull("No account was found with accountId:"+accountId, account);
		AccountUser accountUser = accountBean.findAccountUser(userName, account.getId());
		assertNotNull("No accountUser was found with accountId:"+accountId +" and username:"+userName, accountUser);
		MenuStructure ms = menuBean.findMenuStructure(account, msPath);
		assertNotNull("No menustructure was found with path:"+msPath +" in account:"+accountId, ms);
		System.out.println("MenuStructure found "+ms);
		MenuPath path = new MenuPath();
		control.setAccountUser(accountUser);
		control.setLimit( 15);
		control.setMenuStructure( ms );
		control.setNow( new Date() );
		control.setOffset( 0 );
		control.setOriginalTargetPath(path);
		control.setRequestedPath(path);
		System.out.println(listDataBean.countListData(control));
		
		
	}
	
}
