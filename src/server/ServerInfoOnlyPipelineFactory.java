/**
 * $$\\ToureNPlaner\\$$
 */
package server;

import static org.jboss.netty.channel.Channels.pipeline;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

/**
 * This class is used to construct a Pipeline that only handles "/info" requests
 * as required for handling these requests on a non SSL Socket even for private
 * servers
 * 
 * @author Niklas Schnelle, Peter Vollmer
 * @version 0.1 Prototype
 * 
 *          Initially based on:
 *          http://docs.jboss.org/netty/3.2/xref/org/jboss/netty
 *          /example/http/snoop/package-summary.html
 */
public class ServerInfoOnlyPipelineFactory implements ChannelPipelineFactory {
	private final Map<String, Object> info;

	public ServerInfoOnlyPipelineFactory(Map<String, Object> sInfo) {
		this.info = sInfo;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();

		pipeline.addLast("decoder", new HttpRequestDecoder());

		pipeline.addLast("encoder", new HttpResponseEncoder());
		// We could add compression support by uncommenting the following line
		// pipeline.addLast("deflater", new HttpContentCompressor());
		pipeline.addLast("handler", new ServerInfoHandler(info));
		return pipeline;
	}
}
