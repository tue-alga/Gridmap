/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.mix;

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
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;

/**
 * This class essentially represents a polygon, but edges need not be line
 * segments.
 *
 * @param <TEdge> class of edges for this geometry
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class GeometryCycle<TEdge extends OrientedGeometry<TEdge>> extends CyclicGeometry<GeometryCycle<TEdge>> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final List<TEdge> _edges;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Constructs an empty cycle.
     */
    public GeometryCycle() {
        _edges = new ArrayList();
    }

    /**
     * Constructs a geometry cycle consisting of the specified edges.
     *
     * @param edges edges of the desired cycle
     */
    public GeometryCycle(List<? extends TEdge> edges) {
        _edges = (List) edges;
    }

    /**
     * Creates a geometry cycle consisting of the specified edges.
     *
     * @param edges edges of the desired cycle
     */
    public GeometryCycle(TEdge... edges) {
        this();
        _edges.addAll(Arrays.asList(edges));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    /**
     * Allows a forward iteration over the edges.
     *
     * @return iterable over the edges
     */
    public List<TEdge> edges() {
        return _edges;
    }

    /**
     * Returns the edge at the provided index. Index is treated circularly.
     *
     * @param index index of the desired edge
     * @return the edge at index
     */
    public TEdge edge(int index) {
        final int n = _edges.size();
        while (index < 0) {
            index += n;
        }
        return _edges.get(index % n);
    }

    /**
     * Returns the number of edges in this geometry.
     *
     * @return number of edges
     */
    public int edgeCount() {
        return _edges.size();
    }

    /**
     * Returns the vertex at the provided index. Index is treated circularly.
     *
     * @param index index of the desired vertex
     * @return the vertex at index
     */
    public Vector vertex(int index) {
        final int n = _edges.size();
        while (index < 0) {
            index += n;
        }
        return _edges.get(index % n).getStart();
    }

    /**
     * Returns the number of vertices in this geometry.
     *
     * @return number of vertices
     */
    public int vertexCount() {
        return _edges.size();
    }

    /**
     * Allows a forward iteration over the vertices.
     *
     * @return iterable over the vertices
     */
    public Iterable<Vector> vertices() {
        return new Iterable<Vector>() {

            @Override
            public Iterator<Vector> iterator() {
                return new Iterator<Vector>() {

                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < vertexCount();
                    }

                    @Override
                    public Vector next() {
                        return vertex(index++);
                    }

                    @Override
                    public void remove() {
                        Logger.getLogger(GeometryCycle.class.getName()).log(Level.SEVERE, "Cannot remove vertices from a GeometryCycle. Remove the relevant edges instead.");
                    }

                };
            }
        };
    }

    @Override
    public boolean onBoundary(Vector point, double prec) {
        for (TEdge edge : _edges) {
            if (edge.onBoundary(point, prec)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Vector closestPoint(Vector point) {
        Vector result = null;
        double distance = Double.POSITIVE_INFINITY;
        for (TEdge part : _edges) {
            Vector closest = part.closestPoint(point);
            double d = closest.squaredDistanceTo(point);
            if (d < distance) {
                result = closest;
                distance = d;
            }
        }
        return result;
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
        // TODO: make use that it's really only the vertices that can coincide...
        // here and for polyline/gon, rectangle and string
        int presize = intersections.size();
        for (int i = 0; i < edgeCount(); i++) {
            TEdge edge = edge(i);
            intersections.addAll(edge.intersect(otherGeom, prec));
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

    @Override
    public double perimeter() {
        double perimeter = 0;
        for (TEdge edge : _edges) {
            perimeter += edge.perimeter();
        }
        return perimeter;
    }

    @Override
    public double areaSigned() {
        throw new UnsupportedOperationException("NYI");
    }
    
    @Override
    public boolean contains(Vector point, double prec) {
        throw new UnsupportedOperationException("NYI");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void translate(double deltaX, double deltaY) {
        for (TEdge edge : _edges) {
            edge.translate(deltaX, deltaY);
        }
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        for (TEdge edge : _edges) {
            edge.rotate(counterclockwiseangle);
        }
    }

    @Override
    public void scale(double factorX, double factorY) {
        for (TEdge edge : _edges) {
            edge.scale(factorX, factorY);
        }
    }
    
    @Override
    public void reverse() {
        Collections.reverse(_edges);
        for (TEdge edge : _edges) {
            edge.reverse();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.GEOMETRYCYCLE;
    }
    
    @Override
    public GeometryCycle<TEdge> clone() {
        List<TEdge> cloned = new ArrayList();
        for (TEdge edge : _edges) {
            cloned.add(edge.clone());
        }
        return new GeometryCycle(cloned);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _edges.size() + "]";
    }
    //</editor-fold>
}
