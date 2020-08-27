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
 * A halfline or ray, starting at a certain point, its origin, shooting into a
 * given direction to infinity.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class HalfLine extends BaseGeometry<HalfLine> implements InfiniteGeometry<LineSegment> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Vector _origin;
    private Vector _direction;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">    
    /**
     * Constructs a halfline starting at a point, in a certain direction. Note
     * that the direction need not be normalized.
     *
     * @param origin the halfline starts at this point
     * @param unnormalizedDirection the direction of the halfline
     */
    public HalfLine(Vector origin, Vector unnormalizedDirection) {
        _origin = origin;
        _direction = unnormalizedDirection;
        _direction.normalize();
    }

    /**
     * Computes a line, by providing two points through which it passes. Note
     * that the first provided point (origin) is used to define the line.
     *
     * @param origin startpoint of the halfline
     * @param through the halfline must pass through this point
     * @return constructed halfline
     */
    public static HalfLine byThroughpoint(Vector origin, Vector through) {
        final Vector delta = Vector.subtract(through, origin);
        return new HalfLine(origin, delta);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public Vector getOrigin() {
        return _origin;
    }

    public void setOrigin(Vector origin) {
        _origin = origin;
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
        // (1) no intersections: halfline isnt visible
        // (2) an overlap: halfline is partially visible, but only that what overlaps a side (overlap is a LineSegment)
        // (3) 1 intersection: only visible if the origin is strictly inside the rectangle
        // (4) 2 intersections: halfline crosses view
        List<BaseGeometry> intersections = intersect(rect);
        switch (intersections.size()) {
            default:
                Logger.getLogger(Line.class.getName()).log(Level.SEVERE, "Unexpected number of intersections in clipping: {0}", intersections.size());
            case 0:
                return null;
            case 1:
                if (rect.contains(_origin, -DoubleUtil.EPS)) {
                    return new LineSegment(_origin.clone(), (Vector) intersections.get(0));
                } else {
                    return null;
                }
            case 2:
                // must be two vectors!
                return new LineSegment((Vector) intersections.get(0), (Vector) intersections.get(1));
        }
    }

    /**
     * Returns the vector that is 90 degrees rotated (in counterclockwise
     * direction) from the direction of this halfline. This perpendicular points
     * towards the left side of the halfline.
     *
     * @return the perpendicular
     */
    public Vector perpendicular() {
        Vector perp = _direction.clone();
        perp.rotate90DegreesCounterclockwise();
        return perp;
    }

    /**
     * Determines whether the provided point lies inside the halfspace indicated
     * by this halfline, at precision DoubleUtil.EPS.
     *
     * @param point test point
     * @return whether point is in halfspace
     */
    public boolean inHalfSpace(Vector point) {
        return inHalfSpace(point, DoubleUtil.EPS);
    }

    /**
     * Determines whether the provided point lies inside the halfspace indicated
     * by this halfline, at a given precision. Note that a negative value of
     * precision allows for testing an open halfspace (excluding the boundary).
     *
     * @param point test point
     * @param prec precision
     * @return whether point is in halfspace
     */
    public boolean inHalfSpace(Vector point, double prec) {
        Vector dir = Vector.subtract(point, _origin);
        return Vector.dotProduct(dir, _direction) > -prec;
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        // TODO: this kind of implementation for all geometries?
        return closestPoint(point).isApproximately(point, prec);
//        Vector dir = Vector.subtract(point, _origin);
//        if (Vector.dotProduct(dir, _direction) <= -prec) {
//            // not even in halfspace
//            return false;
//        }
//
//        Vector perp = perpendicular();
//        return Vector.dotProduct(dir, perp) <= prec;
    }

    @Override
    public Vector closestPoint(Vector point) {
        Vector dir = Vector.subtract(point, _origin);
        double dotp = Vector.dotProduct(dir, _direction);
        if (dotp < 0) {
            // not in halfspace
            return _origin;
        } else {
            return Vector.add(_origin, Vector.multiply(dotp, _direction));
        }
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
        // extend to line
        int presize = intersections.size();
        new Line(_origin, _direction).intersect(otherGeom, prec, intersections);

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
                    boolean inhalfspace = inHalfSpace(inthalfline._origin, prec);
                    // NB: is either -1 or 1
                    if (Vector.dotProduct(_direction, inthalfline._direction) < 0) {
                        // oppositely directed
                        if (inhalfspace) {
                            if (_origin.isApproximately(inthalfline._origin, prec)) {
                                intersections.set(i, inthalfline._origin);
                            } else {
                                intersections.set(i, new LineSegment(_origin, inthalfline._origin));
                            }
                        } else {
                            intersections.remove(i);
                        }
                    } else {
                        // same direction
                        if (inhalfspace) {
                            // nothing needs to be done
                        } else {
                            inthalfline._origin.set(_origin);
                        }
                    }
                    break;
                case LINESEGMENT:
                    LineSegment intsegment = (LineSegment) intgeom;

                    boolean startinhalfspace = inHalfSpace(intsegment.getStart());
                    boolean endinhalfspace = inHalfSpace(intsegment.getEnd());

                    if (startinhalfspace && endinhalfspace) {
                        // nothing needs to be done
                    } else if (startinhalfspace) {
                        if (intsegment.getStart().isApproximately(_origin, prec)) {
                            intersections.set(i, intsegment.getStart());
                        } else {
                            intsegment.getEnd().set(_origin);
                        }
                    } else if (endinhalfspace) {
                        if (intsegment.getEnd().isApproximately(_origin, prec)) {
                            intersections.set(i, intsegment.getEnd());
                        } else {
                            intsegment.getStart().set(_origin);
                        }
                    } else {
                        intersections.remove(i);
                    }
                    break;
                default:
                    Logger.getLogger(HalfLine.class.getName()).log(Level.SEVERE,
                            "Unexpected geometry type in halfline intersection: {0}",
                            intgeom.getGeometryType());
                    break;
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void translate(double deltaX, double deltaY) {
        _origin.translate(deltaX, deltaY);
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        _origin.rotate(counterclockwiseangle);
        _direction.rotate(counterclockwiseangle);
    }

    @Override
    public void scale(double factorX, double factorY) {
        _origin.scale(factorX, factorY);
    }

    /**
     * Inverts this HalfLine (rotation by 180 degrees), around its center point.
     */
    public void invert() {
        _direction.invert();
    }

    /**
     * Rotates this HalfLine 90 degrees clockwise, around its center point.
     */
    public void rotate90DegreesClockwise() {
        _direction.rotate90DegreesClockwise();
    }

    /**
     * Rotates this HalfLine 90 degrees counterclockwise, around its center
     * point.
     */
    public void rotate90DegreesCounterclockwise() {
        _direction.rotate90DegreesCounterclockwise();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.HALFLINE;
    }

    @Override
    public HalfLine clone() {
        return new HalfLine(_origin.clone(), _direction.clone());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _origin + "," + _direction + "]";
    }
    //</editor-fold>
}
