package de.tourenplaner.algorithms;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.graphrep.GraphRep;

/**
 * @author Niklas Schnelle
 *         <p/>
 *         Algorithm that ouptuts an CH down-Graph for the supplied points
 */
public class DownGraphPacket extends GraphAlgorithm {

    private final DijkstraStructs ds;

    public DownGraphPacket(GraphRep graph, DijkstraStructs resourceSharer) {
        super(graph);
        ds = resourceSharer;
    }


    /**
     * Marks the G_down edges by doing a BFS from the target node for
     * consideration !G_down edges are always before G_up edges! That's why we
     * can break the inner loop early
     *
     * @param markedEdges
     * @param targetId
     * @throws IllegalAccessException
     */
    protected final void bfsMarkDown(IntArrayList markedEdges, int targetId) throws IllegalAccessException {
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
                    // Mark the edge
                    markedEdges.add(edgeId);
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
        System.out.println("BFS_down space: " + bfsNodes + " nodes and " + bfsEdges + " edges");
        ds.returnVisitedSet();
        ds.returnDeque();
    }

    @Override
    public void compute(ComputeRequest req) throws ComputeException, Exception {
        RequestPoints points = req.getPoints();
        if (points.size() < 1) throw new ComputeException("Not enough points, need at least 1");

        points.setIdsFromGraph(graph);

        IntArrayList edges = new IntArrayList();
        int pointId;
        for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
            pointId = points.getPointId(pointIndex);
            bfsMarkDown(edges, pointId);
        }

    }
}
