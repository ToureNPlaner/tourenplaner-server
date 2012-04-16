/**
 * 
 */
package de.tourenplaner.config;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Vollmer
 * 
 *         This config manager loads configurations from a config file which is
 *         formated in JSON.
 */
public class ConfigManager {

	private static volatile ConfigManager instance;

	/**
	 * Storage of configuration entries
	 */
	private final Map<String, Object> confMap;

    /**
     * The base path to the keys of this ConfigManager instance. If the instance is
     * created with a config file, the path base is an empty string. If the instance is
     * created from hash map within a multiple level ConfigFile, the path base consists
     * of all keys which leads to the corresponding hash map.
     *
     * Example:
     *
     * <pre>
     * {
     *     "key1" : {
     *         "key2" : {
     *             "key3" : {
     *                 "another-key" : "a value"
     *             }
     *         }
     *     }
     * }
     * </pre>
     *
     * If a ConfigManager is created with the hash map "key3" the path base for all
     * values within the map will be "key1.key2.key3."
     *
     */
    private final String keyPathBase;
    

	/**
	 * Construct the ConfigManager and loads the config from JSON file with the
	 * given file path.
	 * 
	 *
     * @param configPath the file system path to the config file
     * @throws Exception Thrown if mapping of config file fails, for example
     *      because the file was not found or the file has a bad syntax
	 */
	private ConfigManager(String configPath)
			throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		this.confMap = mapper.readValue(new File(configPath),
				new TypeReference<Map<String, Object>>() {
				});
        this.keyPathBase = "";
	}

	/**
	 * Used to construct an empty ConfigManager that only returns defaults
	 */
	private ConfigManager() {
		this.confMap = null;
        this.keyPathBase = "";
	}

    /**
     * Used to construct a ConfigManager with the entries of the given map<br />
     * If confMap == null then this ConfigManager will only return defaults
     *
     * @param confMap a map with the entries for the new ConfigManager
     * @param keyPathBase the path base consisting of keys which leads 
     *                    to the corresponding hash map
     */
    private ConfigManager(Map<String, Object> confMap, String keyPathBase) {
        this.confMap = confMap;
        this.keyPathBase = (keyPathBase == null) ? "" : keyPathBase;
    }

	/**
	 * Initialize the config management and construct the configManager. To do
	 * before getInstance. If Init fails the ConfigManager can be used as an
	 * empty Config that only returns the default
	 *
     * @param configPath the file system path to the config file
	 * @throws Exception Thrown if mapping of config file fails, for example
     *      because the file was not found or the file has a bad syntax
	 */
	public static void init(String configPath)
			throws Exception {
		instance = new ConfigManager(configPath);
	}

	/**
	 * Returns the double value of a given key or the given defaultValue if there
	 * is a cast error or the key is not found. This is for all types number
	 * with an comma.
	 * 
	 * @param key the key
	 * @param defaultValue the default value
	 * @return value of a given key or the given defaultValue
	 *         (ClassCastError/key not found)
	 */
	public double getEntryDouble(String key, double defaultValue) {
		Double value;
		if ((confMap != null) && confMap.containsKey(key)) {
			if (confMap.get(key) != null) {
				try {
					value = (Double) confMap.get(key);
				} catch (ClassCastException e) {
                    System.out.println("Failed to read config value for the key "
                            + this.keyPathBase + key + ": " + e.getMessage());
					e.printStackTrace();
					return defaultValue;
				}
			} else {
				value = defaultValue;
			}

		} else {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * Returns the boolean value of a given key or the given defaultValue if
	 * there is a cast error or the key is not found.
	 * 
	 * @param key the key
	 * @param defaultValue the default value
	 * @return value of a given key or the given defaultValue
	 *         (ClassCastError/key not found)
	 */
	public boolean getEntryBool(String key, boolean defaultValue) {
		Boolean value;
		if ((confMap != null) && confMap.containsKey(key)) {
			if (confMap.get(key) != null) {
				try {
					value = (Boolean) confMap.get(key);
				} catch (ClassCastException e) {
                    System.out.println("Failed to read config value for the key "
                            + this.keyPathBase + key + ": " + e.getMessage());
					e.printStackTrace();
					return defaultValue;
				}
			} else {
				value = defaultValue;
			}

		} else {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * Returns the long value of a given key or the given defaultValue if there
	 * is a cast error or the key is not found. This is for all types of number
	 * without an comma.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return value of a given key or the given defaultValue
	 *         (ClassCastError/key not found)
	 */
	public long getEntryLong(String key, long defaultValue) {
		Long value;
		if ((confMap != null) && confMap.containsKey(key)) {
			if (confMap.get(key) != null) {
				try {
					value = (Long) confMap.get(key);
				} catch (ClassCastException e) {
                    System.out.println("Failed to read config value for the key "
                            + this.keyPathBase + key + ": " + e.getMessage());
					e.printStackTrace();
					return defaultValue;
				}
			} else {
				value = defaultValue;
			}

		} else {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * Returns the int value of a given key or the given defaultValue if there is
	 * a cast error or the key is not found. This is for all types of number
	 * without an comma.
	 * 
	 * @param key the key
	 * @param defaultValue the default value
	 * @return value of a given key or the given defaultValue
	 *         (ClassCastError/key not found)
	 */
	public int getEntryInt(String key, int defaultValue) {
		Number value;
		if ((confMap != null) && confMap.containsKey(key)) {
			if (confMap.get(key) != null) {
				try {
					value = (java.lang.Number) confMap.get(key);
				} catch (ClassCastException e) {
                    System.out.println("Failed to read config value for the key "
                            + this.keyPathBase + key + ": " + e.getMessage());
					e.printStackTrace();
					return defaultValue;
				}
			} else {
				value = defaultValue;
			}

		} else {
			value = defaultValue;
		}
		return value.intValue();
	}

	/**
	 * 
	 * Returns the String value of a given key or the defaultValue if there is a
	 * cast error or the key is not found.
	 * 
	 * @param key the key
	 * @param defaultValue the default value
	 * @return value of a given key or the given defaultValue
	 *         (ClassCastError/key not found)
	 */
	public String getEntryString(String key, String defaultValue) {
		String value;
		if ((confMap != null) && confMap.containsKey(key)) {
			if (confMap.get(key) != null) {
				try {
					value = (String) confMap.get(key);
				} catch (ClassCastException e) {
                    System.out.println("Failed to read config value for the key "
                            + this.keyPathBase + key + ": " + e.getMessage());
					e.printStackTrace();
					return defaultValue;
				}
			} else {
				value = defaultValue;
			}

		} else {
			value = defaultValue;
		}
		return value;
	}


    /**
     *
     * Returns a ConfigManager instance based on the hash map value of a given key
     * or based on the defaultValue if there is a cast error or the key is not found.
     * If defaultValue is null, a ConfigManager that only returns defaults will be returned.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return value of a given key or the given defaultValue
     *         (ClassCastError/key not found)
     */
    public ConfigManager getEntryMap(String key, Map<String, Object> defaultValue) {
        ConfigManager value;
        
        String newKeyPathBase = this.keyPathBase + key + ".";
        ConfigManager defaultConfigManager = new ConfigManager(defaultValue, newKeyPathBase);
        
        if ((confMap != null) && confMap.containsKey(key)) {
            if (confMap.get(key) != null) {
                try {
                    // TODO check if an unchecked cast would work without exceptions outside of this try

                    Map<?, ?> map = (Map<?, ?>) confMap.get(key);
                    HashMap<String, Object> newMap = new HashMap<String, Object>();
                    
                    for (Object subKey : map.keySet()) {
                        if (subKey instanceof String) {
                            newMap.put((String) subKey, map.get(subKey));
                        } else {
                            System.out.println("Failed to read config value for the key "
                                    + this.keyPathBase + key + ": A key of the hash map was not a string.");
                            return defaultConfigManager;
                        }
                    }

                    value = new ConfigManager(newMap, newKeyPathBase);
                } catch (ClassCastException e) {
                    System.out.println("Failed to read config value for the key "
                            + this.keyPathBase + key + ": " + e.getMessage());
                    e.printStackTrace();
                    return defaultConfigManager;
                }
            } else {
                value = defaultConfigManager;
            }

        } else {
            value = defaultConfigManager;
        }
        return value;
    }

	/**
	 * Return an instance of a constructed ConfigManager. If there was no Init
	 * before an empty ConfigManager will be created that always returns the
	 * default
	 * 
	 * 
	 * @return instance of ConfigManager
	 */
	public static ConfigManager getInstance() {
		if (instance == null) {
			instance = new ConfigManager();
		}

		return instance;
	}

    /**
     * Resets the ConfigManager as if it would not have been initialized
     */
    public static void resetConfigManager() {
        instance = null;
    }

}
