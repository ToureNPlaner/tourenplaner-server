/*
 * Copyright 2013 ToureNPlaner
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

package de.tourenplaner.algorithms.shortestpath;

import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.DijkstraStructs;
import de.tourenplaner.algorithms.Heap;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.computecore.Way;
import de.tourenplaner.graphrep.GraphRep;

import java.util.List;

/**
 * Provides an implementation of ShortestPath algorithm without CH speedup
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ShortestPathNoCH extends ShortestPath {

    // DijkstraStructs used by the ShortestPathCH
    protected DijkstraStructs ds;

    public ShortestPathNoCH(GraphRep graph, DijkstraStructs rs) {
        super(graph);
        ds = rs;
    }

    /**
     *
     * Computes the shortest path over points[0] -> points[1] -> points[2]...
     * and stores all points on the path in resultWay
     *
     * @param points
     * @param resultWays
     * @param tour
     * @return
     * @throws de.tourenplaner.algorithms.ComputeException
     * @throws IllegalAccessException
     */
    @Override
    public int shortestPath(RequestPoints points, List<Way> resultWays, boolean tour) throws ComputeException, IllegalAccessException {

        int srcId;
        int trgtId;

        int totalDistance = 0;
        int distance;
        double time;

        // in meters
        double directDistance = 0.0;

        for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
            // New Dijkstra need to reset
            long starttime = System.nanoTime();
            srcId = points.getPointId(pointIndex);
            if (pointIndex < points.size() - 1) {
                trgtId = points.getPointId(pointIndex + 1);
            } else if (tour) {
                trgtId = points.getPointId(0);
            } else {
                break;
            }

            // New Point -> new subway
            resultWays.add(new Way());

            // get data structures used by Dijkstra
            int[] dists = ds.borrowDistArray();
            int[] prevEdges = ds.borrowPrevArray();

            directDistance += calcDirectDistance(
                    graph.getLat(srcId) / 10000000.0,
                    (double) graph.getLon(srcId) / 10000000,
                    (double) graph.getLat(trgtId) / 10000000,
                    (double) graph.getLon(trgtId) / 10000000
            );


            // Run Dijkstra stopping when trgtId is removed from the pq
            boolean found = dijkstraStopAtDest(dists, prevEdges, srcId, trgtId);
            long dijkstratime = System.nanoTime();

            if (!found) {
                // Return/Reset the data structures
                ds.returnDistArray(false);
                ds.returnPrevArray();
                log.info("There is no path from src to trgt (" + srcId + " to " + trgtId + ')');
                throw new ComputeException("No Path found");
            }
            // Backtrack to get the actual path
            Way resultWay = resultWays.get(pointIndex);
            backtrack(prevEdges, resultWay, srcId, trgtId);
            resultWay.setTravelTime(dists[trgtId] * graph.travelTimeConstant);
            distance = resultWay.getDistance();
            time = resultWay.getTravelTime();
            long backtracktime = System.nanoTime();

            // Save the distance to the last point at the target
            // wrap around at tour
            points.getConstraints((pointIndex + 1) % points.size()).put("distToPrev", distance);
            points.getConstraints((pointIndex + 1) % points.size()).put("timeToPrev", time);

            totalDistance += distance;

            log.info("found sp with dist = " + distance / 1000.0 + " km (in meters: " + distance + " direct distance: " + directDistance / 1000.0 +
                    " dist[trgtId] = " + dists[trgtId] + '\n' +
                    (dijkstratime - starttime) / 1000000.0 + " ms\n" +
                    "Backtracking: " + (backtracktime - dijkstratime) / 1000000.0 + " ms");

            // Return/Reset the data structures
            ds.returnDistArray(false);
            ds.returnPrevArray();
        }

        return totalDistance;
    }

    /**
     * Performs the Dijkstra Search on euclidian dists stopping when
     * the destination point is removed from the pq
     *
     * @param dists
     * @param prevEdges
     * @param srcId
     * @param trgtId
     * @return
     * @throws IllegalAccessException
     */
    public final boolean dijkstraStopAtDest(int[] dists, int[] prevEdges, int srcId, int trgtId)
            throws IllegalAccessException {

        dists[srcId] = 0;
        Heap heap = ds.borrowHeap();
        heap.insert(srcId, dists[srcId]);

        int nodeDist;
        int edgeId;
        int tempDist;
        int targetNode;
        int nodeId = srcId;
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
            int edgeCount = graph.getOutEdgeCount(nodeId);
            for (int i = 0; i < edgeCount; i++) {
                edgeId = graph.getOutEdgeId(nodeId, i);
                // Ignore Shortcuts
                if (graph.getFirstShortcuttedEdge(edgeId) >= 0) {
                    continue;
                }
                targetNode = graph.getTarget(edgeId);

                // with multiplier = shortest path
                tempDist = dists[nodeId] + graph.getDist(edgeId);

                if (tempDist < dists[targetNode]) {
                    dists[targetNode] = tempDist;
                    prevEdges[targetNode] = edgeId;
                    heap.insert(targetNode, dists[targetNode]);
                }
            }
        }
        ds.returnHeap();
        return nodeId == trgtId;
    }

    /**
     *  Backtracks the prevEdges Array and calculates the actual path length
     *  returns the length of the found path in meters
     *
     *
     * @param prevEdges
     * @param resultWay
     * @param srcId
     * @param trgtId
     * @throws IllegalAccessException
     */
    public final void backtrack(int[] prevEdges, Way resultWay, int srcId, int trgtId) {
        // Find out how much space to allocate, 1 for source that we always add
        int routeElements = 1;

        int length = 0;
        int currNode = trgtId;
        while (currNode != srcId) {
            routeElements++;
            int prevEdge = prevEdges[currNode];
            length += graph.getEuclidianDist(prevEdge);
            currNode = graph.getSource(prevEdge);
        }

        // Add them without values we set the values in the next step
        resultWay.addEmptyPoints(routeElements);

        // backtracking here
        currNode = trgtId;
        while (routeElements > 1) {
            routeElements--;
            resultWay.setPointLat(routeElements, graph.getLat(currNode));
            resultWay.setPointLon(routeElements, graph.getLon(currNode));
            currNode = graph.getSource(prevEdges[currNode]);
        }
        // add source node to the result.
        resultWay.setPointLat(0, graph.getLat(currNode));
        resultWay.setPointLon(0, graph.getLon(currNode));
        resultWay.setDistance(length);
        return;
    }
}
