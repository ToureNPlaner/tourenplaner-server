/**
 * 
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Sascha Meusel
 *
 */
public class DatabaseManager {
		
	private Connection con = null;
	
	/**
	 * 
	 * @param url Example: "jdbc:mysql://localhost:3306/"
	 * @param dbName Example: "tourenplaner"
	 * @param userName User name of the database user account
	 * @param password To the user according password
	 * @throws SQLException
	 */
	public DatabaseManager(String url, String dbName, String userName, 
			String password) throws SQLException {
		
		//TODO use config package
		url = "jdbc:mysql://localhost:3306/";
		dbName = "tourenplaner";
		userName = "tnpuser";
		password = "H4milt0n";
		
		con = DriverManager.getConnection(url + dbName, userName, password);
		
	}
	
	
	public void addNewRequest(int userID, byte[] jsonRequest) 
			throws SQLException  {
		
		String statement = "INSERT INTO Request" +
				" (UserID, JSONRequest, RequestDate) VALUES(?)";

		PreparedStatement pst;
		pst = con.prepareStatement(statement);
		pst.setInt(1, userID);
		pst.setBytes(2, jsonRequest);
		pst.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
		
		pst.executeUpdate();
        pst.close();
		
	}
	
	public void addNewUser(String email, String passwordhash, String salt, 
			String firstName, String lastName, String address, boolean isAdmin) 
			throws SQLException  {
		String statement = "INSERT INTO User" +
				" (Email, Passwordhash, Salt, FirstName, LastName, Address, " +
				"AdminFlag, RegistrationDate) VALUES(?)";
		
		PreparedStatement pst;
		pst = con.prepareStatement(statement);
		pst.setString(1, email);
		pst.setString(2, passwordhash);
		pst.setString(3, salt);
		pst.setString(4, firstName);
		pst.setString(5, lastName);
		pst.setString(6, address);
		pst.setBoolean(7, isAdmin);
		pst.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
		
		pst.executeUpdate();
        pst.close();
	}
	
	

}
