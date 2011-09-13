/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.Map;

import server.Responder;

/**
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class ComputeRequest {
	
	protected Map<String, Object> request;
	private String algName;
	private ComputeResult result;
	private Responder responder;
	

	public ComputeRequest(Responder responder, String algName,Map<String, Object> request){		
		this.algName = algName;
		this.request = request;
		this.responder = responder;
		
		//TODO Make sure the Result object holds all additional data 
		this.result = new ComputeResult();
		result.putAll(request);
	}
	
	public ComputeResult getResultObject(){
		return result;
	}
	
	public Responder getResponder(){
		return responder;
	}
	
	public String getAlgorithmShort(){
		return algName;		
	}
	
	
	public Object get(String index){
		return request.get(index);
	}
	
	
}
