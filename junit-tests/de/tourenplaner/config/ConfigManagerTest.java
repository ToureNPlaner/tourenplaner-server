/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ConfigManagerTest {
    
    private ConfigManager currentConfigManager = null;
    private String currentConfigName = "";

    private void readConfigFile(String configName) throws Exception {
        String configPath =
                ConfigManager.class.getProtectionDomain().getCodeSource().getLocation().getPath() +
                        "../data/test/configfiles/" + configName;
        System.out.println("Loading config file " + configPath);
        ConfigManager.init(configPath);
    }

    
    @Test(expected = Exception.class)
    public void testExceptionOnBadSyntax() throws Exception {
        ConfigManager.resetConfigManager();
        readConfigFile("badsyntax.conf");
        fail("There should be an exception");
    }

    @Test(expected = Exception.class)
    public void testExceptionOnNullPath() throws Exception {
        ConfigManager.resetConfigManager();
        ConfigManager.init(null);
        fail("There should be an exception");
    }


    @Test(expected = Exception.class)
    public void testExceptionOnNotFoundx() throws Exception {
        ConfigManager.resetConfigManager();
        readConfigFile("anyfilenamethatshouldnotexist.conf");
        fail("There should be an exception");
    }


    private void testGetEntryDouble(String key, double expected, double defaultValue) {
        double DELTA = 0.00000000001;

        String message = "Test fails: config '" + this.currentConfigName + "', getEntryDouble, " +
                "key '" + key + "', expected '" + expected + "', value '" + defaultValue + "'";

        assertEquals(message, expected, this.currentConfigManager.getEntryDouble(key, defaultValue), DELTA);
    }

    private void testGetEntryBool(String key, boolean expected, boolean defaultValue) {
        String message = "Test fails: config '" + this.currentConfigName + "', getEntryBool, " +
                "key '" + key + "', expected '" + expected + "', value '" + defaultValue + "'";

        assertEquals(message, expected, this.currentConfigManager.getEntryBool(key, defaultValue));
    }

    private void testGetEntryLong(String key, long expected, long defaultValue) {
        String message = "Test fails: config '" + this.currentConfigName + "', getEntryLong, " +
                "key '" + key + "', expected '" + expected + "', value '" + defaultValue + "'";

        assertEquals(message, expected, this.currentConfigManager.getEntryLong(key, defaultValue));
    }

    private void testGetEntryInt(String key, int expected, int defaultValue) {
        String message = "Test fails: config '" + this.currentConfigName + "', getEntryInt, " +
                "key '" + key + "', expected '" + expected + "', value '" + defaultValue + "'";

        assertEquals(message, expected, this.currentConfigManager.getEntryInt(key, defaultValue));
    }

    private void testGetEntryString(String key, String expected, String defaultValue) {
        String message = "Test fails: config '" + this.currentConfigName + "', getEntryString, " +
                "key '" + key + "', expected '" + expected + "', value '" + defaultValue + "'";

        assertEquals(message, expected, currentConfigManager.getEntryString(key, defaultValue));
    }


    /**
     * Prepares ConfigManager for with next config file
     *
     * @param configName if null the default config will be loaded
     * @throws Exception throws if reading config file fails
     */
    private void prepareNextConfigManager(String configName) throws Exception {
        currentConfigName = "defaultConfig";
        ConfigManager.resetConfigManager();
        if (configName != null) {
            currentConfigName = configName;
            readConfigFile("test.conf");
        }
        currentConfigManager = ConfigManager.getInstance();
    }

    /**
     * will check if only defaults will be retrieved, and not values within the test.conf
     */
    private void testDefaultConfig() {
        // we should get the default values
        testGetEntryDouble("double", 4.2,  4.2);
        testGetEntryBool("bool", false,  false);
        testGetEntryLong("long", 357468147680579L, 357468147680579L);
        testGetEntryInt("int", 3, 3);
        testGetEntryString("string", "allyourbasearebelongtous", "allyourbasearebelongtous");

        // we should get the default values if we use null as key
        testGetEntryDouble(null, 4.2,  4.2);
        testGetEntryBool(null, false,  false);
        testGetEntryLong(null, 357468147680579L, 357468147680579L);
        testGetEntryInt(null, 3, 3);
        testGetEntryString(null, "allyourbasearebelongtous", "allyourbasearebelongtous");
    }

    /**
     * will check if retrieved values matches with real values of the test.conf,
     * and not with default values. also checks if wrong getEntryMethods will
     * retrieve default values
     */
    private void testTestConfig() {
        // check if we get the correct values from test.conf and not the defaults
        testGetEntryDouble("double", 493.7213587466, 4.2);
        testGetEntryBool("bool", true, false);
        testGetEntryLong("long", 94437512851394228L, 357468147680579L);
        testGetEntryInt("int", -385155479, 3);
        testGetEntryString("string", "end of the universe", "allyourbasearebelongtous");


        // check if we get default values if we using getEntry methods
        // whose types differ from the types of the retrieved json values

        // interpret double "493.7213587466"
        testGetEntryBool("double", false, false);
        testGetEntryLong("double", 357468147680579L, 357468147680579L);
        // testGetEntryInt interprets double as int (double value is floored)
        testGetEntryString("double", "allyourbasearebelongtous", "allyourbasearebelongtous");

        // interpret bool "true"
        testGetEntryDouble("bool", 4.2, 4.2);
        testGetEntryLong("bool", 357468147680579L, 357468147680579L);
        testGetEntryInt("bool", 3, 3);
        testGetEntryString("bool", "allyourbasearebelongtous", "allyourbasearebelongtous");

        // interpret long "94437512851394228L,"
        testGetEntryDouble("long", 4.2, 4.2);
        testGetEntryBool("long", false, false);
        // TODO this should return 3, but returns overflowed value
        //testGetEntryInt("long", 3, 3);
        testGetEntryString("long", "allyourbasearebelongtous", "allyourbasearebelongtous");

        // interpret int "-385155479"
        testGetEntryDouble("int", 4.2, 4.2);
        testGetEntryBool("int", false, false);
        testGetEntryLong("int", 357468147680579L, 357468147680579L);
        testGetEntryString("int", "allyourbasearebelongtous", "allyourbasearebelongtous");

        // interpret string "end of the universe"
        testGetEntryDouble("string", 4.2, 4.2);
        testGetEntryBool("string", false, false);
        testGetEntryLong("string", 357468147680579L, 357468147680579L);
        testGetEntryInt("string", 3, 3);

        // check values which can be interpreted as another type
        testGetEntryInt("double", 493, 3);
        // TODO check if we should avoid overflows through casts from long to int
        testGetEntryInt("long", -419787084, 3);
    }

    @Test
    public void testConfigManager() throws Exception {

        // test ConfigManager without init
        prepareNextConfigManager(null);

        testDefaultConfig();

        // test ConfigManager with test.conf
        prepareNextConfigManager("test.conf");

        testTestConfig();

        // check nested hash maps
        ConfigManager oldConfigManager = currentConfigManager;
        String oldConfigName = currentConfigName;

        currentConfigManager = oldConfigManager.getEntryMap("another-pseudo-key", null);
        currentConfigName = oldConfigName + ".map-key-not-found-level-1";
        testDefaultConfig();

        currentConfigManager = oldConfigManager.getEntryMap(null, null);
        currentConfigName = oldConfigName + ".map-key-is-null-level-1";
        testDefaultConfig();

        currentConfigManager = oldConfigManager.getEntryMap("level-1", null);
        currentConfigName = oldConfigName + ".level-1";
        testTestConfig();

        oldConfigManager = currentConfigManager;
        oldConfigName = currentConfigName;

        currentConfigManager = oldConfigManager.getEntryMap("another-pseudo-key", null);
        currentConfigName = oldConfigName + ".map-key-not-found-level-2";
        testDefaultConfig();

        currentConfigManager = oldConfigManager.getEntryMap(null, null);
        currentConfigName = oldConfigName + ".map-key-is-null-level-2";
        testDefaultConfig();

        currentConfigManager = oldConfigManager.getEntryMap("level-2", null);
        currentConfigName = oldConfigName + ".level-2";
        testTestConfig();

    }

}

