package algorithms;

import java.util.Arrays;

public class Heap {

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

	public Heap(int initialSize) {
		heaparr = new int[initialSize];
		heaplength = 0;
	}

	public final void insert(int id, int dist) {
		checkHeapArray();
		heaparr[heaplength * 2] = id;
		heaparr[(heaplength * 2) + 1] = dist;
		heaplength += 1;
		bubbleUp((heaplength - 1) * 2);
	}

	public final boolean isEmpty() {
		return heaplength <= 0;
	}

	public final int peekMinId() {
		return heaparr[0];
	}

	public final int peekMinDist() {
		return heaparr[1];
	}

	public final void removeMin() {
		heaplength -= 1;
		heaparr[0] = heaparr[heaplength * 2];
		heaparr[1] = heaparr[(heaplength * 2) + 1];
		siftDown(0);
	}

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

	public final void resetHeap() {
		heaplength = 0;
	}

	private final void checkHeapArray() {
		if (((heaplength * 2) + 1) >= heaparr.length) {
			System.out.println("Increased Heap size from " + heaparr.length
					+ " to " + (heaparr.length + arrayGrowthSum));
			heaparr = Arrays.copyOf(heaparr, heaparr.length + arrayGrowthSum);
		}
	}
}
