/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.algorithms;

import de.tourenplaner.graphrep.GraphRep;

import java.util.List;
import java.util.Map;
/**
 *  @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 * This class is used to construct Graphalgorithms
 * and provides additional information only used by Graphalgorithms
 *
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
	public abstract List<Map<String, Object>> getConstraints();

    /**
     * Gets Details for the the algorithm
     * this includes whether the algorithm has sourceIsTarget
     * and the minimal number of points
     *
     * @return
     */
    public abstract Map<String, Object> getDetails();
	

}