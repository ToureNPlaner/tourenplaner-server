/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import com.carrotsearch.hppc.DoubleArrayList;

public class Points {
	private DoubleArrayList points;

	public Points() {
		points = new DoubleArrayList();
	}

	public void addPoint(double lat, double lon) {
		points.add(lat);
		points.add(lon);
	}

	public void addEmptyPoints(int num) {
		points.resize(points.size() + num);
	}

	public double getPointLat(int index) {
		return points.get(index * 2);
	}

	public double getPointLon(int index) {
		return points.get(index * 2 + 1);
	}

	public void setPointLat(int index, double lat) {
		points.set(index * 2, lat);
	}

	public void setPointLon(int index, double lon) {
		points.set(index * 2 + 1, lon);
	}

	public int size() {
		return points.size() / 2;
	}

}