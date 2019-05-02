package de.tourenplaner.utils;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A GraphRep used for ClientSideProcessing
 */
public class ClientSideGraphRep {

    private int edgeCount;

    private static class OutEdges {
        public IntArrayList oridIds;
        public IntArrayList targetIds;
        public IntArrayList dists;
    }

    private IntObjectHashMap<OutEdges> graph;

    public ClientSideGraphRep(){
        graph = new IntObjectHashMap<OutEdges>();
    }

    public int getEdgeCount(){
        return edgeCount;
    }

    public int getNodeCount(){
        return graph.size();
    }

    public int getOutEdgeCount(int nodeId){
        return graph.get(nodeId).targetIds.size();
    }

    public int getOutEdgeId(int nodeId, int edgeNum) {
        return graph.get(nodeId).oridIds.get(edgeNum);
    }

    public int getOutDist(int nodeId, int edgeNum){
        return graph.get(nodeId).dists.get(edgeNum);
    }

    public int getOutTarget(int nodeId, int edgeNum){
        return graph.get(nodeId).targetIds.get(edgeNum);
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
        edgeCount++;
    }


    public void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException {
        JsonGenerator gen = mapper.getFactory().createGenerator(stream);
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
