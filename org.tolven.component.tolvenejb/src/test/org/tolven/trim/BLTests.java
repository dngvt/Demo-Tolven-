package test.org.tolven.trim;

import java.text.ParseException;

import junit.framework.TestCase;

import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.trim.ex.BLEx;
import org.tolven.trim.ex.TrimFactory;

public class BLTests extends TestCase {
	private static final TrimFactory factory = new TrimFactory( );
	

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
	
	public void testGTS1() throws ParseException {
		BLEx bl = (BLEx) factory.createBL();
		bl.setValue(true);
		te.addVariable("bl", bl);
		
		String ittrue = "#{bl.value}";
		Boolean atest = (Boolean) te.evaluate(ittrue );
		assertTrue(atest);
		//String atest = (String) te.evaluate(ittrue);
		//assertEquals(atest, "true");
		
		String isittrue = "#{bl.value?'true':'false'}";
		String thetest = (String) te.evaluate(isittrue);
		//Boolean thetest = (Boolean) te.evaluate(isittrue );
		assertEquals(thetest, "true");
		
	/*	String istrue = "#{bl.value=='true'}";
		String test = (String) te.evaluate(istrue);
		//Boolean test = (Boolean) te.evaluate(istrue);
		assertEquals(test, "true");
	
		*/
		/*
		#{myboolean.BL.value='true'} 
		#{myboolean.BL.value?true:false} 
		and 
		#{myboolean.BL.value} */
		
	}
	
}
