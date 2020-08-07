/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.linear;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * A simple axis-aligned rectangle class, particularly useful for computing
 * bounding boxes. The box may be empty, by infinite boxes are not currently
 * supported.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Rectangle extends CyclicGeometry<Rectangle> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private double _left, _right;
    private double _bottom, _top;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs an empty rectangle.
     */
    public Rectangle() {
        _left = Double.POSITIVE_INFINITY;
        _right = Double.NEGATIVE_INFINITY;
        _bottom = Double.POSITIVE_INFINITY;
        _top = Double.NEGATIVE_INFINITY;
    }

    /**
     * Constructs a rectangle from the given dimensions. Note that setting left
     * higher than right, or bottom higher than top, yields an empty rectangle.
     *
     * @param left X-coordinate of the left side
     * @param right X-coordinate of the right side
     * @param bottom Y-coordinate of the bottom side
     * @param top Y-coordinate of the top side
     */
    public Rectangle(double left, double right, double bottom, double top) {
        _left = left;
        _right = right;
        _bottom = bottom;
        _top = top;
    }

    /**
     * Constructs a rectangle by specifying two diagonal corners.
     *
     * @param pointA one corner
     * @param pointB another corner, diagonal from the first
     * @return the spanned rectangle
     */
    public static Rectangle byCorners(Vector pointA, Vector pointB) {
        return new Rectangle(
                Math.min(pointA.getX(), pointB.getX()),
                Math.max(pointA.getX(), pointB.getX()),
                Math.min(pointA.getY(), pointB.getY()),
                Math.max(pointA.getY(), pointB.getY()));
    }

    /**
     * Constructs a rectangle with a provided corner and a vector pointing from
     * this corner to the diagonally opposite one.
     *
     * @param point a corner of the rectangle
     * @param diagonal the offset of the diagonally opposite corner
     * @return the spanned rectangle
     */
    public static Rectangle byCornerAndDiagonal(Vector point, Vector diagonal) {
        double x1 = point.getX();
        double x2 = point.getX() + diagonal.getX();
        double y1 = point.getY();
        double y2 = point.getY() + diagonal.getY();
        return new Rectangle(Math.min(x1, x2), Math.max(x1, x2), Math.min(y1, y2), Math.max(y1, y2));
    }

    /**
     * Constructs the rectangle from a point with given width and height.
     * Positive width, uses the X-coordinate of the point as the left side;
     * negative width makes it the right side. Similarly, positive height uses
     * the Y-coordinate as the bottom side; negative as top.
     *
     * @param point corner of the rectangle
     * @param width desired width
     * @param height desired height
     * @return the spanned rectangle
     */
    public static Rectangle byCornerAndSize(Vector point, double width, double height) {
        double x1 = point.getX();
        double x2 = point.getX() + width;
        double y1 = point.getY();
        double y2 = point.getY() + height;
        return new Rectangle(Math.min(x1, x2), Math.max(x1, x2), Math.min(y1, y2), Math.max(y1, y2));
    }

    /**
     * Constructs the rectangle from a point with given width and height. Using
     * negative width or height results in an empty rectangle.
     *
     * @param point center of the rectangle
     * @param width desired width
     * @param height desired height
     * @return the spanned rectangle
     */
    public static Rectangle byCenterAndSize(Vector point, double width, double height) {
        double x1 = point.getX() - width / 2.0;
        double x2 = point.getX() + width / 2.0;
        double y1 = point.getY() - height / 2.0;
        double y2 = point.getY() + height / 2.0;
        return new Rectangle(x1, x2, y1, y2);
    }

    /**
     * Computes the smallest enclosing rectangle for the provided geometries.
     *
     * @param include geometries to include in this computation
     * @return smallest enclosing rectangle
     */
    public static Rectangle byBoundingBox(GeometryConvertable... include) {
        return byBoundingBox(Arrays.asList(include));
    }

    /**
     * Computes the smallest enclosing rectangle for the provided geometries.
     *
     * @param include geometries to include in this computation
     * @return smallest enclosing rectangle
     */
    public static Rectangle byBoundingBox(List<? extends GeometryConvertable> include) {
        Rectangle result = new Rectangle();
        result.includeGeometry(include);
        return result;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GET & SET">
    public double getBottom() {
        return _bottom;
    }

    public void setBottom(double bottom) {
        _bottom = bottom;
    }

    public double getLeft() {
        return _left;
    }

    public void setLeft(double left) {
        _left = left;
    }

    public double getRight() {
        return _right;
    }

    public void setRight(double right) {
        _right = right;
    }

    public double getTop() {
        return _top;
    }

    public void setTop(double top) {
        _top = top;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Allows for easy iteration over the four corners of the rectangle. Goes in
     * counterclockwise direction, starting from the left-bottom corner.
     *
     * @return iterable for the corners
     */
    public Iterable<Vector> corners() {
        return new Iterable<Vector>() {

            @Override
            public Iterator<Vector> iterator() {
                return new Iterator<Vector>() {

                    int corner = 0;

                    @Override
                    public boolean hasNext() {
                        return corner < 4;
                    }

                    @Override
                    public Vector next() {
                        corner++;
                        switch (corner) {
                            case 1:
                                return leftBottom();
                            case 2:
                                return rightBottom();
                            case 3:
                                return rightTop();
                            case 4:
                                return leftTop();
                            default:
                                return null;
                        }
                    }
                };
            }
        };
    }

    @Override
    public Vector closestPoint(Vector point) {
        if (isEmpty()) {
            return null;
        }

        if (point.getX() <= _left) {
            // left outside
            if (point.getY() <= _bottom) {
                return leftBottom();
            } else if (point.getY() >= _top) {
                return leftTop();
            } else {
                return new Vector(_left, point.getY());
            }
        } else if (point.getX() >= _right) {
            // right outside
            if (point.getY() <= _bottom) {
                return rightBottom();
            } else if (point.getY() >= _top) {
                return rightTop();
            } else {
                return new Vector(_right, point.getY());
            }
        } else {
            // inside
            double dx;
            if (point.getX() - _left < _right - point.getX()) {
                dx = _left - point.getX();
            } else {
                dx = _right - point.getX();
            }
            double dy;
            if (point.getY() - _bottom < _top - point.getY()) {
                dy = _bottom - point.getY();
            } else {
                dy = _top - point.getY();
            }
            if (dx < dy) {
                return new Vector(point.getX() + dx, point.getY());
            } else {
                return new Vector(point.getX(), point.getY() + dy);
            }
        }
    }

    @Override
    public boolean contains(Vector point, double prec) {
        if (isEmpty()) {
            return false;
        } else {
            return DoubleUtil.inClosedInterval(point.getX(), _left, _right, prec)
                    && DoubleUtil.inClosedInterval(point.getY(), _bottom, _top, prec);
        }
    }

    public boolean containsCompletely(BaseGeometry geom) {
        return containsCompletely(geom, 0);
    }

    public boolean containsCompletely(BaseGeometry geom, double prec) {
        Rectangle R = Rectangle.byBoundingBox(geom);
        return contains(R.leftBottom(), prec) && contains(R.rightBottom());
    }

    public boolean overlaps(Rectangle rect) {
        return overlaps(rect, 0);
    }

    public boolean overlaps(Rectangle rect, double eps) {
        if (_right < rect._left - eps || _left > rect._right + eps) {
            return false;
        }

        if (_top < rect._bottom - eps || _bottom > rect._top + eps) {
            return false;
        }

        return true;
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        if (isEmpty()) {
            return false;
        } else if (DoubleUtil.close(point.getX(), _left, prec)
                || DoubleUtil.close(point.getX(), _right, prec)) {
            return DoubleUtil.inClosedInterval(point.getY(), _bottom, _top, prec);
        } else if (DoubleUtil.close(point.getY(), _bottom, prec)
                || DoubleUtil.close(point.getY(), _top, prec)) {
            return DoubleUtil.inClosedInterval(point.getX(), _left, _right, prec);
        } else {
            return false;
        }
    }

    @Override
    public double perimeter() {
        if (isEmpty()) {
            return 0;
        } else {
            return 2 * (width() + height());
        }
    }

    @Override
    public double areaSigned() {
        if (isEmpty()) {
            return 0;
        } else {
            return width() * height();
        }
    }

    /**
     * Constructs the left-bottom corner of the rectangle. Returns null if the
     * rectangle is empty.
     *
     * @return left-bottom corner
     */
    public Vector leftBottom() {
        if (isEmpty()) {
            return null;
        } else {
            return new Vector(_left, _bottom);
        }
    }

    /**
     * Constructs the left-top corner of the rectangle. Returns null if the
     * rectangle is empty.
     *
     * @return left-top corner
     */
    public Vector leftTop() {
        if (isEmpty()) {
            return null;
        } else {
            return new Vector(_left, _top);
        }
    }

    /**
     * Constructs the right-bottom corner of the rectangle. Returns null if the
     * rectangle is empty.
     *
     * @return right-bottom corner
     */
    public Vector rightBottom() {
        if (isEmpty()) {
            return null;
        } else {
            return new Vector(_right, _bottom);
        }
    }

    /**
     * Constructs the right-top corner of the rectangle. Returns null if the
     * rectangle is empty.
     *
     * @return right-top corner
     */
    public Vector rightTop() {
        if (isEmpty()) {
            return null;
        } else {
            return new Vector(_right, _top);
        }
    }

    /**
     * Constructs the center of the rectangle. Returns null if the rectangle is
     * empty.
     *
     * @return center point
     */
    public Vector center() {
        if (isEmpty()) {
            return null;
        } else {
            return new Vector((_left + _right) / 2.0, (_bottom + _top) / 2.0);
        }
    }

    /**
     * Computes the Y-coordinate of the center. Returns NaN if the rectangle is
     * empty.
     *
     * @return vertical midpoint
     */
    public double verticalCenter() {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            return (_bottom + _top) / 2.0;
        }
    }

    /**
     * Computes the X-coordinate of the center. Returns NaN if the rectangle is
     * empty.
     *
     * @return horizontal midpoint
     */
    public double horizontalCenter() {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            return (_left + _right) / 2.0;
        }
    }

    /**
     * Computes the width of the rectangle. Returns zero for empty rectangles.
     *
     * @return width
     */
    public double width() {
        if (isEmpty()) {
            return 0;
        } else {
            return _right - _left;
        }
    }

    /**
     * Computes the height of the rectangle. Returns zero for empty rectangles.
     *
     * @return height
     */
    public double height() {
        if (isEmpty()) {
            return 0;
        } else {
            return _top - _bottom;
        }
    }

    /**
     * Checks whether the rectangle is empty.
     *
     * @return whether the rectangle is empty
     */
    public boolean isEmpty() {
        return _left > _right || _bottom > _top;
    }

    /**
     * Checks if the rectangle is a singleton, with a precision of
     * DoubleUtil.EPS. Note that this is strictly exclusive with
     * {@link #isEmpty}.
     *
     * @return whether the rectangle is a single point
     */
    public boolean isSingleton() {
        return !isEmpty() && _left >= _right - DoubleUtil.EPS
                && _bottom >= _top - DoubleUtil.EPS;
    }

    @Override
    public void intersect(BaseGeometry other, double prec, List<BaseGeometry> intersections) {
        int presize = intersections.size();
        leftSide().intersect(other, prec, intersections);
        rightSide().intersect(other, prec, intersections);
        topSide().intersect(other, prec, intersections);
        bottomSide().intersect(other, prec, intersections);

        for (int i = intersections.size() - 1; i >= presize; i--) {
            if (intersections.get(i).getGeometryType() == GeometryType.VECTOR) {
                Vector point = (Vector) intersections.get(i);
                for (int j = intersections.size() - 1; j >= presize; j--) {
                    if (i != j && intersections.get(j).onBoundary(point, prec)) {
                        intersections.remove(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Constructs a line segment to represent the left side of the rectangle.
     *
     * @return the left side
     */
    public LineSegment leftSide() {
        if (isEmpty()) {
            return null;
        } else {
            return new LineSegment(leftTop(), leftBottom());
        }
    }

    /**
     * Constructs a line segment to represent the bottom side of the rectangle.
     *
     * @return the bottom side
     */
    public LineSegment bottomSide() {
        if (isEmpty()) {
            return null;
        } else {
            return new LineSegment(leftBottom(), rightBottom());
        }
    }

    /**
     * Constructs a line segment to represent the right side of the rectangle.
     *
     * @return the right side
     */
    public LineSegment rightSide() {
        if (isEmpty()) {
            return null;
        } else {
            return new LineSegment(rightBottom(), rightTop());
        }
    }

    /**
     * Constructs a line segment to represent the top side of the rectangle.
     *
     * @return the top side
     */
    public LineSegment topSide() {
        if (isEmpty()) {
            return null;
        } else {
            return new LineSegment(rightTop(), leftTop());
        }
    }

    /**
     * Constructs a counterclockwise polygon identical to the rectangle. Returns
     * null if the rectangle is empty.
     *
     * @return a new polygon
     */
    public Polygon toPolygon() {
        if (isEmpty()) {
            return null;
        } else {
            return new Polygon(leftTop(), leftBottom(), rightBottom(), rightTop());
        }
    }

    /**
     * Computes the diagonal of the rectangle. Returns zero if the rectangle is
     * empty.
     *
     * @return length of the diagonal
     */
    public double diagonal() {
        if (isEmpty()) {
            return 0;
        } else {
            double w = width();
            double h = height();
            return Math.sqrt(w * w + h * h);
        }
    }
    
    @Override
    public void intersectInterior(BaseGeometry other, double prec, List<BaseGeometry> intersections) {
        throw new UnsupportedOperationException("Interior intersection not yet implemented for Rectangle");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void reverse() {
        // no effect
    }

    /**
     * Sets the width of this rectangle. Area is added equally to the left and
     * right side. If the rectangle is empty, the left and right side will be
     * negative and positive newwidth / 2.0. Note that this may still mean the
     * rectangle is empty afterwards due to the vertical dimension. This way,
     * calling setWidth() and setHeight() consecutively on an empty rectangle
     * ensures that afterwards, the rectangle has the desired size and is
     * centered on the origin.
     *
     * @param newwidth the desired width of the rectangle
     */
    public void setWidth(double newwidth) {
        if (isEmpty()) {
            _left = -newwidth / 2.0;
            _right = newwidth / 2.0;
        } else {
            double currwidth = width();
            double inc = (newwidth - currwidth) / 2.0;
            _left -= inc;
            _right += inc;
        }
    }

    /**
     * Sets the height of this rectangle. Area is added equally to the bottom
     * and top side. If the rectangle is empty, the bottom and top side will be
     * negative and positive newheight / 2.0. Note that this may still mean the
     * rectangle is empty afterwards due to the horizontal dimension. This way,
     * calling setWidth() and setHeight() consecutively on an empty rectangle
     * ensures that afterwards, the rectangle has the desired size and is
     * centered on the origin.
     *
     * @param newheight the desired height of the rectangle
     */
    public void setHeight(double newheight) {
        if (isEmpty()) {
            _bottom = -newheight / 2.0;
            _top = newheight / 2.0;
        } else {
            double currheight = height();
            double inc = (newheight - currheight) / 2.0;
            _bottom -= inc;
            _top += inc;
        }
    }

    /**
     * Performs the minimal extension, such that the rectangle contains point
     * (x,y).
     *
     * @param x X-coordinate to include
     * @param y Y-coordinate to include
     */
    public void include(double x, double y) {
        _left = Math.min(_left, x);
        _right = Math.max(_right, x);
        _bottom = Math.min(_bottom, y);
        _top = Math.max(_top, y);
    }

    /**
     * Performs the minimal extension, such that the rectangle contains all
     * provided points.
     *
     * @param points locations that must be contained by the rectangle
     */
    public void include(Vector... points) {
        include(Arrays.asList(points));
    }

    /**
     * Performs the minimal extension, such that the rectangle contains all
     * provided points.
     *
     * @param points locations that must be contained by the rectangle
     */
    public void include(List<? extends Vector> points) {
        for (Vector v : points) {
            include(v.getX(), v.getY());
        }
    }

    /**
     * Performs the minimal extension, such that the rectangle covers all
     * provided geometries.
     *
     * @param geos geometries to be covered
     */
    public void includeGeometry(GeometryConvertable... geos) {
        includeGeometry(Arrays.asList(geos));
    }

    /**
     * Performs the minimal extension, such that the rectangle covers all
     * provided geometries.
     *
     * @param geos geometries to be covered
     */
    public void includeGeometry(Iterable<? extends GeometryConvertable> geos) {
        for (GeometryConvertable gc : geos) {
            BaseGeometry g = gc.toGeometry();

            switch (g.getGeometryType()) {
                case VECTOR:
                    include((Vector) g);
                    break;
                case LINESEGMENT: {
                    LineSegment ls = (LineSegment) g;
                    include(ls.getStart(), ls.getEnd());
                    break;
                }
                case CIRCLE: {
                    Circle c = (Circle) g;
                    include(c.getCenter().getX() - c.getRadius(), c.getCenter().getY() - c.getRadius());
                    include(c.getCenter().getX() + c.getRadius(), c.getCenter().getY() + c.getRadius());
                    break;
                }
                case CIRCULARARC: {
                    CircularArc ca = (CircularArc) g;

                    include(ca.getStart());
                    include(ca.getEnd());

                    if (ca.getCenter() != null) {
                        double r = ca.radius();
                        Vector dir = new Vector(0, 0);

                        Vector endarm = Vector.subtract(ca.getEnd(), ca.getCenter());
                        endarm.normalize();

                        boolean ccw = ca.isCounterclockwise();
                        double angle = Math.abs(ca.centralAngle(r));

                        dir.set(-1, 0);
                        if ((ccw ? dir.computeClockwiseAngleTo(endarm, false, false) : dir.computeClockwiseAngleTo(endarm, false, false)) < angle) {
                            include(ca.getCenter().getX() - r, ca.getCenter().getY());
                        }

                        dir.set(1, 0);
                        if ((ccw ? dir.computeClockwiseAngleTo(endarm, false, false) : dir.computeClockwiseAngleTo(endarm, false, false)) < angle) {
                            include(ca.getCenter().getX() + r, ca.getCenter().getY());
                        }

                        dir.set(0, -1);
                        if ((ccw ? dir.computeClockwiseAngleTo(endarm, false, false) : dir.computeClockwiseAngleTo(endarm, false, false)) < angle) {
                            include(ca.getCenter().getX(), ca.getCenter().getY() - r);
                        }

                        dir.set(0, 1);
                        if ((ccw ? dir.computeClockwiseAngleTo(endarm, false, false) : dir.computeClockwiseAngleTo(endarm, false, false)) < angle) {
                            include(ca.getCenter().getX(), ca.getCenter().getY() + r);
                        }
                    }
                    break;
                }
                case POLYLINE: {
                    PolyLine p = (PolyLine) g;
                    for (int i = 0; i < p.vertexCount(); i++) {
                        include(p.vertex(i));
                    }
                    break;
                }
                case POLYGON: {
                    Polygon p = (Polygon) g;
                    for (int i = 0; i < p.vertexCount(); i++) {
                        include(p.vertex(i));
                    }
                    break;
                }
                case RECTANGLE: {
                    Rectangle r = (Rectangle) g;
                    include(r.getLeft(), r.getBottom());
                    include(r.getRight(), r.getTop());
                    break;
                }
                case GEOMETRYSTRING:
                    includeGeometry(((GeometryString) g).edges());
                    break;
                case GEOMETRYCYCLE:
                    includeGeometry(((GeometryCycle) g).edges());
                    break;
                case GEOMETRYGROUP:
                    includeGeometry(((GeometryGroup) g).getParts());
                    break;
                case HALFLINE:
                case LINE:
                    Logger.getLogger(Rectangle.class.getName()).log(Level.INFO,
                            "Trying to include infinite geometry ({0}) into a finite bounding box",
                            g.getGeometryType());
                    break;
                default:
                    Logger.getLogger(Rectangle.class.getName()).log(Level.WARNING,
                            "Unexpected geometry type to compute bounding box: {0}",
                            g.getGeometryType());
                    break;
            }
        }
    }

    @Override
    public void translate(double deltaX, double deltaY) {
        _left += deltaX;
        _right += deltaX;
        _bottom += deltaY;
        _top += deltaY;
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        throw new UnsupportedOperationException("Cannot rotate an axis-aligned rectangle over arbitrary angles. Use Polygon instead.");
    }

    @Override
    public void scale(double factorX, double factorY) {
        _left *= factorX;
        _right *= factorX;
        _bottom *= factorY;
        _top *= factorY;
    }

    /**
     * Grows the box in all four directions. Equal values all directions are
     * used.
     *
     * @param d growth in X- and Y-direction
     */
    public void grow(double d) {
        grow(d, d, d, d);
    }

    /**
     * Grows the box in all four directions. Equal values for X- and Y-direction
     * are used.
     *
     * @param dx growth in X-direction
     * @param dy growth in Y-direction
     */
    public void grow(double dx, double dy) {
        grow(dx, dx, dy, dy);
    }

    /**
     * Grows the box in all four directions. Different values for each direction
     * are possible.
     *
     * @param dleft growth to the left
     * @param dright growth to the right
     * @param dbottom growth downward
     * @param dtop growth upward
     */
    public void grow(double dleft, double dright, double dbottom, double dtop) {
        if (isEmpty()) {
            _left = -dleft;
            _right = dright;
            _bottom = -dbottom;
            _top = dtop;
        } else {
            _left -= dleft;
            _right += dright;
            _bottom -= dbottom;
            _top += dtop;
        }
    }

    /**
     * Performs the minimal extension to ensure that the rectangle has the same
     * aspect ratio (defined by width/height) as the provided rectangle. This
     * method has no effect when invoked on an empty rectangle or when an empty
     * rectangle is provided. When providing a singleton rectangle, the aspect
     * ratio 1 is used.
     *
     * @param target rectangle of which the aspect ratio is used as a target
     */
    public void growToAspectRatio(Rectangle target) {
        if (target.isEmpty()) {
            // nothing to do
        } else if (target.isSingleton()) {
            growToAspectRatio(1);
        } else {
            growToAspectRatio(target.width() / target.height());
        }
    }

    /**
     * Performs the minimal extension to ensure that the rectangle has the
     * desired aspect ratio (defined by width/height). This method has no effect
     * on empty rectangles.
     *
     * @param desiredRatio the desired aspect ratio
     */
    public void growToAspectRatio(double desiredRatio) {
        if (isEmpty()) {
            // nothing to do
        } else {
            double currwidth = width();
            double currheight = height();

            if (currwidth / currheight < desiredRatio) {
                setWidth(desiredRatio * currheight);
            } else {
                setHeight(currwidth / desiredRatio);
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.RECTANGLE;
    }

    @Override
    public Rectangle clone() {
        return new Rectangle(_left, _right, _bottom, _top);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _left + "," + _right + "," + _bottom + "," + _top + "]";
    }
    //</editor-fold>
}
