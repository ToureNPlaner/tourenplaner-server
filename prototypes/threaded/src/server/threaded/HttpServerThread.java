/**
 * $$\\ToureNPlaner\\$$
 */

package de.tourenplaner.server.threaded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;

/**
 * @author Christoph Haag, Peter Vollmer
 *
 */
public class HttpServerThread extends Thread {
	private final HttpService httpservice;
	private final HttpServerConnection conn;
	PrintWriter out = null;
	BufferedReader in = null;
	String inputLine;
	final int cnt;

	public HttpServerThread(HttpServerConnection conn, int count,
			HttpService httpService) {
		super();

		this.cnt = count;

		this.httpservice = httpService;
		this.conn = conn;

		// DEBUG
		LoggerStub.debugMsg(cnt + ": Client connected: " + conn.toString());
	}

	@Override
	public void run() {
		HttpContext context = new BasicHttpContext(null);
		try {
			while (!Thread.interrupted() && this.conn.isOpen()) {
				LoggerStub.debugMsg(cnt + ": Handle request for query");
				this.httpservice.handleRequest(this.conn, context);
			}
		} catch (ConnectionClosedException ex) {
			LoggerStub.errorLog(cnt + ": Client closed connection");
		} catch (IOException ex) {
			LoggerStub.errorLog(cnt + ": I/O error: " + ex.getMessage());
		} catch (HttpException ex) {
			LoggerStub.errorLog(cnt
					+ ": Unrecoverable HTTP protocol violation: "
					+ ex.getMessage());
		} finally {
			try {
				this.conn.shutdown();
			} catch (IOException ignore) {
			}
		}
	}
}
