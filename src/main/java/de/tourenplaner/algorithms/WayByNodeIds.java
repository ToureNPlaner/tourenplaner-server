package de.tourenplaner.algorithms;

import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.computecore.Way;
import de.tourenplaner.computecore.WayResult;
import de.tourenplaner.graphrep.GraphRep;

import java.util.logging.Logger;

/**
 * @author Niklas Schnelle
 * this algorithm computes a WayResult from a list of nodeIds
 * by following the edges.
 */
public class WayByNodeIds extends GraphAlgorithm {

    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");
    private DijkstraStructs ds;

    public WayByNodeIds(GraphRep graph, DijkstraStructs ds) {
        super(graph);
        this.ds = ds;
    }

    private WayResult unpackEdges(IntArrayDeque edges) {
        int edgeId, shortedEdge1, shortedEdge2, currNode;
        int nodeLat, nodeLon, length = 0;
        RequestPoints points = new RequestPoints();
        WayResult result = new WayResult(points, null);
        Way resultWay = new Way();

        if(!edges.isEmpty()){
            // Add first and last node as points
            int firstNode = graph.getSource(edges.getFirst());
            int lastNode = graph.getTarget(edges.getLast());
            points.addPoint(graph.getNodeLat(firstNode), graph.getNodeLon(firstNode));
            points.addPoint(graph.getNodeLat(lastNode), graph.getNodeLon(lastNode));
            while (!edges.isEmpty()) {
                // Get the top edge and check if it's a shortcut that needs
                // further
                // unpacking
                edgeId = edges.removeFirst();
                shortedEdge1 = graph.getFirstShortcuttedEdge(edgeId);
                if (shortedEdge1 != -1) {
                    // We have a shortcut unpack it
                    shortedEdge2 = graph.getSecondShortcuttedEdge(edgeId);
                    edges.addFirst(shortedEdge2);
                    edges.addFirst(shortedEdge1);
                } else {
                    // No shortcut remember it
                    currNode = graph.getSource(edgeId);
                    nodeLat = graph.getNodeLat(currNode);
                    nodeLon = graph.getNodeLon(currNode);
                    resultWay.addPoint(nodeLat, nodeLon);
                    length += graph.getEuclidianDist(edgeId);
                }
            }
            // Add destination node
            nodeLat = graph.getNodeLat(lastNode);
            nodeLon = graph.getNodeLon(lastNode);
            resultWay.addPoint(nodeLat, nodeLon);

            resultWay.setDistance(length);
        }
        result.getResultWays().add(resultWay);
        return result;
    }

    private void findEdges(IntArrayDeque deque,NodeIdsRequestData req) throws ComputeException{
        IntArrayList nodeIds = req.getNodeIds();
        int currNode, nextNode, currEdge, currTarget;
        for(int i=0; i < nodeIds.size()-1; i++){
            currNode = nodeIds.get(i);
            nextNode = nodeIds.get(i+1);
            int numEdges = graph.getOutEdgeCount(currNode);
            boolean found = false;
            for(int j = 0; j < numEdges; j++ ){
                currEdge = graph.getOutEdgeId(currNode, j);
                currTarget = graph.getTarget(currEdge);
                if(nextNode == currTarget){
                    found = true;
                    deque.addLast(currEdge);
                    break;
                }
            }
            if(!found){
                throw new ComputeException("Couldn't find edge from "+currNode+" to "+nextNode);
            }
        }
        return;
    }

    @Override
    public void compute(ComputeRequest request) throws ComputeException, Exception {
        assert request != null;
        log.warning("WayByNodeIds called");
        NodeIdsRequestData req = (NodeIdsRequestData) request.getRequestData();
        IntArrayDeque deque = ds.borrowDeque();
        try {
            findEdges(deque, req);
            WayResult res = unpackEdges(deque);
            request.setResultObject(res);
        } finally {
            ds.returnDeque();
        }

    }
}
