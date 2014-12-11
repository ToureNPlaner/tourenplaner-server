package de.tourenplaner.algorithms;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.graphrep.GraphRep;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Algorithm to extract Core Graphs, these are subgraphs that include all nodes and their edges.
 */
public class CoreGraph extends GraphAlgorithm{
	private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");

	public CoreGraph(GraphRep graph) {
		super(graph);
	}

	@Override
	public void compute(ComputeRequest request) throws ComputeException, Exception {
		CoreGraphRequest req = (CoreGraphRequest) request.getRequestData();


		Map<String, Object> constraints = req.getConstraints();
		int coreLevel = 80;
		if (constraints != null && req.getConstraints().get("coreLevel") != null) {
			try {
				coreLevel = ((Number) req.getConstraints().get("coreLevel")).intValue();
			} catch (ClassCastException e) {
				throw new ComputeException("Couldn't read Maximum Altitude Difference, wrong type: " + e.getMessage());
			}
		}

		long start = System.nanoTime();
		IntArrayList cgraph = new IntArrayList();
		// Find high edges
		for(int edgeId = 0; edgeId < graph.getEdgeCount(); ++edgeId){
			int src = graph.getSource(edgeId);
			// Optimization we only need to look at the target if source is high so pull it into if
			// int trgt = graph.getTarget(edgeId);
			if(graph.getNodeRank(src) >= coreLevel && graph.getNodeRank(graph.getTarget(edgeId)) >= coreLevel){
				boolean outputEdge = true;
				// Skip shortcuts where the shortcutted node is high their respective skip edges get added already
				int firstSkipped = graph.getFirstShortcuttedEdge(edgeId);
				if(firstSkipped >= 0){
					if(graph.getNodeRank(graph.getTarget(firstSkipped)) >= coreLevel){
						outputEdge = false;
					}
				}

				if(outputEdge){
					cgraph.add(edgeId);
				}

			}
		}
		log.info("Took " + (double)(System.nanoTime() - start) / 1000000.0+" ms");
		request.setResultObject(new SubgraphResult(graph, cgraph, -1, -1));
	}
}
