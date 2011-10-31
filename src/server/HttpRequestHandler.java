/**
 * $$\\ToureNPlaner\\$$
 */

package server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;

import algorithms.Points;

import computecore.ComputeCore;
import computecore.ComputeRequest;

import config.ConfigManager;
import database.DatabaseManager;
import database.UsersDBRow;

/**
 * This handler handles HTTP Requests on the normal operation socket including *
 * 
 * @author Niklas Schnelle, Peter Vollmer
 * @version 0.1
 * 
 *          Initially based on: http://docs.jboss.org/netty/3.2/xref
 *          /org/jboss/netty/example/http/snoop/package-summary.html
 */
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

	/** ObjectMapper we can reuse **/
	private final ObjectMapper mapper;

	/** The ComputeCore managing the threads **/
	private final ComputeCore computer;

	private final Map<String, Object> serverInfo;

	private boolean isPrivate;

	private DatabaseManager dbm;

	private MessageDigest digester;

	private Responder responder;

	private static final class MapType extends
			TypeReference<Map<String, Object>> {
	};

	private static final MapType JSONOBJECT = new MapType();

	/**
	 * Constructs a new RequestHandler using the given ComputeCore and
	 * ServerInfo
	 * 
	 * @param cCore
	 * @param serverInfo
	 */
	public HttpRequestHandler(final ObjectMapper mapper,
			final ComputeCore cCore, final Map<String, Object> serverInfo) {
		super();
		final ConfigManager cm = ConfigManager.getInstance();
		this.mapper = mapper;
		this.computer = cCore;
		this.serverInfo = serverInfo;
		this.isPrivate = cm.getEntryBool("private", false);

		if (isPrivate) {
			try {
				this.dbm = new DatabaseManager(cm.getEntryString("dburi",
						"jdbc:mysql://localhost:3306/"), cm.getEntryString(
						"dbname", "tourenplaner"), cm.getEntryString("dbuser",
						"toureNPlaner"), cm.getEntryString("dbpw",
						"toureNPlaner"));
				digester = MessageDigest.getInstance("SHA-1");
			} catch (SQLException e) {
				System.err
						.println("Can't connect to database (switching to public mode) "
								+ e.getMessage());
				this.isPrivate = false;
			} catch (NoSuchAlgorithmException e) {
				System.err
						.println("Can't load SHA-1 Digester. Will now switch to public mode");
				this.isPrivate = false;
			}
		} else {
			digester = null;
		}
	}

	/**
	 * Called when a message is received
	 */
	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final MessageEvent e) throws Exception {

		final HttpRequest request = (HttpRequest) e.getMessage();
		final Channel channel = e.getChannel();
		// System.out.print(request.toString());
		// Handle preflighted requests so wee need to work with OPTION Requests
		if (request.getMethod().equals(HttpMethod.OPTIONS)) {
			handlePreflights(request, channel);
			return;
		}

		if (responder == null) {
			responder = new Responder(mapper, channel, isKeepAlive(request));
		}

		// Get the Requeststring e.g. /info
		final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
				request.getUri());

		final String path = queryStringDecoder.getPath();

		if ("/info".equals(path)) {

			handleInfo(request);

		} else if (path.startsWith("/alg")) {

			if (isPrivate && (auth(request) == null)) {
				responder.writeUnauthorizedClose();
			} else {
				final String algName = queryStringDecoder.getPath()
						.substring(4);
				handleAlg(request, algName);
			}

		} else if (isPrivate) {
			if ("/registeruser".equals(path)) {

				handleRegisterUser(request);

			} else if ("/authuser".equals(path)) {

				handleAuthUser(request);

			} else if ("/getuser".equals(path)) {

				handleGetUser(request);

			} else if ("/updateuser".equals(path)) {

				handleUpdateUser(request);

			} else if ("/listrequests".equals(path)) {

				handleListRequests(request);

			} else if ("/listusers".equals(path)) {

				handleListUsers(request);

			}
		} else {
			// Unknown request, close connection
			responder.writeErrorMessage("EUNKNOWNURL",
					"An unknown URL was requested", null,
					HttpResponseStatus.NOT_FOUND);
		}

	}

	private void handleAlg(final HttpRequest request, final String algName)
			throws IOException {

		final ComputeRequest req = readComputeRequest(algName, responder,
				request);
		if (req != null) {

			final boolean success = computer.submit(req);

			if (!success) {
				responder
						.writeErrorMessage(
								"EBUSY",
								"This server is currently too busy to fullfill the request",
								null, HttpResponseStatus.SERVICE_UNAVAILABLE);
			}
		}
	}

	/**
	 * Extracts and parses the JSON encoded content of the given HttpRequest, in
	 * case of error sends a EBADJSON or HttpStatus.NO_CONTENT answer to the
	 * client and returns null, the connection will be closed afterwards.
	 * 
	 * @param responder
	 * @param request
	 * @throws IOException
	 */
	private Map<String, Object> getJSONContent(final Responder responder,
			final HttpRequest request) throws IOException {

		Map<String, Object> objmap = null;
		final ChannelBuffer content = request.getContent();
		if (content.readableBytes() > 0) {
			try {
				objmap = mapper.readValue(
						new ChannelBufferInputStream(content),
						new TypeReference<Map<String, Object>>() {
						});
			} catch (JsonParseException e) {
				responder.writeErrorMessage("EBADJSON",
						"Could not parse supplied JSON", null,
						HttpResponseStatus.UNAUTHORIZED);
				objmap = null;
			}

		} else {
			// Respond with No Content
			final HttpResponse response = new DefaultHttpResponse(HTTP_1_1,
					NO_CONTENT);
			// Write the response.
			final ChannelFuture future = responder.getChannel().write(response);
			future.addListener(ChannelFutureListener.CLOSE);
		}

		return objmap;
	}

	/**
	 * Reads a JSON encoded compute request from the content field of the given
	 * request
	 * 
	 * @param mapper
	 * @param algName
	 * @param responder
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws JsonParseException
	 */
	private ComputeRequest readComputeRequest(final String algName,
			final Responder responder, final HttpRequest request)
			throws IOException, JsonParseException {

		Map<String, Object> constraints = null;
		final Points points = new Points();
		final ChannelBuffer content = request.getContent();
		if (content.readableBytes() > 0) {

			final JsonParser jp = mapper.getJsonFactory().createJsonParser(
					new ChannelBufferInputStream(content));
			jp.setCodec(mapper);

			if (jp.nextToken() != JsonToken.START_OBJECT) {
				throw new JsonParseException("Request contains no json object",
						jp.getCurrentLocation());
			}

			String fieldname;
			JsonToken token;
			double lat = 0, lon = 0;
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				fieldname = jp.getCurrentName();
				token = jp.nextToken(); // move to value, or
										// START_OBJECT/START_ARRAY
				if ("points".equals(fieldname)) {
					// Should be on START_ARRAY
					if (token != JsonToken.START_ARRAY) {
						throw new JsonParseException("points is no array",
								jp.getCurrentLocation());
					}
					// Read array elements
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						while (jp.nextToken() != JsonToken.END_OBJECT) {
							fieldname = jp.getCurrentName();
							jp.nextToken();
							if ("lt".equals(fieldname)) {
								lat = jp.getDoubleValue();
							} else if ("ln".equals(fieldname)) {
								lon = jp.getDoubleValue();
							} else {
								throw new JsonParseException("Unknown field "
										+ fieldname, jp.getCurrentLocation());
							}
						}
						points.addPoint(lat, lon);
					}

				} else if ("constraints".equals(fieldname)) {
					constraints = jp.readValueAs(JSONOBJECT);
				} else {
					// ignore for now TODO: user version string etc.
					if ((token != JsonToken.START_ARRAY)
							&& (token != JsonToken.START_OBJECT)) {
						jp.nextToken();
					} else {
						jp.skipChildren();
					}
				}
			}

		} else {
			// Respond with No Content
			final HttpResponse response = new DefaultHttpResponse(HTTP_1_1,
					NO_CONTENT);
			// Write the response.
			final ChannelFuture future = responder.getChannel().write(response);
			future.addListener(ChannelFutureListener.CLOSE);
		}

		return new ComputeRequest(responder, algName, points, constraints);
	}

	private void handleListUsers(final HttpRequest request) {
		// TODO Auto-generated method stub

	}

	private void handleListRequests(final HttpRequest request) {
		// TODO Auto-generated method stub

	}

	private void handleUpdateUser(final HttpRequest request) {
		// TODO Auto-generated method stub

	}

	private void handleGetUser(final HttpRequest request) {
		// TODO Auto-generated method stub

	}

	private void handleAuthUser(final HttpRequest request) {
		// TODO Auto-generated method stub

	}

	private void handleRegisterUser(final HttpRequest request) {
		try {
			UsersDBRow user = null;
			UsersDBRow authUser = null;

			final Map<String, Object> objmap = getJSONContent(responder,
					request);
			if (objmap == null) {
				return;
			}

			// TODO optimize salt-generation
			final Random rand = new Random();
			final StringBuilder saltBuilder = new StringBuilder(100);
			for (int i = 0; i < 10; i++) {
				saltBuilder.append(Long.toHexString(rand.nextLong()));
			}

			final String salt = saltBuilder.toString();
			final String pw = (String) objmap.get("password");

			final String toHash = generateHash(salt, pw);
			// TODO cover all cases

			// if no authorization header add not verified user
			if (request.getHeader("Authorization") == null) {

				user = dbm.addNewUser((String) objmap.get("email"), toHash,
						salt, (String) objmap.get("firstname"),
						(String) objmap.get("lastname"),
						(String) objmap.get("address"),
						(Boolean) objmap.get("admin"));
			} else {
				if (((authUser = auth(request)) != null) && authUser.isAdmin) {

					user = dbm.addNewVerifiedUser((String) objmap.get("email"),
							toHash, salt, (String) objmap.get("firstname"),
							(String) objmap.get("lastname"),
							(String) objmap.get("address"),
							(Boolean) objmap.get("admin"));

				} else {
					responder.writeUnauthorizedClose();
				}
			}

			// TODO handler if user already existed
			if (user == null) {

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			// TODO JSON Exception Handling
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected String generateHash(final String salt, final String pw) {
		// Compute SHA1 of PW:SALT
		String toHash = pw + ":" + salt;

		final byte[] bindigest = digester.digest(toHash
				.getBytes(CharsetUtil.UTF_8));
		// Convert to Hex String
		final StringBuilder hexbuilder = new StringBuilder(bindigest.length * 2);
		for (byte b : bindigest) {
			hexbuilder.append(Integer.toHexString((b >>> 4) & 0x0F));
			hexbuilder.append(Integer.toHexString(b & 0x0F));
		}
		toHash = hexbuilder.toString();
		return toHash;
	}

	private void handleInfo(final HttpRequest request)
			throws JsonGenerationException, JsonMappingException, IOException {
		responder.writeJSON(serverInfo, HttpResponseStatus.OK);
	}

	/**
	 * Handles preflighted OPTION Headers
	 * 
	 * @param request
	 * @param channel
	 */
	private void handlePreflights(final HttpRequest request,
			final Channel channel) {
		boolean keepAlive = isKeepAlive(request);
		HttpResponse response;

		// We only allow POST and GET methods so only allow request when Method
		// is Post or Get
		final String methodType = request
				.getHeader("Access-Control-Request-Method");
		if ((methodType != null)
				&& (methodType.trim().equals("POST") || methodType.trim()
						.equals("GET"))) {
			response = new DefaultHttpResponse(HTTP_1_1, OK);
			response.addHeader("Connection", "Keep-Alive");
		} else {
			response = new DefaultHttpResponse(HTTP_1_1, FORBIDDEN);
			// We don't want to keep the connection now
			keepAlive = false;
		}

		final ArrayList<String> allowHeaders = new ArrayList<String>(2);
		allowHeaders.add("Content-Type");
		allowHeaders.add("Authorization");

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
		response.setHeader(CONTENT_TYPE, "application/json");
		response.setHeader("Content-Length", "0");

		response.setHeader("Access-Control-Allow-Headers", allowHeaders);

		final ChannelFuture future = channel.write(response);
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}

	}

	/**
	 * Authenticates a Request using HTTP Basic Authentication and returns the
	 * UsersDBRow object of the authenticated user or null if authentication
	 * failed. Errors will be sent to the client as error messages see protocol
	 * specification for details. The connection will get closed after the error
	 * has been sent
	 * 
	 * @param request
	 * @return the UsersDBRow object of the user or null if auth failed
	 * @throws SQLException
	 */
	private UsersDBRow auth(final HttpRequest myReq) throws SQLException {
		String email, emailandpw, pw;
		UsersDBRow user = null;
		int index = 0;
		// Why between heaven and earth does Java have AES Encryption in
		// the standard library but not Base64 though it has it internally
		// several times
		emailandpw = myReq.getHeader("Authorization");
		if (emailandpw == null) {
			return null;
		}

		ChannelBuffer encodeddata;
		ChannelBuffer data;
		// Base64 is always ASCII
		encodeddata = ChannelBuffers.wrappedBuffer(emailandpw.substring(
				emailandpw.lastIndexOf(' ')).getBytes(CharsetUtil.US_ASCII));

		data = Base64.decode(encodeddata);
		// The string itself is utf-8
		emailandpw = data.toString(CharsetUtil.UTF_8);
		index = emailandpw.indexOf(':');
		if (index <= 0) {
			return null;
		}

		email = emailandpw.substring(0, index);
		pw = emailandpw.substring(index + 1);
		// TODO Database

		user = dbm.getUser(email);
		if (user == null) {
			responder.writeErrorMessage("EAUTH", "Wrong username or password",
					null, HttpResponseStatus.UNAUTHORIZED);
			return null;
		}

		// Compute SHA1 of PW:SALT
		final String toHash = generateHash(user.salt, pw);

		System.out.println(pw + ":" + user.salt + " : " + toHash);
		if (!user.passwordhash.equals(toHash)) {
			responder.writeErrorMessage("EAUTH", "Wrong username or password",
					null, HttpResponseStatus.UNAUTHORIZED);
			return null;
		}

		return user;
	}

	/**
	 * Called when an uncaught exception occurs
	 */
	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
			final ExceptionEvent e) throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
