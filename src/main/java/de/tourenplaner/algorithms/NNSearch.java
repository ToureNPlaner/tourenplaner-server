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

package de.tourenplaner.algorithms;

import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.computecore.WayResult;
import de.tourenplaner.graphrep.GraphRep;

/**
 * Provides an implementation of NNSearch to find nearest neighbours for given points.
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class NNSearch extends GraphAlgorithm {

	public NNSearch(GraphRep graph) {
		super(graph);
	}

	@Override
	public void compute(ComputeRequest request) throws ComputeException {
		assert request != null : "We ended up without a request object in run";
        ClassicRequestData req = (ClassicRequestData) request.getRequestData();
        WayResult res = new WayResult(req.getPoints(), req.getConstraints());

		// TODO: send error messages to client
		RequestPoints points = req.getPoints();
		// Check if we have enough points to do something useful
		if (points.size() < 1) {
			throw new ComputeException("Not enough points, need at least 1");
		}
		nearestNeighbourLookup(points);
        request.setResultObject(res);
	}


    /**
     * find the nearest neighbours to the given points in the graphrep
     *
     * @param points
     */
	public void nearestNeighbourLookup(RequestPoints points) {
		int nodeID;


		for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
			nodeID = graph.getIdForCoordinates(points.getPointLat(pointIndex),
					points.getPointLon(pointIndex));
			points.setPointLat(pointIndex, graph.getLat(nodeID));
			points.setPointLon(pointIndex, graph.getLon(nodeID));
		}
	}

}
