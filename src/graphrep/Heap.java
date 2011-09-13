package graphrep;

import java.util.Arrays;

public class Heap {

	// make this consistent:
	// 1) the heaplength is the number of elements in the
	// heap, not the real number of ints in the array
	// 2) the pos parameter is the real position in the array

	// trinary is supposed to be a little bit faster

	/**
	 * uses two fields per node: ... id_35, dist[id_35], id=2, [dist[2], ...
	 */
	private int[] heaparr;
	private int heaplength;

	// TODO: determine good value
	private final int arrayGrowthSum = 20000;

	public Heap(int src, int dist) {
		heaparr = new int[20000];

		// source node
		heaparr[0] = src;
		heaparr[1] = dist;
		heaplength = 1;
	}

	private void heapify(int pos) {

	}

	public void insert(int id, int dist) {
		checkHeapArray();
		heaplength += 1;
		heaparr[heaplength * 2] = id;
		heaparr[heaplength * 2] = dist;
		bubbleUp(heaplength * 2);
	}

	public int peekMinId() {
		return heaparr[0];
	}

	public int peekMinDist() {
		return heaparr[1];
	}

	public void removeMin() {
		heaparr[0] = heaparr[heaplength * 2];
		heaparr[1] = heaparr[heaplength * 2 + 1];
		heaplength -= 1;
		siftDown(0);
	}

	private void bubbleUp(int pos) {
		int parent;
		int tempid;
		int tempdist;
		for (;;) {
			// TODO: improve
			parent = (((pos / 2) - 1) / 3) * 2;
			if (parent >= 0 && heaparr[parent + 1] > heaparr[pos + 1]) {
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

	private void siftDown(int pos) {
		int tempid;
		int tempdist;

		int child1;
		int child2;
		int child3;
		int minChild;

		for (;;) {
			// TODO: improve
			child1 = ((pos / 2) * 3 + 1) * 2;
			child2 = child1 + 2;
			child3 = child2 + 2;

			minChild = -1;
			if (child1 < heaplength) {
				minChild = child1;
			}
			if (child2 < heaplength && heaparr[child2] < minChild) {
				minChild = child2;
			}
			if (child3 < heaplength && heaparr[child3] < minChild) {
				minChild = child3;
			}

			if (minChild != -1 && heaparr[minChild + 1] < heaparr[pos + 1]) {
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

	private void checkHeapArray() {
		if (heaplength * 2 + 1 >= heaparr.length) {
			heaparr = Arrays.copyOf(heaparr, heaparr.length + arrayGrowthSum);
		}
	}
}
