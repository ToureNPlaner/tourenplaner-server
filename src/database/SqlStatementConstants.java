package database;

import com.mysql.jdbc.Statement;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 */
public class SqlStatementConstants {

    /*
      INSERT statements
    */
    public final static String strAddNewRequest = "INSERT INTO Requests "
            + "(UserID, Algorithm, JSONRequest, RequestDate) VALUES(?, ?, ?, ?)";

    public final static String strAddNewUser = "INSERT INTO Users "
            + "(Email, Passwordhash, Salt, FirstName, LastName, Address, "
            + "AdminFlag, Status, RegistrationDate, VerifiedDate)"
            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    /*
       SELECT COUNT(*) statements
     */
    public final static String strCountAllRequests = "SELECT COUNT(*) FROM Requests";

    public final static String strCountRequestsWithUserId = "SELECT COUNT(*) FROM Requests WHERE UserID = ?";

    public final static String strCountAllUsers = "SELECT COUNT(*) FROM Users";


    /*
       SELECT statements for table Requests
     */

    private final static String strGetAllRequestsNoJson = "SELECT id, UserID, "
            + "Algorithm, Cost, "
            + "RequestDate, FinishedDate, CPUTime, Status "
            + "FROM Requests";

    public final static String strGetAllRequestsNoJsonWithLimitOffset = strGetAllRequestsNoJson
            + " ORDER BY RequestDate DESC LIMIT ? OFFSET ?";

    public final static String strGetRequestsNoJsonWithUserIdLimitOffset = strGetAllRequestsNoJson
            + " WHERE UserID = ? ORDER BY RequestDate DESC LIMIT ? OFFSET ?";


    // single result
    public final static String strGetRequestWithRequestId = strGetAllRequestsNoJson
            + " WHERE id = ?";

    public final static String strGetJSONRequestWithRequestId = "SELECT UserID, JSONRequest FROM Requests"
            + " WHERE id = ?";

    public final static String strGetJSONResponseWithRequestId = "SELECT UserID, JSONResponse FROM Requests"
            + " WHERE id = ?";


    /*
       SELECT statements for table Users
     */
    public final static String strGetAllUsers = "SELECT id, Email, "
            + "Passwordhash, Salt, AdminFlag, Status, FirstName, LastName, "
            + "Address, RegistrationDate, VerifiedDate "
            + "FROM Users";

    public final static String strGetAllUsersWithLimitOffset = strGetAllUsers
            + " LIMIT ? OFFSET ?";

    // single result
    public final static String strGetUserWithEmail = strGetAllUsers + " WHERE Email = ?";

    // single result
    public final static String strGetUserWithId = strGetAllUsers + " WHERE id = ?";



    /*
       UPDATE statements
     */
    public final static String strUpdateRequest = "UPDATE Requests SET "
            + "UserID = ?, Algorithm = ?, JSONRequest = ?, JSONResponse = ?, "
            + "Cost = ?, RequestDate = ?, FinishedDate = ?, CPUTime = ?, Status = ? "
            + "WHERE id = ?";

    public final static String strUpdateRequestWithComputeResult = "UPDATE Requests SET JSONResponse = ?, "
            + "Cost = ?, FinishedDate = ?, CPUTime = ?, Status = ? "
            + "WHERE id = ?";

    public final static String strUpdateUser = "UPDATE Users SET "
            + "Email = ?, Passwordhash = ?, Salt = ?, AdminFlag = ?, "
            + "Status = ?, FirstName = ?, LastName = ?, Address = ?, "
            + "RegistrationDate = ?, VerifiedDate = ? "
            + "WHERE id = ?";

    public final static String strUpdateUserStatusToDeleted = "UPDATE Users SET Status = 'deleted' WHERE id = ?";

    /*
       DELETE statements
     */
    public final static String strDeleteRequestWithRequestId = "DELETE FROM Requests WHERE id = ?";

    public final static String strDeleteRequestsOfUserWithUserId = "DELETE FROM Requests WHERE UserID = ?";

    public final static String strDeleteUserWithUserId = "DELETE FROM Users WHERE id = ?";

    public final static String strDeleteUserWithEmail = "DELETE FROM Users WHERE Email = ?";




    // INSERT statements

    public final static SqlStatement AddNewRequest =
            new SqlStatement(strAddNewRequest,
                             Statement.RETURN_GENERATED_KEYS);

    public final static SqlStatement AddNewUser =
            new SqlStatement(strAddNewUser,
                             Statement.RETURN_GENERATED_KEYS);

    // SELECT statements without limit and without offset

    public final static SqlStatement GetAllUsers =
            new SqlStatement(strGetAllUsers);

    public final static SqlStatement GetUserWithEmail =
            new SqlStatement(strGetUserWithEmail);

    public final static SqlStatement GetUserWithId =
            new SqlStatement(strGetUserWithId);

    public final static SqlStatement GetRequestWithRequestId =
            new SqlStatement(strGetRequestWithRequestId);

    public final static SqlStatement GetJSONRequestWithRequestId =
            new SqlStatement(strGetJSONRequestWithRequestId);

    public final static SqlStatement GetJSONResponseWithRequestId =
            new SqlStatement(strGetJSONResponseWithRequestId);

    // UPDATE statements

    public final static SqlStatement UpdateRequest =
            new SqlStatement(strUpdateRequest);

    public final static SqlStatement UpdateRequestWithComputeResult =
            new SqlStatement(strUpdateRequestWithComputeResult);

    public final static SqlStatement UpdateUser =
            new SqlStatement(strUpdateUser);

    public final static SqlStatement UpdateUserStatusToDeleted =
            new SqlStatement(strUpdateUserStatusToDeleted);

    // DELETE statements

    public final static SqlStatement DeleteRequestWithRequestId =
            new SqlStatement(strDeleteRequestWithRequestId);

    public final static SqlStatement DeleteRequestsOfUserWithUserId =
            new SqlStatement(strDeleteRequestsOfUserWithUserId);

    public final static SqlStatement DeleteUserWithUserId =
            new SqlStatement(strDeleteUserWithUserId);

    public final static SqlStatement DeleteUserWithEmail =
            new SqlStatement(strDeleteUserWithEmail);

    // SELECT statements with limit and offset

    public final static SqlStatement GetAllUsersWithLimitOffset =
            new SqlStatement(strGetAllUsersWithLimitOffset);

    public final static SqlStatement GetAllRequestsNoJsonWithLimitOffset =
            new SqlStatement(strGetAllRequestsNoJsonWithLimitOffset);

    public final static SqlStatement GetRequestsNoJsonWithUserIdLimitOffset =
            new SqlStatement(strGetRequestsNoJsonWithUserIdLimitOffset);

    // statements for COUNTING rows

    public final static SqlStatement CountAllRequests =
            new SqlStatement(strCountAllRequests);

    public final static SqlStatement CountRequestsWithUserId =
            new SqlStatement(strCountRequestsWithUserId);

    public final static SqlStatement CountAllUsers =
            new SqlStatement(strCountAllUsers);




}
