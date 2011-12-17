/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import algorithms.AlgorithmFactory;
import algorithms.DijkstraStructs;
import algorithms.SharingAlgorithmFactory;

/**
 * @author Niklas Schnelle
 * 
 */
public class ShareEnabledAM extends AlgorithmManager {
	private final DijkstraStructs rs;

	public ShareEnabledAM() {
		rs = new DijkstraStructs();
	}

	/**
	 * @see computecore.AlgorithmManager#addAlgorithm(algorithms.AlgorithmFactory)
	 */
	@Override
	public void addAlgorithm(AlgorithmFactory algFac) {
		if (algFac instanceof SharingAlgorithmFactory) {
			super.addAlgorithm(algFac.getURLSuffix(),
					((SharingAlgorithmFactory) algFac).createAlgorithm(rs));
		} else {
			super.addAlgorithm(algFac.getURLSuffix(), algFac.createAlgorithm());
		}
	}
}
