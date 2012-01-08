/**
 * $$\\ToureNPlaner\\$$
 */
package server;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import computecore.AlgorithmRegistry;
import computecore.ComputeCore;

import config.ConfigManager;

// TODO: maybe move more of this to the main TourenPlaner

/**
 * ToureNPlaner Event Based Server
 * 
 * This is the main class used to start the server and construct utility objects
 * like the GraphRep. and ConfigManager and ComputeCore
 * 
 * @author Niklas Schnelle
 * @version 0.1 Prototype
 * 
 *          Initially based on:
 *          http://docs.jboss.org/netty/3.2/xref/org/jboss/netty
 *          /example/http/snoop/package-summary.html
 */
public class HttpServer {

	public HttpServer(ConfigManager cm,
			AlgorithmRegistry reg, Map<String, Object> serverInfo,
			ComputeCore comCore) {
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
