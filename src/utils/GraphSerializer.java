package utils;

import graphrep.GraphRep;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class GraphSerializer {
	public static void serialize(OutputStream dumpStream, GraphRep g)
			throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(dumpStream);
		oos.writeObject(g);
		oos.close();
		System.out.println("Successfully dumped graph");
	}
}
