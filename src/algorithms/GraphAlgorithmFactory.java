/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import graphrep.GraphRep;

import java.util.List;
import java.util.Map;
/**
 * This class is used to construct Graphalgorithms
 * and provides additional information only used by Graphalgorithms
 * 
 * @author Niklas Schnelle
 *
 */
public abstract class GraphAlgorithmFactory extends AlgorithmFactory {
	
	protected GraphRep graph;
	
	
	public GraphAlgorithmFactory(GraphRep graph){
		this.graph = graph;
	}
	
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