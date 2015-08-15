package de.tourenplaner.algorithms.drawcore;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.GraphAlgorithm;
import de.tourenplaner.algorithms.bbbundle.BBBundleEdge;
import de.tourenplaner.algorithms.bbbundle.EdgeUnpacker;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.graphrep.GraphRep;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by niklas on 30.03.15.
 */
public class DrawCore extends GraphAlgorithm {
    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");

    private final EdgeUnpacker unpacker;

    public DrawCore(GraphRep graph) {
        super(graph);
        unpacker = new EdgeUnpacker(graph);
    }

    @Override
    public void compute(ComputeRequest request) throws ComputeException, Exception {
        DrawCoreRequestData req = (DrawCoreRequestData) request.getRequestData();

        ArrayList<BBBundleEdge> edges =  new ArrayList<>();
        IntArrayList edgesToDraw = new IntArrayList();
        IntArrayList verticesToDraw = new IntArrayList();
        unpacker.reset();
        // Nodes are sorted by rank descending, so the highest nodeCount nodes are 0,..,nodeCount-1
        for (int nodeId = 0; nodeId < req.getNodeCount() -1; ++nodeId) {
            // Out edges are sorted by target rank ascending, go them backwards so we can
            // break as soon as targets get too low
            for (int outEdgeNum = graph.getOutEdgeCount(nodeId) - 1; outEdgeNum >= 0; --outEdgeNum) {
                int edgeId = graph.getOutEdgeId(nodeId, outEdgeNum);
                int trgtId = graph.getTarget(edgeId);
                // Too low
                if(trgtId >= req.getNodeCount()) {
                    break;
                }

                // Shortcut and skipped edges in core so we get them and don't need the shortcut itself
                int skipA = graph.getFirstShortcuttedEdge(edgeId);
                if(skipA > 0 && graph.getTarget(skipA) < req.getNodeCount()) {
                    continue;
                }
                BBBundleEdge edge = new BBBundleEdge(edgeId, nodeId, trgtId, graph.getDist(edgeId));
                // TODO how far do we need to unpack the CORE?
                unpacker.unpack(req.isLatLonMode(), edge, verticesToDraw, edgesToDraw, null, req.getMinLen(), req.getMaxLen(), req.getMaxRatio());
                edges.add(edge);
            }
        }
        request.setResultObject(new DrawCoreResult(graph, req.isLatLonMode(), edges, verticesToDraw, edgesToDraw,req.getNodeCount()));
    }
}
