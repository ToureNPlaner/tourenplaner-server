package utils;

import graphrep.Graphrep;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Graphloader {
	public static Graphrep loadGraph(String filename) throws IOException {
		Graphrep g;
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fin);
		try {
			g = (Graphrep) ois.readObject();

			ois.close();
			return g;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
