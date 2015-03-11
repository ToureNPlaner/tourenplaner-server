package de.tourenplaner.algorithms.coregraph;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import de.tourenplaner.computecore.StreamJsonWriter;
import de.tourenplaner.graphrep.GraphRep;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is used to store and send back
 * subgraphs for client side computation
 */
public class SubgraphResult implements StreamJsonWriter {



    private final GraphRep graph;
    private IntArrayList cgraph;
    private final int srcId, trgtId;

    public SubgraphResult(GraphRep graphRep, IntArrayList cgraph, int srcId, int trgtId) {
        this.graph = graphRep;
        this.cgraph = cgraph;
        this.srcId = srcId;
        this.trgtId = trgtId;
    }

    @Override
    public void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException {
        JsonGenerator gen = mapper.getJsonFactory().createJsonGenerator(stream);
        gen.writeStartObject();
        gen.writeNumberField("srcId", srcId);
        gen.writeNumberField("trgtId", trgtId);
        gen.writeArrayFieldStart("edges");
        for (IntCursor edgeId : cgraph) {
            gen.writeNumber(graph.getSource(edgeId.value)); //src
            gen.writeNumber(graph.getTarget(edgeId.value)); //trgt
            gen.writeNumber(graph.getDist(edgeId.value)); // dist
        }
        gen.writeEndArray();
        gen.writeEndObject();
        this.cgraph = null;
        gen.flush();
    }
}
