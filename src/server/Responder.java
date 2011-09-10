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
 * This class encapsulates the translation from ComputeResult to the wire
 * 
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class Responder {
	
	private Channel replyChannel;
	private boolean keepAlive;
	
	public Responder(Channel replyChan, boolean keepAlive){
		this.replyChannel=replyChan;
		this.keepAlive = keepAlive;
	}
	
	public Channel getChannel(){
		return replyChannel;
	}
	
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
	
	public void writeJSON(Object toWrite, HttpResponseStatus status){
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
	
	public void writeServerOverloaded(){
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, SERVICE_UNAVAILABLE);
        // Write the response.
        ChannelFuture future =replyChannel.write(response);
        // Close the connection
        future.addListener(ChannelFutureListener.CLOSE);

	}
}
