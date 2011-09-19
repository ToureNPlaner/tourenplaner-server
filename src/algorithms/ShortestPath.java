package algorithms;

import graphrep.GraphRep;

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

	public ShortestPath(GraphRep graph) {
		super(graph);
		dist = new float[graph.getNodeCount()];
		prev = new int[graph.getNodeCount()];
	}

	@Override
	public void setRequest(ComputeRequest req) {
		// reset dists
		for (int i = 0; i < dist.length; i++) {
			dist[i] = Float.MAX_VALUE;
		}
		this.req = req;
	}

	@Override
	public ComputeResult getResult() {
		return res;
	}

	// we will store dists with multiplier applied in here, and since
	// multipliers are 0.0 < mult < 1.3 (?), we need float
	private final float[] dist;
	private final int[] prev;

	// in meters
	private float directDistance;

	/**
	 * @return Whether the given point is near enaugh at the direct connection
	 *         to add it to U
	 */
	private boolean nearEnaugh(float lt, float ln) {
		// TODO: find good values
		// at the moment derivation from direct way is 50 km + 10% of total
		// length of the direct way
		return true;
	}

	// maybe use http://code.google.com/p/simplelatlng/ instead
	private final float distFrom(double lat1, double lng1, double lat2,
			double lng2) {
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

	@Override
	public void run() {
		if (req == null) {
			// TODO: some problem here
			System.err
					.println("We don't have a request object, how did we even get called without one??");
			return;
		}
		res = req.getResultObject();

		// TODO: Validation!
		ArrayList<Map<String, Double>> points = (ArrayList<Map<String, Double>>) req
				.get("points");

		srclat = points.get(0).get("lt").floatValue();
		srclon = points.get(0).get("ln").floatValue();
		destlat = points.get(1).get("lt").floatValue();
		destlon = points.get(1).get("ln").floatValue();

		srcid = graph.getIDForCoordinates(srclat, srclon);
		destid = graph.getIDForCoordinates(destlat, destlon);

		directDistance = distFrom(srclat, srclon, destlat, destlon);

		dist[srcid] = 0.0F;
		graphrep.Heap h = new graphrep.Heap();

		h.insert(srcid, dist[srcid]);

		int nodeID = -1;
		float nodeDist;
		int outTarget = 0;
		float tempDist;

		long start = System.nanoTime();
		long end1;
		long end2;
		DIJKSTRA: while (!h.isEmpty()) {
			nodeID = h.peekMinId();
			nodeDist = h.peekMinDist();
			h.removeMin();
			if (nodeID == destid) {
				break DIJKSTRA;
			} else if (graphrep.GraphRep.gt_Float(nodeDist, dist[nodeID])) {
				continue;
			}
			for (int i = 0; i < graph.getOutEdgeCount(nodeID); i++) {
				outTarget = graph.getOutTarget(nodeID, i);

				// without multiplier
				// tempDist = dist[nodeID] + graph.getOutDist(nodeID, i);
				// if (tempDist < dist[outTarget]) {
				// dist[outTarget] = tempDist;
				// prev[outTarget] = nodeID;
				// h.insert(outTarget, dist[outTarget]);
				// }

				// with multiplier
				// TODO: check if this usage of mult is right
				tempDist = (dist[nodeID] + (graph.getOutDist(nodeID, i) / graph
						.getOutMult(nodeID, i)));
				if (graphrep.GraphRep.lt_Float(tempDist, dist[outTarget])) {
					dist[outTarget] = tempDist;
					prev[outTarget] = nodeID;
					h.insert(outTarget, dist[outTarget]);
				}
			}
		}
		end1 = System.nanoTime();

		if (nodeID != destid) {
			System.err.println("There is no path from src to dest");
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
		end2 = System.nanoTime();
		Map<String, Float> misc = new HashMap<String, Float>(2);
		misc.put("distance", dist[outTarget]);

		System.err.println("found sp with dist = " + (distance / 1000.0)
				+ " km (direct distance: " + (directDistance / 1000.0)
				+ " km; Distance with multiplier: " + (dist[destid] / 1000.0)
				+ ")");
		System.err.println("Dijkstra: " + ((end1 - start) / 1000000.0)
				+ "ms; Backtracking: " + ((end2 - end1) / 1000000.0));

		// System.out
		// .println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
		// System.out
		// .println("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" creator=\"Oregon 400t\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\">");
		// System.out.println("  <trk>\n"
		// + "    <name>Example GPX Document</name>");
		// System.out.println("<trkseg>");
		// for (ArrayList<Float> l : list) {
		// System.out.println("<trkpt lat=\"" + l.get(0) + "\" lon=\""
		// + l.get(1) + "\"></trkpt>");
		// }
		// System.out.println("</trkseg>\n</trk>\n</gpx>");
		res.put("points", new Points(lats, lons));
		res.put("misc", misc);
	}
}