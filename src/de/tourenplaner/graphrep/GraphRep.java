/**
 * $$\\ToureNPlaner\\$$
 */

package de.tourenplaner.graphrep;

import java.io.Serializable;

/**
 * Class GraphRep
 */
public class GraphRep implements Serializable {
    private static final long serialVersionUID = 15L;

    /**
     * This class is used internally to sort the edge arrays
     * it's a heap sort implementation using a ternary heap.
     */
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
            int temp = graphRep.mappingInToOut[i];
            graphRep.mappingInToOut[i] = graphRep.mappingInToOut[j];
            graphRep.mappingInToOut[j] = temp;
        }

        /**
         * @param graphRep
         * @param i
         * @param j
         * @return
         */
        private static boolean less(GraphRep graphRep, int i, int j) {
            int edgeI, edgeJ;
            int targetI, targetJ;
            int sourceRankI, sourceRankJ;
            edgeI = graphRep.mappingInToOut[i];
            edgeJ = graphRep.mappingInToOut[j];
            targetI = graphRep.trgt[edgeI];
            targetJ = graphRep.trgt[edgeJ];
            sourceRankI = graphRep.getRank(graphRep.src[edgeI]);
            sourceRankJ = graphRep.getRank(graphRep.src[edgeJ]);
            return targetI < targetJ || (targetI == targetJ && sourceRankI > sourceRankJ);
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
    protected final int[] rankSlope;

    protected final int[] shortedEdge1;
    protected final int[] shortedEdge2;

    protected int[] offsetOut;
    protected int[] offsetIn;

    /**
     * The constant used to compute time traveled on an edge from it's
     * non euclidian distance value by time=getDist(edgeId)*travelTimeConstant
     * time is in seconds
     */
    public final double travelTimeConstant = 0.02769230769230769230769230769230769;

    /**
     * Index = "virtual" id for in edges
     * <p/>
     * content = position in the edge array
     */
    protected int[] mappingInToOut;

    /**
     * A GraphRep is the representation of a graph used to perform several
     * algorithms on.
     * This constructor creates all internal data structures but the data needs
     * to be set with setEdgeDate, setShortcutData and setNodeData
     * then one can generate the offset arrays from this data.
     */
    public GraphRep(int nodeCount, int edgeCount) {
        this.nodeCount = nodeCount;
        this.edgeCount = edgeCount;

        this.lat = new int[nodeCount];
        this.lon = new int[nodeCount];
        this.height = new int[nodeCount];

        this.rank = new int[nodeCount];

        this.src = new int[edgeCount];
        this.trgt = new int[edgeCount];
        this.dist = new int[edgeCount];
        this.euclidianDist = new int[edgeCount];

        this.rankSlope = new int[edgeCount];
        this.shortedEdge1 = new int[edgeCount];
        this.shortedEdge2 = new int[edgeCount];

        // Use the simple DumbNN as searcher per default
        // this one needs no setup time but is slow
        this.searcher = new DumbNN(this);
    }

    /**
     * Set the NNSearcher used by this GraphRep
     * @param searcher
     */
    public void setNNSearcher(NNSearcher searcher) {
        this.searcher = searcher;
    }

    /**
     * Sets the data fields of the node given by it's id
     *
     * @param id
     * @param lat    in degrees*10^7
     * @param lon    in degrees*10^7
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
     */
    protected final void setEdgeData(int index, int source, int target, int dist, int euclidianDist) {
        this.src[index] = source;
        this.trgt[index] = target;
        this.dist[index] = dist;
        this.euclidianDist[index] = euclidianDist;

        this.shortedEdge1[index] = -1;
        this.shortedEdge1[index] = -1;
        this.rankSlope[index] = rank[target] - rank[source];
    }

    /**
     * Sets the shortcut fields of the edge given by it's id
     *
     * @param id
     * @param shortedEdge2
     * @paramn shortedEdge1
     */
    protected final void setShortcutData(int id, int shortedEdge1, int shortedEdge2) {
        this.shortedEdge1[id] = shortedEdge1;
        this.shortedEdge2[id] = shortedEdge2;
    }


    /**
     * Sets the offsetOut array to the given array, this method
     * is only used for low level graph loading so generateOffsets()
     * can be avoided
     *
     * @param newOffsetOut
     */
    protected final void setOffsetOut(int[] newOffsetOut) {
        this.offsetOut = newOffsetOut;
    }

    /**
     * Gets the offsetOut array used by this GraphRep, this method
     * is only used for low level graph writing.
     */
    protected final int[] getOffsetOut() {
        return this.offsetOut;
    }

    /**
     * Sets the offsetIn array to the given array, this method
     * is only used for low level graph loading so generateOffsets()
     * can be avoided
     *
     * @param newOffsetIn
     */
    protected final void setOffsetIn(int[] newOffsetIn) {
        this.offsetIn = newOffsetIn;
    }

    /**
     * Gets the offsetIn array used by this GraphRep, this method
     * is only used for low level graph writing.
     */
    protected final int[] getOffsetIn() {
        return this.offsetIn;
    }

    /**
     * Sets the mappingInToOut array to the given array, this method
     * is only used for low level graph loading so generateOffsets()
     * can be avoided
     *
     * @param newMapping
     */
    protected final void setMappingInToOut(int[] newMapping) {
        this.mappingInToOut = newMapping;
    }

    /**
     * Gets the mappingInToOut array used by this GraphRep, this method
     * is only used for low level graph writing.
     */
    protected final int[] getMappingInToOut() {
        return this.mappingInToOut;
    }

    /**
     * Regenerates the offset arrays from the current edge arrays
     */
    protected final void generateOffsets() {
        this.mappingInToOut = new int[edgeCount];
        this.offsetOut = new int[nodeCount + 1];
        this.offsetIn = new int[nodeCount + 1];

        // Set mapping to inital values (0,1,2,3..)
        for (int i = 0; i < edgeCount; i++) {
            this.mappingInToOut[i] = i;
        }

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
            currentDest = trgt[mappingInToOut[i]];
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
     * @param edgeId
     * @return int
     */
    public final int getDist(int edgeId) {
        return dist[edgeId];
    }

    /**
     * Gets the distance (in meters) of the the edge given by it's edgeId
     * (that's not an edgeNum but a unique Id for each edge) get the Id with
     * GetOutEdgeID() and GetInEdgeID()
     *
     * @param edgeId
     * @return int
     */
    public final int getEuclidianDist(int edgeId) {
        return euclidianDist[edgeId];
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
    public final int getIdForCoordinates(int srclat, int srclon) {
        return searcher.getIDForCoordinates(srclat, srclon);
    }

    /**
     * Gets the distance in shortest path form that is multiplied for travel
     * time of the in going edge identified by it's target node and edgeNum the
     * edgeNum is between 0 and getInEdgeCount(nodeId)-1
     *
     * @param nodeId
     * @param edgeNum
     * @return int
     */
    public final int getInDist(int nodeId, int edgeNum) {
        // return dist_in[offsetIn[nodeID] + edgeNum];
        return dist[mappingInToOut[offsetIn[nodeId] + edgeNum]];
    }

    /**
     * Gets the rankSlope of the in going edge identified by it's target
     * node and edgeNum, the edgeNum is between 0 and getInEdgeCount(nodeId)-1
     *
     * @param nodeId
     * @param edgeNum
     * @return
     */
    public final int getInRankSlope(int nodeId, int edgeNum) {
        return rankSlope[mappingInToOut[offsetIn[nodeId] + edgeNum]];
    }

    /**
     * Gets the euclidian distance (in meters) of the in going edge identified
     * by it's target node and edgeNum the edgeNum is between 0 and
     * getInEdgeCount(nodeId)-1
     *
     * @param nodeId
     * @param edgeNum
     * @return int
     */
    public final int getInEuclidianDist(int nodeId, int edgeNum) {
        // return dist_in[offsetIn[nodeID] + edgeNum];
        return euclidianDist[mappingInToOut[offsetIn[nodeId] + edgeNum]];
    }

    /**
     * Gets the number of in going edges of the given node
     *
     * @param nodeId
     * @return int
     */
    public final int getInEdgeCount(int nodeId) {
        return offsetIn[nodeId + 1] - offsetIn[nodeId];
    }

    /**
     * gets the edgeId of the in going edge identified by it's target node and
     * edgeNum
     *
     * @param nodeId
     * @param edgeNum
     * @return
     */
    public final int getInEdgeId(int nodeId, int edgeNum) {
        return mappingInToOut[offsetIn[nodeId] + edgeNum];
    }

    /**
     * Gets the source of the in going edge identified by it's target node and
     * edgeNum the edgeNum is between 0 and getInEdgeCount(nodeId)-1
     *
     * @param nodeId
     * @return int
     */
    public final int getInSource(int nodeId, int edgeNum) {
        // return src_in[offsetIn[nodeID] + edgeNum];
        return src[mappingInToOut[offsetIn[nodeId] + edgeNum]];
    }

    /**
     * Gets the edgeId of the first shortcutted edge of the given ingoing edge
     *
     * @param nodeId
     * @param edgeNum
     * @return
     */
    public final int getInFirstShortcuttedEdge(int nodeId, int edgeNum) {
        return shortedEdge1[mappingInToOut[offsetOut[nodeId] + edgeNum]];
    }

    /**
     * Gets the edgeId of the second shortcutted edge of the given ingoing edge
     *
     * @param nodeId
     * @param edgeNum
     * @return
     */
    public final int getInSecondShortcuttedEdge(int nodeId, int edgeNum) {
        return shortedEdge2[mappingInToOut[offsetOut[nodeId] + edgeNum]];
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
     * @param nodeId
     * @return float
     */
    public final int getNodeHeight(int nodeId) {
        return height[nodeId];
    }

    /**
     * Gets the latitude of the node with the given nodeId
     *
     * @param nodeId
     * @return int (degrees*10^7)
     */
    public final int getNodeLat(int nodeId) {
        return lat[nodeId];
    }

    /**
     * Gets the longitude of the node with the given nodeId
     *
     * @param nodeId
     * @return int (degrees*10^7)
     */
    public final int getNodeLon(int nodeId) {
        return lon[nodeId];
    }

    /**
     * Gets the multiplied distance (in meters) of the out going edge identified
     * by it's source node and edgeNum the edgeNum is between 0 and
     * getOutEdgeCount(nodeId)-1
     *
     * @param nodeId
     * @param edgeNum
     * @return int
     */
    public final int getOutDist(int nodeId, int edgeNum) {
        return dist[offsetOut[nodeId] + edgeNum];
    }

    /**
     * Gets the rankSlope of the out going edge identified by it's source
     * node and edgeNum, the edgeNum is between 0 and getInEdgeCount(nodeId)-1
     *
     * @param nodeId
     * @param edgeNum
     * @return
     */
    public final int getOutRankSlope(int nodeId, int edgeNum) {
        return rankSlope[offsetOut[nodeId] + edgeNum];
    }

    /**
     * Gets the euclidian distance (in meters) of the out going edge identified
     * by it's source node and edgeNum the edgeNum is between 0 and
     * getOutEdgeCount(nodeId)-1
     *
     * @param nodeId
     * @param edgeNum
     * @return int
     */
    public final int getOutEuclidianDist(int nodeId, int edgeNum) {
        return euclidianDist[offsetOut[nodeId] + edgeNum];
    }

    /**
     * Gets the number of out going edges of the given node
     *
     * @param nodeId
     */
    public final int getOutEdgeCount(int nodeId) {
        return offsetOut[nodeId + 1] - offsetOut[nodeId];
    }

    /**
     * Gets the edgeId of the out going edge identified by it's source node and
     * edgeNum
     *
     * @param nodeId
     * @param edgeNum
     * @return
     */
    public final int getOutEdgeId(int nodeId, int edgeNum) {
        return offsetOut[nodeId] + edgeNum;
    }

    /**
     * Gets the edgeId of the first shortcutted edge of the given outgoing edge
     *
     * @param nodeId
     * @param edgeNum
     * @return
     */
    public final int getOutFirstShortcuttedEdge(int nodeId, int edgeNum) {
        return shortedEdge1[offsetOut[nodeId] + edgeNum];
    }

    /**
     * Gets the edgeId of the second shortcutted edge of the given outgoing edge
     *
     * @param nodeId
     * @param edgeNum
     * @return
     */
    public final int getOutSecondShortcuttedEdge(int nodeId, int edgeNum) {
        return shortedEdge2[offsetOut[nodeId] + edgeNum];
    }

    /**
     * Gets the target of the out going edge identified by it's source node and
     * edgeNum the edgeNum is between 0 and getOutEdgeCount(nodeId)-1
     *
     * @param nodeId
     * @param edgeNum
     * @return int
     */
    public final int getOutTarget(int nodeId, int edgeNum) {
        return trgt[offsetOut[nodeId] + edgeNum];
    }

    /**
     * Gets the CH rank in the graph, can be MAX_INT if the node hasn't been
     * contracted at all CH property is getRank(nodeA)<=getRank(nodeB)
     *
     * @param nodeId
     * @return
     */
    public final int getRank(int nodeId) {
        return rank[nodeId];
    }

    /**
     * Gets the source of the the edge given by it's edgeId (that's not an
     * edgeNum but a unique Id for each edge) get the Id with GetOutEdgeID() and
     * GetInEdgeID()
     *
     * @param edgeId
     * @return int
     */
    public final int getSource(int edgeId) {
        return src[edgeId];
    }

    /**
     * Gets the rankSlope of the edge identified by it's edgeId
     *
     * @param edgeId
     * @return
     */
    public final int getRankSlope(int edgeId) {
        return rankSlope[edgeId];
    }

    /**
     * Gets the target of the the edge given by it's edgeId (that's not an
     * edgeNum but a unique Id for each edge) get the Id with GetOutEdgeID() and
     * GetInEdgeID()
     *
     * @param edgeId
     * @return int
     */
    public final int getTarget(int edgeId) {
        return trgt[edgeId];
    }

}
