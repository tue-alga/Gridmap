/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.linear;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * A line segment, spanning two points. It is considered oriented, having one
 * point as its designated start point.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class LineSegment extends OrientedGeometry<LineSegment> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Vector _start, _end;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    public LineSegment(Vector start, Vector end) {
        _start = start;
        _end = end;
    }

    public static LineSegment byStartAndOffset(Vector start, Vector offset) {
        return new LineSegment(start, Vector.add(start, offset));
    }

    public static LineSegment byStartDirectionAndLength(Vector start, Vector normalizeddirection, double length) {
        return new LineSegment(start, Vector.add(start, Vector.multiply(length, normalizeddirection)));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    @Override
    public Vector getStart() {
        return _start;
    }

    public void setStart(Vector start) {
        _start = start;
    }

    @Override
    public Vector getEnd() {
        return _end;
    }

    public void setEnd(Vector end) {
        _end = end;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    @Override
    public Vector getStartTangent() {
        return getDirection();
    }

    @Override
    public Vector getEndTangent() {
        return getDirection();
    }

    /**
     * Computes the normalized direction of the line segment.
     *
     * @return normalized direction
     */
    public Vector getDirection() {
        Vector dir = Vector.subtract(_end, _start);
        dir.normalize();
        return dir;
    }

    @Override
    public double areaSigned() {
        return 0;
    }

    @Override
    public double perimeter() {
        return _start.distanceTo(_end);
    }

    /**
     * Alias for {@link #perimeter}.
     *
     * @return length of the line segment
     */
    public double length() {
        return perimeter();
    }

    /**
     * Computes the squared length of the line segment.
     *
     * @return squared length of the line segment
     */
    public double squaredLength() {
        return _start.squaredDistanceTo(_end);
    }

    /**
     * Determines whether the point lies in the infinite slab spanned by this
     * line segment, at precision DoubleUtil.EPS.
     *
     * @param point test point
     * @return whether point lies inside the slab
     */
    public boolean inSlab(Vector point) {
        return inSlab(point, DoubleUtil.EPS);
    }

    /**
     * Determines whether the point lies in the infinite slab spanned by this
     * line segment, at a given precision.
     *
     * @param point test point
     * @param prec precision
     * @return whether point lies inside the slab
     */
    public boolean inSlab(Vector point, double prec) {
        Vector dir = Vector.subtract(point, _start);
        Vector chorddir = Vector.subtract(_end, _start);
        double len = chorddir.length();
        chorddir.scale(1.0 / len);
        return DoubleUtil.inClosedInterval(Vector.dotProduct(dir, chorddir), 0, len, prec);
    }

    @Override
    public Vector closestPoint(Vector point) {
        Vector dir = Vector.subtract(point, _start);
        Vector chorddir = Vector.subtract(_end, _start);
        double len = chorddir.length();
        chorddir.scale(1.0 / len);

        double dotp = Vector.dotProduct(dir, chorddir);
        if (dotp <= 0) {
            return _start;
        } else if (dotp >= len) {
            return _end;
        } else {
            chorddir.scale(dotp);
            chorddir.translate(_start);
            return chorddir;
        }
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
         // TODO: this kind of implementation for all geometries?
        return closestPoint(point).isApproximately(point, prec);
//        double point_end_x = _end.getX() - point.getX();
//        double point_end_y = _end.getY() - point.getY();
//        double start_point_x = point.getX() - _start.getX();
//        double start_point_y = point.getY() - _start.getY();
//        double crossproduct = Vector.crossProduct(point_end_x, point_end_y, start_point_x, start_point_y);
//        if (Math.abs(crossproduct) > prec) {
//            return false;
//        }
//        double dotproduct = Vector.dotProduct(point_end_x, point_end_y, start_point_x, start_point_y);
//        return DoubleUtil.inClosedInterval(dotproduct, 0, _start.squaredDistanceTo(_end), prec);
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
        // extend to line
        int presize = intersections.size();
        Line.byThroughpoints(_start, _end).intersect(otherGeom, prec, intersections);

        for (int i = intersections.size() - 1; i >= presize; i--) {
            BaseGeometry intgeom = intersections.get(i);

            switch (intgeom.getGeometryType()) {
                case VECTOR:
                    if (!onBoundary((Vector) intgeom, prec)) {
                        intersections.remove(i);
                    }
                    break;
                case LINE:
                    intersections.set(i, clone());
                    break;
                case HALFLINE:
                    HalfLine inthalfline = (HalfLine) intgeom;

                    boolean startinhalfspace = inthalfline.inHalfSpace(_start);
                    boolean endinhalfspace = inthalfline.inHalfSpace(_end);

                    if (startinhalfspace && endinhalfspace) {
                        intersections.set(i, clone());
                    } else if (startinhalfspace) {
                        if (_start.isApproximately(inthalfline.getOrigin(), prec)) {
                            intersections.set(i, inthalfline.getOrigin());
                        } else {
                            intersections.set(i, new LineSegment(_start, inthalfline.getOrigin()));
                        }
                    } else if (endinhalfspace) {
                        if (_end.isApproximately(inthalfline.getOrigin(), prec)) {
                            intersections.set(i, inthalfline.getOrigin());
                        } else {
                            intersections.set(i, new LineSegment(_end, inthalfline.getOrigin()));
                        }
                    } else {
                        intersections.remove(i);
                    }
                    break;
                case LINESEGMENT:
                    LineSegment intsegment = (LineSegment) intgeom;

                    if (Vector.dotProduct(Vector.subtract(_end, _start), Vector.subtract(intsegment._end, intsegment._start)) < 0) {
                        intsegment.reverse();
                    }

                    // five outcomes
                    // 1: this contains intsegment
                    // 2: intsegment contains this
                    // 3: start of this overlaps
                    // 4: end of this overlap
                    // 5: no overlap
                    if (inSlab(intsegment._start)) {
                        if (inSlab(intsegment._end)) {
                            // case 1:
                            // nothing to do
                        } else {
                            // case 4:
                            if (_end.isApproximately(intsegment._start, prec)) {
                                intersections.set(i, intsegment._start);
                            } else {
                                intsegment._end.set(_end);
                            }
                        }
                    } else if (inSlab(intsegment._end)) {
                        // case 3:                    
                        if (_start.isApproximately(intsegment._end, prec)) {
                            intersections.set(i, intsegment._end);
                        } else {
                            intsegment._start.set(_start);
                        }
                    } else if (intsegment.inSlab(_start)) {
                        // case 2:
                        intsegment._start.set(_start);
                        intsegment._end.set(_end);
                    } else {
                        // case 5:
                        intersections.remove(i);
                    }
                    break;
                default:
                    Logger.getLogger(LineSegment.class.getName()).log(Level.SEVERE,
                            "Unexpected geometry type in line segment intersection: {0}",
                            intgeom.getGeometryType());
                    break;
            }
        }
    }
    
    
    public Vector getPointAlongPerimeter(double fraction) {
        return Vector.add(Vector.multiply((1-fraction), _start), Vector.multiply(fraction, _end));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void updateEndpoints(Vector start, Vector end) {

        if (end == null && start == null) {
            return;
        }

        if (start != null) {
            _start.set(start);
        }
        if (end != null) {
            _end.set(end);
        }
    }

    @Override
    public void translate(final double deltaX, final double deltaY) {
        _start.translate(deltaX, deltaY);
        _end.translate(deltaX, deltaY);
    }

    @Override
    public void rotate(final double counterclockwiseangle) {
        _start.rotate(counterclockwiseangle);
        _end.rotate(counterclockwiseangle);
    }

    @Override
    public void scale(final double factorX, final double factorY) {
        _start.scale(factorX, factorY);
        _end.scale(factorX, factorY);
    }

    @Override
    public void reverse() {
        final Vector oldstart = _start;
        _start = _end;
        _end = oldstart;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.LINESEGMENT;
    }

    @Override
    public LineSegment clone() {
        return new LineSegment(_start.clone(), _end.clone());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _start + "," + _end + "]";
    }
    //</editor-fold>


}
