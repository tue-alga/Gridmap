/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.curved;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * A circular arc, either in clockwise or counterclockwise direction. Note that
 * this class cannot be used to (perfectly) represent a full circle: when start
 * and endpoint are the same, it will be considered as an arc of length zero.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class CircularArc extends ParameterizedCurve<CircularArc> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private Vector _start, _end, _center;
    private boolean _counterclockwise;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Creates a basic circular arc, provided its center point, start and end
     * point, and whether it should take the clockwise or counterclockwise arc
     * between the endpoints.
     *
     * @param center desired center point
     * @param start startpoint of the arc
     * @param end endpoint of the arc
     * @param counterclockwise direction along the coinciding circle
     */
    public CircularArc(Vector center, Vector start, Vector end, boolean counterclockwise) {
        _center = center;
        _start = start;
        _end = end;
        _counterclockwise = counterclockwise;
    }

    /**
     * Constructs an arc from start to endpoint, by prescribing an initial
     * tangent.
     *
     * @param start startpoint of the arc
     * @param startTangent tangent at the startpoint
     * @param end endpoint of the arc
     * @return the prescribed arc
     */
    public static CircularArc fromStartTangent(Vector start, Vector startTangent, Vector end) {

        boolean ccw = 0 < Vector.crossProduct(
                startTangent.getX(), startTangent.getY(),
                end.getX() - start.getX(), end.getY() - start.getY());

        Line bisec = Line.bisector(start, end);
        Line perpen = Line.perpendicularAt(start, startTangent);

        List<BaseGeometry> ints = bisec.intersect(perpen);
        Vector center;
        if (ints.isEmpty()) {
            center = null;
        } else {
            assert ints.get(0).getGeometryType() == GeometryType.VECTOR;
            center = (Vector) ints.get(0);
        }

        return new CircularArc(center, start, end, ccw);
    }

    /**
     * Constructs an arc from start to endpoint, by prescribing a final tangent.
     *
     * @param start startpoint of the arc
     * @param end endpoint of the arc
     * @param endTangent tangent at the endpoint
     * @return the prescribed arc
     */
    public static CircularArc fromEndTangent(Vector start, Vector end, Vector endTangent) {
        Vector startTangent = Vector.multiply(-1, endTangent);
        CircularArc arc = CircularArc.fromStartTangent(start, startTangent, end);
        arc.reverse();
        return arc;
    }

    /**
     * Constructs an arc from start to endpoint, that goes through the given
     * midpoint. The method assumes that start and end are different, and if mid
     * is collinear with the given start and end, then it is in between these.
     * If these conditions are violated, null is returned.
     *
     * @param start
     * @param mid
     * @param end
     * @return
     */
    public static CircularArc byThroughPoint(Vector start, Vector mid, Vector end) {
        Line lA = Line.bisector(start, mid);
        Line lB = Line.bisector(mid, end);

        List<BaseGeometry> intersect = lA.intersect(lB);
        if (intersect.isEmpty()) {
            if (start.squaredDistanceTo(mid) + mid.squaredDistanceTo(end) <= start.squaredDistanceTo(end)) {
                return new CircularArc(null, start, end, true);
            } else {
                return null;
            }
        } else if (intersect.get(0).getGeometryType() == GeometryType.VECTOR) {
            Vector dirEnd = Vector.subtract(end, start);
            Vector dirMid = Vector.subtract(mid, start);

            Vector i = (Vector) intersect.get(0);
            return new CircularArc(i, start, end, Vector.crossProduct(dirMid, dirEnd) >= 0);
        } else {
            // start == end
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

    @Override
    public Vector getEnd() {
        return _end;
    }

    @Override
    public Vector getStart() {
        return _start;
    }

    public void setStart(Vector start) {
        _start = start;
    }

    public void setEnd(Vector end) {
        _end = end;
    }

    public boolean isCounterclockwise() {
        return _counterclockwise;
    }

    public void setCounterclockwise(boolean counterclockwise) {
        _counterclockwise = counterclockwise;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    @Override
    public Vector getStartTangent() {
        Vector direction;
        if (_center == null) {
            direction = Vector.subtract(_end, _start);
        } else {
            direction = Vector.subtract(_start, _center);
            if (_counterclockwise) {
                direction.rotate90DegreesCounterclockwise();
            } else {
                direction.rotate90DegreesClockwise();
            }
        }
        direction.normalize();
        return direction;
    }

    @Override
    public Vector getEndTangent() {
        Vector direction;
        if (_center == null) {
            direction = Vector.subtract(_end, _start);
        } else {
            direction = Vector.subtract(_end, _center);
            if (_counterclockwise) {
                direction.rotate90DegreesCounterclockwise();
            } else {
                direction.rotate90DegreesClockwise();
            }
        }
        direction.normalize();
        return direction;
    }

    @Override
    public double getMinimumParameter() {
        return 0;
    }

    @Override
    public double getMaximumParameter() {
        return 1;
    }

    @Override
    public Vector getPointAt(double t) {
        if (_center == null) {
            Vector v = Vector.subtract(_end, _start);
            v.scale(t);
            v.translate(_start);
            return v;
        } else {
            Vector v = Vector.subtract(_start, _center);
            double a = centralAngle();
            v.rotate(t * a);
            v.translate(_center);
            return v;
        }
    }

    /**
     * Returns a normalized vector, giving the direction from center to
     * startpoint. In case center is null (a line segment), the normal is
     * generated, depending on whether the line segment is "clockwise" or
     * "counterclockwise".
     *
     * @return normalized arm
     */
    public Vector getStartArm() {
        Vector arm;
        if (_center == null) {
            arm = Vector.subtract(_end, _start);
            if (_counterclockwise) {
                arm.rotate90DegreesClockwise();
            } else {
                arm.rotate90DegreesCounterclockwise();
            }
        } else {
            arm = Vector.subtract(_start, _center);
        }
        arm.normalize();
        return arm;
    }

    /**
     * Returns a normalized vector, giving the direction from center to
     * endpoint. In case center is null (a line segment), the normal is
     * generated, depending on whether the line segment is "clockwise" or
     * "counterclockwise".
     *
     * @return normalized arm
     */
    public Vector getEndArm() {
        Vector arm;
        if (_center == null) {
            arm = Vector.subtract(_end, _start);
            if (_counterclockwise) {
                arm.rotate90DegreesClockwise();
            } else {
                arm.rotate90DegreesCounterclockwise();
            }
        } else {
            arm = Vector.subtract(_end, _center);
        }
        arm.normalize();
        return arm;
    }

    /**
     * Computes the radius of the circle. Yields infinity if center is null.
     *
     * @return radius of the arc
     */
    public double radius() {
        if (_center == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return _start.distanceTo(_center);
        }
    }

    /**
     * Computes the central angle for the arc. Positive values indicate a
     * counterclockwise arc; clockwise arcs have a negative central angle.
     *
     * @return signed central angle
     */
    public double centralAngle() {
        return centralAngle(radius());
    }

    /**
     * Computes the central angle for the arc. Positive values indicate a
     * counterclockwise arc; clockwise arcs have a negative central angle.
     *
     * @param radius the radius of this arc
     * @return signed central angle
     */
    public double centralAngle(double radius) {
        if (_center == null) {
            return 0;
        }

        double xStart = (_start.getX() - _center.getX()) / radius;
        double yStart = (_start.getY() - _center.getY()) / radius;

        double xEnd = (_end.getX() - _center.getX()) / radius;
        double yEnd = (_end.getY() - _center.getY()) / radius;

        double angle = Math.asin(DoubleUtil.clipValue(
                Vector.crossProduct(xStart, yStart, xEnd, yEnd), -1, 1));

        // angle is now in range [-pi,pi]        
        // check if we must make adjustments via dot product and angle
        if (Vector.dotProduct(xStart, yStart, xEnd, yEnd) < 0) {
            angle = Math.PI - angle;
        } else if (angle < 0) {
            angle = 2 * Math.PI + angle;
        }

        // now we have the CCW angle from start to end
        if (_counterclockwise) {
            return angle;
        } else {
            // arc is the complement: central angle is (2pi - angle)
            // and multiply by -1 as its clockwise!
            return angle - 2 * Math.PI;
        }
    }

    @Override
    public double areaSigned() {
        if (_center == null) {
            return 0;
        } else {
            return areaSigned(radius(), centralAngle());
        }
    }

    /**
     * Speed-up for computing the signed area.
     *
     * @see #areaSigned
     * @param radius radius of this arc
     * @param centralangle central angle of this arc
     * @return signed area of the arc
     */
    public double areaSigned(double radius, double centralangle) {
        if (_center == null) {
            return 0;
        } else {
            centralangle = Math.abs(centralangle);
            double area = 0.5 * radius * radius * (centralangle - Math.sin(centralangle));

            if (_counterclockwise) {
                return area;
            } else {
                return -area;
            }
        }
    }

    /**
     * Computes the signed area of the sector. Positive values indicate
     * counterclockwise arcs.
     *
     * @return signed sector area
     */
    public double sectorAreaSigned() {
        if (_center == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return centralAngle() * radius();
        }
    }

    /**
     * Computes the signed area of the segment. Positive values indicate
     * counterclockwise arcs.
     *
     * @return signed sector area
     */
    public double segmentAreaSigned() {
        if (_center == null) {
            return 0;
        } else {
            double rad = radius();
            double ca = centralAngle();
            return 0.5 * rad * rad * (ca - Math.sin(ca));
        }
    }

    @Override
    public double perimeter() {
        if (_center == null) {
            return _start.distanceTo(_end);
        } else {
            return centralAngle() * radius();
        }
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        if (_center == null) {
            return new LineSegment(_start, _end).onBoundary(point, prec);
        } else if (!DoubleUtil.close(_center.distanceTo(point), radius())) {
            return false;
        }

        //test in sector extended to inf
        Vector toEnd = Vector.subtract(_end, _center);
        Vector toStart = Vector.subtract(_start, _center);
        Vector toPoint = Vector.subtract(point, _center);

        double angleToEnd = toStart.computeCounterClockwiseAngleTo(toEnd);
        double angleToPoint = toStart.computeCounterClockwiseAngleTo(toPoint);

        return (angleToPoint <= angleToEnd) == _counterclockwise;
    }

    /**
     * Determines whether the provided point lies in the sector described by
     * this arc, at precision DoubleUtil.EPS. Note that for arcs with center =
     * null, the sector is defined to be above or below the chord, depending on
     * whether it is counterclockwise or not.
     *
     * @param point test point
     * @return whether the point is inside the sector
     */
    public boolean inSector(Vector point) {
        return inSector(point, DoubleUtil.EPS);
    }

    /**
     * Determines whether the provided point lies in the sector described by
     * this arc, at a given precision. Note that for arcs with center = null,
     * the sector is defined to be above or below the chord, depending on
     * whether it is counterclockwise or not.
     *
     * @param point test point
     * @param prec precision
     * @return whether the point is inside the sector
     */
    public boolean inSector(Vector point, double prec) {
        if (!inInfiniteSector(point, prec)) {
            return false;
        }

        if (_center == null) {
            if (_counterclockwise) {
                // must be left of chordal line                
                return Line.byThroughpoints(_start, _end).isLeftOf(point, prec);
            } else {
                // must be right of chordal line
                return Line.byThroughpoints(_start, _end).isRightOf(point, prec);
            }
        } else {
            return radius() < point.distanceTo(_center) + prec;
        }
    }

    /**
     * Determines whether the provided point lies in the infinite sector
     * described by this arc, at precision DoubleUtil.EPS. If center is null,
     * this becomes the infinite slab spanned by the line segment.
     *
     * @param point test point
     * @return whether the point is inside the sector
     */
    public boolean inInfiniteSector(Vector point) {
        return inInfiniteSector(point, DoubleUtil.EPS);
    }

    /**
     * Determines whether the provided point lies in the infinite sector
     * described by this arc, at a given precision. If center is null, this
     * becomes the infinite slab spanned by the line segment.
     *
     * @param point test point
     * @param prec precision
     * @return whether the point is inside the sector
     */
    public boolean inInfiniteSector(Vector point, double prec) {
        if (_center == null) {
            Vector dir = Vector.subtract(point, _start);
            Vector chorddir = Vector.subtract(_end, _start);
            double length = chorddir.length();
            chorddir.scale(1.0 / length);

            double dotp = Vector.dotProduct(dir, chorddir);
            return DoubleUtil.inClosedInterval(dotp, 0, length, prec);
        } else {
            Vector arm = Vector.subtract(point, _center);
            arm.normalize();

            double angle = _counterclockwise
                    ? getStartArm().computeCounterClockwiseAngleTo(arm, false, false)
                    : getStartArm().computeClockwiseAngleTo(arm, false, false);

            if (angle > 2 * Math.PI - prec) {
                return true;
            }

            double centralangle = Math.abs(centralAngle());
            return angle < centralangle + prec;
        }
    }

    @Override
    public Vector closestPoint(Vector point) {
        if (inInfiniteSector(point)) {
            // same as for circle
            Vector arm = Vector.subtract(point, _center);
            arm.normalize();
            arm.scale(radius());
            arm.translate(_center);
            return arm;
        } else {
            // one of endpoints is closest
            if (point.distanceTo(_start) < point.distanceTo(_end)) {
                return _start;
            } else {
                return _end;
            }
        }
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
        if (_center == null) {
            // "cast" to line segment
            new LineSegment(_start, _end).intersect(otherGeom, prec, intersections);
        } else {
            // extend to circle
            int presize = intersections.size();
            new Circle(_center, radius()).intersect(otherGeom, prec, intersections);

            for (int i = intersections.size() - 1; i >= presize; i--) {
                BaseGeometry intgeom = intersections.get(i);

                switch (intgeom.getGeometryType()) {
                    case VECTOR:
                        if (!onBoundary((Vector) intgeom, prec)) {
                            intersections.remove(i);
                        }
                        break;
                    case CIRCLE:
                        intersections.set(i, this.clone());
                        break;
                    case CIRCULARARC:

                        CircularArc intarc = (CircularArc) intgeom;
                        // strict overlap. So center point of intarc is same!
                        // just have to find out what portion of intarc lies on this arc.
                        if (intarc._counterclockwise != _counterclockwise) {
                            intarc.reverse();
                        }
                        // now arcs have the same orientation

                        Vector startarm = getStartArm();
                        Vector endarm = getEndArm();
                        Vector intstartarm = intarc.getStartArm();
                        Vector intendarm = intarc.getEndArm();

                        double angle_end = _counterclockwise
                                ? startarm.computeCounterClockwiseAngleTo(endarm, false, false)
                                : startarm.computeClockwiseAngleTo(endarm, false, false);

                        double angle_intstart = _counterclockwise
                                ? startarm.computeCounterClockwiseAngleTo(intstartarm, false, false)
                                : startarm.computeClockwiseAngleTo(intstartarm, false, false);

                        double angle_intend = _counterclockwise
                                ? startarm.computeCounterClockwiseAngleTo(intendarm, false, false)
                                : startarm.computeClockwiseAngleTo(intendarm, false, false);

                        // there are 6 cases
                        // 1. intarc is fully overlaps this arc
                        //    start -> end -> intend -> intstart
                        // 2. intarc is fully contained in this arc
                        //    start -> intstart -> intend -> end
                        // 3. intarc is fully disjoint from this arc
                        //    start -> end -> intstart -> intend
                        // 4. intarc's end overlaps with this arc's start
                        //    start -> intend -> end -> intstart
                        // 5. intarc's start overlaps with this arc's end
                        //    start -> intstart -> end -> intend
                        // 6: both 4 and 5 apply
                        //    start -> intend -> intstart -> end
                        if (angle_intstart < angle_intend) {
                            // start -> intstart -> intend
                            if (angle_end < angle_intstart) {
                                // start -> end -> intstart -> intend
                                // case 3
                                // fully disjoint
                                intersections.remove(i);
                                if (intarc._start.isApproximately(_end, prec)) {
                                    intersections.add(intarc._start);
                                }
                                if (intarc._end.isApproximately(_start, prec)) {
                                    intersections.add(intarc._end);
                                }
                            } else if (angle_end < angle_intend) {
                                // start -> intstart -> end -> intend                            
                                // case 5 end overlap
                                if (intarc._start.isApproximately(_end, prec)) {
                                    intersections.set(i, intarc._start);
                                } else {
                                    intarc._end.set(_end);
                                }
                            } else {
                                // start -> intstart -> intend -> end
                                // case 2: full containment
                                // no need to update!                            
                            }
                        } else {
                            // start -> intend -> intstart
                            if (angle_end < angle_intend) {
                                // start -> end -> intend -> intstart
                                // case 1
                                intarc._start.set(_start);
                                intarc._end.set(_end);
                            } else if (angle_end < angle_intstart) {
                                // start -> intend -> end -> intstart  
                                // case 4: start overlap
                                if (_start.isApproximately(intarc._end, prec)) {
                                    intersections.set(i, intarc._end);
                                } else {
                                    intarc._start.set(_start);
                                }
                            } else {
                                // start -> intend -> intstart -> end
                                // case 6: both ends overlap
                                CircularArc intclone = intarc.clone();
                                if (_start.isApproximately(intarc._end, prec)) {
                                    intersections.set(i, intarc._end);
                                } else {
                                    intarc._start.set(_start);
                                }

                                if (intclone._start.isApproximately(_end, prec)) {
                                    intersections.add(intclone._start);
                                } else {
                                    intclone._end.set(_end);
                                    intersections.add(intclone);
                                }
                            }
                        }
                        break;
                    default:
                        Logger.getLogger(CircularArc.class.getName()).log(Level.SEVERE,
                                "Unexpected geometry type in circular arc intersection: {0}",
                                intgeom.getGeometryType());
                        break;
                }
            }
        }
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

        // if it is straight, maintain it straight
        if (_center != null) {
            // compute closest center point that makes this circular arc valid
            // NB, must be a point on the bisector of start/end
            double dx = _start.getY() - _end.getY();
            double dy = _end.getX() - _start.getX();
            double dlen = Math.sqrt(dx * dx + dy * dy);
            dx /= dlen;
            dy /= dlen;
            // vector d = (dx,dy) is the normalized direction of bisector
            double px = (_start.getX() + _end.getX()) / 2.0;
            double py = (_start.getY() + _end.getY()) / 2.0;
            // vector p = (px,py) is a point on bisector

            // closest point c = (cx,cy) on bisector: c = p + (center - p) . d
            double cx = px + (_center.getX() - px) * dx;
            double cy = py + (_center.getY() - py) * dy;

            _center.set(cx, cy);
        }
    }

    @Override
    public void translate(double deltaX, double deltaY) {
        _start.translate(deltaX, deltaY);
        _end.translate(deltaX, deltaY);
        if (_center != null) {
            _center.translate(deltaX, deltaY);
        }
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        _start.rotate(counterclockwiseangle);
        _end.rotate(counterclockwiseangle);
        if (_center != null) {
            _center.rotate(counterclockwiseangle);
        }
    }

    @Override
    public void scale(double factorX, double factorY) {
        _start.scale(factorX, factorY);
        _end.scale(factorX, factorY);
        if (_center != null) {
            _center.scale(factorX, factorY);
        }
    }

    @Override
    public void reverse() {
        Vector t = _start;
        _start = _end;
        _end = t;
        _counterclockwise = !_counterclockwise;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.CIRCULARARC;
    }

    @Override
    public CircularArc clone() {
        return new CircularArc(_center == null ? null : _center.clone(), _start.clone(), _end.clone(), _counterclockwise);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _center + "," + _start + "," + _end + "," + _counterclockwise + "]";
    }
    //</editor-fold>
}
