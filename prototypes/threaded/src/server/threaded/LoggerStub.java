package server.threaded;

public class LoggerStub {
	public static boolean debugmsg = false;
	public static boolean errmsg = false;

	static void errorLog(String msg) {
		if (errmsg) {
			System.err.println("ERR: " + msg);
		}
	}

	static void debugMsg(String msg) {
		if (debugmsg) {
			System.out.println("DEBUG: " + msg);
		}
	}

}
