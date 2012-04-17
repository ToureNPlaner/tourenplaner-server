/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.graphrep;

/**
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 * does the same as KDTreeNN, but dumber (linear search)
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
