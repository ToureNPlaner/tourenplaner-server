package de.tourenplaner.graphrep;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.fail;

public class TestGraphReader {

    GraphRepTextReader graphRepTextReader = new GraphRepTextReader();

    public GraphRep readTestGraph() {
        String
                path_15k =
                GraphRepTextReaderTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() +
                "../data/test/15k_ch.txt";
        System.out.println("Loading graph from " + path_15k);
        try {
            return graphRepTextReader.createGraphRep(new FileInputStream(path_15k));
        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
    }

}
