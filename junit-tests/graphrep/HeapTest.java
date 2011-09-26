package graphrep;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HeapTest {

	@Test
	public final void testInsert() {
		Heap heap = new Heap();

		heap.insert(23456, 65432);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);

		assertEquals(12345, heap.peekMinId());
		assertEquals(54321, heap.peekMinDist());
	}

	@Test
	public final void testIsEmpty() {
		Heap heap = new Heap();
		assertEquals(true, heap.isEmpty());
		heap.insert(12345, 54321);
		assertEquals(false, heap.isEmpty());
		heap.removeMin();
		assertEquals(true, heap.isEmpty());
	}

	@Test
	public final void testPeekMinId() {
		Heap heap = new Heap();

		heap.insert(23456, 65432);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);

		assertEquals(12345, heap.peekMinId());

	}

	@Test
	public final void testPeekMinDist() {
		Heap heap = new Heap();

		heap.insert(23456, 65432);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);

		assertEquals(54321, heap.peekMinDist());
	}

	@Test
	public final void testRemoveMin() {
		Heap heap = new Heap();

		heap.insert(23456, 65432);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);

		heap.removeMin();
		assertEquals(23456, heap.peekMinId());
		assertEquals(65432, heap.peekMinDist());

	}

	@Test
	public final void testResetHeap() {
		Heap heap = new Heap();

		heap.insert(23456, 65432);
		heap.insert(12345, 54321);
		heap.insert(34567, 76543);

		heap.resetHeap();

		assertEquals(true, heap.isEmpty());
	}

}
