package org.tolven.app;

import java.util.List;

public interface GeneratePQRIXMLInterface {
	//public String genHCQM_PQRI(List<HCQMMeasure> hcqmMeasures);
	public String generatePQRI_XML(List<DisplayMeasure> measures);
}
