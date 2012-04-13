/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.algorithms;

import de.tourenplaner.computecore.ComputeRequest;

 /**
 *  @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 *         A class implementing this interface can be used by the ComputeCore to
 *         do Computations
 * 
 */
public interface Algorithm {

	/**
	 * Runs the Algorithm instance on the given ComputeRequest
	 * 
	 *
     * @param req The compute request
     * @throws ComputeException see java doc of exception
     * @throws InternalException see java doc of exception
	 */
    void compute(ComputeRequest req) throws ComputeException, InternalException;

}