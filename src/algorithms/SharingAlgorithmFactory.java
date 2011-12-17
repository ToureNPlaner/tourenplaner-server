/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import graphrep.GraphRep;

/**
 * @author Niklas Schnelle
 * 
 */
public abstract class SharingAlgorithmFactory extends GraphAlgorithmFactory {

	public SharingAlgorithmFactory(GraphRep graph) {
		super(graph);
	}

	public abstract Algorithm createAlgorithm(DijkstraStructs rs);

}
