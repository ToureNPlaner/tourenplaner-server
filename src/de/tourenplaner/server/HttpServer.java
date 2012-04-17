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

import de.tourenplaner.computecore.ComputeCore;
import de.tourenplaner.config.ConfigManager;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

// TODO: maybe move more of this to the main TourenPlaner

/**
 * ToureNPlaner Event Based Server
 * 
 * This is the main class used to start the server and construct utility objects
 * like the GraphRep. and ConfigManager and ComputeCore
 * <p>
 *          Initially based on:
 *          http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
 * </p>
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class HttpServer {

	public HttpServer(
            ConfigManager cm, Map<String, Object> serverInfo, ComputeCore comCore
                     ) {
		// Configure the server.

		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory( // Change to Oio* if you want
													// OIO
						Executors.newCachedThreadPool(), Executors
								.newCachedThreadPool()));

		if (cm.getEntryBool("private", false)) {
			// The Bootstrap handling info only
			ServerBootstrap infoBootstrap = new ServerBootstrap(
					new NioServerSocketChannelFactory( // Change to Oio* if you
														// want
														// OIO
							Executors.newCachedThreadPool(), Executors
									.newCachedThreadPool()));

			// Set up the event pipeline factory with ssl
			bootstrap.setPipelineFactory(new ServerPipelineFactory(comCore, serverInfo));

			infoBootstrap.setPipelineFactory(new ServerInfoOnlyPipelineFactory(serverInfo));
			// Bind and start to accept incoming connections.
			bootstrap.bind(new InetSocketAddress(cm
					.getEntryInt("sslport", 8081)));
			infoBootstrap.bind(new InetSocketAddress(cm.getEntryInt("httpport",
					8080)));
		} else {
			// Set up the event pipeline factory without ssl
			bootstrap.setPipelineFactory(new ServerPipelineFactory(comCore, serverInfo));
			// Bind and start to accept incoming connections.
			bootstrap.bind(new InetSocketAddress(cm.getEntryInt("httpport",
					8080)));
		}
	}
}
