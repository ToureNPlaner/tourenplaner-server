/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

/**
 * This factory class is used to create Algorithm instances and to provide information 
 * on the created Algorithm used by clients to adapt to the server
 * 
 * 
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public abstract class AlgorithmFactory {
	public abstract Algorithm createAlgorithm();

	public abstract String getURLSuffix();
	
	public abstract String getAlgName();
	
	public abstract int getVersion();
}
