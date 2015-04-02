package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntStack;
import com.carrotsearch.hppc.cursors.IntCursor;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.PrioAlgorithm;
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
public class BBBundle extends PrioAlgorithm {
    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");
    private static final int UNSEEN = 0;
    private static final int DISCOVERED = 1;
    private static final int ACTIVE = 2;
    private static final int COMPLETED = 3;

    private final int[] dfsState;
    private final int[] mappedIds;
    private final BitSet drawn;
    private final IntArrayList needClear;

    public BBBundle(GraphRep graph, PrioDings prioDings) {
        super(graph, prioDings);
        dfsState = new int[graph.getNodeCount()];
        mappedIds = new int[graph.getNodeCount()];
        needClear = new IntArrayList();
        drawn = new BitSet(graph.getEdgeCount());
    }

    private IntArrayDeque topoSortNodes(IntArrayList bboxNodes, int P, int coreSize) {
        IntArrayDeque topSorted = new IntArrayDeque(bboxNodes.size());
        IntStack stack = new IntStack();
        int currIdMap = coreSize;
        for (int i = 0; i < bboxNodes.size(); ++i) {
            int nodeId = bboxNodes.get(i);
            // We pnly want nodes that aren't in the core because we got them already
            if (nodeId >= coreSize) {
                dfsState[nodeId] = DISCOVERED;
                needClear.add(nodeId);
                stack.push(nodeId);
            }
        }

        while (!stack.isEmpty()) {
            int nodeId = stack.peek();
            int nodeRank = graph.getRank(nodeId);
            switch (dfsState[nodeId]) {
                case DISCOVERED: {
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
                        assert dfsState[trgtId] == UNSEEN;
                        dfsState[trgtId] = DISCOVERED;
                        needClear.add(trgtId);
                        stack.push(trgtId);
                    }

                    // Down-In edges
                    for (int downEdgeNum = 0; downEdgeNum < graph.getInEdgeCount(nodeId); ++downEdgeNum) {
                        int edgeId = graph.getInEdgeId(nodeId, downEdgeNum);
                        int srcId = graph.getSource(edgeId);
                        if (dfsState[srcId] > 0 || srcId < coreSize) {
                            continue;
                        }

                        int srcRank = graph.getRank(srcId);
                        // In edges are sorted by source rank descending
                        if (srcRank < P || srcRank < nodeRank) {
                            break;
                        }
                        assert dfsState[srcId] == UNSEEN;
                        dfsState[srcId] = DISCOVERED;
                        needClear.add(srcId);
                        stack.push(srcId);
                    }
                    break;
                }
                case ACTIVE: {
                    dfsState[nodeId] = COMPLETED;
                    stack.pop();
                    topSorted.addFirst(nodeId);
                    mappedIds[nodeId] = currIdMap++;
                    break;
                }
                default:
                    log.info("Crazy state dfsState[" + nodeId + "] = " + dfsState[nodeId]);
                    stack.pop();
            }
        }

        for (IntCursor ic : needClear) {
            dfsState[ic.value] = UNSEEN;
        }
        needClear.clear();

        return topSorted;
    }

    public void unpack(int index, IntArrayList unpackIds, double minLen, double maxLen, double maxRatio) {
        if (drawn.get(index)) {
            return;
        }
        double edgeLen = ((double) graph.getEuclidianDist(index));

        int skipA = graph.getFirstShortcuttedEdge(index);

        if (skipA == -1 || edgeLen <= minLen) {
            drawn.set(index);
            unpackIds.add(index);
            return;
        }

        int skipB = graph.getSecondShortcuttedEdge(index);
        /*
		*              |x1 y1 1|
		* A = abs(1/2* |x2 y2 1|)= 1/2*Baseline*Height
		*              |x3 y3 1|
		*   = 1/2*((x1*y2+y1*x3+x2*y3) - (y2*x3 + y1*x2 + x1*y3))
		* Height = 2*A/Baseline
		* ratio = Height/Baseline
		* */
        if (edgeLen <= maxLen) {
            int middle = graph.getTarget(skipA);

            int srcId = graph.getSource(index);
            double x1 = graph.getXPos(srcId);
            double y1 = graph.getYPos(srcId);
            double x2 = graph.getXPos(middle);
            double y2 = graph.getYPos(middle);
            int trgtId = graph.getTarget(index);
            double x3 = graph.getXPos(trgtId);
            double y3 = graph.getYPos(trgtId);
            double A = Math.abs(0.5 * ((x1 * y2 + y1 * x3 + x2 * y3) - (y2 * x3 + y1 * x2 + x1 * y3)));
            double ratio = 2.0 * A / (edgeLen * edgeLen);
            if (ratio <= maxRatio) {
                drawn.set(index);
                unpackIds.add(index);
                return;
            }
        }

        unpack(skipA, unpackIds, minLen, maxLen, maxRatio);
        unpack(skipB, unpackIds, minLen, maxLen, maxRatio);
    }


    private void extractEdges(IntArrayDeque nodes, ArrayList<BBBundleEdge> upEdges, ArrayList<BBBundleEdge> downEdges, int P, int coreSize, double minLen, double maxLen, double maxRatio) {
        int edgeCount = 0;
        for (IntCursor ic : nodes) {
            int nodeId = ic.value;
            int nodeRank = graph.getRank(nodeId);
            // UpOut edges
            for (int upEdgeNum = graph.getOutEdgeCount(nodeId) - 1; upEdgeNum >= 0; --upEdgeNum) {
                int edgeId = graph.getOutEdgeId(nodeId, upEdgeNum);
                int trgtId = graph.getTarget(edgeId);
                int trgtRank = graph.getRank(trgtId);

                // Out edges are sorted by target rank ascending, mind we're going down
                if (trgtRank < P || trgtRank < nodeRank) {
                    break;
                }
                int srcIdMapped = (nodeId >= coreSize) ? mappedIds[nodeId] : nodeId;
                int trgtIdMapped = (trgtId >= coreSize) ? mappedIds[trgtId] : trgtId;
                BBBundleEdge e = new BBBundleEdge(edgeId, srcIdMapped, trgtIdMapped, graph.getDist(edgeId));
                unpack(edgeId, e.unpacked, minLen, maxLen, maxRatio);
                upEdges.add(e);
                edgeCount++;
            }

            // Down-In edges
            for (int downEdgeNum = 0; downEdgeNum < graph.getInEdgeCount(nodeId); ++downEdgeNum) {
                int edgeId = graph.getInEdgeId(nodeId, downEdgeNum);
                int srcId = graph.getSource(edgeId);

                int srcRank = graph.getRank(srcId);
                // In edges are sorted by source rank descending
                if (srcRank < P || srcRank < nodeRank) {
                    break;
                }
                int srcIdMapped = (srcId >= coreSize) ? mappedIds[srcId] : srcId;
                int trgtIdMapped = (nodeId >= coreSize) ? mappedIds[nodeId] : nodeId;
                BBBundleEdge e = new BBBundleEdge(edgeId, srcIdMapped, trgtIdMapped, graph.getDist(edgeId));
                unpack(edgeId, e.unpacked, minLen, maxLen, maxRatio);
                downEdges.add(e);
                edgeCount++;
            }
        }
        drawn.clear();
        log.info(edgeCount + " edges");
    }

    @Override
    public void compute(ComputeRequest request) throws ComputeException, Exception {
        BBBundleRequestData req = (BBBundleRequestData) request.getRequestData();
        BoundingBox bbox = req.getBbox();

        long start = System.nanoTime();
        IntArrayList bboxNodes = new IntArrayList();
        int currNodeCount;
        int level;
        if (req.mode == BBBundleRequestData.LevelMode.AUTO) {

            level = graph.getMaxRank();
            do {
                level = level - 10;
                bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
                currNodeCount = bboxNodes.size();
            } while (level > 0 && currNodeCount < req.nodeCount);

        } else if (req.mode == BBBundleRequestData.LevelMode.HINTED) {

            level = graph.getMaxRank();
            do {
                level = level - 10;
                bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
                currNodeCount = bboxNodes.size();
            } while (level > 0 && currNodeCount < req.nodeCount);
            log.info("AutoLevel was: " + level);
            level = (req.getHintLevel() + level) / 2;
            bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);

        } else { // else if (req.mode == BBPrioLimitedRequestData.LevelMode.EXACT){
            level = req.getHintLevel();
            bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
        }

        log.info("ExtractBBox took " + (double) (System.nanoTime() - start) / 1000000.0 + " ms");
        log.info("Level was: " + level);
        log.info("Nodes extracted: " + bboxNodes.size() + " of " + graph.getNodeCount());
        start = System.nanoTime();
        IntArrayDeque nodes = topoSortNodes(bboxNodes, level, req.getCoreSize());
        log.info("TopSort took " + (double) (System.nanoTime() - start) / 1000000.0 + " ms");
        log.info("Nodes after TopSort: (with coreSize = " + req.getCoreSize() + ") " + nodes.size());


        start = System.nanoTime();
        ArrayList<BBBundleEdge> upEdges = new ArrayList<>();
        ArrayList<BBBundleEdge> downEdges = new ArrayList<>();
        extractEdges(nodes, upEdges, downEdges, level, req.getCoreSize(), req.getMinLen(), req.getMaxLen(), req.getMaxRatio());

        log.info("Extract edges took " + (double) (System.nanoTime() - start) / 1000000.0 + " ms");
        log.info("UpEdges: " + upEdges.size() + ", downEdges: " + downEdges.size());
        request.setResultObject(new BBBundleResult(graph, nodes.size(), upEdges, downEdges));
    }
}
