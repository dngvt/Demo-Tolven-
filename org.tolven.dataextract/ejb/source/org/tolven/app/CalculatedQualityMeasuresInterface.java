package org.tolven.app;

import java.util.List;
import java.util.Map;

public interface CalculatedQualityMeasuresInterface {
	public Map<String,String> buildSql();
	public List<DisplayMeasure> getMeasures();
	public String getPQRI(List<DisplayMeasure> measures);

}
