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

package de.tourenplaner.utils;

/**
 * Sorter Utility class based on a trinary heap
 *
 * @author Niklas Schnelle
 */
public class Sorter {
	public static void sort(SortAdapter toSort) {

		heapify(toSort);
		int endI = toSort.length() - 1;
		while (endI > 0) {
			toSort.swap(endI, 0);
			siftDown(toSort, 0, endI - 1);
			endI--;
		}
	}


	private static void heapify(SortAdapter toSort) {
		int pos = toSort.length() - 1;
		while (pos >= 0) {
			siftDown(toSort, pos, toSort.length() - 1);
			pos--;
		}
	}

	private static void siftDown(SortAdapter toSort, int topI, int endI) {
		int cLI;
		int cMI;
		int cRI;
		int cMaxI;

		while (topI * 3 + 1 <= endI) {
			cLI = topI * 3 + 1;
			cMI = cLI + 1;
			cRI = cLI + 2;
			cMaxI = topI;

			if (toSort.less(cMaxI, cLI)) {
				cMaxI = cLI;
			}
			if (cMI <= endI && toSort.less(cMaxI, cMI)) {
				cMaxI = cMI;
			}
			if (cRI <= endI && toSort.less(cMaxI, cRI)) {
				cMaxI = cRI;
			}
			if (cMaxI != topI) {
				toSort.swap(cMaxI, topI);
				topI = cMaxI;
			} else {
				return;
			}
		}
	}
}
