package de.tourenplaner.graphrep;

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

	private final long sqDistToCoords(int nodeID, int lat, int lon) {
		return ((long) (graphRep.getNodeLat(nodeID) - lat))
				* ((long) (graphRep.getNodeLat(nodeID) - lat))
				+ ((long) (graphRep.getNodeLon(nodeID) - lon))
				* ((long) (graphRep.getNodeLon(nodeID) - lon));
	}

	@Override
	public int getIDForCoordinates(int lat, int lon) {

		int numberOfNodes = graphRep.getNodeCount();

		int pos = 0;
		long squareDistance;
		long bestDistance;
		if (numberOfNodes <= 0) {
			return -1;
		}

		bestDistance = sqDistToCoords(pos, lat, lon);
		for (int i = 1; i < numberOfNodes; i++) {
			squareDistance = sqDistToCoords(i, lat, lon);
			if (squareDistance < bestDistance) {
				bestDistance = squareDistance;
				pos = i;
			}
		}
		return pos;
	}
}
