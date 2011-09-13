package graphrep;

/**
 * does the same as KDTreeNN, but dumber (linear search)
 * 
 * @author Christoph Haag
 */
public class DumbNN implements NNSearcher {
	private static final long serialVersionUID = 1L;
	Graphrep graphrep;

	public DumbNN(Graphrep graphrep) {
		this.graphrep = graphrep;
	}

	@Override
	public int getIDForCoordinates(float lat, float lon) {

		int numberOfNodes = graphrep.getNodeCount();

		int pos = 0;
		float squareDistance = 0;
		float bestDistance = 0;
		if (numberOfNodes <= 0) {
			return -1;
		}

		bestDistance = (lon - graphrep.lon[pos]) * (lon - graphrep.lon[pos])
				+ (lat - graphrep.lat[pos]) * (lat - graphrep.lat[pos]);
		for (int i = 1; i < numberOfNodes; i++) {
			squareDistance = (lon - graphrep.lon[i]) * (lon - graphrep.lon[i])
					+ (lat - graphrep.lat[i]) * (lat - graphrep.lat[i]);
			if (squareDistance < bestDistance) {
				bestDistance = squareDistance;
				pos = i;
			}
		}
		return pos;
	}
}
