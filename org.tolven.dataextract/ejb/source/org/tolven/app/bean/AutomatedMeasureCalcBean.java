package org.tolven.app.bean;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.tolven.app.AutomatedMeasureCalcInterface;
import org.tolven.app.DisplayMeasure;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;

@Stateless()
@Local(AutomatedMeasureCalcInterface.class)
public class AutomatedMeasureCalcBean extends DisplayMeasureBean implements AutomatedMeasureCalcInterface {


	public List<DisplayMeasure> getMeasures(){
		List<DisplayMeasure> list = new ArrayList<DisplayMeasure>();
		try{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource)ic.lookup("TolvenDS");
			Connection connection = ds.getConnection();
			Long accountId = TolvenRequest.getInstance().getAccount().getId();			
			CallableStatement proc = connection.prepareCall("{ ? = call automatedmeasure(" + accountId + ") }");
			proc.registerOutParameter(1, Types.OTHER);
			proc.execute();
			ResultSet results = (ResultSet) proc.getObject(1);
			while (results.next()) {
				DisplayMeasure m = new DisplayMeasure(results.getString("account_id"),results.getString("measure"),results.getString("numerator"),results.getString("denominator"),results.getString("percentage"));
				list.add(m);
			}
			results.close();
			proc.close();
			connection.close();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
		return list;
	}

	
	public Map<String,String> buildSql() {
		AccountUser activeAccountUser = TolvenRequest.getInstance().getAccountUser();
		MenuStructure ms = getMenuBean().findMenuStructure(activeAccountUser.getAccount(),activeAccountUser.getAccount().getAccountType().getKnownType());
		Map<String,String> sqlMap = new HashMap<String,String>();
		Pattern parentPattern = Pattern.compile("parent[0-9][0-9]");
		createSelectSQL(ms,sqlMap,activeAccountUser,parentPattern);
		return sqlMap;
	}
	
}
