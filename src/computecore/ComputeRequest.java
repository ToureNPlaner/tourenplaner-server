/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.Map;

import server.Responder;
import algorithms.Points;

/**
 * This class is used to represent a request for computation
 * 
 * @author Niklas Schnelle, Peter Vollmer
 * 
 */
public class ComputeRequest {

	private Points points;
	private Points resultPoints;
	private Map<String, Object> constraints;
	private Map<String, Object> misc;
	private String algName;
	private Responder responder;

	/**
	 * Constructs a new ComputeRequest using the given Responder, Points and
	 * Constraints
	 * 
	 * @param responder
	 * @param algName
	 * @param request
	 */
	public ComputeRequest(Responder responder, String algName, Points points,
			Map<String, Object> constraints) {
		this.algName = algName;
		this.points = points;
		this.resultPoints = new Points();
		this.constraints = constraints;
		this.responder = responder;
		this.misc = null;
	}

	/**
	 * Gets the responder object which is used to send the result to the correct
	 * client connection
	 * 
	 * @return
	 */
	public Responder getResponder() {
		return responder;
	}

	/**
	 * Gets the URLSuffix for the requested algorithmm e.g. "sp" for a shortest
	 * path algorithm
	 * 
	 * @return
	 */
	public String getAlgorithmURLSuffix() {
		return algName;
	}

	/**
	 * Returns the Points object associated with this request
	 * 
	 * @return
	 */
	public Points getPoints() {
		return points;
	}

	/**
	 * Returns the Points object used to store the result of this request
	 * 
	 * @return
	 */
	public Points getResultPoints() {
		return resultPoints;
	}

	/**
	 * Returns the constraints associated with this request
	 * 
	 * @return
	 */
	public Map<String, Object> getConstraints() {
		return constraints;
	}

	/**
	 * Returns the misc field used to store results, initially this is null
	 * 
	 * @return
	 */
	public Map<String, Object> getMisc() {
		return misc;
	}

	public void setMisc(Map<String, Object> misc) {
		this.misc = misc;
	}
}
