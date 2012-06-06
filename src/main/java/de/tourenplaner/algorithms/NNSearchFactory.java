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

package de.tourenplaner.algorithms;

import de.tourenplaner.graphrep.GraphRep;

/**
 * Provides an implementation of resource NNSearch algorithm.
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class NNSearchFactory extends GraphAlgorithmFactory {


    public NNSearchFactory(GraphRep graph) {
        super(graph);
        details.put("hidden", this.isHidden());
        details.put("minpoints", 1);
        details.put("sourceistarget", false);

    }

    @Override
    public String getDescription() {
        return "Finds the nearest point on a road for the given coordinates";
    }

    /*
      * @see de.tourenplaner.algorithms.AlgorithmFactory#createAlgorithm()
      */
    @Override
    public Algorithm createAlgorithm() {
        return new NNSearch(graph);
    }

    @Override
    public String getURLSuffix() {
        return "nns";
    }

    @Override
    public String getAlgName() {
        return "Nearest Neighbor Search";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

}
