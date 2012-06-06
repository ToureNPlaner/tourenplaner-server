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
 * Used to create instances of ShortestPathCH algorithm
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ShortestPathCHFactory extends SharingAlgorithmFactory {

    public ShortestPathCHFactory(GraphRep graph) {
        super(graph);
        details.put("hidden", this.isHidden());
        details.put("minpoints", 2);
        details.put("sourceistarget", false);
    }

    @Override
    public String getDescription() {
        return "Calculates the shortest path visiting the given points in the order they are supplied";
    }

    @Override
    public Algorithm createAlgorithm(DijkstraStructs rs) {
        return new ShortestPathCH(graph, rs);
    }

    /*
      * @see de.tourenplaner.algorithms.AlgorithmFactory#createAlgorithm()
      */
    @Override
    public Algorithm createAlgorithm() {
        return new ShortestPathCH(graph, new DijkstraStructs(
                graph.getNodeCount(), graph.getEdgeCount()));
    }

    @Override
    public String getURLSuffix() {
        return "sp";
    }

    @Override
    public String getAlgName() {
        return "Shortest Path CH";
    }

    @Override
    public int getVersion() {
        return 1;
    }


    @Override
    public boolean isHidden() {
        return false;
    }

}
