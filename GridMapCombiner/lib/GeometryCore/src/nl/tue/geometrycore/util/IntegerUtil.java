/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

/**
 * Utility class with convenience methods for dealing with integer values.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class IntegerUtil {
    
    /**
     * Ensures that value A lies in range [min,max].
     *
     * @param A Value to clip to range
     * @param min Lower bound of range
     * @param max Upper bound of range
     * @return A if A in [min,max]; min if A &lt; min; max if A &gt; max
     */
    public static int clipValue(int A, int min, int max) {
        return Math.max(Math.min(A, max), min);
    }

    /**
     * Returns whether a value lies in a closed interval.
     * Assumes x &lt;= y. 
     *
     * @param a value to check for
     * @param x lower value of interval
     * @param y upper value of interval
     * @return a in [x,y].
     */
    public static boolean inClosedInterval(int a, int x, int y) {
        return x <= a && a <= y;
    }

    /**
     * Returns whether a value lies in an open interval.
     * Assumes x &lt;= y. 
     *
     * @param a value to check for
     * @param x lower value of interval
     * @param y upper value of interval
     * @return a in (x,y).
     */
    public static boolean inOpenInterval(int a, int x, int y) {
        return x  < a && a < y;
    }
}
