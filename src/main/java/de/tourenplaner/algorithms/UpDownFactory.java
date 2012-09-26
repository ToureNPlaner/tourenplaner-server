package de.tourenplaner.algorithms;

import de.tourenplaner.graphrep.GraphRep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niklas Schnelle
 */
public class UpDownFactory extends  SharingAlgorithmFactory {
    private final Map<String, Object> details;
    private final List<Map<String, Object>> constraints;
    private final List<Map<String, Object>> pointConstraints;

    public UpDownFactory(GraphRep graphRep){
        super(graphRep);
        constraints = new ArrayList<Map<String, Object>>(0);
        constraints.add(new HashMap<String, Object>(4));
        constraints.get(0).put("id", "maxSearchLevel");
        constraints.get(0).put("name", "Maximum Search Level");
        constraints.get(0).put("description", "The maximum node level to explore");
        constraints.get(0).put("type", "integer");
        constraints.get(0).put("min", 0);


        pointConstraints = new ArrayList<Map<String, Object>>(0);
        details = new HashMap<String, Object>(3);
        details.put("hidden", this.isHidden());
        details.put("minpoints", 2);
        details.put("sourceistarget", false);
    }

    @Override
    public Algorithm createAlgorithm(DijkstraStructs rs) {
        return new UpDownGraphPacket(graph, rs);
    }

    @Override
    public List<Map<String, Object>> getPointConstraints() {
        return pointConstraints;
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
        return constraints;
    }

    @Override
    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
