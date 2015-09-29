/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tourenplaner.graphrep;

import com.carrotsearch.hppc.IntArrayList;

/**
 * RangeTree stores key, value pairs of ints for fast range access on the key values.
 * Instead of using a pointer based tree all data is stored in continues arrays.
 *
 * @author spark
 */
public final class RangeTree {
    // treeKey stores the key information such that treeKey[i] is the key
    // for treeInf[i]
    private final int[] treeKey;
    // treeInf[i] stores the value for treeKey[i]
    private final int[] treeInf;

    private int initArray(int[] inpKey, int[] inpInf, int treePos, int arrPos) {
        if (2 * treePos + 1 < treeKey.length) {
            arrPos = initArray(inpKey, inpInf, 2 * treePos + 1, arrPos);
        }
        treeKey[treePos] = inpKey[arrPos];
        treeInf[treePos] = inpInf[arrPos];
        arrPos++;
        if (2 * treePos + 2 < treeKey.length) {
            arrPos = initArray(inpKey, inpInf, 2 * treePos + 2, arrPos);
        }
        return arrPos;

    }

    /**
     * Query the tree with the root at pos for values larger than lower (inclusive). The result consists of
     * individual items in keyItems and infItems as well as the indices of the heads of all subtrees completely
     * contained in the result which are stored in batches.
     */
    private void batchQueryLeft(int lower, int pos, IntArrayList batches, IntArrayList keyItems, IntArrayList infItems) {
        if (pos > treeKey.length - 1) {
            return;
        }
        if (treeKey[pos] >= lower) {
            batchQueryLeft(lower, 2 * pos + 1, batches, keyItems, infItems);
            keyItems.add(treeKey[pos]);
            infItems.add(treeInf[pos]);

            if (2 * pos + 2 < treeKey.length) {
                batches.add(2 * pos + 2);
            }
        } else // if (keys[pos]<lower)
        {
            batchQueryLeft(lower, 2 * pos + 2, batches, keyItems, infItems);
        }
    }

    /**
     * Query the tree with the root at pos for values smaller than upper (inclusive). The result consists of
     * individual items in keyItems and infItems as well as the indices of the heads of all subtrees completely
     * contained in the result which are stored in batches.
     */
    private void batchQueryRight(int upper, int pos, IntArrayList batches, IntArrayList keyItems, IntArrayList infItems) {
        if (pos > treeKey.length - 1) {
            return;
        }
        if (treeKey[pos] <= upper) {
            keyItems.add(treeKey[pos]);
            infItems.add(treeInf[pos]);

            if (2 * pos + 1 < treeKey.length) {
                batches.add(2 * pos + 1);
            }
            batchQueryRight(upper, 2 * pos + 2, batches, keyItems, infItems);
        } else // if (keys[pos]>upper)
        {
            batchQueryRight(upper, 2 * pos + 1, batches, keyItems, infItems);
        }
    }

    /**
     * Query the tree with the root at pos for values larger than lower and smaller than upper (inclusive). The result consists of
     * individual items in keyItems and infItems as well as the indices of the heads of all subtrees completely
     * contained in the result which are stored in batches.
     */
    public void batchQuery(int lower, int upper, int pos, IntArrayList batches, IntArrayList keyItems, IntArrayList infItems) {
        // starting at subtree rooted at pos, returns everything between lower and upper
        // in batches (heads of subtrees) and individual infs
        if ((treeKey[pos] < lower) && (2 * pos + 2 < treeKey.length))
            batchQuery(lower, upper, 2 * pos + 2, batches, keyItems, infItems);
        else if ((treeKey[pos] > upper) && (2 * pos + 1 < treeKey.length))
            batchQuery(lower, upper, 2 * pos + 1, batches, keyItems, infItems);
        else if (treeKey[pos] > lower && treeKey[pos] < upper)// we have a split
        {
            batchQueryLeft(lower, 2 * pos + 1, batches, keyItems, infItems);    // report everything in the left subtree larger then lower
            keyItems.add(treeKey[pos]);    // split item itself should be reported
            infItems.add(treeInf[pos]);
            batchQueryRight(upper, 2 * pos + 2, batches, keyItems, infItems);    // report everything in the right subtree smaller than upper
        }
    }

    /**
     * Appends all items and their keys stored in the subtree rooted at pos to keyItems and infItems
     */
    public void reportSubtree(int pos, IntArrayList keyItems, IntArrayList infItems) {
        if (pos > treeKey.length - 1) {
            return;
        }
        reportSubtree(2 * pos + 1, keyItems, infItems);
        keyItems.add(treeKey[pos]);
        infItems.add(treeInf[pos]);
        reportSubtree(2 * pos + 2, keyItems, infItems);
    }

    /**
     * Creates a RangeTree on the key value pairs stored in inpKey and inpInf,
     * IMPORTANT: Data is assumed to be sorted according to keys
     */
    public RangeTree(int[] inpKey, int[] inpInf) // assume that <data,inf> is sorted according to Keys
    // we are happy with keys occuring several times 
    {
        for (int i = 0; i < inpKey.length - 1; i++) {
            assert (inpKey[i] <= inpKey[i + 1]);
        }
        treeKey = new int[inpKey.length];
        treeInf = new int[inpKey.length];
        initArray(inpKey, inpInf, 0, 0);
    }


}
