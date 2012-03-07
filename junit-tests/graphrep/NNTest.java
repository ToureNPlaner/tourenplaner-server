package graphrep;


import org.junit.Before;

import java.util.Random;

public abstract class NNTest {
    
    protected GraphRep graph;
    protected int minLat = Integer.MAX_VALUE, minLon = Integer.MAX_VALUE;
    protected int maxLat = Integer.MIN_VALUE, maxLon = Integer.MIN_VALUE;
    protected Random rand;


    
    protected final int nextRandomLat(){
        return rand.nextInt(maxLat - minLat + 1) + minLat;
    }

    protected final int nextRandomLon() {
        return rand.nextInt(maxLon - minLon + 1) + minLon;
    }


    protected final long sqDistToCoords(int nodeID, int lat, int lon) {
        return ((long) (graph.getNodeLat(nodeID) - lat)) * ((long) (graph.getNodeLat(nodeID) - lat)) + ((long) (graph.getNodeLon(nodeID) - lon)) * ((long) (graph.getNodeLon(nodeID) - lon));
    }


    protected final int getIdSimple(int lat, int lon){
        int nodeCount = graph.getNodeCount();
        int minId = 0;
        long minDist = Long.MAX_VALUE;
        long dist;
        for (int i = 0; i < nodeCount; i++){
            dist = sqDistToCoords(i, lat, lon);
            if (dist < minDist){
                minDist = dist;
                minId = i;
            }
        }
        return minId;
    }
    
    @Before
    public void setup(){
        graph = (new TestGraphReader()).readTestGraph();
        int nodeCount = graph.getNodeCount();
        int curr;
        // First search for min/max Lat then for min/max Lon
        for (int i = 0; i < nodeCount; i++){
            curr = graph.getNodeLat(i);
            if(curr < minLat)
                minLat = curr;
            if(curr > maxLat)
                maxLat = curr;
        }

        for (int i = 0; i < nodeCount; i++) {
            curr = graph.getNodeLat(i);
            if (curr < minLon)
                minLon = curr;
            if (curr > maxLon)
                maxLon = curr;
        }
        // Use a fixed seed to be deterministic
        rand = new Random(42);
    }

    public void testNNSearcher(NNSearcher searcher){
        int lat, lon;
        for (int i = 0; i < 10000; i++){
            lat = nextRandomLat();
            lon = nextRandomLon();
            org.junit.Assert.assertEquals("Lookup: lat=" + lat + " lon=" + lon, getIdSimple(lat, lon), searcher.getIDForCoordinates(lat, lon));
        }
    }

}
