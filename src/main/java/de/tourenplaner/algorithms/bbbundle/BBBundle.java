package de.tourenplaner.algorithms.bbbundle;

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
import de.tourenplaner.utils.Timing;

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
    private final IntArrayList needClear;

    public BBBundle(GraphRep graph, PrioDings prioDings) {
        super(graph, prioDings);
        dfsState = new int[graph.getNodeCount()];
        mappedIds = new int[graph.getNodeCount()];
        needClear = new IntArrayList();
    }

    private IntArrayDeque topoSortNodes(IntArrayList bboxNodes, int P, int coreSize) {
        IntArrayDeque topSorted = new IntArrayDeque(bboxNodes.size());


        for (int i = 0; i < bboxNodes.size(); ++i) {
            int bboxNodeId = bboxNodes.get(i);
            // We only want nodes that aren't in the core because we got them already
            if (bboxNodeId >= coreSize && dfsState[bboxNodeId] == UNSEEN) {
                dfs(topSorted, bboxNodeId, coreSize);
            }
        }
        int currMapId = coreSize;
        for (IntCursor ic: topSorted) {
            assert dfsState[ic.value] == COMPLETED;
            mappedIds[ic.value] = currMapId++;
        }

        for (IntCursor ic : needClear) {
            dfsState[ic.value] = UNSEEN;
        }
        needClear.clear();

        return topSorted;
    }

    private void dfs(IntArrayDeque topSorted, int bboxNodeId, int coreSize) {
        IntStack stack = new IntStack();
        dfsState[bboxNodeId] = DISCOVERED;
        needClear.add(bboxNodeId);
        stack.push(bboxNodeId);

        while (!stack.isEmpty()) {
            int nodeId = stack.peek();

            switch (dfsState[nodeId]) {
                case DISCOVERED: {
                    dfsState[nodeId] = ACTIVE;
                    int nodeRank = graph.getRank(nodeId);
                    // Up-Out edges
                    for (int upEdgeNum = graph.getOutEdgeCount(nodeId) - 1; upEdgeNum >= 0; --upEdgeNum) {
                        int edgeId = graph.getOutEdgeId(nodeId, upEdgeNum);
                        int trgtId = graph.getTarget(edgeId);
                        int trgtRank = graph.getRank(trgtId);
                        // Out edges are sorted by target rank ascending, mind we're going down
                        if (trgtRank < nodeRank) {
                            break;
                        }

                        assert dfsState[trgtId] != ACTIVE;
                        if (dfsState[trgtId] == COMPLETED || trgtId < coreSize) {
                            continue;
                        }
                        assert nodeRank <= trgtRank; // up edge
                        assert nodeId >= trgtId; // up edge + nodes sorted by rank ascending

                        dfsState[trgtId] = DISCOVERED;
                        needClear.add(trgtId);
                        stack.push(trgtId);
                    }

                    // Down-In edges
                    for (int downEdgeNum = 0; downEdgeNum < graph.getInEdgeCount(nodeId); ++downEdgeNum) {
                        int edgeId = graph.getInEdgeId(nodeId, downEdgeNum);
                        int srcId = graph.getSource(edgeId);
                        int srcRank = graph.getRank(srcId);

                        // In edges are sorted by source rank descending
                        if (srcRank < nodeRank) {
                            break;
                        }

                        assert dfsState[srcId] != ACTIVE;
                        if (dfsState[srcId] == COMPLETED || srcId < coreSize) {
                            continue;
                        }
                        assert nodeRank <= srcRank; // down edge
                        assert nodeId >= srcId; // down edge + nodes sorted by rank ascending


                        dfsState[srcId] = DISCOVERED;
                        needClear.add(srcId);
                        stack.push(srcId);
                    }
                    break;
                }
                case ACTIVE: {
                    dfsState[nodeId] = COMPLETED;
                    topSorted.addFirst(stack.pop());
                    break;
                }
                case COMPLETED: // Stale stack entry, ignore
                    stack.pop();
                    break;
                default:
                    throw new RuntimeException("Crazy dfsState "+dfsState[nodeId]);
            }
        }
    }

    public void unpack(int index, IntArrayList unpackIds, double minLen, double maxLen, double maxRatio) {
        /*if (drawn.get(index)) {
            return;
        }*/
        double edgeLen = ((double) graph.getEuclidianDist(index));

        int skipA = graph.getFirstShortcuttedEdge(index);

        if (skipA == -1 || edgeLen <= minLen) {
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
                unpackIds.add(index);
                return;
            }
        }

        unpack(skipA, unpackIds, minLen, maxLen, maxRatio);
        unpack(skipB, unpackIds, minLen, maxLen, maxRatio);
    }


    private void extractEdges(IntArrayDeque nodes, ArrayList<BBBundleEdge> upEdges, ArrayList<BBBundleEdge> downEdges, int coreSize, double minLen, double maxLen, double maxRatio) {
        int edgeCount = 0;
        for (IntCursor ic : nodes) {
            int nodeId = ic.value;
            int nodeRank = graph.getRank(nodeId);
            // Up-Out edges
            for (int upEdgeNum = graph.getOutEdgeCount(nodeId) - 1; upEdgeNum >= 0; --upEdgeNum) {
                int edgeId = graph.getOutEdgeId(nodeId, upEdgeNum);
                int trgtId = graph.getTarget(edgeId);
                int trgtRank = graph.getRank(trgtId);

                // Out edges are sorted by target rank ascending, mind we're going down
                if (trgtRank < nodeRank) {
                    break;
                }

                assert nodeRank <= trgtRank; // up edge
                assert nodeId >= trgtId; // up edge + nodes sorted by rank ascending
                assert (trgtId < coreSize) || mappedIds[nodeId] < mappedIds[trgtId];

                int srcIdMapped = mappedIds[nodeId];
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
                if (srcRank < nodeRank) {
                    break;
                }

                assert nodeRank <= srcRank; // down edge
                assert nodeId >= srcId; // down edge + nodes sorted by rank ascending
                //assert (srcId < coreSize) || mappedIds[nodeId] < mappedIds[srcId]; // topological order, trgt -> src

                int srcIdMapped = (srcId >= coreSize) ? mappedIds[srcId] : srcId;
                int trgtIdMapped = mappedIds[nodeId];

                BBBundleEdge e = new BBBundleEdge(edgeId, srcIdMapped, trgtIdMapped, graph.getDist(edgeId));
                unpack(edgeId, e.unpacked, minLen, maxLen, maxRatio);
                downEdges.add(e);
                edgeCount++;
            }
        }
        log.info(edgeCount + " edges");
    }

    private IntArrayList findBBoxNodes(BBBundleRequestData req) {
        BoundingBox bbox = req.getBbox();

        long start = System.nanoTime();
        IntArrayList bboxNodes = new IntArrayList();
        int currNodeCount;
        int level;
        if (req.getMode() == BBBundleRequestData.LevelMode.AUTO) {

            level = graph.getMaxRank();
            do {
                level = level - 10;
                req.setLevel(level);
                bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
                currNodeCount = bboxNodes.size();
            } while (level > 0 && currNodeCount < req.getNodeCount());

        } else if (req.getMode() == BBBundleRequestData.LevelMode.HINTED) {

            level = graph.getMaxRank();
            do {
                level = level - 10;
                bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
                currNodeCount = bboxNodes.size();
            } while (level > 0 && currNodeCount < req.getNodeCount());
            log.info("AutoLevel was: " + level);
            level = (req.getLevel() + level) / 2;
            req.setLevel(level);
            bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);

        } else { // else if (req.mode == BBPrioLimitedRequestData.LevelMode.EXACT){
            level = req.getLevel();
            bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
        }

        log.info(Timing.took("ExtractBBox", start));
        log.info("Level was: " + req.getLevel());
        log.info("Nodes extracted: " + bboxNodes.size() + " of " + graph.getNodeCount());
        return bboxNodes;
    }

    @Override
    public void compute(ComputeRequest request) throws ComputeException {
        BBBundleRequestData req = (BBBundleRequestData) request.getRequestData();
        IntArrayList bboxNodes = findBBoxNodes(req);
        long start;
        start = System.nanoTime();
        IntArrayDeque nodes = topoSortNodes(bboxNodes, req.getLevel(), req.getCoreSize());
        log.info(Timing.took("TopSort", start));
        log.info("Nodes after TopSort: (with coreSize = " + req.getCoreSize() + ") " + nodes.size());


        start = System.nanoTime();
        ArrayList<BBBundleEdge> upEdges = new ArrayList<>();
        ArrayList<BBBundleEdge> downEdges = new ArrayList<>();
        extractEdges(nodes, upEdges, downEdges, req.getCoreSize(), req.getMinLen(), req.getMaxLen(), req.getMaxRatio());

        log.info(Timing.took("Extracting edges", start));
        log.info("UpEdges: " + upEdges.size() + ", downEdges: " + downEdges.size());
        request.setResultObject(new BBBundleResult(graph, nodes.size(), upEdges, downEdges, req));
    }
}
