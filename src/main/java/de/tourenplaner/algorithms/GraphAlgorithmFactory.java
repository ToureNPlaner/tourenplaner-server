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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to construct Graphalgorithms
 * and provides additional information only used by Graphalgorithms
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public abstract class GraphAlgorithmFactory extends AlgorithmFactory {

    protected GraphRep graph;
    protected final List<Map<String, Object>> pointConstraints;


    public GraphAlgorithmFactory(GraphRep graph) {
        this.graph = graph;
        this.pointConstraints = new ArrayList<Map<String, Object>>();
    }

    /**
     * Gets the List of PointConstraints in a jsonserializable Map format
     *
     * @return A list of maps of pointconstraints or null
     */
    public List<Map<String, Object>> getPointConstraints() {
        return pointConstraints;
    }
}
