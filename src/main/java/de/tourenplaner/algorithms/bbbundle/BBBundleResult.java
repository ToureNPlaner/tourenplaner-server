package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.IntArrayList;
import com.fasterxml.jackson.core.JsonGenerator;
import de.tourenplaner.computecore.FormattedStreamWriter;
import de.tourenplaner.computeserver.Responder;
import de.tourenplaner.graphrep.GraphRep;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * This class is used to store and send back
 * subgraphs for client side computation
 */
public final class BBBundleResult implements FormattedStreamWriter {

    //private final GraphRep graph;
    private final IntArrayList edgesToDraw;
    private final IntArrayList verticesToDraw;
    private final int[] nodes;
    private final ArrayList<BBBundleEdge> upEdges;
    private final ArrayList<BBBundleEdge> downEdges;
    private final BBBundleRequestData request;
    private final GraphRep graph;
    private final boolean latLonMode;

    public BBBundleResult(GraphRep graph, boolean latLonMode, int[] nodes, IntArrayList verticesToDraw, IntArrayList edgesToDraw, ArrayList<BBBundleEdge> upEdges, ArrayList<BBBundleEdge> downEdges, BBBundleRequestData request) {
        this.graph = graph;
        this.latLonMode = latLonMode;
        this.nodes = nodes;
        this.verticesToDraw = verticesToDraw;
        this.edgesToDraw = edgesToDraw;
        this.upEdges = upEdges;
        this.downEdges = downEdges;
        this.request = request;
    }

    @Override
    public void writeToStream(Responder.ResultFormat format, OutputStream stream) throws IOException {
        JsonGenerator gen = format.getMapper().getFactory().createGenerator(stream);
        gen.writeStartObject();
        // Head
        gen.writeObjectFieldStart("head");
        gen.writeNumberField("nodeCount", nodes.length);
        gen.writeNumberField("upEdgeCount", upEdges.size());
        gen.writeNumberField("downEdgeCount", downEdges.size());
        gen.writeNumberField("level", request.getLevel());
        gen.writeEndObject();
        // Drawing Data
        gen.writeObjectFieldStart("draw");
        gen.writeArrayFieldStart("vertices");
        if(latLonMode) {
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
        // Original node ids
        gen.writeArrayFieldStart("oNodeIds");
        for (int nodeId : nodes) {
            gen.writeNumber(nodeId);
        }
        gen.writeEndArray();
        // Edges
        gen.writeObjectFieldStart("edges");
        gen.writeArrayFieldStart("upEdges");
        for (BBBundleEdge e : upEdges) {
            gen.writeNumber(e.srcId);
            gen.writeNumber(e.trgtId);
            gen.writeNumber(e.cost);
            gen.writeNumber(e.drawEdgeIndex);
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("downEdges");
        for (BBBundleEdge e : downEdges) {
            gen.writeNumber(e.srcId);
            gen.writeNumber(e.trgtId);
            gen.writeNumber(e.cost);
            gen.writeNumber(e.drawEdgeIndex);
        }
        gen.writeEndArray();
        gen.writeEndObject();
        gen.writeEndObject();
        gen.flush();
    }
}
