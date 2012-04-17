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

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * provides heap structures for a minimum heap for ints
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class Heap {
    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");

	// 1) the heaplength is the number of elements in the
	// heap, not the real number of ints in the array. For example heaplength =
	// 3 means that the array containing the heap contains 6*integers. So for
	// heaplength = 3 the index of the last element is (3-1)/2 and the index for
	// the dist of the last element is ((3-1)/2)+1
	// 2) the pos parameter is the real position in the array
	// 3) a trinary heap is supposed to be a little bit faster

	private int[] heaparr;
	private int heaplength;

	// TODO: determine good value
	// with this number of elements in the heap there is no growing from
	// stuttgart-> hamburg
	private static final int arrayGrowthSum = 4000;

	/**
	 * initial size is {@value #arrayGrowthSum}
	 */
	public Heap() {
		heaparr = new int[arrayGrowthSum];
		heaplength = 0;
	}

    /**
     * initializes a heap with given initialSize
     *
     * @param initialSize
     */
	public Heap(int initialSize) {
		heaparr = new int[initialSize];
		heaplength = 0;
	}

    /**
     * insert a element to the heap
     *
     * @param id
     * @param dist
     */

	public final void insert(int id, int dist) {
		checkHeapArray();
		heaparr[heaplength * 2] = id;
		heaparr[(heaplength * 2) + 1] = dist;
		heaplength += 1;
		bubbleUp((heaplength - 1) * 2);
	}

    /**
     * check for empty heap
     *
     * @return
     */
	public final boolean isEmpty() {
		return heaplength <= 0;
	}

    /**
     * peeks the id of the minimum of the heap
     *
     * @return
     */
	public final int peekMinId() {
		return heaparr[0];
	}


    /**
     * peeks the dist of the minimum of the heap
     *
     * @return
     */
	public final int peekMinDist() {
		return heaparr[1];
	}

    /**
     * removes the min element of the heap
     */
	public final void removeMin() {
		heaplength -= 1;
		heaparr[0] = heaparr[heaplength * 2];
		heaparr[1] = heaparr[(heaplength * 2) + 1];
		siftDown(0);
	}

    /**
     * implements bubble up to correct miss alignments from the button of the heap
     *
     * @param pos
     */
	private final void bubbleUp(int pos) {
		int parent;
		int tempid;
		int tempdist;
		while (true) {
			// TODO: improve
			parent = (((pos / 2) - 1) / 3) * 2;
			if ((parent >= 0) && (heaparr[parent + 1] > heaparr[pos + 1])) {
				tempid = heaparr[parent];
				tempdist = heaparr[parent + 1];
				heaparr[parent] = heaparr[pos];
				heaparr[parent + 1] = heaparr[pos + 1];
				heaparr[pos] = tempid;
				heaparr[pos + 1] = tempdist;
				pos = parent;
			} else {
				break;
			}

		}
	}


    /**
     * implements sift down to correct miss alignments from the top of the heap.
     *
     * @param pos
     */
	private final void siftDown(int pos) {
		int tempid;
		int tempdist;

		int child1;
		int child2;
		int child3;
		int minChild;
		while (true) {
			// TODO: improve
			child1 = (((pos / 2) * 3) + 1) * 2;
			child2 = child1 + 2;
			child3 = child1 + 4;

			minChild = -1;

			if (child1 <= ((heaplength * 2) - 2)) {
				minChild = child1;
			}
			if ((child2 <= ((heaplength * 2) - 2))
					&& (heaparr[child2 + 1] < heaparr[minChild + 1])) {
				minChild = child2;
			}
			if ((child3 <= ((heaplength * 2) - 2))
					&& (heaparr[child3 + 1] < heaparr[minChild + 1])) {
				minChild = child3;
			}

			if ((minChild != -1) && (heaparr[minChild + 1] < heaparr[pos + 1])) {
				tempid = heaparr[pos];
				tempdist = heaparr[pos + 1];
				heaparr[pos] = heaparr[minChild];
				heaparr[pos + 1] = heaparr[minChild + 1];
				heaparr[minChild] = tempid;
				heaparr[minChild + 1] = tempdist;
				pos = minChild;
			} else {
				break;
			}
		}
	}

    /**
     * resets heap
     */
    public final void resetHeap() {
		heaplength = 0;
	}

    /**
     * checks heap array size and increase if needed
     */
	private final void checkHeapArray() {
		if (((heaplength * 2) + 1) >= heaparr.length) {
			log.finer("Increased Heap size from " + heaparr.length
					+ " to " + (heaparr.length + arrayGrowthSum));
			heaparr = Arrays.copyOf(heaparr, heaparr.length + arrayGrowthSum);
		}
	}
}
