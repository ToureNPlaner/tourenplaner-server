package graphrep;

import java.io.IOException;
import java.io.InputStream;

public abstract class GraphRepFactory {

	public abstract GraphRep createGraphRep(InputStream in) throws IOException;

}
