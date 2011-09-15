/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import graphrep.GraphRep;
/**
 * This abstract class is the basis for Algorithms using a Graphrepresentation
 * and will be constructed with a reference to the
 * GraphRep. used by the server
 * 
 * @author Niklas Schnelle
 *
 */
public abstract class GraphAlgorithm implements Algorithm {
	private GraphAlgorithm() {
	}

	GraphRep graph;

	public GraphAlgorithm(GraphRep graph) {
		this.graph = graph;
	}

}
