package de.tourenplaner.graphrep;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class GraphSerializer implements GraphRepWriter{

    @Override
    public void writeGraphRep(OutputStream out, GraphRep graphRep) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(graphRep);
        oos.close();
        System.out.println("Successfully dumped graph");
    }
}
