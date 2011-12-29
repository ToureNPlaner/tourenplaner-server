package algorithms;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntOpenHashSet;
import computecore.ComputeRequest;
import computecore.RequestPoint;
import computecore.RequestPoints;
import graphrep.GraphRep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Niklas Schnelle
 * Date: 12/26/11
 * Time: 4:31 PM
 */
public class TravelingSalesman extends GraphAlgorithm {

    private final DijkstraStructs ds;
    private final ShortestPathCH chdijks;

    public TravelingSalesman(GraphRep graphrep, DijkstraStructs ds) {
        super(graphrep);
        this.ds = ds;
        this.chdijks = new ShortestPathCH(graph, ds);
    }


    private int[][] computeDistMatrix(int[] points) throws IllegalAccessException {
        int[][] distmat = new int[points.length][points.length];
        BitSet markedEdges = ds.borrowMarkedSet();

        // BfsMark all points
        for (int i = 0; i < points.length; i++) {
            chdijks.bfsMark(markedEdges, points[i]);
        }

        // Calculate the distance matrix rows
        for (int i = 0; i < points.length; i++) {
            int[] dists = ds.borrowDistArray();
            // Dijkstra sets dist labels
            chdijks.dijkstraStopAtEmptyDistOnly(dists, markedEdges, points[i]);
            for (int j = 0; j < points.length; j++) {
                distmat[i][j] = dists[points[j]];
            }
            ds.returnDistArray(false);
        }
        ds.returnMarkedSet();

        return distmat;
    }

    @Override
    public void compute(ComputeRequest req) throws ComputeException {
        // Map points to ids
        RequestPoints requestPoints = req.getPoints();
        int[][] distmat;

        // DEBUG:
        Map<String, Object> debugMat = new HashMap<String, Object>();

        int[] points = new int[requestPoints.size()];
        for (int i = 0; i < points.length; i++) {
            points[i] = graph.getIdForCoordinates(requestPoints.getPointLat(i), requestPoints.getPointLon(i));
        }
        try {
            // Looks cheap but computes the n^2 matrix of distances for the given points
            distmat = computeDistMatrix(points);



            IntOpenHashSet visited = new IntOpenHashSet(points.length);
            int currIndex = 0;
            int minValue;
            int minIndex;
            List<RequestPoint> requestPointList = req.getPoints().getStore();
            List<RequestPoint> pointStore = new ArrayList<RequestPoint>(points.length);
            // add the initial point
            pointStore.add(requestPointList.get(currIndex));
            
            for (int nextIndex = 1; nextIndex < points.length; nextIndex++) {
                visited.add(currIndex);

                minValue = Integer.MAX_VALUE;
                minIndex = 0;
                for (int i = 0; i < points.length; i++) {
                    if (minValue > distmat[currIndex][i] && i != currIndex && !visited.contains(i)) {
                        minValue = distmat[currIndex][i];
                        minIndex = i;
                    }
                }
                currIndex = minIndex;
                pointStore.add(requestPointList.get(currIndex));
            }
            
            req.getPoints().setStore(pointStore);
            // Now build real paths
            chdijks.shortestPath(requestPoints, req.getResulWay());


            debugMat.put("distMat", distmat);
            req.setMisc(debugMat);
        } catch (IllegalAccessException e) {
            throw new ComputeException("Illegal Access: " + e.getMessage());
        }
    }
}
