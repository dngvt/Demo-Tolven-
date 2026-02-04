package org.tolven.app.bean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import org.tolven.app.DisplayMeasure;
import org.tolven.app.EncounterMeasure;
import org.tolven.app.GeneratePQRIXMLInterface;
import org.tolven.app.HCQMMeasure;
import org.tolven.app.HCQMMeasuresInterface;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenRequest;

@Stateless()
@Local(HCQMMeasuresInterface.class)
public class HCQMMeasuresBean extends DisplayMeasureBean implements HCQMMeasuresInterface {

	private final String N = "N";
	private final String D = "D";
	private final String X = "X";

	@EJB
	private GeneratePQRIXMLInterface generatePQRIXML;
	
	 private List<String> pqriColumnHeadings;
	    
	    public List<String> getPqriColumnHeadings() {
	    	//[From (date01), Measure (string01), MRN (string05), Result (string02), To (date02), Value (pqValue01)]
	    	if(pqriColumnHeadings == null) {
	    		pqriColumnHeadings = new ArrayList<String>();
	    		pqriColumnHeadings.add("From (date01)");
	    		pqriColumnHeadings.add("Measure (string01)");
	    		pqriColumnHeadings.add("MRN (string05)");
	    		pqriColumnHeadings.add("Result (string02)");
	    		pqriColumnHeadings.add("To (date02)");
	    		pqriColumnHeadings.add("Value (pqValue01)");
	    	}
			return pqriColumnHeadings;
		}


	public List<DisplayMeasure> getMeasures(){
		List<HCQMMeasure> list = new ArrayList<HCQMMeasure>();
		try{
			 MenuQueryControl ctrl = new MenuQueryControl();
			 MenuStructure ms = getMenuBean().findMenuStructure(TolvenRequest.getInstance().getAccount(), "echr:admin:hcqmresults:all");
		        ctrl.setMenuStructure(ms);
		        ctrl.setAccountUser(TolvenRequest.getInstance().getAccountUser());
		        ctrl.setNow(TolvenRequest.getInstance().getNow());		  
		        ctrl.setLimit(-1);
			List<MenuData> mdList = getMenuBean().findMenuData(ctrl);
			for(MenuData md : mdList) {
				
				HCQMMeasure m = new HCQMMeasure(md.getString01(),md.getString02());
				
				if(md.getDate01String() != null) {
					m.setDateFrom(md.getDate01String());					
				}
				if(md.getDate02String() != null) {
					m.setDateTo(md.getDate02String());					
				}
				if(md.getString05() != null) {
					m.setMrn(md.getString05());					
				}
				if(md.getPqStringVal01() != null) {
					m.setValue(md.getPqStringVal01());					
				}
				list.add(m);
			}			
	
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
		List<EncounterMeasure> ems = groupHCQMMeasuresByNumber(list); 
		List<DisplayMeasure> measures = convertEncounterMeasuresToMeasures(ems);		
		
		return measures;
	}	
	
	private List<DisplayMeasure> convertEncounterMeasuresToMeasures(List<EncounterMeasure> ems) {
		List<DisplayMeasure> measures = new ArrayList<DisplayMeasure>();
		for (EncounterMeasure em : ems) {
			DisplayMeasure newM = new DisplayMeasure(em.getMeasureName(), em.getPqrimeasurenumber());
			
			//set flag if this is an ED calculation
			boolean calcED = false;
			if (em.getHcqmMeasures().size() > 0	&& em.getHcqmMeasures().get(0).getMeasureName().startsWith("ED")) {
				calcED = true;
				newM.setEmergencyDepartmentMeasure(Boolean.TRUE);
			}
			
			//set accountid
			newM.setAccountId(String.valueOf(TolvenRequest.getInstance().getAccount().getId()));
			
			//set denominator
			newM.setDenominator(calcEligibleInstances(em).toString());
			
			//Set numerator
			if (calcED) {
				newM.setNumerator(calcAvgWaitTime(em).toString());
			} else {
				newM.setNumerator(String.valueOf(em.getTotalN()));
			}
			
			//set exclusions
			newM.setExclusions(em.getTotalX());
			
			//set performances not met
			newM.setPerformanceNotMet(em.getTotalD());
			
			//set percentage
			if (calcED) {
				newM.setPercentage(String.valueOf(0).concat("%"));
			} else {
				newM.setPercentage(calcPerformanceRate(newM).toString().concat("%"));
			}
			measures.add(newM);
		}
		return measures;
	}
	
	private List<EncounterMeasure> groupHCQMMeasuresByNumber(List<HCQMMeasure> hcqmMeasures) {
		
		List<EncounterMeasure> ems = new ArrayList<EncounterMeasure>();
		
		for(HCQMMeasure hm : hcqmMeasures) {
			
			if(ems.size() == 0) {				
				ems.add(createNewEncounterMeasure(hm));
			
			} else {
				boolean matchedExistingEm = false;
				for(EncounterMeasure aem : ems) {
					if(aem.getPqrimeasurenumber().equalsIgnoreCase(hm.getMeasureNumber())) {
						//just add to the counts of this em
						aem = addCountsToEncouterMeasure(aem, hm);
						//also add this hcqm measure to the list on this em
						aem.getHcqmMeasures().add(hm);
						matchedExistingEm = true;
						break;
					} else {
						continue;
					}
				}
				if(!matchedExistingEm) {
					//then we need to add a new EM because an existing one wasnt found
					ems.add(createNewEncounterMeasure(hm));
				}
			}
		}
		
		return ems;
		
	}
	
	private EncounterMeasure addCountsToEncouterMeasure(EncounterMeasure em, HCQMMeasure hm) {
		//Add the value of the result of the HM to the EM 
		if (hm.getResult().equals(X)) {
			em.setTotalX(em.getTotalX()+1);
		} else if (hm.getResult().equals(D)) {
			em.setTotalD(em.getTotalD()+1);
		} else if (hm.getResult().equals(N)) {
			em.setTotalN(em.getTotalN()+1);
		}
		return em;
	}
	

	private EncounterMeasure createNewEncounterMeasure(HCQMMeasure hm) {
		List<HCQMMeasure> hms = new ArrayList<HCQMMeasure>();
		hms.add(hm);
		EncounterMeasure em = new EncounterMeasure(hm.getMeasureName(), hm.getMeasureNumber(), hms);
		em = addCountsToEncouterMeasure(em, hm);
		return em;
	}
	
	private Integer calcEligibleInstances(EncounterMeasure em) {
		// X + D + N
		return Integer.valueOf(em.getTotalX() + em.getTotalD() + em.getTotalN());
	}

	private Integer calcPerformanceRate(DisplayMeasure measure) {
		// Number of ‘N’s / Number of ‘N’s + Number of ‘D’s or Number of ‘N’s/
		// eligible – exclusions.
		// Performance rate for most items will be meets-performance-instances/
		// eligible-instances - performance-exclusion-instances
		double num = Double.valueOf(measure.getNumerator());
		double dem = Double.valueOf(measure.getDenominator()) - measure.getExclusions();
		if (dem != 0) {
			double calc = num/dem;
			calc = calc*100;		
			String calcStr = String.valueOf(calc);
			calcStr = calcStr.substring(0, calcStr.indexOf("."));			
			return Integer.valueOf(calcStr);
		} else {
			return 0;
		}
	}

	private Integer calcAvgWaitTime(EncounterMeasure em) {
		double edTotalWaitTime = 0;
		for (HCQMMeasure hm : em.getHcqmMeasures()) {
			if (hm.getResult().equals(D)) {
				edTotalWaitTime += Double.valueOf(hm.getValue());
			}
		}
		double edMeasureCount = em.getTotalD();
		if (edMeasureCount > 0) {
			double avgValue = Double.valueOf(edTotalWaitTime / edMeasureCount);
			return (int) Math.round(avgValue);
		} else {
			return 0;
		}
	}
	
/*	public String getPQRI(List<HCQMMeasure> hcqmMeasures) {
		String pqri = "";
		try {
			pqri = generatePQRIXML.genHCQM_PQRI(hcqmMeasures);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return pqri;
	}
*/

	@Override
	public String getPQRI(List<DisplayMeasure> measures) {
		String pqri = "";
		try {
			pqri = generatePQRIXML.generatePQRI_XML(measures);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return pqri;	}
	
	
	 
}
