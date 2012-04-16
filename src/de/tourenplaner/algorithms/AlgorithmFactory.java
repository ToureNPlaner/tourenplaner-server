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

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer

 * This factory class is used to create Algorithm instances and to provide
 * information on the created Algorithm used by clients to adapt to the server
 */
public abstract class AlgorithmFactory {
	/**
	 * Creates a new instance of the Algorithm class(s) associated with this
	 * factory
	 * 
	 * @return a new Algorithm instance
	 */
	public abstract Algorithm createAlgorithm();

	/**
	 * Used to get the URLSuffix for the constructed Algorithms e.. "sp" will
	 * make the Algorithm available under /algsp
	 * 
	 * @return
	 */
	public abstract String getURLSuffix();

	/**
	 * Returns the human readable name of the constructed Algorithms e.g.
	 * "Shortest Path"
	 * 
	 * @return
	 */
	public abstract String getAlgName();

	/**
	 * Returns the version of the constructed Algorithms
	 * 
	 * @return
	 */
	public abstract int getVersion();

	/**
	 * Returns if it is an isHidden algorithm
	 * 
	 */
	public abstract boolean isHidden();

    /**
     * Gets the Constraints not bound to any Point
     *
     * @return A List of Maps with the constraints or null
     */
    public abstract List<Map<String, Object>> getConstraints();

    /**
     * Gets Details for the the algorithm
     * this includes whether the algorithm has sourceIsTarget
     * and the minimal number of points
     *
     * @return A map with the details or null
     */
    public abstract Map<String, Object> getDetails();
}
