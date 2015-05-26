package de.tourenplaner.algorithms.bbprioclassic;

/**
 * @author Niklas Schnelle
 *
 * This class acts as the RequestData object for Algorithms
 * using the classic protocoll
 */
public class BoundingBox {
	// x, y is upper left corner (subject to change)
	public int x;
	public int y;
	public int width;
	public int height;

	public boolean contains(int px, int py) {
		long right = x+width;
		long top = y+height;
		return (px >= x && px < right) && (py >= y && py < top);
	}

	public BoundingBox(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public BoundingBox(){}
}
