/**
 *
 */
package algorithms;

import graphrep.GraphRep;

import java.util.*;

/**
 *  @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 * Used to create instances of ConstraintShortestPath algorithm
 */
public class ConstrainedSPFactory extends SharingAlgorithmFactory {
    private final Map<String, Object> details;
    private final List<Map<String, Object>> constraints;
    private final List<Map<String, Object>> pointConstraints;


    /**
     * @param graph
     */
    public ConstrainedSPFactory(GraphRep graph) {
        super(graph);
        constraints = new ArrayList<Map<String, Object>>();
        constraints.add(new HashMap<String, Object>(4));
        constraints.get(0).put("id", "maxAltitudeDifference");
        constraints.get(0).put("name", "Maximum Altitude Difference");
        constraints.get(0).put("description", "The maximum altitude difference over the path");
        constraints.get(0).put("type", "meter");
        constraints.get(0).put("min", 0);

        pointConstraints = new ArrayList<Map<String, Object>>(0);

        details = new HashMap<String, Object>(3);
        details.put("hidden", this.isHidden());
        details.put("minpoints", 2);
        details.put("maxpoints", 2);
        details.put("sourceistarget", false);

    }

    @Override
    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public Algorithm createAlgorithm(DijkstraStructs rs) {
        return new ConstrainedSP(graph, rs);
    }

    /*
      * (non-Javadoc)
      *
      * @see algorithms.AlgorithmFactory#createAlgorithm()
      */
    @Override
    public Algorithm createAlgorithm() {
        return new ConstrainedSP(graph, new DijkstraStructs(
                graph.getNodeCount(), graph.getEdgeCount()));
    }

    @Override
    public String getURLSuffix() {
        return "csp";
    }

    @Override
    public String getAlgName() {
        return "Constrained Shortest Path";
    }

    @Override
    public int getVersion() {
        return 1;
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
    public boolean isHidden() {
        return false;
    }

}
