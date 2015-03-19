package de.tourenplaner.algorithms.bbbundle;

import de.tourenplaner.algorithms.bbprioclassic.BoundingBox;
import de.tourenplaner.computecore.RequestData;

/**
 * Created by niklas on 19.03.15.
 */
public class BBBundleRequestData extends RequestData {
    protected BoundingBox bbox;



    public BoundingBox getBbox() {
        return bbox;
    }

    protected int minLevel;

    protected double minLen;
    protected double maxLen;
    protected double maxRatio;

    public double getMinLen(){ return minLen;}

    public double getMaxLen(){ return maxLen;}

    public double getMaxRatio(){ return maxRatio;}

    public int getMinLevel() {
        return minLevel;
    }

    public BBBundleRequestData(String algSuffix) {
        super(algSuffix);
    }

    public BBBundleRequestData(String algSuffix, BoundingBox bbox, double minLen, double maxLen, double maxRatio, int minLevel){
        super(algSuffix);
        this.bbox = bbox;
        this.minLevel = minLevel;
        this.minLen = minLen;
        this.maxLen = maxLen;
        this.maxRatio = maxRatio;
    }
}
