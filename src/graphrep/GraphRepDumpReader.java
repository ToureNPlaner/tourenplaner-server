package graphrep;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;

public class GraphRepDumpReader extends GraphRepFactory {

	@Override
	public GraphRep createGraphRep(InputStream in) throws IOException,
			InvalidClassException {
		GraphRep g = null;
		ObjectInputStream ois = new ObjectInputStream(in);
		try {
			System.out.println("Reading graph ...");
			g = (GraphRep) ois.readObject();
			ois.close();
			System.out.println("... success!");
			return g;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return g;
		}
	}
}
