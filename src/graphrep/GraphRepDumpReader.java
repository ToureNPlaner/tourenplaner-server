package graphrep;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;

public class GraphRepDumpReader extends GraphRepFactory {

	@Override
	public GraphRep createGraphRep(InputStream in) throws IOException {
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
		} catch (InvalidClassException e) {

			System.err
					.println("Dumped Graph version does not match the required version: "
							+ e.getMessage());

			// TODO: doesn't work, because the in channel is not at the begin.
			// mark() and reset don't work on this stream. Need solution!

			// System.out.println("Falling back to text reading...");
			//
			// GraphRepTextReader f = new GraphRepTextReader();
			// // throws the same IOException the other read would throw
			// return f.createGraphRep(in);

			System.err.println("Please restart using \"-f text\"");
			System.exit(1);

		}
		return g;
	}
}
