package graphrep;


import org.junit.Test;

public class GridNNTest extends NNTest{

    @Test
    public void testGetIdForCoordinates() {
        NNSearcher searcher = new GridNN(graph);
        testNNSearcher(searcher);
    }
}
