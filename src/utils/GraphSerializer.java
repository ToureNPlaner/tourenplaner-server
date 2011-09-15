package utils;

import graphrep.GraphRep;
import graphrep.GraphRepTextReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import config.ConfigManager;

public class GraphSerializer {
	public static void main(String[] args) {
		ConfigManager cm = ConfigManager.getInstance();
		GraphRep g = null;
		try {
			String filename = cm.getEntryString("graphfilepath",
					System.getProperty("user.home") + "/germany.txt");
			String dumpedfilename = filename + ".dat";
			g = new GraphRepTextReader().createGraphRep(filename);

			FileOutputStream fout = new FileOutputStream(dumpedfilename);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(g);
			oos.close();
			System.out.println("Dumped graph to " + dumpedfilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
