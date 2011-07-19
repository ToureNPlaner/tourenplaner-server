/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

/**
 * @author Peter Vollmer
 *
 */
public class BubblesortFactory extends AlgorithmFactory {

	/**
	 * @see algorithms.AlgorithmFactory#createAlgorithm()
	 */
	@Override
	public Algorithm createAlgorithm() {
		return new Bubblesort();
	}


}
