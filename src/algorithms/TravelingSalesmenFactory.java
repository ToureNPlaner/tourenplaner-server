package algorithms;

import graphrep.GraphRep;

import java.util.*;

/**
 * User: Niklas Schnelle
 * Date: 12/26/11
 * Time: 5:42 PM
 */
public class TravelingSalesmenFactory extends SharingAlgorithmFactory{
    private final Map<String, Object> details;
    private final List<Map<String, Object>> constraints;
    private final List<Map<String, Object>> pointConstraints;

    public TravelingSalesmenFactory(GraphRep graph){
      super(graph);
        constraints = new ArrayList<Map<String, Object>> (0);
        pointConstraints = new ArrayList<Map<String, Object>>(0);
        details = new HashMap<String, Object>(3);
        details.put("hidden", this.isHidden());
        details.put("minpoints", 2);
        details.put("sourceistarget", true);

    }

    @Override
    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public Algorithm createAlgorithm(DijkstraStructs rs) {
        return new TravelingSalesman(graph, rs);
    }

    @Override
    public List<Map<String, Object>> getPointConstraints() {
        return pointConstraints;
    }

    @Override
    public List<Map<String, Object>> getConstraints() {
        return constraints;
    }

    @Override
    public Algorithm createAlgorithm() {
        return new TravelingSalesman(graph, new DijkstraStructs(graph.getNodeCount(), graph.getEdgeCount()));
    }

    @Override
    public String getURLSuffix() {
        return "tsp";
    }

    @Override
    public String getAlgName() {
        return "Traveling Salesman";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public boolean isHidden() {
        return false;
    }
}
