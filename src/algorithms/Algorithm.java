/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import computecore.ComputeRequest;

/**
 * @author Niklas Schnelle, Peter Vollmer
 * 
 *         A class implementing this interface can be used by the ComputeCore to
 *         do Computations
 * 
 */
public interface Algorithm {

	/**
	 * Runs the Algorithm instance on the given ComputeRequest
	 * 
	 * @param req
	 */
	public void compute(ComputeRequest req) throws ComputeException;

}