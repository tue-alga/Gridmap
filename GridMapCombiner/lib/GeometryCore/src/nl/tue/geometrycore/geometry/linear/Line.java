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
import nl.tue.geometrycore.geometry.InfiniteGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * Infinite line, represented by a point that it passes through and a direction.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Line extends BaseGeometry<Line> implements InfiniteGeometry<LineSegment> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Vector _through;
    private Vector _direction;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs a line that goes through a point, in a certain direction. Note
     * that the direction need not be normalized.
     *
     * @param origin the line passes through this point
     * @param unnormalizedDirection the direction of the line
     */
    public Line(Vector origin, Vector unnormalizedDirection) {
        _through = origin;
        _direction = unnormalizedDirection;
        _direction.normalize();
    }

    /**
     * Computes a line, by providing two points through which it passes. Note
     * that the first provided point is used to define the line.
     *
     * @param throughA first point through which the line passes
     * @param throughB second point through which the line passes
     * @return constructed line
     */
    public static Line byThroughpoints(Vector throughA, Vector throughB) {
        final Vector delta = Vector.subtract(throughB, throughA);
        return new Line(throughA, delta);
    }

    /**
     * Computes the bisector of two points. Note that the provided points are
     * not used to define the line.
     *
     * @param pointA first point to define the bisector
     * @param pointB second point to define the bisector
     * @return constructed line
     */
    public static Line bisector(Vector pointA, Vector pointB) {
        final Vector dir = Vector.subtract(pointB, pointA);
        dir.rotate90DegreesCounterclockwise();

        final Vector through = Vector.add(pointA, pointB);
        through.scale(0.5);

        return new Line(through, dir);
    }

    /**
     * Computes the line that is perpendicular to a given direction, at the
     * specified location. Note that the location is used to define the line.
     *
     * @param location the line passes through this location
     * @param direction the direction, 90 degrees rotated from the desired line
     * @return perpendicular line
     */
    public static Line perpendicularAt(Vector location, Vector direction) {
        final Vector perpendicular = direction.clone();
        perpendicular.rotate90DegreesCounterclockwise();

        return new Line(location, perpendicular);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public Vector getThrough() {
        return _through;
    }

    public void setThrough(Vector through) {
        _through = through;
    }

    public Vector getDirection() {
        return _direction;
    }

    public void setDirection(Vector direction) {
        _direction = direction;
        _direction.normalize();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">    
    @Override
    public LineSegment clip(Rectangle rect) {
        // four outcomes:
        // (1) no intersections: line isnt visible
        // (2) an overlap: line is partially visible, but only that what overlaps a side (overlap is a LineSegment)
        // (3) 1 intersection: line touches a corner, not going to be visibile
        // (4) 2 intersections: line crosses view

        List<BaseGeometry> intersections = intersect(rect);
        switch (intersections.size()) {
            default:
                Logger.getLogger(Line.class.getName()).log(Level.SEVERE, "Unexpected number of intersections in clipping: {0}", intersections.size());
            case 0:
            case 1:
                return null;
            case 2:
                // must be two vectors!
                return new LineSegment((Vector) intersections.get(0), (Vector) intersections.get(1));
        }
    }

    /**
     * Returns the vector that is 90 degrees rotated (in counterclockwise
     * direction) from the direction of this line. This perpendicular points
     * towards the left side of the line.
     *
     * @return the perpendicular
     */
    public Vector perpendicular() {
        Vector perp = _direction.clone();
        perp.rotate90DegreesCounterclockwise();
        return perp;
    }

    /**
     * Determines whether the provided point lies to the left of or on the line,
     * at precision DoubleUtil.EPS.
     *
     * @param point test point
     * @return whether the point is left of the line
     */
    public boolean isLeftOf(Vector point) {
        return isLeftOf(point, DoubleUtil.EPS);
    }

    /**
     * Determines whether the provided point lies to the left of or on the line,
     * at a given precision. Note that a negative precision allows to test,
     * "strictly left of the line".
     *
     * @param point test point
     * @param prec precision
     * @return whether the point is left of the line
     */
    public boolean isLeftOf(Vector point, double prec) {

        Vector dir = Vector.subtract(point, _through);
        Vector perp = perpendicular();

        return Vector.dotProduct(dir, perp) > -prec;
    }

    /**
     * Determines whether the provided point lies to the right of or on the
     * line, at precision DoubleUtil.EPS.
     *
     * @param point test point
     * @return whether the point is right of the line
     */
    public boolean isRightOf(Vector point) {
        return isRightOf(point, DoubleUtil.EPS);
    }

    /**
     * Determines whether the provided point lies to the right of or on the
     * line, at a given precision. Note that a negative precision allows to
     * test, "strictly right of the line".
     *
     * @param point test point
     * @param prec precision
     * @return whether the point is right of the line
     */
    public boolean isRightOf(Vector point, double prec) {
        return !isLeftOf(point, -prec);
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        Vector dir = Vector.subtract(point, _through);
        // TODO: should we normalize? distance from through point should have no effect on precision
        dir.normalize();
        Vector perp = perpendicular();
        return DoubleUtil.close(Vector.dotProduct(dir, perp), 0, prec);
    }

    @Override
    public Vector closestPoint(Vector point) {
        Vector vp = Vector.subtract(point, _through);
        return Vector.add(_through, Vector.multiply(Vector.dotProduct(vp, _direction), _direction));
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
        switch (otherGeom.getGeometryType()) {
            case LINE:
                lineIntersection((Line) otherGeom, prec, intersections);
                break;
            case HALFLINE:
                halflineIntersection((HalfLine) otherGeom, prec, intersections);
                break;
            case LINESEGMENT:
                segmentIntersection((LineSegment) otherGeom, prec, intersections);
                break;
            default:
                otherGeom.intersect(this, prec, intersections);
                break;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">    
    @Override
    public void translate(double deltaX, double deltaY) {
        _through.translate(deltaX, deltaY);
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        _through.rotate(counterclockwiseangle);
        _direction.rotate(counterclockwiseangle);
    }

    @Override
    public void scale(double factorX, double factorY) {
        _through.scale(factorX, factorY);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.LINE;
    }

    @Override
    public Line clone() {
        return new Line(_through.clone(), _direction.clone());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _through + "," + _direction + "]";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">    
    private void lineIntersection(Line otherLine, double prec, List<BaseGeometry> intersections) {

        if (otherLine._direction.isApproximately(_direction, prec)
                || otherLine._direction.isApproximateInverse(_direction, prec)) {
            if (onBoundary(otherLine._through, prec)) {
                // overlapping
                intersections.add(clone());
            } else {
                // parallel - no intersections
            }
        } else {
            // intersection
            double det = Vector.crossProduct(otherLine._direction, _direction);
            double z = (otherLine._direction.getX() * (otherLine._through.getY() - _through.getY())
                    + otherLine._direction.getY() * (_through.getX() - otherLine._through.getX())) / det;
            Vector intersection = new Vector(_through.getX() + _direction.getX() * z, _through.getY() + _direction.getY() * z);
            intersections.add(intersection);
        }
    }

    private void halflineIntersection(HalfLine halfline, double prec, List<BaseGeometry> intersections) {
        int presize = intersections.size();
        lineIntersection(new Line(halfline.getOrigin(), halfline.getDirection()), prec, intersections);
        // at most one intersection!
        if (presize != intersections.size()) {
            BaseGeometry intgeom = intersections.get(presize);
            switch (intgeom.getGeometryType()) {
                case VECTOR:
                    if (!halfline.onBoundary((Vector) intgeom, prec)) {
                        intersections.remove(presize);
                    }
                    break;
                case LINE:
                    intersections.set(presize, halfline.clone());
                    break;
                default:
                    Logger.getLogger(Line.class.getName()).log(Level.SEVERE,
                            "Unexpected geometry type in halfline intersection: {0}",
                            intgeom.getGeometryType());
                    break;
            }
        }
    }

    private void segmentIntersection(LineSegment segment, double prec, List<BaseGeometry> intersections) {
        int presize = intersections.size();
        lineIntersection(Line.byThroughpoints(segment.getStart(), segment.getEnd()), prec, intersections);
        // at most one intersection!
        if (presize != intersections.size()) {
            BaseGeometry intgeom = intersections.get(presize);
            switch (intgeom.getGeometryType()) {
                case VECTOR:
                    if (!segment.onBoundary((Vector) intgeom, prec)) {
                        intersections.remove(presize);
                    }
                    break;
                case LINE:
                    intersections.set(presize, segment.clone());
                    break;
                default:
                    Logger.getLogger(Line.class.getName()).log(Level.SEVERE,
                            "Unexpected geometry type in linesegment intersection: {0}",
                            intgeom.getGeometryType());
                    break;
            }
        }
    }
    //</editor-fold>
}
