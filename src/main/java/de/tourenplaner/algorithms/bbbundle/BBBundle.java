package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntStack;
import com.carrotsearch.hppc.cursors.IntCursor;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.PrioAlgorithm;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.graphrep.BoundingBoxPriorityTree;
import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.utils.Timing;

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

    private final EdgeUnpacker unpacker;

    private final int[] dfsState;
    private final int[] mappedIds;

    private final IntArrayList needClear;

    public BBBundle(GraphRep graph, BoundingBoxPriorityTree prioDings) {
        super(graph, prioDings);
        dfsState = new int[graph.getNodeCount()];
        mappedIds = new int[graph.getNodeCount()];
        needClear = new IntArrayList();
        unpacker = new EdgeUnpacker(graph);
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


    private void extractEdges(IntArrayDeque nodes, ArrayList<BBBundleEdge> upEdges, ArrayList<BBBundleEdge> downEdges,
                              IntArrayList verticesToDraw, IntArrayList edgesToDraw,
                              BoundingBox bbox, int coreSize, double minLen, double maxLen, double maxRatio) {
        int edgeCount = 0;
        // Reset to -1
        unpacker.reset();
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
                unpacker.unpack(e, verticesToDraw, edgesToDraw, bbox, minLen, maxLen, maxRatio);
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
                assert (srcId < coreSize) || mappedIds[nodeId] < mappedIds[srcId]; // topological order, trgt -> src

                int srcIdMapped = (srcId >= coreSize) ? mappedIds[srcId] : srcId;
                int trgtIdMapped = mappedIds[nodeId];

                BBBundleEdge e = new BBBundleEdge(edgeId, srcIdMapped, trgtIdMapped, graph.getDist(edgeId));
                unpacker.unpack(e, verticesToDraw, edgesToDraw, bbox, minLen, maxLen, maxRatio);
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
                bboxNodes = prioDings.getNodeSelection(bbox, level);
                currNodeCount = bboxNodes.size();
            } while (level > 0 && currNodeCount < req.getNodeCount());

        } else if (req.getMode() == BBBundleRequestData.LevelMode.HINTED) {

            level = graph.getMaxRank();
            do {
                level = level - 10;
                bboxNodes = prioDings.getNodeSelection(bbox, level);
                currNodeCount = bboxNodes.size();
            } while (level > 0 && currNodeCount < req.getNodeCount());
            log.info("AutoLevel was: " + level);
            level = (req.getLevel() + level) / 2;
            req.setLevel(level);
            bboxNodes = prioDings.getNodeSelection(bbox, level);

        } else { // else if (req.mode == BBPrioLimitedRequestData.LevelMode.EXACT){
            level = req.getLevel();
            bboxNodes = prioDings.getNodeSelection(bbox, level);
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
        IntArrayList verticesToDraw = new IntArrayList();
        IntArrayList edgesToDraw = new IntArrayList();
        extractEdges(nodes, upEdges, downEdges, verticesToDraw, edgesToDraw, req.getBbox(), req.getCoreSize(), req.getMinLen(), req.getMaxLen(), req.getMaxRatio());

        log.info(Timing.took("Extracting edges", start));
        log.info("UpEdges: " + upEdges.size() + ", downEdges: " + downEdges.size());
        request.setResultObject(new BBBundleResult(graph, nodes, verticesToDraw, edgesToDraw, upEdges, downEdges, req));
    }
}
