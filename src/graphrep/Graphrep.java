package graphrep;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class Graphrep
 */
public class Graphrep {

	private int nodeCount;
	private int edgeCount;

	// #File Format:
	// #N -> Number of Nodes
	// #X -> Number of Edges:
	// #N * (ID oldOsmID lat lon height)
	// #M * (source destination multiplier distance elevationDelta)

	// nodes
	// why no osm id??
	// private int[] osmIDs;
	private float[] lat;
	private float[] lon;
	private int[] height;

	// edges
	int[] source;
	int[] dest;
	int[] mult;
	float[] elev;

	/**
	 * Constructor loads graph from disk
	 * 
	 */
	public Graphrep() {
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
		try {

			// exception should happen when file format is wrong
			line = in.readLine();

			while (line != null && line.trim().startsWith("#")) {
				line = in.readLine();
			}

			if (line != null) {
				nodeCount = Integer.parseInt(line);
			}

			line = in.readLine();
			if (line != null) {
				edgeCount = Integer.parseInt(line);
			}

			// osmIDs = new int[nodeCount];
			lat = new float[nodeCount];
			lon = new float[nodeCount];
			height = new int[nodeCount];

			source = new int[edgeCount];
			dest = new int[edgeCount];
			mult = new int[edgeCount];
			elev = new float[edgeCount];

			// used for splitted lines in 1. nodes 2. edges
			String[] linesplitted;
			System.out.println("Reading " + nodeCount + " nodes and "
					+ edgeCount + " edges...");
			for (int i = 0; i < nodeCount; i++) {
				linesplitted = in.readLine().split(" ");
				// 0 is consecutive IDs
				// osmIDs[i] = Integer.parseInt(linesplitted[1]);
				lat[i] = Float.parseFloat(linesplitted[1]);
				lon[i] = Float.parseFloat(linesplitted[2]);
				height[i] = Integer.parseInt(linesplitted[3]);
			}
			System.out.println("successfully read nodes");
			for (int i = 0; i < edgeCount; i++) {
				linesplitted = in.readLine().split(" ");
				source[i] = Integer.parseInt(linesplitted[0]);
				dest[i] = Integer.parseInt(linesplitted[1]);
				mult[i] = Integer.parseInt(linesplitted[2]);
				elev[i] = Float.parseFloat(linesplitted[3]);
			}
			System.out.println("successfully read edges");
		} catch (IOException e) {
			// TODO error while reading graph file
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO : graph file format is wrong!
			e.printStackTrace();
		}
	};

	//
	// Methods
	//

	//
	// Accessor methods
	//

	//
	// Other methods
	//

	/**
	 * @return float
	 * @param nodeId
	 */
	public float getNodeLat(int nodeId) {
		return nodeId;
	}

	/**
	 * @return float
	 * @param nodeId
	 */
	public float getNodeLon(int nodeId) {
		return nodeId;
	}

	/**
	 * @return long
	 * @param nodeId
	 */
	public long getNodeOsmId(int nodeId) {
		return nodeId;
	}

	/**
	 * @return float
	 * @param nodeId
	 */
	public float getNodeHeight(int nodeId) {
		return nodeId;
	}

	/**
	 * @param nodeId
	 */
	public void getOutEdgeCount(int nodeId) {
	}

	/**
	 * @return int
	 * @param nodeId
	 */
	public int getInEdgeCount(int nodeId) {
		return nodeId;
	}

	/**
	 * @return int
	 * @param nodeId
	 * @param edgeNum
	 */
	public int getOutTarget(int nodeId, int edgeNum) {
		return edgeNum;
	}

	/**
	 * @return int
	 * @param nodeId
	 * @param edgeNum
	 */
	public int getInSource(int nodeId, int edgeNum) {
		return edgeNum;
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public float getOutDist(int nodeId, int edgeNum) {
		return edgeNum;
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public float getOutMult(int nodeId, int edgeNum) {
		return edgeNum;
	}

	/**
	 * @param nodeId
	 * @param edgeNum
	 */
	public void getOutEleDelta(int nodeId, int edgeNum) {
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public float getInDist(int nodeId, int edgeNum) {
		return edgeNum;
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public float getInMult(int nodeId, int edgeNum) {
		return edgeNum;
	}

	/**
	 * @param nodeId
	 * @param edgeNum
	 */
	public void getInEleDelta(int nodeId, int edgeNum) {
	}

	/**
	 * @return int
	 */
	public int getNodeCount() {
		return 0;
	}

	/**
	 * @return int
	 */
	public int getEdgeCount() {
		return 0;
	}

}