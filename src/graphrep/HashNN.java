/**
 * 
 */
package graphrep;

import java.util.HashMap;

import com.carrotsearch.hppc.IntArrayList;

/**
 * @author nino
 * 
 */
public class HashNN implements NNSearcher {

	GraphRep graphRep;
	HashMap<Long, Object> hashMap;
	NNSearcher dumpNN;
	private static final int maxHopLimit = 10;

	public HashNN(GraphRep graphRep) {
		this.graphRep = graphRep;
		dumpNN = new DumbNN(graphRep);
		hashMap = new HashMap<Long, Object>();
		for (int i = 0; i < graphRep.getNodeCount(); i++) {
			int tempLat = (int) (graphRep.getNodeLat(i) * 1000);
			int tempLon = (int) (graphRep.getNodeLon(i) * 1000);

			long key = tempLat << 32 | tempLon;
			IntArrayList tempValues = (IntArrayList) hashMap.get(key);
			if (tempValues == null) {
				tempValues = new IntArrayList(10);
				tempValues.add(i);
				hashMap.put(key, tempValues);
			} else {
				tempValues.add(i);
			}
		}
		for (Long key : hashMap.keySet()) {
			int[] arr = ((IntArrayList) hashMap.get(key)).toArray();
			hashMap.put(key, arr);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see graphrep.NNSearcher#getIDForCoordinates(double, double)
	 */
	@Override
	public int getIDForCoordinates(double lat, double lon) {
		int keyLat = (int) (lat * 1000);
		int keyLon = (int) (lon * 1000);
		int pos = -1;
		long key;
		double dist = Long.MAX_VALUE;
		double tempDist = Long.MAX_VALUE;
		boolean found = false;
		int hops = 1;
		for (int i = 0; i <= hops; i++) {
			for (int j = -i; j <= i; j++) {
				for (int k = -i; k <= i; k++) {
					key = keyLat + j << 32 | keyLon + k;
					if (hashMap.containsKey(key)) {
						int[] ringArr = (int[]) hashMap.get(key);
						for (int nodeID : ringArr) {
							tempDist = (graphRep.getNodeLat(nodeID) - lat)
									* (graphRep.getNodeLat(nodeID) - lat)
									+ (graphRep.getNodeLon(nodeID) - lon)
									* (graphRep.getNodeLon(nodeID) - lon);
							if (tempDist < dist) {
								dist = tempDist;
								pos = nodeID;
							}

						}
						found = true;
					}
				}
				if (found != true) {
					hops++;
				}

			}
		}

		if (pos == -1) {
			pos = dumpNN.getIDForCoordinates(lat, lon);
		}
		return pos;
	}
}
