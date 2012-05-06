package de.tourenplaner.utils;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A GraphRep used for ClientSideProcessing
 */
public class ClientSideGraphRep {

    private static class OutEdges {
        public IntArrayList oridIds;
        public IntArrayList targetIds;
        public IntArrayList dists;
    }

    private IntObjectOpenHashMap<OutEdges> graph;

    public ClientSideGraphRep(){
        graph = new IntObjectOpenHashMap<OutEdges>();
    }

    public void addEdge(int origId, int sourceId,int targetId, int dist){
        OutEdges outEdges = graph.get(sourceId);
        if (outEdges == null){
            outEdges = new OutEdges();
            outEdges.oridIds = new IntArrayList();
            outEdges.targetIds = new IntArrayList();
            outEdges.dists = new IntArrayList();
            graph.put(sourceId, outEdges);
        }
        outEdges.oridIds.add(origId);
        outEdges.targetIds.add(targetId);
        outEdges.dists.add(dist);
    }

    public void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException {
        JsonGenerator gen = mapper.getJsonFactory().createJsonGenerator(stream);
        gen.writeStartObject();
        gen.writeObjectFieldStart("graph");
        IntArrayList list;
        for (IntObjectCursor<OutEdges> outEdges : graph) {
           gen.writeObjectFieldStart(Integer.toString(outEdges.key));
            gen.writeArrayFieldStart("eid");
            list = outEdges.value.oridIds;
            for(int i = 0; i < list.size(); i++){
                gen.writeNumber(list.get(i));
            }
            gen.writeEndArray();
            gen.writeArrayFieldStart("trgts");
            list = outEdges.value.targetIds;
            for (int i = 0; i < list.size(); i++) {
                gen.writeNumber(list.get(i));
            }
            gen.writeEndArray();
            gen.writeArrayFieldStart("dists");
            list = outEdges.value.dists;
            for (int i = 0; i < list.size(); i++) {
                gen.writeNumber(list.get(i));
            }
            gen.writeEndArray();
           gen.writeEndObject();
        }
        gen.writeEndObject();
        gen.writeEndObject();
        gen.flush();
    }
}
