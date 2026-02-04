package org.tolven.app;

import java.util.List;
import java.util.Map;

public interface AutomatedMeasureCalcInterface {
	public Map<String,String> buildSql();
	public List<DisplayMeasure> getMeasures();
}
