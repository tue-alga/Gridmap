/*
 * GeometryCore library   
 * Copyright (C) 2019   Wouter Meulemans (w.meulemans@tue.nl)
 * 
 * Licensed under GNU GPL v3. See provided license documents (license.txt and gpl-3.0.txt) for more information.
 */
package nl.tue.geometrycore.algorithms.delaunay;

import java.util.Iterator;
import java.util.List;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;
import nl.tue.geometrycore.util.Pair;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SimpleDelaunayTriangulation<TVector extends Vector> {

    private final List<TVector> points;
    private final DelaunayTriangulation<IGraph, LineSegment, IVertex, IEdge> dt;
    private final IGraph graph;

    public SimpleDelaunayTriangulation(List<TVector> points) {
        this.points = points;
        this.graph = new IGraph();
        this.dt = new DelaunayTriangulation<>(graph, (LineSegment geometry) -> geometry);
    }

    public void run() {
        this.graph.clear();
        for (TVector p : points) {
            IVertex v = graph.addVertex(p);
            v.original = p;
        }
        dt.run();
    }

    public Iterable<Pair<TVector, TVector>> edges() {
        Iterator<IEdge> it = graph.getEdges().iterator();
        return () -> {
            return new Iterator() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Object next() {
                    IEdge e = it.next();
                    return new Pair(e.getStart().original, e.getEnd().original);
                }
            };
        };
    }

    private class IGraph extends SimpleGraph<LineSegment, IVertex, IEdge> {

        @Override
        public IVertex createVertex(double x, double y) {
            return new IVertex(x, y);
        }

        @Override
        public IEdge createEdge() {
            return new IEdge();
        }

    }

    private class IVertex extends SimpleVertex<LineSegment, IVertex, IEdge> {

        private TVector original;

        public IVertex(double x, double y) {
            super(x, y);
        }

    }

    private class IEdge extends SimpleEdge<LineSegment, IVertex, IEdge> {

    }
}
