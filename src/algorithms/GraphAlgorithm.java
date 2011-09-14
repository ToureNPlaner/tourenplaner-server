/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import graphrep.Graphrep;
/**
 * This abstract class is the basis for Algorithms using a Graphrepresentation
 * and will be constructed with a reference to the
 * Graphrep. used by the server
 * 
 * @author Niklas Schnelle
 *
 */
public abstract class GraphAlgorithm implements Algorithm {
	private GraphAlgorithm() {
	}

	Graphrep graph;

	public GraphAlgorithm(Graphrep graph) {
		this.graph = graph;
	}

}
