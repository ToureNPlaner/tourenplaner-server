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
			int endI = graphRep.dest.length - 1;
			while (endI > 0) {
				swap(graphRep, endI, 0);
				siftDown(graphRep, 0, endI - 1);
				endI--;
			}
		}

		private static void swap(GraphRep graphRep, int i, int j) {
			int temp = graphRep.mapping_InToOut[i];
			graphRep.mapping_InToOut[i] = graphRep.mapping_InToOut[j];
			graphRep.mapping_InToOut[j] = temp;
			// // save to temp
			// int tempDest = graphRep.dest_in[i];
			// int tempDist = graphRep.dist_in[i];
			// int tempSrc = graphRep.src_in[i];
			// int tempMult = graphRep.multipliedDist_in[i];
			// int tempShortedID = graphRep.shortedID_in[i];
			// int tempOutEdgeSourceNum = graphRep.outEdgeSourceNum_in[i];
			// int tempOutEdgeShortedNum = graphRep.outEdgeShortedNum_in[i];
			//
			// // change place
			// graphRep.dest_in[i] = graphRep.dest_in[j];
			// graphRep.dist_in[i] = graphRep.dist_in[j];
			// graphRep.src_in[i] = graphRep.src_in[j];
			// graphRep.multipliedDist_in[i] = graphRep.multipliedDist_in[j];
			// graphRep.shortedID_in[i] = graphRep.shortedID_in[j];
			// graphRep.outEdgeSourceNum_in[i] =
			// graphRep.outEdgeSourceNum_in[j];
			// graphRep.outEdgeShortedNum_in[i] =
			// graphRep.outEdgeShortedNum_in[j];
			//
			// // write back to the new place
			// graphRep.dest_in[j] = tempDest;
			// graphRep.dist_in[j] = tempDist;
			// graphRep.src_in[j] = tempSrc;
			// graphRep.multipliedDist_in[j] = tempMult;
			// graphRep.shortedID_in[j] = tempShortedID;
			// graphRep.outEdgeSourceNum_in[i] = tempOutEdgeSourceNum;
			// graphRep.outEdgeShortedNum_in[j] = tempOutEdgeShortedNum;
		}

		/**
		 * 
		 * @param graphRep
		 * @param i
		 * @param j
		 * @return
		 */
		private static boolean less(GraphRep graphRep, int i, int j) {
			return graphRep.dest[graphRep.mapping_InToOut[i]] < graphRep.dest[graphRep.mapping_InToOut[j]];
		}

		private static void Heapify(GraphRep graphRep) {
			int pos = graphRep.dest.length - 1;
			while (pos >= 0) {
				siftDown(graphRep, pos, graphRep.dest.length - 1);
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

		graphRep.lat = new double[graphRep.nodeCount];
		graphRep.lon = new double[graphRep.nodeCount];
		graphRep.height = new int[graphRep.nodeCount];
		graphRep.rank = new int[graphRep.nodeCount];

		graphRep.offsetOut = new int[graphRep.nodeCount + 1];
		graphRep.offsetIn = new int[graphRep.nodeCount + 1];

		graphRep.src = new int[graphRep.edgeCount];
		graphRep.dest = new int[graphRep.edgeCount];
		graphRep.multipliedDist = new int[graphRep.edgeCount];
		// TODO graphRep.elev_out = new float[edgeCount];
		graphRep.dist = new int[graphRep.edgeCount];
		graphRep.shortedID = new int[graphRep.edgeCount];
		graphRep.outEdgeSourceNum = new int[graphRep.edgeCount];
		graphRep.outEdgeShortedNum = new int[graphRep.edgeCount];

		graphRep.mapping_InToOut = new int[graphRep.edgeCount];

		// graphRep.src_in = new int[graphRep.edgeCount];
		// graphRep.dest_in = new int[graphRep.edgeCount];
		// graphRep.multipliedDist_in = new int[graphRep.edgeCount];
		// // TODO graphRep.elev_in = new float[edgeCount];
		// graphRep.dist_in = new int[graphRep.edgeCount];
		// graphRep.shortedID_in = new int[graphRep.edgeCount];
		// graphRep.outEdgeSourceNum_in = new int[graphRep.edgeCount];
		// graphRep.outEdgeShortedNum_in = new int[graphRep.edgeCount];

		// used for splitted lines in 1. nodes 2. edges
		String[] splittedLine;

		System.out.println("Reading " + graphRep.nodeCount + " nodes and "
				+ graphRep.edgeCount + " edges ...");
		for (int i = 0; i < graphRep.nodeCount; i++) {
			splittedLine = inb.readLine().split(" ");
			graphRep.lat[i] = Double.parseDouble(splittedLine[1]);
			graphRep.lon[i] = Double.parseDouble(splittedLine[2]);
			graphRep.height[i] = Integer.parseInt(splittedLine[3]);
			if (splittedLine.length == 5) {
				graphRep.rank[i] = Integer.parseInt(splittedLine[4]);
			} else {
				graphRep.rank[i] = Integer.MAX_VALUE;
			}
		}

		int currentSource;
		int prevSource = -1;
		for (int i = 0; i < graphRep.edgeCount; i++) {
			graphRep.mapping_InToOut[i] = i;
			splittedLine = inb.readLine().split(" ");
			currentSource = Integer.parseInt(splittedLine[0]);
			graphRep.src[i] = currentSource;
			// graphRep.src_in[i] = currentSource;

			graphRep.dest[i] = Integer.parseInt(splittedLine[1]);
			// graphRep.dest_in[i] = graphRep.dest[i];

			graphRep.dist[i] = Integer.parseInt(splittedLine[2]);
			// graphRep.dist_in[i] = graphRep.dist[i];

			// make mult better usable for us: save the distances with
			// multipliers applied directly
			// TODO find out what exactly is used by CH generator
			graphRep.multipliedDist[i] = (int) ((1.3 / Double
					.parseDouble(splittedLine[3])) * ((double) graphRep.dist[i]));
			// graphRep.multipliedDist_in[i] = graphRep.multipliedDist[i];

			// TODO graphRep.elev_out[i] = Float.parseFloat(splittedLine[4]);
			// TODO graphRep.elev_in[i] = graphRep.elev_out[i];

			if (splittedLine.length == 7) {
				graphRep.shortedID[i] = Integer.parseInt(splittedLine[4]);
				graphRep.outEdgeShortedNum[i] = Integer
						.parseInt(splittedLine[5]);
				graphRep.outEdgeSourceNum[i] = Integer
						.parseInt(splittedLine[6]);
			} else {
				graphRep.shortedID[i] = -1;
				graphRep.outEdgeShortedNum[i] = -1;
				graphRep.outEdgeSourceNum[i] = -1;
			}
			// graphRep.shortedID_in[i] = graphRep.shortedID[i];
			// graphRep.outEdgeShortedNum_in[i] = graphRep.outEdgeShortedNum[i];
			// graphRep.outEdgeSourceNum_in[i] = graphRep.outEdgeSourceNum[i];

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
			currentDest = graphRep.dest[graphRep.mapping_InToOut[i]];
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