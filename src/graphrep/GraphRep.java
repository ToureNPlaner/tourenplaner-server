/**
 * $$\\ToureNPlaner\\$$
 */

package graphrep;

import java.io.Serializable;

/**
 * Class GraphRep
 */
public class GraphRep implements Serializable {
	private static final long serialVersionUID = 13L;

	private static class Sorter {
		private static void sort(GraphRep graphRep) {

			heapify(graphRep);
			int endI = graphRep.trgt.length - 1;
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
			int edgeI, edgeJ;
			int targetI, targetJ;
			int sourceRankI, sourceRankJ;
			edgeI = graphRep.mapping_InToOut[i];
			edgeJ = graphRep.mapping_InToOut[j];
			targetI = graphRep.trgt[edgeI];
			targetJ = graphRep.trgt[edgeJ];
			sourceRankI = graphRep.getRank(graphRep.src[edgeI]);
			sourceRankJ = graphRep.getRank(graphRep.src[edgeJ]);
			return targetI < targetJ || targetI == targetJ
					&& sourceRankI > sourceRankJ;
		}

		private static void heapify(GraphRep graphRep) {
			int pos = graphRep.trgt.length - 1;
			while (pos >= 0) {
				siftDown(graphRep, pos, graphRep.trgt.length - 1);
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

	private final int nodeCount;
	private final int edgeCount;

	// note: edgeNum refers to the relative number of an outgoing edge
	// from a node:
	// (first outgoing edge = 0, second outgoing edge = 1, ...)

	// nodes
	// In degrees*10^7
	protected final int[] lat;
	protected final int[] lon;
	protected final int[] height;
	protected final int[] rank;

	// edges
	protected final int[] src;
	protected final int[] trgt;
	protected final int[] dist;
	protected final int[] euclidianDist;

	protected final int[] shortedEdge1;
	protected final int[] shortedEdge2;

	protected final int[] offsetOut;
	protected final int[] offsetIn;

	/**
	 * Index = "virtual" id for in edges
	 * 
	 * content = position in the edge array
	 */
	protected final int[] mapping_InToOut;

	/**
	 * A graphrep is the representation of a graph used to perform several
	 * algorithms on. All its protected fields must be be set from the outside
	 * (which is not nice)
	 */
	public GraphRep(int nodeCount, int edgeCount) {
		this.nodeCount = nodeCount;
		this.edgeCount = edgeCount;

		this.lat = new int[nodeCount];
		this.lon = new int[nodeCount];
		this.height = new int[nodeCount];

		this.rank = new int[nodeCount];

		this.offsetOut = new int[nodeCount + 1];
		this.offsetIn = new int[nodeCount + 1];

		this.src = new int[edgeCount];
		this.trgt = new int[edgeCount];
		this.dist = new int[edgeCount];
		this.euclidianDist = new int[edgeCount];

		this.shortedEdge1 = new int[edgeCount];
		this.shortedEdge2 = new int[edgeCount];

		this.mapping_InToOut = new int[edgeCount];
	}

	/**
	 * Sets the data fields of the node given by it's id
	 * 
	 * @param id
	 * @param lat
	 *            in degrees*10^7
	 * @param lon
	 *            in degrees*10^7
	 * @param height
	 */
	protected final void setNodeData(int id, int lat, int lon, int height) {
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
	 * @param source
	 * @param target
	 * @param dist
	 * @param multipliedDist
	 */
	protected final void setEdgeData(int index, int source, int target,
			int dist, int euclidianDist) {
		this.mapping_InToOut[index] = index;

		this.src[index] = source;
		this.trgt[index] = target;
		this.dist[index] = dist;
		this.euclidianDist[index] = euclidianDist;

		this.shortedEdge1[index] = -1;
		this.shortedEdge1[index] = -1;
	}

	/**
	 * Sets the shortcut fields of the edge given by it's id
	 * 
	 * @param id
	 * @param shortedID
	 * @param outEdgeSourceNum
	 * @param outEdgeShortedNum
	 */
	protected final void setShortcutData(int id, int shortedEdge1,
			int shortedEdge2) {
		this.shortedEdge1[id] = shortedEdge1;
		this.shortedEdge2[id] = shortedEdge2;
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

		Sorter.sort(this);

		int currentDest;
		int prevDest = -1;
		for (int i = 0; i < edgeCount; i++) {
			currentDest = trgt[mapping_InToOut[i]];
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
	 * Gets the distance in the shortest path format that is multiplied for
	 * travel time of the the edge given by it's edgeId (that's not an edgeNum
	 * but a unique Id for each edge) get the Id with GetOutEdgeID() and
	 * GetInEdgeID()
	 * 
	 * @return int
	 * @param edgeID
	 * @param edgeNum
	 */
	public final int getDist(int edgeID) {
		return dist[edgeID];
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
	public final int getEuclidianDist(int edgeID) {
		return euclidianDist[edgeID];
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
	 * Gets the edgeId of the first shortcutted edge
	 * 
	 * @param edgeId
	 * @return
	 */
	public final int getFirstShortcuttedEdge(int edgeId) {
		return shortedEdge1[edgeId];
	}

	/**
	 * Gets the edgeId of the second shortcutted edge
	 * 
	 * @param edgeId
	 * @return
	 */
	public final int getSecondShortcuttedEdge(int edgeId) {
		return shortedEdge2[edgeId];
	}

	/**
	 * Gets the node id of the node nearest to the given coordinates
	 * 
	 * @param srclat
	 * @param srclon
	 * @return
	 */
	public final int getIDForCoordinates(int srclat, int srclon) {
		return searcher.getIDForCoordinates(srclat, srclon);
	}

	/**
	 * Gets the distance in shortest path form that is multiplied for travel time
    *  of the in going edge identified by it's
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
	 * Gets the euclidian distance (in meters) of the in going edge identified
	 * by it's target node and edgeNum the edgeNum is between 0 and
	 * getInEdgeCount(nodeId)-1
	 * 
	 * @return int
	 * @param nodeID
	 * @param edgeNum
	 */
	public final int getInEuclidianDist(int nodeID, int edgeNum) {
		// return dist_in[offsetIn[nodeID] + edgeNum];
		return euclidianDist[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
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
	public final int getInEdgeID(int nodeID, int edgeNum) {
		return mapping_InToOut[offsetIn[nodeID] + edgeNum];
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
	 * Gets the edgeId of the first shortcutted edge of the given ingoing edge
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public final int getInFirstShortcuttedEdge(int nodeID, int edgeNum) {
		return shortedEdge1[mapping_InToOut[offsetOut[nodeID] + edgeNum]];
	}

	/**
	 * Gets the edgeId of the second shortcutted edge of the given ingoing edge
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public final int getInSecondShortcuttedEdge(int nodeID, int edgeNum) {
		return shortedEdge2[mapping_InToOut[offsetOut[nodeID] + edgeNum]];
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
	 * @return int (degrees*10^7)
	 * @param nodeID
	 */
	public final int getNodeLat(int nodeID) {
		return lat[nodeID];
	}

	/**
	 * Gets the longitude of the node with the given nodeId
	 * 
	 * @return int (degrees*10^7)
	 * @param nodeID
	 */
	public final int getNodeLon(int nodeID) {
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
	public final int getOutEdgeID(int nodeID, int edgeNum) {
		return offsetOut[nodeID] + edgeNum;
	}

	/**
	 * Gets the edgeId of the first shortcutted edge of the given outgoing edge
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public final int getOutFirstShortcuttedEdge(int nodeID, int edgeNum) {
		return shortedEdge1[offsetOut[nodeID] + edgeNum];
	}

	/**
	 * Gets the edgeId of the second shortcutted edge of the given outgoing edge
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public final int getOutSecondShortcuttedEdge(int nodeID, int edgeNum) {
		return shortedEdge2[offsetOut[nodeID] + edgeNum];
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
		return trgt[offsetOut[nodeID] + edgeNum];
	}

	/**
	 * Gets the CH rank in the graph, can be MAX_INT if the node hasn't been
	 * contracted at all CH property is getRank(nodeA)<=getRank(nodeB)
	 * 
	 * @param nodeID
	 * @return
	 */
	public final int getRank(int nodeID) {
		return rank[nodeID];
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
		return trgt[edgeID];
	}

}
