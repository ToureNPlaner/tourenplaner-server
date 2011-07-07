import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

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

public class HttpServer {

	// main socket
	private static ServerSocket s = null;
	public static int threadCnt = 0;

	private static HttpParams params;
	private static HttpService httpService;

	public static void main(String[] args) {

		// ---------------------------------------------------------------------
		// http setup part
		// ---------------------------------------------------------------------
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
		// TODO: docroot file servicing brauchen wir nicht
		// reqistry.register("*", new FileHandlerExample.HttpFileHandler("."));
		// stattdessen ein eigener handler
		// reqistry.register("regex for authentication", new
		// AuthenticateHandler());
		reqistry.register("*", new TourenPlanerRequestHandler());

		// Set up the HTTP service
		httpService = new HttpService(httpproc,
				new DefaultConnectionReuseStrategy(),
				new DefaultHttpResponseFactory(), reqistry, params);

		// ---------------------------------------------------------------------
		// server part
		// ---------------------------------------------------------------------
		try {
			s = new ServerSocket(8081);
		} catch (IOException e) {
			// TODO: log
			System.err.println("ServerSocket initialization error: "
					+ e.getMessage());
			// server cannot start
			System.exit(1);
		}

		try {
			while (!Thread.currentThread().isInterrupted()) {

				Socket newSocket = s.accept();
				DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
				System.out.println("Incoming connection from "
						+ s.getInetAddress());
				conn.bind(newSocket, params);

				Thread clientThread = new HttpServerThread(conn, threadCnt,
						httpService);
				clientThread.setDaemon(true);
				clientThread.start();
				threadCnt = (threadCnt + 1) % 1000;

			}
		} catch (InterruptedIOException ex) {
			System.out.println("Shutting down...");
		} catch (IOException e) {
			System.err.println("Client connection initialization error: "
					+ e.getMessage());
		}
	}
}
