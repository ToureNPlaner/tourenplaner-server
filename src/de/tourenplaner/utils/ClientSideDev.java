package de.tourenplaner.utils;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import de.tourenplaner.algorithms.DijkstraStructs;
import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.graphrep.GraphRepBinaryReader;
import de.tourenplaner.graphrep.GridNN;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.smile.SmileFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Development test class for client side computation
 */
public class ClientSideDev {

    public static class GraphPacketBuilder {
        private DijkstraStructs ds;
        private GraphRep graph;
        public int bfsNodes, bfsEdges;

        public GraphPacketBuilder(GraphRep graphRep){
            graph =graphRep;
            ds = new DijkstraStructs(graph.getNodeCount(), graph.getEdgeCount());
            bfsEdges = 0;
            bfsNodes = 0;
        }

        protected final ClientSideGraphRep generateSubGraph(int srcId, int targetId) throws IllegalAccessException{
            IntOpenHashSet edgeIds = new IntOpenHashSet();
            ClientSideGraphRep result = new ClientSideGraphRep();
            this.bfsMarkUp(edgeIds, srcId);
            this.bfsMarkDown(edgeIds, targetId);
            for (IntCursor edgeId : edgeIds){
                result.addEdge(edgeId.value, graph.getSource(edgeId.value), graph.getTarget(edgeId.value), graph.getDist(edgeId.value));
            }
            return result;
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
        protected final void bfsMarkDown(IntOpenHashSet markedEdges, int targetId) throws IllegalAccessException {
            int edgeId;
            int currNode;
            int sourceNode;
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

        /**
         * Marks the G_up edges by doing a BFS from the target node for
         * consideration !G_down edges are always before G_up edges! That's why we
         * can't break the inner loop early
         *
         * @param markedEdges
         * @param targetId
         * @throws IllegalAccessException
         */
        protected final void bfsMarkUp(IntOpenHashSet markedEdges, int targetId) throws IllegalAccessException {
            int edgeId;
            int currNode;
            int targetNode;
            IntArrayDeque deque = ds.borrowDeque();
            BitSet visited = ds.borrowVisitedSet();
            deque.addLast(targetId);
            visited.set(targetId);
            while (!deque.isEmpty()) {
                currNode = deque.removeLast();
                bfsNodes++;
                Inner:
                for (int i = graph.getOutEdgeCount(currNode) - 1; i >= 0 ; i--) {
                    edgeId = graph.getOutEdgeId(currNode, i);
                    targetNode = graph.getTarget(edgeId);
                    // Check if G_up
                    if (graph.getRankSlope(edgeId) >= 0) {
                        bfsEdges++;
                        // Mark the edge
                        markedEdges.add(edgeId);
                        if (!visited.get(targetNode)) {
                            visited.set(targetNode);
                            // Add target for exploration
                            deque.addFirst(targetNode);
                        }
                    }
                }
            }
            System.out.println("BFS_up space: " + bfsNodes + " nodes and " + bfsEdges + " edges");
            ds.returnVisitedSet();
            ds.returnDeque();
        }
    }





    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("Usage: foo graphfile.txt.dat dumpsubgraph.txt.dat" );
            System.exit(1);
        }
        try {
            GraphRep graph = new GraphRepBinaryReader().createGraphRep(new FileInputStream(args[0]));
            graph.setNNSearcher(new GridNN(graph));

            int stgtId = graph.getIdForCoordinates(487786110, 91794440);
            int hambId = graph.getIdForCoordinates(535652780, 100013890);

            GraphPacketBuilder pbuilder = new GraphPacketBuilder(graph);
            System.out.println("Build a hash map based client graph");
            ClientSideGraphRep cg = pbuilder.generateSubGraph(stgtId, hambId);

            //TODO target needs to be added extra

            System.out.println("Write Client Graph to file "+args[1]);
            ObjectMapper mapper = new ObjectMapper(new SmileFactory());

            FileOutputStream fo = new FileOutputStream(args[1]);
            cg.writeToStream(mapper, fo);
            fo.flush();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
