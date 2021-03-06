package de.tourenplaner.algorithms.drawcore;

import com.carrotsearch.hppc.IntArrayList;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tourenplaner.algorithms.bbbundle.BBBundleEdge;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.computecore.FormattedStreamWriter;
import de.tourenplaner.computeserver.Responder;
import de.tourenplaner.graphrep.GraphRep;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by niklas on 30.03.15.
 */
public class DrawCoreResult implements FormattedStreamWriter {

    private final ArrayList<BBBundleEdge> edges;
    private final int coreSize;
    private final GraphRep graph;
    private final IntArrayList edgesToDraw;
    private final IntArrayList verticesToDraw;
    private final boolean latLonMode;

    public DrawCoreResult(GraphRep graph, boolean latLonMode, ArrayList<BBBundleEdge> edges, IntArrayList verticesToDraw, IntArrayList edgesToDraw, int coreSize) {
        this.latLonMode = latLonMode;
        this.graph = graph;
        this.edges = edges;
        this.coreSize = coreSize;
        this.verticesToDraw = verticesToDraw;
        this.edgesToDraw = edgesToDraw;
    }

    @Override
    public void writeToStream(Responder.ResultFormat format, OutputStream stream) throws IOException {
        JsonGenerator gen = format.getMapper().getFactory().createGenerator(stream);
        gen.writeStartObject();
        gen.writeNumberField("nodeCount", coreSize);
        gen.writeNumberField("edgeCount", edges.size());
        // Drawing Data
        // Drawing Data
        gen.writeObjectFieldStart("draw");
        gen.writeArrayFieldStart("vertices");
        if(latLonMode){
            for (int i = 0; i < verticesToDraw.size(); ++i) {
                int nodeId = verticesToDraw.get(i);
                gen.writeNumber(graph.getLat(nodeId));
                gen.writeNumber(graph.getLon(nodeId));
            }
        } else {
            for (int i = 0; i < verticesToDraw.size(); ++i) {
                int nodeId = verticesToDraw.get(i);
                gen.writeNumber(graph.getXPos(nodeId));
                gen.writeNumber(graph.getYPos(nodeId));
            }
        }
        gen.writeEndArray();
        gen.writeArrayFieldStart("lines");
        for (int i = 0; i < edgesToDraw.size();) {
            gen.writeNumber(edgesToDraw.get(i++)); // srcVId
            gen.writeNumber(edgesToDraw.get(i++)); // trgtVId
            gen.writeNumber(edgesToDraw.get(i++)); // type
            gen.writeNumber(edgesToDraw.get(i++)); // drawScA
            gen.writeNumber(edgesToDraw.get(i++)); // drawScB
        }
        gen.writeEndArray();
        gen.writeEndObject();
        // Edges
        gen.writeArrayFieldStart("edges");
        for (BBBundleEdge e : edges) {
            gen.writeNumber(e.srcId);
            gen.writeNumber(e.trgtId);
            gen.writeNumber(e.cost);
            gen.writeNumber(e.drawEdgeIndex);
        }
        gen.writeEndArray();
        gen.writeEndObject();
        gen.flush();
    }
}
