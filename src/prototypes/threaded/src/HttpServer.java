package tourenplaner.server.prototype.thread;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer {

	// main socket
	public static ServerSocket s = null;
	public static int threadCnt = 0;

	public static void main(String[] args) {
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
				threadCnt = (threadCnt + 1) % 1000;
				new HttpServerThread(s.accept(), threadCnt).start();
			}
		} catch (IOException e) {
			System.err.println("Client connection initialization error: "
					+ e.getMessage());
		}
	}
}
