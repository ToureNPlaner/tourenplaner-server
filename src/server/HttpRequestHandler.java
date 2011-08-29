/**
 * $$\\ToureNPlaner\\$$
 */

package server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import computecore.ComputeCore;
import computecore.ComputeRequest;

/**
 * *ToureNPlaner Event Based Prototype
 * 
 * 
 * @author Niklas Schnelle, Peter Vollmer
 * @version 0.1 Prototype
 * 
 *          Initially based on: http://docs.jboss.org/netty/3.2/xref
 *          /org/jboss/netty/example/http/snoop/package-summary.html
 */
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

	/** JSONParser we can reuse **/
	private final JSONParser parser = new JSONParser();

	/** The ComputeCore managing the threads **/
	private final ComputeCore computer;

	public HttpRequestHandler(ComputeCore cCore) {
		super();
		computer = cCore;
	}

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

		if (auth(request)) {

			ChannelBuffer content = request.getContent();
			if (content.readableBytes() != 0) {
				handleContent(request, channel, content);
			} else {
				// Respond with No Content
				HttpResponse response = new DefaultHttpResponse(HTTP_1_1,
						NO_CONTENT);
				// Send the client the realm so it knows we want Basic Access
				// Auth.
				response.setHeader("WWW-Authenticate",
						"Basic realm=\"ToureNPlaner\"");
				// Write the response.
				ChannelFuture future = e.getChannel().write(response);
				future.addListener(ChannelFutureListener.CLOSE);
			}
		} else {
			// Respond with Unauthorized Access
			HttpResponse response = new DefaultHttpResponse(HTTP_1_1,
					UNAUTHORIZED);
			// Send the client the realm so it knows we want Basic Access Auth.
			response.setHeader("WWW-Authenticate",
					"Basic realm=\"ToureNPlaner\"");
			// Write the response.
			ChannelFuture future = e.getChannel().write(response);
			future.addListener(ChannelFutureListener.CLOSE);
		}
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

		// We only allow POST methods so only allow request when Method is Post
		String methodType = request.getHeader("Access-Control-Request-Method");
		if (methodType != null && methodType.trim().equals("POST")) {
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
		response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
		response.setHeader(CONTENT_TYPE, "application/json");
		response.setHeader("Content-Length", "0");

		response.setHeader("Access-Control-Allow-Headers", allowHeaders);

		ChannelFuture future = channel.write(response);
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}

	}

	/**
	 * Deals with handling the Content (json) and adds requests to the queue
	 * 
	 * @param request
	 * @param channel
	 * @param content
	 * @throws IOException
	 * @throws ParseException
	 */
	private void handleContent(HttpRequest request, Channel channel,
			ChannelBuffer content) throws IOException, ParseException {
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
				request.getUri());
		// Map<String, List<String>> params =
		// queryStringDecoder.getParameters();

		InputStreamReader inReader = new InputStreamReader(
				new ChannelBufferInputStream(content));

		JSONObject requestJSON = (JSONObject) parser.parse(inReader);
		// System.out.println(requestJSON);
		@SuppressWarnings("unchecked")
		Map<String, Object> objmap = requestJSON;
		String algName = queryStringDecoder.getPath().substring(1);
		ResultResponder responder = new ResultResponder(channel,
				isKeepAlive(request));

		// Create ComputeRequest and commit to workqueue
		ComputeRequest req = new ComputeRequest(responder, algName, objmap);
		boolean sucess = computer.submit(req);

		if (!sucess) {
			responder.writeServerOverloaded();
		}
	}

	/**
	 * Authenticates a Request using HTTP Basic Authentication returns true if
	 * authorized false otherwise
	 * 
	 * @param request2
	 * @return
	 */
	private boolean auth(HttpRequest myReq) {
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
			if (userandpw.trim().equals("FooUser:FooPassword")) {
				result = true;
			}
			;

		} catch (UnsupportedEncodingException e) {
			System.err
					.println("We can't fcking convert to ASCII this box is really broken");
		}

		return result;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
