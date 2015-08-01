package de.tourenplaner.algorithms.bbprioclassic;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.PrioAlgorithm;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.graphrep.BoundingBoxPriorityTree;
import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.utils.Timing;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Niklas Schnelle
 *
 */
public class BBPrioLimitedGraph extends PrioAlgorithm {
	private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");
	private EdgeExtractor edx;

	public BBPrioLimitedGraph(GraphRep graph, BoundingBoxPriorityTree prioDings) {
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
				nodes = prioDings.getNodeSelection(bbox, level);
				currNodeCount = nodes.size();
			} while (level > 0 && currNodeCount < req.nodeCount);

		} else if (req.mode == BBPrioLimitedRequestData.LevelMode.HINTED){

			level = graph.getMaxRank();
			do {
				level = level - 10;
				nodes = prioDings.getNodeSelection(bbox, level);
				currNodeCount = nodes.size();
			} while (level > 0 && currNodeCount < req.nodeCount);
			log.info("AutoLevel was: "+level);
			level = (req.hintLevel+level)/2;
			nodes = prioDings.getNodeSelection(bbox, level);
			currNodeCount = nodes.size();

		} else { // else if (req.mode == BBPrioLimitedRequestData.LevelMode.EXACT){
			level = req.getHintLevel();
			nodes = prioDings.getNodeSelection(bbox, level);
			currNodeCount = nodes.size();
		}
		log.info(Timing.took("NodeSelection", start));
		log.info("Level was: "+level);
		log.info("Nodes found: "+currNodeCount);
		start = System.nanoTime();

		ArrayList<BBPrioResult.Edge> edges = new ArrayList<BBPrioResult.Edge>();
		edx.getPriorityEdges(nodes, edges, req.getMinLen(), req.getMaxLen(), req.getMaxRatio(), level);
		log.info(Timing.took("edx.getPriorityEdges", start));

		request.setResultObject(new BBPrioResult(graph, nodes, edges));
	}
}