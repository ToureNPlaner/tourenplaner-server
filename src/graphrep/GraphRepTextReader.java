package graphrep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GraphRepTextReader extends GraphRepFactory {

	// new fileformat is probably:
	// nodecount
	// edgecount
	// (nodecount *) ID lat lon height
	// (edgecount *) source dest dist mult

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

		System.out.println("Reading " + nodeCount + " nodes and " + edgeCount
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
		int src, dest, dist;
		int shortcuttedEdge1, shortcuttedEdge2;
		for (int i = 0; i < edgeCount; i++) {

			splittedLine = inb.readLine().split(" ");

			src = Integer.parseInt(splittedLine[0]);
			dest = Integer.parseInt(splittedLine[1]);
			dist = Integer.parseInt(splittedLine[2]);

			graphRep.setEdgeData(i, src, dest, dist);

			if (splittedLine.length == 5) {
				shortcuttedEdge1 = Integer.parseInt(splittedLine[3]);
				shortcuttedEdge2 = Integer.parseInt(splittedLine[4]);

				graphRep.setShortcutData(i, shortcuttedEdge1, shortcuttedEdge2);
			}

		}
		in.close();
		System.out.println("Start generating offsets");
		graphRep.generateOffsets();
		System.out.println("successfully created offset of InEdges");

		// choose the NNSearcher here
		// DumbNN uses linear search and is slow.
		// HashNN should be faster but needs more RAM
		graphRep.searcher = new HashNN(graphRep);
		System.gc();
		System.out.println("... success!");
		return graphRep;
	}
}