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

import de.tourenplaner.computecore.ComputeRequest;

/**
 * A class implementing this interface can be used by the ComputeCore to
 * do Computations
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public interface Algorithm {

	/**
	 * Runs the Algorithm instance on the given ComputeRequest
	 *
     * @param req The compute request
     * @throws ComputeException see java doc of exception
     * @throws Exception Exceptions which occurred because of server problems
     *      or faulty algorithm code
	 */
    void compute(ComputeRequest req) throws ComputeException, Exception;

}