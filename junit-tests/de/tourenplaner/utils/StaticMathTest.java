package de.tourenplaner.utils;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class StaticMathTest {
    @Test
    public void testReverse() throws Exception {
        int[] testArray = new int[]{35,44,76,83,768,37,54,68,3,5,4,7,6,8};
        StaticMath.reverse(testArray, 0, 14);
        assertArrayEquals(new int[]{8,6,7,4,5,3,68,54,37,768,83,76,44,35},testArray);
    }

    @Test
    public void testNextPerm() throws Exception {
        
        
        
        int[] testArray= {1,2,3,4,5};
        StaticMath.nextPerm(testArray);
        assertArrayEquals(new int[]{1,2,3,5,4}, testArray);
        StaticMath.nextPerm(testArray);
        assertArrayEquals(new int[]{1,2,4,3,5}, testArray);
        
        testArray = new int[]{1,2,3,4,5};
        int counter = 1;
        while (StaticMath.nextPerm(testArray)){
            counter++;
        }
        assertEquals(120,counter);
        assertArrayEquals(new int[]{1,2,3,4,5},testArray);

    }
}
