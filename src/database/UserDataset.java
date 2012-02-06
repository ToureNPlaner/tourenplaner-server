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

	@JsonIgnore
	public Date deleteRequestDate;

	public UserDataset(int id, String email, String passwordhash, String salt,
			boolean isAdmin, UserStatusEnum status, String firstName,
			String lastName, String address, Date registrationDate,
			Date verifiedDate, Date deleteRequestDate) {
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
		this.deleteRequestDate = deleteRequestDate;
	}
}
