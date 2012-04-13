/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.algorithms;

/**
 * An algorithm should not throw the InternalException if expected errors
 * occur during normal server activity and normal server state.
 * Do use this exception if an error occurs because of server problems
 * or faulty algorithm code.
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 * 
 */
public class InternalException extends Exception {

	private static final long serialVersionUID = 1L;

	public InternalException(String message) {
		super(message);
	}

}
