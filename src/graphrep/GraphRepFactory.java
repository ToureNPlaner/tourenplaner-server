package graphrep;

import java.io.IOException;

public abstract class GraphRepFactory {

	public abstract GraphRep createGraphRep(String filename) throws IOException;

}
