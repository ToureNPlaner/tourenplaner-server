/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.algorithms;

/**
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class DummyFactory extends AlgorithmFactory {

	/* (non-Javadoc)
	 * @see de.tourenplaner.algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm() {
		return new ShortestPathDummy();
	}

}
