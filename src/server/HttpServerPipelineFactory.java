/**
 * $$\\ToureNPlaner\\$$
 */
package server;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

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
public class HttpServerPipelineFactory implements ChannelPipelineFactory {
	ComputeCore cCore;
	
    public HttpServerPipelineFactory(ComputeCore comCore) {
		cCore = comCore;
	}

	public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        // TODO Implement SSL SSLEngine
        //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        //engine.setUseClientMode(false);
        //pipeline.addLast("ssl", new SslHandler(engine));

        pipeline.addLast("decoder", new HttpRequestDecoder());
        //  TODO Might change to handling HttpChunks on our own here we have 10 MB request size limit
        pipeline.addLast("aggregator", new HttpChunkAggregator(10485760));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        // We could add compression support bei uncommenting the following line
        //pipeline.addLast("deflater", new HttpContentCompressor());
        pipeline.addLast("handler", new HttpRequestHandler(cCore));
        return pipeline;
    }
}
