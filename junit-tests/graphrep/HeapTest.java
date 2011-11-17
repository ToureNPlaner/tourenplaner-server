package graphrep;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import algorithms.Heap;

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
