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

/**
 * Provides the possibility to specify hashures to fill geometries in their
 * rendering, rather than a plain solid fill. The lines of the hashures are all
 * drawn in the direction, specified via an angle. The pattern consists of a
 * sequence of values, such that starting with the first, it alternatingly
 * specifies a hashure line width, followed by the space between two lines. The
 * pattern direction (for measuring gap size) is the line direction rotated by
 * 90 degrees in clockwise direction. In other words, the pattern goes to the
 * line direction (e.g. rightwards for upward lines).
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
    }

    public static Hashures evenSpacing(double angle, double linethickness) {
        return new Hashures(angle, linethickness);
    }

    public static Hashures evenSpacing(HashureSlope slope, double linethickness) {
        return evenSpacing(slope.toAngle(), linethickness);
    }

    public static Hashures narrowSpacing(double angle, double linethickness) {
        return new Hashures(angle, linethickness, 0.5 * linethickness);
    }

    public static Hashures narrowSpacing(HashureSlope slope, double linethickness) {
        return narrowSpacing(slope.toAngle(), linethickness);
    }

    public static Hashures wideSpacing(double angle, double linethickness) {
        return new Hashures(angle, linethickness, 1.5 * linethickness);
    }

    public static Hashures wideSpacing(HashureSlope slope, double linethickness) {
        return wideSpacing(slope.toAngle(), linethickness);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final double _angle;
    private final double[] _pattern;
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
     * of the geometry
     */
    public List<LineSegment> computeHashures(BaseGeometry geom, double scale) {

        Rectangle box = Rectangle.byBoundingBox(geom);
        box.grow(3 * getMaximumStrokeWidth() * scale);

        Vector dir = getDirection();
        Vector shift = dir.clone();
        shift.rotate90DegreesCounterclockwise();
        shift.scale(scale);

        Vector center = box.center();

        Line forwardline = new Line(center, dir);
        Line backwardline = forwardline.clone();

        List<LineSegment> result = new ArrayList();

        // draw the forward line
        LineSegment clipped;
        int pi;

        // go backwards
        pi = shift(backwardline, shift, 0, -1);

        clipped = backwardline.clip(box);
        while (clipped != null) {
            result.add(clipped);
            pi = shift(backwardline, shift, pi, -1);
            clipped = backwardline.clip(box);
        }
        Collections.reverse(result);

        // go forwards
        clipped = forwardline.clip(box);
        pi = 0;
        while (clipped != null) {
            result.add(clipped);
            pi = shift(forwardline, shift, pi, 1);
            clipped = forwardline.clip(box);
        }

        return result;
    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private int shift(Line line, Vector shift, int pi, int dpi) {
        double shiftscale = 0.5 * _pattern[pi];
        pi = (pi + dpi + _pattern.length) % _pattern.length;
        shiftscale += _pattern[pi];
        pi = (pi + dpi + _pattern.length) % _pattern.length;
        shiftscale += 0.5 * _pattern[pi];

        shiftscale *= dpi;
        line.translate(shiftscale * shift.getX(), shiftscale * shift.getY());
        return pi;
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
