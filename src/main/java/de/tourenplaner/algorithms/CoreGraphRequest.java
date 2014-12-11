package de.tourenplaner.algorithms;

import de.tourenplaner.computecore.RequestData;

import java.util.Map;

/**
 * RequestData for CoreGraph's that only contains the constraints of the core graph, points
 * are not required because CoreGraphs are global
 */
public class CoreGraphRequest extends RequestData {
	private Map<String, Object> constraints;
	/**
	 * Returns the constraints associated with this request
	 *
	 * @return A Map representing the constraints
	 */
	public Map<String, Object> getConstraints() {
		return constraints;
	}

	public CoreGraphRequest (String algSuffix, Map<String, Object> constraints) {
		super(algSuffix);
		this.constraints = constraints;
	}
}
