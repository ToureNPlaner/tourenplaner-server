/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import org.json.simple.JSONAware;

public class Points implements JSONAware {
	private final double[] lats;
	private final double[] lons;
	StringBuilder sb;

	public Points(double[] lats, double[] lons) {
		this.lats = lats;
		this.lons = lons;
		sb = new StringBuilder();
	}
	
	@Override
	public String toJSONString() {
		sb.append("[");
		for (int i = 0; i < (lats.length - 1); i++) {
			sb.append("{\"lt\":" + lats[i] + ",\"ln\":" + lons[i] + "},");
		}
		sb.append("{\"lt\":" + lats[lats.length - 1] + ",\"ln\":"
				+ lons[lats.length - 1] + "}");
		sb.append("]");
		return sb.toString();
	}
}