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

package de.tourenplaner.algorithms;

import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.computecore.Way;
import de.tourenplaner.graphrep.GraphRep;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 * Provides an implementation of resource constrained shortest path algorithm.
 */
public class ConstrainedSP extends GraphAlgorithm {
    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");
    private Heap heap;

    // DijkstraStructs used by the ConstrainedShortestPath
    private final DijkstraStructs ds;

    
    public ConstrainedSP(GraphRep graph, DijkstraStructs dijkstraStructs) {
        super(graph);
        ds = dijkstraStructs;
        misc = new HashMap<String, Object>(1);
    }

    // dists in this array are stored with the multiplier applied. They also are
    // rounded and are stored as integers
    private int[] dists;

    /**
     * edge id
     */
    private int[] prevEdges;

    private Map<String, Object> misc;
    @Override
    public void compute(ComputeRequest req) throws ComputeException, Exception {
        misc.clear();

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

        int altitudeDiff = cSP(points, resultWay, maxAltitudeDifference);

        misc.put("distance", resultWay.getDistance());
        misc.put("time", resultWay.getTravelTime());
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
    protected int cSP (RequestPoints points, Way resultWay, int maxAltitudeDifference) throws ComputeException {
        int srclat, srclon;
        int destlat, destlon;
        int srcId, trgtId;
        int distance;

        try {
            heap = ds.borrowHeap();
            dists = ds.borrowDistArray();
            prevEdges = ds.borrowPrevArray();
        } catch (IllegalAccessException e) {
            log.log(Level.SEVERE,"Illegal Access", e);
        }

        double threshold = 1.0 / (double) ((long)graph.getNodeCount() * (long)graph.getNodeCount() * (long)maxAltitudeDifference * (long) maxAltitudeDifference);

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
        // shortest path's difference of altitude
        altitudeDiff = dijkstra(srcId, trgtId, lamda);
        // shortest path serves not the constraint
        if (altitudeDiff > maxAltitudeDifference) {
            log.finer(
                    "There is no shortest path from src: " + srcId + " to trgt: " + trgtId + "with constraint" +
                            maxAltitudeDifference
            );
            lamdaOfBad = lamda;
            lamda = 0.0;
            // exists there a path that serves the constraint
            altitudeDiff = dijkstra(srcId, trgtId, lamda);
            if (altitudeDiff > maxAltitudeDifference) {
                log.finer("There is no path from src: " + srcId + " to trgt: " + trgtId + "with constraint" +
                        maxAltitudeDifference
                );
                misc.put("message", "Not feasible, but found path with smallest altitude difference = " + altitudeDiff + " m");
                ds.returnDistArray(false);
                ds.returnHeap();
                ds.returnPrevArray();
                backtrack(prevEdges, resultWay, srcId, trgtId);
                return  altitudeDiff;


            }
            lamdaOfGood = lamda;
            lamda = (lamdaOfGood + lamdaOfBad) / 2.0;
            altitudeDiff = dijkstra(srcId, trgtId, lamda);

            while (lamdaOfBad - lamdaOfGood >= threshold + 0.000000001 ) {
                if (altitudeDiff <= maxAltitudeDifference) {
                    lamdaOfGood = lamda;
                } else {
                    lamdaOfBad = lamda;
                }
                lamda = (lamdaOfGood + lamdaOfBad) / 2.0;
                altitudeDiff = dijkstra(srcId, trgtId, lamda);
            }
            altitudeDiff = dijkstra(srcId, trgtId, lamdaOfGood);
        }
        log.finer("path goes over " + altitudeDiff + " meters of altitude Difference");
        backtrack(prevEdges, resultWay, srcId, trgtId);

        ds.returnDistArray(false);
        ds.returnHeap();
        ds.returnPrevArray();
        return altitudeDiff;
    }

    /**
     * Backtracks the prevEdges Array and calculates the actual path length
     *  returns the length of the found path in meters
     *
     *
     * @param prevEdges
     * @param resultWay
     * @param srcId
     * @param trgtId
     */
    private void backtrack(int[] prevEdges, Way resultWay, int srcId, int trgtId) {
        int currNode = trgtId;
        int routeElements = 1;
        while (currNode != srcId) {
            routeElements++;
            currNode = graph.getSource(prevEdges[currNode]);
        }
        log.finer("path goes over " + routeElements + " nodes.");

        // Add them without values we set the values in the next step
        resultWay.addEmptyPoints(routeElements);
        int distance = 0;
        int multdistance = 0;
        // backtracking here
        // Don't read distance from multipliedDist[], because there are
        // distances with
        // regard to the multiplier
        // only to the penultimate element (srcId)
        currNode = trgtId;
        int prevEdge;
        while (routeElements > 1) {
            prevEdge = prevEdges[currNode];
            distance += graph.getEuclidianDist(prevEdge);
            multdistance += graph.getDist(prevEdge);
            routeElements--;
            resultWay.setPointLat(routeElements, graph.getNodeLat(currNode));
            resultWay.setPointLon(routeElements, graph.getNodeLon(currNode));
            currNode = graph.getSource(prevEdges[currNode]);
        }
        // add source node to the result.
        resultWay.setPointLat(0, graph.getNodeLat(currNode));
        resultWay.setPointLon(0, graph.getNodeLon(currNode));
        resultWay.setTravelTime(multdistance * graph.travelTimeConstant);
        resultWay.setDistance(distance);
        return;
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
            }
            if (nodeDist > dists[nodeId]) {
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
            log.finer(
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
