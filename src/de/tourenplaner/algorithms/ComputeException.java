/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.algorithms;

/**
 * An algorithm should throw the ComputeException if expected errors
 * occur during normal server activity and normal server state.
 * Do not use this exception if the error occurs because of server problems
 * or faulty algorithm code.
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 * 
 */
public class ComputeException extends Exception {

	private static final long serialVersionUID = 1L;

	public ComputeException(String message) {
		super(message);
	}

}
