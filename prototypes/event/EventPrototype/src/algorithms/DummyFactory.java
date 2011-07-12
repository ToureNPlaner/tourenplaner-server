/**
 * ToureNPlaner
 */
package algorithms;

/**
 * @author Niklas Schnelle
 *
 */
public class DummyFactory extends AlgorithmFactory {

	/* (non-Javadoc)
	 * @see algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm() {
		return new ShortestPathDummy();
	}

}
