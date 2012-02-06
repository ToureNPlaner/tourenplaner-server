/**
 * 
 */
package database;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Date;

/**
 * @author Sascha Meusel
 * 
 */
public class UserDataset {

    /*
      id                INT           NOT NULL AUTO_INCREMENT,
      Email             VARCHAR(255)  NOT NULL UNIQUE,
      Passwordhash      TEXT          NOT NULL,
      Salt              TEXT          NOT NULL,
      AdminFlag         BOOL          NOT NULL DEFAULT 0,
      Status            ENUM ('needs_verification','verified')
                                      NOT NULL DEFAULT 'needs_verification',
      FirstName         TEXT          NOT NULL,
      LastName          TEXT          NOT NULL,
      Address           TEXT          NOT NULL,
      RegistrationDate  DATETIME      NOT NULL,
      VerifiedDate      DATETIME               DEFAULT NULL,
      PRIMARY KEY (ID)
	*/

	public int userid = 0;
	public String email;
	public String firstName;
	public String lastName;
	public String address;
	public boolean admin;
	public UserStatusEnum status;

	@JsonIgnore
	public String passwordhash;
	@JsonIgnore
	public String salt;

	public Date registrationDate;
	public Date verifiedDate;

	public UserDataset(int id, String email, String passwordhash, String salt,
			boolean isAdmin, UserStatusEnum status, String firstName,
			String lastName, String address, Date registrationDate,
			Date verifiedDate) {
		this.userid = id;
		this.email = email;
		this.passwordhash = passwordhash;
		this.salt = salt;
		this.admin = isAdmin;
		this.status = status;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.registrationDate = registrationDate;
		this.verifiedDate = verifiedDate;
	}
}
