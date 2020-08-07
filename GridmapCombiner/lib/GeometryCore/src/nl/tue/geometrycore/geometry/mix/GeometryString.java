/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.geometry.mix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;

/**
 * This geometry represents essentially a polyline, but edges need not be line
 * segments.
 *
 * @param <TEdge> class of edges for this geometry
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class GeometryString<TEdge extends OrientedGeometry<TEdge>> extends OrientedGeometry<GeometryString<TEdge>> {

    //<editor-fold defaultstate="collapsed" desc="FIELDS">
    private final List<TEdge> _edges;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
    /**
     * Creates an empty geometry string.
     */
    public GeometryString() {
        _edges = new ArrayList();
    }

    /**
     * Creates a geometry string from the provided edges.
     *
     * @param edges edges of the desired string
     */
    public GeometryString(List<? extends TEdge> edges) {
        _edges = (List) edges;
    }

    /**
     * Creates a geometry string from the provided edges.
     *
     * @param edges edges of the desired string
     */
    public GeometryString(TEdge... edges) {
        this();
        _edges.addAll(Arrays.asList(edges));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="QUERIES">
    @Override
    public Vector getStart() {
        if (_edges.isEmpty()) {
            return null;
        } else {
            return _edges.get(0).getStart();
        }
    }

    @Override
    public Vector getEnd() {
        if (_edges.isEmpty()) {
            return null;
        } else {
            return _edges.get(0).getStart();
        }
    }

    /**
     * Allows a forward iteration over the edges.
     *
     * @return iterable over the edges
     */
    public Iterable<TEdge> edges() {
        return _edges;
    }

    /**
     * Returns the edge at the provided index.
     *
     * @param index index of the desired edge
     * @return the edge at index
     */
    public TEdge edge(int index) {
        assert 0 <= index && index < _edges.size();

        return _edges.get(index);
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
     * Returns the vertex at the provided index.
     *
     * @param index index of the desired vertex
     * @return the vertex at index
     */
    public Vector vertex(int index) {
        assert 0 <= index;

        final int n = _edges.size();
        if (index < n) {
            return _edges.get(index).getStart();
        } else {
            assert index == n;
            return _edges.get(n).getEnd();
        }
    }

    /**
     * Returns the number of vertices in this geometry.
     *
     * @return number of vertices
     */
    public int vertexCount() {
        return _edges.size() + 1;
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
                        Logger.getLogger(GeometryString.class.getName()).log(Level.SEVERE, "Cannot remove vertices from a GeometryString. Remove the relevant edges instead.");
                    }
                };
            }
        };
    }

    @Override
    public Vector getStartTangent() {
        final int n = _edges.size();
        switch (n) {
            case 0:
                return null;
            default:
                return _edges.get(0).getStartTangent();
        }
    }

    @Override
    public Vector getEndTangent() {
        final int n = _edges.size();
        switch (n) {
            case 0:
                return null;
            default:
                return _edges.get(n - 1).getEndTangent();
        }
    }

    @Override
    public double areaSigned() {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public void intersect(BaseGeometry otherGeom, double prec, List<BaseGeometry> intersections) {
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
    public double perimeter() {
        double len = 0;
        for (TEdge edge : _edges) {
            len += edge.perimeter();
        }
        return len;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="METHODS">
    @Override
    public void updateEndpoints(Vector start, Vector end) {
        final int n = _edges.size();
        switch (n) {
            case 0:
                // do nothing
                break;
            case 1:
                _edges.get(0).updateEndpoints(start, end);
                break;
            default:
                _edges.get(0).updateStart(start);
                _edges.get(n - 1).updateEnd(end);
                break;
        }
    }

    @Override
    public void reverse() {
        int i = 0;
        int j = _edges.size() - 1;
        while (i < j) {
            final TEdge edgeI = _edges.get(i);
            edgeI.reverse();
            final TEdge edgeJ = _edges.get(j);
            edgeJ.reverse();

            _edges.set(i, edgeJ);
            _edges.set(j, edgeI);

            i++;
            j--;
        }

        if (i == j) {
            _edges.get(i).reverse();
        }
    }

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
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UTIL">
    @Override
    public GeometryType getGeometryType() {
        return GeometryType.GEOMETRYSTRING;
    }

    @Override
    public GeometryString<TEdge> clone() {
        List<TEdge> cloned = new ArrayList();
        for (TEdge edge : _edges) {
            cloned.add(edge.clone());
        }
        return new GeometryString(cloned);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + _edges.size() + "]";
    }
    //</editor-fold>
}
