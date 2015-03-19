package de.tourenplaner.algorithms.bbprioclassic;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.PrioAlgorithm;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.graphrep.EdgeExtractor;
import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.graphrep.PrioDings;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Niklas Schnelle
 *
 */
public class BBPrioLimitedGraph extends PrioAlgorithm {
	private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");
	private EdgeExtractor edx;

	public BBPrioLimitedGraph(GraphRep graph, PrioDings prioDings) {
		super(graph, prioDings);
		edx = new EdgeExtractor(graph);
	}



	@Override
	public void compute(ComputeRequest request) throws ComputeException, Exception {
		BBPrioLimitedRequestData req = (BBPrioLimitedRequestData) request.getRequestData();
		BoundingBox bbox = req.getBbox();


		long start = System.nanoTime();
		int currNodeCount;
		int level;
		IntArrayList nodes;
		if (req.mode == BBPrioLimitedRequestData.LevelMode.AUTO) {

			level = graph.getMaxRank();
			do {
				level = level - 10;
				nodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
				currNodeCount = nodes.size();
			} while (level > 0 && currNodeCount < req.nodeCount);

		} else if (req.mode == BBPrioLimitedRequestData.LevelMode.HINTED){

			level = graph.getMaxRank();
			do {
				level = level - 10;
				nodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
				currNodeCount = nodes.size();
			} while (level > 0 && currNodeCount < req.nodeCount);
			log.info("AutoLevel was: "+level);
			level = (req.hintLevel+level)/2;
			nodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
			currNodeCount = nodes.size();

		} else { // else if (req.mode == BBPrioLimitedRequestData.LevelMode.EXACT){
			level = req.getHintLevel();
			nodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
			currNodeCount = nodes.size();
		}
		log.info("Level was: "+level);
		log.info("Nodes found: "+currNodeCount);

		ArrayList<BBPrioResult.Edge> edges = new ArrayList<BBPrioResult.Edge>();
		edx.getPriorityEdges(nodes, edges, req.getMinLen(), req.getMaxLen(), req.getMaxRatio(), level);


		log.info("Took " + (double)(System.nanoTime() - start) / 1000000.0+" ms");
		request.setResultObject(new BBPrioResult(graph, nodes, edges));
	}
}