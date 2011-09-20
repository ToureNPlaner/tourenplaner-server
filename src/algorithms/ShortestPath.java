package algorithms;

import graphrep.GraphRep;
import graphrep.Heap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONAware;

import computecore.ComputeRequest;
import computecore.ComputeResult;

public class ShortestPath extends GraphAlgorithm {

	private class Points implements JSONAware {
		private final float[] lats;
		private final float[] lons;
		StringBuilder sb;

		public Points(float[] lats, float[] lons) {
			this.lats = lats;
			this.lons = lons;
			sb = new StringBuilder();
		}

		@Override
		public String toJSONString() {
			sb.append("[");
			for (int i = 0; i < (lats.length - 1); i++) {
				sb.append("{\"lt\":" + lats[i] + ",\"ln\":" + lons[i] + "},");
			}
			sb.append("{\"lt\":" + lats[lats.length - 1] + ",\"ln\":"
					+ lons[lats.length - 1] + "}");
			sb.append("]");
			return sb.toString();
		}
	}

	private ComputeRequest req = null;
	private ComputeResult res = null;

	float srclat;
	float srclon;
	float destlat;
	float destlon;

	int srcid;
	int destid;

	private final Heap heap;

	public ShortestPath(GraphRep graph) {
		super(graph);
		heap = new graphrep.Heap();
		dist = new int[graph.getNodeCount()];
		prev = new int[graph.getNodeCount()];
	}

	@Override
	public void setRequest(ComputeRequest req) {
		// reset dists
		for (int i = 0; i < dist.length; i++) {
			dist[i] = Integer.MAX_VALUE;
		}
		heap.resetHeap();
		this.req = req;
	}

	@Override
	public ComputeResult getResult() {
		return res;
	}

	// dists in this array are stored with the multiplier applied. They also are
	// rounded and are stored as integers
	private final int[] dist;
	private final int[] prev;

	// in meters
	private float directDistance;


	// maybe use http://code.google.com/p/simplelatlng/ instead
	private final float calcDirectDistance(double lat1, double lng1,
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
		return (float) (dist * 1000);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		if (req == null) {
			// TODO: some problem here
			System.err
					.println("We don't have a request object, how did we even get called without one??");
			return;
		}
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

		srclat = points.get(0).get("lt").floatValue();
		srclon = points.get(0).get("ln").floatValue();
		destlat = points.get(1).get("lt").floatValue();
		destlon = points.get(1).get("ln").floatValue();

		srcid = graph.getIDForCoordinates(srclat, srclon);
		destid = graph.getIDForCoordinates(destlat, destlon);
		directDistance = calcDirectDistance(srclat, srclon, destlat, destlon);

		dist[srcid] = 0;
		heap.insert(srcid, dist[srcid]);

		int nodeID = -1;
		float nodeDist;
		int outTarget = 0;
		int tempDist;

		long starttime = System.nanoTime();
		long dijkstratime;
		long backtracktime;
		DIJKSTRA: while (!heap.isEmpty()) {
			nodeID = heap.peekMinId();
			nodeDist = heap.peekMinDist();
			heap.removeMin();
			if (nodeID == destid) {
				break DIJKSTRA;
			} else if (nodeDist > dist[nodeID]) {
				continue;
			}
			for (int i = 0; i < graph.getOutEdgeCount(nodeID); i++) {
				outTarget = graph.getOutTarget(nodeID, i);

				// without multiplier = shortest path
				// tempDist = dist[nodeID] + graph.getOutDist(nodeID, i);

				// with multiplier = fastest path
				tempDist = dist[nodeID] + graph.getOutMult(nodeID, i);

				if (tempDist < dist[outTarget]) {
					dist[outTarget] = tempDist;
					prev[outTarget] = nodeID;
					heap.insert(outTarget, dist[outTarget]);
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
			currNode = prev[currNode];
		} while (currNode != srcid);

		float[] lats = new float[routeElements];
		float[] lons = new float[routeElements];

		// backtracking here
		// Don't read distance from dist[], because there are distances with
		// regard to the multiplier
		int distance = 0;
		currNode = nodeID;
		// don't declare new integer all the time
		int tempEdge;
		do {
			for (tempEdge = 0; tempEdge < graph.getOutEdgeCount(prev[currNode]); tempEdge++) {
				if (graph.getOutTarget(prev[currNode], tempEdge) == currNode) {
					// we found our edge, now get the dist and add it
					distance += graph.getOutDist(prev[currNode], tempEdge);
					break;
				}
			}
			routeElements--;
			lats[routeElements] = graph.getNodeLat(currNode);
			lons[routeElements] = graph.getNodeLon(currNode);
			currNode = prev[currNode];
		} while (currNode != srcid);
		backtracktime = System.nanoTime();
		Map<String, Integer> misc = new HashMap<String, Integer>(2);
		misc.put("distance", dist[outTarget]);

		System.out.println("found sp with dist = " + (distance / 1000.0)
				+ " km (direct distance: " + (directDistance / 1000.0)
				+ " km; Distance with multiplier: " + (dist[destid] / 1000.0)
				+ ")");
		System.out.println("Dijkstra: "
				+ ((dijkstratime - starttime) / 1000000.0)
				+ " ms; Backtracking: "
				+ ((backtracktime - dijkstratime) / 1000000.0) + " ms");

		res.put("points", new Points(lats, lons));
		res.put("misc", misc);
	}
}