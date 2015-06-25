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

    private final void addEdge(int edgeId, IntArrayList unpackedIndices, IntArrayList edgesToDraw) {
        edgesToDraw.add(edgeId);
        int unpackedIndex = (edgesToDraw.size() - 1);
        unpackedMap[edgeId] = unpackedIndex;
        int reverseEdgeId = graph.getReverseEdgeId(edgeId);
        if (reverseEdgeId >= 0) {
            unpackedMap[reverseEdgeId] = unpackedIndex;
        }
        unpackedIndices.add(unpackedIndex);
    }

    public final void unpack(BoundingBox bbox, int edgeId, IntArrayList unpackedIndices, IntArrayList edgesToDraw, double minLen, double maxLen, double maxRatio) {
        int srcId = graph.getSource(edgeId);
        int trgtId = graph.getTarget(edgeId);
        int x1 = graph.getXPos(srcId);
        int y1 = graph.getYPos(srcId);
        int x3 = graph.getXPos(trgtId);
        int y3 = graph.getYPos(trgtId);
        if(bbox.contains(x1, y1) || bbox.contains(x3, y3)) {
            unpackRecursive(bbox, edgeId, x1, y1, trgtId, x3, y3, unpackedIndices, edgesToDraw, minLen, maxLen, maxRatio);
        }
    }

    private final void unpackRecursive(BoundingBox bbox, int edgeId, int x1, int y1, int x3, int y3, int trgtId, IntArrayList unpackedIndices, IntArrayList edgesToDraw, double minLen, double maxLen, double maxRatio) {
        int mappedEdgeId = unpackedMap[edgeId];
        if (mappedEdgeId >= 0) {
            unpackedIndices.add(mappedEdgeId);
            return;
        }
        int edgeLen = graph.getEuclidianDist(edgeId);

        int skipA = graph.getFirstShortcuttedEdge(edgeId);

        if (skipA == -1 || edgeLen <= minLen) {
            addEdge(edgeId, unpackedIndices, edgesToDraw);
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
                addEdge(edgeId, unpackedIndices, edgesToDraw);
                return;
            }
        }
        boolean middleContained = bbox.contains(x2, y2);
        if(bbox.contains(x1, y1) || middleContained) {
            unpackRecursive(bbox, skipA,  x1, y1, middle, x2, y2, unpackedIndices, edgesToDraw, minLen, maxLen, maxRatio);
        }
        if(middleContained || bbox.contains(x3, y3)) {
            int skipB = graph.getSecondShortcuttedEdge(edgeId);
            unpackRecursive(bbox, skipB, x2, y2, trgtId, x3, y3, unpackedIndices, edgesToDraw, minLen, maxLen, maxRatio);
        }
    }

    public final void reset() {
        Arrays.fill(unpackedMap, -1);
    }
}
