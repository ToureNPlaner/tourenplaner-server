/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.computecore;

import de.tourenplaner.algorithms.AlgorithmFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

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
    private static Logger log = Logger.getLogger("tourenplaner");

	private final HashMap<String, AlgorithmFactory> registry;

	/**
	 * Constructs a new empty AlgorithmRegistry
	 * 
	 */
	public AlgorithmRegistry() {
		registry = new HashMap<String, AlgorithmFactory>();
	}

	/**
	 * Registers a new AlgorithmFactory in this registry
	 * 
	 * @param algFac
	 */
	public void registerAlgorithm(AlgorithmFactory algFac) {
        log.fine("AlgorithmFactory for "+algFac.getAlgName()+" registered");
		registry.put(algFac.getURLSuffix(), algFac);
	}

	/**
	 * Creates a new AlgorithmManager that has an instance for every registered
	 * Algorithms
	 * 
	 * @return
	 */
	public de.tourenplaner.computecore.AlgorithmManager getAlgorithmManager(de.tourenplaner.computecore.AlgorithmManagerFactory amFac) {
		de.tourenplaner.computecore.AlgorithmManager m = amFac.createAlgorithmManager();

		for (AlgorithmFactory algFac : registry.values()) {
			m.addAlgorithm(algFac);
		}

		return m;
	}

    /**
     * Gets the AlgorithmFactory registred for the given urlSuffix
     * @param urlSuffix
     * @return
     */
    public AlgorithmFactory getAlgByURLSuffix(String urlSuffix){
        return registry.get(urlSuffix);
    }

	/**
	 * Gets a Collection of registered Algorithm's represented by their specific
	 * AlgorithmFactories
	 * 
	 * @return collection of registred Factories
	 */
	public Collection<AlgorithmFactory> getAlgorithms() {
		return registry.values();
	}
}
