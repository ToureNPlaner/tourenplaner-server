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

package de.tourenplaner.graphrep;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.fail;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class TestGraphReader {

    GraphRepTextReader graphRepTextReader = new GraphRepTextReader();

    public GraphRep readTestGraph() {
        String
                path_15k =
                GraphRepTextReaderTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() +
                "../data/test/15k_ch.txt";
        System.out.println("Loading graph from " + path_15k);
        try {
            return graphRepTextReader.createGraphRep(new FileInputStream(path_15k));
        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
    }

}
