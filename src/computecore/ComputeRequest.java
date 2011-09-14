/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.Map;

import server.Responder;

/**
 * This class is used to represent a request for computation
 * 
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class ComputeRequest {
	
	protected Map<String, Object> request;
	private String algName;
	private ComputeResult result;
	private Responder responder;
	
	/**
	 * Constructs a new ComputeRequest using the given Responder and initialized with
	 * the given Map (representing a JSON Object)
	 * 
	 * @param responder
	 * @param algName
	 * @param request
	 */
	public ComputeRequest(Responder responder, String algName, Map<String, Object> request){		
		this.algName = algName;
		this.request = request;
		this.responder = responder;
		
		//TODO Make sure the Result object holds all additional data 
		this.result = new ComputeResult();
		result.putAll(request);
	}
	/**
	 * Get the result object in which to write the result of the computation
	 * of this request
	 * 
	 * @return
	 */
	public ComputeResult getResultObject(){
		return result;
	}
	/**
	 * Gets the responder object which is used to send the result to the correct 
	 * client connection
	 * 
	 * @return
	 */
	public Responder getResponder(){
		return responder;
	}
	
	/**
	 * Gets the URLSuffix for the requested algorithmm e.g. "sp" for
	 * a shortest path algorithm
	 * 
	 * @return
	 */
	public String getAlgorithmURLSuffix(){
		return algName;		
	}
	
	/**
	 * Returns the Object referenced with the given key
	 * 
	 * @param index
	 * @return
	 */
	public Object get(String index){
		return request.get(index);
	}
	
	
}
