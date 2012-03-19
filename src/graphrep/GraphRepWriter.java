package graphrep;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The interface implemented by classes that writing a GraphRep to an OutputStream
 */
public interface GraphRepWriter {
    void writeGraphRep(OutputStream out, GraphRep graphRep) throws IOException;
}
