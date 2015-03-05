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

package de.tourenplaner.computeserver;

import de.tourenplaner.computecore.ComputeCore;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsHandler;

import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.codec.http.HttpHeaders.Names;


import java.util.Map;


/**
 * This class is used to initialize a server and create it's pipeline
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ComputeServerInitializer extends ChannelInitializer<SocketChannel> {
	private final ComputeCore cCore;

	private final Map<String, Object> serverInfo;

	public ComputeServerInitializer(ComputeCore comCore, Map<String, Object> serverInfo) {
		this.cCore = comCore;
		this.serverInfo = serverInfo;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		CorsConfig corsConfig = CorsConfig
				.withAnyOrigin()
				.allowNullOrigin()
				.allowedRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS)
				.allowedRequestHeaders("Content-Type")
				.preflightResponseHeader(Names.CONTENT_TYPE, "application/json")
				.build();
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpObjectAggregator(10485760));
		pipeline.addLast("chunkedwirter", new ChunkedWriteHandler());
		pipeline.addLast("corshandler", new CorsHandler(corsConfig));
		pipeline.addLast(new MasterHandler(cCore, serverInfo));
	}
}
