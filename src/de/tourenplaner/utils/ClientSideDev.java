package de.tourenplaner.utils;

import com.carrotsearch.hppc.*;
import de.tourenplaner.algorithms.DijkstraStructs;
import de.tourenplaner.algorithms.ShortestPath;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.computecore.Way;
import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.graphrep.GraphRepBinaryReader;
import de.tourenplaner.graphrep.GraphRepBinaryWriter;
import de.tourenplaner.graphrep.GridNN;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        /**
         * Marks the G_down edges by doing a BFS from the target node for
         * consideration !G_down edges are always before G_up edges! That's why we
         * can break the inner loop early
         *
         * @param markedEdges
         * @param markedNodes
         * @param targetId
         * @throws IllegalAccessException
         */
        protected final void bfsMarkDown(BitSet markedEdges, BitSet markedNodes, int targetId) throws IllegalAccessException {
            int edgeId;
            int currNode;
            int sourceNode;
            IntArrayDeque deque = ds.borrowDeque();
            BitSet visited = ds.borrowVisitedSet();
            deque.addLast(targetId);
            visited.set(targetId);
            markedNodes.set(targetId);
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
                        markedEdges.set(edgeId);
                        if (!visited.get(sourceNode)) {
                            visited.set(sourceNode);
                            markedNodes.set(sourceNode);
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
         * @param markedNodes
         * @param targetId
         * @throws IllegalAccessException
         */
        protected final void bfsMarkUp(BitSet markedEdges, BitSet markedNodes, int targetId) throws IllegalAccessException {
            int edgeId;
            int currNode;
            int targetNode;
            IntArrayDeque deque = ds.borrowDeque();
            BitSet visited = ds.borrowVisitedSet();
            deque.addLast(targetId);
            visited.set(targetId);
            markedNodes.set(targetId);
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
                        markedEdges.set(edgeId);
                        if (!visited.get(targetNode)) {
                            visited.set(targetNode);
                            markedNodes.set(targetNode);
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
            BitSet markedEdges = new BitSet();
            BitSet markedNodes = new BitSet();
            pbuilder.bfsMarkUp(markedEdges, markedNodes , stgtId);
            pbuilder.bfsMarkDown(markedEdges, markedNodes, hambId);


            IntArrayList edgeIds = new IntArrayList();
            IntArrayList nodeIds = new IntArrayList();
            IntIntOpenHashMap origEdgeIdToNewEdgeId = new IntIntOpenHashMap();
            IntIntOpenHashMap origNodeIdToNewNodeId = new IntIntOpenHashMap();

            BitSetIterator iter = markedEdges.iterator();
            int bitIndex;
            int newEdgeId=0;
            while (BitSetIterator.NO_MORE != (bitIndex = iter.nextSetBit())) {
                edgeIds.add(bitIndex);
                origEdgeIdToNewEdgeId.put(bitIndex, newEdgeId);
                newEdgeId++;
            }

            iter = markedNodes.iterator();
            int newNodeId = 0;
            while (BitSetIterator.NO_MORE != (bitIndex = iter.nextSetBit())) {
                nodeIds.add(bitIndex);
                origNodeIdToNewNodeId.put(bitIndex, newNodeId);
                newNodeId++;
            }


            System.out.println("Marked edges (union): " + edgeIds.size()+ " should be near "+pbuilder.bfsEdges);
            System.out.println("Marked nodes (union): " + nodeIds.size() + " should be less than (sum) " + pbuilder.bfsNodes);

            System.out.println("Generate a Graph from the union");
            ClientSideGraphRep sg = new ClientSideGraphRep(nodeIds.size(), edgeIds.size());
            for (int i=0; i < nodeIds.size(); i++){
                sg.setNodeData(i, graph.getNodeLat(nodeIds.get(i)), graph.getNodeLon(nodeIds.get(i)), graph.getNodeHeight(nodeIds.get(i)));
                sg.setNodeRank(i, graph.getNodeRank(nodeIds.get(i)));
                sg.setOrigNodeId(i, nodeIds.get(i));
            }

            for (int i = 0; i < edgeIds.size(); i++) {
                sg.setEdgeData(i, origNodeIdToNewNodeId.get(graph.getSource(edgeIds.get(i))),
                        origNodeIdToNewNodeId.get(graph.getTarget(edgeIds.get(i))),
                        graph.getDist(edgeIds.get(i)),
                        graph.getEuclidianDist(edgeIds.get(i)));
                sg.setShortcutData(i,
                        origEdgeIdToNewEdgeId.get(graph.getFirstShortcuttedEdge(edgeIds.get(i))),
                        origEdgeIdToNewEdgeId.get(graph.getSecondShortcuttedEdge(edgeIds.get(i))));
                sg.setOrigEdgeId(i,edgeIds.get(i));
            }

            sg.generateOffsets();
            System.out.println("Run a ShortestPath on the client side Graph, can't do unpacking");
            ShortestPath sp = new ShortestPath(sg, new DijkstraStructs(sg.getNodeCount(), sg.getEdgeCount()));
            // Build request
            RequestPoints points = new RequestPoints();
            // Stuttgart
            points.addPoint(487786110, 91794440, new HashMap<String, Object>());
            // Hamburg
            points.addPoint(535652780, 100013890, new HashMap<String, Object>());
            // First let's map the RequestPoints to Ids
            points.setIdsFromGraph(sg);
            // Add constraints object

            List<Way> resultWays = new ArrayList<Way>();
            int length = sp.shortestPath(points, resultWays, false);
            System.out.println("Length "+length);

            System.out.println("The following way was found");
            for (Way way : resultWays){
                for(int i=0; i< way.size(); i++){
                    System.out.println(way.getPointLat(i) + ", " + way.getPointLon(i));
                }
            }

            System.out.println("Write the subgraph to disk as "+args[1]);
            GraphRepBinaryWriter gwriter = new GraphRepBinaryWriter();
            FileOutputStream fo = new FileOutputStream(args[1]);
            gwriter.writeGraphRep(fo, sg);
            fo.flush();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
