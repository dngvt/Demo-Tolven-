package test.org.tolven.app;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.tolven.app.bean.ConfigChangeBean;
import org.tolven.app.entity.DataMigrationLog;

public class ConfigChangeBeanTest extends TestCase {
	
	private ConfigChangeBean changeBean;
	private EntityManager em;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("jpa-pu");
        this.em = factory.createEntityManager();
        changeBean = new ConfigChangeBean();
        changeBean.setEm(em);
       
	}
	public void testFindMigrationLog(){
		long accountId = 16000;
		List<DataMigrationLog> list = changeBean.findMigrationChangeLog(16000);
		assertTrue(list.size() > 0);
	}
	
}
