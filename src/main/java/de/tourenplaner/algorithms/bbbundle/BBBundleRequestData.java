package de.tourenplaner.algorithms.bbbundle;

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
    private BoundingBox bbox;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCoreSize() {return coreSize;}

    public int getNodeCountHint() {return nodeCountHint;}

    public LevelMode getMode() {return mode;}

    public boolean isLatLonMode() {return  latlon;}

    public BoundingBox getBbox() {
        return bbox;
    }

    private int level;
    private final int coreSize;
    private final int nodeCountHint;
    private final LevelMode mode;
    private final double minLen;
    private final double maxLen;
    private final double maxRatio;
    private final boolean latlon;

    public double getMinLen(){ return minLen;}

    public double getMaxLen(){ return maxLen;}

    public double getMaxRatio(){ return maxRatio;}

    public BBBundleRequestData(String algSuffix, boolean latlon, BoundingBox bbox, LevelMode mode, double minLen, double maxLen, double maxRatio, int nodeCountHint, int level, int coreSize){
        super(algSuffix);
        this.latlon = latlon;
        this.bbox = bbox;
        this.nodeCountHint = nodeCountHint;
        this.minLen = minLen;
        this.maxLen = maxLen;
        this.maxRatio = maxRatio;
        this.level = level;
        this.coreSize = coreSize;
        this.mode = mode;
    }

}
