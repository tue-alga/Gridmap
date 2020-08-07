/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.CyclicGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 * Class to represent a single (typically simple) polygon as a sequence of
 * vertices. Polygon cannot contain holes.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Polygon extends CyclicGeometry<Polygon> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final List<Vector> _vertices;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs an empty polygon.
     */
    public Polygon() {
        _vertices = new ArrayList();
    }

    /**
     * Constructs a polygon from the provided vertices.
     *
     * @param vertices corner points of the polygon
     */
    public Polygon(Vector... vertices) {
        this();
        _vertices.addAll(Arrays.asList(vertices));
    }

    /**
     * Constructs a polygon from the provided vertices.
     *
     * @param vertices corner points of the polygon
     */
    public Polygon(List<Vector> vertices) {
        _vertices = vertices;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">    
    /**
     * Returns the number of vertices defining this polygonal line.
     *
     * @return number of vertices
     */
    public int vertexCount() {
        return _vertices.size();
    }

    /**
     * Returns the vertex at the specified index. Note that the index is treated
     * circularly.
     *
     * @param index position of the desired vertex
     * @return vertex at given index
     */
    public Vector vertex(int index) {
        return _vertices.get(index(index));
    }

    /**
     * Returns the number of edges of this polyline. This is equal to the number
     * of vertices minus one.
     *
     * @return edge count
     */
    public int edgeCount() {
        return _vertices.size();
    }

    /**
     * Constructs the edge between vertex at the specified index and the index
     * after. Value of index must lie in between 0 and the number of edges. Note
     * that the index is treated circularly.
     *
     * @param index edge index
     * @return newly constructed edge
     */
    public LineSegment edge(int index) {
        return new LineSegment(vertex(index), vertex(index + 1));
    }

    @Override
    public Vector closestPoint(Vector point) {
        Vector result = null;
        double distance = Double.POSITIVE_INFINITY;
        for (LineSegment edge : edges()) {
            Vector closest = edge.closestPoint(point);
            double d = closest.squaredDistanceTo(point);
            if (d < distance) {
                result = closest;
                distance = d;
            }
        }
        return result;
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        for (LineSegment edge : edges()) {
            if (edge.onBoundary(point, prec)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double areaSigned() {
        double total = 0;
        Vector prev = vertex(-1);
        for (int i = 0; i < _vertices.size(); i++) {
            Vector curr = _vertices.get(i);

            total += Vector.crossProduct(prev, curr);

            prev = curr;
        }
        return total / 2.0;
    }

    @Override
    public double perimeter() {
        double total = 0;
        final int n = _vertices.size();
        if (n > 1) {
            Vector prev = _vertices.get(_vertices.size()-1);
            for (int i = 0; i < n; i++) {
                Vector next = _vertices.get(i);
                total += prev.distanceTo(next);

                prev = next;
            }
        }
        return total;
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
        int presize = intersections.size();
        for (LineSegment edge : edges()) {
            edge.intersect(otherGeom, prec, intersections);
        }
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
     * Returns the vertex list of this polygon. Note that changes in this list
     * affect the polygon.
     *
     * @return vertex list
     */
    public List<Vector> vertices() {
        return _vertices;
    }

    /**
     * Allows iteration over the edges of the polygon. Removals are not
     * permitted: modify the vertex list instead.
     *
     * @return iterable over the edges
     */
    public Iterable<LineSegment> edges() {
        return new Iterable<LineSegment>() {

            @Override
            public Iterator<LineSegment> iterator() {
                return new Iterator<LineSegment>() {

                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < edgeCount();
                    }

                    @Override
                    public LineSegment next() {
                        return edge(index++);
                    }

                    @Override
                    public void remove() {
                        Logger.getLogger(Polygon.class.getName()).log(Level.SEVERE, "Cannot remove edges from a Polygon. Remove the relevant vertices instead.");
                    }

                };
            }
        };
    }

    @Override
    public boolean contains(Vector point, double prec) {
        // check if the point is on top of an edge
        double absprec = Math.abs(prec);
        for (LineSegment edge : edges()) {
            if (edge.onBoundary(point, absprec)) {
                return prec >= 0;
            }
        }

        double totangle = 0;

        Vector dirPrev = Vector.subtract(vertex(-1), point);
        dirPrev.normalize();

        for (int i = 0; i < _vertices.size(); i++) {
            Vector dirCurr = Vector.subtract(vertex(i), point);
            dirCurr.normalize();
            totangle += dirPrev.computeSignedAngleTo(dirCurr, false, false);
            dirPrev = dirCurr;
        }

        // its either 0 or a multiple of 2 PI
        // 0  -> outside
        // !0 -> inside
        return !(-0.5 < totangle && totangle < 0.5);
    }

    /**
     * Speed-up of {@link #containsPoint} for convex polygons.
     *
     * @param point location to check containment for
     * @return whether the point lies inside or on the boundary
     */
    public boolean convexContainsPoint(Vector point) {
        return convexContainsPoint(point, DoubleUtil.EPS);
    }

    /**
     * Speed-up of {@link #containsPoint} for convex polygons.
     *
     * @param point location to check containment for
     * @param prec precision
     * @return whether the point lies inside or on the boundary
     */
    public boolean convexContainsPoint(Vector point, double prec) {
        double dirPrevX = vertex(-1).getX() - point.getX();
        double dirPrevY = vertex(-1).getY() - point.getY();

        double dirCurrX = vertex(0).getX() - point.getX();
        double dirCurrY = vertex(0).getY() - point.getY();

        double cp = Vector.crossProduct(dirPrevX, dirPrevY, dirCurrX, dirCurrY);
        double sig = Math.signum(cp);

        double absprec = Math.abs(prec);

        if (DoubleUtil.close(cp, 0, absprec)) {
            // either vertex is on line segment (inside if prec positive) or in parallel with it (outside)
            if (Vector.dotProduct(dirPrevX, dirPrevY, dirCurrX, dirCurrY) < absprec) {
                return prec >= 0;
            } else {
                return false;
            }
        } else {
            for (int i = 1; i < _vertices.size(); i++) {
                dirPrevX = dirCurrX;
                dirPrevY = dirCurrY;

                dirCurrX = vertex(i).getX() - point.getX();
                dirCurrY = vertex(i).getY() - point.getY();

                cp = Vector.crossProduct(dirPrevX, dirPrevY, dirCurrX, dirCurrY);
                double s = Math.signum(cp);
                if (DoubleUtil.close(cp, 0, absprec)) {
                    // either vertex is on line segment (inside if prec positive) or in parallel with it (outside)
                    if (Vector.dotProduct(dirPrevX, dirPrevY, dirCurrX, dirCurrY) < absprec) {
                        return prec >= 0;
                    } else {
                        return false;
                    }
                } else if (DoubleUtil.close(sig, s)) {
                    // continue, same sign
                } else {
                    // opposite signs
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Computes the centroid of the polygon, assuming a planar polygon. Returns
     * null if the polygon has no vertices.
     *
     * @return new vector representing the centroid
     */
    public Vector centroid() {
        if (vertexCount() == 0) {
            return null;
        } else if (vertexCount() == 1) {
            return vertex(0).clone();
        }

        double A = 0;
        double Cx = 0;
        double Cy = 0;

        Vector p = vertex(-1);
        Vector q;
        for (int i = 0; i < vertexCount(); i++) {
            q = vertex(i);
            double cross = Vector.crossProduct(p, q);
            A += cross;
            Cx += (p.getX() + q.getX()) * cross;
            Cy += (p.getY() + q.getY()) * cross;
            p = q;
        }
        A *= 3;
        Cx = Cx / A;
        Cy = Cy / A;
        return new Vector(Cx, Cy);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void translate(double deltaX, double deltaY) {
        for (Vector v : _vertices) {
            v.translate(deltaX, deltaY);
        }
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        for (Vector v : _vertices) {
            v.rotate(counterclockwiseangle);
        }
    }

    @Override
    public void scale(double factorX, double factorY) {
        for (Vector v : _vertices) {
            v.scale(factorX, factorY);
        }
    }

    /**
     * Appends a vertex to this polygon.
     *
     * @param vertex point to append
     */
    public void addVertex(Vector vertex) {
        _vertices.add(vertex);
    }

    /**
     * Inserts a vertex at the specified index. Vertices at or after this index
     * are shifted. Note that the index is treated circularly.
     *
     * @param index index for the new vertex
     * @param vertex point to insert
     */
    public void addVertex(int index, Vector vertex) {
        _vertices.add(index(index), vertex);
    }

    /**
     * Replaces the vertex at the specified index. Note that the index is
     * treated circularly.
     *
     * @param index index of the vertex to be replaced
     * @param vertex the new position for the vertex
     */
    public void replaceVertex(int index, Vector vertex) {
        _vertices.set(index(index), vertex);
    }

    /**
     * Removes the vertex at the specified index. Note that the index is treated
     * circularly.
     *
     * @param index index of the vertex to be removed
     * @return the removed vertex
     */
    public Vector removeVertex(int index) {
        return _vertices.remove(index(index));
    }

    /**
     * Removes the specified vertex from the polygon.
     *
     * @param vertex point to be removed
     */
    public void removeVertex(Vector vertex) {
        _vertices.remove(vertex);
    }

    @Override
    public void reverse() {
        Collections.reverse(_vertices);
    }

    /**
     * Removes any vertices that do not contribute to the shape of this polygon
     * with precision DoubleUtil.EPS. Both duplicates and collinearities are
     * removed.
     */
    public void minimize() {
        minimize(DoubleUtil.EPS, DoubleUtil.EPS);
    }

    /**
     * Removes any vertices that do not contribute to the shape of this polygon
     * with given precisions. Both duplicates and collinearities are removed.
     *
     * @param distanceprecision precision used for eliminating duplicate
     * vertices
     * @param angleprecision precision used to compare angles of consecutive
     * edges
     */
    public void minimize(double distanceprecision, double angleprecision) {
        // first, remove all zero-length edges
        {
            int i = 0;
            while (i < _vertices.size() && _vertices.size() > 1) {

                if (vertex(i).isApproximately(vertex(i + 1), distanceprecision)) {
                    _vertices.remove(i + 1);
                } else {
                    i++;
                }
            }
        }
        // second, merge all adjacent aligned edges
        {
            Vector curr = vertex(0);
            Vector dprev = Vector.subtract(curr, vertex(-1));
            dprev.normalize();

            int i = 0;
            while (i < _vertices.size() && _vertices.size() > 1) {

                Vector next = vertex(i + 1);

                Vector dnext = Vector.subtract(next, curr);
                dnext.normalize();

                if (Math.abs(dnext.computeSignedAngleTo(dprev, false, false)) < angleprecision) {
                    _vertices.remove(i);
                } else {
                    i++;
                }
                dprev = dnext;
                curr = next;
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POLYGON;
    }

    @Override
    public Polygon clone() {
        List<Vector> cloned = new ArrayList();
        for (Vector vertex : _vertices) {
            cloned.add(vertex.clone());
        }
        return new Polygon(cloned);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _vertices.size() + "]";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private int index(int index) {
        if (_vertices.size() == 0) {
            return index;
        }
        while (index < 0) {
            index += _vertices.size();
        }
        return index % _vertices.size();
    }
    //</editor-fold>
}
