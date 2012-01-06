/**
 * 
 */
package database;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;

/**
 * @author Sascha Meusel
 *
 */
public class RequestDataset {
	
	public int requestID;
	public int userID;
	public String algorithm;
    @JsonProperty("request")
	public byte[] jsonRequest;
    @JsonProperty("response")
	public byte[] jsonResponse;
	public int costs;
	public boolean isPaid;
	public Date requestDate;
	public Date finishedDate;
    public long duration;
    public RequestStatusEnum status;
    @JsonIgnore
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
		this.costs = costs;
		this.isPaid = isPaid;
		this.requestDate = requestDate;
		this.finishedDate = finishedDate;
		this.duration = duration;
		if (isPending && !hasFailed) {
            this.status = RequestStatusEnum.Pending;
        } else if (isPending && hasFailed) {
            this.status = RequestStatusEnum.TryAgainLater;
        } else if (!isPending && hasFailed) {
            this.status = RequestStatusEnum.Failed;
        } else {
            this.status = RequestStatusEnum.OK;
        }

        this.failDescription = failDescription;

	}
}
