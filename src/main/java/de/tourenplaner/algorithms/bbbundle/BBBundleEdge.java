package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.algorithms.bbprioclassic.BoundingBox;
import de.tourenplaner.graphrep.GraphRep;

/**
 * Created by niklas on 30.03.15.
 */
public class BBBundleEdge {
    public final int edgeId;
    public final int srcId;
    public final int trgtId;
    public final int cost;
    public final IntArrayList unpacked;

    public BBBundleEdge(int edgeId, int srcId, int trgtId, int cost) {
        this.edgeId = edgeId;
        this.srcId = srcId;
        this.trgtId = trgtId;
        this.cost = cost;
        this.unpacked = new IntArrayList();
    }

    public static void unpack(GraphRep graph, BoundingBox bbox, int index, IntArrayList unpackIds, double minLen, double maxLen, double maxRatio) {
        /*if (drawn.get(index)) {
            return;
        }*/
        int edgeLen = graph.getEuclidianDist(index);

        int skipA = graph.getFirstShortcuttedEdge(index);

        if (skipA == -1 || edgeLen <= minLen) {
            unpackIds.add(index);
            return;
        }

        int skipB = graph.getSecondShortcuttedEdge(index);

        int srcId = graph.getSource(index);
        double x1 = graph.getXPos(srcId);
        double y1 = graph.getYPos(srcId);
        int trgtId = graph.getTarget(index);
        double x3 = graph.getXPos(trgtId);
        double y3 = graph.getYPos(trgtId);
        if(!bbox.contains((int) x1, (int) y1) && !bbox.contains((int)x3, (int)y3)){
            unpackIds.add(index);
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
                unpackIds.add(index);
                return;
            }
        }

        unpack(graph, bbox, skipA, unpackIds, minLen, maxLen, maxRatio);
        unpack(graph, bbox, skipB, unpackIds, minLen, maxLen, maxRatio);
    }
}
