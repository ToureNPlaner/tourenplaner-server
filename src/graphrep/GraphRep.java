package graphrep;

/**
 * Class GraphRep
 */
public class GraphRep {

	protected NNSearcher searcher;

	protected int nodeCount;
	protected int edgeCount;

	// note: edgeNum often refers to the relative number of an outgoing edge
	// from a node:
	// (first outgoing edge = 0, second outgoing edge = 1, ...)

	// new fileformat is probably:
	// nodecount
	// edgecount
	// (nodecount *) ID lat lon height
	// (edgecount *) source dest dist mult

	// TODO: at the moment a simplistic solution for the inedges: create the
	// file with inedges with this script (assuming it is in new file format and
	// has no leading and no trailing newline):
	//
	// #!/bin/bash
	// INFILE="$1"
	// [[ -z $INFILE ]] && echo -e
	// "USAGE:\n\t$0 FILENAME\n\twill create FILENAME_inedges.txt" && exit 1
	// OUTFILE="$1_inedges.txt"
	// EDGENUM=$(head -2 ${INFILE} | tail -1)
	// tail -$EDGENUM ${INFILE} | sort -b -k2n,2 > ${OUTFILE}
	// exit 0

	// nodes
	// why no osm id??
	// private final int[] osmIDs;
	protected float[] lat;
	protected float[] lon;
	protected int[] height;

	// edges
	protected int[] source_out;
	protected int[] dest_out;
	protected float[] mult_out;
	protected int[] dist_out;
	protected float[] elev_out;

	protected int[] offsetOut;

	protected int[] source_in;
	protected int[] dest_in;
	protected float[] mult_in;
	protected int[] dist_in;
	protected float[] elev_in;

	protected int[] offsetIn;

	/**
	 * 
	 * 
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
	public final float getOutMult(int nodeId, int edgeNum) {
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
	public final float getInMult(int nodeId, int edgeNum) {
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