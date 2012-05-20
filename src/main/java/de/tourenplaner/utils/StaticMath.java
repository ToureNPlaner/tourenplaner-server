/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.utils;

/**
 * Class used to store static math functions for example for
 * dealing with permutations
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class StaticMath {
    /**
     * Reverses the order of the given int array in-place [first, last)
     *
     * @param values
     * @param first  the first element of the reversed subsequence (inclusive)
     * @param last   the element after the last reversed (exclusive)
     */
    public static void reverse(int[] values, int first, int last) {
        int temp;
        if (first == last) return;
        --last;
        while (first < last) {
            // swap first, last
            temp = values[first];
            values[first] = values[last];
            values[last] = temp;

            ++first;
            --last;
        }
    }

    /**
     * Transforms values into the lexicographically next Permutation and returns
     * false if this is the last and true otherwise
     *
     * @param values
     * @return false if this is the last permutation, true otherwise
     */
    public static boolean nextPerm(int[] values) {
        int i = values.length - 1;

        if (values.length < 2) return false;

        for (; ; ) {
            int ii = i;
            --i;
            int temp;
            if (values[i] < values[ii]) {
                int j = values.length;
                while (values[i] >= values[--j]) ;

                // swap i, j
                temp = values[i];
                values[i] = values[j];
                values[j] = temp;

                reverse(values, ii, values.length);
                return true;
            }
            if (i == 0) {
                reverse(values, 0, values.length);
                return false;
            }
        }
    }

}
