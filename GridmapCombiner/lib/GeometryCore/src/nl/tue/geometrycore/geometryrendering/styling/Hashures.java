/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometryrendering.styling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.util.Pair;

/**
 * Provides the possibility to specify hashures to fill geometries in their
 * rendering, rather than a plain solid fill. The lines of the hashures are all
 * drawn in the direction, specified via an angle. The pattern consists of a
 * sequence of values, such that starting with the first, it alternatingly
 * specifies a hashure line width, followed by the space between two lines. The
 * pattern direction (for measuring gap size) is the line direction rotated by
 * 90 degrees in clockwise direction. In other words, the pattern goes to the
 * line direction (e.g. rightwards for upward lines). Optionally, an anchor can
 * be set that ensures that the first line always goes through this anchor
 * point. By default, this is the origin. If set to null, the center of the
 * object's bounding box is used.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Hashures {

    //<editor-fold defaultstate="collapsed" desc="STATIC">
    /**
     * Solid fills are represented by a null value. This is variable provided to
     * make the solid more explicit in code.
     */
    public final static Hashures SOLID = null;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    public Hashures(double angle, double... pattern) {
        _angle = angle;
        _pattern = pattern;
        _anchor = Vector.origin();
    }

    public Hashures(Vector anchor, double angle, double... pattern) {
        _angle = angle;
        _pattern = pattern;
        _anchor = anchor;
    }

    public static Hashures evenSpacing(double angle, double linethickness) {
        return evenSpacing(Vector.origin(), angle, linethickness);
    }

    public static Hashures evenSpacing(HashureSlope slope, double linethickness) {
        return evenSpacing(Vector.origin(), slope.toAngle(), linethickness);
    }

    public static Hashures evenSpacing(Vector anchor, double angle, double linethickness) {
        return new Hashures(anchor, angle, linethickness);
    }

    public static Hashures evenSpacing(Vector anchor, HashureSlope slope, double linethickness) {
        return evenSpacing(anchor, slope.toAngle(), linethickness);
    }

    public static Hashures narrowSpacing(double angle, double linethickness) {
        return narrowSpacing(Vector.origin(), angle, linethickness);
    }

    public static Hashures narrowSpacing(HashureSlope slope, double linethickness) {
        return narrowSpacing(Vector.origin(), slope.toAngle(), linethickness);
    }

    public static Hashures narrowSpacing(Vector anchor, double angle, double linethickness) {
        return new Hashures(anchor, angle, linethickness, 0.5 * linethickness);
    }

    public static Hashures narrowSpacing(Vector anchor, HashureSlope slope, double linethickness) {
        return narrowSpacing(slope.toAngle(), linethickness);
    }

    public static Hashures wideSpacing(double angle, double linethickness) {
        return wideSpacing(Vector.origin(), angle, linethickness);
    }

    public static Hashures wideSpacing(HashureSlope slope, double linethickness) {
        return wideSpacing(Vector.origin(), slope.toAngle(), linethickness);
    }

    public static Hashures wideSpacing(Vector anchor, double angle, double linethickness) {
        return new Hashures(anchor, angle, linethickness, 1.5 * linethickness);
    }

    public static Hashures wideSpacing(Vector anchor, HashureSlope slope, double linethickness) {
        return wideSpacing(anchor, slope.toAngle(), linethickness);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final double _angle;
    private final double[] _pattern;
    private final Vector _anchor;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    public double getAngle() {
        return _angle;
    }

    public Vector getDirection() {
        Vector dir = Vector.right();
        dir.rotate(_angle);
        return dir;
    }

    public Vector getPatternDirection() {
        Vector patdir = getDirection();
        patdir.rotate90DegreesClockwise();
        return patdir;
    }

    public double[] getPattern() {
        return _pattern;
    }

    /**
     * Computes the hashure pattern for the given geometry. This is done in
     * world space, but the scale parameter can be used to adjust to view space.
     *
     * @param geom geometry to receive hashures
     * @param scale scale factor
     * @return list of line segments slightly extending beyond the bounding box
     * of the geometry, and an integer, indicating where to start in the pattern
     */
    public Pair<List<LineSegment>, Integer> computeHashures(BaseGeometry geom, double scale) {

        // find out a box that easily contains all the required hashures
        Rectangle box = Rectangle.byBoundingBox(geom);
        box.grow(3 * getMaximumStrokeWidth() * scale);

        // basic pattern things
        Vector dir = getDirection();
        Vector shift = dir.clone();
        shift.rotate90DegreesCounterclockwise();
        shift.scale(scale);
        Vector start = _anchor == null ? box.center() : _anchor.clone();
        double patternLen = getTotalPatternLength() * scale;

        // find out the extrema for shifting
        boolean isrightwards = Vector.dotProduct(shift, Vector.right()) >= 0;
        boolean isupwards = Vector.dotProduct(shift, Vector.up()) >= 0;
        Vector mincorner = new Vector(isrightwards ? box.getLeft() : box.getRight(),
                isupwards ? box.getBottom() : box.getTop());
        Vector maxcorner = new Vector(isrightwards ? box.getRight() : box.getLeft(),
                isupwards ? box.getTop() : box.getBottom());

        List<LineSegment> result = new ArrayList();

        // backward shifting
        int last = doShifting(new Line(start.clone(), dir.clone()), box, shift, result, -1, maxcorner, mincorner, patternLen, scale);

        // remove the first line and reverse
        Collections.reverse(result);
        if (result.size() > 0) {
            result.remove(result.size() - 1);
        }

        // forward shifting
        int first = doShifting(new Line(start.clone(), dir.clone()), box, shift, result, 1, mincorner, maxcorner, patternLen, scale);

        if (last < 0) {
            return new Pair(result, first);
        } else {
            return new Pair(result, last);
        }
    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private int doShifting(Line line, Rectangle box, Vector shift, List<LineSegment> result, int dpi, Vector mincorner, Vector maxcorner, double L, double scale) {

        double dist;

        // distance to minimum        
        dist = dpi * Vector.dotProduct(Vector.subtract(mincorner, line.getThrough()), shift) / scale;

        if (dist > L) {
            int k = (int) (dist / L);
            dist -= L * k;
            line.translate(L / scale * shift.getX(), L / scale * shift.getY());
        }
        int pi = 0;
        while (dist >= 0) {
            Pair<Integer, Double> pair = shift(line, shift, pi, dpi);
            pi = pair.getFirst();
            dist -= pair.getSecond() * scale;
        }

        // distance to maximum
        dist = dpi * Vector.dotProduct(Vector.subtract(maxcorner, line.getThrough()), shift) / scale;
        int num = -1;
        while (dist >= 0) {
            LineSegment clip = line.clip(box);
            if (clip != null) {
                if (dpi > 0) {
                    // interested in first
                    if (num < 0) {
                        num = pi;
                    }
                } else {
                    // interested in last
                    num = pi;
                }
                result.add(clip);
            }
            Pair<Integer, Double> pair = shift(line, shift, pi, dpi);
            pi = pair.getFirst();
            dist -= pair.getSecond() * scale;
        }

        return num;
    }

    private Pair<Integer, Double> shift(Line line, Vector shift, int pi, int dpi) {
        double shiftscale = 0.5 * _pattern[pi];
        pi = (pi + dpi + _pattern.length) % _pattern.length;
        shiftscale += _pattern[pi];
        pi = (pi + dpi + _pattern.length) % _pattern.length;
        shiftscale += 0.5 * _pattern[pi];

        shiftscale *= dpi;
        line.translate(shiftscale * shift.getX(), shiftscale * shift.getY());
        return new Pair(pi, Math.abs(shiftscale));
    }

    private double getMaximumStrokeWidth() {
        double maxstrokewidth = 0;
        if (_pattern.length % 2 == 0) {
            for (int i = 0; i < _pattern.length; i += 2) {
                maxstrokewidth = Math.max(maxstrokewidth, _pattern[i]);
            }
        } else {
            for (int i = 0; i < _pattern.length; i++) {
                maxstrokewidth = Math.max(maxstrokewidth, _pattern[i]);
            }
        }
        return maxstrokewidth;
    }

    /**
     *
     * @return the length of the pattern until the first value is used for a
     * line again.
     */
    private double getTotalPatternLength() {
        double total = 0;
        for (int i = 0; i < _pattern.length; i++) {
            total += _pattern[i];
        }
        if (_pattern.length % 2 == 1) {
            total *= 2;
        }
        return total;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="STANDARD SLOPES">
    /**
     * Enumeration for the convenience of specifying some likely typical
     * directions.
     */
    public enum HashureSlope {

        HORIZONTAL {
            @Override
            public double toAngle() {

                return 0;
            }
        },
        VERTICAL {
            @Override
            public double toAngle() {
                return Math.PI / 2;
            }
        },
        DOWNWARD_30 {
            @Override
            public double toAngle() {
                return 5 * Math.PI / 6;
            }
        },
        UPWARD_30 {
            @Override
            public double toAngle() {
                return Math.PI / 6;
            }
        },
        DOWNWARD_45 {
            @Override
            public double toAngle() {
                return 3 * Math.PI / 4;
            }
        },
        UPWARD_45 {
            @Override
            public double toAngle() {
                return Math.PI / 4;
            }
        },
        DOWNWARD_60 {
            @Override
            public double toAngle() {
                return 4 * Math.PI / 6;
            }
        },
        UPWARD_60 {
            @Override
            public double toAngle() {
                return 2 * Math.PI / 6;
            }
        };

        public abstract double toAngle();
    }
    //</editor-fold>
}
