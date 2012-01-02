package algorithms;

import computecore.ComputeRequest;
import computecore.Points;
import computecore.RequestPoints;
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
        System.out.println("lamda" + lamda);
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
                edgeId = graph.getOutEdgeId(nodeId,i);
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
                //if (tempAltitudeDiff > 0) {
                    edgeAltDiffMultiplied = ((double) tempAltitudeDiff) * (1.0 - lamda);
                    edgeLength = (int) (((double) (graph.getEuclidianDist(edgeId))) * lamda + edgeAltDiffMultiplied);
                //} else {
                //    edgeLength = (int) ((double) ((graph.getEuclidianDist(edgeId)))*lamda);
                //}

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
        int prevNode;
        int currNodeHeight;
        int prevNodeHeight;

        int altitudeDiff = 0;
        while (routeElements > 0) {
            prevNode = graph.getSource(prevEdges[currNode]);
            routeElements--;
            currNodeHeight = graph.getNodeHeight(currNode);
            System.out.println("Knotenhöhe" + currNodeHeight);
            prevNodeHeight = graph.getNodeHeight(prevNode);
            tempAltitudeDiff = currNodeHeight - prevNodeHeight;
            if (tempAltitudeDiff > 0) {
                altitudeDiff += tempAltitudeDiff;
            }
            currNode = graph.getSource(prevEdges[currNode]);
        }
        return altitudeDiff;
    }

    @Override
    public void compute(ComputeRequest req) throws ComputeException {
        assert req != null : "We ended up without a request object in run";
        RequestPoints points = req.getPoints();

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

        int maxAltitudeDifference = (Integer) req.getConstraints().get("maxAltitudeDifference");
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
        int altitudeDiff;
        long length;
        double oldLamda;
        // shortest path's difference of altitude
        altitudeDiff = dijkstra(srcId, trgtId, lamda);
        System.out.println("bad " + altitudeDiff);
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
            System.out.println("good " + altitudeDiff);
            if (altitudeDiff > maxAltitudeDifference) {
                System.err.println(
                        "There is no path from src: " + srcId + " to trgt: " + trgtId + "with constraint" +
                        maxAltitudeDifference
                                  );
                throw new ComputeException("No path found");
            }
            lamdaOfGood = lamda;
            lengthOfGood = dists[trgtId];
            long oldLength = Integer.MAX_VALUE;
            lamda = (lamdaOfGood + lamdaOfBad) / 2.0;
            altitudeDiff = dijkstra(srcId, trgtId, lamda);
            length = dists[trgtId];

            while (oldLength != lengthOfGood && lamda > Double.MIN_VALUE && lamda != lamdaOfBad && lamda != lamdaOfGood) {

                if (altitudeDiff <= maxAltitudeDifference && length < lengthOfGood ) {
                    System.out.println("good");
                    oldLamda = lamdaOfGood;
                    lamdaOfGood = lamda;
                    oldLength = lengthOfGood;
                    lengthOfGood = length;
                } else {
                    System.out.println("bad");
                    oldLamda = lamdaOfBad;
                    lamdaOfBad = lamda;
                }
                System.out.println("oldlength " + oldLength + "length" + lengthOfGood);
                lamda = (lamdaOfGood + lamdaOfBad) / 2.0;
                altitudeDiff = dijkstra(srcId, trgtId, lamda);
                length = dists[trgtId];
                System.out.println("Höhenunterschied " + altitudeDiff + " mit gewichteter Länge " + length + " lamda " + lamda + " lamdagood " + lamdaOfGood + " lamdabad "  + lamdaOfBad + " oldlength " + oldLength + " lengthgood " + lengthOfGood );
            }
            altitudeDiff = dijkstra(srcId, trgtId, lamdaOfGood);
        }
        // Find out how much space to allocate
        int currNode = trgtId;
        int routeElements = 0;

        while (currNode != srcId) {
            routeElements++;
            currNode = graph.getSource(prevEdges[currNode]);
        }
        System.out.println("route Elements" + routeElements);
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
