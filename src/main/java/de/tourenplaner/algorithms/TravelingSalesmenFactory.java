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
 * Used to create instances of TravelingSalesmen algorithm
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class TravelingSalesmenFactory extends SharingAlgorithmFactory {

    public TravelingSalesmenFactory(GraphRep graph) {
        super(graph);
        details.put("hidden", this.isHidden());
        details.put("minpoints", 2);
        details.put("sourceistarget", true);

    }

    @Override
    public String getDescription() {
        return "Calculates a short tour visiting each of the supplied points";
    }

    @Override
    public Algorithm createAlgorithm(DijkstraStructs rs) {
        return new TravelingSalesman(graph, rs);
    }

    @Override
    public Algorithm createAlgorithm() {
        return new TravelingSalesman(graph, new DijkstraStructs(graph.getNodeCount(), graph.getEdgeCount()));
    }

    @Override
    public String getURLSuffix() {
        return "tsp";
    }

    @Override
    public String getAlgName() {
        return "Traveling Salesman";
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
