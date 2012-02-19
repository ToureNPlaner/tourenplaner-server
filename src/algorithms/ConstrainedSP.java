package algorithms;

import computecore.ComputeRequest;
import computecore.Way;
import computecore.RequestPoints;
import graphrep.GraphRep;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 * Provides an implementation of resource constrained shortest path algorithm.
 */
public class ConstrainedSP extends GraphAlgorithm {
    private static Logger log = Logger.getLogger("algorithms");
    private Heap heap;

    // DijkstraStructs used by the ConstrainedShortestPath
    private final DijkstraStructs ds;

    
    public ConstrainedSP(GraphRep graph, DijkstraStructs dijkstraStructs) {
        super(graph);
        ds = dijkstraStructs;
    }

    // dists in this array are stored with the multiplier applied. They also are
    // rounded and are stored as integers
    private int[] dists;

    /**
     * edge id
     */
    private int[] prevEdges;


    @Override
    public void compute(ComputeRequest req) throws ComputeException {


        assert req != null : "We ended up without a request object in run";
        RequestPoints points = req.getPoints();

        // Check if we have enough points to do something useful
        if (points.size() != 2) {
            throw new ComputeException("Not enough points or too much points, need 2");
        }
        // We can only do 1-1 ways so just create and use one Way
        req.getResultWays().add(new Way());
        Way resultWay = req.getResultWays().get(0);

        // Check for Constraint
        int maxAltitudeDifference;

        if (req.getConstraints() == null || req.getConstraints().get("maxAltitudeDifference") == null){
            throw new ComputeException("Missing Maximum Altitude Difference constraint");
        }
        try {
            maxAltitudeDifference = ((Number) req.getConstraints().get("maxAltitudeDifference")).intValue();
        } catch (ClassCastException e){
            throw new ComputeException("Couldn't read Maximum Altitude Difference, wrong type: " + e.getMessage());
        }

        int[] result = cSP(points, resultWay, maxAltitudeDifference);
        int altitudeDiff = result[0];
        int distance = result[1];
        
        Map<String, Object> misc = new HashMap<String, Object>(1);
        misc.put("distance", distance);
        misc.put("altitude", altitudeDiff);
        req.setMisc(misc);


    }

    /**
     *  calculates resource constrained shortest path
     *
     * @param points
     * @param resultWay
     * @param maxAltitudeDifference
     * @return
     * @throws ComputeException
     */
    protected int[] cSP (RequestPoints points, Way resultWay, int maxAltitudeDifference) throws ComputeException {
        int srclat, srclon;
        int destlat, destlon;
        int srcId, trgtId;
        int resultAddIndex = 0;


        try {
            heap = ds.borrowHeap();
            dists = ds.borrowDistArray();
            prevEdges = ds.borrowPrevArray();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        double threshold = 1.0 / (double) ((long)graph.getNodeCount() * (long)graph.getNodeCount() * (long)maxAltitudeDifference + (long) maxAltitudeDifference);

        srclat = points.getPointLat(0);
        srclon = points.getPointLon(0);
        destlat = points.getPointLat(1);
        destlon = points.getPointLon(1);
        srcId = graph.getIdForCoordinates(srclat, srclon);
        trgtId = graph.getIdForCoordinates(destlat, destlon);

        double lamdaOfGood;
        double lamdaOfBad;
        double lamda = 1.0;
        int altitudeDiff;
        double oldLamda;
        // shortest path's difference of altitude
        altitudeDiff = dijkstra(srcId, trgtId, lamda);
        // shortest path serves not the constraint
        if (altitudeDiff > maxAltitudeDifference) {
            log.fine(
                    "There is no shortest path from src: " + srcId + " to trgt: " + trgtId + "with constraint" +
                            maxAltitudeDifference
            );
            lamdaOfBad = lamda;
            lamda = 0.0;
            // exists there a path that serves the constraint
            altitudeDiff = dijkstra(srcId, trgtId, lamda);
            if (altitudeDiff > maxAltitudeDifference) {
                log.fine("There is no path from src: " + srcId + " to trgt: " + trgtId + "with constraint" +
                        maxAltitudeDifference
                );
                ds.returnDistArray(false);
                ds.returnHeap();
                ds.returnPrevArray();
                throw new ComputeException("No path found");
            }
            lamdaOfGood = lamda;
            long oldLength = 0;
            lamda = (lamdaOfGood + lamdaOfBad) / 2.0;
            altitudeDiff = dijkstra(srcId, trgtId, lamda);

            while (lamdaOfBad - lamdaOfGood >= threshold ) {
                if (altitudeDiff <= maxAltitudeDifference) {
                    oldLamda = lamdaOfGood;
                    lamdaOfGood = lamda;
                } else {
                    oldLamda = lamdaOfBad;
                    lamdaOfBad = lamda;
                }
                lamda = (lamdaOfGood + lamdaOfBad) / 2.0;
                altitudeDiff = dijkstra(srcId, trgtId, lamda);
            }
            altitudeDiff = dijkstra(srcId, trgtId, lamdaOfGood);
        }
        // Find out how much space to allocate
        int currNode = trgtId;
        int routeElements = 1;

        while (currNode != srcId) {
            routeElements++;
            currNode = graph.getSource(prevEdges[currNode]);
        }
        log.fine(
                "path goes over " + routeElements + " nodes and over " + altitudeDiff +
                        " meters of altitude Difference");
        // Add points to the end
        resultAddIndex = resultWay.size();
        // Add them without values we set the values in the next step
        resultWay.addEmptyPoints(routeElements);
        int distance = 0;
        // backtracking here
        // Don't read distance from multipliedDist[], because there are
        // distances with
        // regard to the multiplier
        // only to the penultimate element (srcId)
        currNode = trgtId;
        while (routeElements > 1) {
            distance += graph.getEuclidianDist(prevEdges[currNode]);
            routeElements--;
            resultWay.setPointLat(resultAddIndex + routeElements, graph.getNodeLat(currNode));
            resultWay.setPointLon(resultAddIndex + routeElements, graph.getNodeLon(currNode));
            currNode = graph.getSource(prevEdges[currNode]);
        }
        // add source node to the result.
        resultWay.setPointLat(resultAddIndex, graph.getNodeLat(currNode));
        resultWay.setPointLon(resultAddIndex, graph.getNodeLon(currNode));

        ds.returnDistArray(false);
        ds.returnHeap();
        ds.returnPrevArray();
        return new int[]{altitudeDiff,distance};
    }
    
    
    /**
     * Performs the Dijkstra Search on edge weights with
     * euclidian distance * lamda + altitude difference * 1-lamda
     * for edges with positive altitude difference. For all other edges the weight will calculated only with:
     * euclidian distance * lamda.
     * It stopps when the target point is removed from the pq
     *
     * @param srcId
     * @param trgtId
     * @param lamda
     * @return
     * @throws ComputeException
     */
    private int dijkstra(int srcId, int trgtId, double lamda) throws ComputeException {
        // reset dists
        for (int i = 0; i < dists.length; i++) {
            dists[i] = Integer.MAX_VALUE;
        }
        dists[srcId] = 0;
        heap.resetHeap();
        heap.insert(srcId, dists[srcId]);

        int nodeId = -1;
        int nodeDist;

        double edgeAltDiffMultiplied;
        int targetNode;
        int srcHeight;
        int trgtHeight;
        int tempDist;
        int tempAltitudeDiff;
        int edgeLength;
        int edgeId;
        DIJKSTRA:
        while (!heap.isEmpty()) {
            nodeId = heap.peekMinId();
            nodeDist = heap.peekMinDist();
            heap.removeMin();
            if (nodeId == trgtId) {
                break DIJKSTRA;
            } else if (nodeDist > dists[nodeId]) {
                continue;
            }
            for (int i = 0; i < graph.getOutEdgeCount(nodeId); i++) {
                edgeId = graph.getOutEdgeId(nodeId, i);
                // Ignore Shortcuts
                if (graph.getFirstShortcuttedEdge(edgeId) != -1) {
                    continue;
                }

                targetNode = graph.getTarget(edgeId);
                srcHeight = graph.getNodeHeight(nodeId);
                trgtHeight = graph.getNodeHeight(targetNode);
                tempAltitudeDiff = trgtHeight - srcHeight;
                // if only positive altitude differences of edges are allowed,
                // negative edges have only euclidian distance.
                if (tempAltitudeDiff > 0) {
                    edgeAltDiffMultiplied = ((double) tempAltitudeDiff) * (1.0 - lamda);
                    edgeLength = (int) (((double) (graph.getEuclidianDist(edgeId))) * lamda + edgeAltDiffMultiplied);
                } else {
                    edgeLength = (int) ((double) ((graph.getEuclidianDist(edgeId))) * lamda);
                }

                // without multiplier = shortest path + constraints weights
                tempDist = dists[nodeId] + edgeLength;

                if (tempDist < dists[targetNode]) {
                    dists[targetNode] = tempDist;
                    prevEdges[targetNode] = edgeId;
                    heap.insert(targetNode, dists[targetNode]);
                }
            }
        }
        if (nodeId != trgtId) {
            log.fine(
                    "There is no path from src: " + srcId + " to trgt: " + trgtId + "Dijkstra did not find the " +
                            "target"
            );
            ds.returnDistArray(false);
            ds.returnHeap();
            ds.returnPrevArray();
            throw new ComputeException("No path found");
        }

        int currNode = trgtId;
        int routeElements = 0;

        while (currNode != srcId) {
            routeElements++;
            currNode = graph.getSource(prevEdges[currNode]);
        }
        currNode = trgtId;
        int prevNode;
        int currNodeHeight;
        int prevNodeHeight;

        int altitudeDiff = 0;
        while (routeElements > 0) {
            prevNode = graph.getSource(prevEdges[currNode]);
            routeElements--;
            currNodeHeight = graph.getNodeHeight(currNode);
            prevNodeHeight = graph.getNodeHeight(prevNode);
            tempAltitudeDiff = currNodeHeight - prevNodeHeight;
            if (tempAltitudeDiff > 0) {
                altitudeDiff += tempAltitudeDiff;
            }
            currNode = graph.getSource(prevEdges[currNode]);
        }
        return altitudeDiff;
    }

}
