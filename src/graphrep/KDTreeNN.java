/**
 * 
 */
package graphrep;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * @author Sascha Meusel
 * 
 */
public class KDTreeNN implements NNSearcher {
	private static final long serialVersionUID = 1L;

	private int numberOfNodes = 0;

	private float[] xCoords = null;
	private float[] yCoords = null;

	private int[] kdTree = null;

	private class XComparator implements Comparator<Integer> {
		@Override
		public int compare(Integer left, Integer right) {

			return Float.compare(xCoords[left], xCoords[right]);
		}
	}

	private class YComparator implements Comparator<Integer> {
		@Override
		public int compare(Integer left, Integer right) {
			return Float.compare(yCoords[left], yCoords[right]);
		}
	}

	/**
	 * Test constructor, generates pseudo random float coordinates, depends on
	 * how much nodes you want. With the coordinates a test k-d-tree will be
	 * constructed, so you can try the nearest neighbour search on it.
	 * 
	 * @param numberOfNodes
	 *            number of nodes the test graph should have
	 */
	public KDTreeNN(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
		System.out.println("Start k-d-Tree test initialization");
		long time = System.currentTimeMillis();
		this.initCoords();
		System.out.println("Initialization time: "
				+ (System.currentTimeMillis() - time) + " ms");
		System.out.println("Start k-d-Tree test construction");
		time = System.currentTimeMillis();
		this.constructKDTree();
		System.out.println("Construction time: "
				+ (System.currentTimeMillis() - time) + " ms");
	}

	/**
	 * 
	 */
	public KDTreeNN(float[] latCoords, float[] lonCoords) {
		this.numberOfNodes = latCoords.length;
		xCoords = lonCoords;
		yCoords = latCoords;
		System.out.println("Start k-d-Tree construction");
		long time = System.currentTimeMillis();
		this.constructKDTree();
		System.out.println("Construction time: "
				+ (System.currentTimeMillis() - time) + " ms");
	}

	private void initCoords() {
		xCoords = new float[numberOfNodes];
		yCoords = new float[numberOfNodes];
		Random rand = new Random(10421337L);
		for (int i = 0; i < xCoords.length; i++) {
			xCoords[i] = rand.nextFloat() * 100.0F;
			yCoords[i] = rand.nextFloat() * 100.0F;
		}
	}

	/*
	 * Height with root and without unbalanced leafs: floor(ld (n+1)) Unbalanced
	 * leafs: (n+1) - 2^floor(ld (n+1))
	 * 
	 * Height with root and without leafs: H = floor(ld (n))
	 * 
	 * Leafs: L = (n+1) - 2^H
	 * 
	 * Max possible number of subtree leafs with subtree height H-1 (height
	 * without leafs): S_L = 2^(H-1)
	 * 
	 * Number of subtree nodes without leafs: S = 2^(H-1) - 1 S = S_L - 1
	 * 
	 * Nodes left of root: S + (S_L - L >= 0)? L : S_L
	 */
	private void constructKDTree() {
		this.kdTree = new int[numberOfNodes];
		Integer[] coordsSortedIDs = new Integer[numberOfNodes];

		Comparator<Integer> xComp = new XComparator();
		Comparator<Integer> yComp = new YComparator();

		for (int i = 0; i < coordsSortedIDs.length; i++) {
			coordsSortedIDs[i] = i;
		}

		int currentKDTreeIndex = 0;
		int currentHeightPos = 0;

		// see comment of "Integer.numberOfLeadingZeros()"
		// or http://en.wikipedia.org/Binary_logarithm
		int height = 31 - Integer.numberOfLeadingZeros(numberOfNodes);
		// int height = (int) Math.floor(Math.log(numberOfNodes) / Math.log(2));

		int leafs = (numberOfNodes + 1) - (1 << height);

		int currentHeight = height;

		int numberOfNodesAtCurrentLevel = 1;
		while (currentHeightPos < height) {

			int subtreeLeafs = (1 << (currentHeight - 1));
			int sumOfSubtreeLeafs = 0;

			int sortFromIndex = 0;
			int sortToIndex = 0;

			for (int i = 0; i < numberOfNodesAtCurrentLevel; i++) {

				// add leafs of left subtree
				sumOfSubtreeLeafs += subtreeLeafs;

				int leafDiff = sumOfSubtreeLeafs - leafs;
				if (leafDiff > subtreeLeafs) {
					leafDiff = subtreeLeafs;
				}

				int middlePos = subtreeLeafs
						- 1
						+ ((leafDiff > 0) ? (subtreeLeafs - leafDiff)
								: subtreeLeafs);

				// add leafs of right subtree
				sumOfSubtreeLeafs += subtreeLeafs;
				leafDiff = sumOfSubtreeLeafs - leafs;
				if (leafDiff > 2 * subtreeLeafs) {
					leafDiff = 2 * subtreeLeafs;
				}

				sortToIndex = sortFromIndex
						+ 1
						+ 2
						* subtreeLeafs
						- 2
						+ ((leafDiff > 0) ? (2 * subtreeLeafs - leafDiff)
								: 2 * subtreeLeafs);
				if (sortToIndex > coordsSortedIDs.length) {
					sortToIndex = coordsSortedIDs.length;
				}

				if (currentHeightPos % 2 == 0) {
					Arrays.sort(coordsSortedIDs, sortFromIndex, sortToIndex,
							xComp);
				} else {
					Arrays.sort(coordsSortedIDs, sortFromIndex, sortToIndex,
							yComp);
				}
				kdTree[currentKDTreeIndex] = coordsSortedIDs[sortFromIndex
						+ middlePos];

				// System.out.println("mid" + middlePos);

				sortFromIndex = sortToIndex + 1;
				currentKDTreeIndex++;
			}

			currentHeight--;
			currentHeightPos++;

			// is equal to numberOfNodesAtCurrentLevel = 2^currentHeightPos
			numberOfNodesAtCurrentLevel = numberOfNodesAtCurrentLevel * 2;
		}
		// now currentHeightPos = height, so now the leafs will be inputed
		int pos = 0;
		for (int i = currentKDTreeIndex; i < numberOfNodes; i++) {
			kdTree[currentKDTreeIndex] = coordsSortedIDs[pos];
			pos += 2;
		}

	}

	private float squareDistance(int kdTreePos, float x, float y) {
		return (x - xCoords[kdTree[kdTreePos]])
				* (x - xCoords[kdTree[kdTreePos]])
				+ (y - yCoords[kdTree[kdTreePos]])
				* (y - yCoords[kdTree[kdTreePos]]);
	}

	public int searchNN(float lat, float lon) {

		if (kdTree.length == 0) {
			return -1;
		}
		if (kdTree.length == 1) {
			return kdTree[0];
		}

		float y = lat;
		float x = lon;

		int pos = 0;
		int child = 0;
		int depth = 0;
		int currentBestPos = 0;
		float currentBestDistance = squareDistance(0, x, y);
		boolean anotherFlag = false;

		int anotherPos = 0;
		float anotherDistance = 0.0F;
		boolean moveDownward = true;
		// BitSet otherChildVisited = new BitSet(this.kdTreeHeight);
		// We have at most MAX_INT elements so fits into int
		int otherChildVisited = 0;
		boolean lastChildIsRightChild = false;

		while (pos > 0 || moveDownward) {

			if (moveDownward) {
				// otherChildVisited.set(depth);
				otherChildVisited |= 1 << depth;
				anotherFlag = false;
				// left child
				child = 2 * (pos + 1) - 1;

				while (child < kdTree.length) {
					// otherChildVisited.clear(depth);
					otherChildVisited ^= 1 << depth;
					boolean coordLesserEqual;

					if (depth % 2 == 0) {
						coordLesserEqual = (x <= xCoords[kdTree[pos]]);
					} else {
						coordLesserEqual = (y <= yCoords[kdTree[pos]]);
					}

					if (coordLesserEqual) {
						// go left
						pos = child;
						// currentIsLeftChild = true;
						depth++;
					} else {
						if (child + 1 < kdTree.length) {
							// go right
							pos = child + 1;
							// currentIsLeftChild = false;
							depth++;
						} else {
							// want go right, but there is no right child
							// so compare left child with current pos
							anotherPos = child;
							anotherDistance = squareDistance(anotherPos, x, y);
							anotherFlag = true;
							break;
						}
					}

					child = 2 * (pos + 1) - 1;
				}

				float tmpDistance = squareDistance(pos, x, y);
				if (tmpDistance < currentBestDistance) {
					currentBestPos = pos;
					currentBestDistance = tmpDistance;
				}

				// comparison with left child, if pos is no leaf
				if (anotherFlag && currentBestDistance > anotherDistance) {
					currentBestPos = anotherPos;
					currentBestDistance = anotherDistance;
				}

			}
			moveDownward = false;
			// end of downward move

			lastChildIsRightChild = (pos % 2 == 0);
			// get parent
			pos = ((pos + 1) / 2) - 1;
			// pos = (int) (Math.ceil(pos/2) - 1);
			depth--;

			anotherDistance = squareDistance(pos, x, y);
			if (currentBestDistance > anotherDistance) {
				currentBestPos = pos;
				currentBestDistance = anotherDistance;
			}

			float axisAlignedSquareDistance = 0;
			if (depth % 2 == 0) {
				axisAlignedSquareDistance = (x - xCoords[kdTree[pos]])
						* (x - xCoords[kdTree[pos]]);
			} else {
				axisAlignedSquareDistance = (y - yCoords[kdTree[pos]])
						* (y - yCoords[kdTree[pos]]);
			}
			if (axisAlignedSquareDistance <= currentBestDistance) {
				if (!lastChildIsRightChild) {
					// go right
					child = 2 * (pos + 1);
					// otherChildVisited.bit[depth] = 0
					if (child < kdTree.length
							&& (otherChildVisited & (1 << depth)) == 0) {
						// otherChildVisited.set(depth);
						otherChildVisited |= 1 << depth;
						pos = child;
						moveDownward = true;
						depth++;
					}
				} else {
					// go left
					child = 2 * (pos + 1) - 1;
					// otherChildVisited.bit[depth] = 0
					if (child < kdTree.length
							&& (otherChildVisited & (1 << depth)) == 0) {
						// otherChildVisited.set(depth);
						otherChildVisited |= 1 << depth;
						pos = child;
						moveDownward = true;
						depth++;
					}
				}
			}

		}

		return kdTree[currentBestPos];

	}

	public void putKDTree() {
		for (int i = 0; i < kdTree.length; i++) {
			System.out.println(xCoords[kdTree[i]] + "," + yCoords[kdTree[i]]);
		}
		System.out.println("--");
		for (int i = 0; i < kdTree.length; i++) {
			System.out.println(xCoords[i] + "," + yCoords[i]);
		}
	}

	// public static void main(String[] args) {
	// int numberOfNodes = (1 << 20);
	// if (args != null && args.length >= 1) {
	// try {
	// numberOfNodes = Integer.parseInt(args[0]);
	// } catch (NumberFormatException e) {
	// numberOfNodes = (1 << 20);
	// }
	// }
	// System.out.println(numberOfNodes);
	// KDTreeNN nns = new KDTreeNN(numberOfNodes);
	// long time = System.nanoTime();
	// float y = 42.1337F;
	// float x = 3.1415F;
	// int id = nns.searchNN(y, x);
	// System.out.println("Time KD: " + (System.nanoTime() - time));
	// System.out.println("ID: " + id);
	// System.out
	// .println("Dist: "
	// + ((y - nns.yCoords[id]) * (y - nns.yCoords[id]) + (x - nns.xCoords[id])
	// * (x - nns.xCoords[id])));
	// System.out.println("x: " + nns.xCoords[id]);
	// System.out.println("y: " + nns.yCoords[id]);
	//
	// time = System.nanoTime();
	// id = nns.dumbSearchNN(y, x);
	// System.out.println("Time Dumb: " + (System.nanoTime() - time));
	// System.out.println("ID: " + id);
	// System.out
	// .println("Dist: "
	// + ((y - nns.yCoords[id]) * (y - nns.yCoords[id]) + (x - nns.xCoords[id])
	// * (x - nns.xCoords[id])));
	// System.out.println("x: " + nns.xCoords[id]);
	// System.out.println("y: " + nns.yCoords[id]);
	// // dumb
	// // nns.putKDTree();
	// }

	@Override
	public int getIDForCoordinates(float lat, float lon) {
		// TODO Auto-generated method stub
		return 0;
	}

}
