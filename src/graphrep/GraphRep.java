package graphrep;

import java.io.Serializable;

/**
 * Class GraphRep
 */
public class GraphRep implements Serializable {
	private static final long serialVersionUID = 7L;

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

			while (topI * 3 + 1 <= endI) {
				cLI = topI * 3 + 1;
				cMI = cLI + 1;
				cRI = cLI + 2;
				cMaxI = topI;

				if (less(graphRep, cMaxI, cLI)) {
					cMaxI = cLI;
				}
				if (cMI <= endI && less(graphRep, cMaxI, cMI)) {
					cMaxI = cMI;
				}
				if (cRI <= endI && less(graphRep, cMaxI, cRI)) {
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

	protected NNSearcher searcher;

	private int nodeCount;
	private int edgeCount;

	// note: edgeNum refers to the relative number of an outgoing edge
	// from a node:
	// (first outgoing edge = 0, second outgoing edge = 1, ...)

	// nodes
	private double[] lat;
	private double[] lon;
	private int[] height;
	private int[] rank;

	// edges
	private int[] src;
	private int[] dest;
	private int[] multipliedDist;
	private int[] dist;

	private int[] shortedID;
	private int[] outEdgeSourceNum;
	private int[] outEdgeShortedNum;

	private int[] offsetOut;
	private int[] offsetIn;

	/**
	 * Index = "virtual" id for in edges
	 * 
	 * content = position in the edge array
	 */
	private int[] mapping_InToOut;

	/**
	 * A graphrep is the representation of a graph used to perform several
	 * algorithms on. All its protected fields must be be set from the outside
	 * (which is not nice)
	 */
	public GraphRep(int nodeCount, int edgeCount) {
		this.nodeCount = nodeCount;
		this.edgeCount = edgeCount;

		this.lat = new double[nodeCount];
		this.lon = new double[nodeCount];
		this.height = new int[nodeCount];

		this.rank = new int[nodeCount];

		this.offsetOut = new int[nodeCount + 1];
		this.offsetIn = new int[nodeCount + 1];

		this.src = new int[edgeCount];
		this.dest = new int[edgeCount];
		this.multipliedDist = new int[edgeCount];
		this.dist = new int[edgeCount];

		this.shortedID = new int[edgeCount];
		this.outEdgeSourceNum = new int[edgeCount];
		this.outEdgeShortedNum = new int[edgeCount];

		this.mapping_InToOut = new int[edgeCount];
	}

	/**
	 * Sets the data fields of the node given by it's id
	 * 
	 * @param id
	 * @param lat
	 * @param lon
	 * @param height
	 */
	protected final void setNodeData(int id, double lat, double lon, int height) {
		this.lat[id] = lat;
		this.lon[id] = lon;
		this.height[id] = height;
		this.rank[id] = Integer.MAX_VALUE;
	}

	/**
	 * Sets the rank of the node given by it's id
	 * 
	 * @param id
	 * @param rank
	 */
	protected final void setNodeRank(int id, int rank) {
		this.rank[id] = rank;
	}

	/**
	 * Set the data filed of the edge given by it's id
	 * 
	 * @param index
	 * @param src
	 * @param dest
	 * @param dist
	 * @param multipliedDist
	 */
	protected final void setEdgeData(int index, int src, int dest, int dist,
			int multipliedDist) {
		this.mapping_InToOut[index] = index;

		this.src[index] = src;
		this.dest[index] = dest;
		this.dist[index] = dist;
		this.multipliedDist[index] = multipliedDist;

		this.shortedID[index] = -1;
		this.outEdgeSourceNum[index] = -1;
		this.outEdgeShortedNum[index] = -1;
	}

	/**
	 * Sets the shortcut fields of the edge given by it's id
	 * 
	 * @param id
	 * @param shortedID
	 * @param outEdgeSourceNum
	 * @param outEdgeShortedNum
	 */
	protected final void setShortcutData(int id, int shortedID,
			int outEdgeSourceNum, int outEdgeShortedNum) {
		this.shortedID[id] = shortedID;
		this.outEdgeSourceNum[id] = outEdgeSourceNum;
		this.outEdgeShortedNum[id] = outEdgeShortedNum;
	}

	/**
	 * Regenerates the offset arrays from the current edge arrays
	 */
	protected final void generateOffsets() {
		int currentSource;
		int prevSource = -1;
		for (int i = 0; i < edgeCount; i++) {
			currentSource = src[i];
			if (currentSource != prevSource) {
				for (int j = currentSource; j > prevSource; j--) {
					offsetOut[j] = i;
				}
				prevSource = currentSource;
			}
		}

		offsetOut[nodeCount] = edgeCount;
		// assuming we have at least one edge
		for (int cnt = nodeCount - 1; offsetOut[cnt] == 0; cnt--) {
			offsetOut[cnt] = offsetOut[cnt + 1];
		}

		Sorter.Sort(this);

		int currentDest;
		int prevDest = -1;
		for (int i = 0; i < edgeCount; i++) {
			currentDest = dest[mapping_InToOut[i]];
			if (currentDest != prevDest) {
				for (int j = currentDest; j > prevDest; j--) {
					offsetIn[j] = i;
				}
				prevDest = currentDest;
			}
		}

		offsetIn[nodeCount] = edgeCount;
		// assuming we have at least one edge
		for (int cnt = nodeCount - 1; offsetIn[cnt] == 0; cnt--) {
			offsetIn[cnt] = offsetIn[cnt + 1];
		}
	}

	/**
	 * Gets the distance (in meters) of the the edge given by it's edgeId
	 * (that's not an edgeNum but a unique Id for each edge) get the Id with
	 * GetOutEdgeID() and GetInEdgeID()
	 * 
	 * @return int
	 * @param edgeID
	 * @param edgeNum
	 */
	public final int getDist(int edgeID) {
		return dist[edgeID];
	}

	/**
	 * Gets the number of edges in the graph
	 * 
	 * @return int
	 */
	public final int getEdgeCount() {
		return edgeCount;
	}

	/**
	 * Gets the edgeNum of the out going edge from the source of this edge to
	 * the shortcutted node. This is -1 if this edge is not a shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getEdgeSourceNum(int edgeId) {
		return outEdgeSourceNum[edgeId];
	}

	/**
	 * Gets the edgeNum of the out going edge from the shortcutted node of this
	 * edge to the shortcutted node. This is -1 if this edge is not a shortcut
	 * 
	 * @param edgeId
	 * @return
	 */
	public int getEdgeShortedNum(int edgeId) {
		return outEdgeShortedNum[edgeId];
	}

	/**
	 * Gets the node id of the node nearest to the given coordinates
	 * 
	 * @param srclat
	 * @param srclon
	 * @return
	 */
	public final int getIDForCoordinates(double srclat, double srclon) {
		return searcher.getIDForCoordinates(srclat, srclon);
	}

	/**
	 * Gets the distance (in meters) of the in going edge identified by it's
	 * target node and edgeNum the edgeNum is between 0 and
	 * getInEdgeCount(nodeId)-1
	 * 
	 * @return int
	 * @param nodeID
	 * @param edgeNum
	 */
	public final int getInDist(int nodeID, int edgeNum) {
		// return dist_in[offsetIn[nodeID] + edgeNum];
		return dist[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
	}

	/**
	 * Gets the number of in going edges of the given node
	 * 
	 * @return int
	 * @param nodeID
	 */
	public final int getInEdgeCount(int nodeID) {
		return offsetIn[nodeID + 1] - offsetIn[nodeID];
	}

	/**
	 * gets the edgeId of the in going edge identified by it's target node and
	 * edgeNum
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getInEdgeID(int nodeID, int edgeNum) {
		return mapping_InToOut[offsetIn[nodeID] + edgeNum];
	}

	/**
	 * Gets the nodeId of the shortcutted node of the in going edge identified
	 * by it's target node and edgeNum This is -1 if this edge is not a shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getInEdgeShortedId(int nodeID, int edgeNum) {
		// return shortedID_in[offsetIn[nodeID] + edgeNum];
		return shortedID[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
	}

	/**
	 * Gets the edgeNum of the out going edge from the shortcutted node of this
	 * edge to the target of this edge. This is -1 if this edge is not a
	 * shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getInEdgeShortedOut(int nodeID, int edgeNum) {
		// return outEdgeShortedNum_in[offsetIn[nodeID] + edgeNum];
		return outEdgeShortedNum[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
	}

	/**
	 * Gets the edgeNum of the out going edge from the source of this edge to
	 * the shortcutted node. This is -1 if this edge is not a shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getInEdgeSourceOut(int nodeID, int edgeNum) {
		// return outEdgeSourceNum_in[offsetIn[nodeID] + edgeNum];
		return outEdgeSourceNum[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
	}

	/**
	 * Gets the weighted distance of the in going edge identified by it's target
	 * node and edgeNum the edgeNum is between 0 and getInEdgeCount(nodeId)-1
	 * 
	 * @return int
	 * @param nodeID
	 * @param edgeNum
	 */
	public final int getInMultipliedDist(int nodeID, int edgeNum) {
		// return multipliedDist_in[offsetIn[nodeID] + edgeNum];
		return multipliedDist[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
	}

	/**
	 * Gets the source of the in going edge identified by it's target node and
	 * edgeNum the edgeNum is between 0 and getInEdgeCount(nodeId)-1
	 * 
	 * @return int
	 * @param nodeID
	 */
	public final int getInSource(int nodeID, int edgeNum) {
		// return src_in[offsetIn[nodeID] + edgeNum];
		return src[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
	}

	/**
	 * Gets the weighted distance of the the edge given by it's edgeId (that's
	 * not an edgeNum but a unique Id for each edge) get the Id with
	 * GetOutEdgeID() and GetInEdgeID()
	 * 
	 * @param edgeId
	 * @return
	 */
	public final int getMultipliedDist(int edgeId) {
		return multipliedDist[edgeId];
	}

	/**
	 * Gets the number of nodes in the graph
	 * 
	 * @return int
	 */
	public final int getNodeCount() {
		return nodeCount;
	}

	/**
	 * Gets the height of the given node in meters
	 * 
	 * @return float
	 * @param nodeID
	 */
	public final float getNodeHeight(int nodeID) {
		return height[nodeID];
	}

	/**
	 * Gets the latitude of the node with the given nodeId
	 * 
	 * @return double
	 * @param nodeID
	 */
	public final double getNodeLat(int nodeID) {
		return lat[nodeID];
	}

	/**
	 * Gets the longitude of the node with the given nodeId
	 * 
	 * @return double
	 * @param nodeID
	 */
	public final double getNodeLon(int nodeID) {
		return lon[nodeID];
	}

	/**
	 * Gets the distance (in meters) of the out going edge identified by it's
	 * source node and edgeNum the edgeNum is between 0 and
	 * getOutEdgeCount(nodeId)-1
	 * 
	 * @return int
	 * @param nodeID
	 * @param edgeNum
	 */
	public final int getOutDist(int nodeID, int edgeNum) {
		return dist[offsetOut[nodeID] + edgeNum];
	}

	/**
	 * Gets the number of out going edges of the given node
	 * 
	 * @param nodeID
	 */
	public final int getOutEdgeCount(int nodeID) {
		return offsetOut[nodeID + 1] - offsetOut[nodeID];
	}

	/**
	 * Gets the edgeId of the out going edge identified by it's source node and
	 * edgeNum
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getOutEdgeID(int nodeID, int edgeNum) {
		return offsetOut[nodeID] + edgeNum;
	}

	/**
	 * Gets the edgeNum of the out going edge from the shortcutted node of this
	 * edge to the target of this edge. This is -1 if this edge is not a
	 * shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getOutEdgeShortedOut(int nodeID, int edgeNum) {
		return outEdgeShortedNum[offsetOut[nodeID] + edgeNum];
	}

	/**
	 * Gets the edgeNum of the out going edge from the source of this edge to
	 * the shortcutted node. This is -1 if this edge is not a shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getOutEdgeSourceNum(int nodeID, int edgeNum) {
		return outEdgeSourceNum[offsetOut[nodeID] + edgeNum];
	}

	/**
	 * Gets the weighted distance of the out going edge identified by it's
	 * source node and edgeNum the edgeNum is between 0 and
	 * getOutEdgeCount(nodeId)-1
	 * 
	 * @return int
	 * @param nodeID
	 * @param edgeNum
	 */
	public final int getOutMultipliedDist(int nodeID, int edgeNum) {
		return multipliedDist[offsetOut[nodeID] + edgeNum];
	}

	/**
	 * Gets the nodeId of the shortcutted node of the out going edge identified
	 * by it's source node and edgeNum This is -1 if this edge is not a shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getOutShortedId(int nodeID, int edgeNum) {
		return shortedID[offsetOut[nodeID] + edgeNum];
	}

	/**
	 * Gets the edgeNum of the out going edge from the shortcutted node of this
	 * edge to the target of this edge. This is -1 if this edge is not a
	 * shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getOutShortedOut(int edgeId) {
		return outEdgeShortedNum[edgeId];
	}

	/**
	 * Gets the target of the out going edge identified by it's source node and
	 * edgeNum the edgeNum is between 0 and getOutEdgeCount(nodeId)-1
	 * 
	 * @return int
	 * @param nodeID
	 * @param edgeNum
	 */
	public final int getOutTarget(int nodeID, int edgeNum) {
		return dest[offsetOut[nodeID] + edgeNum];
	}

	/**
	 * Gets the CH rank in the graph, can be MAX_INT if the node hasn't been
	 * contracted at all CH property is getRank(nodeA)<=getRank(nodeB)
	 * 
	 * @param nodeID
	 * @return
	 */
	public int getRank(int nodeID) {
		return rank[nodeID];
	}

	/**
	 * Gets the nodeId of the shortcutted node of the given edge This is -1 if
	 * this edge is not a shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getShortedId(int edgeId) {
		return shortedID[edgeId];
	}

	/**
	 * Gets the source of the the edge given by it's edgeId (that's not an
	 * edgeNum but a unique Id for each edge) get the Id with GetOutEdgeID() and
	 * GetInEdgeID()
	 * 
	 * @return int
	 * @param edgeID
	 */
	public final int getSource(int edgeID) {
		return src[edgeID];
	}

	/**
	 * Gets the target of the the edge given by it's edgeId (that's not an
	 * edgeNum but a unique Id for each edge) get the Id with GetOutEdgeID() and
	 * GetInEdgeID()
	 * 
	 * @return int
	 * @param edgeID
	 */
	public final int getTarget(int edgeID) {
		return dest[edgeID];
	}

}