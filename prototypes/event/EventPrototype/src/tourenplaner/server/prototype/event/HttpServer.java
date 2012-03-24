/**
 * $$\\ToureNPlaner\\$$
 */
package tourenplaner.server.prototype.event;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import de.tourenplaner.algorithms.BubblesortFactory;
import de.tourenplaner.algorithms.DummyFactory;
import de.tourenplaner.algorithms.KnapsackFactory;

import de.tourenplaner.computecore.AlgorithmRegistry;
import de.tourenplaner.computecore.ComputeCore;

/**
 * ToureNPlaner Event Based Prototype
 * 
 * @author Niklas Schnelle
 * @version 0.1 Prototype
 * 
 *  Initially based on: 
 * 	http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
 */
public class HttpServer {
    public static void main(String[] args) {
        // Configure the de.tourenplaner.server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory( // Change to Nio* if you want NIO
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Register Algorithms
        AlgorithmRegistry reg = AlgorithmRegistry.getInstance();
        reg.registerAlgorithm("ks", new KnapsackFactory());
        reg.registerAlgorithm("sp", new DummyFactory());
        reg.registerAlgorithm("bsort", new BubblesortFactory());
        
        // Create our ComputeCore that manages all ComputeThreads
        ComputeCore comCore = new ComputeCore(16, 32);
        
        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpServerPipelineFactory(comCore));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(8081));
    }
}
