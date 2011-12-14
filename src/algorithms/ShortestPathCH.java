/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import graphrep.GraphRep;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayDeque;
import computecore.ComputeRequest;
import computecore.Points;
import computecore.RequestPoints;

public class ShortestPathCH extends GraphAlgorithm {

	// Variables used for debugging/diagnosing
	int bfsNodes = 0;
	int bfsEdges = 0;

	// Heap used as priority queue in Dijkstra
	private final Heap heap;

	// Used to mark nodes with BFS
	private final BitSet marked;
	private final BitSet visited;

	// Dequeue used as fifo and stack
	private final IntArrayDeque deque;

	// dists in this array are stored with the multiplier applied. They also are
	// rounded and are stored as integers
	private final int[] dists;

	// Stores at position i the edge leading to the node i in the shortest path
	// tree
	private final int[] prevEdges;

	public ShortestPathCH(GraphRep graph) {
		super(graph);
		heap = new algorithms.Heap();
		dists = new int[graph.getNodeCount()];
		prevEdges = new int[graph.getNodeCount()];
		marked = new BitSet(graph.getEdgeCount());
		visited = new BitSet(graph.getNodeCount());
		deque = new IntArrayDeque();
	}

	private void reset() {
		// reset dists
		visited.clear();
		marked.clear();

		Arrays.fill(dists, Integer.MAX_VALUE);
		heap.resetHeap();
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
		assert (req != null) : "We ended up without a request object in run";

		RequestPoints points = req.getPoints();
		// Check if we have enough points to do something useful
		if (points.size() < 2) {
			throw new ComputeException("Not enough points, need at least 2");
		}

		Points resultPoints = req.getResulWay();
		int distance = 0;
		distance = shortestPath(points, resultPoints);

		Map<String, Object> misc = new HashMap<String, Object>(1);
		misc.put("distance", distance);
		req.setMisc(misc);
	}

	/**
	 * Marks the G_down edges by doing a BFS from the target node for
	 * consideration !G_down edges are always before G_up edges! That's why we
	 * can break the inner loop early
	 * 
	 * @param targetId
	 */
	public final void bfsMark(int targetId) {
		int edgeId;
		int currNode;
		int sourceNode;
		deque.clear();
		deque.addLast(targetId);
		visited.set(targetId);
		while (!deque.isEmpty()) {
			currNode = deque.removeLast();
			bfsNodes++;
			Inner: for (int i = 0; i < graph.getInEdgeCount(currNode); i++) {
				bfsEdges++;
				edgeId = graph.getInEdgeID(currNode, i);
				sourceNode = graph.getSource(edgeId);
				if (graph.getRank(sourceNode) >= graph.getRank(currNode)) {
					// Mark the edge
					marked.set(edgeId);
					if (!visited.get(sourceNode)) {
						visited.set(sourceNode);
						// Add source for exploration
						deque.addFirst(sourceNode);
					}
				} else {
					break Inner;
				}
			}
		}
	}

	/**
	 * Performs the Dijkstra Search on E_up U E_marked
	 * 
	 * @param srcId
	 * @param destId
	 * @return
	 */
	public final boolean dijkstraOneToOne(int srcId, int destId) {
		dists[srcId] = 0;
		heap.insert(srcId, dists[srcId]);

		int nodeDist;
		int edgeId;
		int tempDist;
		int targetNode;
		int nodeId = srcId;
		DIJKSTRA: while (!heap.isEmpty()) {
			nodeId = heap.peekMinId();
			nodeDist = heap.peekMinDist();
			heap.removeMin();
			if (nodeId == destId) {
				break DIJKSTRA;
			} else if (nodeDist > dists[nodeId]) {
				continue;
			}
			for (int i = 0; i < graph.getOutEdgeCount(nodeId); i++) {
				edgeId = graph.getOutEdgeID(nodeId, i);
				targetNode = graph.getTarget(edgeId);

				// Either marked (by BFS) or G_up edge
				if (marked.get(edgeId)
						|| (graph.getRank(nodeId) <= graph.getRank(targetNode))) {

					tempDist = dists[nodeId] + graph.getDist(edgeId);

					if (tempDist < dists[targetNode]) {
						dists[targetNode] = tempDist;

						prevEdges[targetNode] = edgeId;
						heap.insert(targetNode, tempDist);
					}

				}
			}
		}
		return nodeId == destId;
	}

	/**
	 * Backtracks the prevEdges Array and calculates the actual path length
	 * returns the length of the found path in meters
	 * 
	 * @param resultPoints
	 * @param srcId
	 * @param destId
	 */
	public final int backtrack(Points resultPoints, int srcId, int destId) {
		int nodeLat;
		int nodeLon;
		int edgeId;
		int length = 0;
		// backtracking and shortcut unpacking use dequeue as stack
		deque.clear();
		// Add the edges to our unpacking stack we
		// go from destination to start so add at the end of the deque
		int currNode = destId;
		int shortedEdge1, shortedEdge2;

		while (currNode != srcId) {
			edgeId = prevEdges[currNode];
			deque.addFirst(edgeId);
			currNode = graph.getSource(edgeId);
		}

		// Unpack shortcuts "recursively"
		System.out.println("Start backtrack with " + deque.size() + " edges");
		while (!deque.isEmpty()) {
			// Get the top edge and check if it's a shortcut that needs
			// further
			// unpacking
			edgeId = deque.removeFirst();
			shortedEdge1 = graph.getFirstShortcuttedEdge(edgeId);
			if (shortedEdge1 != -1) {
				// We have a shortcut unpack it
				shortedEdge2 = graph.getSecondShortcuttedEdge(edgeId);
				deque.addFirst(shortedEdge2);
				deque.addFirst(shortedEdge1);
			} else {
				// No shortcut remember it
				currNode = graph.getSource(edgeId);
				nodeLat = graph.getNodeLat(currNode);
				nodeLon = graph.getNodeLon(currNode);
				resultPoints.addPoint(nodeLat, nodeLon);
				length += graph.getDist(edgeId);
			}
		}
		return length;
	}

	/**
	 * Computes the shortest path over points[0] -> points[1] -> points[2]...
	 * and stores all points on the path in resultPoints
	 * 
	 * @param points
	 * @param resultPoints
	 * @return
	 * @throws Exception
	 */
	public int shortestPath(RequestPoints points, Points resultPoints)
			throws ComputeException {
		int srclat;
		int srclon;
		int destlat;
		int destlon;

		int srcId = 0;
		int destId = 0;

		int oldDistance = 0;
		int distance = 0;
		// in meters
		double directDistance;
		for (int pointIndex = 0; pointIndex < (points.size() - 1); pointIndex++) {

			// New Dijkstra need to reset
			long starttime = System.nanoTime();
			srclat = points.getPointLat(pointIndex);
			srclon = points.getPointLon(pointIndex);
			destlat = points.getPointLat(pointIndex + 1);
			destlon = points.getPointLon(pointIndex + 1);
			reset();
			srcId = graph.getIdForCoordinates(srclat, srclon);
			destId = graph.getIdForCoordinates(destlat, destlon);

			directDistance = calcDirectDistance(srclat / 10000000.0,
					((double) srclon) / 10000000,
					((double) destlat) / 10000000,
					((double) destlon) / 10000000);

			/*
			 * Start BFS on G_down beginning at destination and marking edges
			 * for consideration !G_down edges are always before G_up edges!
			 * 
			 * TODO: Create proper infrastructure for getting arrays with
			 * |nodes| length
			 */
			long setuptime = System.nanoTime();
			bfsMark(destId);
			long bfsdonetime = System.nanoTime();

			boolean found = dijkstraOneToOne(srcId, destId);
			long dijkstratime = System.nanoTime();

			if (!found) {
				System.err.println("There is no path from src to trgt");
				throw new ComputeException("No Path found");
			}

			distance += backtrack(resultPoints, srcId, destId);

			long backtracktime = System.nanoTime();

			// Save the distance to the last point at the target
			points.getConstraints(pointIndex + 1).put("distToPrev",
					distance - oldDistance);

			oldDistance = distance;
			System.out.println("found sp with dist = " + (distance / 1000.0)
					+ " km (direct distance: " + (directDistance / 1000.0)
					+ " dist[destid] = " + dists[destId]);
			System.out.println("Setup: "
					+ ((setuptime - starttime) / 1000000.0) + " ms");

			System.out.println("BFS: "
					+ ((bfsdonetime - setuptime) / 1000000.0) + " ms with "
					+ bfsNodes + " nodes and " + bfsEdges + " edges");

			System.out.println("Dijkstra: "
					+ ((dijkstratime - bfsdonetime) / 1000000.0) + " ms");

			System.out.println("Backtracking: "
					+ ((backtracktime - dijkstratime) / 1000000.0) + " ms");
		}

		Map<String, Integer> misc = new HashMap<String, Integer>(2);
		misc.put("distance", distance);
		// Don't forget to add destination
		resultPoints.addPoint(graph.getNodeLat(destId),
				graph.getNodeLon(destId));
		return distance;
	}

}