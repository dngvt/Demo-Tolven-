package org.tolven.trim.scan;

import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.trim.Act;
import org.tolven.trim.ActParticipation;
import org.tolven.trim.ActRelationship;
import org.tolven.trim.Observation;
import org.tolven.trim.Trim;
import org.tolven.trim.ex.ActEx;
import org.tolven.trim.ex.ObservationEx;
import org.tolven.trim.ex.TrimEx;

/**
 * Process a trim graph during the parse process. This process primarily expands the extends element of the trim
 * and include elements in actRelationships and participations. It also includes valueSet components.
 * @author John Churin
 */
public class ExtensionScanner extends Scanner {
	private TrimEx targetTrim = null;
	private TrimExpressionEvaluator ee;

	@Override
	public void scan() {
		ee = new TrimExpressionEvaluator();
		ee.addVariable("trim", this.getTargetTrim());
		super.scan();
	}

	/**
	 * <p>Extend the supplied target trim by scanning the extendsTrim and offering to supply missing components to the target trim.</p>
	 * <p>Extending is very different from instantiation and binding which is done after this step.</p>
	 */
	@Override
	protected void preProcessTrim(Trim trim) {
		super.preProcessTrim(trim);
		((TrimEx)targetTrim).blend(trim);
	}

	public Trim getTargetTrim() {
		return targetTrim;
	}

	public void setTargetTrim(Trim targetTrim) {
		this.targetTrim = (TrimEx) targetTrim;
	}

	
}
