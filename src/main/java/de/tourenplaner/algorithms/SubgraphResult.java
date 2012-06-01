package de.tourenplaner.algorithms;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
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
    private final IntObjectOpenHashMap<IntArrayList> cgraph;

    public SubgraphResult(GraphRep graphRep, IntObjectOpenHashMap<IntArrayList> cgraph) {
        this.graph = graphRep;
        this.cgraph = cgraph;
    }

    @Override
    public void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException {
        JsonGenerator gen = mapper.getJsonFactory().createJsonGenerator(stream);
        gen.writeStartObject();
        gen.writeArrayFieldStart("edges");
        for (IntObjectCursor node : cgraph){
            gen.writeStartArray();
            gen.writeNumber(node.key);
            gen.writeStartArray();
            IntArrayList values = (IntArrayList)node.value;
            for (IntCursor edgeId : values) {
                gen.writeNumber(graph.getTarget(edgeId.value));
                gen.writeNumber(graph.getDist(edgeId.value));
            }
            gen.writeEndArray();
            gen.writeEndArray();
        }
        gen.writeEndArray();
        gen.writeEndObject();
        gen.flush();
    }
}
