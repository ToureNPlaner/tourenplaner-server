package server;

import graphrep.GraphRep;
import graphrep.GraphRepDumpReader;
import graphrep.GraphRepTextReader;

import java.io.IOException;

import config.ConfigManager;

public class TourenPlaner {

	/**
	 * This is the main class of ToureNPlaner. It passes CLI parameters to the
	 * handler and creates the httpserver
	 */
	public static void main(String[] args) {
		/**
		 * inits config manager if config file is provided; also prints usage
		 * information if necessary
		 */
		CLIHandler handler = new CLIHandler(args);

		// if serialize, then ignore whether to read text or dump and read
		// text graph since it wouldn't make sense to read a serialized
		// graph just to serialize it. Also do this before anything server
		// related gets actually started
		if (handler.serializegraph()) {
			System.out.println("Dumping Graph");
			GraphRep graph = null;
			String graphfilename = ConfigManager.getInstance().getEntryString(
					"graphfilepath",
					System.getProperty("user.home") + "/germany.txt");
			try {
				graph = new GraphRepTextReader().createGraphRep(graphfilename);
				utils.GraphSerializer.serialize(graphfilename, graph);
				System.exit(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// uses defaults if it was not initialized by the CLIHandler
		ConfigManager cm = ConfigManager.getInstance();
		// Load the Graph
		GraphRep graph = null;
		String graphfilename = cm.getEntryString("graphfilepath",
				System.getProperty("user.home") + "/germany.txt");
		try {
			if (handler.loadTextGraph()) {
				graph = new GraphRepTextReader().createGraphRep(graphfilename);
			} else {
				graph = new GraphRepDumpReader().createGraphRep(graphfilename
						+ ".dat");
			}
		} catch (IOException e) {
			e.printStackTrace();
			// TODO: server won't calculate graph algorithms without a graph,
			// but maybe it will provide some other functionality?
		}
		new HttpServer(graph, cm);
	}
}
