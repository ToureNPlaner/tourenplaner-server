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
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import computecore.AlgorithmRegistry;
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
public class ServerInfoHandler extends SimpleChannelUpstreamHandler {

	/** JSONParser we can reuse **/
	private final JSONParser parser = new JSONParser();

	private Map<String, Object> info;

	public ServerInfoHandler(Map<String, Object> sInfo) {
		super();
		info = sInfo;
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
		
		
		Responder responder = new Responder(channel, false);

		// Get the Requeststring e.g. /info
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
				request.getUri());
		
		String path = queryStringDecoder.getPath();
		
		if(path.equals("/info")){
			
			responder.writeJSON(info, HttpResponseStatus.OK);
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

		// We only allow GET methods so only allow request when Method is Post
		String methodType = request.getHeader("Access-Control-Request-Method");
		if (methodType != null && methodType.trim().equals("GET")) {
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
		response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
		response.setHeader(CONTENT_TYPE, "application/json");
		response.setHeader("Content-Length", "0");

		response.setHeader("Access-Control-Allow-Headers", allowHeaders);

		ChannelFuture future = channel.write(response);
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}

	}





	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}
