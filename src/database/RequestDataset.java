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

    /*
      id              INT           NOT NULL AUTO_INCREMENT,
      UserID          INT           NOT NULL REFERENCES Users (id),
      Algorithm       VARCHAR(255)  NOT NULL,
      JSONRequest     LONGBLOB,
      JSONResponse    LONGBLOB               DEFAULT NULL,
      Cost            INT           NOT NULL DEFAULT 0,
      RequestDate     DATETIME      NOT NULL,
      FinishedDate    DATETIME               DEFAULT NULL,
      CPUTime         BIGINT        NOT NULL DEFAULT 0,
      Status          ENUM ('ok','pending','failed')
                                    NOT NULL DEFAULT 'pending',
      PRIMARY KEY (ID),
      FOREIGN KEY (UserID) REFERENCES Users (id)
    */

	public int requestID;
	public int userID;
	public String algorithm;
    @JsonIgnore
	public byte[] jsonRequest;
    @JsonIgnore
	public byte[] jsonResponse;
	public int cost;
	public Date requestDate;
	public Date finishedDate;
    public long duration;
    public RequestStatusEnum status;
	
	public RequestDataset(int id, int userID, String algorithm, byte[] jsonRequest,
			byte[] jsonResponse, int cost, Date requestDate, Date finishedDate,
            long duration, RequestStatusEnum status) {
		this.requestID = id;
		this.userID = userID;
		this.algorithm = algorithm;
		this.jsonRequest = jsonRequest;
		this.jsonResponse = jsonResponse;
		this.cost = cost;
		this.requestDate = requestDate;
		this.finishedDate = finishedDate;
		this.duration = duration;
        this.status = status;
	}
}
