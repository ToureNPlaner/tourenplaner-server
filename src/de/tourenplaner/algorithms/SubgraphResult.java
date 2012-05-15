package de.tourenplaner.algorithms;

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
    private final IntArrayList edges;

    public SubgraphResult(GraphRep graphRep, IntArrayList edges){
        this.graph = graphRep;
        this.edges = edges;
    }

    @Override
    public void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException {
        JsonGenerator gen = mapper.getJsonFactory().createJsonGenerator(stream);
        gen.writeStartObject();
        gen.writeArrayFieldStart("edges");
        for (IntCursor edge : edges){
            gen.writeStartArray();
            gen.writeNumber(graph.getSource(edge.value));
            gen.writeNumber(graph.getTarget(edge.value));
            gen.writeNumber(graph.getDist(edge.value));
            gen.writeEndArray();
        }
        gen.writeEndArray();
        gen.writeEndObject();
        gen.flush();
    }
}
