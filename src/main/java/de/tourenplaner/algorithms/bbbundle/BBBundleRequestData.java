package de.tourenplaner.algorithms.bbbundle;

import de.tourenplaner.algorithms.bbprioclassic.BoundingBox;
import de.tourenplaner.computecore.RequestData;

/**
 * Created by niklas on 19.03.15.
 */
public final class BBBundleRequestData extends RequestData {
    public enum LevelMode {
        EXACT,
        AUTO,
        HINTED
    }
    protected BoundingBox bbox;

    public int getHintLevel() {
        return hintLevel;
    }

    public int getCoreLevel() {return coreLevel;}

    public BoundingBox getBbox() {
        return bbox;
    }

    protected final int hintLevel;
    protected final int coreLevel;
    protected final int nodeCount;
    protected final LevelMode mode;
    protected final double minLen;
    protected final double maxLen;
    protected final double maxRatio;

    public double getMinLen(){ return minLen;}

    public double getMaxLen(){ return maxLen;}

    public double getMaxRatio(){ return maxRatio;}

    public BBBundleRequestData(String algSuffix, BoundingBox bbox, LevelMode mode, double minLen, double maxLen, double maxRatio, int nodeCount, int hintLevel, int coreLevel){
        super(algSuffix);
        this.bbox = bbox;
        this.nodeCount = nodeCount;
        this.minLen = minLen;
        this.maxLen = maxLen;
        this.maxRatio = maxRatio;
        this.hintLevel = hintLevel;
        this.coreLevel = coreLevel;
        this.mode = mode;
    }

}
