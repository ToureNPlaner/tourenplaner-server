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

import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.algorithms.AlgorithmFactory;
import de.tourenplaner.algorithms.DijkstraStructs;
import de.tourenplaner.algorithms.SharingAlgorithmFactory;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 * 
 */
public class ShareEnabledAM extends AlgorithmManager {
	private final DijkstraStructs rs;

	public ShareEnabledAM(GraphRep graph) {
		rs = new DijkstraStructs(graph.getNodeCount(), graph.getEdgeCount());
	}

	/**
	 * @see de.tourenplaner.computecore.AlgorithmManager#addAlgorithm(de.tourenplaner.algorithms.AlgorithmFactory)
	 */
	@Override
	public void addAlgorithm(AlgorithmFactory algFac) {
		if (algFac instanceof SharingAlgorithmFactory) {
			addAlgorithm(algFac.getURLSuffix(), ((SharingAlgorithmFactory) algFac).createAlgorithm(rs));
		} else {
			addAlgorithm(algFac.getURLSuffix(), algFac.createAlgorithm());
		}
	}
}
