package de.tourenplaner.utils;

/**
 * Helper class for prettier and easily grepable timing
 *
 * Created by niklas on 14.05.15.
 */
public class Timing {

    private static final double NANOSECONDS_PER_MILLISECOND = 1000000.0;

    public static final String took(String what, long start) {
        long now = System.nanoTime();
        return "TIMING: "+what+" took "+asString(now - start);
    }

    public static final String asString(long timespan){
        return asStringNoUnit(timespan) + " ms";
    }

    public static final String asStringNoUnit(long timespan){
        return Double.toString(timespan/NANOSECONDS_PER_MILLISECOND);
    }

}
