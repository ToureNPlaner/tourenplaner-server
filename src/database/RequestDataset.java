/**
 * 
 */
package database;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sascha Meusel
 *
 */
public class RequestDataset {
	
	public int id = 0;
	public int userID = 0;
	public String algorithm = null;
	public byte[] jsonRequest = null;
	public byte[] jsonResponse = null;
	public boolean isPending = true;
	public int costs = 0;
	public boolean isPaid = false;
	public Date requestDate = null;
	public Date finishedDate = null;
	public long cpuTime = 0;
	public boolean hasFailed = false;
	public String failDescription = null;
	
	public RequestDataset(int id, int userID, String algorithm, byte[] jsonRequest, 
			byte[] jsonResponse, boolean isPending, int costs, boolean isPaid, 
			Date requestDate, Date finishedDate, long cpuTime, 
			boolean hasFailed, String failDescription) {
		
		this.id = id;
		this.userID = userID;
		this.algorithm = algorithm;
		this.jsonRequest = jsonRequest;
		this.jsonResponse = jsonResponse;
		this.isPending = isPending;
		this.costs = costs;
		this.isPaid = isPaid;
		this.requestDate = requestDate;
		this.finishedDate = finishedDate;
		this.cpuTime = cpuTime;
		this.hasFailed = hasFailed;
		this.failDescription = failDescription;
	}
	
	/**
	 * Returns a HashMap with following structure and without json data:<br>
	 * <pre>
	 * "requestid" : "...",
	 * "userid" : "...",
	 * "algorithm" : "...",
	 * "ispending" : true/false,
	 * "cost" : "...",
	 * "ispaid" : true/false,
	 * "requestdate" : "...", 
	 * "finisheddate" : "...",
	 * "duration" : "...",
	 * "hasfailed" : true/false,
	 * "faildescription" : "..."</pre>
	 * 
	 * @return
	 */
	public Map<String,Object> getSmallRequestDatasetHashMap() {
		Map<String, Object> request = new HashMap<String, Object>(11);
		request.put("requestid", id);
		request.put("userid", userID);
		request.put("algorithm", algorithm);
		request.put("ispending", isPending);
		request.put("cost", costs);
		request.put("ispaid", isPaid);
		request.put("requestdate", requestDate); 
		request.put("finisheddate", finishedDate);
		request.put("duration", cpuTime);
		request.put("hasfailed", hasFailed);
		request.put("faildescription", failDescription);
		
		return request;
	}
	
}
