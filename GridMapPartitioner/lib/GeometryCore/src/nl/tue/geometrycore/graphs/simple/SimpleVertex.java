/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.graphs.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;

/**
 * Class to represent a vertex in a simple edge-list implementation of a graph.
 *
 * @param <TGeom> The geometry of an edge, must inherit from OrientedGeometry
 * @param <TVertex> The class of a vertex
 * @param <TEdge> The class of an edge
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class SimpleVertex<TGeom extends OrientedGeometry<TGeom>, TVertex extends SimpleVertex<TGeom, TVertex, TEdge>, TEdge extends SimpleEdge<TGeom, TVertex, TEdge>> extends Vector {

    final List<TEdge> _edges;
    int _graphIndex; // package-private

    public SimpleVertex(Vector position) {
        this(position.getX(), position.getY());
    }

    public SimpleVertex(double x, double y) {
        super(x, y);
        _graphIndex = -1;
        _edges = new ArrayList();
    }
    
    public int getGraphIndex() {
        return _graphIndex;
    }

    public List<TEdge> getEdges() {
        return _edges;
    }

    public boolean isNeighborOf(TVertex vertex) {
        return getEdgeTo(vertex) != null;
    }

    public TEdge getEdgeTo(TVertex vertex) {
        for (TEdge e : _edges) {
            if (e.getOtherVertex(this) == vertex) {
                return e;
            }
        }
        return null;
    }

    @Override
    public void set(double x, double y) {
        super.set(x, y);
        updateIncidentEdges();
    }

    @Override
    public void set(Vector v) {
        super.set(v);
        updateIncidentEdges();
    }

    @Override
    public void setY(double y) {
        super.setY(y);
        updateIncidentEdges();
    }

    @Override
    public void setX(double x) {
        super.setX(x);
        updateIncidentEdges();
    }
    
    @Override
    public void rotate90DegreesCounterclockwise() {
        super.rotate90DegreesCounterclockwise(); 
        updateIncidentEdges();
    }

    @Override
    public void rotate90DegreesClockwise() {
        super.rotate90DegreesClockwise(); 
        updateIncidentEdges();
    }

    @Override
    public void invert() {
        super.invert(); 
        updateIncidentEdges();
    }

    @Override
    public void normalize() {
        super.normalize(); 
        updateIncidentEdges();
    }

    @Override
    public void scale(double factorX, double factorY) {
        super.scale(factorX, factorY); 
        updateIncidentEdges();
    }

    @Override
    public void rotate(double counterclockwiseangle) {
        super.rotate(counterclockwiseangle); 
        updateIncidentEdges();
    }

    @Override
    public void translate(double deltaX, double deltaY) {
        super.translate(deltaX, deltaY); 
        updateIncidentEdges();
    }

    public void updateIncidentEdges() {
        for (TEdge edge : _edges) {
            if (edge._start == this) {
                edge._geometry.updateStart(this);
            } else {
                edge._geometry.updateEnd(this);
            }
        }
    }

    public void sortEdges(boolean counterclockwise) {
        sortEdges(counterclockwise, Vector.right());
    }

    public void sortEdges(boolean counterclockwise, Vector reference) {
        final Map<TEdge, Double> angles = new HashMap<TEdge, Double>();

        for (TEdge e : _edges) {
            Vector dir;
            if (e._start == this) {
                dir = e.getGeometry().getStartTangent();
            } else {
                dir = e.getGeometry().getEndTangent();
                dir.invert();
            }

            double angle;
            if (counterclockwise) {
                angle = reference.computeCounterClockwiseAngleTo(dir, false, false);
            } else {
                angle = reference.computeClockwiseAngleTo(dir, false, false);
            }

            angles.put(e, angle);
        }

        Collections.sort(_edges, new Comparator<TEdge>() {

            @Override
            public int compare(TEdge o1, TEdge o2) {
                return Double.compare(angles.get(o1), angles.get(o2));
            }
        });

    }

    public int getDegree() {
        return _edges.size();
    }

    
    public Iterable<TVertex> getNeighbors() {
        final SimpleVertex thisVertex = this;
        return new Iterable<TVertex>() {

            @Override
            public Iterator<TVertex> iterator() {
                return new Iterator<TVertex>() {

                    Iterator<TEdge> _it = _edges.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return _it.hasNext();
                    }

                    @Override
                    public TVertex next() {
                        TEdge edge = _it.next();
                        return edge.getOtherVertex(thisVertex);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Cannot remove neighbors this way. Call the appropriate method on the graph instead.");
                    }
                };
            }
        };
    }
}
