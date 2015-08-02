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

    private final void addEdge(BBBundleEdge edge, int segmentEdgeId, int srcId, int trgtId, IntArrayList verticesToDraw, IntArrayList drawEdges) {
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
        float speed = (float) graph.getEuclidianDist(segmentEdgeId) / (float) graph.getDist(segmentEdgeId);
        drawEdges.add(mappedSrc, mappedTrgt, (int)(speed*100));
        int unpackedIndex = (drawEdges.size()/3 - 1);
        edgeMap[segmentEdgeId] = unpackedIndex;
        edgeClear.add(segmentEdgeId);
        int reverseEdgeId = graph.getReverseEdgeId(segmentEdgeId);
        if (reverseEdgeId >= 0) {
            edgeMap[reverseEdgeId] = unpackedIndex;
            edgeClear.add(reverseEdgeId);
        }
        edge.unpacked.add(unpackedIndex);
    }

    public final void unpack(boolean latLonMode, BBBundleEdge edge, IntArrayList verticesToDraw, IntArrayList drawEdges, BoundingBox bbox, double minLen, double maxLen, double maxRatio) {
        int srcId = graph.getSource(edge.edgeId);
        int trgtId = graph.getTarget(edge.edgeId);
        if(latLonMode){
            int lat1 = graph.getLat(srcId);
            int lon1 = graph.getLon(srcId);
            int lat3 = graph.getLat(trgtId);
            int lon3 = graph.getLon(trgtId);
            unpackRecursiveLatLon(edge, edge.edgeId, srcId, lat1, lon1, trgtId, lat3, lon3, verticesToDraw, drawEdges, bbox, minLen, maxLen, maxRatio);

        } else {
            int x1 = graph.getXPos(srcId);
            int y1 = graph.getYPos(srcId);
            int x3 = graph.getXPos(trgtId);
            int y3 = graph.getYPos(trgtId);
            unpackRecursiveXY(edge, edge.edgeId, srcId, x1, y1, trgtId, x3, y3, verticesToDraw, drawEdges, bbox, minLen, maxLen, maxRatio);
        }

    }

    private final void unpackRecursiveLatLon(BBBundleEdge edge, int segmentEdgeId, int srcId, int lat1, int lon1, int trgtId, int lat3, int lon3, IntArrayList verticesToDraw, IntArrayList drawEdges, BoundingBox bbox, double minLen, double maxLen, double maxRatio) {
        int mappedEdgeId = edgeMap[segmentEdgeId];
        if (mappedEdgeId >= 0) {
            edge.unpacked.add(mappedEdgeId);
            return;
        }
        int edgeLen = graph.getEuclidianDist(segmentEdgeId);

        int skipA = graph.getFirstShortcuttedEdge(segmentEdgeId);

        if (skipA == -1 || edgeLen <= minLen || (!bbox.contains(lat1, lon1) && !bbox.contains(lat3, lon3))) {
            addEdge(edge, segmentEdgeId, srcId, trgtId, verticesToDraw, drawEdges);
            return;
        }

        /*
        *              |lat1 lon1 1|
        * A = abs(1/2* |lat2 lon2 1|)= 1/2*Baseline*Height
        *              |lat3 lon3 1|
        *   = 1/2*((lat1*lon2+lon1*lat3+lat2*lon3) - (lon2*lat3 + lon1*lat2 + lat1*lon3))
        * Height = 2*A/Baseline
        * ratio = Height/Baseline
        * */
        int middle = graph.getTarget(skipA);
        int lat2 = graph.getLat(middle);
        int lon2 = graph.getLon(middle);

        if (edgeLen <= maxLen) {
            double A = Math.abs(0.5 * (
                    ((double) lat1 * (double) lon2 + (double) lon1 * (double) lat3 + (double) lat2 * (double) lon3)
                            - ((double)lon2 * (double) lat3 + (double) lon1 * (double)lat2 + (double) lat1 * (double) lon3)
            ));
            double ratio = 2.0 * A / (edgeLen * edgeLen);

            if (ratio <= maxRatio) {
                addEdge(edge, segmentEdgeId, srcId, trgtId, verticesToDraw, drawEdges);
                return;
            }
        }
        int skipB = graph.getSecondShortcuttedEdge(segmentEdgeId);
        unpackRecursiveLatLon(edge, skipA, srcId, lat1, lon1, middle, lat2, lon2, verticesToDraw, drawEdges, bbox, minLen, maxLen, maxRatio);
        unpackRecursiveLatLon(edge, skipB, middle, lat2, lon2, trgtId, lat3, lon3, verticesToDraw, drawEdges, bbox, minLen, maxLen, maxRatio);
    }

    private final void unpackRecursiveXY(BBBundleEdge edge, int segmentEdgeId, int srcId, int x1, int y1, int trgtId, int x3, int y3, IntArrayList verticesToDraw, IntArrayList drawEdges, BoundingBox bbox, double minLen, double maxLen, double maxRatio) {
        int mappedEdgeId = edgeMap[segmentEdgeId];
        if (mappedEdgeId >= 0) {
            edge.unpacked.add(mappedEdgeId);
            return;
        }
        int edgeLen = graph.getEuclidianDist(segmentEdgeId);

        int skipA = graph.getFirstShortcuttedEdge(segmentEdgeId);

        if (skipA == -1 || edgeLen <= minLen || (!bbox.contains(x1, y1) && !bbox.contains(x3, y3))) {
            addEdge(edge, segmentEdgeId, srcId, trgtId, verticesToDraw, drawEdges);
            return;
        }

        /*
        *              |x1 y1 1|
        * A = abs(1/2* |x2 y2 1|)= 1/2*Baseline*Height
        *              |x3 y3 1|
        *   = 1/2*((x1*y2+y1*x3+x2*y3) - (y2*x3 + y1*x2 + x1*y3))
        * Height = 2*A/Baseline
        * ratio = Height/Baseline
        * */
        int middle = graph.getTarget(skipA);
        int x2 = graph.getXPos(middle);
        int y2 = graph.getYPos(middle);

        if (edgeLen <= maxLen) {
            double A = Math.abs(0.5 * (
                    ((double) x1 * (double) y2 + (double) y1 * (double) x3 + (double) x2 * (double) y3)
                    - ((double)y2 * (double) x3 + (double) y1 * (double)x2 + (double) x1 * (double) y3)
            ));
            double ratio = 2.0 * A / (edgeLen * edgeLen);

            if (ratio <= maxRatio) {
                addEdge(edge, segmentEdgeId, srcId, trgtId, verticesToDraw, drawEdges);
                return;
            }
        }
        int skipB = graph.getSecondShortcuttedEdge(segmentEdgeId);
        unpackRecursiveXY(edge, skipA, srcId, x1, y1, middle, x2, y2, verticesToDraw, drawEdges, bbox, minLen, maxLen, maxRatio);
        unpackRecursiveXY(edge, skipB, middle, x2, y2, trgtId, x3, y3, verticesToDraw, drawEdges, bbox, minLen, maxLen, maxRatio);
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
