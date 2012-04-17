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

package de.tourenplaner.graphrep;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tourenplaner.algorithms.Heap;

public class HeapTest {

	@Test
	public final void testInsert() {
		Heap heap = new Heap();

		heap.insert(23456, 65432);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);
		heap.insert(97221, 98742);
		heap.insert(3543, 72987);

		assertEquals(12345, heap.peekMinId());
		assertEquals(54321, heap.peekMinDist());
	}

	@Test
	public final void testIsEmpty() {
		Heap heap = new Heap();
		assertEquals(true, heap.isEmpty());
		heap.insert(12345, 54321);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);
		heap.insert(97221, 98742);
		heap.insert(3543, 72987);

		assertEquals(false, heap.isEmpty());
		heap.removeMin();
		assertEquals(false, heap.isEmpty());
		heap.removeMin();
		heap.removeMin();
		heap.removeMin();
		heap.removeMin();
		assertEquals(true, heap.isEmpty());

	}

	@Test
	public final void testPeekMinId() {
		Heap heap = new Heap();

		heap.insert(23456, 65432);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);
		heap.insert(97221, 98742);
		heap.insert(3543, 72987);

		assertEquals(12345, heap.peekMinId());
		heap.removeMin();
		heap.removeMin();
		assertEquals(3543, heap.peekMinId());

	}

	@Test
	public final void testPeekMinDist() {
		Heap heap = new Heap();

		heap.insert(23456, 65432);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);
		heap.insert(97221, 98742);
		heap.insert(3543, 72987);

		assertEquals(54321, heap.peekMinDist());
		heap.removeMin();
		heap.removeMin();
		assertEquals(72987, heap.peekMinDist());
	}

	@Test
	public final void testRemoveMin() {
		Heap heap = new Heap();

		heap.insert(23456, 65432);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);
		heap.insert(97221, 98742);
		heap.insert(3543, 72987);

		heap.removeMin();
		assertEquals(23456, heap.peekMinId());
		assertEquals(65432, heap.peekMinDist());
		heap.removeMin();
		heap.removeMin();
		assertEquals(34567, heap.peekMinId());
		assertEquals(76543, heap.peekMinDist());

	}

	@Test
	public final void testResetHeap() {
		Heap heap = new Heap();
		assertEquals(true, heap.isEmpty());
		heap.resetHeap();
		assertEquals(true, heap.isEmpty());
		heap.insert(23456, 65432);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);
		heap.insert(97221, 98742);
		heap.insert(3543, 72987);

		heap.resetHeap();

		assertEquals(true, heap.isEmpty());
	}

}
