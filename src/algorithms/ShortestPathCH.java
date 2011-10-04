package algorithms;

import graphrep.GraphRep;
import graphrep.Heap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONAware;

import computecore.ComputeRequest;
import computecore.ComputeResult;

public class ShortestPathCH extends GraphAlgorithm {

	private ComputeRequest req = null;
	private ComputeResult res = null;

	double srclat;
	double srclon;
	double destlat;
	double destlon;

	int srcid;
	int destid;

	private final Heap heap;

	// Used to mark nodes with BFS
	private final boolean[] marked;
	private final boolean[] visited;

	// TODO: Replace with int array based fifo
	private final ArrayDeque<Integer> fifo;

	public ShortestPathCH(GraphRep graph) {
		super(graph);
		heap = new graphrep.Heap();
		multipliedDist = new int[graph.getNodeCount()];
		prevEdges = new int[graph.getNodeCount()];
		marked = new boolean[graph.getEdgeCount()]; // TODO: Compare with BitSet
		visited = new boolean[graph.getNodeCount()];										
		fifo = new ArrayDeque<Integer>();
	}

	@Override
	public void setRequest(ComputeRequest req) {
		// reset dists
		for (int i = 0; i < graph.getNodeCount(); i++) {
			multipliedDist[i] = Integer.MAX_VALUE;
			visited[i] =false;
		}
		// reset marks
		for (int i = 0; i < graph.getEdgeCount(); i++) {
			marked[i] = false;
			// Don't need to reset prevEdges because we backtrack visited edges
			// only by design
		}
		heap.resetHeap();
		fifo.clear();

		this.req = req;
	}

	@Override
	public ComputeResult getResult() {
		return res;
	}

	// dists in this array are stored with the multiplier applied. They also are
	// rounded and are stored as integers
	private final int[] multipliedDist;

	/**
	 * edge id
	 */
	private final int[] prevEdges;

	// in meters
	private double directDistance;

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

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		assert (req != null) : "We ended up without a request object in run";

		res = req.getResultObject();

		ArrayList<Map<String, Double>> points = null;
		// TODO: send error messages to client
		try {
			points = (ArrayList<Map<String, Double>>) req.get("points");
		} catch (ClassCastException e) {
			System.err.println("The request's contents are invalid\n"
					+ e.getMessage());
			e.printStackTrace();
			return;
		} catch (NullPointerException e) {
			System.err
					.println("The request doesn't contain the information required for this algorithm\n"
							+ e.getMessage());
			e.printStackTrace();
			return;
		}

		srclat = points.get(0).get("lt");
		srclon = points.get(0).get("ln");
		destlat = points.get(1).get("lt");
		destlon = points.get(1).get("ln");

		srcid = graph.getIDForCoordinates(srclat, srclon);
		destid = graph.getIDForCoordinates(destlat, destlon);
		directDistance = calcDirectDistance(srclat, srclon, destlat, destlon);
		
		int nodeID = destid;
		int sourceNode;
		int edgeId;
		/*
		 * Start BFS on G_down beginning at destination and marking edges for
		 * consideration
		 * 
		 * TODO: Create proper infrastructure for getting arrays with |nodes| length
		 */

		
		System.out.println("Start BFS");
		long starttime = System.nanoTime();
		long numEdges =0;
		fifo.addLast(destid);
		visited[destid] = true;
		
		while (!fifo.isEmpty()) {
			nodeID = fifo.poll();		
			
			for (int i = 0; i < graph.getInEdgeCount(nodeID); i++) {
				edgeId = graph.getInEdgeID(nodeID, i);
				sourceNode = graph.getSource(edgeId);

				if (!visited[sourceNode] && graph.getRank(sourceNode) >= graph.getRank(nodeID)){
					// Mark the edge
					marked[edgeId] = true;
					visited[sourceNode] = true;
					// Add source for exploration
					fifo.addLast(sourceNode);
				}
				numEdges++;
				
			}
		}
		
		System.out.println("BFS("+numEdges+") time: "+(System.nanoTime()-starttime)/1000000.0);

		multipliedDist[srcid] = 0;
		heap.insert(srcid, multipliedDist[srcid]);

		int nodeDist;
		
		int tempDist;
		int targetNode;
		starttime = System.nanoTime();
		long dijkstratime;
		long backtracktime;

		DIJKSTRA: while (!heap.isEmpty()) {
			nodeID = heap.peekMinId();
			nodeDist = heap.peekMinDist();
			heap.removeMin();
			if (nodeID == destid) {
				break DIJKSTRA;
			} else if (nodeDist > multipliedDist[nodeID]) {
				continue;
			}
			for (int i = 0; i < graph.getOutEdgeCount(nodeID); i++) {
				edgeId = graph.getOutEdgeID(nodeID, i);
				targetNode = graph.getTarget(edgeId);
				// Either marked (by BFS) or G_up edge
				if (marked[edgeId] || graph.getRank(nodeID) <= graph.getRank(targetNode)) {
					// without multiplier = shortest path
					// tempDist = dist[nodeID] + graph.getOutDist(nodeID, i);

					// with multiplier = fastest path
					tempDist = multipliedDist[nodeID]
							+ graph.getMultipliedDist(edgeId);

					if (tempDist < multipliedDist[targetNode]) {
						multipliedDist[targetNode] = tempDist;
						prevEdges[targetNode] = graph.getOutEdgeID(nodeID, i);
						heap.insert(targetNode, tempDist);
					}
					
				}
			}
		}
		dijkstratime = System.nanoTime();

		if (nodeID != destid) {
			// TODO: send errmsg to client and do something useful
			System.err.println("There is no path from src to dest");
			return;
		}

		// Find out how much space to allocate
		int currNode = nodeID;
		int routeElements = 0;

		do {
			routeElements++;
			currNode = graph.getSource(prevEdges[currNode]);
		} while (currNode != srcid);

		System.out.println("path goes over " + routeElements + " edges");
		double[] lats = new double[routeElements];
		double[] lons = new double[routeElements];

		// backtracking here
		// Don't read distance from dist[], because there are distances with
		// regard to the multiplier
		int distance = 0;
		currNode = nodeID;
		do {
			distance += graph.getDist(prevEdges[currNode]);
			routeElements--;
			lats[routeElements] = graph.getNodeLat(currNode);
			lons[routeElements] = graph.getNodeLon(currNode);
			currNode = graph.getSource(prevEdges[currNode]);
		} while (currNode != srcid);
		backtracktime = System.nanoTime();
		Map<String, Integer> misc = new HashMap<String, Integer>(2);
		misc.put("distance", distance);

		System.out.println("found sp with dist = " + (distance / 1000.0)
				+ " km (direct distance: " + (directDistance / 1000.0)
				+ " km; Distance with multiplier: "
				+ (multipliedDist[destid] / 1000.0) + ")");
		System.out.println("Dijkstra: "
				+ ((dijkstratime - starttime) / 1000000.0)
				+ " ms; Backtracking: "
				+ ((backtracktime - dijkstratime) / 1000000.0) + " ms");

		res.put("points", new Points(lats, lons));
		res.put("misc", misc);
	}
}