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

package de.tourenplaner.computecore;

import de.tourenplaner.algorithms.Algorithm;
import de.tourenplaner.algorithms.AlgorithmFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * An AlgorithmManager manages the Algorithm instances used by a single
 * ComputeThread @see de.tourenplaner.computecore.ComputeThread
 * 
 * @author Niklas Schnelle, Peter Vollmer
 * 
 */
public class AlgorithmManager {

	private final Map<String, Algorithm> algs = new HashMap<String, Algorithm>();

	protected AlgorithmManager() {
	}

	/**
	 * Adds a new Algorithm to this manager, called by AlgorithmRegistry during
	 * construction of new AlgorithmManager instances
	 * 
	 * @param algFac
	 */
	protected void addAlgorithm(AlgorithmFactory algFac) {
		algs.put(algFac.getURLSuffix(), algFac.createAlgorithm());
	}

	/**
	 * Adds a new Algorithm to this manager, called by AlgorithmRegistry during
	 * construction of new AlgorithmManager instances
	 * 
	 * @param sn
	 * @param alg
	 */
	protected void addAlgorithm(String sn, Algorithm alg) {
		algs.put(sn, alg);
	}

	/**
	 * Used to get an Algorithm instance by it's URLSuffix. e.g. "sp" might give
	 * you an Algorithm instance for Shortest Paths
	 * 
	 * @param sn
	 * @return
	 */
	public Algorithm getAlgByURLSuffix(String sn) {
		return algs.get(sn);
	}
}
