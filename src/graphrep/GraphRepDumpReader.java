package graphrep;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

public class GraphRepDumpReader implements GraphRepReader {
    private static Logger log = Logger.getLogger("tourenplaner");

	@Override
	public GraphRep createGraphRep(InputStream in) throws IOException,
			InvalidClassException {
		GraphRep g = null;
		ObjectInputStream ois = new ObjectInputStream(in);
		try {
			log.info("Loading Graphdump");
			g = (GraphRep) ois.readObject();
			ois.close();
			System.gc();
			log.info("Graph read with "+g.getNodeCount()+" nodes, "+g.getEdgeCount()+" edges");
			return g;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return g;
		}
	}
}
