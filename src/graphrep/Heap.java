package graphrep;

import java.util.Arrays;

public class Heap {

	// a trinary heap is supposed to be a little bit faster

	// use one float array for dists and one for node ids

	/**
	 * uses two fields per node: ... id_35, dist[id_35], id=2, [dist[2], ...
	 */
	private int[] heaparr;
	private float[] heaparr_dist;
	private int heaplength;

	// TODO: determine good value
	// with 5000 no growing from stuttgart-> hamburg
	private final int arrayGrowthSum = 10000;

	public Heap() {
		heaparr = new int[10000];
		heaparr_dist = new float[10000];
		heaplength = 0;
	}

	public void insert(int id, float dist) {
		checkHeapArray();
		heaparr[heaplength] = id;
		heaparr_dist[heaplength] = dist;
		heaplength += 1;
		bubbleUp(heaplength - 1);
	}

	public boolean isEmpty() {
		return heaplength <= 0;
	}

	public int peekMinId() {
		return heaparr[0];
	}

	public float peekMinDist() {
		return heaparr_dist[0];
	}

	public void removeMin() {
		heaparr[0] = heaparr[heaplength - 1];
		heaparr_dist[0] = heaparr_dist[heaplength - 1];
		heaplength -= 1;
		siftDown(0);
	}

	private void bubbleUp(int pos) {
		int parent;
		int tempid;
		float tempdist;
		while (true) {
			// TODO: improve
			parent = (pos - 1) / 3;
			if ((parent >= 0)
					&& (GraphRep.gt_Float(heaparr_dist[parent], heaparr_dist[pos]))) {
				tempid = heaparr[parent];
				tempdist = heaparr_dist[parent];
				heaparr[parent] = heaparr[pos];
				heaparr_dist[parent] = heaparr_dist[pos];
				heaparr[pos] = tempid;
				heaparr_dist[pos] = tempdist;
				pos = parent;
			} else {
				break;
			}

		}
	}

	private void siftDown(int pos) {
		int tempid;
		float tempdist;

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

			if (child1 <= ((heaplength) - 1)) {
				minChild = child1;
			}
			if ((child2 <= ((heaplength) - 1))
					&& (GraphRep.lt_Float(heaparr_dist[child2],
							heaparr_dist[minChild]))) {
				minChild = child2;
			}
			if ((child3 <= ((heaplength) - 1))
					&& (GraphRep.lt_Float(heaparr_dist[child3],
							heaparr_dist[minChild]))) {
				minChild = child3;
			}

			if ((minChild != -1)
					&& (GraphRep.lt_Float(heaparr_dist[minChild],
							heaparr_dist[pos]))) {
				tempid = heaparr[pos];
				tempdist = heaparr_dist[pos];
				heaparr[pos] = heaparr[minChild];
				heaparr_dist[pos] = heaparr_dist[minChild];
				heaparr[minChild] = tempid;
				heaparr_dist[minChild] = tempdist;
				pos = minChild;
			} else {
				break;
			}
		}
	}

	private void checkHeapArray() {
		if (((heaplength) + 2) >= heaparr.length) {
			System.err.println("Increase Heap size");
			heaparr = Arrays.copyOf(heaparr, heaparr.length + arrayGrowthSum);
			heaparr_dist = Arrays.copyOf(heaparr_dist, heaparr_dist.length
					+ arrayGrowthSum);
		}
	}
}
