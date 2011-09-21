package graphrep;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;

import config.ConfigManager;

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
			// TODO: unify default options?
			String textGraphFilename = ConfigManager.getInstance()
					.getEntryString("graphfilepath",
							System.getProperty("user.home") + "/germany.txt");
			System.out
					.println("Falling back to text reading from "
							+ textGraphFilename
							+ " (path provided by config file) ...");
			in = new FileInputStream(textGraphFilename);
			return (new GraphRepTextReader()).createGraphRep(in);
		}
		return g;
	}
}
