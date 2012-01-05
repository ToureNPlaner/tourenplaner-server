/**
 * 
 */
package database;

import java.util.Date;

/**
 * @author Sascha Meusel
 *
 */
public class RequestDataset {
	
	public int requestID;
	public int userID;
	public String algorithm;
	public byte[] jsonRequest;
	public byte[] jsonResponse;
	public boolean isPending;
	public int costs;
	public boolean isPaid;
	public Date requestDate;
	public Date finishedDate;
    public long duration;
	public boolean hasFailed;
	public String failDescription;
	
	public RequestDataset(int id, int userID, String algorithm, byte[] jsonRequest,
			byte[] jsonResponse, boolean isPending, int costs, boolean isPaid, 
			Date requestDate, Date finishedDate, long duration,
			boolean hasFailed, String failDescription) {
		this.requestID = id;
		this.userID = userID;
		this.algorithm = algorithm;
		this.jsonRequest = jsonRequest;
		this.jsonResponse = jsonResponse;
		this.isPending = isPending;
		this.costs = costs;
		this.isPaid = isPaid;
		this.requestDate = requestDate;
		this.finishedDate = finishedDate;
		this.duration = duration;
		this.hasFailed = hasFailed;
		this.failDescription = failDescription;
	}
}
