/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.util;

/**
 * Represents a closed interval on the real domain. Empty intervals are
 * represented by a maximum that is strictly lower than the configured minimum.
 *
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Interval {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private double _min;
    private double _max;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Clones the given interval.
     *
     * @param clone interval to be copied
     */
    public Interval(Interval clone) {
        _min = clone._min;
        _max = clone._max;
    }

    /**
     * Creates an empty interval.
     */
    public Interval() {
        _min = 1;
        _max = 0;
    }

    /**
     * Creates a singleton interval.
     *
     * @param value the single value
     */
    public Interval(double value) {
        _min = value;
        _max = value;
    }

    /**
     * Creates an arbitrary interval, based on the minimum and maximum value.
     * Note that setting min greater than max yields an empty interval.
     *
     * @param min lower bound on the interval
     * @param max upper bound on the interval
     */
    public Interval(double min, double max) {
        _min = min;
        _max = max;
    }

    /**
     * Creates the smallest interval containing all specified values.
     *
     * @param values values to be contained by the interval
     */
    public Interval(double... values) {
        if (values.length == 0) {
            _min = 1;
            _max = 0;
        } else {
            _min = values[0];
            _max = values[0];
            for (int i = 1; i < values.length; i++) {
                _min = Math.min(_min, values[i]);
                _max = Math.max(_max, values[i]);
            }
        }
    }

    /**
     * Constructs the interval that represents the union of the provided
     * intervals. Note that any gaps between the provided intervals will also be
     * included in the resulting interval.
     *
     * @param intervals intervals to compute the union of
     * @return union of the provided intervals
     */
    public static Interval union(Interval... intervals) {
        if (intervals.length == 0) {
            return new Interval();
        } else {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (Interval interval : intervals) {
                if (!interval.isEmpty()) {
                    min = Math.min(min, interval.getMin());
                    max = Math.max(max, interval.getMax());
                }
            }
            return new Interval(min, max);
        }
    }

    /**
     * Constructs the interval that represents the intersection of the provided
     * intervals.
     *
     * @param intervals intervals to compute the intersection of
     * @return intersection of the provided intervals
     */
    public static Interval intersection(Interval... intervals) {
        if (intervals.length == 0) {
            return new Interval();
        } else if (intervals[0].isEmpty()) {
            return new Interval();
        } else {
            double min = intervals[0].getMin();
            double max = intervals[0].getMax();
            for (int i = 1; i < intervals.length; i++) {
                if (intervals[i].isEmpty()) {
                    min = 1;
                    max = 0;
                    break;
                } else {
                    min = Math.max(min, intervals[i].getMin());
                    max = Math.min(max, intervals[i].getMax());
                }
            }
            return new Interval(min, max);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public void setMin(double min) {
        _min = min;
    }

    public void setMax(double max) {
        _max = max;
    }

    public double getMin() {
        return _min;
    }

    public double getMax() {
        return _max;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Tests if the interval is empty, with a precision of DoubleUtil.EPS.
     *
     * @return maximum &lt; minimum - EPS
     */
    public boolean isEmpty() {
        return isEmpty(DoubleUtil.EPS);
    }

    /**
     * Tests if the interval is empty, with a specified precision.
     *
     * @param prec precision
     * @return maximum &lt; minimum - prec
     */
    public boolean isEmpty(double prec) {
        return _max < _min - prec;
    }

    /**
     * Tests for a singleton interval, with a precision of DoubleUtil.EPS. Note
     * that it also returns true for empty intervals.
     *
     * @return maximum &lt; minimum + EPS
     */
    public boolean isSingleton() {
        return isSingleton(DoubleUtil.EPS);
    }

    /**
     * Tests for a singleton interval, with a specified precision. Note that it
     * also returns true for empty intervals.
     *
     * @param prec precision
     * @return maximum &lt; minimum + prec
     */
    public boolean isSingleton(double prec) {
        return _max <= _min + prec;
    }

    /**
     * Computes the length of the interval. Note that both singleton and empty
     * intervals have a length of 0.
     *
     * @return maximum - minimum
     */
    public double length() {
        if (isEmpty() || isSingleton()) {
            return 0;
        } else {
            return _max - _min;
        }
    }

    /**
     * Returns the mean value of the interval. NaN will be returned for empty
     * intervals.
     *
     * @return (min + max) / 2
     */
    public double mean() {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            return (_max + _min) / 2.0;
        }
    }

    /**
     * Tests whether a given value is contained in the interval, with a
     * precision of DoubleUtil.EPS.
     *
     * @param value value to test for
     * @return value in [minimum-EPS, maximum+EPS]
     */
    public boolean contains(double value) {
        return contains(value, DoubleUtil.EPS);
    }

    /**
     * Tests whether a given value is contained in the interval, with a
     * specified precision.
     *
     * @param value value to test for
     * @param prec precision
     * @return value in [minimum-prec, maximum+prec]
     */
    public boolean contains(double value, double prec) {
        return _min - prec <= value && value <= _max + prec;
    }

    /**
     * Generates a random number contained in the interval. NaN will be returned
     * for empty intervals.
     *
     * @return random number in [minimum,maximum)
     */
    public double random() {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            return _min + (_max - _min) * Math.random();
        }
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        } else if (isSingleton()) {
            return "[" + _min + "]";
        } else {
            return "[" + _min + ", " + _max + "]";
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    /**
     * Scales the interval around its mean value. Has no effect on empty
     * intervals.
     *
     * @param factor factor to increase the length by
     */
    public void scale(double factor) {
        scale(factor, mean());
    }

    /**
     * Scales the interval around the provided center value.
     *
     * @param factor factor to increase the length by
     * @param center central value for scaling
     */
    public void scale(double factor, double center) {
        if (!isEmpty()) {
            _min = center - (center - _min) * factor;
            _max = center + (_max - center) * factor;
        }
    }

    /**
     * Shifts the interval, increasing both minimum and maximum by the same
     * amount. Has no effect on empty intervals.
     *
     * @param shift desired increase
     */
    public void shift(double shift) {
        if (!isEmpty()) {
            _min += shift;
            _max += shift;
        }
    }
    //</editor-fold>
}
