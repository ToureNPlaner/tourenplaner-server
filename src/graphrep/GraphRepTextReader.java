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
		int src, dest, dist, multipliedDist, shortedID;
		short outEdgeSourceNum, outEdgeShortedNum;
		for (int i = 0; i < edgeCount; i++) {

			splittedLine = inb.readLine().split(" ");

			src = Integer.parseInt(splittedLine[0]);
			dest = Integer.parseInt(splittedLine[1]);
			dist = Integer.parseInt(splittedLine[2]);

			// make mult better usable for us: save the distances with
			// multipliers applied directly
			// TODO find out what exactly is used by CH generator
			multipliedDist = (int) (1.3 / Double.parseDouble(splittedLine[3]) * dist);

			graphRep.setEdgeData(i, src, dest, dist, multipliedDist);

			if (splittedLine.length == 7) {
				shortedID = Integer.parseInt(splittedLine[4]);
				outEdgeSourceNum = Short.parseShort(splittedLine[5]);
				outEdgeShortedNum = Short.parseShort(splittedLine[6]);
				graphRep.setShortcutData(i, shortedID, outEdgeSourceNum,
						outEdgeShortedNum);
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