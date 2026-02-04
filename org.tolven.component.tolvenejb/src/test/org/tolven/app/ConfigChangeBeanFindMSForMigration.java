package test.org.tolven.app;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.tolven.app.bean.ConfigChangeBean;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.core.bean.AccountDAOBean;
import org.tolven.core.entity.Account;

public class ConfigChangeBeanFindMSForMigration extends TestCase {
	
	private ConfigChangeBean changeBean;
	private EntityManager em;
	private AccountDAOBean accountBean;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("jpa-pu");
        this.em = factory.createEntityManager();
        changeBean = new ConfigChangeBean();
        changeBean.setEm(em);
        accountBean = new AccountDAOBean();
        accountBean.setEm(em);
       
	}
	public void testFindMenuStructureToMigrate(){
		Account account = accountBean.findAccount(8003);
		List<AccountMenuStructure> list = changeBean.findMenuStructureToMigrate(account);
		assertTrue(list.size() > 0);
	}
	
}
