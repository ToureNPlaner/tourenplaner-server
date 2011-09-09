package graphrep;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class Graphrep
 */
public class Graphrep {

	private final int nodeCount;
	private final int edgeCount;

	// note: edgeNum often refers to the relative number of an outgoing edge
	// from a node:
	// (first outgoing edge = 0, second outgoing edge = 1, ...)

	// (deprecated) File Format:
	// TODO: Ask Frederic for updated description
	// #N -> Number of Nodes
	// #X -> Number of Edges:
	// #N * (ID oldOsmID lat lon height)
	// #M * (source destination multiplier distance elevationDelta)

	// nodes
	// why no osm id??
	// private final int[] osmIDs;
	private final float[] lat;
	private final float[] lon;
	private final int[] height;

	// edges
	private final int[] source;
	private final int[] dest;
	private final float[] mult;
	private final float[] dist;
	// private final float[] elev;

	final int[] offsetOut;

	/**
	 * Constructor loads graph from disk
	 * 
	 * @throws IOException
	 * 
	 */
	public Graphrep() throws IOException {
		// TODO: get filename from config and replace the following
		String filename = System.getProperty("user.home") + "/germany.txt";

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filename));

		} catch (FileNotFoundException e) {
			// TODO: what happens here?
			e.printStackTrace();
		}

		String line;

		// exception should happen when file format is wrong
		line = in.readLine();

		while (line != null && line.trim().startsWith("#")) {
			line = in.readLine();
		}
		if (line != null) {
			nodeCount = Integer.parseInt(line);
		} else {
			nodeCount = 0;
		}

		line = in.readLine();
		if (line != null) {
			edgeCount = Integer.parseInt(line);
		} else {
			edgeCount = 0;
		}

		// TODO osmIDs = new int[nodeCount];
		lat = new float[nodeCount];
		lon = new float[nodeCount];
		height = new int[nodeCount];

		offsetOut = new int[nodeCount + 1];

		source = new int[edgeCount];
		dest = new int[edgeCount];
		mult = new float[edgeCount];
		// TODO elev = new float[edgeCount];
		dist = new float[edgeCount];

		// used for splitted lines in 1. nodes 2. edges
		String[] splittedLine;
		System.out.println("Reading " + nodeCount + " nodes and " + edgeCount
				+ " edges...");
		for (int i = 0; i < nodeCount; i++) {
			splittedLine = in.readLine().split(" ");
			// TODO osmIDs[i] = Integer.parseInt(linesplitted[1]);
			lat[i] = Float.parseFloat(splittedLine[1]);
			lon[i] = Float.parseFloat(splittedLine[2]);
			height[i] = Integer.parseInt(splittedLine[3]);
		}

		System.out.println("successfully read nodes");
		int currentSource;
		int prevSource = -1;
		for (int i = 0; i < edgeCount; i++) {
			splittedLine = in.readLine().split(" ");
			currentSource = Integer.parseInt(splittedLine[0]);
			source[i] = currentSource;
			dest[i] = Integer.parseInt(splittedLine[1]);
			dist[i] = Integer.parseInt(splittedLine[2]);
			mult[i] = Float.parseFloat(splittedLine[3]);
			// TODO mult[i] = Integer.parseInt(splittedLine[4]);

			if (currentSource != prevSource) {
				offsetOut[currentSource] = i;
				prevSource = currentSource;
			}
		}
		System.out.println("successfully read edges");

		for (int i = 0; i < edgeCount; i++) {

		}
	};

	/**
	 * @return float
	 * @param nodeId
	 */
	public final float getNodeLat(int nodeId) {
		return lat[nodeId];
	}

	/**
	 * @return float
	 * @param nodeId
	 */
	public final float getNodeLon(int nodeId) {
		return lon[nodeId];
	}

	/**
	 * @return long
	 * @param nodeId
	 */
	public final long getNodeOsmId(int nodeId) {
		// TODO: is not in the graph txt
		return -1;
	}

	/**
	 * @return float
	 * @param nodeId
	 */
	public final float getNodeHeight(int nodeId) {
		return height[nodeId];
	}

	/**
	 * @param nodeId
	 */
	public final int getOutEdgeCount(int nodeId) {
		return (offsetOut[nodeId + 1] - offsetOut[nodeId]);
	}

	/**
	 * @return int
	 * @param nodeId
	 */
	public final int getInEdgeCount(int nodeId) {
		// TODO: this is bad
		int result = 0;
		for (int i = 0; i < edgeCount; i++) {
			if (dest[i] == nodeId) {
				result += 1;
			}
		}
		return result;
	}

	/**
	 * @return int
	 * @param nodeId
	 * @param edgeNum
	 */
	public final int getOutTarget(int nodeId, int edgeNum) {
		return dest[offsetOut[nodeId + edgeNum]];
	}

	/**
	 * @return int
	 * @param nodeId
	 * @param edgeNum
	 */
	public final int getInSource(int nodeId, int edgeNum) {
		// TODO
		return edgeNum;
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public final float getOutDist(int nodeId, int edgeNum) {
		return dist[offsetOut[nodeId + edgeNum]];
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public final float getOutMult(int nodeId, int edgeNum) {
		return mult[offsetOut[nodeId + edgeNum]];
	}

	/**
	 * @param nodeId
	 * @param edgeNum
	 */
	public final float getOutEleDelta(int nodeId, int edgeNum) {
		// TODO
		return nodeId;
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public final float getInDist(int nodeId, int edgeNum) {
		// TODO
		return edgeNum;
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public final float getInMult(int nodeId, int edgeNum) {
		// TODO
		return edgeNum;
	}

	/**
	 * @param nodeId
	 * @param edgeNum
	 */
	public final int getInEleDelta(int nodeId, int edgeNum) {
		// TODO
		return edgeNum;
	}

	/**
	 * @return int
	 */
	public final int getNodeCount() {
		return nodeCount;
	}

	/**
	 * @return int
	 */
	public final int getEdgeCount() {
		return edgeCount;
	}

}