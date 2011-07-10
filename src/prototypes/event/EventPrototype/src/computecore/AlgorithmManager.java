/**
 * ToureNPlaner
 */
package computecore;

import java.util.HashMap;
import java.util.Map;

import algorithms.Algorithm;

/**
 * @author Niklas Schnelle
 *
 */
public class AlgorithmManager {
	
	private Map<String, Algorithm> algs = new HashMap<String, Algorithm>(); 
	
	protected AlgorithmManager(){}
	
	
	protected void addAlgorithm(String sn, Algorithm alg){
		algs.put(sn, alg);
	}
	
	
	public Algorithm getAlgByShortname(String sn){
		return algs.get(sn);
	}
}