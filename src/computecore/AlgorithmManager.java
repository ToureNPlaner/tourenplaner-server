/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.HashMap;
import java.util.Map;

import algorithms.Algorithm;

/**
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class AlgorithmManager {
	
	private Map<String, Algorithm> algs = new HashMap<String, Algorithm>(); 
	
	protected AlgorithmManager(){}
	
	
	protected void addAlgorithm(String sn, Algorithm alg){
		algs.put(sn, alg);
	}
	
	
	public Algorithm getAlgByURLSuffix(String sn){
		return algs.get(sn);
	}
}
