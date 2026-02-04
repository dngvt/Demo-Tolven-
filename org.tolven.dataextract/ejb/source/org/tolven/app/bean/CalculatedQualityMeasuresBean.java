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

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.tolven.app.CalculatedQualityMeasuresInterface;
import org.tolven.app.GeneratePQRIXMLInterface;
import org.tolven.app.DisplayMeasure;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.AccountUser;

@Stateless()
@Local(CalculatedQualityMeasuresInterface.class)
public class CalculatedQualityMeasuresBean extends DisplayMeasureBean implements CalculatedQualityMeasuresInterface  {
	
	@EJB
	private GeneratePQRIXMLInterface generatePQRIXML;
	
	public List<DisplayMeasure> getMeasures(){
		List<DisplayMeasure> list = new ArrayList<DisplayMeasure>();
		try{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource)ic.lookup("TolvenDS");
			Connection connection = ds.getConnection();
			Long accountId = TolvenRequest.getInstance().getAccount().getId();			
			
			CallableStatement proc = connection.prepareCall("{ ? = call calculatedmeasure(" + accountId + ") }");
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
	
	public String getPQRI(List<DisplayMeasure> measures) {
		String pqri = "";
		try {
			//Modify the list of measures so they don't include the full name and only the NQF number
			List<DisplayMeasure> truncMeasures = truncateMeasureNames(measures);
			pqri = generatePQRIXML.generatePQRI_XML(truncMeasures);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return pqri;
	}
	
	private List<DisplayMeasure> truncateMeasureNames(List<DisplayMeasure> measures) {
		List<DisplayMeasure> tm = new ArrayList<DisplayMeasure>();
		for (DisplayMeasure m : measures) {			
			String [] measureName = m.getMeasureName().split(" ");
			String newMeasureName = measureName[0] + " " + measureName[1];
			boolean parens = false;
			for (String part : measureName) {
				if(part.contains("(")) {
					newMeasureName = newMeasureName.concat(" " + part);
					parens = true;
				} else if (parens) {
					newMeasureName = newMeasureName.concat(part);
				}
			}
			DisplayMeasure newm = new DisplayMeasure(newMeasureName);
			newm.setAccountId(m.getAccountId());
			newm.setDenominator(m.getDenominator());
			newm.setNumerator(m.getNumerator());
			newm.setPercentage(m.getPercentage());
			tm.add(newm);
		}
		return tm;
	}
	
	
	
}
