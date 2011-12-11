package algorithms;

import graphrep.GraphRep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NNSearchFactory extends GraphAlgorithmFactory {

	public NNSearchFactory(GraphRep graph) {
		super(graph);
	}

	@Override
	public List<Map<String, Object>> getPointConstraints() {
		return null;
	}

	@Override
	public Map<String, Object> getConstraints() {
		Map<String, Object> map = new HashMap<String, Object>(2);
		map.put("minPoints", Integer.valueOf(1));
		map.put("sourceIsTarget", Boolean.FALSE);
		return map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm() {
		return new NNSearch(graph);
	}

	@Override
	public String getURLSuffix() {
		return "nns";
	}

	@Override
	public String getAlgName() {
		return "Nearest Neighbor Search";
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public boolean hidden() {
		return true;
	}

}
