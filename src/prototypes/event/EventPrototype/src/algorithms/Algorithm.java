/**
 * ToureNPlaner
 * 
 */
package algorithms;

/**
 * @author Niklas Schnelle
 *
 */
public interface Algorithm extends Runnable {
	
	public String getName();
	public String getShorthand();

	/**
	 * Sets the ComputeRequest to work on when run is called.
	 * This method obviously need to be called before run.
	 * Implementations are allowed to transform the request into a domain
	 * specific representation and forget about it
	 * @param req
	 */
	public void setComputeRequest(ComputeRequest req);
	

	
	/**
	 * Used by the Algorithm to save the result of the computation.
	 * It must be created with the ComputeRequest as parameter
	 * 
	 */
	public ComputeResult getComputeResult();
	
	
}