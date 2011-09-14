/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Graphloader;

/**
 * @author Niklas Schnelle, Peter Vollmer
 * 
 */
public class ShortestPathFactory extends GraphAlgorithmFactory {

	static ShortestPath sp = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm() {
		// TODO: move storage of graphrep to approrpriate place instead of
		// loading every time
		synchronized (this) {
			if (sp == null) {
				try {
					sp = new ShortestPath(Graphloader.loadGraph(System
							.getProperty("user.home")
							+ "/serializedGraphrep.dat"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return sp;
	}

	@Override
	public String getURLSuffix() {
		return "sp";
	}

	@Override
	public String getAlgName() {
		return "Shortest Path";
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public List<Map<String, Object>> getPointConstraints() {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>(1);
		Map<String, Object> map = new HashMap<String, Object>(4);
		map.put("name", "height");
		map.put("type", "meter");
		map.put("min", new Float(-20.0));
		map.put("max", new Float(9000.0));
		ret.add(map);
		return ret;
	}

	@Override
	public Map<String, Object> getConstraints() {
		Map<String, Object> map = new HashMap<String, Object>(2);
		map.put("minPoints", new Integer(2));
		map.put("sourceIsTarget", new Boolean(false));
		return map;
	}

}
