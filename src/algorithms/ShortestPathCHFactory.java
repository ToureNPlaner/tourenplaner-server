/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import graphrep.GraphRep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niklas Schnelle, Peter Vollmer
 * 
 */
public class ShortestPathCHFactory extends SharingAlgorithmFactory {

	public ShortestPathCHFactory(GraphRep graph) {
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
		return new ShortestPathCH(graph, new DijkstraStructs());
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
		return null;
	}

	@Override
	public Map<String, Object> getConstraints() {
		Map<String, Object> map = new HashMap<String, Object>(2);
		map.put("minPoints", Integer.valueOf(2));
		map.put("sourceIsTarget", Boolean.FALSE);
		return map;
	}

	@Override
	public boolean hidden() {
		return false;
	}

}
