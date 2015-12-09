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

package de.tourenplaner.algorithms.shortestpath;

import de.tourenplaner.algorithms.ClassicRequestData;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.GraphAlgorithm;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.computecore.Way;
import de.tourenplaner.computecore.WayResult;
import de.tourenplaner.graphrep.GraphRep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Provides an implementation of ShortestPath algorithm.
 * 
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public abstract class ShortestPath extends GraphAlgorithm {

    protected static Logger log = Logger.getLogger("de.tourenplaner.algorithms");

    // maybe use http://code.google.com/p/simplelatlng/ instead
    protected final double calcDirectDistance(double lat1, double lng1,
                                            double lat2, double lng2) {
        double earthRadius = 6370.97327862;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = (Math.sin(dLat / 2) * Math.sin(dLat / 2))
                + (Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math
                .sin(dLng / 2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;
        return dist * 1000;
    }

    public ShortestPath(GraphRep graph) {
        super(graph);
    }

    /**
     *
     * Computes the shortest path over points[0] -> points[1] -> points[2]...
     * and stores all points on the path in resultWay
     *
     * @param points
     * @param resultWays
     * @param tour
     * @return
     * @throws de.tourenplaner.algorithms.ComputeException
     * @throws IllegalAccessException
     */
    public abstract int shortestPath(RequestPoints points, List<Way> resultWays, boolean tour) throws ComputeException, IllegalAccessException;

    @Override
    public void compute(ComputeRequest request) throws ComputeException {
        assert request != null : "We ended up without a request object in run";
        ClassicRequestData req = (ClassicRequestData) request.getRequestData();
        RequestPoints points = req.getPoints();
        // Check if we have enough points to do something useful
        if (points.size() < 2) {
            throw new ComputeException("Not enough points, need at least 2");
        }

        WayResult res = new WayResult(req.getPoints(), req.getConstraints());

        List<Way> resultWays = res.getResultWays();
        int distance = 0;
        try {
            // First let's map the RequestPoints to Ids
            points.setIdsFromGraph(graph);
            // Then compute the multi hop shortest path of them
            distance = shortestPath(points, resultWays, false);
        } catch (IllegalAccessException e) {
            // If this happens there likely is a programming error
            e.printStackTrace();
        }

        // Calculate total time
        int numWays = resultWays.size();
        double totalTime = 0;
        for (int i = 0; i < numWays; i++) {
            totalTime += resultWays.get(i).getTravelTime();
        }

        Map<String, Object> misc = new HashMap<String, Object>(1);
        misc.put("distance", distance);
        misc.put("time", totalTime);
        res.setMisc(misc);

        // Save the result
        request.setResultObject(res);
    }
}
