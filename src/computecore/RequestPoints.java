/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Niklas Schnelle
 * 
 */
public class RequestPoints extends Points {
	private final ArrayList<Map<String, Object>> pConstraints;

	public RequestPoints() {
		super();
		pConstraints = new ArrayList<Map<String, Object>>();
	}

	public void addPoint(int lat, int lon, Map<String, Object> pconst) {
		super.addPoint(lat, lon);
		pConstraints.add(pconst);
	}

	/**
	 * Gets the Constraint with the name conName for point i
	 * 
	 * @param i
	 * @param conName
	 * @return
	 */
	public Object getConstraint(int i, String conName) {
		return (pConstraints.get(i) != null) ? pConstraints.get(i).get(conName)
				: null;
	}

	/**
	 * Gets the Constraints of the point i
	 * 
	 * @param i
	 * @return
	 */
	public Map<String, Object> getConstraints(int i) {
		return (pConstraints.get(i) != null) ? pConstraints.get(i) : null;
	}

	@Override
	public void addEmptyPoints(int num) {
		super.addEmptyPoints(num);
		// TODO: ugly please improve
		for (int i = 0; i < num; i++) {
			pConstraints.add(null);
		}
	}

	@Override
	public void addPoint(int lat, int lon) {
		super.addPoint(lat, lon);
		pConstraints.add(null);

	}
}
