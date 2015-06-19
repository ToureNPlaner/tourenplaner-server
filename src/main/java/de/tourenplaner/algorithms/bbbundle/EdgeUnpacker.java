package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.algorithms.bbprioclassic.BoundingBox;
import de.tourenplaner.graphrep.GraphRep;

import java.util.Arrays;

/**
 * Manages unpacking of edges and tracking which have alredy been rembebered for drawing
 *
 * Created by niklas on 15.06.15.
 */
public class EdgeUnpacker {
    private final GraphRep graph;
    private final int[] unpackedMap;

    public EdgeUnpacker(GraphRep graph) {
        this.graph = graph;
        this.unpackedMap = new int[graph.getEdgeCount()];
    }

    public void unpack(BoundingBox bbox, int edgeId, IntArrayList unpackedIndices, IntArrayList edgesToDraw, double minLen, double maxLen, double maxRatio) {
        if (unpackedMap[edgeId] >= 0 ){
            unpackedIndices.add(unpackedMap[edgeId]);
            return;
        }
        int edgeLen = graph.getEuclidianDist(edgeId);

        int skipA = graph.getFirstShortcuttedEdge(edgeId);

        if (skipA == -1 || edgeLen <= minLen) {
            edgesToDraw.add(edgeId);
            int unpackedIndex = (edgesToDraw.size() - 1);
            unpackedMap[edgeId] = unpackedIndex;
            unpackedIndices.add(unpackedIndex);
            return;
        }

        int skipB = graph.getSecondShortcuttedEdge(edgeId);

        int srcId = graph.getSource(edgeId);
        double x1 = graph.getXPos(srcId);
        double y1 = graph.getYPos(srcId);
        int trgtId = graph.getTarget(edgeId);
        double x3 = graph.getXPos(trgtId);
        double y3 = graph.getYPos(trgtId);
        if(!bbox.contains((int) x1, (int) y1) && !bbox.contains((int)x3, (int)y3)){
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
            double x2 = graph.getXPos(middle);
            double y2 = graph.getYPos(middle);
            double A = Math.abs(0.5 * ((x1 * y2 + y1 * x3 + x2 * y3) - (y2 * x3 + y1 * x2 + x1 * y3)));
            double ratio = 2.0 * A / (edgeLen * edgeLen);
            if (ratio <= maxRatio) {
                edgesToDraw.add(edgeId);
                int unpackedIndex = (edgesToDraw.size() - 1);
                unpackedMap[edgeId] = unpackedIndex;
                unpackedIndices.add(unpackedIndex);
                return;
            }
        }

        unpack(bbox, skipA, unpackedIndices, edgesToDraw, minLen, maxLen, maxRatio);
        unpack(bbox, skipB, unpackedIndices, edgesToDraw, minLen, maxLen, maxRatio);
    }

    public void reset() {
        Arrays.fill(unpackedMap, -1);
    }
}
