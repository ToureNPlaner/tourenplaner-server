/**
 * 
 */
package database;

import java.util.Date;

/**
 * @author Sascha Meusel
 *
 */
public class UsersDBRow {

	public int id = 0;
	public String email = "";
	public String passwordhash = "";
	public String salt = "";
	public boolean isAdmin = false;
	public UserStatusEnum status = UserStatusEnum.NeedsVerification;
	public String firstName = "";
	public String lastName = "";
	public String address = "";
	public Date registrationDate = null;
	public Date verifiedDate = null;
	public Date deleteRequestDate = null;
	
	public UsersDBRow(int id, String email, String passwordhash, String salt, 
			boolean isAdmin, UserStatusEnum status, 
			String firstName, String lastName, String address, 
			Date registrationDate, Date verifiedDate, Date deleteRequestDate) {
		
		this.id = id;
		this.email = email;
		this.passwordhash = passwordhash;
		this.salt = salt;
		this.isAdmin = isAdmin;
		this.status = status;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.registrationDate = registrationDate;
		this.verifiedDate = verifiedDate;
		this.deleteRequestDate = deleteRequestDate;
		
	}
	
}
