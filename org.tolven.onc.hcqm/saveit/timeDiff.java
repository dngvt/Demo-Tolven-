package com.myorg.trim;
import java.text.*;  
import java.util.*;

import org.tolven.trim.ex.ObservationValueSlotEx;
import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.process.ComputeBase;
import org.tolven.trim.Act;

public class timeDiff extends ComputeBase {
	private String destination;
	private String dtetimeA;
	private String dtetimeD;

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDtetimeA() {
		return dtetimeA;
	}

	public void setDtetimeA(String dtetimeA) {
		this.dtetimeA = dtetimeA;
	}

	public String getDtetimeD() {
		return dtetimeD;
	}

	public void setDtetimeD(String dtetimeD) {
		this.dtetimeD = dtetimeD;
	}

	@Override
	public void compute() throws Exception {
		DecimalFormat df2 = new DecimalFormat( "###,##0.00" );
		TrimExpressionEvaluator ee = new TrimExpressionEvaluator();
		ee.addVariable("trim", getTrim());
		ee.addVariable("account", getAccountUser().getAccount());
		if (getNode() instanceof Act) {
			ee.addVariable("act", getNode());
		}
	//	ObservationValueSlotEx a = (ObservationValueSlotEx) ee.evaluate(getDtetimeD());
	//	Date dtetimeD_d = new GregorianCalendar(a.getTS().getOriginalText());
	//	ObservationValueSlotEx d = (ObservationValueSlotEx) ee.evaluate(getDtetimeA());
	//	Date dtetimeA_d = new GregorianCalendar(d.getTS().getOriginalText());


        Date d1 = new GregorianCalendar(2008, 5, 20, 15, 30, 44).getTime();  
        Date d2 = new GregorianCalendar(2008, 5, 20, 15, 10, 44).getTime();	
        SimpleDateFormat dfm = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
    //   System.out.println(dfm.format(d1));  
    //   System.out.println(dfm.format(d2));            
        long td_l = ( Math.abs( d1.getTime() - d2.getTime()) / 1000 ) / 60;  		  	  
		ee.setValue(getDestination(), new Long(df2.format(td_l)).longValue());
	}
}



