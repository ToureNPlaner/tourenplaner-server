package algorithms;

import computecore.ComputeRequest;
import computecore.RequestPoints;
import computecore.Way;
import graphrep.GraphRep;

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

    private static Logger log = Logger.getLogger("algorithms");

	// DijkstraStructs used by the ShortestPathCH
	private final DijkstraStructs ds;

	double directDistance;

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
    public void compute(ComputeRequest req) throws ComputeException {
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

        Map<String, Object> misc = new HashMap<String, Object>(1);
        misc.put("distance", distance);
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

        int srcId = 0;
        int trgtId = 0;

        int oldDistance = 0;
        int distance = 0;

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
                // Don't forget to add destination to prev. way (trgtId is still the last one)
                resultWays.get(pointIndex-1).addPoint(graph.getNodeLat(trgtId), graph.getNodeLon(trgtId));
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
                log.info("There is no path from src to trgt (" + srcId + " to " + trgtId + ")");
                throw new ComputeException("No Path found");
            }
            // Backtrack to get the actual path
            distance += backtrack(dists, prevEdges, resultWays.get(pointIndex), srcId, trgtId);

            long backtracktime = System.nanoTime();

            // Save the distance to the last point at the target
            // wrap around at tour
            points.getConstraints((pointIndex + 1) % points.size()).put("distToPrev", distance - oldDistance);

            oldDistance = distance;

            log.fine(
                    "found sp with dist = " + distance / 1000.0 + " km (direct distance: " + directDistance / 1000.0 +
                    " dist[destid] = " + dists[trgtId] + "\n" +
                    (dijkstratime - starttime) / 1000000.0 + " ms\n" +
                    "Backtracking: " + (backtracktime - dijkstratime) / 1000000.0 + " ms"
                    );

            // Return/Reset the data structures
            ds.returnDistArray(false);
            ds.returnPrevArray();
        }

        return distance;
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
     * @param dists
     * @param prevEdges
     * @param resultWay
     * @param srcId
     * @param trgtId
     * @return
     * @throws IllegalAccessException
     */
    protected final int backtrack(int[] dists, int[] prevEdges, Way resultWay, int srcId, int trgtId) throws
                                                                                                            IllegalAccessException {
        // Find out how much space to allocate
        int currNode = trgtId;
        int routeElements = 1;

        int length = 0;

        while (currNode != srcId) {
            routeElements++;
            currNode = graph.getSource(prevEdges[currNode]);
        }

        // Add points to the end
        int resultAddIndex = resultWay.size();
        // Add them without values we set the values in the next step
        resultWay.addEmptyPoints(routeElements);

        // backtracking here
        // Don't read distance from multipliedDist[], because there are
        // distances with
        // regard to the multiplier
        currNode = trgtId;
        while (routeElements > 0) {
            length += graph.getDist(prevEdges[currNode]);
            routeElements--;

            resultWay.setPointLat(resultAddIndex + routeElements, graph.getNodeLat(currNode));
            resultWay.setPointLon(resultAddIndex + routeElements, graph.getNodeLon(currNode));

            currNode = graph.getSource(prevEdges[currNode]);
        }
        return length;
    }

}
