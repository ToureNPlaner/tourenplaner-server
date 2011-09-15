package utils;

import graphrep.GraphRep;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Graphloader {
	public static GraphRep loadGraph(String filename) throws IOException {
		GraphRep g;
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fin);
		try {
			g = (GraphRep) ois.readObject();

			ois.close();
			return g;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
