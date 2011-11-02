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
	public int getIDForCoordinates(int lat, int lon) {

		int numberOfNodes = graphRep.getNodeCount();

		int pos = 0;
		long squareDistance = 0;
		long bestDistance = 0;
		if (numberOfNodes <= 0) {
			return -1;
		}

		bestDistance = ((lon - graphRep.getNodeLat(pos)
				* (lon - graphRep.getNodeLon(pos) + ((lat - graphRep
						.getNodeLat(pos)) * (lat - graphRep.getNodeLat(pos))))));
		for (int i = 1; i < numberOfNodes; i++) {
			squareDistance = ((lon - graphRep.getNodeLon(i)) * (lon - graphRep
					.getNodeLon(i)))
					+ ((lat - graphRep.getNodeLat(i)) * (lat - graphRep
							.getNodeLat(i)));
			if (squareDistance < bestDistance) {
				bestDistance = squareDistance;
				pos = i;
			}
		}
		return pos;
	}
}
