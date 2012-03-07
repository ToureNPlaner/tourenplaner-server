package graphrep;


import org.junit.Test;

public class HashNNTest extends NNTest{
    @Test
    public void testGetIdForCoordinates(){
        NNSearcher searcher = new HashNN(graph);
        testNNSearcher(searcher);
    }
}
