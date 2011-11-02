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
	
	/**
	 * Returns a HashMap with following structure and without password:<br>
	 * <pre>
	 * "email" : "...",
	 * "firstname" : "...",
	 * "lastname" : "...",
	 * "address" : "...",
	 * "admin" : false,
	 * "status" : "..."</pre>
	 * 
	 * @return
	 */
	public Map<String,Object> getSmallUserHashMap() {
		Map<String, Object> user = new HashMap<String, Object>(6);
		user.put("email", email);
		user.put("firstname", firstName);
		user.put("lastname", lastName);
		user.put("address", address);
		user.put("admin", isAdmin);
		user.put("status", status.toString());
		
		return user;
	}
	
}
