/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.computecore;

import de.tourenplaner.graphrep.GraphRep;

/**
 * @author Niklas Schnelle
 * 
 */
public class SharingAMFactory extends AlgorithmManagerFactory {

	private final GraphRep graph;

	public SharingAMFactory(GraphRep graph) {
		this.graph = graph;
	}

	/**
	 * @see de.tourenplaner.computecore.AlgorithmManagerFactory#createAlgorithmManager()
	 */
	@Override
	public AlgorithmManager createAlgorithmManager() {
		return new ShareEnabledAM(graph);
	}

}
