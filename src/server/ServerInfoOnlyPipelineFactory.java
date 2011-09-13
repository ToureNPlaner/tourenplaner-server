/**
 * $$\\ToureNPlaner\\$$
 */
package server;

import static org.jboss.netty.channel.Channels.pipeline;

import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;

import computecore.AlgorithmRegistry;
import computecore.ComputeCore;

/**
 * ToureNPlaner Event Based Prototype
 * 
 * @author Niklas Schnelle, Peter Vollmer
 * @version 0.1 Prototype
 * 
 * Initially based on: 
 * 	http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
 */
public class ServerInfoOnlyPipelineFactory implements ChannelPipelineFactory {
	private Map<String, Object> info;
	
	
    public ServerInfoOnlyPipelineFactory(Map<String, Object> sInfo) {
		info = sInfo;
	}

	public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();



        pipeline.addLast("decoder", new HttpRequestDecoder());

        pipeline.addLast("encoder", new HttpResponseEncoder());
        // We could add compression support by uncommenting the following line
        //pipeline.addLast("deflater", new HttpContentCompressor());
        pipeline.addLast("handler", new ServerInfoHandler(info));
        return pipeline;
    }
}
