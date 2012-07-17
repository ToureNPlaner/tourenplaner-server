package de.tourenplaner.algorithms;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.graphrep.GraphRep;

import java.util.logging.Logger;

/**
 * @author Niklas Schnelle
 *
 * Algorithm that ouptuts an CH up-Graph for the supplied points
 *
 */
public class UpDownGraphPacket extends GraphAlgorithm {
    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");

    private final DijkstraStructs ds;

    public UpDownGraphPacket(GraphRep graph, DijkstraStructs resourceSharer) {
        super(graph);
        ds = resourceSharer;
    }

    /**
     * Marks the G_up edges by doing a BFS from the target node for
     * consideration !G_down edges are always before G_up edges! That's why we
     * can't break the inner loop early
     *
     * @param cgraph
     * @param targetId
     * @throws IllegalAccessException
     */
    protected final void bfsMarkUp(IntArrayList cgraph, int targetId) throws IllegalAccessException {
        int edgeId;
        int currNode;
        int targetNode;
        int bfsNodes = 0, bfsEdges = 0;

        IntArrayDeque deque = ds.borrowDeque();
        BitSet visited = ds.borrowVisitedSet();
        deque.addLast(targetId);
        visited.set(targetId);
        while (!deque.isEmpty()) {
            currNode = deque.removeLast();
            bfsNodes++;
            Inner:
            for (int i = graph.getOutEdgeCount(currNode) - 1; i >= 0; i--) {
                edgeId = graph.getOutEdgeId(currNode, i);
                targetNode = graph.getTarget(edgeId);
                // Check if G_up
                if (graph.getRankSlope(edgeId) >= 0) {
                    bfsEdges++;
                    // Add the edge
                    cgraph.add(edgeId);

                    if (!visited.get(targetNode)) {
                        visited.set(targetNode);
                        // Add target for exploration
                        deque.addFirst(targetNode);
                    }
                }
            }
        }
        log.fine("BFS_up space: " + bfsNodes + " nodes and " + bfsEdges + " edges");
        ds.returnVisitedSet();
        ds.returnDeque();
    }

    /**
     * Marks the G_down edges by doing a BFS from the target node for
     * consideration !G_down edges are always before G_up edges! That's why we
     * can break the inner loop early
     *
     * @param cgraph
     * @param targetId
     * @throws IllegalAccessException
     */
    protected final void bfsMarkDown(IntArrayList cgraph, int targetId) throws IllegalAccessException {
        int edgeId;
        int currNode;
        int sourceNode;
        int bfsNodes = 0, bfsEdges = 0;

        IntArrayDeque deque = ds.borrowDeque();
        BitSet visited = ds.borrowVisitedSet();
        deque.addLast(targetId);
        visited.set(targetId);
        while (!deque.isEmpty()) {
            currNode = deque.removeLast();
            bfsNodes++;
            Inner:
            for (int i = 0; i < graph.getInEdgeCount(currNode); i++) {
                edgeId = graph.getInEdgeId(currNode, i);
                sourceNode = graph.getSource(edgeId);
                // Check if G_down
                if (graph.getRankSlope(edgeId) <= 0) {
                    bfsEdges++;
                    // Add the edge
                    cgraph.add(edgeId);

                    if (!visited.get(sourceNode)) {
                        visited.set(sourceNode);
                        // Add source for exploration
                        deque.addFirst(sourceNode);
                    }
                } else {
                    break Inner;
                }
            }
        }
        log.fine("BFS_down space: " + bfsNodes + " nodes and " + bfsEdges + " edges");
        ds.returnVisitedSet();
        ds.returnDeque();
    }


    @Override
    public void compute(ComputeRequest request) throws ComputeException, Exception {
        ClassicRequestData req = (ClassicRequestData) request.getRequestData();
        RequestPoints points  = req.getPoints();
        if (points.size() != 2)
            throw new ComputeException("Not enough points, need 2");

        points.setIdsFromGraph(graph);
        long start = System.nanoTime();
        IntArrayList cgraph = new IntArrayList();
        bfsMarkUp(cgraph, points.getPointId(0));
        bfsMarkDown(cgraph, points.getPointId(1));
        log.info("Took " + (double)(System.nanoTime() - start) / 1000000.0+" ms");
        request.setResultObject(new SubgraphResult(graph, cgraph, points.getPointId(0), points.getPointId(1)));
    }
}
