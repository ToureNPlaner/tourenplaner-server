/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.Map;

import server.ResultResponder;

/**
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class ComputeRequest {
	
	protected Map<String, Object> request;
	private String algName;
	private ComputeResult result;
	private ResultResponder responder;
	

	public ComputeRequest(ResultResponder responder, String algName,Map<String, Object> request){		
		this.algName = algName;
		this.request = request;
		this.responder = responder;
		this.result = new ComputeResult();
	}
	
	public ComputeResult getResultObject(){
		return result;
	}
	
	public ResultResponder getResponder(){
		return responder;
	}
	
	public String getAlgorithmShort(){
		return algName;		
	}
	
	
	public Object get(String index){
		return request.get(index);
	}
	
	
}
