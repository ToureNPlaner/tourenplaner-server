package de.tourenplaner.algorithms.bbbundle;

/**
 * @author Niklas Schnelle
 * BoundingBox class used for both projected x,y coordinates and lat, lon geo coordinates
 *
 *
 * */
public class BoundingBox {
	// x, y is upper left corner (subject to change)
	public int x;
	public int y;
	public int width;
	public int height;

	public boolean contains(int px, int py) {
		long right = x+width;
		long top = y+height;
		return (px >= x && px <= right) && (py >= y && py <= top);
	}

	public BoundingBox(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public BoundingBox(){}
}
