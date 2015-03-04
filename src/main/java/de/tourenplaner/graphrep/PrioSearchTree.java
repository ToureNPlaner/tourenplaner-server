/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tourenplaner.graphrep;

import java.util.ArrayList;

/**
 * PrioSearchTree stores key, value pairs according to an associated priority
 * and allows effieciently querying for pairs with priority greater than
 * some minimum.
 *
 * @author spark
 */
public class PrioSearchTree {

    
    int[] treeKey;
    int[] treePrio;
    int[] treeInf;
    int[] heapKey;
    int[] heapPrio;
    int[] heapInf;
    boolean[] stored;
    int[] subTreeSize;

    private void initArray(int[] inpKey, int[] inpPrio, int[] inpInf, int treePos, int arrStart, int arrEnd) // stores unstored things starting from arrStart to arrEnd in the input array in
    // subtree below (including) treePos
    // note that (arrEnd-arrStart+1) can be larger than the subtree size since some
    // of the elements in the input array have been stored before (as prio-point) -- at most log n many, though
    {

        // find the one with max priority
        int priomax = -1;
        int priomaxpos = -1;
        for (int i = arrStart; i <= arrEnd; i++) {
            if (stored[i] == false) {
                int cur_prio = inpPrio[i];
                if (cur_prio > priomax) {
                    priomax = cur_prio;
                    priomaxpos = i;
                }
            }
        }
        assert (priomaxpos != -1);

        // store the prio-point
        heapKey[treePos] = inpKey[priomaxpos];
        heapPrio[treePos] = inpPrio[priomaxpos];
        heapInf[treePos] = inpInf[priomaxpos];
        stored[priomaxpos] = true;


        // figure out the split point
        int splitpos = -1;
        int leftcounter = 0;
        int leftsize = 0;

        if (2 * treePos + 1 < treeKey.length) // figure out size of left subtree
        {
            leftsize += subTreeSize[2 * treePos + 1];
        }

        int curpos = arrStart;
        while (leftcounter <= leftsize) {
            if (stored[curpos] == false) {
                splitpos = curpos;
                leftcounter++;
            }
            curpos++;
        }
        treeKey[treePos] = inpKey[splitpos];
        treePrio[treePos] = inpPrio[splitpos];
        treeInf[treePos] = inpInf[splitpos];
        stored[splitpos] = true;

        if (leftsize > 0) {
            initArray(inpKey, inpPrio, inpInf, 2 * treePos + 1, arrStart, splitpos - 1);
        }

        if (2 * treePos + 2 < treeKey.length) {
            initArray(inpKey, inpPrio, inpInf, 2 * treePos + 2, splitpos + 1, arrEnd);
        }
    }

    private void queryPSTleft(int lower, int prio, int pos, ArrayList<Integer> dataKey, ArrayList<Integer> dataPrio, ArrayList<Integer> dataInf) // return everything below the tree at pos which is larger than lower and has priority >=prio
    // result consists of individual data items 
    {
        if (pos > treeKey.length - 1) {
            return;
        }
        // if prio point has already too small priority, abort the query
        if (heapPrio[pos] < prio) {
            return;
        }
        // check if current prioPoint should be reported
        if (heapKey[pos] >= lower) {
            dataKey.add(heapKey[pos]);
            dataPrio.add(heapPrio[pos]);
            dataInf.add(heapInf[pos]);
        }

        if (treeKey[pos] >= lower) {
            if (2 * pos + 1 < treeKey.length) {
                queryPSTleft(lower, prio, 2 * pos + 1, dataKey, dataPrio, dataInf);
            }
            // check if current splitPoint should be reported

            if (treePrio[pos] >= prio) {
                dataKey.add(treeKey[pos]);
                dataPrio.add(treePrio[pos]);
                dataInf.add(treeInf[pos]);
            }

            if (2 * pos + 2 < treeKey.length) {
                reportSubtreePrio(2 * pos + 2, prio, dataKey, dataPrio, dataInf);
            }
        } else // if (keys[pos]<lower)
        if (2 * pos + 2 < treeKey.length) {
            queryPSTleft(lower, prio, 2 * pos + 2, dataKey, dataPrio, dataInf);
        }
    }

    private void queryPSTright(int upper, int prio, int pos, ArrayList<Integer> dataKey, ArrayList<Integer> dataPrio, ArrayList<Integer> dataInf) // return everything below the tree at pos which is smaller than upper and has priority >=prio
    // result consists of individual data items 
    {
        if (pos > treeKey.length - 1) {
            return;
        }
        // if prio point has already too small priority, abort the query
        if (heapPrio[pos] < prio) {
            return;
        }
        // check if current prioPoint should be reported
        if (heapKey[pos] <= upper) {
            dataKey.add(heapKey[pos]);
            dataPrio.add(heapPrio[pos]);
            dataInf.add(heapInf[pos]);
        }

        if (treeKey[pos] <= upper) {
            if (2 * pos + 1 < treeKey.length) { // prio-report left subtree
                reportSubtreePrio(2 * pos + 1, prio, dataKey, dataPrio, dataInf);

            }
            // check if current splitPoint should be reported

            if (treePrio[pos] >= prio) {
                dataKey.add(treeKey[pos]);
                dataPrio.add(treePrio[pos]);
                dataInf.add(treeInf[pos]);
            }
            if (2 * pos + 2 < treeKey.length) {
                queryPSTright(upper, prio, 2 * pos + 2, dataKey, dataPrio, dataInf);

            }
        } else // if (keys[pos]<lower)
        if (2 * pos + 1 < treeKey.length) {
            queryPSTright(upper, prio, 2 * pos + 1, dataKey, dataPrio, dataInf);
        }
    }

	/**
	 *  Query the subtree rooted at pos for all data with keys between lower and upper and priority >= prio
	 * */
    public void queryPST(int lower, int upper, int prio, int pos, ArrayList<Integer> dataKey, ArrayList<Integer> dataPrio, ArrayList<Integer> dataInf) {
        // starting at subtree rooted at pos, returns everything between lower and upper with priority >=prio
        // result comes as vector of individual data items 
        if (pos > treeKey.length - 1) {
            return;
        }
        // if prio point has already too small priority, abort the query
        if (heapPrio[pos] < prio) {
            return;
        }

        // check if current prioPoint should be reported
        if ((heapPrio[pos] >= prio) && (heapKey[pos] >= lower) && (heapKey[pos] <= upper)) {
            dataKey.add(heapKey[pos]);
            dataPrio.add(heapPrio[pos]);
            dataInf.add(heapInf[pos]);

        }

        if (treeKey[pos] < lower) // descend into right subtree
        {
            queryPST(lower, upper, prio, 2 * pos + 2, dataKey, dataPrio, dataInf);
        } else if (treeKey[pos] > upper) // descend into left subtree
        {
            queryPST(lower, upper, prio, 2 * pos + 1, dataKey, dataPrio, dataInf);
        } else // we have a split
        {
            queryPSTleft(lower, prio, 2 * pos + 1, dataKey, dataPrio, dataInf);	// report everything in the left subtree larger then lower
            if (treePrio[pos] >= prio) {          // if prio is ok, split item itself should be reported
                dataKey.add(treeKey[pos]);
                dataPrio.add(treePrio[pos]);
                dataInf.add(treeInf[pos]);
            }
            queryPSTright(upper, prio, 2 * pos + 2, dataKey, dataPrio, dataInf);	// report everything in the right subtree smaller than upper
        }
    }


    private void reportSubtreePrio(int pos, int prio, ArrayList<Integer> dataKey, ArrayList<Integer> dataPrio, ArrayList<Integer> dataInf) // appends the actual items in the subtree rooted at pos at the end as long as prio matches
    {
        assert (pos <= treeKey.length - 1);

        // if prio gets too small, abort
        if (heapPrio[pos] < prio) {
            return;
        }

        // report prio-item
        dataKey.add(heapKey[pos]);
        dataPrio.add(heapPrio[pos]);
        dataInf.add(heapInf[pos]);


        // check if split item should be reported
        if (treePrio[pos] >= prio) {
            dataKey.add(treeKey[pos]);
            dataPrio.add(treePrio[pos]);
            dataInf.add(treeInf[pos]);
        }


        if (2 * pos + 1 < heapKey.length) {
            reportSubtreePrio(2 * pos + 1, prio, dataKey, dataPrio, dataInf);
        }
        if (2 * pos + 2 < heapKey.length) {
            reportSubtreePrio(2 * pos + 2, prio, dataKey, dataPrio, dataInf);
        }
    }

    private int initSubTreeSize(int pos) {
        subTreeSize[pos] = 2;
        if (2 * pos + 1 < treeKey.length) {
            subTreeSize[pos] += initSubTreeSize(2 * pos + 1);
        }
        if (2 * pos + 2 < treeKey.length) {
            subTreeSize[pos] += initSubTreeSize(2 * pos + 2);
        }
        return subTreeSize[pos];
    }

	/**
	 * Creates a new PrioritySearchTree containing the given items.
	 * IMPORTANT:
	 * - Items are assumed to be sorted by key, priority and value
	 * - The number of elements is assumed to be even
	 * - Keys can occur multiple times
	 */
    public PrioSearchTree(int[] inpKey, int[] inpPrio, int[] inpInf)
    {
        /*
         for(int i=0; i<inpKey.length-1; i++)
         {
         assert(inpData[i]<=inpData[i+1]);
         assert(get<1>(inpData[i])>=0);
         }*/

        treeKey = new int[inpKey.length / 2];
        treePrio = new int[inpKey.length / 2];
        treeInf = new int[inpKey.length / 2];
        heapKey = new int[inpKey.length / 2];
        heapPrio = new int[inpKey.length / 2];
        heapInf = new int[inpKey.length / 2];

        stored = new boolean[inpKey.length];
        subTreeSize = new int[inpKey.length / 2];


        initSubTreeSize(0);
        assert(subTreeSize[0] == inpKey.length);

        // do the actual construction
        initArray(inpKey, inpPrio, inpInf, 0, 0, inpKey.length - 1);

        for (int i = 0; i < inpKey.length - 1; i++) {
            assert (stored[i] == true);
        }
        stored = null;
        subTreeSize = null;
    }
}
