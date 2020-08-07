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
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;

/**
 * A polygonal line, based on a vertex representation.
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class PolyLine extends OrientedGeometry<PolyLine> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final List<Vector> _vertices;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs an empty polygonal line.
     */
    public PolyLine() {
        _vertices = new ArrayList();
    }

    /**
     * Constructs a polygonal line from the provided vertices. Note that the
     * list is used to define the polyline.
     *
     * @param vertices corner points of the polyline
     */
    public PolyLine(List<Vector> vertices) {
        _vertices = vertices;
    }

    /**
     * Constructs a polygonal line from the provided vertices.
     *
     * @param vertices corner points of the polyline
     */
    public PolyLine(Vector... vertices) {
        this();
        _vertices.addAll(Arrays.asList(vertices));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    @Override
    public Vector getStart() {
        if (_vertices.isEmpty()) {
            return null;
        } else {
            return _vertices.get(0);

        }
    }

    @Override
    public Vector getEnd() {
        if (_vertices.isEmpty()) {
            return null;
        } else {
            return _vertices.get(_vertices.size() - 1);
        }
    }

    @Override
    public double areaSigned() {
        int n = _vertices.size();
        double area = Vector.crossProduct(_vertices.get(n - 1), _vertices.get(0));
        for (int i = 1; i < n; i++) {
            area += Vector.crossProduct(_vertices.get(i - 1), _vertices.get(i));
        }
        return area * 0.5;
    }

    @Override
    public Vector getStartTangent() {
        if (_vertices.size() < 2) {
            assert false : "Polyline has less than 2 vertices";
        }
        Vector direction = Vector.subtract(_vertices.get(1), _vertices.get(0));
        direction.normalize();
        return direction;
    }

    @Override
    public Vector getEndTangent() {
        if (_vertices.size() < 2) {
            assert false : "Polyline has less than 2 vertices";
            return null;
        }
        Vector direction = Vector.subtract(_vertices.get(_vertices.size() - 1), _vertices.get(_vertices.size() - 2));
        direction.normalize();
        return direction;
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

    /**
     * Returns the number of vertices defining this polygonal line.
     *
     * @return number of vertices
     */
    public int vertexCount() {
        return _vertices.size();
    }

    /**
     * Returns the vertex at the specified index.
     *
     * @param index position of the desired vertex
     * @return vertex at given index
     */
    public Vector vertex(int index) {
        return _vertices.get(index);
    }

    /**
     * Returns the first vertex of the polyline. Returns null if the number of
     * vertices is zero.
     *
     * @return startpoint
     */
    public Vector firstVertex() {
        if (_vertices.isEmpty()) {
            return null;
        } else {
            return _vertices.get(0);
        }
    }

    /**
     * Returns the last vertex of the polyline. Returns null if the number of
     * vertices is zero.
     *
     * @return endpoint
     */
    public Vector lastVertex() {
        if (_vertices.isEmpty()) {
            return null;
        } else {
            return _vertices.get(_vertices.size() - 1);
        }
    }

    /**
     * Returns the number of edges of this polyline. This is equal to the number
     * of vertices minus one.
     *
     * @return edge count
     */
    public int edgeCount() {
        if (_vertices.size() == 0) {
            return 0;
        } else {
            return _vertices.size() - 1;
        }
    }

    /**
     * Constructs the edge between vertex at the specified index and the index
     * after. Value of index must lie in between 0 and the number of edges.
     *
     * @param index edge index
     * @return newly constructed edge
     */
    public LineSegment edge(int index) {
        return new LineSegment(_vertices.get(index), _vertices.get(index + 1));
    }

    /**
     * Constructs the first edge, or null if the number of vertices is at most
     * one.
     *
     * @return the first edge
     */
    public LineSegment firstEdge() {
        if (_vertices.size() <= 1) {
            return null;
        } else {
            return new LineSegment(_vertices.get(0), _vertices.get(1));
        }
    }

    /**
     * Constructs the last edge, or null if the number of vertices is at most
     * one.
     *
     * @return the last edge
     */
    public LineSegment lastEdge() {
        if (_vertices.size() <= 1) {
            return null;
        } else {
            return new LineSegment(_vertices.get(_vertices.size() - 2), _vertices.get(_vertices.size() - 1));
        }
    }

    @Override
    public double perimeter() {
        double total = 0;
        final int n = _vertices.size();
        if (n > 0) {
            Vector prev = _vertices.get(0);
            for (int i = 1; i < n; i++) {
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
     * Returns the vertex list of this polyline. Note that changes in this list
     * affect the polyline.
     *
     * @return vertex list
     */
    public List<Vector> vertices() {
        return _vertices;
    }

    /**
     * Allows iteration over the edges of the polyline. Removals are not
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
                        Logger.getLogger(PolyLine.class.getName()).log(Level.SEVERE, "Cannot remove edges from a PolyLine. Remove the relevant vertices instead.");
                    }

                };
            }
        };
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void updateEndpoints(Vector start, Vector end) {

        if (end == null && start == null) {
            return;
        }
        if (_vertices.isEmpty()) {
            return;
        }

        if (start != null) {
            _vertices.get(0).set(start);
        }
        if (end != null) {
            _vertices.get(_vertices.size() - 1).set(end);
        }
    }

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
     * Appends a vertex to this polyline.
     *
     * @param vertex point to append
     */
    public void addVertex(Vector vertex) {
        _vertices.add(vertex);
    }

    /**
     * Inserts a vertex at the specified index. Vertices at or after this index
     * are shifted.
     *
     * @param index index for the new vertex
     * @param vertex point to insert
     */
    public void addVertex(int index, Vector vertex) {
        _vertices.add(index, vertex);
    }

    /**
     * Replaces the vertex at the specified index.
     *
     * @param index index of the vertex to be replaced
     * @param vertex the new position for the vertex
     */
    public void replaceVertex(int index, Vector vertex) {
        _vertices.set(index, vertex);
    }

    /**
     * Removes the vertex at the specified index.
     *
     * @param index index of the vertex to be removed
     * @return the removed vertex
     */
    public Vector removeVertex(int index) {
        return _vertices.remove(index);
    }

    /**
     * Removes the specified vertex from the polyline.
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
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POLYLINE;
    }
    
    @Override
    public PolyLine clone() {
        List<Vector> cloned = new ArrayList();
        for (Vector vertex : _vertices) {
            cloned.add(vertex.clone());
        }
        return new PolyLine(cloned);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _vertices.size() + "]";
    }
    //</editor-fold>
}
