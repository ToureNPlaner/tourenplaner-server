/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import graphrep.GraphRep;

import java.util.*;

/**
 * @author Niklas Schnelle, Peter Vollmer
 * 
 */
public class ShortestPathCHFactory extends SharingAlgorithmFactory {
    private final Map<String, Object> details;
    private final List<Map<String, Object>> constraints;
    private final List<Map<String, Object>> pointConstraints;

    public ShortestPathCHFactory(GraphRep graph) {
		super(graph);
        constraints = new ArrayList<Map<String, Object>>(0);
        pointConstraints = new ArrayList<Map<String, Object>>(0);
        details = new HashMap<String, Object>(3);
        details.put("hidden", this.isHidden());
        details.put("minpoints", 1);
        details.put("sourceistarget", false);

    }

	@Override
	public Algorithm createAlgorithm(DijkstraStructs rs) {
		return new ShortestPathCH(graph, rs);
    }

    @Override
    public Map<String, Object> getDetails() {
        return details;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm() {
		return new ShortestPath(graph, new DijkstraStructs(
				graph.getNodeCount(), graph.getEdgeCount()));
	}

	@Override
	public String getURLSuffix() {
		return "sp";
	}

	@Override
	public String getAlgName() {
		return "Shortest Path CH";
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
