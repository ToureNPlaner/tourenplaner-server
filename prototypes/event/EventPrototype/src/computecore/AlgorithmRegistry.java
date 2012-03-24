/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.computecore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.tourenplaner.algorithms.AlgorithmFactory;

/**
 * Singelton Pattern used
 * 
 *
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class AlgorithmRegistry {

	
	private static AlgorithmRegistry instance=null;
	
	private ConcurrentHashMap<String, AlgorithmFactory> registry;
	
	private AlgorithmRegistry(){
		registry = new ConcurrentHashMap<String, AlgorithmFactory>();
	};
	
	public static AlgorithmRegistry getInstance(){
		if(instance == null){
				instance = new AlgorithmRegistry();
		}
		return instance;
	}
	
	public void registerAlgorithm(String shortName, AlgorithmFactory algFac){
		registry.putIfAbsent(shortName, algFac);
	}
	
	public void unregisterAlgorithm(String shortName){
		registry.remove(shortName);
	}
	/**
	 * Creates a new AlgorithmManager that has an instance for every registered Algorithms
	 * 
	 * @return
	 */
	public AlgorithmManager createAlgorithmManager(){
		AlgorithmManager m = new AlgorithmManager();
		
		for(Map.Entry<String, AlgorithmFactory> registered : registry.entrySet()){
			m.addAlgorithm(registered.getKey(), registered.getValue().createAlgorithm());
		}
		
		return m;
	}
}
