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
import de.tourenplaner.config.ConfigManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Map;

// TODO: maybe move more of this to the main TourenPlaner

/**
 * ToureNPlaner Event Based Server
 * <p/>
 * This is the main class used to start the server and construct utility objects
 * like the GraphRep. and ConfigManager and ComputeCore
 * <p>
 * Initially based on:
 * http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
 * </p>
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class HttpComputeServer {

    public HttpComputeServer(ConfigManager cm, Map<String, Object> serverInfo, ComputeCore comCore) throws InterruptedException {

	    // Configure the server.
	    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	    EventLoopGroup workerGroup = new NioEventLoopGroup();
	    try {
		    ServerBootstrap b = new ServerBootstrap();
		    b.option(ChannelOption.SO_BACKLOG, 1024);
		    b.group(bossGroup, workerGroup)
				    .channel(NioServerSocketChannel.class)
				    .handler(new LoggingHandler(LogLevel.INFO))
				    .childHandler(new ComputeServerInitializer(comCore, serverInfo));

		    Channel ch = b.bind(cm.getEntryInt("httpport", 8080)).sync().channel();
		    ch.closeFuture().sync();
	    } finally {
		    bossGroup.shutdownGracefully();
		    workerGroup.shutdownGracefully();
	    }
    }
}
