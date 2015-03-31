package de.tourenplaner.algorithms.drawcore;

import de.tourenplaner.computecore.RequestData;

/**
 * Request for a drawable core graph
 *
 * Created by niklas on 30.03.15.
 */
public class DrawCoreRequestData extends RequestData {
    public final int nodeCount;

    public DrawCoreRequestData(String algSuffix, int nodeCount) {
        super(algSuffix);
        this.nodeCount = nodeCount;
    }
}
