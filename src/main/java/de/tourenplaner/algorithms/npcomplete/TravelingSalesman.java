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

package de.tourenplaner.algorithms.npcomplete;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntOpenHashSet;
import de.tourenplaner.algorithms.ClassicRequestData;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.DijkstraStructs;
import de.tourenplaner.algorithms.GraphAlgorithm;
import de.tourenplaner.algorithms.shortestpath.ShortestPathCH;
import de.tourenplaner.computecore.*;
import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.utils.StaticMath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to create instances of TravelingSalesman algorithm
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class TravelingSalesman extends GraphAlgorithm {
    private static final int SMALL = 12;

    private final DijkstraStructs ds;
    private final ShortestPathCH chdijks;

    public TravelingSalesman(GraphRep graphrep, DijkstraStructs ds) {
        super(graphrep);
        this.ds = ds;
        this.chdijks = new ShortestPathCH(graph, ds);
    }


    private int[][] computeDistMatrix(RequestPoints points) throws IllegalAccessException {
        int[][] distmat = new int[points.size()][points.size()];
        BitSet markedEdges = ds.borrowMarkedSet();

        // BfsMark all points
        IntArrayDeque toMark = ds.borrowDeque();
        for (int i = 0; i < points.size(); i++) {
            toMark.addLast(points.getPointId(i));
        }
        chdijks.bfsMarkAll(markedEdges, toMark);
        ds.returnDeque();

        // Calculate the distance matrix rows
        for (int i = 0; i < points.size(); i++) {
            int[] dists = ds.borrowDistArray();
            // Dijkstra sets dist labels
            chdijks.dijkstraStopAtEmptyDistOnly(dists, markedEdges, points.getPointId(i));
            for (int j = 0; j < points.size(); j++) {
                distmat[i][j] = dists[points.getPointId(j)];
            }
            ds.returnDistArray(false);
        }
        ds.returnMarkedSet();

        return distmat;
    }

    @Override
    public void compute(ComputeRequest request) throws ComputeException {
        ClassicRequestData req = (ClassicRequestData) request.getRequestData();
        // Map points to ids
        RequestPoints points = req.getPoints();
        if (points.size() < 2) {
            throw new ComputeException("Not enough points, need at least 2");
        }
        int[][] distmat;

        try {
            // Map our requested points to ids
            points.setIdsFromGraph(graph);

            // Looks cheap but computes the n^2 matrix of distances for the given points
            distmat = computeDistMatrix(points);
            int[] currTour;
            if (points.size() < SMALL) {
                // It's small use the Giant Hammer Method (TM)
                currTour = exactSolve(distmat);
            } else {
                // To big let the Heuristics get something nice
                currTour = nnHeuristic(distmat);
                // Improve it with twoOpt
                currTour = twoOpt(distmat, currTour);
            }
            List<RequestPoint> pointStore = new ArrayList<RequestPoint>(points.size());
            List<RequestPoint> requestPointList = points.getStore();
            // Rearrange the point store to the found bestTour
            for (int currTourEntry : currTour) {
                pointStore.add(requestPointList.get(currTourEntry));
            }
            req.getPoints().setStore(pointStore);
            WayResult res = new WayResult(req.getPoints(), req.getConstraints());
            // Now build real paths
            List<Way> resultWays = res.getResultWays();
            int distance = chdijks.shortestPath(points, resultWays, true);
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

            // Set the result
            request.setResultObject(res);

        } catch (IllegalAccessException e) {
            throw new ComputeException("Illegal Access: " + e.getMessage());
        }
    }

    private final int[] exactSolve(int[][] distmat) {
        int[] bestTour = null;
        int[] currTour = new int[distmat.length];
        // Begin with tour in original/identity permutation
        for (int i = 0; i < currTour.length; i++) {
            currTour[i] = i;
        }
        int bestLength = Integer.MAX_VALUE;
        int currLength;
        do {
            currLength = calcTourLength(distmat, currTour);
            if (currLength < bestLength) {
                bestTour = currTour.clone();
                bestLength = currLength;
            }
        } while (StaticMath.nextPerm(currTour));

        return bestTour;
    }

    private final int[] twoOpt(int[][] distmat, int[] currTour) {
        int[] shiftedTour = currTour.clone();
        int[] bestTour = shiftedTour.clone();
        int betterLength;
        int bestLength;
        betterLength = calcTourLength(distmat, bestTour);
        do {
            bestLength = betterLength;
            for (int i = 0; i < shiftedTour.length; i++) {
                for (int j = 1; j < shiftedTour.length - 1; j++) {
                    int[] neighborTour = shiftedTour.clone();
                    StaticMath.reverse(neighborTour, 0, j + 1);
                    int length = calcTourLength(distmat, neighborTour);
                    if (length < bestLength) {
                        bestTour = neighborTour.clone();
                    }

                    neighborTour = shiftedTour.clone();
                    StaticMath.reverse(neighborTour, j, shiftedTour.length);
                    length = calcTourLength(distmat, neighborTour);
                    if (length < bestLength) {
                        bestTour = neighborTour.clone();
                    }
                }
                // shift  array to the left
                int first = shiftedTour[0];
                System.arraycopy(shiftedTour, 1, shiftedTour, 0, shiftedTour.length - 1);
                shiftedTour[shiftedTour.length - 1] = first;
            }
            betterLength = calcTourLength(distmat, bestTour);
            shiftedTour = bestTour.clone();
        } while (betterLength < bestLength);

        return bestTour;
    }

    private final int calcTourLength(int[][] distmat, int[] tourPerm) {
        int length = 0;
        for (int i = 0; i < tourPerm.length - 1; i++) {
            length += distmat[tourPerm[i]][tourPerm[i + 1]];
        }
        // We have to make sure beforehand that there is an element
        // because this will be called A LOT save that check
        length += distmat[tourPerm[tourPerm.length - 1]][tourPerm[0]];
        return length;
    }

    /**
     * Computes a tour (permutation) with the Way ordered by the nearest neighbor
     * heuristic starting at the first point successively adding the nearest unvisited point
     *
     * @param distmat the n^2 matrix of distance values for the given points
     * @return
     */
    private final int[] nnHeuristic(int[][] distmat) {
        int[] tour = new int[distmat.length];
        IntOpenHashSet visited = new IntOpenHashSet(tour.length);
        int currIndex = 0;
        int minValue;
        int minIndex;
        // add the initial point
        tour[0] = 0;
        for (int nextIndex = 1; nextIndex < tour.length; nextIndex++) {
            visited.add(currIndex);

            minValue = Integer.MAX_VALUE;
            minIndex = 0;
            for (int i = 0; i < tour.length; i++) {
                if (minValue > distmat[currIndex][i] && i != currIndex && !visited.contains(i)) {
                    minValue = distmat[currIndex][i];
                    minIndex = i;
                }
            }
            currIndex = minIndex;
            tour[nextIndex] = currIndex;
        }
        return tour;
    }
}
