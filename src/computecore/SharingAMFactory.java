/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import graphrep.GraphRep;

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
	 * @see computecore.AlgorithmManagerFactory#createAlgorithmManager()
	 */
	@Override
	public AlgorithmManager createAlgorithmManager() {
		return new ShareEnabledAM(graph);
	}

}
