package de.tourenplaner.algorithms.bbprioclassic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tourenplaner.computecore.StreamJsonWriter;
import de.tourenplaner.graphrep.GraphRep;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * This class is used to store and send back
 * subgraphs for client side computation
 */
public class BBPrioResult implements StreamJsonWriter {

	public static class Edge {
		public int edgeId;
		public ArrayList<Integer> unpacked;

		public Edge() {
			this.unpacked = new ArrayList<Integer>();
		}
	}


	//private final GraphRep graph;
	// TODO use IntArrayList
	private ArrayList<Edge> edges;
	private GraphRep graph;

	public BBPrioResult(GraphRep graph, ArrayList<Integer> nodes, ArrayList<Edge> edges) {
		this.graph = graph;
		this.edges = edges;
	}

	@Override
	public void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException {
		JsonGenerator gen = mapper.getFactory().createGenerator(stream);
		gen.writeStartObject();
		gen.writeArrayFieldStart("edges");
		for (Edge e : edges) {
			gen.writeStartObject();
			gen.writeNumberField("src", graph.getSource(e.edgeId));
			gen.writeNumberField("trgt", graph.getTarget(e.edgeId));
			gen.writeNumberField("cost", graph.getDist(e.edgeId));
			gen.writeArrayFieldStart("draw");
			for(int d : e.unpacked){
				gen.writeNumber(d);
			}
			gen.writeEndArray();
			gen.writeEndObject();
		}
		gen.writeEndArray();
		gen.writeEndObject();
		gen.flush();
	}
}
