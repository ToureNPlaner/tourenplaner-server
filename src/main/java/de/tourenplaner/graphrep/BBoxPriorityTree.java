/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tourenplaner.graphrep;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.IntSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import de.tourenplaner.algorithms.bbbundle.BoundingBox;

import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author spark
 */
public final class BBoxPriorityTree implements NNSearcher {

    private final int[] xKeys;
    private final int[] yKeys;
    private final int[] prioKeys;
    private final int[] nodesXstruct;    // all nodeIDs sorted according to x-coordinate
    private final int[] offset2Xstruct;   // offsets into nodesXstruct according to different
    // x-coord; offsetXstruct[i] points to first nodeID
    // of i-th distinct x-coord
    private final int[] rangeTreeKeys;   // distinct x-coordinates for range tree
    private final int[] rangeTreeInfs;   // rank of each x-coordinate
    private final RangeTree myXRT;        // range tree for the x-coords
    private final PrioSearchTree[] myPSTs;
    private static final Random generator = new Random();

    /**
     * Creates the priority data structure for the given input data where
     * xKeysIn[i], yKeysIn[i], priKeysIn[i] belong to object i. Note that
     * these arrays are stored directly and must not be changed after constructing
     * the BoundingBoxPriorityTree or it will misreport
     */
    public BBoxPriorityTree(int[] xKeysIn, int[] yKeysIn, int[] prioKeysIn) {
        assert (yKeysIn.length == yKeysIn.length) && (yKeysIn.length == xKeysIn.length) && (yKeysIn.length == prioKeysIn.length);
        xKeys = xKeysIn;
        yKeys = yKeysIn;
        prioKeys = prioKeysIn;
        // initializes data structures with given nodes
        // IDEA:
        // 0. a. retrieve all x-coordinates from nodes; create sorted list of unique x-coords
        //    b. associate with every x-coordinate list (index of offset array) of resp. nodes
        // 1. build range-tree on top of this set of x-coordinates;
        //      key: x-coordinate
        //      inf: index into offset array above
        // 2. with each of the internal nodes there is a x-range associated
        //      create for top x-level PST (on the y-coordinates)
        TreeMap<Integer, IntSet> unixKeys = new TreeMap<Integer, IntSet>();

        // collect distinct x-coordinates
        for (int i = 0; i < xKeys.length; i++) {
            int curKey = xKeys[i];
            if (unixKeys.containsKey(curKey)) // x-coordinate already present
            {
                IntSet tmpSet = unixKeys.get(curKey);
                tmpSet.add(i);
                unixKeys.put(curKey, tmpSet);
                //System.out.print(".");
            } else // new x-coordinate
            {
                IntSet tmpSet = new IntOpenHashSet();
                tmpSet.add(i);
                unixKeys.put(xKeys[i], tmpSet);
            }
        }
        System.out.println("We have " + unixKeys.size() + " unique X-coordinates from " + xKeys.length + " values");
        nodesXstruct = new int[xKeys.length];
        offset2Xstruct = new int[unixKeys.size() + 1];
        int curPos = 0;
        int curKey = unixKeys.firstKey();
        offset2Xstruct[0] = curPos;
        for (int i = 0; i < unixKeys.size(); i++) {
            IntSet tmpSet = unixKeys.get(curKey);
            Iterator<IntCursor> itr = tmpSet.iterator();
            while (itr.hasNext()) {
                nodesXstruct[curPos++] = itr.next().value;
                //if (tmpSet.size()>1)
                //    System.out.println(nodes[nodesXstruct[curPos-1]].getxPos());
            }
            offset2Xstruct[i + 1] = curPos;
            if (i < unixKeys.size() - 1) {
                curKey = unixKeys.higherKey(curKey);
            }
        }
        System.out.println("curPos=" + curPos);
        /*
         for(int i=0; i<unixKeysIn.size(); i++)
         {
         for(int j=offsetXstruct[i]; j< offsetXstruct[i+1]; j++)
         {
         int nodeID=nodesXstruct[j];
         System.out.print("("+nodes[nodeID].getxPos()+","+nodes[nodeID].getyPos()+") ");
         }
         System.out.println();
         }*/

        // set up data for range tree:

        rangeTreeKeys = new int[unixKeys.size()];
        rangeTreeInfs = new int[unixKeys.size()];
        for (int i = 0; i < rangeTreeKeys.length; i++) {
            rangeTreeKeys[i] = xKeys[nodesXstruct[offset2Xstruct[i]]];
            rangeTreeInfs[i] = i;
        }
        myXRT = new RangeTree(rangeTreeKeys, rangeTreeInfs);
        // now construct for the upper inner nodes of RangeTree respective PSTs
        // until 0, 2, 6, 14, 30, ...
        int limitPST = 0;
        int height = 0;
        // TODO: Need a comment here for why we divide by 256
        while (limitPST < rangeTreeKeys.length / 256) {
            limitPST = (limitPST + 1) * 2;
            height++;
        }
        limitPST = limitPST / 2;
        height--;
        System.out.println("Building " + limitPST + " PSTs that is, height=" + height);
        myPSTs = new PrioSearchTree[limitPST];
        long tmpcnt = 0;

        for (int j = 0; j < limitPST; j++) {
            // collect nodeIDsPST to be stored in PST j
            IntArrayList xCoordsToStore = new IntArrayList();
            IntArrayList nodeIDOffsetsToStore = new IntArrayList();
            myXRT.reportSubtree(j, xCoordsToStore, nodeIDOffsetsToStore);

            // returned offsets are into
            int sizePST = 0;
            for (int i = 0; i < nodeIDOffsetsToStore.size(); i++) {
                int inf = nodeIDOffsetsToStore.get(i);
                sizePST += offset2Xstruct[inf + 1] - offset2Xstruct[inf]; // number of nodes
            }

            if (sizePST % 2 == 1) {
                System.out.println("ungerade!");
                sizePST++;
            }
            tmpcnt += sizePST;

            int[] yKeysPST = new int[sizePST];
            int[] priosPST = new int[sizePST];
            int[] nodeIDsPST = new int[sizePST];
            yKeysPST[sizePST - 1] = 0;
            priosPST[sizePST - 1] = Integer.MIN_VALUE;
            nodeIDsPST[sizePST - 1] = Integer.MAX_VALUE;


            int nodeCounter = 0;
            for (int i = 0; i < nodeIDOffsetsToStore.size(); i++) {
                int inf = nodeIDOffsetsToStore.get(i);
                for (int k = offset2Xstruct[inf]; k < offset2Xstruct[inf + 1]; k++) {
                    nodeIDsPST[nodeCounter] = nodesXstruct[k];
                    yKeysPST[nodeCounter] = yKeys[nodeIDsPST[nodeCounter]];
                    priosPST[nodeCounter] = prioKeys[nodeIDsPST[nodeCounter]];
                    nodeCounter++;
                }
            }
            System.out.println(nodeCounter + " and " + sizePST);
            // assert(nodeCounter==sizePST);
            for (int i = 0; i < 5; i++) {
                System.out.println(yKeysPST[i] + " " + priosPST[i] + " " + nodeIDsPST[i]);
            }

            mySort(0, sizePST - 1, yKeysPST, priosPST, nodeIDsPST);

            for (int i = 0; i < 5; i++) {
                System.out.println(yKeysPST[i] + " " + priosPST[i] + " " + nodeIDsPST[i]);
            }
            myPSTs[j] = new PrioSearchTree(yKeysPST, priosPST, nodeIDsPST);
            // construct PST corresponding to internal node j (containing all nodes in subtree) */
            System.out.println("Constructing PST with " + sizePST + " elements");

        }
        System.out.println("Added " + tmpcnt + " things to PSTs");

    }

    /**
     * Finds the closest nodes for the given coordinates
     * NOTE this only really fullfills the NNSearcher
     * interface when the BoundingBoxPriorityTree
     * was built over the lat, lon geocoordinates
     *
     * @param lat
     * @param lon
     * @return
     */
    @Override
    public int getIDForCoordinates(int lat, int lon) {
        // TODO fix behaviour at merdians/poles
        return getNearestId(lat, lon, 0);
    }

    /**
     * Get the nearest id for the given x, y coordinates with
     * priority at least P
     */
    public int getNearestId(int x, int y, int P) {

        BoundingBox bbox = new BoundingBox();
        IntArrayList candValues = new IntArrayList();

        bbox.width = bbox.height = 32;
        while (candValues.size() == 0) {
            bbox.x = x - bbox.width / 2;
            bbox.y = y - bbox.height / 2;
            candValues = getNodeSelection(bbox, P);
            bbox.width *= 2;
            bbox.height *= 2;
        }

        int bestNode = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < candValues.size(); i++) {
            int id = candValues.get(i);
            double deltaX = xKeys[id] - x;
            double deltaY = yKeys[id] - y;
            double dist = deltaX * deltaX + deltaY * deltaY;
            if (dist < bestDist) {
                bestDist = dist;
                bestNode = id;
            }
        }
        return bestNode;
    }

    /**
     * Get all nodes contained within the given rectangle with priority at least priority
     */
    public IntArrayList getNodeSelection(BoundingBox bbox, int priority) {
        // returns indices of nodes in NodeArray falling into rectangle and with
        // high enough priority

        // query works as follows:
        // 1. query bbox tree with x-bbox, this yields a set of nodes and a set of
        //      subtree heads
        // 2. inspect all nodes individually
        // 3. for each subtree head: if no PST exists for it: replace it by parent subtree head
        // 4. remove subtree heads whose ancestor is also in the set of subtree heads
        // 5. query all remaining subtree heads -> yields a set of nodes (which need to be checked
        //      for feasiblity, though
        //System.out.println("view " + bbox);
        IntArrayList selectedNodeIDs = new IntArrayList();
        int left = (int) bbox.x, right = (int) (bbox.x + bbox.width);
        int lower = (int) bbox.y, upper = (int) (bbox.y + bbox.height);

//        System.out.println("PST query with "+left+"-"+right+" and "+lower+"-"+upper+"----- "+priority);
        IntArrayList resPST = new IntArrayList();
        IntArrayList resKeys = new IntArrayList();
        IntArrayList resInfs = new IntArrayList();

        myXRT.batchQuery(left, right, 0, resPST, resKeys, resInfs);

        // first 
        for (int i = 0; i < resInfs.size(); i++) {
            for (int j = offset2Xstruct[resInfs.get(i)]; j < offset2Xstruct[resInfs.get(i) + 1]; j++) {
                int nd = nodesXstruct[j];
                assert (xKeys[nd] >= left);
                assert (xKeys[nd] <= right);
                if ((yKeys[nd] >= lower) && (yKeys[nd] <= upper) && (prioKeys[nd] >= priority)) {
                    selectedNodeIDs.add(nd);
                }
            }
        }
//        System.out.println("Selected " + selectedNodeIDs.size() + " nodes from " + resInfs.size() + " from X-structure");
        for (int i = 0; i < resPST.size(); i++) {
            IntArrayList dataKeys = new IntArrayList();
            IntArrayList dataPrios = new IntArrayList();
            IntArrayList dataInfs = new IntArrayList();

            int tmp_cnt = 0;
            if (resPST.get(i) < myPSTs.length) {
                //System.out.println("PSTQuery mit: "+priority+" on PST number "+resPST.get(i));
                myPSTs[resPST.get(i)].queryPST(lower, upper, priority, 0, dataKeys, dataPrios, dataInfs);
                for (int j = 0; j < dataInfs.size(); j++) {
                    int jj = dataInfs.get(j);
                    assert (yKeys[jj] >= lower);
                    assert (yKeys[jj] <= upper);
                    assert (prioKeys[jj] >= priority);


                    assert (xKeys[jj] >= left);
                    assert (xKeys[jj] <= right);
                    selectedNodeIDs.add(jj);
                    tmp_cnt++;
                }
                //System.out.println("Added " + tmp_cnt + " from PST " + resPST.get(i));
            } else // otherwise simply scan the subtree
            {
                IntArrayList xCoordsToDrop = new IntArrayList();
                IntArrayList nodeIDOffsetsToCheck = new IntArrayList();
                myXRT.reportSubtree(resPST.get(i), xCoordsToDrop, nodeIDOffsetsToCheck);

                // returned offsets are into
                for (int kk = 0; kk < nodeIDOffsetsToCheck.size(); kk++) {
                    int inf = nodeIDOffsetsToCheck.get(kk);
                    for (int jj = offset2Xstruct[inf]; jj < offset2Xstruct[inf + 1]; jj++) {
                        int nd = nodesXstruct[jj];
                        assert (xKeys[nd] >= left);
                        assert (xKeys[nd] <= right);
                        if ((yKeys[nd] >= lower) && (yKeys[nd] <= upper) && (prioKeys[nd] >= priority)) {

                            selectedNodeIDs.add(nd);
                        }

                    }
                }

            }
        }

        //System.out.println("Added in total " + selectedNodeIDs.size() + " elements to result");
        return selectedNodeIDs;
    }

    private void swapElements(int a, int b, int[] data1, int[] data2, int[] data3) {
        int tmp1 = data1[a], tmp2 = data2[a], tmp3 = data3[a];
        data1[a] = data1[b];
        data2[a] = data2[b];
        data3[a] = data3[b];

        data1[b] = tmp1;
        data2[b] = tmp2;
        data3[b] = tmp3;
    }

    private void mySort(int start, int end, int[] data1, int[] data2, int[] data3) {
        // quicksort between start and end (including)
        // sort according to (data1,data2)

        if (start >= end) {
            return;
        }
        int pivot = start + generator.nextInt(end - start);
        //System.out.println("QS " + start + " " + end+" "+pivot+" "+data1[end]+ " "+data1[pivot]);

        swapElements(pivot, end, data1, data2, data3);
        //System.out.println("QS2 " + start + " " + end+" "+pivot+" "+data1[end]+ " "+data1[pivot]);

        int storage = start;
        for (int j = start; j < end; j++) {
            // compare elements with pivot (at end)
            if ((data1[j] < data1[end]) || ((data1[j] == data1[end]) && (data2[j] < data2[end]))) {
                swapElements(j, storage, data1, data2, data3);
                storage++;
            }
        }
        // System.out.println("storage: "+storage);
        swapElements(storage, end, data1, data2, data3);
        mySort(start, storage - 1, data1, data2, data3);
        mySort(storage + 1, end, data1, data2, data3);
    }
}
