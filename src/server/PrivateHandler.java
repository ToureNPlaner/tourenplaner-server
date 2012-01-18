package server;

import database.DatabaseManager;
import database.RequestDataset;
import database.UserDataset;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.*;
import java.util.logging.Logger;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * User: Niklas Schnelle, Sascha Meusel
 * Date: 12/26/11
 * Time: 11:33 PM
 */
public class PrivateHandler extends RequestHandler {

    private static Logger log = Logger.getLogger("server");

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
     * case of error sends a EBADJSON or HttpStatus.NO_CONTENT answer to the
     * client and returns null, the connection will be closed afterwards.
     *
     * @param responder
     * @param request
     * @return
     * @throws java.io.IOException
     */
    private Map<String, Object> getJSONContent(final Responder responder, final HttpRequest request) throws IOException {

        Map<String, Object> objmap = null;
        final ChannelBuffer content = request.getContent();
        if (content.readableBytes() > 0) {
            try {
                objmap = mapper.readValue(new ChannelBufferInputStream(content), new TypeReference<Map<String, Object>>() {
                });
            } catch (JsonParseException e) {
                responder.writeErrorMessage("EBADJSON", "Could not parse supplied JSON", e.getMessage(), HttpResponseStatus.UNAUTHORIZED);
                objmap = null;
            }

        } else {
            // Respond with No Content
            final HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NO_CONTENT);
            // Write the response.
            final ChannelFuture future = responder.getChannel().write(response);
            future.addListener(ChannelFutureListener.CLOSE);
        }

        return objmap;
    }

    public void handleListUsers(final HttpRequest request, Map<String, List<String>> parameters) throws SQLException, JsonGenerationException, JsonMappingException, IOException {
        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth fails
        if (user == null) {
            return;
        }

        if (!user.admin) {
            responder.writeErrorMessage(
                    "ENOTADMIN",
                    "You are not an admin",
                    "You must be admin to list users",
                    HttpResponseStatus.FORBIDDEN);
            return;
        }

        int limit = extractPosIntParameter(parameters, "limit");
        // if parameter is invalid, an error response is sent from extractPosIntParameter.
        // the if and return is needed exactly here, because following methods could send more responses,
        // but only one response per http request is allowed (else Exceptions will be thrown)
        if (limit < 0) {
            return;
        }

        int offset = extractPosIntParameter(parameters, "offset");
        // if parameter is invalid, an error response is sent from extractPosIntParameter.
        // the if and return is needed exactly here, because following methods could send more responses,
        // but only one response per http request is allowed (else Exceptions will be thrown)
        if (offset < 0) {
            return;
        }

        List<UserDataset> userDatasetList = null;
        userDatasetList = dbm.getAllUsers(limit, offset);
        int count = dbm.getNumberOfUsers();

        Map<String, Object> responseMap = new HashMap<String, Object>(2);
        responseMap.put("number", count);
        responseMap.put("users", userDatasetList);

        responder.writeJSON(responseMap, HttpResponseStatus.OK);
        log.finest("ListUsers successful.");

    }

    public void handleListRequests(final HttpRequest request, Map<String, List<String>> parameters) throws SQLException, JsonGenerationException, JsonMappingException, IOException {

        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth
        // fails
        if (user == null) {
            return;
        }


        int limit = extractPosIntParameter(parameters, "limit");
        // if parameter is invalid, an error response is sent from extractPosIntParameter.
        // the if and return is needed exactly here, because following methods could send more responses,
        // but only one response per http request is allowed (else Exceptions will be thrown)
        if (limit < 0) {
            return;
        }

        int offset = extractPosIntParameter(parameters, "offset");
        // if parameter is invalid, an error response is sent from extractPosIntParameter.
        // the if and return is needed exactly here, because following methods could send more responses,
        // but only one response per http request is allowed (else Exceptions will be thrown)
        if (offset < 0) {
            return;
        }


        Integer userID = -1;
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
            userID = user.id;
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


    public void handleGetRequest(final HttpRequest request, Map<String, List<String>> parameters) {
        // TODO Auto-generated method stub

    }

    public void handleUpdateUser(final HttpRequest request, Map<String, List<String>> parameters) {
        // TODO Auto-generated method stub

    }

    public void handleGetUser(final HttpRequest request, Map<String, List<String>> parameters) throws IOException, SQLException {
        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth fails
        if (user == null) {
            return;
        }

        int userID = -1;
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
            responder.writeErrorMessage("ENOUSERID", "The given user id is unknown to this server", "The user id is not in the database", HttpResponseStatus.UNAUTHORIZED);
            return;
        }

        responder.writeJSON(selectedUser, HttpResponseStatus.OK);
        log.finest("GetUser successful.");

    }

    public void handleDeleteUser(final HttpRequest request, Map<String, List<String>> parameters) throws IOException, SQLException {
        UserDataset user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth fails
        if (user == null) {
            return;
        }

        int userID = -1;
        if (parameters.containsKey("id")) {
            userID = parseUserIdParameter(parameters.get("id").get(0), user, false);
            // if parameter is invalid, an error response is sent from parseUserIdParameter.
            // the if and return is needed exactly here, because following methods could send more responses,
            // but only one response per http request is allowed (else Exceptions will be thrown)
            if (userID < 0) {
                return;
            }
        } else {
            responder.writeErrorMessage("ENOUSERID", "The given user id is unknown to this server", "You must send an id parameter", HttpResponseStatus.UNAUTHORIZED);
            return;
        }

        dbm.deleteRequestsOfUser(userID);
        if (dbm.deleteUser(userID) != 1) {
            responder.writeErrorMessage("ENOUSERID", "The given user id is unknown to this server", "The user id is not in the database", HttpResponseStatus.UNAUTHORIZED);
            return;
        }

        responder.writeStatusResponse(HttpResponseStatus.OK);
        log.finest("DeleteUser successful.");

    }
    
    /**
     * Returns -1 if parameter is invalid (not a natural number) and will then
     * response to request with error message. But the authenticated user must be an admin (will be checked by this
     * method) or an error message will be sent. If "id=all" is allowed and the value of the parameter is "all",
     * this method will return null.
     * @param parameterValue the value of the id parameter as String
     * @param authenticatedUser UserDataset of the user who sent the request
     * @param valueAllIsAllowed determines if id=all is allowed for the parameter
     * @return
     * @throws java.io.IOException
     */
    private Integer parseUserIdParameter(String parameterValue, UserDataset authenticatedUser,
                                           boolean valueAllIsAllowed) throws IOException {

        if (!authenticatedUser.admin) {
            responder.writeErrorMessage("ENOTADMIN", "You are not an admin", "You must be admin if you want to use the id parameter", HttpResponseStatus.FORBIDDEN);
            return -1;
        }

        if (parameterValue == null) {
            responder.writeErrorMessage("ENOUSERID", "The given user id is unknown to this server", "The given id is null", HttpResponseStatus.UNAUTHORIZED);
            return -1;
        }

        int userID = -1;

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
            responder.writeErrorMessage("ENOUSERID", "The given user id is unknown to this server", "The given id is not an allowed number (positive or zero)", HttpResponseStatus.UNAUTHORIZED);
            return userID;
        }

        return userID;
        
    }

    /**
     * Returns -1 if parameter is invalid (missing or not a natural number) and will then response to request
     * with error message.
     * @param parameters
     * @return
     */
    private int extractPosIntParameter(Map<String, List<String>> parameters, String name) throws IOException{
        int param = -1;

        if (!parameters.containsKey(name)) {
            responder.writeErrorMessage("E" + name.toUpperCase(), "The given " + name + " is invalid", "You must send a " + name + " parameter", HttpResponseStatus.NOT_ACCEPTABLE);
            return -1;
        }

        if  (parameters.get(name).get(0) != null) {
            try {
                param = Integer.parseInt(parameters.get(name).get(0));
            } catch (NumberFormatException e) {
                param = -1;
            }
        }

        if (param < 0) {
            responder.writeErrorMessage("E" + name.toUpperCase(), "The given " + name + " is invalid", "You must send a " + name + " parameter", HttpResponseStatus.NOT_ACCEPTABLE);

            return -1;
        }

        return param;
    }

    public void handleAuthUser(final HttpRequest request) throws JsonGenerationException, JsonMappingException, IOException, SQLException {
        UserDataset user = authorizer.auth(request);
        if (user != null) responder.writeJSON(user, HttpResponseStatus.OK);

    }

    /**
     * If authorization is okay, but no admin, registration fails. If no
     * authorization as admin, the new registered user will not be registered as
     * admin, even if json admin flag is true.
     *
     * @param request
     * @throws java.sql.SQLFeatureNotSupportedException
     *
     * @throws SQLException
     * @throws java.io.IOException
     */
    public void handleRegisterUser(final HttpRequest request) throws IOException, SQLFeatureNotSupportedException, SQLException {

        UserDataset authenticatedUser = null;

        // if no authorization header keep on with adding not verified user
        if (request.getHeader("Authorization") != null) {
            authenticatedUser = authorizer.auth(request);
            if (authenticatedUser == null) {
                // auth(request) sent error response
                return;
            }

            if (!authenticatedUser.admin) {
                responder.writeErrorMessage("ENOTADMIN", "You are not an admin", "A logged in user has to be admin to register users.", HttpResponseStatus.UNAUTHORIZED);
                return;
            }
        }

        Map<String, Object> objmap = getJSONContent(responder, request);

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
            responder.writeErrorMessage("EBADJSON", "Could not parse supplied JSON",
                    "JSON user object was not correct (needs email, password, firstname, lastname, address)",
                    HttpResponseStatus.UNAUTHORIZED);
            return;
        }
        
        final String email = (String) objmap.get("email");
        final String pw = (String) objmap.get("password");
        final String firstName = (String) objmap.get("firstname");
        final String lastName = (String) objmap.get("lastname");
        final String address = (String) objmap.get("address");

        if ((pw == null) || (email == null) || (firstName == null) || (lastName == null) || (address == null)) {
            responder.writeErrorMessage("EBADJSON", "Could not parse supplied JSON",
                    "JSON user object was not correct (needs email, password, firstname, lastname, address)",
                    HttpResponseStatus.UNAUTHORIZED);
            return;
        }

        final String salt = authorizer.generateSalt();

        final String toHash = authorizer.generateHash(salt, pw);


        UserDataset newUser;

        // if there is no authorization, add user but without verification
        if (authenticatedUser == null) {
            // if there is no authorization as admin, the new registered user will
            // never be registered as admin, even if json admin flag is true
            newUser = dbm.addNewUser(email, toHash, salt, firstName, lastName, address, false);
        } else {

            boolean adminFlag = false;
            // TODO specify the case objmap.get("admin") == null for protocol specification: adminFlag is then false
            if (objmap.get("admin") != null) {
                // if (objmap.get("admin") is null, then "instanceof Boolean" would be always false
                if ( !(objmap.get("admin") instanceof Boolean) ) {
                    responder.writeErrorMessage("EBADJSON", "Could not parse supplied JSON",
                            "JSON user object was not correct (\"admin\" should be boolean)",
                            HttpResponseStatus.UNAUTHORIZED);
                    return;
                }
                adminFlag = (Boolean) objmap.get("admin");
            }
            newUser = dbm.addNewVerifiedUser(email, toHash, salt, firstName, lastName, address, adminFlag);
        }

        if ( newUser == null) {
            responder.writeErrorMessage("EREGISTERED", "This email is already registered", null, HttpResponseStatus.FORBIDDEN);
            return;
        }

        responder.writeJSON(newUser, HttpResponseStatus.OK);
        log.finest("RegisterUser successful.");

    }



}
