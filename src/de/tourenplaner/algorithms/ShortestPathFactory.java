/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.algorithms;

import de.tourenplaner.graphrep.GraphRep;

import java.util.*;

/**
 *  @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 * Used to create instances of ShortestPath algorithm
 */
public class ShortestPathFactory extends SharingAlgorithmFactory {
    private final Map<String, Object> details;
    private final List<Map<String, Object>> constraints;
    private final List<Map<String, Object>> pointConstraints;


    public ShortestPathFactory(GraphRep graph) {
		super(graph);
        constraints = new ArrayList<Map<String, Object>>(0);
        pointConstraints = new ArrayList<Map<String, Object>>(0);
        details = new HashMap<String, Object>(3);
        details.put("hidden", this.isHidden());
        details.put("minpoints", 1);
        details.put("sourceistarget", false);

    }

    @Override
    public Map<String, Object> getDetails() {
        return details;
    }
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tourenplaner.algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm(DijkstraStructs rs) {
		return new ShortestPath(graph, rs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tourenplaner.algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm() {
		return new ShortestPath(graph, new DijkstraStructs(
				graph.getNodeCount(), graph.getEdgeCount()));
	}

	@Override
	public String getURLSuffix() {
		return "sps";
	}

	@Override
	public String getAlgName() {
		return "Shortest Path Simple";
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
