package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntStack;
import com.carrotsearch.hppc.cursors.IntCursor;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.PrioAlgorithm;
import de.tourenplaner.algorithms.bbprioclassic.BBPrioResult;
import de.tourenplaner.algorithms.bbprioclassic.BoundingBox;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.graphrep.PrioDings;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by niklas on 19.03.15.
 */
public class BBBundle  extends PrioAlgorithm {
    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");
    private static final int UNSEEN = 0;
    private static final int DISCOVERED = 1;
    private static final int ACTIVE = 2;
    private static final int COMPLETED = 3;

    private final int[] dfsState;
    private final int[] mappedIds;

    public BBBundle(GraphRep graph, PrioDings prioDings) {
        super(graph, prioDings);
        dfsState = new int[graph.getNodeCount()];
        mappedIds = new int[graph.getNodeCount()];
    }

    private IntArrayDeque topoSortNodes(IntArrayList bboxNodes, int P, int coreSize) {
        IntArrayDeque topSorted = new IntArrayDeque(bboxNodes.size());
        IntArrayList needClear = new IntArrayList();
        IntStack stack = new IntStack();
        int currIdMap = coreSize;

        stack.pushAll(bboxNodes);
        while(!stack.isEmpty()) {
            int nodeId = stack.peek();
            int nodeRank = graph.getRank(nodeId);
            switch(dfsState[nodeId]) {
                case DISCOVERED:
                    dfsState[nodeId] = ACTIVE;
                    // Up-Out edges
                    for (int upEdgeNum = graph.getOutEdgeCount(nodeId) - 1; upEdgeNum >= 0; --upEdgeNum) {
                        int edgeId = graph.getOutEdgeId(nodeId, upEdgeNum);
                        int trgtId = graph.getTarget(edgeId);
                        int trgtRank = graph.getRank(trgtId);
                        if (dfsState[trgtId] > 0 || trgtId < coreSize) {
                            continue;
                        }

                        // Out edges are sorted by target rank ascending, mind we're going down
                        if (trgtRank < P || trgtRank < nodeRank) {
                            break;
                        }
                        dfsState[trgtId] = DISCOVERED;
                        needClear.add(trgtId);
                        stack.push(trgtId);
                    }

                    // Down-In edges
                    for (int downEdgeNum = 0; downEdgeNum < graph.getOutEdgeCount(nodeId); ++downEdgeNum) {
                        int edgeId = graph.getOutEdgeId(nodeId, downEdgeNum);
                        int srcId = graph.getSource(edgeId);
                        if (dfsState[srcId] > 0) {
                            continue;
                        }

                        int srcRank = graph.getRank(srcId);
                        // In edges are sorted by source rank descending
                        if (srcRank < P || srcRank < nodeRank) {
                            break;
                        }
                        dfsState[srcId] = DISCOVERED;
                        needClear.add(srcId);
                        stack.push(srcId);
                    }
                case ACTIVE:
                    dfsState[nodeId] = COMPLETED;
                    nodeId = stack.pop();
                    topSorted.addFirst(nodeId);
                    mappedIds[nodeId] = currIdMap++;
            }
        }

        for (IntCursor ic : needClear) {
            dfsState[ic.value] = UNSEEN;
        }
        needClear.clear();

        return topSorted;
    }

    @Override
    public void compute(ComputeRequest request) throws ComputeException, Exception {
        BBBundleRequestData req = (BBBundleRequestData) request.getRequestData();
        BoundingBox bbox = req.getBbox();


        long start = System.nanoTime();
        int currNodeCount;
        int level;
        IntArrayList bboxNodes;
        if (req.mode == BBBundleRequestData.LevelMode.AUTO) {

            level = graph.getMaxRank();
            do {
                level = level - 10;
                bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
                currNodeCount = bboxNodes.size();
            } while (level > 0 && currNodeCount < req.nodeCount);

        } else if (req.mode == BBBundleRequestData.LevelMode.HINTED){

            level = graph.getMaxRank();
            do {
                level = level - 10;
                bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
                currNodeCount = bboxNodes.size();
            } while (level > 0 && currNodeCount < req.nodeCount);
            log.info("AutoLevel was: "+level);
            level = (req.hintLevel+level)/2;
            bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
            currNodeCount = bboxNodes.size();

        } else { // else if (req.mode == BBPrioLimitedRequestData.LevelMode.EXACT){
            level = req.getHintLevel();
            bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
            currNodeCount = bboxNodes.size();
        }
        log.info("Level was: "+level);
        log.info("Nodes found: "+currNodeCount);



        ArrayList<BBPrioResult.Edge> edges = new ArrayList<>();

        log.info("Took " + (double)(System.nanoTime() - start) / 1000000.0+" ms");
        request.setResultObject(new BBPrioResult(graph, bboxNodes, edges));
    }
}
