package algorithms;

import com.carrotsearch.hppc.BitSet;
import computecore.ComputeRequest;
import computecore.RequestPoints;
import graphrep.GraphRep;

import java.util.HashMap;
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
            distmat = computeDistMatrix(points);
            debugMat.put("distMat", distmat);
            req.setMisc(debugMat);
        } catch (IllegalAccessException e) {
            throw new ComputeException("Illegal Access: " + e.getMessage());
        }
    }
}
