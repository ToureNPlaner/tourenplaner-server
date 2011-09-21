/**
 * 
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.mysql.jdbc.DatabaseMetaData;

/**
 * @author Sascha Meusel
 *
 */
public class DatabaseManager {
		
	private Connection con = null;
	
	private final PreparedStatement pstAddNewRequest;
	private final PreparedStatement pstAddNewUser;
	private final PreparedStatement pstGetAllRequests;
	private final PreparedStatement pstGetAllUsers;
	private final PreparedStatement pstGetUserWithEmail;
	private final PreparedStatement pstGetUserWithId;
	
	private final PreparedStatement pstUpdateRequest;
	private final PreparedStatement pstUpdateUser;
	private final PreparedStatement pstDeleteRequestWithRequestId;
	private final PreparedStatement pstDeleteRequestsOfUserWithUserId;
	private final PreparedStatement pstDeleteUserWithUserId;
	private final PreparedStatement pstDeleteUserWithEmail;
	private final PreparedStatement pstGetAllRequestsWithLimitOffset;
	private final PreparedStatement pstGetRequestWithRequestId;
	private final PreparedStatement pstGetRequestsWithUserId;
	private final PreparedStatement pstGetRequestsWithUserIdLimitOffset;
	private final PreparedStatement pstGetAllUsersWithLimitOffset;
	
	
	private final static String addNewRequestString = "INSERT INTO Requests " +
			"(UserID, JSONRequest, RequestDate) VALUES(?, ?, ?)";
	
	private final static String addNewUserString = "INSERT INTO Users " +
			"(Email, Passwordhash, Salt, FirstName, LastName, Address, " +
			"AdminFlag, RegistrationDate) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
	
	private final static String getAllRequestsString = "SELECT id, UserID, " +
			"JSONRequest, JSONResponse, PendingFlag, Costs, PaidFlag, " +
			"RequestDate, FinishedDate, CPUTime, FailedFlag, " +
			"FailDescription FROM Requests";
	
	private final static String getAllUsersString = "SELECT id, Email, " +
			"Passwordhash, Salt, AdminFlag, Status, FirstName, LastName, " +
			"Address, RegistrationDate, VerifiedDate, DeleteRequestDate " +
			"FROM Users";
	
	private final static String getUserStringWithEmail = "SELECT id, Email, " +
			"Passwordhash, Salt, AdminFlag, Status, FirstName, LastName, " +
			"Address, RegistrationDate, VerifiedDate, DeleteRequestDate " +
			"FROM Users WHERE Email = ?";

	private final static String getUserStringWithId = "SELECT id, Email, " +
			"Passwordhash, Salt, AdminFlag, Status, FirstName, LastName, " +
			"Address, RegistrationDate, VerifiedDate, DeleteRequestDate " +
			"FROM Users WHERE id = ?";
	
	private final static String updateRequestString = "UPDATE Requests SET " +
			"UserID = ?, JSONRequest = ?, JSONResponse = ?, " +
			"PendingFlag = ?, Costs = ?, PaidFlag = ?, RequestDate = ?, " +
			"FinishedDate = ?, CPUTime = ?, FailedFlag = ?, " +
			"FailDescription = ? WHERE id = ?";
	
	private final static String updateUserString = "UPDATE Users SET " +
			"Email = ?, Passwordhash = ?, Salt = ?, AdminFlag = ?, " +
			"Status = ?, FirstName = ?, LastName = ?, Address = ?, " +
			"RegistrationDate = ?, VerifiedDate = ?, DeleteRequestDate = ? " +
			"WHERE id = ?";
	
	private final static String deleteRequestWithRequestIdString = 
			"DELETE FROM Requests WHERE id = ?";
	
	private final static String deleteRequestsOfUserWithUserIdString = 
			"DELETE FROM Requests WHERE UserID = ?";
	
	private final static String deleteUserWithUserIdString = 
			"DELETE FROM Users WHERE id = ?";
	
	private final static String deleteUserWithEmailString = 
			"DELETE FROM Users WHERE Email = ?";
	
	private final static String getAllRequestsWithLimitOffsetString = 
			getAllRequestsString + " LIMIT ? OFFSET ?";
	
	private final static String getRequestWithRequestIdString = 
			getAllRequestsString + " WHERE id = ?";
	
	private final static String getRequestsWithUserIdString = 
			getAllRequestsString + " WHERE UserID = ?";
	
	private final static String getRequestsWithUserIdLimitOffsetString = 
			getRequestsWithUserIdString + " LIMIT ? OFFSET ?";
	private final static String getAllUsersWithLimitOffsetString = 
			getAllUsersString + " LIMIT ? OFFSET ?";
	
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
		
		con = DriverManager.getConnection(url + dbName, userName, password);
		
		pstAddNewRequest = con.prepareStatement(addNewRequestString);
		pstAddNewUser = con.prepareStatement(addNewUserString);
		pstGetAllRequests = con.prepareStatement(getAllRequestsString);
		pstGetAllUsers = con.prepareStatement(getAllUsersString);
		pstGetUserWithEmail = con.prepareStatement(getUserStringWithEmail);
		pstGetUserWithId = con.prepareStatement(getUserStringWithId);
		pstUpdateRequest = con.prepareStatement(updateRequestString);
		pstUpdateUser = con.prepareStatement(updateUserString);
		pstDeleteRequestWithRequestId = 
				con.prepareStatement(deleteRequestWithRequestIdString);
		pstDeleteRequestsOfUserWithUserId = 
				con.prepareStatement(deleteRequestsOfUserWithUserIdString);
		pstDeleteUserWithUserId = 
				con.prepareStatement(deleteUserWithUserIdString);
		pstDeleteUserWithEmail = 
				con.prepareStatement(deleteUserWithEmailString);
		pstGetAllRequestsWithLimitOffset = 
				con.prepareStatement(getAllRequestsWithLimitOffsetString);
		pstGetRequestWithRequestId = 
				con.prepareStatement(getRequestWithRequestIdString);
		pstGetRequestsWithUserId = 
				con.prepareStatement(getRequestsWithUserIdString);
		pstGetRequestsWithUserIdLimitOffset = 
				con.prepareStatement(getRequestsWithUserIdLimitOffsetString);
		pstGetAllUsersWithLimitOffset = 
				con.prepareStatement(getAllUsersWithLimitOffsetString);
	}
	
	/**
	 * {@value #addNewRequestString}
	 * @param userID
	 * @param jsonRequest
	 * @throws SQLException
	 */
	public void addNewRequest(int userID, byte[] jsonRequest) 
			throws SQLException  {
		pstAddNewRequest.setInt(1, userID);
		pstAddNewRequest.setBytes(2, jsonRequest);
		pstAddNewRequest.setTimestamp(3, 
				new Timestamp(System.currentTimeMillis()));
		
		pstAddNewRequest.executeUpdate();
		
	}
	
	/**
	 * {@value #addNewUserString}
	 * @param email
	 * @param passwordhash
	 * @param salt
	 * @param firstName
	 * @param lastName
	 * @param address
	 * @param isAdmin
	 * @return TODO
	 * @throws SQLException
	 */
	public UsersDBRow addNewUser(String email, String passwordhash, String salt, 
			String firstName, String lastName, String address, boolean isAdmin) 
			throws SQLException  {
		
		UsersDBRow user = null;
		
		try {
		
		Timestamp stamp = new Timestamp(System.currentTimeMillis());
		
		pstAddNewUser.setString(1, email);
		pstAddNewUser.setString(2, passwordhash);
		pstAddNewUser.setString(3, salt);
		pstAddNewUser.setString(4, firstName);
		pstAddNewUser.setString(5, lastName);
		pstAddNewUser.setString(6, address);
		pstAddNewUser.setBoolean(7, isAdmin);
		pstAddNewUser.setTimestamp(8, stamp);
		
		pstAddNewUser.executeUpdate();
		
		user = new UsersDBRow(
				-1, 
				email, 
				passwordhash, 
				salt,
				isAdmin, 
				UserStatusEnum.NeedsVerification, 
				firstName, 
				lastName, 
				address, 
				new Date(stamp.getTime()), 
				null, 
				null
				);
		} catch (SQLIntegrityConstraintViolationException ex) {
			if (ex.getNextException() == null) {
				return null;
			}
			throw ex;
		}
		return user;
	}
	
	/**
	 * {@value #updateRequestString}
	 * @param request
	 * @throws SQLException
	 */
	public void updateRequest(RequestsDBRow request) throws SQLException {
		
		pstUpdateRequest.setInt(1,request.userID);
		pstUpdateRequest.setBytes(2,request.jsonRequest);
		pstUpdateRequest.setBytes(3,request.jsonResponse);
		pstUpdateRequest.setBoolean(4,request.isPending);
		pstUpdateRequest.setInt(5,request.costs);
		pstUpdateRequest.setBoolean(6,request.isPaid);
		pstUpdateRequest.setTimestamp(7,dateToTimestamp(request.requestDate));
		pstUpdateRequest.setTimestamp(8,dateToTimestamp(request.finishedDate));
		pstUpdateRequest.setLong(9,request.cpuTime);
		pstUpdateRequest.setBoolean(10,request.hasFailed);
		pstUpdateRequest.setString(11,request.failDescription);
		pstUpdateRequest.setInt(12,request.id);
		
		pstUpdateRequest.executeUpdate();
	}
	
	/**
	 * {@value #updateUserString}
	 * @param request
	 * @throws SQLException
	 */
	public void updateUser(UsersDBRow user) throws SQLException {
		pstUpdateUser.setString(1,user.email);
		pstUpdateUser.setString(2,user.passwordhash);
		pstUpdateUser.setString(3,user.salt);
		pstUpdateUser.setBoolean(4,user.isAdmin);
		pstUpdateUser.setString(5,user.status.toString());
		pstUpdateUser.setString(6,user.firstName);
		pstUpdateUser.setString(7,user.lastName);
		pstUpdateUser.setString(8,user.address);
		pstUpdateUser.setTimestamp(9,dateToTimestamp(user.registrationDate));
		pstUpdateUser.setTimestamp(10,dateToTimestamp(user.verifiedDate));
		pstUpdateUser.setTimestamp(11,dateToTimestamp(user.deleteRequestDate));
		pstUpdateUser.setInt(12,user.id);
		
		pstUpdateUser.executeUpdate();
	}
	
	/**
	 * {@value #deleteRequestWithRequestIdString}
	 * @param id
	 * @throws SQLException
	 */
	public void deleteRequest(int id) throws SQLException {
		pstDeleteRequestWithRequestId.setInt(1, id);
		
		pstDeleteRequestWithRequestId.executeUpdate();
	}
	
	/**
	 * {@value #deleteRequestsOfUserWithUserIdString}
	 * @param userId
	 * @throws SQLException
	 */
	public void deleteRequestsOfUser(int userId) throws SQLException {
		pstDeleteRequestsOfUserWithUserId.setInt(1, userId);	
		
		pstDeleteRequestsOfUserWithUserId.executeUpdate();
	}
	
	/**
	 * {@value #deleteUserWithUserIdString}
	 * @param id
	 * @throws SQLException
	 */
	public void deleteUser(int id) throws SQLException {
		pstDeleteUserWithUserId.setInt(1, id);
		
		pstDeleteUserWithUserId.executeUpdate();
	}
	
	/**
	 * {@value #deleteUserWithEmailString}
	 * @param email
	 * @throws SQLException
	 */
	public void deleteUser(String email) throws SQLException {
		pstDeleteUserWithEmail.setString(1, email);
		
		pstDeleteUserWithEmail.executeUpdate();
	}
	
	/**
	 * {@value #getAllRequestsString}
	 * @return
	 * @throws SQLException
	 */
	public RequestsDBRow[] getAllRequests() throws SQLException {
		ResultSet resultSet = pstGetAllRequests.executeQuery();
		ArrayList<RequestsDBRow> list = new ArrayList<RequestsDBRow>();
		
		while(resultSet.next()) {
			
			list.add(new RequestsDBRow(
					resultSet.getInt(1), 
					resultSet.getInt(2), 
					resultSet.getBytes(3), 
					resultSet.getBytes(4),
					resultSet.getBoolean(5), 
					resultSet.getInt(6), 
					resultSet.getBoolean(7), 
					timestampToDate(resultSet.getTimestamp(8)), 
					timestampToDate(resultSet.getTimestamp(9)), 
					resultSet.getLong(10), 
					resultSet.getBoolean(11), 
					resultSet.getString(12)
					));
		}
		
		return list.toArray(new RequestsDBRow[0]);
	}
	
	/**
	 * {@value #getAllRequestsWithLimitOffsetString}
	 * @param limit
	 * @param offset
	 * @return
	 * @throws SQLException
	 */
	public RequestsDBRow[] getAllRequests(int limit, int offset) 
			throws SQLException {

		pstGetAllRequestsWithLimitOffset.setInt(1,limit);
		pstGetAllRequestsWithLimitOffset.setInt(2,offset);
		
		ResultSet resultSet = pstGetAllRequestsWithLimitOffset.executeQuery();
		ArrayList<RequestsDBRow> list = new ArrayList<RequestsDBRow>();
		
		while(resultSet.next()) {
			
			list.add(new RequestsDBRow(
					resultSet.getInt(1), 
					resultSet.getInt(2), 
					resultSet.getBytes(3), 
					resultSet.getBytes(4),
					resultSet.getBoolean(5), 
					resultSet.getInt(6), 
					resultSet.getBoolean(7), 
					timestampToDate(resultSet.getTimestamp(8)), 
					timestampToDate(resultSet.getTimestamp(9)), 
					resultSet.getLong(10), 
					resultSet.getBoolean(11), 
					resultSet.getString(12)
					));
		}
		
		return list.toArray(new RequestsDBRow[0]);
	}
	
	/**
	 * {@value #getRequestWithRequestIdString}
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public RequestsDBRow getRequest(int id) throws SQLException {
		
		pstGetRequestWithRequestId.setInt(1, id);
		ResultSet resultSet = pstGetRequestWithRequestId.executeQuery();
		RequestsDBRow request = null;
				
		while(resultSet.next()) {
			request = new RequestsDBRow(
					resultSet.getInt(1), 
					resultSet.getInt(2), 
					resultSet.getBytes(3), 
					resultSet.getBytes(4),
					resultSet.getBoolean(5), 
					resultSet.getInt(6), 
					resultSet.getBoolean(7), 
					timestampToDate(resultSet.getTimestamp(8)), 
					timestampToDate(resultSet.getTimestamp(9)), 
					resultSet.getLong(10), 
					resultSet.getBoolean(11), 
					resultSet.getString(12)
					);
		}
		
		return request;
	}
	
	/**
	 * {@value #getRequestsWithUserIdString}
	 * @param userId
	 * @return
	 * @throws SQLException
	 */
	public RequestsDBRow[] getRequests(int userId) throws SQLException {
		pstGetRequestsWithUserId.setInt(1,userId);
		
		ResultSet resultSet = pstGetRequestsWithUserId.executeQuery();
		ArrayList<RequestsDBRow> list = new ArrayList<RequestsDBRow>();
		
		while(resultSet.next()) {
			
			list.add(new RequestsDBRow(
					resultSet.getInt(1), 
					resultSet.getInt(2), 
					resultSet.getBytes(3), 
					resultSet.getBytes(4),
					resultSet.getBoolean(5), 
					resultSet.getInt(6), 
					resultSet.getBoolean(7), 
					timestampToDate(resultSet.getTimestamp(8)), 
					timestampToDate(resultSet.getTimestamp(9)), 
					resultSet.getLong(10), 
					resultSet.getBoolean(11), 
					resultSet.getString(12)
					));
		}
		
		return list.toArray(new RequestsDBRow[0]);
	}
	
	/**
	 * {@value #getRequestsWithUserIdLimitOffsetString}
	 * @param userId
	 * @param limit
	 * @param offset
	 * @return
	 * @throws SQLException
	 */
	public RequestsDBRow[] getRequests(int userId, int limit, int offset) 
			throws SQLException {
		pstGetRequestsWithUserIdLimitOffset.setInt(1,userId);
		pstGetRequestsWithUserIdLimitOffset.setInt(2,limit);
		pstGetRequestsWithUserIdLimitOffset.setInt(3,offset);
		
		ResultSet resultSet = 
				pstGetRequestsWithUserIdLimitOffset.executeQuery();
		ArrayList<RequestsDBRow> list = new ArrayList<RequestsDBRow>();
		
		while(resultSet.next()) {
			
			list.add(new RequestsDBRow(
					resultSet.getInt(1), 
					resultSet.getInt(2), 
					resultSet.getBytes(3), 
					resultSet.getBytes(4),
					resultSet.getBoolean(5), 
					resultSet.getInt(6), 
					resultSet.getBoolean(7), 
					timestampToDate(resultSet.getTimestamp(8)), 
					timestampToDate(resultSet.getTimestamp(9)), 
					resultSet.getLong(10), 
					resultSet.getBoolean(11), 
					resultSet.getString(12)
					));
		}
		
		return list.toArray(new RequestsDBRow[0]);
	}
	
	
	/**
	 * {@value #getAllUsersString}
	 * @return
	 * @throws SQLException
	 */
	public UsersDBRow[] getAllUsers() throws SQLException {
		ResultSet resultSet = pstGetAllUsers.executeQuery();
		ArrayList<UsersDBRow> list = new ArrayList<UsersDBRow>();
		
		while(resultSet.next()) {
			
			list.add(new UsersDBRow(
					resultSet.getInt(1), 
					resultSet.getString(2), 
					resultSet.getString(3), 
					resultSet.getString(4),
					resultSet.getBoolean(5), 
					UserStatusEnum.valueOf(resultSet.getString(6)), 
					resultSet.getString(7), 
					resultSet.getString(8), 
					resultSet.getString(9), 
					timestampToDate(resultSet.getTimestamp(10)), 
					timestampToDate(resultSet.getTimestamp(11)), 
					timestampToDate(resultSet.getTimestamp(12))
					));
		}
		
		return list.toArray(new UsersDBRow[0]);
	}
	
	
	/**
	 * {@value #getAllUsersWithLimitOffsetString}
	 * @param limit
	 * @param offset
	 * @return
	 * @throws SQLException
	 */
	public UsersDBRow[] getAllUsers(int limit, int offset) 
			throws SQLException {
		pstGetAllUsersWithLimitOffset.setInt(1,limit);
		pstGetAllUsersWithLimitOffset.setInt(2,offset);
		
		ResultSet resultSet = pstGetAllUsersWithLimitOffset.executeQuery();
		ArrayList<UsersDBRow> list = new ArrayList<UsersDBRow>();
		
		while(resultSet.next()) {
			
			list.add(new UsersDBRow(
					resultSet.getInt(1), 
					resultSet.getString(2), 
					resultSet.getString(3), 
					resultSet.getString(4),
					resultSet.getBoolean(5), 
					UserStatusEnum.valueOf(resultSet.getString(6)), 
					resultSet.getString(7), 
					resultSet.getString(8), 
					resultSet.getString(9), 
					timestampToDate(resultSet.getTimestamp(10)), 
					timestampToDate(resultSet.getTimestamp(11)), 
					timestampToDate(resultSet.getTimestamp(12))
					));
		}
		
		return list.toArray(new UsersDBRow[0]);
	}
	
	/**
	 * {@value #getUserStringWithEmail}
	 * @param email
	 * @return
	 * @throws SQLException
	 */
	public UsersDBRow getUser(String email) throws SQLException {
		pstGetUserWithEmail.setString(1, email);
		ResultSet resultSet = pstGetUserWithEmail.executeQuery();
		UsersDBRow user = null;
		
		while(resultSet.next()) {
			
			user = new UsersDBRow(
					resultSet.getInt(1), 
					resultSet.getString(2), 
					resultSet.getString(3), 
					resultSet.getString(4),
					resultSet.getBoolean(5), 
					UserStatusEnum.valueOf(resultSet.getString(6)), 
					resultSet.getString(7), 
					resultSet.getString(8), 
					resultSet.getString(9), 
					timestampToDate(resultSet.getTimestamp(10)), 
					timestampToDate(resultSet.getTimestamp(11)), 
					timestampToDate(resultSet.getTimestamp(12))
					);
		}
		
		return user;
	}
	
	/**
	 * {@value #getUserStringWithId}
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public UsersDBRow getUser(int id) throws SQLException {
		pstGetUserWithId.setInt(1, id);
		ResultSet resultSet = pstGetUserWithId.executeQuery();
		UsersDBRow user = null;
		
		while(resultSet.next()) {
			
			user = new UsersDBRow(
					resultSet.getInt(1), 
					resultSet.getString(2), 
					resultSet.getString(3), 
					resultSet.getString(4),
					resultSet.getBoolean(5), 
					UserStatusEnum.valueOf(resultSet.getString(6)), 
					resultSet.getString(7), 
					resultSet.getString(8), 
					resultSet.getString(9), 
					timestampToDate(resultSet.getTimestamp(10)), 
					timestampToDate(resultSet.getTimestamp(11)), 
					timestampToDate(resultSet.getTimestamp(12))
					);
		}
		
		return user;
	}
	
	private Date timestampToDate(Timestamp stamp) {
		if (stamp == null) {
			return null;
		}
		return new Date(stamp.getTime());
	}
	
	private Timestamp dateToTimestamp(Date date) {
		if (date == null) {
			return null;
		}
		return new Timestamp(date.getTime());
	}
	
	/**
	 * Yet only for testing purpose
	 */
	public void printDatabaseInformation() {
		try {
			addNewUser("sascha@tourenplaner", "xyz", "pepper", 
					"sascha", "verratinet", "", true);
			//addNewRequest(10, ("EinTestBlob mit falscher UserID").getBytes());
		} catch (SQLIntegrityConstraintViolationException e) {
			
			SQLException ex = e;
			
			while(ex != null) {
			
				System.out.println("errorcode: " + ex.getErrorCode());
				System.out.println("sqlstate: " + ex.getSQLState());
				try {
					System.out.println("stateType: " 
							+ con.getMetaData().getSQLStateType());
				} catch (SQLException e1) {
					System.out.println("someConnectionError:");
					e1.printStackTrace();
				}
				System.out.println(ex.getLocalizedMessage());
			
				ex = ex.getNextException();
			}
			
			System.out.println("xopen: " + DatabaseMetaData.sqlStateXOpen);
			System.out.println("sql: " + DatabaseMetaData.sqlStateSQL);
			System.out.println("sql99: " + DatabaseMetaData.sqlStateSQL99);
			
		} catch (SQLException ex) {
			System.out.println("anotherConnectionError:");
			ex.printStackTrace();
		}
		
		
	}

}
