package de.tourenplaner.algorithms.drawcore;

import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.GraphAlgorithm;
import de.tourenplaner.algorithms.bbbundle.BBBundleEdge;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.graphrep.GraphRep;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by niklas on 30.03.15.
 */
public class DrawCore extends GraphAlgorithm {
    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");

    public DrawCore(GraphRep graph) {
        super(graph);
    }

    @Override
    public void compute(ComputeRequest request) throws ComputeException, Exception {
        DrawCoreRequestData req = (DrawCoreRequestData) request.getRequestData();

        ArrayList<BBBundleEdge> edges =  new ArrayList<>();
        // Nodes are sorted by rank descending, so the highest nodeCount nodes are 0,..,nodeCount-1
        for (int nodeId = 0; nodeId < req.nodeCount -1; ++nodeId) {
            // Out edges are sorted by target rank ascending, go them backwards so we can
            // break as soon as targets get too low
            for (int outEdgeNum = graph.getOutEdgeCount(nodeId) - 1; outEdgeNum >= 0; --outEdgeNum) {
                int edgeId = graph.getOutEdgeId(nodeId, outEdgeNum);
                int trgtId = graph.getTarget(edgeId);
                // Too low
                if(trgtId >= req.nodeCount) {
                    break;
                }

                // Shortcut and skipped edges in core so we get them and don't need the shortcut itself
                int skipA = graph.getFirstShortcuttedEdge(edgeId);
                if(skipA > 0 && graph.getTarget(skipA) < req.nodeCount) {
                    continue;
                }
                BBBundleEdge edge = new BBBundleEdge(edgeId, nodeId, trgtId, graph.getDist(edgeId));
                // TODO how far do we need to unpack the CORE?
                edge.unpacked.add(edgeId);
                edges.add(edge);
            }
        }
        request.setResultObject(new DrawCoreResult(graph, edges, req.nodeCount));
    }
}
