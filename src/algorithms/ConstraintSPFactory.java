/**
 *
 */
package algorithms;

import graphrep.GraphRep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nino
 */
public class ConstraintSPFactory extends SharingAlgorithmFactory {

    /**
     * @param graph
     */
    public ConstraintSPFactory(GraphRep graph) {
        super(graph);
    }

    @Override
    public Algorithm createAlgorithm(DijkstraStructs rs) {
        return new ShortestPathCH(graph, rs);
    }

    /*
      * (non-Javadoc)
      *
      * @see algorithms.AlgorithmFactory#createAlgorithm()
      */
    @Override
    public Algorithm createAlgorithm() {
        return new ConstraintSP(graph, new DijkstraStructs(
                graph.getNodeCount(), graph.getEdgeCount()));
    }

    @Override
    public String getURLSuffix() {
        return "csp";
    }

    @Override
    public String getAlgName() {
        return "Constraint Shortest Path";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public List<Map<String, Object>> getPointConstraints() {
        return null;
    }

    @Override
    public Map<String, Object> getConstraints() {
        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("minPoints", Integer.valueOf(2));
        map.put("maxPoints", Integer.valueOf(2));
        map.put("sourceIsTarget", Boolean.FALSE);
        map.put("maxAltitudeDifference", "meter");
        return map;
    }

    @Override
    public boolean hidden() {
        return false;
    }

}
