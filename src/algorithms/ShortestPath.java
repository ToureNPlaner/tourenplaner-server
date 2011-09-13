package algorithms;

import graphrep.Graphrep;

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

	@Override
	public void run() {

		if (r == null) {
			// TODO: some problem here
			return;
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
		srclat = 53.565278;
		srclon = 10.001389;

		srcid = graph.getIDForCoordinates((float) srclat, (float) srclon);
		destid = graph.getIDForCoordinates((float) destlat, (float) destlon);

		// TODO: maybe not so big needed
		dist[srcid] = 0;
		graphrep.Heap h = new graphrep.Heap(srcid, dist[srcid]);

		int outTarget;
		for (int i = 0; i <= graph.getOutEdgeCount(srcid); i++) {
			outTarget = graph.getOutTarget(srcid, i);
			if (graph.getOutDist(srcid, i) < dist[outTarget]) {
				dist[outTarget] = graph.getOutDist(srcid, i);
				h.insert(outTarget, dist[outTarget]);
			}
		}
	}
}
