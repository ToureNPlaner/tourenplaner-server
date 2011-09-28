package graphrep;

/**
 * does the same as KDTreeNN, but dumber (linear search)
 * 
 * @author Christoph Haag
 */
public class DumbNN implements NNSearcher {
	private static final long serialVersionUID = 1L;

	GraphRep graphRep;

	public DumbNN(GraphRep graphRep) {
		this.graphRep = graphRep;
	}

	@Override
	public int getIDForCoordinates(double lat, double lon) {

		int numberOfNodes = graphRep.getNodeCount();

		int pos = 0;
		double squareDistance = 0;
		double bestDistance = 0;
		if (numberOfNodes <= 0) {
			return -1;
		}

		bestDistance = ((lon - graphRep.lon[pos]) * (lon - graphRep.lon[pos]))
				+ ((lat - graphRep.lat[pos]) * (lat - graphRep.lat[pos]));
		for (int i = 1; i < numberOfNodes; i++) {
			squareDistance = ((lon - graphRep.lon[i]) * (lon - graphRep.lon[i]))
					+ ((lat - graphRep.lat[i]) * (lat - graphRep.lat[i]));
			if (squareDistance < bestDistance) {
				bestDistance = squareDistance;
				pos = i;
			}
		}
		return pos;
	}
}
