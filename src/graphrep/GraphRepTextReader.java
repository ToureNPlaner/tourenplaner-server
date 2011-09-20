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

	private static class Sorter {
		private static void Sort(GraphRep graphRep) {

			Heapify(graphRep);
			int endI = graphRep.dest_in.length - 1;
			while (endI > 0) {
				swap(graphRep, endI, 0);
				siftDown(graphRep, 0, endI - 1);
				endI--;
			}
		}

		private static void swap(GraphRep graphRep, int i, int j) {
			// save to temp
			int tempDest = graphRep.dest_in[i];
			int tempDist = graphRep.dist_in[i];
			int tempSrc = graphRep.source_in[i];
			int tempMult = graphRep.mult_in[i];

			// change place
			graphRep.dest_in[i] = graphRep.dest_in[j];
			graphRep.dist_in[i] = graphRep.dist_in[j];
			graphRep.source_in[i] = graphRep.source_in[j];
			graphRep.mult_in[i] = graphRep.mult_in[j];

			// write back to the new place
			graphRep.dest_in[j] = tempDest;
			graphRep.dist_in[j] = tempDist;
			graphRep.source_in[j] = tempSrc;
			graphRep.mult_in[j] = tempMult;
		}

		private static boolean less(GraphRep graphRep, int i, int j) {
			return graphRep.dest_in[i] < graphRep.dest_in[j];
		}

		private static void Heapify(GraphRep graphRep) {
			int pos = graphRep.dest_in.length - 1;
			while (pos >= 0) {
				siftDown(graphRep, pos, graphRep.dest_in.length - 1);
				pos--;
			}
		}

		private static void siftDown(GraphRep graphRep, int topI, int endI) {
			int cLI;
			int cMI;
			int cRI;
			int cMaxI;

			while (((topI * 3) + 1) <= endI) {
				cLI = (topI * 3) + 1;
				cMI = cLI + 1;
				cRI = cLI + 2;
				cMaxI = topI;

				if (less(graphRep, cMaxI, cLI)) {
					cMaxI = cLI;
				}
				if ((cMI <= endI) && less(graphRep, cMaxI, cMI)) {
					cMaxI = cMI;
				}
				if ((cRI <= endI) && less(graphRep, cMaxI, cRI)) {
					cMaxI = cRI;
				}
				if (cMaxI != topI) {
					swap(graphRep, cMaxI, topI);
					topI = cMaxI;
				} else {
					return;
				}
			}
		}
	}

	@Override
	public GraphRep createGraphRep(InputStream in) throws IOException {
		GraphRep graphRep = new GraphRep();

		BufferedReader inb = new BufferedReader(new InputStreamReader(in));

		String line;

		// exception should happen when file format is wrong
		line = inb.readLine();

		while ((line != null) && line.trim().startsWith("#")) {
			line = inb.readLine();
		}
		if (line != null) {
			graphRep.nodeCount = Integer.parseInt(line);
		} else {
			graphRep.nodeCount = 0;
		}

		line = inb.readLine();
		if (line != null) {
			graphRep.edgeCount = Integer.parseInt(line);
		} else {
			graphRep.edgeCount = 0;
		}

		graphRep.lat = new float[graphRep.nodeCount];
		graphRep.lon = new float[graphRep.nodeCount];
		graphRep.height = new int[graphRep.nodeCount];

		graphRep.offsetOut = new int[graphRep.nodeCount + 1];

		graphRep.source_out = new int[graphRep.edgeCount];
		graphRep.dest_out = new int[graphRep.edgeCount];
		graphRep.mult_out = new int[graphRep.edgeCount];
		// TODO graphRep.elev_out = new float[edgeCount];
		graphRep.dist_out = new int[graphRep.edgeCount];

		graphRep.source_in = new int[graphRep.edgeCount];
		graphRep.dest_in = new int[graphRep.edgeCount];
		graphRep.mult_in = new int[graphRep.edgeCount];
		// TODO graphRep.elev_in = new float[edgeCount];
		graphRep.dist_in = new int[graphRep.edgeCount];

		graphRep.offsetIn = new int[graphRep.nodeCount + 1];

		// used for splitted lines in 1. nodes 2. edges
		String[] splittedLine;

		System.out.println("Reading " + graphRep.nodeCount + " nodes and "
				+ graphRep.edgeCount + " edges ...");
		for (int i = 0; i < graphRep.nodeCount; i++) {
			splittedLine = inb.readLine().split(" ");
			graphRep.lat[i] = Float.parseFloat(splittedLine[1]);
			graphRep.lon[i] = Float.parseFloat(splittedLine[2]);
			graphRep.height[i] = Integer.parseInt(splittedLine[3]);
		}

		int currentSource;
		int prevSource = -1;
		for (int i = 0; i < graphRep.edgeCount; i++) {
			splittedLine = inb.readLine().split(" ");
			currentSource = Integer.parseInt(splittedLine[0]);
			graphRep.source_out[i] = currentSource;
			graphRep.source_in[i] = currentSource;

			graphRep.dest_out[i] = Integer.parseInt(splittedLine[1]);
			graphRep.dest_in[i] = graphRep.dest_out[i];

			graphRep.dist_out[i] = Integer.parseInt(splittedLine[2]);
			graphRep.dist_in[i] = graphRep.dist_out[i];

			// make mult better usable for us: save the distances with
			// multipliers applied directly
			graphRep.mult_out[i] = Math.round((1.3F / Float
					.parseFloat(splittedLine[3])) * graphRep.dist_out[i]);
			graphRep.mult_in[i] = graphRep.mult_out[i];

			// TODO graphRep.elev_out[i] = Float.parseFloat(splittedLine[4]);
			// TODO graphRep.elev_in[i] = graphRep.elev_out[i];

			if (currentSource != prevSource) {
				for (int j = currentSource; j > prevSource; j--) {
					graphRep.offsetOut[j] = i;
				}
				prevSource = currentSource;
			}
		}
		in.close();

		graphRep.offsetOut[graphRep.nodeCount] = graphRep.edgeCount;
		// assuming we have at least one edge
		for (int cnt = graphRep.nodeCount - 1; graphRep.offsetOut[cnt] == 0; cnt--) {
			graphRep.offsetOut[cnt] = graphRep.offsetOut[cnt + 1];
		}

		Sorter.Sort(graphRep);

		int currentDest;
		int prevDest = -1;
		for (int i = 0; i < graphRep.edgeCount; i++) {
			currentDest = graphRep.dest_in[i];
			if (currentDest != prevDest) {
				for (int j = currentDest; j > prevDest; j--) {
					graphRep.offsetIn[j] = i;
				}
				prevDest = currentDest;
			}
		}

		graphRep.offsetIn[graphRep.nodeCount] = graphRep.edgeCount;
		// assuming we have at least one edge
		for (int cnt = graphRep.nodeCount - 1; graphRep.offsetIn[cnt] == 0; cnt--) {
			graphRep.offsetIn[cnt] = graphRep.offsetIn[cnt + 1];
		}
		System.out.println("successfully created offset of InEdges");

		// choose the NNSearcher here
		// DumbNN uses linear search and is slow.
		// KDTreeNN should be faster
		graphRep.searcher = new DumbNN(graphRep);
		System.out.println("... success!");
		return graphRep;
	}
}