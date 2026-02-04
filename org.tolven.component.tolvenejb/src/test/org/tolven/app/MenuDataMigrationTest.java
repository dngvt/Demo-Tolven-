package test.org.tolven.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tolven.app.bean.ConfigChangeBean;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.ConfigChange;
import org.tolven.app.entity.MSColumn;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuStructure;

import junit.framework.TestCase;

public class MenuDataMigrationTest extends TestCase {
	private ConfigChangeBean bean;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		bean = new ConfigChangeBean();
	}
	
	public void testMigrateString01toString02(){
		MenuData md = new MenuData();
		md.setField("string01", "test");
		md.setField("string03", "nochange");
		ConfigChange change = new ConfigChange();
		
		change.setOldLocation("string01");
		change.setNewLocation("string02");
			Map<String, Object> changeTracker = new HashMap<String, Object>();
		bean.recordMenuDataChanges(md, change,changeTracker);
		bean.transformMenuData(md, changeTracker);
		assertNull("Migration failed", md.getField(change.getOldLocation()));
		assertEquals("test", md.getField(change.getNewLocation()));
		assertEquals("nochange", md.getField("string03"));
	}
	
	public void testMigratestring01TOstring03(){
		MenuData md = new MenuData();
		md.setField("string01", "test");
		ConfigChange change = new ConfigChange();
		change.setOldLocation("string01");
		change.setNewLocation("string03");
		Map<String, Object> changeTracker = new HashMap<String, Object>();
		bean.recordMenuDataChanges(md, change,changeTracker);
		bean.transformMenuData(md, changeTracker);
		assertNull("Migration failed", md.getField(change.getOldLocation()));
		assertEquals("test", md.getField(change.getNewLocation()));
	}
	public void testMigratestring01TOlong01(){
		MenuData md = new MenuData();
		md.setField("string01", "20");
		ConfigChange change = new ConfigChange();
		change.setOldLocation("string01");
		change.setNewLocation("long01");
		Map<String, Object> changeTracker = new HashMap<String, Object>();
		bean.recordMenuDataChanges(md, change,changeTracker);
		bean.transformMenuData(md, changeTracker);
		assertNull("Migration failed", md.getField(change.getOldLocation()));
		assertEquals(new Long(20), md.getField(change.getNewLocation()));
	}
	
	public void testMigratelong01Tostring01(){
		MenuData md = new MenuData();
		md.setField("long01", "20");
		ConfigChange change = new ConfigChange();
		change.setOldLocation("long01");
		change.setNewLocation("string01");
		Map<String, Object> changeTracker = new HashMap<String, Object>();
		bean.recordMenuDataChanges(md, change,changeTracker);
		bean.transformMenuData(md, changeTracker);
		assertNull("Migration failed", md.getField(change.getOldLocation()));
		assertEquals("20", md.getField(change.getNewLocation()));
	}
	public void tesSwapping(){
		MenuData md = new MenuData();
		md.setField("string01", "Srini");
		md.setField("string02", "Kandula");
		ConfigChange change = new ConfigChange();
		change.setOldLocation("string01");
		change.setNewLocation("string02");
		ConfigChange change1 = new ConfigChange();
		change1.setOldLocation("string02");
		change1.setNewLocation("string01");
		Map<String, Object> changeTracker = new HashMap<String, Object>();
		bean.recordMenuDataChanges(md, change,changeTracker);
		bean.recordMenuDataChanges(md, change1,changeTracker);
		bean.transformMenuData(md, changeTracker);
		assertEquals("Srini", md.getField("string02"));
		assertEquals("Kandula", md.getField("string01"));
	}
	
	public void testReplaceColumsn(){
		MenuStructure oldMs = new AccountMenuStructure();
		createColumns(oldMs);
	}
	/*public void testMigratestring01TOextended(){
		MenuData md = new MenuData();
		md.setField("string01", "20");
		ConfigChange change = new ConfigChange();
		change.setOldLocation("string01");
		change.setNewLocation("extended");
		bean.migrateMenuData(md, change);
		assertNull("Migration failed", md.getField(change.getOldLocation()));
		assertEquals(new Long(20), md.getField(change.getNewLocation()));
	}
	*/
	
	
	
	
	
	
	
	private void createColumns(MenuStructure oldMs) {
		List<MSColumn> columns = new ArrayList<MSColumn>();
		MSColumn col1 = new MSColumn();
		col1.setHeading("name");
		col1.setInternal("string01");
		//col1.set
		
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}

}
