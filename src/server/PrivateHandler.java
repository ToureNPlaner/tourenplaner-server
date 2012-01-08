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
        UserDataset user = null;
        user = authorizer.auth(request);

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
            System.out.println("HttpRequestHandler: ListUsers failed, " +
                    "you must be admin to list users.");
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

        UserDataset user = null;
        user = authorizer.auth(request);

        // authentication needed, auth(request) responses with error if auth
        // fails
        if (user == null) {
            return;
        }

        int userID = -1;
        boolean allRequests = false;
        if (parameters.containsKey("id")) {
            if (!user.admin) {
                responder.writeErrorMessage("ENOTADMIN", "You are not an admin", "You must be admin if you want to use the id parameter", HttpResponseStatus.FORBIDDEN);
                return;
            }

            String idParameter = parameters.get("id").get(0);

            if (idParameter != null) {
                responder.writeErrorMessage("ENOID", "The given user id is unknown to this server", "The given id is null", HttpResponseStatus.UNAUTHORIZED);
                return;
            }

            if ("all".equals(idParameter)) {
                allRequests = true;
            } else {
                try {
                    userID = Integer.parseInt(idParameter);
                } catch (NumberFormatException e) {
                    userID = -1;
                }
            }

            if (userID < 0 && !allRequests) {
                responder.writeErrorMessage("ENOID", "The given user id is unknown to this server", "The given id is not an allowed number (positive or zero)", HttpResponseStatus.UNAUTHORIZED);
                return;
            }
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

        if (userID < 0) {
            userID = user.id;
        }

        List<RequestDataset> requestDatasetList = null;
        int count = 0;
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

        try {
            param = Integer.parseInt(parameters.get(name).get(0));
        } catch (NumberFormatException e) {
            param = -1;
        }

        if (param < 0) {
            responder.writeErrorMessage("E" + name.toUpperCase(), "The given " + name + " is invalid", "You must send a " + name + " parameter", HttpResponseStatus.NOT_ACCEPTABLE);

            return -1;
        }

        return param;
    }

    public void handleUpdateUser(final HttpRequest request) {
        // TODO Auto-generated method stub

    }

    public void handleGetUser(final HttpRequest request) {
        // TODO Auto-generated method stub

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
     */
    public void handleRegisterUser(final HttpRequest request) throws IOException, SQLFeatureNotSupportedException, SQLException {

        UserDataset user = null;
        UserDataset authUser = null;

        // if no authorization header keep on with adding not verified user
        if (request.getHeader("Authorization") != null) {
            authUser = authorizer.auth(request);
            if (authUser == null) {
                // auth(request) sent error response
                return;
            }

            if (!authUser.admin) {
                responder.writeUnauthorizedClose();
                log.warning("RegisterUser failed, a logged in user has to be admin to register users.");
                return;
            }
        }

        Map<String, Object> objmap = getJSONContent(responder, request);

        // getJSONContent adds error-message to responder
        // if json object is bad or if there is no json object
        // so no further handling needed if objmap == null
        if (objmap == null) {
            log.warning("RegisterUser failed, bad json object.");
            return;
        }

        final String email = (String) objmap.get("email");
        final String pw = (String) objmap.get("password");
        final String firstName = (String) objmap.get("firstname");
        final String lastName = (String) objmap.get("lastname");
        final String address = (String) objmap.get("address");

        if ((pw == null) || (email == null) || (firstName == null) || (lastName == null) || (address == null)) {
            // TODO maybe change error id and message
            responder.writeErrorMessage("EBADJSON", "Could not parse supplied JSON", "JSON user object was not correct " + "(needs email, password, firstname, lastname, address)", HttpResponseStatus.UNAUTHORIZED);
            return;
        }

        // TODO optimize salt-generation
        final Random rand = new Random();
        final StringBuilder saltBuilder = new StringBuilder(64);
        for (int i = 0; i < 4; i++) {
            saltBuilder.append(Long.toHexString(rand.nextLong()));
        }

        final String salt = saltBuilder.toString();

        final String toHash = authorizer.generateHash(salt, pw);

        // if no authorization add not verified user
        if (authUser == null) {
            // if there is no authorization as admin, the new registered
            // user will
            // never be registered as admin, even if json admin flag is true
            user = dbm.addNewUser(email, toHash, salt, firstName, lastName, address, false);
        } else if (objmap.get("admin") != null) {

            user = dbm.addNewVerifiedUser(email, toHash, salt, firstName, lastName, address, (Boolean) objmap.get("admin"));
        }

        if (user == null) {
            responder.writeErrorMessage("EREGISTERED", "This email is already registered", null, HttpResponseStatus.FORBIDDEN);
            log.warning("RegisterUser failed, email is already registered.");
            return;
        } else {
            responder.writeJSON(user, HttpResponseStatus.OK);
            log.finest("MasterHandler: RegisterUser succseeded.");
        }

    }

}
