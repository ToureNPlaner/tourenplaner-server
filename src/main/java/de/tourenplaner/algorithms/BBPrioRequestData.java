package de.tourenplaner.algorithms;

import de.tourenplaner.computecore.RequestData;

/**
 * @author Niklas Schnelle
 */
public class BBPrioRequestData extends RequestData {
	protected BoundingBox bbox;

	public int getMinLevel() {
		return minLevel;
	}

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

	public BBPrioRequestData(String algSuffix) {
		super(algSuffix);
	}

	public BBPrioRequestData(String algSuffix, BoundingBox bbox, double minLen, double maxLen, double maxRatio, int minLevel){
		super(algSuffix);
		this.bbox = bbox;
		this.minLevel = minLevel;
		this.minLen = minLen;
		this.maxLen = maxLen;
		this.maxRatio = maxRatio;
	}

}
