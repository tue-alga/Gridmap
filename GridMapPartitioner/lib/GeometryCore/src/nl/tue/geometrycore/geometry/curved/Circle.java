/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.curved;

import java.util.List;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.HalfLine;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * A simple circle. Note that, although it inherits from CyclicGeometry, it has
 * no clear orientation. Thus, the computed area will always be positive.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Circle extends CyclicGeometry<Circle> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Vector _center;
    private double _radius;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Basic constructor of a circle, by center point and radius.
     *
     * @param center desired center
     * @param radius desired radius
     */
    public Circle(Vector center, double radius) {
        _center = center;
        _radius = radius;
    }

    /**
     * Constructs a circle by a center point and a point on the circle boundary.
     *
     * @param center desired center
     * @param through point through which the circle passes
     * @return the described circle
     */
    public static Circle byThroughPoint(Vector center, Vector through) {
        return new Circle(center, center.distanceTo(through));
    }

    /**
     * Constructs the circle for which the two provided points are diametrical
     * points. In other words, it computes the smallest enclosing circle of the
     * two points.
     *
     * @param diametricA a point on the boundary
     * @param diametricB another point on the boundary
     * @return a circle such that the two points are diametrical
     */
    public static Circle byDiametricPoints(Vector diametricA, Vector diametricB) {
        Vector center = Vector.divide(Vector.add(diametricA, diametricB), 2);
        return new Circle(center, center.distanceTo(diametricA));
    }

    /**
     * Computes the circle that passes through the three given points. Null is
     * returned when the points are collinear.
     *
     * @param throughA first point on the circle boundary
     * @param throughB second point on the circle boundary
     * @param throughC third point on the circle boundary
     * @return the circle through the three points
     */
    public static Circle byThreePoints(Vector throughA, Vector throughB, Vector throughC) {
        Line lineA = Line.bisector(throughA, throughB);
        Line lineB = Line.bisector(throughA, throughC);
        List<BaseGeometry> ints = lineA.intersect(lineB);
        if (ints.size() == 1 && ints.get(0).getGeometryType() == GeometryType.VECTOR) {
            Vector center = (Vector) ints.get(0);
            return byThroughPoint(center, throughA);
        } else {
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public Vector getCenter() {
        return _center;
    }

    public void setCenter(Vector center) {
        _center = center;
    }

    public double getRadius() {
        return _radius;
    }

    public void setRadius(double radius) {
        _radius = radius;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    @Override
    public double areaSigned() {
        return Math.PI * _radius * _radius;
    }

    @Override
    public double perimeter() {
        return 2 * Math.PI * _radius;
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        return DoubleUtil.close(_center.distanceTo(point), _radius, prec);
    }

    @Override
    public double distanceTo(Vector point) {
        return Math.abs(_radius - point.distanceTo(_center));
    }

    @Override
    public boolean contains(Vector point, double prec) {
        return point.distanceTo(_center) <= _radius + prec;
    }

    @Override
    public Vector closestPoint(Vector point) {
        Vector arm = Vector.subtract(point, _center);
        arm.normalize();
        arm.scale(_radius);
        arm.translate(_center);
        return arm;
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
        switch (otherGeom.getGeometryType()) {
            case CIRCLE:
                circleIntersection((Circle) otherGeom, prec, intersections);
                break;
            case LINESEGMENT:
                lineSegmentIntersection((LineSegment) otherGeom, prec, intersections);
                break;
            case HALFLINE:
                halfLineIntersection((HalfLine) otherGeom, prec, intersections);
                break;
            case LINE:
                Line line = (Line) otherGeom;
                lineIntersection(line.getThrough(), line.getDirection(), prec, intersections);
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
        _center.translate(deltaX, deltaY);
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        _center.rotate(counterclockwiseangle);
    }

    @Override
    public void scale(double factorX, double factorY) {
        assert factorX == factorY : "Cannot perform nonuniform scaling on a circle.";

        _center.scale(factorX, factorX);
        _radius *= factorX;
    }

    @Override
    public void reverse() {
        // no effect
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.CIRCLE;
    }

    @Override
    public Circle clone() {
        return new Circle(_center.clone(), _radius);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _center + "," + _radius + "]";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE METHODS">
    private void circleIntersection(Circle circle, double prec, List<BaseGeometry> intersections) {
        Vector dir = Vector.subtract(circle._center, _center);
        double distance = dir.length();

        if (_center.isApproximately(circle._center, prec)
                && DoubleUtil.close(_radius, circle._radius, prec)) {
            intersections.add(clone());
        } else if (distance >= _radius + circle._radius + prec) {
            //too far apart
        } else if (distance < Math.abs(_radius - circle._radius) - prec) {
            // contained
        } else if (DoubleUtil.close(distance, _radius + circle._radius, prec)) {
            // touch
            intersections.add(Vector.add(_center, dir));
        } else {
            // two intersections            
            dir.scale(1.0 / distance);

            double a = (_radius * _radius - circle._radius * circle._radius + distance * distance) / (2.0 * distance);
            double sqrdiff = Math.abs(_radius * _radius - a * a);
            double h;
            if (sqrdiff < prec) {
                h = 0;
            } else {
                h = Math.sqrt(sqrdiff);
            }

            Vector p = new Vector(_center.getX() + dir.getX() * a, _center.getY() + dir.getY() * a);
            dir.rotate90DegreesClockwise();
            dir.scale(h);

            if (h > prec) {
                intersections.add(Vector.add(p, dir));
                dir.invert();
                intersections.add(Vector.add(p, dir));
            } else {
                intersections.add(p);
            }
        }
    }

    private void lineIntersection(Vector through, Vector direction, double prec, List<BaseGeometry> intersections) {
        Vector p = new Line(through, direction).closestPoint(_center);
        double dist = _center.distanceTo(p);
        if (dist > _radius + prec) {
            // disjoint
        } else if (DoubleUtil.close(dist, _radius, prec)) {
            // touch
            intersections.add(p);
        } else {
            // two intersections
            double h = Math.sqrt(_radius * _radius - dist * dist);
            Vector dir = Vector.multiply(h, direction);
            intersections.add(Vector.add(p, dir));
            dir.invert();
            intersections.add(Vector.add(p, dir));
        }
    }

    private void halfLineIntersection(HalfLine halfLine, double prec, List<BaseGeometry> intersections) {
        int presize = intersections.size();
        lineIntersection(halfLine.getOrigin(), halfLine.getDirection(), prec, intersections);
        for (int i = intersections.size() - 1; i >= presize; i--) {
            if (!halfLine.onBoundary((Vector) intersections.get(i), prec)) {
                intersections.remove(i);
            }
        }
    }

    private void lineSegmentIntersection(LineSegment lineSegment, double prec, List<BaseGeometry> intersections) {
        int presize = intersections.size();
        lineIntersection(lineSegment.getStart(), lineSegment.getDirection(), prec, intersections);
        for (int i = intersections.size() - 1; i >= presize; i--) {
            if (!lineSegment.onBoundary((Vector) intersections.get(i), prec)) {
                intersections.remove(i);
            }
        }
    }
    //</editor-fold>
}
