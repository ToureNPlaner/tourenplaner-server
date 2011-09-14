/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import algorithms.AlgorithmFactory;

/**
 * An AlgorithmRegistry is used to register available Algorithms represented
 * by there specific Factory Objects. Evert registered Algorithm will be
 * available to ComputeThreads through their AlgorithmManagers and will
 * be listed as available Algorithm in the ServerInfo 
 *
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class AlgorithmRegistry {

	
	private ConcurrentHashMap<String, AlgorithmFactory> registry;
	/**
	 * Constructs a new empty AlgorithmRegistry
	 * 
	 */
	public AlgorithmRegistry(){
		registry = new ConcurrentHashMap<String, AlgorithmFactory>();
	};
	
	/**
	 * Registers a new AlgorithmFactory in this registry
	 * 
	 * @param algFac
	 */
	public void registerAlgorithm(AlgorithmFactory algFac){
		registry.putIfAbsent(algFac.getURLSuffix(), algFac);
	}
	

	/**
	 * Creates a new AlgorithmManager that has an instance for every registered Algorithms
	 * 
	 * @return
	 */
	public AlgorithmManager getAlgorithmManager(){
		AlgorithmManager m = new AlgorithmManager();
		
		for(Map.Entry<String, AlgorithmFactory> registered : registry.entrySet()){
			m.addAlgorithm(registered.getKey(), registered.getValue().createAlgorithm());
		}
		
		return m;
	}
	
	/** 
	 * Gets a Collection of registered Algorithm's represented by their
	 * specific AlgorithmFactories
	 * 
	 * @return
	 */
	public Collection<AlgorithmFactory> getAlgorithms(){
		return registry.values();
	}
}
