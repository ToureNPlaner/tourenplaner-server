/**
 * 
 */
package computecore;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * @author Sascha Meusel
 *
 */
public class NearestNeighbourSearch {
	
	private int numberOfNodes = (int) Math.pow(2,20);
	
	private float[] xCoords = new float[numberOfNodes];
	private float[] yCoords = new float[numberOfNodes];
	
	private Integer[] coordsSortedIDs = new Integer[numberOfNodes];
	//private Integer[] yCoordsSortedIDs = new Integer[numberOfNodes];
	
	private int[] kdTree = new int[numberOfNodes];
	
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
	
	
	private void initCoords() {
		Random rand = new Random(10421337L);
		for (int i=0; i < xCoords.length; i++) {
			xCoords[i] = rand.nextFloat() * 100.0F;
			yCoords[i] = rand.nextFloat() * 100.0F;
		}
	}
	
	
	/*
	 * Height with root and without unbalanced leafs: floor(ld (n+1))
	 * Unbalanced leafs: (n+1) - 2^floor(ld (n+1))
	 * 
	 * Height with root and without leafs: 
	 * 		H = floor(ld (n))
	 * 
	 * Leafs: 
	 * 		L = (n+1) - 2^H
	 * 
	 * Max possible number of subtree leafs with subtree height H-1 (height without leafs):
	 * 		S_L = 2^(H-1)
	 * 
	 * Number of subtree nodes without leafs:
	 * 		S = 2^(H-1) - 1
	 * 		S = S_L - 1
	 * 
	 * Nodes left of root: S + (S_L - L >= 0)? L : S_L
	 */
	private void constructKDTree() {
		Comparator<Integer> xComp = new XComparator();
		Comparator<Integer> yComp = new YComparator();
		
		for (int i=0; i < coordsSortedIDs.length; i++) {
			coordsSortedIDs[i] = i;
		}
		
		int currentKDTreeIndex = 0;
		int currentHeightPos = 0;
		
		int height = (int) Math.floor(Math.log(numberOfNodes) / Math.log(2));
		
		int leafs = (numberOfNodes + 1) - (int) Math.pow(2,height);
		
		int currentHeight = height;
		
		int numberOfNodesAtCurrentLevel = 1;
		while (currentHeightPos < height) {
			
			int subtreeLeafs = (int) Math.pow(2,currentHeight - 1);
			int sumOfSubtreeLeafs = 0;
			
			int sortFromIndex = 0;
			int sortToIndex = 0;
			
			for (int i=0; i < numberOfNodesAtCurrentLevel; i++) {
				
				//add leafs of left subtree
				sumOfSubtreeLeafs += subtreeLeafs;
				
				int leafDiff = sumOfSubtreeLeafs - leafs;
				if (leafDiff > subtreeLeafs) {
					leafDiff = subtreeLeafs;
				}
				
				int middlePos = subtreeLeafs - 1 
						+ ((leafDiff > 0)? (subtreeLeafs - leafDiff) : subtreeLeafs);
				
				//add leafs of right subtree
				sumOfSubtreeLeafs += subtreeLeafs;
				leafDiff = sumOfSubtreeLeafs - leafs;
				if (leafDiff > 2*subtreeLeafs) {
					leafDiff = 2*subtreeLeafs;
				}
				
				sortToIndex = sortFromIndex + 1 + 2*subtreeLeafs - 2 
						+ ((leafDiff > 0)? (2*subtreeLeafs - leafDiff) : 2*subtreeLeafs);
				if (sortToIndex > coordsSortedIDs.length) {
					sortToIndex = coordsSortedIDs.length;
				}
				
				if (currentHeightPos % 2 == 0) {
					Arrays.sort(coordsSortedIDs, sortFromIndex, sortToIndex, xComp);
				} else {
					Arrays.sort(coordsSortedIDs, sortFromIndex, sortToIndex, yComp);
				}
				kdTree[currentKDTreeIndex] = coordsSortedIDs[middlePos];
				
				
				
				sortFromIndex = sortToIndex + 1;
				currentKDTreeIndex++;
			}

			currentHeight--;
			currentHeightPos++;
			
			//is equal to numberOfNodesAtCurrentLevel = 2^currentHeightPos
			numberOfNodesAtCurrentLevel = numberOfNodesAtCurrentLevel*2;
		}
		// now currentHeightPos = height, so now the leafs will be inputed
		int pos = 0;
		for (int i=currentKDTreeIndex; i < numberOfNodes; i++) {
			kdTree[currentKDTreeIndex] = coordsSortedIDs[pos];
			pos += 2;
		}
		
		
		
	}
	
	private float squareDistance(int kdTreePos, float x, float y) {
		return (float) (Math.pow(Math.abs(x - xCoords[kdTree[kdTreePos]]), 2)
				+ Math.pow(Math.abs(y - yCoords[kdTree[kdTreePos]]), 2));
	}
	
	public void searchNN(float lat, float lon) {
		float y = lat;
		float x = lon;
		
		int pos = 0;
		int child = 0;
		int depth = 0;
		int currentBestPos = 0;
		float currentBestDistance = 0.0F;
		boolean anotherFlag = false;
		
		//left child
		child = 2*(pos + 1) - 1;
		
		int anotherPos = 0;
		float anotherDistance = 0.0F;
		
		while (child < kdTree.length) {
			if (depth % 2 == 0) {
				if (x <= xCoords[kdTree[pos]]) {
					pos = child;
				} else {
					if (child + 1 < kdTree.length) {
						pos = child + 1;
					} else {
						anotherPos = child;
						anotherDistance = squareDistance(anotherPos, x, y);
						anotherFlag = true;
						break;
					}
				}
			} else {
				if (y <= yCoords[kdTree[pos]]) {
					pos = child;
				} else {
					if (child + 1 < kdTree.length) {
						pos = child + 1;
					} else {
						anotherPos = child;
						anotherDistance = squareDistance(anotherPos, x, y);
						anotherFlag = true;
						break;
					}
				}
			}
			
			child = 2*(pos + 1) - 1;
			depth++;
		}
		currentBestPos = pos;
		currentBestDistance = squareDistance(currentBestPos, x, y);
		
		if (anotherFlag && currentBestDistance > anotherDistance) {
			currentBestPos = anotherPos;
			currentBestDistance = anotherDistance;
		}
		
		while (pos >= 0) {
			pos = (int) (Math.ceil(pos/2) - 1);
			anotherDistance = squareDistance(pos, x, y);
			if (currentBestDistance > anotherDistance) {
				currentBestPos = pos;
				currentBestDistance = anotherDistance;
			}
			
			
			
		}
		
		
		
	}
	
	public static void main(String[] args) {
		NearestNeighbourSearch nns = new NearestNeighbourSearch();
		long time = System.currentTimeMillis();
		System.out.println("start");
		nns.initCoords();
		System.out.println(System.currentTimeMillis()- time);
		time = System.currentTimeMillis();
		nns.constructKDTree();
		System.out.println(System.currentTimeMillis() - time);
	}
	
	
}
