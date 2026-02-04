package test.org.tolven.app.el;

import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.core.entity.Account;
import org.tolven.trim.SETIISlot;

import junit.framework.TestCase;

public class ELFunctionTest  extends TestCase {
	TrimExpressionEvaluator te;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		te = new TrimExpressionEvaluator();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	public void testFunctionPlaceHolder(){
		Account account = new Account();
		SETIISlot path = new SETIISlot();
		String slot = new String();
		te.addVariable("account", account);
		te.addVariable("slot", slot);
		te.addVariable("path", path);
		te.evaluate("#{placeholder(account,slot)}");
		te.evaluate("#{placeholder(account,path)}");
		//if the above code is executed the test is passed
		assertEquals(true, true);
	}
	
}
