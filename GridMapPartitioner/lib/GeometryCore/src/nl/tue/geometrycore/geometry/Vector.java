/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry;

import java.util.List;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * This class is used to represent a 2-dimensional point or vector in Euclidean
 * space.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Vector extends BaseGeometry<Vector> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private double _x;
    private double _y;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs a new vector with given X- and Y-coordinate
     *
     * @param x the X-coordinate
     * @param y the Y-coordinate
     */
    public Vector(double x, double y) {
        _x = x;
        _y = y;
    }

    /**
     * Clone constructor.
     *
     * @param clone coordinates to be cloned
     */
    public Vector(Vector clone) {
        _x = clone._x;
        _y = clone._y;
    }

    /**
     * Creates a vector(0,0).
     *
     * @return a new vector at position (0,0)
     */
    public static Vector origin() {
        return new Vector(0, 0);
    }

    /**
     * Creates a vector representing the up-direction.
     *
     * @return a new vector at position (0,1)
     */
    public static Vector up() {
        return new Vector(0, 1);
    }
    
    /**
     * Creates a vector representing the up-direction.
     *
     * @param length length of the desired vector
     * @return a new vector at position (0,length)
     */
    public static Vector up(double length) {
        return new Vector(0, length);
    }

    /**
     * Creates a vector representing the down-direction.
     *
     * @return a new vector at position (0,-1)
     */
    public static Vector down() {
        return new Vector(0, -1);
    }
    
    /**
     * Creates a vector representing the down-direction.
     *
     * @param length length of the desired vector
     * @return a new vector at position (0,-1)
     */
    public static Vector down(double length) {
        return new Vector(0, -length);
    }

    /**
     * Creates a vector representing the left-direction.
     *
     * @return a new vector at position (-1,0)
     */
    public static Vector left() {
        return new Vector(-1, 0);
    }
    
    /**
     * Creates a vector representing the left-direction.
     *
     * @param length length of the desired vector
     * @return a new vector at position (-length,0)
     */
    public static Vector left(double length) {
        return new Vector(-length, 0);
    }

    /**
     * Creates a vector representing the right-direction.
     *
     * @return a new vector at position (1,0)
     */
    public static Vector right() {
        return new Vector(1, 0);
    }
    
    /**
     * Creates a vector representing the right-direction.
     *
     * @param length length of the desired vector
     * @return a new vector at position (1,0)
     */
    public static Vector right(double length) {
        return new Vector(length, 0);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public double getX() {
        return _x;
    }

    public void setX(double x) {
        _x = x;
    }

    public double getY() {
        return _y;
    }

    public void setY(double y) {
        _y = y;
    }

    /**
     * Copies the position of the provided Vector to this Vector.
     *
     * @param vector vector to be copied
     */
    public void set(Vector vector) {
        _x = vector._x;
        _y = vector._y;
    }

    /**
     * Sets both coordinates of this Vector at the same time
     *
     * @param x the new X-coordinate
     * @param y the new Y-coordinate
     */
    public void set(double x, double y) {
        _x = x;
        _y = y;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    public double length() {
        return Math.sqrt(_x * _x + _y * _y);
    }

    public double squaredLength() {
        return _x * _x + _y * _y;
    }

    @Override
    public double distanceTo(Vector other) {
        final double dx = other._x - _x;
        final double dy = other._y - _y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double squaredDistanceTo(Vector other) {
        final double dx = other._x - _x;
        final double dy = other._y - _y;
        return dx * dx + dy * dy;
    }

    @Override
    public Vector closestPoint(Vector point) {
        return this;
    }

    /**
     * Angle in range of [-pi,pi]. Positive angle is associated with
     * counterclockwise.
     *
     * @param other
     * @return
     */
    public double computeSignedAngleTo(Vector other) {
        return computeSignedAngleTo(other, true, true);
    }

    public double computeSignedAngleTo(Vector other, boolean normalizeThis, boolean normalizeOther) {
        double xThis, yThis;
        if (normalizeThis) {
            double len = length();
            xThis = _x / len;
            yThis = _y / len;
        } else {
            xThis = _x;
            yThis = _y;
        }

        double xOther, yOther;
        if (normalizeOther) {
            double len = other.length();
            xOther = other._x / len;
            yOther = other._y / len;
        } else {
            xOther = other._x;
            yOther = other._y;
        }

        int sign = 1;
        if (Vector.crossProduct(xThis, yThis, xOther, yOther) < 0) {
            sign = -1;
        }

        return sign * Math.acos(DoubleUtil.clipValue(
                Vector.dotProduct(xThis, yThis, xOther, yOther),
                -1, 1));
    }

    public double computeClockwiseAngleTo(Vector other) {
        return 2 * Math.PI - this.computeCounterClockwiseAngleTo(other, true, true);
    }

    public double computeClockwiseAngleTo(Vector other, boolean normalizeThis, boolean normalizeOther) {
        return 2 * Math.PI - this.computeCounterClockwiseAngleTo(other, normalizeThis, normalizeOther);
    }

    public double computeCounterClockwiseAngleTo(Vector other) {
        return computeCounterClockwiseAngleTo(other, true, true);
    }

    public double computeCounterClockwiseAngleTo(Vector other, boolean normalizeThis, boolean normalizeOther) {

        double angle = computeSignedAngleTo(other, normalizeThis, normalizeOther);

        // angle is now in range [-pi,pi]    
        // range [0,pi] is OK, but [-pi,0] must be mapped to [pi,2pi]
        // check if we must make adjustments via dot product and angle
        if (angle < 0) {
            angle = 2 * Math.PI + angle;
        }

        return angle;
    }

    public boolean isApproximately(Vector other) {
        return isApproximately(other, DoubleUtil.EPS);
    }

    public boolean isApproximately(Vector other, double prec) {
        return DoubleUtil.close(_x, other._x, prec) && DoubleUtil.close(_y, other._y, prec);
    }

    public boolean isEqual(Vector other) {
        return _x == other._x && _y == other._y;
    }

    public boolean isInverse(Vector other) {
        return other._x == -_x && other._y == -_y;
    }

    public boolean isApproximateInverse(Vector other) {
        return isApproximateInverse(other, DoubleUtil.EPS);
    }

    public boolean isApproximateInverse(Vector other, double prec) {
        return DoubleUtil.close(other._x, -_x, prec) && DoubleUtil.close(other._y, -_y, prec);
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        return isApproximately(point, prec);
    }

    @Override
    public void intersect(BaseGeometry other, double prec, List<BaseGeometry> intersections) {
        if (other.onBoundary(this, prec)) {
            intersections.add(clone());
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void translate(double deltaX, double deltaY) {
        _x += deltaX;
        _y += deltaY;
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        final double newx = _x * Math.cos(counterclockwiseangle) - _y * Math.sin(counterclockwiseangle);
        _y = _x * Math.sin(counterclockwiseangle) + _y * Math.cos(counterclockwiseangle);
        _x = newx;
    }

    @Override
    public void scale(double factorX, double factorY) {
        _x *= factorX;
        _y *= factorY;
    }

    /**
     * Scales this Vector to have length 1.
     */
    public void normalize() {
        final double len = Math.sqrt(_x * _x + _y * _y);
        _x /= len;
        _y /= len;
    }

    /**
     * Inverts this Vector (rotation by 180 degrees).
     */
    public void invert() {
        _x = -_x;
        _y = -_y;
    }

    /**
     * Rotates this Vector 90 degrees clockwise.
     */
    public void rotate90DegreesClockwise() {
        final double oldx = _x;
        _x = _y;
        _y = -oldx;
    }

    /**
     * Rotates this Vector 90 degrees counterclockwise.
     */
    public void rotate90DegreesCounterclockwise() {
        final double oldx = _x;
        _x = -_y;
        _y = oldx;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.VECTOR;
    }

    @Override
    public Vector clone() {
        return new Vector(_x, _y);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _x + " " + _y + "]";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="VECTOR COMPUTATION (STATIC)">
    /**
     * Performs standard Vector addition, creating a new Vector.
     *
     * @param a
     * @param b
     * @return new Vector representing a+b
     */
    public static Vector add(final Vector a, final Vector b) {
        return new Vector(a._x + b._x, a._y + b._y);
    }
    
    /**
     * Performs standard Vector addition of a sequence of vectors, creating a new Vector.
     *
     * @param vs array of zero or more vectors
     * @return new Vector representing addition of all vectors in vs
     */
    public static Vector addSeq(final Vector... vs) {
        Vector r = Vector.origin();
        for (Vector v : vs) {
            r.translate(v);
        }
        return r;
    }

    /**
     * Performs standard Vector subtraction, creating a new Vector.
     *
     * @param a
     * @param b
     * @return new Vector representing a-b
     */
    public static Vector subtract(final Vector a, final Vector b) {
        return new Vector(a._x - b._x, a._y - b._y);
    }

    /**
     * Performs standard Vector dot product.
     *
     * @param a
     * @param b
     * @return dot product of a and b
     */
    public static double dotProduct(Vector a, Vector b) {
        return a._x * b._x + a._y * b._y;
    }

    /**
     * Performs standard Vector dot product.
     *
     * @param aX
     * @param aY
     * @param bX
     * @param bY
     * @return dot product of (aX, aY) and (bX, bY)
     */
    public static double dotProduct(double aX, double aY, double bX, double bY) {
        return aX * bX + aY * bY;
    }

    /**
     * Performs standard Vector cross product, creating a new Vector.
     *
     * @param a
     * @param b
     * @return cross product of a and b
     */
    public static double crossProduct(final Vector a, final Vector b) {
        return a._x * b._y - a._y * b._x;
    }

    /**
     * Performs standard Vector cross product.
     *
     * @param aX
     * @param aY
     * @param bX
     * @param bY
     * @return new Vector representing cross product of a and b
     */
    public static double crossProduct(double aX, double aY, double bX, double bY) {
        return aX * bY - aY * bX;
    }

    /**
     * Performs standard Number-Vector multiplication (scaling), creating a new
     * Vector.
     *
     * @param factor
     * @param a
     * @return new Vector representing factor * a
     */
    public static Vector multiply(double factor, Vector a) {
        return new Vector(factor * a._x, factor * a._y);
    }

    /**
     * Performs standard Vector-Number division (1/scaling), creating a new
     * Vector.
     *
     * @param a
     * @param factor
     * @return new Vector representing a/factor
     */
    public static Vector divide(Vector a, double factor) {
        return new Vector(a._x / factor, a._y / factor);
    }

    /**
     * Performs a counterclockwise rotation on the given vector.
     *
     * @param a
     * @param angle
     * @return new Vector representing the rotated input
     */
    public static Vector rotate(Vector a, double angle) {
        Vector r = a.clone();
        r.rotate(angle);
        return r;
    }

    /**
     * Find to factors, f and g, such that f * d1 + g * d2 = s.
     *
     * @param d1
     * @param d2
     * @param s
     * @return array with two values {f,g}
     */
    public static double[] solveVectorAddition(Vector d1, Vector d2, Vector s) {

        double sx = s.getX();
        double sy = s.getY();
        double d1x = d1.getX();
        double d1y = d1.getY();
        double d2x = d2.getX();
        double d2y = d2.getY();

        // e.g. find f and g such that:
        // sdx = f * d1x + g * d2x
        // sdy = f * d1y + g * d2y
        double f, g;

        // g * d2y = sy - f * d1y
        // NB division by 0!
        if (DoubleUtil.close(d2y, 0)) {
            // d2y == 0
            // f * d1y = sy
            // NB: d1y cant be zero, as that would result in two parallel directions

            f = sy / d1y;

            // now, solve for g, using f, knowing that d2x != 0
            // sx = f * d1x + g * d2x hence yields:
            g = (sx - f * d1x) / d2x;
        } else {
            // g = (sy - f * d1y) / d2y
            // subsituting in first equation:
            // sx = f * d1x + ((sy - f * d1y) / d2y) * d2x
            // rewriting:
            // f (d1x - d1y * (d2x / d2y)) = sx - (sy / d2y) * d2x

            // can d1x - d1y * (d2x / d2y) = 0 hold?
            // == d2x / d2y = d1x / d1y
            // hence, this will only be zero if the directions d1 and d2 are
            // equal (which cannot occur)
            f = (sx - (sy / d2y) * d2x) / (d1x - d1y * (d2x / d2y));

            // now, solve for g, using f, knowing that d2y != 0
            // sy = f * d1y + g * d2y hence yields:
            g = (sy - f * d1y) / d2y;
        }

        return new double[]{f, g};
    }
    //</editor-fold>
}
