package graphrep;

import java.io.Serializable;

/**
 * Class GraphRep
 */
public class GraphRep implements Serializable {
	private static final long serialVersionUID = 1L;

	protected NNSearcher searcher;

	protected int nodeCount;
	protected int edgeCount;

	// note: edgeNum refers to the relative number of an outgoing edge
	// from a node:
	// (first outgoing edge = 0, second outgoing edge = 1, ...)

	// nodes
	protected float[] lat;
	protected float[] lon;
	protected int[] height;

	// edges
	protected int[] source_out;
	protected int[] dest_out;
	protected int[] mult_out;
	protected int[] dist_out;
	protected float[] elev_out;

	protected int[] offsetOut;

	protected int[] source_in;
	protected int[] dest_in;
	protected int[] mult_in;
	protected int[] dist_in;
	protected float[] elev_in;

	protected int[] offsetIn;

	/**
	 * A graphrep is the representation of a graph used to perform several
	 * algorithms on. All its protected fields must be be set from the outside
	 * (which is not nice)
	 */
	public GraphRep() {
	}

	/**
	 * @param lat
	 * @param lon
	 * @return
	 */
	public final int getIDForCoordinates(float lat, float lon) {
		return searcher.getIDForCoordinates(lat, lon);
	}

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
		return offsetOut[nodeId + 1] - offsetOut[nodeId];
	}

	/**
	 * @return int
	 * @param nodeId
	 */
	public final int getInEdgeCount(int nodeId) {
		return offsetIn[nodeId + 1] - offsetIn[nodeId];
	}

	/**
	 * @return int
	 * @param nodeId
	 * @param edgeNum
	 */
	public final int getOutTarget(int nodeId, int edgeNum) {
		return dest_out[offsetOut[nodeId] + edgeNum];
	}

	/**
	 * @return int
	 * @param nodeId
	 * @param edgeNum
	 */
	public final int getInSource(int nodeId, int edgeNum) {
		return source_in[offsetIn[nodeId] + edgeNum];
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public final int getOutDist(int nodeId, int edgeNum) {
		return dist_out[offsetOut[nodeId] + edgeNum];
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public final int getOutMult(int nodeId, int edgeNum) {
		return mult_out[offsetOut[nodeId] + edgeNum];
	}

	/**
	 * @param nodeId
	 * @param edgeNum
	 */
	public final float getOutEleDelta(int nodeId, int edgeNum) {
		// TODO
		return -1;
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public final int getInDist(int nodeId, int edgeNum) {
		return dist_in[offsetIn[nodeId] + edgeNum];
	}

	/**
	 * @return float
	 * @param nodeId
	 * @param edgeNum
	 */
	public final int getInMult(int nodeId, int edgeNum) {
		return mult_in[offsetIn[nodeId] + edgeNum];
	}

	/**
	 * @param nodeId
	 * @param edgeNum
	 */
	public final int getInEleDelta(int nodeId, int edgeNum) {
		// TODO
		return -1;
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