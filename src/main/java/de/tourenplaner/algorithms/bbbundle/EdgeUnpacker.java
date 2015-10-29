package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.graphrep.GraphRep;

import java.util.Arrays;

/**
 * Manages unpacking of edges and tracking which have alredy been rembebered for drawing
 * <p>
 * Created by niklas on 15.06.15.
 */
public final class EdgeUnpacker {
    private final GraphRep graph;
    private final int[] edgeMap;
    private final int[] nodeMap;

    private final IntArrayList nodeClear;
    private final IntArrayList edgeClear;

    public EdgeUnpacker(GraphRep graph) {
        this.graph = graph;
        this.edgeMap = new int[graph.getEdgeCount()];
        this.nodeMap = new int[graph.getNodeCount()];
        this.nodeClear = new IntArrayList();
        this.edgeClear = new IntArrayList();
        Arrays.fill(nodeMap, -1);
        Arrays.fill(edgeMap, -1);
    }

    private void addEdge(BBBundleEdge edge, int segmentEdgeId, int srcId, int trgtId, int P, IntArrayList verticesToDraw, IntArrayList drawEdges) {
        int mappedSrc = nodeMap[srcId];
        if (mappedSrc < 0){
            verticesToDraw.add(srcId);
            mappedSrc = verticesToDraw.size() - 1;
            nodeMap[srcId] = mappedSrc;
            nodeClear.add(srcId);
        }
        int mappedTrgt = nodeMap[trgtId];
        if (mappedTrgt < 0){
            verticesToDraw.add(trgtId);
            mappedTrgt = verticesToDraw.size() - 1;
            nodeMap[trgtId] = mappedTrgt;
            nodeClear.add(trgtId);
        }
        // TODO proper edge types
        int speed = (int) ((float) (graph.getEuclidianDist(segmentEdgeId)*13) / (float) graph.getDist(segmentEdgeId));
        drawEdges.add(mappedSrc, mappedTrgt, speed);
        int unpackedIndex = (drawEdges.size()/3 - 1);
        edge.path.add(unpackedIndex);
        // If we are dealing with a shortcut that shortcuts a node above P
        // we need to remember that we kept the parent for all it's children down to P
        // this prevents drawing them as well
        propagateToChildren(segmentEdgeId, unpackedIndex, P);
    }

    private void propagateToChildren(int segmentEdgeId, int unpackedIndex, int P) {
        if(edgeMap[segmentEdgeId] >= 0){
            return;
        }
        edgeMap[segmentEdgeId] = unpackedIndex;
        edgeClear.add(segmentEdgeId);
        int reverseEdgeId = graph.getReverseEdgeId(segmentEdgeId);
        if (reverseEdgeId >= 0) {
            edgeMap[reverseEdgeId] = unpackedIndex;
            edgeClear.add(reverseEdgeId);
        }

        /*int skipA = graph.getFirstShortcuttedEdge(segmentEdgeId);
        if(skipA >= 0) {
            int skipId = graph.getTarget(skipA);
            // For the core we already skip shortcuts above coreSize
            if (graph.getRank(skipId) >= P) {
                propagateToChildren(skipA, unpackedIndex, P);
                int skipB = graph.getSecondShortcuttedEdge(segmentEdgeId);
                propagateToChildren(skipB, unpackedIndex, P);
            }
        }*/
    }

    public final void unpack(boolean latLonMode, BBBundleEdge edge, IntArrayList verticesToDraw, IntArrayList drawEdges, int P, BoundingBox bbox, double minLen, double maxLen, double maxRatio) {
        if(latLonMode){
            unpackRecursiveLatLon(edge, edge.edgeId, verticesToDraw, drawEdges, P, bbox, minLen, maxLen, maxRatio);

        } else {
            unpackRecursiveXY(edge, edge.edgeId, verticesToDraw, drawEdges, P,  bbox, minLen, maxLen, maxRatio);
        }

    }

    private final void unpackRecursiveLatLon(BBBundleEdge edge, int segmentEdgeId, IntArrayList verticesToDraw, IntArrayList drawEdges, int P,  BoundingBox bbox, double minLen, double maxLen, double maxRatio) {

        int mappedEdgeId = edgeMap[segmentEdgeId];
        if (mappedEdgeId >= 0) {
            edge.path.add(mappedEdgeId);
            return;
        }
        int edgeLen = graph.getEuclidianDist(segmentEdgeId);

        int srcId = graph.getSource(segmentEdgeId);
        int trgtId = graph.getTarget(segmentEdgeId);
        int skipA = graph.getFirstShortcuttedEdge(segmentEdgeId);
        if (skipA == -1){
            addEdge(edge, segmentEdgeId, srcId, trgtId, P, verticesToDraw, drawEdges);
            return;
        }
        int skipId = graph.getTarget(skipA);
        int skipB = graph.getSecondShortcuttedEdge(segmentEdgeId);
        int lat1 = graph.getLat(srcId);
        int lon1 = graph.getLon(srcId);
        int lat3 = graph.getLat(trgtId);
        int lon3 = graph.getLon(trgtId);

        // TODO figure out how to keep these for paths
        if(bbox != null &&  !bbox.contains(lat1, lon1) && !bbox.contains(lat3, lon3)){
            return;
        }

        if (edgeLen <= minLen) {
            addEdge(edge, segmentEdgeId, srcId, trgtId, P, verticesToDraw, drawEdges);
            return;
        }


        if (edgeLen <= maxLen) {
            int lat2 = graph.getLat(skipId);
            int lon2 = graph.getLon(skipId);
            double ratio = edgeBendingRatio(edgeLen, lon1, lat1, lon3, lat3, lon2, lat2);
            if (ratio <= maxRatio) {
                addEdge(edge, segmentEdgeId, srcId, trgtId, P, verticesToDraw, drawEdges);
                return;
            }
        }

        unpackRecursiveLatLon(edge, skipA, verticesToDraw, drawEdges, P, bbox, minLen, maxLen, maxRatio);
        unpackRecursiveLatLon(edge, skipB, verticesToDraw, drawEdges, P, bbox, minLen, maxLen, maxRatio);
    }



    private void unpackRecursiveXY(BBBundleEdge edge, int segmentEdgeId, IntArrayList verticesToDraw, IntArrayList drawEdges, int P,  BoundingBox bbox, double minLen, double maxLen, double maxRatio) {
        int mappedEdgeId = edgeMap[segmentEdgeId];
        if (mappedEdgeId >= 0) {
            edge.path.add(mappedEdgeId);
            return;
        }
        int edgeLen = graph.getEuclidianDist(segmentEdgeId);


        int srcId = graph.getSource(segmentEdgeId);
        int trgtId = graph.getTarget(segmentEdgeId);
        int skipA = graph.getFirstShortcuttedEdge(segmentEdgeId);
        if (skipA == -1){
            addEdge(edge, segmentEdgeId, srcId, trgtId, P, verticesToDraw, drawEdges);
            return;
        }

        int skipId = graph.getTarget(skipA);
        int x1 = graph.getXPos(srcId);
        int y1 = graph.getYPos(srcId);
        int x3 = graph.getXPos(trgtId);
        int y3 = graph.getYPos(trgtId);
        if (edgeLen <= minLen || (bbox != null && !bbox.contains(x1, y1) && !bbox.contains(x3, y3))) {
            addEdge(edge, segmentEdgeId, srcId, trgtId, P, verticesToDraw, drawEdges);
            return;
        }


        if (edgeLen <= maxLen) {
            int x2 = graph.getXPos(skipId);
            int y2 = graph.getYPos(skipId);
            double ratio = edgeBendingRatio(edgeLen, x1, y1, x3, y3, x2, y2);
            if (ratio <= maxRatio) {
                addEdge(edge, segmentEdgeId, srcId, trgtId, P, verticesToDraw, drawEdges);
                return;
            }
        }

        int skipB = graph.getSecondShortcuttedEdge(segmentEdgeId);
        unpackRecursiveXY(edge, skipA, verticesToDraw, drawEdges, P, bbox, minLen, maxLen, maxRatio);
        unpackRecursiveXY(edge, skipB, verticesToDraw, drawEdges, P, bbox, minLen, maxLen, maxRatio);
    }

    private static double edgeBendingRatio(int edgeLen, double x1, double y1, double x3, double y3, double x2, double y2) {
        /*
        *              |x1 y1 1|
        * A = abs(1/2* |x2 y2 1|)= 1/2*Baseline*Height
        *              |x3 y3 1|
        *   = 1/2*((x1*y2+y1*x3+x2*y3) - (y2*x3 + y1*x2 + x1*y3))
        * Height = 2*A/Baseline
        * ratio = Height/Baseline
        * */
        double A = Math.abs(0.5 * (
                (x1 * y2 + y1 * x3 + x2 * y3)
                        - (y2 * x3 + y1 * x2 + x1 * y3)
        ));
        return 2.0 * A / (edgeLen * edgeLen);
    }

    public final void reset() {
        for(int i = 0; i < edgeClear.size(); ++i){
            edgeMap[edgeClear.get(i)] = -1;
        }

        for(int i = 0; i < nodeClear.size(); ++i){
            nodeMap[nodeClear.get(i)] = -1;
        }
        nodeClear.clear();
        edgeClear.clear();
    }
}
