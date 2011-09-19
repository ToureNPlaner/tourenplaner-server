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

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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

	/** JSONParser we can reuse **/
	private final JSONParser parser = new JSONParser();

	/** The ComputeCore managing the threads **/
	private final ComputeCore computer;

	private final Map<String, Object> serverInfo;

	private final boolean isPrivate;

	private DatabaseManager dbm;

	/**
	 * Constructs a new RequestHandler using the given ComputeCore and
	 * ServerInfo
	 * 
	 * @param cCore
	 * @param serverInfo
	 */
	public HttpRequestHandler(ComputeCore cCore,
			Map<String, Object> serverInfo, boolean isPrivate) {
		super();
		ConfigManager cm = ConfigManager.getInstance();
		computer = cCore;
		this.serverInfo = serverInfo;
		this.isPrivate = isPrivate;
		if (isPrivate) {
			try {

				this.dbm = new DatabaseManager(cm.getEntryString("dburi",
						"jdbc:mysql://localhost:3306/"), cm.getEntryString(
						"dbname", "tourenplaner"), cm.getEntryString("dbuser",
						"toureNPlaner"), cm.getEntryString("dbpw",
						"toureNPlaner"));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Called when a message is received
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		HttpRequest request = (HttpRequest) e.getMessage();
		Channel channel = e.getChannel();
		// System.out.print(request.toString());
		// Handle preflighted requests so wee need to work with OPTION Requests
		if (request.getMethod().equals(HttpMethod.OPTIONS)) {
			handlePreflights(request, channel);
			return;
		}

		Responder responder = new Responder(channel, isKeepAlive(request));

		// Get the Requeststring e.g. /info
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
				request.getUri());

		String path = queryStringDecoder.getPath();

		if (path.equals("/info")) {

			handleInfo(request, responder);

		} else if (isPrivate && path.equals("/registeruser")) {

			handleRegisterUser(request, responder);

		} else if (isPrivate && path.equals("/authuser")) {

			handleAuthUser(request, responder);

		} else if (isPrivate && path.equals("/getuser")) {

			handleGetUser(request, responder);

		} else if (isPrivate && path.equals("/updateuser")) {

			handleUpdateUser(request, responder);

		} else if (isPrivate && path.equals("/listrequests")) {

			handleListRequests(request, responder);

		} else if (isPrivate && path.equals("/listusers")) {

			handleListUsers(request, responder);

		} else if (path.startsWith("/alg")) {

			if (!isPrivate || auth(request)) {
				String algName = queryStringDecoder.getPath().substring(4);
				handleAlg(request, responder, algName);
			} else {
				responder.writeUnauthorizedClose();
			}
		}

	}

	private void handleAlg(HttpRequest request, Responder responder,
			String algName) throws Exception {

		ChannelBuffer content = request.getContent();
		if (content.readableBytes() != 0) {
			InputStreamReader inReader = new InputStreamReader(
					new ChannelBufferInputStream(content));

			JSONObject requestJSON = (JSONObject) parser.parse(inReader);
			// System.out.println(requestJSON);
			@SuppressWarnings("unchecked")
			Map<String, Object> objmap = requestJSON;

			// Create ComputeRequest and commit to workqueue
			ComputeRequest req = new ComputeRequest(responder, algName, objmap);
			boolean sucess = computer.submit(req);

			if (!sucess) {
				responder.writeServerOverloaded();
			}
		} else {
			// Respond with No Content
			HttpResponse response = new DefaultHttpResponse(HTTP_1_1,
					NO_CONTENT);
			// Write the response.
			ChannelFuture future = responder.getChannel().write(response);
			future.addListener(ChannelFutureListener.CLOSE);
		}

	}

	private void handleListUsers(HttpRequest request, Responder responder) {
		// TODO Auto-generated method stub

	}

	private void handleListRequests(HttpRequest request, Responder responder) {
		// TODO Auto-generated method stub

	}

	private void handleUpdateUser(HttpRequest request, Responder responder) {
		// TODO Auto-generated method stub

	}

	private void handleGetUser(HttpRequest request, Responder responder) {
		// TODO Auto-generated method stub

	}

	private void handleAuthUser(HttpRequest request, Responder responder) {
		// TODO Auto-generated method stub

	}

	private void handleRegisterUser(HttpRequest request, Responder responder) {
		// TODO Auto-generated method stub

	}

	private void handleInfo(HttpRequest request, Responder responder) {
		responder.writeJSON(serverInfo, HttpResponseStatus.OK);
	}

	/**
	 * Handles preflighted OPTION Headers
	 * 
	 * @param request
	 * @param channel
	 */
	private void handlePreflights(HttpRequest request, Channel channel) {
		boolean keepAlive = isKeepAlive(request);
		HttpResponse response;

		// We only allow POST and GET methods so only allow request when Method
		// is Post or Get
		String methodType = request.getHeader("Access-Control-Request-Method");
		if (methodType != null
				&& (methodType.trim().equals("POST") || methodType.trim()
						.equals("GET"))) {
			response = new DefaultHttpResponse(HTTP_1_1, OK);
			response.addHeader("Connection", "Keep-Alive");
		} else {
			response = new DefaultHttpResponse(HTTP_1_1, FORBIDDEN);
			// We don't want to keep the connection now
			keepAlive = false;
		}

		ArrayList<String> allowHeaders = new ArrayList<String>(2);
		allowHeaders.add("Content-Type");
		allowHeaders.add("Authorization");

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
		response.setHeader(CONTENT_TYPE, "application/json");
		response.setHeader("Content-Length", "0");

		response.setHeader("Access-Control-Allow-Headers", allowHeaders);

		ChannelFuture future = channel.write(response);
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}

	}

	/**
	 * Authenticates a Request using HTTP Basic Authentication returns true if
	 * authorized false otherwise
	 * 
	 * @param request2
	 * @return
	 * @throws SQLException 
	 */
	private boolean auth(HttpRequest myReq) throws SQLException {
		// Why between heaven and earth does Java have AES Encryption in
		// the standard library but not Base64 though it has it inetrnally
		// several times
		String userandpw = myReq.getHeader("Authorization");
		if (userandpw == null) {
			return false;
		}

		ChannelBuffer encodeddata;
		ChannelBuffer data;
		boolean result = false;
		try {
			// Base64 is always ASCII
			encodeddata = ChannelBuffers.wrappedBuffer(userandpw.substring(
					userandpw.lastIndexOf(' ')).getBytes("US-ASCII"));

			data = Base64.decode(encodeddata);
			// The string itself is utf-8
			userandpw = new String(data.array(), "UTF-8");
			// TODO Database
			
			UsersDBRow user = dbm.getUser(userandpw.substring(0, userandpw.indexOf(':')));
			
			if (userandpw.trim().equals("FooUser:FooPassword")) {
				result = true;
			}
			
		} catch (UnsupportedEncodingException e) {
			System.err
					.println("We can't fcking convert to ASCII this box is really broken");
		}

		return result;
	}

	/**
	 * Called when an uncaught exception occurs
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
