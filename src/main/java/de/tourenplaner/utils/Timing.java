package de.tourenplaner.utils;

/**
 * Helper class for prettier and easily grepable timing
 *
 * Created by niklas on 14.05.15.
 */
public class Timing {
    public static String took(String what, long start) {
        long now = System.nanoTime();
        return "TIMING: "+what+" took "+(double) (now - start) / 1000000.0 + " ms ";
    }
}
