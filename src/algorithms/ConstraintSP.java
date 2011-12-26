package algorithms;

import computecore.ComputeRequest;
import computecore.Points;
import graphrep.GraphRep;

import java.util.HashMap;
import java.util.Map;

public class ConstraintSP extends GraphAlgorithm {

    private final Heap heap;

    public ConstraintSP(GraphRep graph) {
        super(graph);
        heap = new algorithms.Heap(10000);
        dists = new int[graph.getNodeCount()];
        prevEdges = new int[graph.getNodeCount()];
    }

    // dists in this array are stored with the multiplier applied. They also are
    // rounded and are stored as integers
    private final int[] dists;

    /**
     * edge id
     */
    private final int[] prevEdges;

    private int dijkstra(int srcId, int trgtId, double lamda) throws ComputeException {
        // reset dists
        for (int i = 0; i < dists.length; i++) {
            dists[i] = Integer.MAX_VALUE;
        }
        double edgeAltDiffMultiplied;
        int srcHeight;
        int trgtHeight;
        heap.resetHeap();

        dists[srcId] = 0;
        heap.insert(srcId, dists[srcId]);
        int nodeId = -1;
        int nodeDist;
        int targetNode = 0;
        int tempDist;
        int tempAltitudeDiff;
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
            int edgeLength;
            for (int i = 0; i < graph.getOutEdgeCount(nodeId); i++) {
                // Ignore Shortcuts
                if (graph.getOutFirstShortcuttedEdge(nodeId, i) != -1) {
                    continue;
                }

                targetNode = graph.getOutTarget(nodeId, i);
                srcHeight = graph.getNodeHeight(nodeId);
                trgtHeight = graph.getNodeHeight(targetNode);
                tempAltitudeDiff = trgtHeight - srcHeight;
                // if only positive altitude differences of edges are allowed,
                // negative edges have only euclidian distance.
                if (tempAltitudeDiff < 0) {
                    edgeLength = graph.getOutEuclidianDist(nodeId, i);
                } else {
                    edgeAltDiffMultiplied = tempAltitudeDiff * (1 - lamda);
                    edgeLength = (int) (graph.getOutEuclidianDist(nodeId, i) * lamda + edgeAltDiffMultiplied);
                }

                // without multiplier = shortest path + constraints weights
                tempDist = dists[nodeId] + edgeLength;
                System.out.println(tempDist);

                if (tempDist < dists[targetNode]) {
                    dists[targetNode] = tempDist;
                    prevEdges[targetNode] = graph.getOutEdgeId(nodeId, i);
                    heap.insert(targetNode, dists[targetNode]);
                }
            }
        }
        if (nodeId != trgtId) {
            System.err.println(
                    "There is no path from src: " + srcId + " to trgt: " + trgtId + "Dijkstra does not found the " +
                    "target"
                              );
            throw new ComputeException("No path found");
        }
        int currNode = trgtId;
        int routeElements = 0;

        while (currNode != srcId) {
            routeElements++;
            currNode = graph.getSource(prevEdges[currNode]);
        }
        currNode = trgtId;
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
            if (tempAltitudeDiff >= 0) {
                altitudeDiff = altitudeDiff + tempAltitudeDiff;

            }
            currNode = graph.getSource(prevEdges[currNode]);
        }
        return altitudeDiff;
    }

    @Override
    public void compute(ComputeRequest req) throws ComputeException {
        assert req != null : "We ended up without a request object in run";
        Points points = req.getPoints();

        // Check if we have enough points to do something useful
        if (points.size() < 2 && points.size() > 2) {
            throw new ComputeException(
                    "Not enough points or to much points, need 2"
            );
        }

        Points resultPoints = req.getResulWay();
        int distance = 0;
        int srclat, srclon;
        int destlat, destlon;
        int srcId, trgtId;
        int resultAddIndex = 0;

        int maxAltitudeDifference = (Integer) req.getConstraints().get(
                "maxAltitudeDifference"
                                                                      );

        srclat = points.getPointLat(0);
        srclon = points.getPointLon(0);
        destlat = points.getPointLat(1);
        destlon = points.getPointLon(1);
        srcId = graph.getIdForCoordinates(srclat, srclon);
        trgtId = graph.getIdForCoordinates(destlat, destlon);

        double lamdaOfGood;
        long lengthOfGood;
        double lamdaOfBad;
        double lamda = 1.0;
        int altitudeDiff = 0;
        long length;

        // shortest path's difference of altitude
        altitudeDiff = dijkstra(srcId, trgtId, lamda);
        System.out.println(altitudeDiff);
        // shortest path serves not the constraint
        if (altitudeDiff > maxAltitudeDifference) {
            System.err.println(
                    "There is no shortest path from src: " + srcId + " to trgt: " + trgtId + "with constraint" +
                    maxAltitudeDifference
                              );
            lamdaOfBad = lamda;
            lamda = 0.0;
            // exists there a path that serves the constraint
            altitudeDiff = dijkstra(srcId, trgtId, lamda);
            System.out.println(altitudeDiff);
            if (altitudeDiff > maxAltitudeDifference) {
                System.err.println(
                        "There is no path from src: " + srcId + " to trgt: " + trgtId + "with constraint" +
                        maxAltitudeDifference
                                  );
                throw new ComputeException("No path found");
            }
            lamdaOfGood = lamda;
            lengthOfGood = dists[trgtId];
            long oldLengthOfGood = Integer.MAX_VALUE;
            do {
                lamda = (lamdaOfGood + lamdaOfBad) / 2;
                altitudeDiff = dijkstra(srcId, trgtId, lamda);
                length = dists[trgtId];
                if (altitudeDiff <= maxAltitudeDifference && length < lengthOfGood) {
                    lamdaOfGood = lamda;
                    oldLengthOfGood = lengthOfGood;
                    lengthOfGood = length;
                } else {
                    lamdaOfBad = lamda;
                }
            } while (altitudeDiff <= maxAltitudeDifference && oldLengthOfGood == lengthOfGood);

        }

        // Find out how much space to allocate
        int currNode = trgtId;
        int routeElements = 0;

        while (currNode != srcId) {
            routeElements++;
            currNode = graph.getSource(prevEdges[currNode]);
        }

        System.out.println(
                "path goes over " + routeElements + " nodes" + "and over" + altitudeDiff +
                "meters of altitude Difference"
                          );
        // Add points to the end
        resultAddIndex = resultPoints.size();
        // Add them without values we set the values in the next step
        resultPoints.addEmptyPoints(routeElements);

        // backtracking here
        // Don't read distance from multipliedDist[], because there are
        // distances with
        // regard to the multiplier
        currNode = trgtId;
        while (routeElements > 0) {
            distance += graph.getDist(prevEdges[currNode]);
            routeElements--;

            resultPoints.setPointLat(
                    resultAddIndex + routeElements, graph.getNodeLat(currNode)
                                    );
            resultPoints.setPointLon(
                    resultAddIndex + routeElements, graph.getNodeLon(currNode)
                                    );

            currNode = graph.getSource(prevEdges[currNode]);
        }

        Map<String, Object> misc = new HashMap<String, Object>(1);
        misc.put("distance", distance);
        req.setMisc(misc);

    }
}
