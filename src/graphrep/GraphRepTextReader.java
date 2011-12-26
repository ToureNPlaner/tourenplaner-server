package graphrep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class GraphRepTextReader extends GraphRepFactory {
    private static Logger log = Logger.getLogger("tourenplaner");

	// new fileformat is probably:
	// nodecount
	// edgecount
	// (nodecount *) ID lat lon height
	// (edgecount *) source trgt dist mult

	@Override
	public GraphRep createGraphRep(InputStream in) throws IOException {

		BufferedReader inb = new BufferedReader(new InputStreamReader(in));

		String line;

		// exception should happen when file format is wrong
		line = inb.readLine();
		int edgeCount;
		int nodeCount;

		while (line != null && line.trim().startsWith("#")) {
			line = inb.readLine();
		}
		if (line != null) {
			nodeCount = Integer.parseInt(line);
		} else {
			nodeCount = 0;
		}

		line = inb.readLine();
		if (line != null) {
			edgeCount = Integer.parseInt(line);
		} else {
			edgeCount = 0;
		}

		GraphRep graphRep = new GraphRep(nodeCount, edgeCount);

		// used for splitted lines in 1. nodes 2. edges
		String[] splittedLine;

		log.info("Reading " + nodeCount + " nodes and " + edgeCount
				+ " edges ...");
		int lat, lon;
		int height;

		for (int i = 0; i < nodeCount; i++) {
			splittedLine = inb.readLine().split(" ");
			lat = Integer.parseInt(splittedLine[1]);
			lon = Integer.parseInt(splittedLine[2]);
			height = Integer.parseInt(splittedLine[3]);
			graphRep.setNodeData(i, lat, lon, height);

			if (splittedLine.length == 5) {
				graphRep.setNodeRank(i, Integer.parseInt(splittedLine[4]));
			}

		}

		// temporary values for edges
		int src, dest, dist, euclidianDist;
		int shortcuttedEdge1, shortcuttedEdge2;
		for (int i = 0; i < edgeCount; i++) {

			splittedLine = inb.readLine().split(" ");

			src = Integer.parseInt(splittedLine[0]);
			dest = Integer.parseInt(splittedLine[1]);
			dist = Integer.parseInt(splittedLine[2]);
         euclidianDist = Integer.parseInt(splittedLine[3]);

			graphRep.setEdgeData(i, src, dest, dist, euclidianDist);

			if (splittedLine.length == 6) {
				shortcuttedEdge1 = Integer.parseInt(splittedLine[4]);
				shortcuttedEdge2 = Integer.parseInt(splittedLine[5]);

				graphRep.setShortcutData(i, shortcuttedEdge1, shortcuttedEdge2);
			}

		}
		in.close();
		log.info("Start generating offsets");
		graphRep.generateOffsets();
		log.info("successfully created offset of InEdges");

		// choose the NNSearcher here
		// DumbNN uses linear search and is slow.
		// HashNN should be faster but needs more RAM
        log.info("Start creating NNSearcher");
		graphRep.searcher = new HashNN(graphRep);
		System.gc();
		log.info("Graph loaded");
		return graphRep;
	}
}
