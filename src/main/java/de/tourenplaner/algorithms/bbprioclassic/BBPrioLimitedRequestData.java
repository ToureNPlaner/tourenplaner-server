package de.tourenplaner.algorithms.bbprioclassic;

import de.tourenplaner.computecore.RequestData;

/**
 * @author Niklas Schnelle
 */
public class BBPrioLimitedRequestData extends RequestData {
	public enum LevelMode {
		EXACT,
		AUTO,
		HINTED
	}
	protected BoundingBox bbox;

	public int getHintLevel() {
		return hintLevel;
	}

	public BoundingBox getBbox() {
		return bbox;
	}

	protected int hintLevel;
	protected int nodeCount;
	protected LevelMode mode;
	protected double minLen;
	protected double maxLen;
	protected double maxRatio;

	public double getMinLen(){ return minLen;}

	public double getMaxLen(){ return maxLen;}

	public double getMaxRatio(){ return maxRatio;}

	public BBPrioLimitedRequestData(String algSuffix) {
		super(algSuffix);
	}

	public BBPrioLimitedRequestData(String algSuffix, BoundingBox bbox, LevelMode mode, double minLen, double maxLen, double maxRatio, int nodeCount, int hintLevel){
		super(algSuffix);
		this.bbox = bbox;
		this.nodeCount = nodeCount;
		this.minLen = minLen;
		this.maxLen = maxLen;
		this.maxRatio = maxRatio;
		this.hintLevel = hintLevel;
		this.mode = mode;
	}

}
