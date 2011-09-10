/**
 * 
 */
package algorithms;

import java.util.List;
import java.util.Map;
/**
 * @author niklas
 *
 */
public abstract class GraphAlgorithmFactory extends AlgorithmFactory {
	
	
	/**
	 * Gets the List of PointConstraints in a jsonserializable Map format
	 * 
	 * @return
	 */
	public abstract List<Map<String, Object>> getPointConstraints();
	
	
	/**
	 * Gets the Constraints not bound to any Point
	 * 
	 * @return
	 */
	public abstract Map<String, Object> getConstraints();
	

}
