package test.org.tolven.app;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.tolven.app.bean.MenuBean;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.bean.AccountDAOBean;
import org.tolven.core.entity.Account;

public class MenuBeanTest extends TestCase {
	
	private MenuBean menuBean;
	private EntityManager em;
	private AccountDAOBean accountDAOBean;
	private long ACCOUNT_ID = 1071;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("jpa-pu");
        this.em = factory.createEntityManager();
        menuBean = new MenuBean();
        menuBean.setEm(em);
        
        accountDAOBean = new AccountDAOBean();
        accountDAOBean.setEm(em);
        
    }
	public void testFindMenuDataItem(){
		//the hard coded ids need to be changed in order to run the tests in development environment
		MenuData md = menuBean.findMenuDataItem(Long.parseLong("6242"));
		assertNotNull(md);
		System.out.println(new String(md.getXml01()));
		//assertNotNull(menuBean.findMenuDataItem(73008,"documentMenu-182017"));
	}
	public void testFindListContents(){
		Account account = accountDAOBean.findAccount(ACCOUNT_ID);
		MenuStructure ms = menuBean.findMenuStructure(account, "echr:admin:staff:all");
		List<MenuData> list = menuBean.findListContents(ms.getAccount() , ms, null) ;
		assertEquals(true, list.size() > 0);
		System.out.println("found staff in echr:admin:staff:all: "+list.size());
		ms = menuBean.findMenuStructure(account, "echr:patient:encounters:active");
		MenuData md = menuBean.findMenuDataItem(6242);
		list = menuBean.findListContents(ms.getAccount() , ms, md) ;
		assertEquals(true, list.size() > 0);
		System.out.println("found items in list echr:patient-6242:encounters:active: "+list.size());
		
	}
	 
	
	
}
