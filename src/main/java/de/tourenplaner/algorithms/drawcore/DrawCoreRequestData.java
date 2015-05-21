package de.tourenplaner.algorithms.drawcore;

import de.tourenplaner.algorithms.bbbundle.BBBundleRequestData;
import de.tourenplaner.computecore.RequestData;

/**
 * Request for a drawable core graph
 *
 * Created by niklas on 30.03.15.
 */
public class DrawCoreRequestData extends RequestData {

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getNodeCount() {return nodeCount;}

    public BBBundleRequestData.LevelMode getMode() {return mode;}

    private int level;
    private final int nodeCount;
    private final BBBundleRequestData.LevelMode mode;
    private final double minLen;
    private final double maxLen;
    private final double maxRatio;

    public double getMinLen(){ return minLen;}

    public double getMaxLen(){ return maxLen;}

    public double getMaxRatio(){ return maxRatio;}

    public DrawCoreRequestData(String algSuffix, int nodeCount, BBBundleRequestData.LevelMode mode, double minLen, double maxLen, double maxRatio) {
        super(algSuffix);
        this.nodeCount = nodeCount;
        this.mode = mode;
        this.minLen = minLen;
        this.maxLen = maxLen;
        this.maxRatio = maxRatio;
    }
}
