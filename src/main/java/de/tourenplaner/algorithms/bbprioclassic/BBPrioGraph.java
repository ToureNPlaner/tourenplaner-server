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
 */
public class BBPrioGraph extends PrioAlgorithm {
    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");
    private EdgeExtractor edx;

    public BBPrioGraph(GraphRep graph, BoundingBoxPriorityTree prioDings) {
        super(graph, prioDings);
        edx = new EdgeExtractor(graph);
    }


    @Override
    public void compute(ComputeRequest request) throws ComputeException, Exception {
        BBPrioRequestData req = (BBPrioRequestData) request.getRequestData();
        BoundingBox bbox = req.getBbox();


        long start = System.nanoTime();
        IntArrayList nodes = prioDings.getNodeSelection(bbox, req.getMinLevel());
        log.info(Timing.took("nodeSelection", start));

        start = System.nanoTime();
        ArrayList<BBPrioResult.Edge> edges = new ArrayList<BBPrioResult.Edge>();
        edx.getPriorityEdges(nodes, edges, req.getMinLen(), req.getMaxLen(), req.getMaxRatio(), req.getMinLevel());
        log.info(Timing.took("edx.getPriorityEdges", start));

        request.setResultObject(new BBPrioResult(graph, nodes, edges));
    }
}
