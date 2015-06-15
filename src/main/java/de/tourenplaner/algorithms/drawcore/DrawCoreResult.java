package de.tourenplaner.algorithms.drawcore;

import com.carrotsearch.hppc.IntArrayList;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tourenplaner.algorithms.bbbundle.BBBundleEdge;
import de.tourenplaner.computecore.StreamJsonWriter;
import de.tourenplaner.graphrep.GraphRep;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by niklas on 30.03.15.
 */
public class DrawCoreResult implements StreamJsonWriter {

    private final ArrayList<BBBundleEdge> edges;
    private final int coreSize;
    private final GraphRep graph;
    private final IntArrayList edgesToDraw;

    public DrawCoreResult(GraphRep graph, ArrayList<BBBundleEdge> edges, IntArrayList edgesToDraw, int coreSize) {
        this.graph = graph;
        this.edges = edges;
        this.coreSize = coreSize;
        this.edgesToDraw = edgesToDraw;
    }

    @Override
    public void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException {
        JsonGenerator gen = mapper.getFactory().createGenerator(stream);
        gen.writeStartObject();
        gen.writeNumberField("nodeCount", coreSize);
        gen.writeNumberField("edgeCount", edges.size());
        // Drawing Data
        gen.writeArrayFieldStart("draw");
        for (int i = 0; i < edgesToDraw.size(); ++i) {
            writeDrawEdge(gen, edgesToDraw.get(i));
        }
        gen.writeEndArray();
        // Edges
        gen.writeArrayFieldStart("edges");
        for (BBBundleEdge e : edges) {
            gen.writeStartObject();
            gen.writeNumberField("src", e.srcId);
            gen.writeNumberField("trgt", e.trgtId);
            gen.writeNumberField("cost", e.cost);
            gen.writeNumberField("edgeId", e.edgeId);
            gen.writeArrayFieldStart("path");
            for (int i = 0; i < e.unpacked.size(); ++i){
                gen.writeNumber(e.unpacked.get(i));
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
        gen.flush();
    }

    public final void writeDrawEdge(JsonGenerator gen, int edgeId) throws IOException {
        int s = graph.getSource(edgeId);
        int t = graph.getTarget(edgeId);
        // TODO we need to save the real type
        int type = 0;
        gen.writeNumber(graph.getXPos(s));
        gen.writeNumber(graph.getYPos(s));
        gen.writeNumber(graph.getXPos(t));
        gen.writeNumber(graph.getYPos(t));
        float speed = (float) graph.getEuclidianDist(edgeId) / (float) graph.getDist(edgeId);
        gen.writeNumber((int)(speed*100));
    }
}
