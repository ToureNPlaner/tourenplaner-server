package de.tourenplaner.graphrep;


import org.junit.Test;

public class DumbNNTest extends NNTest {
    @Test
    public void testGetIdForCoordinates() {
        NNSearcher searcher = new DumbNN(graph);
        testNNSearcher(searcher);
    }
}
