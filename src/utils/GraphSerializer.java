package utils;

import graphrep.Graphrep;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class GraphSerializer {
	public static void main(String[] args) {
		Graphrep g = null;
		try {
			String filename = (args.length == 1 && args[0] != null) ? args[0]
					: System.getProperty("user.home")
							+ "/serializedGraphrep.dat";
			g = new Graphrep();
			FileOutputStream fout = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(g);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
