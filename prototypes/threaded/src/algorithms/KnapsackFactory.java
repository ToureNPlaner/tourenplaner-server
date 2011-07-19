/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

/**
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class KnapsackFactory extends AlgorithmFactory {

	/**
	 * @see algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm() {
		return new Knapsack();
	}

}
