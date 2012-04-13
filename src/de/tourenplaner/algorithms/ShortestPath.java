package de.tourenplaner.algorithms;

import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.computecore.Way;
import de.tourenplaner.graphrep.GraphRep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 * Provides an implementation of ShortestPath algorithm.
 */
public class ShortestPath extends GraphAlgorithm {

    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");

	// DijkstraStructs used by the ShortestPathCH
	private final DijkstraStructs ds;

    public ShortestPath(GraphRep graph, DijkstraStructs rs) {
		super(graph);
		ds = rs;
	}

	// maybe use http://code.google.com/p/simplelatlng/ instead
	private final double calcDirectDistance(double lat1, double lng1,
			double lat2, double lng2) {
		double earthRadius = 6370.97327862;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = (Math.sin(dLat / 2) * Math.sin(dLat / 2))
				+ (Math.cos(Math.toRadians(lat1))
						* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math
							.sin(dLng / 2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;
		return dist * 1000;
	}

    @Override
    public void compute(ComputeRequest req) throws ComputeException, Exception {
        assert req != null : "We ended up without a request object in run";

        RequestPoints points = req.getPoints();
        // Check if we have enough points to do something useful
        if (points.size() < 2) {
            throw new ComputeException("Not enough points, need at least 2");
        }

        List<Way> resultWays = req.getResultWays();
        int distance = 0;
        try {
            // First let's map the RequestPoints to Ids
            points.setIdsFromGraph(graph);
            // Then compute the multi hop shortest path of them
            distance = shortestPath(points, resultWays, false);
        } catch (IllegalAccessException e) {
            // If this happens there likely is a programming error
            e.printStackTrace();
        }

        // Calculate total time
        int numWays = resultWays.size();
        double totalTime = 0;
        for (int i = 0; i < numWays; i++) {
            totalTime += resultWays.get(i).getTravelTime();
        }

        Map<String, Object> misc = new HashMap<String, Object>(1);
        misc.put("distance", distance);
        misc.put("time", totalTime);
        req.setMisc(misc);
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
     * @throws ComputeException
     * @throws IllegalAccessException
     */
    protected int shortestPath(RequestPoints points, List<Way> resultWays, boolean tour) throws ComputeException, IllegalAccessException {

        int srcId;
        int trgtId = 0;

        int totalDistance = 0;
        int distance = 0;
        double time = 0;

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
                    graph.getNodeLat(srcId) / 10000000.0,
                    (double) graph.getNodeLon(srcId) / 10000000,
                    (double) graph.getNodeLat(trgtId) / 10000000,
                    (double) graph.getNodeLon(trgtId) / 10000000
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
            backtrack(prevEdges, resultWays.get(pointIndex), srcId, trgtId);
            resultWays.get(pointIndex).setTravelTime(dists[trgtId] * graph.travelTimeConstant);
            distance = resultWays.get(pointIndex).getDistance();
            time = resultWays.get(pointIndex).getTravelTime();
            long backtracktime = System.nanoTime();

            // Save the distance to the last point at the target
            // wrap around at tour
            points.getConstraints((pointIndex + 1) % points.size()).put("distToPrev", distance);
            points.getConstraints((pointIndex + 1) % points.size()).put("timeToPrev", time);

            totalDistance += distance;

            log.finer("found sp with dist = " + distance / 1000.0 + " km (direct distance: " + directDistance / 1000.0 +
                    " dist[destid] = " + dists[trgtId] + '\n' +
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
    protected final boolean dijkstraStopAtDest(int[] dists, int[] prevEdges, int srcId, int trgtId )
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
                if (graph.getFirstShortcuttedEdge(edgeId) != -1) {
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
    protected final void backtrack(int[] prevEdges, Way resultWay, int srcId, int trgtId) {
        // Find out how much space to allocate
        int currNode = trgtId;
        int routeElements = 1;

        int length = 0;

        while (currNode != srcId) {
            routeElements++;
            currNode = graph.getSource(prevEdges[currNode]);
        }

        // Add them without values we set the values in the next step
        resultWay.addEmptyPoints(routeElements);

        // backtracking here
        currNode = trgtId;
        int prevEdge;
        while (routeElements > 1) {
            prevEdge = prevEdges[currNode];
            length += graph.getEuclidianDist(prevEdge);
            routeElements--;

            resultWay.setPointLat(routeElements, graph.getNodeLat(currNode));
            resultWay.setPointLon(routeElements, graph.getNodeLon(currNode));

            currNode = graph.getSource(prevEdges[currNode]);
        }
        // add source node to the result.
        resultWay.setPointLat(0, graph.getNodeLat(currNode));
        resultWay.setPointLon(0, graph.getNodeLon(currNode));
        resultWay.setDistance(length);
        return;
    }

}
