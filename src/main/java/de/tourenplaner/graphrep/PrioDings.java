/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tourenplaner.graphrep;

import java.awt.*;
import java.util.*;

/**
 *
 * @author spark
 */
public final class PrioDings {

    private final int[] nodesXstruct;    // all nodeIDs sorted according to x-coordinate
    private final int[] offset2Xstruct;   // offsets into nodesXstruct according to different
    // x-coord; offsetXstruct[i] points to first nodeID
    // of i-th distinct x-coord
    private final GraphRep graph;
    private final int[] rangeTreeKeys;   // distinct x-coordinates for range tree
    private final int[] rangeTreeInfs;   // rank of each x-coordinate
    private final RangeTree myXRT;        // range tree for the x-coords
    private final PrioSearchTree[] myPSTs;
    private static final Random generator = new Random();


	/**
	 * Get the nearest node for the given x, y coordinates with
	 * priority at least P
	 */
    public int getNextNode(int x, int y, int P) {
   
        Rectangle.Double range=new Rectangle.Double();
        ArrayList<Integer> candNodes=new ArrayList<Integer>();
 
        range.width = range.height = 32;
        while (candNodes.size()==0)
        { 
            range.x = x - range.width / 2;
            range.y = y - range.height / 2;
            candNodes = getNodeSelection(range, P);
            range.width*=2;
            range.height*=2;
        }
        
        int bestNode=0;
        double bestDist= Double.MAX_VALUE;
        for (int i = 0; i < candNodes.size(); i++)
        {
            int nodeId=candNodes.get(i);
            double deltaX=graph.getXPos(nodeId)-x;
            double deltaY=graph.getYPos(nodeId)-y;
            double dist=deltaX*deltaX+deltaY*deltaY;
            if (dist<bestDist)
            {
                bestDist=dist;
                bestNode=nodeId;
            }
        }
        return bestNode;
    }

	/**
	 *  Get all nodes contained within the given rectangle with priority at least priority
	 */
    public ArrayList<Integer> getNodeSelection(Rectangle.Double range, int priority) {
        // returns indices of nodes in NodeArray falling into rectangle and with
        // high enough priority

        // query works as follows:
        // 1. query range tree with x-range, this yields a set of nodes and a set of 
        //      subtree heads
        // 2. inspect all nodes individually
        // 3. for each subtree head: if no PST exists for it: replace it by parent subtree head
        // 4. remove subtree heads whose ancestor is also in the set of subtree heads
        // 5. query all remaining subtree heads -> yields a set of nodes (which need to be checked
        //      for feasiblity, though
        //System.out.println("view " + range);
        ArrayList<Integer> selectedNodeIDs = new ArrayList<Integer>();
        int left = (int) range.x, right = (int) (range.x + range.width);
        int lower = (int) range.y, upper = (int) (range.y + range.height);

//        System.out.println("PST query with "+left+"-"+right+" and "+lower+"-"+upper+"----- "+priority);
        ArrayList<Integer> resPST = new ArrayList<Integer>();
        ArrayList<Integer> resKeys = new ArrayList<Integer>();
        ArrayList<Integer> resInfs = new ArrayList<Integer>();

        myXRT.batchQuery(left, right, 0, resPST, resKeys, resInfs);

        // first 
        for (int i = 0; i < resInfs.size(); i++) {
            for (int j = offset2Xstruct[resInfs.get(i)]; j < offset2Xstruct[resInfs.get(i) + 1]; j++) {
                int nd = nodesXstruct[j];
                assert (graph.getXPos(nd) >= left);
                assert (graph.getXPos(nd) <= right);
                if ((graph.getYPos(nd) >= lower) && (graph.getYPos(nd) <= upper) && (graph.getRank(nd) >= priority)) {
                    selectedNodeIDs.add(nd);
                }
            }
        }
//        System.out.println("Selected " + selectedNodeIDs.size() + " nodes from " + resInfs.size() + " from X-structure");
        for (int i = 0; i < resPST.size(); i++) {
            ArrayList<Integer> dataKeys = new ArrayList<Integer>();
            ArrayList<Integer> dataPrios = new ArrayList<Integer>();
            ArrayList<Integer> dataInfs = new ArrayList<Integer>();

            int tmp_cnt = 0;
            if (resPST.get(i) < myPSTs.length) {
                //System.out.println("PSTQuery mit: "+priority+" on PST number "+resPST.get(i));
                myPSTs[resPST.get(i)].queryPST(lower, upper, priority, 0, dataKeys, dataPrios, dataInfs);
                for (int j = 0; j < dataInfs.size(); j++) {
                    int jj=dataInfs.get(j);
                    assert (graph.getYPos(jj) >= lower);
                    assert (graph.getYPos(jj) <= upper);
                    assert (graph.getRank(jj) >= priority);
                    
                    
                    assert (graph.getXPos(jj) >= left);
                    assert (graph.getXPos(jj) <= right);

                    selectedNodeIDs.add(jj);
                    tmp_cnt++;
                }
                //System.out.println("Added " + tmp_cnt + " from PST " + resPST.get(i));
            } else // otherwise simply scan the subtree
            {
                ArrayList<Integer> xCoordsToDrop = new ArrayList<Integer>();
                ArrayList<Integer> nodeIDOffsetsToCheck = new ArrayList<Integer>();
                myXRT.reportSubtree(resPST.get(i), xCoordsToDrop, nodeIDOffsetsToCheck);

                // returned offsets are into
                for (int kk = 0; kk < nodeIDOffsetsToCheck.size(); kk++) {
                    int inf = nodeIDOffsetsToCheck.get(kk);
                    for (int jj = offset2Xstruct[inf]; jj < offset2Xstruct[inf + 1]; jj++) {
                        int nd = nodesXstruct[jj];
                        assert (graph.getXPos(nd) >= left);
                        assert (graph.getXPos(nd) <= right);
                        if ((graph.getYPos(nd) >= lower) && (graph.getYPos(nd) <= upper) && (graph.getRank(nd) >= priority)) {
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

	/**
	 * Creates the priority data structure for the given graph
	 */
    public PrioDings(GraphRep graph) {
        this.graph = graph;
        // initializes data structures with given nodes
        // IDEA:
        // 0. a. retrieve all x-coordinates from nodes; create sorted list of unique x-coords
        //    b. associate with every x-coordinate list (index of offset array) of resp. nodes
        // 1. build range-tree on top of this set of x-coordinates;
        //      key: x-coordinate
        //      inf: index into offset array above
        // 2. with each of the internal nodes there is a x-range associated
        //      create for top x-level PST (on the y-coordinates)
        TreeMap<Integer, Set<Integer>> uniXkeys = new TreeMap<Integer, Set<Integer>>();

        // collect distinct x-coordinates
        for (int i = 0; i < graph.getNodeCount(); i++) {
            int curKey = graph.getXPos(i);
            if (uniXkeys.containsKey(curKey)) // x-coordinate already present
            {
                Set<Integer> tmpSet = uniXkeys.get(curKey);
                tmpSet.add(i);
                uniXkeys.put(curKey, tmpSet);
                //System.out.print(".");
            } else // new x-coordinate
            {
                TreeSet<Integer> tmpSet = new TreeSet<Integer>();
                tmpSet.add(i);
                uniXkeys.put(graph.getXPos(i), tmpSet);
            }
        }
        System.out.println("We have " + uniXkeys.size() + " unique X-coordinates from " + graph.getNodeCount() + " nodes");
        nodesXstruct = new int[graph.getNodeCount()];
        offset2Xstruct = new int[uniXkeys.size() + 1];
        int curPos = 0;
        int curKey = uniXkeys.firstKey();
        offset2Xstruct[0] = curPos;
        for (int i = 0; i < uniXkeys.size(); i++) {
            Set<Integer> tmpSet = uniXkeys.get(curKey);
            Iterator<Integer> itr = tmpSet.iterator();
            while (itr.hasNext()) {
                nodesXstruct[curPos++] = itr.next();
                //if (tmpSet.size()>1)
                //    System.out.println(nodes[nodesXstruct[curPos-1]].getxPos());
            }
            offset2Xstruct[i + 1] = curPos;
            if (i < uniXkeys.size() - 1) {
                curKey = uniXkeys.higherKey(curKey);
            }
        }
        System.out.println("curPos=" + curPos);
        /*
         for(int i=0; i<uniXkeys.size(); i++)
         {
         for(int j=offsetXstruct[i]; j< offsetXstruct[i+1]; j++)
         {
         int nodeID=nodesXstruct[j];
         System.out.print("("+nodes[nodeID].getxPos()+","+nodes[nodeID].getyPos()+") ");
         }
         System.out.println();
         }*/

        // set up data for range tree:

        rangeTreeKeys = new int[uniXkeys.size()];
        rangeTreeInfs = new int[uniXkeys.size()];
        for (int i = 0; i < rangeTreeKeys.length; i++) {
            rangeTreeKeys[i] = graph.getXPos(nodesXstruct[offset2Xstruct[i]]);
            rangeTreeInfs[i] = i;
        }
        myXRT = new RangeTree(rangeTreeKeys, rangeTreeInfs);
        // now construct for the upper inner nodes of RangeTree respective PSTs
        // until 0, 2, 6, 14, 30, ...
        int limitPST = 0;
        int height = 0;
	    // TODO: Need a comment here for why we divide by 256
        while (limitPST < rangeTreeKeys.length/256) {
            limitPST = (limitPST + 1) * 2;
            height++;
        }
        limitPST = limitPST / 2;
        height--;
        System.out.println("Building " + limitPST + " PSTs that is, height=" + height);
        myPSTs = new PrioSearchTree[limitPST];
        long tmpcnt = 0;

        for (int j = 0; j < limitPST; j++) {
            // collect nodeIDs to be stored in PST j
            ArrayList<Integer> xCoordsToStore = new ArrayList<Integer>();
            ArrayList<Integer> nodeIDOffsetsToStore = new ArrayList<Integer>();
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

            int[] yKeys = new int[sizePST];
            int[] prios = new int[sizePST];
            int[] nodeIDs = new int[sizePST];
            yKeys[sizePST - 1] = 0;
            prios[sizePST - 1] = 0;
            nodeIDs[sizePST - 1] = 0;


            int nodeCounter = 0;
            for (int i = 0; i < nodeIDOffsetsToStore.size(); i++) {
                int inf = nodeIDOffsetsToStore.get(i);
                for (int k = offset2Xstruct[inf]; k < offset2Xstruct[inf + 1]; k++) {
                    nodeIDs[nodeCounter] = nodesXstruct[k];
                    yKeys[nodeCounter] = graph.getYPos(nodeIDs[nodeCounter]);
                    prios[nodeCounter] = graph.getRank(nodeIDs[nodeCounter]);
                    nodeCounter++;
                }
            }
            System.out.println(nodeCounter + " und " + sizePST);
            // assert(nodeCounter==sizePST);
            for (int i = 0; i < 5; i++) {
                System.out.println(yKeys[i] + " " + prios[i] + " " + nodeIDs[i]);
            }
            
            mySort(0, sizePST - 1, yKeys, prios, nodeIDs);

            for (int i = 0; i < 5; i++) {
                System.out.println(yKeys[i] + " " + prios[i] + " " + nodeIDs[i]);
            }
            myPSTs[j] = new PrioSearchTree(yKeys, prios, nodeIDs);
            // construct PST corresponding to internal node j (containing all nodes in subtree) */
            System.out.println("Constructing PST with " + sizePST + " elements");

        }
        System.out.println("Added " + tmpcnt + " things to PSTs");

    }
}
