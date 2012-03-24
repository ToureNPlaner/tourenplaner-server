/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.algorithms;

/**
 * @author Peter Vollmer
 *
 */
public class BubblesortFactory extends AlgorithmFactory {

	/**
	 * @see de.tourenplaner.algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm() {
		return new Bubblesort();
	}


}
