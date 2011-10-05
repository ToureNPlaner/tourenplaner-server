package graphrep;

import java.io.Serializable;

/**
 * Class GraphRep
 */
public class GraphRep implements Serializable {
	private static final long serialVersionUID = 5L;

	protected NNSearcher searcher;

	protected int nodeCount;
	protected int edgeCount;

	// note: edgeNum refers to the relative number of an outgoing edge
	// from a node:
	// (first outgoing edge = 0, second outgoing edge = 1, ...)

	// nodes
	protected double[] lat;
	protected double[] lon;
	protected int[] height;
	protected int[] rank;

	// edges
	protected int[] src;
	protected int[] dest;
	protected int[] multipliedDist;
	protected int[] dist;
	// protected double[] elev_out;
	protected int[] shortedID;
	protected int[] outEdgeSourceNum;
	protected int[] outEdgeShortedNum;

	protected int[] offsetOut;
	protected int[] offsetIn;

	/**
	 * Index = "virtual" id for in edges
	 * 
	 * content = position in the edge array
	 */
	protected int[] mapping_InToOut;

	// protected int[] src_in;
	// protected int[] dest_in;
	// protected int[] multipliedDist_in;
	// protected int[] dist_in;
	// // protected double[] elev_in;
	// protected int[] shortedID_in;
	// protected int[] outEdgeSourceNum_in;
	// protected int[] outEdgeShortedNum_in;

	/**
	 * A graphrep is the representation of a graph used to perform several
	 * algorithms on. All its protected fields must be be set from the outside
	 * (which is not nice)
	 */
	public GraphRep() {
	}

	/**
	 * Gets the distance (in meters) of the given edge
	 * 
	 * @return int
	 * @param nodeID
	 * @param edgeNum
	 */
	public final int geDist(int edgeId) {
		return dist[edgeId];
	}

	/**
	 * Gets the distance (in meters) of the the edge given by it's edgeId (that's not an edgeNum but a unique Id for each edge)
	 * get the Id with GetOutEdgeID() and GetInEdgeID()
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
	 * Gets the edgeNum of the out going edge from the source of this edge
	 * to the shortcutted node.
	 * This is -1 if this edge is not a shortcut
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getEdgeSourceNum(int edgeId) {
		return outEdgeSourceNum[edgeId];
	}

	/**
	 * Gets the edgeNum of the out going edge from the shortcutted node of this edge
	 * to the shortcutted node.
	 * This is -1 if this edge is not a shortcut
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
	 * Gets the distance (in meters) of the in going edge identified by it's target node and edgeNum
	 * the edgeNum is between 0 and getInEdgeCount(nodeId)-1
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
	 * gets the edgeId of the in going edge identified by it's target node and edgeNum
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getInEdgeID(int nodeID, int edgeNum) {
		return mapping_InToOut[offsetIn[nodeID] + edgeNum];
	}

	/**
	 * Gets the nodeId of the shortcutted node of the in going edge identified by it's target node and edgeNum
	 * This is -1 if this edge is not a shortcut
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
	 * Gets the edgeNum of the out going edge from the shortcutted node of this edge
	 * to the target of this edge.
	 * This is -1 if this edge is not a shortcut
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
	 * Gets the edgeNum of the out going edge from the source of this edge
	 * to the shortcutted node.
	 * This is -1 if this edge is not a shortcut
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
	 * Gets the weighted distance of the in going edge identified by it's target node and edgeNum
	 * the edgeNum is between 0 and getInEdgeCount(nodeId)-1
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
	 * Gets the source of the in going edge identified by it's target node and edgeNum
	 * the edgeNum is between 0 and getInEdgeCount(nodeId)-1
	 * 
	 * @return int
	 * @param nodeID
	 */
	public final int getInSource(int nodeID, int edgeNum) {
		// return src_in[offsetIn[nodeID] + edgeNum];
		return src[mapping_InToOut[offsetIn[nodeID] + edgeNum]];
	}
	/**
	 * Gets the weighted distance of the the edge given by it's edgeId (that's not an edgeNum but a unique Id for each edge)
	 * get the Id with GetOutEdgeID() and GetInEdgeID()
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
	 * Gets the distance (in meters) of the out going edge identified by it's source node and edgeNum
	 * the edgeNum is between 0 and getOutEdgeCount(nodeId)-1
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
	 * Gets the edgeId of the out going edge identified by it's source node and edgeNum
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return 
	 */
	public int getOutEdgeID(int nodeID, int edgeNum) {
		return offsetOut[nodeID] + edgeNum;
	}
	
	/**
	 * Gets the edgeNum of the out going edge from the shortcutted node of this edge
	 * to the target of this edge.
	 * This is -1 if this edge is not a shortcut
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getOutEdgeShortedOut(int nodeID, int edgeNum) {
		return outEdgeShortedNum[offsetOut[nodeID] + edgeNum];
	}

	/**
	 * Gets the edgeNum of the out going edge from the source of this edge
	 * to the shortcutted node.
	 * This is -1 if this edge is not a shortcut
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getOutEdgeSourceNum(int nodeID, int edgeNum) {
		return outEdgeSourceNum[offsetOut[nodeID] + edgeNum];
	}
	
	
	/**
	 * Gets the weighted distance of the out going edge identified by it's source node and edgeNum
	 * the edgeNum is between 0 and getOutEdgeCount(nodeId)-1
	 * 
	 * @return int
	 * @param nodeID
	 * @param edgeNum
	 */
	public final int getOutMultipliedDist(int nodeID, int edgeNum) {
		return multipliedDist[offsetOut[nodeID] + edgeNum];
	}


	/**
	 * Gets the nodeId of the shortcutted node of the out going edge identified by it's source node and edgeNum
	 * This is -1 if this edge is not a shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getOutShortedId(int nodeID, int edgeNum) {
		return shortedID[offsetOut[nodeID] + edgeNum];
	}
	

	/**
	 * Gets the edgeNum of the out going edge from the shortcutted node of this edge
	 * to the target of this edge.
	 * This is -1 if this edge is not a shortcut
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getOutShortedOut(int edgeId) {
		return outEdgeShortedNum[edgeId];
	}
	
	
	/**
	 * Gets the target of the out going edge identified by it's source node and edgeNum
	 * the edgeNum is between 0 and getOutEdgeCount(nodeId)-1
	 * 
	 * @return int
	 * @param nodeID
	 * @param edgeNum
	 */
	public final int getOutTarget(int nodeID, int edgeNum) {
		return dest[offsetOut[nodeID] + edgeNum];
	}
	
	
	

	/**
	 * Gets the CH rank in the graph, can be MAX_INT if the node hasn't been contracted at all
	 * CH property is getRank(nodeA)<=getRank(nodeB)
	 * 
	 * @param nodeID
	 * @return
	 */
	public int getRank(int nodeID) {
		return rank[nodeID];
	}

	/**
	 * Gets the nodeId of the shortcutted node of the given edge
	 * This is -1 if this edge is not a shortcut
	 * 
	 * @param nodeID
	 * @param edgeNum
	 * @return
	 */
	public int getShortedId(int edgeId) {
		return shortedID[edgeId];
	}
	
	/**
	 * Gets the source of the the edge given by it's edgeId (that's not an edgeNum but a unique Id for each edge)
	 * get the Id with GetOutEdgeID() and GetInEdgeID()
	 * 
	 * @return int
	 * @param edgeID
	 */
	public final int getSource(int edgeID) {
		return src[edgeID];
	}
	/**
	 * Gets the target of the the edge given by it's edgeId (that's not an edgeNum but a unique Id for each edge)
	 * get the Id with GetOutEdgeID() and GetInEdgeID()
	 * 
	 * @return int
	 * @param edgeID
	 */
	public final int getTarget(int edgeID) {
		return dest[edgeID];
	}
	

}