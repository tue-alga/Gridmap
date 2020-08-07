/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

/**
 * Utility class with convenience methods for dealing with double values, in
 * particular, focused on handling imprecision.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DoubleUtil {

    //<editor-fold defaultstate="collapsed" desc="STATIC FIELDS">
    /**
     * Default tolerance for imprecision.
     */
    public static final double EPS = 0.000001;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRECISION">
    /**
     * Returns whether the absolute value between A and B is at most EPS.
     *
     * @param A first value
     * @param B second value
     * @return |A-B| &lt;= DoubleUtil.EPS
     */
    public static boolean close(double A, double B) {
        return Math.abs(A - B) <= EPS;
    }

    /**
     * Returns whether the absolute value between A and B is at most the given
     * eps value.
     *
     * @param A first value
     * @param B second value
     * @param eps tolerance value
     * @return |A-B| &lt;= eps
     */
    public static boolean close(double A, double B, double eps) {
        return -eps <= A - B && A - B <= eps;
    }

    /**
     * Ensures that value A lies in range [min,max].
     *
     * @param A Value to clip to range
     * @param min Lower bound of range
     * @param max Upper bound of range
     * @return A if A in [min,max]; min if A &lt; min; max if A &gt; max
     */
    public static double clipValue(double A, double min, double max) {
        return Math.max(Math.min(A, max), min);
    }

    /**
     * Returns whether a value lies in a closed interval for a given precision.
     * Assumes x &lt;= y. Positive precision makes the check "enlarge" the
     * interval, negative values shrink the interval.
     *
     * @param a value to check for
     * @param x lower value of interval
     * @param y upper value of interval
     * @param prec precision
     * @return a in [x-prec,y+prec].
     */
    public static boolean inClosedInterval(double a, double x, double y, double prec) {
        return x - prec <= a && a <= y + prec;
    }

    /**
     * Checks whether a value lies in an closed interval.
     *
     * @param a value to check for
     * @param x lower value of the interval
     * @param y upper value of the interval
     * @return a in [x-EPS, y+EPS]
     */
    public static boolean inClosedInterval(double a, double x, double y) {
        return inClosedInterval(a, x, y, EPS);
    }

    /**
     * Checks whether a value lies in an open interval.
     *
     * @param a value to check for
     * @param x lower value of the interval
     * @param y upper value of the interval
     * @return a in (x+EPS, y-EPS)
     */
    public static boolean inOpenInterval(double a, double x, double y) {
        return inOpenInterval(a, x, y, -EPS);
    }

    /**
     * Returns whether a value lies in an open interval for a given precision.
     * Assumes x &lt;= y. Positive precision makes the check "enlarge" the
     * interval, negative values shrink the interval.
     *
     * @param a value to check for
     * @param x lower value of interval
     * @param y upper value of interval
     * @param prec precision
     * @return a in (x-prec,y+prec).
     */
    public static boolean inOpenInterval(double a, double x, double y, double prec) {
        return x - prec < a && a < y + prec;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="EQUATION SOLVING">
    /**
     * Returns the solutions of a*x^2 + b*x + c = 0.
     *
     * @param a factor for quadratic term
     * @param b factor for linear term
     * @param c factor for constant term
     * @return array with 0, 1, or 2 values for x that satisfy the equation, in
     * increasing order
     */
    public static double[] solveQuadraticEquation(double a, double b, double c) {
        if (close(a, 0)) {
            // b x + c = 0 --> x = -c/b
            return new double[]{-c / b};
        } else {
            double d = b * b - 4 * a * c;
            if (d < -EPS) {
                return new double[]{};
            } else if (d < EPS) {
                return new double[]{-b / (2 * a)};
            } else {
                double s1 = (-b + Math.sqrt(d)) / (2 * a);
                double s2 = (-b - Math.sqrt(d)) / (2 * a);
                if (s1 < s2) {
                    return new double[]{s1, s2};
                } else {
                    return new double[]{s2, s1};
                }

            }
        }
    }

    /**
     * Returns the smallest solution of a*x^2 + b*x + c = 0 strictly greater
     * than a given threshold value.
     *
     * @param a factor for quadratic term
     * @param b factor for linear term
     * @param c factor for constant term
     * @param threshold threshold value
     * @return the smallest positive solution; NaN if no positive solution
     * exists
     */
    public static double solveQuadraticEquationForSmallestPositive(double a, double b, double c, double threshold) {
        double[] sol = solveQuadraticEquation(a, b, c);

        switch (sol.length) {
            case 0:
                return Double.NaN;
            case 1:
                if (sol[0] > threshold) {
                    return sol[0];
                } else {
                    return Double.NaN;
                }
            default:
                if (sol[0] > threshold && sol[1] > threshold) {
                    return Math.min(sol[0], sol[1]);
                } else if (sol[0] > threshold) {
                    return sol[0];
                } else if (sol[1] > threshold) {
                    return sol[1];
                } else {
                    return Double.NaN;
                }
        }
    }
    //</editor-fold>    
}
