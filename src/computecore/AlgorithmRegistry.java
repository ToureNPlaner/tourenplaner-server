/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.ArrayList;
import java.util.Collection;

import algorithms.AlgorithmFactory;

/**
 * An AlgorithmRegistry is used to register available Algorithms represented by
 * there specific Factory Objects. Every registered Algorithm will be available
 * to ComputeThreads through their AlgorithmManagers and will be listed as
 * available Algorithm in the ServerInfo
 * 
 * @author Niklas Schnelle, Peter Vollmer
 * 
 */
public class AlgorithmRegistry {

	private final ArrayList<AlgorithmFactory> registry;

	/**
	 * Constructs a new empty AlgorithmRegistry
	 * 
	 */
	public AlgorithmRegistry() {
		registry = new ArrayList<AlgorithmFactory>();
	};

	/**
	 * Registers a new AlgorithmFactory in this registry
	 * 
	 * @param algFac
	 */
	public void registerAlgorithm(AlgorithmFactory algFac) {
		registry.add(algFac);
	}

	/**
	 * Creates a new AlgorithmManager that has an instance for every registered
	 * Algorithms
	 * 
	 * @return
	 */
	public AlgorithmManager getAlgorithmManager(AlgorithmManagerFactory amFac) {
		AlgorithmManager m = amFac.createAlgorithmManager();

		for (AlgorithmFactory algFac : registry) {
			m.addAlgorithm(algFac);
		}

		return m;
	}

	/**
	 * Gets a Collection of registered Algorithm's represented by their specific
	 * AlgorithmFactories
	 * 
	 * @return collection of registred Factories
	 */
	public Collection<AlgorithmFactory> getAlgorithms() {
		return registry;
	}
}
