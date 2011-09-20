/**
 * 
 */
package database;

import java.util.Date;

/**
 * @author Sascha Meusel
 *
 */
public class RequestsDBRow {
	
	public int id = 0;
	public int userID = 0;
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
	
	public RequestsDBRow(int id, int userID, byte[] jsonRequest, 
			byte[] jsonResponse, boolean isPending, int costs, boolean isPaid, 
			Date requestDate, Date finishedDate, long cpuTime, 
			boolean hasFailed, String failDescription) {
		
		this.id = id;
		this.userID = userID;
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
	
}