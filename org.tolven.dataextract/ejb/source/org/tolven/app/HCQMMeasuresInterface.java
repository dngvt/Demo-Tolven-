package org.tolven.app;

import java.util.List;

public interface HCQMMeasuresInterface {
	
	public List<DisplayMeasure> getMeasures();
	public String getPQRI(List<DisplayMeasure> measures);
}
