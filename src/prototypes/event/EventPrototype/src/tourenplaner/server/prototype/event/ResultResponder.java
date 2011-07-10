/**
 * ToureNPlaner
 */
package tourenplaner.server.prototype.event;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONValue;

import computecore.ComputeResult;


/**
 * This class encapsulates the translation from ComputeResult to the wire
 * 
 * @author Niklas Schnelle
 *
 */
public class ResultResponder {
	
	private Channel replyChannel;
	private boolean keepAlive;
	
	public ResultResponder(Channel replyChan, boolean keepAlive){
		this.replyChannel=replyChan;
		this.keepAlive = keepAlive;
	}
	
	public void writeResponse(ComputeResult res) {
        
		
		
		String result = JSONValue.toJSONString(res);

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.setContent(ChannelBuffers.copiedBuffer(result, CharsetUtil.UTF_8));
        response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
        

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
}
