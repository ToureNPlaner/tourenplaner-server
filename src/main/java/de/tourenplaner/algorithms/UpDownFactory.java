package de.tourenplaner.algorithms;

import de.tourenplaner.graphrep.GraphRep;

import java.util.List;
import java.util.Map;

/**
 * @author Niklas Schnelle
 */
public class UpDownFactory extends  SharingAlgorithmFactory {

    public UpDownFactory(GraphRep graphRep){
        super(graphRep);
    }

    @Override
    public Algorithm createAlgorithm(DijkstraStructs rs) {
        return new UpDownGraphPacket(graph, rs);
    }

    @Override
    public List<Map<String, Object>> getPointConstraints() {
        return null;
    }

    @Override
    public Algorithm createAlgorithm() {
        return new UpDownGraphPacket(graph, new DijkstraStructs(graph.getNodeCount(), graph.getEdgeCount()));
    }

    @Override
    public String getURLSuffix() {
        return "updowng";
    }

    @Override
    public String getAlgName() {
        return "Upwards+Downwards Graph";
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public List<Map<String, Object>> getConstraints() {
        return null;
    }

    @Override
    public Map<String, Object> getDetails() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
