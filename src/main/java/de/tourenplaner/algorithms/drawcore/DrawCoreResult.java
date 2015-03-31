package de.tourenplaner.algorithms.drawcore;

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

    public DrawCoreResult(GraphRep graph, ArrayList<BBBundleEdge> edges, int coreSize) {
        this.graph = graph;
        this.edges = edges;
        this.coreSize = coreSize;
    }

    @Override
    public void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException {
        JsonGenerator gen = mapper.getFactory().createGenerator(stream);
        gen.writeStartObject();
        gen.writeNumberField("nodeCount", coreSize);
        gen.writeNumberField("edgeCount", edges.size());
        gen.writeArrayFieldStart("edges");
        for (BBBundleEdge e : edges) {
            gen.writeStartObject();
            gen.writeNumberField("src", e.srcId);
            gen.writeNumberField("trgt", e.trgtId);
            gen.writeNumberField("cost", e.cost);
            gen.writeNumberField("edgeId", e.edgeId);
            gen.writeArrayFieldStart("path");
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

    public final void writeDrawEdge(JsonGenerator gen, BBBundleEdge e, int i) throws IOException {
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
