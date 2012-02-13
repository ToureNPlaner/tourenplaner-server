package algorithms;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntOpenHashSet;
import computecore.ComputeRequest;
import computecore.RequestPoint;
import computecore.RequestPoints;
import graphrep.GraphRep;
import utils.StaticMath;

import java.util.*;

/**
 *  @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 * Used to create instances of TravelingSalesman algorithm
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
        for (int i = 0; i < points.size(); i++) {
            chdijks.bfsMark(markedEdges, points.getPointId(i));
        }

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
    public void compute(ComputeRequest req) throws ComputeException {
       
        // Map points to ids
        RequestPoints points = req.getPoints();
        if(points.size() < 2){
            throw new ComputeException("Not enough points, need at least 2");
        }
        int[][] distmat;




        try {
            // Map our requested points to ids
            points.setIdsFromGraph(graph);

            // Looks cheap but computes the n^2 matrix of distances for the given points
            distmat = computeDistMatrix(points);
            List<RequestPoint> pointStore;
            if (points.size() < SMALL){
                // It's small use the Giant Hammer Method (TM)
                pointStore = exactSolve(distmat, points.getStore());
            } else {
                // To big let the Heuristics get something nice
                pointStore = nnHeuristic(distmat, points.getStore());
                // Improve it with twoOpt
                pointStore = twoOpt(distmat, pointStore);
            }

            req.getPoints().setStore(pointStore);
            // Now build real paths
            int distance = chdijks.shortestPath(points, req.getResultWay(), true);
            req.getMisc().put("distance", distance);

        } catch (IllegalAccessException e) {
            throw new ComputeException("Illegal Access: " + e.getMessage());
        }
    }
    
    private final List<RequestPoint> exactSolve(int[][] distmat, List<RequestPoint> requestPointList){
        List<RequestPoint> pointStore = new ArrayList<RequestPoint>(requestPointList.size());
        int[] currTour = new int[requestPointList.size()];
        for(int i = 0; i < currTour.length; i++){
            currTour[i]=i;
        }
        int[] bestTour = null;
        int bestLength = Integer.MAX_VALUE;
        int currLength;
        do {
            currLength = calcTourLength(distmat, currTour);
            if(currLength < bestLength){
                bestTour = currTour.clone();
                bestLength = currLength;
            }
        } while(StaticMath.nextPerm(currTour));

        // Rearrange the point store to the found bestTour
        for(int i = 0; i < bestTour.length; i++){
            pointStore.add(requestPointList.get(bestTour[i]));
        }
        return  pointStore;
    }

    private final List<RequestPoint> twoOpt(int[][] distmat, List<RequestPoint> requestPointList){
        List<RequestPoint> pointStore = new ArrayList<RequestPoint>(requestPointList.size());
        int[] shiftedTour = new int[requestPointList.size()];
        for (int i = 0; i < shiftedTour.length; i++) {
            shiftedTour[i] = i;
        }

        int[] bestTour = shiftedTour.clone();
        int bestLength = calcTourLength(distmat, bestTour);
        for(int i = 0; i < shiftedTour.length; i++){
            for(int j = 1;j< shiftedTour.length-1;j++){
                int[] neighborTour = shiftedTour.clone();
                StaticMath.reverse(neighborTour, 0,j+1);
                int length = calcTourLength(distmat, neighborTour);
                if(length < bestLength){
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
            shiftedTour[shiftedTour.length-1] = first;
        }

        // Rearrange the point store to the found bestTour
        for (int i = 0; i < bestTour.length; i++) {
            pointStore.add(requestPointList.get(bestTour[i]));
        }
        return pointStore;
    }
    
    private final int calcTourLength(int[][] distmat, int[] tourPerm){
        int length = 0;
        for(int i = 0; i < tourPerm.length - 1; i++){
            length += distmat[tourPerm[i]][tourPerm[i+1]];
        }
        // We have to make sure beforehand that there is an element
        // because this will be called A LOT save that check
        length +=  distmat[tourPerm[tourPerm.length - 1]][tourPerm[0]];
        return length;
    }

    /**
     * Computes a RequestPoint List (tour) with the Points ordered by the nearest neighbor
     * heuristic starting at the first point successively adding the nearest unvisited point
     *
     *
     * @param distmat the n^2 matrix of distance values for the given points
     * @param requestPointList the list of points for which a tour should be computed
     * @return
     */
    private final List<RequestPoint> nnHeuristic(int[][] distmat, List<RequestPoint> requestPointList) {
        List<RequestPoint> pointStore = new ArrayList<RequestPoint>(requestPointList.size());
        IntOpenHashSet visited = new IntOpenHashSet(requestPointList.size());
        int currIndex = 0;
        int minValue;
        int minIndex;
        // add the initial point
        pointStore.add(requestPointList.get(currIndex));
        for (int nextIndex = 1; nextIndex < requestPointList.size(); nextIndex++) {
            visited.add(currIndex);

            minValue = Integer.MAX_VALUE;
            minIndex = 0;
            for (int i = 0; i < requestPointList.size(); i++) {
                if (minValue > distmat[currIndex][i] && i != currIndex && !visited.contains(i)) {
                    minValue = distmat[currIndex][i];
                    minIndex = i;
                }
            }
            currIndex = minIndex;
            pointStore.add(requestPointList.get(currIndex));
        }
        return pointStore;
    }
}
