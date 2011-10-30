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
public interface Algorithm extends Runnable {

	/**
	 * Sets the ComputeRequest to work on when run is called. This method
	 * obviously need to be called before run. The algorihtm instance will use
	 * the Request object to store it's results in the points and misc fields
	 * 
	 * @param req
	 */
	public void setRequest(ComputeRequest req);

}