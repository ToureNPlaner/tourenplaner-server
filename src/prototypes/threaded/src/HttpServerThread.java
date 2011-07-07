package tourenplaner.server.prototype.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HttpServerThread extends Thread {
	Socket socket;
	PrintWriter out = null;
	BufferedReader in = null;
	String inputLine;
	final int cnt;

	void printdebug(String s) {
		System.out.println(cnt + ": " + s);
	}

	void errlog(String s) {
		System.err.println(cnt + ": " + s);
	}

	public HttpServerThread(Socket s, int count) {
		this.socket = s;
		// DEBUG
		this.cnt = count;
		printdebug("Client connected: " + s.getInetAddress());
	}

	@Override
	public void run() {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);

			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (IOException e) {
			// TODO: log
			errlog("Stream initializiation error: " + e.getMessage());
		}

		try {
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.trim().toLowerCase().equals("quit")) {
					printdebug("Client " + socket.getInetAddress()
							+ " wants to close the connection. We obey.");
					socket.close();
					Thread.currentThread().interrupt();
					return;
				}
				printdebug("Client writes: " + inputLine);
				// we write back:
				out.println("Your input had length: " + inputLine.length());
				out.flush();
			}
		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {
				errlog("error while closing socket in error state: ("
						+ e.getMessage() + "): " + e1.getMessage());
			}
		}
	}
}
