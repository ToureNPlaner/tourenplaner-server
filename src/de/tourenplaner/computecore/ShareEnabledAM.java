/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.computecore;

import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.algorithms.AlgorithmFactory;
import de.tourenplaner.algorithms.DijkstraStructs;
import de.tourenplaner.algorithms.SharingAlgorithmFactory;

/**
 * @author Niklas Schnelle
 * 
 */
public class ShareEnabledAM extends AlgorithmManager {
	private final DijkstraStructs rs;

	public ShareEnabledAM(GraphRep graph) {
		rs = new DijkstraStructs(graph.getNodeCount(), graph.getEdgeCount());
	}

	/**
	 * @see de.tourenplaner.computecore.AlgorithmManager#addAlgorithm(de.tourenplaner.algorithms.AlgorithmFactory)
	 */
	@Override
	public void addAlgorithm(AlgorithmFactory algFac) {
		if (algFac instanceof SharingAlgorithmFactory) {
			addAlgorithm(algFac.getURLSuffix(), ((SharingAlgorithmFactory) algFac).createAlgorithm(rs));
		} else {
			addAlgorithm(algFac.getURLSuffix(), algFac.createAlgorithm());
		}
	}
}
