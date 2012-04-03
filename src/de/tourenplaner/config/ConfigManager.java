/**
 * 
 */
package de.tourenplaner.config;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
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
	 * Construct the ConfigManager and loads the config from JSON file with the
	 * given file path.
	 * 
	 * @param mapper
	 *            shared ObjectMapper
	 * @param configPath
	 * @throws Exception
	 */
	private ConfigManager(ObjectMapper mapper, String configPath)
			throws Exception {
		this.confMap = mapper.readValue(new File(configPath),
				new TypeReference<Map<String, Object>>() {
				});

	}

	/**
	 * Used to construct an empty config Manager that only returns defaults
	 */
	private ConfigManager() {
		this.confMap = null;
	}

	/**
	 * Initialize the config management and construct the configManager. To do
	 * before getInstance. If Init fails the ConfigManager can be used as an
	 * empty Config that only returns the default
	 * 
	 * @param configPath
	 * 
	 * @throws Exception
	 */
	public static void init(ObjectMapper mapper, String configPath)
			throws Exception {
		instance = new ConfigManager(mapper, configPath);
	}

	/**
	 * Return the double value of a given key or the given defaultValue if there
	 * is a cast error or the key is not found. This is for all types number
	 * with an comma.
	 * 
	 * @param key
	 * @param defaultValue
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
					System.out.println("Failed to read config value: "
							+ e.getMessage());
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
	 * Return the boolean value of a given key or the given defaultValue if
	 * there is a cast error or the key is not found.
	 * 
	 * @param key
	 * @param defaultValue
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
					System.out.println("Failed to read config value: "
							+ e.getMessage());
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
	 * Return the long value of a given key or the given defaultValue if there
	 * is a cast error or the key is not found. This is for all types of number
	 * without an comma.
	 *
	 * @param key
	 * @param defaultValue
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
					System.out.println("Failed to read config value: "
							+ e.getMessage());
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
	 * Return the int value of a given key or the given defaultValue if there is
	 * a cast error or the key is not found. This is for all types of number
	 * without an comma.
	 * 
	 * @param key
	 * @param defaultValue
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
					System.out.println("Failed to read config value: "
							+ e.getMessage());
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
	 * Return the String value of a given key or the defaultValue if there is a
	 * cast error or the key is not found.
	 * 
	 * @param key
	 * @param defaultValue
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
					System.out.println("Failed to read config value: "
							+ e.getMessage());
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
     * Return the Map<?, ?> value of a given key or the defaultValue if there is a
     * cast error or the key is not found.
     *
     * To read the map (should be of type Map<String, Object>) you can use following code:
     *
     * <pre>
     *     Map<?, ?> map = ConfigManager.getInstance().getEntryMap("key", new HashMap<String, Object>());
     *     for (Object key : map.keySet()) {
     *         Object value = map.get(key);
     *         if (key instanceof String) {
     *             String keyString = (String) key;
     *             // do something with this entry
     *         }
     *     }
     * </pre>
     *
     * @param key
     * @param defaultValue
     * @return value of a given key or the given defaultValue
     *         (ClassCastError/key not found)
     */
    public Map<?, ?> getEntryMap(String key, Map<String, Object> defaultValue) {
        Map<?, ?> value;
        if ((confMap != null) && confMap.containsKey(key)) {
            if (confMap.get(key) != null) {
                try {
                    value = (Map<?, ?>) confMap.get(key);
                } catch (ClassCastException e) {
                    System.out.println("Failed to read config value: "
                            + e.getMessage());
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

}
