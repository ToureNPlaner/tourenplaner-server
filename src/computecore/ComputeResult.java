/**
 * $$\\ToureNPlaner\\$$
 */

package computecore;

import java.util.HashMap;

/**
 * This class represents the computation result of a ComputeRequest
 * 
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class ComputeResult extends HashMap<String, Object>{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -3444036789339280455L;


	/**
	 * Creates a ComputeResult with the standard capacity
	 * 
	 */
	protected ComputeResult(){}

	/**
	 * Creates a new result Object with the given capacity
	 * 
	 * @param capacity
	 */
	protected ComputeResult(int capacity){
		super(capacity);
	}

}
