package de.tourenplaner.server;

import de.tourenplaner.database.*;
import de.tourenplaner.utils.SHA1;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 */
public class PrivateHandler extends RequestHandler {

    private static Logger log = Logger.getLogger("de.tourenplaner.server");

    private final DatabaseManager dbm;

    private final Authorizer authorizer;

    private static final ObjectMapper mapper = new ObjectMapper();

    public PrivateHandler(Authorizer authorizer, DatabaseManager dbm) {
        super(null);
        this.dbm = dbm;
        this.authorizer = authorizer;
    }


    /**
     * Extracts and parses the JSON encoded content of the given HttpRequest, in
     * case of error sends a EBADJSON answer to the client and returns null,
     * the connection will be closed afterwards.
     *
     * @param request HttpRequest
     * @return Returns parsed json map or null in case of an error
     * @throws IOException Thrown if error message sending or reading json content fails
     * @throws JsonMappingException Thrown if mapping json content fails
     */
    private Map<String, Object> getJSONContent(final HttpRequest request) throws IOException {

        Map<String, Object> objmap = null;
        final ChannelBuffer content = request.getContent();
        if (content.readableBytes() > 0) {
            try {
                objmap = mapper.readValue(new ChannelBufferInputStream(content), new TypeReference<Map<String, Object>>() {
                });
            } catch (JsonParseException e) {
                responder.writeErrorMessage(ErrorMessage.EBADJSON, e.getMessage());
                objmap = null;
            }

        } else {
            responder.writeErrorMessage(ErrorMessage.EBADJSON_NOCONTENT);
        }

        return objmap;
    }



    /**
     * If authorization is okay, but no admin, registration fails. If no
     * authorization as admin, the new registered user will not be registered as
     * admin, even if json admin flag is true.
     *
     * @param request HttpRequest
     * @throws SQLFeatureNotSupportedException
     *          Thrown if a function is not supported by driver.
     * @throws SQLException
     *          Thrown if database query fails
     * @throws JsonMappingException
     *          Thrown if mapping object to json fails
     * @throws JsonGenerationException
     *          Thrown if generating json fails
     * @throws IOException
     *          Thrown if error message sending or reading/writing json content fails
     */
    public void handleRegisterUser(final HttpRequest request) throws IOException, SQLException {

        UserDataset authenticatedUser = null;

        // if no authorization header keep on with adding not verified user
        if (request.getHeader("Authorization") != null) {
            authenticatedUser = authorizer.auth(request);
            if (authenticatedUser == null) {
                // auth(request) sent error response
                return;
            }

            if (!authenticatedUser.admin) {
                responder.writeErrorMessage(ErrorMessage.ENOTADMIN_REGISTER_USER);
                return;
            }
        }

        Map<String, Object> objmap = getJSONContent(request);

        // getJSONContent adds error-message to responder
        // if json object is bad or if there is no json object
        // so no further handling needed if objmap == null
        if (objmap == null) {
            log.warning("Failed, bad json object.");
            return;
        }

        if ( !(objmap.get("email") instanceof String) || !(objmap.get("password") instanceof String)
                || !(objmap.get("firstname") instanceof String) || !(objmap.get("lastname") instanceof String)
                || !(objmap.get("address") instanceof String) ) {
            responder.writeErrorMessage(ErrorMessage.EBADJSON_INCORRECT_USER_OBJ);
            return;
        }

        final String email = (String) objmap.get("email");
        final String pw = (String) objmap.get("password");
        final String firstName = (String) objmap.get("firstname");
        final String lastName = (String) objmap.get("lastname");
        final String address = (String) objmap.get("address");

        if ((pw == null) || (email == null) || (firstName == null) || (lastName == null) || (address == null)) {
            responder.writeErrorMessage(ErrorMessage.EBADJSON_INCORRECT_USER_OBJ);
            return;
        }

        final String salt = authorizer.generateSalt();

        final String toHash = SHA1.SHA1(pw + ':' + salt);


        UserDataset newUser;

        // if there is no authorization, add user but without verification
        if (authenticatedUser == null) {
            // if there is no authorization as admin, the new registered user will
            // never be registered as admin, even if json admin flag is true
            newUser = dbm.addNewUser(email, toHash, salt, firstName, lastName, address, false);
        } else {

            boolean adminFlag = false;
            if (objmap.get("admin") != null) {
                // if (objmap.get("admin") is null, then "instanceof Boolean" would be always false
                // so following check makes only sense if objmap.get("admin") != null
                if ( !(objmap.get("admin") instanceof Boolean) ) {
                    responder.writeErrorMessage(ErrorMessage.EBADJSON_INCORRECT_USER_OBJ_ADMIN);
                    return;
                }
                adminFlag = (Boolean) objmap.get("admin");
            }
            newUser = dbm.addNewVerifiedUser(email, toHash, salt, firstName, lastName, address, adminFlag);
        }

        if ( newUser == null) {
            responder.writeErrorMessage(ErrorMessage.EREGISTERED);
            return;
        }

        responder.writeJSON(newUser, HttpResponseStatus.OK);
        log.finest("RegisterUser successful.");

    }


    /**
     * Authenticates the client and sends the corresponding user object as json to the client.
     *
     * @param request HttpRequest
     * @throws SQLException Thrown if database query fails
     * @throws JsonGenerationException Thrown if generating json fails
     * @throws JsonMappingException Thrown if mapping object to json fails
     * @throws IOException Thrown if error message sending or writing json content fails
     */
    public void handleAuthUser(final HttpRequest request) throws IOException, SQLException {
        UserDataset user = authorizer.auth(request);
        if (user != null) {
            responder.writeJSON(user, HttpResponseStatus.OK);
        }
    }


    /**
     * Sends the data of one user as json to the client.
     *
     * @param request HttpRequest
     * @param parameters map with url parameters from client
     * @throws SQLException Thrown if database query fails
     * @throws JsonMappingException Thrown if mapping object to json fails
     * @throws JsonGenerationException Thrown if generating json fails
     * @throws IOException Thrown if error message sending or writing json content fails
     */
    public void handleGetUser(final HttpRequest request, Map<String, List<String>> parameters)
            throws IOException, SQLException {
        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth fails
        if (user == null) {
            return;
        }

        int userID;
        UserDataset selectedUser;

        if (parameters.containsKey("id")) {
            userID = parseUserIdParameter(parameters.get("id").get(0), user, false);
            // if parameter is invalid, an error response is sent from parseUserIdParameter.
            // the if and return is needed exactly here, because following methods could send more responses,
            // but only one response per http request is allowed (else Exceptions will be thrown)
            if (userID < 0) {
                return;
            }
            selectedUser = dbm.getUser(userID);
        } else {
            selectedUser = user;
        }

        if (selectedUser == null) {
            responder.writeErrorMessage(ErrorMessage.ENOUSERID_NOT_IN_DB);
            return;
        }

        responder.writeJSON(selectedUser, HttpResponseStatus.OK);
        log.finest("GetUser successful.");

    }


    /**
     * Updates the data of a user.
     *
     * @param request HttpRequest
     * @param parameters map with url parameters from client
     * @throws SQLException Thrown if database query fails
     * @throws JsonMappingException Thrown if mapping object to json fails
     * @throws JsonGenerationException Thrown if generating json fails
     * @throws IOException Thrown if error message sending or reading/writing json content fails
     */
    public void handleUpdateUser(final HttpRequest request, Map<String, List<String>> parameters)
            throws IOException, SQLException {
        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth fails
        if (user == null) {
            return;
        }


        Map<String, Object> objmap = getJSONContent(request);

        // getJSONContent adds error-message to responder
        // if json object is bad or if there is no json object
        // so no further handling needed if objmap == null
        if (objmap == null) {
            log.warning("Failed, bad json object.");
            return;
        }


        int userID;
        boolean isAdmin = user.admin;
        UserDataset selectedUser;

        if (parameters.containsKey("id")) {
            userID = parseUserIdParameter(parameters.get("id").get(0), user, false);
            // if parameter is invalid, an error response is sent from parseUserIdParameter.
            // the if and return is needed exactly here, because following methods could send more responses,
            // but only one response per http request is allowed (else Exceptions will be thrown)
            if (userID < 0) {
                return;
            }
            selectedUser = dbm.getUser(userID);

            if (selectedUser == null) {
                responder.writeErrorMessage(ErrorMessage.ENOUSERID_NOT_IN_DB);
                return;
            }
        } else {
            selectedUser = user;
        }


        if (objmap.get("password") != null && (objmap.get("password") instanceof String)) {
            selectedUser.salt = authorizer.generateSalt();
            selectedUser.passwordhash = SHA1.SHA1((String) objmap.get("password") + ':' + selectedUser.salt);
        }

        if (isAdmin) {
            if (objmap.get("email") != null && (objmap.get("email") instanceof String)) {
                selectedUser.email = (String) objmap.get("email");
            }
            if (objmap.get("firstname") != null && (objmap.get("firstname") instanceof String)) {
                selectedUser.firstName = (String) objmap.get("firstname");
            }
            if (objmap.get("lastname") != null && (objmap.get("lastname") instanceof String)) {
                selectedUser.lastName = (String) objmap.get("lastname");
            }
            if (objmap.get("address") != null && (objmap.get("address") instanceof String)) {
                selectedUser.address = (String) objmap.get("address");
            }

            // the user with id = 1 should always be admin, so no admin flag changing
            if (selectedUser.userid != 1 && objmap.get("admin") != null && (objmap.get("admin") instanceof Boolean)) {
                selectedUser.admin = (Boolean) objmap.get("admin");
            }

            // the user with id = 1 should always be verified, so no status changing
            if (selectedUser.userid != 1 && objmap.get("status") != null && (objmap.get("status") instanceof String)) {
                String status = (String) objmap.get("status");
                UserStatusEnum previousStatus = selectedUser.status;
                try {
                    selectedUser.status = UserStatusEnum.valueOf(status);
                } catch (IllegalArgumentException e) {
                    responder.writeErrorMessage(ErrorMessage.EBADJSON_INCORRECT_USER_OBJ_STATUS);
                    return;
                }

                if (previousStatus == UserStatusEnum.needs_verification
                        && selectedUser.status == UserStatusEnum.verified) {
                    selectedUser.verifiedDate = new Date(System.currentTimeMillis());
                }
            }
        }

        int rowsChanged = dbm.updateUser(selectedUser);
        if (rowsChanged == -1) {
            responder.writeErrorMessage(ErrorMessage.EREGISTERED);
            return;
        }
        responder.writeJSON(selectedUser, HttpResponseStatus.OK);
        log.finest("UpdateUser successful.");
    }


    /**
     * Sends the JsonRequest of the request with the given id to the client.
     *
     * @param request HttpRequest
     * @param parameters map with url parameters from client
     * @throws SQLException Thrown if database query fails
     * @throws IOException Thrown if error message sending or writing content fails
     */
    public void handleGetRequest(final HttpRequest request, Map<String, List<String>> parameters) throws IOException, SQLException {
        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth fails
        if (user == null) {
            return;
        }

        int requestID;
        JSONObject jsonObject;

        if (parameters.containsKey("id")) {
            requestID = parseRequestIdParameter(parameters.get("id").get(0));
            // if parameter is invalid, an error response is sent from parseUserIdParameter.
            // the if and return is needed exactly here, because following methods could send more responses,
            // but only one response per http request is allowed (else Exceptions will be thrown)
            if (requestID < 0) {
                return;
            }

            jsonObject = dbm.getJsonRequest(requestID);

            if (jsonObject == null) {
                responder.writeErrorMessage(ErrorMessage.ENOREQUESTID_NOT_IN_DB);
                return;
            }
        } else {
            responder.writeErrorMessage(ErrorMessage.ENOREQUESTID_MISSING);
            return;
        }

        if (user.userid != jsonObject.getUserID() && !user.admin) {
            responder.writeErrorMessage(ErrorMessage.ENOTADMIN_OTHER_USER_REQUEST);
            return;
        }

        responder.writeByteArray(jsonObject.getJsonByteArray(), HttpResponseStatus.OK);
        log.finest("GetRequest successful.");

    }


    /**
     * Sends the JsonResponse of the request with the given id to the client.
     *
     * @param request HttpRequest
     * @param parameters map with url parameters from client
     * @throws SQLException Thrown if database query fails
     * @throws IOException Thrown if error message sending or writing content fails
     */
    public void handleGetResponse(final HttpRequest request, Map<String, List<String>> parameters) throws IOException, SQLException {
        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth fails
        if (user == null) {
            return;
        }

        int requestID;
        JSONObject jsonObject;

        if (parameters.containsKey("id")) {
            requestID = parseRequestIdParameter(parameters.get("id").get(0));
            // if parameter is invalid, an error response is sent from parseUserIdParameter.
            // the if and return is needed exactly here, because following methods could send more responses,
            // but only one response per http request is allowed (else Exceptions will be thrown)
            if (requestID < 0) {
                return;
            }

            jsonObject = dbm.getJsonResponse(requestID);

            if (jsonObject == null) {
                responder.writeErrorMessage(ErrorMessage.ENOREQUESTID_NOT_IN_DB);
                return;
            }
        } else {
            responder.writeErrorMessage(ErrorMessage.ENOREQUESTID_MISSING);
            return;
        }

        if (user.userid != jsonObject.getUserID() && !user.admin) {
            responder.writeErrorMessage(ErrorMessage.ENOTADMIN_OTHER_USER_REQUEST);
            return;
        }

        responder.writeByteArray(jsonObject.getJsonByteArray(), HttpResponseStatus.OK);
        log.finest("GetResponse successful.");

    }

    /**
     * Sends a list with requests as json to the client.
     *
     * @param request HttpRequest
     * @param parameters map with url parameters from client
     * @throws SQLException Thrown if database query fails
     * @throws JsonGenerationException Thrown if generating json fails
     * @throws JsonMappingException Thrown if mapping object to json fails
     * @throws IOException Thrown if error message sending or writing json content fails
     */
    public void handleListRequests(final HttpRequest request, Map<String, List<String>> parameters)
            throws SQLException, IOException {

        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth
        // fails
        if (user == null) {
            return;
        }


        int limit = extractNaturalIntParameter(parameters, "limit", ErrorMessage.ELIMIT_MISSING,
                ErrorMessage.ELIMIT_NOT_NAT_NUMBER);
        // if parameter is invalid, an error response is sent from extractNaturalIntParameter.
        // the if and return is needed exactly here, because following methods could send more responses,
        // but only one response per http request is allowed (else Exceptions will be thrown)
        if (limit < 0) {
            return;
        }

        int offset = extractNaturalIntParameter(parameters, "offset", ErrorMessage.EOFFSET_MISSING,
                ErrorMessage.EOFFSET_NOT_NAT_NUMBER);
        // if parameter is invalid, an error response is sent from extractNaturalIntParameter.
        // the if and return is needed exactly here, because following methods could send more responses,
        // but only one response per http request is allowed (else Exceptions will be thrown)
        if (offset < 0) {
            return;
        }


        Integer userID;
        boolean allRequests = false;
        if (parameters.containsKey("id")) {
            userID = parseUserIdParameter(parameters.get("id").get(0), user, true);
            // if parameter is invalid, an error response is sent from parseUserIdParameter.
            // the if and return is needed exactly here, because following methods could send more responses,
            // but only one response per http request is allowed (else Exceptions will be thrown)
            if (userID != null && userID < 0) {
                return;
            }
            if (userID == null) {
                allRequests = true;
            }
        } else {
            userID = user.userid;
        }

        List<RequestDataset> requestDatasetList;
        int count;
        if (allRequests) {
            requestDatasetList = dbm.getAllRequests(limit, offset);
            count = dbm.getNumberOfRequests();

        } else {
            requestDatasetList = dbm.getRequests(userID, limit, offset);
            count = dbm.getNumberOfRequestsWithUserId(userID);
        }

        Map<String, Object> responseMap = new HashMap<String, Object>(2);
        responseMap.put("number", count);
        responseMap.put("requests", requestDatasetList);

        responder.writeJSON(responseMap, HttpResponseStatus.OK);
        log.finest("ListRequests successful.");

    }





    /**
     * Sends a list with users as json to the client.
     *
     * @param request HttpRequest
     * @param parameters map with url parameters from client
     * @throws SQLException Thrown if database query fails
     * @throws JsonGenerationException Thrown if generating json fails
     * @throws JsonMappingException Thrown if mapping object to json fails
     * @throws IOException Thrown if error message sending or writing json content fails
     */
    public void handleListUsers(final HttpRequest request, Map<String, List<String>> parameters)
            throws SQLException, IOException {
        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth fails
        if (user == null) {
            return;
        }

        if (!user.admin) {
            responder.writeErrorMessage(ErrorMessage.ENOTADMIN_LIST_USERS);
            return;
        }

        int limit = extractNaturalIntParameter(parameters, "limit", ErrorMessage.ELIMIT_MISSING,
                ErrorMessage.ELIMIT_NOT_NAT_NUMBER);
        // if parameter is invalid, an error response is sent from extractNaturalIntParameter.
        // the if and return is needed exactly here, because following methods could send more responses,
        // but only one response per http request is allowed (else Exceptions will be thrown)
        if (limit < 0) {
            return;
        }

        int offset = extractNaturalIntParameter(parameters, "offset", ErrorMessage.EOFFSET_MISSING,
                ErrorMessage.EOFFSET_NOT_NAT_NUMBER);
        // if parameter is invalid, an error response is sent from extractNaturalIntParameter.
        // the if and return is needed exactly here, because following methods could send more responses,
        // but only one response per http request is allowed (else Exceptions will be thrown)
        if (offset < 0) {
            return;
        }

        List<UserDataset> userDatasetList;
        userDatasetList = dbm.getAllUsers(limit, offset);
        int count = dbm.getNumberOfUsers();

        Map<String, Object> responseMap = new HashMap<String, Object>(2);
        responseMap.put("number", count);
        responseMap.put("users", userDatasetList);

        responder.writeJSON(responseMap, HttpResponseStatus.OK);
        log.finest("ListUsers successful.");

    }


    /**
     * Sets the status flag of the user to deleted.
     *
     * @param request HttpRequest
     * @param parameters map with url parameters from client
     * @throws IOException Thrown if error message sending fails
     * @throws SQLException Thrown if database query fails
     */
    public void handleDeleteUser(final HttpRequest request, Map<String, List<String>> parameters)
            throws IOException, SQLException {
        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth fails
        if (user == null) {
            return;
        }

        int userID;
        if (parameters.containsKey("id")) {
            userID = parseUserIdParameter(parameters.get("id").get(0), user, false);
            // if parameter is invalid, an error response is sent from parseUserIdParameter.
            // the if and return is needed exactly here, because following methods could send more responses,
            // but only one response per http request is allowed (else Exceptions will be thrown)
            if (userID < 0) {
                return;
            }
        } else {
            responder.writeErrorMessage(ErrorMessage.ENOUSERID_MISSING);
            return;
        }

        // the user with id = 1 should always be verified, so no status changing
        if (userID != 1) {
            if (dbm.updateUserStatusToDeleted(userID) != 1) {
                responder.writeErrorMessage(ErrorMessage.ENOUSERID_NOT_IN_DB);
                return;
            }
        }

        responder.writeStatusResponse(HttpResponseStatus.OK);
        log.finest("DeleteUser successful.");

    }



    /**
     * Returns the parsed number or -1 if parameter is invalid (not a natural number) and will then
     * response to request with error message.
     * @param parameterValue the value of the id parameter as String
     * @return Returns the parsed number or -1 if parameter is invalid (not a natural number)
     * @throws IOException Thrown if error message sending fails
     */
    private int parseRequestIdParameter(String parameterValue) throws IOException {

        int requestID;

        try {
            requestID = Integer.parseInt(parameterValue);
        } catch (NumberFormatException e) {
            requestID = -1;
        }

        if (requestID < 0) {
            responder.writeErrorMessage(ErrorMessage.ENOREQUESTID_NOT_NAT_NUMBER);
            return requestID;
        }

        return requestID;

    }


    /**
     * Returns the parsed number or -1 if parameter is invalid (not a natural number) and will then
     * response to request with error message. But the authenticated user must be an admin (will be checked by this
     * method) or an error message will be sent. If "id=all" is allowed and the value of the parameter is "all",
     * this method will return null.
     * @param parameterValue the value of the id parameter as String
     * @param authenticatedUser UserDataset of the user who sent the request
     * @param valueAllIsAllowed determines if id=all is allowed for the parameter
     * @return Returns the parsed number or -1 if parameter is invalid (not a natural number).
     *          If "id=all" is allowed and the value of the parameter is "all",
     *          this method will return null.
     * @throws IOException Thrown if error message sending fails
     */
    private Integer parseUserIdParameter(String parameterValue, UserDataset authenticatedUser,
                                           boolean valueAllIsAllowed) throws IOException {

        if (!authenticatedUser.admin) {
            responder.writeErrorMessage(ErrorMessage.ENOTADMIN_USER_ID_PARAM);
            return -1;
        }

        int userID;

        if ("all".equals(parameterValue) && valueAllIsAllowed) {
            return null;
        } else {
            try {
                userID = Integer.parseInt(parameterValue);
            } catch (NumberFormatException e) {
                userID = -1;
            }
        }

        if (userID < 0) {
            if (valueAllIsAllowed) {
                responder.writeErrorMessage(ErrorMessage.ENOUSERID_NOT_NAT_NUMBER_OR_ALL);
            } else {
                responder.writeErrorMessage(ErrorMessage.ENOUSERID_NOT_NAT_NUMBER);
            }
            return userID;
        }

        return userID;
    }


    /**
     * Returns the parsed number or -1 if parameter is invalid (missing or not a natural number)
     * and will then response to request with error message.
     * @param parameters map with url parameters from client
     * @param name the name of the parameter
     * @param eMissing the corresponding ErrorMessage to the parameter if parameter is missing
     * @param eNotNaturalNumber the corresponding ErrorMessage to the parameter if parameter is not a natural number
     * @return Returns the parsed number or -1 if parameter is invalid (missing or not a natural number)
     * @throws IOException Thrown if error message sending fails
     */
    private int extractNaturalIntParameter(Map<String, List<String>> parameters, String name, 
                                           ErrorMessage eMissing, ErrorMessage eNotNaturalNumber)
            throws IOException {
        int param;

        if (!parameters.containsKey(name) || parameters.get(name).get(0) == null) {
            responder.writeErrorMessage(eMissing);
            return -1;
        }

        try {
            param = Integer.parseInt(parameters.get(name).get(0));
        } catch (NumberFormatException e) {
            param = -1;
        }

        if (param < 0) {
            responder.writeErrorMessage(eNotNaturalNumber);

            return -1;
        }

        return param;
    }


}
