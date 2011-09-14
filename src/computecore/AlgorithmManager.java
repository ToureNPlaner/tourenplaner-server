/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.HashMap;
import java.util.Map;

import algorithms.Algorithm;

/**
 * An AlgorithmManager manages the Algorithm instances used by a single
 * ComputeThread @see computecore.ComputeThread
 * 
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class AlgorithmManager {
	
	private Map<String, Algorithm> algs = new HashMap<String, Algorithm>(); 
	
	protected AlgorithmManager(){}
	
	/**
	 * Adds a new Algorithm to this manager, called by AlgorithmRegistry during
	 * construction of new AlgorithmManager instances
	 * 
	 * @param sn
	 * @param alg
	 */
	protected void addAlgorithm(String sn, Algorithm alg){
		algs.put(sn, alg);
	}
	
	/**
	 * Used to get an Algorithm instance by it's URLSuffix.
	 * e.g. "sp" might give you an Algorithm instance for Shortest Paths
	 * 
	 * @param sn
	 * @return
	 */
	public Algorithm getAlgByURLSuffix(String sn){
		return algs.get(sn);
	}
}
