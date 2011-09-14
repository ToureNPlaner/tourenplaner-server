/**
 * $$\\ToureNPlaner\\$$
 */
package server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONValue;

import computecore.ComputeResult;


/**
 * This class encapsulates the translation from Map<String, Object> to
 * JSON and then to the wire.
 * As well as providing utility methods for sending answers to clients 
 * 
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class Responder {
	
	private Channel replyChannel;
	private boolean keepAlive;
	
	/**
	 * Constructs a new Responder from the given Channel that either uses
	 * keepAlive HTTP connections or not based on the corresponding flag
	 * 
	 * @param replyChan
	 * @param keepAlive
	 */
	public Responder(Channel replyChan, boolean keepAlive){
		this.replyChannel=replyChan;
		this.keepAlive = keepAlive;
	}
	
	/**
	 * Gets the Channel associated with this Responder
	 * @return
	 */
	public Channel getChannel(){
		return replyChannel;
	}
	
	/**
	 * Writes a HTTP Unauthorized answer to the wire and closes the connection
	 * 
	 */
	public void writeUnauthorizedClose(){
		// Respond with Unauthorized Access
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1,
				UNAUTHORIZED);
		// Send the client the realm so it knows we want Basic Access Auth.
		response.setHeader("WWW-Authenticate",
				"Basic realm=\"ToureNPlaner\"");
		// Write the response.
		ChannelFuture future = replyChannel.write(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}
	
	/**
	 * Translates a given Map<String, Object> (must be json compatible see simple_json)
	 * to JSON and writes it onto the wire
	 * 
	 * @param toWrite
	 * @param status
	 */
	public void writeJSON(Map<String, Object> toWrite, HttpResponseStatus status){
		String result = JSONValue.toJSONString(toWrite);

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

		response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
        
        response.setContent(ChannelBuffers.copiedBuffer(result, CharsetUtil.UTF_8));

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
        }

        // Write the response.
        ChannelFuture future =replyChannel.write(response);

        // Close the non-keep-alive connection after the write operation is done.
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
	}
	
	/**
	 * Writes a server overloaded response and closes the connection
	 */
	public void writeServerOverloaded(){
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, SERVICE_UNAVAILABLE);
        // Write the response.
        ChannelFuture future =replyChannel.write(response);
        // Close the connection
        future.addListener(ChannelFutureListener.CLOSE);

	}
}
