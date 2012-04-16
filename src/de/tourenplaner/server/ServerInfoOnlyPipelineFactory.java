/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.server;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import java.util.Map;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * This class is used to construct a Pipeline that only handles "/info" requests
 * as required for handling these requests on a non SSL Socket even for private
 * servers
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
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
