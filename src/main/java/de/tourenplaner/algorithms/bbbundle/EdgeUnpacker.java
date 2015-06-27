package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.algorithms.bbprioclassic.BoundingBox;
import de.tourenplaner.graphrep.GraphRep;

import java.util.Arrays;

/**
 * Manages unpacking of edges and tracking which have alredy been rembebered for drawing
 * <p>
 * Created by niklas on 15.06.15.
 */
public final class EdgeUnpacker {
    private final GraphRep graph;
    private final int[] unpackedMap;

    public EdgeUnpacker(GraphRep graph) {
        this.graph = graph;
        this.unpackedMap = new int[graph.getEdgeCount()];
    }

    private final void addEdge(BBBundleEdge edge, int segmentEdgeId, IntArrayList edgesToDraw) {
        edgesToDraw.add(segmentEdgeId);
        int unpackedIndex = (edgesToDraw.size() - 1);
        unpackedMap[segmentEdgeId] = unpackedIndex;
        int reverseEdgeId = graph.getReverseEdgeId(segmentEdgeId);
        if (reverseEdgeId >= 0) {
            unpackedMap[reverseEdgeId] = unpackedIndex;
        }
        edge.unpacked.add(unpackedIndex);
    }

    public final void unpack(BBBundleEdge edge, IntArrayList edgesToDraw, BoundingBox bbox, double minLen, double maxLen, double maxRatio) {
        int srcId = graph.getSource(edge.edgeId);
        int trgtId = graph.getTarget(edge.edgeId);
        int x1 = graph.getXPos(srcId);
        int y1 = graph.getYPos(srcId);
        int x3 = graph.getXPos(trgtId);
        int y3 = graph.getYPos(trgtId);
        if(bbox.contains(x1, y1) || bbox.contains(x3, y3)) {
            unpackRecursive(edge, edge.edgeId, x1, y1, trgtId, x3, y3, edgesToDraw, bbox,  minLen, maxLen, maxRatio);
        }
    }

    private final void unpackRecursive(BBBundleEdge edge, int segmentEdgeId, int x1, int y1, int x3, int y3, int trgtId, IntArrayList edgesToDraw, BoundingBox bbox, double minLen, double maxLen, double maxRatio) {
        int mappedEdgeId = unpackedMap[segmentEdgeId];
        if (mappedEdgeId >= 0) {
            edge.unpacked.add(mappedEdgeId);
            return;
        }
        int edgeLen = graph.getEuclidianDist(segmentEdgeId);

        int skipA = graph.getFirstShortcuttedEdge(segmentEdgeId);

        if (skipA == -1 || edgeLen <= minLen) {
            addEdge(edge, segmentEdgeId, edgesToDraw);
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
                addEdge(edge, segmentEdgeId, edgesToDraw);
                return;
            }
        }
        boolean middleContained = bbox.contains(x2, y2);
        if(bbox.contains(x1, y1) || middleContained) {
            unpackRecursive(edge, skipA, x1, y1, middle, x2, y2, edgesToDraw, bbox, minLen, maxLen, maxRatio);
        }
        if(middleContained || bbox.contains(x3, y3)) {
            int skipB = graph.getSecondShortcuttedEdge(segmentEdgeId);
            unpackRecursive(edge, skipB, x2, y2, trgtId, x3, y3, edgesToDraw, bbox, minLen, maxLen, maxRatio);
        }
    }

    public final void reset() {
        Arrays.fill(unpackedMap, -1);
    }
}
