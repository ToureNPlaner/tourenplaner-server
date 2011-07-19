/**
 * ToureNPlaner
 * 
 */
package computecore;

import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author Niklas Schnelle
 * 
 */
public class ComputeRequest {

	protected Map<String, Object> request;
	private final String algName;
	private final ComputeResult result;
	private final Semaphore waitComputation;

	public ComputeRequest(String algName, Map<String, Object> request) {
		waitComputation = new Semaphore(0);

		this.algName = algName;
		this.request = request;
		this.result = new ComputeResult();
	}

	public ComputeResult getResultObject() {
		return result;
	}

	public String getAlgorithmShort() {
		return algName;
	}

	public Object get(String index) {
		return request.get(index);
	}

	public Semaphore getWaitComputation() {
		return this.waitComputation;
	}

}
