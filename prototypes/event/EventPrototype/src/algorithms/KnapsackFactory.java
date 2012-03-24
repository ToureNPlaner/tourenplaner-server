/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.algorithms;

/**
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class KnapsackFactory extends AlgorithmFactory {

	/**
	 * @see de.tourenplaner.algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm() {
		return new Knapsack();
	}

}
