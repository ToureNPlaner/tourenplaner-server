/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.graphrep;

import de.tourenplaner.algorithms.bbbundle.BoundingBox;
import de.tourenplaner.utils.SortAdapter;
import de.tourenplaner.utils.Sorter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class GraphRep
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class GraphRep implements Serializable {
    private static final long serialVersionUID = 15L;
    private static Logger log = Logger.getLogger("de.tourenplaner.graphrep");



    /**
     * This class is used internally to sort the Nodes by rank, which is needed
     * so that the CORE Nodes have ids [0, 1, 2, .., k]
     */
    private static final class NodeSortAdapter extends SortAdapter {
        private final GraphRep graph;
        private final int[] order;

        public NodeSortAdapter(GraphRep graph, int[] order) {
            this.graph = graph;
            this.order = order;
        }

        @Override
        public void swap(int i, int j) {
            int tmpLat = graph.lat[i];
            int tmpLon = graph.lon[i];
            int tmpRank = graph.rank[i];
            int tmpHeight = graph.height[i];
            int tmpOrder = order[i];

            graph.lat[i] = graph.lat[j];
            graph.lon[i] = graph.lon[j];
            graph.rank[i] = graph.rank[j];
            graph.height[i] = graph.height[j];
            order[i] = order[j];

            graph.lat[j] = tmpLat;
            graph.lon[j] = tmpLon;
            graph.rank[j] = tmpRank;
            graph.height[j] = tmpHeight;
            order[j] = tmpOrder;
        }

        @Override
        public boolean less(int i, int j) {
            return graph.rank[i] > graph.rank[j];
        }

        @Override
        public int length() {
            return graph.lat.length;
        }
    }

    /**
     * This class is used internally to sort the in edge arrays
     * it's a heap sort implementation using a ternary heap.
     */
    private static final class MappingSortAdapter extends SortAdapter {
        private final GraphRep graph;

        public MappingSortAdapter(GraphRep graph) {
            this.graph = graph;
        }

        @Override
        public boolean less(int i, int j) {
            int edgeI, edgeJ;
            int targetI, targetJ;
            int sourceRankI, sourceRankJ;
            edgeI = graph.mappingInToOut[i];
            edgeJ = graph.mappingInToOut[j];
            targetI = graph.trgt[edgeI];
            targetJ = graph.trgt[edgeJ];
            sourceRankI = graph.getRank(graph.src[edgeI]);
            sourceRankJ = graph.getRank(graph.src[edgeJ]);
            return targetI < targetJ || (targetI == targetJ && sourceRankI > sourceRankJ);
        }

        @Override
        public void swap(int i, int j) {
            int temp = graph.mappingInToOut[i];
            graph.mappingInToOut[i] = graph.mappingInToOut[j];
            graph.mappingInToOut[j] = temp;
        }

        @Override
        public int length() {
            return graph.mappingInToOut.length;
        }
    }

    /**
     * This class is used internally to sort the out edge arrays
     * in ascending rank order
     * it's a heap sort implementation using a ternary heap.
     */
    private static final class OutEdgeSortAdapter extends SortAdapter {
        private final GraphRep graph;
        private final int[] order;

        /**
         * Sorts the graphs out edges storing their new id in the order array
         * which needs to have edgeNum size and needs to be initialized
         * wit 0,1,2..,edgeNum-1
         *
         * @param graph
         * @param order
         */
        public OutEdgeSortAdapter(GraphRep graph, int[] order) {
            this.graph = graph;
            this.order = order;
        }

        @Override
        public boolean less(int i, int j) {
            int sourceI, sourceJ;
            int targetRankI, targetRankJ;
            sourceI = graph.src[i];
            sourceJ = graph.src[j];
            targetRankI = graph.getRank(graph.trgt[i]);
            targetRankJ = graph.getRank(graph.trgt[j]);
            return sourceI < sourceJ || (sourceI == sourceJ && targetRankI < targetRankJ);
        }

        @Override
        public void swap(int i, int j) {
            int tempId = order[i];
            int tempSrc = graph.src[i];
            int tempTrgt = graph.trgt[i];
            int tempDist = graph.dist[i];
            int tempEuclidianDist = graph.euclidianDist[i];
            int tempShorted1 = graph.shortedEdge1[i];
            int tempShorted2 = graph.shortedEdge2[i];

            graph.src[i] = graph.src[j];
            graph.trgt[i] = graph.trgt[j];
            graph.dist[i] = graph.dist[j];
            graph.euclidianDist[i] = graph.euclidianDist[j];
            graph.shortedEdge1[i] = graph.shortedEdge1[j];
            graph.shortedEdge2[i] = graph.shortedEdge2[j];
            order[i] = order[j];

            graph.src[j] = tempSrc;
            graph.trgt[j] = tempTrgt;
            graph.dist[j] = tempDist;
            graph.euclidianDist[j] = tempEuclidianDist;
            graph.shortedEdge1[j] = tempShorted1;
            graph.shortedEdge2[j] = tempShorted2;
            order[j] = tempId;
        }

        @Override
        public int length() {
            return graph.src.length;
        }
    }

    protected NNSearcher searcher;
    private BBoxPriorityTree bboxXYTree;
    private BBoxPriorityTree bboxLatLonTree;

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

    protected final int[] xPos;
    protected final int[] yPos;

    protected int[] offsetOut;
    protected int[] offsetIn;

    private int maxRank;
    private BoundingBox bbox;

    // edges
    protected final int[] src;
    protected final int[] trgt;
    protected final int[] dist;
    protected final int[] euclidianDist;

    protected final int[] shortedEdge1;
    protected final int[] shortedEdge2;

    // For each edge edgeId saves the edge going in the opposite direction with the same length if it exists
    protected int[] reverseMap;



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

        this.xPos = new int[nodeCount];
        this.yPos = new int[nodeCount];

        this.rank = new int[nodeCount];

        this.src = new int[edgeCount];
        this.trgt = new int[edgeCount];
        this.dist = new int[edgeCount];
        this.euclidianDist = new int[edgeCount];
        this.shortedEdge1 = new int[edgeCount];
        this.shortedEdge2 = new int[edgeCount];

        // Use the simple DumbNN as searcher per default
        // this one needs no setup time but is slow
        this.searcher = new DumbNN(this);
    }

    /**
     * Get the internal BoundingBoxPriorityTree constructed over the XY coordinates
     *
     * @return
     */
    public BBoxPriorityTree getXYBBoxPriorityTree() {
        return bboxXYTree;
    }

    /**
     * Get the internal BoundingBoxPriorityTree constructed over the lat, lon geo coordinates
     *
     * @return
     */
    public BBoxPriorityTree getLatLonBBoxSearchTree() {
        return bboxLatLonTree;
    }



    /**
     * Regenerates the offset arrays from the current edge arrays
     */
    public final void setup() {
        checkAndSortNodesByRank();
        checkAndSortOutEdges();
        mapAndSortInEdges();
        generateOffsets();
        computeXYCoords();
        computeReverseMap();
        this.bboxXYTree = new BBoxPriorityTree(xPos, yPos, rank, false);
        // TODO need to fix behavior at merdian/poles
        this.bboxLatLonTree = new BBoxPriorityTree(lon, lat, rank, true);
        this.searcher = this.bboxLatLonTree;
    }

    private int getXYDistance(double x1, double y1, double x2, double y2) {
        double lon1 =  (x1 / 180 * Math.PI);
        double lon2 = (x2 / 180 * Math.PI);
        double lat1 = (y1 / 180 * Math.PI);
        double lat2 = (y2 / 180 * Math.PI);
        int dist = (int) (1000 * 6378 *Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1)));
        return dist;
    }

    public double lon2x(double lat) {
        return (180.0/Math.PI * Math.log(Math.tan(Math.PI/4+lat*(Math.PI/180.0)*0.5)));
    }

    private void computeXYCoords() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        int tmpRank = 0;

        // Find min/max
        for (int i = 0; i < nodeCount; ++i) {
            int x = (int) lon2x(this.lon[i] / 10_000_000.0);
            int y = (int) (this.lat[i] / 10_000_000.0);
            tmpRank = this.rank[i];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxRank = Math.max(maxRank, tmpRank);
        }

        // Compute the coordinates
        int boundWidth = getXYDistance(minX, minY, maxX, minY);
        int boundHeight = getXYDistance(minX, minY, minX, maxY);


        int minXfinal = Integer.MAX_VALUE;
        int minYfinal = Integer.MAX_VALUE;
        int maxXfinal = Integer.MIN_VALUE;
        int maxYfinal = Integer.MIN_VALUE;

        for (int i = 0; i < nodeCount; ++i) {
            double x = lon2x(this.lon[i] / 10_000_000.0);
            double y = (this.lat[i] / 10_000_000.0);
            int xPos = getXYDistance(x, minY, minX, minY);
            int yPos = boundHeight - getXYDistance(minX, y, minX, minY);
            minXfinal = Math.min(minXfinal, xPos);
            minYfinal = Math.min(minYfinal, yPos);
            maxXfinal = Math.max(maxXfinal, xPos);
            maxYfinal = Math.max(maxYfinal, yPos);
            this.xPos[i] = xPos;
            this.yPos[i] = yPos;
        }
        this.bbox = new BoundingBox(minXfinal, minYfinal, maxXfinal-minXfinal, maxYfinal-minYfinal);
        log.log(Level.INFO, "Bounding box: " + bbox.x + ", " + bbox.y + " - " + bbox.width + ", " + bbox.height);

    }

    /**
     * Compute the reverse map
     */
    private void computeReverseMap() {
        reverseMap = new int[edgeCount];
        Arrays.fill(reverseMap, -1);
        for (int edgeId = 0; edgeId < edgeCount; ++edgeId) {
            if(reverseMap[edgeId] >= 0) {
                continue;
            }
            int trgt = this.getTarget(edgeId);
            int src = this.getSource(edgeId);
            for (int edgeNum  = 0; edgeNum < this.getOutEdgeCount(trgt); edgeNum++){
                int otherEdgeId = this.getOutEdgeId(trgt, edgeNum);
                if(src == this.getTarget(otherEdgeId) && this.getDist(edgeId) == this.getDist(otherEdgeId)) {
                    reverseMap[edgeId] = otherEdgeId;
                    reverseMap[otherEdgeId] = edgeId;
                    break;
                }
            }
        }
    }

    /**
     * Generate mapping for InEdges by sorting
     */
    private void mapAndSortInEdges() {
        this.mappingInToOut = new int[edgeCount];
        // Set mapping to initial values (0,1,2,3..)
        for (int i = 0; i < edgeCount; i++) {
            this.mappingInToOut[i] = i;
        }
        Sorter.sort(new MappingSortAdapter(this));
    }

    /**
     * Compute offset arrays
     */
    private void generateOffsets() {
        this.offsetIn = new int[nodeCount + 1];
        this.offsetOut = new int[nodeCount + 1];

        for (int i = 0; i < edgeCount; ++i) {
            offsetOut[src[i]]++;
            offsetIn[trgt[mappingInToOut[i]]]++;
        }
        int outSum = 0;
        int inSum = 0;
        for (int i = 0; i < nodeCount; ++i) {
            int oldOutSum = outSum;
            int oldInSum = inSum;
            outSum += offsetOut[i];
            inSum += offsetIn[i];
            offsetOut[i] = oldOutSum;
            offsetIn[i] = oldInSum;
        }
        offsetOut[nodeCount] = outSum;
        offsetIn[nodeCount] = inSum;
    }

    /**
     * Nodes must be ordered by rank descending, this function checks just that
     *
     * @return
     */
    private boolean nodesSorted() {
        boolean sorted = true;
        for (int i = 0; i < nodeCount-1; ++i) {
            if (rank[i] < rank[i+1]) {
                sorted = false;
                break;
            }
        }
        return sorted;
    }

    /**
     * Sort Nodes by their rank, this is needed so that the CORE has  node ids [0,1,..,k]
     */
    private void checkAndSortNodesByRank() {
        boolean sorted = nodesSorted();
        if (!sorted) {
            // TODO: Find less memory hungry method to map shorted edges to new ids
            int[] order = new int[nodeCount];
            log.log(Level.INFO, "Nodes are not sorted, resort");
            for (int i = 0; i < nodeCount; i++) {
                order[i] = i;
            }
            Sorter.sort(new NodeSortAdapter(this, order));

            int[] newIds = new int[nodeCount];
            for (int i = 0; i < newIds.length; i++) {
                newIds[order[i]] = i;
            }

            // Fixup edges
            for (int i = 0; i < edgeCount; i++) {
                this.src[i] = newIds[src[i]];
                this.trgt[i] = newIds[trgt[i]];
            }

            // check if it's true
            sorted = nodesSorted();
            log.log(Level.INFO, "nodes sorted: " + sorted);
        }
    }
    /*
    * Check if out edges are sorted correctly and sort them if they aren't
     */
    private void checkAndSortOutEdges() {
        // Check if out edges are correctly sorted and
        // resort if not, the ChConstructor should
        // produce already sorted graphs so we check it first
        boolean sorted = outEdgesSorted();
        if (!sorted) {
            // TODO: Find less memory hungry method to map shorted edges to new ids
            int[] order = new int[edgeCount];
            log.log(Level.INFO, "Out edges are not sorted, resort");
            for (int i = 0; i < edgeCount; i++) {
                order[i] = i;
            }
            Sorter.sort(new OutEdgeSortAdapter(this, order));

            int[] newIds = new int[edgeCount];
            for (int i = 0; i < newIds.length; i++) {
                newIds[order[i]] = i;
            }

            // Fixup shorted edges
            for (int i = 0; i < edgeCount; i++) {
                int oldId1 = this.shortedEdge1[i];
                int oldId2 = this.shortedEdge2[i];
                if (oldId1 >= 0) {
                    this.shortedEdge1[i] = newIds[oldId1];
                }
                if (oldId2 >= 0) {
                    this.shortedEdge2[i] = newIds[oldId2];
                }
            }
            // check if it's true
            sorted = outEdgesSorted();
            log.log(Level.INFO, "out edges sorted: " + sorted);
        }
    }

    /**
     * Checks if the out edges are sorted by ascending rank
     * just as chconstructor does
     *
     * @return
     */
    private boolean outEdgesSorted() {
        boolean sorted = true;
        for (int i = 0; i < this.src.length - 1; i++) {
            int srcId1 = this.src[i];
            int srcId2 = this.src[i + 1];
            if (srcId1 > srcId2 || (srcId1 == srcId2 && this.rank[this.trgt[i + 1]] < this.rank[this.trgt[i]])) {
                sorted = false;
                break;
            }
        }
        return sorted;
    }

    /**
     * Get the maximum rank value in the graph
     * @return
     */
    public int getMaxRank() {
        return maxRank;
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
     * Gets the reverse edge if it exists or -1 if it doesn't. A reverse edge
     * is an edge going from this edges source to this edges target and has the same length
     * @param edgeId
     * @return
     */
    public int getReverseEdgeId(int edgeId) {
        return reverseMap[edgeId];
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
    public final int getHeight(int nodeId) {
        return height[nodeId];
    }

    /**
     * Gets the latitude of the node with the given nodeId
     *
     * @param nodeId
     * @return int (degrees*10^7)
     */
    public final int getLat(int nodeId) {
        return lat[nodeId];
    }

    /**
     * Gets the longitude of the node with the given nodeId
     *
     * @param nodeId
     * @return int (degrees*10^7)
     */
    public final int getLon(int nodeId) {
        return lon[nodeId];
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
     * Get the smallest BoundingBox containing the entire graph
     * @return
     */
    public BoundingBox getBbox() {return bbox;}

    /**
     * Get the projected x position
     *
     * @param nodeId
     * @return
     */
    public final int getXPos(int nodeId) {
        return xPos[nodeId];
    }

    /**
     * Get the projected y position
     *
     * @param nodeId
     * @return
     */
    public final int getYPos(int nodeId) {
        return yPos[nodeId];
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

    /**
     * Set the NNSearcher used by this GraphRep
     *
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
    public final void setNodeData(int id, int lat, int lon, int height) {
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
    public final void setRank(int id, int rank) {
        this.rank[id] = rank;
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
     * Set the data filed of the edge given by it's id
     *
     * @param index
     * @param source
     * @param target
     * @param dist
     */
    public final void setEdgeData(int index, int source, int target, int dist, int euclidianDist) {
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
     * @param shortedEdge2
     * @param shortedEdge1
     */
    public final void setShortcutData(int id, int shortedEdge1, int shortedEdge2) {
        this.shortedEdge1[id] = shortedEdge1;
        this.shortedEdge2[id] = shortedEdge2;
    }


    /**
     * Sets the offsetOut array to the given array, this method
     * is only used for low level graph loading so setup()
     * can be avoided
     *
     * @param newOffsetOut
     */
    public final void setOffsetOut(int[] newOffsetOut) {
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
     * is only used for low level graph loading so setup()
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
     * is only used for low level graph loading so setup()
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


}
