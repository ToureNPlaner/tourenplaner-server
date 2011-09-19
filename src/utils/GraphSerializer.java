package utils;

import graphrep.GraphRep;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class GraphSerializer {
	public static void serialize(String filename, GraphRep g)
			throws IOException {

		String dumpedfilename = filename + ".dat";
		FileOutputStream fout = new FileOutputStream(dumpedfilename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(g);
		oos.close();
		System.out.println("Dumped graph to " + dumpedfilename);

	}
}
