package algorithms;

import graphrep.GraphRep;

import java.util.*;

public class NNSearchFactory extends GraphAlgorithmFactory {
    private final Map<String, Object> details;
    private final List<Map<String, Object>> constraints;
    private final List<Map<String, Object>> pointConstraints;
    

	public NNSearchFactory(GraphRep graph) {
		super(graph);
        constraints = new ArrayList<Map<String, Object>> (0);
        pointConstraints = new ArrayList<Map<String, Object>>(0);
        details = new HashMap<String, Object>(3);
        details.put("hidden", this.isHidden());
        details.put("minpoints", 1);
        details.put("sourceistarget", false);

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
	public boolean isHidden() {
		return true;
	}

}
