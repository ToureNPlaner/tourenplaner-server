/**
 * $$\\ToureNPlaner\\$$
 */

package de.tourenplaner.server.threaded;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;

import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import de.tourenplaner.algorithms.BubblesortFactory;
import de.tourenplaner.algorithms.DummyFactory;
import de.tourenplaner.algorithms.KnapsackFactory;

import de.tourenplaner.computecore.AlgorithmRegistry;
import de.tourenplaner.computecore.ComputeCore;

/**
 * @author Christoph Haag, Peter Vollmer
 * 
 */
public class HttpServer {

	// main socket
	private static ServerSocket s = null;
	private static ComputeCore comCore = null;

	public static void main(String[] args) {
		LoggerStub.debugmsg = true;
		LoggerStub.errmsg = true;
		// Register Algorithms
		AlgorithmRegistry reg = AlgorithmRegistry.getInstance();
		reg.registerAlgorithm("ks", new KnapsackFactory());
		reg.registerAlgorithm("sp", new DummyFactory());
		reg.registerAlgorithm("bsort", new BubblesortFactory());

		// Create our ComputeCore that manages all ComputeThreads
		// must be after de.tourenplaner.algorithms have been registered
		comCore = new ComputeCore(4, 20);
		setuphttp();

		try {
			s = new ServerSocket(8081);
		} catch (IOException e) {
			LoggerStub.errorLog("ServerSocket initialization error: "
					+ e.getMessage());
			e.printStackTrace();
			// de.tourenplaner.server cannot start
			System.exit(1);
		}

		try {
			while (!Thread.currentThread().isInterrupted()) {

				DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
				LoggerStub.debugMsg("Incoming connection from "
						+ s.getInetAddress());
				conn.bind(s.accept(), params);

				Thread clientThread = new HttpServerThread(conn, threadCnt,
						httpService);
				clientThread.setDaemon(true);
				clientThread.start();
				threadCnt = (threadCnt + 1) % 1000;

			}
		} catch (InterruptedIOException ex) {
			System.out.println("Shutting down...");
		} catch (IOException e) {
			LoggerStub.errorLog("Client connection initialization error: "
					+ e.getMessage());
			e.printStackTrace();
		}

	}

	static void setuphttp() {
		params = new SyncBasicHttpParams();
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
				.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
						8 * 1024)
				.setBooleanParameter(
						CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
				.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
				.setParameter(CoreProtocolPNames.ORIGIN_SERVER,
						"HttpComponents/1.1");
		// Set up the HTTP protocol processor
		HttpProcessor httpproc = new ImmutableHttpProcessor(
				new HttpResponseInterceptor[] { new ResponseDate(),
						new ResponseServer(), new ResponseContent(),
						new ResponseConnControl() });
		// Set up request handlers
		HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
		// TODO: do we need other handlers?
		reqistry.register("*", new TourenPlanerRequestHandler(comCore));

		// Set up the HTTP service
		httpService = new HttpService(httpproc,
				new DefaultConnectionReuseStrategy(),
				new DefaultHttpResponseFactory(), reqistry, params);
	}

	// TODO: only debugging to see nicely what thread sends what message
	public static int threadCnt = 0;

	private static HttpParams params;
	private static HttpService httpService;
}
