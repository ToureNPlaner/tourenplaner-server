/**
 * ToureNPlaner Event Based Prototype
 * 
 * @author Niklas Schnelle
 * 
 * Initially based on: 
 * 	http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
 */
package tourenplaner.server.prototype.event;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;


import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.base64.Base64Decoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import computecore.ComputeCore;
import computecore.ComputeRequest;

/**
 * @author Niklas Schnelle
 * @version 0.1 Prototype
 */
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

    private HttpRequest request;
    /** Buffer that stores the response content */
    private final StringBuilder buf = new StringBuilder();
    /** JSONParser we can reuse **/
    private final JSONParser parser = new JSONParser();
    
    /** The ComputeCore managing the threads**/
    private ComputeCore computer;
    
    public HttpRequestHandler(ComputeCore cCore){
    	super();
    	computer = cCore;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        
        request = (HttpRequest) e.getMessage();
        //System.out.print(request.toString());
        // Handle preflighted requests so wee need to work with OPTION Requests
        if(request.getMethod().equals(HttpMethod.OPTIONS)){
        	boolean keepAlive = isKeepAlive(request);
        	HttpResponse response;
        	
        	// We only allow POST methods so only allow request when Method is Post
        	String  methodType = request.getHeader("Access-Control-Request-Method");
        	if(methodType != null  && methodType.trim().equals("POST")){
        		response = new DefaultHttpResponse(HTTP_1_1, OK);
        		response.addHeader("Connection", "Keep-Alive");        		
        	} else {
        		response = new DefaultHttpResponse(HTTP_1_1,FORBIDDEN);
        		// We don't want to keep the connection now
                keepAlive = false;
        	}

        	response.setHeader("Access-Control-Allow-Origin", "*");
    		response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
    		response.setHeader(CONTENT_TYPE, "plain/text");
    		response.setHeader("Content-Length","0");
        	response.setHeader("Access-Control-Allow-Headers","Content-Type");
    		
    		ChannelFuture future = e.getChannel().write(response);
            if(!keepAlive){
            	future.addListener(ChannelFutureListener.CLOSE);
            }
            return;
        }
        
        if(auth(request)){
        
			        
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
			//Map<String, List<String>> params = queryStringDecoder.getParameters();
			   
			   
			ChannelBuffer content = request.getContent();
			InputStreamReader inReader = new InputStreamReader(new ChannelBufferInputStream(content));
			
			
			JSONObject requestJSON = (JSONObject) parser.parse(inReader);
			//System.out.println(requestJSON);
			@SuppressWarnings("unchecked")
			Map<String, Object> objmap = requestJSON;
			String algName = queryStringDecoder.getPath().substring(1);
			ResultResponder responder = new ResultResponder(e.getChannel(), isKeepAlive(request));
			
			// Create ComputeRequest and commit to workqueue
			ComputeRequest req = new ComputeRequest(responder, algName, objmap);
			boolean sucess = computer.submit(req);
			
			if(!sucess){
				responder.writeServerOverloaded();
	        }
        } else {
        	// Respond with Unauthorized Access
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, UNAUTHORIZED);
            // Send the client the realm so it knows we want Basic Access Auth.
            response.setHeader("WWW-Authenticate", "Basic realm=\"ToureNPlaner\"");
            // Write the response.
            ChannelFuture future = e.getChannel().write(response);
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
    
/**
 * Authenticates a Request using HTTP Basic Authentication returns true if authorized false otherwise
 * 
 * @param request2
 * @return
 */
private boolean auth(HttpRequest myReq) {
		// Why between heaven and earth does Java have AES Encryption in
		// the standard library but not Base64 though it has it inetrnally several times
		String userandpw = myReq.getHeader("Authorization");
		if(userandpw == null){
			return false;
		}
				
		ChannelBuffer encodeddata;
		ChannelBuffer data;
		boolean result = false;
		try {
			// Base64 is always ASCII
			encodeddata = ChannelBuffers.wrappedBuffer(userandpw.substring(userandpw.lastIndexOf(' ')).getBytes("US-ASCII"));

			data = Base64.decode(encodeddata);
			// The string itself is utf-8 
			userandpw = new String(data.array(), "UTF-8");
			if(userandpw.trim().equals("FooUser:FooPassword")){
				result = true;
			};
			
		} catch (UnsupportedEncodingException e) {
			System.err.println("We can't fcking convert to ASCII this box is really broken");
		}
		
		return result;
	}

/*    *//**
     * Authenticats the request in the ChannelBuffer content with the parameters given in params
     * see: @link https://gerbera.informatik.uni-stuttgart.de/projects/server/wiki/Authentifizierung
     * for a detailed explanation
     * 
     * @param params
     * @param content
     * @return
     *//*
    private boolean auth(Map<String, List<String>> params, ChannelBuffer content) {
		boolean authentic = false;
		if(!params.isEmpty()){
			List<String> username;
			List<String> signature;
			String realSecret;
			byte[] bodyhash, finalhash;
			
			
			username = params.get("tp-user");
			signature = params.get("tp-signature");
			if(username != null && username.size() == 1 && signature != null && signature.size() == 1) {
				//System.out.println("User: "+username.get(0));
				//System.out.println("Signature: "+signature.get(0));
				// TODO replace static test with database access
				if(username.get(0).equals("FooUser")){
					realSecret = "FooPassword";
					// Hash the content
					digester.reset();
					
					digester.update(content.array(), 0, content.readableBytes());
					
					bodyhash = digester.digest();
					// Contenthash to String and then hash for SHA1(BODYHASH:SECRET)
					StringBuilder sb = new StringBuilder();
					*//** @link http://www.spiration.co.uk/post/1199/Java-md5-example-with-MessageDigest **//*
					for (byte element : bodyhash) {
						sb.append(Character.forDigit((element >> 4) & 0xf, 16));
						sb.append(Character.forDigit(element & 0xf, 16));
					}
					// DEBUG
					System.out.println("Bodyhash: "+sb.toString());
					sb.append(':');
					try {
						sb.append(realSecret);
						// Now sb is BODYHASH:SECRET do the final hashing and hex conversion
						digester.reset();
						digester.update(sb.toString().getBytes("UTF-8"));
						finalhash = digester.digest();
						// Reset the string builder so we can reuse it
						//System.out.println("Before Hashing: "+sb.toString());
						sb.delete(0, sb.length());
						for (byte element : finalhash) {
							sb.append(Character.forDigit((element >> 4) & 0xf, 16));
							sb.append(Character.forDigit(element & 0xf, 16));
						}
						//System.out.println("MyHash: "+sb.toString());
						authentic = (sb.toString().equals(signature.get(0)))? true: false;
					} catch (UnsupportedEncodingException e) {
						System.err.println("We can't fcking find UTF-8 Charset hell wtf?");
					}
					
				}
				
				
			}
			
		}
		return authentic; 
	}*/

	private void writeResponse(MessageEvent e) {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(request);

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
        response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
        }

        // Write the response.
        ChannelFuture future = e.getChannel().write(response);

        // Close the non-keep-alive connection after the write operation is done.
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
