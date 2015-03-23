package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.IntArrayList;
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
public final class BBBundleResult implements StreamJsonWriter {

    public static final class Edge {
        public final int edgeId;
        public final int srcId;
        public final int trgtId;
        public final int cost;
        public final IntArrayList unpacked;

        public Edge(int edgeId, int srcId, int trgtId, int cost) {
            this.edgeId = edgeId;
            this.srcId = srcId;
            this.trgtId = trgtId;
            this.cost = cost;
            this.unpacked = new IntArrayList();
        }
    }

    //private final GraphRep graph;
    // TODO use IntArrayList
    private final ArrayList<BBBundleResult.Edge> upEdges;
    private final ArrayList<BBBundleResult.Edge> downEdges;
    private final int nodeCount;
    private final GraphRep graph;

    public BBBundleResult(GraphRep graph, int nodeCount, ArrayList<BBBundleResult.Edge> upEdges, ArrayList<BBBundleResult.Edge> downEdges) {
        this.upEdges = upEdges;
        this.downEdges = downEdges;
        this.nodeCount= nodeCount;
        this.graph = graph;
    }

    @Override
    public void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException {
        JsonGenerator gen = mapper.getFactory().createGenerator(stream);
        gen.writeStartObject();
        gen.writeNumberField("nodeCount", nodeCount);
        gen.writeArrayFieldStart("upEdges");
        for (Edge e : upEdges) {
            gen.writeStartObject();
            gen.writeNumberField("src", e.srcId);
            gen.writeNumberField("trgt", e.trgtId);
            gen.writeNumberField("cost", e.cost);
            gen.writeArrayFieldStart("draw");
            for (int i = 0; i < e.unpacked.size(); ++i){
                writeDrawEdge(gen, e, i);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("downEdges");
        for (Edge e : downEdges) {
            gen.writeStartObject();
            gen.writeNumberField("src", e.srcId);
            gen.writeNumberField("trgt", e.trgtId);
            gen.writeNumberField("cost", e.cost);
            gen.writeArrayFieldStart("draw");
            for (int i = 0; i < e.unpacked.size(); ++i){
                writeDrawEdge(gen, e, i);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
        gen.writeEndArray();

        gen.writeEndObject();
        gen.flush();
    }

    private void writeDrawEdge(JsonGenerator gen, Edge e, int i) throws IOException {
        int edgeId = e.unpacked.get(i);
        int s = graph.getSource(edgeId);
        int t = graph.getTarget(edgeId);
        // TODO we need to save the real type
        int type = 0;
        gen.writeNumber(graph.getXPos(s));
        gen.writeNumber(graph.getYPos(s));
        gen.writeNumber(graph.getXPos(t));
        gen.writeNumber(graph.getYPos(t));
        gen.writeNumber(type);
    }
}
