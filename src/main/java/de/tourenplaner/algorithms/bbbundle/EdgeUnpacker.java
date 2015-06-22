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
public class EdgeUnpacker {
    private final GraphRep graph;
    private final int[] unpackedMap;

    public EdgeUnpacker(GraphRep graph) {
        this.graph = graph;
        this.unpackedMap = new int[graph.getEdgeCount()];
    }

    private void addEdge(int edgeId, IntArrayList unpackedIndices, IntArrayList edgesToDraw) {
        edgesToDraw.add(edgeId);
        int unpackedIndex = (edgesToDraw.size() - 1);
        unpackedMap[edgeId] = unpackedIndex;
        int reverseEdgeId = graph.getReverseEdgeId(edgeId);
        if (reverseEdgeId > 0) {
            unpackedMap[reverseEdgeId] = unpackedIndex;
        }
        unpackedIndices.add(unpackedIndex);
    }

    public void unpack(BoundingBox bbox, int edgeId, IntArrayList unpackedIndices, IntArrayList edgesToDraw, double minLen, double maxLen, double maxRatio) {
        if (unpackedMap[edgeId] >= 0) {
            unpackedIndices.add(unpackedMap[edgeId]);
            return;
        }
        int edgeLen = graph.getEuclidianDist(edgeId);

        int skipA = graph.getFirstShortcuttedEdge(edgeId);

        if (skipA == -1 || edgeLen <= minLen) {
            addEdge(edgeId, unpackedIndices, edgesToDraw);
            return;
        }

        int srcId = graph.getSource(edgeId);
        int x1 = graph.getXPos(srcId);
        int y1 = graph.getYPos(srcId);
        int trgtId = graph.getTarget(edgeId);
        int x3 = graph.getXPos(trgtId);
        int y3 = graph.getYPos(trgtId);
        if (!bbox.contains(x1, y1) && !bbox.contains(x3, y3)) {
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
        if (edgeLen <= maxLen) {
            int middle = graph.getTarget(skipA);
            double x2d = graph.getXPos(middle);
            double y2d = graph.getYPos(middle);
            double A = Math.abs(0.5 * (
                    ((double) x1 * y2d + (double) y1 * (double) x3 + x2d * (double) y3)
                    - (y2d * (double) x3 + (double) y1 * x2d + (double) x1 * (double) y3)
            ));
            double ratio = 2.0 * A / (edgeLen * edgeLen);

            if (ratio <= maxRatio) {
                addEdge(edgeId, unpackedIndices, edgesToDraw);
                return;
            }
        }

        unpack(bbox, skipA, unpackedIndices, edgesToDraw, minLen, maxLen, maxRatio);
        int skipB = graph.getSecondShortcuttedEdge(edgeId);
        unpack(bbox, skipB, unpackedIndices, edgesToDraw, minLen, maxLen, maxRatio);
    }

    public void reset() {
        Arrays.fill(unpackedMap, -1);
    }
}
