package algorithms;

import graphrep.GraphRep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Niklas Schnelle
 * Date: 12/26/11
 * Time: 5:42 PM
 */
public class TravelingSalesmenFactory extends SharingAlgorithmFactory{
    public TravelingSalesmenFactory(GraphRep graph){
      super(graph);
    }

    @Override
    public Algorithm createAlgorithm(DijkstraStructs rs) {
        return new TravelingSalesman(graph, rs);
    }

    @Override
    public List<Map<String, Object>> getPointConstraints() {
        return null;
    }

    @Override
    public Map<String, Object> getConstraints() {
        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("minPoints", Integer.valueOf(2));
        map.put("sourceIsTarget", Boolean.TRUE);
        return map;
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
    public boolean hidden() {
        return false;
    }
}
