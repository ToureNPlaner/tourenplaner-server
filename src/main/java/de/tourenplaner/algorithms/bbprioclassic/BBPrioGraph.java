package de.tourenplaner.algorithms.bbprioclassic;

import de.tourenplaner.algorithms.ComputeException;
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
public class BBPrioGraph extends PrioAlgorithm {
    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");
	private EdgeExtractor edx;

    public BBPrioGraph(GraphRep graph, PrioDings prioDings) {
	    super(graph, prioDings);
	    edx = new EdgeExtractor(graph);
    }



    @Override
    public void compute(ComputeRequest request) throws ComputeException, Exception {
        BBPrioRequestData req = (BBPrioRequestData) request.getRequestData();
	    BoundingBox bbox = req.getBbox();


        long start = System.nanoTime();
	    ArrayList<Integer> nodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), req.getMinLevel());
	    ArrayList<BBPrioResult.Edge> edges = new ArrayList<BBPrioResult.Edge>();
	    edx.getPriorityEdges(nodes, edges, req.getMinLen(), req.getMaxLen(), req.getMaxRatio(), req.getMinLevel());


        log.info("Took " + (double)(System.nanoTime() - start) / 1000000.0+" ms");
        request.setResultObject(new BBPrioResult(graph, nodes, edges));
    }
}
