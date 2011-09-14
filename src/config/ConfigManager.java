/**
 * 
 */
package config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author Peter Vollmer
 * 
 * This config manager loads configurations from a config file which is formated in JSON. 
 */
public class ConfigManager {

	private static ConfigManager instance = null;

	/**
	 * Parses the config file in JSON format.
	 */
	private final JSONParser parser = new JSONParser();

	/**
	 * Storage of configuration entries
	 */
	private Map<String, Object> confMap = null;

	/**
	 * Construct the ConfigManager and loads the config from JSON file with the given file path.
	 * 
	 * @param configPath
	 * @throws Exception
	 */
	private ConfigManager(String configPath) throws Exception {
		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				configPath), Charset.forName("UTF-8")));
		JSONObject requestJSON = (JSONObject) parser.parse(reader);

		this.confMap = requestJSON;

	}
	
	/** 
	 * Used to construct an empty config Manager that only returns defaults
	 */
	private ConfigManager(){}


	/**
	 * Initialize the config management and construct the configManager. To do
	 * before getInstance. If Init fails the ConfigManager can be used
	 * as an empty Config that only returns the deault
	 * 
	 * @param configPath
	 * 
	 * @throws Exception
	 */
	public static void Init(String configPath) throws Exception {
		try {
			instance = new ConfigManager(configPath);
		} catch (IOException e){
			// initialize with empty ConfigManager if we can't load
			instance = new ConfigManager();
			throw e;
		}
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
		Double value = null;
		if (confMap != null && confMap.containsKey(key)) {
			if (confMap.get(key) != null) {
				try {
					value = (Double) confMap.get(key);
				} catch (ClassCastException e) {
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
		Boolean value = null;
		if (confMap != null && confMap.containsKey(key)) {
			if (confMap.get(key) != null) {
				try {
					value = (Boolean) confMap.get(key);
				} catch (ClassCastException e) {
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
		Long value = null;
		if (confMap != null && confMap.containsKey(key)) {
			if (confMap.get(key) != null) {
				try {
					value = (Long) confMap.get(key);
				} catch (ClassCastException e) {
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
	 * Return the String value of a given key or the defaultValue if there is a
	 * cast error or the key is not found.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return value of a given key or the given defaultValue
	 *         (ClassCastError/key not found)
	 */
	public String getEntryString(String key, String defaultValue) {
		String value = null;
		if (confMap != null && confMap.containsKey(key)) {
			if (confMap.get(key) != null) {
				try {
					value = (String) confMap.get(key);
				} catch (ClassCastException e) {
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
	 * Return an instance of a constructed ConfigManager. 
	 * If there was no Init before an empty ConfigManager will be
	 * created that always returns the default
	 * 
	 * 
	 * @return instance of ConfigManager
	 */
	public static ConfigManager getInstance() {
		if(instance == null){
			instance = new ConfigManager();
		}
		
		return instance;
	}

}
