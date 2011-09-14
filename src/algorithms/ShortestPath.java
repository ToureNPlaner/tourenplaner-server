package algorithms;

import graphrep.Graphrep;

import java.util.ArrayList;

import computecore.ComputeRequest;
import computecore.ComputeResult;

public class ShortestPath extends GraphAlgorithm {

	private ComputeRequest r = null;

	double srclat;
	double srclon;
	double destlat;
	double destlon;

	int srcid;
	int destid;

	public ShortestPath(Graphrep graph) {
		super(graph);
		dist = new int[graph.getNodeCount()];
		prev = new int[graph.getNodeCount()];
		for (int i = 0; i < dist.length; i++) {
			dist[i] = Integer.MAX_VALUE;
		}
	}

	@Override
	public void setRequest(ComputeRequest req) {
		r = req;
	}

	@Override
	public ComputeResult getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	private final int[] dist;
	private final int[] prev;

	@Override
	public void run() {

		if (r == null) {
			// TODO: some problem here
			// return;
		}

		// TODO: somehow get the coordinates
		// srclat = r.
		// srclon = r.
		// destlat = r.
		// destlon = r.

		// Stuttgart
		srclat = 48.778611;
		srclon = 9.179444;

		// Hamburg
		destlat = 53.565278;
		destlon = 10.001389;

		srcid = graph.getIDForCoordinates((float) srclat, (float) srclon);
		destid = graph.getIDForCoordinates((float) destlat, (float) destlon);

		dist[srcid] = 0;
		graphrep.Heap h = new graphrep.Heap();

		h.insert(srcid, dist[srcid]);

		int nodeID;
		int nodeDist;
		int outTarget = 0;

		DIJKSTRA: for (;;) {
			if (!h.isEmpty()) {
				nodeID = h.peekMinId();
				nodeDist = h.peekMinDist();
				h.removeMin();
				if (nodeID == destid) {
					break DIJKSTRA;
				} else if (nodeDist > dist[nodeID]) {
					continue;
				}
				for (int i = 0; i < graph.getOutEdgeCount(nodeID); i++) {
					outTarget = graph.getOutTarget(nodeID, i);

					if (dist[nodeID] + graph.getOutDist(nodeID, i) < dist[outTarget]) {
						dist[outTarget] = dist[nodeID]
								+ graph.getOutDist(nodeID, i);
						prev[outTarget] = nodeID;
						h.insert(outTarget, dist[outTarget]);
					}
				}
			} else {
				System.err.println("heap empty");
				// TODO: do something useful
				return;
			}
		}
		System.err.println("found sp with dist = " + dist[outTarget] / 1000
				+ " km");
		int currNode = outTarget;
		ArrayList<ArrayList<Float>> list = new ArrayList<ArrayList<Float>>(
				dist[outTarget] / 50);
		do {
			list.add(0, new ArrayList<Float>(2));
			list.get(0).add(graph.getNodeLat(currNode));
			list.get(0).add(graph.getNodeLon(currNode));
			currNode = prev[currNode];
		} while (currNode != srcid);

		System.out
				.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
		System.out
				.println("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" creator=\"Oregon 400t\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\">");
		System.out.println("  <trk>\n"
				+ "    <name>Example GPX Document</name>");
		System.out.println("<trkseg>");
		for (ArrayList<Float> l : list) {
			System.out.println("<trkpt lat=\"" + l.get(0) + "\" lon=\""
					+ l.get(1) + "\"></trkpt>");
		}
		System.out.println("</trkseg>\n</trk>\n</gpx>");
	}
}