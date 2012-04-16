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

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ConfigManagerTest {

    private void readConfigFile(String configName) throws Exception {
        String configPath =
                ConfigManager.class.getProtectionDomain().getCodeSource().getLocation().getPath() +
                        "../data/test/configfiles/" + configName;
        System.out.println("Loading config file " + configPath);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        ConfigManager.init(mapper, configPath);

    }

    
    @Test(expected = Exception.class)
    public void testExceptionOnBadSyntax() throws Exception {
        ConfigManager.resetConfigManager();
        readConfigFile("badsyntax.conf");
    }


    @Test(expected = Exception.class)
    public void testExceptionOnNotFoundx() throws Exception {
        ConfigManager.resetConfigManager();
        readConfigFile("anyfilenamethatshouldnotexist.conf");
    }
    
    private void testGetEntryMethods(ConfigManager cm, String configName, Map<String, Object> expectedValues) {
        assertEquals("get double in " + configName + " fails",
                (Double) expectedValues.get("double"), cm.getEntryDouble("double", 4.2), 0.00000000001);
        assertEquals("get bool in " + configName + " fails",
                expectedValues.get("bool"), cm.getEntryBool("bool", false));
        assertEquals("get long in " + configName + " fails",
                expectedValues.get("long"), cm.getEntryLong("long", 357468147680579L));
        assertEquals("get int in " + configName + " fails",
                expectedValues.get("int"), cm.getEntryInt("int", 3));
        assertEquals("get string in " + configName + " fails",
                expectedValues.get("string"), cm.getEntryString("string", "allyourbasearebelongtous"));
        
    }

    @Test
    public void testConfigManager() {
        ConfigManager cm;
        
        Map<String, Object> expectedValues = new HashMap<String, Object>();

        // test ConfigManager without init
        ConfigManager.resetConfigManager();
        cm = ConfigManager.getInstance();

        expectedValues.put("double", 4.2);
        expectedValues.put("bool", false);
        expectedValues.put("long", 357468147680579L);
        expectedValues.put("int", 3);
        expectedValues.put("string", "allyourbasearebelongtous");

        testGetEntryMethods(cm, "default config", expectedValues);

        try {
            ConfigManager.resetConfigManager();
            readConfigFile("test.conf");
            cm = ConfigManager.getInstance();

            // check if we get the correct values
            expectedValues.clear();
            expectedValues.put("double", 493.7213587466);
            expectedValues.put("bool", true);
            expectedValues.put("long", 94437512851394228L);
            expectedValues.put("int", -385155479);
            expectedValues.put("string", "end of the universe");
            testGetEntryMethods(cm, "default config", expectedValues);

        } catch (Exception e) {
            fail("Exception catched: " + e.getMessage());
        }
    }

}

