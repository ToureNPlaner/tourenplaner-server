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

import com.carrotsearch.hppc.IntArrayDeque;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.computecore.Way;
import de.tourenplaner.graphrep.GraphRep;

import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Logger;

class BDPQElement implements Comparable<BDPQElement> {

    public int key;
    public int value;
    public int queue;

    BDPQElement(int a, int b, int c) {
        key = a;
        value = b;
        queue = c;
    }

    public int compareTo(BDPQElement o) {
        if (key > o.key) return 1;
        else if (key == o.key) return 0;
        else return -1;
    }
}

/**
 * Shortest Path Algorithm taking advantage of Contraction Hierarchies
 *
 * @author Stefan Funke, Niklas Schnelle
 */
public class ShortestPathBDCH extends ShortestPath {

    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");

    int[] distFwd;
    int[] distBwd;
    boolean[] settledFwd;
    boolean[] settledBwd;
    int[] prevEdgesFwd;
    int[] prevEdgesBck;
    PriorityQueue<BDPQElement> myQueue;
    int[] touchedNodes;
    int nofTouchedNodes;
    int nofTouchedEdges;

    // TODO: Find better place
    int bestId;

    // DijkstraStructs used by the ShortestPathCH
    private final DijkstraStructs ds;

    public ShortestPathBDCH(GraphRep graph, DijkstraStructs resourceSharer) {
        super(graph);
        prevEdgesFwd = new int[graph.getNodeCount()];
        prevEdgesBck = new int[graph.getNodeCount()];
        distFwd = new int[graph.getNodeCount()];
        distBwd = new int[graph.getNodeCount()];
        settledFwd = new boolean[graph.getNodeCount()];
        settledBwd = new boolean[graph.getNodeCount()];
        touchedNodes = new int[graph.getNodeCount()];
        for (int i = 0; i < graph.getNodeCount(); i++) {
            distFwd[i] = distBwd[i] = Integer.MAX_VALUE;
            settledFwd[i] = settledBwd[i] = false;
        }
        myQueue = new PriorityQueue<BDPQElement>();
        nofTouchedNodes = 0;
        ds = resourceSharer;
    }

    /**
     * Labels a forward edge
     *
     * @param trgtId    target node
     * @param d
     */
    void labelFwd(int trgtId, int edgeId, int d) {
        if ((distFwd[trgtId] == Integer.MAX_VALUE) && (distBwd[trgtId] == Integer.MAX_VALUE)) {
            touchedNodes[nofTouchedNodes++] = trgtId;
        }
        distFwd[trgtId] = d;
        prevEdgesFwd[trgtId] = edgeId;
        myQueue.add(new BDPQElement(d, trgtId, 0));
    }

    /**
     * Labels a backward edge
     *
     * @param srcId
     * @param d
     */
    void labelBwd(int srcId, int trgtId, int edgeId, int d) {
        if ((distFwd[srcId] == Integer.MAX_VALUE) && (distBwd[srcId] == Integer.MAX_VALUE)) {
            touchedNodes[nofTouchedNodes++] = srcId;
        }
        distBwd[srcId] = d;
        prevEdgesBck[srcId] = edgeId;
        myQueue.add(new BDPQElement(d, srcId, 1));
    }

    /**
     * Performs the Dijkstra Search
     *
     * @param srcId
     * @param destId
     * @return
     * @throws IllegalAccessException
     */
    public final int bddijkstra(int srcId, int destId) throws IllegalAccessException {
        // clean up previously touched nodes
        for (int i = 0; i < nofTouchedNodes; i++) {
            distFwd[touchedNodes[i]] = distBwd[touchedNodes[i]] = Integer.MAX_VALUE;
            settledFwd[touchedNodes[i]] = settledBwd[touchedNodes[i]] = false;
        }
        nofTouchedNodes = 0;
        myQueue.clear();
        // start with src and dest
        touchedNodes[nofTouchedNodes++] = srcId;
        distFwd[srcId] = 0;
        myQueue.add(new BDPQElement(0, srcId, 0));

        touchedNodes[nofTouchedNodes++] = destId;
        distBwd[destId] = 0;
        myQueue.add(new BDPQElement(0, destId, 1));

        int bestDist = Integer.MAX_VALUE;

        nofTouchedEdges = 0;
        while ((!myQueue.isEmpty())) {
            //System.err.print(".");
            BDPQElement cur = myQueue.remove();
            int cur_dist = cur.key;
            int cur_node = cur.value;
            int cur_side = cur.queue;

            if (cur_dist > bestDist)
                break;


            if (cur_side == 0)    // we are in forward search
            {
                if (cur_dist == distFwd[cur_node]) {
                    settledFwd[cur_node] = true;

                    boolean stalled = false;

                    // check for stalling (if there is a node tmp_node (ABOVE) and an edge (tmp_node,cur_node)
                    // which sum to a smaller distance (!)
                    for (int j = 0; j < graph.getInEdgeCount(cur_node); j++) {
                        int tmp_edge = graph.getInEdgeId(cur_node, j);
                        int tmp_wgt = graph.getDist(tmp_edge);
                        int tmp_node = graph.getSource(tmp_edge);
                        if (distFwd[cur_node] - tmp_wgt > distFwd[tmp_node]) {
                            stalled = true;
                            break;
                        }
                        if (graph.getRank(tmp_node) < graph.getRank(cur_node)) break; // sorted by source rank descending
                    }

                    if ((settledBwd[cur_node]) && (distFwd[cur_node] + distBwd[cur_node] < bestDist)){
                        bestDist = distFwd[cur_node] + distBwd[cur_node];
                        bestId = cur_node;
                    }

                    if (!stalled) for (int i = graph.getOutEdgeCount(cur_node) - 1; i >= 0; i--) {
                        int cur_edge = graph.getOutEdgeId(cur_node, i);
                        int cur_trg = graph.getTarget(cur_edge);
                        int cur_weight = graph.getDist(cur_edge);
                        if (graph.getRank(cur_trg) >= graph.getRank(cur_node)) nofTouchedEdges++; // sorted by target rank ascending
                        else break;
                        // correct rank order is checked above
                        if (distFwd[cur_trg] > cur_dist + cur_weight) {
                            labelFwd(cur_trg, cur_edge, cur_dist + cur_weight);
                        }
                    }
                }
            } else    // we are in backward search
            {
                if (cur_dist == distBwd[cur_node]) {
                    settledBwd[cur_node] = true;
                    boolean stalled = false;

                    // check for stalling: if there is a node ABOVE cur_node ...
                    for (int j = graph.getOutEdgeCount(cur_node) - 1; j >= 0; j--) {
                        int tmp_edge = graph.getOutEdgeId(cur_node, j);
                        int tmp_wgt = graph.getDist(tmp_edge);
                        int tmp_node = graph.getTarget(tmp_edge);
                        if (distBwd[cur_node] - tmp_wgt > distBwd[tmp_node]) {
                            stalled = true;
                            break;
                        }
                        if (graph.getRank(cur_node) > graph.getRank(tmp_node)) break; //sorted by target rank ascending
                    }


                    if ((settledFwd[cur_node]) && (distFwd[cur_node] + distBwd[cur_node] < bestDist)){
                        bestDist = distFwd[cur_node] + distBwd[cur_node];
                        bestId = cur_node;
                    }

                    if (!stalled) for (int i = 0; i < graph.getInEdgeCount(cur_node); i++) {
                        int cur_edge = graph.getInEdgeId(cur_node, i);
                        int cur_src = graph.getSource(cur_edge);
                        int cur_weight = graph.getDist(cur_edge);
                        if (graph.getRank(cur_src) >= graph.getRank(cur_node)) nofTouchedEdges++; // sorted descending by source rank
                        else break;
                        // correct rank order is checked above
                        if (distBwd[cur_src] > cur_dist + cur_weight) {
                            labelBwd(cur_src, cur_node, cur_edge, cur_dist + cur_weight);
                        }
                    }
                }
            }
        }
        return bestDist;
    }

    /**
     * Backtracks the prevEdges Array and calculates the actual path length
     * returns the length of the found path in meters
     *
     * @param resultWay
     * @param srcId
     * @param destId
     * @throws IllegalAccessException
     */
    public final void backtrack(Way resultWay, int srcId, int destId) throws IllegalAccessException {
        int nodeLat;
        int nodeLon;
        int edgeId;
        int length = 0;
        // backtracking and shortcut unpacking use dequeue as stack
        IntArrayDeque deque = ds.borrowDeque();

        // Add the edges to our unpacking stack we
        // first from bestId to src
        int currNode = bestId;
        int shortedEdge1, shortedEdge2;

        while (currNode != srcId) {
            edgeId = prevEdgesFwd[currNode];
            deque.addFirst(edgeId);
            currNode = graph.getSource(edgeId);
        }

        // now from bestId to destId
        currNode = bestId;
        while (currNode != destId) {
            edgeId = prevEdgesBck[currNode];
            deque.addLast(edgeId);
            currNode = graph.getTarget(edgeId);
        }

        // Unpack shortcuts "recursively"
        log.finer("Start backtrack with " + deque.size() + " edges");
        while (!deque.isEmpty()) {
            // Get the top edge and check if it's a shortcut that needs
            // further
            // unpacking
            edgeId = deque.removeFirst();
            shortedEdge1 = graph.getFirstShortcuttedEdge(edgeId);
            if (shortedEdge1 > 0) {
                // We have a shortcut unpack it
                shortedEdge2 = graph.getSecondShortcuttedEdge(edgeId);
                deque.addFirst(shortedEdge2);
                deque.addFirst(shortedEdge1);
            } else {
                // No shortcut remember it
                currNode = graph.getSource(edgeId);
                nodeLat = graph.getLat(currNode);
                nodeLon = graph.getLon(currNode);
                resultWay.addPoint(nodeLat, nodeLon);
                length += graph.getEuclidianDist(edgeId);
            }
        }
        // Add destination node
        nodeLat = graph.getLat(destId);
        nodeLon = graph.getLon(destId);
        resultWay.addPoint(nodeLat, nodeLon);

        resultWay.setDistance(length);
        ds.returnDeque();
        return;
    }

    /**
     * Computes the shortest path over points[0] -> points[1] -> points[2]...
     * and stores all points on the path in resultWays
     *
     * @param points     The points the route should span, the id's must have been set
     * @param resultWays
     * @param tour       Specifies whether this is a tour and points[n]->points[0]
     * @return
     * @throws IllegalAccessException
     * @throws Exception
     */
    @Override
    public int shortestPath(RequestPoints points, List<Way> resultWays, boolean tour) throws ComputeException, IllegalAccessException {

        int srcId = 0;
        int destId = 0;

        int distance = 0;
        int totalDistance = 0;
        // in meters
        double directDistance = 0.0;

        for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {

            // New Dijkstra need to reset
            long starttime = System.nanoTime();
            srcId = points.getPointId(pointIndex);
            if (pointIndex < points.size() - 1) {
                destId = points.getPointId(pointIndex + 1);
            } else if (tour) {
                destId = points.getPointId(0);
            } else {
                break;
            }

            //New point -> new subway
            resultWays.add(new Way());

            // get data structures used by Dijkstra

            directDistance += calcDirectDistance(graph.getLat(srcId) / 10000000.0, (double) graph.getLon(srcId) / 10000000, (double) graph.getLat(destId) / 10000000, (double) graph.getLon(destId) / 10000000);


            // Run Dijkstra stopping when destId is removed from the pq
            int bestDist = bddijkstra(srcId, destId);
            long dijkstratime = System.nanoTime();
            if (bestDist == Integer.MAX_VALUE){
                log.info("There is no path from src to trgt (" + srcId + " to " + destId + ")");
                throw new ComputeException("No Path found");
            }

            // Backtrack to get the actual path
            backtrack(resultWays.get(pointIndex), srcId, destId);
            resultWays.get(pointIndex).setTravelTime(bestDist * graph.travelTimeConstant);
            distance = resultWays.get(pointIndex).getDistance();
            totalDistance += distance;

            long backtracktime = System.nanoTime();
            log.info("found sp with dist = " + resultWays.get(pointIndex).getDistance() / 1000.0 + " km (direct distance: " + directDistance / 1000.0  + "\n"+
                    "Dijkstra: " + (dijkstratime - starttime) / 1000000.0 + " ms\n" + "Backtracking: " + (backtracktime - dijkstratime) / 1000000.0 + " ms\n" +
                    "Touched Nodes/Edges "+nofTouchedNodes+"/"+nofTouchedEdges);

            // Save the distance to the last point at the target
            // wrap around at tour
            points.getConstraints((pointIndex + 1) % points.size()).put("distToPrev", distance);
            points.getConstraints((pointIndex + 1) % points.size()).put("timeToPrev", resultWays.get(pointIndex).getTravelTime());
        }

        return totalDistance;
    }

}