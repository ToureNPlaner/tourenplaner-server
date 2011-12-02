/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.Map;

import org.codehaus.jackson.type.TypeReference;

import database.RequestDataset;

import server.Responder;
import algorithms.Points;

/**
 * This class is used to represent a request for computation
 * 
 * @author Niklas Schnelle, Peter Vollmer, Sascha Meusel
 * 
 */
public class ComputeRequest {

	private static final class MapType extends
			TypeReference<Map<String, Object>> {
	};

	@SuppressWarnings("unused")
	private static final MapType JSONOBJECT = new MapType();
	private Points points;
	private Points resultPoints;
	private Map<String, Object> constraints;
	private Map<String, Object> misc;
	private String algName;
	private Responder responder;
	private RequestDataset requestDataset;

	/**
	 * Constructs a new ComputeRequest using the given Responder, Points and
	 * Constraints. The corresponding RequestDataset is null, should be set 
	 * with {@link #setRequestDataset(RequestDataset)} if server is private.
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
		this.requestDataset = null;
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
	
	/**
	 * Sets the corresponding RequestDataset, should be null if server is
	 * not in private mode.
	 * @param requestDataset
	 */
	public void setRequestDataset(RequestDataset requestDataset) {
		this.requestDataset = requestDataset;
	}
	
	/**
	 * Gets the corresponding RequestDataset, should be null if server is
	 * not in private mode. The RequestDataset is not synchronized with
	 * the database. So if you need the up-to-date corresponding RequestDataset,
	 * you have to use the attribute id of this RequestDataset and get the
	 * up-to-date RequestDataset from the database yourself.
	 * @return
	 */
	public RequestDataset getRequestDataset() {
		return this.requestDataset;
	}
}
