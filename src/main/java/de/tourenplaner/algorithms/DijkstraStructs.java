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

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayDeque;

import java.util.Arrays;
import java.util.ConcurrentModificationException;

/**
 * This class is used to share data structures used by several Dijkstra
 * based algorithms. This helps to prevent unnecessary memory usage and
 * minimizes time spent in allocations.<br /><br />
 * 
 * Instances of this class MUST only be shared within the same thread
 * and can only be used by one algorithm at a time
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class DijkstraStructs {
	// Heap used as priority queue in Dijkstra
	private final Heap heap;

	// Used to mark nodes with BFS
	private final BitSet marked;
	private final BitSet visited;

	// Dequeue used as fifo and stack
	private final IntArrayDeque deque;

	// dists in this array are stored with the multiplier applied. They also are
	// rounded and are stored as integers
	private final int[] dists;

	// Stores at position i the edge leading to the node i in the shortest path
	// tree
	private final int[] prevEdges;

	private boolean heapBorrowed;
	private boolean markedBorrowed;
	private boolean visitedBorrowed;
	private boolean dequeBorrowed;
	private boolean distsBorrowed;
	private boolean prevsBorrowed;

	/**
	 * Creates a new DijkstraStructs instance usable for a new thread. This
	 * instance manages structures for a graph with numNodes nodes and numEdges
	 * edges
	 * 
	 * @param numNodes
	 * @param numEdges
	 */
	public DijkstraStructs(int numNodes, int numEdges) {
		heap = new Heap(8000);
		marked = new BitSet(numEdges);
		visited = new BitSet(numNodes);
		deque = new IntArrayDeque(1000);
		dists = new int[numNodes];
		prevEdges = new int[numNodes];
		Arrays.fill(dists, Integer.MAX_VALUE);
	}

	/**
	 * Borrows the dist array which is filled with Integer.MAX_VALUE before
	 * calling this method again the dist array have must have been returned
	 * 
	 * @return
	 * @throws IllegalAccessException
	 */
	public int[] borrowDistArray() {
		if (distsBorrowed)
			throw new ConcurrentModificationException("Dist Array borrowed again");
		distsBorrowed = true;
		return dists;
	}

	/**
	 * Returns the dist array to this DijstraStructs instance declaring it ready
	 * for reuse. If alreadyReset is true the array will not be reset to
	 * Integer.MAX_VALUE it's the callers responsibility to not use the array
	 * after it has been returned and to make sure it's really reset to
	 * Integer.MAX_VALUE when alreadyReset is true
	 * 
	 * @param alreadyReset
	 */
	public void returnDistArray(boolean alreadyReset) {
		if (!alreadyReset)
			Arrays.fill(dists, Integer.MAX_VALUE);
		distsBorrowed = false;
	}

	/**
	 * Borrows the heap. Before calling this method again the heap must have
	 * been returned
	 * 
	 * @return
	 * @throws IllegalAccessException
	 */
	public Heap borrowHeap() {
		if (heapBorrowed)
			throw new ConcurrentModificationException("Heap borrowed again");
		heapBorrowed = true;
		return heap;
	}

	/**
	 * Returns the heap to this DijkstraStucts instance declaring it ready for
	 * reuse, the heap will be reset so there is no need to have it reset
	 * before. It's the callers responsibility to not use the heap after it has
	 * been returned.
	 */
	public void returnHeap() {
		heapBorrowed = false;
		heap.resetHeap();
	}

	/**
	 * Borrows the int array used to store predecessor edges this array has no
	 * guaranteed content when newly borrowed (i.e. is not reset)
	 * 
	 * @return
	 * @throws IllegalAccessException
	 */
	public int[] borrowPrevArray() {
		if (prevsBorrowed)
			throw new ConcurrentModificationException("PrevArray borrowed again");
		prevsBorrowed = true;
		return prevEdges;
	}

	/**
	 * Returns the prev array to this DijkstraStucts instance declaring it ready
	 * for reuse. It's the callers responsibility to not use the array after it
	 * has been returned.
	 */
	public void returnPrevArray() {
		prevsBorrowed = false;
	}

	/**
	 * Borrowed the BitSet used for marking edges it's guaranteed to be cleared
	 * when borrwed.
	 * 
	 * @return
	 * @throws IllegalAccessException
	 */
	public BitSet borrowMarkedSet() {
		if (markedBorrowed)
			throw new ConcurrentModificationException("MarkedSet borrowed again");
		markedBorrowed = true;
		return marked;
	}

	/**
	 * Returns the MarkedSet to this DijkstraStructs instance declaring it ready
	 * for reuse. It's the callers responsibility to not use the set after it
	 * has been returned. The BitSet is clear()'ed when returned so there is no
	 * need to clear it before
	 */
	public void returnMarkedSet() {
		markedBorrowed = false;
		marked.clear();
	}

	/**
	 * Borrowed the BitSet used for marking visited nodes it's guaranteed to be
	 * cleared when borrwed.
	 * 
	 * @return
	 * @throws IllegalAccessException
	 */
	public BitSet borrowVisitedSet() {
		if (visitedBorrowed)
			throw new ConcurrentModificationException("VisitedSet borrowed again");
		visitedBorrowed = true;
		return visited;
	}

	/**
	 * Returns the VisitedSet to this DijkstraStructs instance declaring it
	 * ready for reuse. It's the callers responsibility to not use the set after
	 * it has been returned. The BitSet is clear()'ed when returned so there is
	 * no need to clear it before
	 */
	public void returnVisitedSet() {
		visitedBorrowed = false;
		visited.clear();
	}

	/**
	 * Borrows the deque used as stack and/or fifo it's guaranteed to be cleared
	 * 
	 * @return
	 * @throws IllegalAccessException
	 */
	public IntArrayDeque borrowDeque() {
		if (dequeBorrowed)
			throw new ConcurrentModificationException("Deque borrowed again");
		dequeBorrowed = true;
		return deque;
	}

	/**
	 * Returns the deque used as stack and/or fifo to this DijkstraStructs
	 * instance declaring it ready for reuse and clearing it. It's the callers
	 * responsibility to not use the deque after it has been returned.
	 */
	public void returnDeque() {
		dequeBorrowed = false;
		deque.clear();
	}
}
