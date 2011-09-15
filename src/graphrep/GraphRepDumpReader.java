package graphrep;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class GraphRepDumpReader extends GraphRepFactory {

	@Override
	/**
	 * returns null when graph not readable
	 */
	public GraphRep createGraphRep(String filename) throws IOException {
		// TODO Auto-generated method stub
		GraphRep g = null;
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fin);
		try {
			System.out.println("Reading graph ...");
			g = (GraphRep) ois.readObject();
			ois.close();
			System.out.println("... success!");
			return g;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return g;
	}
}
