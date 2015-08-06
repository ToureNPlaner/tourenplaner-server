package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.IntArrayList;

/**
 * Created by niklas on 30.03.15.
 */
public class BBBundleEdge {
    public final int edgeId;
    public final int srcId;
    public final int trgtId;
    public final int cost;
    public final IntArrayList path;

    public BBBundleEdge(int edgeId, int srcId, int trgtId, int cost) {
        this.edgeId = edgeId;
        this.srcId = srcId;
        this.trgtId = trgtId;
        this.cost = cost;
        this.path = new IntArrayList();
    }
}
